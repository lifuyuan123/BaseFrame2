package com.lfy.baseframe.app

import android.app.Activity
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.LoadState
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lfy.baseframe.R
import com.lfy.baseframe.entity.BaseBean
import com.lfy.baseframe.utils.GlideEngine
import com.lfy.baseframe.utils.ImageFileCompressEngine
import com.lfy.baseframe.utils.MeSandboxFileEngine
import com.lfy.baseframe.utils.ToastUtils
import com.google.gson.JsonParser
import com.hjq.gson.factory.GsonFactory
import com.lfy.baselibrary.topActivity
import com.lfy.baselibrary.ui.adapter.BasePagingDataAdapter
import com.lfy.baselibrary.ui.view.StatusPager
import com.lfy.baselibrary.vm.BaseViewModel
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
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
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author admin
 * @Date 2021/12/8-14:15
 * @describe:扩展方法
 */

/**
 * 全局吐丝
 */
fun Any.toastPlus(msg: Any?) {
    App.context?.topActivity()?.runOnUiThread {
        ToastUtils.toast(msg)
    }
}

//统一处理网络异常  retrofit + Coroutines(协程)
suspend inline fun <T> apiCall(crossinline request: suspend CoroutineScope.() -> BaseBean<T>): BaseBean<T>? {
    return withContext(Dispatchers.IO) {
        val data = try {
            request()
        } catch (e: Throwable) {
            // 网络错误，将状态码和消息封装为 ResponseResult
            toastPlus("${e.message}")
            Timber.e("apiCall 网络异常  $e")
            null
        }
        return@withContext data
    }
}


//带加载监听的统一协程请求
fun <T> BaseViewModel.flowRequest(
    flow: Flow<T>?,
    showLoading: Boolean = false,
    block: suspend () -> T
) {
    viewModelScope.launch {
        if (showLoading) {
            isShowLoading(true)
        }
        try {
            val response = block.invoke()
            when (flow) {
                is MutableStateFlow<T> -> {
                    flow.emit(response)
                }
                is MutableSharedFlow<T> -> {
                    flow.emit(response)
                }
            }
        } catch (e: Exception) {
            Timber.e("接口异常:$e")
            isShowLoading(false)
            var msg = when (e) {
                is ConnectException -> e.message
                is SocketTimeoutException -> "请求网络超时"
                is HttpException -> when (e.code()) {
                    500 -> "服务器发生错误"
                    404 -> "请求地址不存在"
                    403 -> "请求被服务器拒绝"
                    307 -> "请求被重定向到其他页面"
                    else -> e.message()
                }
                else -> e?.message
            }
            if (e is SocketTimeoutException || e is HttpException || e is ConnectException) {
                toastPlus(msg)
            }
        } finally {
            if (showLoading) {
                isShowLoading(false)
            }
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


//gson转换集合实体 GsonFactory.getSingletonGson().fromJson<MutableList<Bean>>(this, object : TypeToken<MutableList<Bean>>() {}.type)
inline fun <reified T> String.formJson(): MutableList<T> {
    val list = mutableListOf<T>()
    if (isNullOrEmpty()) {
        return list
    }
    val jsonArray = JsonParser().parse(this).asJsonArray
    for (jsonElement in jsonArray) {
        list.add(GsonFactory.getSingletonGson().fromJson(jsonElement, T::class.java))
    }
    return list
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
 * 刷新状态设置
 */
fun <T> SmartRefreshLayout.freshStatus(
    adapter: BaseQuickAdapter<T, *>?,
    data: BaseBean<MutableList<T>>
) {
    if (data.isSuccess && data?.page_count != null && data?.page_count > 1) {//加载更多
        if (data.data.isNullOrEmpty()) {//没有数据
            finishLoadMoreWithNoMoreData()
        } else {
            adapter?.addData(data.data)
            finishLoadMore()
        }
    } else {//刷新
        if (data.data.isNullOrEmpty()) {//没有数据
            adapter?.setEmptyView(com.lfy.baselibrary.R.layout.state_empty)
            finishLoadMoreWithNoMoreData()
        } else {
            resetNoMoreData()//恢复没有更多数据的原始状态
        }
        adapter?.setNewInstance(data.data)
        finishRefresh()
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
                selectNormalTextColor =
                    ContextCompat.getColor(this@pictureSelector, R.color.cl_9b9b9b)
                selectTextColor = ContextCompat.getColor(this@pictureSelector, R.color.white)
                selectText = getString(R.string.ps_done_front_num)
            }
            bottomBarStyle = BottomNavBarStyle().apply {
                bottomPreviewNormalTextColor =
                    ContextCompat.getColor(this@pictureSelector, R.color.cl_9b9b9b)
                bottomSelectNumTextColor =
                    ContextCompat.getColor(this@pictureSelector, R.color.white)
                bottomPreviewNormalTextColor =
                    ContextCompat.getColor(this@pictureSelector, R.color.cl_9b9b9b)
                bottomPreviewSelectTextColor =
                    ContextCompat.getColor(this@pictureSelector, R.color.white)
                bottomSelectNumResources = R.drawable.shape_picture_num_bg//底部选中数量
                bottomOriginalDrawableLeft = R.drawable.ps_original_selector//原图选中效果
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

/**
 * 预览图片视频
 * block内处理删除逻辑
 * adapter.remove(position)
 * adapter.notifyItemRemoved(position)
 */
fun Activity.preview(position: Int, list: ArrayList<LocalMedia>, block: (position: Int) -> Unit) {
    // 预览图片、视频、音频
    PictureSelector.create(this)
        .openPreview()
        .setImageEngine(GlideEngine.createGlideEngine())
        .setSelectorUIStyle(PictureSelectorStyle().apply {//入场动画
            windowAnimationStyle = PictureWindowAnimationStyle().apply {
                activityEnterAnimation = R.anim.public_translate_right_to_center
                activityExitAnimation = R.anim.public_translate_center_to_right
            }
            selectMainStyle = SelectMainStyle().apply {
                selectNormalTextColor = ContextCompat.getColor(this@preview, R.color.cl_9b9b9b)
                selectTextColor = ContextCompat.getColor(this@preview, R.color.white)
                selectText = getString(R.string.ps_done_front_num)
            }
            bottomBarStyle = BottomNavBarStyle().apply {
                bottomPreviewNormalTextColor =
                    ContextCompat.getColor(this@preview, R.color.cl_9b9b9b)
                bottomSelectNumTextColor = ContextCompat.getColor(this@preview, R.color.white)
                bottomPreviewNormalTextColor =
                    ContextCompat.getColor(this@preview, R.color.cl_9b9b9b)
                bottomPreviewSelectTextColor = ContextCompat.getColor(this@preview, R.color.white)
                bottomSelectNumResources = R.drawable.shape_picture_num_bg//底部选中数量
                bottomOriginalDrawableLeft = R.drawable.ps_original_selector//原图选中效果
                isCompleteCountTips = false//隐藏已选中数字
            }
        })
        .isPreviewFullScreenMode(true)
        .setExternalPreviewEventListener(object : OnExternalPreviewEventListener {
            //预览界面删除数据监听
            override fun onPreviewDelete(position: Int) {
                block.invoke(position)
            }

            override fun onLongPressDownload(media: LocalMedia?): Boolean {
                return false
            }
        })
        .startActivityPreview(position, true, list)
}

/**
 * float保留小数点
 */
fun Float.numFormat(digit: Int = 1): String {
    return when (digit) {
        1 -> {
            DecimalFormat("#.0").format(this)
        }
        2 -> {
            DecimalFormat("#.00").format(this)
        }
        else -> {
            this.toString()
        }
    }
}


/**
 * int保留小数点
 */
fun Int.numFormat(digit: Int = 1): String {
    return this.toFloat().numFormat(digit)
}

/**
 * 去掉小数后面不必要的0
 */
fun Float.clearZero(): String {
    return NumberFormat.getInstance().format(this)
}

fun Double.clearZero(): String {
    return NumberFormat.getInstance().format(this)
}

/**
 * time转string
 */
fun Long.getDateToString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format)
    return sdf.format(Date(this))
}

/**
 * 字符串转date
 */
fun String.getDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date {
    val sdf = SimpleDateFormat(format)
    return sdf.parse(this)
}

/**
 * 转换时间
 */
fun Long.transTime(): String {
    val d = this / (24 * 60 * 60 * 1000)
    val h = this % (24 * 60 * 60 * 1000) / (60 * 60 * 1000)
    var m = this % (60 * 60 * 1000) / (60 * 1000)

    //四舍五入分钟数
    if (this % (60 * 60 * 1000) % (60 * 1000) > 0) {
        m += 1
    }

    val str = if (d.toInt() > 0) {
        "${d}天${h}小时${m}分钟"
    } else {
        if (h.toInt() > 0) {
            "${h}小时${m}分钟"
        } else {
            "${m}分钟"
        }
    }
    return str
}

/**
 * 转换时间  分钟
 */
fun Long.transTimeOfMinute(): String {
    var minute = this / (60 * 1000)
    if (this / 1000 % 60 > 0) {
        minute += 1
    }
    return "$minute"
}
