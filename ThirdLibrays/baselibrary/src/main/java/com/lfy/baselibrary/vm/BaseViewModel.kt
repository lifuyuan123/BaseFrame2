package com.lfy.baselibrary.vm

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData

/**
 * @Author admin
 * @Date 2022/2/17-9:26
 * @describe:viewmodel基类  处理视图层公共状态
 */
open class BaseViewModel : ViewModel() {

    //用于load弹窗
    val loadEvent = UnPeekLiveData<Boolean>()


    fun isShowLoading(isLoading: Boolean) {
        loadEvent.value = isLoading
    }

}