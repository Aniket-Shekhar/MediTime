package com.levi.meditime.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.levi.meditime.manager.LocationManager
import com.levi.meditime.manager.PdfManager
import com.levi.meditime.manager.TtsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTtsManager(@ApplicationContext context: Context): TtsManager {
        return TtsManager(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): com.levi.meditime.core.util.ConnectivityObserver {
        return com.levi.meditime.core.util.ConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context, firestore: FirebaseFirestore): LocationManager {
        return LocationManager(context, firestore)
    }

    @Provides
    @Singleton
    fun providePdfManager(@ApplicationContext context: Context): PdfManager {
        return PdfManager(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "meditime_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
