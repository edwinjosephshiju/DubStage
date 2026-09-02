package com.example.dubstage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dubstage.model.Language
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.ui.theme.*
import com.example.dubstage.viewmodel.MicTestState

@Composable
fun MicTestDialog(
    state: MicTestState,
    liveDb: Float,
    language: Language,
    onStartTest: () -> Unit,
    onClose: () -> Unit
) {
    if (!state.isOpen) return

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Panel),
            border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Acc,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = DubStageStrings.microphone(language) + " Test",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Dim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Records a 2-second voice snippet, measures audio dB level, and plays it back to verify your microphone calibration.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Dim
                )

                Spacer(Modifier.height(18.dp))

                // Live Level Progress
                val dbNorm = ((liveDb + 60f) / 60f).coerceIn(0f, 1f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE VU LEVEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Dim
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = String.format("%.0f dB", liveDb),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = if (liveDb > -30f) Acc else Gold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (state.isTesting) state.progress else dbNorm },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (state.isTesting) Red else Acc,
                        trackColor = PanelHi
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (state.resultDb != null) {
                    val db = state.resultDb
                    val isGood = db > -35f && db < -2f
                    Surface(
                        color = if (isGood) AccDark else Color(0x33FFC861),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isGood) EdgeHi else Edge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isGood) DubStageStrings.micGood(language, db)
                            else DubStageStrings.micTooQuiet(language, db),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGood) AccHi else Gold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Button(
                    onClick = onStartTest,
                    enabled = !state.isTesting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isTesting) Red else Acc,
                        contentColor = if (state.isTesting) Color.White else AccDarkest
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = if (state.isTesting) "Recording test sample..." else "Start 2s Mic Test",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
