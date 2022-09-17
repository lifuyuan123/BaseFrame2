package com.example.baseframe.utils


import com.hjq.gson.factory.GsonFactory
import com.tencent.mmkv.MMKV
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author admin
 * @Date 2021/8/13-17:23
 * @describe: 构建请求参数
 */
class ParamsBuider {

    private val json = JSONObject()
    private val jsonArry = JSONArray()
    private val gson by lazy {
        GsonFactory.getSingletonGson()
    }

    fun putArry(value: Any?): ParamsBuider {
        jsonArry.put(value)
        return this
    }

    fun put(key: String, value: Any?): ParamsBuider {
        json.put(key, value)
        return this
    }

    fun putToken(): ParamsBuider {
        json.put("token", MMKV.defaultMMKV().decodeString(Tags.TOKEN, ""))
        return this
    }


    /**
     * post请求构建对象类型RequestBody
     **/
    fun buildObjectBody(): RequestBody {
        return gson.toJson(json).toRequestBody(Tags.MEDIA_TYPE)
    }


    fun buildArryBody(): RequestBody {
        return gson.toJson(jsonArry).toRequestBody(Tags.MEDIA_TYPE)
    }

    fun jsonObject() = json
    fun jsonArry() = jsonArry

    fun jsonString() = json.toString()

}