package com.example.dubstage.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max

/**
 * Extracts and decodes native audio tracks from local video/audio files (MP4, MKV, WebM, WAV, AAC, MP3)
 * into high-fidelity 44.1kHz FloatArray PCM buffers for neural processing.
 */
object AudioMediaDecoder {

    private const val TAG = "AudioMediaDecoder"
    const val TARGET_SAMPLE_RATE = 44100

    suspend fun decodeAudioFromUri(context: Context, uri: Uri): FloatArray? = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        var codec: MediaCodec? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null
            var mime: String? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val trackMime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (trackMime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    mime = trackMime
                    break
                }
            }

            if (audioTrackIndex == -1 || mime == null || audioFormat == null) {
                Log.w(TAG, "No audio track found in media file: $uri")
                return@withContext null
            }

            extractor.selectTrack(audioTrackIndex)
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(audioFormat, null, null, 0)
            codec.start()

            val sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            } else 44100

            val channelCount = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            } else 2

            val rawSamples = mutableListOf<Float>()
            val bufferInfo = MediaCodec.BufferInfo()
            var isEos = false
            val timeoutUs = 5000L

            val maxSamples = TARGET_SAMPLE_RATE * 180 // Limit max decoded audio to 3 minutes for memory safety

            while (!isEos && rawSamples.size < maxSamples) {
                val inIndex = codec.dequeueInputBuffer(timeoutUs)
                if (inIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEos = true
                        } else {
                            val pts = extractor.sampleTime
                            codec.queueInputBuffer(inIndex, 0, sampleSize, pts, 0)
                            extractor.advance()
                        }
                    }
                }

                var outIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                while (outIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

                        val shortBuffer = outputBuffer.asShortBuffer()
                        val numShorts = shortBuffer.remaining()

                        if (channelCount >= 2) {
                            var i = 0
                            while (i < numShorts && rawSamples.size < maxSamples) {
                                val left = shortBuffer.get(i).toFloat() / 32768.0f
                                val right = if (i + 1 < numShorts) shortBuffer.get(i + 1).toFloat() / 32768.0f else left
                                rawSamples.add((left + right) * 0.5f)
                                i += channelCount
                            }
                        } else {
                            for (i in 0 until numShorts) {
                                if (rawSamples.size >= maxSamples) break
                                rawSamples.add(shortBuffer.get(i).toFloat() / 32768.0f)
                            }
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                        break
                    }
                    outIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            if (rawSamples.isNotEmpty()) {
                // Resample to 44.1kHz with smooth linear interpolation if needed
                if (sampleRate != TARGET_SAMPLE_RATE && sampleRate > 0) {
                    val ratio = sampleRate.toDouble() / TARGET_SAMPLE_RATE
                    val targetLen = (rawSamples.size / ratio).toInt()
                    val resampled = FloatArray(targetLen)
                    for (i in 0 until targetLen) {
                        val srcPos = i * ratio
                        val srcIdx0 = srcPos.toInt().coerceIn(0, rawSamples.size - 1)
                        val srcIdx1 = (srcIdx0 + 1).coerceIn(0, rawSamples.size - 1)
                        val frac = (srcPos - srcIdx0).toFloat()
                        resampled[i] = rawSamples[srcIdx0] * (1f - frac) + rawSamples[srcIdx1] * frac
                    }
                    return@withContext resampled
                }
                return@withContext rawSamples.toFloatArray()
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode audio track from $uri", e)
            null
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (_: Exception) {}
            try {
                extractor?.release()
            } catch (_: Exception) {}
        }
    }
}
