package com.lfy.baselibrary.ui.dialog

import android.app.Dialog
import android.content.Context
import com.airbnb.lottie.LottieAnimationView
import com.lfy.baselibrary.R

/**
 * @Author:  admin
 * @Date:  2021/8/25-17:42
 * @describe:  网络加载弹窗
 */
class ProgresDialog(context: Context) : Dialog(context, R.style.dialog_progress) {

    private var animationView: LottieAnimationView

    override fun show() {
        super.show()
        animationView.playAnimation()
    }

    override fun dismiss() {
        animationView.cancelAnimation()
        super.dismiss()
    }

    init {
        setContentView(R.layout.dialog_porgress)
        animationView = findViewById(R.id.animation_view)
        setCanceledOnTouchOutside(false)
    }

}