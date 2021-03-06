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
    private var scaleDetector: ScaleGestureDetector? = null//????????????
    private var doubleClickDetector: GestureDetector? = null//????????????
    private var singleTapDetector: GestureDetector? = null//??????
    private lateinit var cameraZoomState: LiveData<ZoomState>//?????????????????????
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
                    //??????uri??????????????????
                    val scanBitmap = FileUtils.uriToBitmap(applicationContext, it)
                    //??????????????????????????????
                    val bitmap = BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(scanBitmap)))

                    val result = MultiFormatReader().decodeWithState(bitmap).text

                    //????????????
                    imageAnalysis.clearAnalyzer()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            binding.qrZone.visibility = View.GONE
                            binding.qrZone.stopAnimator()
                        }
                    }
                    //??????????????????
                    beepManager.playBeepSoundAndVibrate()

                    //??????????????????
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply { putExtra(Tags.DATA, result) })
                    finish()
                } catch (e: Exception) {
                    Timber.e("???????????? $e")
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
     * ???????????????
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initCamera() {
        //?????????
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //?????? CameraProvider ?????????  ???????????????????????????????????????
        cameraProviderFuture.addListener(Runnable {

            cameraProvider = cameraProviderFuture.get()

            bindPreview(cameraProvider)

            //???????????????????????????????????????
            binding.previewView.setOnTouchListener { v, event ->
                //????????????
                scalePreview(event)
                //????????????
                doubleClickZoom(event)
                //??????
                singleTapForFocus(event)
                true
            }


        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * ??????????????????
     */
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //?????? Preview
        preview = Preview.Builder()
            .build()

        //????????????????????? LensFacing ?????????  ???????????????
        val cameraSelector =
            if (isBack) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

        //??? Preview ????????? PreviewView
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        //????????????
        imageAnalysis = ImageAnalysis.Builder()
            //?????????????????? ??????????????????????????????
            .setTargetResolution(mSize)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        //????????????
        imageCapture = if (binding.previewView.display?.rotation != null) {
            ImageCapture.Builder()
                //????????????????????????????????????
                .setTargetResolution(mSize)
                //??????
//                .setFlashMode(FLASH_MODE_ON)
                .build()
        } else {
            ImageCapture.Builder()
                .build()
        }

        //????????????
        videoCapture = VideoCapture.Builder()//??????????????????
//            .setTargetAspectRatio(binding.previewView.display.rotation) //???????????????
//            .setTargetRotation(binding.previewView.display.rotation)//??????????????????
            //????????????????????????????????????
            .setTargetResolution(mSize)
            .setVideoFrameRate(25)
            .setBitRate(3 * 1024 * 1024)
            .build()

        //?????????????????????
        cameraProvider.unbindAll()

        try {
            //??????????????????????????????????????????????????????
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
            Timber.e("?????????????????????  $e")
        }

        //??????????????????
        cameraZoomState = mCamera.cameraInfo.zoomState


        //????????????
        focusOnPosition(
            (binding.previewView.width / 2).toFloat(),
            (binding.previewView.height / 2).toFloat()
        )

        binding.qrZone.visibility = View.GONE
    }

    /**
     * ??????
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
     * ??????????????????
     */
    private fun focusOnPosition(x: Float, y: Float, isShow: Boolean = false) {
        // ?????????????????????
        val action = FocusMeteringAction.Builder(
            binding.previewView.meteringPointFactory.createPoint(x, y)
        ).build()
        try {
            //??????????????????
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
     * ??????????????????
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
     * ????????????
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
     * ??????
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
     * ??????????????????  ??????????????????
     */
    fun takePicktureOrVideo(view: View) {

        if (isVideo) {
            takeVideo()
        } else {
            takePickture()
        }
    }


    /**
     * ??????
     */
    @SuppressLint("RestrictedApi")
    fun takePickture() {
        //??????????????????
        val fileName = "cameraX_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".JPG"
        val outputFileOptions = if (true) {//Build.VERSION.SDK_INT >= 29
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )//?????????
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")//MediaStore???????????????
            ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()
        } else {
            ImageCapture.OutputFileOptions.Builder(
                FileUtils.getFileAndroid9(fileName, "picture")
            ).build()
        }
        Timber.e("?????????  $isBack")
        outputFileOptions.metadata.isReversedHorizontal = !isBack
        //????????????
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Timber.e("??????????????? $error")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Timber.e(
                        "????????? ${outputFileResults.savedUri}    ${
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

                    //??????????????????
                    val path = FileUtils.getFileAbsolutePath(
                        applicationContext,
                        outputFileResults.savedUri
                    )
                    //??????????????????
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply { putExtra(Tags.DATA, path) })
                    finish()
                }
            })
    }

    /**
     * ?????????
     */
    @SuppressLint("MissingPermission", "RestrictedApi")
    fun takeVideo() {

        isRecording = !isRecording
        binding.camera.setImageResource(
            if (isRecording) R.drawable.ic_capture_record_pressing else R.drawable.ic_capture_record
        )
        if (isRecording) {
            //??????????????????
            val fileName =
                "cameraX_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".mp4"
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )//?????????
            contentValues.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "video/mp4"
            )//MediaStore???????????????
            val outputFileOptions = VideoCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()

            lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.Default) {
                    //????????????
                    videoCapture.startRecording(
                        outputFileOptions,
                        ContextCompat.getMainExecutor(this@CameraActivity),
                        object : VideoCapture.OnVideoSavedCallback {
                            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                                Timber.e(
                                    "???????????????  ${outputFileResults.savedUri}    ${
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

                                //??????????????????
                                val path = FileUtils.getFileAbsolutePath(
                                    applicationContext,
                                    outputFileResults.savedUri
                                )
                                //??????????????????
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
                                Timber.e("????????????onError??? $cause   $message")
                            }

                        })
                }

            }
        } else {
            videoCapture?.stopRecording()//????????????
        }
    }

    /**
     * ?????????????????????
     */
    fun switchs(view: View) {
        isBack = !isBack
        bindPreview(cameraProvider)
        imageAnalysis.clearAnalyzer()
    }

    /**
     * ??????  ????????????
     */
    @SuppressLint("RestrictedApi")
    fun scan(view: View) {

        //??????????????????????????????????????????
        if (binding.qrZone.visibility == View.VISIBLE) {
            startActivitylaunch.launch("image/*")
        } else {

            binding.qrZone.visibility = View.VISIBLE

            //??????????????????
            mCamera.cameraControl.setZoomRatio(1f)

            imageAnalysis.setAnalyzer(CameraXExecutors.ioExecutor()) { imageProxy ->
                val rotation = imageProxy.imageInfo.rotationDegrees
                Timber.e("camera?????????$rotation")

                val byteBuffer = imageProxy.planes[0].buffer
                val data = ByteArray(byteBuffer.remaining())
                byteBuffer[data]

                val width = imageProxy.width
                val height = imageProxy.height
                Timber.e("?????????  $width  $height")
                val source = PlanarYUVLuminanceSource(
                    data, width, height, 0, 0, width, height, false
                )

                val bitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = MultiFormatReader().decode(bitmap).text

                    //????????????
                    imageAnalysis.clearAnalyzer()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            binding.qrZone.visibility = View.GONE
                            binding.qrZone.stopAnimator()
                        }
                    }

                    //??????????????????
                    beepManager.playBeepSoundAndVibrate()
                    mCamera.cameraControl.setZoomRatio(1f)

                    lifecycleScope.launchWhenResumed {
                        //??????????????????
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
     * ?????????
     */
    fun flash(view: View) {
        isFlahsOn = !isFlahsOn
        (view as ImageView).setImageResource(if (isFlahsOn) R.drawable.ic_torch_open else R.drawable.ic_torch_close)
        mCamera?.cameraControl?.enableTorch(isFlahsOn)
    }

    /**
     * ???????????????????????????
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