package com.company.baseframe.ui.test

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.company.baseframe.R
import com.company.baseframe.databinding.GankItemBinding
import com.company.baseframe.entity.Bean
import com.lfy.baselibrary.ui.adapter.BasePagingDataAdapter
import me.jessyan.autosize.AutoSizeConfig
import javax.inject.Inject

/**
 * gank适配器
 */
class TestAdapter @Inject constructor() : BasePagingDataAdapter<Bean>() {

    override fun getLayout() = R.layout.gank_item

    override fun bindData(binding: ViewDataBinding, bean: Bean, position: Int) {
        (binding as GankItemBinding).bean = bean
        //这句代码放在  binding.bean=bean 前面会崩
        binding.executePendingBindings()
        val width = AutoSizeConfig.getInstance().screenWidth
        val params: ViewGroup.LayoutParams = binding.cl.layoutParams

        if (position == 0) {
            params.height = (width / 2.3 * 0.75).toInt()
        } else {
            params.height = (width / 2.3 * 1.5).toInt()
        }
        binding.cl.layoutParams = params
    }
}