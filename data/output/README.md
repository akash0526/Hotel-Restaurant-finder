# Nepal Hotels & Accommodation — Master Database

**Compiled:** 26 November 2026 · **Records:** 8,281 · **Coverage:** all 7 provinces, 75 of 77 districts

Built for a tourist-facing hotel social media app. Every record carries exact GPS coordinates,
official district/province, category, whatever contact channels exist, and a licence-clean photo
reference where one could be verified.

---

## Files

| File | What it is |
|---|---|
| `Nepal_Hotels_Master_Database.xlsx` | **Start here.** 5 tabs: Summary, By District, By Region, Hotels (all 8,281), Contactable (2,326) |
| `hotels_nepal_master.csv` | Full dataset, 42 columns, UTF-8 with BOM (opens cleanly in Excel) |
| `hotels_nepal_with_contacts.csv` | The 2,326 hotels that have a phone, mobile or email — your outreach/claim-list |
| `hotels_nepal_master.json` | Same data as JSON, for direct import into your app backend |
| `README.md` | This document |

---

## What's actually in the data

| Field | Coverage | Notes |
|---|---|---|
| Name | 7,630 (92%) | 651 records are unnamed map POIs — useful as locations, not as listings |
| Exact coordinates | **8,281 (100%)** | Decimal degrees, WGS84. Every single record has lat/lon |
| District | 8,250 (99.6%) | Official 77-district names, assigned by point-in-polygon |
| Province | 8,281 (100%) | Official federal province names |
| Category | 8,281 (100%) | hotel 4,171 · guest_house 2,914 · hostel 615 · apartment 471 · chalet 59 · motel 47 |
| Phone / mobile | 2,278 (27%) | As tagged in OSM, plus numbers scraped from hotel websites |
| Email | 757 (9%) | |
| Website | 560 (7%) | 268 confirmed reachable at build time |
| Facebook / Instagram | 65 (~1%) | Weakest field — see "Gaps" below |
| Photo | 158 (1.9%) | 152 from the hotel's own site, 5 from Wikimedia Commons, 1 unattributed |
| Star rating | 78 | Nepal-specific and sparse; do not treat as official DoT classification |
| Rooms / beds | 289 / 187 | |

**Average completeness across 12 key fields: 14.4%.** That number is the honest headline.
The structure is complete; the *contact layer* is thin. Build your app so it degrades gracefully
when a hotel has a name, a pin and nothing else.

### Tourism regions (app-friendly grouping)

| Region | Records |
|---|---|
| Pokhara & Annapurna | 2,517 |
| Kathmandu Valley | 2,167 |
| Everest Region | 775 |
| Eastern Terai & Hills | 668 |
| Langtang & Central Hills | 563 |
| Chitwan & Terai Jungle | 526 |
| Lumbini & Western Terai | 473 |
| Karnali & Rara | 366 |
| Far West | 188 |

Top districts: Kaski 1,531 · Kathmandu 1,401 · Solukhumbu 668 · Kavrepalanchok 331 · Rupandehi 301 · Chitawan 278 · Manang 251 · Mustang 218 · Lalitpur 187 · Rasuwa 182.

---

## Where the data came from

1. **OpenStreetMap** — Geofabrik's Nepal extract, parsed locally with `osmium`. Source of all 8,540
   raw lodging POIs, coordinates, names and most contact fields. Licence: **ODbL 1.0** (attribution required).
2. **OCHA / HDX `cod-ab-npl`** — official admin-2 boundaries, used for district + province assignment.
   Licence: public domain.
3. **Wikidata** — 708 Nepal hotels, used for cross-checking, QIDs, and a few stars/websites.
4. **Wikimedia Commons** — mined 321 Nepal hotel images; only 5 passed strict verification.
5. **Hotel own websites** — 575 fetched, 268 reachable; yielded 152 hero photos, plus some phones and emails.

### Quality control that was actually run

- **District assignment was initially wrong and got fixed.** The first boundary file I used
  (geoBoundaries) placed Pokhara's Lakeside in Syangja district and had no Rupandehi at all
  (zero hotels in Lumbini's gateway city). Swapping to OCHA/HDX moved 310 records into Rupandehi
  and 576 more into Kaski. Boundaries now verified against 10 known landmarks.
- **Photo matching was deliberately cut from 171 → 158.** Early fuzzy matching attached a
  Blue Horizon photo to "Kathmandu Guest House", a generic Everest View photo to "Everest Guest House"
  in Kathmandu, and a Manaslu photo to 7 unrelated trekking lodges. Mountain names (Annapurna,
  Everest, Manaslu, Kailash, Fishtail) are reused by hundreds of lodges, so name matching alone is
  unsafe. Only exact/location-verified matches were kept. **Small and correct beats large and wrong.**
- Every image URL was fetched at build time and checked for HTTP 200 + `image/*` content-type +
  >12 KB, and logo/icon/flag filenames were rejected.
- 259 duplicate listings (same hotel mapped twice within 120 m) were merged, richest record kept.

---

## Known gaps — read before you build

- **Facebook/Instagram is the weakest field (~1%).** For a social media app this is the most valuable
  field and the one open data does not have. Nepali hotels live on Facebook far more than on websites.
- **No prices, no star-classification you can trust, no opening hours at scale.** Nepal's official
  classification list sits behind a login at `hotel.dot.gov.np`, and no public bulk list exists.
- **158 photos is a seed, not a catalogue.** They also belong to the hotels — the 152 website photos
  are marked `© hotel website – permission required`. Get written consent or a link-back agreement
  before publishing them in-app. The 5 Wikimedia ones list their licence in `photo_license`.
- **Trekking lodges dominate the raw count.** Solukhumbu, Manang, Mustang, Rasuwa and Myagdi are
  full of small teahouses with a name and a pin only. Use the `is_trekking_lodge` flag to filter.
- **651 unnamed POIs** — keep them for map density or drop them for listings.

## Suggested next steps to close the gap

1. **Claim-your-listing flow.** Use `hotels_nepal_with_contacts.csv` (2,326 hotels) as the seed for
   outreach; let owners self-serve the missing fields.
2. **Targeted enrichment by hand** for the ~500 commercially important hotels (5-star, resorts,
   Kathmandu/Pokhara/Chitwan). That is a few days of work and gets you a genuinely strong launch set.
3. **Google Places / Foursquare** enrichment for phone, hours, rating and photos — paid, and check
   each provider's terms on caching and display before shipping.
4. **Department of Tourism classified-hotel list** — request bulk access, or transcribe the published
   PDFs, to get true 1–5 star classification.

## Attribution

> Accommodation data © OpenStreetMap contributors, available under the Open Database Licence (ODbL 1.0).
> Contains information from Wikidata (CC0) and Wikimedia Commons. Boundaries: OCHA/HDX (public domain).
> Photo credits are per-record in `photo_author` / `photo_license` / `photo_source`.

If you display this in-app, keep a visible "Data © OpenStreetMap contributors" credit and offer the
ODbL notice. Shared/derived databases must stay ODbL — worth knowing before you blend in proprietary data.
