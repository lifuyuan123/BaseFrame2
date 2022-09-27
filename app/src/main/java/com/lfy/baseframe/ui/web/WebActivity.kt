package com.lfy.baseframe.ui.web

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.JavascriptInterface
import com.lfy.baseframe.databinding.ActivityWebBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.lfy.baseframe.R
import com.gyf.immersionbar.ImmersionBar
import com.lfy.baseframe.utils.Tags
import com.lfy.baselibrary.ui.view.StatusPager
import com.lfy.baselibrary.visible
import com.tencent.mmkv.MMKV
import com.tencent.smtt.export.external.interfaces.*
import com.tencent.smtt.sdk.*
import timber.log.Timber

/**
 * @Author:  admin
 * @Date:  2022/5/31-14:39
 * @describe:  公共web
 */
@AndroidEntryPoint
class WebActivity : BaseActivity<ActivityWebBinding, WebViewModel>() {
    private var mUrl: String? = ""

    //展示各状态下页面
    private val statePager by lazy {
        StatusPager.builder(binding.web)
            .emptyViewLayout(com.lfy.baselibrary.R.layout.state_empty)
            .loadingViewLayout(com.lfy.baselibrary.R.layout.state_loading)
            .errorViewLayout(com.lfy.baselibrary.R.layout.state_error)
            .addRetryButtonId(com.lfy.baselibrary.R.id.btn_retry)
            .setRetryClickListener { _, _ ->
                binding.web.loadUrl(mUrl)
            }
            .build()
    }

    override fun getLayout() = R.layout.activity_web

    override fun onResume() {
        super.onResume()
        if (mUrl.isNullOrEmpty()) {
            setCookies()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this
        initWeb()
        loadData()
    }

    /**
     * 装在数据
     */
    private fun loadData() {
        //是否需要状态栏  默认不需要
        val hasStatusBar = intent.getBooleanExtra(Tags.STATUS, false)
        binding.statusBarView.visible(hasStatusBar)

        //加载html
        val data = intent.getStringExtra(Tags.DATA)
        if (!data.isNullOrEmpty()) {
            binding.web.loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
        }
        mUrl = intent.getStringExtra(Tags.URL)
        //加载url
        binding.web.loadUrl(mUrl)
        statePager.showLoading()
        Timber.e("地址url：$mUrl")
    }

    /**
     * 初始化
     */
    private fun initWeb() {
        window?.setFormat(PixelFormat.TRANSLUCENT)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        binding.web.apply {
            //设置和js交互接口以及参数名
            addJavascriptInterface(JsInterface(this@WebActivity), "base")
            settings.apply {
                //不加这句可能很多页面无法加载
                javaScriptEnabled = true
                //支持插件
                pluginsEnabled = true
                //设置自适应屏幕，两者合用
                useWideViewPort = true //将图片调整到适合webview的大小
                loadWithOverviewMode = true // 缩放至屏幕的大小
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
                useWideViewPort = true
                //缩放操作
                setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
                builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
                displayZoomControls = false //隐藏原生的缩放控件
                defaultTextEncodingName = "utf-8"
                loadsImagesAutomatically = true
                requestFocusFromTouch()
                removeJavascriptInterface("searchBoxJavaBridge_")
                removeJavascriptInterface("accessibility")
                removeJavascriptInterface("accessibilityTraversal")
                cacheMode = WebSettings.LOAD_NO_CACHE
                allowContentAccess = true
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                setGeolocationEnabled(true)
                CookieSyncManager.createInstance(applicationContext)
                setGeolocationEnabled(true)
                setAppCacheMaxSize(Long.MAX_VALUE)
                //允许http和https混合
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode =
                        WebSettings.LOAD_NO_CACHE
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        p0: WebView?,
                        filePathCallback: ValueCallback<Array<Uri?>>?,
                        p2: FileChooserParams?
                    ): Boolean {
                        return true
                    }

                    override fun onProgressChanged(p0: WebView?, p1: Int) {
                        Timber.e("进度$p1")
                        if (p1 > 30) {
                            if (statePager.curState != StatusPager.VIEW_STATE_CONTENT) {
                                statePager.showContent()
                            }
                        }
                        super.onProgressChanged(p0, p1)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(p0: WebView?, p1: String?): Boolean {
                        Timber.e("请求  shouldOverrideUrlLoading   $p1")
                        return false
                    }

                    override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
                        super.onPageStarted(p0, p1, p2)
                        Timber.e("请求  onPageStarted")
                    }

                    override fun onPageFinished(p0: WebView?, p1: String?) {
                        super.onPageFinished(p0, p1)
                        Timber.e("请求  onPageFinished  progress:${p0?.progress}")
                    }

                    override fun shouldInterceptRequest(
                        webView: WebView?,
                        s: String?
                    ): WebResourceResponse? {
                        Timber.e("请求shouldInterceptRequest:  $s")
                        return super.shouldInterceptRequest(webView, s)
                    }

                    override fun shouldInterceptRequest(
                        webView: WebView?,
                        webResourceRequest: WebResourceRequest
                    ): WebResourceResponse? {
                        Timber.e("请求url：${webResourceRequest.url}")
                        Timber.e("请求Header：${webResourceRequest.requestHeaders}")
                        Timber.e("请求url.port：${webResourceRequest.url.port}")
                        Timber.e("请求method：${webResourceRequest.method}")
                        return super.shouldInterceptRequest(webView, webResourceRequest)
                    }

                    override fun onReceivedError(
                        p0: WebView,
                        p1: WebResourceRequest,
                        p2: WebResourceError
                    ) {
                        super.onReceivedError(p0, p1, p2)
                        Timber.e("请求 onReceivedError：  $p1   ${p2.errorCode}     ${p2.description}")
                    }

                    override fun onReceivedError(p0: WebView?, p1: Int, p2: String?, p3: String?) {
                        super.onReceivedError(p0, p1, p2, p3)
                        Timber.e("请求 onReceivedError22：  $p1   $p2")
                    }

                    override fun onReceivedSslError(
                        p0: WebView?,
                        p1: SslErrorHandler?,
                        p2: SslError?
                    ) {
                        Timber.e("请求 onReceivedSslError：  $p1   $p2")
                        //支持https
                        p1?.proceed()
                    }
                }
            }
        }
    }

    /**
     * 配置Cookies
     */
    private fun setCookies() {
        val cookie = MMKV.defaultMMKV().decodeString(Tags.TOKEN)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().removeAllCookie()
        CookieManager.getInstance().setCookie(mUrl, cookie)
        CookieSyncManager.getInstance().sync()
        Timber.e("web_token  $cookie  ")
    }

    //返回上个页面
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.web.canGoBack()) {
            binding.web.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    /**
     * 与js交互接口
     */
    inner class JsInterface(private val mContext: Context) {

        /**
         * 关闭页面
         */
        @JavascriptInterface
        fun back() {
            (mContext as? WebActivity)?.finish()
        }

        /**
         * 获取状态栏高度
         */
        @JavascriptInterface
        fun getStatusBarHeight(): Float {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return ImmersionBar.getStatusBarHeight(this@WebActivity) / displayMetrics.density
        }

        /**
         * 调用js方法
         */
        @JavascriptInterface
        fun js() {
            val json = ""
            binding.web.post {
                binding.web.loadUrl("javascript:window.getPicture('$json')")
            }
        }
    }
}