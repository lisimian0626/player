package org.wysaid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;

import org.wysaid.common.Common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * author: Hanson
 * date:   2018/5/14
 * describe:
 */
public class KaraokeRecordGLSurfaceView extends CameraGLSurfaceView {
    private AccompanyAssistant mAssistant;
    private MixerQueue mMixerQueue;

    public KaraokeRecordGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void attachAccompanyAssistant(String filename) throws IOException {
        mMixerQueue = new MixerQueue();
        mAssistant = new AccompanyAssistant(filename, mMixerQueue);
    }

    private boolean mShouldRecord = false;

    public synchronized boolean isRecording() {
        return mShouldRecord;
    }

    private final Object mRecordStateLock = new Object();

    private KaraokeRecordGLSurfaceView.AudioRecordRunnable mAudioRecordRunnable;
    private Thread mAudioThread;

    public void setLyricsBitmap(final Bitmap bitmap) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mFrameRecorder.setLyrics(bitmap);
                bitmap.recycle();
            }
        });
    }

    public interface StartRecordingCallback {
        void startRecordingOver(boolean success);
    }

    public void startRecording(final String filename) {
        startRecording(filename, null);
    }

    public void startRecording(final String filename, final CameraRecordGLSurfaceView.StartRecordingCallback recordingCallback) {

        queueEvent(new Runnable() {
            @Override
            public void run() {

                if (mFrameRecorder == null) {
                    if (recordingCallback != null) {
                        recordingCallback.startRecordingOver(false);
                    }
                    return;
                }

                if (!mFrameRecorder.startRecording(30, filename)) {
                    Log.e(LOG_TAG, "start recording failed!");
                    if (recordingCallback != null)
                        recordingCallback.startRecordingOver(false);
                    return;
                }
                Log.i(LOG_TAG, "glSurfaceView recording, file: " + filename);
                synchronized (mRecordStateLock) {
                    mShouldRecord = true;
                    mAudioRecordRunnable = new KaraokeRecordGLSurfaceView.AudioRecordRunnable(recordingCallback);
                    if (mAudioRecordRunnable.audioRecord != null) {
                        mAudioThread = new Thread(mAudioRecordRunnable);
                        mAudioThread.start();
                        mAssistant.start();
                    }
                }
            }
        });
    }

    public interface EndRecordingCallback {
        void endRecordingOK();
    }

    public void endRecording() {
        endRecording(null, true);
    }

    public void endRecording(final CameraRecordGLSurfaceView.EndRecordingCallback callback) {
        endRecording(callback, true);
    }

    // The video may be invalid if "shouldSave" is false;
    public void endRecording(final CameraRecordGLSurfaceView.EndRecordingCallback callback, final boolean shouldSave) {
        Log.i(LOG_TAG, "notify quit...");
        synchronized (mRecordStateLock) {
            mShouldRecord = false;
        }

        if (mFrameRecorder == null) {
            return;
        }
        joinAudioRecording();

        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFrameRecorder != null)
                    mFrameRecorder.endRecording(shouldSave);
                if (callback != null) {
                    callback.endRecordingOK();
                }
            }
        });
    }

    @Override
    public synchronized void release(final ReleaseOKCallback callback) {

        synchronized (mRecordStateLock) {
            mShouldRecord = false;
        }

        joinAudioRecording();
        super.release(callback);
    }

    @Override
    public void stopPreview() {

        synchronized (mRecordStateLock) {
            if (mShouldRecord) {
                Log.e(LOG_TAG, "The camera is recording! cannot stop!");
                return;
            }
        }

        super.stopPreview();
    }

    public void joinAudioRecording() {

        if (mAudioThread != null) {
            try {
                mAudioThread.join();
                mAssistant.stop();
                mMixerQueue.reset();
                mAudioThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class AudioRecordRunnable implements Runnable {

        int bufferSize;
        //        short[] audioData;
        int bufferReadResult;
        public AudioRecord audioRecord;
        public volatile boolean isInitialized;
        ByteBuffer audioBufferRef;
        CameraRecordGLSurfaceView.StartRecordingCallback recordingCallback;

        private AudioRecordRunnable(CameraRecordGLSurfaceView.StartRecordingCallback callback) {
            recordingCallback = callback;
            try {
                bufferSize = 4096; //必须为这个，暂时不支持自动;aac 1024 samples
                Log.i(LOG_TAG, "audio min buffer size: " + bufferSize);
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Common.AUDIO_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//                audioData = new short[bufferSize];
                audioBufferRef = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
            } catch (Exception e) {
                if (audioRecord != null) {
                    audioRecord.release();
                    audioRecord = null;
                }
            }

            if (audioRecord == null && recordingCallback != null) {
                recordingCallback.startRecordingOver(false);
                recordingCallback = null;
            }
        }

        public int getAudioSessionId() {
            if (audioRecord == null) {
                return -1;
            }

            return audioRecord.getAudioSessionId();
        }

        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            this.isInitialized = false;

            if (this.audioRecord == null) {
                recordingCallback.startRecordingOver(false);
                recordingCallback = null;
                return;
            }

            //判断音频录制是否被初始化
            while (this.audioRecord.getState() == 0) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException localInterruptedException) {
                    localInterruptedException.printStackTrace();
                }
            }
            this.isInitialized = true;

            try {
                this.audioRecord.startRecording();
            } catch (Exception e) {
                if (recordingCallback != null) {
                    recordingCallback.startRecordingOver(false);
                    recordingCallback = null;
                }
                return;
            }

            if (this.audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                if (recordingCallback != null) {
                    recordingCallback.startRecordingOver(false);
                    recordingCallback = null;
                }
                return;
            }

            if (recordingCallback != null) {
                recordingCallback.startRecordingOver(true);
                recordingCallback = null;
            }


            while (true) {
                synchronized (mRecordStateLock) {
                    if (!mShouldRecord) //&& mFrameRecorder.getVideoStreamtime() <= mFrameRecorder.getAudioStreamtime()
                        break;
                }

                audioBufferRef.rewind();
                bufferReadResult = this.audioRecord.read(audioBufferRef, bufferSize);
                audioBufferRef.position(0);
                mMixerQueue.putMicData(audioBufferRef, bufferReadResult);
                if (mShouldRecord && bufferReadResult > 0 && mFrameRecorder != null &&
                        mFrameRecorder.getTimestamp() > mFrameRecorder.getAudioStreamtime()) {
                    mFrameRecorder.recordAudioFrame(mMixerQueue.getMixedAudio().asShortBuffer(), bufferReadResult / 2);
                }
            }
            this.audioRecord.stop();
            this.audioRecord.release();
        }
    }
}
