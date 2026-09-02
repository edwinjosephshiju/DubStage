package com.example.dubstage.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class AudioRecordEngine {

    companion object {
        const val SAMPLE_RATE = 44100
        const val FRAME_MS = 20
        const val SAMPLES_PER_FRAME = SAMPLE_RATE * FRAME_MS / 1000 // 882 samples
    }

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentRmsDb = MutableStateFlow(-100f)
    val currentRmsDb: StateFlow<Float> = _currentRmsDb.asStateFlow()

    private val _liveEnvelope = MutableStateFlow<List<Float>>(emptyList())
    val liveEnvelope: StateFlow<List<Float>> = _liveEnvelope.asStateFlow()

    private val recordedChunks = Collections.synchronizedList(mutableListOf<FloatArray>())
    private val envelopePoints = Collections.synchronizedList(mutableListOf<Float>())

    @SuppressLint("MissingPermission")
    fun startRecording(onChunk: ((FloatArray) -> Unit)? = null): Boolean {
        if (_isRecording.value) return false

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = max(minBufferSize, SAMPLES_PER_FRAME * 4)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return false
            }

            recordedChunks.clear()
            envelopePoints.clear()
            _liveEnvelope.value = emptyList()
            _currentRmsDb.value = -100f

            audioRecord?.startRecording()
            _isRecording.value = true

            recordJob = scope.launch {
                val shortBuffer = ShortArray(SAMPLES_PER_FRAME)
                var accumulatedSamples = 0
                var currentMax = 0f
                var sumSquares = 0.0

                while (isActive && _isRecording.value) {
                    val readCount = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                    if (readCount > 0) {
                        val floatChunk = FloatArray(readCount)
                        for (i in 0 until readCount) {
                            val sample = shortBuffer[i] / 32768.0f
                            floatChunk[i] = sample
                            val absVal = Math.abs(sample)
                            if (absVal > currentMax) currentMax = absVal
                            sumSquares += (sample * sample)
                            accumulatedSamples++
                        }
                        recordedChunks.add(floatChunk)
                        onChunk?.invoke(floatChunk)

                        // If we have at least 1 frame of samples, compute envelope point & dB
                        if (accumulatedSamples >= SAMPLES_PER_FRAME) {
                            val meanSquare = sumSquares / accumulatedSamples
                            val rms = sqrt(meanSquare).toFloat()
                            val db = if (rms > 1e-5f) 20f * log10(rms) else -100f

                            envelopePoints.add(currentMax.coerceIn(0.02f, 1.0f))
                            _currentRmsDb.value = db
                            _liveEnvelope.value = envelopePoints.toList()

                            accumulatedSamples = 0
                            currentMax = 0f
                            sumSquares = 0.0
                        }
                    }
                }
            }
            return true
        } catch (_: Exception) {
            stopRecording()
            return false
        }
    }

    fun stopRecording(): FloatArray {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        // Merge all recorded chunks into single continuous FloatArray
        val totalLength = recordedChunks.sumOf { it.size }
        val merged = FloatArray(totalLength)
        var offset = 0
        for (chunk in recordedChunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.size)
            offset += chunk.size
        }

        return merged
    }

    fun getRecordedEnvelope(): List<Float> {
        return envelopePoints.toList()
    }

    /**
     * Resamples or bins raw audio into N visual peak values (0..1)
     */
    fun extractPeaks(pcm: FloatArray, count: Int = 60): List<Float> {
        if (pcm.isEmpty() || count <= 0) return emptyList()
        val step = pcm.size.toFloat() / count
        val peaks = mutableListOf<Float>()
        for (c in 0 until count) {
            val start = (c * step).toInt()
            val end = ((c + 1) * step).toInt().coerceAtMost(pcm.size)
            var maxAmp = 0f
            for (i in start until end) {
                val abs = Math.abs(pcm[i])
                if (abs > maxAmp) maxAmp = abs
            }
            peaks.add(maxAmp.coerceIn(0.05f, 0.98f))
        }
        return peaks
    }

    /**
     * Normalizes float audio to peak ~0.95
     */
    fun normalize(pcm: FloatArray, targetPeak: Float = 0.95f): FloatArray {
        var maxAmp = 0f
        for (sample in pcm) {
            val abs = Math.abs(sample)
            if (abs > maxAmp) maxAmp = abs
        }
        if (maxAmp < 1e-4f) return pcm
        val scale = targetPeak / maxAmp
        val out = FloatArray(pcm.size)
        for (i in pcm.indices) {
            out[i] = (pcm[i] * scale).coerceIn(-1.0f, 1.0f)
        }
        return out
    }
}
