package com.placefinder.directory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.placefinder.directory.R
import com.placefinder.directory.data.GeoUtils
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceStatus
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.ExploreViewModel

/** Explore screen: hotels near you, or search by name / city. */
@Composable
fun ExploreScreen(
    repository: PlaceRepository,
    onPlaceClick: (Place) -> Unit = {},
) {
    val viewModel: ExploreViewModel = viewModel { ExploreViewModel(repository) }
    val places by viewModel.places.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val origin by viewModel.origin.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun onLocation(location: Location) {
        viewModel.onUserLocationChanged(location.latitude, location.longitude)
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
        if (granted) requestMyLocation()
        else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.explore)) })
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.search_places)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )

            if (places.isEmpty()) {
                Text(
                    text = if (searchQuery.isBlank() && origin == null) {
                        stringResource(R.string.enable_location_or_search)
                    } else {
                        stringResource(R.string.no_places_found)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(places, key = { it.place.id }) { row ->
                    PlaceListItem(
                        place = row.place,
                        distanceLabel = row.distanceMeters?.let { GeoUtils.formatDistance(it) },
                        onClick = { onPlaceClick(row.place) },
                    )
                }
            }
        }
    }
}

/** A single row in the list of places. */
@Composable
fun PlaceListItem(
    place: Place,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    distanceLabel: String? = null,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                val subtitle = listOfNotNull(
                    GeoUtils.listingTypeLabel(place.listingType),
                    place.city.ifBlank { null },
                    distanceLabel,
                ).joinToString(" · ")
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        statusLabel(place.status),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    place.roomsFree?.let { rooms ->
                        Spacer(Modifier.width(8.dp))
                        Text("$rooms ·", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            onDelete?.let { del ->
                IconButton(onClick = del, modifier = Modifier.padding(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                    )
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: PlaceStatus): String = when (status) {
    PlaceStatus.OPEN -> stringResource(R.string.status_open)
    PlaceStatus.CLOSED -> stringResource(R.string.status_closed)
    PlaceStatus.UNAVAILABLE -> stringResource(R.string.status_unavailable)
}
