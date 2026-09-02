package com.example.dubstage.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubstage.model.DubLine
import com.example.dubstage.model.SceneVisualType
import com.example.dubstage.ui.theme.*

@Composable
fun ScenePreviewVisualizer(
    sceneType: SceneVisualType,
    currentLine: DubLine?,
    isPlaying: Boolean,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    elapsedSeconds: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scene_anim")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BgBot)
            .border(1.dp, if (isRecording) Red else Edge, RoundedCornerShape(24.dp))
    ) {
        // Canvas Scene Graphics
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (sceneType) {
                SceneVisualType.SCI_FI -> {
                    // Deep space starry backdrop + horizon grid
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF07090E), Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    )

                    // Cybernetic star grid lines
                    val horizonY = h * 0.65f
                    for (i in -4..12) {
                        val startX = w * 0.5f + (i * 45f - w * 0.2f)
                        drawLine(
                            color = Color(0x33A8C7FA),
                            start = Offset(w * 0.5f, horizonY),
                            end = Offset(startX * 1.8f, h),
                            strokeWidth = 1.5f
                        )
                    }

                    // Celestial planet orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFD1E4FF), Color(0xFFA8C7FA), Color(0x000F172A)),
                            center = Offset(w * 0.75f, h * 0.35f),
                            radius = 90f * if (isPlaying) pulseAnim else 1f
                        ),
                        radius = 80f * if (isPlaying) pulseAnim else 1f,
                        center = Offset(w * 0.75f, h * 0.35f)
                    )
                }
                SceneVisualType.ANIME_BATTLE -> {
                    // Dramatic action energy glow & speed lines
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF220A10), Color(0xFF3B0F1E), Color(0xFF14050A))
                        )
                    )

                    // Anime speed lines
                    val numLines = 14
                    for (i in 0 until numLines) {
                        val angleRatio = i.toFloat() / numLines
                        val lineStart = Offset(w * 0.5f, h * 0.5f)
                        val lineEnd = Offset(
                            w * 0.5f + (Math.cos(angleRatio * Math.PI * 2) * w).toFloat(),
                            h * 0.5f + (Math.sin(angleRatio * Math.PI * 2) * h).toFloat()
                        )
                        drawLine(
                            color = if (isPlaying || isRecording) Color(0x44FF8599) else Color(0x18FF8599),
                            start = lineStart,
                            end = lineEnd,
                            strokeWidth = 2f
                        )
                    }

                    // Central glowing spirit sigil
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFB3BA), Color(0x88FF5252), Color(0x00000000)),
                            center = Offset(w * 0.5f, h * 0.45f),
                            radius = 110f * if (isPlaying || isRecording) pulseAnim else 0.9f
                        ),
                        radius = 100f,
                        center = Offset(w * 0.5f, h * 0.45f)
                    )
                }
                SceneVisualType.NOIR_DETECTIVE -> {
                    // Noir venetian blind shadows + rain drops
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF121316), Color(0xFF1B1C20), Color(0xFF0B0C0E))
                        )
                    )

                    // Venetian blinds horizontal cast shadows
                    for (y in 0..h.toInt() step 32) {
                        drawLine(
                            color = Color(0x44000000),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(w, y.toFloat() + 15f),
                            strokeWidth = 14f
                        )
                    }

                    // Golden warm streetlamp beam
                    val lampPath = Path().apply {
                        moveTo(w * 0.2f, 0f)
                        lineTo(w * 0.55f, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = lampPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x33A8C7FA), Color(0x08A8C7FA))
                        )
                    )
                }
                SceneVisualType.COMEDY_CAFE -> {
                    // Warm retro diner neon aesthetic
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0F1D2B), Color(0xFF142E44), Color(0xFF0B1520))
                        )
                    )

                    // Neon diner arches
                    drawCircle(
                        color = Color(0x33A8C7FA),
                        radius = 140f,
                        center = Offset(w * 0.5f, h * 0.4f),
                        style = Stroke(width = 3f)
                    )
                    drawCircle(
                        color = Color(0x22D1E4FF),
                        radius = 100f,
                        center = Offset(w * 0.5f, h * 0.4f),
                        style = Stroke(width = 2f)
                    )
                }
                SceneVisualType.CUSTOM -> {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF131316), Color(0xFF1C1D22))
                        )
                    )
                }
            }
        }

        // Top Scene HUD overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xDD121316),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Scene",
                        tint = Acc,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "SCENE PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Txt,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Live Timecode HUD
            Surface(
                color = Color(0xDD121316),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Text(
                    text = String.format(
                        "TC %02d:%05.2f",
                        (elapsedSeconds / 60).toInt(),
                        (elapsedSeconds % 60)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Red else Acc
                )
            }
        }

        // Bottom Character Badge & Status
        if (currentLine != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xEE121316),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Acc),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = com.example.dubstage.ui.theme.AccDarkest,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentLine.character,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Line #${currentLine.index} (${String.format("%.1fs", currentLine.durationSeconds)})",
                                fontSize = 10.sp,
                                color = Dim
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (isRecording) {
                    Surface(
                        color = Red,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "LIVE REC",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else if (isPlaying) {
                    Surface(
                        color = Acc,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Playing",
                                tint = com.example.dubstage.ui.theme.AccDarkest,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "PLAYING",
                                color = com.example.dubstage.ui.theme.AccDarkest,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
