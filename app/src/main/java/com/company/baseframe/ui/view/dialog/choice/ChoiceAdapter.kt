package com.company.baseframe.ui.view.dialog.choice

import com.company.baseframe.R
import com.company.baseframe.databinding.ItemChoiceProjectBinding
import com.company.baseframe.entity.ChoiceBean
import com.lfy.baselibrary.ui.adapter.BaseDataBindingAdapter
import com.lfy.baselibrary.ui.adapter.BaseDataBindingViewHolder
import timber.log.Timber
import javax.inject.Inject

/**
 * @Author admin
 * @Date 2021/8/5-15:56
 * @describe: 选择弹窗适配器
 */
class ChoiceAdapter @Inject constructor() :
    BaseDataBindingAdapter<ChoiceBean, ItemChoiceProjectBinding>(R.layout.item_choice_project) {

    override fun convertOfVB(
        binding: ItemChoiceProjectBinding?,
        helper: BaseDataBindingViewHolder<ItemChoiceProjectBinding>,
        item: ChoiceBean
    ) {
        binding?.bean = item
    }
}