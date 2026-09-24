package com.palash.setu.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * API 33+ only: queries whether the Hindi on-device model is actually
 * installed, via SpeechRecognizer#checkRecognitionSupport.
 *
 * This class directly references [RecognitionSupport] and
 * [RecognitionSupportCallback], which do not exist below API 33. It MUST
 * only ever be loaded on API 33+ devices — the sole call site
 * ([OnDeviceHindiStt.checkHindiPack]) checks the SDK level first.
 * This is the real pack-presence check: it must not be confused with
 * [SpeechRecognizer.isOnDeviceRecognitionAvailable], which only reports
 * whether an on-device recognition *service* exists.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal object OnDeviceRecognitionCheck {

    private const val TAG = "VoicePack"

    fun query(
        context: Context,
        recognizer: SpeechRecognizer,
        intent: Intent,
        languageTag: String,
        onResult: (OnDeviceHindiStt.HindiPackState) -> Unit
    ) {
        try {
            recognizer.checkRecognitionSupport(
                intent,
                context.mainExecutor,
                object : RecognitionSupportCallback {
                    override fun onSupportResult(support: RecognitionSupport) {
                        val installed = support.installedOnDeviceLanguages.orEmpty()
                        Log.i(
                            TAG,
                            "support check: installed=$installed " +
                                "supported=${support.supportedOnDeviceLanguages.orEmpty()} " +
                                "pending=${support.pendingOnDeviceLanguages.orEmpty()} " +
                                "online=${support.onlineLanguages.orEmpty()}"
                        )
                        if (matchesLanguage(installed, languageTag)) {
                            onResult(OnDeviceHindiStt.HindiPackState.INSTALLED)
                        } else {
                            onResult(OnDeviceHindiStt.HindiPackState.MISSING)
                        }
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "support check failed, error=$error")
                        onResult(OnDeviceHindiStt.HindiPackState.UNKNOWN)
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "support check threw", e)
            onResult(OnDeviceHindiStt.HindiPackState.UNKNOWN)
        }
    }

    private fun matchesLanguage(tags: List<String>, want: String): Boolean {
        val wantLang = want.substringBefore('-').substringBefore('_')
        return tags.any { tag ->
            tag.equals(want, ignoreCase = true) ||
                tag.substringBefore('-').substringBefore('_').equals(wantLang, ignoreCase = true)
        }
    }
}
