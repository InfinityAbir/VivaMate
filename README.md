<div align="center">

# VivaMate

**AI-Powered Viva Practice for Students**

Practice oral exams, lab vivas, thesis defenses & more with an intelligent AI tutor.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/AI-Gemini-4285F4?style=flat&logo=google)](https://ai.google.dev)

</div>

---

## About

VivaMate is an Android application that helps students prepare for oral examinations using AI-powered mock viva sessions. Built with modern Android technologies and Google's Gemini AI, it provides personalized practice questions, real-time feedback, and performance analytics.

## Features

- **AI-Powered Viva Sessions** — Engage in realistic oral exam practice with Gemini AI
- **Subject Library** — Create and manage subjects for organized study
- **Speech Recognition** — Voice-based interaction for natural viva simulation
- **Session Summary** — Review detailed feedback after each practice session
- **Revision Plans** — Track and plan your revision schedule
- **Progress Analytics** — Monitor your performance over time
- **Onboarding** — Guided setup for new users
- **Dark/Light Theme** — Customizable appearance

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Programming Language |
| Jetpack Compose | Declarative UI |
| Room Database | Local Persistence |
| Firebase AI (Gemini) | AI Integration |
| Navigation Compose | Screen Navigation |
| DataStore | Preferences Storage |
| Retrofit + OkHttp | Networking |
| Moshi | JSON Serialization |
| Coroutines + Flow | Async Operations |

## Architecture

```
app/src/main/java/com/example/
├── data/
│   ├── local/          # Room DB, DAOs, DataStore
│   ├── remote/gemini/  # Gemini AI service layer
│   ├── repository/     # Data repositories
│   ├── speech/         # Speech-to-text services
│   ├── billing/        # Billing integration
│   └── model/          # Data entities
├── ui/
│   ├── screens/        # Feature screens
│   ├── components/     # Reusable UI components
│   ├── viewmodel/      # ViewModels
│   ├── navigation/     # Navigation graph
│   └── theme/          # App theming
└── MainActivity.kt
```

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Hedgehog or later)
- Android SDK 36
- A [Gemini API Key](https://ai.google.dev/)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/InfinityAbir/vivamate.git
   cd vivamate
   ```

2. **Configure API Key**

   Create a `.env` file in the project root:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Open in Android Studio**

   Open the project directory in Android Studio and allow Gradle sync to complete.

4. **Remove AI Studio signing config**

   In `app/build.gradle.kts`, remove this line:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

5. **Build & Run**

   Run the app on an emulator or connected physical device.

## Project Structure

| Screen | Description |
|--------|-------------|
| Onboarding | First-time user setup |
| Home | Dashboard with quick actions |
| Viva Setup | Configure a new viva session |
| Viva Room | Live AI-powered viva practice |
| Session Summary | Review session performance |
| Subject Library | Manage study subjects |
| Revision Plan | Schedule and track revisions |
| Analytics | View performance history |
| Settings | App preferences |
| Privacy | Data management options |

## License

This project was generated with [Google AI Studio](https://ai.studio).

---

<div align="center">

Made with AI assistance

</div>
