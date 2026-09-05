package com.wu.hen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.wu.hen.platform.common.UrlUtils

/** 链接工具单元测试 */
class UrlUtilsTest {

    @Test
    fun `合法链接通过校验`() {
        assertTrue(UrlUtils.isValidUrl("https://v.douyin.com/AbCdEf/"))
        assertTrue(UrlUtils.isValidUrl("https://www.bilibili.com/video/BV1xx411c7mD"))
    }

    @Test
    fun `非链接文本被拒绝`() {
        assertEquals(false, UrlUtils.isValidUrl("这是一段普通文本"))
        assertEquals(false, UrlUtils.isValidUrl(""))
    }

    @Test
    fun `域名提取正确`() {
        assertEquals("v.douyin.com", UrlUtils.getDomain("https://v.douyin.com/AbCdEf/"))
    }
}
