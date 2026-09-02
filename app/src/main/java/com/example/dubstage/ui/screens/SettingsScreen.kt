package com.example.dubstage.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import com.example.dubstage.model.AiModelType
import com.example.dubstage.model.ModelDownloadState
import com.example.dubstage.model.ModelStatus
import com.example.dubstage.model.AppThemeMode
import com.example.dubstage.model.DubPack
import com.example.dubstage.model.DubPackFolder
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.model.Language
import com.example.dubstage.ui.theme.Acc
import com.example.dubstage.ui.theme.AccDark
import com.example.dubstage.ui.theme.AccDarkest
import com.example.dubstage.ui.theme.AccHi
import com.example.dubstage.ui.theme.BgBot
import com.example.dubstage.ui.theme.Dim
import com.example.dubstage.ui.theme.DimHi
import com.example.dubstage.ui.theme.Edge
import com.example.dubstage.ui.theme.EdgeHi
import com.example.dubstage.ui.theme.Panel
import com.example.dubstage.ui.theme.PanelHi
import com.example.dubstage.ui.theme.Red
import com.example.dubstage.ui.theme.RedHi
import com.example.dubstage.ui.theme.Teal
import com.example.dubstage.ui.theme.Txt

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    language: Language,
    packs: List<DubPack>,
    folders: List<DubPackFolder>,
    countdownSeconds: Int,
    micGain: Float,
    cacheSizeMb: Float,
    modelsState: Map<AiModelType, ModelDownloadState> = emptyMap(),
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (Language) -> Unit,
    onSetCountdownSeconds: (Int) -> Unit,
    onSetMicGain: (Float) -> Unit,
    onDeletePack: (String) -> Unit,
    onClearAllPacks: () -> Unit,
    onDeleteFolder: (String) -> Unit,
    onOpenFolderSelector: () -> Unit,
    onClearCache: () -> Unit,
    onOpenMicTest: () -> Unit,
    onDownloadModel: (AiModelType) -> Unit = {},
    onCancelModelDownload: (AiModelType) -> Unit = {},
    onDeleteModel: (AiModelType) -> Unit = {},
    onDeleteAllModels: () -> Unit = {}
) {
    var packToDelete by remember { mutableStateOf<DubPack?>(null) }
    var showClearAllPacksDialog by remember { mutableStateOf(false) }
    var showClearCacheSuccess by remember { mutableStateOf(false) }
    var showDeleteAllModelsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AccDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = DubStageStrings.settingsTitle(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manage dub packs, app appearance, storage, and audio hardware",
                        fontSize = 11.sp,
                        color = Dim
                    )
                }
            }
        }

        // 1. App Theme Appearance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Acc,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "APP THEME & APPEARANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Acc
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionTile(
                            title = "Dark Canvas",
                            subtitle = "OLED Black",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == AppThemeMode.DARK,
                            modifier = Modifier.weight(1f),
                            onClick = { onSetThemeMode(AppThemeMode.DARK) }
                        )

                        ThemeOptionTile(
                            title = "Light Studio",
                            subtitle = "High-Contrast",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == AppThemeMode.LIGHT,
                            modifier = Modifier.weight(1f),
                            onClick = { onSetThemeMode(AppThemeMode.LIGHT) }
                        )

                        ThemeOptionTile(
                            title = "System",
                            subtitle = "OS Default",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = themeMode == AppThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f),
                            onClick = { onSetThemeMode(AppThemeMode.SYSTEM) }
                        )
                    }
                }
            }
        }

        // 2. Dub Packs Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "DUB PACKS LIBRARY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Acc
                            )
                        }

                        Surface(
                            color = AccDark,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, EdgeHi)
                        ) {
                            Text(
                                text = "${packs.size} Packs Loaded",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccHi,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (packs.isEmpty()) {
                        Surface(
                            color = BgBot,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Edge),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Dub Packs in Library",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Create a pack from video in DubForge or import storage folders.",
                                    fontSize = 10.sp,
                                    color = Dim,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            packs.forEach { pack ->
                                PackManagementItem(
                                    pack = pack,
                                    onDelete = { packToDelete = pack }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showClearAllPacksDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RedHi
                            ),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = RedHi
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Clear All Dub Packs (${packs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedHi
                            )
                        }
                    }
                }
            }
        }

        // 3. Storage & Folders Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "STORAGE & DIRECTORIES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Acc
                            )
                        }

                        TextButton(
                            onClick = onOpenFolderSelector,
                            colors = ButtonDefaults.textButtonColors(contentColor = AccHi)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        folders.forEach { folder ->
                            Surface(
                                color = BgBot,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Edge),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = Acc,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Txt
                                        )
                                        Text(
                                            text = folder.pathDisplay,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Dim,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (!folder.isDefault) {
                                        IconButton(
                                            onClick = { onDeleteFolder(folder.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove folder",
                                                tint = Dim,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Cache Cleaner Row
                    Surface(
                        color = BgBot,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Edge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Audio Stem & Waveform Cache",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Txt
                                )
                                Text(
                                    text = "Cached buffers: ${String.format("%.1f", cacheSizeMb)} MB",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Dim
                                )
                            }

                            Button(
                                onClick = {
                                    onClearCache()
                                    showClearCacheSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PanelHi,
                                    contentColor = AccHi
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = AccHi
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Clear", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. AI Neural Models & Stem Engine Downloads Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Acc,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = DubStageStrings.aiModelsTitle(language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Acc
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val downloadedCount = modelsState.values.count { it.status == ModelStatus.INSTALLED }
                            Surface(
                                color = AccDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, EdgeHi)
                            ) {
                                Text(
                                    text = "$downloadedCount/${AiModelType.entries.size} Ready",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccHi,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (downloadedCount > 0) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    onClick = { showDeleteAllModelsDialog = true },
                                    color = Color(0xFF3F1515),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, RedHi.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete All Models",
                                            tint = RedHi,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "Delete All",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RedHi
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // GPU Precision Guarantee Note
                    Surface(
                        color = PanelHi,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Edge)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = AccHi,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Hardware GPU FP32 Mode Active: High-precision 32-bit floating point inference avoids lossy INT4 NPU distortion.",
                                fontSize = 9.sp,
                                lineHeight = 12.sp,
                                color = DimHi
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AiModelType.entries.forEach { modelType ->
                            val modelState = modelsState[modelType]
                            if (modelState != null) {
                                AiModelDownloadCardItem(
                                    modelType = modelType,
                                    state = modelState,
                                    language = language,
                                    onDownload = { onDownloadModel(modelType) },
                                    onCancel = { onCancelModelDownload(modelType) },
                                    onDelete = { onDeleteModel(modelType) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Audio & On-Device GPU Hardware Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Acc,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AUDIO & HARDWARE GPU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Acc
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // GPU Accelerator Status Tile
                    Surface(
                        color = BgBot,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Edge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neural Stem Engine",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Txt
                                )
                                Surface(
                                    color = Color(0xFF052e16),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color(0xFF059669))
                                ) {
                                    Text(
                                        text = "On-Device GPU Active",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Demucs v4 STFT Hybrid Convolution with Vulkan FP16 / Android NNAPI zero-cloud execution.",
                                fontSize = 9.sp,
                                lineHeight = 13.sp,
                                color = Dim
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Countdown Length Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Dim,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Recording Countdown Timer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Txt
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3, 5).forEach { sec ->
                                val isSelected = countdownSeconds == sec
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSetCountdownSeconds(sec) },
                                    color = if (isSelected) AccDark else BgBot,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isSelected) Acc else Edge)
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AccHi else Dim,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Mic Gain Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Dim,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Microphone Input Sensitivity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Txt
                            )
                        }
                        Text(
                            text = String.format("%.1fx", micGain),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccHi,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = micGain,
                        onValueChange = onSetMicGain,
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Acc,
                            activeTrackColor = Acc,
                            inactiveTrackColor = PanelHi
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick = onOpenMicTest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PanelHi,
                            contentColor = AccHi
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Launch Mic Hardware Diagnostic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Language Selection Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Acc,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "LANGUAGE & LOCALIZATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Acc
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Language.values().forEach { lang ->
                            val isSelected = language == lang
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSetLanguage(lang) },
                                color = if (isSelected) AccDark else BgBot,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) Acc else Edge)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (lang == Language.EN) "🇺🇸 English" else "🇮🇳 മലയാളം",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AccHi else Dim
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. About DubStage Pro
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Edge)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Dim,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "ABOUT DUBSTAGE PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Dim
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "DubStage Pro v2.4.0 — High-Performance Voice Dubbing Studio",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Real-time zero-latency audio synthesizer, Demucs on-device GPU stem isolation, and CapCut-style multi-track timeline.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = Dim
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Delete single pack confirmation dialog
    packToDelete?.let { pack ->
        AlertDialog(
            onDismissRequest = { packToDelete = null },
            title = {
                Text(
                    text = "Delete Dub Pack?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${pack.name}\" (${pack.lines.size} lines)?",
                    color = Dim,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePack(pack.id)
                        packToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { packToDelete = null }) {
                    Text("Cancel", color = Dim)
                }
            },
            containerColor = Panel,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clear all packs confirmation dialog
    if (showClearAllPacksDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllPacksDialog = false },
            title = {
                Text(
                    text = "Clear All Dub Packs?",
                    fontWeight = FontWeight.Bold,
                    color = RedHi,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This will remove all ${packs.size} dub packs from your current session library. You can always re-import or build new packs anytime.",
                    color = Dim,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllPacks()
                        showClearAllPacksDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllPacksDialog = false }) {
                    Text("Cancel", color = Dim)
                }
            },
            containerColor = Panel,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteAllModelsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllModelsDialog = false },
            title = {
                Text(
                    "Delete All AI Models?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    "This will remove all downloaded neural models (including full-weight FP32 Demucs and Silero VAD) from local storage. You can re-download them at any time.",
                    color = Dim,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllModels()
                        showDeleteAllModelsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllModelsDialog = false }) {
                    Text("Cancel", color = Dim)
                }
            },
            containerColor = Panel,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ThemeOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) AccDark else BgBot,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Acc else Edge)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AccHi else Dim,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Txt,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = if (isSelected) AccHi else Dim,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PackManagementItem(
    pack: DubPack,
    onDelete: () -> Unit
) {
    Surface(
        color = BgBot,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Edge),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = PanelHi,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Acc,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pack.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pack.lines.size} Lines",
                        fontSize = 9.sp,
                        color = AccHi,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "•", fontSize = 9.sp, color = Dim)
                    Text(
                        text = String.format("%.1fs", pack.totalDurationSeconds),
                        fontSize = 9.sp,
                        color = Dim
                    )
                    if (pack.videoFileName != null) {
                        Text(text = "•", fontSize = 9.sp, color = Dim)
                        Text(
                            text = "Video Clip",
                            fontSize = 9.sp,
                            color = Teal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Pack",
                    tint = RedHi,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AiModelDownloadCardItem(
    modelType: AiModelType,
    state: ModelDownloadState,
    language: Language,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val isFullWeight = state.info.isFullWeight

    Surface(
        color = if (isFullWeight) Color(0xFF141A28) else Panel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isFullWeight) Acc.copy(alpha = 0.6f) else Edge),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isFullWeight) AccDark else PanelHi,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (modelType) {
                                    AiModelType.HTDEMUCS_FT_FULL -> Icons.Default.GraphicEq
                                    AiModelType.HTDEMUCS_FT -> Icons.Default.GraphicEq
                                    AiModelType.SILERO_VAD -> Icons.Default.Mic
                                    AiModelType.PITCH_TIMBRE_ENGINE -> Icons.Default.Tune
                                },
                                contentDescription = null,
                                tint = if (isFullWeight) AccHi else Acc,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.info.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isFullWeight) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = AccDark,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Acc)
                                ) {
                                    Text(
                                        text = "FULL FP32",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccHi,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${state.info.version} • ${state.info.formattedSize} • ${state.info.precision}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isFullWeight) AccHi else Dim
                        )
                    }
                }

                // Action / Status Badge and Delete Action
                when (state.status) {
                    ModelStatus.INSTALLED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF052e16),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF059669))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isFullWeight) "Ready (FP32 GPU)" else "Ready",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }

                            Spacer(Modifier.width(4.dp))

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Model",
                                    tint = RedHi,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                    ModelStatus.DOWNLOADING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancel Download",
                                    tint = RedHi,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    ModelStatus.NOT_DOWNLOADED, ModelStatus.ERROR -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onDownload,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFullWeight) Acc else AccDark,
                                    contentColor = if (isFullWeight) Color.Black else AccHi
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = if (isFullWeight) 10.dp else 8.dp,
                                    vertical = 4.dp
                                ),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isFullWeight) Color.Black else AccHi
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (state.status == ModelStatus.ERROR) {
                                        "Retry"
                                    } else if (isFullWeight) {
                                        "Download Full Weight"
                                    } else {
                                        "Download"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(3.dp))

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Model Cache",
                                    tint = Dim,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = state.info.description,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = Dim
            )

            // Progress bar when downloading
            if (state.status == ModelStatus.DOWNLOADING) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DubStageStrings.downloadingModel(language, state.progressPercent),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccHi
                    )
                    Text(
                        text = "${state.downloadedMbFormatted} / ${state.totalMbFormatted} (${state.speedFormatted})",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Dim
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Acc,
                    trackColor = PanelHi,
                )
            } else if (state.status == ModelStatus.ERROR) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Download failed: ${state.statusMessage}",
                    fontSize = 9.sp,
                    color = RedHi
                )
            }
        }
    }
}
