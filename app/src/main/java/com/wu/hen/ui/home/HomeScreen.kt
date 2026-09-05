package com.wu.hen.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wu.hen.data.models.VideoFormatOption
import com.wu.hen.data.models.VideoInfo
import com.wu.hen.platform.common.UrlUtils
import org.koin.androidx.compose.koinViewModel

/**
 * 首页：粘贴链接 → 解析 → 选择画质 → 下载
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, initialUrl: String? = null) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var url by rememberSaveable { mutableStateOf(initialUrl.orEmpty()) }
    var formatKey by rememberSaveable { mutableStateOf("") }

    // 分享进入时自动解析
    LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrBlank() && uiState is HomeViewModel.UiState.Idle) {
            viewModel.parse(initialUrl)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "无痕",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "粘贴各大平台分享链接，本地解析无水印视频",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("视频链接") },
                placeholder = { Text("https://v.douyin.com/…") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboardManager.getText()?.text?.let { pasted ->
                            if (UrlUtils.isValidUrl(pasted.trim())) url = pasted.trim()
                        }
                    }) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = "粘贴")
                    }
                },
            )

            Button(
                onClick = { viewModel.parse(url) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is HomeViewModel.UiState.Loading,
            ) {
                if (uiState is HomeViewModel.UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("解析视频")
            }

            when (val state = uiState) {
                is HomeViewModel.UiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HomeViewModel.UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is HomeViewModel.UiState.Success -> {
                    ParsedVideoCard(
                        videoInfo = state.videoInfo,
                        selectedFormatKey = formatKey,
                        onSelectFormat = { format -> formatKey = format.formatId },
                        onDownload = { format ->
                            viewModel.download(state.videoInfo, format)
                        },
                    )
                }
                else -> Unit
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 解析结果卡片：封面、标题、画质选择与下载入口 */
@Composable
private fun ParsedVideoCard(
    videoInfo: VideoInfo,
    selectedFormatKey: String,
    onSelectFormat: (VideoFormatOption) -> Unit,
    onDownload: (VideoFormatOption) -> Unit,
) {
    val effectiveSelected =
        videoInfo.availableFormats.find { it.formatId == selectedFormatKey }
            ?: videoInfo.availableFormats.firstOrNull()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                videoInfo.thumbnailUrl?.let { thumbnail ->
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = null,
                        modifier = Modifier.size(width = 96.dp, height = 64.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = videoInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                    )
                    Text(
                        text = "@${videoInfo.author} · ${videoInfo.platformName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (videoInfo.availableFormats.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    videoInfo.availableFormats.take(4).forEach { format ->
                        FilterChip(
                            selected = format.formatId == effectiveSelected?.formatId,
                            onClick = { onSelectFormat(format) },
                            label = { Text(format.quality.displayName) },
                        )
                    }
                }
            }

            Button(
                onClick = { effectiveSelected?.let(onDownload) },
                modifier = Modifier.fillMaxWidth(),
                enabled = effectiveSelected != null,
            ) {
                Text("下载${effectiveSelected?.quality?.displayName?.let { "（$it）" } ?: ""}")
            }

            Text(
                text = "仅供个人学习与收藏，请勿用于商业用途",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
