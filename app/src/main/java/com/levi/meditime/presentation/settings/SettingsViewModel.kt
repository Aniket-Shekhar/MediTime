package com.levi.meditime.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.datastore.ThemeMode
import com.levi.meditime.datastore.ThemePreferences
import com.levi.meditime.datastore.UserSettings
import com.levi.meditime.manager.ReminderNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val repository: com.levi.meditime.domain.repository.MedicineRepository,
    private val analyticsManager: com.levi.meditime.manager.AnalyticsManager,
    private val notificationManager: ReminderNotificationManager
) : ViewModel() {
    
    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    val settings: StateFlow<UserSettings?> = themePreferences.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun backupNow() {
        viewModelScope.launch {
            analyticsManager.logEvent("settings_backup_started")
            val result = repository.backupData()
            if (result is com.levi.meditime.core.common.Resource.Success) {
                _message.value = "Backup successful"
            } else {
                _message.value = "Backup failed: ${result.message}"
            }
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            analyticsManager.logEvent("settings_restore_started")
            val result = repository.restoreData()
            if (result is com.levi.meditime.core.common.Resource.Success) {
                _message.value = "Restore successful"
            } else {
                _message.value = "Restore failed: ${result.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setRemindersEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setVibrationEnabled(enabled)
        }
    }

    fun setLowStockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setLowStockEnabled(enabled)
        }
    }

    fun setFamilyNotifEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setFamilyNotifEnabled(enabled)
        }
    }

    fun setMissedReminderDelay(minutes: Int) {
        viewModelScope.launch {
            themePreferences.setMissedReminderDelay(minutes)
        }
    }

    fun setAutoCallEmergency(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAutoCallEmergency(enabled)
        }
    }

    fun setAccessibilityMode(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAccessibilityMode(enabled)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            themePreferences.setLanguage(lang)
            analyticsManager.logLanguageChanged(lang)
        }
    }

    fun setVoiceAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setVoiceAlarmEnabled(enabled)
        }
    }

    fun setVoiceType(type: String) {
        viewModelScope.launch {
            themePreferences.setVoiceType(type)
        }
    }

    fun setVoiceVolume(volume: Float) {
        viewModelScope.launch {
            themePreferences.setVoiceVolume(volume)
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAppLockEnabled(enabled)
        }
    }

    fun setAppPin(pin: String?) {
        viewModelScope.launch {
            themePreferences.setAppPin(pin)
        }
    }

    fun setAllowScreenshots(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAllowScreenshots(enabled)
        }
    }

    fun setReminderSound(sound: String) {
        viewModelScope.launch {
            themePreferences.setReminderSound(sound)
        }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            themePreferences.setSnoozeDuration(minutes)
        }
    }

    fun setMissedReminderAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setMissedReminderAlertEnabled(enabled)
        }
    }

    fun sendTestNotification() {
        notificationManager.showMedicineReminder("Test Medicine", "test_id")
    }
}
