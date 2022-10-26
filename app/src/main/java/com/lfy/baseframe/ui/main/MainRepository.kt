package com.lfy.baseframe.ui.main

import com.lfy.baseframe.api.Service
import com.lfy.baseframe.entity.AppPackageBean
import com.lfy.baseframe.entity.BaseBean
import com.lfy.baseframe.utils.Tags
import javax.inject.Inject

class MainRepository @Inject constructor(private val service: Service) {
    suspend fun getAppInfo(): BaseBean<AppPackageBean> = service.getAppInfo(Tags.PGYER_API_KEY, Tags.PGYER_APPKEY)
}