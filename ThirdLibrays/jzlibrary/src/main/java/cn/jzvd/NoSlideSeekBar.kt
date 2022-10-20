package cn.jzvd

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar


/**
 * @Author lfy
 * @Date 2020/11/15-10:24
 * @Email 446960231@qq.com
 * @describe:不可拖动seekbar
 */
class NoSlideSeekBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : SeekBar(context, attrs, defStyleAttr) {
    /**
     * 是否支持拖动进度
     */
    private var touch = false

    @Synchronized
    override fun onDraw(canvas: Canvas?) {
//        LogUtil.getLog().d("voice progressbar onDraw");
        super.onDraw(canvas)
    }

    fun setTouch(touch: Boolean) {
        this.touch = touch
    }

    /**
     * onTouchEvent 是在 SeekBar 继承的抽象类 AbsSeekBar
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (touch) {
            super.onTouchEvent(event)
        } else false
    }
}