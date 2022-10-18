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

    //用于load弹窗
    private val _loadEvent = MutableSharedFlow<Boolean>(0, 1, BufferOverflow.DROP_OLDEST)
    val loadEvent: SharedFlow<Boolean> =_loadEvent


    fun isShowLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _loadEvent.emit(isLoading)
        }
    }

}