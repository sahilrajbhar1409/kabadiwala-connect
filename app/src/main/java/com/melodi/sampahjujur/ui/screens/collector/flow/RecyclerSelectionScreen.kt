package com.melodi.sampahjujur.ui.screens.collector.flow

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
import com.melodi.sampahjujur.model.Recycler
import com.melodi.sampahjujur.ui.theme.*
import com.melodi.sampahjujur.viewmodel.CollectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclerSelectionScreen(
    lotId: String,
    viewModel: CollectionViewModel,
    onBackClick: () -> Unit,
    onRecyclerAssigned: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRecycler by remember { mutableStateOf<Recycler?>(null) }
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
                            "Select Authorized Recycler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Lot: $lotId",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            selectedRecycler?.let { recycler ->
                                viewModel.assignRecycler(lotId, recycler) {
                                    onRecyclerAssigned(lotId)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = selectedRecycler != null && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (selectedRecycler == null) "Select a Recycler to Continue" else "Assign ${selectedRecycler?.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreenLight.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = PrimaryGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Only government authorized & licensed recycling hubs are listed to ensure EPR compliance and fair rates.",
                            fontSize = 12.sp,
                            color = PrimaryGreenDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            items(uiState.authorizedRecyclers) { recycler ->
                val isSelected = selectedRecycler?.id == recycler.id
                RecyclerCard(
                    recycler = recycler,
                    isSelected = isSelected,
                    onSelect = { selectedRecycler = recycler }
                )
            }
        }
    }
}

@Composable
fun RecyclerCard(
    recycler: Recycler,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryGreen) else null,
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recycler.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = recycler.businessName,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = PrimaryGreenLight.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Authorized",
                            tint = PrimaryGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUTHORIZED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & License
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = recycler.facilityLocation,
                    fontSize = 12.sp,
                    color = DarkGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lic: ${recycler.licenseNumber}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Accepted Materials
            Text("Accepted Materials:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recycler.acceptedMaterials) { mat ->
                    Surface(
                        color = BackgroundGray,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = mat,
                            fontSize = 11.sp,
                            color = DarkGray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Offered Base Rate", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "₹${recycler.offeredRatePerKg.toInt()}/kg",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreenDark
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                )
            }
        }
    }
}
