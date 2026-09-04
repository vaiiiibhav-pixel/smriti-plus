# 🧠 SMRITI+

### AI-Powered Cognitive & Memory Assistance Platform for Elderly Care

> **Helping seniors stay mentally active, independent, and connected — while keeping caregivers informed.**

SMRITI+ is an AI-powered elderly care platform designed to support **cognitive wellness, memory assistance, daily reminders, and caregiver monitoring** through an intuitive, elderly-friendly interface.

The platform combines **cognitive games, adaptive difficulty, performance analysis, voice interaction, reminders, and caregiver insights** into one accessible application.

---

## 🌟 Why SMRITI+?

As people age, maintaining cognitive activity and remembering everyday tasks can become increasingly difficult.

At the same time, family members and caregivers may not always be available to continuously monitor their loved one's cognitive engagement and daily activities.

**SMRITI+ bridges this gap.**

Instead of simply providing a collection of games or reminders, SMRITI+ continuously observes user performance and uses that information to provide **personalized cognitive exercises and meaningful insights for caregivers.**

---

## ✨ Key Features

### 🌟 Personalized Onboarding & Essential Permissions
To ensure privacy, comfort, and personalized care from the very first launch, SMRITI+ introduces a 2-step onboarding experience:
1. **Interactive Permission Manager**:
   - Transparently requests **Microphone & Voice Access** (`RECORD_AUDIO`) for hands-free natural speech navigation.
   - Requests **Medicine & Water Notifications** (`POST_NOTIFICATIONS`) for timely daily alerts and hydration reminders.
   - Provides clear explanations for why each permission is essential to elderly wellness.
2. **Personal Information & Emergency Profile**:
   - Captures preferred name, age, caregiver / family contact details, and emergency phone numbers.
   - Personalizes home screen greetings (*"नमस्ते, Daadaji 🙏"*), voice assistant dialogue, and caregiver monitoring records.
   - Stores all personal data locally in an encrypted Room database table (`user_profile`).

---

### 🎨 Modern Wellness Visual Design & Enhanced Contrast
- **Modern Palette**: Upgraded from dull earthen tones to a vibrant, dignified Royal Sapphire Navy (`#1E3A8A`), Radiant Teal (`#0D9488`), and Warm Golden Amber (`#D97706`).
- **High-Contrast Readability**: Pure pearl white card surfaces on clean slate background with ultra-deep ink typography (`#0F172A`) ensuring optimal legibility for elderly vision and low-light environments.
- **Accessible Touch Targets**: Generous 48dp+ buttons, rounded tactile cards, and prominent status pills for intuitive touch navigation.

---

### 🧠 Cognitive Brain Hub & Multi-Exercise Suite

SMRITI+ provides a neuro-adaptive suite of exercises specifically engineered for elderly users to stimulate working, semantic, and visual memory:

1. **Working Memory: Pattern Sequence Game**:
   - High-contrast, tactile colored tiles with gentle audio/visual cues
   - Memorize and reproduce expanding rhythmic sequences
   - Immediate feedback on accuracy, speed, and cognitive consistency
   - Real-time adaptive difficulty scaling (Levels 1 to 5)

2. **Episodic Association: Word & Picture Recall**:
   - Card-matching exercise featuring nostalgic, everyday items (*Tea Cup, Vintage Radio, Garden Flower, Story Book, Family Home, Heritage Train*)
   - Stimulates visual recognition pathways and semantic recall without countdown anxiety or time pressure
   - Encourages calming cognitive engagement

3. **Daily Cognitive Plan & Milestones**:
   - **Daily Practice Streaks**: Tracks consecutive days of brain wellness activities
   - **Personalized Daily Checklist**: Recommends short 2-3 minute cognitive routines
   - **Achievement Badges**: Unlocks milestone rewards (*First Step, Sharp Recall, Zen Master*) to motivate daily consistency

---

### 📖 Stored Memories & Reminiscence Life Timeline

Reminiscence therapy is a proven, non-pharmacological approach to reducing anxiety and stimulating long-term memory for older adults and individuals with memory loss.

- **Visual Chronological Spine**: Connects life stories with an aesthetic timeline spine, era tags, and milestone badges
- **Reminiscence Audio Playback**: Integrated Text-to-Speech reads life stories aloud in a gentle, warm voice
- **Life Categories**: Segregates memories across *Family, Milestones, Travel, Celebrations, Childhood, and Daily Joy*
- **Sentiment & Mood Indicators**: Captures emotional context (*Joyful, Heartwarming, Nostalgic, Proud, Peaceful*)
- **Instant Search & Archive**: Fast keyword search by people, places, or dates, with an accessible memory creation dialog

---

### 🤖 AI-Based Cognitive Analysis & Adaptive Engine

SMRITI+ analyzes gameplay performance using multiple indicators:

- 🎯 Accuracy
- ⚡ Reaction time
- ❌ Mistakes
- 📊 Recent scores
- 🧩 Current difficulty level
- 📈 Performance trends

The system generates a cognitive performance insight and recommends whether the difficulty should:

- Increase
- Decrease
- Remain stable

This creates a **personalized cognitive training experience** rather than a fixed difficulty game.

---

### 📈 Adaptive Difficulty Engine

The game dynamically adjusts difficulty based on the user's performance.

| Performance | System Response     |
| ----------- | ------------------- |
| Score ≥ 80  | Increase difficulty |
| Score 50–79 | Maintain difficulty |
| Score < 50  | Reduce difficulty   |

This allows the application to gradually challenge users without making the experience unnecessarily frustrating.

---

### 👨‍👩‍👧 Caregiver Dashboard

Caregivers can monitor important cognitive activity indicators through a dedicated dashboard.

The prototype displays:

- Average score
- Average accuracy
- Games completed
- Average reaction time
- Best score
- Current difficulty
- Recent performance
- Cognitive wellness status
- AI-generated cognitive insights
- Performance trends
- Medication/reminder information

The goal is to provide caregivers with **simple, understandable information rather than overwhelming clinical-style data.**

---

### 🎤 Voice Assistant

SMRITI+ provides voice interaction to make the application easier to use for elderly users.

Users can speak commands such as:

- "Start my memory exercise"
- "Show my reminders"
- "What medicines do I have?"
- "Tell me my performance"
- "Hello"

The prototype supports English and selected Hindi keywords.

The application uses speech recognition and text-to-speech to provide a more natural interaction experience.

---

### 🔔 Smart Reminders

The application provides an elderly-friendly reminder interface for everyday activities such as:

- 💊 Medication
- 💧 Drinking water
- 🏥 Doctor appointments
- 📅 Daily activities

The prototype currently demonstrates the reminder interface and interaction flow.

---

### 💾 Local Offline-First Persistence (Room Database)

All user data is stored securely and privately on-device using **Android Room Database** with reactive Kotlin Flow streaming:

The application tracks:

- 🧩 **Game Sessions**: Sequence scores, accuracy, reaction time, mistakes, timestamps
- 🏆 **Cognitive Metrics**: Running score averages, difficulty level, session count, clinical wellness trends
- 💊 **Medication & Hydration Reminders**: Names, schedules, categories, completion status
- 📖 **Stored Memories**: Life stories, milestone dates, category tags, emotional sentiment indicators

The system functions 100% offline without mandatory internet connectivity, safeguarding elderly privacy.

---

## 🏗️ System Architecture

```text
                 ┌─────────────────────────────────────────┐
                 │             SMRITI+ Platform            │
                 │   Modern Android (Jetpack Compose / M3) │
                 └────────────────────┬────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
        ▼                             ▼                             ▼
  ┌───────────┐                 ┌───────────┐                 ┌─────────────┐
  │ Cognitive │                 │   Voice   │                 │   Stored    │
  │ Brain Hub │                 │ Assistant │                 │  Memories   │
  │ (Sequences│                 │ (Speech & │                 │  Timeline   │
  │  & Recall)│                 │    TTS)   │                 │ (Reminisce) │
  └─────┬─────┘                 └─────┬─────┘                 └──────┬──────┘
        │                             │                              │
        └──────────────────────┬──────┴──────────────────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  SmritiViewModel     │
                    │  (StateFlow / MVVM)  │
                    └──────────┬───────────┘
                               │
                               ▼
        ┌──────────────────────────────────────────────┐
        │              Offline Data Layer              │
        │      Room Database (Entities, DAOs, Flow)    │
        │ ┌───────────┬─────────────┬────────────────┐ │
        │ │ Reminders │ GameSession │ StoredMemories │ │ │
        │ └───────────┴─────────────┴────────────────┘ │
        └──────────────────────┬───────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                ▼                             ▼
      ┌──────────────────┐          ┌───────────────────┐
      │ Adaptive Engine  │          │    Caregiver      │
      │ & AI Insights    │          │    Dashboard      │
      └──────────────────┘          └───────────────────┘
```

---

## 🛠️ Technology Stack

### Android & UI Architecture

- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose (Declarative UI)
- **Design System**: Material Design 3 (M3) with warm, high-contrast, elder-friendly accessibility
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **State Management**: Kotlin Coroutines & `MutableStateFlow` with lifecycle-aware collection
- **Navigation**: Navigation Compose with type-safe screen destinations

### Local Persistence & Offline-First

- **Database**: Android Room Database (SQLite abstraction)
- **Annotation Processing**: KSP (Kotlin Symbol Processing)
- **Data Entities**: `ReminderItem`, `GameSession`, `StoredMemory`, `CaregiverMetrics`
- **Reactive Streaming**: Kotlin Flow for immediate UI updates

### Cognitive Engine & Voice

- **Cognitive AI**: Neuro-adaptive difficulty adjustment, pattern error detection, and multi-metric scoring
- **Speech Recognition**: Android SpeechRecognizer integration
- **Audio Output**: Android Text-to-Speech (TTS) for reminiscence storytelling and conversational feedback

---

## 📊 Cognitive Performance Model

SMRITI+ currently calculates the overall performance score using:

```text
Overall Score =
    Accuracy × 50%
  + Speed × 30%
  + Consistency × 20%
```

This allows the system to consider more than simply whether the user completed the game.

### Example

```text
Accuracy       → 85%
Speed          → 72%
Consistency    → 90%

Overall Score  → 82%
```

The resulting score is then used by the adaptive difficulty engine.

---

## 🔄 Personalization Flow

```text
User plays cognitive game
          ↓
Performance recorded
          ↓
Accuracy + Speed + Mistakes
          ↓
AI analyzes performance
          ↓
Cognitive insight generated
          ↓
Difficulty adjusted
          ↓
Next exercise personalized
```

This creates a continuous feedback loop:

**Play → Analyze → Adapt → Improve**

---

## 📱 Application Modules

### Elderly User

- **Home Dashboard**: High-contrast, card-based navigation with greeting, date, and quick action cards
- **Cognitive Brain Hub**:
  - *Pattern Sequence*: Visual rhythm and working memory reproduction
  - *Word & Picture Recall*: Nostalgic object association matching
  - *Daily Brain Plan*: Daily streak tracker, exercise checklists, and milestone reward badges
- **Stored Memories Timeline**: Visual life story milestone cards, category filtering, sentiment tags, and reminiscence audio playback
- **Voice Assistant**: Natural voice interaction with intent-based navigation and bilingual cues
- **Smart Reminders**: Medication schedules, water reminders, and completion check-offs
- **Performance Feedback**: Immediate praise, clear round stats, and difficulty indicators

### Caregiver

- **Cognitive Wellness Overview**: Real-time cognitive score, accuracy, and clinical status
- **Performance Statistics**: Session logs, reaction time averages, mistake distributions
- **Recent Activity**: Chronological logs of cognitive sessions and completion metrics
- **AI Cognitive Insights**: Contextual advice, performance trends, and stability assessment
- **Medication & Reminder Monitoring**: Track missed or completed medication events

---

## 🚀 Getting Started

### Android (Native Kotlin & Jetpack Compose)

This project contains the modern native Android app built with Kotlin, Jetpack Compose, and Room Database under `/app`.

#### Prerequisites

- Android Studio Meerkat or newer
- JDK 17 or higher
- Android SDK 34/35

#### Build & Run via Gradle

To compile the Android application:

```bash
gradle :app:assembleDebug
```

To run automated unit tests:

```bash
gradle :app:testDebugUnitTest
```

---

## 🗺️ Roadmap

### ✅ Phase 1 — Foundation & Core Features

- [x] Elderly-friendly home screen with large touch targets (48dp+)
- [x] Working Memory Sequence exercise
- [x] Multi-metric performance scoring (Accuracy, Speed, Consistency)
- [x] Adaptive difficulty engine (Levels 1 to 5)
- [x] Caregiver monitoring dashboard with clinical indicators
- [x] AI-driven cognitive analysis & trend summaries
- [x] Voice assistant with voice input & text-to-speech feedback
- [x] Interactive medication & hydration reminders
- [x] Offline-first Room Database architecture

### ✅ Phase 2 — Intelligent Personalization & Reminiscence

- [x] **Cognitive Brain Hub**: Multi-exercise hub (Pattern Sequence + Word & Picture Recall)
- [x] **Word & Picture Association**: Card-matching exercise with nostalgic everyday objects
- [x] **Stored Memories Visual Timeline**: Life milestones with category filters & sentiment badges
- [x] **Reminiscence Audio Narration**: Text-to-speech audio storytelling for cherished memories
- [x] **Daily Brain Wellness Plan**: Daily practice streaks, routines, and milestone badges (*First Step, Sharp Recall, Zen Master*)

### ☁️ Phase 3 — Cloud & Caregiver Ecosystem

- [ ] Firebase / Cloud secure synchronization
- [ ] Caregiver remote multi-device access
- [ ] Cross-device notification alerts for missed medications
- [ ] Multi-lingual speech recognition (Hindi, Marathi, Tamil, Spanish)

### 🚀 Phase 4 — Clinical & Wearable Integration

- [ ] Smartwatch heart rate & step telemetry integration
- [ ] Longitudinal cognitive decline trend detection
- [ ] Exportable PDF clinical reports for healthcare providers
- [ ] Emergency SOS quick contact auto-dialer
- [ ] Large-scale deployment

---

## 🔐 Privacy & Safety

SMRITI+ is designed with elderly users and their privacy in mind.

The prototype prioritizes:

- Local performance storage
- Minimal data collection
- Simple user interaction
- Clear caregiver insights
- Offline-first design

> **Important:** SMRITI+ is a cognitive wellness and assistance platform. It is not intended to diagnose dementia, Alzheimer's disease, or any other medical condition.

---

## 🎯 Hackathon Context

SMRITI+ is developed as a solution for **Smart India Hackathon (SIH)** under the elderly-care and cognitive assistance problem domain.

The project focuses on combining:

**AI + Accessibility + Cognitive Wellness + Voice Interaction + Caregiver Monitoring**

into a single practical platform.

---

## 👨‍💻 Project

**SMRITI+**

AI-Powered Cognitive & Memory Assistance Platform for Elderly Care

Built with ❤️ using Flutter and AI-driven personalization.

---

## 📄 License

This project is currently developed as a prototype for educational and hackathon purposes.

---

## ⭐ Support the Project

If you find SMRITI+ interesting, consider giving the repository a ⭐ on GitHub.

**Repository:**
https://github.com/vaiiiibhav-pixel/smriti-plus

---

### 💡 SMRITI+

> **Remember. Engage. Connect.**
>
> _Technology that helps care remember._
