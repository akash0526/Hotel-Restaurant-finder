package com.placefinder.directory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.ui.components.EmptyState
import com.placefinder.directory.ui.components.PlaceCard
import com.placefinder.directory.ui.components.ScreenHeader
import com.placefinder.directory.viewmodels.MyPlacesViewModel

@Composable
fun MyPlacesScreen(
    repository: PlaceRepository,
    onPlaceClick: (Place) -> Unit = {},
    onAddPlace: () -> Unit = {},
) {
    val viewModel: MyPlacesViewModel = viewModel { MyPlacesViewModel(repository) }
    val places by viewModel.places.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlace,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.my_places_headline),
                subtitle = stringResource(R.string.my_places_subhead),
                modifier = Modifier.padding(top = 12.dp),
            )
            when {
                isLoading -> EmptyState(
                    icon = Icons.Outlined.Storefront,
                    message = stringResource(R.string.loading),
                )
                places.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Storefront,
                    message = stringResource(R.string.no_my_places),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceCard(
                            place = place,
                            onClick = { onPlaceClick(place) },
                            onDelete = { viewModel.onDeletePlace(place) },
                        )
                    }
                }
            }
        }
    }
}
