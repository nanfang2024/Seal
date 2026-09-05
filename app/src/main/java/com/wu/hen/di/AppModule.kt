package com.wu.hen.di

import com.wu.hen.data.database.AppDatabase
import com.wu.hen.data.preferences.PreferencesRepository
import com.wu.hen.ui.downloads.DownloadsViewModel
import com.wu.hen.ui.home.HomeViewModel
import com.wu.hen.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin 依赖注入模块
 */
val appModule = module {

    single { AppDatabase.build(androidContext()) }

    single { get<AppDatabase>().downloadRecordDao() }

    single { PreferencesRepository(androidContext()) }

    viewModelOf(::HomeViewModel)

    viewModelOf(::DownloadsViewModel)

    viewModelOf(::SettingsViewModel)
}
