package com.placefinder.directory.data.seed

import android.content.Context
import androidx.room.withTransaction
import com.placefinder.directory.data.database.PlaceDatabase
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceCategory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads the Nepal hotels catalog from [assets/hotels_seed.json] into Room once.
 * That asset is the only catalog the APK ships; the repo `data/` pipeline is not used
 * at runtime and can be deleted before a Play Store build.
 * Seeded rows are not owned by the user, so they show on Explore/Map but not My Places.
 */
object PlaceSeeder {

    private const val PREFS = "place_seed"
    private const val KEY_DONE = "hotels_nepal_v1"
    private const val ASSET = "hotels_seed.json"
    private const val SQLITE_CHUNK = 30

    suspend fun seedIfNeeded(context: Context, db: PlaceDatabase) {
        val dao = db.placeDao()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (dao.countCatalog() > 0) {
            prefs.edit().putBoolean(KEY_DONE, true).apply()
            return
        }
        if (prefs.getBoolean(KEY_DONE, false)) return

        val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val places = ArrayList<Place>(array.length())
        for (i in 0 until array.length()) {
            places.add(array.getJSONObject(i).toPlace())
        }

        db.withTransaction {
            places.chunked(SQLITE_CHUNK).forEach { chunk ->
                dao.insertAll(chunk)
            }
        }
        prefs.edit().putBoolean(KEY_DONE, true).apply()
    }

    private fun JSONObject.toPlace(): Place {
        val city = optString("city")
        val address = optString("a").ifBlank {
            listOfNotNull(city.takeIf { it.isNotBlank() }, optString("r").takeIf { it.isNotBlank() })
                .joinToString(", ")
        }
        val stars = if (has("s")) optDouble("s") else 0.0
        return Place(
            id = getString("id"),
            name = getString("n"),
            category = PlaceCategory.HOTEL,
            address = address,
            city = city,
            region = optString("r").ifBlank { null },
            latitude = optDouble("lat"),
            longitude = optDouble("lon"),
            phone = optString("p").ifBlank { null },
            email = optString("e").ifBlank { null },
            website = optString("w").ifBlank { null },
            description = optString("d"),
            openingHours = optString("h").ifBlank { null },
            rating = if (stars.isNaN()) 0.0 else stars,
            listingType = optString("t").ifBlank { "hotel" },
            photoUrl = optString("photo").ifBlank { null },
            isOwnedByUser = false,
        )
    }
}
