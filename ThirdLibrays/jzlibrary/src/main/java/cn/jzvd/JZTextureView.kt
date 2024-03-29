package cn.jzvd

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import cn.jzvd.Jzvd

/**
 *
 * 参照Android系统的VideoView的onMeasure方法
 * <br></br>注意!relativelayout中无法全屏，要嵌套一个linearlayout
 *
 * Referring Android system Video View of onMeasure method
 * <br></br>NOTE! Can not fullscreen relativelayout, to nest a linearlayout
 * Created by Nathen
 * On 2016/06/02 00:01
 */
class JZTextureView : TextureView {
    var currentVideoWidth = 0
    var currentVideoHeight = 0

    constructor(context: Context) : super(context) {
        currentVideoWidth = 0
        currentVideoHeight = 0
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        currentVideoWidth = 0
        currentVideoHeight = 0
    }

    fun setVideoSize(currentVideoWidth: Int, currentVideoHeight: Int) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth
            this.currentVideoHeight = currentVideoHeight
            requestLayout()
        }
    }

    override fun setRotation(rotation: Float) {
        if (rotation != getRotation()) {
            super.setRotation(rotation)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        Log.i(TAG, "onMeasure " + " [" + this.hashCode() + "] ")
        val viewRotation = rotation.toInt()
        val videoWidth = currentVideoWidth
        var videoHeight = currentVideoHeight
        var parentHeight = (parent as View).measuredHeight
        var parentWidth = (parent as View).measuredWidth
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE == Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**强制充满 */
                videoHeight = videoWidth * parentHeight / parentWidth
            }
        }

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            val tempMeasureSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempMeasureSpec
        }
        var width = View.getDefaultSize(videoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            Log.i(TAG, "widthMeasureSpec  [" + MeasureSpec.toString(widthMeasureSpec) + "]")
            Log.i(TAG, "heightMeasureSpec [" + MeasureSpec.toString(heightMeasureSpec) + "]")
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE == Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL) {
                /**原图 */
                height = videoHeight
                width = videoWidth
            } else if (Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE == Jzvd.Companion.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**充满剪切 */
                if (videoHeight.toDouble() / videoWidth > parentHeight.toDouble() / parentWidth) {
                    height = (parentWidth.toDouble() / width.toDouble() * height.toDouble()).toInt()
                    width = parentWidth
                } else if (videoHeight.toDouble() / videoWidth < parentHeight.toDouble() / parentWidth) {
                    width = (parentHeight.toDouble() / height.toDouble() * width.toDouble()).toInt()
                    height = parentHeight
                }
            }
        }
        setMeasuredDimension(width, height)
    }

    companion object {
        protected const val TAG = "JZResizeTextureView"
    }
}