package com.example.baseframe.ui.splash

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.baseframe.databinding.ActivitySplashBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.baseframe.R
import com.example.baseframe.ui.mainParent.MainParentActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.delay
import me.jessyan.autosize.internal.CancelAdapt
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, BaseViewModel>() , CancelAdapt {

    override fun getLayout() = R.layout.activity_splash

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this

        lifecycleScope.launchWhenCreated {
            delay(2000)
            startActivity<MainParentActivity>()
            finish()
        }

        XXPermissions.with(this)
            .permission(Permission.CAMERA) // 申请单个权限
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .permission(Permission.ACCESS_FINE_LOCATION)
            .permission(Permission.ACCESS_COARSE_LOCATION)
            .permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
//                        toast("获取所有权限成功")
//                            countDown()
                    } else {
                        toast("获取部分权限成功，部分权限未正常授予")
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        toast("被永久拒绝授权，请手动授予权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@SplashActivity, permissions)
                    } else {
                        toast("获取所有权限失败")
                    }
                }
            })

    }

}