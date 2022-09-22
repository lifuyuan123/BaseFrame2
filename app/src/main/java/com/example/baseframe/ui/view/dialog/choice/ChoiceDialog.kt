package com.example.baseframe.ui.view.dialog.choice

import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.example.baseframe.R
import com.example.baseframe.databinding.DialogChoiceBinding
import com.example.baseframe.entity.ChoiceBean
import com.example.baseframe.entity.RemoteKeys
import com.example.baseframe.utils.ToastUtils.toast
import com.lfy.baselibrary.dp2px
import com.lfy.baselibrary.getWidthAndHeight
import com.lfy.baselibrary.ui.dialog.BaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Author:  admin
 * @Date:  2022/9/22-10:12
 * @describe:  公共弹窗选择  支持单/多选
 */
@AndroidEntryPoint
class ChoiceDialog(
    private val choiceIds: MutableList<String> = mutableListOf(),
    private val list: MutableList<ChoiceBean> = mutableListOf(),
    private val title: String = "标题",
    private val isMultiple: Boolean = false
) : BaseDialogFragment<DialogChoiceBinding>() {


    @Inject
    lateinit var adapter: ChoiceAdapter


    override fun onStart() {
        super.onStart()
        mWindow?.setGravity(Gravity.BOTTOM)
        mWindow?.setWindowAnimations(com.lfy.baselibrary.R.style.public_bottom_dialog)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            mWindow?.getWidthAndHeight()?.get(1)!! / 3 * 2
        )
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            mWindow?.getWidthAndHeight()?.get(1)!! / 3 * 2
        )
    }

    override fun getLayout() = R.layout.dialog_choice


    override fun initData(view: View?) {
        binding.dialog = this
        binding.tvTitle.text = title
        binding.rv.adapter = adapter

        adapter.setOnItemClickListener { _, view, position ->
            if (isMultiple) {
                adapter.data[position].choice = !adapter.data[position].choice

            } else {
                adapter.data.forEach {
                    it.choice = it.id == adapter.data[position].id
                }
            }
            adapter.notifyDataSetChanged()
        }

        choiceIds.forEach {
            list.forEach { item ->
                item.choice = it == item.id
            }
        }

        adapter.setNewInstance(list)


    }

    /**
     * 确认
     */
    fun affirm() {
        val list = adapter.data.filter { it.choice }
        if (list.isNotEmpty()) {
            if (isMultiple) {
                multipleCallBack?.onClick(list)
            } else {
                callBack?.onClick(list[0])
            }

            dismiss()
        }
    }


    //单选
    interface CallBack {
        fun onClick(bean: ChoiceBean)
    }

    var callBack: CallBack? = null


    //多选
    interface MultipleCallBack {
        fun onClick(list: List<ChoiceBean>)
    }

    var multipleCallBack: MultipleCallBack? = null

}