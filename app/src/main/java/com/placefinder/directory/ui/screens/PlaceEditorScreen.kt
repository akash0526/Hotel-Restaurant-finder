package com.placefinder.directory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.placefinder.directory.R
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceCategory
import com.placefinder.directory.data.models.PlaceStatus
import com.placefinder.directory.data.repository.PlaceRepository
import com.placefinder.directory.viewmodels.PlaceEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )

            StatusChips(form = form, onStatusChange = viewModel::setStatus)

            OutlinedTextField(
                value = form.roomsFree?.toString().orEmpty(),
                onValueChange = { text -> viewModel.setRoomsFree(text.toIntOrNull()) },
                label = { Text(stringResource(R.string.place_rooms_free)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )

            Field(
                stringResource(R.string.place_availability_note),
                form.availabilityNote.orEmpty(),
                viewModel::setAvailabilityNote,
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.save() },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.save))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = fieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    )
}

@Composable
private fun ValidationText(message: String?) {
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
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
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip(stringResource(R.string.category_hotel), form.category == PlaceCategory.HOTEL) {
            onCategoryChange(PlaceCategory.HOTEL)
        }
        Chip(stringResource(R.string.category_restaurant), form.category == PlaceCategory.RESTAURANT) {
            onCategoryChange(PlaceCategory.RESTAURANT)
        }
        Chip(stringResource(R.string.category_both), form.category == PlaceCategory.BOTH) {
            onCategoryChange(PlaceCategory.BOTH)
        }
    }
}

@Composable
private fun StatusChips(
    form: Place,
    onStatusChange: (PlaceStatus) -> Unit,
) {
    Text(
        text = stringResource(R.string.place_status),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip(stringResource(R.string.status_open), form.status == PlaceStatus.OPEN) {
            onStatusChange(PlaceStatus.OPEN)
        }
        Chip(stringResource(R.string.status_closed), form.status == PlaceStatus.CLOSED) {
            onStatusChange(PlaceStatus.CLOSED)
        }
        Chip(stringResource(R.string.status_unavailable), form.status == PlaceStatus.UNAVAILABLE) {
            onStatusChange(PlaceStatus.UNAVAILABLE)
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.small,
    )
}
