package com.example.dubstage.audio

import com.example.dubstage.model.DubPack
import kotlin.math.max
import kotlin.math.min

object DubMixer {

    private const val SAMPLE_RATE = 44100

    /**
     * Renders a full scene mix combining backing track and user takes / original dialogue
     */
    fun renderFullSceneMix(
        pack: DubPack,
        duckOriginalFactor: Float = 0.20f
    ): FloatArray {
        val totalDuration = max(pack.totalDurationSeconds, 1.0f)
        val totalSamples = (totalDuration * SAMPLE_RATE).toInt() + SAMPLE_RATE
        val mix = FloatArray(totalSamples)

        // 1. Synthesize / load backing track
        val stemResult = pack.demucsStemResult
        val backing = if (stemResult != null && stemResult.backingPcm.isNotEmpty()) {
            stemResult.backingPcm
        } else if (pack.hasBackingTrack) {
            AudioSynth.synthesizeBackingTrack(totalDuration + 1f, pack.videoSceneType)
        } else null

        if (backing != null) {
            val copyLen = min(mix.size, backing.size)
            System.arraycopy(backing, 0, mix, 0, copyLen)
        }

        // 2. Mix in takes (or isolated original vocal line if not recorded)
        for (line in pack.lines) {
            val startSample = (line.startSeconds * SAMPLE_RATE).toInt()
            val audioToMix = if (line.isRecorded && line.takePcm != null && line.takePcm.isNotEmpty()) {
                // User's voice take
                line.takePcm
            } else if (stemResult != null && stemResult.vocalsPcm.isNotEmpty()) {
                // Isolated original vocal stem slice
                val lineStart = (line.startSeconds * SAMPLE_RATE).toInt().coerceIn(0, stemResult.vocalsPcm.size)
                val lineEnd = ((line.startSeconds + line.durationSeconds) * SAMPLE_RATE).toInt().coerceIn(lineStart, stemResult.vocalsPcm.size)
                if (lineEnd > lineStart) stemResult.vocalsPcm.copyOfRange(lineStart, lineEnd)
                else AudioSynth.synthesizeOriginalDialogueAudio(line.durationSeconds, pack.videoSceneType, pitchBase = 200.0 + (line.index * 35.0))
            } else {
                // Original dialogue audio fallback
                AudioSynth.synthesizeOriginalDialogueAudio(
                    line.durationSeconds,
                    pack.videoSceneType,
                    pitchBase = 200.0 + (line.index * 35.0)
                )
            }

            if (startSample < mix.size) {
                val availableSpace = mix.size - startSample
                val mixLen = min(audioToMix.size, availableSpace)

                // If no backing track, duck background under dialogue
                if (backing == null) {
                    for (i in 0 until mixLen) {
                        mix[startSample + i] *= duckOriginalFactor
                    }
                }

                for (i in 0 until mixLen) {
                    mix[startSample + i] = (mix[startSample + i] + audioToMix[i] * 0.9f).coerceIn(-1.5f, 1.5f)
                }
            }
        }

        // 3. Master normalization
        var peak = 0f
        for (sample in mix) {
            val abs = Math.abs(sample)
            if (abs > peak) peak = abs
        }

        if (peak > 1e-4f) {
            val gain = 0.95f / peak
            for (i in mix.indices) {
                mix[i] = (mix[i] * gain).coerceIn(-1.0f, 1.0f)
            }
        }

        return mix
    }
}
