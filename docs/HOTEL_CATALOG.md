# Nepal hotels catalog (ships with the app)

The Android app does **not** read the repo `data/` folder. That folder is an offline
scrape/enrichment pipeline (OSM extracts, Python scripts, spreadsheets). Delete it
before a Play Store upload; the APK only contains what is under `app/`.

## What the app loads

| File | Role |
|---|---|
| `app/src/main/assets/hotels_seed.json` | Bundled catalog. 7,630 **named** lodgings with GPS, seeded into Room on first launch. |

Unnamed map POIs from the raw extract were dropped. Contact fields are sparse
(phone ~27%, email ~9%, photos 156). The app already degrades to name + pin.

Tourism regions in the seed: Pokhara & Annapurna, Kathmandu Valley, Everest Region,
Eastern Terai & Hills, Langtang & Central Hills, Chitwan & Terai Jungle,
Lumbini & Western Terai, Karnali & Rara, Far West.

## Sources

1. **OpenStreetMap** — lodging POIs, coordinates, names, most contacts. Licence: **ODbL 1.0** (attribution required).
2. **OCHA / HDX** — district/province boundaries (public domain).
3. **Wikidata** — cross-check QIDs / a few websites (CC0).
4. **Hotel websites / Wikimedia Commons** — a small set of photo URLs.

## Attribution (keep in-app)

> Accommodation data © OpenStreetMap contributors, available under the Open Database Licence (ODbL 1.0).
> Contains information from Wikidata (CC0) and Wikimedia Commons. Boundaries: OCHA/HDX (public domain).

The Map screen shows: `Accommodation data © OpenStreetMap contributors (ODbL)`.
Derived databases must stay ODbL if you blend this catalog with proprietary data.

Website photo URLs are the hotels’ own assets — get permission before promoting them
as official listing photos.
