package com.placefinder.directory.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceCategory
import com.placefinder.directory.data.models.PlaceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Remote [PlaceRepository] backed by Firestore.
 *
 * Hotels/restaurants update their listing and availability here; the app keeps a live
 * local view of the `places` collection via `addSnapshotListener`.
 */
class FirestorePlaceRepository(
    private val db: FirebaseFirestore,
) : PlaceRepository {

    companion object {
        const val PLACES_COLLECTION = "places"
    }

    private val _places = MutableStateFlow<List<Place>>(emptyList())

    override val allPlaces: Flow<List<Place>> = _places

    override val myPlaces: Flow<List<Place>> = _places.map { places ->
        places.filter { it.isOwnedByUser }
    }

    override fun placesByCity(city: String): Flow<List<Place>> = _places.map { places ->
        places.filter { it.city == city }
    }

    override fun placesByRegion(region: String): Flow<List<Place>> = _places.map { places ->
        places.filter { it.region == region }
    }

    override fun searchPlaces(query: String): Flow<List<Place>> = _places.map { places ->
        if (query.isBlank()) {
            places
        } else {
            places.filter { p ->
                p.name.contains(query, ignoreCase = true) ||
                    p.address.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getPlaceById(id: String): Place? =
        Tasks.await(db.collection(PLACES_COLLECTION).document(id).get()).toPlace()

    override suspend fun savePlace(place: Place) {
        Tasks.await(
            db.collection(PLACES_COLLECTION).document(place.id).set(place.toFirestoreMap())
        )
    }

    override suspend fun removePlace(place: Place) {
        Tasks.await(db.collection(PLACES_COLLECTION).document(place.id).delete())
    }

    /** Start the live view: lists stay up to date as hotels publish or update places. */
    fun startListening() {
        db.collection(PLACES_COLLECTION)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                _places.value = snapshot?.documents?.map { it.toPlace() } ?: emptyList()
            }
    }

    private fun DocumentSnapshot.toPlace(): Place = Place(
        id = reference.id,
        name = getString("name") ?: "",
        category = safeEnum(getString("category"), PlaceCategory.HOTEL),
        address = getString("address") ?: "",
        city = getString("city") ?: "",
        region = getString("region"),
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        phone = getString("phone"),
        email = getString("email"),
        website = getString("website"),
        description = getString("description") ?: "",
        status = safeEnum(getString("status"), PlaceStatus.OPEN),
        openingHours = getString("openingHours"),
        roomsFree = getLong("roomsFree")?.toInt(),
        availabilityNote = getString("availabilityNote"),
        isOwnedByUser = getBoolean("isOwnedByUser") ?: false,
        rating = getDouble("rating") ?: 0.0,
        reviewCount = getLong("reviewCount")?.toInt() ?: 0,
        createdAt = getLong("createdAt") ?: 0,
        updatedAt = getLong("updatedAt") ?: 0,
    )

    private fun Place.toFirestoreMap(): Map<String, Any?> = hashMapOf(
        "name" to name,
        "category" to category.name,
        "address" to address,
        "city" to city,
        "region" to region,
        "latitude" to latitude,
        "longitude" to longitude,
        "phone" to phone,
        "email" to email,
        "website" to website,
        "description" to description,
        "status" to status.name,
        "openingHours" to openingHours,
        "roomsFree" to roomsFree,
        "availabilityNote" to availabilityNote,
        "isOwnedByUser" to isOwnedByUser,
        "rating" to rating,
        "reviewCount" to reviewCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )

    private inline fun <T : Enum<T>> safeEnum(value: String?, fallback: T): T =
        value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback
}
