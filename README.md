# Emotionia — AI Emotion Mirror

> **Version 1.0** · Android · Jetpack Compose · Kotlin

Emotionia is an Android application that uses on-device AI to detect and track facial emotions in real time via the device camera. This document covers the architecture, structure, and implementation details of the first version.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture](#4-architecture)
5. [UI Components](#5-ui-components)
6. [Data Layer](#6-data-layer)
7. [Dependencies](#7-dependencies)
8. [Known Limitations (v1)](#8-known-limitations-v1)
9. [Roadmap](#9-roadmap)

---

## 1. Overview

Emotionia displays a live camera feed and runs a facial emotion recognition model in real time. The detected emotions (Neutral, Sad, Happy, Angry, Surprised) are visualised as animated progress bars, and session statistics (total interactions, average emotions, session duration) are accumulated and can be reset at any time.

| Feature | Status |
|---|---|
| Camera Preview Placeholder | ✅ |
| Animated Emotion Bars | ✅ |
| Status Card (model + camera state) | ✅ |
| Action Icons Row (Email / Share / Camera) | ✅ |
| Session Statistics Card | ✅ |
| Live CameraX integration | 🔜 |
| On-device ML inference | 🔜 |

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| State management | `StateFlow` / `collectAsState` |
| Async | Kotlin Coroutines |
| Build system | Gradle (KTS) |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 36 |

---

## 3. Project Structure

```
emotionia/
├── app/
│   └── src/main/java/com/mobile/emotion_ia/
│       ├── MainActivity.kt                    # Entry point
│       ├── data/
│       │   ├── model/
│       │   │   └── EmotionData.kt             # Data model
│       │   └── EmotionRepository.kt           # Repository interface + stub impl
│       └── ui/
│           ├── emotion_screen/
│           │   ├── EmotionScreen.kt           # Main screen composable
│           │   └── EmotionViewModel.kt        # ViewModel
│           └── theme/
│               ├── Color.kt
│               ├── Theme.kt
│               └── Type.kt
├── gradle/
│   └── libs.versions.toml                     # Version catalog
└── app/build.gradle.kts                       # App-level build config
```

---

## 4. Architecture

Emotionia follows the **MVVM (Model-View-ViewModel)** pattern recommended by Google for Jetpack Compose apps.

```
┌─────────────────────────────┐
│          View               │
│  EmotionScreen.kt           │  ← Composable, reads StateFlow, sends events
└────────────┬────────────────┘
             │ collectAsState / events
┌────────────▼────────────────┐
│        ViewModel            │
│  EmotionViewModel.kt        │  ← Holds & mutates UI state via MutableStateFlow
└────────────┬────────────────┘
             │ suspend calls / Flow
┌────────────▼────────────────┐
│       Repository            │
│  EmotionRepository.kt       │  ← Abstracts data source (camera / ML / DB)
└─────────────────────────────┘
```

### Data flow

1. `EmotionViewModel` holds a `MutableStateFlow<EmotionData>` initialised with mock data.
2. `EmotionScreen` collects the flow with `collectAsState()` and renders reactively.
3. User actions (e.g. "Reset Stats") call ViewModel functions, which mutate the state inside a `viewModelScope` coroutine.

---

## 5. UI Components

All composables live in `EmotionScreen.kt`. They are **private** and called from the public `EmotionScreen` entry point.

### `EmotionScreen`
Top-level composable. Hosts a vertically scrollable `Column` and composes all sub-components.

### `CameraPreviewCard`
Displays a rounded grey placeholder (`260 dp` tall) with a camera icon and label. This is the integration point for a future **CameraX** `PreviewView`.

```kotlin
// Replace with CameraX PreviewView via AndroidView
Icon(imageVector = Icons.Filled.PhotoCamera, ...)
```

### `EmotionBarsCard`
Renders five animated horizontal bars, one per emotion. Each bar uses `animateFloatAsState` with a 600 ms tween for smooth transitions.

| Emotion | Colour |
|---|---|
| Neutral | `#8E8E93` (grey) |
| Sad | `#4B8EF1` (blue) |
| Happy | `#FFD60A` (yellow) |
| Angry | `#FF3B30` (red) |
| Surprised | `#AF52DE` (purple) |

### `StatusCard`
Shows three rows:
- **Status** — current detection state + confidence score
- **Models** — whether the ML models are loaded (green ✓ / red ✗)
- **Camera** — whether the camera feed is active (green ✓ / red ✗)

### `ActionIconsRow`
A horizontal row of three icon buttons separated by thin vertical dividers:

| Icon | Label |
|---|---|
| `Icons.Filled.MailOutline` | Email |
| `Icons.Filled.Share` | Share |
| `Icons.Filled.PhotoCamera` | Camera |

> **Note:** These icons require `material-icons-extended` (added in v1 bug fix).

### `StatisticsCard`
Displays three collapsible sections:
- **Total Interactions** — integer counter
- **Average Emotions** — per-emotion percentages with matching accent colours
- **Session Info** — last visit timestamp, session duration, active time
- **Reset Stats** button (red) — calls `viewModel.resetStats()`

---

## 6. Data Layer

### `EmotionData` — Data Model

```kotlin
data class EmotionData(
    val neutral: Float,        // 0.0 – 1.0
    val sad: Float,
    val happy: Float,
    val angry: Float,
    val surprised: Float,
    val status: String,        // e.g. "Face detected!"
    val score: Float,          // Confidence score 0.0 – 1.0
    val modelsLoaded: Boolean,
    val cameraActive: Boolean,
    val totalInteractions: Int,
    val averageEmotions: Map<String, Float>,
    val lastVisit: String,     // e.g. "01:55:42"
    val duration: String,      // e.g. "0:00:19"
    val activeTime: String     // e.g. "0:00:05"
)
```

### `EmotionRepository` — Interface

```kotlin
interface EmotionRepository {
    fun getLiveEmotionData(): Flow<EmotionData>
    suspend fun resetStats()
}
```

`EmotionRepositoryImpl` is a stub (all methods throw `TODO`). The real implementation will wire a **CameraX** stream through an **on-device ML model** (TFLite / MediaPipe).

---

## 7. Dependencies

Defined in `gradle/libs.versions.toml` and applied in `app/build.gradle.kts`.

| Dependency | Version |
|---|---|
| `androidx.core:core-ktx` | 1.17.0 |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.10.0 |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.10.0 |
| `androidx.activity:activity-compose` | 1.12.4 |
| `androidx.compose` (BOM) | 2024.09.00 |
| `androidx.compose.material3` | via BOM |
| `androidx.compose.material:material-icons-extended` | via BOM |
| AGP | 9.0.1 |
| Kotlin | 2.0.21 |

---

## 8. Known Limitations (v1)

- **No real camera feed.** `CameraPreviewCard` is a static placeholder; CameraX is not yet integrated.
- **No ML inference.** `EmotionRepositoryImpl` stubs all methods with `TODO()`. The ViewModel uses hardcoded mock data.
- **No navigation.** The app has a single screen (`EmotionScreen`) set directly in `MainActivity`.
- **No dependency injection.** The ViewModel is created with `viewModel()` without a factory; the repository is not injected.
- **No persistence.** Session statistics are in-memory only and lost on process death.

---

## 9. Roadmap

- [ ] **CameraX integration** — replace `CameraPreviewCard` placeholder with a live `PreviewView`
- [ ] **TFLite / MediaPipe model** — run facial landmark + emotion classification on-device
- [ ] **Real repository** — connect `EmotionRepositoryImpl` to the ML pipeline
- [ ] **Hilt DI** — inject the repository into the ViewModel
- [ ] **Room persistence** — store session statistics locally
- [ ] **Navigation** — add a settings / history screen
- [ ] **Notifications / alerts** — surface mood trends to the user
- [ ] **Contacts sharing** — send emotion summaries via email or share sheet

---

*Documentation generated for Emotionia v1.0 — 2026-03-16*
