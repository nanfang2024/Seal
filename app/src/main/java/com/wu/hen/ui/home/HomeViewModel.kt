package com.wu.hen.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wu.hen.data.models.VideoFormatOption
import com.wu.hen.data.models.VideoInfo
import com.wu.hen.download.DownloadManager
import com.wu.hen.platform.common.PlatformRegistry
import com.wu.hen.platform.common.UrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页状态：链接输入 → 解析 → 展示结果 → 发起下载
 */
class HomeViewModel : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState

        data object Loading : UiState

        data class Success(val videoInfo: VideoInfo) : UiState

        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    /** 解析视频链接 */
    fun parse(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = UiState.Error("请输入视频链接")
            return
        }
        if (!UrlUtils.isValidUrl(trimmed)) {
            _uiState.value = UiState.Error("链接格式不正确，请粘贴完整的分享链接")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            PlatformRegistry.parseVideo(trimmed)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { e ->
                    _uiState.value = UiState.Error(e.message ?: "解析失败，请稍后重试")
                }
        }
    }

    /** 将选中格式加入下载队列并立即开始 */
    fun download(videoInfo: VideoInfo, format: VideoFormatOption) {
        viewModelScope.launch {
            runCatching {
                val task = DownloadManager.addDownload(videoInfo, format)
                DownloadManager.startDownload(task.id)
            }.onSuccess {
                _snackbarMessage.value = "已加入下载队列"
            }.onFailure {
                _snackbarMessage.value = "加入下载队列失败：${it.message}"
            }
        }
    }

    /** 消费一次性提示 */
    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    /** 回到初始状态 */
    fun reset() {
        _uiState.value = UiState.Idle
    }
}
