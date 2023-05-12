package com.lfy.baselibrary.ui.dialog

import android.app.Dialog
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.lfy.baselibrary.R

/**
 * @Author:  admin
 * @Date:  2021/8/25-17:42
 * @describe:  网络加载弹窗
 */
class ProgresDialog(context: Context) : Dialog(context, R.style.dialog_progress),
    DefaultLifecycleObserver {

    private var animationView: LottieAnimationView


    override fun dismiss() {
        animationView.cancelAnimation()
        super.dismiss()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dismiss()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        dismiss()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        dismiss()
    }

    init {
        setContentView(R.layout.dialog_porgress)
        animationView = findViewById(R.id.animation_view)
        setCanceledOnTouchOutside(false)
    }

}