package com.example.baseframe.ui.cameraX

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.baseframe.R
import com.example.baseframe.databinding.ActivityCameraBinding
import com.example.baseframe.utils.FileUtils
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.example.baseframe.utils.Tags
import com.lfy.baselibrary.entity.PermissType
import com.lfy.baselibrary.permiss
import com.lfy.baselibrary.ui.activity.BaseActivity
import com.lfy.baselibrary.visible
import com.lfy.baselibrary.vm.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : BaseActivity<ActivityCameraBinding, BaseViewModel>() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var imageCapture: ImageCapture
    private lateinit var videoCapture: VideoCapture
    private var scaleDetector: ScaleGestureDetector? = null//手势缩放
    private var doubleClickDetector: GestureDetector? = null//双击缩放
    private var singleTapDetector: GestureDetector? = null//聚焦
    private lateinit var cameraZoomState: LiveData<ZoomState>//用于聚焦和缩放
    private lateinit var mCamera: Camera
    private val beepManager by lazy { BeepManager(this) }
    private var isBack = true
    private var isVideo = false
    private var isFlahsOn = false
    private var isRecording = false
    private val mSize by lazy { Size(720, 1280 ) }
    private lateinit var startActivitylaunch: ActivityResultLauncher<String>

    override fun getLayout() = R.layout.activity_camera
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivitylaunch =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                try {
                    //根据uri获取对应位图
                    val scanBitmap = FileUtils.uriToBitmap(applicationContext, it)
                    //转化成扫码需要的信息
                    val bitmap = BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(scanBitmap)))

                    val result = MultiFormatReader().decodeWithState(bitmap).text

                    //停止分析
                    imageAnalysis.clearAnalyzer()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            binding.qrZone.visibility = View.GONE
                            binding.qrZone.stopAnimator()
                        }
                    }
                    //振动、提示音
                    beepManager.playBeepSoundAndVibrate()

                    //设置返回数据
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply { putExtra(Tags.DATA, result) })
                    finish()
                } catch (e: Exception) {
                    Timber.e("解析异常 $e")
                }
            }
    }
    
    override fun initData(savedInstanceState: Bundle?) {
        binding.cameraActivity = this
        permiss(PermissType.PermissCameraWriteRecord){
            initCamera()
        }
    }

    /**
     * 相机初始化
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initCamera() {
        //初始化
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //检查 CameraProvider 可用性  能否在视图创建后成功初始化
        cameraProviderFuture.addListener(Runnable {

            cameraProvider = cameraProviderFuture.get()

            bindPreview(cameraProvider)

            //监听双击、单击、缩放等功能
            binding.previewView.setOnTouchListener { v, event ->
                //手势缩放
                scalePreview(event)
                //双击缩放
                doubleClickZoom(event)
                //聚焦
                singleTapForFocus(event)
                true
            }


        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 相机预览绑定
     */
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //创建 Preview
        preview = Preview.Builder()
            .build()

        //指定所需的相机 LensFacing 选项。  后置摄像头
        val cameraSelector =
            if (isBack) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

        //将 Preview 连接到 PreviewView
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        //图片分析
        imageAnalysis = ImageAnalysis.Builder()
            //这里有个大坑 宽高写反会扫描不出来
            .setTargetResolution(mSize)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        //拍照设置
        imageCapture = if (binding.previewView.display?.rotation != null) {
            ImageCapture.Builder()
                //设置预期的输出目标分辨率
                .setTargetResolution(mSize)
                //闪光
//                .setFlashMode(FLASH_MODE_ON)
                .build()
        } else {
            ImageCapture.Builder()
                .build()
        }

        //录像设置
        videoCapture = VideoCapture.Builder()//录像用例配置
//            .setTargetAspectRatio(binding.previewView.display.rotation) //设置高宽比
//            .setTargetRotation(binding.previewView.display.rotation)//设置旋转角度
            //设置预期的输出目标分辨率
            .setTargetResolution(mSize)
            .setVideoFrameRate(25)
            .setBitRate(3 * 1024 * 1024)
            .build()

        //先解绑所有用例
        cameraProvider.unbindAll()

        try {
            //将所选相机和任意用例绑定到生命周期。
            mCamera = if (isVideo) {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    videoCapture,
                    preview
                )
            } else {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imageAnalysis,
                    imageCapture,
                    preview
                )
            }
        } catch (e: Exception) {
            Timber.e("相机预览初始化  $e")
        }

        //获取缩放状态
        cameraZoomState = mCamera.cameraInfo.zoomState


        //聚焦中心
        focusOnPosition(
            (binding.previewView.width / 2).toFloat(),
            (binding.previewView.height / 2).toFloat()
        )

        binding.qrZone.visibility = View.GONE
    }

    /**
     * 聚焦
     */
    private fun singleTapForFocus(event: MotionEvent) {
        if (singleTapDetector == null) {
            singleTapDetector = GestureDetector(this,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                        Timber.e("singleTapForFocus  Single tap confirmed with event:$e")
                        focusOnPosition(event.x, event.y, true)
                        return super.onSingleTapConfirmed(e)
                    }
                })
        }
        singleTapDetector?.onTouchEvent(event)
    }

    /**
     * 聚焦点击位置
     */
    private fun focusOnPosition(x: Float, y: Float, isShow: Boolean = false) {
        // 点击时聚焦视图
        val action = FocusMeteringAction.Builder(
            binding.previewView.meteringPointFactory.createPoint(x, y)
        ).build()
        try {
            //展示聚焦方框
            if (isShow) {
                showTapView(x.toInt(), y.toInt())
            }
            Timber.e("focusOnPosition  Focus camera")
            mCamera.cameraControl.startFocusAndMetering(action)
        } catch (e: Exception) {
            Timber.e("focusOnPosition  Error focus camera:e")
        }
    }

    /**
     * 显示聚焦方框
     */
    private fun showTapView(x: Int, y: Int) {
        val popupWindow = PopupWindow(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.ic_focus_view)
        val offset = resources.getDimensionPixelSize(R.dimen.tap_view_size)
        Timber.e("singleTapForFocus  showTapView offset:$offset")
        popupWindow.contentView = imageView
        popupWindow.showAsDropDown(binding.previewView, x - offset / 2, y - offset / 2)
        binding.previewView.postDelayed({ popupWindow.dismiss() }, 600)

    }

    /**
     * 双击缩放
     */
    private fun doubleClickZoom(event: MotionEvent?) {
        Timber.e("doubleClickZoom  doubleClickZoom event:$event")
        if (doubleClickDetector == null) {
            doubleClickDetector = GestureDetector(this,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent?): Boolean {
                        Log.e("doubleClickZoom", "Double tap")
                        cameraZoomState.value?.let {
                            val zoomRatio = it.zoomRatio
                            val minRatio = it.minZoomRatio
                            Timber.e("doubleClickZoom  Double tap zoomRatio:$zoomRatio min:$minRatio max:${it.maxZoomRatio}")
                            // Ratio parameter from 0f to 1f.
                            if (zoomRatio > minRatio) {
                                // Reset to original ratio
                                mCamera.cameraControl.setLinearZoom(0f)
                            } else {
                                // Or zoom to 0.5 ratio
                                mCamera.cameraControl.setLinearZoom(0.5f)
                            }
                        }
                        return true
                    }
                })
        }
        Timber.e("doubleClickZoom   doubleClickDetector onTouchEvent")
        doubleClickDetector?.onTouchEvent(event)
    }


    /**
     * 缩放
     */
    private fun scalePreview(event: MotionEvent?) {
        if (scaleDetector == null) {
            scaleDetector = ScaleGestureDetector(this,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        cameraZoomState.value?.let {
                            val zoomRatio = it.zoomRatio
                            Timber.e("scale  Scale factor:${detector.scaleFactor} current:$zoomRatio linear:${it.linearZoom}")
                            mCamera.cameraControl.setZoomRatio(zoomRatio * detector.scaleFactor)
                        }
                        return true
                    }
                })
        }
        Timber.e("scale   scalePreview onTouchEvent")
        scaleDetector?.onTouchEvent(event)
    }


    /**
     * 选择操作模式  拍照还是录制
     */
    fun takePicktureOrVideo(view: View) {

        if (isVideo) {
            takeVideo()
        } else {
            takePickture()
        }
    }


    /**
     * 拍照
     */
    @SuppressLint("RestrictedApi")
    fun takePickture() {
        //文件目录设置
        val fileName = "cameraX_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".JPG"
        val outputFileOptions = if (true) {//Build.VERSION.SDK_INT >= 29
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )//文件名
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")//MediaStore对应类型名
            ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()
        } else {
            ImageCapture.OutputFileOptions.Builder(
                FileUtils.getFileAndroid9(fileName, "picture")
            ).build()
        }
        Timber.e("镜像：  $isBack")
        outputFileOptions.metadata.isReversedHorizontal = !isBack
        //开始拍照
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Timber.e("保存失败： $error")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Timber.e(
                        "保存： ${outputFileResults.savedUri}    ${
                            FileUtils.getFileAbsolutePath(
                                applicationContext,
                                outputFileResults.savedUri
                            )
                        }"
                    )
                    sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            outputFileResults.savedUri
                        )
                    )

                    //获取文件路径
                    val path = FileUtils.getFileAbsolutePath(
                        applicationContext,
                        outputFileResults.savedUri
                    )
                    //设置返回数据
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply { putExtra(Tags.DATA, path) })
                    finish()
                }
            })
    }

    /**
     * 拍视频
     */
    @SuppressLint("MissingPermission", "RestrictedApi")
    fun takeVideo() {

        isRecording = !isRecording
        binding.camera.setImageResource(
            if (isRecording) R.drawable.ic_capture_record_pressing else R.drawable.ic_capture_record
        )
        if (isRecording) {
            //文件目录设置
            val fileName =
                "cameraX_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".mp4"
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )//文件名
            contentValues.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "video/mp4"
            )//MediaStore对应类型名
            val outputFileOptions = VideoCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()

            lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.Default) {
                    //开始录像
                    videoCapture.startRecording(
                        outputFileOptions,
                        ContextCompat.getMainExecutor(this@CameraActivity),
                        object : VideoCapture.OnVideoSavedCallback {
                            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                                Timber.e(
                                    "保存录像：  ${outputFileResults.savedUri}    ${
                                        FileUtils.getFileAbsolutePath(
                                            applicationContext,
                                            outputFileResults.savedUri
                                        )
                                    }"
                                )

                                sendBroadcast(
                                    Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        outputFileResults.savedUri
                                    )
                                )

                                //获取文件路径
                                val path = FileUtils.getFileAbsolutePath(
                                    applicationContext,
                                    outputFileResults.savedUri
                                )
                                //设置返回数据
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent().apply { putExtra(Tags.DATA, path) })
                                finish()
                            }

                            override fun onError(
                                videoCaptureError: Int,
                                message: String,
                                cause: Throwable?
                            ) {
                                Timber.e("保存录像onError： $cause   $message")
                            }

                        })
                }

            }
        } else {
            videoCapture?.stopRecording()//停止录制
        }
    }

    /**
     * 切换前后摄像头
     */
    fun switchs(view: View) {
        isBack = !isBack
        bindPreview(cameraProvider)
        imageAnalysis.clearAnalyzer()
    }

    /**
     * 扫码  图片分析
     */
    @SuppressLint("RestrictedApi")
    fun scan(view: View) {

        //已经显示扫码界面就从相册扫码
        if (binding.qrZone.visibility == View.VISIBLE) {
            startActivitylaunch.launch("image/*")
        } else {

            binding.qrZone.visibility = View.VISIBLE

            //是指缩放比例
            mCamera.cameraControl.setZoomRatio(1f)

            imageAnalysis.setAnalyzer(CameraXExecutors.ioExecutor()) { imageProxy ->
                val rotation = imageProxy.imageInfo.rotationDegrees
                Timber.e("camera角度：$rotation")

                val byteBuffer = imageProxy.planes[0].buffer
                val data = ByteArray(byteBuffer.remaining())
                byteBuffer[data]

                val width = imageProxy.width
                val height = imageProxy.height
                Timber.e("图片：  $width  $height")
                val source = PlanarYUVLuminanceSource(
                    data, width, height, 0, 0, width, height, false
                )

                val bitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = MultiFormatReader().decode(bitmap).text

                    //停止分析
                    imageAnalysis.clearAnalyzer()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            binding.qrZone.visibility = View.GONE
                            binding.qrZone.stopAnimator()
                        }
                    }

                    //振动、提示音
                    beepManager.playBeepSoundAndVibrate()
                    mCamera.cameraControl.setZoomRatio(1f)

                    lifecycleScope.launchWhenResumed {
                        //设置返回数据
                        setResult(
                            Activity.RESULT_OK,
                            Intent().apply { putExtra(Tags.DATA, result) })
                        finish()
                    }
                    Timber.e("Camera   result:$result")
                } catch (e: Exception) {
                    Timber.e("Camera  Error decoding barcode ${e.message}")
                    imageProxy.close()
                }
                imageProxy.close()
            }
        }
    }


    override fun onDestroy() {
        beepManager.close()
        super.onDestroy()
    }

    /**
     * 闪光灯
     */
    fun flash(view: View) {
        isFlahsOn = !isFlahsOn
        (view as ImageView).setImageResource(if (isFlahsOn) R.drawable.ic_torch_open else R.drawable.ic_torch_close)
        mCamera?.cameraControl?.enableTorch(isFlahsOn)
    }

    /**
     * 选择拍照、录制视频
     */
    fun choice(view: View) {
        isVideo = !isVideo

        bindPreview(cameraProvider)
        imageAnalysis.clearAnalyzer()

        binding.scan.visible(!isVideo)
        binding.ivRecord.setImageResource(if (isVideo) R.drawable.ic_camera else R.drawable.ic_video)
        binding.camera.setImageResource(
            if (isVideo)R.drawable.ic_capture_record else R.drawable.ic_capture
        )
    }
}