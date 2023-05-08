package com.company.baseframe.utils

import android.content.Context
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.utils.SandboxTransformUtils

/**
 * @Author admin
 * @Date 2022/5/30-16:34
 * @describe: 自定义沙箱文件处理
 */
class MeSandboxFileEngine : UriToFileTransformEngine{
    override fun onUriToFileAsyncTransform(
        context: Context?,
        srcPath: String?,
        mineType: String?,
        call: OnKeyValueResultCallbackListener?
    ) {
        call?.onCallback(
            srcPath,
            SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
        )
    }
}