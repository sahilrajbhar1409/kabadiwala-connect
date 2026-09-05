package com.kabadiwalaconnect.presentation.recycler

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.model.PaymentMethod
import com.kabadiwalaconnect.data.model.Transaction
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.Cream
import com.kabadiwalaconnect.ui.theme.Green
import com.kabadiwalaconnect.ui.theme.GreenDark
import com.kabadiwalaconnect.ui.theme.GreenLight
import com.kabadiwalaconnect.ui.theme.TextMuted
import java.util.Locale

private val recyclerId: String
    get() = SessionState.RECYCLER_ID

@Composable
fun RecyclerDashboardScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val available = repository.getAvailableRecyclerLots().size
    val incoming = repository.getIncomingLots(recyclerId).size
    val processed = repository.getRecyclerHistory(recyclerId).size
    val paid = repository.getRecyclerPayments(recyclerId)
        .filter { it.status.name == "COMPLETED" }
        .sumOf { it.amount }

    Scaffold(
        containerColor = Cream,
        bottomBar = { RecyclerBottomBar(nav, Routes.RECYCLER_DASHBOARD) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Recycler dashboard", style = MaterialTheme.typography.headlineMedium)
                Text("Keep every lot traceable from handover to recycling.", color = TextMuted)
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = Green) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("New material is ready", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("$available handed-over lots are available to receive.", color = Color.White.copy(alpha = .85f))
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { nav.navigate(Routes.RECYCLER_AVAILABLE_LOTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("View available lots", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RecyclerStat("Available", available.toString(), Modifier.weight(1f))
                    RecyclerStat("Incoming", incoming.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RecyclerStat("Recycled", processed.toString(), Modifier.weight(1f))
                    RecyclerStat("Paid", "₹%.0f".format(Locale.US, paid), Modifier.weight(1f))
                }
            }
            item {
                RecyclerLinkCard("Incoming lots", "Confirm receipt and view lot details", Icons.Default.Inventory) {
                    nav.navigate(Routes.RECYCLER_INCOMING_LOTS)
                }
            }
            item {
                RecyclerLinkCard("Payments", "View completed recycler payments", Icons.Default.AccountBalanceWallet) {
                    nav.navigate(Routes.RECYCLER_PAYMENTS)
                }
            }
        }
    }
}

@Composable
private fun RecyclerStat(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextMuted, fontSize = 12.sp)
            Text(value, color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        }
    }
}

@Composable
fun RecyclerAvailableLotsScreen(nav: NavHostController) {
    RecyclerLotsScreen(nav, incoming = false)
}

@Composable
fun RecyclerIncomingLotsScreen(nav: NavHostController) {
    RecyclerLotsScreen(nav, incoming = true)
}

@Composable
private fun RecyclerLotsScreen(nav: NavHostController, incoming: Boolean) {
    val repository = remember { CollectionRepositoryProvider.instance }
    var refresh by remember { mutableIntStateOf(0) }
    @Suppress("UNUSED_EXPRESSION")
    refresh
    val lots = if (incoming) {
        repository.getIncomingLots(recyclerId)
    } else {
        repository.getAvailableRecyclerLots()
    }
    val title = if (incoming) "Incoming lots" else "Available lots"

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, title) },
        bottomBar = { RecyclerBottomBar(nav, if (incoming) Routes.RECYCLER_INCOMING_LOTS else Routes.RECYCLER_AVAILABLE_LOTS) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (incoming) "Lots assigned to your recycler account."
                    else "Receive a lot only after checking its Lot ID and handover values.",
                    color = TextMuted
                )
            }
            if (lots.isEmpty()) {
                item { RecyclerEmptyMessage("No $title.lowercase() right now.") }
            } else {
                items(lots, key = { it.lotId }) { lot ->
                    RecyclerLotCard(lot) {
                        nav.navigate(Routes.recyclerLotDetails(lot.lotId))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecyclerLotCard(lot: Lot, onOpen: () -> Unit) {
    val material = PriceServiceProvider.instance.findMaterial(lot.materialId)?.name ?: lot.materialId
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        onClick = onOpen
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(material, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Lot ID: ${lot.lotId}", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(lot.status.recyclerLabel(), color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(lot.handoverLocation ?: lot.pickupLocation, color = TextMuted, fontSize = 13.sp)
            Text(
                "%.2f kg · ₹%.2f".format(Locale.US, lot.actualWeight ?: lot.estimatedWeight, lot.actualValue ?: lot.estimatedValue),
                color = TextMuted,
                fontSize = 13.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Open details", color = Green, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, null, tint = Green)
            }
        }
    }
}

@Composable
fun RecyclerLotDetailsScreen(nav: NavHostController, lotId: String?) {
    val repository = remember { CollectionRepositoryProvider.instance }
    var lot by remember(lotId) { mutableStateOf(lotId?.let(repository::getLot)) }
    var error by remember { mutableStateOf<String?>(null) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    val current = lot

    Scaffold(containerColor = Cream, topBar = { AppTopBar(nav, "Lot details") }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            if (current == null) {
                item { RecyclerEmptyMessage("This lot could not be found. Check the Lot ID and try again.") }
            } else {
                item {
                    LotSummary(current)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                }
                item {
                    when {
                        current.status == LotStatus.HANDED_OVER &&
                            (current.recyclerId.isNullOrBlank() || current.recyclerId == recyclerId) -> {
                            Button(
                                onClick = {
                                    try {
                                        if (current.recyclerId.isNullOrBlank()) {
                                            repository.assignRecycler(current.lotId, recyclerId)
                                        }
                                        lot = repository.confirmRecyclerReceipt(current.lotId, recyclerId)
                                        error = null
                                    } catch (exception: IllegalArgumentException) {
                                        error = exception.message ?: "Unable to confirm this lot."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Confirm receipt") }
                        }
                        current.status == LotStatus.HANDED_OVER ->
                            Text("This lot is assigned to another recycler.", color = MaterialTheme.colorScheme.error)
                        current.status == LotStatus.RECYCLER_CONFIRMED -> {
                            Text("Payment method", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PaymentMethod.values().forEach { method ->
                                    androidx.compose.material3.FilterChip(
                                        selected = paymentMethod == method,
                                        onClick = { paymentMethod = method },
                                        label = { Text(method.name) }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    try {
                                        repository.payRecycler(current.lotId, recyclerId, paymentMethod)
                                        lot = repository.getLot(current.lotId)
                                        error = null
                                    } catch (exception: IllegalArgumentException) {
                                        error = exception.message ?: "Unable to complete payment."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Pay for lot") }
                        }
                        current.status == LotStatus.PAID -> {
                            Button(
                                onClick = { nav.navigate(Routes.recyclerRecycling(current.lotId)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Record recycling") }
                        }
                        current.status == LotStatus.RECYCLED ->
                            Text("Recycling completed. This Lot ID remains traceable in history.", color = Green)
                        else -> Text("Lot is not ready for recycler processing.", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun LotSummary(lot: Lot) {
    val material = PriceServiceProvider.instance.findMaterial(lot.materialId)?.name ?: lot.materialId
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(material, style = MaterialTheme.typography.headlineSmall)
            Text("Lot ID: ${lot.lotId}", color = Green, fontWeight = FontWeight.Bold)
            LotDetailRow("Status", lot.status.recyclerLabel())
            LotDetailRow("Collected weight", "%.2f kg".format(Locale.US, lot.actualWeight ?: lot.estimatedWeight))
            LotDetailRow("Estimated weight", "%.2f kg".format(Locale.US, lot.estimatedWeight))
            LotDetailRow("Actual weight", "%.2f kg".format(Locale.US, lot.actualWeight ?: 0.0))
            LotDetailRow("Estimated value", "₹%.2f".format(Locale.US, lot.estimatedValue))
            LotDetailRow("Actual value", "₹%.2f".format(Locale.US, lot.actualValue ?: 0.0))
            LotDetailRow("Collector", lot.collectorId.ifBlank { "Not recorded" })
            LotDetailRow("Request reference", lot.requestId)
            LotDetailRow("Pickup time", lot.pickupTimestamp ?: "Not recorded")
            LotDetailRow("Handover location", lot.handoverLocation ?: "Not recorded")
            LotDetailRow("Handover time", lot.handoverTimestamp ?: "Not recorded")
            LotDetailRow("Recycler", lot.recyclerId ?: "Unassigned")
        }
    }
}

@Composable
private fun LotDetailRow(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RecyclerRecyclingScreen(nav: NavHostController, lotId: String?) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val lot = lotId?.let(repository::getLot)
    var weightText by remember(lotId) { mutableStateOf(lot?.actualWeight?.toString() ?: "") }
    var materialText by remember(lotId) {
        mutableStateOf(lot?.let { PriceServiceProvider.instance.findMaterial(it.materialId)?.name } ?: "")
    }
    var dateText by remember(lotId) { mutableStateOf("") }
    var facilityText by remember(lotId) { mutableStateOf("") }
    var notesText by remember(lotId) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val weight = weightText.toDoubleOrNull()

    Scaffold(containerColor = Cream, topBar = { AppTopBar(nav, "Record recycling") }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lot == null) {
                RecyclerEmptyMessage("Lot not found. Recycling cannot be recorded.")
                return@Column
            }
            Text("Complete recycling", style = MaterialTheme.typography.headlineSmall)
            Text("Lot ID ${lot.lotId} · ${lot.status.recyclerLabel()}", color = TextMuted)
            Text("Record the processed weight to close the traceable workflow.", color = TextMuted)
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Processed weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = materialText,
                onValueChange = { materialText = it },
                label = { Text("Actual material") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Recycling date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") }
            )
            OutlinedTextField(
                value = facilityText,
                onValueChange = { facilityText = it },
                label = { Text("Recycling facility") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (weight == null || !weight.isFinite() || weight <= 0 || materialText.isBlank()) {
                        error = "Enter a processed weight greater than zero and actual material."
                    } else {
                        try {
                            repository.recordRecycling(
                                lot.lotId,
                                recyclerId,
                                weight,
                                materialText,
                                facilityText,
                                notesText.trim(),
                                dateText.trim()
                            )
                            nav.navigate(Routes.RECYCLER_HISTORY) {
                                popUpTo(Routes.RECYCLER_DASHBOARD)
                            }
                        } catch (exception: IllegalArgumentException) {
                            error = exception.message ?: "Unable to record recycling."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(13.dp)
            ) { Text("Mark as recycled", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun RecyclerPaymentsScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val payments = repository.getRecyclerPayments(recyclerId)
    val total = payments.filter { it.status.name == "COMPLETED" }.sumOf { it.amount }
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Payments") },
        bottomBar = { RecyclerBottomBar(nav, Routes.RECYCLER_PAYMENTS) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = Green) {
                    Column(Modifier.fillMaxWidth().padding(21.dp)) {
                        Text("Total received", color = Color.White.copy(alpha = .8f))
                        Text("₹%.2f".format(Locale.US, total), color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Bold)
                        Text("${payments.size} transactions", color = Color.White.copy(alpha = .8f))
                    }
                }
            }
            item { Text("Payment history", style = MaterialTheme.typography.titleLarge) }
            if (payments.isEmpty()) {
                item { RecyclerEmptyMessage("Completed payments will appear here after a lot is confirmed.") }
            } else {
                items(payments, key = { it.id }) { PaymentRow(it) }
            }
        }
    }
}

@Composable
private fun PaymentRow(transaction: Transaction) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Green)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Lot ${transaction.lotId}", fontWeight = FontWeight.Bold)
                Text(transaction.status.name, color = TextMuted, fontSize = 12.sp)
            }
            Text("₹%.2f".format(Locale.US, transaction.amount), color = Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecyclerHistoryScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val lots = repository.getRecyclerHistory(recyclerId)
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Recycling history") },
        bottomBar = { RecyclerBottomBar(nav, Routes.RECYCLER_HISTORY) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Completed recycling", style = MaterialTheme.typography.headlineSmall)
                Text("Every record keeps its original Lot ID for traceability.", color = TextMuted)
            }
            if (lots.isEmpty()) {
                item { RecyclerEmptyMessage("Your recycled lots will appear here.") }
            } else {
                items(lots, key = { it.lotId }) { lot ->
                    RecyclerLotCard(lot) { nav.navigate(Routes.recyclerLotDetails(lot.lotId)) }
                }
            }
        }
    }
}

@Composable
fun RecyclerProfileScreen(nav: NavHostController) {
    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Recycler profile") },
        bottomBar = { RecyclerBottomBar(nav, Routes.RECYCLER_PROFILE) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(22.dp))
            Surface(shape = RoundedCornerShape(48.dp), color = GreenLight, modifier = Modifier.size(92.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Recycling, null, tint = Green, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Local recycler", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Recycler ID: $recyclerId", color = TextMuted)
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
private fun RecyclerLinkCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Green)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextMuted, fontSize = 13.sp)
            }
            Icon(Icons.Default.ArrowForward, null, tint = Green)
        }
    }
}

@Composable
private fun RecyclerEmptyMessage(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Text(message, color = TextMuted, modifier = Modifier.fillMaxWidth().padding(20.dp))
    }
}

@Composable
private fun RecyclerBottomBar(nav: NavHostController, selected: String) {
    fun go(route: String) {
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.RECYCLER_DASHBOARD) { saveState = true }
        }
    }
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected == Routes.RECYCLER_DASHBOARD, { go(Routes.RECYCLER_DASHBOARD) }, { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected == Routes.RECYCLER_AVAILABLE_LOTS, { go(Routes.RECYCLER_AVAILABLE_LOTS) }, { Icon(Icons.Default.ListAlt, null) }, label = { Text("Lots") })
        NavigationBarItem(selected == Routes.RECYCLER_PAYMENTS, { go(Routes.RECYCLER_PAYMENTS) }, { Icon(Icons.Default.AccountBalanceWallet, null) }, label = { Text("Payments") })
        NavigationBarItem(selected == Routes.RECYCLER_PROFILE, { go(Routes.RECYCLER_PROFILE) }, { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}

private fun LotStatus.recyclerLabel(): String = when (this) {
    LotStatus.HANDED_OVER -> "HANDED OVER"
    LotStatus.RECYCLER_CONFIRMED -> "RECEIVED"
    LotStatus.PAID -> "PAID"
    LotStatus.RECYCLED -> "RECYCLED"
    else -> name
}
