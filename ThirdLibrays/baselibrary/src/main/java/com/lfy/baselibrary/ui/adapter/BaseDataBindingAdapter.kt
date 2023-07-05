package com.lfy.baselibrary.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lfy.baselibrary.ActivityManager
import com.lfy.baselibrary.Tags
import com.lfy.baselibrary.click
import me.jessyan.autosize.AutoSize

/**
 * @Author admin
 * @Date 2022/8/3-15:14
 * @describe:适配databinding万能适配器
 */
abstract class BaseDataBindingAdapter<T, VB : ViewDataBinding> constructor(
    private val layoutResId: Int,
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, BaseDataBindingViewHolder<VB>>(layoutResId, data) {
    constructor(data: MutableList<T>?) : this(0, data)

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseDataBindingViewHolder<VB> {
        AutoSize.autoConvertDensity(ActivityManager.instance.mCurrentActivity, Tags.with, true)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            layoutResId, parent, false
        )
        return BaseDataBindingViewHolder(binding.root, binding as VB)
    }

    override fun convert(holder: BaseDataBindingViewHolder<VB>, item: T) {
        convertOfVB(holder.getBinding(), holder, item)
        holder.getBinding<VB>()?.executePendingBindings()
    }

    /**
     * binding暴露给实现类
     */
    abstract fun convertOfVB(binding: VB?, helper: BaseDataBindingViewHolder<VB>, item: T)

    override fun setOnItemClick(v: View, position: Int) {
        click {
            super.setOnItemClick(v, position)
        }

    }

    override fun setOnItemChildClick(v: View, position: Int) {
        click {
            super.setOnItemChildClick(v, position)
        }
    }
}

class BaseDataBindingViewHolder<VB : ViewDataBinding> constructor(view: View, binding: VB? = null) :
    BaseViewHolder(view)

