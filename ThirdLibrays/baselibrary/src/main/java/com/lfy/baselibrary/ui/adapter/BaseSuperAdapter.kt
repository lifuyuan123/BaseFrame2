package com.lfy.baselibrary.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


/**
 * baseadapter
 */
abstract class BaseSuperAdapter<T, K : BaseViewHolder> constructor(
    layoutResId: Int,
    data: MutableList<T>?=null
) : BaseQuickAdapter<T, K>(layoutResId, data){
    constructor(data: MutableList<T>?) : this(0, data)
}