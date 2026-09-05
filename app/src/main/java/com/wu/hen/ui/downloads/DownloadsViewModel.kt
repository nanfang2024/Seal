package com.wu.hen.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wu.hen.data.models.DownloadTask
import com.wu.hen.download.DownloadManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 下载管理页状态：实时任务队列与操作入口
 */
class DownloadsViewModel : ViewModel() {

    val tasks: StateFlow<List<DownloadTask>> = DownloadManager.downloadTasks

    fun pause(taskId: String) {
        DownloadManager.pauseDownload(taskId)
    }

    fun resume(taskId: String) {
        viewModelScope.launch { DownloadManager.resumeDownload(taskId) }
    }

    fun cancel(taskId: String) {
        DownloadManager.cancelDownload(taskId)
    }

    fun pauseAll() {
        DownloadManager.pauseAllDownloads()
    }

    fun clearCompleted() {
        DownloadManager.clearCompleted()
    }
}
