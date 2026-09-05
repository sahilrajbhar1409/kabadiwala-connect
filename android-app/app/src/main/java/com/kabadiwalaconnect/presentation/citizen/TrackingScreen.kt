package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.components.TrackingStep
import com.kabadiwalaconnect.ui.theme.*

@Composable
fun TrackingScreen(nav: NavHostController, lotId: String? = null) {
    val repository = CollectionRepositoryProvider.instance
    val lot = lotId?.let { repository.getLot(it) } ?: repository.getLatestLot()
    val currentStatus = lot?.status ?: LotStatus.REQUESTED
    val timeline = listOf(
        LotStatus.REQUESTED to "Pickup requested",
        LotStatus.ASSIGNED to "Collector assigned",
        LotStatus.ACCEPTED to "Pickup accepted",
        LotStatus.PICKUP_IN_PROGRESS to "Pickup in progress",
        LotStatus.COLLECTED to "Materials collected",
        LotStatus.HANDED_OVER to "Handed to recycler",
        LotStatus.RECYCLER_CONFIRMED to "Recycler confirmed",
        LotStatus.PAID to "Payment completed",
        LotStatus.RECYCLED to "Recycled"
    )

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Pickup tracking") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = GreenLight
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Current status", color = GreenDark, fontSize = 13.sp)
                        Text(
                            currentStatus.displayName(),
                            color = GreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            lot?.let { "Lot ${it.lotId}" } ?: "No pickup request found",
                            color = TextMuted
                        )
                    }
                }
            }
            item { Text("Pickup status", style = MaterialTheme.typography.titleLarge) }
            items(timeline) { (status, title) ->
                val reached = currentStatus != LotStatus.CANCELLED &&
                    status.ordinal <= currentStatus.ordinal
                TrackingStep(
                    title = title,
                    time = if (status == currentStatus) "Current" else if (reached) "Completed" else "Pending",
                    completed = reached
                )
            }
            item {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Contact collector")
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        lot?.let { nav.navigate(Routes.traceability(it.lotId)) }
                            ?: nav.navigate(Routes.TRACEABILITY)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("View traceability & EPR")
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        nav.navigate(Routes.HISTORY) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("View history")
                }
            }
        }
    }
}

private fun LotStatus.displayName(): String = when (this) {
    LotStatus.REQUESTED -> "Pickup requested"
    LotStatus.ASSIGNED -> "Collector assigned"
    LotStatus.ACCEPTED -> "Pickup accepted"
    LotStatus.PICKUP_IN_PROGRESS -> "Pickup in progress"
    LotStatus.COLLECTED -> "Materials collected"
    LotStatus.HANDED_OVER -> "Handed to recycler"
    LotStatus.RECYCLER_CONFIRMED -> "Recycler confirmed"
    LotStatus.PAID -> "Payment completed"
    LotStatus.RECYCLED -> "Recycled"
    LotStatus.CANCELLED -> "Pickup cancelled"
}
