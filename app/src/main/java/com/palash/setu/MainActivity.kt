package com.palash.setu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.palash.setu.data.PalashDatabase
import com.palash.setu.ui.navigation.PalashNavGraph
import com.palash.setu.ui.theme.PalashSetuTheme
import com.palash.setu.util.LocalPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalashSetuTheme {
                // Normal app launch: voice-model setup is a background state
                // of the translator screen, never a gate.
                val preferences = remember { LocalPreferences(this) }
                val database = remember { PalashDatabase.getInstance(this) }
                PalashNavGraph(
                    preferences = preferences,
                    dictionaryDao = database.flnDictionaryDao(),
                    worksheetDao = database.generatedWorksheetDao(),
                    userDao = database.userDao()
                )
            }
        }
    }
}
