package com.company.baseframe.api

import com.company.baseframe.entity.AppPackageBean
import com.company.baseframe.entity.BaseBean
import com.company.baseframe.entity.MeiZiBean
import com.company.baseframe.utils.Tags
import com.lfy.baselibrary.Api
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


/**
 * @Author:  admin
 * @Date:  2021/7/29-11:34
 * @describe:  网络服务
 */
interface Service {

    @GET("/cos2.php")
    suspend fun getimg(
        @Query("return") json: String,
    ): MeiZiBean

    /**
     * 获取最新app信息
     */
    @POST
    suspend fun getAppInfo(
        @Url url: String = "${Api.PGYER_URL}/apiv2/app/view",
        @Body requestBody: RequestBody = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)
            addFormDataPart("_api_key", Tags.PGYER_API_KEY)
            addFormDataPart("appKey", Tags.PGYER_APPKEY)
        }.build()
    ): BaseBean<AppPackageBean>
}