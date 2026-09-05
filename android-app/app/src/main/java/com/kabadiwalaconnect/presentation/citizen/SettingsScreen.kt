package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.R
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.components.SettingRow
import com.kabadiwalaconnect.ui.components.SettingToggle
import com.kabadiwalaconnect.ui.theme.Cream
import com.kabadiwalaconnect.ui.theme.TextMuted

@Composable
fun SettingsScreen(nav: NavHostController) {
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Settings") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            SettingToggle(
                "Notifications",
                "Pickup and activity updates",
                notifications
            ) { notifications = it }

            // ✅ Person 6 entry
            SettingRow(
                title = stringResource(id = R.string.safety_title),
                subtitle = "Tips for handling e-waste",
                onClick = { nav.navigate(Routes.SAFETY) }
            )

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