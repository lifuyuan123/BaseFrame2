package com.lfy.baselibrary.ui.adapter

import android.view.View
import android.widget.Checkable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lfy.baselibrary.click
import com.lfy.baselibrary.lastClickTime


/**
 * baseadapter
 */
abstract class BaseSuperAdapter<T, K : BaseViewHolder> constructor(
    layoutResId: Int,
    data: MutableList<T>?=null
) : BaseQuickAdapter<T, K>(layoutResId, data){
    constructor(data: MutableList<T>?) : this(0, data)

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