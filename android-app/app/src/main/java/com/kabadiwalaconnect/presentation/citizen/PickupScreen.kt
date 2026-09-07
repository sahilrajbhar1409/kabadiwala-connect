package com.kabadiwalaconnect.presentation.citizen

import android.net.Uri
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
import com.kabadiwalaconnect.data.api.RetrofitClient
import com.kabadiwalaconnect.navigation.Routes
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.components.ImagePicker
import com.kabadiwalaconnect.ui.components.RealTimeMap
import com.kabadiwalaconnect.ui.components.rememberCurrentLocation
import com.kabadiwalaconnect.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PickupScreen(nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember {
        PickupViewModel(backendRepository = RetrofitClient.create(context))
    }
    var selectedMaterial by remember { mutableStateOf("Paper") }
    var quantity by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val location = rememberCurrentLocation()
    
    // Manual location fallback
    var manualAddress by remember { mutableStateOf("") }
    var useManualLocation by remember { mutableStateOf(false) }

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
            item { Text("Add photos", style = MaterialTheme.typography.titleLarge) }
            item {
                ImagePicker(
                    imageUri = selectedPhotoUri,
                    onImageSelected = { uri ->
                        selectedPhotoUri = uri
                        viewModel.addPhoto(uri)
                    },
                    onImageRemoved = {
                        selectedPhotoUri = null
                        if (viewModel.photoUris.isNotEmpty()) {
                            viewModel.removePhoto(0)
                        }
                    }
                )
            }
            
            // Show upload status if there are photos
            if (viewModel.photoUris.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${viewModel.photoUris.size} photo(s) selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = Green
                        )
                        viewModel.photoUris.forEach { photo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (photo.isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Text("Uploading...", style = MaterialTheme.typography.bodySmall)
                                } else if (photo.uploadedUrl != null) {
                                    Text("✓", style = MaterialTheme.typography.bodySmall, color = Green)
                                    Text("Uploaded", style = MaterialTheme.typography.bodySmall)
                                } else if (photo.error != null) {
                                    Text("✗", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    Text("Error: ${photo.error}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            
            item { Text("Pickup address", style = MaterialTheme.typography.titleLarge) }
            
            // GPS location with map (if available) or manual fallback
            item {
                if (location != null && !useManualLocation) {
                    RealTimeMap(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { useManualLocation = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Enter address manually", color = Green)
                    }
                } else {
                    OutlinedTextField(
                        value = manualAddress,
                        onValueChange = { manualAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pickup address or location name") },
                        placeholder = { Text("e.g., Home, Office, Sector 5") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    if (location != null) {
                        TextButton(onClick = { useManualLocation = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Use GPS location", color = Green)
                        }
                    } else {
                        Text("Enable location access for automatic GPS detection, or enter address manually.", 
                            color = TextMuted, 
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
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
            
            // Show error message if any
            if (viewModel.errorMessage != null) {
                item {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(
                            viewModel.errorMessage ?: "",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        val weight = quantity.toDoubleOrNull()
                        // Get address: use GPS location name if available, otherwise use manual address
                        val address = if (location != null && !useManualLocation) {
                            "Current location (GPS: ${location.latitude}, ${location.longitude})"
                        } else {
                            manualAddress.trim()
                        }
                        
                        if (weight == null || weight <= 0 || submitting) return@Button
                        if (address.isEmpty()) {
                            viewModel.errorMessage = "Please provide a pickup address"
                            return@Button
                        }
                        
                        submitting = true
                        scope.launch {
                            val result = viewModel.submitToBackend(
                                context = context,
                                materialId = selectedMaterial,
                                estimatedWeight = weight,
                                estimatedValue = 0.0,
                                pickupAddress = address,
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
