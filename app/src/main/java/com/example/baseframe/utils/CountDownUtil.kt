package com.example.baseframe.utils

import android.os.CountDownTimer
import com.tencent.mmkv.MMKV

/**
 * @Author admin
 * @Date 2021/8/20-10:47
 * @describe:倒计时
 */
object CountDownUtil {

    private const val KEY_TIME = "TotalTimeMills"
    private var mTotalTimeMills = 0L
    private var mListener: OnCountDownListener? = null

    private val mCountDownTimer: CountDownTimer
        get() {
            return object : CountDownTimer(getCountTimeMills(),1000){
                override fun onTick(millisUntilFinished: Long) {
                    mListener?.onTick(millisUntilFinished/1000)
                }

                override fun onFinish() {
                    mListener?.onFinish()
                }

            }
        }

    /**
     * 开始倒计时
     * [totalTimeMills] 倒计时时间毫秒值
     */
    fun start(totalTimeMills: Long){
        mTotalTimeMills = totalTimeMills+System.currentTimeMillis()
        MMKV.defaultMMKV().encode(KEY_TIME, mTotalTimeMills)
        mCountDownTimer.start()
    }

    /**
     * 继续倒计时
     */
    fun continueCount(){
        mCountDownTimer.start()
    }

    /**
     * 取消倒计时
     */
    fun cancel(){
        mCountDownTimer.cancel()
    }

    /**
     * 获取当前的倒计时时间毫秒值
     */
    fun getCountTimeMills(): Long{
        mTotalTimeMills = MMKV.defaultMMKV().decodeLong(KEY_TIME)
        return mTotalTimeMills - System.currentTimeMillis()
    }

    /**
     * 当前时间是否在倒计时范围内
     * @return true: 倒计时正在进行 false:倒计时未进行
     */
    fun isCounting(): Boolean{
        mTotalTimeMills = MMKV.defaultMMKV().decodeLong(KEY_TIME)
        return System.currentTimeMillis() < mTotalTimeMills
    }

    /**
     * 设置监听器
     */
    fun setOnCountDownListener(listener: OnCountDownListener){
        mListener = listener
    }

    interface OnCountDownListener{
        /**
         * 倒计时
         * [seconds] 倒计时剩余时间，秒为单位
         */
        fun onTick(seconds: Long)

        /**
         * 倒计时结束
         */
        fun onFinish()
    }
}