package com.lfy.baselibrary

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
    value = ["shape_solidColor", "shape_radius", "shape_strokeColor", "shape_strokeWitdh"],
    requireAll = false
)
fun setViewBackground(view: View, solidColor: Int, radius: Int = 0, strokeColor: Int, strokeWidth: Int) {
    val drawable = GradientDrawable()
    drawable.setColor(solidColor)
    drawable.cornerRadius = view.context.dp2px(radius.toFloat()).toFloat()
    drawable.setStroke(view.context.dp2px(strokeWidth.toFloat()), strokeColor)
    view.background = drawable
}




