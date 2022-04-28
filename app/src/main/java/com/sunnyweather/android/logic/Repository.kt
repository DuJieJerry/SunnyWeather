package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.RuntimeException

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
    fun searchPlace(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlace(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}