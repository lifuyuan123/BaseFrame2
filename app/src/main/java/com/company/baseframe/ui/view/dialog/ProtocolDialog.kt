package com.company.baseframe.ui.view.dialog

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.company.baseframe.R
import com.company.baseframe.databinding.DialogProtocolBinding
import com.lfy.baselibrary.ui.dialog.BaseDialogFragment

/**
 * @Author admin
 * @Date 2022/10/14-15:05
 * @describe: 协议弹窗
 */
class ProtocolDialog : BaseDialogFragment<DialogProtocolBinding>() {

    override fun getLayout() = R.layout.dialog_protocol

    override fun onStart() {
        super.onStart()
        mWindow?.setGravity(Gravity.CENTER)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun initData(view: View?) {
        binding.dialog = this
    }

    /**
     * 跳转协议
     */
    fun goProtocol(type: Int = 1) {
        var url = when (type) {
            1 -> "https://www.baidu.com/"
            else -> ""
        }
        startActivity(Intent().apply {
            action = "android.intent.action.VIEW"
            data = Uri.parse(url)
        })
    }


    fun affirm(isAffrim: Boolean) {
        callBack?.onAffirm(isAffrim)
        dismiss()
    }

    fun onClick() {}

    //单选
    interface CallBack {
        fun onAffirm(isAffrim: Boolean)
    }

    var callBack: CallBack? = null
}