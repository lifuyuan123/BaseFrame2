package com.lfy.baselibrary.utils.print

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @Author admin
 * @Date 2022/2/15-15:16
 * @describe: 处理http请求和响应结果处理接口
 */
interface HttpHandlerListener {
    /**
     * 这里可以先客户端一步拿到每一次 Http 请求的结果, 可以先解析成 Json, 再做一些操作, 如检测到 token 过期后
     * 重新请求 token, 并重新执行请求
     *
     * @param httpResult 服务器返回的结果 (已被框架自动转换为字符串)
     * @param chain      [okhttp3.Interceptor.Chain]
     * @param response   [Response]
     * @return [Response]
     */
    fun onHttpResultResponse(
        httpResult: String?,
        chain: Interceptor.Chain,
        response: Response
    ): Response

    /**
     * 这里可以在请求服务器之前拿到 [Request], 做一些操作比如给 [Request] 统一添加 token 或者 header 以及参数加密等操作
     *
     * @param chain   [okhttp3.Interceptor.Chain]
     * @param request [Request]
     * @return [Request]
     */
    fun onHttpRequestBefore(chain: Interceptor.Chain, request: Request): Request
}