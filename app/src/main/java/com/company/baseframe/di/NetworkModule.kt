package com.company.baseframe.di

import android.content.Context
import com.company.baseframe.BuildConfig
import com.lfy.baselibrary.Api.BASE_URL
import com.company.baseframe.api.Service
import com.google.gson.Gson
import com.hjq.gson.factory.GsonFactory
import com.company.baseframe.app.HttpHandlerListenerImpl
import com.company.baseframe.app.ResponseErrorListenerImpl
import com.lfy.baselibrary.SSLSocketClient
import com.lfy.baselibrary.utils.print.RequestInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @Author admin
 * @Date 2021/8/4-17:15
 * @describe: 网络请求容器  实体用@Provides   接口用@Binds
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECTTIMEOUTSECOND = 10 * 1000L
    private const val READTIMEOUTSECOND = 10 * 1000L

    /**
     * 网络异常监听实现类
     */
    @Provides
    @Singleton
    fun provideResponseErrorListener(@ApplicationContext context: Context) =
        ResponseErrorListenerImpl(context)

    /**
     * 网络请求和响应类
     */
    @Provides
    @Singleton
    fun provideHttpHandlerListener() = HttpHandlerListenerImpl()

    @Provides
    @Singleton
    fun provideInterceptor(
        responseErrorListenerImpl: ResponseErrorListenerImpl,
        handlerListenerImpl: HttpHandlerListenerImpl
    ): RequestInterceptor {
        return RequestInterceptor(
            if (BuildConfig.LOG_DEBUG) RequestInterceptor.Level.ALL else RequestInterceptor.Level.NONE,
            responseErrorListenerImpl,
            handlerListenerImpl
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(requestInterceptor: RequestInterceptor): OkHttpClient {
        return RetrofitUrlManager.getInstance().with(OkHttpClient.Builder())
            .addNetworkInterceptor(requestInterceptor)//日志打印拦截
            .addInterceptor(Interceptor { chain ->//请求前拦截处理
                chain.proceed(
                    requestInterceptor.handlerListenerImpl.onHttpRequestBefore(
                        chain,
                        chain.request()
                    )
                )
            })
            .connectTimeout(CONNECTTIMEOUTSECOND, TimeUnit.MILLISECONDS)
            .readTimeout(READTIMEOUTSECOND, TimeUnit.MILLISECONDS)
            .writeTimeout(READTIMEOUTSECOND, TimeUnit.MILLISECONDS)
            //添加证书 https
            .sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory(),
                SSLSocketClient.getTrustManager()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            //gson容错
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideService(retrofit: Retrofit): Service {
        return retrofit.create(Service::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonFactory.getSingletonGson()
    }
}