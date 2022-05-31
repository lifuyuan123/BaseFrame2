package com.example.baseframe.app

import android.app.Activity
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.LoadState
import com.example.baseframe.R
import com.example.baseframe.entity.BaseBean
import com.example.baseframe.utils.GlideEngine
import com.example.baseframe.utils.ImageFileCompressEngine
import com.example.baseframe.utils.MeSandboxFileEngine
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.lfy.baselibrary.entity.Result
import com.lfy.baselibrary.ui.adapter.BasePagingDataAdapter
import com.lfy.baselibrary.ui.view.StatusPager
import com.lfy.baselibrary.vm.BaseViewModel
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @Author admin
 * @Date 2021/12/8-14:15
 * @describe:扩展方法
 */

//统一处理网络异常  retrofit + Coroutines(协程)
suspend inline fun <T> apiCall(crossinline call: suspend CoroutineScope.() -> BaseBean<T>): Result<T> {
    return withContext(Dispatchers.IO) {
        val res: Result<T>
        try {
            res = Result.Success(call().data)
        } catch (e: Throwable) {
            // 网络错误，将状态码和消息封装为 ResponseResult
            Timber.e("apiCall 网络异常  $e")
            return@withContext Result.Error(Exception(e))
        }
        return@withContext res
    }
}

//统一处理协程网络异常
suspend inline fun <T> BaseViewModel.request(
    block: suspend () -> T,
    liveData: UnPeekLiveData<T>,
    showLoading: Boolean = false,
) {
    if (showLoading) {
        isShowLoading(true)
    }
    try {
        val result = block.invoke()
        liveData.postValue(result)
    } catch (e: Exception) {
        Timber.e("接口异常:$e")
    } finally {
        if (showLoading) {
            isShowLoading(false)
        }
    }
}

//带加载监听的统一协程请求
suspend fun <T> BaseViewModel.flowRequest(
    flow: Flow<T>,
    showLoading: Boolean = false,
    block: suspend () -> T
) {
    if (showLoading) {
        isShowLoading(true)
    }
    try {
        val response = block.invoke()
        when (flow) {
            is MutableSharedFlow<T> -> {
                flow.emit(response)
            }
            is MutableStateFlow<T> -> {
                flow.emit(response)
            }
        }
    } catch (e: Exception) {
        Timber.e("接口异常:$e")
    } finally {
        if (showLoading) {
            isShowLoading(false)
        }
    }
}

//FlowWithLifecycle
//repeatOnLifecycle 本身是个挂起函数，一旦被调用，将走不到后续代码，除非 lifecycle 进入 DESTROYED。
//supervisorScope代码块中多任务中有异常其他任务回继续执行
inline fun Fragment.launchAndRepeatWithViewLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(state) {
            block()
        }
    }
}

inline fun AppCompatActivity.launchAndRepeatWithViewLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(state) {
            block()
        }
    }
}

//越界回弹设置
fun Activity.overScrollBounce(smart: SmartRefreshLayout) {
    smart.setEnableRefresh(false)
    smart.setEnableLoadMore(false)
    smart.setEnablePureScrollMode(true)//纯滚动
    smart.setEnableOverScrollBounce(true)//越界回弹
    smart.setEnableOverScrollDrag(true)//越界拖动
    smart.setReboundInterpolator(DecelerateInterpolator())
}

fun Fragment.overScrollBounce(smart: SmartRefreshLayout) {
    this.activity?.overScrollBounce(smart)
}

//gson转换集合实体
fun <T> String.formJson(): T {
    return GsonFactory.getSingletonGson().fromJson(this, object : TypeToken<T>() {}.type)
}

//转公共泛型object实体
fun <T> String.formJsonFan(): BaseBean<T> {
    return GsonFactory.getSingletonGson().fromJson(this, object : TypeToken<BaseBean<T>>() {}.type)
}

//转公共泛型集合实体
fun <T> String.formJsonFanList(): BaseBean<List<T>> {
    return GsonFactory.getSingletonGson()
        .fromJson(this, object : TypeToken<BaseBean<List<T>>>() {}.type)
}

//Pagingadapter公共分页监听
fun BasePagingDataAdapter<*>.addLoadPageListener(
    smart: SmartRefreshLayout,
    statePager: StatusPager
) {
    this.addLoadStateListener { loadState ->
        val noMoreData = loadState.source.append.endOfPaginationReached
        when (loadState.refresh) {
            is LoadState.Loading -> {
                //手动刷新，不展示loading
                if (smart.state != RefreshState.Refreshing && this.itemCount == 0) {
                    //显示加载页
                    statePager.showLoading()
                }
                smart.resetNoMoreData()
            }
            is LoadState.NotLoading -> {
                //显示内容
                if (this.itemCount == 0) {
                    statePager.showError()
                } else {
                    statePager.showContent()
                }
                smart.finishRefresh(true)
                smart.setNoMoreData(noMoreData)
            }
            is LoadState.Error -> {
                //显示失败页面
                if (this.itemCount == 0) {
                    statePager.showError()
                } else {
                    statePager.showContent()
                }
                smart.finishRefresh(false)
            }
        }

        when (loadState.append) {
            is LoadState.Loading -> {
                //充值上拉加载状态  显示loading
                smart.resetNoMoreData()
            }
            is LoadState.NotLoading -> {
                if (noMoreData) {
                    //没有更多数据了
                    smart.finishLoadMoreWithNoMoreData()
                } else {
                    smart.finishLoadMore(true)
                }
            }
            is LoadState.Error -> {
                smart.finishLoadMore(false)
            }
        }
    }
}

/**
 * 图片选择
 */
fun Activity.pictureSelector(): PictureSelectionModel {
    // 进入相册
    return PictureSelector.create(this)
        .openGallery(SelectMimeType.ofAll())//选择类型
        .setSelectorUIStyle(PictureSelectorStyle().apply {//入场动画
            windowAnimationStyle = PictureWindowAnimationStyle().apply {
                activityEnterAnimation = R.anim.public_translate_right_to_center
                activityExitAnimation = R.anim.public_translate_center_to_right
            }
            selectMainStyle = SelectMainStyle().apply {
                selectNormalTextColor = resources.getColor(R.color.cl_9b9b9b)
                selectTextColor = resources.getColor(R.color.white)
                selectText=getString(R.string.ps_done_front_num)
            }
            bottomBarStyle = BottomNavBarStyle().apply {
                bottomPreviewNormalTextColor = resources.getColor(R.color.cl_9b9b9b)
                bottomSelectNumTextColor = resources.getColor(R.color.white)
                bottomPreviewNormalTextColor = resources.getColor(R.color.cl_9b9b9b)
                bottomPreviewSelectTextColor = resources.getColor(R.color.white)
                bottomSelectNumResources = R.drawable.shape_picture_num_bg//底部选中数量
                bottomOriginalDrawableLeft= R.drawable.ps_original_selector//原图选中效果
                isCompleteCountTips = false//隐藏已选中数字
            }
        })
        .setImageEngine(GlideEngine.createGlideEngine())//glide加载引擎
        .setCompressEngine(ImageFileCompressEngine())//压缩引擎
        .setSandboxFileEngine(MeSandboxFileEngine())//自定义沙箱文件处理
        .setSelectionMode(SelectModeConfig.MULTIPLE)//多选
        .isOriginalControl(true)//是否原图
        .isDisplayTimeAxis(true)//时间轴
        .isDisplayCamera(true)//打开拍照按钮
        .isPreviewFullScreenMode(true)//预览全屏
        .isPreviewZoomEffect(true)//预览缩放
        .isPreviewImage(true)//预览
        .isPreviewVideo(true)
        .isPreviewAudio(true)
        .setMaxSelectNum(5)//选择最大数量
        .setMaxVideoSelectNum(2)
        .setRecyclerAnimationMode(AnimationType.DEFAULT_ANIMATION)
//                    .setSelectedData(mAdapter.getData())//以选择的照片
}