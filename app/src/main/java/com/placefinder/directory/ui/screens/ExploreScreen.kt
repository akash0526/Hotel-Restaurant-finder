package com.placefinder.directory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceStatus
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.ExploreViewModel

/** Explore screen: the searchable directory of published places. */
@Composable
fun ExploreScreen(
    repository: PlaceRepository,
    onPlaceClick: (Place) -> Unit = {},
) {
    val viewModel: ExploreViewModel = viewModel { ExploreViewModel(repository) }
    val places by viewModel.places.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.explore)) })
        }
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
                    text = stringResource(R.string.no_places_found),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(places, key = { it.id }) { place ->
                    PlaceListItem(place = place, onClick = { onPlaceClick(place) })
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
                Text(
                    place.city,
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
