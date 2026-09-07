# PALASH-Setu (पलाश सेतु) 🌿🌉

> **Bridging Tribal Languages with Foundational Literacy through AI-Powered Vernacular Pedagogy and Real-Time Translation**

---

## 📌 Project Overview

**PALASH-Setu** is an offline-first, Android-based educational platform developed for **Smart India Hackathon 2026**. It addresses the language barrier faced by non-native (e.g., Hindi-medium) teachers instructing primary school students in tribal regions[cite: 1]. 

By unifying real-time voice translation, mother-tongue-based Foundational Literacy and Numeracy (FLN) pedagogy, interactive phonics (such as Ol Chiki script), and automatic bilingual worksheet creation, PALASH-Setu ensures no child is left behind due to language barriers[cite: 1].

* **Problem Statement ID:** 26042[cite: 1]
* **Problem Statement Title:** AI Powered Vernacular Pedagogy and Real-Time Translation Tool For Mother Tongue-Based Primary Education[cite: 1]
* **Category / Theme:** Software / Smart Education[cite: 1]
* **Team ID & Name:** Team 45 - `HACKERS`[cite: 1]

---

## 🚀 Key Features

* **🎙️ Real-Time Voice & Text Translation:** Converts Hindi teacher speech/text into native tribal language text and speech output with a target latency of $\le 3$ seconds[cite: 1].
* **📚 Vernacular FLN Pedagogy:** Delivers bilingual lesson scripts, local vocabulary guides, interactive soundboards, and Ol Chiki phonics[cite: 1].
* **📄 Automated Bilingual Worksheet Generator:** Automatically compiles and generates print-ready PDF worksheets (Hindi + Tribal language) locally on the device[cite: 1].
* **📶 Offline-First Architecture:** Operates seamlessly in remote rural areas without internet access using local databases, preloaded audio, and embedded translation logic[cite: 1].
* **📱 Ultra-Lightweight & Low Hardware Requirements:** Optimized for low-cost Android tablets running **Android 9+ with 2 GB RAM**[cite: 1].

---

## 🛠️ Supported Languages

Designed with a modular architecture to scale across multiple tribal languages:
* **Santhali** (with Ol Chiki script support)[cite: 1]
* **Ho**[cite: 1]
* **Mundari**[cite: 1]

---

## 🧰 Tech Stack

* **Platform:** Android Native
* **Language:** Java / Kotlin
* **Build / Native Tools:** CMake, C++ (for optimized native processing)
* **Database & Persistence:** Room Database (SQLite Engine)
* **Speech Recognition:** Android `RecognizerIntent` / Local ASR Processing
* **Document Engine:** Android Native PDF Generation Pipeline

---

## 📂 Repository Structure

```files
PALASH-Setu/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── cpp/            # Native C++/CMake compilation scripts
│   │   │   ├── java/           # Core translation, database, and UI logic
│   │   │   ├── res/            # Audio assets, layouts, and static FLN dictionaries
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
├── docs/
│   └── SIH2026-Presentation.pdf # Project Presentation Deck
├── CMakeLists.txt
├── build.gradle
└── README.md
```
⚡ Quick Start & Installation
Prerequisites
Android Studio (Hedgehog | 2023.1.1 or higher)

Android SDK API Level 28 (Android 9.0 Pie) or higher[cite: 1]

NDK & CMake (configured within Android Studio for native component compilation)[cite: 1]

Installation Steps
Clone the Repository:
git clone [https://github.com/rakshitha-reddyk/PALASH-Setu.git](https://github.com/rakshitha-reddyk/PALASH-Setu.git)
cd PALASH-Setu
Open in Android Studio:

Launch Android Studio and choose Open an Existing Project.

Navigate to and select the cloned PALASH-Setu directory.

Build & Sync:

Let Gradle sync dependencies and CMake resolve native C++ links[cite: 1].

Select a target emulator or connected physical Android device (min 2 GB RAM)[cite: 1].

Run Project:

Click Run (Shift + F10) to compile and deploy the APK directly to the device.

🎯 Policy Alignment & Impact
NIPUN Bharat Mission: Direct alignment with foundational literacy goals across primary schools[cite: 1].

National Education Policy (NEP): Supports Mother Tongue-Based Multilingual Education (MTB-MLE) in early learning[cite: 1].

Scalability: Built with a modular language framework allowing simple integration of additional regional/tribal dialects across India[cite: 1].
