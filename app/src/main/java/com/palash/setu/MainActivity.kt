package com.palash.setu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.palash.setu.data.PalashDatabase
import com.palash.setu.ui.navigation.PalashNavGraph
import com.palash.setu.ui.theme.PalashSetuTheme
import com.palash.setu.util.LocalPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = LocalPreferences(this)
        val database = PalashDatabase.getInstance(this)
        setContent {
            PalashSetuTheme {
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
