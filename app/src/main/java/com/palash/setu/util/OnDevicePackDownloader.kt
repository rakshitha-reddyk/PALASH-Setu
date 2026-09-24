package com.palash.setu.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.ModelDownloadListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * API 34+ only: triggers the system download of the Hindi on-device speech
 * model with progress/success/error callbacks.
 *
 * This class directly references [ModelDownloadListener], which does not
 * exist below API 34. It MUST only ever be loaded on API 34+ devices —
 * the sole call site ([OnDeviceHindiStt.requestModelDownload]) checks
 * `Build.VERSION.SDK_INT >= 34` first. Keeping it in its own file keeps
 * every other launch path (including API 33) completely free of that
 * reference, so the app starts normally everywhere.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal object OnDevicePackDownloader {

    fun trigger(
        context: Context,
        recognizer: SpeechRecognizer,
        intent: Intent,
        onProgress: (Int) -> Unit,
        onScheduled: () -> Unit,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ): Boolean {
        return try {
            recognizer.triggerModelDownload(
                intent,
                context.mainExecutor,
                object : ModelDownloadListener {
                    override fun onProgress(progress: Int) = onProgress(progress)
                    override fun onScheduled() = onScheduled()
                    override fun onSuccess() = onSuccess()
                    override fun onError(error: Int) = onError(error)
                }
            )
            true
        } catch (e: Exception) {
            Log.w("OnDevicePackDownloader", "triggerModelDownload failed", e)
            onError(-1)
            false
        }
    }
}
