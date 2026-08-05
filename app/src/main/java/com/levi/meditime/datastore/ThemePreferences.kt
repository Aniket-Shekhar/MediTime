package com.levi.meditime.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

data class UserSettings(
    val themeMode: ThemeMode,
    val remindersEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val lowStockNotificationsEnabled: Boolean,
    val familyNotificationsEnabled: Boolean,
    val missedReminderDelayMinutes: Int = 15,
    val autoCallEmergencyEnabled: Boolean = false,
    val accessibilityMode: Boolean = false,
    val language: String = "en",
    val voiceAlarmEnabled: Boolean = true,
    val voiceType: String = "Female", // Male, Female
    val voiceVolume: Float = 1.0f,
    val appLockEnabled: Boolean = false,
    val appPin: String? = null, // PRD 1.3: Biometric PIN Fallback
    val allowScreenshots: Boolean = true,
    val reminderSound: String = "Default",
    val snoozeDurationMinutes: Int = 10,
    val missedReminderAlertEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val notifBannerDismissed: Boolean = false
)

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val REMINDERS_KEY = booleanPreferencesKey("reminders_enabled")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val LOW_STOCK_KEY = booleanPreferencesKey("low_stock_enabled")
        private val FAMILY_NOTIF_KEY = booleanPreferencesKey("family_notif_enabled")
        private val MISSED_DELAY_KEY = stringPreferencesKey("missed_delay")
        private val AUTO_CALL_KEY = booleanPreferencesKey("auto_call_emergency")
        private val ACCESSIBILITY_KEY = booleanPreferencesKey("accessibility_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val VOICE_ALARM_KEY = booleanPreferencesKey("voice_alarm_enabled")
        private val VOICE_TYPE_KEY = stringPreferencesKey("voice_type")
        private val VOICE_VOLUME_KEY = floatPreferencesKey("voice_volume")
        private val APP_LOCK_KEY = booleanPreferencesKey("app_lock_enabled")
        private val APP_PIN_KEY = stringPreferencesKey("app_pin")
        private val SCREENSHOT_BLOCK_KEY = booleanPreferencesKey("screenshot_block_enabled")
        private val REMINDER_SOUND_KEY = stringPreferencesKey("reminder_sound")
        private val SNOOZE_DURATION_KEY = intPreferencesKey("snooze_duration")
        private val MISSED_ALERTS_KEY = booleanPreferencesKey("missed_alerts_enabled")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val NOTIF_BANNER_DISMISSED_KEY = booleanPreferencesKey("notif_banner_dismissed")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            themeMode = ThemeMode.valueOf(preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name),
            remindersEnabled = preferences[REMINDERS_KEY] ?: true,
            vibrationEnabled = preferences[VIBRATION_KEY] ?: true,
            lowStockNotificationsEnabled = preferences[LOW_STOCK_KEY] ?: true,
            familyNotificationsEnabled = preferences[FAMILY_NOTIF_KEY] ?: true,
            missedReminderDelayMinutes = (preferences[MISSED_DELAY_KEY] ?: "15").toInt(),
            autoCallEmergencyEnabled = preferences[AUTO_CALL_KEY] ?: false,
            accessibilityMode = preferences[ACCESSIBILITY_KEY] ?: false,
            language = preferences[LANGUAGE_KEY] ?: "en",
            voiceAlarmEnabled = preferences[VOICE_ALARM_KEY] ?: true,
            voiceType = preferences[VOICE_TYPE_KEY] ?: "Female",
            voiceVolume = preferences[VOICE_VOLUME_KEY] ?: 1.0f,
            appLockEnabled = preferences[APP_LOCK_KEY] ?: false,
            appPin = preferences[APP_PIN_KEY],
            allowScreenshots = preferences[SCREENSHOT_BLOCK_KEY] ?: true,
            reminderSound = preferences[REMINDER_SOUND_KEY] ?: "Default",
            snoozeDurationMinutes = preferences[SNOOZE_DURATION_KEY] ?: 10,
            missedReminderAlertEnabled = preferences[MISSED_ALERTS_KEY] ?: true,
            isOnboardingCompleted = preferences[ONBOARDING_COMPLETED_KEY] ?: false,
            notifBannerDismissed = preferences[NOTIF_BANNER_DISMISSED_KEY] ?: false
        )
    }

    val themeMode: Flow<ThemeMode> = userSettings.map { it.themeMode }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
    }

    suspend fun setNotifBannerDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[NOTIF_BANNER_DISMISSED_KEY] = dismissed }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDERS_KEY] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_KEY] = enabled }
    }

    suspend fun setLowStockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[LOW_STOCK_KEY] = enabled }
    }

    suspend fun setFamilyNotifEnabled(enabled: Boolean) {
        context.dataStore.edit { it[FAMILY_NOTIF_KEY] = enabled }
    }

    suspend fun setMissedReminderDelay(minutes: Int) {
        context.dataStore.edit { it[MISSED_DELAY_KEY] = minutes.toString() }
    }

    suspend fun setAutoCallEmergency(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CALL_KEY] = enabled }
    }

    suspend fun setAccessibilityMode(enabled: Boolean) {
        context.dataStore.edit { it[ACCESSIBILITY_KEY] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    suspend fun setVoiceAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_ALARM_KEY] = enabled }
    }

    suspend fun setVoiceType(type: String) {
        context.dataStore.edit { it[VOICE_TYPE_KEY] = type }
    }

    suspend fun setVoiceVolume(volume: Float) {
        context.dataStore.edit { it[VOICE_VOLUME_KEY] = volume }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[APP_LOCK_KEY] = enabled }
    }

    suspend fun setAppPin(pin: String?) {
        context.dataStore.edit { prefs ->
            if (pin == null) prefs.remove(APP_PIN_KEY)
            else prefs[APP_PIN_KEY] = pin
        }
    }

    suspend fun setAllowScreenshots(enabled: Boolean) {
        context.dataStore.edit { it[SCREENSHOT_BLOCK_KEY] = enabled }
    }

    suspend fun setReminderSound(sound: String) {
        context.dataStore.edit { it[REMINDER_SOUND_KEY] = sound }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_DURATION_KEY] = minutes }
    }

    suspend fun setMissedReminderAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[MISSED_ALERTS_KEY] = enabled }
    }
}
