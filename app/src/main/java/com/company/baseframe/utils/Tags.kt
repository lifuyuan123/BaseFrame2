package com.company.baseframe.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull

/**
 * @Author admin
 * @Date 2021/8/6-17:22
 * @describe: 一些tag值
 */
object Tags {
    const val ROOM_ID = "ROOM_ID"
    const val HAS_USB_DEVICE = "HAS_USB_DEVICE"
    const val TOKEN = "TOKEN"
    const val PHONE = "PHONE"
    const val USER_ID = "USER_ID"
    const val TYPE = "TYPE"
    const val ID = "ID"
    const val URL = "URL"
    const val TITLE = "TITLE"
    const val DATA = "DATA"
    const val STATUS = "STATUS"
    const val NAME = "name"
    const val USER = "USER"
    const val LATITUDE = "LATITUDE"
    const val LONGITUDE = "LONGITUDE"
    const val PSW = "psw"
    const val ISCONNECT = "ISCONNECT"
    const val REQUEST = "request"
    const val RESPONSE = "response"
    const val UUID = "UUID"
    const val IS_FIRST_COME = "IS_FIRST_COME"
    const val PGYER = "PGYER"

    //蒲公英应用key
    const val PGYER_API_KEY = "da8226cb726c0f002baa5e8b31c126a9"
    const val PGYER_APPKEY = "f6eea5142d2c640e9698e2ba06d2867d"

    const val SELECT_DATE = "SELECT_DATE"
    const val START_DATE = "START_DATE"
    const val END_DATE = "END_DATE"



    val MEDIA_TYPE = "application/json;charset=utf-8".toMediaTypeOrNull()
    val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()
    val MEDIA_TYPE_VIDEO = "video/mpeg4".toMediaTypeOrNull()
}