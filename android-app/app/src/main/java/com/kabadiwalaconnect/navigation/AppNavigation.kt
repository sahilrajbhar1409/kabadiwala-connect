package com.kabadiwalaconnect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kabadiwalaconnect.presentation.auth.LoginScreen
import com.kabadiwalaconnect.presentation.auth.OnboardingScreen
import com.kabadiwalaconnect.presentation.auth.RegisterScreen
import com.kabadiwalaconnect.presentation.auth.SplashScreen
import com.kabadiwalaconnect.presentation.citizen.HistoryScreen
import com.kabadiwalaconnect.presentation.citizen.HomeScreen
import com.kabadiwalaconnect.presentation.citizen.NearbyScreen
import com.kabadiwalaconnect.presentation.citizen.PickupScreen
import com.kabadiwalaconnect.presentation.citizen.ProfileScreen
import com.kabadiwalaconnect.presentation.citizen.SettingsScreen
import com.kabadiwalaconnect.presentation.citizen.TrackingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.ONBOARDING) { OnboardingScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.PICKUP) { PickupScreen(navController) }
        composable(Routes.TRACKING) { TrackingScreen(navController) }
        composable(Routes.HISTORY) { HistoryScreen(navController) }
        composable(Routes.NEARBY) { NearbyScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}
