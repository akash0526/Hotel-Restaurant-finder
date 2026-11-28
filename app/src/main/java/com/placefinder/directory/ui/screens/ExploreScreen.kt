package com.placefinder.directory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.placefinder.directory.R
import com.placefinder.directory.data.GeoUtils
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.ui.components.EmptyState
import com.placefinder.directory.ui.components.PlaceCard
import com.placefinder.directory.ui.components.PlacesSearchField
import com.placefinder.directory.ui.components.ScreenHeader
import com.placefinder.directory.viewmodels.ExploreViewModel

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

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.explore_headline),
            subtitle = stringResource(R.string.explore_subhead),
            modifier = Modifier.padding(top = 12.dp),
        )
        PlacesSearchField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = stringResource(R.string.search_places),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        if (places.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Search,
                message = if (searchQuery.isBlank() && origin == null) {
                    stringResource(R.string.enable_location_or_search)
                } else {
                    stringResource(R.string.no_places_found)
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(places, key = { it.place.id }) { row ->
                    PlaceCard(
                        place = row.place,
                        distanceLabel = row.distanceMeters?.let { GeoUtils.formatDistance(it) },
                        onClick = { onPlaceClick(row.place) },
                    )
                }
            }
        }
    }
}
