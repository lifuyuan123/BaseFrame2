package cn.jzvd

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView

/**
 * Created by Nathen on 2017/11/7.
 * 自定义播放器
 */
abstract class JZMediaInterface : TextureView.SurfaceTextureListener {

    constructor( jzvd: Jzvd?){
        this.jzvd=jzvd
    }
    @JvmField
    var mMediaHandlerThread: HandlerThread? = null
    @JvmField
    var mMediaHandler: Handler? = null
    @JvmField
    var handler: Handler? = null
    @JvmField
    var jzvd: Jzvd? = null
    abstract fun start()
    abstract fun prepare()
    abstract fun pause()
    abstract val isPlaying: Boolean
    abstract fun seekTo(time: Long)
    abstract fun release()
    abstract val currentPosition: Long
    abstract val duration: Long
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)
    abstract fun setSpeed(speed: Float)
    abstract fun setSurface(surface: Surface?)

    companion object {
        @JvmField
        var SAVED_SURFACE: SurfaceTexture? = null
    }

}