package com.example.dubstage.data

import com.example.dubstage.model.DubLine
import com.example.dubstage.model.DubPack
import com.example.dubstage.model.SceneVisualType
import kotlin.math.sin

object SamplePackRepository {

    private fun generateSpeechPeaks(count: Int, seed: Float = 1.0f): List<Float> {
        return List(count) { i ->
            val phase = (i.toFloat() / count) * 3.14159f
            val envelope = sin(phase)
            val modulation = 0.35f * sin(phase * 4.5f * seed) + 0.25f * sin(phase * 8.2f * seed)
            val base = (envelope * (0.60f + modulation)).coerceIn(0.08f, 0.95f)
            base
        }
    }

    fun getDefaultPacks(): List<DubPack> {
        return listOf(
            DubPack(
                id = "pack_cyberpunk_01",
                name = "Cyberpunk Rooftop Encounter",
                description = "High-octane stealth extraction on the rainy neon rooftops of Sector 7.",
                category = "Sci-Fi Action",
                totalDurationSeconds = 14.5f,
                hasBackingTrack = true,
                backingTrackTheme = "Cyberpunk Synthwave & Heavy Rain SFX",
                videoAccentColor = 0xFF00E5FF,
                videoSceneType = SceneVisualType.SCI_FI,
                lines = listOf(
                    DubLine(
                        id = "cp_line_01",
                        index = 1,
                        name = "01_subnet_lockdown",
                        character = "V / Netrunner",
                        startSeconds = 0.8f,
                        durationSeconds = 3.6f,
                        caption = "The subnet is locked down. We've got less than thirty seconds before the ICE detects us!",
                        originalPeaks = generateSpeechPeaks(55, 1.1f)
                    ),
                    DubLine(
                        id = "cp_line_02",
                        index = 2,
                        name = "02_bypass_handshake",
                        character = "V / Netrunner",
                        startSeconds = 4.8f,
                        durationSeconds = 3.8f,
                        caption = "Upload the bypass handshake now. I'll cover the rear elevator shaft!",
                        originalPeaks = generateSpeechPeaks(55, 1.4f)
                    ),
                    DubLine(
                        id = "cp_line_03",
                        index = 3,
                        name = "03_extraction_clear",
                        character = "V / Netrunner",
                        startSeconds = 9.2f,
                        durationSeconds = 4.2f,
                        caption = "Target data secured! Grab the neural drive and leap for the hover-cab!",
                        originalPeaks = generateSpeechPeaks(60, 0.9f)
                    )
                ),
                isCustom = false
            ),
            DubPack(
                id = "pack_fantasy_02",
                name = "Anime Fantasy Summit",
                description = "Dramatic clifftop confrontation before the ancient dragon temple.",
                category = "Anime Fantasy",
                totalDurationSeconds = 15.0f,
                hasBackingTrack = true,
                backingTrackTheme = "Orchestral Strings & Mountain Winds SFX",
                videoAccentColor = 0xFFFF7043,
                videoSceneType = SceneVisualType.ANIME_BATTLE,
                lines = listOf(
                    DubLine(
                        id = "fan_line_01",
                        index = 1,
                        name = "01_sword_draw",
                        character = "Ren / Blademaster",
                        startSeconds = 1.0f,
                        durationSeconds = 3.5f,
                        caption = "You shouldn't have followed me to the summit. Turn back while you still have your honor.",
                        originalPeaks = generateSpeechPeaks(55, 1.2f)
                    ),
                    DubLine(
                        id = "fan_line_02",
                        index = 2,
                        name = "02_oath_challenge",
                        character = "Ren / Blademaster",
                        startSeconds = 5.2f,
                        durationSeconds = 4.0f,
                        caption = "My oath to the royal guard cannot be broken, not even for an old friend.",
                        originalPeaks = generateSpeechPeaks(58, 1.6f)
                    ),
                    DubLine(
                        id = "fan_line_03",
                        index = 3,
                        name = "03_final_clash",
                        character = "Ren / Blademaster",
                        startSeconds = 9.8f,
                        durationSeconds = 4.2f,
                        caption = "Then draw your blade! Let the wind decide which creed endures this dusk!",
                        originalPeaks = generateSpeechPeaks(60, 1.0f)
                    )
                ),
                isCustom = false
            )
        )
    }
}
