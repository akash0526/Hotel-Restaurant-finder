package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Powers the Map screen.
 *
 * `placesFlow` shows the radius filter: when we know the user's location, only places
 * within [radiusMeters] are listed. Before the location is known, everything is shown.
 */
class MapViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _radiusMeters = MutableStateFlow(20_000)
    val radiusMeters: StateFlow<Int> = _radiusMeters

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val placesFlow: Flow<List<Place>> = combine(
        repository.allPlaces,
        _radiusMeters,
        _userLocation,
    ) { allPlaces, radiusMeters, userLocation ->
        if (userLocation == null) {
            allPlaces
        } else {
            allPlaces.filter { place ->
                val lat = place.latitude
                val lng = place.longitude
                lat != null && lng != null &&
                    distanceBetween(userLocation.latitude, userLocation.longitude, lat, lng) <= radiusMeters
            }
        }
    }

    init {
        viewModelScope.launch { placesFlow.collect { _places.value = it } }
    }

    fun setRadiusMeters(value: Int) {
        _radiusMeters.value = value
    }

    fun onUserLocationChanged(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }

    /** Approximate great-circle distance between two points, in meters. */
    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6_371_000.0
        val p1 = lat1 * Math.PI / 180.0
        val p2 = lat2 * Math.PI / 180.0
        val dp = (lat2 - lat1) * Math.PI / 180.0
        val dl = (lng2 - lng1) * Math.PI / 180.0
        val a = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
        return 2 * R * asin(sqrt(a))
    }
}
