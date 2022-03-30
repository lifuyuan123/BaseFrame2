package com.example.baseframe.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var repository: MainRepository

}