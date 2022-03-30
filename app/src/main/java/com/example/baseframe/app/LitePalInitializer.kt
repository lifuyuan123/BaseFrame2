package com.example.baseframe.app

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.amap.api.location.AMapLocationClient
import com.example.baseframe.BuildConfig
import com.example.baseframe.R
import com.example.baseframe.utils.CrashHandler
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.tencent.mmkv.MMKV
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.weikaiyun.fragmentation.Fragmentation
import timber.log.Timber
import java.util.*


/**
 * @Author admin
 * @Date 2021/8/5-10:32
 * @describe: 用于执行Initializer初始化
 */
class LitePalInitializer : Initializer<Unit> {

    //这里执行第三方库初始化
    override fun create(context: Context) {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
//            layout.setEnableAutoLoadMore(false)
            MaterialHeader(context).setColorSchemeResources(
                R.color.cl_02AFFE,
                R.color.cl_01D4FF,
                R.color.cl_F1E6A0
            )
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate)
        }

        //当 App 中出现多进程, 并且您需要适配所有的进程, 就需要在 App 初始化时调用 initCompatMultiProcess()
        //在 Demo 中跳转的三方库中的 DefaultErrorActivity 就是在另外一个进程中, 所以要想适配这个 Activity 就需要调用 initCompatMultiProcess()
//        AutoSize.initCompatMultiProcess(context.applicationContext)

        //单activity初始化
        Fragmentation.builder() // 设置 栈视图 模式为 （默认）悬浮球模式   SHAKE: 摇一摇唤出  NONE：隐藏， 仅在Debug环境生效
            .stackViewMode(Fragmentation.SHAKE)
            .debug(BuildConfig.LOG_DEBUG) // 实际场景建议.debug(BuildConfig.DEBUG)
            .animation(
                R.anim.public_translate_right_to_center,  //进入动画
                R.anim.public_translate_center_to_left,  //隐藏动画
                R.anim.public_translate_left_to_center,  //重新出现时的动画
                R.anim.public_translate_center_to_right  //退出动画
            )
            .install()

        //监听崩溃并收集日志
        CrashHandler.instance.init(context.applicationContext)
        //bugly
//        CrashReport.initCrashReport(context.applicationContext, "b18e0d6cf2", BuildConfig.LOG_DEBUG)

        //Timber初始化
        if (BuildConfig.LOG_DEBUG) {
            //Timber 是一个日志框架容器,外部使用统一的Api,内部可以动态的切换成任何日志框架(打印策略)进行日志打印
            //并且支持添加多个日志框架(打印策略),做到外部调用一次 Api,内部却可以做到同时使用多个策略
            //比如添加三个策略,一个打印日志,一个将日志保存本地,一个将日志上传服务器
            Timber.plant(Timber.DebugTree())
        }

        //高德滴入合规检查  需要保证隐私政策合规
        AMapLocationClient.updatePrivacyShow(context, true, true)
        AMapLocationClient.updatePrivacyAgree(context, true)

        //腾讯web
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)

        QbSdk.setDownloadWithoutWifi(true) //非wifi条件下允许下载X5内核
        val cb: QbSdk.PreInitCallback = object : QbSdk.PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，
                //否则表示x5内核加载失败，会自动切换到系统内核。
                Timber.e(" tbs onViewInitFinished is     $arg0")
            }

            override fun onCoreInitFinished() {}
        }
        //x5内核初始化接口
        QbSdk.initX5Environment(context.applicationContext, cb)


        //初始化轻量级储存 替代sharedPreferences
        val rootDir = MMKV.initialize(context)
        Timber.e("rootDir:$rootDir")

        //管理activity栈
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(
            LifecycleCallbacks.activityLifecycleCallbacks
        )
    }

    //当前的LitePalInitializer是否还依赖于其他的Initializer，如果有的话，就在这里进行配置，
    // App Startup会保证先初始化依赖的Initializer，然后才会初始化当前的LitePalInitializer。
    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()

    }
}