package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Powers the Explore screen: live search over the published places.
 *
 * [placesFlow] switches between "all places" and "places matching the query" using
 * `flatMapLatest`, so typing a query cancels the previous search instead of overlapping.
 */
class ExploreViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val placesFlow: Flow<List<Place>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allPlaces else repository.searchPlaces(query)
        }

    init {
        viewModelScope.launch {
            placesFlow.collect { list ->
                _places.value = list
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
