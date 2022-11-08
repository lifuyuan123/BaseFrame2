package com.lfy.baselibrary.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * @Author admin
 * @Date 2022/2/17-9:26
 * @describe:viewmodel基类  处理视图层公共状态
 */
open class BaseViewModel : ViewModel() {

    //用于load弹窗   replay=1,onBufferOverflow=BufferOverflow.DROP_LATES 传入这两个参数第一次发射才会成功
    private val _loadEvent =
        MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val loadEvent: SharedFlow<Boolean> = _loadEvent


    fun isShowLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _loadEvent.emit(isLoading)
        }
    }

}