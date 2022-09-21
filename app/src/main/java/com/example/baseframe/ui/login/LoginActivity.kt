package com.example.baseframe.ui.login

import android.os.Bundle
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.backgroundColor
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import com.example.baseframe.databinding.ActivityLoginBinding
import com.lfy.baselibrary.ui.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.baseframe.R
import com.example.baseframe.app.toastPlus

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun getLayout() = R.layout.activity_login

    override fun initData(savedInstanceState: Bundle?) {
        binding.activity = this
        initAgreement()//初始化协议
    }

    /**
     * 初始化协议
     */
    private fun initAgreement() {
        //设置此项 ClickableSpan点击后才会有反应
        binding.tvAgreement.movementMethod = LinkMovementMethod()
        binding.tvAgreement.text =
            buildSpannedString {
                backgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.white)) {
                    inSpans(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            toastPlus("我已同意并阅读")
                        }

                        //没有这步重写 文字会有下划线 点击会有背景色
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color =
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    com.lfy.baselibrary.R.color.cl_373737
                                )//文字颜色
                            ds.isUnderlineText = false//是否有下划线
                        }
                    }) {
                        append("我已同意并阅读")
                    }

                    inSpans(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            toastPlus("用户协议")
                        }

                        //没有这步重写 文字会有下划线 点击会有背景色
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color =
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    com.lfy.baselibrary.R.color.cl_1B93D0
                                )//文字颜色
                            ds.isUnderlineText = false//是否有下划线
                        }
                    }) {
                        append("《用户协议》")
                    }

                    color(
                        ContextCompat.getColor(
                            this@LoginActivity,
                            com.lfy.baselibrary.R.color.cl_373737
                        )
                    ) {
                        append("和")
                    }

                    inSpans(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            toastPlus("隐私政策")
                        }

                        //没有这步重写 文字会有下划线 点击会有背景色
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color =
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    com.lfy.baselibrary.R.color.cl_1B93D0
                                )//文字颜色
                            ds.isUnderlineText = false//是否有下划线
                        }

                    }) {
                        append("《隐私政策》")
                    }
                }
            }

    }

}