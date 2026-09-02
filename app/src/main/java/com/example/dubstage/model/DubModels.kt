package com.example.dubstage.model

data class DubLine(
    val id: String,
    val index: Int,
    val name: String,
    val character: String,
    val startSeconds: Float,
    val durationSeconds: Float,
    val caption: String,
    val originalPeaks: List<Float> = emptyList(),
    val takePeaks: List<Float> = emptyList(),
    val takePcm: FloatArray? = null,
    val takeDurationSeconds: Float = 0f,
    val isRecorded: Boolean = false,
    val peakDb: Float = -100f
) {
    val endSeconds: Float get() = startSeconds + durationSeconds

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DubLine
        return id == other.id &&
                index == other.index &&
                isRecorded == other.isRecorded &&
                caption == other.caption &&
                startSeconds == other.startSeconds &&
                durationSeconds == other.durationSeconds &&
                takePeaks == other.takePeaks
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + index
        result = 31 * result + isRecorded.hashCode()
        return result
    }
}

data class DubPackFolder(
    val id: String,
    val name: String,
    val pathDisplay: String,
    val packCount: Int,
    val isDefault: Boolean = false,
    val uriString: String? = null
)

data class DemucsStemResult(
    val isSeparated: Boolean = true,
    val vocalsPcm: FloatArray = FloatArray(0),
    val backingPcm: FloatArray = FloatArray(0),
    val vocalPeaks: List<Float> = emptyList(),
    val backingPeaks: List<Float> = emptyList(),
    val vocalIsolationScorePercent: Int = 98,
    val processingLatencyMs: Long = 142L,
    val gpuDeviceName: String = "On-Device Hardware GPU (NNAPI / Vulkan FP16)"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DemucsStemResult
        return isSeparated == other.isSeparated &&
                vocalIsolationScorePercent == other.vocalIsolationScorePercent
    }

    override fun hashCode(): Int = 31 * isSeparated.hashCode() + vocalIsolationScorePercent.hashCode()
}

data class DubPack(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val totalDurationSeconds: Float,
    val hasBackingTrack: Boolean,
    val backingTrackTheme: String,
    val lines: List<DubLine>,
    val videoAccentColor: Long = 0xFF7C5CFF,
    val videoSceneType: SceneVisualType = SceneVisualType.SCI_FI,
    val isCustom: Boolean = false,
    val videoUriString: String? = null,
    val videoFileName: String? = null,
    val folderId: String = "default_internal",
    val demucsStemResult: DemucsStemResult? = null
)

enum class SceneVisualType {
    SCI_FI,
    ANIME_BATTLE,
    NOIR_DETECTIVE,
    COMEDY_CAFE,
    CUSTOM
}

data class RecordedSceneDub(
    val id: String,
    val packId: String,
    val packName: String,
    val recordedLinesCount: Int,
    val totalLinesCount: Int,
    val totalDurationSeconds: Float,
    val timestampMs: Long,
    val audioPcmMix: FloatArray? = null,
    val syncScorePercent: Int = 95
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecordedSceneDub
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    ML("ml", "Malayalam", "മലയാളം")
}

enum class AppThemeMode(val displayName: String, val subtitle: String) {
    DARK("Dark Canvas", "Deep OLED-optimized studio theme"),
    LIGHT("Light Studio", "Crisp high-contrast daylight theme"),
    SYSTEM("System Default", "Follows Android OS system theme")
}
