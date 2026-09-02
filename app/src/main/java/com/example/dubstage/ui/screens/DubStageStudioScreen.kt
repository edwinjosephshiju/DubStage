package com.example.dubstage.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubstage.model.DubPack
import com.example.dubstage.model.Language
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.ui.components.ComparisonWaveformStrip
import com.example.dubstage.ui.components.CountdownOverlay
import com.example.dubstage.ui.components.LineProgressStrip
import com.example.dubstage.ui.components.ScenePreviewVisualizer
import com.example.dubstage.ui.theme.*
import com.example.dubstage.viewmodel.UiState

@Composable
fun DubStageStudioScreen(
    state: UiState,
    onExit: () -> Unit,
    onSelectLine: (Int) -> Unit,
    onPlayOriginal: () -> Unit,
    onPlayTake: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onLeaveLineEmpty: () -> Unit,
    onPrevLine: () -> Unit,
    onNextLine: () -> Unit,
    onProceedToFinale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pack = state.selectedPack ?: return
    val currentLine = pack.lines.getOrNull(state.currentLineIndex)
    val isLastLine = state.currentLineIndex >= pack.lines.lastIndex

    Box(modifier = modifier.fillMaxSize().background(BgBot)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Panel)
                        .border(1.dp, Edge, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Pack",
                        tint = Txt,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = pack.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = pack.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Acc
                    )
                }

                Spacer(Modifier.weight(1f))

                // Line Navigation Header Counter
                Surface(
                    color = Panel,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Text(
                        text = DubStageStrings.lineOf(state.language, state.currentLineIndex + 1, pack.lines.size),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccHi
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Video Preview Visualizer
            ScenePreviewVisualizer(
                sceneType = pack.videoSceneType,
                currentLine = currentLine,
                isPlaying = state.isPlayingOriginal || state.isPlayingTake,
                isRecording = state.isRecording,
                elapsedSeconds = currentLine?.startSeconds ?: 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Subtitle & Character Dialogue Card (High contrast, character pill, time range)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Character & Timecode Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AccDark,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Acc)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = currentLine?.character?.uppercase() ?: "CHARACTER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = AccHi
                                )
                            }
                        }

                        Text(
                            text = if (currentLine != null) {
                                val start = currentLine.startSeconds
                                val end = start + currentLine.durationSeconds
                                String.format("%02d:%04.1f - %02d:%04.1f (%.1fs)", (start / 60).toInt(), start % 60, (end / 60).toInt(), end % 60, currentLine.durationSeconds)
                            } else "00:00.0",
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Dim
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "“${currentLine?.caption ?: ""}”",
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // The Signature Waveform Comparison Strip
            ComparisonWaveformStrip(
                line = currentLine,
                isRecording = state.isRecording,
                liveTakeEnvelope = state.liveTakeEnvelope,
                liveMicDb = state.liveMicDb,
                isPlaying = state.isPlayingOriginal || state.isPlayingTake,
                language = state.language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // Line Progress Strip
            LineProgressStrip(
                lines = pack.lines,
                currentIndex = state.currentLineIndex,
                language = state.language,
                onSelectLine = onSelectLine
            )

            Spacer(Modifier.height(18.dp))

            // Action Deck Controls
            // Row 1: Original & My Take
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayOriginal,
                    enabled = !state.isRecording && !state.isCountingDown,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Panel,
                        contentColor = Txt
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Icon(
                        imageVector = if (state.isPlayingOriginal) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Acc,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = DubStageStrings.playOriginal(state.language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Txt
                    )
                }

                Button(
                    onClick = onPlayTake,
                    enabled = (currentLine?.isRecorded == true) && !state.isRecording && !state.isCountingDown,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentLine?.isRecorded == true) com.example.dubstage.ui.theme.AccDark else Panel,
                        contentColor = if (currentLine?.isRecorded == true) AccHi else Dim
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (currentLine?.isRecorded == true) com.example.dubstage.ui.theme.EdgeHi else Edge)
                ) {
                    Icon(
                        imageVector = if (state.isPlayingTake) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (currentLine?.isRecorded == true) AccHi else Dim,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = DubStageStrings.playTake(state.language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentLine?.isRecorded == true) AccHi else Dim
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Giant Main Record Dock
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        if (state.isRecording) {
                            onStopRecording()
                        } else {
                            onStartRecording()
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = if (state.isRecording) Red else AccHi,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (state.isRecording) Red else com.example.dubstage.ui.theme.Acc)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (state.isRecording) Color.White.copy(alpha = 0.25f) else com.example.dubstage.ui.theme.AccDarker),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isRecording) "RECORDING IN PROGRESS" else "VOICE TAKE READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = if (state.isRecording) Color.White.copy(alpha = 0.9f) else com.example.dubstage.ui.theme.AccDarker
                        )
                        Text(
                            text = when {
                                state.isRecording -> DubStageStrings.stop(state.language)
                                currentLine?.isRecorded == true -> DubStageStrings.recordAgain(state.language)
                                else -> DubStageStrings.record(state.language)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isRecording) Color.White else com.example.dubstage.ui.theme.AccDarkest
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Row 3: Skip / Leave Line Empty & Prev / Next Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLeaveLineEmpty,
                    enabled = !state.isRecording && !state.isCountingDown,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Text(
                        text = DubStageStrings.leaveEmpty(state.language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Dim
                    )
                }

                Button(
                    onClick = onPrevLine,
                    enabled = state.currentLineIndex > 0 && !state.isRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = Panel),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = Txt,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = if (isLastLine) onProceedToFinale else onNextLine,
                    enabled = !state.isRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Acc,
                        contentColor = com.example.dubstage.ui.theme.AccDarkest
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (isLastLine) DubStageStrings.finish(state.language) else DubStageStrings.next(state.language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // 3-2-1 Countdown Overlay
        CountdownOverlay(
            countdownNumber = state.countdownNumber,
            isVisible = state.isCountingDown
        )
    }
}
