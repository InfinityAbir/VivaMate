# VivaMate — AI Viva Practice for Students

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/AI-Gemini-FFCA28?style=for-the-badge&logo=google&logoColor=black" alt="Gemini AI" />
</p>

<p align="center"><b>Stop memorizing. Start speaking.</b></p>

<p align="center">Realistic AI mock vivas for oral exams, lab vivas, thesis defenses, and interviews — with voice input, dual scoring, model answers, and revision plans.</p>

---

## Table of Contents

- [About the Project](#about-the-project)
- [Why I Built VivaMate](#why-i-built-vivamate)
- [Who Will Use It](#who-will-use-it)
- [Why They Will Use It](#why-they-will-use-it)
- [Key Features](#key-features)
- [How a Session Works](#how-a-session-works)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Permissions & Privacy](#permissions--privacy)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Author & Contact](#author--contact)

---

## About the Project

**VivaMate** is a native Android app that simulates oral examinations with Google's Gemini AI as your examiner.

Create a subject, add topics or paste your syllabus, configure a session (Viva, Lab Viva, Thesis Defense, Interview, Rapid Practice), then answer by voice or text. After each answer you get conceptual + communication scores, strengths, missing points, misconceptions, corrections, a model answer, and follow-up questions with adaptive difficulty. At the end you get a session summary, analytics, and a prioritized revision plan.

Package: `com.aistudio.vivamate.qxvz` • Min SDK 24 • Target SDK 36

---

## Why I Built VivaMate

Viva exams are a different skill from written exams — and most students practice them badly:

1. **No practice partner:** You can’t rehearse out loud alone. Friends are busy, teachers have limited time, and reading notes silently doesn’t build speaking confidence.
2. **Anxiety without feedback:** Students walk into lab vivas and thesis defenses not knowing how they sound, where they ramble, or which misconceptions they repeat.
3. **Generic Q&A isn’t enough:** Flashcards test recall, not explanation, comparison, application, troubleshooting, or practical lab reasoning under follow-up pressure.
4. **No tracking:** Without scores and revision lists, students repeat the same weak topics before every exam.

I built VivaMate to be the strict but supportive examiner that’s always available:

- Real viva flow with voice-first interaction, not just MCQs
- Examiner-style follow-ups that adapt to your last answer
- Honest dual scoring (what you know + how you say it) with model answers
- A revision system that turns every mistake into a next action
- Offline-first local data so your subjects and history stay yours

It’s also my deep dive into voice UX on Android, structured Gemini prompting with JSON contracts, and Room modeling for a real tutoring domain.

---

## Who Will Use It

- **Undergraduate / Postgraduate / Diploma / High-school students** preparing for course vivas and oral exams.
- **Lab students** facing practical / lab vivas with procedure, observation, and troubleshooting questions.
- **Thesis / project students** preparing for defense presentations and committee cross-questioning.
- **Job / internship candidates** practicing technical interviews out loud.
- **Non-native English speakers** who want to improve explanation clarity alongside subject knowledge.
- **Teachers / tutors** looking for a question-generation and revision-planning aid for their students.

Supports multiple academic levels and languages (configurable per session).

---

## Why They Will Use It

- **Realistic, not robotic:** Sessions feel like a viva — sequential questions, examiner hints, estimated answer times, clarification prompts, and adaptive next-question difficulty based on your performance.
- **Voice-first:** Android Speech-to-Text for natural answering, with text fallback when you’re in a noisy room. Transcripts are reviewable.
- **Two scores that matter:** `conceptualScore` (correctness, key points, misconceptions) + `communicationScore` (clarity, structure) — so you fix both knowledge and delivery.
- **Learn from every answer:** Strengths, missing points, misconceptions, concise correction, model answer, and suggested revision topics after each response.
- **Organized study:** Subject Library + Topics with syllabus text, importance, and target confidence; Revision Plan with High/Medium/Low priorities you can check off.
- **Progress you can see:** Session summaries, per-question breakdowns, and history analytics across subjects and difficulty levels.
- **Private and controllable:** Local Room storage, transcript/audio retention toggles, analytics consent, and privacy/deletion screen. Mock Gemini service allows practice without an API key.

---

## Key Features

- **5 Session Modes** — Viva, Lab Viva, Thesis Defense, Interview, Rapid Practice
- **Adaptive Difficulty** — Beginner / Intermediate / Advanced / Adaptive with `nextQuestionDifficulty` after each evaluation
- **7 Question Types** — definition, concept, comparison, application, troubleshooting, practical_lab, follow_up
- **Voice & Transcript UI** — `VoiceAndTranscript` components, liveTranscript, answer modes (Voice/Text)
- **Detailed Evaluation** — strengths, missingPoints, misconceptions, correction, modelAnswer, revisionTopics, clarificationQuestion
- **Session Summary** — Overall / conceptual / communication scores, answered vs. total, per-question review
- **Subject & Topic Library** — CRUD subjects, colors, levels, archived flag; topics with syllabusText and importance
- **Revision Plan** — Auto-generated `RevisionRecommendation` list with priority and completion tracking
- **Analytics** — History + performance trends by subject, mode, and difficulty
- **Onboarding, Settings, Paywall, Support** — Guided setup, theme/language/retention prefs, premium scaffolding, help screen
- **Offline Resilience** — `MockGeminiService` + `FakeSpeechToTextService` for development and no-key runs

---

## How a Session Works

1. **Setup** — Pick subject + topic, mode, difficulty, language, total questions (`VivaSetupScreen` + `VivaSetupViewModel`)
2. **Room** — Examiner asks Q1 with hint and timer estimate (`VivaRoomScreen`). Answer by voice or text.
3. **Evaluate** — `LiveGeminiService` evaluates against expected key points → `VivaEvaluationResult` (scores, strengths, missing, misconceptions, correction, model answer, next difficulty)
4. **Adapt** — Next question is generated with context: previous Q/A, weak areas, performance summary (`VivaQuestionContext`)
5. **Summarize** — End session → overall scores, question-by-question review, revision recommendations (`SessionSummaryScreen`)
6. **Revise** — Work through `RevisionPlanScreen`, track completion, start a focused follow-up session

All entities (`Subject`, `Topic`, `VivaSession`, `VivaQuestion`, `VivaResponse`, `RevisionRecommendation`) persist in Room for analytics.

---

## Tech Stack

**Language & UI:**
- Kotlin, Jetpack Compose (Material 3), Navigation Compose, reusable components (`Cards`, `Buttons`, `FeedbackBottomSheet`, `ProgressIndicators`, `VoiceAndTranscript`)

**AI:**
- Firebase AI / Gemini via `data/remote/gemini/GeminiService.kt`, `LiveGeminiService.kt`, `MockGeminiService.kt` with structured JSON evaluation contracts; Secrets Gradle Plugin for `.env` key

**Voice:**
- `SpeechToTextService` interface + `AndroidSpeechToTextService` + `FakeSpeechToTextService` for real and test paths

**Data:**
- Room (5 DAOs: Subject, Session, Question, Response, Recommendation), DataStore Preferences (`PreferencesDataStore`), Moshi + Retrofit/OkHttp for any REST needs, Coroutines + Flow

**Monetization & Build:**
- Billing integration scaffolding (`BillingService`), AGP + KSP, Secrets plugin, Google Services passthrough, Roborazzi + Compose UI tests

See `gradle/libs.versions.toml` and `app/build.gradle.kts` for full versions.

---

## Architecture Overview

MVVM + Repository, feature-split UI:

- `MainActivity.kt` — NavHost entry
- `data/model/Entities.kt` — Room entities + context/result models (`VivaQuestionContext`, `VivaEvaluationResult`, `UserSettings`)
- `data/remote/gemini/` — prompt builders, JSON parsers, live vs. mock switch
- `data/speech/` — STT abstraction for testability
- `data/local/` — `AppDatabase`, DAOs, `PreferencesDataStore`
- `data/repository/VivaRepository.kt`, `SettingsRepository.kt` — session orchestration + prefs
- `data/billing/BillingService.kt` — premium hooks
- `ui/screens/` — `home/`, `setup/`, `room/`, `summary/`, `library/`, `revision/`, `analytics/`, `settings/`, `privacy/`, `paywall/`, `support/`, `onboarding/`
- `ui/viewmodel/` — `VivaSetupViewModel`, `VivaRoomViewModel`, `SessionSummaryViewModel`, `SubjectViewModel`, `HomeViewModel`, `SettingsViewModel`
- `ui/navigation/Screen.kt`, `ui/components/`, `ui/theme/`

---

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Hedgehog or newer)
- Android SDK 36, JDK 11+
- A [Gemini API Key](https://ai.google.dev/) (optional for mock mode, required for live AI examiner)

### Setup

1. Clone:
   ```bash
   git clone https://github.com/InfinityAbir/VivaMate.git
   cd VivaMate
   ```
2. Create `.env` in project root (see `.env.example`):
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
3. Open in Android Studio and let Gradle sync complete.
4. For your own Play release, set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` or drop a `debug.keystore` for local runs (see `app/build.gradle.kts` signing logic).
5. Run on emulator or physical device (physical device recommended for microphone testing, Min SDK 24).

> Without a key, the app still runs with `MockGeminiService` + `FakeSpeechToTextService` for UI and flow testing.

---

## Project Structure

| Screen | Description |
|--------|-------------|
| Onboarding | First-run setup |
| Home | Dashboard with quick actions |
| Viva Setup | Configure subject, topic, mode, difficulty, language |
| Viva Room | Live voice/text Q&A with examiner |
| Session Summary | Scores + per-question feedback |
| Subject Library | Manage subjects + Create/Edit Subject |
| Revision Plan | Prioritized follow-up list |
| Analytics | Performance history and trends |
| Settings | Theme, language, retention, reminders |
| Privacy | Transcript/audio retention + deletion |
| Paywall / Support | Premium + help |

---

## Permissions & Privacy

| Permission / Data | Why / Control |
|---|---|
| `RECORD_AUDIO` | Voice answers via Speech-to-Text (optional — text mode works without it) |
| `INTERNET` | Live Gemini examiner calls |
| Transcripts | Stored in Room only if `retainTranscripts = true`; deletable via Privacy screen |
| Audio | Not retained by default (`retainAudio = false`) |
| Analytics | Opt-in via `analyticsConsent` flag |

No ads SDKs. Your subjects, sessions, and answers stay on-device unless you explicitly share them.

---

## Roadmap

- [ ] Text-to-Speech examiner voice + barge-in
- [ ] Syllabus PDF import → auto topic + question bank generation
- [ ] Timer pressure mode + rapid-fire viva drills
- [ ] Export summary as PDF for teachers
- [ ] Streaks / daily reminder + revision notifications
- [ ] Cloud backup / classroom sharing (opt-in)
- [ ] Play Store release

---

## Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m "Add your feature"`
4. Push and open a Pull Request

If you add a question type or evaluation rubric, please update `Entities.kt`, the Gemini prompt contract, and include a sample session transcript in your PR description.

---

## License

This project is currently **All Rights Reserved** — source available for learning and review.

Contact the author to reuse code or propose an open-source license (e.g., MIT).

---

## Author & Contact

**Abir Hasan (InfinityAbir)** — building clean, secure, real-world apps with ASP.NET Core, Android, and AI integration.

- GitHub: https://github.com/InfinityAbir
- Portfolio: https://infinityabir.github.io/abir-hasan-portfolio/
- LinkedIn: https://www.linkedin.com/in/infinityabirhasan/
- Email: abirha3896@gmail.com

> If VivaMate helps you pass a viva, please ⭐ star the repo and share which mode (Lab / Thesis / Interview) helped most — it guides what I build next.
