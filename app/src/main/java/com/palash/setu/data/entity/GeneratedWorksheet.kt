package com.palash.setu.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "generated_worksheets",
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
data class GeneratedWorksheet(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val targetLanguage: String,
    val nipunCompetencyCode: String,
    val pdfLocalFilePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
