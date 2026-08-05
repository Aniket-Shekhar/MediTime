package com.levi.meditime.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.model.User
import com.levi.meditime.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.levi.meditime.manager.AuditLogManager
import com.levi.meditime.core.util.SecurityUtils

import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.mapper.toDomain
import com.levi.meditime.data.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dbRef: DatabaseReference,
    private val storage: FirebaseStorage,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val dao: MedicineDao,
    private val auditLogManager: AuditLogManager,
    private val alarmHelper: com.levi.meditime.manager.AlarmManagerHelper
) : UserRepository {

    override suspend fun saveUser(user: User): Resource<Unit> {
        return try {
            dao.insertUser(user.toEntity())
            dbRef.child("users").child(user.uid).child("profile").setValue(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not save user data")
        }
    }

    override fun getUser(uid: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        
        // Collect local user
        val localJob = launch {
            dao.getUserById(uid).collect { entity ->
                entity?.let { trySend(Resource.Success(it.toDomain())) }
            }
        }
        
        dbRef.child("users").child(uid).child("profile")
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    launch {
                        dao.insertUser(user.toEntity())
                    }
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("User not found"))
                }
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.message ?: "An error occurred"))
            }
        awaitClose { localJob.cancel() }
    }

    override suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            // Check Duplicate Phone
            if (user.phoneNumber.isNotBlank()) {
                val snapshot = dbRef.child("users").get().await()
                val duplicate = snapshot.children.any { 
                    it.key != user.uid && it.child("profile/phoneNumber").value == user.phoneNumber 
                }
                if (duplicate) return Resource.Error("Phone number already in use")
            }

            dao.insertUser(user.toEntity())
            val updates = mapOf<String, Any>(
                "users/${user.uid}/profile" to user
            )
            dbRef.updateChildren(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not update user")
        }
    }

    override suspend fun deleteUser(uid: String): Resource<Unit> {
        return try {
            // 1. Delete from Storage
            try {
                storage.reference.child("profile_pictures/$uid/profile.jpg").delete().await()
            } catch (e: Exception) {
                // Ignore if file doesn't exist
            }

            // 2. Delete from Firestore
            firestore.collection("users").document(uid).delete().await()
            firestore.collection("email_otps").document(uid).delete().await()
            firestore.collection("audit_logs").document(uid).delete().await()

            // 3. Delete from Realtime Database
            dbRef.child("users").child(uid).removeValue().await()
            dbRef.child("medicines").child(uid).removeValue().await()
            dbRef.child("reminders").child(uid).removeValue().await()
            dbRef.child("history").child(uid).removeValue().await()
            dbRef.child("family").child(uid).removeValue().await()

            // 4. Cancel all local Alarms
            val reminders = dao.getAllReminders().first().map { it.toDomain() }
            alarmHelper.cancelAll(reminders)

            // 5. Delete from Room (local)
            dao.deleteAllMedicines()
            dao.deleteAllReminders()
            dao.deleteAllHistory()
            dao.deleteAllFamilyMembers()

            // 6. Delete Auth Account (Must be last)
            auth.currentUser?.delete()?.await()

            auditLogManager.logEvent(AuditLogManager.ACTION_DELETE_ACCOUNT, uid)
            Resource.Success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Resource.Error("REAUTH_REQUIRED") // PRD 1.2: Specific code for UI to catch
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error(e.message ?: "Could not fully delete user account.")
        }
    }

    override suspend fun uploadProfileImage(uid: String, imageUri: Uri): Resource<String> = withContext(Dispatchers.IO) {
        try {
            // PRD 2: Check file size BEFORE loading into memory (OOM Prevention)
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(imageUri, "r")
            val fileSize = parcelFileDescriptor?.statSize ?: 0L
            parcelFileDescriptor?.close()

            if (fileSize > 5 * 1024 * 1024) {
                return@withContext Resource.Error("Image size exceeds 5MB limit.")
            }

            // PRD 1.2: Path changed to profile_pictures/{uid}/profile.jpg
            val ref = storage.reference.child("profile_pictures/$uid/profile.jpg")
            
            // PRD 1.2: Compression Logic
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Resource.Error("Could not access the selected image.")
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                return@withContext Resource.Error("Could not process image format. Please try another image.")
            }

            val outputStream = ByteArrayOutputStream()
            
            // 70% quality for optimization
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val data = outputStream.toByteArray()

            if (data.isEmpty()) {
                return@withContext Resource.Error("Could not process image. Please try another one.")
            }

            // PRD 1.2: Await upload before getting URL
            try {
                ref.putBytes(data).await()
                val url = ref.downloadUrl.await().toString()
                
                auditLogManager.logEvent(AuditLogManager.ACTION_PRESCRIPTION_UPLOAD, uid)
                Resource.Success(url)
            } catch (e: com.google.firebase.storage.StorageException) {
                val userMessage = when (e.errorCode) {
                    com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND -> "Upload destination not found."
                    com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED -> "Permission denied for upload. Please check your account."
                    com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED -> "Storage limit reached. Please contact support."
                    else -> "Image upload failed. Please check your internet connection."
                }
                Resource.Error(userMessage)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(SecurityUtils.scrubPii(e.message), e)
            )
            Resource.Error("Image upload failed. Please try again later.")
        }
    }
}
