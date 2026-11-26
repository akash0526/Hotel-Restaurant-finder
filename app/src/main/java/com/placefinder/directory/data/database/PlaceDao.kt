package com.placefinder.directory.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.placefinder.directory.data.models.Place
import kotlinx.coroutines.flow.Flow

/**
 * Room data access object for [Place]s.
 *
 * Returning [Flow] makes lists live: the UI re-renders automatically when data changes.
 */
@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE city = :city ORDER BY name COLLATE NOCASE")
    fun getByCity(city: String): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE region = :region ORDER BY name COLLATE NOCASE")
    fun getByRegion(region: String): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%' COLLATE NOCASE OR address LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY name COLLATE NOCASE")
    fun search(query: String): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE isOwnedByUser = 1 ORDER BY updatedAt DESC")
    fun getMine(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Place?

    @Upsert
    suspend fun upsert(place: Place)

    @Delete
    suspend fun delete(place: Place)
}
