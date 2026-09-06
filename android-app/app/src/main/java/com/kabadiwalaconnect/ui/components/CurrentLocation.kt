package com.kabadiwalaconnect.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

data class DeviceLocation(val latitude: Double, val longitude: Double)

@Composable
fun rememberCurrentLocation(): DeviceLocation? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<DeviceLocation?>(null) }
    var permissionGranted by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions.values.any { it }
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            context.fetchCurrentLocation { location = it }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    return location
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun Context.fetchCurrentLocation(
    onLocation: (DeviceLocation?) -> Unit
) {
    LocationServices.getFusedLocationProviderClient(this).getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { current ->
        onLocation(current?.let { DeviceLocation(it.latitude, it.longitude) })
    }
}
