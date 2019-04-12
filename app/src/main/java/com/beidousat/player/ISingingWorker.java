package com.beidousat.player;

/**
 * author: Hanson
 * date:   2018/1/29
 * describe:
 */

public interface ISingingWorker {
    void init();

    void startRecording() throws Exception;

    void stopRecording(OnRecordEndListener listener) throws Exception;

    void onProgress(int msec);

    void rerecord() throws Exception;

    int getAudioSessionId();
}
