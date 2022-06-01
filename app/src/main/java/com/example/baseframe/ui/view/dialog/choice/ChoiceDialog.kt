package com.example.baseframe.ui.view.dialog.choice

import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseframe.R
import com.example.baseframe.databinding.DialogChoiceBinding
import com.example.baseframe.entity.RemoteKeys
import com.lfy.baselibrary.dp2px
import com.lfy.baselibrary.getWidthAndHeight
import com.lfy.baselibrary.toast
import com.lfy.baselibrary.ui.dialog.BaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Author admin
 * @Date 2021/8/3-17:31
 * @describe:
 */
@AndroidEntryPoint
class ChoiceDialog(private val datas: MutableList<RemoteKeys>) : BaseDialogFragment<DialogChoiceBinding>() {


    @Inject lateinit var adapter : ChoiceAdapter


    private var job: Job? = null

    override fun onStart() {
        super.onStart()
        mWindow?.setGravity(Gravity.BOTTOM)
        mWindow?.setWindowAnimations(com.lfy.baselibrary.R.style.public_bottom_dialog)
        activity?.applicationContext?.let { it.dp2px( 87f) }?.let {
            mWindow?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                mWindow?.getWidthAndHeight()?.get(1)!! / 2
            )
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activity?.applicationContext?.let { it.dp2px( 87f) }?.let {
            mWindow?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                mWindow?.getWidthAndHeight()?.get(1)!! / 2
            )
        }
    }

    override fun getLayout()=R.layout.dialog_choice


    override fun initData(view: View?) {
        binding.tvTitle.text = "标题"
        binding.rv.layoutManager = LinearLayoutManager(activity)
        binding.rv.adapter = adapter
        adapter.addChildClickOfId(R.id.tvContent)
        adapter.addChildLongClickOfId(R.id.tvContent)
        adapter.setOnItemClickListener { adapter, view, position ->
            toast(adapter.getData(position).toString())
        }
        adapter.setOnItemLongClickListener { adapter, v, position ->
            toast("长按item")
        }
        adapter.setOnItemChildClickListener { adapter, v, position ->
            toast("点击文字")
        }
        adapter.setOnItemChildLongClickListener { adapter, v, position ->
            toast("长按文字")
        }

        getdata()


    }

    private fun getdata() {
        job?.cancel()
        job = lifecycleScope.launch {
            Pager(
                config = PagingConfig(
                    pageSize = 100,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    object : PagingSource<Int, RemoteKeys>() {
                        override fun getRefreshKey(state: PagingState<Int, RemoteKeys>): Int? {
                            return null
                        }

                        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RemoteKeys> {
                            return LoadResult.Page(
                                data = datas,
                                prevKey = null,//上一页
                                nextKey = null
                            )
                        }

                    }
                }
            ).flow.cachedIn(lifecycleScope).collectLatest { it ->
                adapter.submitData(it)
            }
        }
    }


}