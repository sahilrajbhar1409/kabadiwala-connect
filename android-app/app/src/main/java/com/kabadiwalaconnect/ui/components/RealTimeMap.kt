package com.kabadiwalaconnect.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun RealTimeMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Double = 15.0
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            createMapView(context).apply {
                updateMapLocation(this, latitude, longitude, zoom)
            }
        },
        update = { mapView ->
            updateMapLocation(mapView, latitude, longitude, zoom)
        },
        onRelease = { mapView ->
            mapView.onDetach()
        }
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.applicationContext.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(15.0)
    }
}

private fun updateMapLocation(
    mapView: MapView,
    latitude: Double,
    longitude: Double,
    zoom: Double
) {
    if (!latitude.isFinite() || !longitude.isFinite() ||
        latitude !in -90.0..90.0 || longitude !in -180.0..180.0
    ) {
        return
    }

    val point = GeoPoint(latitude, longitude)
    mapView.controller.setZoom(zoom)
    mapView.controller.setCenter(point)
    mapView.overlays.removeAll { it is Marker }
    mapView.overlays.add(
        Marker(mapView).apply {
            position = point
            title = "Pickup location"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    )
    mapView.invalidate()
}
