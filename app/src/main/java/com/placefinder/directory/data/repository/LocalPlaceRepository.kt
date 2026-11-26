package com.placefinder.directory.data.repository

import com.placefinder.directory.data.database.PlaceDao
import com.placefinder.directory.data.models.Place
import kotlinx.coroutines.flow.Flow

class LocalPlaceRepository(private val dao: PlaceDao) : PlaceRepository {

    override val allPlaces: Flow<List<Place>> = dao.getAll()

    override val myPlaces: Flow<List<Place>> = dao.getMine()

    override fun placesByCity(city: String): Flow<List<Place>> = dao.getByCity(city)

    override fun placesByRegion(region: String): Flow<List<Place>> = dao.getByRegion(region)

    override fun searchPlaces(query: String): Flow<List<Place>> =
        if (query.isBlank()) dao.getAll() else dao.search(query)

    override suspend fun getPlaceById(id: String): Place? = dao.getById(id)

    override suspend fun savePlace(place: Place) = dao.upsert(place)

    override suspend fun removePlace(place: Place) = dao.delete(place)
}
