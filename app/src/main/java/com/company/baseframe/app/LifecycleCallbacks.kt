package com.company.baseframe.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gyf.immersionbar.ImmersionBar
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
//            if (ActivityManager.instance.mCurrentActivity == activity) {
//                ActivityManager.instance.mCurrentActivity = null
//            }

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            ActivityManager.instance.removeActivity(activity)
            Timber.e("onActivityDestroyed:  ${activity.localClassName}")

        }
    }
}