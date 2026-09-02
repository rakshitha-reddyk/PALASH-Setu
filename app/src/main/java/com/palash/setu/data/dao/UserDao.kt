package com.palash.setu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.palash.setu.data.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserEntity?

    @Insert
    suspend fun insert(user: UserEntity)
}
