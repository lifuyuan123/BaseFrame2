package com.lfy.baseframe.ui.main

import com.lfy.baseframe.app.flowRequest
import com.lfy.baseframe.entity.AppPackageBean
import com.lfy.baseframe.entity.BaseBean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : BaseViewModel() {

    private val _updata = MutableSharedFlow<BaseBean<AppPackageBean>>()
    val updata: SharedFlow<BaseBean<AppPackageBean>> = _updata


    /**
     * 获取app信息
     */
    fun getAppInfo() {
        flowRequest(_updata) {
            delay(3000)
            repository.getAppInfo()
        }
    }
}