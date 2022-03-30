package com.example.baseframe.ui.view.dialog.choice

import androidx.databinding.ViewDataBinding
import com.example.baseframe.R
import com.example.baseframe.databinding.ItemChoiceProjectBinding
import com.example.baseframe.entity.RemoteKeys
import com.lfy.baselibrary.ui.adapter.BasePagingDataAdapter
import javax.inject.Inject

/**
 * @Author admin
 * @Date 2021/8/5-15:56
 * @describe: 选择弹窗适配器
 */
class ChoiceAdapter @Inject constructor() : BasePagingDataAdapter<RemoteKeys>() {
    override fun getLayout(): Int {
        return R.layout.item_choice_project
    }

    override fun bindData(binding: ViewDataBinding, data: RemoteKeys,position:Int) {
        (binding as ItemChoiceProjectBinding).bean = data
        binding.executePendingBindings()
    }
}