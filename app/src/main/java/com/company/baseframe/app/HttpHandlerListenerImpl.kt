package com.company.baseframe.app

import com.lfy.baselibrary.utils.print.HttpHandlerListener
import com.lfy.baselibrary.utils.print.RequestInterceptor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

/**
 * @Author admin
 * @Date 2022/2/15-15:24
 * @describe: http请求和响应结果处理类
 */
class HttpHandlerListenerImpl : HttpHandlerListener {

    /**
     * 请求前处理
     */
    override fun onHttpRequestBefore(chain: Interceptor.Chain, request: Request): Request {

        val newRequest = chain.request().newBuilder()

        return newRequest.addHeader("123", "321").build()
    }

    /**
     * 处理返回数据
     */
    override fun onHttpResultResponse(
        httpResult: String?,
        chain: Interceptor.Chain,
        response: Response
    ): Response {
        if (!httpResult.isNullOrEmpty()&& RequestInterceptor.isJson(response.body?.contentType())) {
            Timber.e("拦截数据---$httpResult")
        }
        return response
    }


}