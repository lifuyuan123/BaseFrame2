package com.lfy.baseframe.ui.view.dialog.showImg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.lfy.baseframe.R
import com.lfy.baseframe.databinding.DialogShowPagerImgBinding


/**
 * 展示大图
 */
class ShowPageImgDialog<T>(
    context: Context,
    val position: Int,
    val list: List<T>
) : Dialog(context, com.lfy.baselibrary.R.style.public_ustomDialog) {
    private lateinit var adapter: ShowPageAdapter<T>
    private lateinit var binding: DialogShowPagerImgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_show_pager_img, null, false
        )
        setContentView(binding.root)
        try {
            initView()
            initData()
            val layoutParams = window?.attributes
            layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
            window?.attributes = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initView() {
        //当图片只有一张的时候隐藏下标
        binding.tvIndext.visibility = if (list.size <= 1) View.GONE else View.VISIBLE
    }

    private fun initData() {
        binding.tvIndext.text = String.format("%s/%s", position + 1, list.size)
        adapter = ShowPageAdapter(context, list)
        binding.dialogViewpager.adapter = adapter
        binding.dialogViewpager.currentItem = position
        adapter.setCallBack(object : ShowPageAdapter.CallBack<T> {
            override fun onItemClick(view: View?) {
                dismiss()
            }

            override fun onItemLoad(t: T, iv: ImageView) {
                loadImage?.load(t, iv)
            }

        })
        binding.dialogViewpager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.tvIndext.text = String.format("%s/%s", position + 1, list.size)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    interface LoadImage<T> {
        fun load(t: T, iv: ImageView)
    }

    private var loadImage: LoadImage<T>? = null

    fun setLoadImage(loadImage: LoadImage<T>?) {
        this.loadImage = loadImage
    }

}
