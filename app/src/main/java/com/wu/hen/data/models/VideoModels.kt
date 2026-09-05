package com.wu.hen.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents video information extracted from a video platform
 * @param url Original video URL that was parsed
 * @param title Video title
 * @param author Video author/creator
 * @param thumbnailUrl URL for video thumbnail image
 * @param durationSeconds Video duration in seconds
 * @param platformName Name of the source platform (e.g., "douyin", "bilibili")
 * @param formatFormat Container format (mp4, mov, etc.)
 */
@Serializable
data class VideoInfo(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int = 0,
    val platformName: String,
    val availableFormats: List<VideoFormatOption>,
) {
    /**
     * Get the preferred format based on user preference
     */
    fun getPreferredFormat(preferredQuality: VideoQuality): VideoFormatOption? {
        return when (preferredQuality) {
            VideoQuality.HIGHEST -> availableFormats.maxByOrNull { it.bitrate ?: 0 }
            VideoQuality.LOWEST -> availableFormats.minByOrNull { it.bitrate ?: 0 }
            else -> availableFormats.find { it.quality == preferredQuality }
                    ?: availableFormats.firstOrNull()
        }
    }
}

/**
 * Represents different quality/format options for a video
 */
@Serializable
data class VideoFormatOption(
    val formatId: String,
    val quality: VideoQuality,
    val resolution: Pair<Int, Int>? = null, // width, height
    val extension: String = "mp4",
    val bitrate: Int? = null, // in kbps
    val fps: Int? = null,
    val filesize: Long? = null,
    val isAudioOnly: Boolean = false,
    val audioCodec: String? = null,
)

/**
 * Quality level enum for video downloads
 */
enum class VideoQuality(val displayName: String) {
    HIGHEST("最高画质"),
    UHD_2160P("4K 2160p"),
    FHD_1080P("1080p 全高清"),
    HD_720P("720p 高清"),
    SD_480P("480p 标准"),
    LOW_360P("360p 流畅"),
    AUDIO_ONLY("仅音频")
}

/**
 * Download task represents a download job in progress or completed
 */
@Serializable
data class DownloadTask(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val videoInfo: VideoInfo,
    val selectedFormat: VideoFormatOption,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val downloadedPath: String? = null,
    val fileSizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val startTimeMillis: Long = 0L,
    val estimatedCompletionTimeMillis: Long = 0L,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val priority: Int = 0,
) {
    /**
     * Get download progress percentage (0-100)
     */
    val progressPercentage: Float
        get() = if (fileSizeBytes > 0) {
            (downloadedBytes.toFloat() / fileSizeBytes) * 100
        } else {
            0f
        }

    /**
     * Check if download is complete
     */
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED

    /**
     * Check if download can be resumed
     */
    val canResume: Boolean
        get() = status == DownloadStatus.PAUSED && downloadedBytes > 0
}

/**
 * Enum representing download task states
 */
enum class DownloadStatus {
    PENDING,      // Added to queue but not started
    DOWNLOADING,  // Currently downloading
    PAUSED,       // Download paused by user
    COMPLETED,    // Download finished successfully
    FAILED,       // Download failed
    CANCELLED     // Download cancelled by user
}

/**
 * User preferences stored in DataStore
 */
@Serializable
data class UserPreferences(
    val defaultQuality: VideoQuality = VideoQuality.FHD_1080P,
    val preferredStoragePath: String? = null,
    val autoDownloadOnParse: Boolean = false,
    val enableNotifications: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val enabledPlatforms: Set<String> = setOf(
        "douyin", "kuaishou", "bilibili", "xiaohongshu", "pipixia"
    ),
    val filenameTemplate: String = "{title} - {author}",
    val overwriteFiles: Boolean = false,
    val compactMode: Boolean = false,
    val darkTheme: Boolean = false,
) {
    companion object {
        val DEFAULT = UserPreferences()
    }
}

/**
 * Statistics for storage management
 */
data class StorageStats(
    val totalUsedBytes: Long,
    val downloadCount: Int,
    val recentDownloads: List<RecentDownloadItem>
)

data class RecentDownloadItem(
    val path: String,
    val title: String,
    val fileSizeBytes: Long,
    val timestampMillis: Long,
    val thumbnailUrl: String? = null
)
