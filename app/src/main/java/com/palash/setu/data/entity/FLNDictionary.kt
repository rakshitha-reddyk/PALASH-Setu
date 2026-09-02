package com.palash.setu.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fln_dictionary",
    indices = [Index(value = ["hindiTerm"])]
)
data class FLNDictionary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hindiTerm: String,
    val santhaliOlChiki: String,
    val santhaliDevanagari: String,
    val hoWarangChiti: String,
    val hoDevanagari: String,
    val mundariDevanagari: String,
    val domainCategory: String,
    val gradeLevel: Int
)
