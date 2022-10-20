package cn.jzvd

import java.util.*

class JZDataSource {
    @JvmField
    var currentUrlIndex: Int
    @JvmField
    var urlsMap: LinkedHashMap<Any?, Any?> = LinkedHashMap()
    var title: String? = ""
    @JvmField
    var headerMap = HashMap<String, String>()
    @JvmField
    var looping = false
    @JvmField
    var objects: Array<Any>?=null

    constructor(url: String?) {
        urlsMap[URL_KEY_DEFAULT] = url
        currentUrlIndex = 0
    }

    constructor(url: String?, title: String?) {
        urlsMap[URL_KEY_DEFAULT] = url
        this.title = title
        currentUrlIndex = 0
    }

    constructor(url: Any?) {
        urlsMap[URL_KEY_DEFAULT] = url
        currentUrlIndex = 0
    }

    constructor(urlsMap: LinkedHashMap<*, *>) {
        this.urlsMap.clear()
        this.urlsMap.putAll(urlsMap)
        currentUrlIndex = 0
    }

    constructor(urlsMap: LinkedHashMap<*, *>, title: String?) {
        this.urlsMap.clear()
        this.urlsMap.putAll(urlsMap)
        this.title = title
        currentUrlIndex = 0
    }

    val currentUrl: Any?
        get() = getValueFromLinkedMap(currentUrlIndex)

    val currentKey: Any?
        get() = getKeyFromDataSource(currentUrlIndex)

    fun getKeyFromDataSource(index: Int): String? {
        var currentIndex = 0
        for (key in urlsMap.keys) {
            if (currentIndex == index) {
                return key.toString()
            }
            currentIndex++
        }
        return null
    }

    fun getValueFromLinkedMap(index: Int): Any? {
        var currentIndex = 0
        for (key in urlsMap.keys) {
            if (currentIndex == index) {
                return urlsMap[key]
            }
            currentIndex++
        }
        return null
    }

    fun containsTheUrl(`object`: Any?): Boolean {
        return if (`object` != null) {
            urlsMap.containsValue(`object`)
        } else false
    }

    fun cloneMe(): JZDataSource {
        val map: LinkedHashMap<Any?, Any?> = LinkedHashMap()
        map.putAll(urlsMap)
        return JZDataSource(map, title)
    }

    companion object {
        const val URL_KEY_DEFAULT = "URL_KEY_DEFAULT"
    }
}