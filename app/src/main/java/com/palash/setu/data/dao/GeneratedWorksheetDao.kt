package com.palash.setu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.palash.setu.data.entity.GeneratedWorksheet
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedWorksheetDao {
    @Query("SELECT * FROM generated_worksheets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: String): Flow<List<GeneratedWorksheet>>

    @Insert
    suspend fun insert(worksheet: GeneratedWorksheet)
}
