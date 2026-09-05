package com.melodi.sampahjujur.ui.screens.recycler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.model.CollectionStatus
import com.melodi.sampahjujur.model.PaymentMethod
import com.melodi.sampahjujur.ui.screens.collector.flow.StatusBadgeLarge
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclerRequestDetailScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onViewReceiptClick: (String) -> Unit,
    onViewTraceabilityClick: (String) -> Unit
) {
    val requestFlow = remember(lotId) { viewModel.observeRequest(lotId) }
    val request by requestFlow.collectAsState(initial = null)
    val handoverFlow = remember(lotId) { viewModel.observeHandover(lotId) }
    val handoverRecord by handoverFlow.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Weighbridge state
    var verifiedWeightInput by remember { mutableStateOf("") }
    var finalAmountInput by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Payment state
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var upiRefInput by remember { mutableStateOf("") }

    LaunchedEffect(request) {
        if (!isInitialized && request != null) {
            verifiedWeightInput = request!!.totalWeight.toString()
            finalAmountInput = (if (request!!.finalSaleValue > 0) request!!.finalSaleValue else request!!.quotedPrice).toString()
            isInitialized = true
        }
    }

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
                        Text("Intake & Settlement", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(lotId, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MediumGray)
                    }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            val req = request!!
            val status = req.getCollectionStatus()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Status
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
                                    Text("LOT IDENTIFIER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MediumGray)
                                    Text(
                                        text = req.lotId,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                }
                                StatusBadgeLarge(status = status)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Collector: ${req.collectorId.take(14)}", fontSize = 13.sp, color = DarkGray)
                            Text("Registered: " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.createdAt)), fontSize = 12.sp, color = MediumGray)
                            if (req.collectionLocation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(req.collectionLocation, fontSize = 12.sp, color = TextSecondary)
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
                            Text("Scrap Material Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            req.materials.forEach { mat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mat.category, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${mat.approximateWeight} kg @ ₹${mat.currentBuyingRate.toInt()}/kg", fontSize = 13.sp, color = DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Declared Gross Weight", fontSize = 13.sp, color = TextSecondary)
                                Text("${req.totalWeight} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Quoted Estimated Value", fontSize = 13.sp, color = TextSecondary)
                                Text("₹${String.format(Locale.US, "%,.2f", req.quotedPrice)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreenDark)
                            }
                        }
                    }
                }

                // ACTION SECTION BASED ON STATUS

                // 1. Recycler Assigned: Accept / Reject
                if (status == CollectionStatus.RECYCLER_ASSIGNED) {
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
                                Text("Facility Intake Decision", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Review the scrap lot category and location. Accept to schedule logistics intake or reject if material is not compliant with your facility license.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.rejectRequestByRecycler(req.lotId) },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("Reject Lot", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.acceptRequestByRecycler(req.lotId) },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                    ) {
                                        Text("Accept Lot", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Accepted / Scheduled / In Transit
                if (status == CollectionStatus.ACCEPTED) {
                    item {
                        Button(
                            onClick = { viewModel.transitionStatus(req.lotId, CollectionStatus.SCHEDULED) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule Facility Pickup", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (status == CollectionStatus.SCHEDULED) {
                    item {
                        Button(
                            onClick = { viewModel.transitionStatus(req.lotId, CollectionStatus.COLLECTED) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusInProgress)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm Material Arrived at Gate", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Ready for Weighbridge / Handover Verification
                if (status == CollectionStatus.COLLECTED || status == CollectionStatus.HANDOVER_PENDING || status == CollectionStatus.HANDED_OVER) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, PrimaryGreen)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = PrimaryGreenDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Weighbridge Scale Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Enter verified scale net weight and agreed settlement amount to certify formal intake.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = verifiedWeightInput,
                                    onValueChange = { verifiedWeightInput = it },
                                    label = { Text("Certified Net Weight (kg)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Text("kg  ", fontWeight = FontWeight.Bold, color = MediumGray) }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = finalAmountInput,
                                    onValueChange = { finalAmountInput = it },
                                    label = { Text("Final Settlement Valuation (₹)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Text("₹  ", fontWeight = FontWeight.Bold, color = PrimaryGreenDark) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val wt = verifiedWeightInput.toDoubleOrNull() ?: req.totalWeight
                                        val amt = finalAmountInput.toDoubleOrNull() ?: req.quotedPrice
                                        val recId = req.recyclerId ?: uiState.selectedRecyclerId
                                        viewModel.confirmWeighbridgeHandover(req.lotId, recId, wt, amt) {}
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(26.dp),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.Verified, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Confirm Handover & Issue Certificate", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Payment Settlement
                if (status == CollectionStatus.PAYMENT_PENDING) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, SecondaryOrange)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payment, contentDescription = null, tint = SecondaryOrange)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Record Payment Settlement", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Amount Payable: ₹" + String.format(Locale.US, "%,.2f", req.finalSaleValue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedPaymentMethod == PaymentMethod.CASH,
                                        onClick = { selectedPaymentMethod = PaymentMethod.CASH },
                                        label = { Text("Cash Settlement") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = selectedPaymentMethod == PaymentMethod.UPI,
                                        onClick = { selectedPaymentMethod = PaymentMethod.UPI },
                                        label = { Text("UPI Transfer") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (selectedPaymentMethod == PaymentMethod.UPI) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = upiRefInput,
                                        onValueChange = { upiRefInput = it },
                                        label = { Text("UPI UTR / Ref Number") },
                                        placeholder = { Text("e.g. UPI/2026/890123") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val ref = if (selectedPaymentMethod == PaymentMethod.CASH) {
                                            "CASH-${System.currentTimeMillis()}"
                                        } else {
                                            upiRefInput.ifBlank { "UPI-${System.currentTimeMillis()}" }
                                        }
                                        viewModel.recordPaymentByRecycler(
                                            lotId = req.lotId,
                                            method = selectedPaymentMethod,
                                            amount = req.finalSaleValue,
                                            reference = ref
                                        ) {}
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(26.dp),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Record Payment Settled (PAID)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Completed
                if (status == CollectionStatus.COMPLETED) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PrimaryGreenLight.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreenDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Intake & Settlement Completed", fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Handover Reference: ${handoverRecord?.handoverReference ?: "HO-KC-RECORD"}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Settled: ₹${String.format(Locale.US, "%,.2f", req.finalSaleValue)} via ${req.paymentMethod ?: "CASH"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PrimaryGreenDark
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onViewReceiptClick(req.lotId) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Digital Handover Certificate", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Traceability Audit Button
                item {
                    OutlinedButton(
                        onClick = { onViewTraceabilityClick(req.lotId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify Complete Traceability Chain", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
