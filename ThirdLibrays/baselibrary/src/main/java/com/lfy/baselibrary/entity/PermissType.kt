package com.lfy.baselibrary.entity

/**
 * @Author admin
 * @Date 2022/5/26-16:56
 * @describe:权限相关类
 */
sealed class PermissType{
    object PermissLocation:PermissType()
    object PermissPhone:PermissType()
    object PermissWrite:PermissType()
    object PermissRecord:PermissType()
    object PermissCamera:PermissType()
    object PermissLocationAndWrite:PermissType()
    object PermissCameraWriteRecord:PermissType()
}
