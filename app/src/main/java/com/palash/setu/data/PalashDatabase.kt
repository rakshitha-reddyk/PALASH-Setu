package com.palash.setu.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.data.dao.GeneratedWorksheetDao
import com.palash.setu.data.dao.TranslationHistoryDao
import com.palash.setu.data.dao.UserDao
import com.palash.setu.data.entity.FLNDictionary
import com.palash.setu.data.entity.GeneratedWorksheet
import com.palash.setu.data.entity.TranslationHistory
import com.palash.setu.data.entity.UserEntity
import com.palash.setu.data.seed.FLNSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, FLNDictionary::class, TranslationHistory::class, GeneratedWorksheet::class],
    version = 1,
    exportSchema = true
)
abstract class PalashDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun flnDictionaryDao(): FLNDictionaryDao
    abstract fun translationHistoryDao(): TranslationHistoryDao
    abstract fun generatedWorksheetDao(): GeneratedWorksheetDao

    companion object {
        @Volatile
        private var instance: PalashDatabase? = null

        fun getInstance(context: Context): PalashDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PalashDatabase::class.java,
                "palash_setu.db"
            ).build().also { database ->
                instance = database
                CoroutineScope(Dispatchers.IO).launch {
                    if (database.flnDictionaryDao().count() == 0) {
                        database.flnDictionaryDao().insertAll(FLNSeedData.coreTerms)
                    }
                }
            }
        }
    }
}
