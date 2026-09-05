package com.wu.hen.download

import android.content.Intent
import android.os.Environment
import androidx.core.content.ContextCompat
import com.wu.hen.WuHenApp
import com.wu.hen.data.models.DownloadStatus
import com.wu.hen.data.models.DownloadTask
import com.wu.hen.data.models.RecentDownloadItem
import com.wu.hen.data.models.StorageStats
import com.wu.hen.data.models.VideoFormatOption
import com.wu.hen.data.models.VideoInfo
import com.wu.hen.platform.common.HttpClient
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 全局下载管理器：维护下载队列、驱动实际下载协程、
 * 通过 StateFlow 向 UI 暴露任务状态；暂停/继续/取消均为协程级控制。
 */
object DownloadManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = HttpClient()

    /** taskId -> 正在执行的下载协程 */
    private val workerJobs = LinkedHashMap<String, Job>()

    private val _downloadTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloadTasks: StateFlow<List<DownloadTask>> = _downloadTasks.asStateFlow()

    /** 活动任务数（下载中 + 排队） */
    val activeTaskCount: Int
        get() =
            _downloadTasks.value.count {
                it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING
            }

    /** 是否有正在下载的任务 */
    val isDownloading: Boolean
        get() = _downloadTasks.value.any { it.status == DownloadStatus.DOWNLOADING }

    /**
     * 新建下载任务并加入队列
     */
    suspend fun addDownload(
        videoInfo: VideoInfo,
        format: VideoFormatOption,
        priority: Int = 0,
    ): DownloadTask {
        val task =
            DownloadTask(
                videoInfo = videoInfo,
                selectedFormat = format,
                status = DownloadStatus.PENDING,
                startTimeMillis = System.currentTimeMillis(),
                priority = priority,
            )
        _downloadTasks.update { it + task }
        ensureServiceRunning()
        return task
    }

    /** 开始（或继续）指定任务 */
    suspend fun startDownload(taskId: String): Result<Boolean> {
        val task = _downloadTasks.value.find { it.id == taskId }
            ?: return Result.failure(IllegalArgumentException("任务不存在"))
        if (task.status !in
            setOf(DownloadStatus.PENDING, DownloadStatus.PAUSED, DownloadStatus.FAILED)
        ) {
            return Result.failure(IllegalStateException("任务当前状态（${task.status.displayName}）不可开始"))
        }
        updateTask(taskId) { it.copy(status = DownloadStatus.DOWNLOADING) }
        launchWorker(taskId)
        ensureServiceRunning()
        return Result.success(true)
    }

    /** 暂停任务（取消下载协程，已下载字节保留用于续传） */
    fun pauseDownload(taskId: String): Result<Boolean> {
        val task = _downloadTasks.value.find { it.id == taskId }
            ?: return Result.failure(IllegalArgumentException("任务不存在"))
        if (task.status != DownloadStatus.DOWNLOADING) {
            return Result.failure(IllegalStateException("任务未在下载中"))
        }
        workerJobs.remove(taskId)?.cancel()
        updateTask(taskId) { it.copy(status = DownloadStatus.PAUSED) }
        return Result.success(true)
    }

    /** 继续任务 */
    suspend fun resumeDownload(taskId: String): Result<Boolean> = startDownload(taskId)

    /** 取消并移除任务（同时删除未完成的临时文件） */
    fun cancelDownload(taskId: String): Result<Boolean> {
        val task = _downloadTasks.value.find { it.id == taskId }
            ?: return Result.failure(IllegalArgumentException("任务不存在"))
        workerJobs.remove(taskId)?.cancel()
        _downloadTasks.update { list -> list.filterNot { it.id == taskId } }
        if (!task.isCompleted) deleteFileIfExists(task.downloadedPath)
        return Result.success(true)
    }

    /** 清除已完成任务 */
    fun clearCompleted(): Int {
        val completed = _downloadTasks.value.count { it.isCompleted }
        _downloadTasks.update { list -> list.filterNot { it.isCompleted } }
        return completed
    }

    /** 存储统计 */
    fun getStorageStats(): StorageStats {
        val completedTasks = _downloadTasks.value.filter { it.isCompleted }
        var totalSize = 0L
        val recentItems = mutableListOf<RecentDownloadItem>()
        for (task in completedTasks) {
            task.downloadedPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    totalSize += file.length()
                    recentItems.add(
                        RecentDownloadItem(
                            path = path,
                            title = task.videoInfo.title,
                            fileSizeBytes = file.length(),
                            timestampMillis = task.startTimeMillis,
                            thumbnailUrl = task.videoInfo.thumbnailUrl,
                        )
                    )
                }
            }
        }
        return StorageStats(
            totalUsedBytes = totalSize,
            downloadCount = completedTasks.size,
            recentDownloads = recentItems.sortedByDescending { it.timestampMillis }.take(10),
        )
    }

    /** 暂停全部任务 */
    fun pauseAllDownloads(): Result<Int> {
        val downloading =
            _downloadTasks.value.filter { it.status == DownloadStatus.DOWNLOADING }
        downloading.forEach { task ->
            workerJobs.remove(task.id)?.cancel()
            updateTask(task.id) { it.copy(status = DownloadStatus.PAUSED) }
        }
        return Result.success(downloading.size)
    }

    /** 取消全部任务 */
    fun cancelAllDownloads(includeCompleted: Boolean = false): Result<Int> {
        workerJobs.values.forEach { it.cancel() }
        workerJobs.clear()
        val removed = _downloadTasks.value
        val toRemove =
            if (includeCompleted) removed
            else removed.filter { it.status != DownloadStatus.COMPLETED }
        _downloadTasks.update { list -> list - toRemove.toSet() }
        return Result.success(toRemove.size)
    }

    /** 释放资源（进程退出时调用） */
    fun shutdown() {
        scope.cancel()
    }

    // ---------- 内部实现 ----------

    private fun launchWorker(taskId: String) {
        val job = scope.launch {
            val task =
                _downloadTasks.value.find { it.id == taskId }
                    ?: return@launch
            val destFile = File(downloadDirectory(), buildFileName(task))
            updateTask(taskId) { it.copy(downloadedPath = destFile.absolutePath) }
            try {
                httpClient
                    .downloadFile(
                        url = task.selectedFormat.url,
                        destinationPath = destFile.absolutePath,
                        headers = buildDownloadHeaders(task),
                    ) { downloaded, total ->
                        updateTask(taskId) { current ->
                            current.copy(
                                downloadedBytes = downloaded,
                                fileSizeBytes =
                                    if (total > 0) total else current.fileSizeBytes,
                            )
                        }
                    }
                    .onSuccess { path ->
                        updateTask(taskId) {
                            it.copy(
                                status = DownloadStatus.COMPLETED,
                                downloadedPath = path,
                                fileSizeBytes = File(path).length(),
                                errorMessage = null,
                            )
                        }
                    }
                    .onFailure { e ->
                        updateTask(taskId) {
                            it.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = e.message ?: "未知错误",
                            )
                        }
                    }
            } catch (e: CancellationException) {
                // 暂停/取消由外层负责改状态，这里保持取消语义
                throw e
            } catch (e: Exception) {
                updateTask(taskId) {
                    it.copy(status = DownloadStatus.FAILED, errorMessage = e.message)
                }
            } finally {
                workerJobs.remove(taskId)
            }
        }
        workerJobs[taskId] = job
    }

    private fun buildDownloadHeaders(task: DownloadTask): Map<String, String> =
        mapOf(
            "User-Agent" to
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Referer" to task.videoInfo.url,
        )

    /** 下载目录：应用外部私有 Movies/WuHen（无需存储权限） */
    private fun downloadDirectory(): File {
        val context = WuHenApp.instance
        val dir =
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: context.filesDir
        return File(dir, "WuHen").apply { mkdirs() }
    }

    /** 文件名规范化：去除非法字符并截断长度 */
    private fun buildFileName(task: DownloadTask): String {
        val raw = "${task.videoInfo.title}.${task.selectedFormat.extension}"
        return raw.replace(Regex("[\\\\/:*?\"<>|\\r\\n]"), "_").trim().take(120)
    }

    private fun updateTask(taskId: String, transform: (DownloadTask) -> DownloadTask) {
        _downloadTasks.update { list ->
            list.map { if (it.id == taskId) transform(it) else it }
        }
    }

    private fun ensureServiceRunning() {
        val context = WuHenApp.instance
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, DownloadQueueService::class.java),
            )
        }
    }

    private fun deleteFileIfExists(path: String?) {
        path?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
    }
}
