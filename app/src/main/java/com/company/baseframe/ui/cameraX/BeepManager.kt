package com.company.baseframe.ui.cameraX

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Vibrator
import android.util.Log
import com.company.baseframe.R
import java.io.Closeable
import java.io.IOException

/**
 * 管理声音和震动
 */
class BeepManager(private val activity: Activity) : OnCompletionListener,
    MediaPlayer.OnErrorListener, Closeable {
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep = true
    private var vibrate = true


    fun setPlayBeep(playBeep: Boolean) {
        this.playBeep = playBeep
    }

    fun setVibrate(vibrate: Boolean) {
        this.vibrate = vibrate
    }

    @Synchronized
    fun updatePrefs() {
        if (playBeep && mediaPlayer == null) {
            // STREAM_SYSTEM上的音量不可调，用户找到了它 太大声,
            //我们现在在音乐流上播放
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = buildMediaPlayer(activity)
        }
    }

    /**
     * 开启响铃和震动
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer?.start()
        }
        if (vibrate) {
            val vibrator = activity
                .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    /**
     * 创建MediaPlayer
     *
     * @param activity
     * @return
     */
    private fun buildMediaPlayer(activity: Context): MediaPlayer? {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        // 监听是否播放完成
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        // 配置播放资源
        return try {
            val file = activity.resources
                .openRawResourceFd(R.raw.beep_sound)
            try {
                mediaPlayer.setDataSource(
                    file.fileDescriptor,
                    file.startOffset, file.length
                )
            } finally {
                file.close()
            }
            // 设置音量
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
            mediaPlayer.prepare()
            mediaPlayer
        } catch (ioe: IOException) {
            Log.e("TAG", ioe.toString())
            mediaPlayer.release()
            null
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        //当哔哔声结束播放后，倒回去再排一个
        mp.seekTo(0)
    }

    @Synchronized
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // 我们讲完了，所以如果需要的话，请写一个适当的祝酒词 并完成
            activity.finish()
        } else {
            // 可能媒体播放器错误，所以释放和重建
            mp.release()
            mediaPlayer = null
            updatePrefs()
        }
        return true
    }

    @Synchronized
    override fun close() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    companion object {
        private const val BEEP_VOLUME = 0.10f
        private const val VIBRATE_DURATION = 200L
    }

    init {
        updatePrefs()
    }
}