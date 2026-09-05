package com.wu.hen.platform.common

import com.wu.hen.data.models.VideoInfo

/**
 * 所有平台解析器的统一契约
 */
interface VideoPlatformParser {
    /** 平台唯一标识（如 douyin / bilibili） */
    val platformName: String

    /** 判断该解析器能否处理给定链接 */
    fun canParse(url: String): Boolean

    /** 解析视频链接，提取无水印直链与元信息（挂起函数，IO 线程执行） */
    suspend fun parse(videoUrl: String): Result<VideoInfo>

    /** 解析器版本，便于调试 */
    fun getVersion(): String = "1.0.0"
}

/**
 * 解析器公共基类：URL 模式匹配、UA 构造等通用能力
 */
abstract class BaseParser(override val platformName: String) : VideoPlatformParser {

    /** 平台链接的正则模式列表（支持部分匹配） */
    abstract fun getUrlPatterns(): List<String>

    /** 从链接中提取视频 ID（子类按需实现） */
    protected fun extractVideoId(url: String): String? = null

    /** 按客户端类型构造真实浏览器/客户端 User-Agent */
    protected fun buildUserAgent(clientType: ClientType = ClientType.ANDROID): String {
        return when (clientType) {
            ClientType.ANDROID ->
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            ClientType.IOS ->
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X)" +
                    " AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1"
            ClientType.WEB ->
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
    }

    override fun canParse(url: String): Boolean {
        return getUrlPatterns().any { pattern ->
            try {
                Regex(pattern).containsMatchIn(url)
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun parse(videoUrl: String): Result<VideoInfo> {
        return Result.failure(ParseException("「$platformName」的解析逻辑尚未实现"))
    }
}

/** 请求模拟的客户端类型 */
enum class ClientType {
    ANDROID,
    IOS,
    WEB,
}

/** 解析相关异常 */
class ParseException(message: String) : Exception(message)

class UnsupportedFormatException(message: String) : Exception(message)

class RateLimitException(message: String) : Exception(message)
