package com.example.dubstage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.dubstage.ui.components.ScenePreviewVisualizer
import com.example.dubstage.ui.theme.*
import com.example.dubstage.viewmodel.UiState

@Composable
fun FinaleScreen(
    state: UiState,
    onTogglePlayMaster: () -> Unit,
    onSaveDub: () -> Unit,
    onBackToLines: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pack = state.selectedPack ?: return
    var isSaved by remember { mutableStateOf(false) }

    val recordedCount = pack.lines.count { it.isRecorded }
    val progress = (state.masterPlaybackTimeSeconds / pack.totalDurationSeconds).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBot)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackToLines,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Txt,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = DubStageStrings.backToLines(state.language),
                    fontSize = 12.sp,
                    color = Txt
                )
            }

            Spacer(Modifier.weight(1f))

            Surface(
                color = com.example.dubstage.ui.theme.AccDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.dubstage.ui.theme.EdgeHi)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccHi,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Take: $recordedCount/${pack.lines.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccHi
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = DubStageStrings.finaleTitle(state.language),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = pack.name,
            fontSize = 13.sp,
            color = Dim
        )

        Spacer(Modifier.height(16.dp))

        // Master Scene Video Preview
        ScenePreviewVisualizer(
            sceneType = pack.videoSceneType,
            currentLine = pack.lines.firstOrNull {
                state.masterPlaybackTimeSeconds >= it.startSeconds &&
                        state.masterPlaybackTimeSeconds <= (it.startSeconds + it.durationSeconds)
            },
            isPlaying = state.isPlayingMaster,
            isRecording = false,
            elapsedSeconds = state.masterPlaybackTimeSeconds,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(Modifier.height(14.dp))

        // Running Subtitles Box
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
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.activeRunningCaption.isNotBlank())
                        "“${state.activeRunningCaption}”"
                    else
                        "[ Scene Music / Ambiance Playing ]",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (state.activeRunningCaption.isNotBlank()) Color.White else Dim
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Master Playback Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%02d:%05.2f", (state.masterPlaybackTimeSeconds / 60).toInt(), (state.masterPlaybackTimeSeconds % 60)),
                    fontSize = 11.sp,
                    color = Acc,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = String.format("%02d:%05.2f", (pack.totalDurationSeconds / 60).toInt(), (pack.totalDurationSeconds % 60)),
                    fontSize = 11.sp,
                    color = Dim,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Acc,
                trackColor = PanelHi
            )
        }

        Spacer(Modifier.height(20.dp))

        // Play/Pause Master Full Scene Button
        Button(
            onClick = onTogglePlayMaster,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isPlayingMaster) Red else Acc,
                contentColor = if (state.isPlayingMaster) Color.White else com.example.dubstage.ui.theme.AccDarkest
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = if (state.isPlayingMaster) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (state.isPlayingMaster) DubStageStrings.stop(state.language) else DubStageStrings.playMaster(state.language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // Save as Video / Audio Dub
        Button(
            onClick = {
                onSaveDub()
                isSaved = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaved) com.example.dubstage.ui.theme.AccDark else Panel,
                contentColor = if (isSaved) AccHi else Txt
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSaved) com.example.dubstage.ui.theme.EdgeHi else Edge)
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                contentDescription = null,
                tint = if (isSaved) AccHi else Acc,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isSaved) "Saved to Showcase ✓" else DubStageStrings.saveAsVideo(state.language),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
