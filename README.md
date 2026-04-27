# Lost & Found App — SIT708 Task 7.1

An Android mobile application that allows users to post, browse, search, and manage lost and found item listings, backed by a local SQLite database.

---

## Overview

The Lost & Found app provides a simple and clean interface for reporting lost items or listing items that have been found. Users can attach an image, categorise their posting, and include contact details so others can reach out. All data is stored locally on the device using SQLite.

---

## Features

- **Post adverts** — Create listings for lost or found items with full details
- **Image upload** — Attach a photo from the device gallery using the system file picker
- **Category filtering** — Filter listings by Electronics, Pets, Wallets, Keys, Clothing, or Other
- **Live search** — Search across item names and descriptions in real time
- **Time stamps** — Each listing shows how long ago it was posted (e.g. "2 hours ago")
- **Item detail view** — View all information for a selected listing
- **Remove listings** — Delete a posting with a confirmation dialog
- **Persistent storage** — All data survives app restarts via SQLite

---

## Screens

| Screen | Description |
|---|---|
| **Home** | Entry point with two navigation buttons |
| **Create Advert** | Form to submit a new lost or found listing |
| **Item List** | Scrollable list of all postings with search and filter |
| **Item Detail** | Full details of a selected item with remove option |

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Min SDK | API 24 (Android 7.0 Nougat) |
| Target SDK | API 36 (Android 16) |
| Database | SQLite via `SQLiteOpenHelper` |
| UI | XML Layouts, ConstraintLayout, MaterialComponents |
| Image Picker | `ActivityResultLauncher` with `ACTION_OPEN_DOCUMENT` |
| Build System | Gradle with Kotlin DSL (`build.gradle.kts`) |

---

## Project Structure

```
app/src/main/
├── java/com/example/lostandfound/
│   ├── MainActivity.java          # Home screen
│   ├── CreateAdvertActivity.java  # New listing form
│   ├── ItemListActivity.java      # List with search & filter
│   ├── ItemDetailActivity.java    # Detail view & remove
│   ├── DatabaseHelper.java        # SQLite CRUD operations
│   ├── LostFoundItem.java         # Model / POJO class
│   └── ItemAdapter.java           # RecyclerView adapter
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_create_advert.xml
│   │   ├── activity_item_list.xml
│   │   ├── activity_item_detail.xml
│   │   └── item_row.xml
│   └── values/
│       ├── strings.xml
│       ├── colors.xml
│       └── themes.xml
└── AndroidManifest.xml
```

---

## Database Schema

**Table:** `lost_found_items`

| Column | Type | Description |
|---|---|---|
| `id` | INTEGER PK | Auto-incremented row ID |
| `post_type` | TEXT | `Lost` or `Found` |
| `name` | TEXT | Poster's name |
| `phone` | TEXT | Contact phone number |
| `description` | TEXT | Item description |
| `date` | TEXT | Date the item was lost/found |
| `location` | TEXT | Where it was lost/found |
| `category` | TEXT | Electronics / Pets / Wallets / Keys / Clothing / Other |
| `image_uri` | TEXT | Content URI of the attached image |
| `created_at` | TEXT | ISO timestamp set on insert |

---

## Setup & Installation

1. Clone or download this repository
2. Open the project in **Android Studio Ladybug** or later
3. Let Gradle sync complete (it will download all dependencies automatically)
4. Run the app on an emulator (API 24+) or a physical Android device

No API keys or external services are required — the app runs entirely offline.

---

## Dependencies

Declared in `gradle/libs.versions.toml` and applied in `app/build.gradle.kts`:

```
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.recyclerview:recyclerview:1.3.2
androidx.constraintlayout:constraintlayout:2.1.4
androidx.activity:activity:1.9.0
```

---

## Permissions

| Permission | Purpose |
|---|---|
| `READ_EXTERNAL_STORAGE` | Image access on Android 12 and below |
| `READ_MEDIA_IMAGES` | Image access on Android 13 and above |

---

## Color Scheme

| Role | Hex |
|---|---|
| Primary (ActionBar, buttons) | `#1565C0` |
| Accent | `#42A5F5` |
| Lost badge | `#E53935` |
| Found badge | `#43A047` |

---

## Author

**Vedant Pandya**  
SIT708 — Mobile Application Development  
Task 7.1 — Lost & Found App
