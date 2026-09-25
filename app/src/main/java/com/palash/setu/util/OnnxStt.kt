package com.palash.setu.util

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.sqrt

object OnnxStt {
    const val NO_MATCH_MESSAGE = "Didn't catch that — try again, or type the Hindi text below."
    const val FAILED_MESSAGE = "Voice input failed on this device — type the Hindi text below."
    const val DOWNLOAD_FAILED_MESSAGE = "Speech model download failed. Check your connection and retry, or type the Hindi text below."
    const val UNAVAILABLE_MESSAGE = "Speech model failed to load on this device. Try again, or type the Hindi text below."

    private const val TAG = "OnnxStt"
    private const val MODEL_URL = "https://huggingface.co/parismitaglobalsolutions/indicconformer-sherpa-onnx/resolve/main/hi/model.int8.onnx?download=true"
    private const val MODEL_DIRECTORY = "stt"
    private const val MODEL_FILE_NAME = "indicconformer_hi_int8.onnx"
    private const val SAMPLE_RATE = 16000
    private const val SPEECH_RMS_THRESHOLD = 0.01f
    private const val SILENCE_END_MS = 2500L
    private const val NO_SPEECH_TIMEOUT_MS = 8000L
    private const val MAX_RECORD_MS = 30000L
    private const val MIN_SPEECH_SAMPLES = SAMPLE_RATE * 3 / 10

    interface Listener {
        fun onAmplitude(value: Float)
        fun onResult(text: String)
        fun onListeningEnd()
        fun onError(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val sttExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "onnx-stt")
    }
    private val downloadExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "onnx-stt-download")
    }
    private val downloadRunning = AtomicBoolean(false)

    @Volatile
    private var recording = false

    @Volatile
    private var cancelRequested = false

    fun modelFile(context: Context): File =
        File(File(context.filesDir, MODEL_DIRECTORY), MODEL_FILE_NAME)

    fun isModelInstalled(context: Context): Boolean =
        modelFile(context).let { it.exists() && it.length() > 0 }

    fun start(context: Context, listener: Listener) {
        if (recording) return
        if (!isModelInstalled(context)) {
            mainHandler.post { listener.onError(NO_MATCH_MESSAGE) }
            return
        }
        recording = true
        cancelRequested = false
        sttExecutor.execute { record(context, listener) }
    }

    fun stopListening() {
        recording = false
    }

    fun cancel() {
        recording = false
        cancelRequested = true
    }

    fun destroy() {
        recording = false
        cancelRequested = true
        sttExecutor.execute { IndicCtcEngine.close() }
    }

    fun prewarm(context: Context, onError: (String) -> Unit = {}) {
        sttExecutor.execute {
            try {
                IndicCtcEngine.ensureSession(context)
            } catch (e: Exception) {
                Log.w(TAG, "prewarm failed", e)
                mainHandler.post { onError(UNAVAILABLE_MESSAGE) }
            }
        }
    }

    fun downloadModel(
        context: Context,
        onProgress: (Int) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isModelInstalled(context)) {
            mainHandler.post { onProgress(100); onSuccess() }
            return
        }
        if (!downloadRunning.compareAndSet(false, true)) return
        downloadExecutor.execute {
            var partial: File? = null
            try {
                val target = modelFile(context)
                target.parentFile?.mkdirs()
                val partFile = File(target.parentFile, "${target.name}.part")
                partial = partFile
                if (partFile.exists()) partFile.delete()
                val connection = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                }
                connection.connect()
                val total = connection.contentLengthLong
                var written = 0L
                var lastPercent = -1
                connection.inputStream.use { input ->
                    FileOutputStream(partFile).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                val percent = ((written * 100) / total).toInt()
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    mainHandler.post { onProgress(percent.coerceIn(0, 100)) }
                                }
                            }
                        }
                    }
                }
                connection.disconnect()
                if (total > 0 && written != total) throw IllegalStateException("incomplete model download")
                if (!partFile.renameTo(target)) throw IllegalStateException("could not install model")
                downloadRunning.set(false)
                mainHandler.post { onProgress(100); onSuccess() }
            } catch (e: Exception) {
                Log.w(TAG, "model download failed", e)
                partial?.delete()
                downloadRunning.set(false)
                mainHandler.post { onError(DOWNLOAD_FAILED_MESSAGE) }
            }
        }
    }

    private fun record(context: Context, listener: Listener) {
        var audio: AudioRecord? = null
        try {
            val minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuffer <= 0) throw IllegalStateException("unsupported audio configuration")
            val created = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuffer, SAMPLE_RATE)
            )
            audio = created
            if (created.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("microphone unavailable")
            }
            created.startRecording()

            val chunk = ShortArray(SAMPLE_RATE / 5)
            var samples = ShortArray(SAMPLE_RATE * 8)
            var count = 0
            var sawSpeech = false
            var speechSamples = 0
            var silenceSamples = 0
            val noSpeechLimit = NO_SPEECH_TIMEOUT_MS * SAMPLE_RATE / 1000
            val silenceLimit = SILENCE_END_MS * SAMPLE_RATE / 1000
            val maxSamples = MAX_RECORD_MS * SAMPLE_RATE / 1000

            while (recording && count < maxSamples) {
                val read = created.read(chunk, 0, chunk.size)
                if (read <= 0) {
                    if (created.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                        break
                    }
                    continue
                }
                if (count + read > samples.size) {
                    samples = samples.copyOf(maxOf(count + read, samples.size * 2))
                }
                System.arraycopy(chunk, 0, samples, count, read)
                count += read

                var sumSquares = 0f
                for (i in 0 until read) {
                    val value = chunk[i] / 32768f
                    sumSquares += value * value
                }
                val rms = sqrt(sumSquares / read)
                if (rms >= SPEECH_RMS_THRESHOLD) {
                    sawSpeech = true
                    speechSamples += read
                    silenceSamples = 0
                } else if (sawSpeech) {
                    silenceSamples += read
                    if (silenceSamples >= silenceLimit) break
                }
                val amplitude = if (rms > 0f) (20f * log10(rms)).coerceIn(-60f, 0f) else -60f
                mainHandler.post { listener.onAmplitude(amplitude) }

                if (!sawSpeech && count >= noSpeechLimit) break
            }

            try {
                created.stop()
            } catch (e: Exception) {
                Log.w(TAG, "stop failed", e)
            }
            created.release()
            audio = null
            recording = false
            if (cancelRequested) return
            mainHandler.post { listener.onListeningEnd() }
            if (speechSamples < MIN_SPEECH_SAMPLES) {
                mainHandler.post { listener.onError(NO_MATCH_MESSAGE) }
                return
            }
            val pcm = if (count == samples.size) samples else samples.copyOf(count)
            val text = try {
                IndicCtcEngine.transcribe(context, pcm)
            } catch (e: Exception) {
                Log.e(TAG, "transcribe failed", e)
                mainHandler.post { listener.onError(FAILED_MESSAGE) }
                return
            }
            if (cancelRequested) return
            if (text.isBlank()) {
                mainHandler.post { listener.onError(NO_MATCH_MESSAGE) }
                return
            }
            mainHandler.post { listener.onResult(text) }
        } catch (e: Exception) {
            Log.e(TAG, "recording failed", e)
            recording = false
            mainHandler.post { listener.onError(FAILED_MESSAGE) }
        } finally {
            recording = false
            try {
                audio?.release()
            } catch (e: Exception) {
                Log.w(TAG, "release failed", e)
            }
        }
    }
}
