package com.placefinder.directory.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.placefinder.directory.viewmodels.MyPlacesViewModel

/** Screen listing places owned by this device ("My Places"). */
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_places)) },
                actions = {
                    IconButton(onClick = onAddPlace) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add),
                        )
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                items(1) { Text(stringResource(R.string.loading), Modifier.padding(16.dp)) }
            }
            if (!isLoading && places.isEmpty()) {
                items(1) { Text(stringResource(R.string.no_my_places), Modifier.padding(16.dp)) }
            }
            items(places, key = { it.id }) { place ->
                PlaceListItem(
                    place = place,
                    onClick = { onPlaceClick(place) },
                    onDelete = { viewModel.onDeletePlace(place) },
                )
            }
        }
    }
}
