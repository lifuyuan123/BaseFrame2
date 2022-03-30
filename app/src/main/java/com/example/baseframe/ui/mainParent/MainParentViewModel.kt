package com.example.baseframe.ui.mainParent

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.lfy.baselibrary.vm.BaseViewModel

@HiltViewModel
class MainParentViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var repository: MainParentRepository

}