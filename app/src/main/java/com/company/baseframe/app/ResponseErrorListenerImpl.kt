package com.company.baseframe.app


import android.content.Context
import com.lfy.baselibrary.topActivity
import com.lfy.baselibrary.utils.print.ResponseErrorListener
import org.jetbrains.anko.toast
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * @Author:  admin
 * @Date:  2021/8/3-15:04
 * @describe:  网络异常
 */

class ResponseErrorListenerImpl(private val context: Context) :
    ResponseErrorListener {
    override fun handleResponseError(t: Throwable?) {
        var msg = when (t) {
            is UnknownHostException, is ConnectException -> t.message
            is SocketTimeoutException -> "请求网络超时"
            is HttpException -> when (t.code()) {
                500 -> "服务器发生错误"
                404 -> "请求地址不存在"
                403 -> "请求被服务器拒绝"
                307 -> "请求被重定向到其他页面"
                else -> t.message()
            }
            else -> t?.message
        }
        if (t is SocketTimeoutException || t is HttpException || t is ConnectException) {
            toastPlus(msg)
        }
        Timber.e(msg)
    }
}