package com.company.baseframe.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import dagger.hilt.android.HiltAndroidApp

/**
 * @Author admin
 * @Date 2021/8/2-14:12
 * @describe:app
 */
@HiltAndroidApp
class App : Application() {


    companion object {
        var context: Context? = null
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}