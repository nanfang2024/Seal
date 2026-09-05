package com.wu.hen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wu.hen.ui.MainScreen
import com.wu.hen.ui.theme.WuHenTheme

/**
 * 主界面：承载三 Tab（首页 / 下载 / 设置）
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = intent?.getStringExtra(ShareReceiverActivity.EXTRA_SHARED_URL)

        setContent { WuHenTheme { MainScreen(sharedUrl = sharedUrl) } }
    }
}

/**
 * 接收其他应用分享的视频链接，转交主界面解析
 */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText =
            intent?.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent?.getStringExtra(Intent.EXTRA_SUBJECT)

        if (!sharedText.isNullOrBlank()) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_SHARED_URL, sharedText)
                }
            )
        }
        finish()
    }

    companion object {
        const val EXTRA_SHARED_URL = "com.wu.hen.EXTRA_SHARED_URL"
    }
}
