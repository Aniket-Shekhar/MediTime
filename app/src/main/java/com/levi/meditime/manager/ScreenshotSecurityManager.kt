package com.levi.meditime.manager

import android.app.Activity
import android.util.Log
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.levi.meditime.presentation.navigation.Screen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotSecurityManager @Inject constructor() {

    fun setScreenshotEnabled(activity: Activity, enabled: Boolean) {
        if (enabled) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Log.d("SCREENSHOT_SECURITY", "FLAG_SECURE Removed - Screenshots Allowed")
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            Log.d("SCREENSHOT_SECURITY", "FLAG_SECURE Applied - Screenshots Restricted")
        }
    }

    /**
     * Determines if a screen should be protected based on user settings and route.
     * If [allowScreenshots] is true, NO screens are protected.
     * If false, only sensitive screens are protected.
     */
    fun shouldProtectScreen(route: String?, allowScreenshots: Boolean): Boolean {
        if (allowScreenshots) {
            Log.d("SCREENSHOT_SECURITY", "Allow Screenshots ON - Normal Screen Screenshot Allowed: $route")
            return false
        }

        val sensitiveRoutes = listOf(
            Screen.Login.route,
            Screen.SignUp.route,
            Screen.OtpVerification.route.split("/").first(), // Basic route match
            Screen.EmailVerification.route.split("/").first(),
            Screen.EmergencyContacts.route,
            Screen.SOS.route,
            Screen.Prescriptions.route,
            Screen.PrescriptionScanner.route,
            Screen.History.route,
            Screen.Reports.route,
            Screen.MedicineDetail.route.split("/").first(),
            Screen.Account.route
        )

        val isSensitive = sensitiveRoutes.any { route?.startsWith(it) == true }
        if (isSensitive) {
            Log.d("SCREENSHOT_SECURITY", "Sensitive Screen Protected: $route")
        } else {
            Log.d("SCREENSHOT_SECURITY", "Normal Screen Screenshot Allowed: $route")
        }
        return isSensitive
    }

    // Biometric methods moved from SecurityManager (to maintain functionality after rename)
    fun isBiometricAvailable(activity: Activity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "MediTime Security",
        subtitle: String = "Authenticate to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
