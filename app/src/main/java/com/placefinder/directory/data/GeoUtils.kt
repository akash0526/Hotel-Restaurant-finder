package com.placefinder.directory.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    /** Approximate great-circle distance between two WGS84 points, in meters. */
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val radius = 6_371_000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = Math.toRadians(lat2 - lat1)
        val dl = Math.toRadians(lng2 - lng1)
        val a = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
        return 2 * radius * asin(sqrt(a))
    }

    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000.0)
        }
    }

    fun listingTypeLabel(listingType: String?): String {
        return when (listingType?.lowercase()) {
            "guest_house", "guesthouse" -> "Guest house"
            "hostel" -> "Hostel"
            "apartment" -> "Apartment"
            "chalet" -> "Chalet"
            "motel" -> "Motel"
            "homestay" -> "Homestay"
            "resort" -> "Resort"
            "hotel", null, "" -> "Hotel"
            else -> listingType.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
    }
}
