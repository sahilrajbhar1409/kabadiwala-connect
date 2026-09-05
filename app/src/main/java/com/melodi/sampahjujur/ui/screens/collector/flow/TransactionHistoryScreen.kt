package com.melodi.sampahjujur.ui.screens.collector.flow

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
import com.melodi.sampahjujur.model.PaymentStatus
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterOptions = listOf("ALL", "ACTIVE", "COMPLETED", "PENDING_PAYMENT")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transaction History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar by Lot ID
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.filterByQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Search by Lot ID (e.g. KC-2026-...) or Recycler") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MediumGray) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.filterByQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = LightGray
                )
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = uiState.selectedStatusFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterByStatus(filter) },
                        label = { Text(filter.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MediumGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No collection records found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredRequests) { req ->
                        TransactionHistoryCard(
                            request = req,
                            onClick = { onTransactionClick(req.lotId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryCard(
    request: CollectionRequest,
    onClick: () -> Unit
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
            // Row 1: Lot ID + Status Badge
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

            // Row 2: Material & Weight
            Text(
                text = "Material: " + request.materials.joinToString { it.category } + " • ${request.totalWeight} kg",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGray
            )

            // Row 3: Recycler
            Text(
                text = "Recycler: ${request.recyclerName.ifBlank { "Pending assignment" }}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            // Row 4: Date
            Text(
                text = "Date: " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(request.createdAt)),
                fontSize = 11.sp,
                color = MediumGray
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Row 5: Value & Payment info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Final Sale Value", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "₹${String.format("%,.2f", if (request.finalSaleValue > 0) request.finalSaleValue else request.quotedPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = PrimaryGreenDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (request.isPaid()) PrimaryGreenLight.copy(alpha = 0.4f) else StatusPending.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${request.paymentMethod ?: "CASH"} • ${request.paymentStatus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (request.isPaid()) PrimaryGreenDark else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
                }
            }
        }
    }
}
