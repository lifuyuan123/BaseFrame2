package com.example.baseframe.utils

import android.widget.Toast
import com.example.baseframe.app.App

/**
 * @Author admin
 * @Date 2022/8/24-9:52
 * @describe:toast单例
 */
object ToastUtils {

    private var oldMsg: String = ""
    private var clickTime: Long = 0//点击的时间
    private val toast by lazy (LazyThreadSafetyMode.SYNCHRONIZED) {
        Toast.makeText(
            App.context?.applicationContext,
            oldMsg,
            Toast.LENGTH_SHORT
        )
    }

    fun toast(msg: Any?) {
        val nowTime = System.currentTimeMillis()
        if (oldMsg == msg.toString()) {
            if (nowTime - clickTime > 1800) {
                toast.setText(msg.toString())
                toast.show()
                clickTime = nowTime
            }
        } else {
            toast.setText(msg.toString())
            toast.show()
            clickTime = nowTime
            oldMsg = msg.toString()
        }
    }

}