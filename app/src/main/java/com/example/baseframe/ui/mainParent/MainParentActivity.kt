package com.example.baseframe.ui.mainParent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.baseframe.R
import com.example.baseframe.app.LifecycleCallbacks
import com.example.baseframe.databinding.ActivityMainParentBinding
import com.example.baseframe.ui.cameraX.CameraFragment
import com.example.baseframe.ui.main.MainFragment
import com.lfy.baselibrary.toast
import com.yzq.zxinglibrary.common.Constant
import org.jetbrains.anko.toast
import timber.log.Timber

@AndroidEntryPoint
class MainParentActivity : BaseActivity<ActivityMainParentBinding, MainParentViewModel>() {

    private var isLogin = false
    companion object {
        @JvmStatic
        val ISLOGIN = "ISLOGIN"
    }

    override fun getLayout() = R.layout.activity_main_parent

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this

        //监听所有fragment
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            LifecycleCallbacks.fragmentLifecycleCallbacks,
            true
        )

        //判断是否登录  这里目标fragment不同
        isLogin = intent.getBooleanExtra(ISLOGIN, false)
//        var fragment =
//            SupportHelper.findFragment(
//                supportFragmentManager,
//                if (isLogin) WebFragment::class.java else LoginFragment::class.java
//            )
//        if (fragment == null) {
//            fragment = if (isLogin) WebFragment.newInstance().apply {
//                val bundle = Bundle()
//                bundle.putString(WebFragment.URL, Api.GSM_URL)
//                arguments = bundle
//            } else LoginFragment.newInstance()

            loadRootFragment(R.id.container, MainFragment.newInstance())
//        }
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(LifecycleCallbacks.fragmentLifecycleCallbacks)
        super.onDestroy()

    }

    override fun onBackPressedSupport() {
        exitApp()
    }

    private var mPressedTime: Long = 0

    private fun exitApp() {
        val mNowTime = System.currentTimeMillis()
        if (mNowTime - mPressedTime > 2000) {
            toast("再按一次退出程序")
            mPressedTime = mNowTime
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==100&&resultCode == Activity.RESULT_OK) {
            Timber.e("返回数据：${data?.getStringExtra(Constant.CODED_CONTENT)}")
            data?.getStringExtra(Constant.CODED_CONTENT)?.let { toast(it) }
        }
    }
}