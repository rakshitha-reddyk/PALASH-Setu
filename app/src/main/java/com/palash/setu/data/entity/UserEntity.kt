package com.palash.setu.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val schoolCode: String,
    val teacherName: String,
    val district: String,
    val block: String,
    val preferredTargetLanguage: String,
    val createdAt: Long = System.currentTimeMillis(),
    val role: String = "teacher"
)
