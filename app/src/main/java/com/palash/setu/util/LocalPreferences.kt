package com.palash.setu.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class LocalPreferences(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        "palash_teacher_preferences",
        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val isOnboarded: Boolean get() = preferences.getBoolean(KEY_ONBOARDED, false)
    val targetLanguage: String get() = preferences.getString(KEY_LANGUAGE, "Santhali") ?: "Santhali"
    val teacherId: String get() = preferences.getString(KEY_TEACHER_ID, "") ?: ""

    fun saveTeacherSetup(teacherId: String, district: String, block: String, language: String) {
        preferences.edit()
            .putBoolean(KEY_ONBOARDED, true)
            .putString(KEY_TEACHER_ID, teacherId)
            .putString(KEY_DISTRICT, district.trim())
            .putString(KEY_BLOCK, block.trim())
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    companion object {
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_TEACHER_ID = "teacher_id"
        private const val KEY_DISTRICT = "district"
        private const val KEY_BLOCK = "block"
        private const val KEY_LANGUAGE = "target_language"
    }
}
