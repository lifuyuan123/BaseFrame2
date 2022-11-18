package com.lfy.baselibrary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lfy.baselibrary.ActivityManager
import me.jessyan.autosize.AutoSize

/**
 * @Author admin
 * @Date 2022/6/30-9:52
 * @describe: 用于pageAdapter头布局或尾布局
 */
abstract class BaseLoadStateAdapter<T : Any>(val data: T) :
    LoadStateAdapter<RecyclerView.ViewHolder>() {

    abstract fun getLayout(): Int

    protected abstract fun bindData(binding: ViewDataBinding, data: T)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, loadState: LoadState) {
        (holder as? BaseLoadStateAdapter<*>.BaseLoadStateHolder)?.onBindViewHolder()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecyclerView.ViewHolder {
        AutoSize.autoConvertDensity(ActivityManager.instance.mCurrentActivity, 375f, true)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            getLayout(), parent, false
        )
        return BaseLoadStateHolder(binding)
    }

    /**
     * 基类holder
     */
    inner class BaseLoadStateHolder internal constructor(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindViewHolder() {
            bindData(binding, data)
        }
    }

}