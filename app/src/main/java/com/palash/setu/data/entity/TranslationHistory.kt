package com.palash.setu.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translation_history",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TranslationHistory(
    @PrimaryKey val id: String,
    val userId: String,
    val sourceHindiText: String,
    val targetLanguage: String,
    val translatedNativeText: String,
    val translatedDevanagariText: String,
    val audioDurationMs: Int,
    val processingLatencyMs: Int,
    val isDictionaryExactMatch: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
