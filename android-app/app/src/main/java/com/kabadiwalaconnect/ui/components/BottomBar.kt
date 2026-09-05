package com.kabadiwalaconnect.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.navigation.Routes

@Composable
fun BottomBar(nav: NavHostController, selected: String) {
    fun navigateTo(route: String) {
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.HOME) { saveState = true }
        }
    }
    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        NavigationBarItem(
            selected = selected == Routes.HOME,
            onClick = { navigateTo(Routes.HOME) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selected == Routes.PICKUP,
            onClick = { navigateTo(Routes.PICKUP) },
            icon = { Icon(Icons.Default.LocalShipping, null) },
            label = { Text("Pickup") }
        )
        NavigationBarItem(
            selected = selected == Routes.HISTORY,
            onClick = { navigateTo(Routes.HISTORY) },
            icon = { Icon(Icons.Default.History, null) },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = selected == Routes.PROFILE,
            onClick = { navigateTo(Routes.PROFILE) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}
