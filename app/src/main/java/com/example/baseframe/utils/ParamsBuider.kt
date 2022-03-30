package com.example.baseframe.utils


import com.jdjinsui.gsm.app.Tags
import com.tencent.mmkv.MMKV
import org.json.JSONObject

/**
 * @Author admin
 * @Date 2021/8/13-17:23
 * @describe: 构建请求参数
 */
class ParamsBuider {

    private val json=JSONObject()

    fun put(key:String,value:Any?): ParamsBuider {
        json.put(key,value)
        return this
    }

    fun putToken(): ParamsBuider {
        json.put("token",MMKV.defaultMMKV().decodeString(Tags.TOKEN,""))
        return this
    }

    fun jsonObject()=json

    fun jsonString()= json.toString()

}