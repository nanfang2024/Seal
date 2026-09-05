package com.wu.hen.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wu.hen.data.models.UserPreferences
import com.wu.hen.data.models.VideoQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好的 DataStore 持久化仓库
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val ENABLED_PLATFORMS = stringSetPreferencesKey("enabled_platforms")
        val AUTO_DOWNLOAD = booleanPreferencesKey("auto_download_on_parse")
        val NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("enable_haptic_feedback")
        val FILENAME_TEMPLATE = stringPreferencesKey("filename_template")
        val OVERWRITE_FILES = booleanPreferencesKey("overwrite_files")
    }

    val preferences: Flow<UserPreferences> =
        context.userPreferencesDataStore.data.map { prefs ->
            UserPreferences(
                defaultQuality =
                    prefs[Keys.DEFAULT_QUALITY]?.let { qualityFromName(it) }
                        ?: UserPreferences.DEFAULT.defaultQuality,
                enabledPlatforms =
                    prefs[Keys.ENABLED_PLATFORMS] ?: UserPreferences.DEFAULT.enabledPlatforms,
                autoDownloadOnParse =
                    prefs[Keys.AUTO_DOWNLOAD] ?: UserPreferences.DEFAULT.autoDownloadOnParse,
                enableNotifications =
                    prefs[Keys.NOTIFICATIONS] ?: UserPreferences.DEFAULT.enableNotifications,
                enableHapticFeedback =
                    prefs[Keys.HAPTIC_FEEDBACK] ?: UserPreferences.DEFAULT.enableHapticFeedback,
                filenameTemplate =
                    prefs[Keys.FILENAME_TEMPLATE] ?: UserPreferences.DEFAULT.filenameTemplate,
                overwriteFiles =
                    prefs[Keys.OVERWRITE_FILES] ?: UserPreferences.DEFAULT.overwriteFiles,
            )
        }

    suspend fun setDefaultQuality(quality: VideoQuality) {
        context.userPreferencesDataStore.edit { it[Keys.DEFAULT_QUALITY] = quality.name }
    }

    suspend fun setAutoDownload(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.AUTO_DOWNLOAD] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setEnabledPlatforms(platforms: Set<String>) {
        context.userPreferencesDataStore.edit { it[Keys.ENABLED_PLATFORMS] = platforms }
    }

    private fun qualityFromName(name: String): VideoQuality? =
        VideoQuality.entries.find { it.name == name }
}
