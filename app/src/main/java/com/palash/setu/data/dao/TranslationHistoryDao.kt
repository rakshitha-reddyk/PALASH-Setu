package com.palash.setu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.palash.setu.data.entity.TranslationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: String): Flow<List<TranslationHistory>>

    @Insert
    suspend fun insert(history: TranslationHistory)
}
