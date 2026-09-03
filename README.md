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

### 🧠 Adaptive Memory Games

SMRITI+ includes interactive cognitive exercises designed to encourage memory and attention.

The current prototype features a **Memory Sequence Game** where users:

- Observe a sequence of colored blocks
- Memorize the sequence
- Reproduce it by tapping the blocks
- Receive an instant performance score
- Get feedback based on accuracy, speed, and consistency

The difficulty automatically adapts according to performance.

---

### 🤖 AI-Based Cognitive Analysis

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

### 💾 Local Performance Tracking

User performance is stored locally using `shared_preferences`.

The application tracks:

- Game scores
- Accuracy
- Reaction time
- Mistakes
- Difficulty
- Recent performance history

The prototype maintains recent game history and uses it for personalized analysis.

---

## 🏗️ System Architecture

```text
                 ┌─────────────────────────┐
                 │       SMRITI+ App       │
                 │       Flutter UI        │
                 └────────────┬────────────┘
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
        ┌───────────┐   ┌────────────┐   ┌─────────────┐
        │ Cognitive │   │   Voice    │   │  Reminders  │
        │   Games   │   │ Assistant  │   │             │
        └─────┬─────┘   └────────────┘   └─────────────┘
              │
              ▼
       ┌────────────────┐
       │ Performance    │
       │   Tracking     │
       └───────┬────────┘
               │
               ▼
       ┌────────────────┐
       │ Cognitive AI   │
       │    Analysis    │
       └───────┬────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
┌──────────────┐  ┌───────────────┐
│ Adaptive     │  │   Caregiver   │
│ Difficulty   │  │   Dashboard   │
└──────────────┘  └───────────────┘
```

---

## 🛠️ Technology Stack

### Frontend

- **Flutter**
- **Dart**
- Material Design

### AI / Logic

- Adaptive difficulty algorithm
- Cognitive performance scoring
- Rule-based AI analysis
- Performance trend analysis

### Voice

- `speech_to_text`
- `flutter_tts`

### Local Storage

- `shared_preferences`

### Planned Backend

- Python
- FastAPI
- SQLite
- Firebase / Cloud synchronization

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

- Home Dashboard
- Memory Games
- Voice Assistant
- Reminders
- Performance Feedback

### Caregiver

- Cognitive Wellness Overview
- Performance Statistics
- Recent Activity
- AI Cognitive Insights
- Performance Trends
- Medication & Reminder Monitoring

---

## 🚀 Getting Started

### Prerequisites

Make sure you have:

- Flutter SDK
- Dart SDK
- Android Studio
- Android SDK
- Android device or emulator

Check Flutter installation:

```bash
flutter doctor
```

---

### Clone the Repository

```bash
git clone https://github.com/vaiiiibhav-pixel/smriti-plus.git
```

Navigate into the project:

```bash
cd smriti-plus
```

---

### Install Dependencies

```bash
flutter pub get
```

---

### Run the Application

For a connected Android device:

```bash
flutter run
```

For Windows:

```bash
flutter run -d windows
```

For Chrome:

```bash
flutter run -d chrome
```

---

## 📦 Build APK

To generate a debug APK:

```bash
flutter build apk --debug
```

The generated APK will be available at:

```text
build/app/outputs/flutter-apk/app-debug.apk
```

For a release build:

```bash
flutter build apk --release
```

---

## 🧪 Testing

Run Flutter tests with:

```bash
flutter test
```

Analyze the project with:

```bash
flutter analyze
```

---

## 🗺️ Roadmap

### ✅ Phase 1 — Prototype

- [x] Elderly-friendly home screen
- [x] Memory Sequence game
- [x] Performance scoring
- [x] Adaptive difficulty
- [x] Local performance storage
- [x] Caregiver dashboard
- [x] AI cognitive analysis
- [x] Voice assistant
- [x] Text-to-speech
- [x] Reminder interface
- [x] Android prototype

### 🔄 Phase 2 — Intelligent Personalization

- [ ] More cognitive games
- [ ] Advanced AI personalization
- [ ] Long-term cognitive trends
- [ ] Personalized daily exercise plans
- [ ] More regional languages
- [ ] Improved voice interaction

### ☁️ Phase 3 — Cloud & Caregiver Ecosystem

- [ ] FastAPI backend
- [ ] Firebase integration
- [ ] Secure cloud synchronization
- [ ] Caregiver accounts
- [ ] Multi-user support
- [ ] Cross-device synchronization
- [ ] Offline-first synchronization

### 🚀 Phase 4 — Advanced Platform

- [ ] More sophisticated cognitive assessment
- [ ] Personalized wellness recommendations
- [ ] Advanced caregiver analytics
- [ ] Wearable/device integration
- [ ] Doctor/care-team integration
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
