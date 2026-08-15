package com.levi.meditime.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.levi.meditime.presentation.dashboard.DashboardScreen
import com.levi.meditime.presentation.dashboard.SimpleDashboardScreen
import com.levi.meditime.presentation.medicine.add.AddMedicineScreen
import com.levi.meditime.presentation.medicine.add.AddMedicineViewModel
import com.levi.meditime.presentation.auth.login.LoginScreen
import com.levi.meditime.presentation.auth.register.RegisterScreen
import com.levi.meditime.presentation.account.AccountScreen
import com.levi.meditime.presentation.settings.SettingsScreen
import com.levi.meditime.presentation.history.MedicineHistoryScreen
import com.levi.meditime.presentation.family.FamilyListScreen
import com.levi.meditime.presentation.family.FamilyMemberDetailScreen
import com.levi.meditime.presentation.family.AddFamilyMemberScreen
import com.levi.meditime.presentation.family.FamilyViewModel
import com.levi.meditime.presentation.about.AboutScreen
import com.levi.meditime.presentation.auth.profile.EditProfileScreen
import com.levi.meditime.presentation.account.LinkedDevicesScreen
import com.levi.meditime.presentation.emergency.EmergencyContactsScreen
import com.levi.meditime.presentation.auth.email.EmailVerificationScreen
import com.levi.meditime.presentation.auth.otp.OtpScreen
import com.levi.meditime.presentation.symptoms.SymptomDashboardScreen
import com.levi.meditime.presentation.caretaker.CaretakerListScreen
import com.levi.meditime.presentation.prescription.PrescriptionListScreen
import com.levi.meditime.presentation.appointment.AppointmentListScreen
import com.levi.meditime.presentation.sos.SosScreen
import com.levi.meditime.presentation.profile_management.ProfileSelectionScreen
import com.levi.meditime.presentation.medicine.list.MedicineListScreen
import com.levi.meditime.presentation.reports.ReportDashboardScreen
import com.levi.meditime.presentation.medicine.detail.MedicineDetailScreen
import com.levi.meditime.presentation.medicine.detail.MedicineDetailViewModel
import com.levi.meditime.presentation.notifications.NotificationScreen
import com.levi.meditime.presentation.prescription.PrescriptionScannerScreen
import com.levi.meditime.presentation.settings.ReminderSettingsScreen
import com.levi.meditime.presentation.auth.AuthScreen
import com.levi.meditime.presentation.auth.OnboardingScreen
import com.levi.meditime.datastore.ThemePreferences

@Composable
fun NavGraph(
    navController: NavHostController, 
    startDestination: String,
    accessibilityMode: Boolean = false,
    themePreferences: ThemePreferences,
    onOnboardingComplete: () -> Unit = {}
) {
    fun navigateTo(route: String) {
        if (navController.currentDestination?.route != route) { // Prevent redundant navigation
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) + slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        popExitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) + slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    onOnboardingComplete()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToOtp = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                },
                onNavigateToEmailVerify = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToPhoneOtp = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                }
            )
        }
        composable(Screen.SignUp.route) {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email)) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            if (accessibilityMode) {
                SimpleDashboardScreen(
                    onNavigateToSOS = { navigateTo(Screen.SOS.route) }
                )
            } else {
                DashboardScreen(
                    onAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
                    onNavigateToSymptoms = { navigateTo(Screen.Symptoms.createRoute()) },
                    onNavigateToSOS = { navigateTo(Screen.SOS.route) },
                    onNavigateToPrescriptions = { navigateTo(Screen.PrescriptionScanner.route) },
                    onNavigateToAppointments = { navigateTo(Screen.Appointments.createRoute()) },
                    onNavigateToNotifications = { navigateTo(Screen.Notifications.route) },
                    onNavigateToReports = { navigateTo(Screen.Reports.createRoute()) },
                    onNavigateToProfile = { navigateTo(Screen.Account.route) },
                    onNavigateToMedicineDetail = { id -> 
                        navController.navigate(Screen.MedicineDetail.createRoute(id)) 
                    },
                    onViewAllMedicines = { navigateTo(Screen.Medicines.route) },
                    onViewAllFamily = { navigateTo(Screen.Family.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Screen.Medicines.route) {
            MedicineListScreen(
                onAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
                onMedicineClick = { medicine ->
                    navController.navigate(Screen.MedicineDetail.createRoute(medicine.id))
                },
                onEditMedicine = { medicine ->
                    navController.navigate(Screen.MedicineDetail.createRoute(medicine.id))
                }
            )
        }
        composable(
            route = Screen.MedicineDetail.route,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId") ?: ""
            MedicineDetailScreen(
                medicineId = medicineId,
                onBack = { navController.popBackStack() },
                onEdit = { /* Navigate to Edit */ }
            )
        }
        composable(
            route = Screen.Reports.route,
            arguments = listOf(navArgument("memberId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            ReportDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddMedicine.route) {
            AddMedicineScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFamily = { navigateTo(Screen.Family.route) }
            )
        }
        composable(Screen.Account.route) {
            AccountScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSettings = { navigateTo(Screen.Settings.route) },
                onNavigateToFamily = { navigateTo(Screen.Family.route) },
                onNavigateToCaretakers = { navigateTo(Screen.Caretakers.route) },
                onNavigateToAppointments = { navigateTo(Screen.Appointments.route) },
                onNavigateToProfileSelection = { navigateTo(Screen.ProfileSelection.route) },
                onNavigateToAbout = { navigateTo(Screen.About.route) },
                onNavigateToEditProfile = { navigateTo(Screen.EditProfile.route) },
                onNavigateToLinkedDevices = { navigateTo(Screen.LinkedDevices.route) },
                onNavigateToEmergencyContacts = { navigateTo(Screen.EmergencyContacts.route) },
                onNavigateToReminderSettings = { navigateTo(Screen.ReminderSettings.route) },
                onNavigateToOtpVerification = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                },
                onNavigateToEmailVerification = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.History.route) {
            MedicineHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Family.route) {
            FamilyListScreen(
                onBack = { navController.popBackStack() },
                onMemberClick = { member ->
                    navController.navigate(Screen.FamilyMemberDetail.createRoute(member.id))
                },
                onAddMember = {
                    navController.navigate(Screen.AddFamilyMember.route)
                }
            )
        }
        composable(
            route = Screen.FamilyMemberDetail.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            
            FamilyMemberDetailScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onNavigateToMedicines = { id ->
                    // Logic to navigate to medicine list filtered by user
                    navController.navigate(Screen.Medicines.route)
                },
                onNavigateToReports = { id ->
                    navController.navigate(Screen.Reports.createRoute(id))
                },
                onNavigateToSymptoms = { id ->
                    navController.navigate(Screen.Symptoms.createRoute(id))
                },
                onNavigateToAppointments = { id ->
                    navController.navigate(Screen.Appointments.createRoute(id))
                }
            )
        }
        composable(Screen.AddFamilyMember.route) {
            AddFamilyMemberScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LinkedDevices.route) {
            LinkedDevicesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ReminderSettings.route) {
            ReminderSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Symptoms.route,
            arguments = listOf(navArgument("memberId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            SymptomDashboardScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Caretakers.route) {
            CaretakerListScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Prescriptions.route) {
            PrescriptionListScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Appointments.route,
            arguments = listOf(navArgument("memberId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AppointmentListScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SOS.route) {
            SosScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PrescriptionScanner.route) {
            PrescriptionScannerScreen(
                onBack = { navController.popBackStack() },
                onTextExtracted = { text ->
                    navController.navigate(Screen.PrescriptionAutoFill.createRoute(text))
                }
            )
        }
        composable(
            route = Screen.PrescriptionAutoFill.route,
            arguments = listOf(navArgument("text") { type = NavType.StringType })
        ) { backStackEntry ->
            val text = backStackEntry.arguments?.getString("text") ?: ""
            val viewModel: AddMedicineViewModel = hiltViewModel()
            
            // Trigger parsing once
            LaunchedEffect(text) {
                viewModel.parseExtractedText(text)
            }
            
            AddMedicineScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFamily = { navigateTo(Screen.Family.route) },
                viewModel = viewModel
            )
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ProfileSelection.route) {
            ProfileSelectionScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onBack = { navController.popBackStack() },
                onVerificationSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationScreen(
                email = email,
                onNavigateToLogin = { navController.popBackStack() },
                onVerificationSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
