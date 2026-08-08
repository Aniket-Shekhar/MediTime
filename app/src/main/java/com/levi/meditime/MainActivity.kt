package com.levi.meditime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.levi.meditime.core.util.LocaleHelper
import com.levi.meditime.datastore.ThemeMode
import com.levi.meditime.datastore.ThemePreferences
import com.levi.meditime.domain.repository.AuthRepository
import com.levi.meditime.presentation.navigation.NavGraph
import com.levi.meditime.presentation.navigation.Screen
import com.levi.meditime.presentation.security.AppLockScreen
import com.levi.meditime.presentation.dashboard.SimpleDashboardScreen
import com.levi.meditime.presentation.theme.FloatingBottomNavigation

import com.levi.meditime.presentation.theme.MediTimeMedicineReminderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : androidx.fragment.app.FragmentActivity() {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("MediTime_Crash", "MainActivity caught exception: ${throwable.localizedMessage}", throwable)
    }

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var screenshotSecurityManager: com.levi.meditime.manager.ScreenshotSecurityManager

    @Inject
    lateinit var connectivityObserver: com.levi.meditime.core.util.ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by themePreferences.userSettings.collectAsState(initial = null)
            val themeMode = userSettings?.themeMode ?: ThemeMode.SYSTEM
            val currentLang = userSettings?.language ?: "en"
            val allowScreenshots = userSettings?.allowScreenshots ?: true
            val accessibilityMode = userSettings?.accessibilityMode ?: false

            var isAuthReady by remember { mutableStateOf(false) }
            val user by authRepository.currentUser.collectAsState(initial = null)
            
            LaunchedEffect(Unit) {
                // Wait for the first emission from the auth flow to prevent startup race
                authRepository.currentUser.first()
                isAuthReady = true
            }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Predictive full-screen logic: check current route OR what we expect to show first
            val isFullScreen = remember(currentRoute, userSettings, user, isAuthReady) {
                if (!isAuthReady) return@remember true // Default to full screen (splash) while loading
                
                val onboardingComplete = userSettings?.isOnboardingCompleted ?: false
                val route = currentRoute ?: when {
                    user != null -> Screen.Dashboard.route
                    !onboardingComplete -> Screen.Onboarding.route
                    else -> Screen.Auth.route
                }
                route == Screen.Onboarding.route || route == Screen.Auth.route
            }

            LaunchedEffect(currentLang) {
                val currentAppLocale = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
                if (currentAppLocale != currentLang) {
                    LocaleHelper.applyLocale(currentLang)
                    recreate()
                }
            }

            Crossfade(
                targetState = themeMode,
                animationSpec = tween(durationMillis = 600),
                label = "themeSwitch"
            ) { mode: ThemeMode ->
                MediTimeMedicineReminderTheme(
                    darkTheme = when (mode) {
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                    },
                    accessibilityMode = accessibilityMode
                ) {
                    // Global Session Revocation Listener with token propagation guard
                LaunchedEffect(user) {
                    user?.let { domainUser ->
                        val uid = domainUser.uid
                        try {
                            // Part 1: Force token refresh to ensure RTDB connection is authenticated
                            // This eliminates the "Permission Denied" race window on cold start.
                            authRepository.ensureTokenPropagation()

                            val securityRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("security")
                            val snapshot = securityRef.get().await()

                            val lastLogin = snapshot.child("lastLoginTimestamp").getValue(Long::class.java) ?: 0L
                            val revocationTime = snapshot.child("revocationTimestamp").getValue(Long::class.java) ?: 0L
                            
                            if (revocationTime > lastLogin) {
                                authRepository.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MediTime_Crash", "Revocation check failed: ${e.message}")
                        }
                    }
                }

                LaunchedEffect(currentRoute, allowScreenshots) {
                    val shouldProtect = screenshotSecurityManager.shouldProtectScreen(currentRoute, allowScreenshots)
                    screenshotSecurityManager.setScreenshotEnabled(this@MainActivity, !shouldProtect)
                }

                // PRD 3.2: Network Connectivity Observer
                val connectivityStatus by connectivityObserver.observe().collectAsState(
                    initial = com.levi.meditime.core.util.ConnectivityStatus.Available
                )
                val isOffline = connectivityStatus != com.levi.meditime.core.util.ConnectivityStatus.Available

                // PRD 1.3: Notification Permission Handler
                var hasNotificationPermission by remember { mutableStateOf(true) }
                val bannerDismissed = userSettings?.notifBannerDismissed ?: false
                
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val status = androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                        hasNotificationPermission = status == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (!hasNotificationPermission) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Reset dismissal flag if permission is granted, so it can show up again if revoked later
                LaunchedEffect(hasNotificationPermission) {
                    if (hasNotificationPermission && bannerDismissed) {
                        themePreferences.setNotifBannerDismissed(false)
                    }
                }

                val showBanner = !hasNotificationPermission && !bannerDismissed
                val scope = rememberCoroutineScope()

                // PRD 1.3: Use rememberSaveable to persist unlock state during configuration changes
                var isUnlocked by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            isUnlocked = false
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val appLockEnabled = userSettings?.appLockEnabled ?: false
                
                if (appLockEnabled && !isUnlocked) {
                    AppLockScreen(
                        onUnlockSuccess = { isUnlocked = true },
                        securityManager = screenshotSecurityManager
                    )
                } else {
                    val bottomNavRoutes = listOf(
                        Screen.Dashboard.route,
                        Screen.Medicines.route,
                        Screen.Reports.route,
                        Screen.Family.route,
                        Screen.Account.route
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            // Only show banners if NOT on Onboarding or Auth
                            if (!isFullScreen) {
                                Column(modifier = Modifier.statusBarsPadding()) {
                                    if (isOffline) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Rounded.CloudOff, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Offline Mode. Changes will sync later.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        }
                                    }

                                    if (showBanner) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            modifier = Modifier.fillMaxWidth().clickable { 
                                                // Deep link to settings
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                                                    }
                                                    startActivity(intent)
                                                } else {
                                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = android.net.Uri.fromParts("package", packageName, null)
                                                    }
                                                    startActivity(intent)
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error)
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Reminders are disabled. Tap to enable in settings.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 2
                                                )
                                                IconButton(onClick = { 
                                                    scope.launch { themePreferences.setNotifBannerDismissed(true) }
                                                }) {
                                                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else null
                        },
                        bottomBar = {
                            // Only show bottom bar if logged in AND on a tab route AND not full screen
                            if (user != null && currentRoute in bottomNavRoutes && !isFullScreen) {
                                FloatingBottomNavigation(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onAddMedicine = {
                                        navController.navigate(Screen.AddMedicine.route)
                                    },
                                    onScanPrescription = {
                                        navController.navigate(Screen.PrescriptionScanner.route)
                                    },
                                    onAddAppointment = {
                                        navController.navigate(Screen.Appointments.route)
                                    },
                                    onAddFamily = {
                                        navController.navigate(Screen.AddFamilyMember.route)
                                    }
                                )
                            } else null
                        }
                    ) { padding ->
                        if (userSettings == null || !isAuthReady) {
                            // High-quality Loading Splash State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Rounded.Medication,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(24.dp))
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else {
                            val isOnboardingCompleted = userSettings?.isOnboardingCompleted ?: false
                            val startDestination = when {
                                user != null -> Screen.Dashboard.route
                                !isOnboardingCompleted -> Screen.Onboarding.route
                                else -> Screen.Auth.route
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(if (isFullScreen) PaddingValues(0.dp) else padding)
                            ) {
                                NavGraph(
                                    navController = navController, 
                                    startDestination = startDestination,
                                    accessibilityMode = accessibilityMode,
                                    themePreferences = themePreferences,
                                    onOnboardingComplete = {
                                        lifecycleScope.launch {
                                            themePreferences.setOnboardingCompleted(true)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
