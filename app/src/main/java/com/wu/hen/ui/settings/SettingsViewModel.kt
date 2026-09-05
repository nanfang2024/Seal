package com.wu.hen.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wu.hen.data.models.StorageStats
import com.wu.hen.data.models.UserPreferences
import com.wu.hen.data.models.VideoQuality
import com.wu.hen.data.preferences.PreferencesRepository
import com.wu.hen.download.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页状态：偏好读写 + 存储统计
 */
class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {

    val preferences: StateFlow<UserPreferences> =
        repository.preferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences.DEFAULT)

    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    init {
        refreshStorageStats()
    }

    fun setDefaultQuality(quality: VideoQuality) {
        viewModelScope.launch { repository.setDefaultQuality(quality) }
    }

    fun setAutoDownload(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoDownload(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifications(enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticFeedback(enabled) }
    }

    fun refreshStorageStats() {
        _storageStats.value = DownloadManager.getStorageStats()
    }
}
