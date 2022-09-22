package com.example.baseframe.entity

import java.io.Serializable
import kotlin.random.Random

/**
 * 接收类
 */
data class BaseBean<T>(
    var data: T,
    val code: Int = 0,
    val message: String = "",
    var page_count: Int = 0
) : Serializable {
    val isSuccess: Boolean
        get() = code == 200

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    override fun equals(other: Any?) = false
}