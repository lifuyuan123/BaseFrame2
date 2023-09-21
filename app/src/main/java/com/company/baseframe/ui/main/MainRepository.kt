package com.company.baseframe.ui.main

import androidx.fragment.app.Fragment
import com.flyco.tablayout.listener.CustomTabEntity
import com.company.baseframe.R
import com.company.baseframe.api.Service
import com.company.baseframe.entity.AppPackageBean
import com.company.baseframe.entity.BaseBean
import com.company.baseframe.ui.test.TestFragment
import javax.inject.Inject

class MainRepository @Inject constructor(private val service: Service) {
    suspend fun getAppInfo(): BaseBean<AppPackageBean> =  service.getAppInfo()


    fun provideIconUnselectedIds() = intArrayOf(
        R.mipmap.icon_home,
        R.mipmap.icon_find,
        R.mipmap.icon_mine
    )

    fun provideIconSelectIds() = intArrayOf(
        R.mipmap.icon_home_choice,
        R.mipmap.icon_find_choice,
        R.mipmap.icon_mine_choice
    )

    fun provideTabEntities() = ArrayList<CustomTabEntity>()

    fun provideTitles() = arrayOf("妹子", "发现", "我的")

    fun provideFragments(): MutableList<Fragment> {
        val fragments = mutableListOf<Fragment>()
        fragments.add(TestFragment.newInstance("1"))
        fragments.add(TestFragment.newInstance("2"))
        fragments.add(TestFragment.newInstance("3"))
        return fragments
    }
}