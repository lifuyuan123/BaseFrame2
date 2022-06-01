package com.example.baseframe.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.example.baseframe.R
import com.lfy.baselibrary.dp2px

/**
 * 自定义edittext
 */
class ClearEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyle), View.OnFocusChangeListener, TextWatcher {

    internal var onClearListener: OnClearListener? = null
    /**
     * 删除按钮的引用
     */
    private var mClearDrawable: Drawable? = null

    private var mClearIconVisibleDelay: Long = 200
    /**
     * 控件是否有焦点
     */
    private var hasFoucs: Boolean = false

    private var onTextChangeListener: OnTextChangeListener? = null

    private var onClearFocusChangeListener: OnClearFocusChangeListener? = null

    init {
        init()
    }


    private fun init() {
        //获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
        mClearDrawable = compoundDrawables[2]
        if (mClearDrawable == null) {
            //        	throw new NullPointerException("You can add drawableRight attribute in XML");
            mClearDrawable = resources.getDrawable(R.drawable.ic_close_circle)
        }

        mClearDrawable!!.setBounds(
            0,
            0,
            context.dp2px(20f),
            context.dp2px(20f)
        )
        //默认设置隐藏图标
        setClearIconVisible(false)
        //设置焦点改变的监听
        onFocusChangeListener = this
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this)
    }


    /**
     * 设置删除按钮出现 消失 的延时
     * @param clearIconVisibleDelay
     */
    fun setClearIconVisibleDelay(clearIconVisibleDelay: Long) {
        mClearIconVisibleDelay = clearIconVisibleDelay
    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (compoundDrawables[2] != null) {

                val touchable =
                    event.x > width - totalPaddingRight && event.x < width - paddingRight

                if (touchable) {
                    if (onClearListener != null && onClearListener!!.onClear(this)) {
                    } else {
                        this.setText("")
                    }

                }
            }
        }

        return super.onTouchEvent(event)
    }

    fun setOnClearListener(onClearListener: OnClearListener) {
        this.onClearListener = onClearListener
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (onClearFocusChangeListener != null) {
            onClearFocusChangeListener!!.onFocusChange(v, hasFocus)
        }
        this.hasFoucs = hasFocus
        if (hasFocus) {
            setClearIconVisible(text!!.isNotEmpty())
        } else {
            setClearIconVisible(false)
        }
    }

    fun setOnTextChanageListener(listener: OnTextChangeListener) {
        onTextChangeListener = listener
    }

    fun setOnFocusChangeListener(focusChangeListener: OnClearFocusChangeListener) {
        this.onClearFocusChangeListener = focusChangeListener
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    fun setClearIconVisible(visible: Boolean) {
        val right = if (visible) mClearDrawable else null
        postDelayed({
            setCompoundDrawables(
                compoundDrawables[0],
                compoundDrawables[1], right, compoundDrawables[3]
            )
        }, mClearIconVisibleDelay)


    }


    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    override fun onTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) {
        if (onTextChangeListener != null) {
            onTextChangeListener!!.onTextChanged(s, start, count, after)
        }
        if (hasFoucs) {
            setClearIconVisible(s.isNotEmpty())
        }

    }

    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) {
        if (onTextChangeListener != null) {
            onTextChangeListener!!.beforeTextChanged(s, start, count, after)
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (onTextChangeListener != null) {
            onTextChangeListener!!.afterTextChanged(s)
        }
    }

    interface OnClearListener {
        fun onClear(v: ClearEditText): Boolean
    }

    interface OnTextChangeListener : TextWatcher{
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    interface OnClearFocusChangeListener : OnFocusChangeListener
}//这里构造方法也很重要，不加这个很多属性不能再XML里面定义