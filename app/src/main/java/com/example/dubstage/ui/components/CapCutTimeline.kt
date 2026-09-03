package com.example.dubstage.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Repeat
import com.example.dubstage.forge.DetectedSegment
import com.example.dubstage.model.DemucsStemResult
import com.example.dubstage.viewmodel.ForgeStemMode
import com.example.dubstage.ui.theme.Acc
import com.example.dubstage.ui.theme.AccDark
import com.example.dubstage.ui.theme.AccDarkest
import com.example.dubstage.ui.theme.AccHi
import com.example.dubstage.ui.theme.BgBot
import com.example.dubstage.ui.theme.BgTop
import com.example.dubstage.ui.theme.Dim
import com.example.dubstage.ui.theme.Edge
import com.example.dubstage.ui.theme.EdgeHi
import com.example.dubstage.ui.theme.Panel
import com.example.dubstage.ui.theme.PanelHi
import com.example.dubstage.ui.theme.Red
import com.example.dubstage.ui.theme.Txt
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * CapCut-Style Multi-Track Audio & Video Timeline
 * Features:
 * - Timecode ruler with tick marks and scrubber
 * - Multi-track layout: Video Filmstrip, Demucs Vocals/Speech, Instrumental BGM, Subtitles
 * - Interactive clip selection with trim borders & split/delete controls
 * - Zoom in/out and playhead scrubbing
 */
@Composable
fun CapCutTimeline(
    totalDurationSeconds: Float,
    segments: List<DetectedSegment>,
    selectedSegmentIndex: Int,
    currentPlayheadSeconds: Float,
    isPlaying: Boolean,
    onSelectSegment: (Int) -> Unit,
    onScrubPlayhead: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onSplitSegment: ((Int) -> Unit)? = null,
    onDeleteSegment: ((Int) -> Unit)? = null,
    onAddSegment: (() -> Unit)? = null,
    isLoopingClip: Boolean = false,
    onToggleLoopClip: (() -> Unit)? = null,
    isMuteVocals: Boolean = false,
    isMuteBgm: Boolean = false,
    onToggleMuteVocals: (() -> Unit)? = null,
    onToggleMuteBgm: (() -> Unit)? = null,
    demucsStemResult: DemucsStemResult? = null,
    videoFileName: String? = null,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) } // 0.6f (fit) to 2.5f (zoomed)

    val safeDuration = max(totalDurationSeconds, (segments.maxOfOrNull { it.endSeconds } ?: 15.0f) + 1.0f)
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C0D11))
            .border(1.dp, Edge, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        val containerWidth = maxWidth
        val trackHeaderWidth = 68.dp
        val availableCanvasWidth = (containerWidth - trackHeaderWidth - 2.dp).coerceAtLeast(200.dp)

        // 1 second in timeline corresponds to dynamic pixelsPerSec based on zoom
        val basePixelsPerSec = (availableCanvasWidth.value / safeDuration).coerceIn(24f, 90f)
        val pixelsPerSec = basePixelsPerSec * zoomLevel
        val calculatedWidthDp = (safeDuration * pixelsPerSec).dp
        val timelineWidthDp = if (calculatedWidthDp < availableCanvasWidth) availableCanvasWidth else calculatedWidthDp

        Column(modifier = Modifier.fillMaxWidth()) {
            // --- CapCut Top Toolbar (Row 1: Header & Timecode) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: CapCut Badge
                Surface(
                    color = AccDark,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "TIMELINE EDITOR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = AccHi
                        )
                    }
                }

                // Right: Timecode Display Badge
                Surface(
                    color = PanelHi,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Text(
                        text = String.format(
                            "%02d:%04.1f / %02d:%04.1f",
                            (currentPlayheadSeconds / 60).toInt(),
                            currentPlayheadSeconds % 60,
                            (safeDuration / 60).toInt(),
                            safeDuration % 60
                        ),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // --- CapCut Controls Bar (Row 2: Play, Loop, Edit Tools, Zoom) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Playback & Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Play / Pause preview button
                    Button(
                        onClick = onTogglePlay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) Red else Acc,
                            contentColor = if (isPlaying) Color.White else AccDarkest
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Loop Selected Clip Tool
                    if (onToggleLoopClip != null) {
                        Surface(
                            color = if (isLoopingClip) AccHi else PanelHi,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isLoopingClip) AccHi else EdgeHi),
                            modifier = Modifier
                                .height(30.dp)
                                .clickable { onToggleLoopClip() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Loop Selected Clip",
                                    tint = if (isLoopingClip) AccDarkest else AccHi,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = if (isLoopingClip) "LOOP ON" else "LOOP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLoopingClip) AccDarkest else AccHi
                                )
                            }
                        }
                    }

                    // Split Clip Tool
                    if (onSplitSegment != null && selectedSegmentIndex in segments.indices) {
                        Surface(
                            color = PanelHi,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi),
                            modifier = Modifier
                                .height(30.dp)
                                .clickable { onSplitSegment(selectedSegmentIndex) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = "Split Clip",
                                    tint = Acc,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "SPLIT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccHi
                                )
                            }
                        }
                    }

                    // Delete Clip Tool
                    if (onDeleteSegment != null && segments.size > 1 && selectedSegmentIndex in segments.indices) {
                        Surface(
                            color = PanelHi,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Red.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(30.dp)
                                .clickable { onDeleteSegment(selectedSegmentIndex) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Clip",
                                    tint = Red,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "DEL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Red
                                )
                            }
                        }
                    }
                }

                // Right: Zoom Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = PanelHi,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { zoomLevel = (zoomLevel - 0.25f).coerceIn(0.6f, 2.5f) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = Dim,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Surface(
                        color = PanelHi,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { zoomLevel = (zoomLevel + 0.25f).coerceIn(0.6f, 2.5f) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = Acc,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // --- Timeline Multi-Track Body ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF13151A))
                    .border(1.dp, Edge, RoundedCornerShape(14.dp))
            ) {
                // Left Track Headers (Fixed Column)
                Column(
                    modifier = Modifier
                        .width(trackHeaderWidth)
                        .background(Color(0xFF161820))
                        .padding(vertical = 4.dp)
                ) {
                    // Header space for Ruler
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "TRACKS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Dim
                        )
                    }

                    // Track 1 Header: Vocals / Speech
                    TrackHeaderRow(
                        label = "VOCALS",
                        color = Color(0xFF00F2FE),
                        icon = Icons.Default.GraphicEq,
                        isMuted = isMuteVocals,
                        height = 52.dp,
                        onToggleMute = { onToggleMuteVocals?.invoke() }
                    )

                    Spacer(Modifier.height(4.dp))

                    // Track 2 Header: BGM / Instrumental
                    TrackHeaderRow(
                        label = "BGM",
                        color = Color(0xFFA855F7),
                        icon = Icons.Default.VolumeUp,
                        isMuted = isMuteBgm,
                        height = 38.dp,
                        onToggleMute = { onToggleMuteBgm?.invoke() }
                    )

                    Spacer(Modifier.height(4.dp))

                    // Track 3 Header: Captions
                    TrackHeaderRow(
                        label = "TEXT",
                        color = Color(0xFFFFB800),
                        icon = Icons.Default.Subtitles,
                        isMuted = false,
                        height = 30.dp,
                        onToggleMute = {}
                    )
                }

                // Right Horizontally Scrollable Multi-Track Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier
                            .width(timelineWidthDp)
                            .padding(vertical = 4.dp)
                    ) {
                        // 1. Timecode Ruler
                        TimecodeRuler(
                            durationSeconds = safeDuration,
                            widthDp = timelineWidthDp,
                            onScrub = onScrubPlayhead
                        )

                        // 1. Track 1: Vocals / Detected Speech Clips (Interactive)
                        SpeechClipsTrack(
                            durationSeconds = safeDuration,
                            segments = segments,
                            selectedSegmentIndex = selectedSegmentIndex,
                            demucsStemResult = demucsStemResult,
                            isMuted = isMuteVocals,
                            onSelectSegment = onSelectSegment,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )

                        Spacer(Modifier.height(4.dp))

                        // 2. Track 2: BGM / Demucs Backing Waveform
                        BgmWaveformTrack(
                            durationSeconds = safeDuration,
                            demucsStemResult = demucsStemResult,
                            isMuted = isMuteBgm,
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        )

                        Spacer(Modifier.height(4.dp))

                        // 3. Track 3: Captions / Dialogue Script
                        CaptionsTrack(
                            durationSeconds = safeDuration,
                            segments = segments,
                            selectedSegmentIndex = selectedSegmentIndex,
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        )
                    }

                    // 6. Glowing Vertical Playhead Line traversing all tracks
                    val playheadFraction = (currentPlayheadSeconds / safeDuration).coerceIn(0f, 1f)
                    val playheadOffsetDp = timelineWidthDp * playheadFraction

                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp - 1.dp)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Red, Color(0xFFFF4757), Color(0x88FF4757))
                                )
                            )
                    )

                    // Playhead Head Knob
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp - 6.dp, y = 0.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Red)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            // --- Active Selected Clip Inspector Bar (Adaptive) ---
            if (selectedSegmentIndex in segments.indices) {
                val selected = segments[selectedSegmentIndex]
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF161820),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Acc)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${selected.label.uppercase()} (%.1fs)".format(selected.durationSeconds),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccHi,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "%.1fs - %.1fs".format(selected.startSeconds, selected.endSeconds),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Dim,
                                maxLines = 1
                            )
                        }

                        Surface(
                            color = AccDark,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                        ) {
                            Text(
                                text = "Stem Active",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccHi,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderRow(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isMuted: Boolean,
    height: androidx.compose.ui.unit.Dp,
    onToggleMute: () -> Unit
) {
    Surface(
        color = Color(0xFF1C1E26),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262832)),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMuted) Dim else Color.White
                )
            }

            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else icon,
                contentDescription = null,
                tint = if (isMuted) Red else color,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onToggleMute() }
            )
        }
    }
}

@Composable
private fun TimecodeRuler(
    durationSeconds: Float,
    widthDp: androidx.compose.ui.unit.Dp,
    onScrub: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Color(0xFF101217))
            .pointerInput(durationSeconds) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onScrub(fraction * durationSeconds)
                }
            }
            .pointerInput(durationSeconds) {
                detectDragGestures { change, _ ->
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onScrub(fraction * durationSeconds)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val totalSec = durationSeconds.toInt().coerceAtLeast(1)

            // Draw major second ticks and minor subdivision ticks
            for (sec in 0..totalSec) {
                val x = (sec / durationSeconds) * w
                // Major tick
                drawLine(
                    color = Color(0xFF4A4E5C),
                    start = Offset(x, h - 10f),
                    end = Offset(x, h),
                    strokeWidth = 1.5f
                )

                // 2 Minor ticks between each second
                for (sub in 1..3) {
                    val subX = x + ((sub / 4f) / durationSeconds) * w
                    if (subX < w) {
                        drawLine(
                            color = Color(0xFF282B36),
                            start = Offset(subX, h - 5f),
                            end = Offset(subX, h),
                            strokeWidth = 1f
                        )
                    }
                }
            }
        }

        // Second Labels on ruler
        val stepSec = if (durationSeconds > 30f) 5 else if (durationSeconds > 15f) 2 else 1
        for (sec in 0..durationSeconds.toInt() step stepSec) {
            val fraction = sec / durationSeconds
            Text(
                text = String.format("%02d:%02d", sec / 60, sec % 60),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Dim,
                modifier = Modifier
                    .offset(x = widthDp * fraction + 2.dp, y = 2.dp)
            )
        }
    }
}

@Composable
private fun SpeechClipsTrack(
    durationSeconds: Float,
    segments: List<DetectedSegment>,
    selectedSegmentIndex: Int,
    demucsStemResult: DemucsStemResult?,
    isMuted: Boolean,
    onSelectSegment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val vocalsPcm = demucsStemResult?.vocalsPcm ?: FloatArray(0)
    val vocalPeaks = demucsStemResult?.vocalPeaks ?: emptyList()

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF242734), RoundedCornerShape(8.dp))
    ) {
        val totalW = maxWidth

        segments.forEachIndexed { index, seg ->
            val isSelected = index == selectedSegmentIndex
            val startFraction = (seg.startSeconds / durationSeconds).coerceIn(0f, 1f)
            val durationFraction = (seg.durationSeconds / durationSeconds).coerceIn(0f, 1f)

            val clipLeft = totalW * startFraction
            val clipWidth = (totalW * durationFraction).coerceAtLeast(32.dp)

            Box(
                modifier = Modifier
                    .offset(x = clipLeft)
                    .width(clipWidth)
                    .fillMaxHeight()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isMuted -> Color(0xFF1E2028)
                            isSelected -> Color(0xFF2D1B4E)
                            else -> Color(0xFF1E293B)
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = when {
                            isMuted -> Color(0xFF4A4E5C)
                            isSelected -> Color(0xFF00F2FE)
                            else -> Color(0xFF38BDF8).copy(alpha = 0.6f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectSegment(index) }
            ) {
                // Real Waveform drawing inside clip block
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f
                    val barCount = (w / 4f).toInt().coerceIn(8, 48)

                    val segStartSample = (seg.startSeconds * 44100).toInt().coerceIn(0, vocalsPcm.size)
                    val segEndSample = (seg.endSeconds * 44100).toInt().coerceIn(segStartSample, vocalsPcm.size)
                    val segLen = segEndSample - segStartSample

                    for (i in 0 until barCount) {
                        val barX = (i.toFloat() / barCount) * w
                        var amp = 0.25f

                        if (segLen > 0 && vocalsPcm.isNotEmpty()) {
                            val sampleIdx = segStartSample + ((i.toFloat() / barCount) * segLen).toInt().coerceIn(0, segLen - 1)
                            val s = kotlin.math.abs(vocalsPcm.getOrElse(sampleIdx) { 0f })
                            amp = (s * 2.2f).coerceIn(0.12f, 0.95f)
                        } else if (vocalPeaks.isNotEmpty()) {
                            val peakIdx = ((seg.startSeconds / durationSeconds * vocalPeaks.size) + (i.toFloat() / barCount) * (seg.durationSeconds / durationSeconds * vocalPeaks.size)).toInt().coerceIn(0, vocalPeaks.size - 1)
                            amp = vocalPeaks.getOrElse(peakIdx) { 0.3f }.coerceIn(0.12f, 0.95f)
                        } else {
                            val seed = (index * 17 + i * 7).toDouble()
                            amp = (sin(seed * 0.4) * 0.35 + sin(seed * 1.8) * 0.25 + 0.45).toFloat().coerceIn(0.15f, 0.95f)
                        }

                        val barHeight = amp * (h - 10f)

                        drawLine(
                            color = when {
                                isMuted -> Color(0xFF4A4E5C)
                                isSelected -> Color(0xFF00F2FE)
                                else -> Color(0xFF7DD3FC)
                            },
                            start = Offset(barX, midY - barHeight / 2f),
                            end = Offset(barX, midY + barHeight / 2f),
                            strokeWidth = 2f
                        )
                    }
                    
                    // Left and Right Editing Handles if selected
                    if (isSelected) {
                        // Left Handle
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(0f, 0f),
                            size = Size(8f, h),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawLine(
                            color = Color.Black,
                            start = Offset(4f, h / 2f - 6f),
                            end = Offset(4f, h / 2f + 6f),
                            strokeWidth = 2f
                        )
                        
                        // Right Handle
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(w - 8f, 0f),
                            size = Size(8f, h),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawLine(
                            color = Color.Black,
                            start = Offset(w - 4f, h / 2f - 6f),
                            end = Offset(w - 4f, h / 2f + 6f),
                            strokeWidth = 2f
                        )
                    }
                }

                // Clip Title & Handle Badges
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isMuted) "${seg.label} (Muted)" else seg.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) Dim else if (isSelected) Color.White else Color(0xFFE2E8F0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isSelected) {
                        Surface(
                            color = if (isMuted) Color(0xFF4A4E5C) else Color(0xFF00F2FE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = String.format("%.1fs", seg.durationSeconds),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isMuted) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BgmWaveformTrack(
    durationSeconds: Float,
    demucsStemResult: DemucsStemResult?,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val backingPcm = demucsStemResult?.backingPcm ?: FloatArray(0)
    val backingPeaks = demucsStemResult?.backingPeaks ?: emptyList()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF242734), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp)) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val bars = (w / 4.5f).toInt().coerceAtLeast(30)

            for (i in 0 until bars) {
                val bx = (i.toFloat() / bars) * w
                var amp = 0.3f

                if (backingPcm.isNotEmpty()) {
                    val sampleIdx = ((i.toFloat() / bars) * backingPcm.size).toInt().coerceIn(0, backingPcm.size - 1)
                    val s = kotlin.math.abs(backingPcm[sampleIdx])
                    amp = (s * 2.0f).coerceIn(0.1f, 0.95f)
                } else if (backingPeaks.isNotEmpty()) {
                    val peakIdx = ((i.toFloat() / bars) * backingPeaks.size).toInt().coerceIn(0, backingPeaks.size - 1)
                    amp = backingPeaks[peakIdx].coerceIn(0.1f, 0.95f)
                } else {
                    amp = (sin(i * 0.18) * 0.35 + sin(i * 0.8) * 0.25 + 0.4).toFloat().coerceIn(0.1f, 0.9f)
                }

                val barH = amp * (h - 8f)

                drawLine(
                    color = if (isMuted) Color(0xFF4A4E5C) else Color(0xFFA855F7).copy(alpha = 0.85f),
                    start = Offset(bx, midY - barH / 2f),
                    end = Offset(bx, midY + barH / 2f),
                    strokeWidth = 2.2f
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isMuted) "BGM / SFX (Muted)" else "BGM / Instrumental & SFX Track (htdemucs_ft Isolated)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMuted) Dim else Color(0xFFD8B4FE)
            )
        }
    }
}

@Composable
private fun CaptionsTrack(
    durationSeconds: Float,
    segments: List<DetectedSegment>,
    selectedSegmentIndex: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111319))
            .border(1.dp, Color(0xFF20222C), RoundedCornerShape(8.dp))
    ) {
        val totalW = maxWidth

        segments.forEachIndexed { index, seg ->
            val isSelected = index == selectedSegmentIndex
            val startFraction = (seg.startSeconds / durationSeconds).coerceIn(0f, 1f)
            val durationFraction = (seg.durationSeconds / durationSeconds).coerceIn(0f, 1f)

            val clipLeft = totalW * startFraction
            val clipWidth = (totalW * durationFraction).coerceAtLeast(30.dp)

            Surface(
                modifier = Modifier
                    .offset(x = clipLeft)
                    .width(clipWidth)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color(0xFF854D0E) else Color(0xFF27272A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFFFBBF24) else Color(0xFF52525B)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Subtitle #${index + 1}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color(0xFFFEF3C7) else Color(0xFFA1A1AA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
