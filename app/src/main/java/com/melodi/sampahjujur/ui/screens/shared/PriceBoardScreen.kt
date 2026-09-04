package com.melodi.sampahjujur.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.R
import com.melodi.sampahjujur.model.PriceInfo
import com.melodi.sampahjujur.ui.components.PriceHistoryChart
import com.melodi.sampahjujur.ui.theme.PrimaryGreen
import com.melodi.sampahjujur.utils.TtsManager
import com.melodi.sampahjujur.utils.WastePriceCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceBoardScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    val categories = remember { WastePriceCalculator.getWasteTypes() }
    var selectedCategory by remember { mutableStateOf("PCB") }
    var inputWeight by remember { mutableStateOf("2.5") }

    val currentPriceInfo by remember(selectedCategory) {
        derivedStateOf { WastePriceCalculator.getPriceInfo(selectedCategory) }
    }

    val historyRecords by remember(selectedCategory) {
        derivedStateOf { WastePriceCalculator.getMockPriceHistory(selectedCategory) }
    }

    val weightValue by remember(inputWeight) {
        derivedStateOf { inputWeight.toDoubleOrNull() ?: 0.0 }
    }

    val estimatedValue by remember(selectedCategory, weightValue) {
        derivedStateOf { WastePriceCalculator.calculateValue(selectedCategory, weightValue) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.price_board),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            ttsManager.speakPrice(
                                currentPriceInfo.material,
                                currentPriceInfo.buyingPrice,
                                weightValue,
                                estimatedValue
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = stringResource(R.string.speak_price),
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selector Chips
            item {
                Text(
                    text = stringResource(R.string.select_material),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category.equals(selectedCategory, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.Black
                            )
                        )
                    }
                }
            }

            // Current Price & Rate Board Card
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
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentPriceInfo.material,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${currentPriceInfo.location} • ${currentPriceInfo.subcategory}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Trend Badge
                            TrendBadge(trend = currentPriceInfo.trend)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Rate Display
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "₹${currentPriceInfo.buyingPrice.toInt()}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreen
                            )
                            Text(
                                text = "/${currentPriceInfo.unit}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Market Range & Recycler Offer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.market_range),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${currentPriceInfo.marketMin.toInt()} – ₹${currentPriceInfo.marketMax.toInt()}/kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.recycler_offer),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${currentPriceInfo.recyclerOffer.toInt()}/kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }

            // Weight & Estimated Value Calculator Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Calculate Estimated Value",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text(stringResource(R.string.weight_kg)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            supportingText = {
                                if (inputWeight.isNotBlank() && weightValue <= 0) {
                                    Text(
                                        text = stringResource(R.string.invalid_weight),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Estimated Value Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.estimated_value),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${weightValue} kg × ₹${currentPriceInfo.buyingPrice.toInt()}/kg",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "₹${estimatedValue.toInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                }
            }

            // Compose Canvas Price History Line Chart
            item {
                PriceHistoryChart(
                    history = historyRecords,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun TrendBadge(trend: String) {
    val (icon, color, textKey) = when (trend) {
        PriceInfo.TREND_INCREASING -> Triple(Icons.Default.TrendingUp, Color(0xFF2E7D32), R.string.increasing)
        PriceInfo.TREND_DECREASING -> Triple(Icons.Default.TrendingDown, Color(0xFFC62828), R.string.decreasing)
        else -> Triple(Icons.Default.TrendingFlat, Color(0xFFF57C00), R.string.stable)
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Trend",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(textKey),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
