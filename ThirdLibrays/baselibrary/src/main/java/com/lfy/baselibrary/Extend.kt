package com.lfy.baselibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lfy.baselibrary.entity.PermissType
import org.jetbrains.anko.toast
import timber.log.Timber


val Float.px: Float get() = (this * Resources.getSystem().displayMetrics.density)
val Int.px: Int get() = ((this * Resources.getSystem().displayMetrics.density).toInt())

//防抖动点击事件---------------
inline fun <T : View> T.singleClick(time: Long = 800, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

//兼容点击事件设置为this的情况
fun <T : View> T.singleClick(onClickListener: View.OnClickListener, time: Long = 800) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            onClickListener.onClick(this)
        }
    }
}

var lastTime: Long = 0

//防抖动点击事件---------------
fun Any.click(time: Long = 800, block: () -> Unit) {
    val currentTimeMillis = System.currentTimeMillis()
    if (currentTimeMillis - lastTime > time) {
        lastTime = currentTimeMillis
        block()
    }
}

var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0

//防抖动点击事件---------------


//设置View的可见
fun View.visible(isVisible: Boolean): View {
    visibility = if (isVisible) View.VISIBLE else View.GONE
    return this
}


//activity显示dialog
fun AppCompatActivity.showDialog(dialog: DialogFragment) {
    dialog.show(supportFragmentManager, "TAG")
}

//Fragment
fun Fragment.showDialog(dialog: DialogFragment) {
    parentFragmentManager?.let { dialog.show(it, "TAG") }
}

//获取EditText文本
fun EditText.content() = this.text.toString().trim()


//设置Drawable
fun TextView.setDrawables(mipmap: Int, type: Int = 3, width: Int = 0, height: Int = 0) {
    val drawable = ContextCompat.getDrawable(context,mipmap)
    drawable?.setBounds(
        0,
        0,
        if (width == 0) drawable.minimumWidth else width,
        if (height == 0) drawable.minimumHeight else height
    )
    when (type) {
        1 -> {
            this.setCompoundDrawables(drawable, null, null, null)
        }
        2 -> {
            this.setCompoundDrawables(null, drawable, null, null)
        }
        3 -> {
            this.setCompoundDrawables(null, null, drawable, null)
        }
        4 -> {
            this.setCompoundDrawables(null, null, null, drawable)
        }
    }
}

//加载图片
fun ImageView.loadImage(url: String?, placeholder: Int = 0, errorPic: Int = R.drawable.ic_load_fail) {
    Timber.e("图片  ： $url")
    GlideApp.with(this.context)
        .load(url)
//        .centerCrop() 这类处理交给xml中iv处理
        .error(errorPic)
        .placeholder(placeholder)
        .into(this)
}

//隐藏软键盘
@SuppressLint("ServiceCast")
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = window.currentFocus
    val windowToken = if (currentFocus != null) {
        currentFocus.windowToken
    } else {
        window.decorView.windowToken
    }
    if (windowToken != null) {
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

//获取栈顶activity
fun Context.topActivity(): Activity? {
    return ActivityManager.instance.getTopActivity()
}

//获取栈顶activity
fun Activity.topActivity(): Activity? {
    return ActivityManager.instance.getTopActivity()
}

fun Window.getWidthAndHeight(): Array<Int?>? {
    val integer = arrayOfNulls<Int>(2)
    val dm = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        windowManager.defaultDisplay.getRealMetrics(dm)
    } else {
        windowManager.defaultDisplay.getMetrics(dm)
    }
    integer[0] = dm.widthPixels
    integer[1] = dm.heightPixels
    return integer
}

//dp 转 px
fun Context.dp2px(dpValue: Float): Int {
    val scale: Float =
        this.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

//dp 转 px
fun Activity.dp2px(dpValue: Float): Int {
    val scale: Float =
        this.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}


/**
 * 获取权限快捷方式
 */
fun Activity.permiss(type: PermissType, block:()-> Unit) {

    val permission = XXPermissions.with(this)

    when (type) {
        is PermissType.PermissLocation -> {//定位权限
            permission
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
        }
        is PermissType.PermissPhone -> {//电话权限
            permission
                .permission(Permission.CALL_PHONE)
        }
        is PermissType.PermissRecord -> {//音频权限
            permission
                .permission(Permission.RECORD_AUDIO)
        }
        is PermissType.PermissWrite -> {//读写权限
            permission
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
        }
        is PermissType.PermissCamera -> {//相机权限
            permission
                .permission(Permission.CAMERA)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
        }
        is PermissType.PermissLocationAndWrite -> {//定位加读写
            permission
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.READ_PHONE_STATE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
        }
        is PermissType.PermissCameraWriteRecord -> {//相机读写多媒体
            permission
                .permission(Permission.CAMERA)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.RECORD_AUDIO)
        }
        else -> {}
    }

    permission.request(object : OnPermissionCallback {
        override fun onGranted(permissions: List<String>, all: Boolean) {
            if (all) {
                block.invoke()
            } else {
                toast("获取部分权限成功，部分权限未正常授予")
            }
        }

        override fun onDenied(permissions: List<String>, never: Boolean) {
            if (never) {
                toast("被永久拒绝授权，请手动授予权限")
                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                XXPermissions.startPermissionActivity(this@permiss, permissions)
            } else {
                toast("获取所有权限失败")
            }
        }
    })
}

















