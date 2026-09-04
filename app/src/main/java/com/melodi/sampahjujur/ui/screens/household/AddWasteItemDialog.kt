package com.melodi.sampahjujur.ui.screens.household

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melodi.sampahjujur.R
import com.melodi.sampahjujur.ui.components.ImagePicker
import com.melodi.sampahjujur.ui.theme.PrimaryGreen
import com.melodi.sampahjujur.ui.theme.SampahJujurTheme
import com.melodi.sampahjujur.utils.CloudinaryUploadService
import com.melodi.sampahjujur.utils.TtsManager
import com.melodi.sampahjujur.utils.WastePriceCalculator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.UUID

private fun cacheImageUri(context: Context, tempId: String, imageUri: Uri) {
    val sharedPrefs = context.getSharedPreferences("image_cache", Context.MODE_PRIVATE)
    sharedPrefs.edit().putString("image_$tempId", imageUri.toString()).apply()
    Log.d("AddWasteItemDialog", "Cached image URI for retry: image_$tempId")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWasteItemDialog(
    isLoading: Boolean = false,
    onDismiss: () -> Unit = {},
    onAddItem: (type: String, weight: Double, value: Double, description: String, imageUrl: String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val wasteTypes = WastePriceCalculator.getWasteTypes()

    LaunchedEffect(Unit) {
        try {
            CloudinaryUploadService.initialize(context)
        } catch (e: Exception) {
            uploadError = "Failed to initialize image service: ${e.message}"
        }
    }

    val calculatedValue by remember {
        derivedStateOf {
            val weightValue = weight.toDoubleOrNull() ?: 0.0
            if (selectedType.isNotBlank() && weightValue > 0) {
                WastePriceCalculator.calculateValue(selectedType, weightValue)
            } else {
                0.0
            }
        }
    }

    val isWeightValid by remember {
        derivedStateOf {
            val w = weight.toDoubleOrNull()
            w != null && w > 0.0
        }
    }

    val isFormValid by remember {
        derivedStateOf {
            selectedType.isNotBlank() &&
            weight.isNotBlank() &&
            isWeightValid &&
            imageUri != null &&
            !isUploading &&
            !isLoading
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.add_waste_item),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select e-waste category, photo & approximate weight",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Image Picker
            ImagePicker(
                imageUri = imageUri,
                onImageSelected = { uri ->
                    imageUri = uri
                    uploadError = null
                },
                onImageRemoved = { imageUri = null },
                modifier = Modifier.fillMaxWidth()
            )

            if (uploadError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uploadError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Waste Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_material) + " *") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    wasteTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text("$type (₹${WastePriceCalculator.getPricePerKg(type).toInt()}/kg)") },
                            onClick = {
                                selectedType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight Field (Decimals supported, rejected <= 0)
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(stringResource(R.string.weight_kg) + " *") },
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
                    if (weight.isNotBlank() && !isWeightValid) {
                        Text(
                            text = stringResource(R.string.invalid_weight),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estimated Value Display (Calculated deterministically)
            if (selectedType.isNotBlank()) {
                val rate = WastePriceCalculator.getPricePerKg(selectedType)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryGreen.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.estimated_value),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${stringResource(R.string.current_price)}: ₹${rate.toInt()}/kg",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            if (calculatedValue > 0) {
                                Text(
                                    text = "${weight} kg × ₹${rate.toInt()} = ₹${calculatedValue.toInt()}",
                                    fontSize = 10.sp,
                                    color = PrimaryGreen
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${calculatedValue.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )

                            IconButton(
                                onClick = {
                                    val w = weight.toDoubleOrNull() ?: 0.0
                                    ttsManager.speakPrice(selectedType, rate, w, calculatedValue)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = stringResource(R.string.speak_price),
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("e.g. Intact circuit board or copper wire bundle") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (imageUri != null) {
                            isUploading = true
                            uploadError = null
                            coroutineScope.launch {
                                try {
                                    withTimeout(5000L) {
                                        val uploadedUrl = CloudinaryUploadService.uploadImage(
                                            context = context,
                                            imageUri = imageUri!!,
                                            folder = "sampah-jujur/waste-items"
                                        )
                                        val weightValue = weight.toDoubleOrNull() ?: 0.0
                                        onAddItem(selectedType, weightValue, calculatedValue, description, uploadedUrl)
                                    }
                                } catch (e: TimeoutCancellationException) {
                                    val tempId = UUID.randomUUID().toString()
                                    cacheImageUri(context, tempId, imageUri!!)
                                    Log.w("AddWasteItemDialog", "Upload timeout - saving offline: $tempId")
                                    val weightValue = weight.toDoubleOrNull() ?: 0.0
                                    onAddItem(selectedType, weightValue, calculatedValue, description, "pending_upload:$tempId")
                                    uploadError = "Saved offline - image will upload when online"
                                    isUploading = false
                                } catch (e: Exception) {
                                    uploadError = "Image upload failed: ${e.message}"
                                    isUploading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(28.dp),
                    enabled = isFormValid
                ) {
                    if (isUploading || isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.add_waste_item),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
