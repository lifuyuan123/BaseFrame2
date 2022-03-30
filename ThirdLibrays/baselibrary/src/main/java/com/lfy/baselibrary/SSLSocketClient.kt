package com.lfy.baselibrary

import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * @Author:  admin
 * @Date:  2021/8/3-11:31
 * @describe:  证书相关
 */
object SSLSocketClient {
    //获取这个SSLSocketFactory
    fun getSSLSocketFactory(): SSLSocketFactory {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, getTrustManagers(), SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    //获取TrustManager
    private fun getTrustManagers(): Array<TrustManager> {
        return arrayOf(getTrustManager())
    }

    //获取HostnameVerifier
    fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { s, sslSession -> true }
    }

    fun getTrustManager(): X509TrustManager {
        return MyTrustManager()
    }

    private class MyTrustManager : X509TrustManager {

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {

        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {

        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }
}