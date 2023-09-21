package com.company.baseframe.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.company.baseframe.databinding.ActivityMainBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.company.baseframe.R
import com.company.baseframe.entity.TabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.company.baseframe.app.launchAndRepeatWithViewLifecycle
import com.company.baseframe.ui.view.dialog.updata.AppUpdataDialog
import com.lfy.baselibrary.showDialog
import com.zy.devicelibrary.utils.FileUtils
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.anko.toast
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    private var currentTab = 0
    override fun getLayout() = R.layout.activity_main

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this
        Timber.e("唯一标识：${FileUtils.getSDDeviceTxt()}")
        initFragment()
        initTab()
        //获取最新应用信息
        viewModel.getAppInfo()
        //应用更新
        launchAndRepeatWithViewLifecycle {
            viewModel.updata.collectLatest {
                if (it.code == 0) {
                    val code = packageManager.getPackageInfo(packageName, 0).versionCode
                    val name = packageManager.getPackageInfo(packageName, 0).packageName
                    if (it.data.buildVersionNo.toInt() > code && name == it.data.buildIdentifier) {
                        val dialog = AppUpdataDialog.newInstans(it.data)
                        showDialog(dialog)
                    }
                }
            }
        }
    }

    private fun initFragment() {
        @SuppressLint("WrongConstant")
        binding.vp.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount(): Int {
                return viewModel.fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return viewModel.titles[position]
            }

            override fun getItem(position: Int): Fragment {
                return viewModel.fragments[position]
            }
        }
    }

    private fun initTab() {
        for (i in viewModel.titles.indices) {
            viewModel.tabEntities.add(
                TabEntity(
                    viewModel.titles[i],
                    viewModel.iconSelectIds[i],
                    viewModel.iconUnselectedIds[i]
                )
            )
        }
        binding.vp.offscreenPageLimit = 2//防止内存泄露
        binding.tab.setTabData(viewModel.tabEntities)
        binding.tab.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                binding.vp.currentItem = position
                currentTab = position
            }

            //消息数点击
            override fun onTabReselect(position: Int) {


            }
        })
        binding.vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.vp.currentItem = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
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
}