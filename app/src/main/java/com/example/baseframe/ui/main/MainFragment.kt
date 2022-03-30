package com.example.baseframe.ui.main

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.baseframe.databinding.FragmentMainBinding
import com.lfy.baselibrary.ui.fragment.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.baseframe.R
import com.example.baseframe.di.BindSelectIds
import com.example.baseframe.di.BindUnselectedIds
import com.example.baseframe.entity.TabEntity
import com.example.baseframe.ui.cameraX.CameraFragment
import com.example.baseframe.ui.test.TestFragment
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.lfy.baselibrary.toast
import com.weikaiyun.fragmentation.SupportFragment
import com.weikaiyun.fragmentation.SupportHelper
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

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

    private var homeFragment: Fragment?=null
    private var findFragment: Fragment?=null
    private var mineFragment: Fragment?=null
    private var currentTab = 0

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun getLayout() = R.layout.fragment_main

    override fun initData(savedInstanceState: Bundle?) {
        binding.fragment = this

        initFragment()
        initTab()
    }

    private fun initFragment() {
        val fragment =
            SupportHelper.findFragment(childFragmentManager, TestFragment::class.java)
        if (fragment != null) {
            homeFragment = fragment
            findFragment =
                SupportHelper.findFragment(childFragmentManager, TestFragment::class.java)
            mineFragment =
                SupportHelper.findFragment(childFragmentManager, TestFragment::class.java)
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

    /**
     * 此处用于接收其自身包括子fragment回调的处理
     */
    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        if (requestCode==100&&resultCode == Activity.RESULT_OK) {
            Timber.e("返回数据：${data?.getString(CameraFragment.CODE_DATA)}")
            data?.getString(CameraFragment.CODE_DATA)?.let { toast(it) }
        }
    }
}