package com.placefinder.directory.data.repository

import com.placefinder.directory.data.models.Place
import kotlinx.coroutines.flow.Flow

/**
 * Data source abstraction — the app talks to this interface, not to the database directly.
 *
 * Stage 2+ will get a remote implementation (e.g. HTTP/Firebase) without changing the
 * screens, so remote and local data behave the same way.
 */
interface PlaceRepository {
    val allPlaces: Flow<List<Place>>
    val myPlaces: Flow<List<Place>>

    fun placesByCity(city: String): Flow<List<Place>>

    fun placesByRegion(region: String): Flow<List<Place>>

    fun searchPlaces(query: String): Flow<List<Place>>

    suspend fun getPlaceById(id: String): Place?

    suspend fun savePlace(place: Place)

    suspend fun removePlace(place: Place)
}
