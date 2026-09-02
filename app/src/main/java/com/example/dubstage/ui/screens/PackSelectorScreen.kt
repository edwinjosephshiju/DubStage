package com.example.dubstage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dubstage.model.DubPack
import com.example.dubstage.model.Language
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.ui.theme.*

import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import com.example.dubstage.model.DubPackFolder
import com.example.dubstage.ui.components.FolderSelectorDialog

@Composable
fun PackSelectorScreen(
    packs: List<DubPack>,
    language: Language,
    folders: List<DubPackFolder> = emptyList(),
    selectedFolderId: String? = null,
    isFolderSelectorOpen: Boolean = false,
    onOpenFolderSelector: () -> Unit = {},
    onCloseFolderSelector: () -> Unit = {},
    onSelectFolder: (String?) -> Unit = {},
    onImportFolder: (name: String, path: String, uriString: String?) -> Unit = { _, _, _ -> },
    onSelectPack: (DubPack) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isFolderSelectorOpen) {
        FolderSelectorDialog(
            folders = folders,
            selectedFolderId = selectedFolderId,
            onSelectFolder = onSelectFolder,
            onImportFolder = onImportFolder,
            onDismiss = onCloseFolderSelector
        )
    }

    val filteredPacks = if (selectedFolderId == null) {
        packs
    } else {
        packs.filter { it.folderId == selectedFolderId || (selectedFolderId == "default_internal" && !it.isCustom) }
    }

    val activeFolderName = folders.firstOrNull { it.id == selectedFolderId }?.name ?: "All Folders"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBot)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Sleek Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Panel)
                    .border(1.dp, Edge, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STUDIO SELECTION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp,
                                color = Acc
                            )
                            Text(
                                text = DubStageStrings.title(language),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PanelHi,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccHi)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Acc
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Dub scenes line by line. Match the original timing curve on the waveform comparison strip, then hear the entire scene play back with your voice!",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Dim
                    )
                }
            }
        }

        item {
            // Folder Selector Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FOLDER:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Dim,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = PanelHi,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge),
                        modifier = Modifier.clickable { onOpenFolderSelector() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = activeFolderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Change Folder Button
                Button(
                    onClick = onOpenFolderSelector,
                    colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = AccHi),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EdgeHi),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Select Folder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "${DubStageStrings.pickPack(language).uppercase()} (${filteredPacks.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = Dim,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 4.dp)
            )
        }

        if (filteredPacks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = BgBot,
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = AccHi,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "No Dub Packs Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Import a storage folder containing scene dub packs, or use the DubForge tab to import a local video and generate new dub packs using GPU Demucs.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Dim,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onOpenFolderSelector,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Acc,
                                contentColor = AccDarkest
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Open Folder Selector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredPacks) { pack ->
                PackCardItem(
                    pack = pack,
                    language = language,
                    onClick = { onSelectPack(pack) }
                )
            }
        }
    }
}

@Composable
fun PackCardItem(
    pack: DubPack,
    language: Language,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = com.example.dubstage.ui.theme.AccDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.dubstage.ui.theme.EdgeHi)
                ) {
                    Text(
                        text = pack.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AccHi
                    )
                }

                Spacer(Modifier.weight(1f))

                if (pack.hasBackingTrack) {
                    Surface(
                        color = PanelHi,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = DubStageStrings.withBacking(language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Acc
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = pack.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = pack.description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Dim
            )

            Spacer(Modifier.height(16.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PanelHi,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = Acc,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = DubStageStrings.linesCount(language, pack.lines.size),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Txt
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PanelHi,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Edge)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Dim,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format("%.0fs", pack.totalDurationSeconds),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Dim
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Acc,
                        contentColor = com.example.dubstage.ui.theme.AccDarkest
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = DubStageStrings.startDubbing(language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
