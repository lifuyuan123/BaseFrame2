package com.example.baseframe.utils

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.jdjinsui.gsm.app.Tags
import com.tencent.mmkv.MMKV
import timber.log.Timber

/**
 * @Author:  admin
 * @Date:  2021/8/10-15:32
 * @describe:  获取定位
 */
class LocationManager private constructor (
    private val context: Context,
    private val isOnce: Boolean = false,
    private val locationListener: AMapLocationListener? = null
) : LifecycleObserver {
    private var mAMapLocationClient: AMapLocationClient? = null

    companion object {
        fun newInstance(
            context: Context,
            isOnce: Boolean = false,
            locationListener: AMapLocationListener? = null
        ) = LocationManager(context, isOnce, locationListener)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun createLocation() {
        startLocation()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destoryLocal() {
        destoryLocation()
    }


    /**
     * 开始定位
     */
    private fun startLocation() {
        mAMapLocationClient = AMapLocationClient(context)
        //初始化定位参数
        val option = AMapLocationClientOption()
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        option.isNeedAddress = true
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        if (isOnce) {
            option.isOnceLocation = true
        } else {
            //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
            option.interval = 10 * 1000
        }
        option.isLocationCacheEnable = false
        //设置定位参数
        mAMapLocationClient?.setLocationOption(option)
        if (locationListener != null) {
            mAMapLocationClient?.setLocationListener(locationListener)
        } else {
            //设置定位监听
            mAMapLocationClient?.setLocationListener {
                if (it.errorCode == 0) {

                    MMKV.defaultMMKV().encode(Tags.LATITUDE, it.latitude.toString())
                    MMKV.defaultMMKV().encode(Tags.LONGITUDE, it.longitude.toString())

//                    MMKV.defaultMMKV().encode(Tags.LATITUDE,(it.latitude + (0..6).random() * 0.01).toString())
//                    MMKV.defaultMMKV().encode(Tags.LONGITUDE,(it.longitude + (0..6).random() * 0.01).toString())

                    Timber.e(it.toString())
                } else {
                    Timber.e("${it.errorCode}   ${it.errorInfo}")
                }

            }
        }
        mAMapLocationClient?.startLocation()
    }


    /**
     * 销毁
     */
    private fun destoryLocation() {
        mAMapLocationClient?.stopLocation()
        mAMapLocationClient?.onDestroy()
    }
}