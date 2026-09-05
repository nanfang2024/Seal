package com.wu.hen.platform.common

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * OkHttp 封装：带指数退避的请求重试、统一的错误语义，
 * 以及支持 Range 断点续传与进度回调的文件下载。
 */
class HttpClient(
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
) {

    /** GET 请求，失败自动重试（指数退避） */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        maxRetries: Int = 3,
    ): Result<String> {
        var lastError: Exception? = null
        repeat(maxRetries + 1) { attempt ->
            executeRequest(
                Request.Builder().url(url).headers(headers.toHeaders()).build()
            )
                .onSuccess { return Result.success(it) }
                .onFailure { e ->
                    lastError = e as? Exception ?: RuntimeException(e)
                    if (attempt < maxRetries) delay(backoffMillis(attempt))
                }
        }
        return Result.failure(lastError ?: ParseException("GET 请求失败（已重试 $maxRetries 次）"))
    }

    /** POST 请求，失败自动重试（指数退避） */
    suspend fun post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        contentType: String = "application/json",
        maxRetries: Int = 3,
    ): Result<String> {
        var lastError: Exception? = null
        val requestBody =
            body?.toRequestBody(contentType.toMediaType())
                ?: ByteArray(0).toRequestBody(null)
        repeat(maxRetries + 1) { attempt ->
            executeRequest(
                Request.Builder()
                    .url(url)
                    .headers(headers.toHeaders())
                    .post(requestBody)
                    .build()
            )
                .onSuccess { return Result.success(it) }
                .onFailure { e ->
                    lastError = e as? Exception ?: RuntimeException(e)
                    if (attempt < maxRetries) delay(backoffMillis(attempt))
                }
        }
        return Result.failure(lastError ?: ParseException("POST 请求失败（已重试 $maxRetries 次）"))
    }

    /**
     * 下载文件到本地，已存在部分文件时通过 Range 头断点续传。
     * 协程取消时同步取消底层网络请求，实现"暂停"语义。
     * @return 成功时返回文件的绝对路径
     */
    suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        val destFile = File(destinationPath)
        destFile.parentFile?.mkdirs()

        var existing = if (destFile.exists()) destFile.length() else 0L
        val request =
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .apply { if (existing > 0) header("Range", "bytes=$existing-") }
                .build()

        val call = client.newCall(request)
        // 协程被取消（暂停/取消任务）时，中止底层网络调用
        val cancelHandle =
            coroutineContext.job.invokeOnCompletion { cause ->
                if (cause != null) call.cancel()
            }

        try {
            call.execute().use { response ->
                when {
                    response.code == 206 -> {
                        // 服务端支持续传，追加写入
                    }
                    response.isSuccessful -> {
                        // 服务端不支持 Range，从头下载
                        existing = 0L
                        destFile.delete()
                    }
                    else -> {
                        val snippet = response.body?.string()?.take(200) ?: ""
                        return@withContext Result.failure(
                            ParseException("HTTP ${response.code}: $snippet")
                        )
                    }
                }

                val body =
                    response.body
                        ?: return@withContext Result.failure(ParseException("响应体为空"))
                val total = existing + (body.contentLength().takeIf { it > 0 } ?: -1L)

                body.byteStream().use { input ->
                    FileOutputStream(destFile, response.code == 206).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var downloaded = existing
                        while (true) {
                            ensureActive() // 暂停/取消的响应点
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            onProgress?.invoke(downloaded, total)
                        }
                        output.flush()
                    }
                }
                Result.success(destFile.absolutePath)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // 保持取消语义向上传递
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            cancelHandle?.dispose()
        }
    }

    private suspend fun executeRequest(request: Request): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    when {
                        response.isSuccessful -> Result.success(response.body?.string() ?: "")
                        response.code == 429 ->
                            Result.failure(RateLimitException("请求过于频繁，请稍后再试"))
                        else -> {
                            val snippet = response.body?.string()?.take(200) ?: "未知错误"
                            Result.failure(ParseException("HTTP ${response.code}: $snippet"))
                        }
                    }
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }

    private fun backoffMillis(attempt: Int): Long = (200L shl attempt)

    private fun Map<String, String>.toHeaders(): Headers {
        val builder = Headers.Builder()
        forEach { (key, value) -> builder.add(key, value) }
        return builder.build()
    }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
    }
}

/**
 * 链接工具函数
 */
object UrlUtils {

    /** 是否为合法链接 */
    fun isValidUrl(url: String): Boolean =
        try {
            java.net.URI(url).let { it.scheme != null && it.host != null }
        } catch (_: Exception) {
            false
        }

    /** 提取域名 */
    fun getDomain(url: String): String? =
        try {
            java.net.URI(url).host
        } catch (_: Exception) {
            null
        }

    /** 是否命中任一正则模式 */
    fun matchesPatterns(url: String, patterns: List<String>): Boolean =
        patterns.any { pattern ->
            try {
                Regex(pattern).containsMatchIn(url)
            } catch (_: Exception) {
                false
            }
        }
}
