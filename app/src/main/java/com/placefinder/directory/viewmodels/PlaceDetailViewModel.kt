package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Loads a single place by id for the detail screen. */
class PlaceDetailViewModel(
    private val repository: PlaceRepository,
    placeId: String,
) : ViewModel() {

    private val _place = MutableStateFlow<Place?>(null)
    val place: StateFlow<Place?> = _place

    init {
        viewModelScope.launch {
            _place.value = repository.getPlaceById(placeId)
        }
    }
}
