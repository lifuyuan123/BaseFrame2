package com.lfy.baseframe.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * @Author:  admin
 * @Date:  2021/7/29-11:36
 * @describe:  gank分页键实体
 */
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val repoId: String,
    val prevKey: Int?,
    val nextKey: Int?
)
