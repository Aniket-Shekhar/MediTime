package com.levi.meditime.manager

import android.content.SharedPreferences
import android.util.Log
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseKeyManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val DB_KEY = "meditime_db_encryption_key"
        private const val KEY_SIZE = 32
    }

    fun getOrCreateKey(): ByteArray {
        val encodedKey = sharedPreferences.getString(DB_KEY, null)
        return if (encodedKey == null) {
            val key = generateRandomKey()
            val newEncodedKey = android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT)
            sharedPreferences.edit().putString(DB_KEY, newEncodedKey).apply()
            Log.d("DatabaseKeyManager", "New database encryption key generated and stored.")
            key
        } else {
            try {
                android.util.Base64.decode(encodedKey, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("DatabaseKeyManager", "Failed to decode database key, regenerating...", e)
                val key = generateRandomKey()
                val newEncodedKey = android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT)
                sharedPreferences.edit().putString(DB_KEY, newEncodedKey).apply()
                key
            }
        }
    }

    private fun generateRandomKey(): ByteArray {
        val key = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(key)
        return key
    }
}
