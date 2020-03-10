package com.season.lib.gif;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.gif.extend.LZWEncoderOrderHolder;
import com.season.lib.gif.extend.ThreadGifEncoder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * CreateAt : 7/12/17
 * Describe :
 *
 * @author chendong
 */
public class GifMaker {

    public static final String TAG = GifMaker.class.getSimpleName();

    private ByteArrayOutputStream mFinalOutputStream;
    private List<LZWEncoderOrderHolder> mEncodeOrders;
    private LZWEncoderOrderHolder mStartEncoder, mEndEncoder;
    public String mOutputPath;
    private Handler mHandler;
    private ExecutorService mExecutor;

    private int mCurrentWorkSize;
    private int mTotalWorkSize;
    public int mDelayTime;

    public OnGifMakerListener mOnGifMakerListener;

    public interface OnGifMakerListener {
        void onMakeGifStart();

        void onMakeProgress(int index, int count);

        void onMakeGifSucceed(String outPath);

        void onMakeGifFail();
    }

    public boolean isGifMaded = false;
    public boolean isLowerDivice = false;
    public int hightQ = 20;
    public int lowQ = 50;//质量1～255，1最高清

    /**
     * 设置是低内存，生成GIF参数修正
     *
     * @param context
     */
    public void setLowerDivice(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        int heapSize = manager.getMemoryClass();
        isLowerDivice = heapSize < 150;
    }

    public GifMaker(int count, int delayTime, ExecutorService executor) {
        mFinalOutputStream = new ByteArrayOutputStream();
        mEncodeOrders = new ArrayList<>();
        mExecutor = executor;
        mDelayTime = delayTime;
        this.mTotalWorkSize = count;
        this.mCurrentWorkSize = mTotalWorkSize;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                isGifMaded = true;
                if (msg.what == 200 && mOnGifMakerListener != null) {
                    mOnGifMakerListener.onMakeGifSucceed(mOutputPath);
                } else if (msg.what == 404 && mOnGifMakerListener != null) {
                    mOnGifMakerListener.onMakeGifFail();
                    mExecutor.shutdownNow();
                } else if (mOnGifMakerListener != null) {
                    mOnGifMakerListener.onMakeProgress(msg.what, getTotalSize());
                }
                super.handleMessage(msg);
            }
        };
    }

    public GifMaker setOutputPath(String outputPath) {
        this.mOutputPath = outputPath;
        return this;
    }

    public void setGifMakerListener(OnGifMakerListener listener) {
        this.mOnGifMakerListener = listener;
    }

    public void reset() {
        isGifMaded = false;
        mExecutor.shutdownNow();
    }

    private int id = 0;

    public boolean isBitmapFull() {
        if (id * repeatCount >= mTotalWorkSize) {
            return true;
        }
        return false;
    }

    public int getTotalSize() {
        return mTotalWorkSize;
    }

    public int getFrameCountNow() {
        return id;
    }

    public int getDelay() {
        return mDelayTime;
    }


    public void addBitmap(Bitmap bitmap) {
        if (id >= mTotalWorkSize) {
            return;
        }
        if (!mExecutor.isShutdown()) {
            mExecutor.execute(new EncodeGifRunnable(bitmap, id++));
            mHandler.sendEmptyMessage(id);
        }
    }


    int repeatCount = 1;

    public void setRepeatCount(int count) {
        this.repeatCount = count;
        if (mCurrentWorkSize % repeatCount != 0) {
            this.mCurrentWorkSize = mCurrentWorkSize / repeatCount + 1;
        } else {
            this.mCurrentWorkSize = mCurrentWorkSize / repeatCount;
        }
    }

    private class EncodeGifRunnable implements Runnable {

        int mOrder;
        Bitmap mBitmap;

        EncodeGifRunnable(Bitmap bitmap, int order) {
            mBitmap = bitmap;
            mOrder = order;
        }

        @Override
        public void run() {
            try {
                if (repeatCount <= 1) {
                    ByteArrayOutputStream currentStream = new ByteArrayOutputStream();
                    ThreadGifEncoder encoder = new ThreadGifEncoder();
                    encoder.setQuality(isLowerDivice ? lowQ : hightQ);
                    encoder.setDelay(mDelayTime);
                    encoder.start(currentStream, mOrder);
                    encoder.setFirstFrame(mOrder == 0);
                    encoder.setRepeat(0);
                    LZWEncoderOrderHolder holder = encoder.addFrame(mBitmap, mOrder);
                    encoder.finishThread(mOrder == (mTotalWorkSize - 1), holder.getLZWEncoder());
                    holder.setByteArrayOutputStream(currentStream);
                    mEncodeOrders.add(holder);
                } else {
                    if (mOrder == 0) {
                        ByteArrayOutputStream startStream = new ByteArrayOutputStream();
                        ThreadGifEncoder encoder = new ThreadGifEncoder();
                        encoder.setQuality(isLowerDivice ? lowQ : hightQ);
                        encoder.setDelay(mDelayTime);
                        encoder.start(startStream, mOrder);
                        encoder.setFirstFrame(mOrder == 0);
                        encoder.setRepeat(0);
                        mStartEncoder = encoder.addFrame(mBitmap, mOrder);
                        encoder.finishThread(false, mStartEncoder.getLZWEncoder());
                        mStartEncoder.setByteArrayOutputStream(startStream);
                    } else if ((mOrder + 1) * repeatCount >= mTotalWorkSize) {
                        ByteArrayOutputStream endStream = new ByteArrayOutputStream();
                        ThreadGifEncoder encoder = new ThreadGifEncoder();
                        encoder.setQuality(isLowerDivice ? lowQ : hightQ);
                        encoder.setDelay(mDelayTime);
                        encoder.start(endStream, mOrder);
                        encoder.setFirstFrame(mOrder == 0);
                        encoder.setRepeat(0);
                        mEndEncoder = encoder.addFrame(mBitmap, mOrder);
                        encoder.finishThread(true, mEndEncoder.getLZWEncoder());
                        mEndEncoder.setByteArrayOutputStream(endStream);
                    }
                    ByteArrayOutputStream currentStream = new ByteArrayOutputStream();
                    ThreadGifEncoder encoder = new ThreadGifEncoder();
                    encoder.setQuality(isLowerDivice ? lowQ : hightQ);
                    encoder.setDelay(mDelayTime);
                    encoder.start(currentStream, 1);
                    encoder.setFirstFrame(false);
                    encoder.setRepeat(0);

                    LZWEncoderOrderHolder holder = encoder.addFrame(mBitmap, mOrder);
                    encoder.finishThread(false, holder.getLZWEncoder());
                    holder.setByteArrayOutputStream(currentStream);

                    mEncodeOrders.add(holder);
                }
                BitmapUtil.recycleBitmaps(mBitmap);
                workDone();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("gifmaker", "e:" + e.toString());
                mHandler.sendEmptyMessage(404);
            }
        }

    }

    /**
     * 多线程输出
     */
    private synchronized void workDone() throws IOException {
        mCurrentWorkSize--;
        if (mCurrentWorkSize == 0) {
            Collections.sort(mEncodeOrders);
            if (repeatCount <= 1) {
                for (int i = 0; i < mEncodeOrders.size(); i++) {
                    LZWEncoderOrderHolder item = mEncodeOrders.get(i);
                    if (item != null){
                        mFinalOutputStream.write(item.getByteArrayOutputStream().toByteArray());
                        item.release();
                        item = null;
                    }
                }
            } else {
                for (int index = 0; index < repeatCount; index++) {
                    if (index == 0) {
                        mFinalOutputStream.write(mStartEncoder.getByteArrayOutputStream().toByteArray());
                        for (int i = 1; i < mEncodeOrders.size(); i++) {
                            mFinalOutputStream
                                    .write(mEncodeOrders.get(i).getByteArrayOutputStream().toByteArray());
                        }
                    } else if (index == repeatCount - 1) {
                        for (int i = 0; i < mEncodeOrders.size() - 1; i++) {
                            mFinalOutputStream
                                    .write(mEncodeOrders.get(i).getByteArrayOutputStream().toByteArray());
                        }
                        mFinalOutputStream.write(mEndEncoder.getByteArrayOutputStream().toByteArray());
                    } else {
                        for (int i = 0; i < mEncodeOrders.size(); i++) {
                            mFinalOutputStream
                                    .write(mEncodeOrders.get(i).getByteArrayOutputStream().toByteArray());
                        }
                    }
                }
            }

            byte[] data = mFinalOutputStream.toByteArray();
            File file = new File(mOutputPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedOutputStream bosToFile = new BufferedOutputStream(new FileOutputStream(file));
            bosToFile.write(data);
            bosToFile.flush();
            bosToFile.close();
            bosToFile = null;
            mFinalOutputStream.close();
            mFinalOutputStream = null;
            data = null;
            for (int i = 0; i < mEncodeOrders.size(); i++) {
                if (mEncodeOrders.get(i) != null){
                    mEncodeOrders.get(i).release();
                }
            }
            mEncodeOrders.clear();
            if (mStartEncoder != null){
                mStartEncoder.release();
                mStartEncoder = null;
            }
            if (mEndEncoder != null){
                mEndEncoder.release();
                mEndEncoder = null;
            }

            mHandler.sendEmptyMessage(200);
        }
    }

}
