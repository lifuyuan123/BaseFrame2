package com.company.baseframe.ui.test

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.company.baseframe.app.flowRequest
import com.company.baseframe.entity.Bean
import com.company.baseframe.entity.MeiZiBean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class TestViewModel @Inject constructor(private val repository: TestRepository) : BaseViewModel() {

    private var gankdata: Flow<PagingData<Bean>>? = null

    //仅仅请求服务器
    fun getGankRomete(): Flow<PagingData<Bean>> {
        val newResult: Flow<PagingData<Bean>> = repository.getGankRomete()
            .cachedIn(viewModelScope)
        gankdata = newResult
        return newResult
    }

    private val flow = MutableStateFlow(MeiZiBean())
    val _flow = flow.asStateFlow()
    fun test() {
        flowRequest(flow) {
            repository.test()
        }
    }

    init {
        test()
    }
}