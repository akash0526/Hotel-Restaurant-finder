package com.placefinder.directory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.compose.GoogleMap
import com.google.android.gms.maps.compose.Marker
import com.google.android.gms.maps.compose.MarkerState
import com.google.android.gms.maps.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.MapViewModel

/**
 * Map screen: places appear as pins on Google Maps.
 *
 * The radius slider = "search within X km"; the My location button centers the map on
 * your current position (uses [FusedLocationProviderClient], requires location permission).
 */
@Composable
fun PlaceMapScreen(
    repository: PlaceRepository,
    onPlaceClick: (Place) -> Unit = {},
) {
    val viewModel: MapViewModel = viewModel { MapViewModel(repository) }
    val places by viewModel.places.collectAsState()
    val radiusMeters by viewModel.radiusMeters.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val fusedLocationClient = remember(context) { FusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    val requestMyLocation: () -> Unit = {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                viewModel.onUserLocationChanged(location.latitude, location.longitude)
                cameraPositionState.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 12f)
                )
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) requestMyLocation()
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { latLng ->
            cameraPositionState.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.map)) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                zoomControlsEnabled = true,
                myLocationEnabled = true,
            ) {
                places.forEach { place ->
                    val lat = place.latitude
                    val lng = place.longitude
                    if (lat != null && lng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            title = place.name,
                            onInfoWindowClick = { onPlaceClick(place) },
                        )
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.search_radius_within_km, radiusMeters / 1000),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            val fineLocationGranted =
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED
                            if (fineLocationGranted && activity != null) {
                                requestMyLocation()
                            } else if (!fineLocationGranted) {
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.my_location),
                        )
                    }
                }
                Slider(
                    value = radiusMeters / 1000f,
                    onValueChange = { viewModel.setRadiusMeters((it * 1000).toInt()) },
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
