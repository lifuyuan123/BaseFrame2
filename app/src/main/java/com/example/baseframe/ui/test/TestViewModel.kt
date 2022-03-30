package com.example.baseframe.ui.test

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.baseframe.entity.Bean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class TestViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var repository: TestRepository
    private var gankdata: Flow<PagingData<Bean>>? = null

    //仅仅请求服务器
    fun getGankRomete(): Flow<PagingData<Bean>> {
        val newResult: Flow<PagingData<Bean>> = repository.getGankRomete()
            .cachedIn(viewModelScope)
        gankdata = newResult
        return newResult
    }
}