package com.company.baseframe.ui.view

import androidx.paging.PagingSource
import androidx.paging.PagingState


/**
 * @Author admin
 * @Date 2021/10/20-16:45
 * @describe: 针对不需要刷新的adapter
 */
class SinglePagingSource<T : Any>(private val data:MutableList<T>): PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return LoadResult.Page(
            data = data,
            prevKey = null,//上一页
            nextKey = null
        )
    }
}