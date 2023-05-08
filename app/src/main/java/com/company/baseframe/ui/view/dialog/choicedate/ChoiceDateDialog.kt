package com.company.baseframe.ui.view.dialog.choicedate

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.gyf.immersionbar.ImmersionBar
import com.company.baseframe.R
import com.company.baseframe.databinding.DialogChoiceDateBinding
import com.company.baseframe.utils.Tags
import com.lfy.baselibrary.singleClick
import com.lfy.baselibrary.ui.dialog.BaseDialogFragment
import java.util.*

/**
 * @Author admin
 * @Date 2022/10/19-15:24
 * @describe: 时间选择弹窗
 */
class ChoiceDateDialog : BaseDialogFragment<DialogChoiceDateBinding>() {

    private var pvOptions: TimePickerView? = null
    private var selectDate: Date? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var type: Int = 2 //1:年月   2：年月日  3：年月日时分  4：年月日时分秒

    companion object {
        fun newInstance(
            selectDate: Date = Date(),
            endDate: Date = Date(),
            startDate: Date = Date(Date().time - 30 * 24 * 60 * 60 * 1000L),
            type: Int = 2
        ) = ChoiceDateDialog().apply {
            arguments = Bundle().apply {
                putSerializable(Tags.SELECT_DATE, selectDate)
                putSerializable(Tags.START_DATE, startDate)
                putSerializable(Tags.END_DATE, endDate)
                putInt(Tags.TYPE, type)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mWindow?.setGravity(Gravity.BOTTOM)
        mWindow?.setWindowAnimations(com.lfy.baselibrary.R.style.public_bottom_dialog)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun getLayout() = R.layout.dialog_choice_date

    override fun initImmersionBar() {
        super.initImmersionBar()
        ImmersionBar.with(this).navigationBarColor(com.lfy.baselibrary.R.color.color_bg).init()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWindow?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun initData(view: View?) {
        selectDate = (arguments?.getSerializable(Tags.SELECT_DATE) as? Date)?:Date()
        startDate = (arguments?.getSerializable(Tags.START_DATE) as? Date)?:Date(Date().time - 30 * 24 * 60 * 60 * 1000L)
        endDate = (arguments?.getSerializable(Tags.END_DATE) as? Date)?:Date()
        type = arguments?.getInt(Tags.TYPE, 2) ?: 2
        initTimePicker()
        pvOptions?.show()
    }


    /**
     * 初始化时间选择器
     */
    private fun initTimePicker() {
        //之前选择的时间
        val select = Calendar.getInstance()
        select.time = selectDate
        //开始时间
        val start = Calendar.getInstance()
        start.time = startDate
        //结束时间
        val end = Calendar.getInstance()
        end.time = endDate

        //选项选择器
        val timePickerBuilder = TimePickerBuilder(requireActivity(),
            OnTimeSelectListener { date, v -> //选中事件回调
                callBack?.choiceData(date)
            })
            .setLayoutRes(R.layout.layout_choice_date_of_mounth) {
                val tvSubmit = it.findViewById<View>(R.id.tvAffirm) as TextView
                val ivCancel = it.findViewById<View>(R.id.tvCancel) as TextView
                tvSubmit.singleClick {
                    pvOptions?.returnData()
                    pvOptions?.dismiss()
                    dismiss()
                }
                ivCancel.singleClick {
                    pvOptions?.dismiss()
                    dismiss()
                }
            }
            .setContentTextSize(16) //滚轮文字大小
            .setTitleSize(18) //标题文字大小
            .setTextColorOut(ContextCompat.getColor(requireContext(), R.color.cl_01D4FF))//未选择颜色
            .setDividerColor(ContextCompat.getColor(requireContext(), com.lfy.baselibrary.R.color.cl_60FFFFFF))//目前是背景色
            .setTextColorCenter(ContextCompat.getColor(requireContext(), R.color.purple_200))//选中颜色
            .setOutSideColor(Color.TRANSPARENT)//背景色透明，不设置时使用圆角相当丑陋
            .setTitleText("选择日期") //标题文字
            .setOutSideCancelable(true) //点击屏幕，点在控件外部范围时，是否取消显示
            .setItemVisibleCount(6)//最多显示5个
            .setLineSpacingMultiplier(3f)//item高度
            .isAlphaGradient(true)
            .setDate(select) // 如果不设置的话，默认是系统时间*/
            .setRangDate(start, end) //起始终止年月日设定
            .setDecorView(binding.fram)
            .isDialog(false)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。

        //1:年月   2：年月日  3：年月日时分  4：年月日时分秒
        when (type) {
            1 -> {
                timePickerBuilder
                    .setType(booleanArrayOf(true, true, false, false, false, false)) // 默认全部显示
                    .setLabel("年", "月", "", "", "", "") //默认设置为年月日时分秒
            }
            2 -> {
                timePickerBuilder
                    .setType(booleanArrayOf(true, true, true, false, false, false)) // 默认全部显示
                    .setLabel("年", "月", "日", "", "", "") //默认设置为年月日时分秒
            }
            3 -> {
                timePickerBuilder
                    .setType(booleanArrayOf(true, true, true, true, true, false)) // 默认全部显示
                    .setLabel("年", "月", "日", "时", "分", "") //默认设置为年月日时分秒
            }
            4 -> {
                timePickerBuilder
                    .setType(booleanArrayOf(true, true, true, true, true, true)) // 默认全部显示
                    .setLabel("年", "月", "日", "时", "分", "秒") //默认设置为年月日时分秒
            }
        }

        pvOptions = timePickerBuilder.build()
    }

    interface CallBack {
        fun choiceData(date: Date)
    }

    private var callBack: CallBack? = null

    fun setCallBack(callBack: CallBack) {
        this.callBack = callBack
    }
}