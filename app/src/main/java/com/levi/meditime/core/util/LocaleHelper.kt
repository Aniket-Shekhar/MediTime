package com.levi.meditime.core.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    /**
     * Applies a language code using AppCompatDelegate.
     * Note: This automatically triggers Activity recreation if the locale changes.
     */
    fun applyLocale(languageCode: String) {
        if (languageCode.isBlank()) return
        
        try {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            val currentAppLocale = AppCompatDelegate.getApplicationLocales()
            
            if (currentAppLocale.toLanguageTags() != appLocale.toLanguageTags()) {
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
            
            // Set the default locale for the current process as a secondary safeguard
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
        } catch (e: Exception) {
            android.util.Log.e("LocaleHelper", "Error applying locale: ${e.message}")
        }
    }
}
