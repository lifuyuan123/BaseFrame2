package com.example.baseframe.utils

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.vincent.videocompressor.VideoCompress
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


/**
 * @Author:  admin
 * @Date:  2021/12/15-9:49
 * @describe:  文件工具
 */
object FileUtils {

    /**
     * 获取android9以下公共目录文件
     * fileName 文件名
     * catalogue 子目录
     */
    fun getFileAndroid9(fileName: String, catalogue: String): File {
        val file = File(
            "${getPublickDiskFileDirAndroid9(Environment.DIRECTORY_DOWNLOADS)}/$catalogue",
            fileName
        )
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        return file
    }


    /**
     * android9以下获取公共目录
     */
    private fun getPublickDiskFileDirAndroid9(fileDir: String?): String? {
        var filePath: String? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
            || !Environment.isExternalStorageRemovable()
        ) {
            filePath = Environment.getExternalStoragePublicDirectory(fileDir).path
        }
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    /**
     * 根据Uri获取文件绝对路径，解决Android4.4以上版本Uri转换 兼容Android 10
     *
     * @param context
     * @param imageUri
     */
    fun getFileAbsolutePath(context: Context, imageUri: Uri?): String? {
        if (imageUri == null) {
            return null
        }

        //此方法 只能用于4.4以下的版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return getRealFilePath(context, imageUri)
        }
        //大于4.4小于10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && DocumentsContract.isDocumentUri(
                context,
                imageUri
            )
        ) {
            if ("com.android.externalstorage.documents" == imageUri.authority) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == imageUri.authority) {
                val id = DocumentsContract.getDocumentId(imageUri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents" == imageUri.authority) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return if (contentUri == null) null else getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        }
        return when {
            //大于10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
//                uriToFileApiQ(context, imageUri)
                //获取文件地址
                getFileFromContentUri(context, imageUri)
            }
            "content".equals(imageUri.scheme, ignoreCase = true) -> {
                // Return the remote address
                if ("com.google.android.apps.photos.content" == imageUri.authority) {
                    imageUri.lastPathSegment
                } else getDataColumn(context, imageUri, null, null)
            }
            "file".equals(imageUri.scheme, ignoreCase = true) -> {
                imageUri.path
            }
            else -> null
        }
    }

    private fun getImageAbsolutePath(context: Context?, imageUri: Uri?): String? {
        if (context == null || imageUri == null) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                imageUri
            )
        ) {
            if ("com.android.externalstorage.documents" == imageUri.authority) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == imageUri.authority) {
                val id = DocumentsContract.getDocumentId(imageUri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(
                    context,
                    contentUri,
                    null,
                    null
                )
            } else if ("com.android.providers.media.documents" == imageUri.authority) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } // MediaStore (and general)
        else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if ("com.google.android.apps.photos.content" == imageUri.authority) imageUri.lastPathSegment else getDataColumn(
                context,
                imageUri,
                null,
                null
            )
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }


    /**
     * 此方法 只能用于4.4以下的版本
     */
    private fun getRealFilePath(context: Context, uri: Uri): String? {
        if (null == uri) {
            return null
        }
        val scheme: String? = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
            val cursor: Cursor? =
                context.contentResolver.query(uri, projection, null, null, null)
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }


    /**
     * 获取文件地址
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor =
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * Android 10 以上适配 此方法是直接获取文件地址
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("Range")
    private fun getFileFromContentUri(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val filePath: String
        val filePathColumn =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            uri, filePathColumn, null,
            null, null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            try {
                filePath = cursor.getString(cursor?.getColumnIndex(filePathColumn[0]))
                return filePath
            } catch (e: Exception) {
            } finally {
                cursor.close()
            }
        }
        return ""
    }

    /**
     * Android 10 以上适配  此方法是复制文件到私有目录
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun uriToFileApiQ(context: Context, uri: Uri): String? {
        var file: File? = null
        //android10以上转换
        if (uri.scheme.equals(ContentResolver.SCHEME_FILE)) {
            file = File(uri.path)
        } else if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (cursor.moveToFirst()) {
                    val displayName: String =
                        cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    try {
                        val `is`: InputStream? = contentResolver.openInputStream(uri)
                        val cache = File(
                            context.externalCacheDir?.absolutePath,
                            ((Math.random() + 1) * 1000).roundToInt().toString() + displayName
                        )
                        val fos = FileOutputStream(cache)

                        val b = ByteArray(`is`!!.available())
                        `is`?.read(b)
                        fos.write(b)

                        file = cache
                        fos.close()
                        `is`?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

        }
        return file!!.absolutePath
    }


    /**
     * path转uri
     */
    fun toUri(context: Context, filePath: String?): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                context.applicationInfo.packageName + ".fileprovider",
                File(filePath)
            )
        } else Uri.fromFile(File(filePath))
    }

    /**
     * 获取私有目录  所有版本可用
     */
    private fun getPrivateDiskFileDir(
        context: Context
    ): String {
        var cachePath = context.cacheDir?.path + "/crash/"
        val file = File(cachePath)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.path //mnt/sdcard/Android/data/com.my.app/files/fileName
    }

    /**
     * 压缩视频
     */
    @Suppress("INACCESSIBLE_TYPE")
    fun videoCompress(
        context: Context,
        path: String,
        listener: CompressListener? = null,
        quality: Int = 3
    ) {
        var destPath: String? =
            getPrivateDiskFileDir(context.applicationContext) + "out_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            ) + ".mp4"
        //1,2,3  高中低
        VideoCompress.compressVideo(quality, path, destPath, object :
            VideoCompress.CompressListener {
            override fun onStart() {
                listener?.onStart()
                Timber.e("压缩 开始")
            }

            override fun onSuccess() {

                if (Build.VERSION.SDK_INT >= 29) {
                    destPath = copyPrivateToDownload(
                        context,
                        destPath,
                        "out_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".mp4"
                    )
                    Timber.e("压缩成功   30")
                } else {
                    destPath = saveFile(
                        context,
                        destPath,
                        "out_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".mp4"
                    )
                    Timber.e("压缩成功   29")
                }
                listener?.onSuccess(destPath)

            }

            override fun onFail() {
                Timber.e("压缩 失败")
                listener?.onFail()
            }

            override fun onProgress(percent: Float) {
                Timber.e("压缩 进度 ${percent}%")
                listener?.onProgress(percent)
            }

        })
    }


    /**
     * android10及以上  插入到公共media目录
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun copyPrivateToDownload(
        context: Context,
        orgFilePath: String?,
        displayName: String?
    ): String? {
        val values = ContentValues()
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.Files.FileColumns.MIME_TYPE, "video/mp4") //MediaStore对应类型名
        values.put(MediaStore.Files.FileColumns.TITLE, displayName)
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Download/Video") //公共目录下目录名
        val external = MediaStore.Downloads.EXTERNAL_CONTENT_URI //内部存储的Download路径
        val resolver = context.contentResolver
        val insertUri = resolver.insert(external, values) //使用ContentResolver创建需要操作的文件
        var ist: InputStream? = null
        var ost: OutputStream? = null
        try {
            ist = FileInputStream(File(orgFilePath))
            if (insertUri != null) {
                ost = resolver.openOutputStream(insertUri)
            }
            if (ost != null) {
                val buffer = ByteArray(4096)
                var byteCount = 0
                while (ist.read(buffer).also { byteCount = it } != -1) {  // 循环从输入流读取 buffer字节
                    ost.write(buffer, 0, byteCount) // 将读取的输入流写入到输出流
                }
            }
        } catch (e: IOException) {
            Timber.e("copyPrivateToDownload $e")
        } finally {
            try {
                ist?.close()
                ost?.close()
            } catch (e: IOException) {
                Timber.e("copyPrivateToDownload $e")
            }
        }
        //更新系统相册
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, insertUri))
        return displayName
    }

    /**
     * android9及一下 保存到公共目录
     */
    fun saveFile(context: Context, path: String?, fileName: String?): String {

        val file = File(
            "${getPublickDiskFileDirAndroid9(Environment.DIRECTORY_DOWNLOADS)}/Video",
            fileName
        )
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        if (!file.exists()) {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)
        val bis = BufferedInputStream(FileInputStream(File(path)))
        val buffer = ByteArray(1024)
        var len: Int
        var total: Long = 0
        while (bis.read(buffer).also { len = it } != -1) {
            fos.write(buffer, 0, len)
            total += len.toLong()
        }
        Timber.e("path ${file.path}")

        // 最后通知图库更新
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(File(file.path))
            )
        )

        return file.path
    }

    /**
     * path转bytearray
     */
    fun pathToByteArray(path: String): ByteArray? {
        try {
            val input = FileInputStream(File(path))
            val data = ByteArray(input.available())
            input.read(data)
            input.close()
            return data
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.e("读取异常  $e")
        }
        return null
    }


    /**
     * uri转bitmap  这里专注于扫码
     */
    fun uriToBitmap(
        context: Context,
        uri: Uri?,
        maxWidth: Int = 400,
        maxHeight: Int = 400
    ): Bitmap {
        //获取图片目录
        val path = getImageAbsolutePath(context.applicationContext, uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        var imageHeight = options.outHeight
        var imageWidth = options.outWidth
        var sampleSize = 1
        //这里处理不好扫码不能识别
        while (1.let { imageWidth = imageWidth shr 1; imageWidth } >= maxWidth && 1.let {
                imageHeight = imageHeight shr it; imageHeight
            } >= maxHeight) {
            sampleSize = sampleSize shl 1
        }
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)

    }

    interface CompressListener {
        fun onSuccess(path: String?)
        fun onFail()
        fun onStart()
        fun onProgress(progress: Float)
    }

    val listener: CompressListener? = null
}