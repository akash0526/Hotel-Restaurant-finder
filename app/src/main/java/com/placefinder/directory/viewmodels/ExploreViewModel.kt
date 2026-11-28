package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.placefinder.directory.data.GeoUtils
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class NearbyPlace(
    val place: Place,
    val distanceMeters: Double? = null,
)

/**
 * Explore: nearby hotels first when GPS is on, plus name/city search.
 */
class ExploreViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _origin = MutableStateFlow<LatLng?>(null)
    val origin: StateFlow<LatLng?> = _origin

    private val _places = MutableStateFlow<List<NearbyPlace>>(emptyList())
    val places: StateFlow<List<NearbyPlace>> = _places

    init {
        viewModelScope.launch {
            combine(repository.allPlaces, _searchQuery, _origin) { allPlaces, query, origin ->
                val q = query.trim()
                val matched = if (q.isEmpty()) {
                    allPlaces
                } else {
                    allPlaces.filter { place ->
                        place.name.contains(q, ignoreCase = true) ||
                            place.city.contains(q, ignoreCase = true) ||
                            (place.region?.contains(q, ignoreCase = true) == true) ||
                            place.address.contains(q, ignoreCase = true)
                    }
                }

                if (origin != null) {
                    matched
                        .map { place ->
                            val lat = place.latitude
                            val lng = place.longitude
                            val meters = if (lat != null && lng != null) {
                                GeoUtils.distanceMeters(origin.latitude, origin.longitude, lat, lng)
                            } else {
                                Double.POSITIVE_INFINITY
                            }
                            NearbyPlace(place, meters.takeUnless { it.isInfinite() })
                        }
                        .sortedBy { it.distanceMeters ?: Double.POSITIVE_INFINITY }
                        .let { rows ->
                            if (q.isEmpty()) {
                                rows.filter {
                                    val d = it.distanceMeters
                                    d != null && d <= NEARBY_METERS
                                }.take(MAX_NEARBY)
                            } else {
                                rows.take(MAX_SEARCH)
                            }
                        }
                } else {
                    if (q.isEmpty()) {
                        emptyList()
                    } else {
                        matched
                            .sortedBy { it.name.lowercase() }
                            .take(MAX_SEARCH)
                            .map { NearbyPlace(it) }
                    }
                }
            }.collect { _places.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onUserLocationChanged(latitude: Double, longitude: Double) {
        _origin.value = LatLng(latitude, longitude)
    }

    companion object {
        private const val NEARBY_METERS = 50_000.0
        private const val MAX_NEARBY = 100
        private const val MAX_SEARCH = 300
    }
}
