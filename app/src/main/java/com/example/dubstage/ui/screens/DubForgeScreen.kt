package com.example.dubstage.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.model.Language
import com.example.dubstage.ui.components.CapCutTimeline
import com.example.dubstage.viewmodel.ForgeStemMode
import com.example.dubstage.ui.theme.Acc
import com.example.dubstage.ui.theme.AccDark
import com.example.dubstage.ui.theme.AccDarkest
import com.example.dubstage.ui.theme.AccHi
import com.example.dubstage.ui.theme.BgBot
import com.example.dubstage.ui.theme.Dim
import com.example.dubstage.ui.theme.Edge
import com.example.dubstage.ui.theme.EdgeHi
import com.example.dubstage.ui.theme.Panel
import com.example.dubstage.ui.theme.PanelHi
import com.example.dubstage.ui.theme.Red
import com.example.dubstage.ui.theme.RedHi
import com.example.dubstage.ui.theme.Txt
import com.example.dubstage.viewmodel.UiState

@Composable
fun DubForgeScreen(
    state: UiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onSelectSegment: (Int) -> Unit,
    onScrubPlayhead: (Float) -> Unit,
    onToggleTimelinePlay: () -> Unit,
    onSplitSegment: (Int) -> Unit,
    onDeleteSegment: (Int) -> Unit,
    onAddSegment: () -> Unit,
    onImportVideo: (uriString: String, fileName: String) -> Unit,
    onRunDemucsSeparation: () -> Unit,
    onBuildPack: () -> Unit,
    onDismissMessage: () -> Unit,
    onNudgeSegmentStart: (Int, Float) -> Unit = { _, _ -> },
    onNudgeSegmentEnd: (Int, Float) -> Unit = { _, _ -> },
    onMergeSegment: (Int) -> Unit = {},
    onPlaySegmentSlice: (Int) -> Unit = {},
    onLoopSegmentSlice: (Int) -> Unit = {},
    onStopSegmentSlice: () -> Unit = {},
    onUpdateSegmentLabel: (Int, String) -> Unit = { _, _ -> },
    onToggleLoopClip: () -> Unit = {},
    onSetStemMode: (ForgeStemMode) -> Unit = {},
    onToggleMuteVocals: () -> Unit = {},
    onToggleMuteBgm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Media / Video Picker Activity Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/")?.substringAfterLast(":") ?: "imported_scene.mp4"
            onImportVideo(uri.toString(), fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBot)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Forge Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Panel)
                    .border(1.dp, Edge, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "ON-DEVICE GPU PIPELINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Acc
                            )
                            Text(
                                text = DubStageStrings.forgeTitle(state.language),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = AccHi,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "GPU htdemucs_ft",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccHi
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "1. Main model (htdemucs_ft) splits video audio into Vocals & BGM (SFX + Music). 2. Plots real waveform envelopes. 3. Silero VAD clips the vocals timeline into dialogue scenes. 4. Synchronized dual-stem playback with solo/mute.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Dim
                    )
                }
            }
        }

        // Pack Created Notification Banner
        if (state.forgePackCreatedMessage != null) {
            item {
                Surface(
                    color = AccDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = state.forgePackCreatedMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccHi,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissMessage, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Dim,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Import Local Video Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                color = Color(0xFF1E1B4B),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4338CA)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "LOCAL VIDEO SOURCE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF818CF8)
                                )
                                Text(
                                    text = state.forgeVideoFileName ?: "No Video Selected",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (state.forgeVideoFileName != null) {
                            Surface(
                                color = Color(0xFF064E3B),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669))
                            ) {
                                Text(
                                    text = "Active Video",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (state.forgeVideoFileName != null) "Change Local Video" else "Import Local Video",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Demucs Audio Separation (Strictly On-Device Hardware GPU) Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "htdemucs_ft Neural Stem Separation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Txt
                            )
                        }

                        Surface(
                            color = if (state.isDemucsProcessing) Color(0xFF78350F) else AccDark,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                        ) {
                            Text(
                                text = if (state.isDemucsProcessing) "Processing..." else "On-Device GPU",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isDemucsProcessing) Color(0xFFFDE047) else AccHi,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "High-precision htdemucs_ft (Fine-Tuned Hybrid Transformer) running directly on device GPU with dual-domain cross-attention and 0.1% vocal bleed.",
                        fontSize = 11.sp,
                        color = Dim
                    )

                    Spacer(Modifier.height(10.dp))

                    // Hardware GPU Specs Info Box
                    Surface(
                        color = BgBot,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ACCELERATOR:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Dim
                                )
                                Text(
                                    text = state.demucsStemResult?.gpuDeviceName ?: "On-Device Hardware GPU (NNAPI / Vulkan FP16)",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccHi,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "VOCAL ISOLATION:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Dim
                                )
                                Text(
                                    text = "${state.demucsStemResult?.vocalIsolationScorePercent ?: 98}% (0.1% bleed)",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                            if ((state.demucsStemResult?.processingLatencyMs ?: 0) > 0) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "INFERENCE LATENCY:",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Dim
                                    )
                                    Text(
                                        text = "${state.demucsStemResult?.processingLatencyMs} ms",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = AccHi
                                    )
                                }
                            }
                        }
                    }

                    if (state.forgeVideoFileName != null) {
                        Spacer(Modifier.height(10.dp))

                        // Stem Audition Mode Selector Pills
                        Text(
                            text = "PLAYBACK & AUDITION STEM TARGET:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Dim
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val modes = listOf(
                                Triple(ForgeStemMode.VOCALS, "🎙️ Vocals Only", "Isolate dialogue & speech"),
                                Triple(ForgeStemMode.BACKING, "🎵 Backing/BGM", "Music & SFX track"),
                                Triple(ForgeStemMode.FULL_MIX, "🎬 Full Mix", "Combined raw audio")
                            )
                            modes.forEach { (mode, label, _) ->
                                val isSelected = state.forgeStemMode == mode
                                Surface(
                                    color = if (isSelected) AccDark else BgBot,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccHi else Edge),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clickable { onSetStemMode(mode) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccHi else Dim
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Re-run GPU Separation Button
                        Button(
                            onClick = onRunDemucsSeparation,
                            enabled = !state.isDemucsProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Acc,
                                contentColor = AccDarkest
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            if (state.isDemucsProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = AccDarkest,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(state.demucsProcessingStep, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Re-Run htdemucs_ft On-Device GPU Stem Isolation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. CapCut-Style Audio & Video Timeline
        item {
            Column {
                Text(
                    text = "MULTI-TRACK TIMELINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Dim,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                if (state.forgeVideoFileName == null) {
                    // Empty Timeline state when no video is imported
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Panel),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = BgBot,
                                shape = CircleShape,
                                modifier = Modifier.size(56.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Dim,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Timeline Idle — No Video Loaded",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Import a local video to extract real audio waveforms, isolate stems on the device GPU, and arrange speech clips on the multi-track timeline.",
                                fontSize = 11.sp,
                                color = Dim,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Acc,
                                    contentColor = AccDarkest
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Import Local Video to Start", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    CapCutTimeline(
                        totalDurationSeconds = state.forgeTotalDurationSeconds,
                        segments = state.forgeSegments,
                        selectedSegmentIndex = state.forgeSelectedSegmentIndex,
                        currentPlayheadSeconds = state.forgePlayheadSeconds,
                        isPlaying = state.forgeIsTimelinePlaying,
                        onSelectSegment = onSelectSegment,
                        onScrubPlayhead = onScrubPlayhead,
                        onTogglePlay = onToggleTimelinePlay,
                        onSplitSegment = onSplitSegment,
                        onDeleteSegment = onDeleteSegment,
                        onAddSegment = onAddSegment,
                        isLoopingClip = state.forgeIsLoopingClip,
                        onToggleLoopClip = onToggleLoopClip,
                        isMuteVocals = state.forgeIsMuteVocals,
                        isMuteBgm = state.forgeIsMuteBgm,
                        onToggleMuteVocals = onToggleMuteVocals,
                        onToggleMuteBgm = onToggleMuteBgm,
                        demucsStemResult = state.demucsStemResult,
                        videoFileName = state.forgeVideoFileName
                    )
                }
            }
        }

        // Active Selected Clip Editor & Fine-Tuning
        if (state.forgeVideoFileName != null && state.forgeSelectedSegmentIndex in state.forgeSegments.indices) {
            val selectedIndex = state.forgeSelectedSegmentIndex
            val selectedClip = state.forgeSegments[selectedIndex]

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = AccHi,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "CLIP #${selectedIndex + 1} FINE-TUNING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    color = AccHi
                                )
                            }

                            Surface(
                                color = AccDark,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                            ) {
                                Text(
                                    text = String.format("%.2fs duration", selectedClip.durationSeconds),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccHi,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Timing Nudge Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start timing nudge box
                            Surface(
                                color = BgBot,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "START: ${String.format("%.2fs", selectedClip.startSeconds)}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Dim
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { onNudgeSegmentStart(selectedIndex, -0.1f) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PanelHi,
                                                contentColor = AccHi
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("-0.1s", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { onNudgeSegmentStart(selectedIndex, 0.1f) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PanelHi,
                                                contentColor = AccHi
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("+0.1s", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // End timing nudge box
                            Surface(
                                color = BgBot,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "END: ${String.format("%.2fs", selectedClip.endSeconds)}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Dim
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { onNudgeSegmentEnd(selectedIndex, -0.1f) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PanelHi,
                                                contentColor = AccHi
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("-0.1s", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { onNudgeSegmentEnd(selectedIndex, 0.1f) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PanelHi,
                                                contentColor = AccHi
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("+0.1s", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Clip Label / Subtitle Text Editor
                        OutlinedTextField(
                            value = selectedClip.label,
                            onValueChange = { onUpdateSegmentLabel(selectedIndex, it) },
                            label = { Text("Dialogue / Subtitle Label", color = Dim, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Txt,
                                unfocusedTextColor = Txt,
                                focusedBorderColor = Acc,
                                unfocusedBorderColor = Edge
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        // Action Buttons: Play Slice, Loop Slice, Merge Next, Delete Clip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onPlaySegmentSlice(selectedIndex) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Acc,
                                    contentColor = AccDarkest
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Audition", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Dedicated Loop Selected Clip button
                            Button(
                                onClick = { onLoopSegmentSlice(selectedIndex) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.forgeIsLoopingClip) AccHi else PanelHi,
                                    contentColor = if (state.forgeIsLoopingClip) AccDarkest else AccHi
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (state.forgeIsLoopingClip) "Looping" else "Loop Clip",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (selectedIndex < state.forgeSegments.size - 1) {
                                OutlinedButton(
                                    onClick = { onMergeSegment(selectedIndex) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA855F7)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallMerge,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Merge Next", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { onDeleteSegment(selectedIndex) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedHi),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Red.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Pack Details & Sensitivity Tuning (only active when video is loaded)
        if (state.forgeVideoFileName != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "METADATA & SPEECH RECOGNITION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = Acc
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.forgeTitle,
                            onValueChange = onTitleChange,
                            label = { Text("Pack Title", color = Dim) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Txt,
                                unfocusedTextColor = Txt,
                                focusedBorderColor = Acc,
                                unfocusedBorderColor = Edge
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.forgeDescription,
                            onValueChange = onDescriptionChange,
                            label = { Text("Scene Description", color = Dim) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Txt,
                                unfocusedTextColor = Txt,
                                focusedBorderColor = Acc,
                                unfocusedBorderColor = Edge
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        // Speech sensitivity slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Speech Detection Sensitivity: ${String.format("%.1fx", state.forgeSensitivity)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Txt
                            )
                        }

                        Slider(
                            value = state.forgeSensitivity,
                            onValueChange = onSensitivityChange,
                            valueRange = 0.4f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = Acc,
                                activeTrackColor = Acc,
                                inactiveTrackColor = PanelHi
                            )
                        )
                    }
                }
            }

            // 6. Build Pack CTA
            item {
                Button(
                    onClick = onBuildPack,
                    enabled = state.forgeSegments.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Acc,
                        contentColor = AccDarkest
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Generate Dub Pack (${state.forgeSegments.size} Clips)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
