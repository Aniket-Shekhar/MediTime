package com.levi.meditime.core.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun applyLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        
        // Use AppCompatDelegate to set locales for the entire app. 
        // This persists the choice and updates the configuration.
        AppCompatDelegate.setApplicationLocales(appLocale)

        // Set the default locale for the current process as a fallback
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
    }
}
