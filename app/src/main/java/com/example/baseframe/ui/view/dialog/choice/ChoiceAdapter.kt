package com.example.baseframe.ui.view.dialog.choice

import com.example.baseframe.R
import com.example.baseframe.databinding.ItemChoiceProjectBinding
import com.example.baseframe.entity.RemoteKeys
import com.lfy.baselibrary.ui.adapter.DataBindingBaseAdapter
import javax.inject.Inject

/**
 * @Author admin
 * @Date 2021/8/5-15:56
 * @describe: 选择弹窗适配器
 */
class ChoiceAdapter @Inject constructor() : DataBindingBaseAdapter<RemoteKeys, ItemChoiceProjectBinding>(R.layout.item_choice_project) {

    override fun convertOfVB(binding: ItemChoiceProjectBinding?, item: RemoteKeys) {
        binding?.bean = item
        binding?.executePendingBindings()
    }
}