package com.placefinder.directory.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceCategory
import com.placefinder.directory.data.models.PlaceStatus
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.PlaceEditorViewModel

/**
 * "Add / edit a place" form. Requires a name and an address to save.
 */
@Composable
fun PlaceEditorScreen(
    repository: PlaceRepository,
    placeId: String? = null,
    onBack: () -> Unit = {},
) {
    val viewModel: PlaceEditorViewModel = viewModel { PlaceEditorViewModel(repository) }
    val form by viewModel.form.collectAsState()
    val isValid = viewModel.isValid

    LaunchedEffect(placeId) {
        if (placeId != null) viewModel.loadPlace(placeId)
    }

    val title = if (placeId == null) R.string.add_place else R.string.edit_place

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }, enabled = isValid) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.save),
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(ScrollState(0)),
        ) {
            Field(stringResource(R.string.place_name), form.name, viewModel::setName)
            ValidationText(if (viewModel.isNameValid) null else stringResource(R.string.validation_name))

            CategoryChips(form = form, onCategoryChange = viewModel::setCategory)

            Field(stringResource(R.string.place_address), form.address, viewModel::setAddress)
            ValidationText(if (viewModel.isAddressValid) null else stringResource(R.string.validation_address))
            Field(stringResource(R.string.place_city), form.city, viewModel::setCity)
            Field(stringResource(R.string.place_region), form.region.orEmpty(), viewModel::setRegion)
            Field(stringResource(R.string.place_phone), form.phone.orEmpty(), viewModel::setPhone)
            Field(stringResource(R.string.place_email), form.email.orEmpty(), viewModel::setEmail)
            Field(stringResource(R.string.place_website), form.website.orEmpty(), viewModel::setWebsite)

            OutlinedTextField(
                value = form.description,
                onValueChange = viewModel::setDescription,
                label = { Text(stringResource(R.string.place_description)) },
                minLines = 3,
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            StatusChips(form = form, onStatusChange = viewModel::setStatus)

            OutlinedTextField(
                value = form.roomsFree?.toString().orEmpty(),
                onValueChange = { text -> viewModel.setRoomsFree(text.toIntOrNull()) },
                label = { Text(stringResource(R.string.place_rooms_free)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            Field(
                stringResource(R.string.place_availability_note),
                form.availabilityNote.orEmpty(),
                viewModel::setAvailabilityNote,
            )

            Button(
                onClick = { viewModel.save() },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

/** Single line text field row. */
@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun ValidationText(message: String?) {
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 40.dp, end = 16.dp),
        )
    }
}

@Composable
private fun CategoryChips(
    form: Place,
    onCategoryChange: (PlaceCategory) -> Unit,
) {
    Text(
        text = stringResource(R.string.place_category),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
    )
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Chip(
            label = stringResource(R.string.category_hotel),
            selected = form.category == PlaceCategory.HOTEL,
            onClick = { onCategoryChange(PlaceCategory.HOTEL) },
        )
        Spacer(Modifier.width(8.dp))
        Chip(
            label = stringResource(R.string.category_restaurant),
            selected = form.category == PlaceCategory.RESTAURANT,
            onClick = { onCategoryChange(PlaceCategory.RESTAURANT) },
        )
        Spacer(Modifier.width(8.dp))
        Chip(
            label = stringResource(R.string.category_both),
            selected = form.category == PlaceCategory.BOTH,
            onClick = { onCategoryChange(PlaceCategory.BOTH) },
        )
    }
}

@Composable
private fun StatusChips(
    form: Place,
    onStatusChange: (PlaceStatus) -> Unit,
) {
    Text(
        text = stringResource(R.string.place_status),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
    )
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Chip(
            label = stringResource(R.string.status_open),
            selected = form.status == PlaceStatus.OPEN,
            onClick = { onStatusChange(PlaceStatus.OPEN) },
        )
        Spacer(Modifier.width(8.dp))
        Chip(
            label = stringResource(R.string.status_closed),
            selected = form.status == PlaceStatus.CLOSED,
            onClick = { onStatusChange(PlaceStatus.CLOSED) },
        )
        Spacer(Modifier.width(8.dp))
        Chip(
            label = stringResource(R.string.status_unavailable),
            selected = form.status == PlaceStatus.UNAVAILABLE,
            onClick = { onStatusChange(PlaceStatus.UNAVAILABLE) },
        )
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
