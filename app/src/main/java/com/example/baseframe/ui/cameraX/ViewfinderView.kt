package com.example.baseframe.ui.cameraX

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.baseframe.R
import timber.log.Timber

/**
 * @Author admin
 * @Date 2021/12/14-9:03
 * @describe: 扫描取景器
 */
class ViewfinderView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs, 0) {

    //矩形四个角
    private val rectPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var rectColor = -1

    //中间取景框
    private val framePaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var frameColor = -1

    //扫描线
    private val scanPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    //背景色
    private val bgPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var bgColor = -1

    // 扫描线移动的y
    private var scanLineTop = 0
    private var valueAnimator: ValueAnimator? = null
    private val frameRect: Rect//取景框

    init {
        rectColor = ContextCompat.getColor(context, R.color.rect_color)
        frameColor = ContextCompat.getColor(context, R.color.frame_color)
        bgColor = ContextCompat.getColor(context, R.color.bg_color)

        //四个角
        rectPaint.color = rectColor
        rectPaint.style = Paint.Style.FILL
        rectPaint.strokeWidth = dp2px(1).toFloat()

        //中间取景框
        framePaint.color = frameColor
        framePaint.strokeWidth = dp2px(1).toFloat()
        framePaint.style = Paint.Style.STROKE

        //扫描线
        scanPaint.strokeWidth = dp2px(2).toFloat()
        scanPaint.style = Paint.Style.FILL
        scanPaint.isDither = true

        //背景色
        bgPaint.color = bgColor
        scanPaint.style = Paint.Style.FILL

        //取景框
        frameRect = getFrameRect(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        if (frameRect == null) {
            return
        }

        val width = canvas.width
        val height = canvas.height


        //绘制遮罩
        drawBg(canvas, frameRect, width, height)


        //绘制取景框边框
        drawFrameBounds(canvas, frameRect)

        //绘制扫描线
        drawScanLine(canvas, frameRect)

        //初始化扫描线动画
        initAnimator()

    }

    /**
     * 绘制扫描线
     */
    private fun drawScanLine(canvas: Canvas, frame: Rect) {
        //获取扫描线
        val bitmap: Bitmap = changeBitmapSize(frame)
        val mSrcRect = Rect(0, 0, bitmap.width, bitmap.height)
        val mDestRect =
            Rect(frame.left, scanLineTop, bitmap.width + frame.left, bitmap.height + scanLineTop)
        canvas.drawBitmap(bitmap, mSrcRect, mDestRect, scanPaint)
    }

    /**
     * 获取制定大小的扫描线
     */
    private fun changeBitmapSize(frame: Rect): Bitmap {
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.scan_light)
        val width = bitmap.width
        val height = bitmap.height
        //设置想要的大小
        val newWidth: Int = frame.right - frame.left
        val newHeight = 10
        //计算压缩的比率
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        //获取想要缩放的matrix
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        //获取新的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        return bitmap
    }

    /**
     * 绘制扫描边框
     */
    private fun drawFrameBounds(canvas: Canvas, frame: Rect) {

        //绘制扫描rect
        canvas.drawRect(frame, framePaint)

        //四个角的长度和宽度
        val width = frame.width()
        val corLength = (width * 0.07).toInt()
        var corWidth = (corLength * 0.2).toInt()

        corWidth = if (corWidth > 15) 15 else corWidth


        //角在线外
        // 左上角
        canvas.drawRect(
            (frame.left - corWidth).toFloat(), frame.top.toFloat(), frame.left.toFloat(), (frame.top
                    + corLength).toFloat(), rectPaint
        )
        canvas.drawRect(
            (frame.left - corWidth).toFloat(), (frame.top - corWidth).toFloat(), (frame.left
                    + corLength).toFloat(), frame.top.toFloat(), rectPaint
        )
        // 右上角
        canvas.drawRect(
            frame.right.toFloat(), frame.top.toFloat(), (frame.right + corWidth).toFloat(), (
                    frame.top + corLength).toFloat(), rectPaint
        )
        canvas.drawRect(
            (frame.right - corLength).toFloat(), (frame.top - corWidth).toFloat(), (
                    frame.right + corWidth).toFloat(), frame.top.toFloat(), rectPaint
        )
        // 左下角
        canvas.drawRect(
            (frame.left - corWidth).toFloat(), (frame.bottom - corLength).toFloat(),
            frame.left.toFloat(), frame.bottom.toFloat(), rectPaint
        )
        canvas.drawRect(
            (frame.left - corWidth).toFloat(), frame.bottom.toFloat(), (frame.left
                    + corLength).toFloat(), (frame.bottom + corWidth).toFloat(), rectPaint
        )
        // 右下角
        canvas.drawRect(
            frame.right.toFloat(), (frame.bottom - corLength).toFloat(), (frame.right
                    + corWidth).toFloat(), frame.bottom.toFloat(), rectPaint
        )
        canvas.drawRect(
            (frame.right - corLength).toFloat(), frame.bottom.toFloat(), (frame.right
                    + corWidth).toFloat(), (frame.bottom + corWidth).toFloat(), rectPaint
        )

    }

    /**
     * 绘制背景色，遮罩
     */
    private fun drawBg(canvas: Canvas, frame: Rect, width: Int, height: Int) {
        // 绘制取景框外的暗灰色的表面，分四个矩形绘制
        //上面的框
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), bgPaint)
        //绘制左边的框
        canvas.drawRect(
            0f,
            frame.top.toFloat(),
            frame.left.toFloat(),
            (frame.bottom + 1).toFloat(),
            bgPaint
        )
        //绘制右边的框
        canvas.drawRect(
            (frame.right + 1).toFloat(),
            frame.top.toFloat(),
            width.toFloat(),
            (frame.bottom + 1).toFloat(),
            bgPaint
        )
        //绘制下面的框
        canvas.drawRect(
            0f,
            (frame.bottom + 1).toFloat(),
            width.toFloat(),
            height.toFloat(),
            bgPaint
        )
    }

    /**
     * 扫描线动画
     */
    private fun initAnimator() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(frameRect.top, frameRect.bottom - 10)
            valueAnimator?.duration = 3000
            valueAnimator?.interpolator = DecelerateInterpolator()
            valueAnimator?.repeatMode = ValueAnimator.RESTART
            valueAnimator?.repeatCount = ValueAnimator.INFINITE
            valueAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
                scanLineTop = animation.animatedValue as Int
                invalidate()
            })
            valueAnimator?.start()
        }
    }


    /**
     * 停止动画
     */
    fun stopAnimator() {
        valueAnimator?.end()
        valueAnimator?.cancel()
        valueAnimator = null
    }


    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
            .toInt()
    }


    /**
     * 获取预览框
     */
    private fun getFrameRect(context: Context): Rect {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point(display.width, display.height)

        Timber.e("宽高：$point")
        val width = (point.x * 0.6).toInt()

        /*水平居中  偏上显示*/
        val leftOffset: Int = (point.x - width) / 2
        val topOffset: Int = point.y/2 - width/3*2
        Timber.e("宽高：    左：${leftOffset}  上:$topOffset  右：${leftOffset + width}  下:${topOffset + width}")
        return Rect(
            leftOffset, topOffset, leftOffset + width,
            topOffset + width
        )
    }

}