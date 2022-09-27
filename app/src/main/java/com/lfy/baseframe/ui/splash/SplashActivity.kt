package com.lfy.baseframe.ui.splash

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lfy.baseframe.databinding.ActivitySplashBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.lfy.baseframe.R
import com.lfy.baseframe.ui.main.MainActivity
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.delay
import me.jessyan.autosize.internal.CancelAdapt
import org.jetbrains.anko.startActivity

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, BaseViewModel>() , CancelAdapt {

    override fun getLayout() = R.layout.activity_splash

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this

        lifecycleScope.launchWhenCreated {
            delay(200)
            startActivity<MainActivity>()
            finish()
        }
    }

}