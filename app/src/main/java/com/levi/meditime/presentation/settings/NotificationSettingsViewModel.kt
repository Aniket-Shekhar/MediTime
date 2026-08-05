package com.levi.meditime.presentation.settings

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi.meditime.datastore.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _selectedRingtoneUri = MutableStateFlow<String?>(null)
    val selectedRingtoneUri = _selectedRingtoneUri.asStateFlow()

    init {
        viewModelScope.launch {
            // themePreferences should be updated to store ringtone URI as well
        }
    }

    fun setRingtone(uri: Uri) {
        _selectedRingtoneUri.value = uri.toString()
        viewModelScope.launch {
            // themePreferences.setRingtoneUri(uri.toString())
        }
    }

    fun getRingtoneName(uriString: String?): String {
        if (uriString == null) return "Default"
        val uri = Uri.parse(uriString)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        return ringtone?.getTitle(context) ?: "Unknown"
    }
}