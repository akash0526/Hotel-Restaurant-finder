package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Powers the My Places screen: the places owned by this device. */
class MyPlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.myPlaces.collect { list ->
                _places.value = list
                _isLoading.value = false
            }
        }
    }

    fun onDeletePlace(place: Place) {
        viewModelScope.launch { repository.removePlace(place) }
    }
}
