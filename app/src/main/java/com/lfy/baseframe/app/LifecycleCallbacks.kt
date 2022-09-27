package com.lfy.baseframe.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.SupportRequestManagerFragment
import com.lfy.baselibrary.ActivityManager
import timber.log.Timber

/**
 * @Author admin
 * @Date 2022/1/12-14:31
 * @describe:
 */
object LifecycleCallbacks {

    /**
     * activity监听管理
     */
    val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            ActivityManager.instance.addActivity(activity)
            Timber.e("onActivityCreated:  ${activity.localClassName}")

        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            Timber.e("onActivityResumed:  ${activity.localClassName}")
            ActivityManager.instance.mCurrentActivity = activity

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {
            Timber.e("onActivityStopped:  ${activity.localClassName}")
            if (ActivityManager.instance.mCurrentActivity == activity) {
                ActivityManager.instance.mCurrentActivity = null
            }

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            ActivityManager.instance.removeActivity(activity)
            Timber.e("onActivityDestroyed:  ${activity.localClassName}")

        }

    }

    /**
     * fragment监听管理
     */
    val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
            super.onFragmentAttached(fm, f, context)
            if (f !is SupportRequestManagerFragment)
            Timber.e("栈中添加的fragment：onFragmentAttached  ${f::class.java.canonicalName}")
        }

        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentCreated(fm, f, savedInstanceState)
        }

        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            super.onFragmentStarted(fm, f)
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentResumed(fm, f)
            ActivityManager.instance.getTopActivity()?.let {
                ImmersionBar.with(it)
                    .statusBarDarkFont(true)
                    .init()
            }

        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentDestroyed(fm, f)
            if (f !is SupportRequestManagerFragment)
            Timber.e("栈中添加的fragment：  onFragmentDestroyed  ${f::class.java.canonicalName}")
        }
    }


}