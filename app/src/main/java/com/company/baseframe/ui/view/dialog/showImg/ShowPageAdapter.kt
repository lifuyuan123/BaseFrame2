package com.company.baseframe.ui.view.dialog.showImg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.company.baseframe.R
import com.company.baseframe.databinding.DialogShowImgBinding

class ShowPageAdapter<T> constructor(var context: Context, val list: List<T>) : PagerAdapter() {

    private lateinit var binding: DialogShowImgBinding
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_show_img,
            null,
            false
        )
        callBack?.onItemLoad(list[position], binding.ivPhoto)
        binding.ivPhoto.setOnClickListener { view -> callBack?.onItemClick(view) }
        container.addView(binding.root)
        return binding.root
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return `object` === view
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface CallBack<T> {
        fun onItemClick(view: View?)
        fun onItemLoad(t: T, iv: ImageView)
    }


    fun setCallBack(callBack: CallBack<T>?) {
        this.callBack = callBack
    }

    private var callBack: CallBack<T>? = null
}