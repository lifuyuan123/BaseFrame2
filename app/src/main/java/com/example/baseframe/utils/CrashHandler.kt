package com.example.baseframe.utils

import android.content.Context
import com.blankj.utilcode.util.DeviceUtils
import com.lfy.baselibrary.ActivityManager
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @Author admin
 * @Date 2021/8/27-14:49
 * @describe: 手机崩溃日志
 */
class CrashHandler private constructor(): Thread.UncaughtExceptionHandler {

    private var context: Context? = null
    private var path: String? = null
    //用于格式化日期,作为日志文件名的一部分
    private val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")

    companion object {
        val instance by lazy {
            CrashHandler()
        }
    }


    //异常回调
    override fun uncaughtException(t: Thread, e: Throwable) {
        val sb = StringBuffer()

        sb.append("型号:  ${DeviceUtils.getModel()}\n")
        sb.append("制造商:  ${DeviceUtils.getManufacturer()}\n")
        sb.append("系统版本号:  ${DeviceUtils.getSDKVersionName()}  ${DeviceUtils.getSDKVersionCode()}\n")

        //崩溃信息
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        e.printStackTrace(printWriter)
        var cause: Throwable? = e.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result: String = writer.toString()

        sb.append(result)//添加崩溃信息

        //写入本地文件
        var fos: FileOutputStream? = null
        try {
            Timber.e("an error  开始写入:\n$sb")
            val time: String = formatter.format(Date())
            val fileName = "crash-$time.log"

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            fos = FileOutputStream(path + fileName)
            fos.write(sb.toString().toByteArray())

        } catch (e: Exception) {
            Timber.e("an error occured while writing file...  $e")
        } finally {
            try {
                fos?.close()
                ActivityManager.instance.appExit()
            } catch (e: IOException) {
                Timber.e("an error occured while fos.close...  $e")
            }
        }
    }

    //设置监听
    fun init(context: Context) {
        this.context = context
        val diskCachePath = FileUtils.getDiskCachePath(context)
        path = "$diskCachePath/crash/"
        Thread.setDefaultUncaughtExceptionHandler(this)
        deleteFile()
    }

    //删除15天前崩溃日志文件
    private fun deleteFile() {
        val file = File(path)
        val nowDate = Date()

        if (file.exists() && file.listFiles().isNotEmpty()) {
            for (item in file.listFiles()) {
                if (item.isFile) {
                    val split = item.name.split(".")
                    if (split.isNotEmpty()) {
                        val old = split[0].substring(6, split[0].length)//截取时间戳
                        val oldDate = formatter.parse(old)
                        val diff = nowDate.time - oldDate.time
                        val days = diff / (1000 * 60 * 60 * 24)
                        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                        if (days > 15) {
                            item.delete()
                        }
                    }
                }
            }
        }
    }


}