package com.example.baseframe.di

import com.example.baseframe.R
import com.flyco.tablayout.listener.CustomTabEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Qualifier

/**
 * @Author admin
 * @Date 2021/8/6-10:32
 * @describe:
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindSelectIds

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindUnselectedIds


@Module
@InstallIn(ActivityComponent::class)
object MainFragmentModule {

    @BindUnselectedIds
    @Provides
    fun provideIconUnselectedIds()=intArrayOf(
        R.mipmap.icon_home,
        R.mipmap.icon_find,
        R.mipmap.icon_mine
    )

    @BindSelectIds
    @Provides
    fun provideIconSelectIds()=intArrayOf(
        R.mipmap.icon_home_choice,
        R.mipmap.icon_find_choice,
        R.mipmap.icon_mine_choice
    )

    @Provides
    fun provideTabEntities()=ArrayList<CustomTabEntity>()

    @Provides
    fun provideTitles()=arrayOf("妹子", "发现", "我的")
}