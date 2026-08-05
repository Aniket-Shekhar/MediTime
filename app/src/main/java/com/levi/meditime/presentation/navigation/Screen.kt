package com.levi.meditime.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    
    // Main Tabs
    object Dashboard : Screen("dashboard")
    object Medicines : Screen("medicines")
    object Reports : Screen("reports")
    object Family : Screen("family")
    object Account : Screen("account")

    object AddMedicine : Screen("add_medicine")
    object Settings : Screen("settings")
    object History : Screen("history")
    object About : Screen("about")
    object EditProfile : Screen("edit_profile")
    object LinkedDevices : Screen("linked_devices")
    object EmergencyContacts : Screen("emergency_contacts")
    object Symptoms : Screen("symptoms")
    object Caretakers : Screen("caretakers")
    object Prescriptions : Screen("prescriptions")
    object Appointments : Screen("appointments")
    object ProfileSelection : Screen("profile_selection")
    object SOS : Screen("sos")
    object PrescriptionScanner : Screen("prescription_scanner")
    object PrescriptionAutoFill : Screen("prescription_autofill/{text}") {
        fun createRoute(text: String) = "prescription_autofill/${android.net.Uri.encode(text)}"
    }
    object AppLock : Screen("app_lock")
    object Notifications : Screen("notifications")
    object AddFamilyMember : Screen("add_family_member")
    object ReminderSettings : Screen("reminder_settings")
    
    object MedicineDetail : Screen("medicine_detail/{medicineId}") {
        fun createRoute(medicineId: String) = "medicine_detail/$medicineId"
    }
    
    object FamilyMemberDetail : Screen("family_member_detail/{memberId}") {
        fun createRoute(memberId: String) = "family_member_detail/$memberId"
    }
    
    object OtpVerification : Screen("otp_verification/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verification/$phoneNumber"
    }
    object EmailVerification : Screen("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }
}
