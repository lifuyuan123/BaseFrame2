package com.lfy.baseframe.utils

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.lfy.baseframe.app.App
import timber.log.Timber

/**
 * @Author admin
 * @Date 2022/8/24-9:52
 * @describe:toast单例
 */
object ToastUtils {

    private var oldMsg: String = ""
    private var clickTime: Long = 0//点击的时间
    private val toast by lazy (LazyThreadSafetyMode.SYNCHRONIZED) {
        Toast.makeText(
            App.context?.applicationContext,
            oldMsg,
            Toast.LENGTH_SHORT
        )
    }

    fun toast(msg: Any?) {

        //解决Android Toast has already been added to the window manager
        clearDying(App.context?.applicationContext!!)

        val nowTime = System.currentTimeMillis()
        if (oldMsg == msg.toString()) {
            if (nowTime - clickTime > 1800) {
                toast.setText(msg.toString())
                toast.show()
                clickTime = nowTime
            }
        } else {
            toast.setText(msg.toString())
            toast.show()
            clickTime = nowTime
            oldMsg = msg.toString()
        }
    }

    /**
     * 解决Android Toast has already been added to the window manager
     */
    private fun clearDying(context: Context) {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            Timber.e("HMToast clearDying: $windowManager")
            //反射拿到WindowManagerGlobal
            val mGlobal = windowManager.javaClass.getDeclaredField("mGlobal")
            mGlobal.isAccessible = true
            Timber.e("HMToast clearDying: $mGlobal")
            //反射拿到WindowManagerGlobal#mViews
            val mViewsField = mGlobal.type.getDeclaredField("mViews")
            val mRootsField = mGlobal.type.getDeclaredField("mRoots")
            mViewsField.isAccessible = true
            mRootsField.isAccessible = true
            val mViews: ArrayList<View> = mViewsField.get(mGlobal.get(windowManager)) as ArrayList<View>
            if (mViews.isEmpty()) {
                return
            }
            val hookViews: ArrayList<View> = ArrayList(mViews.size)
            val hookRoots :ArrayList<Any> = ArrayList(mViews.size)
            Timber.e("HMToast clearDying: %s", mViews.size)
            for (i in 0 until mViews.size) {
                //清理parent为null的view
                if (mViews[i].parent != null) {
                    hookViews.add(mViews[i])
                    hookRoots.add((mRootsField.get(mGlobal.get(windowManager)) as ArrayList<View>)[i])
                }
            }
            Timber.e("HMToast clearDying: hookViews%s", hookViews.size)
            if (mViews.size == hookViews.size) {
                //没有脏view
                return
            }
            mViewsField.set(mGlobal.get(windowManager), hookViews)
            mRootsField.set(mGlobal.get(windowManager), hookRoots)
        } catch (e: Exception) {
            Timber.e("HMToast clearDying: %s", e.message)
            e.printStackTrace()
        }
    }

}