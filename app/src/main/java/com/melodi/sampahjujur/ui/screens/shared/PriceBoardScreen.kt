package com.melodi.sampahjujur.ui.screens.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melodi.sampahjujur.repository.Person3PriceCategories
import com.melodi.sampahjujur.ui.theme.BackgroundGray
import com.melodi.sampahjujur.ui.theme.PrimaryGreen
import com.melodi.sampahjujur.ui.theme.PrimaryGreenDark
import com.melodi.sampahjujur.viewmodel.PriceBoardViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PriceBoardScreen(
    onBackClick: () -> Unit,
    viewModel: PriceBoardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price Board", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Text("Back") } },
                actions = { IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, "Refresh") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Person3PriceCategories.all.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategory == category.backendId,
                            onClick = { viewModel.selectCategory(category.backendId) },
                            label = { Text(category.displayName) }
                        )
                    }
                }
            }
            item {
                if (state.isRefreshing) CircularProgressIndicator(color = PrimaryGreen)
                else {
                    val price = state.selectedPrice
                    if (price == null) {
                        Text("Current price unavailable", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Refresh when online to load the shared reference price.", color = Color.DarkGray)
                    } else {
                        Text(
                            "${price.category.displayName}: ₹${price.currentPrice}/kg",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark
                        )
                        Text("Market range: ₹${price.minPrice} - ₹${price.maxPrice}")
                        if (state.isOffline) Text("Using cached price", color = Color.DarkGray)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = state.weightInput,
                    onValueChange = viewModel::setWeight,
                    label = { Text("Weight") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                val weight = state.weightInput.toDoubleOrNull()
                val rate = state.selectedPrice?.currentPrice
                if (weight != null && weight > 0 && rate != null) {
                    Text("Estimated value: ₹${weight * rate}", fontWeight = FontWeight.Bold)
                }
            }
            item {
                Text("Historical trend", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                PriceTrendChart(state.trendPoints)
            }
            items(state.prices) { price ->
                Text("${price.displayName}: ₹${price.currentPrice}/${price.unit}")
            }
        }
    }
}

@Composable
private fun PriceTrendChart(points: List<Double>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        if (points.size < 2) return@Canvas
        val max = points.maxOrNull() ?: 1.0
        val min = points.minOrNull() ?: 0.0
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val step = size.width / (points.size - 1)
        points.zipWithNext().forEachIndexed { index, pair ->
            drawLine(
                color = PrimaryGreen,
                start = Offset(index * step, size.height - ((pair.first - min) / span * size.height).toFloat()),
                end = Offset((index + 1) * step, size.height - ((pair.second - min) / span * size.height).toFloat()),
                strokeWidth = 4f
            )
        }
    }
}
