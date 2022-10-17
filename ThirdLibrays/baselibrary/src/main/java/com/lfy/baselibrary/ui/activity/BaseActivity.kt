package com.lfy.baselibrary.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.lfy.baselibrary.R
import com.gyf.immersionbar.ImmersionBar
import com.lfy.baselibrary.ui.dialog.ProgresDialog
import com.lfy.baselibrary.vm.BaseViewModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.weikaiyun.fragmentation.SupportActivity
import java.lang.reflect.ParameterizedType

/**
 * @Author admin
 * @Date 2021/8/6-17:38
 * @describe:  activity基类
 */
abstract class BaseActivity<T:ViewDataBinding,VM : BaseViewModel> :SupportActivity() {

    protected val progresDialog by lazy { ProgresDialog(this)  }
    protected lateinit var binding: T
    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(progresDialog)
        ImmersionBar.with(this)
            .navigationBarColor(R.color.color_bg)
            .statusBarDarkFont(true, 0.2f)
            .autoStatusBarDarkModeEnable(true,0.2f)//启用自动根据StatusBar颜色调整深色模式与亮色模式
            .autoNavigationBarDarkModeEnable(true,0.2f)//启用自动根据NavigationBar颜色调整深色模式与亮色模式
            .init()


        vmProvider()

        binding= DataBindingUtil.setContentView(this, getLayout())
        binding.lifecycleOwner=this


        //沉浸状态栏
        binding.root.findViewById<View?>(R.id.status_bar_view)?.let{
            ImmersionBar.setStatusBarView(this, it)
        }


        binding.root.findViewById<View?>(R.id.iv_left)?.let {
            it.setOnClickListener { finish() }
        }
        binding.root.findViewById<View?>(R.id.lin_left)?.let {
            it.setOnClickListener { finish() }
        }

        //设置标题
        findViewById<View>(R.id.toolbar_title)?.let {
            if(it is TextView){
                it.text=title
            }
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
        viewModel.loadEvent.observe(this){
            if (it){
                loadShow()
                progresDialog.show()
            }else{
                loadHide()
                progresDialog.hide()
                binding.root.findViewById<SmartRefreshLayout?>(R.id.smart)?.finishRefresh()
                binding.root.findViewById<SmartRefreshLayout?>(R.id.smart)?.finishLoadMore()
            }
        }
    }

    open fun loadHide(){}

    open fun loadShow(){}

    abstract fun initData(savedInstanceState: Bundle?)

    abstract fun getLayout():Int
}