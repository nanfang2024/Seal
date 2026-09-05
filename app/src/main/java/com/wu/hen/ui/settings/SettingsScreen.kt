package com.wu.hen.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wu.hen.BuildConfig
import com.wu.hen.data.models.VideoQuality
import org.koin.androidx.compose.koinViewModel

/**
 * 设置页：下载偏好、存储管理、关于与免责声明
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // 下载设置
        SettingsCard(title = "下载") {
            Text(
                "默认画质",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                        VideoQuality.HIGHEST,
                        VideoQuality.FHD_1080P,
                        VideoQuality.HD_720P,
                        VideoQuality.AUDIO_ONLY,
                    )
                    .forEach { quality ->
                        FilterChip(
                            selected = preferences.defaultQuality == quality,
                            onClick = { viewModel.setDefaultQuality(quality) },
                            label = { Text(quality.displayName) },
                        )
                    }
            }
            SwitchRow(
                title = "解析后自动下载",
                subtitle = "解析成功后直接按默认画质开始下载",
                checked = preferences.autoDownloadOnParse,
                onCheckedChange = { viewModel.setAutoDownload(it) },
            )
        }

        // 存储管理
        SettingsCard(title = "存储") {
            storageStats?.let { stats ->
                InfoRow(label = "已用空间", value = formatBytes(stats.totalUsedBytes))
                InfoRow(label = "已完成任务", value = "${stats.downloadCount} 个")
            } ?: Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        // 通知与反馈
        SettingsCard(title = "通知与反馈") {
            SwitchRow(
                title = "下载完成通知",
                subtitle = "任务完成后发送系统通知",
                checked = preferences.enableNotifications,
                onCheckedChange = { viewModel.setNotifications(it) },
            )
            SwitchRow(
                title = "触感反馈",
                subtitle = "操作时的轻微振动",
                checked = preferences.enableHapticFeedback,
                onCheckedChange = { viewModel.setHapticFeedback(it) },
            )
        }

        // 关于
        SettingsCard(title = "关于") {
            InfoRow(label = "版本", value = BuildConfig.VERSION_NAME)
            InfoRow(label = "应用", value = "无痕 · 纯本地无水印视频解析")
            Text(
                text =
                    "免责声明：本工具仅供个人学习与研究使用，下载内容请遵循各平台用户协议" +
                        "与著作权相关法律，产生的后果由使用者自行承担。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    return String.format("%.2f GB", mb / 1024.0)
}
