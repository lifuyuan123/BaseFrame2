package com.lfy.baseframe.ui.main

import com.lfy.baseframe.api.Service
import com.lfy.baseframe.entity.AppPackageBean
import com.lfy.baseframe.entity.BaseBean
import javax.inject.Inject

class MainRepository @Inject constructor(private val service: Service) {
    suspend fun getAppInfo(): BaseBean<AppPackageBean> = service.getAppInfo("da8226cb726c0f002baa5e8b31c126a9", "f6eea5142d2c640e9698e2ba06d2867d")
}