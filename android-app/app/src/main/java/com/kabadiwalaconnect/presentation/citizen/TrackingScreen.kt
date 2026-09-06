package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.components.TrackingStep
import com.kabadiwalaconnect.ui.components.RealTimeMap
import com.kabadiwalaconnect.ui.components.rememberCurrentLocation
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun TrackingScreen(nav: NavHostController, lotId: String? = null) {
    val location = rememberCurrentLocation()
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Pickup tracking") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)
        ) {
            if (location == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = GreenLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Waiting for your location…", color = TextMuted)
                    }
                }
            } else {
                RealTimeMap(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                )
            }
            Spacer(Modifier.height(25.dp))
            Text("Pickup status", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(18.dp))
            TrackingStep("Pickup requested", "5:02 PM", true)
            TrackingStep("Collector assigned", "5:06 PM", true)
            TrackingStep("On the way", "5:18 PM", true)
            TrackingStep("Materials collected", "Pending", false)
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Contact collector") }
        }
    }
}
