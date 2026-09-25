package com.palash.setu.util

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

internal object IndicCtcEngine {
    private const val TAG = "IndicCtcEngine"
    private const val SAMPLE_RATE = 16000
    private const val FRAME_LENGTH = 400
    private const val FRAME_SHIFT = 160
    private const val FFT_SIZE = 512
    private const val MEL_BINS = 80
    private const val PREEMPH = 0.97f
    private const val LOG_EPS = 1.1920929e-7f
    private const val DEFAULT_BLANK_ID = 5632

    private val lock = Any()
    private var session: OrtSession? = null
    private var tokens: Array<String> = emptyArray()
    private var blankId = DEFAULT_BLANK_ID

    private val hannWindow = FloatArray(FRAME_LENGTH) { i ->
        (0.5 - 0.5 * cos(2.0 * Math.PI * i / FRAME_LENGTH)).toFloat()
    }

    private val melWeights: Array<FloatArray> by lazy { buildMelWeights() }

    fun ensureSession(context: Context): OrtSession {
        synchronized(lock) {
            session?.let { return it }
            val model = OnnxStt.modelFile(context)
            if (!model.exists()) throw IllegalStateException("speech model is missing")
            val loadedTokens = loadTokens(context)
            val options = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setInterOpNumThreads(1)
                setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            val created = OrtEnvironment.getEnvironment().createSession(model.absolutePath, options)
            tokens = loadedTokens
            blankId = loadedTokens.indexOf("<blk>").takeIf { it >= 0 } ?: DEFAULT_BLANK_ID
            session = created
            return created
        }
    }

    fun close() {
        synchronized(lock) {
            try {
                session?.close()
            } catch (e: Exception) {
                Log.w(TAG, "session close failed", e)
            }
            session = null
            tokens = emptyArray()
        }
    }

    fun transcribe(context: Context, pcm: ShortArray): String {
        val active = ensureSession(context)
        val frames = (pcm.size + FRAME_SHIFT / 2) / FRAME_SHIFT
        if (frames <= 0) return ""
        val features = extractFeatures(pcm, frames)
        val environment = OrtEnvironment.getEnvironment()
        OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(features),
            longArrayOf(1, MEL_BINS.toLong(), frames.toLong())
        ).use { featureTensor ->
            OnnxTensor.createTensor(
                environment,
                LongBuffer.wrap(longArrayOf(frames.toLong())),
                longArrayOf(1)
            ).use { lengthTensor ->
                val inputs = mapOf(
                    "audio_signal" to featureTensor,
                    "length" to lengthTensor
                )
                active.run(inputs).use { result ->
                    val output = result[0] as OnnxTensor
                    return decode(output.floatBuffer)
                }
            }
        }
    }

    private fun decode(logits: FloatBuffer): String {
        val vocab = tokens.size
        val size = logits.remaining()
        if (vocab == 0 || size < vocab) return ""
        val framesOut = size / vocab
        val values = FloatArray(size)
        logits.get(values)
        val builder = StringBuilder()
        var previous = -1
        for (t in 0 until framesOut) {
            val base = t * vocab
            var best = 0
            var bestValue = values[base]
            for (v in 1 until vocab) {
                val value = values[base + v]
                if (value > bestValue) {
                    bestValue = value
                    best = v
                }
            }
            if (best != previous && best != blankId) {
                val token = tokens[best]
                if (token.isNotEmpty() && token != "<unk>" && token != "<blk>") {
                    builder.append(token)
                }
            }
            previous = best
        }
        return builder.toString().replace('▁', ' ').trim()
    }

    private fun extractFeatures(pcm: ShortArray, frames: Int): FloatArray {
        val sampleCount = pcm.size
        val wave = FloatArray(sampleCount) { pcm[it] / 32768f }
        val features = FloatArray(frames * MEL_BINS)
        val frame = FloatArray(FRAME_LENGTH)
        val padded = FloatArray(FFT_SIZE)
        val real = FloatArray(FFT_SIZE)
        val imaginary = FloatArray(FFT_SIZE)
        val power = FloatArray(FFT_SIZE / 2 + 1)

        for (f in 0 until frames) {
            val start = FRAME_SHIFT * f + FRAME_SHIFT / 2 - FRAME_LENGTH / 2
            for (s in 0 until FRAME_LENGTH) {
                var index = start + s
                while (index < 0 || index >= sampleCount) {
                    index = if (index < 0) -index - 1 else 2 * sampleCount - 1 - index
                }
                frame[s] = wave[index]
            }
            for (i in FRAME_LENGTH - 1 downTo 1) frame[i] -= PREEMPH * frame[i - 1]
            frame[0] -= PREEMPH * frame[0]

            padded.fill(0f)
            for (i in 0 until FRAME_LENGTH) padded[i] = frame[i] * hannWindow[i]
            padded.copyInto(real)
            imaginary.fill(0f)
            fft(real, imaginary)

            for (k in 0..FFT_SIZE / 2) {
                power[k] = real[k] * real[k] + imaginary[k] * imaginary[k]
            }

            val offset = f * MEL_BINS
            for (m in 0 until MEL_BINS) {
                val weights = melWeights[m]
                var sum = 0f
                for (k in power.indices) {
                    val weight = weights[k]
                    if (weight != 0f) sum += weight * power[k]
                }
                features[offset + m] = ln(if (sum > LOG_EPS) sum else LOG_EPS)
            }
        }

        normalizePerFeature(features, frames)
        return toChannelFirst(features, frames)
    }

    private fun normalizePerFeature(features: FloatArray, frames: Int) {
        for (m in 0 until MEL_BINS) {
            var mean = 0f
            for (t in 0 until frames) mean += features[t * MEL_BINS + m]
            mean /= frames
            var variance = 0f
            for (t in 0 until frames) {
                val centered = features[t * MEL_BINS + m] - mean
                variance += centered * centered
            }
            val inverseStd = 1f / (sqrt(variance / frames) + 1e-5f)
            for (t in 0 until frames) {
                features[t * MEL_BINS + m] = (features[t * MEL_BINS + m] - mean) * inverseStd
            }
        }
    }

    private fun toChannelFirst(features: FloatArray, frames: Int): FloatArray {
        val transposed = FloatArray(MEL_BINS * frames)
        for (t in 0 until frames) {
            for (m in 0 until MEL_BINS) {
                transposed[m * frames + t] = features[t * MEL_BINS + m]
            }
        }
        return transposed
    }

    private fun fft(real: FloatArray, imaginary: FloatArray) {
        val size = real.size
        var j = 0
        for (i in 1 until size) {
            var bit = size shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val realTemp = real[i]
                real[i] = real[j]
                real[j] = realTemp
                val imaginaryTemp = imaginary[i]
                imaginary[i] = imaginary[j]
                imaginary[j] = imaginaryTemp
            }
        }
        var length = 2
        while (length <= size) {
            val angle = -2.0 * Math.PI / length
            val weightReal = cos(angle).toFloat()
            val weightImaginary = sin(angle).toFloat()
            val half = length shr 1
            for (i in 0 until size step length) {
                var currentReal = 1f
                var currentImaginary = 0f
                for (k in 0 until half) {
                    val topIndex = i + k
                    val bottomIndex = topIndex + half
                    val topReal = real[topIndex]
                    val topImaginary = imaginary[topIndex]
                    val bottomReal = real[bottomIndex] * currentReal - imaginary[bottomIndex] * currentImaginary
                    val bottomImaginary = real[bottomIndex] * currentImaginary + imaginary[bottomIndex] * currentReal
                    real[topIndex] = topReal + bottomReal
                    imaginary[topIndex] = topImaginary + bottomImaginary
                    real[bottomIndex] = topReal - bottomReal
                    imaginary[bottomIndex] = topImaginary - bottomImaginary
                    val nextReal = currentReal * weightReal - currentImaginary * weightImaginary
                    currentImaginary = currentReal * weightImaginary + currentImaginary * weightReal
                    currentReal = nextReal
                }
            }
            length = length shl 1
        }
    }

    private fun buildMelWeights(): Array<FloatArray> {
        val bins = FFT_SIZE / 2 + 1
        val hzPerBin = SAMPLE_RATE.toFloat() / FFT_SIZE
        val melLow = slaneyMel(0f)
        val melHigh = slaneyMel(SAMPLE_RATE / 2f)
        val delta = (melHigh - melLow) / (MEL_BINS + 1)
        val weights = Array(MEL_BINS) { FloatArray(bins) }
        for (m in 0 until MEL_BINS) {
            val left = inverseSlaneyMel(melLow + m * delta)
            val center = inverseSlaneyMel(melLow + (m + 1) * delta)
            val right = inverseSlaneyMel(melLow + (m + 2) * delta)
            for (k in 0 until bins) {
                val hz = k * hzPerBin
                if (hz > left && hz < right) {
                    val weight = if (hz <= center) {
                        (hz - left) / (center - left)
                    } else {
                        (right - hz) / (right - center)
                    }
                    weights[m][k] = weight * 2f / (right - left)
                }
            }
        }
        return weights
    }

    private fun slaneyMel(freq: Float): Float =
        if (freq <= 1000f) freq * 3f / 200f
        else 15f + 14.545078505785561f * ln(freq / 1000f)

    private fun inverseSlaneyMel(mel: Float): Float =
        if (mel <= 15f) mel * 200f / 3f
        else 1000f * exp((mel - 15f) * 0.06875177742094911f)

    private fun loadTokens(context: Context): Array<String> {
        val lines = context.assets.open("indicconformer-hi/tokens.txt")
            .bufferedReader(Charsets.UTF_8)
            .useLines { sequence -> sequence.filter { it.isNotBlank() }.toList() }
        var maxId = -1
        val parsed = ArrayList<Pair<Int, String>>(lines.size)
        for (line in lines) {
            val split = line.lastIndexOf(' ')
            if (split <= 0) continue
            val id = line.substring(split + 1).toIntOrNull() ?: continue
            parsed.add(id to line.substring(0, split))
            if (id > maxId) maxId = id
        }
        if (maxId < 0) throw IllegalStateException("tokens.txt is empty")
        val table = Array(maxId + 1) { "" }
        for ((id, token) in parsed) table[id] = token
        return table
    }
}
