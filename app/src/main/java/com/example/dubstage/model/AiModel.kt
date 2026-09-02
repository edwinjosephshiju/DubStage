package com.example.dubstage.model

enum class AiModelType(val displayName: String) {
    HTDEMUCS_FT_FULL("htdemucs_ft Full Weights (Hybrid Transformer FP32 GPU)"),
    HTDEMUCS_FT("htdemucs_ft Fine-Tuned (Hybrid Transformer FP16 GPU)"),
    SILERO_VAD("Silero Voice Activity Detector (VAD v4 FP32)"),
    PITCH_TIMBRE_ENGINE("Neural Pitch & Formant Timbre Engine (FP32)")
}

enum class ModelStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    INSTALLED,
    ERROR
}

data class AiModelInfo(
    val type: AiModelType,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val version: String,
    val downloadUrl: String,
    val fileName: String,
    val isFullWeight: Boolean = false,
    val precision: String = "FP32 GPU"
) {
    val formattedSize: String
        get() = String.format("%.1f MB", sizeBytes.toFloat() / (1024 * 1024))
}

data class ModelDownloadState(
    val type: AiModelType,
    val info: AiModelInfo,
    val status: ModelStatus = ModelStatus.NOT_DOWNLOADED,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedKbps: Float = 0f,
    val localFilePath: String? = null,
    val statusMessage: String = ""
) {
    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)

    val downloadedMbFormatted: String
        get() = String.format("%.1f MB", downloadedBytes.toFloat() / (1024 * 1024))

    val totalMbFormatted: String
        get() = String.format("%.1f MB", totalBytes.toFloat() / (1024 * 1024))

    val speedFormatted: String
        get() = if (speedKbps >= 1024f) {
            String.format("%.1f MB/s", speedKbps / 1024f)
        } else {
            String.format("%.0f KB/s", speedKbps)
        }
}

