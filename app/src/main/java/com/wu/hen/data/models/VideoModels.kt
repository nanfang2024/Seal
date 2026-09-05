package com.wu.hen.data.models

import kotlinx.serialization.Serializable

/**
 * 从视频平台解析出的视频信息
 */
@Serializable
data class VideoInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int = 0,
    val platformName: String,
    val availableFormats: List<VideoFormatOption>,
) {
    /**
     * 按用户偏好画质选取最合适的格式
     */
    fun getPreferredFormat(preferredQuality: VideoQuality): VideoFormatOption? {
        return when (preferredQuality) {
            VideoQuality.HIGHEST -> availableFormats.maxByOrNull { it.bitrate ?: 0 }
            else ->
                availableFormats.find { it.quality == preferredQuality }
                    ?: availableFormats.minByOrNull { it.bitrate ?: 0 }
        }
    }
}

/**
 * 单个可下载的画质/格式选项
 */
@Serializable
data class VideoFormatOption(
    val formatId: String,
    val url: String,
    val quality: VideoQuality,
    val resolution: Pair<Int, Int>? = null, // 宽, 高
    val extension: String = "mp4",
    val bitrate: Int? = null, // kbps
    val fps: Int? = null,
    val filesize: Long? = null,
    val isAudioOnly: Boolean = false,
    val audioCodec: String? = null,
)

/**
 * 画质档位
 */
enum class VideoQuality(val displayName: String) {
    HIGHEST("最高画质"),
    UHD_2160P("4K 2160p"),
    FHD_1080P("1080p 全高清"),
    HD_720P("720p 高清"),
    SD_480P("480p 标准"),
    LOW_360P("360p 流畅"),
    AUDIO_ONLY("仅音频"),
}

/**
 * 下载任务（进行中或已完成）
 */
@Serializable
data class DownloadTask(
    val id: String = java.util.UUID.randomUUID().toString(),
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
    /** 下载进度百分比（0-100） */
    val progressPercentage: Float
        get() = if (fileSizeBytes > 0) (downloadedBytes.toFloat() / fileSizeBytes) * 100 else 0f

    /** 是否已完成 */
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED

    /** 是否可续传 */
    val canResume: Boolean
        get() = status == DownloadStatus.PAUSED && downloadedBytes > 0
}

/**
 * 下载任务状态机
 */
enum class DownloadStatus(val displayName: String) {
    PENDING("排队中"),
    DOWNLOADING("下载中"),
    PAUSED("已暂停"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消"),
}

/**
 * 用户偏好（DataStore 持久化）
 */
data class UserPreferences(
    val defaultQuality: VideoQuality = VideoQuality.FHD_1080P,
    val preferredStoragePath: String? = null,
    val autoDownloadOnParse: Boolean = false,
    val enableNotifications: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val enabledPlatforms: Set<String> =
        setOf("douyin", "kuaishou", "bilibili", "xiaohongshu", "pipixia"),
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
 * 存储统计
 */
data class StorageStats(
    val totalUsedBytes: Long,
    val downloadCount: Int,
    val recentDownloads: List<RecentDownloadItem>,
)

data class RecentDownloadItem(
    val path: String,
    val title: String,
    val fileSizeBytes: Long,
    val timestampMillis: Long,
    val thumbnailUrl: String? = null,
)
