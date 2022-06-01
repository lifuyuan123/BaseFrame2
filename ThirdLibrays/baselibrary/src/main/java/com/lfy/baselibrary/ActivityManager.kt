package com.lfy.baselibrary

import android.app.Activity
import android.os.Process
import timber.log.Timber
import java.util.*
import kotlin.system.exitProcess

/**
 * @Author admin
 * @Date 2021/8/20-17:40
 * @describe:  activity管理
 */
class ActivityManager private constructor(){

    /**
     * 管理所有存活的 Activity, 容器中的顺序仅仅是 Activity 的创建顺序, 并不能保证和 Activity 任务栈顺序一致
     */
    private var mActivityList: MutableList<Activity>? = null

    /**
     * 当前在前台的 Activity
     */
    var mCurrentActivity: Activity? = null

    companion object{
        val instance by lazy {
            ActivityManager()
        }
    }


    fun getTopActivity(): Activity? {
        if (mActivityList == null) {
            Timber.e("mActivityList == null when getTopActivity()")
            return null
        }
        return if (mActivityList!!.isNotEmpty()) mActivityList!![mActivityList!!.size - 1] else null
    }


    /**
     * 添加 [Activity] 到集合
     */
    fun addActivity(activity: Activity) {
        synchronized(ActivityManager::class.java) {
            val activities: MutableList<Activity>? = getActivityList()
            activities?.let {
                if (!it.contains(activity)) {
                    it.add(activity)
                }
            }

        }
    }

    /**
     * 返回一个存储所有未销毁的 [Activity] 的集合
     *
     * @return
     */
    private fun getActivityList(): MutableList<Activity>? {
        if (mActivityList == null) {
            mActivityList = LinkedList()
        }
        return mActivityList
    }

    /**
     * 删除集合里的指定的 [Activity] 实例
     *
     * @param {@link Activity}
     */
    fun removeActivity(activity: Activity?) {
        if (mActivityList == null) {
            Timber.e("mActivityList == null when removeActivity(Activity)")
            return
        }
        synchronized(ActivityManager::class.java) {
            if (mActivityList!!.contains(activity)) {
                mActivityList!!.remove(activity)
            }
        }
    }

    /**
     * 关闭所有 [Activity]
     */
    fun killAll() {
        synchronized(ActivityManager::class.java) {
            val iterator = getActivityList()?.iterator()
            iterator?.let {
                while (it.hasNext()) {
                    val next = it.next()
                    it.remove()
                    next.finish()
                }
            }
        }
    }

    /**
     * 关闭所有 [Activity],排除指定的 [Activity]
     *
     * @param excludeActivityClasses activity class
     */
    fun killAll(vararg excludeActivityClasses: Class<*>?) {
        val excludeList = listOf(*excludeActivityClasses)
        synchronized(ActivityManager::class.java) {
            val iterator = getActivityList()?.iterator()
            iterator?.let {
                while (it.hasNext()) {
                    val next = it.next()
                    if (excludeList.contains(next.javaClass)) continue
                    it.remove()
                    next.finish()
                }
            }
        }
    }

    /**
     * 退出应用程序
     * 此方法经测试在某些机型上并不能完全杀死 App 进程,
     * 所以此方法如果不能百分之百保证能杀死进程,
     * 就不能贸然调用 [.release] 释放资源, 否则会造成其他问题
     */
    fun appExit() {
        try {
            killAll()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}