package com.levi.meditime.di

import android.content.Context
import androidx.room.Room
import com.levi.meditime.data.local.database.MediTimeDatabase
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.manager.DatabaseKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager
    ): MediTimeDatabase {
        System.loadLibrary("sqlcipher")
        
        val dbFile = context.getDatabasePath("meditime_db")
        val passphrase = keyManager.getOrCreateKey()
        
        // PRD 1.3: Migration logic for SQLCipher
        if (dbFile.exists()) {
            ensureDatabaseEncrypted(context, dbFile, passphrase)
        }

        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            MediTimeDatabase::class.java,
            "meditime_db"
        )
            .openHelperFactory(factory)
            .addMigrations(
                MediTimeDatabase.MIGRATION_10_11, 
                MediTimeDatabase.MIGRATION_11_12,
                MediTimeDatabase.MIGRATION_12_13
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    private fun ensureDatabaseEncrypted(context: Context, dbFile: File, passphrase: ByteArray) {
        try {
            // Check if already encrypted with the correct key
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                passphrase,
                null,
                SQLiteDatabase.OPEN_READONLY,
                null
            ).close()
            return // Already encrypted with correct key
        } catch (e: Exception) {
            android.util.Log.d("DatabaseModule", "DB not accessible with current key, attempting migration...")
        }

        // Migration Path: Plaintext -> Encrypted OR Old Key -> New Key
        val tempDbFile = File(context.cacheDir, "meditime_db_migration")
        if (tempDbFile.exists()) tempDbFile.delete()

        val oldKeys = listOf(
            "".toByteArray(), // Plaintext
            "meditime_secure_key".toByteArray() // Legacy hardcoded key
        )

        var migrationSuccess = false
        for (oldKey in oldKeys) {
            try {
                val oldDb = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    oldKey,
                    null,
                    SQLiteDatabase.OPEN_READWRITE,
                    null
                )

                // Export to encrypted temporary file
                // PRD Fix: Use hex key string for SQLCipher ATTACH for maximum reliability
                val hexPassphrase = passphrase.joinToString("") { "%02x".format(it) }
                oldDb.rawExecSQL("ATTACH DATABASE '${tempDbFile.absolutePath}' AS encrypted KEY \"x'$hexPassphrase'\"")
                oldDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                oldDb.rawExecSQL("DETACH DATABASE encrypted")
                oldDb.close()

                // Replace old DB with new one
                if (dbFile.delete()) {
                    if (!tempDbFile.renameTo(dbFile)) {
                        android.util.Log.e("DatabaseModule", "Failed to rename temp database file.")
                        migrationSuccess = false
                    } else {
                        migrationSuccess = true
                        android.util.Log.i("DatabaseModule", "Database migration/encryption successful.")
                        break
                    }
                } else {
                    android.util.Log.e("DatabaseModule", "Failed to delete old database file.")
                    migrationSuccess = false
                }
            } catch (e: Exception) {
                android.util.Log.w("DatabaseModule", "Migration attempt failed for key: ${if (oldKey.isEmpty()) "plaintext" else "legacy"}. Error: ${e.message}")
            }
        }

        if (!migrationSuccess) {
            android.util.Log.e("DatabaseModule", "All migration attempts failed. Falling back to destructive migration if Room permits.")
        }
    }

    @Provides
    fun provideMedicineDao(db: MediTimeDatabase): MedicineDao {
        return db.medicineDao()
    }
}
