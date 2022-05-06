package com.sunnyweather.android.logic.model

/**
 * P627 用于将Realtime和Daily对象封装起来
 */
data class Weather(val realtime: RealtimeResponse.RealTime, val daily: DailyResponse.Daily)
