package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.backend.BackendNetwork
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.components.RealTimeMap
import com.kabadiwalaconnect.ui.components.rememberCurrentLocation
import com.kabadiwalaconnect.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PickupScreen(nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember {
        PickupViewModel(backendRepository = BackendNetwork.create(context))
    }
    var selectedMaterial by remember { mutableStateOf("Paper") }
    var quantity by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val location = rememberCurrentLocation()

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Request pickup") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Text("What do you want to recycle?", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Paper", "Plastic", "Metal", "E-waste").forEach {
                        FilterChip(
                            selected = selectedMaterial == it,
                            onClick = { selectedMaterial = it },
                            label = { Text(it) }
                        )
                    }
                }
            }
            item { Text("Estimated quantity", style = MaterialTheme.typography.titleLarge) }
            item {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter quantity in kg") },
                    trailingIcon = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
            item { Text("Pickup address", style = MaterialTheme.typography.titleLarge) }
            item {
                Surface(shape = RoundedCornerShape(17.dp), color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Green)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Home", fontWeight = FontWeight.Bold)
                            Text("Your saved pickup address", color = TextMuted, fontSize = 13.sp)
                        }
                        Text("Change", color = Green, fontWeight = FontWeight.Bold)
                    }
                }
            }
                item {
                    if (location == null) {
                        Text("Allow location access to use your current pickup location.", color = TextMuted)
                    } else {
                        RealTimeMap(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            item { Text("Preferred time", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Today", "Tomorrow", "Weekend").forEach {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp)
                        ) { Text(it) }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = GreenLight) {
                    Row(modifier = Modifier.padding(18.dp)) {
                        Text("💡")
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Keep recyclable materials separated and dry for better recovery.",
                            color = GreenDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val weight = quantity.toDoubleOrNull()
                        if (weight == null || weight <= 0 || submitting) return@Button
                        submitting = true
                        scope.launch {
                            val result = viewModel.submitToBackend(
                                materialId = selectedMaterial,
                                estimatedWeight = weight,
                                estimatedValue = 0.0,
                                pickupAddress = "Current location",
                                latitude = location?.latitude ?: 0.0,
                                longitude = location?.longitude ?: 0.0
                            )
                            submitting = false
                            if (result != null) nav.navigate(Routes.tracking(result.lot.lotId))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(15.dp),
                    enabled = !submitting
                ) {
                    if (submitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text("Confirm pickup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
