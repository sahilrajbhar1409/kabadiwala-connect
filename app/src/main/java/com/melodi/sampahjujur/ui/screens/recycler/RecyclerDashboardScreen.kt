package com.melodi.sampahjujur.ui.screens.recycler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.model.CollectionRequest
import com.melodi.sampahjujur.model.CollectionStatus
import com.melodi.sampahjujur.model.Recycler
import com.melodi.sampahjujur.ui.screens.collector.flow.StatusBadgeLarge
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclerDashboardScreen(
    viewModel: CollectionViewModel,
    onRequestClick: (String) -> Unit,
    onSwitchToCollector: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var facilityMenuExpanded by remember { mutableStateOf(false) }

    val activeRecyclers = uiState.authorizedRecyclers
    val currentFacility = activeRecyclers.firstOrNull { it.id == uiState.selectedRecyclerId }
        ?: activeRecyclers.firstOrNull()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
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
                            "Recycler Intake Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            currentFacility?.businessName ?: "Authorized Formal Recycler",
                            fontSize = 12.sp,
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
                    FilledTonalButton(
                        onClick = onSwitchToCollector,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryGreenLight.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collector Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Facility Selector Card
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
                                Text("OPERATING FACILITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                Text(
                                    text = currentFacility?.name ?: "Select Facility",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Box {
                                OutlinedButton(
                                    onClick = { facilityMenuExpanded = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Switch", fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = facilityMenuExpanded,
                                    onDismissRequest = { facilityMenuExpanded = false }
                                ) {
                                    activeRecyclers.forEach { recycler ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(recycler.name, fontWeight = FontWeight.Bold)
                                                    Text(recycler.facilityLocation, fontSize = 11.sp, color = MediumGray)
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectRecyclerFacility(recycler.id)
                                                facilityMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = PrimaryGreenDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CPCB/SPCB Lic: ${currentFacility?.licenseNumber ?: "CPCB/EPR/2026/VALID"}",
                                fontSize = 12.sp,
                                color = DarkGray
                            )
                        }
                    }
                }
            }

            // Quick Stats Banner
            item {
                val requests = uiState.recyclerRequests
                val pendingAction = requests.count { it.status == CollectionStatus.RECYCLER_ASSIGNED.name || it.status == CollectionStatus.COLLECTED.name || it.status == CollectionStatus.HANDED_OVER.name }
                val completedCount = requests.count { it.isPaid() }
                val totalDivertedKg = requests.filter { it.isPaid() }.sumOf { it.totalWeight }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Action Needed", fontSize = 11.sp, color = TextSecondary)
                            Text("$pendingAction", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryOrange)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Settled Lots", fontSize = 11.sp, color = TextSecondary)
                            Text("$completedCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreenDark)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Diverted (kg)", fontSize = 11.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "%.0f", totalDivertedKg), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.recyclerSearchQuery,
                    onValueChange = { viewModel.filterRecyclerByQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by Lot ID (e.g. KC-2026...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MediumGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = LightGray
                    )
                )
            }

            // Status Filter Tabs
            item {
                val filters = listOf("ALL", "ACTIVE", "COMPLETED")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { f ->
                        val isSelected = uiState.recyclerStatusFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.filterRecyclerByStatus(f) },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            )
                        )
                    }
                }
            }

            // Header for Request List
            item {
                Text(
                    text = "Incoming Lots (${uiState.filteredRecyclerRequests.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            // Requests List
            if (uiState.filteredRecyclerRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inbox, contentDescription = null, tint = MediumGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No incoming scrap lots found", color = MediumGray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                items(uiState.filteredRecyclerRequests) { req ->
                    RecyclerRequestCard(
                        request = req,
                        onClick = { onRequestClick(req.lotId) },
                        onAccept = { viewModel.acceptRequestByRecycler(req.lotId) },
                        onReject = { viewModel.rejectRequestByRecycler(req.lotId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecyclerRequestCard(
    request: CollectionRequest,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
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
                Text(
                    text = request.lotId,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                StatusBadgeLarge(status = request.getCollectionStatus())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Collector: " + request.collectorId.take(12),
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "Materials: " + request.materials.joinToString { it.category } + " (${request.totalWeight} kg)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGray
            )
            Text(
                text = "Quoted Estimate: ₹" + String.format(Locale.US, "%,.2f", request.quotedPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreenDark
            )

            if (request.getCollectionStatus() == CollectionStatus.RECYCLER_ASSIGNED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Accept Lot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Intake Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
