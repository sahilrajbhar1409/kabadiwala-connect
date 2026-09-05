package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.CollectionRequestStatus
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.model.Material
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.data.repository.PriceServiceProvider
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val CITIZEN_ID = "citizen-session"

@Composable
fun PickupScreen(nav: NavHostController) {
    val repository = remember { CollectionRepositoryProvider.instance }
    val priceService = remember { PriceServiceProvider.instance }
    val materials = remember { priceService.supportedMaterials() }
    var step by remember { mutableIntStateOf(0) }
    var selectedMaterial by remember { mutableStateOf<Material?>(null) }
    var weightText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val weight = weightText.toDoubleOrNull()
    val estimatedValue = if (selectedMaterial != null && weight != null && weight > 0) {
        priceService.estimateValue(selectedMaterial!!.id, weight)
    } else {
        0.0
    }

    Scaffold(
        containerColor = Cream,
        topBar = { AppTopBar(nav, "Request pickup") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            Text("Step ${step + 1} of 5", color = Green, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { (step + 1) / 5f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                color = Green,
                trackColor = GreenLight
            )
            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            when (step) {
                0 -> MaterialStep(materials, selectedMaterial) {
                    selectedMaterial = it
                    errorMessage = null
                }
                1 -> WeightStep(weightText) {
                    weightText = it
                    errorMessage = null
                }
                2 -> AddressStep(address) {
                    address = it
                    errorMessage = null
                }
                3 -> EstimateStep(selectedMaterial, weight ?: 0.0, estimatedValue)
                4 -> ReviewStep(selectedMaterial, weight ?: 0.0, address, estimatedValue)
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    when (step) {
                        0 -> if (selectedMaterial == null) {
                            errorMessage = "Please select a material to continue."
                        } else step++
                        1 -> if (weight == null || !weight.isFinite() || weight <= 0) {
                            errorMessage = "Enter a valid weight greater than 0 kg."
                        } else step++
                        2 -> if (address.isBlank()) {
                            errorMessage = "Please enter a pickup address."
                        } else step++
                        3 -> step++
                        4 -> {
                            if (isSubmitting) return@Button
                            isSubmitting = true
                            errorMessage = null
                            try {
                                val now = now()
                                val requestId = "REQ-${UUID.randomUUID()}"
                                val lotId = repository.nextLotId()
                                val request = CollectionRequest(
                                    id = requestId,
                                    citizenId = CITIZEN_ID,
                                    materialId = selectedMaterial!!.id,
                                    estimatedWeight = weight!!,
                                    estimatedValue = estimatedValue,
                                    pickupAddress = address.trim(),
                                    latitude = 0.0,
                                    longitude = 0.0,
                                    preferredDate = now.substringBefore("T"),
                                    preferredTime = "Any time",
                                    status = CollectionRequestStatus.REQUESTED,
                                    createdAt = now,
                                    updatedAt = now
                                )
                                repository.createCollectionRequest(request)
                                repository.createLot(
                                    Lot(
                                        lotId = lotId,
                                        requestId = requestId,
                                        citizenId = CITIZEN_ID,
                                        collectorId = "",
                                        materialId = selectedMaterial!!.id,
                                        estimatedWeight = weight,
                                        estimatedValue = estimatedValue,
                                        pickupLocation = address.trim(),
                                        status = LotStatus.REQUESTED,
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                                nav.navigate(Routes.pickupConfirmation(lotId)) {
                                    launchSingleTop = true
                                    popUpTo(Routes.PICKUP) { inclusive = true }
                                }
                            } catch (exception: Exception) {
                                isSubmitting = false
                                errorMessage = exception.message ?: "We couldn't save your pickup request."
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    if (step == 4) {
                        if (isSubmitting) "Saving request…" else "Confirm pickup"
                    } else "Continue",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MaterialStep(
    materials: List<Material>,
    selected: Material?,
    onSelected: (Material) -> Unit
) {
    Text("What do you want to recycle?", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        materials.forEach { material ->
            FilterChip(
                selected = selected?.id == material.id,
                onClick = { onSelected(material) },
                label = { Text(material.name) }
            )
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("Rates are locked to today's published price list.", color = TextMuted, fontSize = 13.sp)
}

@Composable
private fun WeightStep(value: String, onValueChange: (String) -> Unit) {
    Text("How much do you have?", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter weight") },
        supportingText = { Text("Use kilograms, for example 5.5") },
        trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun AddressStep(value: String, onValueChange: (String) -> Unit) {
    Text("Where should we collect it?", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter your complete pickup address") },
        minLines = 3,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun EstimateStep(material: Material?, weight: Double, value: Double) {
    Text("Estimated value", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    Surface(shape = RoundedCornerShape(20.dp), color = GreenLight) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(material?.name ?: "Material", color = GreenDark, fontWeight = FontWeight.Bold)
            Text("%.2f kg × ₹%.2f/kg".format(Locale.US, weight, material?.pricePerUnit ?: 0.0), color = TextMuted)
            Spacer(Modifier.height(10.dp))
            Text("₹%.2f".format(Locale.US, value), color = GreenDark, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Final value is confirmed after weighing at pickup.", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ReviewStep(material: Material?, weight: Double, address: String, value: Double) {
    Text("Review your pickup", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewRow("Material", material?.name ?: "Not selected")
            ReviewRow("Estimated weight", "%.2f kg".format(Locale.US, weight))
            ReviewRow("Pickup address", address)
            ReviewRow("Estimated value", "₹%.2f".format(Locale.US, value))
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun now(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
