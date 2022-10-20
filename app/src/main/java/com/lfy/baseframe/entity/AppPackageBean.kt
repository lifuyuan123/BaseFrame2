package com.lfy.baseframe.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @Author admin
 * @Date 2022/10/19-9:52
 * @describe: app更新实体
 */
@Parcelize
data class AppPackageBean(
    val appName: String = "",
    val downloadAddress: String = "",
    val id: String = "",
    val versionCode: Int,
    val versionDescribe: String = "",
    val versionName: String = "",
    val createTime: String? = "",
    val packageName: String? = "",
    val packageSize: String = ""
) : Parcelable