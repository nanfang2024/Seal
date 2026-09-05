package com.wu.hen.platform.common

import com.wu.hen.data.models.VideoInfo
import com.wu.hen.WuHenApp

/**
 * Registry pattern for managing multiple platform parsers
 * Singleton to ensure single instance of registry
 */
class PlatformRegistry private constructor() {

    /**
     * Map of platform name to parser instance
     */
    private val parsers = mutableMapOf<String, VideoPlatformParser>()

    init {
        registerAllParsers()
    }

    /**
     * Register all available platform parsers
     */
    private fun registerAllParsers() {
        // Douyin parser placeholder
        // addParser(DouyinParser())

        // Kuaishou parser placeholder
        // addParser(KuaishouParser())

        // Bilibili parser placeholder
        // addParser(BilibiliParser())

        // Xiaohongshu parser placeholder
        // addParser(XiaohongshuParser())

        // Pipixia parser placeholder
        // addParser(PipixiaParser())
    }

    /**
     * Add a new parser to the registry
     */
    fun addParser(parser: VideoPlatformParser) {
        parsers[parser.platformName] = parser
    }

    /**
     * Find appropriate parser for given URL
     * @return The matching parser or null if no parser found
     */
    fun findParserForUrl(url: String): VideoPlatformParser? {
        return parsers.values.find { it.canParse(url) }
    }

    /**
     * Get all registered parsers
     */
    fun getAllParsers(): List<VideoPlatformParser> {
        return parsers.values.toList()
    }

    /**
     * Check if a specific platform is available
     */
    fun isPlatformAvailable(platformName: String): Boolean {
        return parsers.keys.contains(platformName)
    }

    /**
     * Parse video using appropriate parser
     * @param url The video URL to parse
     * @return Result containing parsed VideoInfo or error
     */
    suspend fun parseVideo(url: String): Result<VideoInfo> {
        val parser = findParserForUrl(url)
            ?: return Result.failure(ParseException("No suitable parser found for URL: $url"))

        try {
            return parser.parse(url)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Get count of registered parsers
     */
    fun parserCount(): Int {
        return parsers.size
    }

    /**
     * Clear all parsers (useful for testing)
     */
    fun clearAll() {
        parsers.clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: PlatformRegistry? = null

        /**
         * Get singleton instance of PlatformRegistry
         */
        fun init() {
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlatformRegistry().also { INSTANCE = it }
            }
        }

        /**
         * Get current instance (must call init() first)
         */
        val current: PlatformRegistry
            get() = INSTANCE ?: throw IllegalStateException("PlatformRegistry not initialized")

        /**
         * Convenience method for parsing without explicit registry reference
         */
        suspend fun parseVideo(url: String): Result<com.wu.hen.data.models.VideoInfo> {
            return current.parseVideo(url)
        }
    }
}

// Placeholder parser implementations (will be implemented in later steps)

abstract class PlatformBaseParser(
    override val platformName: String
) : BaseParser(platformName)

// Placeholder for Douyin parser
class DouyinParser : PlatformBaseParser("douyin") {
    override fun getUrlPatterns(): List<String> {
        return listOf(
            ".*(?i)(www\\.)?(douyin\\.com|iesdouyin\\.com).*"
        )
    }

    override suspend fun parse(videoUrl: String): Result<com.wu.hen.data.models.VideoInfo> {
        TODO("Implement Douyin parsing logic")
    }
}

// Placeholder for Kuaishou parser
class KuaishouParser : PlatformBaseParser("kuaishou") {
    override fun getUrlPatterns(): List<String> {
        return listOf(
            ".*(?i)(www\\.)?(kuaishou\\.com|ks.*)*"
        )
    }

    override suspend fun parse(videoUrl: String): Result<com.wu.hen.data.models.VideoInfo> {
        TODO("Implement Kuaishou parsing logic")
    }
}

// Placeholder for Bilibili parser
class BilibiliParser : PlatformBaseParser("bilibili") {
    override fun getUrlPatterns(): List<String> {
        return listOf(
            ".*(?i)(www\\.)?bilibili\\.com.*"
        )
    }

    override suspend fun parse(videoUrl: String): Result<com.wu.hen.data.models.VideoInfo> {
        TODO("Implement Bilibili parsing logic")
    }
}

// Placeholder for Xiaohongshu parser
class XiaohongshuParser : PlatformBaseParser("xiaohongshu") {
    override fun getUrlPatterns(): List<String> {
        return listOf(
            ".*(?i)(www\\.)?(xhslink\\.com|xiaohongshu\\.com).*"
        )
    }

    override suspend fun parse(videoUrl: String): Result<com.wu.hen.data.models.VideoInfo> {
        TODO("Implement Xiaohongshu parsing logic")
    }
}

// Placeholder for Pipixia parser
class PipixiaParser : PlatformBaseParser("pipixia") {
    override fun getUrlPatterns(): List<String> {
        return listOf(
            ".*(?i)(www\\.)?(pipixia\\.com).*"
        )
    }

    override suspend fun parse(videoUrl: String): Result<com.wu.hen.data.models.VideoInfo> {
        TODO("Implement Pipixia parsing logic")
    }
}
