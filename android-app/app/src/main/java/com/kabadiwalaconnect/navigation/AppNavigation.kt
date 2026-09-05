package com.kabadiwalaconnect.navigation

import com.kabadiwalaconnect.presentation.citizen.SafetyScreen
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
import com.kabadiwalaconnect.presentation.citizen.PickupConfirmationScreen
import com.kabadiwalaconnect.presentation.citizen.ProfileScreen
import com.kabadiwalaconnect.presentation.citizen.SettingsScreen
import com.kabadiwalaconnect.presentation.citizen.TrackingScreen
import com.kabadiwalaconnect.presentation.citizen.TraceabilityScreen
import com.kabadiwalaconnect.presentation.collector.CollectorActivePickupScreen
import com.kabadiwalaconnect.presentation.collector.CollectorDashboardScreen
import com.kabadiwalaconnect.presentation.collector.CollectorEarningsScreen
import com.kabadiwalaconnect.presentation.collector.CollectorHandoverScreen
import com.kabadiwalaconnect.presentation.collector.CollectorHistoryScreen
import com.kabadiwalaconnect.presentation.collector.CollectorProfileScreen
import com.kabadiwalaconnect.presentation.collector.CollectorRequestsScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerAvailableLotsScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerDashboardScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerHistoryScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerIncomingLotsScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerLotDetailsScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerPaymentsScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerProfileScreen
import com.kabadiwalaconnect.presentation.recycler.RecyclerRecyclingScreen

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
        composable(Routes.PICKUP_CONFIRMATION) { entry ->
            PickupConfirmationScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.TRACKING) { TrackingScreen(navController) }
        composable(Routes.TRACKING_WITH_LOT) {
        TrackingScreen(navController)
        }
        composable(Routes.TRACEABILITY) { TraceabilityScreen(navController) }
        composable(Routes.TRACEABILITY_WITH_LOT) { entry ->
            TraceabilityScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.HISTORY) { HistoryScreen(navController) }
        composable(Routes.NEARBY) { NearbyScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.COLLECTOR_DASHBOARD) { CollectorDashboardScreen(navController) }
        composable(Routes.COLLECTOR_REQUESTS) { CollectorRequestsScreen(navController) }
        composable(Routes.COLLECTOR_ACTIVE_PICKUP) { CollectorActivePickupScreen(navController) }
        composable(Routes.COLLECTOR_ACTIVE_PICKUP_WITH_LOT) { entry ->
            CollectorActivePickupScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.COLLECTOR_HANDOVER) { entry ->
            CollectorHandoverScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.COLLECTOR_EARNINGS) { CollectorEarningsScreen(navController) }
        composable(Routes.COLLECTOR_HISTORY) { CollectorHistoryScreen(navController) }
        composable(Routes.COLLECTOR_PROFILE) { CollectorProfileScreen(navController) }
        composable(Routes.RECYCLER_DASHBOARD) { RecyclerDashboardScreen(navController) }
        composable(Routes.RECYCLER_AVAILABLE_LOTS) { RecyclerAvailableLotsScreen(navController) }
        composable(Routes.RECYCLER_INCOMING_LOTS) { RecyclerIncomingLotsScreen(navController) }
        composable(Routes.RECYCLER_LOT_DETAILS) { entry ->
            RecyclerLotDetailsScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.RECYCLER_RECYCLING) { entry ->
            RecyclerRecyclingScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.RECYCLER_TRACEABILITY) { entry ->
            TraceabilityScreen(navController, entry.arguments?.getString("lotId"))
        }
        composable(Routes.RECYCLER_PAYMENTS) { RecyclerPaymentsScreen(navController) }
        composable(Routes.RECYCLER_HISTORY) { RecyclerHistoryScreen(navController) }
        composable(Routes.RECYCLER_PROFILE) { RecyclerProfileScreen(navController) }
        composable(Routes.SAFETY) { SafetyScreen(navController) }
    }
}
