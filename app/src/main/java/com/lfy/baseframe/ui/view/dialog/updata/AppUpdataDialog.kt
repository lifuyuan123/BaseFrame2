package com.lfy.baseframe.ui.view.dialog.updata

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.azhon.appupdate.listener.OnDownloadListenerAdapter
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.lfy.baseframe.R
import com.lfy.baseframe.databinding.DialogAppUpdataBinding
import com.lfy.baseframe.entity.AppPackageBean
import com.lfy.baseframe.utils.Tags
import com.lfy.baselibrary.ui.dialog.BaseDialogFragment
import com.lfy.baselibrary.visible
import timber.log.Timber

/**
 * @Author admin
 * @Date 2022/10/18-14:43
 * @describe: 升级弹窗
 */
class AppUpdataDialog : BaseDialogFragment<DialogAppUpdataBinding>() {
    private var manager: DownloadManager? = null
    private var appBean: AppPackageBean? = null
    private val apkName = "appupdate.apk"

    override fun getLayout() = R.layout.dialog_app_updata

    companion object {
        fun newInstans(data: AppPackageBean) = AppUpdataDialog().apply {
            arguments = Bundle().apply {
                putParcelable(Tags.DATA, data)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
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
        binding
        appBean = arguments?.getParcelable(Tags.DATA)
        binding.dialog = this
        binding.tvInfo.text =
            "版本：${appBean?.versionName}\n包大小：${appBean?.packageSize}\n更新时间：${appBean?.createTime}"
        binding.tvUpdata.text = "${appBean?.versionDescribe}"

    }

    /**
     * 开始下载
     */
    fun downLoad() {
        val result = ApkUtil.deleteOldApk(
            requireContext(),
            "${requireActivity().externalCacheDir?.path}/$apkName"
        )

        binding.linProgress.visible(true)
        Timber.e("删除旧包：$result")
        manager = DownloadManager.Builder(requireActivity()).run {
            apkUrl(appBean?.downloadAddress ?: "")
            apkName(apkName)
            smallIcon(R.mipmap.ic_launcher)
            onDownloadListener(object : OnDownloadListenerAdapter() {

                override fun downloading(max: Int, progres: Int) {
                    val curr = (progres / max.toDouble() * 100.0).toInt()
                    binding.progress.max = 100
                    binding.progress.progress = curr
                    binding.tvProgress.text = "$curr%"

                    if (curr == 100) {
                        manager?.cancel()
                        dismiss()
                    }
                }
            })
            build()
        }
        manager?.download()
    }


    /**
     * 结束下载，关闭弹窗
     */
    fun cancel() {
        manager?.cancel()
        dismiss()
    }

}