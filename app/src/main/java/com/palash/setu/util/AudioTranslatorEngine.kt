package com.palash.setu.util

import com.palash.setu.data.dao.FLNDictionaryDao
import com.palash.setu.data.entity.FLNDictionary
import kotlinx.coroutines.delay

interface AudioTranslatorEngine {
    suspend fun translate(hindiText: String, targetLanguage: String): TranslationResult
}

data class TranslationResult(
    val sourceHindi: String,
    val nativeText: String,
    val devanagariText: String,
    val processingLatencyMs: Int,
    val isDictionaryExactMatch: Boolean
)

class MockAudioTranslatorEngine(
    private val dictionaryDao: FLNDictionaryDao
) : AudioTranslatorEngine {
    override suspend fun translate(hindiText: String, targetLanguage: String): TranslationResult {
        val normalizedText = hindiText.trim()
        
        // 1. Try Exact Match
        val exactMatch = dictionaryDao.findExact(normalizedText)
        if (exactMatch != null) {
            return mapResult(normalizedText, exactMatch, targetLanguage)
        }

        // 2. Try Partial Match (e.g., if user says "एक किताब" and we have "किताब")
        val partialMatches = dictionaryDao.findPartial("%$normalizedText%")
        if (partialMatches.isNotEmpty()) {
            return mapResult(normalizedText, partialMatches.first(), targetLanguage)
        }

        // 3. Fallback for "No Match Found"
        return TranslationResult(
            sourceHindi = normalizedText,
            nativeText = "[No Match Found]",
            devanagariText = normalizedText,
            processingLatencyMs = 100,
            isDictionaryExactMatch = false
        )
    }

    private fun mapResult(source: String, match: FLNDictionary, target: String): TranslationResult {
        return when (target) {
            "Ho" -> TranslationResult(source, match.hoWarangChiti, match.hoDevanagari, 5, true)
            "Mundari" -> TranslationResult(source, match.mundariDevanagari, match.mundariDevanagari, 5, true)
            else -> TranslationResult(source, match.santhaliOlChiki, match.santhaliDevanagari, 5, true)
        }
    }
}
