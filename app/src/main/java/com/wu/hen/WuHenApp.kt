package com.wu.hen

import android.app.Application
import com.tencent.mmkv.MMKV
import com.wu.hen.di.appModule
import com.wu.hen.platform.common.PlatformRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * 应用入口：初始化 MMKV、Koin 与平台解析器注册表
 */
class WuHenApp : Application() {

    companion object {
        lateinit var instance: WuHenApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 轻量键值存储
        MMKV.initialize(this)

        // 依赖注入容器
        startKoin {
            androidContext(this@WuHenApp)
            modules(appModule)
        }

        // 注册各平台解析器
        PlatformRegistry.init()
    }
}
