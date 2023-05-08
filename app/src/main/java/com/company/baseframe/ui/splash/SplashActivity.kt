package com.company.baseframe.ui.splash

import android.os.Bundle
import com.company.baseframe.databinding.ActivitySplashBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.company.baseframe.R
import com.company.baseframe.ui.login.LoginActivity
import com.company.baseframe.ui.main.MainActivity
import com.company.baseframe.ui.view.dialog.ProtocolDialog
import com.company.baseframe.utils.Tags
import com.lfy.baselibrary.showDialog
import com.lfy.baselibrary.vm.BaseViewModel
import com.tencent.mmkv.MMKV
import me.jessyan.autosize.internal.CancelAdapt
import org.jetbrains.anko.startActivity

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, BaseViewModel>(), CancelAdapt {

    override fun getLayout() = R.layout.activity_splash

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this

        //第一次打开app
        if (!MMKV.defaultMMKV().decodeBool(Tags.IS_FIRST_COME)) {
            val dialog = ProtocolDialog()
            showDialog(dialog)
            dialog.callBack = object : ProtocolDialog.CallBack {
                override fun onAffirm(isAffrim: Boolean) {
                    if (isAffrim) {

                        if (false) {
                            startActivity<LoginActivity>()
                        } else {
                            startActivity<MainActivity>()
                        }
                        MMKV.defaultMMKV().encode(Tags.IS_FIRST_COME, true)
                    }
                    finish()
                }
            }
        } else {
            if (false) {
                startActivity<LoginActivity>()
            } else {
                startActivity<MainActivity>()
            }
            finish()
        }
    }

}