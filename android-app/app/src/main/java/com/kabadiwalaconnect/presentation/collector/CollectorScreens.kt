package com.kabadiwalaconnect.presentation.collector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.SessionState
import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.Border
import com.kabadiwalaconnect.ui.theme.Cream
import com.kabadiwalaconnect.ui.theme.Green
import com.kabadiwalaconnect.ui.theme.GreenDark
import com.kabadiwalaconnect.ui.theme.GreenLight
import com.kabadiwalaconnect.ui.theme.TextMuted
import java.util.Locale

private const val RECYCLER_ID = "recycler-session"

@Composable
fun CollectorDashboardScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val pending = repository.getPendingCollectionRequests().size
    val collectorLots = repository.getCollectorLots(SessionState.COLLECTOR_ID)
    val accepted = collectorLots.count { it.status == LotStatus.ACCEPTED }
    val active = collectorLots.count {
        it.status == LotStatus.PICKUP_IN_PROGRESS || it.status == LotStatus.COLLECTED
    }
    val completed = repository.getCollectorHistory(SessionState.COLLECTOR_ID).size
    val earnings = repository.getCollectorEarnings(SessionState.COLLECTOR_ID)

    Scaffold(
        containerColor = Cream,
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_DASHBOARD) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Collector dashboard", style = MaterialTheme.typography.headlineMedium)
                Text("Good morning 👋", color = TextMuted)
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = Green) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Ready to collect?", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("$pending new requests are waiting in your area.", color = Color.White.copy(alpha = .85f))
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { nav.navigate(Routes.COLLECTOR_REQUESTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("View requests", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CollectorStat("Pending", pending.toString(), Modifier.weight(1f))
                        CollectorStat("Accepted", accepted.toString(), Modifier.weight(1f))
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CollectorStat("Active", active.toString(), Modifier.weight(1f))
                        CollectorStat("Completed", completed.toString(), Modifier.weight(1f))
                    }
                    CollectorStat(
                        "Total earnings",
                        "₹%.0f".format(Locale.US, earnings),
                        Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { nav.navigate(Routes.COLLECTOR_ACTIVE_PICKUP) },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = Green)
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Active pickups", fontWeight = FontWeight.Bold)
                            Text("Start, weigh and hand over material", color = TextMuted, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Green)
                    }
                }
            }
            item { Text("Today’s checklist", style = MaterialTheme.typography.titleLarge) }
            item { ChecklistRow("Accept a citizen request", pending == 0) }
            item { ChecklistRow("Complete pickup", active == 0) }
            item { ChecklistRow("Record recycler handover", repository.getCollectorHistory(SessionState.COLLECTOR_ID).isNotEmpty()) }
        }
    }
}

@Composable
private fun CollectorStat(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextMuted, fontSize = 12.sp)
            Text(value, color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        }
    }
}

@Composable
private fun ChecklistRow(title: String, complete: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (complete) Icons.Default.Check else Icons.Default.PlayArrow, null, tint = if (complete) Green else TextMuted)
        Spacer(Modifier.size(10.dp))
        Text(title, color = if (complete) TextMuted else Color.Unspecified)
    }
}

@Composable
fun CollectorRequestsScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    var refresh by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val requests = repository.getPendingCollectionRequests()
    @Suppress("UNUSED_EXPRESSION")
    refresh

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Pickup requests") },
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_REQUESTS) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Requests from citizens", style = MaterialTheme.typography.headlineSmall)
                Text("Accept a request to add it to your active pickups.", color = TextMuted)
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
            if (requests.isEmpty()) {
                item { EmptyCollectorMessage("No pending requests right now.") }
            } else {
                items(requests, key = { it.id }) { request ->
                    CollectorRequestCard(
                        request = request,
                        onAccept = {
                            try {
                                val lot = repository.acceptCollectionRequest(
                                    request.id,
                                    SessionState.COLLECTOR_ID
                                )
                                refresh++
                                nav.navigate(Routes.collectorActivePickup(lot.lotId))
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Unable to accept this request."
                            }
                        },
                        onReject = {
                            try {
                                repository.rejectCollectionRequest(request.id)
                                refresh++
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Unable to reject this request."
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectorRequestCard(
    request: CollectionRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val material = PriceServiceProvider.instance.findMaterial(request.materialId)?.name ?: request.materialId
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(material, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(request.pickupAddress, color = TextMuted, fontSize = 13.sp)
                }
                Text("₹%.0f".format(Locale.US, request.estimatedValue), color = Green, fontWeight = FontWeight.Bold)
            }
            Text("%.2f kg · %s".format(Locale.US, request.estimatedWeight, request.preferredDate), color = TextMuted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                    Text("Accept")
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun CollectorActivePickupScreen(nav: NavHostController, lotId: String? = null) {
    val repository = remember { CollectionRepositoryProvider.instance }
    var refresh by remember { mutableIntStateOf(0) }
    val lots = repository.getCollectorLots(SessionState.COLLECTOR_ID)
        .filter { it.status == LotStatus.ACCEPTED || it.status == LotStatus.PICKUP_IN_PROGRESS || it.status == LotStatus.COLLECTED }
    @Suppress("UNUSED_EXPRESSION")
    refresh
    val selected = lotId?.let { id -> lots.firstOrNull { it.lotId == id } }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Active pickups") },
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_ACTIVE_PICKUP) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Your route", style = MaterialTheme.typography.headlineSmall)
                Text("Update each pickup as you complete it.", color = TextMuted)
            }
            if (lots.isEmpty()) {
                item { EmptyCollectorMessage("Accepted pickups will appear here.") }
            } else {
                items(if (selected == null) lots else listOf(selected), key = { it.lotId }) { lot ->
                    ActivePickupCard(lot, nav) { refresh++ }
                }
            }
        }
    }
}

@Composable
private fun ActivePickupCard(lot: Lot, nav: NavHostController, onChanged: () -> Unit) {
    val repository = CollectionRepositoryProvider.instance
    val material = PriceServiceProvider.instance.findMaterial(lot.materialId)?.name ?: lot.materialId
    var weightText by remember(lot.lotId, lot.status, lot.actualWeight) {
        mutableStateOf(lot.actualWeight?.toString() ?: "")
    }
    var errorMessage by remember(lot.lotId, lot.status) { mutableStateOf<String?>(null) }
    val weight = weightText.toDoubleOrNull()

    Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(material, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(lot.pickupLocation, color = TextMuted, fontSize = 13.sp)
                }
                Text(lot.status.collectorLabel(), color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("Lot ${lot.lotId}", color = TextMuted, fontSize = 12.sp)
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            when (lot.status) {
                LotStatus.ACCEPTED -> Button(
                    onClick = {
                        try {
                            repository.startPickup(lot.lotId, SessionState.COLLECTOR_ID)
                            onChanged()
                        } catch (exception: Exception) {
                            errorMessage = exception.message ?: "Unable to start this pickup."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp)
                ) { Text("Start pickup") }
                LotStatus.PICKUP_IN_PROGRESS -> {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Actual weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Button(
                        onClick = {
                            try {
                                repository.completePickup(
                                    lot.lotId,
                                    SessionState.COLLECTOR_ID,
                                    weight!!
                                )
                                onChanged()
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Unable to record collection."
                            }
                        },
                        enabled = weight != null && weight.isFinite() && weight > 0,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(11.dp)
                    ) { Text("Record collection") }
                }
                LotStatus.COLLECTED -> {
                    Text("Actual: %.2f kg · ₹%.2f".format(Locale.US, lot.actualWeight ?: 0.0, lot.actualValue ?: 0.0), color = TextMuted)
                    Button(
                        onClick = { nav.navigate(Routes.collectorHandover(lot.lotId)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(11.dp)
                    ) { Text("Record handover") }
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun CollectorHandoverScreen(nav: NavHostController, lotId: String?) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val lot = lotId?.let { repository.getLot(it) }
    var recycler by remember { mutableStateOf(RECYCLER_ID) }
    var location by remember { mutableStateOf("") }
    var weightText by remember(lot?.lotId) { mutableStateOf(lot?.actualWeight?.toString() ?: "") }
    var valueText by remember(lot?.lotId) { mutableStateOf(lot?.actualValue?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    val weight = weightText.toDoubleOrNull()
    val value = valueText.toDoubleOrNull()

    Scaffold(containerColor = Cream, topBar = { AppTopBar(nav, "Recycler handover") }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lot == null) {
                EmptyCollectorMessage("Collected lot not found.")
                return@Column
            }
            Text("Confirm actual values", style = MaterialTheme.typography.headlineSmall)
            Text("Lot ${lot.lotId}", color = TextMuted)
            OutlinedTextField(weightText, { weightText = it }, label = { Text("Actual weight (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(valueText, { valueText = it }, label = { Text("Final earnings (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(recycler, { recycler = it }, label = { Text("Recycler name or ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(location, { location = it }, label = { Text("Handover location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (weight == null || value == null || weight <= 0 || value < 0 || recycler.isBlank() || location.isBlank()) {
                        error = "Enter valid weight, value, recycler and location."
                    } else {
                        try {
                            repository.recordHandover(
                                lot.lotId,
                                SessionState.COLLECTOR_ID,
                                recycler,
                                location,
                                weight,
                                value
                            )
                            nav.navigate(Routes.COLLECTOR_EARNINGS) {
                                popUpTo(Routes.COLLECTOR_DASHBOARD)
                            }
                        } catch (exception: Exception) {
                            error = exception.message ?: "Unable to save handover."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(13.dp)
            ) { Text("Save handover", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun CollectorEarningsScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val collectorId = SessionState.COLLECTOR_ID
    val lots = repository.getCollectorHistory(collectorId)
    val total = repository.getCollectorEarnings(collectorId)
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Earnings") },
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_EARNINGS) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = Green) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Text("Total earnings", color = Color.White.copy(alpha = .8f))
                        Text("₹%.2f".format(Locale.US, total), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("${lots.size} completed handovers", color = Color.White.copy(alpha = .8f))
                    }
                }
            }
            item { Text("Recent payouts", style = MaterialTheme.typography.titleLarge) }
            if (lots.isEmpty()) item { EmptyCollectorMessage("Completed handovers and earnings will appear here.") }
            else items(lots.take(20), key = { it.lotId }) { EarningRow(it) }
        }
    }
}

@Composable
private fun EarningRow(lot: Lot) {
    val material = PriceServiceProvider.instance.findMaterial(lot.materialId)?.name ?: lot.materialId
    Surface(shape = RoundedCornerShape(15.dp), color = Color.White) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Green)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(material, fontWeight = FontWeight.Bold)
                Text("%.2f kg · %s".format(Locale.US, lot.actualWeight ?: 0.0, lot.handoverTimestamp ?: ""), color = TextMuted, fontSize = 12.sp)
            }
            Text("₹%.2f".format(Locale.US, lot.actualValue ?: 0.0), color = Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CollectorHistoryScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val lots = repository.getCollectorHistory(SessionState.COLLECTOR_ID)
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Collection history") },
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_HISTORY) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Completed collections", style = MaterialTheme.typography.headlineSmall) }
            if (lots.isEmpty()) item { EmptyCollectorMessage("Your completed collections will appear here.") }
            else items(lots, key = { it.lotId }) { lot ->
                val material = PriceServiceProvider.instance.findMaterial(lot.materialId)?.name ?: lot.materialId
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(material, fontWeight = FontWeight.Bold)
                            Text("₹%.2f".format(Locale.US, lot.actualValue ?: 0.0), color = Green, fontWeight = FontWeight.Bold)
                        }
                        Text("Lot ${lot.lotId} · %.2f kg".format(Locale.US, lot.actualWeight ?: 0.0), color = TextMuted, fontSize = 13.sp)
                        Text(lot.handoverLocation ?: lot.pickupLocation, color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CollectorProfileScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Collector profile") },
        bottomBar = { CollectorBottomBar(nav, Routes.COLLECTOR_PROFILE) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(22.dp))
            Surface(shape = RoundedCornerShape(48.dp), color = GreenLight, modifier = Modifier.size(92.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Person, null, tint = Green, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Local collector", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Collector ID: ${SessionState.COLLECTOR_ID}", color = TextMuted)
            Spacer(Modifier.height(28.dp))
            OutlinedButton(
                onClick = {
                    SessionState.signOut()
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp)
            ) { Text("Log out") }
        }
    }
}

@Composable
private fun EmptyCollectorMessage(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Text(message, color = TextMuted, modifier = Modifier.fillMaxWidth().padding(20.dp))
    }
}

@Composable
private fun CollectorBottomBar(nav: NavHostController, selected: String) {
    fun go(route: String) {
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.COLLECTOR_DASHBOARD) { saveState = true }
        }
    }
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected == Routes.COLLECTOR_DASHBOARD, { go(Routes.COLLECTOR_DASHBOARD) }, { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected == Routes.COLLECTOR_REQUESTS, { go(Routes.COLLECTOR_REQUESTS) }, { Icon(Icons.Default.ListAlt, null) }, label = { Text("Requests") })
        NavigationBarItem(selected == Routes.COLLECTOR_EARNINGS, { go(Routes.COLLECTOR_EARNINGS) }, { Icon(Icons.Default.AccountBalanceWallet, null) }, label = { Text("Earnings") })
        NavigationBarItem(selected == Routes.COLLECTOR_PROFILE, { go(Routes.COLLECTOR_PROFILE) }, { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}

private fun LotStatus.collectorLabel(): String = when (this) {
    LotStatus.ACCEPTED -> "ACCEPTED"
    LotStatus.PICKUP_IN_PROGRESS -> "IN PROGRESS"
    LotStatus.COLLECTED -> "COLLECTED"
    else -> name
}
