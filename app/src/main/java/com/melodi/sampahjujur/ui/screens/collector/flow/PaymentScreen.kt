package com.melodi.sampahjujur.ui.screens.collector.flow

import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.model.PaymentMethod
import com.melodi.sampahjujur.model.UpiHelper
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onPaymentCompleted: (String) -> Unit
) {
    val requestFlow = remember(lotId) { viewModel.observeRequest(lotId) }
    val request by requestFlow.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var upiRefInput by remember { mutableStateOf("") }

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
                        Text("Record Payment", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Lot: $lotId", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MediumGray)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            val req = request!!
            val payableAmount = if (req.finalSaleValue > 0) req.finalSaleValue else req.quotedPrice

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Big Amount Card
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Settlement Amount", fontSize = 13.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${String.format("%,.2f", payableAmount)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreenDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "From: ${req.recyclerName.ifBlank { "Authorized Recycler" }}",
                                fontSize = 13.sp,
                                color = DarkGray
                            )
                        }
                    }
                }

                // Payment Method Selector Tabs
                item {
                    TabRow(
                        selectedTabIndex = if (selectedMethod == PaymentMethod.CASH) 0 else 1,
                        containerColor = Color.White,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedMethod == PaymentMethod.CASH,
                            onClick = { selectedMethod = PaymentMethod.CASH },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cash Payment", fontWeight = FontWeight.Bold)
                                }
                            },
                            selectedContentColor = PrimaryGreen,
                            unselectedContentColor = MediumGray
                        )
                        Tab(
                            selected = selectedMethod == PaymentMethod.UPI,
                            onClick = { selectedMethod = PaymentMethod.UPI },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("UPI Payment", fontWeight = FontWeight.Bold)
                                }
                            },
                            selectedContentColor = PrimaryGreen,
                            unselectedContentColor = MediumGray
                        )
                    }
                }

                // Tab Content
                if (selectedMethod == PaymentMethod.CASH) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreenDark)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Cash Hand-to-Hand Settlement",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "• Works 100% offline without internet.\n• Instant receipt generated upon confirmation.\n• Finalizes the lot transaction securely.",
                                    fontSize = 13.sp,
                                    color = DarkGray,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        viewModel.recordCashPayment(req.lotId, payableAmount) {
                                            onPaymentCompleted(req.lotId)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = "Confirm Cash Received (₹${payableAmount.toInt()})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "UPI Digital Payment (Optional)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pay using your preferred UPI app directly to the recycler or receive payment to your account.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        val upiUri = UpiHelper.buildUpiUri(
                                            payeeVpa = "recycler@upi",
                                            payeeName = req.recyclerName.ifBlank { "Authorized Recycler" },
                                            amount = payableAmount,
                                            transactionRef = req.lotId,
                                            transactionNote = "Payment for Lot ${req.lotId}"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                        try {
                                            context.startActivity(Intent.createChooser(intent, "Pay with UPI"))
                                        } catch (e: Exception) {
                                            // Fallback if no UPI app installed
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open UPI App", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = upiRefInput,
                                    onValueChange = { upiRefInput = it },
                                    label = { Text("UPI UTR / Transaction Reference") },
                                    placeholder = { Text("e.g. UPI/2026/987654321") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        val ref = upiRefInput.ifBlank { "UPI-${System.currentTimeMillis()}" }
                                        viewModel.recordUpiPaymentSuccess(req.lotId, payableAmount, ref) {
                                            onPaymentCompleted(req.lotId)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(28.dp),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = "Confirm UPI Payment Received",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
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
