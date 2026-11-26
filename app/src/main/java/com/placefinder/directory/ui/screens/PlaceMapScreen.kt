package com.placefinder.directory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val NepalCenter = LatLng(28.3949, 84.1240)

/**
 * Map screen: nearby hotels as pins. Radius is "search within X km" around GPS
 * or a searched city (Pokhara, Butwal, Kathmandu, …).
 */
@Composable
fun PlaceMapScreen(
    repository: PlaceRepository,
    onPlaceClick: (Place) -> Unit = {},
) {
    val viewModel: MapViewModel = viewModel { MapViewModel(repository) }
    val places by viewModel.places.collectAsState()
    val radiusMeters by viewModel.radiusMeters.collectAsState()
    val origin by viewModel.origin.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchHint by viewModel.searchHint.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(NepalCenter, 7f)
    }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var myLocationEnabled by remember { mutableStateOf(false) }
    var geocodeMiss by remember { mutableStateOf(false) }

    fun moveCamera(target: LatLng, zoom: Float = 12f) {
        scope.launch {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, zoom))
        }
    }

    fun onLocation(location: Location) {
        myLocationEnabled = true
        viewModel.onUserLocationChanged(location.latitude, location.longitude)
        moveCamera(LatLng(location.latitude, location.longitude))
    }

    val requestMyLocation: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocation(location)
                } else {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        CancellationTokenSource().token,
                    ).addOnSuccessListener { current ->
                        if (current != null) onLocation(current)
                    }
                }
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) requestMyLocation()
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            requestMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun submitSearch() {
        keyboard?.hide()
        geocodeMiss = false
        val resolved = viewModel.searchPlace(searchQuery)
        if (resolved != null) {
            moveCamera(resolved)
            return
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) return
        scope.launch {
            val geo = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocationName("$q, Nepal", 1)
                        ?.firstOrNull()
                }.getOrNull()
            }
            if (geo != null) {
                viewModel.applyGeocodedOrigin(q, geo.latitude, geo.longitude)
                moveCamera(LatLng(geo.latitude, geo.longitude))
            } else {
                geocodeMiss = true
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.map)) }) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    geocodeMiss = false
                    viewModel.onSearchQueryChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.search_map_location)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
            )

            GoogleMap(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = myLocationEnabled),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                ),
            ) {
                places.forEach { place ->
                    val lat = place.latitude
                    val lng = place.longitude
                    if (lat != null && lng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            title = place.name,
                            snippet = place.city,
                            onInfoWindowClick = { onPlaceClick(place) },
                        )
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                val status = when {
                    geocodeMiss -> stringResource(R.string.location_not_found)
                    origin == null -> stringResource(R.string.enable_location_or_search)
                    searchHint != null -> stringResource(
                        R.string.hotels_near_search,
                        places.size,
                        searchHint!!,
                        radiusMeters / 1000,
                    )
                    else -> stringResource(
                        R.string.hotels_nearby_count,
                        places.size,
                        radiusMeters / 1000,
                    )
                }
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.search_radius_within_km, radiusMeters / 1000),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            val fineLocationGranted =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                ) == PackageManager.PERMISSION_GRANTED
                            if (fineLocationGranted) {
                                requestMyLocation()
                            } else {
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
                Text(
                    text = stringResource(R.string.osm_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
