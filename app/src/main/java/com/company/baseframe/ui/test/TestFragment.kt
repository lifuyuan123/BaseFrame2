package com.company.baseframe.ui.test

import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.company.baseframe.R
import com.company.baseframe.app.addLoadPageListener
import com.company.baseframe.app.launchAndRepeatWithViewLifecycle
import com.company.baseframe.app.pictureSelector
import com.company.baseframe.databinding.FragmentTestBinding
import com.company.baseframe.entity.ChoiceBean
import com.company.baseframe.entity.RemoteKeys
import com.company.baseframe.ui.cameraX.CameraActivity
import com.company.baseframe.ui.view.dialog.choice.ChoiceDialog
import com.company.baseframe.ui.view.dialog.showImg.ShowPageImgDialog
import com.company.baseframe.utils.ToastUtils.toast
import com.lfy.baselibrary.entity.PermissType
import com.lfy.baselibrary.loadImage
import com.lfy.baselibrary.permiss
import com.lfy.baselibrary.showDialog
import com.lfy.baselibrary.singleClick
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
        launchAndRepeatWithViewLifecycle {
            viewModel._flow.collectLatest {
                Timber.e("测试数据:$it")
            }
        }

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
                _mActivity.permiss(PermissType.PermissCamera){
                    _mActivity.startActivityForResult<CaptureActivity>(100)
                }
            } else if (arguments?.getString("data") == "1") {
//                _mActivity.startActivityForResult<CameraActivity>(100)
                val dialog=ChoiceDialog(list = mutableListOf(
                    ChoiceBean(false,"qer","0"),
                    ChoiceBean(false,"df","1"),
                    ChoiceBean(false,"zxcv","2"),
                    ChoiceBean(false,"zxc","3"),
                    ChoiceBean(false,"sdf","4"),
                    ChoiceBean(false,"asdfasdf","5")
                ), isMultiple = true)
                showDialog(dialog)
                dialog.callBack=object :ChoiceDialog.CallBack{
                    override fun onClick(bean: ChoiceBean) {
                        Timber.e("选择：$bean")
                    }

                }
                dialog.multipleCallBack=object :ChoiceDialog.MultipleCallBack{
                    override fun onClick(list: List<ChoiceBean>) {
                        Timber.e("选择：$list")
                    }

                }
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