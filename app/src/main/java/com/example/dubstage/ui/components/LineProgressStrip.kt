package com.example.dubstage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.dubstage.ui.theme.Teal
import com.example.dubstage.ui.theme.Txt

@Composable
fun LineProgressStrip(
    lines: List<DubLine>,
    currentIndex: Int,
    language: Language,
    onSelectLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val recordedCount = lines.count { it.isRecorded }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DubStageStrings.recordedNOfM(language, recordedCount, lines.size).uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Dim
            )

            Text(
                text = DubStageStrings.lineOf(language, currentIndex + 1, lines.size),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = com.example.dubstage.ui.theme.Acc
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(lines) { index, line ->
                val isCurrent = index == currentIndex
                val isRecorded = line.isRecorded

                val backgroundColor = when {
                    isCurrent -> com.example.dubstage.ui.theme.AccHi
                    isRecorded -> com.example.dubstage.ui.theme.AccDark
                    else -> Panel
                }

                val textColor = when {
                    isCurrent -> com.example.dubstage.ui.theme.AccDarkest
                    isRecorded -> com.example.dubstage.ui.theme.AccHi
                    else -> Dim
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectLine(index) },
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = when {
                            isCurrent -> com.example.dubstage.ui.theme.Acc
                            isRecorded -> com.example.dubstage.ui.theme.EdgeHi
                            else -> Edge
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecorded) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Recorded",
                                tint = textColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = "%02d".format(index + 1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
