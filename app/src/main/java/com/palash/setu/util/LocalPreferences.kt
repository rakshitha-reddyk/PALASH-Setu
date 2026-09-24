package com.palash.setu.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class LocalPreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "palash_teacher_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val isOnboarded: Boolean get() = preferences.getBoolean(KEY_ONBOARDED, false)
    val isVoicePackReady: Boolean get() = preferences.getBoolean(KEY_VOICE_PACK, false)
    val targetLanguage: String get() = preferences.getString(KEY_LANGUAGE, "Santhali") ?: "Santhali"
    val teacherId: String get() = preferences.getString(KEY_TEACHER_ID, "") ?: ""

    fun setVoicePackReady(ready: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_PACK, ready).apply()
    }

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
        private const val KEY_VOICE_PACK = "voice_pack_ready"
    }
}
