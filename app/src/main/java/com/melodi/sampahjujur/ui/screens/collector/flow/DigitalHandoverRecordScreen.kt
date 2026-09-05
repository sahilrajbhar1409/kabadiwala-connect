package com.melodi.sampahjujur.ui.screens.collector.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalHandoverRecordScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onViewTraceabilityClick: (String) -> Unit
) {
    val requestFlow = remember(lotId) { viewModel.observeRequest(lotId) }
    val request by requestFlow.collectAsState(initial = null)
    val handoverFlow = remember(lotId) { viewModel.observeHandover(lotId) }
    val handover by handoverFlow.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Digital Handover Record",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onViewTraceabilityClick(lotId) }) {
                        Icon(Icons.Default.Timeline, contentDescription = "Traceability", tint = PrimaryGreen)
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
            val record = handover

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Official Receipt Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(3.dp),
                        border = BorderStroke(1.dp, PrimaryGreenLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header Stamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("HANDOVER RECEIPT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                    Text(
                                        text = record?.handoverReference ?: "HD-RECORD",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Surface(
                                    color = PrimaryGreenLight.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = PrimaryGreenDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("VERIFIED", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreenDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Lot ID & Dates
                            ReceiptRow("Traceable Lot ID", req.lotId, isMono = true)
                            ReceiptRow("Collector ID", req.collectorId.take(12))
                            ReceiptRow("Recycler", req.recyclerName.ifBlank { "Authorized Recycler" })
                            ReceiptRow("Handover Date", formatTime(record?.timestamp ?: req.createdAt))

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Materials & Weight
                            req.materials.forEach { mat ->
                                ReceiptRow("Material Item", "${mat.category} (${mat.approximateWeight} kg)")
                            }
                            ReceiptRow("Total Verified Weight", "${record?.weight ?: req.totalWeight} kg")
                            ReceiptRow("Quoted Estimate", "₹${String.format("%,.2f", req.quotedPrice)}")

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Final Value & Payment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Final Sale Value", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "₹${String.format("%,.2f", if (req.finalSaleValue > 0) req.finalSaleValue else req.quotedPrice)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            ReceiptRow("Payment Method", req.paymentMethod ?: "Cash")
                            ReceiptRow("Payment Status", req.paymentStatus)

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Location
                            ReceiptRow("Handover Location", record?.handoverLocation ?: req.collectionLocation)
                        }
                    }
                }

                // Action Buttons
                item {
                    Button(
                        onClick = { onViewTraceabilityClick(req.lotId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Complete Lot Traceability Chain", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("Back to Dashboard", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = TextPrimary
        )
    }
}

private fun formatTime(millis: Long): String {
    if (millis == 0L) return "N/A"
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
}
