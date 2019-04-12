package com.beidousat.player;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.wysaid.myUtils.ImageUtil;
import org.wysaid.view.CameraRecordGLSurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener,ISingingWorker{
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    CameraRecordGLSurfaceView mCameraView;
    private static final String APP_ROOT_DIR = "/MiniK/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView=findViewById(R.id.play_surf);
//        initPlayer();

        mCameraView.setFitFullView(true);

    }

    private void initPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        CreateMediaPlayer();
        CreateSurfaceView();
    }
    private void CreateMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);

    }
    private void CreateSurfaceView() {
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setVisibility(View.VISIBLE);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
    }
    String generateVideoFilename() {
        return Environment.getExternalStorageDirectory()+APP_ROOT_DIR+System.currentTimeMillis() + ".mp4";
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public void init() {

    }

    @Override
    public void startRecording() throws Exception {
        mCameraView.startRecording(generateVideoFilename(), new CameraRecordGLSurfaceView.StartRecordingCallback() {
            @Override
            public void startRecordingOver(boolean success) {
                Point viewPort = mCameraView.getRecordVideoSize();
            }
        });
    }

    @Override
    public void stopRecording(OnRecordEndListener listener) throws Exception {
        if (mCameraView.isRecording()) {
            mCameraView.endRecording(new CameraRecordGLSurfaceView.EndRecordingCallback() {
                @Override
                public void endRecordingOK() {
                    final ISLocalMedia video = new ISLocalMedia();
                    video.setPath(Constant.File.VIDEO_FILE_NAME);
                    File file = new File(Constant.File.VIDEO_FILE_NAME);
//                    video.setTitle(file.getName());
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(file.getPath());
                    video.setDuration(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    Bitmap bitmap = metadataRetriever.getFrameAtTime(1000);
                    String coverName = Constant.File.getVideoCoverDir() + file.getName().replace(Constant.VIDEO_SUFFIX, Constant.PICTURE_SUFFIX);
                    ImageUtil.saveBitmap(bitmap, coverName);
                    video.setCover(coverName);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onRecordEnded(video);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onProgress(int msec) {

    }

    @Override
    public void rerecord() throws Exception {

    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        /**
         * 创建SurfaceView时开始从上次位置播放或重新播放
         *
         * @param holder
         */
        public void surfaceCreated(SurfaceHolder holder) {
            mediaPlayer.setDisplay(holder);
        }

        /**
         * 离开SurfaceView时停止播放，保存播放位置
         */
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

}
