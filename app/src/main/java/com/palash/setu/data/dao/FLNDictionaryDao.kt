package com.palash.setu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.palash.setu.data.entity.FLNDictionary
import kotlinx.coroutines.flow.Flow

@Dao
interface FLNDictionaryDao {
    @Query("SELECT * FROM fln_dictionary WHERE hindiTerm LIKE :query ORDER BY hindiTerm LIMIT 50")
    fun search(query: String): Flow<List<FLNDictionary>>

    @Query("SELECT * FROM fln_dictionary WHERE hindiTerm = :term LIMIT 1")
    suspend fun findExact(term: String): FLNDictionary?

    @Query("SELECT * FROM fln_dictionary WHERE hindiTerm LIKE :query LIMIT 1")
    suspend fun findPartial(query: String): List<FLNDictionary>

    @Query("SELECT COUNT(*) FROM fln_dictionary")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(terms: List<FLNDictionary>)
}
