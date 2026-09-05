package com.wu.hen.platform.common

import com.wu.hen.data.models.VideoInfo

/**
 * 平台解析器注册表：按 URL 模式路由到对应平台的解析器。
 */
class PlatformRegistry private constructor() {

    private val parsers = LinkedHashMap<String, VideoPlatformParser>()

    init {
        registerAllParsers()
    }

    private fun registerAllParsers() {
        addParser(DouyinParser())
        addParser(KuaishouParser())
        addParser(BilibiliParser())
        addParser(XiaohongshuParser())
        addParser(PipixiaParser())
    }

    fun addParser(parser: VideoPlatformParser) {
        parsers[parser.platformName] = parser
    }

    /** 按链接找到合适的解析器 */
    fun findParserForUrl(url: String): VideoPlatformParser? =
        parsers.values.find { it.canParse(url) }

    fun getAllParsers(): List<VideoPlatformParser> = parsers.values.toList()

    fun isPlatformAvailable(platformName: String): Boolean = platformName in parsers

    /** 解析视频链接 */
    suspend fun parseVideo(url: String): Result<VideoInfo> {
        val parser =
            findParserForUrl(url)
                ?: return Result.failure(ParseException("暂不支持该链接对应的平台：$url"))
        return runCatching { parser.parse(url).getOrThrow() }
    }

    fun parserCount(): Int = parsers.size

    fun clearAll() {
        parsers.clear()
    }

    companion object {
        @Volatile private var INSTANCE: PlatformRegistry? = null

        /** 初始化（幂等） */
        fun init(): PlatformRegistry =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE ?: PlatformRegistry().also { INSTANCE = it }
                }

        val current: PlatformRegistry
            get() =
                INSTANCE
                    ?: throw IllegalStateException("PlatformRegistry 尚未初始化，请先调用 init()")

        suspend fun parseVideo(url: String): Result<VideoInfo> = current.parseVideo(url)
    }
}

/** 各平台解析器的中间基类 */
abstract class PlatformBaseParser(platformName: String) : BaseParser(platformName)

/** 抖音 / TikTok 中国版 */
class DouyinParser : PlatformBaseParser("douyin") {
    override fun getUrlPatterns(): List<String> =
        listOf("(?i)(douyin|iesdouyin)\\.com")

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        // Phase 3 接入真实解析逻辑
        return Result.failure(ParseException("抖音解析暂未实现"))
    }
}

/** 快手 */
class KuaishouParser : PlatformBaseParser("kuaishou") {
    override fun getUrlPatterns(): List<String> =
        listOf("(?i)(kuaishou|chenzhongtech|gifshow)\\.com")

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(ParseException("快手解析暂未实现"))
    }
}

/** 哔哩哔哩 */
class BilibiliParser : PlatformBaseParser("bilibili") {
    override fun getUrlPatterns(): List<String> =
        listOf("(?i)(bilibili|b23\\.tv|bilivideo)\\.(com|cn)")

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(ParseException("哔哩哔哩解析暂未实现"))
    }
}

/** 小红书 */
class XiaohongshuParser : PlatformBaseParser("xiaohongshu") {
    override fun getUrlPatterns(): List<String> =
        listOf("(?i)(xiaohongshu|xhslink)\\.com")

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(ParseException("小红书解析暂未实现"))
    }
}

/** 皮皮虾 */
class PipixiaParser : PlatformBaseParser("pipixia") {
    override fun getUrlPatterns(): List<String> =
        listOf("(?i)(pipixia|pipix)\\.(com|cn)")

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(ParseException("皮皮虾解析暂未实现"))
    }
}
