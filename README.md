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
- All data is device-local (Room database `places.db`)

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
4. ✅ **Stage 4 — done (Firestore):** `FirestorePlaceRepository` is the app's source of
   truth — hotels update listings and availability live; the app keeps a live view of the
   `places` collection. New deps: `com.google.firebase:firebase-firestore:26.6.0`.

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

## Taking it to the Play Store (separate milestone)

When the app works locally: generate a signed App Bundle (`./gradlew :app:bundleRelease`,
add a release signingConfig), set up the $25 Google Play Developer account, and go through
the Play Console publishing flow.
