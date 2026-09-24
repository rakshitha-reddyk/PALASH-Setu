# PALASH-Setu (पलाश सेतु) 🌿🌉

> **Bridging Tribal Languages with Foundational Literacy through AI-Powered Vernacular Pedagogy and Real-Time Translation**

---

## 📌 Project Overview

**PALASH-Setu** is an offline-first, Android-based educational platform. It addresses the language barrier faced by non-native (e.g., Hindi-medium) teachers instructing primary school students in tribal regions.

By unifying real-time voice translation, mother-tongue-based Foundational Literacy and Numeracy (FLN) pedagogy, interactive phonics (such as Ol Chiki script), and automatic bilingual worksheet creation, PALASH-Setu ensures no child is left behind due to language barriers.

---

## 🚀 Key Features

* **🎙️ Real-Time Voice & Text Translation:** Converts Hindi teacher speech/text into native tribal language text and speech output with a target latency of $\le 3$ seconds.
* **📚 Vernacular FLN Pedagogy:** Delivers bilingual lesson scripts, local vocabulary guides, interactive soundboards, and Ol Chiki phonics.
* **📄 Automated Bilingual Worksheet Generator:** Automatically compiles and generates print-ready PDF worksheets (Hindi + Tribal language) locally on the device.
* **📶 Offline-First Architecture:** Operates seamlessly in remote rural areas without internet access using local databases, preloaded audio, and embedded translation logic.
* **📱 Ultra-Lightweight & Low Hardware Requirements:** Optimized for low-cost Android tablets running **Android 9+ with 2 GB RAM**.

---

## 🛠️ Supported Languages

Designed with a modular architecture to scale across multiple tribal languages:
* **Santhali** (with Ol Chiki script support)
* **Ho**
* **Mundari**

---

## 🧰 Tech Stack

• Platform & Language: Android Native, Kotlin
• UI & Architecture: Jetpack Compose, Material 3, MVVM + StateFlow
• Database & Security: Room (SQLite), EncryptedSharedPreferences
• Speech & Audio: Native RecognizerIntent, TextToSpeech, MediaPlayer
• Build Tools: Gradle

---

## 📂 Repository Structure

```files
PALASH-Setu/
├── app/
│   ── src/
│      ├── main/
│      │   ├── java/           # Core translation, database, and UI logic
│      │   ├── res/            # Audio assets, layouts, and static FLN dictionaries
│      │   └── AndroidManifest.xml
│      └── build.gradle
├── build.gradle
└── README.md

```

---

⚡ Quick Start & Installation

### Prerequisites
* Android Studio (Hedgehog | 2023.1.1 or higher)
* Android SDK API Level 28 (Android 9.0 Pie) or higher

### Installation Steps

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/rakshitha-reddyk/PALASH-Setu.git](https://github.com/rakshitha-reddyk/PALASH-Setu.git)
   cd PALASH-Setu

2. Open in Android Studio:

Launch Android Studio and choose Open an Existing Project.

Navigate to and select the cloned PALASH-Setu directory.

3. Build & Sync:

Let Gradle sync the project dependencies.

Select a target emulator or connected physical Android device (min 2 GB RAM).

4. Run Project:

Click Run (Shift + F10) to compile and deploy the APK directly to the device.


---

🎯 Policy Alignment & Impact

* **NIPUN Bharat Mission:** Direct alignment with foundational literacy goals across primary schools.
* **National Education Policy (NEP 2020):** Supports Mother Tongue-Based Multilingual Education (MTB-MLE) in early learning.
* **Scalability:** Built with a modular language framework allowing simple integration of additional regional/tribal dialects across India.
