package com.palash.setu.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Platform on-device Hindi speech-to-text. Everything here is guaranteed
 * offline: recognition runs through [SpeechRecognizer.createOnDeviceSpeechRecognizer]
 * (never the cloud recognizer, never a recognition Activity), and the Hindi
 * voice pack is provisioned through the system recognition service via
 * [SpeechRecognizer.triggerModelDownload]. No app-side models, no downloads
 * by the app itself.
 */
object OnDeviceHindiStt {
    const val LANGUAGE = "hi-IN"

    /** On-device recognizer APIs exist from Android 12 (API 31). */
    fun apiAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * Device-level check. True means the device supports on-device
     * recognition; it does NOT guarantee the Hindi pack is downloaded —
     * a missing pack surfaces as ERROR_LANGUAGE_NOT_SUPPORTED /
     * ERROR_LANGUAGE_UNAVAILABLE at recognition time.
     */
    fun isOnDeviceRecognitionAvailable(context: Context): Boolean {
        if (!apiAvailable()) return false
        return try {
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        } catch (e: Exception) {
            Log.w("OnDeviceHindiStt", "availability check failed", e)
            false
        }
    }

    /**
     * Exclusively on-device recognizer instance. Null when the platform API
     * is missing or construction fails. Callers must never substitute the
     * cloud recognizer or a RecognizerIntent Activity.
     */
    fun createRecognizer(context: Context): SpeechRecognizer? {
        if (!apiAvailable()) return null
        return try {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } catch (e: Exception) {
            Log.w("OnDeviceHindiStt", "on-device recognizer unavailable", e)
            null
        }
    }

    /** Result of the Hindi on-device pack presence check. */
    enum class HindiPackState {
        /** Hindi is installed and ready — never trigger a download. */
        INSTALLED,
        /** Hindi is not installed — a download is needed. */
        MISSING,
        /** Could not determine (old API, or check failed) — caller decides. */
        UNKNOWN
    }

    /**
     * Checks whether the Hindi on-device model is actually installed
     * (API 33+ via checkRecognitionSupport). This is NOT the same as
     * [isOnDeviceRecognitionAvailable], which only reports service
     * existence. Never triggers a download. On API < 33 there is no
     * presence API, so the result is always [HindiPackState.UNKNOWN].
     */
    fun checkHindiPack(
        context: Context,
        recognizer: SpeechRecognizer,
        onResult: (HindiPackState) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onResult(HindiPackState.UNKNOWN)
            return
        }
        OnDeviceRecognitionCheck.query(
            context,
            recognizer,
            recognitionIntent(),
            LANGUAGE,
            onResult
        )
    }

    /** Hindi free-form intent with interim results. No EXTRA_PREFER_OFFLINE:
     *  the recognizer itself is on-device-only, so no cloud switch is possible. */
    fun recognitionIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Keep the session open until the user actually pauses/stops speaking.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
        }

    fun unavailableMessage(): String =
        if (!apiAvailable()) {
            "On-device Hindi voice needs Android 12 or newer. " +
                "You can type the Hindi text below instead."
        } else {
            "On-device Hindi voice isn't supported by this device's recognition service. " +
                "You can type the Hindi text below instead."
        }

    /**
     * Asks the system recognition service to download the Hindi on-device
     * model. Two paths:
     * - API 34+: listener overload with progress/success/error callbacks.
     *   The listener lives in [OnDevicePackDownloader], which is only
     *   class-loaded inside the version-guarded branch below.
     * - API 33: fire-and-forget [SpeechRecognizer.triggerModelDownload]
     *   overload; reported as scheduled since progress isn't observable.
     * Returns false when the trigger API doesn't exist (API < 33).
     * This object itself never references the API 34-only listener type,
     * so the launch path stays crash-free on older devices.
     */
    fun requestModelDownload(
        context: Context,
        recognizer: SpeechRecognizer,
        onProgress: (Int) -> Unit,
        onScheduled: () -> Unit,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return OnDevicePackDownloader.trigger(
                context,
                recognizer,
                recognitionIntent(),
                onProgress,
                onScheduled,
                onSuccess,
                onError
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return try {
                recognizer.triggerModelDownload(recognitionIntent())
                onScheduled()
                true
            } catch (e: Exception) {
                Log.w("OnDeviceHindiStt", "triggerModelDownload failed", e)
                onError(-1)
                false
            }
        }
        return false
    }

    /** True when recognition failed specifically because Hindi is missing —
     *  the only case that should offer a pack download, never cloud. */
    fun isHindiPackMissing(error: Int): Boolean =
        error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
            error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE

    fun errorMessage(error: Int): String = when {
        error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission is needed for voice input. " +
                "You can type the Hindi text below instead."
        isHindiPackMissing(error) ->
            "The Hindi offline voice pack isn't installed on this device. " +
                "Download it below — voice stays fully offline, no cloud is used."
        error == SpeechRecognizer.ERROR_NO_MATCH ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "Didn't catch that — try again, or type the Hindi text below."
        error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Voice recognizer is busy. Wait a moment and try again."
        else ->
            "Voice input failed. Try again, or type the Hindi text below."
    }
}
