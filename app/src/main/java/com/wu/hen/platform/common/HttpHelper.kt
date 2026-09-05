package com.wu.hen.platform.common

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client wrapper providing retry logic and error handling
 */
class HttpClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    /**
     * Execute GET request with automatic retry on failure
     * @param url The URL to fetch
     * @param headers Optional custom headers
     * @param maxRetries Maximum number of retry attempts
     * @return Response body or null if all retries failed
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        maxRetries: Int = 3
    ): Result<String> {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                return executeRequest(url, headers)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    // Exponential backoff
                    val delayMs = (200 * Math.pow(2.0, attempt.toDouble())).toLong()
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }

        return Result.failure(lastException ?: ParseException("Request failed after $maxRetries retries"))
    }

    /**
     * Execute POST request with retry logic
     */
    suspend fun post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        contentType: String = "application/json",
        maxRetries: Int = 3
    ): Result<String> {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val requestBuilder = Request.Builder()
                    .url(url)
                    .method("POST", body?.let { okhttp3.RequestBody.create(it.toRequestBody(contentType.toMediaType())) })
                
                headers.forEach { (key, value) ->
                    requestBuilder.addHeader(key, value)
                }
                
                return executeRequest(requestBuilder.build())
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    val delayMs = (200 * Math.pow(2.0, attempt.toDouble())).toLong()
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }

        return Result.failure(lastException ?: ParseException("POST request failed after $maxRetries retries"))
    }

    private suspend fun executeRequest(url: String, headers: Map<String, String>): Result<String> {
        val request = Request.Builder().url(url).headers(headers.toHeaders()).build()
        return executeRequest(request)
    }

    private suspend fun executeRequest(request: Request): Result<String> {
        return try {
            val call = client.newCall(request)
            val response = call.execute()
            
            when {
                response.isSuccessful -> {
                    val body = response.body?.string()
                    response.close()
                    Result.success(body ?: "")
                }
                response.code == 429 -> {
                    response.close()
                    Result.failure(RateLimitException("Rate limit exceeded"))
                }
                else -> {
                    val body = response.body?.string()
                    response.close()
                    Result.failure(ParseException("HTTP ${response.code}: ${body ?: "Unknown error"}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Download file with progress callback
     * @return Absolute path to downloaded file
     */
    suspend fun downloadFile(
        url: String,
        destinationPath: String,
        onProgress: ((Long, Long) -> Unit)? = null
    ): Result<String> {
        // Implementation will use OkHttp's network byte stream
        TODO("Implement download with progress tracking")
    }

    private fun Map<String, String>.toHeaders(): okhttp3.Headers {
        val builder = okhttp3.Headers.Builder()
        this.forEach { (key, value) ->
            builder.add(key, value)
        }
        return builder.build()
    }
}

/**
 * Utility functions for URL manipulation
 */
object UrlUtils {
    /**
     * Check if string is a valid URL
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URI(url).host != null && java.net.URI(url).scheme != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extract domain from URL
     */
    fun getDomain(url: String): String? {
        try {
            val uri = java.net.URI(url)
            return uri.host
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Check if URL matches any of the patterns
     */
    fun matchesPatterns(url: String, patterns: List<String>): Boolean {
        return patterns.any { pattern ->
            try {
                url.matches(pattern.toRegex())
            } catch (e: Exception) {
                false
            }
        }
    }
}
