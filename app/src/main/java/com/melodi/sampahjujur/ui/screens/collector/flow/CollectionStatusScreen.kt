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
import com.melodi.sampahjujur.model.CollectionStatus
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionStatusScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit
) {
    val requestFlow = remember(lotId) { viewModel.observeRequest(lotId) }
    val request by requestFlow.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Collection Status Journey", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(lotId, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MediumGray)
                    }
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
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            val req = request!!
            val currentStatus = req.getCollectionStatus()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                            Text("Current Milestone", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentStatus.displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreenDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lot ID: ${req.lotId} • Recycler: ${req.recyclerName.ifBlank { "Unassigned" }}",
                                fontSize = 13.sp,
                                color = DarkGray
                            )
                        }
                    }
                }

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
                            Text("Lifecycle Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            val steps = listOf(
                                Triple("1. Lot Created", "Lot ID registered on device", req.createdAt),
                                Triple("2. Recycler Assigned", req.recyclerName.ifBlank { "Authorized facility pending" }, req.scheduledAt),
                                Triple("3. Material Collected", "Physical scrap collected & weighed", req.collectedAt),
                                Triple("4. Handover to Recycler", "Lot delivered to recycling facility", req.handedOverAt),
                                Triple("5. Payment Settled", if (req.isPaid()) "Paid via ${req.paymentMethod ?: "Cash"}" else "Awaiting settlement", if (req.isPaid()) req.handedOverAt else null),
                                Triple("6. Complete & Certified", "Digital Handover Record archived", if (req.isCompleted()) req.handedOverAt else null)
                            )

                            steps.forEachIndexed { index, (title, desc, timestamp) ->
                                val isPassed = timestamp != null && timestamp > 0L
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isPassed) PrimaryGreen else LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isPassed) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            } else {
                                                Text("${index + 1}", color = DarkGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                        if (index < steps.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(36.dp)
                                                    .background(if (isPassed) PrimaryGreen else LightGray)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isPassed) TextPrimary else MediumGray)
                                        Text(desc, fontSize = 12.sp, color = TextSecondary)
                                        if (timestamp != null && timestamp > 0L) {
                                            Text(
                                                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp)),
                                                fontSize = 11.sp,
                                                color = PrimaryGreenDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
