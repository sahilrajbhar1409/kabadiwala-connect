package com.melodi.sampahjujur.ui.screens.collector.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.model.CollectionRequest
import com.melodi.sampahjujur.model.CollectionStatus
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionRequestDetailsScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onAssignRecyclerClick: (String) -> Unit,
    onInitiateHandoverClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit,
    onViewReceiptClick: (String) -> Unit,
    onViewTraceabilityClick: (String) -> Unit
) {
    val requestFlow = remember(lotId) { viewModel.observeRequest(lotId) }
    val request by requestFlow.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Lot Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            lotId,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MediumGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onViewTraceabilityClick(lotId) }) {
                        Icon(Icons.Default.Timeline, contentDescription = "Traceability Chain", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            request?.let { req ->
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (req.getCollectionStatus()) {
                            CollectionStatus.CREATED -> {
                                Button(
                                    onClick = { onAssignRecyclerClick(req.lotId) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Select & Assign Recycler", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.RECYCLER_ASSIGNED -> {
                                Button(
                                    onClick = {
                                        viewModel.transitionStatus(req.lotId, CollectionStatus.ACCEPTED)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Recycler Accepted Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.ACCEPTED -> {
                                Button(
                                    onClick = {
                                        viewModel.transitionStatus(req.lotId, CollectionStatus.SCHEDULED)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Schedule Collection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.SCHEDULED -> {
                                Button(
                                    onClick = {
                                        viewModel.transitionStatus(req.lotId, CollectionStatus.COLLECTED)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusInProgress),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Mark Material Collected", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.COLLECTED -> {
                                Button(
                                    onClick = { onInitiateHandoverClick(req.lotId) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Initiate Handover to Recycler", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.HANDED_OVER -> {
                                Button(
                                    onClick = {
                                        // Trigger recycler confirmation
                                        viewModel.confirmRecyclerHandover(req.lotId, req.recyclerId ?: "") {}
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Confirm Recycler Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.PAYMENT_PENDING -> {
                                Button(
                                    onClick = { onPaymentClick(req.lotId) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Process Payment (Cash / UPI)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            CollectionStatus.COMPLETED -> {
                                Button(
                                    onClick = { onViewReceiptClick(req.lotId) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("View Digital Handover Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                            else -> {
                                OutlinedButton(
                                    onClick = { onViewTraceabilityClick(req.lotId) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("View Traceability Log", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (request == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            val req = request!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("LOT ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MediumGray)
                                    Text(
                                        text = req.lotId,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                }
                                StatusBadgeLarge(status = req.getCollectionStatus())
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Weight", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = "${req.totalWeight} kg",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Final Value / Quoted", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = "₹${String.format("%,.2f", if (req.finalSaleValue > 0) req.finalSaleValue else req.quotedPrice)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryGreenDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Recycler Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Assigned Authorized Recycler",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (req.recyclerId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = SecondaryOrange)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No recycler assigned yet", color = SecondaryOrange, fontSize = 14.sp)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = req.recyclerName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                }
                                if (req.handoverLocation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(req.handoverLocation, fontSize = 12.sp, color = DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Material Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Scrap Materials Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            req.materials.forEach { mat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundGray, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(mat.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${mat.approximateWeight} kg @ ₹${mat.currentBuyingRate.toInt()}/kg", fontSize = 12.sp, color = MediumGray)
                                    }
                                    Text(
                                        text = "₹${String.format("%,.0f", mat.estimatedValue)}",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }

                // Location & Timestamps Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Location & Timestamps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = req.collectionLocation.ifBlank { "GPS: ${req.latitude}, ${req.longitude}" },
                                    fontSize = 12.sp,
                                    color = DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MediumGray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Created: ${formatTimestamp(req.createdAt)}",
                                    fontSize = 12.sp,
                                    color = MediumGray
                                )
                            }
                        }
                    }
                }

                // Traceability Quick Link
                item {
                    OutlinedCard(
                        onClick = { onViewTraceabilityClick(req.lotId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, tint = PrimaryGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Lot Traceability Audit Trail", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("View verifiable lifecycle from origin to recycler", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadgeLarge(status: CollectionStatus) {
    val (bgColor, textColor) = when (status) {
        CollectionStatus.CREATED -> LightGray to DarkGray
        CollectionStatus.RECYCLER_ASSIGNED -> SecondaryAmber.copy(alpha = 0.2f) to Color(0xFFE65100)
        CollectionStatus.ACCEPTED -> PrimaryGreenLight.copy(alpha = 0.5f) to PrimaryGreenDark
        CollectionStatus.SCHEDULED -> StatusInProgress.copy(alpha = 0.15f) to StatusInProgress
        CollectionStatus.COLLECTED -> StatusInProgress.copy(alpha = 0.2f) to StatusInProgress
        CollectionStatus.HANDED_OVER -> SecondaryOrange.copy(alpha = 0.2f) to Color(0xFFBF360C)
        CollectionStatus.PAYMENT_PENDING -> StatusPending.copy(alpha = 0.2f) to Color(0xFFE65100)
        CollectionStatus.COMPLETED -> PrimaryGreen.copy(alpha = 0.15f) to PrimaryGreenDark
        CollectionStatus.CANCELLED, CollectionStatus.REJECTED -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.displayName.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "N/A"
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
