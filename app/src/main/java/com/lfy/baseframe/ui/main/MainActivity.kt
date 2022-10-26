package com.lfy.baseframe.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lfy.baseframe.databinding.ActivityMainBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.lfy.baseframe.R
import com.lfy.baseframe.di.BindSelectIds
import com.lfy.baseframe.di.BindUnselectedIds
import com.lfy.baseframe.entity.TabEntity
import com.lfy.baseframe.ui.test.TestFragment
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.lfy.baseframe.app.launchAndRepeatWithViewLifecycle
import com.lfy.baseframe.ui.view.dialog.updata.AppUpdataDialog
import com.lfy.baseframe.utils.Tags
import com.lfy.baselibrary.Api
import com.lfy.baselibrary.showDialog
import com.weikaiyun.fragmentation.SupportFragment
import com.weikaiyun.fragmentation.SupportHelper
import kotlinx.coroutines.flow.collectLatest
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.toast
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    @Inject
    lateinit var mTitles: Array<String>

    @Inject
    @BindUnselectedIds
    lateinit var mIconUnselectedIds: IntArray

    @Inject
    @BindSelectIds
    lateinit var mIconSelectIds: IntArray

    @Inject
    lateinit var mTabEntities: ArrayList<CustomTabEntity>

    private var homeFragment: Fragment? = null
    private var findFragment: Fragment? = null
    private var mineFragment: Fragment? = null
    private var currentTab = 0
    override fun getLayout() = R.layout.activity_main

    override fun initData(savedInstanceState: Bundle?) {
        RetrofitUrlManager.getInstance().putDomain(Tags.PGYER, Api.PGYER_URL)
        binding.activity = this
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
        val fragment =
            SupportHelper.findFragment(supportFragmentManager, TestFragment::class.java)
        if (fragment != null) {
            homeFragment = fragment
            findFragment =
                SupportHelper.findFragment(supportFragmentManager, TestFragment::class.java)
            mineFragment =
                SupportHelper.findFragment(supportFragmentManager, TestFragment::class.java)
        }
        if (homeFragment == null) homeFragment = TestFragment.newInstance("1")
        if (findFragment == null) findFragment = TestFragment.newInstance("2")
        if (mineFragment == null) mineFragment = TestFragment.newInstance("3")


        loadMultipleRootFragment(
            R.id.fl_container,
            currentTab,
            homeFragment as SupportFragment,
            findFragment as SupportFragment,
            mineFragment as SupportFragment
        )
    }

    private fun initTab() {
        for (i in mTitles.indices) {
            mTabEntities.add(TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectedIds[i]))
        }
        binding.tab.setTabData(mTabEntities)
        binding.tab.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                showFragmentToIndex(position)
                currentTab = position
            }

            //消息数点击
            override fun onTabReselect(position: Int) {

                toast("tab--  $position")
                when (position) {
                    0 -> {

                    }
                    1 -> {

                    }
                    2 -> {

                    }
                }
            }
        })
    }

    /**
     * 根据下标显示fragment
     */
    private fun showFragmentToIndex(index: Int) {
        when (index) {
            0 -> {
                showHideFragment(homeFragment as SupportFragment)
            }
            1 -> {
                showHideFragment(findFragment as SupportFragment)
            }
            2 -> {
                showHideFragment(mineFragment as SupportFragment)
            }
        }
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