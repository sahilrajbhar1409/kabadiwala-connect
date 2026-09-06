package com.melodi.sampahjujur.ui.screens.collector.flow

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melodi.sampahjujur.repository.PricePoint
import com.melodi.sampahjujur.repository.PriceRepository
import com.melodi.sampahjujur.viewmodel.PriceBoardViewModel
import java.util.Locale

@Composable
fun PriceBoardScreen(viewModel: PriceBoardViewModel = hiltViewModel(), onBackClick: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() }
        onDispose { tts?.shutdown() }
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Price Board", style = MaterialTheme.typography.headlineMedium) }
        item { Button(onClick = onBackClick) { Text("Back") } }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PriceRepository.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.select(category) },
                        label = { Text(category) }
                    )
                }
            }
        }
        if (state.loading && state.prices.isEmpty()) item { CircularProgressIndicator() }
        state.error?.let { message -> item { Text(if (state.prices.isEmpty()) "No cached prices: $message" else "Showing latest cached prices") } }
        items(state.prices) { price ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(price.name, style = MaterialTheme.typography.titleLarge)
                    Text("${price.price} / ${price.unit}")
                    Text("Updated: ${price.recordedAt} (${price.source})")
                    Button(onClick = { tts?.speak("${price.name}, ${price.price} rupees per ${price.unit}", TextToSpeech.QUEUE_FLUSH, null, price.category) }) {
                        Text("Read price")
                    }
                }
            }
        }
        item { Text("Historical prices", style = MaterialTheme.typography.titleLarge) }
        if (state.history.isEmpty() && !state.loading) item { Text("No historical prices available for this material.") }
        val maxPrice = state.history.maxOfOrNull { it.price } ?: 0.0
        items(state.history) { point ->
            Column {
                Text("${point.recordedAt}: ${point.price} / ${point.unit}")
                if (maxPrice > 0) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (point.price / maxPrice).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (!state.loading && state.prices.isEmpty()) item { Text("No prices available yet. Connect to the backend to refresh.") }
    }
}

