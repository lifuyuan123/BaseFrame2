package com.lfy.baselibrary.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gyf.immersionbar.ImmersionBar
import com.lfy.baselibrary.R
import com.lfy.baselibrary.getWidthAndHeight
import me.jessyan.autosize.AutoSize

abstract class BaseCommonDialog<T : ViewDataBinding> :DialogFragment() {

    private var mActivity: Activity? = null
    private var mWindow: Window? = null
    private var mWidthAndHeight: Array<Int?>?=null
    protected lateinit var binding: T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AutoSize.autoConvertDensity(activity, 375f, true)
        binding= DataBindingUtil.inflate(inflater,getLayoutId(),container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //点击外部消失
        dialog!!.setCanceledOnTouchOutside(true)
        mWindow = dialog.window
        mWidthAndHeight = mWindow?.getWidthAndHeight()
        setParams()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner=this
        if (isImmersionBarEnabled()) {
            initImmersionBar()
        }
    }

    /**
     * 是否在Fragment使用沉浸式
     *
     * @return the boolean
     */
    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWidthAndHeight = mWindow?.getWidthAndHeight()
    }

    /**
     * 初始化沉浸式
     */
    protected open fun initImmersionBar() {
        ImmersionBar.with(this).init()
    }

    private fun setParams() {
        val layoutParams = dialog!!.window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = layoutParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏
        setStyle(STYLE_NORMAL, R.style.public_ustomDialog)
    }


    override fun show(
        manager: FragmentManager,
        tag: String?
    ) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace()
        }
    }


    protected abstract fun getLayoutId(): Int


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog!!)
        if (mOnDismissListener != null) {
            mOnDismissListener!!.onDismiss()
        }
    }

    private var mOnDismissListener: OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        mOnDismissListener = onDismissListener
    }

    interface OnDismissListener {
        fun onDismiss()
    }
}