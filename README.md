# PALASH-Setu

Offline-first Android Kotlin foundation for the PALASH MTB-MLE classroom co-pilot.

## Phase 1/2 structure

```text
PALASH-Setu/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/palash/setu/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── PalashDatabase.kt
│       │   │   ├── dao/
│       │   │   │   ├── FLNDictionaryDao.kt
│       │   │   │   ├── GeneratedWorksheetDao.kt
│       │   │   │   ├── TranslationHistoryDao.kt
│       │   │   │   └── UserDao.kt
│       │   │   ├── entity/
│       │   │   │   ├── FLNDictionary.kt
│       │   │   │   ├── GeneratedWorksheet.kt
│       │   │   │   ├── TranslationHistory.kt
│       │   │   │   └── UserEntity.kt
│       │   │   └── seed/FLNSeedData.kt
│       │   ├── native/
│       │   ├── ui/
│       │   └── util/
│       └── res/
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## Room database

`PalashDatabase` uses `palash_setu.db`, enforces foreign keys from history and worksheets to local users, and inserts 20 core FLN terms on first access. Seed values are local and never require network access.

Build with Android Studio or a machine with JDK 17 and the Gradle wrapper/tooling available. The current workspace does not include a Gradle wrapper yet, and `gradle`/`java` are not available on the current PATH.
