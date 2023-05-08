package com.company.baseframe.ui.view

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.company.baseframe.entity.BaseBean

/**
 * @Author admin
 * @Date 2021/10/20-16:56
 * @describe:  分页加载
 */
class MultiPagingSource<T:Any>( val respon:suspend (Int)-> BaseBean<MutableList<T>>): PagingSource<Int, T>() {
    //刷新键用于PagingSource的后续刷新调用。初始加载后再加载
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        //我们需要获取页面的前一个键(或者下一个键，如果前一个键是null)
        //最接近最近访问的索引。
        //锚的位置是最近访问的索引
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        //如果未定义，在第1页开始刷新
        try {
            val nextPageNumber = params.key ?: 1
            Log.e("page: ","$nextPageNumber")

            val response=respon(nextPageNumber)

            val ganks = response.data
            //返回结果为空或者已经是最大页数
            val nextKey = if (ganks.isNullOrEmpty()||response.page_count==nextPageNumber) {
                null
            } else {
                Log.e("page: ---","${nextPageNumber +1}")
                nextPageNumber +1
            }

            return LoadResult.Page(
                data = ganks,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,//上一页
                nextKey = nextKey
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}