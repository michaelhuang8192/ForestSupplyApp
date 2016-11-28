package com.tinyappsdev.forestsupply.Scanner;


import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;


public class FrameProcessor extends Thread {
    private boolean mDone;
    private Queue<FrameData> mFrameQueue = new ArrayDeque();
    private Detector mDetector;
    private OnResultListener mOnResultListener;

    public FrameProcessor(Detector detector) {
        mDetector = detector;
    }

    public interface OnResultListener {
        void onResult(FrameData frameData, SparseArray result);
    }

    public static class FrameData {
        public byte[] data;
        public int width;
        public int height;
        public int format;
        public Rect rect;

        public FrameData(byte[] data, int width, int height, int format, Rect rect) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.format = format;
            this.rect = rect;
        }
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        mOnResultListener = onResultListener;
    }

    public void addFrame(FrameData frameData) {
        synchronized (mFrameQueue) {
            mFrameQueue.add(frameData);
            mFrameQueue.notify();
        }
    }

    public void release() {
        mDone = true;
        synchronized (mFrameQueue) {
            mFrameQueue.clear();
        }
        interrupt();
    }

    @Override
    public synchronized void start() {
        mDone = false;
        super.start();
    }

    @Override
    public void run() {
        try {
            while (!mDone) {
                FrameData frameData = null;
                try {
                    synchronized (mFrameQueue) {
                        frameData = mFrameQueue.poll();
                        if (frameData == null)
                            mFrameQueue.wait();
                    }
                    if (frameData != null && mOnResultListener != null)
                        mOnResultListener.onResult(frameData, process(frameData));

                } catch (InterruptedException e) {
                }
            }
        } finally {
            if(mDetector != null) {
                mDetector.release();
                mDetector = null;
            }
        }
    }

    public SparseArray process(FrameData frameData) {
        Frame frame = new Frame.Builder().setImageData(
                ByteBuffer.wrap(frameData.data), frameData.width, frameData.height, frameData.format
        ).build();
        SparseArray result = mDetector.detect(frame);

        return result;
    }

}
