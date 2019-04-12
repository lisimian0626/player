package org.wysaid.view;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * author: Hanson
 * date:   2018/4/12
 * describe:
 */
public class AccompanyAssistant {
    private String mPath;
    private MediaCodec mDecoder;
    private MediaExtractor mExtractor;
    private MediaFormat mAudioFormat;
    private ScheduledThreadPoolExecutor mSchedures;
    private int mAudioIndex;
    private MixerQueue mQueue;
    private boolean mForceStop = false;

    private DecoderAccompanyRunnable mDecoderTask;

    //2 MS
    private static final long TIMEOUT = 2 * 1000;
    private static final String TAG = AccompanyAssistant.class.getSimpleName();

    public AccompanyAssistant(String path, MixerQueue queue) throws IOException {
        this(new File(path), queue);
    }

    public AccompanyAssistant(File file, MixerQueue queue) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file + "not exist");
        }
        mPath = file.getPath();
        mQueue = queue;
    }

    private void init() {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mPath);
            mAudioFormat = extractMediaFormat();
            mDecoder = MediaCodec.createDecoderByType(mAudioFormat.getString(MediaFormat.KEY_MIME));
            mDecoder.configure(mAudioFormat, null, null, 0);
            mExtractor.selectTrack(mAudioIndex);

            mDecoderTask = new DecoderAccompanyRunnable();
            mSchedures = new ScheduledThreadPoolExecutor(1,
                    Executors.defaultThreadFactory());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private MediaFormat extractMediaFormat() {
        MediaFormat format = null;
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat audio = mExtractor.getTrackFormat(i);
            String mime = audio.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio")) {
                format = audio;
                mAudioIndex = i;
                break;
            }
        }

        return format;
    }

    public void start() {
        init();
        mForceStop = false;
        mSchedures.submit(mDecoderTask);
    }

    public void stop() {
        mDecoder.stop();
        mForceStop = true;
        while (mSchedures.isShutdown()) ;
//        release();
    }

    public void release() {
        mDecoder.release();
        mExtractor.release();
    }

    private void extractPcmDataToMixer() {
        boolean eos = false;
        while (!eos && !mForceStop) {
            if (mQueue.isAccompanyPCMOverFlow()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            int inputIndex = mDecoder.dequeueInputBuffer(TIMEOUT);
            ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();

            //将需要解码的数据喂入解码器
            if (inputIndex >= 0) {
                int sampleSize = mExtractor.readSampleData(inputBuffers[inputIndex], 0);
                if (sampleSize > 0) {
                    mDecoder.queueInputBuffer(inputIndex, 0, sampleSize, mExtractor.getSampleTime(), mExtractor.getSampleFlags());
                    mExtractor.advance();
                } else {
                    eos = true;
                    mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
            }

            //获取解码后的pcm
            ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean endOfDecode = false;
            do {
                int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT);
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
//                        Log.v(TAG, "format changed");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        endOfDecode = true;
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        outputBuffers = mDecoder.getOutputBuffers();
//                        Log.v(TAG, "output buffers changed");
                        break;
                    default:
                        outputBuffers[outputBufferIndex].position(bufferInfo.offset);
                        outputBuffers[outputBufferIndex].limit(bufferInfo.size);
                        mQueue.putAccompanyData(outputBuffers[outputBufferIndex], outputBuffers[outputBufferIndex].remaining());
                        mDecoder.releaseOutputBuffer(outputBufferIndex, false);
                        break;
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.v(TAG, "buffer stream end");
                    break;
                }
            } while (!endOfDecode);
        }
    }

    class DecoderAccompanyRunnable implements Runnable {

        @Override
        public void run() {
            mDecoder.start();
            extractPcmDataToMixer();
            stop();
        }
    }
}
