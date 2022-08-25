package com.lfy.baselibrary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.gyf.immersionbar.ImmersionBar
import com.lfy.baselibrary.R
import com.lfy.baselibrary.ui.dialog.ProgresDialog
import com.lfy.baselibrary.visible
import com.lfy.baselibrary.vm.BaseViewModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.weikaiyun.fragmentation.SupportFragment
import me.jessyan.autosize.AutoSize
import java.lang.reflect.ParameterizedType


/**
 * @Author admin
 * @Date 2021/8/6-17:38
 * @describe:  fragment基类
 */
abstract class BaseFragment<T : ViewDataBinding, VM : BaseViewModel> : SupportFragment() {

    protected val progresDialog by lazy {  ProgresDialog(requireContext()) }
    protected lateinit var binding: T
    protected lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        AutoSize.autoConvertDensity(activity, 375f, true)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this

        ImmersionBar.with(this)
            .navigationBarColor(R.color.color_bg)
            .statusBarDarkFont(true, 0.2f)
            .autoStatusBarDarkModeEnable(true, 0.2f)//启用自动根据StatusBar颜色调整深色模式与亮色模式
            .autoNavigationBarDarkModeEnable(true, 0.2f)//启用自动根据NavigationBar颜色调整深色模式与亮色模式
            .init()


        vmProvider()


        //沉浸状态栏
        binding.root.findViewById<View?>(R.id.status_bar_view)?.let {
            ImmersionBar.setStatusBarView(this, it)
        }



        binding.root.findViewById<View?>(R.id.iv_left)?.let {
            it.setOnClickListener { pop() }
        }
        binding.root.findViewById<View?>(R.id.lin_left)?.let {
            it.setOnClickListener { pop() }
        }

        initData(savedInstanceState)

    }

    /**
     * 获取viewModel
     */
    private fun vmProvider() {
        val viewModelClass: Class<BaseViewModel>
        val type = javaClass.genericSuperclass
        viewModelClass = if (type is ParameterizedType) {
            type.actualTypeArguments[1] as Class<BaseViewModel> //获取第1个注解即VM的注解类型
        } else {
            //使用父类的类型
            BaseViewModel::class.java
        }
        viewModel = ViewModelProvider(this)[viewModelClass] as VM

        //监听加载弹窗
        viewModel.loadEvent.observe(viewLifecycleOwner){
            if (it){
                progresDialog.show()
            }else{
                progresDialog.hide()
                binding.root.findViewById<SmartRefreshLayout?>(R.id.smart)?.finishRefresh()
                binding.root.findViewById<SmartRefreshLayout?>(R.id.smart)?.finishLoadMore()
            }
        }
    }

    abstract fun getLayout(): Int

    abstract fun initData(savedInstanceState: Bundle?)

    /**
     * 设置标题
     */
    protected fun title(title: String) {
        binding.root.findViewById<TextView>(R.id.toolbar_title)?.let {
            it.text = title
        }
    }

    /**
     * 隐藏左边按钮
     */
    protected fun setLeftVisible(isVisible: Boolean) {
        binding.root.findViewById<View>(R.id.lin_left)?.let {
            it.visible(isVisible)
        }
    }

    /**
     * 隐藏右边按钮
     */
    protected fun setRightVisible(isVisible: Boolean) {
        binding.root.findViewById<View>(R.id.lin_right)?.let {
            it.visible(isVisible)
        }
    }


}