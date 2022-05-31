package com.example.baseframe.ui.web

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel

@HiltViewModel
class WebViewModel @Inject constructor() : BaseViewModel(){

    @Inject lateinit var repository: WebRepository

}