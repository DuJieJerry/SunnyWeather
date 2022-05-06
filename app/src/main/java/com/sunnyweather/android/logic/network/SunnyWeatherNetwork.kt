package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * P617 定义一个统一的网络数据源访问入口
 */
object SunnyWeatherNetwork {
    // 创建一个PlaceService接口的动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    // 由于需要借助协程技术来实现，因此声明成挂起函数
    suspend fun searchPlace(query: String) = placeService.searchPlaces(query).await()

    // 创建一个WeatherService接口的动态代理对象
    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getRealtimeWeather(lng: String, lat: String) =
        weatherService.getRealtimeWeather(lng, lat).await()

    suspend fun getDailyWeather(lng: String, lat: String) =
        weatherService.getDailyWeather(lng, lat).await()

    // 由于需要借助协程技术来实现，因此声明成挂起函数
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}