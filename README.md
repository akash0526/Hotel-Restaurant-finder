# hotel-restaurant-directory

Android app: hotels and restaurants publish listings (contact details, availability) and
tourists browse them.

- **Stack:** Kotlin + Jetpack Compose + Firestore (remote sync, Stage 4); Room (SQLite) kept for a later local cache
- **Package:** `com.placefinder.directory`

## Build & run

```
./gradlew :app:assembleDebug     # build the app
./gradlew :app:installDebug      # install on a device or emulator
```

## Current state (Stages 1 + 2 + 3)

- Explore screen: live search over all published places
- Map screen: places as pins on Google Maps, radius filter, "My location" button
- My Places screen: places owned by this device, with delete
- Place detail: address, description, phone, with a "Call place" button
- Editor: add/edit a hotel/restaurant profile (name, category, address, phone,
  description, availability), used from My Places
- **Nepal hotels catalog** is seeded on first launch from `app/src/main/assets/hotels_seed.json`
  (7,630 named lodgings with GPS). Explore lists hotels near you; Map drops pins within a
  radius of your GPS or a searched city (Pokhara, Butwal, Kathmandu, …).
- All listing data is device-local (Room database `places.db`). See [docs/HOTEL_CATALOG.md](docs/HOTEL_CATALOG.md).

## Planned stages

1. ✅ **Stage 1 — done (scaffold):** search + local data (this README)
2. ✅ **Stage 2 — done:** places map, `placesByCity`/`placesByRegion` queries, location
   permission (`ACCESS_FINE_LOCATION`), radius filter + "My location" button (your
   "scroll and search on the go" requirement). New deps: `play-services-maps`,
   `play-services-location`. Note: add a Google Maps API key in `AndroidManifest.xml`
   before running the app.
3. ✅ **Stage 3 — done:** "My Places" editor + delete: create/update a hotel/restaurant
   profile (name, category, address, phone, availability), delete places. New:
   `PlaceEditorScreen`, `PlaceEditorViewModel`.
4. ✅ **Stage 4 — Firestore client kept** (`FirestorePlaceRepository`) for a later remote
   sync. The tourist catalog now ships in-app so Explore/Map work without Firebase.
5. ✅ **Stage 5 — Nepal hotel seed + nearby map:** Room is seeded from OpenStreetMap-derived
   `hotels_seed.json`. Location permission centers the map; radius slider filters pins;
   search jumps to a city. Explore sorts by distance.

### Stage 4 setup (required to run the app with Firestore)

1. Create a **Firebase project** at [firebase.google.com/console](https://firebase.google.com/console).
2. Register the Android app with package `com.placefinder.directory`.
3. Download `google-services.json` and place it in `app/` (the google-services plugin
   reads it during the build).
4. Create a `places` collection in Firestore (Database section) and set public read rules
   (tighten later with ownership checks):

```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /places/{document=**} {
      allow read, write: if true; // public listing; tighten later with ownership checks
    }
  }
}
```

### Stage 5 (optional later)

A login flow (Firebase Authentication) to let hotels "claim" places — currently
`isOwnedByUser` is a field on the listing.

## Version notes

Build files reference recent, stable versions (AGP 8.7.3, Kotlin 2.0.20, Room 2.6.1,
Compose ~1.7.x). If Gradle suggests newer/incompatible versions, bump them in the
build files — the structure is the part that matters.

## Play Store: the `data/` folder is not part of the app

Gradle only builds `:app`. The repo `data/` directory is the **offline pipeline** that
produced the catalog (raw OSM JSON, stage files, xlsx/csv). It is **not** packaged in
the APK.

- **Keep:** `app/src/main/assets/hotels_seed.json` (this is what tourists see)
- **Keep:** `docs/HOTEL_CATALOG.md` (sources + ODbL attribution)
- **Safe to delete** before you upload to Play: the entire `data/` folder

```
rm -rf data
./gradlew :app:bundleRelease
```

When the app works locally: generate a signed App Bundle, add a release signingConfig,
set up the $25 Google Play Developer account, and go through the Play Console publishing flow.
