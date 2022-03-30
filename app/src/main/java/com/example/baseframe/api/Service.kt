package com.example.baseframe.api

import com.example.baseframe.entity.BaseBean
import retrofit2.http.*


/**
 * @Author:  admin
 * @Date:  2021/7/29-11:34
 * @describe:  网络服务
 */
interface Service {
    //妹子图片
    @GET("/api/v2/data/category/Girl/type/Girl/page/{pages}/count/{counts}")
    suspend fun getGank(
        @Path("counts") count: String?,
        @Path("pages") page: String?
    ): BaseBean<MutableList<Any>>

    //干货文章
    @GET("/api/v2/data/category/GanHuo/type/Android/page/{page}/count/{count}")
    suspend fun getGanHuo(
        @Path("count") count: Int,
        @Path("page") page: Int
    ): BaseBean<MutableList<Any>>

    //banner
    @GET("/api/v2/banners")
    suspend fun getBanner(): BaseBean<MutableList<Any>>

    @GET("/cos.php")
    suspend fun getimg(
        @Query("return") json: String,
    ): BaseBean<Any>
}