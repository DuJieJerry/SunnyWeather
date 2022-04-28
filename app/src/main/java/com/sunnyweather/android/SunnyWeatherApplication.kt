package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val TOKEN = "这里到时候填写彩云科技API注册的Token"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}