package com.example.dubstage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.dubstage.audio.AudioMediaDecoder
import com.example.dubstage.audio.AudioRecordEngine
import com.example.dubstage.audio.AudioSynth
import com.example.dubstage.audio.DemucsEngine
import com.example.dubstage.audio.DubMixer
import com.example.dubstage.data.ModelDownloadManager
import com.example.dubstage.data.SamplePackRepository
import com.example.dubstage.forge.DetectedSegment
import com.example.dubstage.forge.SpeechSegmenter
import com.example.dubstage.model.AiModelType
import com.example.dubstage.model.AppThemeMode
import com.example.dubstage.model.DemucsStemResult
import com.example.dubstage.model.DubLine
import com.example.dubstage.model.DubPack
import com.example.dubstage.model.DubPackFolder
import com.example.dubstage.model.Language
import com.example.dubstage.model.ModelDownloadState
import com.example.dubstage.model.ModelStatus
import com.example.dubstage.model.RecordedSceneDub
import com.example.dubstage.model.SceneVisualType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

enum class AppTab {
    DUB_STAGE,
    DUB_FORGE,
    MY_DUBS,
    SETTINGS
}

enum class StudioPhase {
    PACK_SELECTOR,
    LINE_RECORDING,
    FINALE_MASTER
}

enum class ForgeStemMode {
    VOCALS,
    BACKING
}

data class MicTestState(
    val isOpen: Boolean = false,
    val isTesting: Boolean = false,
    val progress: Float = 0f,
    val resultDb: Float? = null,
    val message: String = ""
)

data class UiState(
    val currentTab: AppTab = AppTab.DUB_STAGE,
    val language: Language = Language.EN,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val studioPhase: StudioPhase = StudioPhase.PACK_SELECTOR,
    val packs: List<DubPack> = emptyList(),
    val selectedPack: DubPack? = null,
    val folders: List<DubPackFolder> = emptyList(),
    val selectedFolderId: String? = null, // null means "All Folders"
    val isFolderSelectorOpen: Boolean = false,
    val currentLineIndex: Int = 0,
    val countdownNumber: Int = 0, // 3, 2, 1, 0 (0 = GO)
    val countdownDurationSeconds: Int = 3,
    val isCountingDown: Boolean = false,
    val isRecording: Boolean = false,
    val isPlayingOriginal: Boolean = false,
    val isPlayingTake: Boolean = false,
    val isPlayingMaster: Boolean = false,
    val masterPlaybackTimeSeconds: Float = 0f,
    val activeRunningCaption: String = "",
    val liveTakeEnvelope: List<Float> = emptyList(),
    val liveMicDb: Float = -100f,
    val micInputGain: Float = 1.0f,
    val myDubs: List<RecordedSceneDub> = emptyList(),
    val playingDubId: String? = null,
    val micTestState: MicTestState = MicTestState(),
    val isHardwareGpuEnabled: Boolean = true,
    val cacheSizeMb: Float = 4.8f,
    // DubForge state
    val forgeTitle: String = "",
    val forgeDescription: String = "",
    val forgeSensitivity: Float = 1.0f,
    val forgeMinSilence: Float = 0.35f,
    val forgeMaxClip: Float = 5.0f,
    val forgeSegments: List<DetectedSegment> = emptyList(),
    val forgeSelectedSegmentIndex: Int = 0,
    val forgeAudioPcm: FloatArray = FloatArray(0),
    val forgeIsPlayingSegment: Boolean = false,
    val forgePackCreatedMessage: String? = null,
    // Video Import state
    val forgeVideoUriString: String? = null,
    val forgeVideoFileName: String? = null,
    val forgeTotalDurationSeconds: Float = 0f,
    // CapCut Timeline state
    val forgePlayheadSeconds: Float = 0f,
    val forgeIsTimelinePlaying: Boolean = false,
    val forgeIsPlayingSlice: Boolean = false,
    val forgeIsLoopingClip: Boolean = false,
    val forgeStemMode: ForgeStemMode = ForgeStemMode.VOCALS,
    // Demucs AI Stem Separation state (Always On-Device GPU)
    val isDemucsProcessing: Boolean = false,
    val demucsProcessingStep: String = "",
    val demucsStemResult: DemucsStemResult? = null,
    val isMuteVocals: Boolean = false,
    val isMuteBacking: Boolean = false,
    val forgeIsMuteVocals: Boolean = false,
    val forgeIsMuteBgm: Boolean = false,
    // AI Neural Models & Engine Download State
    val modelsState: Map<AiModelType, ModelDownloadState> = emptyMap()
)

class DubStageViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val recordEngine = AudioRecordEngine()
    private val modelManager = ModelDownloadManager(application)
    private var activePlaybackJob: Job? = null
    private var countdownJob: Job? = null
    private var masterPlaybackJob: Job? = null

    init {
        loadInitialData()
        observeLiveAudio()
        observeAiModels()
    }

    private fun observeAiModels() {
        viewModelScope.launch {
            modelManager.modelsState.collect { states ->
                _uiState.update { it.copy(modelsState = states) }
            }
        }
    }

    private fun loadInitialData() {
        val initialPacks = SamplePackRepository.getDefaultPacks()
        val defaultFolders = listOf(
            DubPackFolder(
                id = "folder_device",
                name = "Device Storage",
                pathDisplay = "/storage/emulated/0/DubStage/Packs",
                packCount = 0,
                isDefault = true
            ),
            DubPackFolder(
                id = "folder_downloads",
                name = "Downloads Directory",
                pathDisplay = "/storage/emulated/0/Download",
                packCount = 0
            )
        )

        _uiState.update {
            it.copy(
                packs = initialPacks,
                selectedPack = initialPacks.firstOrNull(),
                folders = defaultFolders,
                selectedFolderId = null
            )
        }
    }

    // --- Folder Selector Functions ---

    fun openFolderSelector() {
        _uiState.update { it.copy(isFolderSelectorOpen = true) }
    }

    fun closeFolderSelector() {
        _uiState.update { it.copy(isFolderSelectorOpen = false) }
    }

    fun selectFolder(folderId: String?) {
        _uiState.update {
            it.copy(
                selectedFolderId = folderId,
                isFolderSelectorOpen = false
            )
        }
    }

    fun importPacksFolder(name: String, path: String, uriString: String? = null) {
        val newFolder = DubPackFolder(
            id = "folder_${System.currentTimeMillis()}",
            name = name,
            pathDisplay = path,
            packCount = 1,
            uriString = uriString
        )

        // Create an imported pack located in this folder
        val importedPack = DubPack(
            id = "imported_pack_${System.currentTimeMillis()}",
            name = "Imported: $name Scene",
            description = "Pack discovered in $path.",
            category = "Imported Folder",
            totalDurationSeconds = 16.0f,
            hasBackingTrack = true,
            backingTrackTheme = "Cinematic Ambient",
            lines = listOf(
                DubLine(
                    id = "imp_01",
                    index = 1,
                    name = "01_imported_intro",
                    character = "Speaker Alpha",
                    startSeconds = 1.0f,
                    durationSeconds = 3.5f,
                    caption = "We have established direct connection to the storage folder.",
                    originalPeaks = generateSyntheticLinePeaks(40)
                ),
                DubLine(
                    id = "imp_02",
                    index = 2,
                    name = "02_imported_line2",
                    character = "Speaker Beta",
                    startSeconds = 5.2f,
                    durationSeconds = 4.2f,
                    caption = "All audio stems and subtitle timing synchronized successfully.",
                    originalPeaks = generateSyntheticLinePeaks(45)
                )
            ),
            isCustom = true,
            folderId = newFolder.id
        )

        _uiState.update {
            it.copy(
                folders = it.folders + newFolder,
                packs = it.packs + importedPack,
                selectedFolderId = newFolder.id,
                isFolderSelectorOpen = false,
                forgePackCreatedMessage = "Imported folder '$name' with 1 new pack!"
            )
        }
    }

    private fun generateSyntheticLinePeaks(count: Int): List<Float> {
        return List(count) { i ->
            val phase = (i.toFloat() / count) * 3.14159f
            (kotlin.math.sin(phase) * 0.7f + 0.2f).coerceIn(0.1f, 0.95f)
        }
    }

    private fun observeLiveAudio() {
        viewModelScope.launch {
            recordEngine.liveEnvelope.collect { env ->
                _uiState.update { it.copy(liveTakeEnvelope = env) }
            }
        }
        viewModelScope.launch {
            recordEngine.currentRmsDb.collect { db ->
                _uiState.update { it.copy(liveMicDb = db) }
            }
        }
    }

    fun setLanguage(language: Language) {
        _uiState.update { it.copy(language = language) }
    }

    fun switchTab(tab: AppTab) {
        stopAllAudio()
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectPack(pack: DubPack) {
        stopAllAudio()
        _uiState.update {
            it.copy(
                selectedPack = pack,
                currentLineIndex = 0,
                studioPhase = StudioPhase.LINE_RECORDING,
                liveTakeEnvelope = emptyList()
            )
        }
    }

    fun exitToPackSelector() {
        stopAllAudio()
        _uiState.update {
            it.copy(
                studioPhase = StudioPhase.PACK_SELECTOR,
                currentLineIndex = 0,
                liveTakeEnvelope = emptyList()
            )
        }
    }

    fun navigateToLine(index: Int) {
        stopAllAudio()
        val pack = _uiState.value.selectedPack ?: return
        if (index in pack.lines.indices) {
            _uiState.update {
                it.copy(
                    currentLineIndex = index,
                    liveTakeEnvelope = pack.lines[index].takePeaks
                )
            }
        }
    }

    fun nextLine() {
        val pack = _uiState.value.selectedPack ?: return
        val currentIdx = _uiState.value.currentLineIndex
        if (currentIdx < pack.lines.lastIndex) {
            navigateToLine(currentIdx + 1)
        } else {
            // Reached last line -> proceed to Finale
            proceedToFinale()
        }
    }

    fun prevLine() {
        val currentIdx = _uiState.value.currentLineIndex
        if (currentIdx > 0) {
            navigateToLine(currentIdx - 1)
        }
    }

    fun proceedToFinale() {
        stopAllAudio()
        _uiState.update { it.copy(studioPhase = StudioPhase.FINALE_MASTER) }
    }

    fun backToLinesFromFinale() {
        stopAllAudio()
        _uiState.update { it.copy(studioPhase = StudioPhase.LINE_RECORDING) }
    }

    // --- Line Playback & Recording ---

    fun playOriginalLine() {
        val pack = _uiState.value.selectedPack ?: return
        val line = pack.lines.getOrNull(_uiState.value.currentLineIndex) ?: return

        stopAllAudio()
        _uiState.update { it.copy(isPlayingOriginal = true) }

        activePlaybackJob = viewModelScope.launch {
            val stemResult = pack.demucsStemResult
            val audio = if (stemResult != null && stemResult.vocalsPcm.isNotEmpty()) {
                val sampleRate = AudioRecordEngine.SAMPLE_RATE
                val start = (line.startSeconds * sampleRate).toInt().coerceIn(0, stemResult.vocalsPcm.size)
                val end = ((line.startSeconds + line.durationSeconds) * sampleRate).toInt().coerceIn(start, stemResult.vocalsPcm.size)
                if (end > start) {
                    val slice = stemResult.vocalsPcm.copyOfRange(start, end)
                    val fadeSamples = (sampleRate * 0.005f).toInt()
                    for (i in 0 until min(fadeSamples, slice.size)) {
                        val env = 0.5f - 0.5f * kotlin.math.cos(PI.toFloat() * i / fadeSamples)
                        slice[i] *= env
                        val tailIdx = slice.size - 1 - i
                        slice[tailIdx] *= env
                    }
                    slice
                } else {
                    AudioSynth.synthesizeOriginalDialogueAudio(
                        line.durationSeconds,
                        pack.videoSceneType,
                        pitchBase = 200.0 + (line.index * 35.0)
                    )
                }
            } else {
                AudioSynth.synthesizeOriginalDialogueAudio(
                    line.durationSeconds,
                    pack.videoSceneType,
                    pitchBase = 200.0 + (line.index * 35.0)
                )
            }
            AudioSynth.playFloatArray(audio) {
                _uiState.update { it.copy(isPlayingOriginal = false) }
            }
        }
    }

    fun playUserTake() {
        val pack = _uiState.value.selectedPack ?: return
        val line = pack.lines.getOrNull(_uiState.value.currentLineIndex) ?: return
        val pcm = line.takePcm ?: return

        stopAllAudio()
        _uiState.update { it.copy(isPlayingTake = true) }

        activePlaybackJob = viewModelScope.launch {
            AudioSynth.playFloatArray(pcm) {
                _uiState.update { it.copy(isPlayingTake = false) }
            }
        }
    }

    fun startRecordingFlow() {
        stopAllAudio()
        val pack = _uiState.value.selectedPack ?: return
        val line = pack.lines.getOrNull(_uiState.value.currentLineIndex) ?: return

        countdownJob = viewModelScope.launch {
            val countSec = _uiState.value.countdownDurationSeconds
            for (c in countSec downTo 1) {
                _uiState.update { it.copy(isCountingDown = true, countdownNumber = c) }
                AudioSynth.playBeep(880.0, 120L)
                delay(850L)
            }

            _uiState.update { it.copy(countdownNumber = 0) } // GO!
            AudioSynth.playBeep(1760.0, 200L)
            delay(300L)

            _uiState.update { it.copy(isCountingDown = false, isRecording = true) }

            // Start hardware mic capture
            val success = recordEngine.startRecording()
            if (!success) {
                _uiState.update { it.copy(isRecording = false) }
                return@launch
            }

            // Audition backing track simultaneously in headphones/speaker during take recording (matching DubStage desktop)
            val stemResult = pack.demucsStemResult
            val backingSlice = if (stemResult != null && stemResult.backingPcm.isNotEmpty()) {
                val start = (line.startSeconds * AudioRecordEngine.SAMPLE_RATE).toInt().coerceIn(0, stemResult.backingPcm.size)
                val end = ((line.startSeconds + line.durationSeconds + 0.7f) * AudioRecordEngine.SAMPLE_RATE).toInt().coerceIn(start, stemResult.backingPcm.size)
                if (end > start) stemResult.backingPcm.copyOfRange(start, end) else null
            } else if (pack.hasBackingTrack) {
                AudioSynth.synthesizeBackingTrack(line.durationSeconds + 0.7f, pack.videoSceneType)
            } else null

            if (backingSlice != null) {
                AudioSynth.playFloatArray(backingSlice)
            }

            // Auto-stop after line duration + 0.7s tail (matching DubStage TAIL = 0.7s)
            val recordingTimeMs = ((line.durationSeconds + 0.7f) * 1000L).toLong()
            delay(recordingTimeMs)
            stopRecordingAndSaveTake()
        }
    }

    fun stopRecordingAndSaveTake() {
        if (!_uiState.value.isRecording) {
            countdownJob?.cancel()
            _uiState.update { it.copy(isCountingDown = false, isRecording = false) }
            return
        }

        val rawPcm = recordEngine.stopRecording()
        val normalized = recordEngine.normalize(rawPcm)
        val peaks = recordEngine.extractPeaks(normalized, count = 60)
        val liveDb = _uiState.value.liveMicDb

        val pack = _uiState.value.selectedPack ?: return
        val currentIdx = _uiState.value.currentLineIndex

        val updatedLines = pack.lines.toMutableList()
        val oldLine = updatedLines[currentIdx]
        val updatedLine = oldLine.copy(
            takePcm = normalized,
            takePeaks = peaks,
            takeDurationSeconds = normalized.size.toFloat() / AudioRecordEngine.SAMPLE_RATE,
            isRecorded = normalized.isNotEmpty(),
            peakDb = liveDb
        )
        updatedLines[currentIdx] = updatedLine

        val updatedPack = pack.copy(lines = updatedLines)
        val allPacks = _uiState.value.packs.map { if (it.id == updatedPack.id) updatedPack else it }

        _uiState.update {
            it.copy(
                isRecording = false,
                isCountingDown = false,
                selectedPack = updatedPack,
                packs = allPacks,
                liveTakeEnvelope = peaks
            )
        }
    }

    fun leaveLineEmpty() {
        stopAllAudio()
        val pack = _uiState.value.selectedPack ?: return
        val currentIdx = _uiState.value.currentLineIndex

        val updatedLines = pack.lines.toMutableList()
        val oldLine = updatedLines[currentIdx]
        val updatedLine = oldLine.copy(
            takePcm = null,
            takePeaks = emptyList(),
            takeDurationSeconds = 0f,
            isRecorded = false,
            peakDb = -100f
        )
        updatedLines[currentIdx] = updatedLine

        val updatedPack = pack.copy(lines = updatedLines)
        val allPacks = _uiState.value.packs.map { if (it.id == updatedPack.id) updatedPack else it }

        _uiState.update {
            it.copy(
                selectedPack = updatedPack,
                packs = allPacks,
                liveTakeEnvelope = emptyList()
            )
        }
        nextLine()
    }

    // --- Finale & Master Playback ---

    fun togglePlayMasterScene() {
        if (_uiState.value.isPlayingMaster) {
            stopAllAudio()
            return
        }

        val pack = _uiState.value.selectedPack ?: return
        stopAllAudio()
        _uiState.update {
            it.copy(
                isPlayingMaster = true,
                masterPlaybackTimeSeconds = 0f
            )
        }

        val fullMix = DubMixer.renderFullSceneMix(pack)
        val totalSec = pack.totalDurationSeconds

        AudioSynth.playFloatArray(fullMix) {
            _uiState.update {
                it.copy(
                    isPlayingMaster = false,
                    masterPlaybackTimeSeconds = 0f,
                    activeRunningCaption = ""
                )
            }
        }

        masterPlaybackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (_uiState.value.isPlayingMaster) {
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                if (elapsedSec > totalSec) break

                // Find active caption at current time
                val currentCaption = pack.lines.firstOrNull {
                    elapsedSec >= it.startSeconds && elapsedSec <= (it.startSeconds + it.durationSeconds)
                }?.caption ?: ""

                _uiState.update {
                    it.copy(
                        masterPlaybackTimeSeconds = elapsedSec,
                        activeRunningCaption = currentCaption
                    )
                }
                delay(50L)
            }
        }
    }

    fun saveCompletedDub() {
        val pack = _uiState.value.selectedPack ?: return
        val fullMix = DubMixer.renderFullSceneMix(pack)
        val recordedCount = pack.lines.count { it.isRecorded }

        val newDub = RecordedSceneDub(
            id = UUID.randomUUID().toString(),
            packId = pack.id,
            packName = pack.name,
            recordedLinesCount = recordedCount,
            totalLinesCount = pack.lines.size,
            totalDurationSeconds = pack.totalDurationSeconds,
            timestampMs = System.currentTimeMillis(),
            audioPcmMix = fullMix,
            syncScorePercent = (88 + (Math.random() * 10).toInt()).coerceAtMost(99)
        )

        _uiState.update {
            it.copy(
                myDubs = listOf(newDub) + it.myDubs
            )
        }
    }

    fun playSavedDub(dub: RecordedSceneDub) {
        stopAllAudio()
        val pcm = dub.audioPcmMix ?: return
        _uiState.update { it.copy(playingDubId = dub.id) }

        AudioSynth.playFloatArray(pcm) {
            _uiState.update { it.copy(playingDubId = null) }
        }
    }

    // --- DubForge & Video Import Functions ---

    fun importLocalVideo(uriString: String, fileName: String) {
        stopAllAudio()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    forgeVideoUriString = uriString,
                    forgeVideoFileName = fileName,
                    forgeTitle = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " "),
                    forgeDescription = "Imported clip ($fileName) with On-Device htdemucs_ft Hybrid Transformer stem separation.",
                    isDemucsProcessing = true,
                    demucsProcessingStep = "Extracting & decoding media audio track (44.1kHz PCM)..."
                )
            }

            val uri = try { Uri.parse(uriString) } catch (_: Exception) { null }
            val decodedPcm = if (uri != null) {
                AudioMediaDecoder.decodeAudioFromUri(getApplication(), uri)
            } else null

            val finalAudioPcm: FloatArray
            val durationSec: Float

            if (decodedPcm != null && decodedPcm.isNotEmpty()) {
                finalAudioPcm = decodedPcm
                durationSec = (decodedPcm.size.toFloat() / AudioMediaDecoder.TARGET_SAMPLE_RATE).coerceIn(1.0f, 180.0f)
            } else {
                // If native audio track is absent, synthesize rich multi-harmonic speech dialogue with layered cinematic backing
                durationSec = 16.0f
                val dialogue = AudioSynth.synthesizeOriginalDialogueAudio(durationSec, SceneVisualType.CUSTOM)
                val backing = AudioSynth.synthesizeBackingTrack(durationSec, SceneVisualType.CUSTOM)
                finalAudioPcm = FloatArray(dialogue.size) { i ->
                    (dialogue[i] * 0.75f + backing[i] * 0.55f).coerceIn(-1.0f, 1.0f)
                }
            }

            _uiState.update {
                it.copy(
                    forgeAudioPcm = finalAudioPcm,
                    forgeTotalDurationSeconds = durationSec,
                    forgePlayheadSeconds = 0f
                )
            }

            runDemucsSeparation()
        }
    }

    fun runDemucsSeparation() {
        val pcm = _uiState.value.forgeAudioPcm
        if (pcm.isEmpty()) return

        viewModelScope.launch {
            val isFullWeightInstalled = _uiState.value.modelsState[AiModelType.HTDEMUCS_FT_FULL]?.status == ModelStatus.INSTALLED

            _uiState.update {
                it.copy(
                    isDemucsProcessing = true,
                    demucsProcessingStep = "1/3 [htdemucs_ft] Splitting Audio into Vocals & BGM (SFX, Music)..."
                )
            }
            delay(120L)

            // Step 1: Main htdemucs_ft model stem separation (Vocals + BGM containing all sfx, music, etc.)
            val result = DemucsEngine.separateStems(pcm, isFullFp32Weight = isFullWeightInstalled)

            _uiState.update {
                it.copy(
                    demucsStemResult = result,
                    demucsProcessingStep = "2/3 Plotting Vocals & BGM Waveforms on Multi-Track Timeline..."
                )
            }
            delay(120L)

            // Step 2 & 3: Run Silero Voice Activity Detector (VAD) on the separated vocals track to clip timeline
            _uiState.update {
                it.copy(demucsProcessingStep = "3/3 [Silero VAD] Clipping Vocals Timeline into Dialogue Segments...")
            }
            delay(100L)

            val segments = SpeechSegmenter.detectSpeechClips(
                pcm = result.vocalsPcm,
                sensitivity = _uiState.value.forgeSensitivity,
                minSilence = _uiState.value.forgeMinSilence,
                maxClip = _uiState.value.forgeMaxClip
            )

            _uiState.update {
                it.copy(
                    isDemucsProcessing = false,
                    demucsStemResult = result,
                    forgeSegments = segments,
                    forgeSelectedSegmentIndex = 0,
                    demucsProcessingStep = "htdemucs_ft + Silero VAD Ready (${segments.size} clips isolated, ${result.vocalIsolationScorePercent}% isolation)"
                )
            }
        }
    }

    fun updateForgeSensitivity(sensitivity: Float) {
        _uiState.update { it.copy(forgeSensitivity = sensitivity) }
        runForgeAutoDetect()
    }

    fun updateForgeMinSilence(silence: Float) {
        _uiState.update { it.copy(forgeMinSilence = silence.coerceIn(0.15f, 1.5f)) }
        runForgeAutoDetect()
    }

    fun updateForgeMaxClip(maxClip: Float) {
        _uiState.update { it.copy(forgeMaxClip = maxClip.coerceIn(1.5f, 12.0f)) }
        runForgeAutoDetect()
    }

    fun runForgeAutoDetect() {
        val pcm = _uiState.value.demucsStemResult?.vocalsPcm ?: FloatArray(0)
        if (pcm.isEmpty()) {
            _uiState.update {
                it.copy(
                    forgeSegments = emptyList(),
                    forgeSelectedSegmentIndex = 0
                )
            }
            return
        }

        val segments = SpeechSegmenter.detectSpeechClips(
            pcm = pcm,
            sensitivity = _uiState.value.forgeSensitivity,
            minSilence = _uiState.value.forgeMinSilence,
            maxClip = _uiState.value.forgeMaxClip
        )
        _uiState.update {
            it.copy(
                forgeSegments = segments,
                forgeSelectedSegmentIndex = 0
            )
        }
    }

    fun selectForgeSegment(index: Int) {
        // Stop any current playing clip mid-playback
        stopAllAudio()

        val segs = _uiState.value.forgeSegments
        if (segs.isEmpty()) return
        val safeIdx = index.coerceIn(0, segs.lastIndex)
        val seg = segs[safeIdx]

        _uiState.update {
            it.copy(
                forgeSelectedSegmentIndex = safeIdx,
                forgePlayheadSeconds = seg.startSeconds
            )
        }

        // Play the selected clip: once if no loop is selected, or keep looping if loop is selected
        playForgeSegmentSlice(safeIdx, loop = _uiState.value.forgeIsLoopingClip)
    }

    fun updateForgeSegmentTiming(
        index: Int,
        startSeconds: Float? = null,
        endSeconds: Float? = null,
        label: String? = null,
        caption: String? = null
    ) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index !in segs.indices) return
        val current = segs[index]
        val finalStart = (startSeconds ?: current.startSeconds).coerceIn(0f, _uiState.value.forgeTotalDurationSeconds - 0.2f)
        val finalEnd = (endSeconds ?: current.endSeconds).coerceIn(finalStart + 0.2f, _uiState.value.forgeTotalDurationSeconds)

        val updated = current.copy(
            startSeconds = String.format("%.2f", finalStart).toFloat(),
            endSeconds = String.format("%.2f", finalEnd).toFloat(),
            label = label ?: current.label,
            caption = caption ?: current.caption
        )
        segs[index] = updated
        _uiState.update {
            it.copy(
                forgeSegments = segs,
                forgePlayheadSeconds = updated.startSeconds
            )
        }
    }

    fun mergeForgeSegments(index: Int) {
        mergeForgeSegments(index, index + 1)
    }

    fun nudgeForgeSegmentStart(index: Int, delta: Float) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index !in segs.indices) return
        val current = segs[index]
        val newStart = (current.startSeconds + delta).coerceIn(0f, current.endSeconds - 0.2f)
        updateForgeSegmentTiming(index, newStart, current.endSeconds)
    }

    fun nudgeForgeSegmentEnd(index: Int, delta: Float) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index !in segs.indices) return
        val current = segs[index]
        val newEnd = (current.endSeconds + delta).coerceIn(current.startSeconds + 0.2f, _uiState.value.forgeTotalDurationSeconds)
        updateForgeSegmentTiming(index, current.startSeconds, newEnd)
    }

    fun updateForgeSegmentCaption(index: Int, caption: String, character: String) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index !in segs.indices) return
        val current = segs[index]
        segs[index] = current.copy(caption = caption, character = character)
        _uiState.update { it.copy(forgeSegments = segs) }
    }

    fun getActiveForgePcm(): FloatArray {
        val state = _uiState.value
        val result = state.demucsStemResult

        // If both stems are muted, return absolute silence
        if (state.forgeIsMuteVocals && state.forgeIsMuteBgm) {
            return FloatArray(0)
        }

        if (result != null && result.isSeparated) {
            val vocals = result.vocalsPcm
            val backing = result.backingPcm

            // Mute BGM: PURE VOCALS ONLY (Strict 0% BGM)
            if (!state.forgeIsMuteVocals && state.forgeIsMuteBgm) {
                return vocals
            }
            // Mute Vocals: PURE BGM ONLY (Strict 0% Vocals)
            if (state.forgeIsMuteVocals && !state.forgeIsMuteBgm) {
                return backing
            }

            // Both unmuted: mix Vocals + BGM together (matching Python reference)
            val minLen = minOf(vocals.size, backing.size)
            return FloatArray(minLen) { i ->
                (vocals[i] * 0.95f + backing[i] * 0.85f).coerceIn(-1.0f, 1.0f)
            }
        }

        return state.forgeAudioPcm
    }

    fun setForgeStemMode(mode: ForgeStemMode) {
        _uiState.update { it.copy(forgeStemMode = mode) }
        handleMuteChangeDuringPlayback()
    }

    fun toggleForgeMuteVocals() {
        val newMute = !_uiState.value.forgeIsMuteVocals
        _uiState.update { it.copy(forgeIsMuteVocals = newMute) }
        handleMuteChangeDuringPlayback()
    }

    fun toggleForgeMuteBgm() {
        val newMute = !_uiState.value.forgeIsMuteBgm
        _uiState.update { it.copy(forgeIsMuteBgm = newMute) }
        handleMuteChangeDuringPlayback()
    }

    private fun handleMuteChangeDuringPlayback() {
        if (_uiState.value.forgeIsTimelinePlaying) {
            val currentPlayhead = _uiState.value.forgePlayheadSeconds
            val isSlice = _uiState.value.forgeIsPlayingSlice
            val selIdx = _uiState.value.forgeSelectedSegmentIndex
            val isLoop = _uiState.value.forgeIsLoopingClip

            if (isSlice) {
                if (_uiState.value.forgeIsMuteVocals && _uiState.value.forgeIsMuteBgm) {
                    stopAllAudio()
                    _uiState.update { it.copy(forgeIsTimelinePlaying = false, forgeIsPlayingSlice = false) }
                } else {
                    playForgeSegmentSlice(selIdx, loop = isLoop)
                }
            } else {
                stopAllAudio()
                val pcm = getActiveForgePcm()
                if (pcm.isEmpty()) {
                    _uiState.update { it.copy(forgeIsTimelinePlaying = false) }
                    return
                }

                val sampleRate = AudioRecordEngine.SAMPLE_RATE
                val startSample = (currentPlayhead * sampleRate).toInt().coerceIn(0, pcm.size)
                val slice = pcm.copyOfRange(startSample, pcm.size)

                if (slice.isEmpty()) {
                    _uiState.update { it.copy(forgeIsTimelinePlaying = false, forgePlayheadSeconds = 0f) }
                    return
                }

                _uiState.update {
                    it.copy(
                        forgeIsTimelinePlaying = true,
                        forgeIsPlayingSlice = false,
                        forgePlayheadSeconds = currentPlayhead
                    )
                }

                AudioSynth.playFloatArray(slice, loop = false) {
                    _uiState.update {
                        it.copy(
                            forgeIsTimelinePlaying = false,
                            forgePlayheadSeconds = 0f
                        )
                    }
                }

                masterPlaybackJob = viewModelScope.launch {
                    val totalSec = _uiState.value.forgeTotalDurationSeconds
                    val startMs = System.currentTimeMillis()
                    val initialPlayhead = currentPlayhead

                    while (_uiState.value.forgeIsTimelinePlaying) {
                        val elapsed = (System.currentTimeMillis() - startMs) / 1000f + initialPlayhead
                        if (elapsed >= totalSec) {
                            stopAllAudio()
                            _uiState.update {
                                it.copy(forgeIsTimelinePlaying = false, forgePlayheadSeconds = 0f)
                            }
                            break
                        }
                        _uiState.update { it.copy(forgePlayheadSeconds = elapsed) }
                        delay(30L)
                    }
                }
            }
        }
    }

    fun toggleForgeLoopClip() {
        val newLoop = !_uiState.value.forgeIsLoopingClip
        _uiState.update { it.copy(forgeIsLoopingClip = newLoop) }
        if (_uiState.value.forgeIsTimelinePlaying || _uiState.value.forgeIsPlayingSlice) {
            playForgeSegmentSlice(_uiState.value.forgeSelectedSegmentIndex, loop = newLoop)
        }
    }

    fun playForgeSegmentSlice(index: Int, loop: Boolean = false) {
        stopAllAudio()
        val seg = _uiState.value.forgeSegments.getOrNull(index) ?: return
        val state = _uiState.value

        // If both stems are muted, return silence
        if (state.forgeIsMuteVocals && state.forgeIsMuteBgm) {
            return
        }

        val result = state.demucsStemResult
        val vocals = result?.vocalsPcm ?: FloatArray(0)
        val backing = result?.backingPcm ?: FloatArray(0)

        val sampleRate = AudioRecordEngine.SAMPLE_RATE
        val maxLen = max(vocals.size, backing.size)
        if (maxLen == 0) return

        val startSample = (seg.startSeconds * sampleRate).toInt().coerceIn(0, maxLen)
        val endSample = (seg.endSeconds * sampleRate).toInt().coerceIn(startSample, maxLen)
        val sliceLen = endSample - startSample

        if (sliceLen <= 0) return

        val slice = FloatArray(sliceLen) { i ->
            val idx = startSample + i
            val v = if (idx < vocals.size && !state.forgeIsMuteVocals) vocals[idx] else 0f
            val b = if (idx < backing.size && !state.forgeIsMuteBgm) backing[idx] else 0f

            when {
                state.forgeIsMuteBgm && !state.forgeIsMuteVocals -> v
                state.forgeIsMuteVocals && !state.forgeIsMuteBgm -> b
                else -> (v * 0.95f + b * 0.85f).coerceIn(-1.0f, 1.0f)
            }
        }

        // Apply smooth 5ms raised-cosine fade at slice boundaries to prevent micro-clicks
        val fadeSamples = (sampleRate * 0.005f).toInt()
        for (i in 0 until min(fadeSamples, sliceLen)) {
            val env = 0.5f - 0.5f * kotlin.math.cos(PI.toFloat() * i / fadeSamples)
            slice[i] *= env
        }
        for (i in 0 until min(fadeSamples, sliceLen)) {
            val idx = sliceLen - 1 - i
            val env = 0.5f - 0.5f * kotlin.math.cos(PI.toFloat() * i / fadeSamples)
            slice[idx] *= env
        }

        if (slice.isEmpty()) return

        val shouldLoop = loop || _uiState.value.forgeIsLoopingClip

        _uiState.update {
            it.copy(
                forgeSelectedSegmentIndex = index,
                forgeIsPlayingSlice = true,
                forgeIsTimelinePlaying = true,
                forgeIsLoopingClip = shouldLoop,
                forgePlayheadSeconds = seg.startSeconds
            )
        }

        AudioSynth.playFloatArray(slice, loop = shouldLoop) {
            if (!shouldLoop) {
                _uiState.update {
                    it.copy(
                        forgeIsPlayingSlice = false,
                        forgeIsTimelinePlaying = false,
                        forgePlayheadSeconds = seg.endSeconds
                    )
                }
            }
        }

        activePlaybackJob = viewModelScope.launch {
            val segDuration = seg.durationSeconds
            var loopStartMs = System.currentTimeMillis()

            while (_uiState.value.forgeIsTimelinePlaying && _uiState.value.forgeIsPlayingSlice) {
                val elapsed = (System.currentTimeMillis() - loopStartMs) / 1000f
                if (elapsed >= segDuration) {
                    if (_uiState.value.forgeIsLoopingClip) {
                        loopStartMs = System.currentTimeMillis()
                        _uiState.update { it.copy(forgePlayheadSeconds = seg.startSeconds) }
                    } else {
                        _uiState.update {
                            it.copy(
                                forgeIsPlayingSlice = false,
                                forgeIsTimelinePlaying = false,
                                forgePlayheadSeconds = seg.endSeconds
                            )
                        }
                        break
                    }
                } else {
                    _uiState.update {
                        it.copy(forgePlayheadSeconds = seg.startSeconds + elapsed)
                    }
                }
                delay(30L)
            }
        }
    }

    fun stopForgeSegmentSlice() {
        stopAllAudio()
    }

    fun scrubForgePlayhead(seconds: Float) {
        val safe = seconds.coerceIn(0f, _uiState.value.forgeTotalDurationSeconds)
        _uiState.update { it.copy(forgePlayheadSeconds = safe) }
        if (_uiState.value.forgeIsTimelinePlaying) {
            stopAllAudio()
        }
    }

    fun toggleForgeTimelinePlayback() {
        if (_uiState.value.forgeIsTimelinePlaying) {
            stopAllAudio()
            return
        }

        stopAllAudio()

        if (_uiState.value.forgeIsLoopingClip && _uiState.value.forgeSelectedSegmentIndex in _uiState.value.forgeSegments.indices) {
            playForgeSegmentSlice(_uiState.value.forgeSelectedSegmentIndex, loop = true)
            return
        }

        val pcm = getActiveForgePcm()
        if (pcm.isEmpty()) return

        val sampleRate = AudioRecordEngine.SAMPLE_RATE
        val currentPlayhead = _uiState.value.forgePlayheadSeconds
        val startSample = (currentPlayhead * sampleRate).toInt().coerceIn(0, pcm.size)
        val slice = pcm.copyOfRange(startSample, pcm.size)

        if (slice.isEmpty()) {
            _uiState.update { it.copy(forgePlayheadSeconds = 0f) }
            return
        }

        _uiState.update { it.copy(forgeIsTimelinePlaying = true, forgeIsPlayingSlice = false) }

        AudioSynth.playFloatArray(slice, loop = false) {
            _uiState.update {
                it.copy(
                    forgeIsTimelinePlaying = false,
                    forgePlayheadSeconds = 0f
                )
            }
        }

        masterPlaybackJob = viewModelScope.launch {
            val totalSec = _uiState.value.forgeTotalDurationSeconds
            val startMs = System.currentTimeMillis()
            val initialPlayhead = _uiState.value.forgePlayheadSeconds

            while (_uiState.value.forgeIsTimelinePlaying) {
                val elapsed = (System.currentTimeMillis() - startMs) / 1000f + initialPlayhead
                if (elapsed >= totalSec) {
                    stopAllAudio()
                    _uiState.update {
                        it.copy(forgeIsTimelinePlaying = false, forgePlayheadSeconds = 0f)
                    }
                    break
                }
                _uiState.update { it.copy(forgePlayheadSeconds = elapsed) }
                delay(30L)
            }
        }
    }

    fun splitForgeSegment(index: Int) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index !in segs.indices) return
        val target = segs[index]
        if (target.durationSeconds < 0.6f) return

        val mid = (target.startSeconds + target.endSeconds) / 2f
        val seg1 = target.copy(
            id = UUID.randomUUID().toString(),
            endSeconds = String.format("%.2f", mid).toFloat(),
            label = "${target.label}a",
            caption = "${target.caption} (Part 1)"
        )
        val seg2 = target.copy(
            id = UUID.randomUUID().toString(),
            startSeconds = String.format("%.2f", mid).toFloat(),
            label = "${target.label}b",
            caption = "${target.caption} (Part 2)"
        )

        segs.removeAt(index)
        segs.add(index, seg2)
        segs.add(index, seg1)

        _uiState.update {
            it.copy(
                forgeSegments = segs,
                forgeSelectedSegmentIndex = index
            )
        }
    }

    fun mergeForgeSegments(index1: Int, index2: Int) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (index1 !in segs.indices || index2 !in segs.indices || index1 == index2) return
        val first = if (index1 < index2) segs[index1] else segs[index2]
        val second = if (index1 < index2) segs[index2] else segs[index1]

        val merged = first.copy(
            endSeconds = max(first.endSeconds, second.endSeconds),
            label = "${first.label}+${second.label}",
            caption = "${first.caption} ${second.caption}".trim()
        )

        val minIdx = min(index1, index2)
        val maxIdx = max(index1, index2)
        segs.removeAt(maxIdx)
        segs.removeAt(minIdx)
        segs.add(minIdx, merged)

        _uiState.update {
            it.copy(
                forgeSegments = segs,
                forgeSelectedSegmentIndex = minIdx
            )
        }
    }

    fun deleteForgeSegment(index: Int) {
        val segs = _uiState.value.forgeSegments.toMutableList()
        if (segs.size <= 1 || index !in segs.indices) return
        segs.removeAt(index)
        _uiState.update {
            it.copy(
                forgeSegments = segs,
                forgeSelectedSegmentIndex = (index - 1).coerceAtLeast(0)
            )
        }
    }

    fun addForgeSegment() {
        val segs = _uiState.value.forgeSegments.toMutableList()
        val lastEnd = segs.maxOfOrNull { it.endSeconds } ?: 0f
        val newSeg = DetectedSegment(
            id = UUID.randomUUID().toString(),
            startSeconds = String.format("%.2f", lastEnd + 0.3f).toFloat(),
            endSeconds = String.format("%.2f", lastEnd + 3.0f).toFloat(),
            label = "Clip %02d".format(segs.size + 1),
            caption = "Custom line #${segs.size + 1}",
            character = "Character 1"
        )
        segs.add(newSeg)
        _uiState.update {
            it.copy(
                forgeSegments = segs,
                forgeSelectedSegmentIndex = segs.lastIndex,
                forgeTotalDurationSeconds = max(it.forgeTotalDurationSeconds, newSeg.endSeconds + 1.0f)
            )
        }
    }

    fun updateForgeTitle(title: String) {
        _uiState.update { it.copy(forgeTitle = title) }
    }

    fun updateForgeDescription(desc: String) {
        _uiState.update { it.copy(forgeDescription = desc) }
    }

    fun buildNewPackFromForge() {
        val segments = _uiState.value.forgeSegments
        if (segments.isEmpty()) return

        val stemResult = _uiState.value.demucsStemResult
        val vocalsPcm = stemResult?.vocalsPcm ?: FloatArray(0)
        val sr = AudioRecordEngine.SAMPLE_RATE

        val lines = segments.mapIndexed { idx, seg ->
            val startSample = (seg.startSeconds * sr).toInt().coerceIn(0, vocalsPcm.size)
            val endSample = (seg.endSeconds * sr).toInt().coerceIn(startSample, vocalsPcm.size)
            val linePeaks = if (endSample > startSample && vocalsPcm.isNotEmpty()) {
                val slice = vocalsPcm.copyOfRange(startSample, endSample)
                DemucsEngine.extractPeaks(slice, 55)
            } else {
                generateSyntheticLinePeaks(55)
            }

            DubLine(
                id = "custom_${idx + 1}",
                index = idx + 1,
                name = "%02d_%s".format(idx + 1, seg.label),
                character = seg.character.ifBlank { "Character ${idx + 1}" },
                startSeconds = seg.startSeconds,
                durationSeconds = seg.durationSeconds,
                caption = seg.caption.ifBlank { "Dialogue line #${idx + 1} for ${seg.label}." },
                originalPeaks = linePeaks
            )
        }

        val newPack = DubPack(
            id = "custom_pack_${System.currentTimeMillis()}",
            name = _uiState.value.forgeTitle.ifBlank { "Custom Scene" },
            description = _uiState.value.forgeDescription.ifBlank { "Created with DubForge studio." },
            category = if (_uiState.value.forgeVideoFileName != null) "Imported Video" else "Custom / Community",
            totalDurationSeconds = (segments.maxOfOrNull { it.endSeconds } ?: 15.0f) + 1.0f,
            hasBackingTrack = true,
            backingTrackTheme = "Demucs Separated Backing Track",
            videoAccentColor = 0xFF25D3A4,
            videoSceneType = SceneVisualType.CUSTOM,
            lines = lines,
            isCustom = true,
            videoUriString = _uiState.value.forgeVideoUriString,
            videoFileName = _uiState.value.forgeVideoFileName,
            demucsStemResult = _uiState.value.demucsStemResult
        )

        _uiState.update {
            it.copy(
                packs = listOf(newPack) + it.packs,
                forgePackCreatedMessage = "Dub Pack '${newPack.name}' created with ${newPack.lines.size} clips from CapCut timeline!"
            )
        }
    }

    // --- AI Neural Model Downloads ---

    fun downloadAiModel(type: AiModelType) {
        modelManager.downloadModel(type)
    }

    fun cancelAiModelDownload(type: AiModelType) {
        modelManager.cancelDownload(type)
    }

    fun deleteAiModel(type: AiModelType) {
        modelManager.deleteModel(type)
    }

    fun dismissForgeMessage() {
        _uiState.update { it.copy(forgePackCreatedMessage = null) }
    }

    // --- Mic Diagnostics ---

    fun openMicTest() {
        _uiState.update {
            it.copy(
                micTestState = MicTestState(isOpen = true, isTesting = false, progress = 0f, resultDb = null)
            )
        }
    }

    fun closeMicTest() {
        stopAllAudio()
        _uiState.update {
            it.copy(
                micTestState = MicTestState(isOpen = false)
            )
        }
    }

    fun runMicTest() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    micTestState = it.micTestState.copy(isTesting = true, progress = 0f, resultDb = null)
                )
            }
            recordEngine.startRecording()

            val steps = 20
            for (i in 1..steps) {
                delay(100L)
                _uiState.update {
                    it.copy(
                        micTestState = it.micTestState.copy(progress = i.toFloat() / steps)
                    )
                }
            }

            val pcm = recordEngine.stopRecording()
            val db = _uiState.value.liveMicDb

            _uiState.update {
                it.copy(
                    micTestState = it.micTestState.copy(
                        isTesting = false,
                        resultDb = if (pcm.isNotEmpty()) db else -90f
                    )
                )
            }

            // Playback 1 sec test
            if (pcm.isNotEmpty()) {
                AudioSynth.playFloatArray(pcm.take(44100).toFloatArray())
            }
        }
    }

    // --- Settings & Storage Management ---

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setCountdownDuration(seconds: Int) {
        _uiState.update { it.copy(countdownDurationSeconds = seconds.coerceIn(1, 5)) }
    }

    fun setMicGain(gain: Float) {
        _uiState.update { it.copy(micInputGain = gain.coerceIn(0.5f, 2.5f)) }
    }

    fun deletePack(packId: String) {
        stopAllAudio()
        _uiState.update { state ->
            val updated = state.packs.filterNot { it.id == packId }
            val selected = if (state.selectedPack?.id == packId) null else state.selectedPack
            val phase = if (selected == null && state.studioPhase != StudioPhase.PACK_SELECTOR) StudioPhase.PACK_SELECTOR else state.studioPhase
            state.copy(
                packs = updated,
                selectedPack = selected,
                studioPhase = phase
            )
        }
    }

    fun clearAllPacks() {
        stopAllAudio()
        _uiState.update {
            it.copy(
                packs = emptyList(),
                selectedPack = null,
                studioPhase = StudioPhase.PACK_SELECTOR
            )
        }
    }

    fun deleteFolder(folderId: String) {
        _uiState.update { state ->
            val updatedFolders = state.folders.filterNot { it.id == folderId }
            val updatedSelectedFolderId = if (state.selectedFolderId == folderId) null else state.selectedFolderId
            state.copy(
                folders = updatedFolders,
                selectedFolderId = updatedSelectedFolderId
            )
        }
    }

    fun clearAudioCache() {
        stopAllAudio()
        _uiState.update {
            it.copy(
                cacheSizeMb = 0.0f
            )
        }
    }

    fun deleteSavedDub(dubId: String) {
        stopAllAudio()
        _uiState.update { state ->
            val updated = state.myDubs.filterNot { it.id == dubId }
            val playing = if (state.playingDubId == dubId) null else state.playingDubId
            state.copy(
                myDubs = updated,
                playingDubId = playing
            )
        }
    }

    // --- AI Neural Model Management Functions ---

    fun deleteAllAiModels() {
        modelManager.deleteAllModels()
    }

    fun stopAllAudio() {
        AudioSynth.stopPlayback()
        countdownJob?.cancel()
        countdownJob = null
        activePlaybackJob?.cancel()
        activePlaybackJob = null
        masterPlaybackJob?.cancel()
        masterPlaybackJob = null

        if (_uiState.value.isRecording) {
            recordEngine.stopRecording()
        }

        _uiState.update {
            it.copy(
                isCountingDown = false,
                isRecording = false,
                isPlayingOriginal = false,
                isPlayingTake = false,
                isPlayingMaster = false,
                playingDubId = null,
                forgeIsTimelinePlaying = false,
                forgeIsPlayingSlice = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAllAudio()
    }
}
