package com.example.baseframe.entity

import java.io.Serializable

/**
 * 接收类
 */
data class BaseBean<T>(var data:T,var page_count:Int=0) : Serializable