package com.example.baseframe.ui.test

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.baseframe.api.Service
import com.example.baseframe.entity.BaseBean
import com.example.baseframe.entity.Bean
import com.example.baseframe.ui.view.MultiPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TestRepository @Inject constructor(private val service: Service) {

    fun getGankRomete(): Flow<PagingData<Bean>> {
        //pageSize：每页多少个条目；必填 prefetchDistance ：预加载下一页的距离，滑动到倒数第几个条目就加载下一页，无缝加载（可选）默认值是pageSize
        //initialLoadSize ：第一页加载条目数量 ，可选，默认值是 3*pageSize （有时候需要第一页多点数据可用）
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                prefetchDistance = 1,
                initialLoadSize = 2,
                enablePlaceholders = false
            ),
//            pagingSourceFactory = {GankPagingSource(service)}
            pagingSourceFactory = {
                MultiPagingSource() { page ->
//                        service.getGank("20", "$page")
                    val list = mutableListOf<Bean>()
                    for (index in 1..10) {
                        val title = "${System.currentTimeMillis()}-${index + (page - 1) * 10}"
//                        list.add(index-1, Bean(url="https://imgapi.cn/cos.php?return=url$title",title = "${index+(page-1)*10}"))
                        list.add(
                            index - 1,
                            Bean(
                                url = "https://imgapi.cn/api.php?fl=meizi&gs=image$title",
                                title = "${index + (page - 1) * 10}"
                            )
                        )
                    }
                    BaseBean(list, 100000)
                }
            }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}