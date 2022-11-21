package com.lfy.baselibrary.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.lfy.baselibrary.R
import com.lfy.baselibrary.getWidthAndHeight
import com.gyf.immersionbar.ImmersionBar
import me.jessyan.autosize.AutoSize

/**
 * @Author:  admin
 * @Date:  2021/8/3-17:10
 * @describe:  BaseDialogFragment
 */

abstract class BaseDialogFragment<T : ViewDataBinding> :DialogFragment() {
    private var mActivity: Activity? = null
    protected var mWindow: Window? = null
    private var mWidthAndHeight: Array<Int?>?=null
    protected lateinit var binding: T


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏
        setStyle(STYLE_NORMAL, R.style.MyDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //点击外部消失
        dialog!!.setCanceledOnTouchOutside(true)
        mWindow = dialog.window
        mWidthAndHeight = mWindow?.getWidthAndHeight()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AutoSize.autoConvertDensity(activity, 375f, true)
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner=this
        if (isImmersionBarEnabled()) {
            initImmersionBar()
        }
        initData(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftKeyBoard(mActivity)
    }


    private fun hideSoftKeyBoard(activity: Activity?) {
        val localView = requireActivity().currentFocus
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (localView != null && imm != null) {
            imm.hideSoftInputFromWindow(
                localView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWidthAndHeight = mWindow?.getWidthAndHeight()
    }

    /**
     * 是否在Fragment使用沉浸式
     *
     * @return the boolean
     */
    private fun isImmersionBarEnabled(): Boolean {
        return true
    }

    /**
     * 初始化沉浸式
     */
     protected open fun initImmersionBar() {
        ImmersionBar.with(this).navigationBarColor(R.color.color_bg).init()
    }


    /**
     * 初始化数据
     */
    protected abstract fun initData(view: View?)

    /**
     * 布局
     */
    abstract fun getLayout():Int


}