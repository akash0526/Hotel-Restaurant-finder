package com.placefinder.directory.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A place — a hotel or a restaurant — that can be listed so tourists can find it.
 *
 * [status] lets a place publish updates like "Closed today" without editing the profile.
 * [roomsFree] and [availabilityNote] are the simple "availability" signal tourists see.
 * [isOwnedByUser] marks places the user manages.
 */
@Entity(tableName = "places")
data class Place(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: PlaceCategory,
    val address: String,
    val city: String,
    val region: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val description: String = "",
    val status: PlaceStatus = PlaceStatus.OPEN,
    val openingHours: String? = null,
    val roomsFree: Int? = null,
    val availabilityNote: String? = null,
    val isOwnedByUser: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val listingType: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
