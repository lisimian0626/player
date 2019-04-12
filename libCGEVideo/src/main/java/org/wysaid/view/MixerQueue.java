package org.wysaid.view;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author: Hanson
 * date:   2018/5/23
 * describe:
 */
public class MixerQueue {
    private ConcurrentLinkedQueue<BlockData> mAccompanyQueue;
    private ConcurrentLinkedQueue<BlockData> mMicQueue;
    private BlockData mLastAccompanyBlock;
    private BlockData mLastMicBlock;
    private byte[][] mCurrentBuffer;

    //mic数组下标
    private static final int INDEX_MIC = 0;
    //伴奏 数组下标
    private static final int INDEX_ACC = 1;
    //2条音轨合成（目前固定两条合成）
    private static final int AUDIO_SOURCE = 2;

    private static final int MAX_PCM_QUEUE_SIZE = 4096;
    private static final int BLOCK_SIZE = 4096;

    public MixerQueue() {
        mAccompanyQueue = new ConcurrentLinkedQueue<>();
        mMicQueue = new ConcurrentLinkedQueue<>();

        mLastAccompanyBlock = new BlockData();
        mLastMicBlock = new BlockData();

        mCurrentBuffer = new byte[2][BLOCK_SIZE];
    }

    public void putMicData(ByteBuffer buffer, int size) {
        if (mLastMicBlock.remain() >= size) {
            mLastMicBlock.putBuffer(buffer, size);
            if (mLastMicBlock.remain() == 0) {
                mMicQueue.add(mLastMicBlock);
                mLastMicBlock = new BlockData();
            }
        } else {
            int remaining = size;
            while (remaining - mLastMicBlock.remain() > 0) {
                remaining -= mLastMicBlock.remain();
                mLastMicBlock.putBuffer(buffer, mLastMicBlock.remain());
                mMicQueue.add(mLastMicBlock);
                mLastMicBlock = new BlockData();
            }
            mLastMicBlock.putBuffer(buffer, remaining);
        }
    }

    public void putAccompanyData(ByteBuffer buffer, int size) {
        if (mLastAccompanyBlock.remain() >= size) {
            mLastAccompanyBlock.putBuffer(buffer, size);

            if (mLastAccompanyBlock.remain() == 0) {
                mAccompanyQueue.add(mLastAccompanyBlock);
                mLastAccompanyBlock = new BlockData();
            }
        } else {
            int remaining = size;
            while (remaining - mLastAccompanyBlock.remain() > 0) {
                remaining -= mLastAccompanyBlock.remain();
                mLastAccompanyBlock.putBuffer(buffer, mLastAccompanyBlock.remain());
                mAccompanyQueue.add(mLastAccompanyBlock);
                mLastAccompanyBlock = new BlockData();
            }
            mLastAccompanyBlock.putBuffer(buffer, remaining);
        }
    }

    public ByteBuffer getMixedAudio() {
        return mix();
    }

    private void memset(byte[][] buffer, byte val) {
        for (int i = 0; i < buffer.length; i++) {
            Arrays.fill(buffer[i], val);
        }
    }

    private ByteBuffer mix() {
        if (mAccompanyQueue.isEmpty() && mMicQueue.isEmpty()) {
            return null;
        }

        memset(mCurrentBuffer, (byte) 0);
        if (!mAccompanyQueue.isEmpty()) {
            BlockData data = mAccompanyQueue.poll();
            System.arraycopy(data.mBytes, 0, mCurrentBuffer[INDEX_ACC], 0, BLOCK_SIZE);
        }
        if (!mMicQueue.isEmpty()) {
            BlockData data = mMicQueue.poll();
            System.arraycopy(data.mBytes, 0, mCurrentBuffer[INDEX_MIC], 0, BLOCK_SIZE);
        }
        byte[] mixData = mixRawAudioBytes(mCurrentBuffer, BLOCK_SIZE);

        ByteBuffer bytes = ByteBuffer.allocateDirect(BLOCK_SIZE);
        bytes.put(mixData);
        bytes.rewind();

        return bytes;
    }

    private byte[] mixRawAudioBytes(byte[][] bMulRoadAudios, int len) {

        if (bMulRoadAudios == null || bMulRoadAudios.length == 0)
            return null;
        byte[] realMixAudio = bMulRoadAudios[0];
        if (realMixAudio == null) {
            return null;
        }
        final int row = bMulRoadAudios.length;

        //单路音轨
        if (bMulRoadAudios.length == 1)
            return realMixAudio;
        //不同轨道长度要一致，不够要补齐
        for (int rw = 0; rw < bMulRoadAudios.length; ++rw) {
            if (bMulRoadAudios[rw] == null || bMulRoadAudios[rw].length != realMixAudio.length) {
                return null;
            }
        }

        /**
         * 精度为 16位
         */
        int col = len / 2;
        short[][] sMulRoadAudios = new short[row][col];
        for (int r = 0; r < row; ++r) {
            for (int c = 0; c < col; ++c) {
                sMulRoadAudios[r][c] = (short) ((bMulRoadAudios[r][c * 2] & 0xff) | (bMulRoadAudios[r][c * 2 + 1] & 0xff) << 8);
            }
        }

        short[] sMixAudio = new short[col];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < col; ++sc) {
            mixVal = 0;
            sr = 0;
            for (; sr < row; ++sr) {
                mixVal += (sr == INDEX_ACC ? sMulRoadAudios[sr][sc] * 0.8 : sMulRoadAudios[sr][sc]);
            }
            sMixAudio[sc] = (short) (mixVal / row);
        }

        for (sr = 0; sr < col; ++sr) {
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }
        return realMixAudio;
    }

    public boolean isAccompanyPCMOverFlow() {
        return mAccompanyQueue.size() > MAX_PCM_QUEUE_SIZE;
    }

    public void reset() {
        if (mAccompanyQueue != null) {
            mAccompanyQueue.clear();
        }
        if (mMicQueue != null) {
            mMicQueue.clear();
        }
        mLastAccompanyBlock = new BlockData();
        mLastMicBlock = new BlockData();
        mCurrentBuffer = new byte[2][BLOCK_SIZE];
    }

    class BlockData {
        private byte[] mBytes;
        private int mOffset;

        public BlockData() {
            mBytes = new byte[BLOCK_SIZE];
            mOffset = 0;
        }

        public int offset() {
            return mOffset;
        }

        public void setOffset(int offset) {
            mOffset = offset;
        }

        public int remain() {
            return BLOCK_SIZE - mOffset;
        }

        public void putBuffer(ByteBuffer buffer, int size) {
            buffer.get(mBytes, mOffset, size);
            mOffset += size;
        }
    }
}
