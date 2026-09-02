package com.example.dubstage.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubstage.model.DubLine
import com.example.dubstage.model.Language
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.ui.theme.Dim
import com.example.dubstage.ui.theme.Edge
import com.example.dubstage.ui.theme.Gold
import com.example.dubstage.ui.theme.Panel
import com.example.dubstage.ui.theme.PanelHi
import com.example.dubstage.ui.theme.Red
import com.example.dubstage.ui.theme.Teal
import com.example.dubstage.ui.theme.Txt
import com.example.dubstage.ui.theme.WaveOrig
import kotlin.math.max

@Composable
fun ComparisonWaveformStrip(
    line: DubLine?,
    isRecording: Boolean,
    liveTakeEnvelope: List<Float>,
    liveMicDb: Float,
    isPlaying: Boolean,
    language: Language,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Panel)
            .border(1.dp, Edge, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Strip Header & Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = com.example.dubstage.ui.theme.Acc,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = DubStageStrings.comparisonTitle(language),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = com.example.dubstage.ui.theme.TxtBright
            )

            Spacer(Modifier.weight(1f))

            // Legend indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WaveOrig)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = DubStageStrings.legendOriginal(language),
                    fontSize = 10.sp,
                    color = Dim
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Red else com.example.dubstage.ui.theme.Acc)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = DubStageStrings.legendTake(language),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isRecording) Red else com.example.dubstage.ui.theme.Acc
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Multi-layered Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(com.example.dubstage.ui.theme.BgBot)
                .border(1.dp, Edge, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // Draw Center Zero Line
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1f
                )

                // 1. Draw Original Waveform Silhouette (Blue/Slate steel bars)
                val originalPeaks = line?.originalPeaks ?: emptyList()
                if (originalPeaks.isNotEmpty()) {
                    val barWidth = (w / originalPeaks.size.toFloat()).coerceAtLeast(2f)
                    for (i in originalPeaks.indices) {
                        val peak = originalPeaks[i].coerceIn(0.05f, 1.0f)
                        val barHeight = peak * (h * 0.85f)
                        val x = i * barWidth
                        drawRoundRect(
                            color = WaveOrig,
                            topLeft = Offset(x, midY - barHeight / 2f),
                            size = Size(max(1.5f, barWidth - 1.5f), barHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }

                // 2. Draw Take Waveform Overlaid (Red when recording, Ice Blue when finished)
                val takeData = if (isRecording) liveTakeEnvelope else (line?.takePeaks ?: emptyList())
                if (takeData.isNotEmpty()) {
                    val count = if (isRecording) max(60, takeData.size) else takeData.size
                    val barWidth = (w / count.toFloat()).coerceAtLeast(2f)
                    val takeColor = if (isRecording) Red.copy(alpha = pulseAlpha) else com.example.dubstage.ui.theme.Acc.copy(alpha = 0.9f)

                    for (i in takeData.indices) {
                        val peak = takeData[i].coerceIn(0.05f, 1.0f)
                        val barHeight = peak * (h * 0.82f)
                        val x = i * barWidth
                        drawRoundRect(
                            color = takeColor,
                            topLeft = Offset(x, midY - barHeight / 2f),
                            size = Size(max(1.5f, barWidth - 1.5f), barHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }

                // 3. Playhead Marker Line (Luminous Ice Blue)
                if (isPlaying || isRecording) {
                    val playheadX = if (isRecording) {
                        val progress = (takeData.size.toFloat() / 60f).coerceIn(0f, 1f)
                        progress * w
                    } else {
                        w * 0.5f
                    }

                    drawLine(
                        color = com.example.dubstage.ui.theme.AccHi,
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, h),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = com.example.dubstage.ui.theme.AccHi,
                        radius = 4f,
                        center = Offset(playheadX, 4f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Time Axis & Microphone DB Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "0.0s",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Dim
            )
            Text(
                text = String.format("+%.1fs", (line?.durationSeconds ?: 3.0f) * 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Dim
            )
            Text(
                text = String.format("+%.1fs", (line?.durationSeconds ?: 3.0f)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Dim
            )

            Spacer(Modifier.width(12.dp))

            // Live Level Gauge Status
            if (isRecording || (line != null && line.isRecorded)) {
                val db = if (isRecording) liveMicDb else line?.peakDb ?: -30f
                val isGood = db > -40f && db < -2f
                Surface(
                    color = PanelHi,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isGood) com.example.dubstage.ui.theme.EdgeHi else Edge)
                ) {
                    Text(
                        text = if (isGood) DubStageStrings.micGood(language, db)
                        else DubStageStrings.micTooQuiet(language, db),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGood) com.example.dubstage.ui.theme.Acc else Gold
                    )
                }
            }
        }
    }
}
