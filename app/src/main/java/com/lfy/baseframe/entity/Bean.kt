package com.lfy.baseframe.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gank")
data class Bean(
    @PrimaryKey
    val _id: String="",
    val createdAt: String="",
    val desc: String="",
    val publishedAt: String="",
    val source: String="",
    val type: String="",
    val url: String="",
    val title: String="",
    val used: Boolean=false,
    val views: Int=0,
    val who: String=""
)