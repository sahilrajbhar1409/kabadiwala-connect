package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.Cream
import com.kabadiwalaconnect.ui.theme.Green
import com.kabadiwalaconnect.ui.theme.GreenDark
import com.kabadiwalaconnect.ui.theme.GreenLight
import com.kabadiwalaconnect.ui.theme.TextMuted
import java.util.Locale

@Composable
fun TraceabilityScreen(nav: NavHostController, lotId: String? = null) {
    val state = remember(lotId) { TraceabilityViewModel().load(lotId) }
    val lot = state.lot
    val material = lot?.let { PriceServiceProvider.instance.findMaterial(it.materialId)?.name }
    val totalRecovered = state.repositoryLots.sumOf { it.actualWeight ?: 0.0 }
    val totalRecycled = state.repositoryLots
        .filter { it.status == LotStatus.RECYCLED }
        .sumOf { it.actualWeight ?: 0.0 }
    val totalValue = state.paymentByLot.values.sumOf { it.amount }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Traceability & EPR") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lot == null) {
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
                        Text(
                            "No pickup lot is available yet. Request a pickup to start a traceable EPR record.",
                            color = TextMuted,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TraceStat("Lots", state.repositoryLots.size.toString(), Modifier.weight(1f))
                        TraceStat(
                            "Recovered",
                            "%.1f kg".format(Locale.US, totalRecovered),
                            Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TraceStat(
                            "Recycled",
                            "%.1f kg".format(Locale.US, totalRecycled),
                            Modifier.weight(1f)
                        )
                        TraceStat(
                            "Transactions",
                            "₹%.0f".format(Locale.US, totalValue),
                            Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = GreenLight) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("Traceable lot", color = GreenDark, fontSize = 13.sp)
                            Text(lot.lotId, color = GreenDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(lot.status.traceabilityLabel(), color = Green, fontWeight = FontWeight.Bold)
                            Text(material ?: lot.materialId, color = TextMuted)
                        }
                    }
                }
                item { Text("Chain of custody", style = MaterialTheme.typography.titleLarge) }
                item {
                    Text("Status journey", style = MaterialTheme.typography.titleLarge)
                    StatusJourney(lot.status)
                }
                item { TraceRow("Citizen pickup", lot.pickupLocation, lot.pickupTimestamp ?: "Recorded at request") }
                item { TraceRow("Collector", lot.collectorId.ifBlank { "Awaiting assignment" }, lot.status.collectorTraceLabel()) }
                item { TraceRow("Recycler handover", lot.handoverLocation ?: "Awaiting handover", lot.handoverTimestamp ?: "Pending") }
                item {
                    TraceRow(
                        "Material & weight",
                        "%.2f kg · ₹%.2f".format(
                            Locale.US,
                            lot.actualWeight ?: lot.estimatedWeight,
                            lot.actualValue ?: lot.estimatedValue
                        ),
                        "Estimate updated at collection"
                    )
                }
                item {
                    val prediction = state.prediction
                    TraceRow(
                        "AI intake evidence",
                        prediction?.imageReference ?: lot.imageReference ?: "Manual material entry",
                        prediction?.let {
                            "%.0f%% confidence · %s".format(Locale.US, it.confidence * 100, it.modelVersion)
                        } ?: "No AI evidence"
                    )
                }
                item {
                    val payment = state.payment
                    TraceRow(
                        "Payment",
                        payment?.id ?: "Not paid yet",
                        payment?.let { "₹%.2f · ${it.paymentMethod}".format(Locale.US, it.amount) } ?: "Pending recycler confirmation"
                    )
                }
                item {
                    val recycling = state.recycling
                    TraceRow(
                        "EPR outcome",
                        recycling?.facility?.ifBlank { "Recycling facility recorded" } ?: "Awaiting recycling",
                        recycling?.let { "%.2f kg processed · ${it.status}".format(Locale.US, it.processedWeight) }
                            ?: "Recycler will add the final processing record"
                    )
                }
            }
        }
    }
}

@Composable
private fun TraceRow(label: String, value: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = TextMuted, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold)
            Text(detail, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TraceStat(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = TextMuted, fontSize = 12.sp)
            Text(value, color = GreenDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusJourney(current: LotStatus) {
    val statuses = listOf(
        LotStatus.REQUESTED,
        LotStatus.ACCEPTED,
        LotStatus.PICKUP_IN_PROGRESS,
        LotStatus.COLLECTED,
        LotStatus.HANDED_OVER,
        LotStatus.RECYCLER_CONFIRMED,
        LotStatus.PAID,
        LotStatus.RECYCLED
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        statuses.forEach { status ->
            val reached = status.ordinal <= current.ordinal
            Text(
                text = "${if (reached) "✓" else "○"} ${status.name}",
                color = if (reached) Green else TextMuted,
                fontWeight = if (status == current) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

private fun LotStatus.traceabilityLabel(): String = when (this) {
    LotStatus.REQUESTED -> "Pickup requested"
    LotStatus.ASSIGNED, LotStatus.ACCEPTED -> "Collector assigned"
    LotStatus.PICKUP_IN_PROGRESS -> "Pickup in progress"
    LotStatus.COLLECTED -> "Collected and ready for handover"
    LotStatus.HANDED_OVER -> "Handed to recycler"
    LotStatus.RECYCLER_CONFIRMED -> "Recycler received"
    LotStatus.PAID -> "Paid · recycling pending"
    LotStatus.RECYCLED -> "Recycled · EPR complete"
    LotStatus.CANCELLED -> "Cancelled"
}

private fun LotStatus.collectorTraceLabel(): String = when (this) {
    LotStatus.REQUESTED -> "Waiting for collector"
    LotStatus.ASSIGNED, LotStatus.ACCEPTED -> "Accepted pickup"
    else -> name
}
