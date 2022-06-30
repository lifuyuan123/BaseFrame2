package com.example.baseframe.ui.test

import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.example.baseframe.R
import com.example.baseframe.app.addLoadPageListener
import com.example.baseframe.app.launchAndRepeatWithViewLifecycle
import com.example.baseframe.app.pictureSelector
import com.example.baseframe.databinding.FragmentTestBinding
import com.example.baseframe.ui.cameraX.CameraActivity
import com.example.baseframe.ui.view.dialog.showImg.ShowPageImgDialog
import com.lfy.baselibrary.loadImage
import com.lfy.baselibrary.singleClick
import com.lfy.baselibrary.toast
import com.lfy.baselibrary.ui.adapter.BaseLoadStateAdapter
import com.lfy.baselibrary.ui.fragment.BaseFragment
import com.lfy.baselibrary.ui.view.StatusPager
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.yzq.zxinglibrary.android.CaptureActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivityForResult
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TestFragment : BaseFragment<FragmentTestBinding, TestViewModel>() {

    @Inject
    lateinit var adapter: TestAdapter
    private var searchJob: Job? = null

    //展示各状态下页面
    private val statePager by lazy {
        StatusPager.builder(binding.fresh)
            .emptyViewLayout(com.lfy.baselibrary.R.layout.state_empty)
            .loadingViewLayout(com.lfy.baselibrary.R.layout.state_loading)
            .errorViewLayout(com.lfy.baselibrary.R.layout.state_error)
            .addRetryButtonId(com.lfy.baselibrary.R.id.btn_retry)
            .setRetryClickListener { _, _ ->
                adapter.refresh()
            }
            .build()
    }

    companion object {
        fun newInstance(str: String) = TestFragment().apply {
            val bundle = Bundle()
            bundle.putString("data", str)
            arguments = bundle
        }
    }

    override fun getLayout() = R.layout.fragment_test

    override fun initData(savedInstanceState: Bundle?) {
        binding.fragment = this

        binding.tv.text = arguments?.getString("data")

        adapter.addChildClickOfId(R.id.tvItem)
        adapter.addChildLongClickOfId(R.id.tvTitle)
        adapter.setOnItemClickListener { adapte, v, position ->
            toast("item点击")

            val data = mutableListOf<String>()
            for (indext in 0 until adapter.itemCount) {
                data.add(adapter.getData(indext)?.url!!)
            }
            ShowPageImgDialog(_mActivity, position, data).apply {
                show()
                setLoadImage(object : ShowPageImgDialog.LoadImage<String> {
                    override fun load(t: String, iv: ImageView) {
                        iv.loadImage(t)
                    }
                })
            }
        }
        adapter.setOnItemLongClickListener { adapter, v, position ->
            toast("item长按")
        }
        adapter.setOnItemChildClickListener { adapter, v, position ->
            toast("itemchile 点击")
        }
        adapter.setOnItemChildLongClickListener { adapter, v, position ->
            toast("itemchild 长安")
        }

        //需要绑定数据
        binding.fragment = this
        binding.fresh.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                adapter.refresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                adapter.retry()
            }
        })

        initAdapter()
        getGanks()

        binding.tv.singleClick {
            if (arguments?.getString("data") == "2") {
                _mActivity.startActivityForResult<CaptureActivity>(100)
            } else if (arguments?.getString("data") == "1") {
                _mActivity.startActivityForResult<CameraActivity>(100)
            } else {
                // 进入相册
                requireActivity().pictureSelector()
                    .forResult(object : OnResultCallbackListener<LocalMedia> {
                        override fun onResult(result: ArrayList<LocalMedia>) {
                            Timber.e("获取数据：$result")
                        }

                        override fun onCancel() {

                        }
                    })
            }
        }
    }

    /**
     * 初始化adapter
     */
    private fun initAdapter() {
        binding.fresh.setEnableLoadMore(true)
        binding.rv.adapter = adapter
        //监听加载状态 加载中  成功  失败
        adapter.addLoadPageListener(binding.fresh, statePager)

        // 当从网络刷新列表时滚动到顶部。
        launchAndRepeatWithViewLifecycle {
            adapter.loadStateFlow
                // 仅在为RemoteMediator刷新LoadState更改时触发。
                .distinctUntilChangedBy { it.refresh }
                // 仅对远程刷新完成的情况作出反应，例如，NotLoading
                .filter { it.refresh is LoadState.NotLoading }
                .collectLatest { binding.rv.scrollToPosition(0) }
        }
    }

    /**
     * 数据获取
     */
    private fun getGanks() {
        // 在创建新作业之前，请确保我们取消了之前的作业
        searchJob?.cancel()
        //注意在fragment中使用iewLifecycleOwner.lifecycleScope
        searchJob = viewLifecycleOwner.lifecycleScope.launch {

            //仅从服务端获取数据
            viewModel.getGankRomete().collectLatest {
                adapter.submitData(it)
            }
        }
    }
}