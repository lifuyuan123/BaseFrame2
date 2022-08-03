package com.lfy.baselibrary.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * @Author admin
 * @Date 2022/8/3-15:14
 * @describe:适配databinding万能适配器
 */
abstract class DataBindingBaseAdapter<T, VB : ViewDataBinding> constructor(
    private val layoutResId: Int,
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, DataBindingBaseViewHolder<VB>>(layoutResId, data) {
    constructor(data: MutableList<T>?) : this(0, data)

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataBindingBaseViewHolder<VB> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            layoutResId, parent, false
        )
        return DataBindingBaseViewHolder(binding.root, binding as VB)
    }

    override fun convert(holder: DataBindingBaseViewHolder<VB>, item: T) {
        convertOfVB(holder.getBinding(), item)
    }

    /**
     * binding暴露给实现类
     */
    abstract fun convertOfVB(binding: VB?, item: T)
}

class DataBindingBaseViewHolder<VB : ViewDataBinding> constructor(view: View, binding: VB? = null) :
    BaseViewHolder(view)

