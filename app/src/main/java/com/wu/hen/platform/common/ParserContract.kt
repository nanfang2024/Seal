package com.wu.hen.platform.common

import com.wu.hen.data.models.VideoInfo

/**
 * Base interface for all video platform parsers
 * Each platform (Douyin, Bilibili, etc.) should implement this interface
 */
interface VideoPlatformParser {
    /**
     * Unique identifier for the platform
     */
    val platformName: String

    /**
     * Check if this parser can handle the given URL
     * @param url The URL to check
     * @return true if this parser can parse the URL
     */
    fun canParse(url: String): Boolean

    /**
     * Parse a video URL and extract video information
     * This is an async operation that runs on IO thread
     * @param videoUrl The video URL to parse
     * @return Result containing VideoInfo or error message
     */
    suspend fun parse(videoUrl: String): Result<VideoInfo>

    /**
     * Optional: Get parser version info for debugging/logging
     */
    fun getVersion(): String = "1.0.0"
}

/**
 * Abstract base class providing common utilities for parsers
 */
abstract class BaseParser(override val platformName: String) : VideoPlatformParser {
    
    /**
     * Validate URL pattern matching for a platform
     * Override this method to define your platform's URL patterns
     */
    abstract fun getUrlPatterns(): List<String>

    /**
     * Extract ID from URL if applicable
     * Many platforms use unique IDs in URLs for identification
     */
    protected fun extractVideoId(url: String): String? = null

    /**
     * Build User-Agent header based on client type
     */
    protected fun buildUserAgent(clientType: ClientType = ClientType.ANDROID): String {
        return when (clientType) {
            ClientType.ANDROID -> "${platformName}_Android/${getVersion()}"
            ClientType.IOS -> "${platformName}_iOS/${getVersion()}"
            ClientType.WEB -> "${platformName}_Web/${getVersion()}"
        }
    }

    override fun canParse(url: String): Boolean {
        return getUrlPatterns().any { pattern ->
            try {
                url.matches(pattern.toRegex())
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(NotImplementedException("Parse method not implemented"))
    }
}

/**
 * Types of clients that can be used for API requests
 */
enum class ClientType {
    ANDROID,
    IOS,
    WEB
}

/**
 * Custom exception types for parsing errors
 */
class ParseException(message: String) : Exception(message)

class UnsupportedFormatException(message: String) : Exception(message)

class RateLimitException(message: String) : Exception(message)
