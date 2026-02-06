# GlobeNews

An Android app that displays news stories on an interactive 3D globe. Zoom in and out to discover stories at different editorial scopes — from international headlines to local news.

## Features

- **Interactive 3D Globe** — Powered by Globe.gl in a WebView, with smooth pan/zoom via touch gestures
- **Zoom-level filtering** — Zoomed out shows international news, mid-zoom shows national, zoomed in shows local stories
- **Marker clustering** — Nearby markers cluster when zoomed out, expand on zoom-in
- **Story cards** — Tap a marker to see headline, source, summary, and a "Read More" link
- **Search** — Type a location name to fly the globe there
- **Dark theme** — Fullscreen globe with minimal UI chrome
- **Bundled fallback data** — 50 sample stories across regions so the app always has content

## Build

### Prerequisites

- Android SDK with API 34 (compileSdk/targetSdk)
- JDK 17+

### Build the debug APK

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Run on emulator

```bash
# Create an emulator (if needed)
avdmanager create avd -n Pixel6 -k "system-images;android-34;google_apis;x86_64" -d pixel_6

# Launch emulator
emulator -avd Pixel6 &

# Install and run
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.globenews/.MainActivity
```

## API Key Configuration

The app uses the [GNews API](https://gnews.io/) for live news data. To use your own key:

1. Sign up at https://gnews.io/ (free tier available)
2. Open `app/src/main/java/com/globenews/data/NewsRepository.kt`
3. Replace `DEMO_API_KEY_REPLACE_ME` with your API key:

```kotlin
private val apiKey = "YOUR_REAL_API_KEY_HERE"
```

Without a valid API key, the app falls back to bundled sample stories in `app/src/main/assets/fallback_news.json`.

## Project Structure

```
app/src/main/
├── assets/
│   ├── globe.html              # Globe.gl WebView page
│   └── fallback_news.json      # Bundled sample stories
├── java/com/globenews/
│   ├── MainActivity.kt         # Entry point
│   ├── data/
│   │   ├── NewsStory.kt        # Data model
│   │   ├── NewsRepository.kt   # API client + fallback loader
│   │   └── GeocodingHelper.kt  # Location search helper
│   ├── ui/
│   │   ├── GlobeNewsScreen.kt  # Main screen composable
│   │   ├── GlobeWebView.kt     # WebView composable with JS bridge
│   │   ├── StoryCard.kt        # Story detail popup card
│   │   └── Theme.kt            # Dark theme
│   └── viewmodel/
│       └── GlobeViewModel.kt   # State management
└── res/
    └── values/
        ├── strings.xml
        └── themes.xml
```

## Zoom Thresholds

| Globe Altitude | Scope Shown |
|---|---|
| > 1.5 | International only |
| 0.6 – 1.5 | International + National |
| < 0.6 | All (International + National + Local) |

## Tech Stack

- Kotlin + Jetpack Compose
- Globe.gl (via WebView)
- OkHttp for networking
- Material 3 dark theme
- AGP 8.7.3, Kotlin 2.0.21, Gradle 8.5
- Target SDK 34, Min SDK 26
