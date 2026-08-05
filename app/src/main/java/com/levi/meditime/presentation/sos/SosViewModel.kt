package com.levi.meditime.presentation.sos

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.core.common.Resource
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.domain.repository.EmergencyContactRepository
import com.levi.meditime.manager.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val emergencyRepository: EmergencyContactRepository,
    private val locationManager: LocationManager,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager
) : AndroidViewModel(application) {

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun triggerSos() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            
            if (!user.emailVerified || !user.phoneVerified) {
                _message.value = "Please verify your email and phone number to use SOS"
                return@launch
            }

            _isSending.value = true
            try {
                analyticsManager.logSosTriggered("Dashboard Button")
                val user = authRepository.currentUser.first() ?: return@launch
                val contacts = emergencyRepository.getAllEmergencyContacts().first().data ?: emptyList()
                val primaryContact = contacts.find { it.notifyOnMissedDose } // Assume primary logic
                
                if (primaryContact != null) {
                    val context = getApplication<Application>()
                    
                    // 1. Call (if permission granted)
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${primaryContact.phoneNumber}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(dialIntent)
                    }

                    // 2. Share Location (handled by locationManager, assuming it checks internal permission)
                    val location = locationManager.shareLiveLocation(user.uid)
                    
                    // 3. Send SMS (if permission granted)
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            context.getSystemService(SmsManager::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsManager.getDefault()
                        }

                        val locMsg = location?.let { " My location: https://www.google.com/maps/search/?api=1&query=${it.latitude},${it.longitude}" } ?: ""
                        smsManager.sendTextMessage(
                            primaryContact.phoneNumber,
                            null,
                            "EMERGENCY SOS from ${user.name}! I need help.$locMsg",
                            null,
                            null
                        )
                    }
                    
                    // 4. Start Foreground Location Service - PRD 1.3
                    val serviceIntent = Intent(context, com.levi.meditime.service.SosLocationService::class.java).apply {
                        putExtra("USER_ID", user.uid)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    _message.value = "SOS alerts triggered to ${primaryContact.name}"
                } else {
                    _message.value = "No emergency contact found"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    fun stopSos() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, com.levi.meditime.service.SosLocationService::class.java)
        context.stopService(serviceIntent)
        _message.value = "SOS alerts stopped"
    }

    fun clearMessage() {
        _message.value = null
    }
}
