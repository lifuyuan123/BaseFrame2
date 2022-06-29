package com.lfy.baselibrary

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

/**
 * @Author admin
 * @Date 2021/8/2-9:56
 * @describe: databinding适配
 */

//图片加载
@BindingAdapter("remoteUrl")
fun remoteUrl(img: ImageView, url: String?) {
    img.loadImage(url)
}

//防抖动点击事件
@BindingAdapter("android:onClick")
fun singleClick(view: View, clickListener: View.OnClickListener) {
    view.singleClick(clickListener)
}

//shape等操作   requireAll:它表示是否需要每个属性都必须绑定了数据才会调用viewBackground方法
@BindingAdapter(
    value = [
        "shape_solidColor",//填充颜色
        "shape_radius",//圆角
        "shape_strokeColor",//描边颜色
        "shape_strokeWitdh",//描边宽度
        "shape_dashWith",//描边虚线单个宽度
        "shape_dashGap",//描边间隔宽度
        "shape_isGradual",//是否渐变
        "shape_startColor",//渐变开始颜色
        "shape_centerColor",//渐变中间颜色
        "shape_endColor",//渐变结束颜色
        "shape_gradualOrientation",//渐变角度
        "shape_tl_radius",//上左圆角
        "shape_tr_radius",//上右圆角
        "shape_bl_radius",//下左圆角
        "shape_br_radius",//下右圆角
    ],
    requireAll = false
)
fun View.setViewBackground(
    //注意  这里的参数只能按照上面value值顺序排列  否则值对应不匹配
    solidColor: Int = Color.TRANSPARENT,
    radius: Int = 0,
    strokeColor: Int = Color.TRANSPARENT,
    strokeWidth: Int = 0,
    shape_dashWith: Int = 0,
    shape_dashGap: Int = 0,
    shape_isGradual: Boolean = false,
    shape_startColor: Int = Color.TRANSPARENT,
    shape_centerColor: Int = Color.TRANSPARENT,
    shape_endColor: Int = Color.TRANSPARENT,
    shape_gradualOrientation: Int = 1,//TOP_BOTTOM = 1 ,TR_BL = 2,RIGHT_LEFT = 3,BR_TL = 4,BOTTOM_TOP = 5,BL_TR = 6,LEFT_RIGHT = 7,TL_BR = 8
    shape_tl_radius: Int = 0,
    shape_tr_radius: Int = 0,
    shape_bl_radius: Int = 0,
    shape_br_radius: Int = 0,
) {
    val drawable = GradientDrawable()
    if (shape_isGradual) {//渐变
        when (shape_gradualOrientation) {
            1 -> drawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
            2 -> drawable.orientation = GradientDrawable.Orientation.TR_BL
            3 -> drawable.orientation = GradientDrawable.Orientation.RIGHT_LEFT
            4 -> drawable.orientation = GradientDrawable.Orientation.BR_TL
            5 -> drawable.orientation = GradientDrawable.Orientation.BOTTOM_TOP
            6 -> drawable.orientation = GradientDrawable.Orientation.BL_TR
            7 -> drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
            8 -> drawable.orientation = GradientDrawable.Orientation.TL_BR
        }
        drawable.gradientType = GradientDrawable.LINEAR_GRADIENT//线性
        drawable.shape = GradientDrawable.RECTANGLE//矩形方正
        drawable.colors = if (shape_centerColor != Color.TRANSPARENT) {//有中间色
            intArrayOf(
                shape_startColor,
                shape_centerColor,
                shape_endColor
            )
        } else {
            intArrayOf(shape_startColor, shape_endColor)
        }//渐变色
    } else {
        drawable.setColor(solidColor)
    }
    //圆角问题
    if (radius == 0) {
        drawable.cornerRadii = floatArrayOf(
            context.dp2px(shape_tl_radius.toFloat()).toFloat(),
            context.dp2px(shape_tl_radius.toFloat()).toFloat(),
            context.dp2px(shape_tr_radius.toFloat()).toFloat(),
            context.dp2px(shape_tr_radius.toFloat()).toFloat(),
            context.dp2px(shape_bl_radius.toFloat()).toFloat(),
            context.dp2px(shape_bl_radius.toFloat()).toFloat(),
            context.dp2px(shape_br_radius.toFloat()).toFloat(),
            context.dp2px(shape_br_radius.toFloat()).toFloat(),
        )
    } else {
        drawable.cornerRadius = context.dp2px(radius.toFloat()).toFloat()
    }

    //可设置虚线
    drawable.setStroke(context.dp2px(strokeWidth.toFloat()), strokeColor,shape_dashWith.toFloat(),shape_dashGap.toFloat())
    background = drawable
}




