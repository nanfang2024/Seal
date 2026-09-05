package com.wu.hen

import android.app.Application
import com.tencent.mmkv.MMKV

/**
 * Main application class for WuHen app
 * Initializes global dependencies and configurations
 */
class WuHenApp : Application() {

    companion object {
        lateinit var instance: WuHenApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize MMKV for fast shared preferences
        MMKV.initialize(this)

        // Initialize other singleton managers if needed
        PlatformRegistry.init()
    }
}
