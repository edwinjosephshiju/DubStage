package com.example.dubstage.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dubstage.model.DubStageStrings
import com.example.dubstage.model.Language
import com.example.dubstage.ui.components.MicTestDialog
import com.example.dubstage.ui.screens.DubForgeScreen
import com.example.dubstage.ui.screens.DubStageStudioScreen
import com.example.dubstage.ui.screens.FinaleScreen
import com.example.dubstage.ui.screens.MyDubsScreen
import com.example.dubstage.ui.screens.PackSelectorScreen
import com.example.dubstage.ui.screens.SettingsScreen
import com.example.dubstage.ui.theme.*
import com.example.dubstage.viewmodel.AppTab
import com.example.dubstage.viewmodel.DubStageViewModel
import com.example.dubstage.viewmodel.StudioPhase
import com.example.ui.theme.MyApplicationTheme

@Composable
fun DubStageApp(
    viewModel: DubStageViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Audio record permission requester
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    MyApplicationTheme(themeMode = state.themeMode) {
        Scaffold(
            topBar = {
                if (state.studioPhase != StudioPhase.LINE_RECORDING) {
                    DubStageTopBar(
                        language = state.language
                    )
                }
            },
            bottomBar = {
                if (state.studioPhase != StudioPhase.LINE_RECORDING) {
                    DubStageBottomBar(
                        currentTab = state.currentTab,
                        language = state.language,
                        dubsCount = state.myDubs.size,
                        onSelectTab = { tab ->
                            viewModel.switchTab(tab)
                        }
                    )
                }
            },
            containerColor = BgBot
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (state.currentTab) {
                    AppTab.DUB_STAGE -> {
                        when (state.studioPhase) {
                            StudioPhase.PACK_SELECTOR -> {
                                PackSelectorScreen(
                                    packs = state.packs,
                                    language = state.language,
                                    folders = state.folders,
                                    selectedFolderId = state.selectedFolderId,
                                    isFolderSelectorOpen = state.isFolderSelectorOpen,
                                    onOpenFolderSelector = { viewModel.openFolderSelector() },
                                    onCloseFolderSelector = { viewModel.closeFolderSelector() },
                                    onSelectFolder = { viewModel.selectFolder(it) },
                                    onImportFolder = { name, path, uri -> viewModel.importPacksFolder(name, path, uri) },
                                    onSelectPack = { pack ->
                                        if (!hasAudioPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        viewModel.selectPack(pack)
                                    }
                                )
                            }
                            StudioPhase.LINE_RECORDING -> {
                                DubStageStudioScreen(
                                    state = state,
                                    onExit = { viewModel.exitToPackSelector() },
                                    onSelectLine = { viewModel.navigateToLine(it) },
                                    onPlayOriginal = { viewModel.playOriginalLine() },
                                    onPlayTake = { viewModel.playUserTake() },
                                    onStartRecording = {
                                        if (!hasAudioPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            viewModel.startRecordingFlow()
                                        }
                                    },
                                    onStopRecording = { viewModel.stopRecordingAndSaveTake() },
                                    onLeaveLineEmpty = { viewModel.leaveLineEmpty() },
                                    onPrevLine = { viewModel.prevLine() },
                                    onNextLine = { viewModel.nextLine() },
                                    onProceedToFinale = { viewModel.proceedToFinale() }
                                )
                            }
                            StudioPhase.FINALE_MASTER -> {
                                FinaleScreen(
                                    state = state,
                                    onTogglePlayMaster = { viewModel.togglePlayMasterScene() },
                                    onSaveDub = { viewModel.saveCompletedDub() },
                                    onBackToLines = { viewModel.backToLinesFromFinale() }
                                )
                            }
                        }
                    }
                    AppTab.DUB_FORGE -> {
                        DubForgeScreen(
                            state = state,
                            onTitleChange = { viewModel.updateForgeTitle(it) },
                            onDescriptionChange = { viewModel.updateForgeDescription(it) },
                            onSensitivityChange = { viewModel.updateForgeSensitivity(it) },
                            onSelectSegment = { viewModel.selectForgeSegment(it) },
                            onScrubPlayhead = { viewModel.scrubForgePlayhead(it) },
                            onToggleTimelinePlay = { viewModel.toggleForgeTimelinePlayback() },
                            onSplitSegment = { viewModel.splitForgeSegment(it) },
                            onDeleteSegment = { viewModel.deleteForgeSegment(it) },
                            onAddSegment = { viewModel.addForgeSegment() },
                            onImportVideo = { uri, name -> viewModel.importLocalVideo(uri, name) },
                            onRunDemucsSeparation = { viewModel.runDemucsSeparation() },
                            onBuildPack = { viewModel.buildNewPackFromForge() },
                            onDismissMessage = { viewModel.dismissForgeMessage() },
                            onNudgeSegmentStart = { index, delta -> viewModel.nudgeForgeSegmentStart(index, delta) },
                            onNudgeSegmentEnd = { index, delta -> viewModel.nudgeForgeSegmentEnd(index, delta) },
                            onMergeSegment = { index -> viewModel.mergeForgeSegments(index) },
                            onPlaySegmentSlice = { index -> viewModel.playForgeSegmentSlice(index, loop = false) },
                            onLoopSegmentSlice = { index -> viewModel.playForgeSegmentSlice(index, loop = true) },
                            onStopSegmentSlice = { viewModel.toggleForgeTimelinePlayback() },
                            onUpdateSegmentLabel = { index, label -> viewModel.updateForgeSegmentTiming(index, label = label) },
                            onToggleLoopClip = { viewModel.toggleForgeLoopClip() },
                            onSetStemMode = { viewModel.setForgeStemMode(it) },
                            onToggleMuteVocals = { viewModel.toggleForgeMuteVocals() },
                            onToggleMuteBgm = { viewModel.toggleForgeMuteBgm() }
                        )
                    }
                    AppTab.MY_DUBS -> {
                        MyDubsScreen(
                            dubs = state.myDubs,
                            playingDubId = state.playingDubId,
                            language = state.language,
                            onPlayDub = { viewModel.playSavedDub(it) },
                            onNavigateToStage = {
                                viewModel.switchTab(AppTab.DUB_STAGE)
                                viewModel.exitToPackSelector()
                            }
                        )
                    }
                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            themeMode = state.themeMode,
                            language = state.language,
                            packs = state.packs,
                            folders = state.folders,
                            countdownSeconds = state.countdownDurationSeconds,
                            micGain = state.micInputGain,
                            cacheSizeMb = state.cacheSizeMb,
                            modelsState = state.modelsState,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            onSetLanguage = { viewModel.setLanguage(it) },
                            onSetCountdownSeconds = { viewModel.setCountdownDuration(it) },
                            onSetMicGain = { viewModel.setMicGain(it) },
                            onDeletePack = { viewModel.deletePack(it) },
                            onClearAllPacks = { viewModel.clearAllPacks() },
                            onDeleteFolder = { viewModel.deleteFolder(it) },
                            onOpenFolderSelector = { viewModel.openFolderSelector() },
                            onClearCache = { viewModel.clearAudioCache() },
                            onOpenMicTest = {
                                if (!hasAudioPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                                viewModel.openMicTest()
                            },
                            onDownloadModel = { viewModel.downloadAiModel(it) },
                            onCancelModelDownload = { viewModel.cancelAiModelDownload(it) },
                            onDeleteModel = { viewModel.deleteAiModel(it) },
                            onDeleteAllModels = { viewModel.deleteAllAiModels() }
                        )
                    }
                }

                // Mic Test Dialog
                MicTestDialog(
                    state = state.micTestState,
                    liveDb = state.liveMicDb,
                    language = state.language,
                    onStartTest = { viewModel.runMicTest() },
                    onClose = { viewModel.closeMicTest() }
                )
            }
        }
    }
}

/**
 * Floating Capsule Top Bar
 * Centered floating pill containing the App Brand and Badge
 */
@Composable
fun DubStageTopBar(
    language: Language,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Panel.copy(alpha = 0.95f),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, EdgeHi),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = AccDark,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = AccHi,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (language == Language.ML) "ഡബ്ബ് സ്റ്റേജ്" else "DubStage",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    color = Acc.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Acc.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "PRO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccHi,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Floating Capsule Bottom Bar
 * Modern floating pill dock with animated item selections
 */
@Composable
fun DubStageBottomBar(
    currentTab: AppTab,
    language: Language,
    dubsCount: Int,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = SurfaceDark.copy(alpha = 0.96f),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, EdgeHi),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FloatingTabItem(
                    selected = currentTab == AppTab.DUB_STAGE,
                    label = DubStageStrings.tabDubStage(language),
                    icon = Icons.Default.Movie,
                    onClick = { onSelectTab(AppTab.DUB_STAGE) }
                )

                FloatingTabItem(
                    selected = currentTab == AppTab.DUB_FORGE,
                    label = DubStageStrings.tabDubForge(language),
                    icon = Icons.Default.ContentCut,
                    onClick = { onSelectTab(AppTab.DUB_FORGE) }
                )

                FloatingTabItem(
                    selected = currentTab == AppTab.MY_DUBS,
                    label = DubStageStrings.tabMyDubs(language),
                    icon = Icons.Default.VideoLibrary,
                    badgeCount = dubsCount,
                    onClick = { onSelectTab(AppTab.MY_DUBS) }
                )

                FloatingTabItem(
                    selected = currentTab == AppTab.SETTINGS,
                    label = DubStageStrings.tabSettings(language),
                    icon = Icons.Default.Settings,
                    onClick = { onSelectTab(AppTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun FloatingTabItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (selected) AccDark else Color.Transparent,
        label = "tab_bg"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) AccHi else Dim,
        label = "tab_content"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = animatedBg,
        shape = RoundedCornerShape(24.dp),
        border = if (selected) BorderStroke(1.dp, Acc.copy(alpha = 0.45f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Acc,
                            contentColor = AccDarkest
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = animatedContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = animatedContentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (selected) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
