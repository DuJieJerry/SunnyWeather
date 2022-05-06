package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

/**
 * P618 仓库层的统一封装
 */
object Repository {
    /**
     * 一般在仓库层定义的方法，为了能将异步获取的数据以响应式变成的方式通知给上一层，通常会返回一个LiveData对象
     *
     * 这里的liveData是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，它可以自动构建并反悔哦一个LiveData对象，
     * 然后在代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了
     *
     * 指定线程为Dispatchers.IO，这样代码块中所有代码都运行在子线程中了
     */
    fun searchPlace(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlace(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    // 这是原来的liveData的方式，这里需要每个方法都自行try catch
//    fun searchPlace(query: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            val placeResponse = SunnyWeatherNetwork.searchPlace(query)
//            if (placeResponse.status == "ok") {
//                val places = placeResponse.places
//                Result.success(places)
//            } else {
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        } catch (e: Exception) {
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)
//    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        // 因为async函数必须在协程作用域下才能调用，所以这里用coroutineScope函数创建了一个协程作用域
        coroutineScope {
            // 由于两个请求没有先后顺序的原因，所以先使用async函数让两个请求并发
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }

            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            // 但是要等到请求都返回数据了才能继续走下面的逻辑，所以这里都用了await()方法，就可以保证只有在两个网络请求都成功响应之后，才进一步执行
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather =
                    Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status} " +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }


    /**
     * P630
     * 按照liveData()函数的参数接收标准定义的一个高阶函数
     * 在fire()函数的内部会先调用一下liveData()函数，
     * 然后在liveData()函数的代码块中统一进行了try catch处理
     * 这样就不用每个方法都自行try catch了
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }


    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}