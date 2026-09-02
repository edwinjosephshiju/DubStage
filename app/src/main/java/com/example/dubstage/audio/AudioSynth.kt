package com.example.dubstage.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.dubstage.model.SceneVisualType
import kotlin.math.PI
import kotlin.math.sin

object AudioSynth {

    private const val SAMPLE_RATE = 44100

    /**
     * Generates PCM 16-bit sine wave audio for audio beeps and preview synthesis
     */
    fun createTonePcm(frequency: Double, durationSeconds: Float, amplitude: Float = 0.6f): ShortArray {
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // smooth attack/decay envelope
            val env = when {
                i < 200 -> i / 200.0
                i > numSamples - 200 -> (numSamples - i) / 200.0
                else -> 1.0
            }
            val sample = (sin(2.0 * PI * frequency * t) * amplitude * env * Short.MAX_VALUE).toInt()
            samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Synthesizes a natural voice-like dialogue audio wave for original lines
     */
    fun synthesizeOriginalDialogueAudio(
        durationSeconds: Float,
        sceneType: SceneVisualType,
        pitchBase: Double = 220.0
    ): FloatArray {
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val out = FloatArray(numSamples)

        val formant1 = when (sceneType) {
            SceneVisualType.SCI_FI -> 380.0
            SceneVisualType.ANIME_BATTLE -> 520.0
            SceneVisualType.NOIR_DETECTIVE -> 240.0
            SceneVisualType.COMEDY_CAFE -> 340.0
            SceneVisualType.CUSTOM -> 320.0
        }
        val formant2 = formant1 * 2.4
        val formant3 = formant1 * 3.8

        val phraseDuration = 5.2
        val activeStart = 0.6
        val activeEnd = 4.4
        val fadeTime = 0.12 // 120ms raised cosine smoothing window

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Smooth continuous phrase envelope with raised cosine fades
            val phraseEnvelope: Double = if (durationSeconds > 5.0f) {
                val cycleT = t % phraseDuration
                when {
                    cycleT < activeStart - fadeTime -> 0.0
                    cycleT < activeStart + fadeTime -> {
                        val progress = (cycleT - (activeStart - fadeTime)) / (2.0 * fadeTime)
                        0.5 - 0.5 * kotlin.math.cos(PI * progress.coerceIn(0.0, 1.0))
                    }
                    cycleT <= activeEnd - fadeTime -> 1.0
                    cycleT < activeEnd + fadeTime -> {
                        val progress = (cycleT - (activeEnd - fadeTime)) / (2.0 * fadeTime)
                        0.5 + 0.5 * kotlin.math.cos(PI * progress.coerceIn(0.0, 1.0))
                    }
                    else -> 0.0
                }
            } else {
                val envStart = (t / 0.18).coerceIn(0.0, 1.0)
                val envEnd = ((durationSeconds - t) / 0.18).coerceIn(0.0, 1.0)
                val startSmooth = 0.5 - 0.5 * kotlin.math.cos(PI * envStart)
                val endSmooth = 0.5 - 0.5 * kotlin.math.cos(PI * envEnd)
                startSmooth * endSmooth
            }

            if (phraseEnvelope <= 0.001) {
                out[i] = 0f
                continue
            }

            // Natural human pitch vibrato & sentence intonation cadence
            val intonation = sin(2.0 * PI * 0.7 * t) * 12.0
            val vibrato = sin(2.0 * PI * 5.2 * t) * 4.0
            val currentPitch = (pitchBase + intonation + vibrato).coerceAtLeast(80.0)

            // Syllabic envelope modulation (approx 3.8 syllables per second with smooth dips)
            val syllableMod = (sin(2.0 * PI * 3.8 * t) * 0.42 + 0.58).coerceIn(0.12, 1.0)

            // Voice glottal pulse & formant harmonics
            val fundamental = sin(2.0 * PI * currentPitch * t)
            val harmonic2 = sin(2.0 * PI * (currentPitch * 2.0) * t) * 0.45
            val harmonic3 = sin(2.0 * PI * (currentPitch * 3.0) * t) * 0.25
            val f1Res = sin(2.0 * PI * formant1 * t) * 0.35
            val f2Res = sin(2.0 * PI * formant2 * t) * 0.20
            val f3Res = sin(2.0 * PI * formant3 * t) * 0.10
            val aspiration = (Math.random() * 2.0 - 1.0) * 0.025

            val voiceSignal = (fundamental + harmonic2 + harmonic3 + f1Res + f2Res + f3Res + aspiration) * syllableMod * phraseEnvelope * 0.75
            out[i] = voiceSignal.toFloat().coerceIn(-1.0f, 1.0f)
        }
        return out
    }

    /**
     * Synthesizes backing score track for the scene
     */
    fun synthesizeBackingTrack(durationSeconds: Float, sceneType: SceneVisualType): FloatArray {
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val out = FloatArray(numSamples)

        when (sceneType) {
            SceneVisualType.SCI_FI -> {
                // Ambient cinematic synth pad chord progression (Cm9 - Abmaj7)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val c3 = sin(2.0 * PI * 130.81 * t) * 0.25
                    val eb3 = sin(2.0 * PI * 155.56 * t) * 0.20
                    val g3 = sin(2.0 * PI * 196.00 * t) * 0.20
                    val subBass = sin(2.0 * PI * 65.41 * t) * 0.30
                    val pulse = (sin(2.0 * PI * 0.5 * t) * 0.5 + 0.5)
                    out[i] = ((c3 + eb3 + g3 + subBass) * pulse * 0.45).toFloat().coerceIn(-1f, 1f)
                }
            }
            SceneVisualType.ANIME_BATTLE -> {
                // Driving bass groove + power chord rhythm
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val bass = sin(2.0 * PI * 82.41 * t) * 0.35
                    val beat = if ((t % 0.5) < 0.08) sin(2.0 * PI * 60.0 * (t % 0.5) * 10) * 0.4 else 0.0
                    val drive = sin(2.0 * PI * 164.81 * t) * 0.25
                    out[i] = ((bass + beat + drive) * 0.5).toFloat().coerceIn(-1f, 1f)
                }
            }
            SceneVisualType.NOIR_DETECTIVE -> {
                // Melancholic jazz bassline + rain hiss
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val bass = sin(2.0 * PI * 73.42 * t) * 0.3
                    val jazzChord = sin(2.0 * PI * 146.83 * t) * 0.15 + sin(2.0 * PI * 220.0 * t) * 0.15
                    val rainHiss = (Math.random() * 2 - 1) * 0.05
                    out[i] = ((bass + jazzChord + rainHiss) * 0.4).toFloat().coerceIn(-1f, 1f)
                }
            }
            SceneVisualType.COMEDY_CAFE -> {
                // Bouncy pizzicato groove
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val bounce = sin(2.0 * PI * 110.0 * t) * 0.25 * ((t % 0.4) / 0.4).let { 1.0 - it }
                    val shaker = if ((t % 0.25) < 0.04) (Math.random() * 0.15) else 0.0
                    out[i] = ((bounce + shaker) * 0.45).toFloat().coerceIn(-1f, 1f)
                }
            }
            SceneVisualType.CUSTOM -> {
                // Dynamic cinematic orchestration (Synth Pad + Kick / Snare Pulse + Sub bass)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val pad = (sin(2.0 * PI * 130.81 * t) * 0.2 + sin(2.0 * PI * 164.81 * t) * 0.15 + sin(2.0 * PI * 196.00 * t) * 0.15)
                    val bass = sin(2.0 * PI * 65.41 * t) * 0.25
                    val beatPulse = if ((t % 1.0) < 0.06) sin(2.0 * PI * 55.0 * (t % 1.0) * 15) * 0.35 else 0.0
                    val hat = if ((t % 0.25) < 0.02) (Math.random() * 2 - 1) * 0.06 else 0.0
                    out[i] = ((pad + bass + beatPulse + hat) * 0.55).toFloat().coerceIn(-1f, 1f)
                }
            }
        }
        return out
    }

    @Volatile
    private var currentTrack: AudioTrack? = null
    @Volatile
    private var currentPlaybackThread: Thread? = null
    @Volatile
    private var isPlayingAudio: Boolean = false

    /**
     * Immediately stops any currently active AudioTrack playback, releasing resources
     */
    fun stopPlayback() {
        isPlayingAudio = false
        try {
            currentTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                    track.flush()
                    track.stop()
                }
                track.release()
            }
        } catch (_: Exception) {}
        currentTrack = null

        try {
            currentPlaybackThread?.interrupt()
        } catch (_: Exception) {}
        currentPlaybackThread = null
    }

    fun isCurrentlyPlaying(): Boolean = isPlayingAudio

    /**
     * Plays PCM float array directly on an AudioTrack in background with optional loop
     */
    fun playFloatArray(
        pcm: FloatArray,
        loop: Boolean = false,
        onFinished: (() -> Unit)? = null
    ): AudioTrack? {
        stopPlayback()

        if (pcm.isEmpty()) {
            onFinished?.invoke()
            return null
        }

        val shorts = ShortArray(pcm.size) { i ->
            (pcm[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBuf * 2).coerceAtLeast(4096)

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) {
            onFinished?.invoke()
            return null
        }

        currentTrack = track
        isPlayingAudio = true

        val thread = Thread {
            try {
                track.play()
                val chunkSize = 2048
                do {
                    var offset = 0
                    while (offset < shorts.size && isPlayingAudio && !Thread.currentThread().isInterrupted) {
                        val count = minOf(chunkSize, shorts.size - offset)
                        val written = track.write(shorts, offset, count)
                        if (written <= 0) break
                        offset += written
                    }
                } while (loop && isPlayingAudio && !Thread.currentThread().isInterrupted)

                if (isPlayingAudio && !loop) {
                    val drainMs = (bufferSize * 1000L / (SAMPLE_RATE * 2)).coerceIn(40L, 250L)
                    Thread.sleep(drainMs)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.pause()
                        track.flush()
                        track.stop()
                    }
                    track.release()
                } catch (_: Exception) {}

                if (currentTrack === track) {
                    currentTrack = null
                    isPlayingAudio = false
                }
                onFinished?.invoke()
            }
        }

        currentPlaybackThread = thread
        thread.start()
        return track
    }

    /**
     * Plays a single short beep (e.g. for countdown)
     */
    fun playBeep(freq: Double, durationMs: Long = 140L) {
        val pcm = createTonePcm(freq, durationMs / 1000f, 0.45f)
        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(pcm.size * 2)

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread {
                Thread.sleep(durationMs + 50L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }.start()
        } catch (_: Exception) {}
    }
}
