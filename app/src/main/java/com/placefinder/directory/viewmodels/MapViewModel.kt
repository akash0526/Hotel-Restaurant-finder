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

/**
 * Powers the Map screen.
 *
 * Pins are hotels within [radiusMeters] of [origin] (GPS or a searched city).
 * Until an origin is set, no pins are drawn — 7k+ markers would freeze the map.
 */
class MapViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _radiusMeters = MutableStateFlow(20_000)
    val radiusMeters: StateFlow<Int> = _radiusMeters

    private val _origin = MutableStateFlow<LatLng?>(null)
    val origin: StateFlow<LatLng?> = _origin

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchHint = MutableStateFlow<String?>(null)
    val searchHint: StateFlow<String?> = _searchHint

    init {
        viewModelScope.launch {
            combine(
                repository.allPlaces,
                _radiusMeters,
                _origin,
            ) { allPlaces, radiusMeters, origin ->
                if (origin == null) {
                    emptyList()
                } else {
                    allPlaces
                        .asSequence()
                        .mapNotNull { place ->
                            val lat = place.latitude ?: return@mapNotNull null
                            val lng = place.longitude ?: return@mapNotNull null
                            val meters = GeoUtils.distanceMeters(
                                origin.latitude, origin.longitude, lat, lng,
                            )
                            if (meters <= radiusMeters) place to meters else null
                        }
                        .sortedBy { it.second }
                        .take(MAX_MARKERS)
                        .map { it.first }
                        .toList()
                }
            }.collect { _places.value = it }
        }
    }

    fun setRadiusMeters(value: Int) {
        _radiusMeters.value = value
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onUserLocationChanged(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        _userLocation.value = latLng
        _origin.value = latLng
        _searchHint.value = null
    }

    /**
     * Center the radius search on a city, region, or hotel name from the catalog.
     * Returns the resolved coordinate, or null if nothing matched.
     */
    fun searchPlace(query: String): LatLng? {
        val q = query.trim()
        if (q.isEmpty()) return _origin.value
        val catalog = repositorySnapshot()
        val resolved = resolveSearch(q, catalog) ?: return null
        _origin.value = resolved
        _searchHint.value = q
        return resolved
    }

    fun applyGeocodedOrigin(query: String, latitude: Double, longitude: Double) {
        _origin.value = LatLng(latitude, longitude)
        _searchHint.value = query.trim()
    }

    private fun repositorySnapshot(): List<Place> = _lastCatalog

    private var _lastCatalog: List<Place> = emptyList()

    init {
        viewModelScope.launch {
            repository.allPlaces.collect { _lastCatalog = it }
        }
    }

    companion object {
        const val MAX_MARKERS = 150

        fun resolveSearch(query: String, places: List<Place>): LatLng? {
            val q = query.trim()
            if (q.isEmpty()) return null
            val exactCity = places.filter {
                it.city.equals(q, ignoreCase = true) || it.region.equals(q, ignoreCase = true)
            }
            val pool = exactCity.ifEmpty {
                places.filter { place ->
                    place.city.contains(q, ignoreCase = true) ||
                        (place.region?.contains(q, ignoreCase = true) == true) ||
                        place.name.contains(q, ignoreCase = true) ||
                        place.address.contains(q, ignoreCase = true)
                }
            }.filter { it.latitude != null && it.longitude != null }
            if (pool.isEmpty()) return null
            return LatLng(
                pool.map { it.latitude!! }.average(),
                pool.map { it.longitude!! }.average(),
            )
        }
    }
}
