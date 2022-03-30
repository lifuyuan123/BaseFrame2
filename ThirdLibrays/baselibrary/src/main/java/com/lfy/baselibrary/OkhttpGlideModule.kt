package com.lfy.baselibrary

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.net.ssl.SSLContext

/**
 * @Author admin
 * @Date 2021/10/21-15:27
 * @describe:Okhttp3添加https自签名证书以及Glide4.X添加https自签名证书
 */
@GlideModule
class OkhttpGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        replace(registry)
    }

    private fun replace(registry: Registry) {

        val builder = OkHttpClient.Builder()
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(SSLSocketClient.getTrustManager()), SecureRandom())
            builder.sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory(),
                SSLSocketClient.getTrustManager()
            )
                .hostnameVerifier { _, _ -> true }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        val okhttpClient = builder.build()
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okhttpClient)
        )

    }
}