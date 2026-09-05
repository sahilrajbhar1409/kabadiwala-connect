package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.ui.components.*
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun SettingsScreen(nav: NavHostController) {
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Settings") }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            SettingToggle(
                "Notifications",
                "Pickup and activity updates",
                notifications
            ) { notifications = it }
            SettingRow("Language", "English")
            SettingRow("Privacy", "Manage your data")
            SettingRow("Help & support", "We're here to help")
            Spacer(Modifier.height(25.dp))
            Text("About", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Kabadiwala Connect", fontWeight = FontWeight.Bold)
            Text(
                "Connecting people, collectors and recyclable materials.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}
