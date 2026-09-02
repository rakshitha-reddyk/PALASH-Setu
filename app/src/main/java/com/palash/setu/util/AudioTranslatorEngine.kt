package com.palash.setu.util

import com.palash.setu.data.dao.FLNDictionaryDao
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
        val exactMatch = dictionaryDao.findExact(normalizedText)
        if (exactMatch != null) {
            return when (targetLanguage) {
                "Ho" -> TranslationResult(normalizedText, exactMatch.hoWarangChiti, exactMatch.hoDevanagari, 5, true)
                "Mundari" -> TranslationResult(normalizedText, exactMatch.mundariDevanagari, exactMatch.mundariDevanagari, 5, true)
                else -> TranslationResult(normalizedText, exactMatch.santhaliOlChiki, exactMatch.santhaliDevanagari, 5, true)
            }
        }

        delay(1_500)
        return TranslationResult(
            sourceHindi = normalizedText,
            nativeText = "अनुवाद उपलब्ध होगा",
            devanagariText = "अनुवाद उपलब्ध होगा",
            processingLatencyMs = 1_500,
            isDictionaryExactMatch = false
        )
    }
}
