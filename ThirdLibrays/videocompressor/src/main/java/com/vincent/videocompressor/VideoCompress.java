package com.vincent.videocompressor;

import android.os.AsyncTask;

/**
 * Created by Vincent Woo
 * Date: 2017/8/16
 * Time: 15:15
 */

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();

    public static VideoCompressTask compressVideoHigh(String srcPath, String destPath, CompressListener listener) {
        VideoCompressTask task = new VideoCompressTask(listener, VideoController.COMPRESS_QUALITY_HIGH);
        task.execute(srcPath, destPath);
        return task;
    }

    public static VideoCompressTask compressVideoMedium(String srcPath, String destPath, CompressListener listener) {
        VideoCompressTask task = new VideoCompressTask(listener, VideoController.COMPRESS_QUALITY_MEDIUM);
        task.execute(srcPath, destPath);
        return task;
    }

    public static VideoCompressTask compressVideoLow(String srcPath, String destPath, CompressListener listener) {
        VideoCompressTask task = new VideoCompressTask(listener, VideoController.COMPRESS_QUALITY_LOW);
        task.execute(srcPath, destPath);
        return task;
    }

    public static VideoCompressTask compressVideo(int quality, String srcPath, String destPath, CompressListener listener) {
        VideoCompressTask task = new VideoCompressTask(listener,
                quality == 1 ? VideoController.COMPRESS_QUALITY_HIGH :
                        quality == 2 ? VideoController.COMPRESS_QUALITY_MEDIUM : VideoController.COMPRESS_QUALITY_LOW);
        task.execute(srcPath, destPath);
        return task;
    }

    /**
     * @author tangxiaopeng
     * @dec 开启线程, 进行压缩
     * @date 2018/10/18 14:45
     */
    private static class VideoCompressTask extends AsyncTask<String, Float, Boolean> {
        private CompressListener mListener;
        private int mQuality;

        public VideoCompressTask(CompressListener listener, int quality) {
            mListener = listener;
            mQuality = quality;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        protected Boolean doInBackground(String... paths) {
            return VideoController.getInstance().convertVideo(paths[0], paths[1], mQuality, new VideoController.CompressProgressListener() {
                @Override
                public void onProgress(float percent) {
                    publishProgress(percent);
                }
            });
        }

        @Override
        protected void onProgressUpdate(Float... percent) {
            super.onProgressUpdate(percent);
            if (mListener != null) {
                mListener.onProgress(percent[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess();
                } else {
                    //取消任务结束后应将值修改回去
                    VideoController.getInstance().setCancel(false);
                    mListener.onFail();
                }
            }
        }
    }

    public interface CompressListener {
        void onStart();

        void onSuccess();

        void onFail();

        void onProgress(float percent);
    }
}
