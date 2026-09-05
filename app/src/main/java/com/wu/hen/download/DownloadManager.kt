package com.wu.hen.download

import com.wu.hen.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.coroutineScope

/**
 * Global download manager for coordinating background downloads
 * Singleton pattern to ensure single instance across app
 */
object DownloadManager {
    
    private val mutex = Mutex()
    
    /**
     * Current list of all download tasks
     */
    private val _downloadTasks = MutableStateFlow(emptyList<DownloadTask>())
    val downloadTasks: Flow<List<DownloadTask>> = _downloadTasks
    
    /**
     * Get active (downloading or queued) tasks count
     */
    val activeTaskCount: Int
        get() = _downloadTasks.value.count { 
            it.status == DownloadStatus.DOWNLOADING || 
            it.status == DownloadStatus.PENDING 
        }

    /**
     * Check if downloader is running
     */
    val isDownloading: Boolean
        get() = _downloadTasks.value.any { it.status == DownloadStatus.DOWNLOADING }

    /**
     * Add a new download task to the queue
     * @param videoInfo The video information to download
     * @param format The selected format option
     * @param priority Task priority (higher = more important)
     */
    suspend fun addDownload(
        videoInfo: VideoInfo,
        format: VideoFormatOption,
        priority: Int = 0
    ): DownloadTask {
        return mutex.withLock {
            val task = DownloadTask(
                videoInfo = videoInfo,
                selectedFormat = format,
                status = DownloadStatus.PENDING,
                startTimeMillis = System.currentTimeMillis(),
                priority = priority
            )
            
            updateTasks(listOf(task))
            task
        }
    }

    /**
     * Start downloading a specific task
     */
    suspend fun startDownload(taskId: String): Result<Boolean> {
        return try {
            val task = _downloadTasks.value.find { it.id == taskId }
                ?: return Result.failure(Exception("Task not found"))
            
            if (task.status != DownloadStatus.PENDING && task.status != DownloadStatus.PAUSED) {
                return Result.failure(Exception("Task is already running"))
            }

            // Update task status to downloading
            val updatedTask = task.copy(
                status = DownloadStatus.DOWNLOADING,
                downloadedBytes = 0L
            )
            
            updateTasks(_downloadTasks.value.map { 
                if (it.id == taskId) updatedTask else it 
            })

            // Start actual download worker
            startDownloadWorker(updatedTask)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pause a downloading task
     */
    suspend fun pauseDownload(taskId: String): Result<Boolean> {
        return mutex.withLock {
            val task = _downloadTasks.value.find { it.id == taskId }
                ?: return Result.failure(Exception("Task not found"))
            
            if (task.status != DownloadStatus.DOWNLOADING) {
                return Result.failure(Exception("Cannot pause this task"))
            }

            // Pause and save state
            val pausedTask = task.copy(
                status = DownloadStatus.PAUSED
            )
            
            updateTasks(_downloadTasks.value.map { 
                if (it.id == taskId) pausedTask else it 
            })

            // Stop worker for this task
            pauseWorker(taskId)
            
            Result.success(true)
        }
    }

    /**
     * Resume a paused task
     */
    suspend fun resumeDownload(taskId: String): Result<Boolean> {
        return startDownload(taskId)
    }

    /**
     * Cancel and remove a task
     */
    suspend fun cancelDownload(taskId: String): Result<Boolean> {
        return mutex.withLock {
            val task = _downloadTasks.value.find { it.id == taskId }
                ?: return Result.failure(Exception("Task not found"))
            
            if (task.status == DownloadStatus.DOWNLOADING) {
                pauseWorker(taskId)
            }

            // Remove task from list
            updateTasks(_downloadTasks.value.filter { it.id != taskId })
            
            // TODO: Delete downloaded file if exists
            deleteFileIfExists(task.downloadedPath)
            
            Result.success(true)
        }
    }

    /**
     * Clear completed tasks from the list
     */
    suspend fun clearCompleted(): Int {
        return mutex.withLock {
            val currentTasks = _downloadTasks.value
            val completedIds = currentTasks.filter { it.isCompleted }.map { it.id }
            
            updateTasks(currentTasks.filterNot { it.isCompleted })
            completedIds.size
        }
    }

    /**
     * Get statistics about storage usage
     */
    suspend fun getStorageStats(): StorageStats {
        val completedTasks = _downloadTasks.value.filter { it.isCompleted }
        
        var totalSize = 0L
        val recentItems = mutableListOf<RecentDownloadItem>()

        for (task in completedTasks) {
            task.downloadedPath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    totalSize += file.length()
                    
                    recentItems.add(0, RecentDownloadItem(
                        path = path,
                        title = task.videoInfo.title,
                        fileSizeBytes = task.fileSizeBytes,
                        timestampMillis = task.startTimeMillis
                    ))
                }
            }
        }

        return StorageStats(
            totalUsedBytes = totalSize,
            downloadCount = completedTasks.size,
            recentDownloads = recentItems.take(10)
        )
    }

    /**
     * Bulk operations
     */
    suspend fun pauseAllDownloads(): Result<Int> {
        return mutex.withLock {
            val failedTasks = mutableSetOf<String>()
            
            coroutineScope {
                _downloadTasks.value.filter { it.status == DownloadStatus.DOWNLOADING }
                    .forEach { task ->
                        launch {
                            pauseDownload(task.id).onFailure { failedTasks.add(task.id) }
                        }
                    }
            }

            if (failedTasks.isNotEmpty()) {
                Result.failure(Exception("Failed to pause ${failedTasks.size} tasks"))
            } else {
                Result.success(_downloadTasks.value.count { it.status == DownloadStatus.DOWNLOADING })
            }
        }
    }

    suspend fun cancelAllDownloads(cancelingCompleted: Boolean = false): Result<Int> {
        return mutex.withLock {
            val tasksToRemove = if (cancelingCompleted) {
                _downloadTasks.value.map { it.id }
            } else {
                _downloadTasks.value.filter { it.status != DownloadStatus.COMPLETED }.map { it.id }
            }

            updateTasks(_downloadTasks.value.filter { it.id !in tasksToRemove })
            Result.success(tasksToRemove.size)
        }
    }

    /**
     * Internal helper methods
     */
    private fun updateTasks(newTasks: List<DownloadTask>) {
        _downloadTasks.value = newTasks.sortedByDescending { it.priority }
            .sortedByDescending { it.status.ordinal }
    }

    private suspend fun startDownloadWorker(task: DownloadTask) {
        // TODO: Implement actual download worker with progress tracking
        // This should run in background service
    }

    private fun pauseWorker(taskId: String) {
        // TODO: Signal worker to pause
    }

    private fun deleteFileIfExists(path: String?) {
        path?.let { 
            val file = java.io.File(it)
            if (file.exists()) file.delete()
        }
    }
}
