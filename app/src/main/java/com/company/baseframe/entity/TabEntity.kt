package com.company.baseframe.entity

import com.flyco.tablayout.listener.CustomTabEntity

class TabEntity constructor(var title :String,var selectedIcon :Int, var unSelectedIcon:Int) :CustomTabEntity {
    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}
