package com.company.baseframe.ui.view

import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import kotlin.math.abs

/**
 * @Author:  admin
 * @Date:  2022/11/8-15:36
 * @describe:  禁用横向滑动
 */
class NoScrollViewPager constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs) {


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 不拦截这个事件
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // 不处理这个事件
        return false
    }

    override fun executeKeyEvent(event: KeyEvent): Boolean {
        // 不响应按键事件
        return false
    }

    override fun setCurrentItem(item: Int) {
        // 只有相邻页才会有动画
        super.setCurrentItem(item, abs(currentItem - item) == 1)
    }
}