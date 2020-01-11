package com.season.lib.gif.frame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.season.lib.gif.utils.Util;
import com.season.lib.gif.movie.FrameDecoder;
import com.season.lib.view.IScaleView;
import com.season.myapplication.BuildConfig;
import com.season.lib.util.Logger;

import java.util.List;


/**
 * Disc: 使用解码器GifDecoder解析出每一帧，然后逐帧显示刷新
 *
 * @see GifDecoder
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 18:37
 */
public class GifFrameView extends View implements IScaleView {

    private final String TAG = "GifFrameView";
    private GifDecoder gifDecoder = null;
    private Paint mPaint;
    public String url;
    private boolean isGifEditMode;
    private float mScale = 1;
    private int recordTime = -1;
    boolean isRecordRelyView = false;
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;
    private float mLeft;
    private float mTop;

    public GifFrameView(Context context) {
        super(context);
        init();
    }

    public GifFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GifFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setMovieResource(int resId) {
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(getContext().getResources(), resId);
        gifDecoder.start();
    }

    public void setFrameList(List<GifFrame> frameList) {
        gifDecoder.setFrameList(frameList);
    }

    public Bitmap firstFrame;
    public String file;

    public void setMovieResource(String strFileName) {
        if (gifDecoder != null) {
            destroy();
        }
        this.file = strFileName;
        firstFrame = new FrameDecoder(file).getFrame();
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(strFileName);
        gifDecoder.start();
    }

    public void destroy() {
        stopDecodeThread();
        if (gifDecoder != null) {
            gifDecoder.destroy();
            gifDecoder = null;
        }
        if (firstFrame != null) Util.recycleBitmaps(firstFrame);
    }

    @Override
    public void onRelease() {
        autoPlay = false;
        destroy();
    }

    /**
     */
    private void stopDecodeThread() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.interrupt();
            gifDecoder.destroy();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (firstFrame != null) {
            int movieWidth = firstFrame.getWidth();
            int movieHeight = firstFrame.getHeight();
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
//			if (!isFullScreen){
//				maximumWidth = movieWidth;
//			}
            float scaleW = (float) movieWidth / (float) maximumWidth;
            mScale = 1f / scaleW;
            mMeasuredMovieWidth = maximumWidth;
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
            if (BuildConfig.DEBUG) {
                Logger.d(TAG + "movieWidth:" + movieWidth + ",movieHeight:" + movieHeight + ",maximumWidth:" + maximumWidth);
                Logger.d(TAG + "scaleW:" + scaleW + ",mScale:" + mScale);
                Logger.d(TAG + "mMeasuredMovieWidth:" + mMeasuredMovieWidth + ",mMeasuredMovieHeight:" + mMeasuredMovieHeight);
            }
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    private long mMovieStart;
    private int mCurrentAnimationTime = 0;

    public boolean autoPlay = false;

    @Override
    public void onDraw(Canvas canvas) {
        drawCanvas(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        if (BuildConfig.DEBUG){
            Logger.d(TAG+"getWidth():"+getWidth()+",mLeft:"+ mLeft +",mTop:"+ mTop);
        }
    }

    @Override
    public void drawCanvas(Canvas canvas) {
        updateAnimationTime();
        if (recordTime >= 0) {
            mCurrentAnimationTime = recordTime;
        }
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (gifDecoder==null){
            return;
        }
        GifFrame gifFrame = gifDecoder.getFrame(mCurrentAnimationTime);
        if (gifFrame != null) {
            if (gifFrame.image != null && gifFrame.image.isRecycled() == false) {
                if (isGifEditMode){
                    canvas.save();
                    canvas.scale(mScale, mScale);
                }
                canvas.drawBitmap(gifFrame.image, mLeft / mScale, mTop / mScale, mPaint);
                if (isGifEditMode)
                canvas.restore();
            }
        }
        if (autoPlay) {
            invalidate();
        }
        isSeeking = false;
    }

    /**
     */
    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int dur = gifDecoder.getDuration();
        if (dur == 0) {
            dur = 3000;
        }
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    @Override
    public int getViewWidth() {
        try {
//			GifFrame gifFrame = gifDecoder.getFrame(0);
//			return gifFrame.image.getWidth();
            return firstFrame.getWidth();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getViewHeight() {
        try {
//			GifFrame gifFrame = gifDecoder.getFrame(0);
//			return gifFrame.image.getHeight();
            return firstFrame.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        return gifDecoder.getDuration();
    }

    @Override
    public int getDelay() {
        return gifDecoder.getDelay();
    }

    @Override
    public void startRecord() {
        isRecordRelyView = true;
    }

    @Override
    public boolean isSeeking() {
        return isSeeking;
    }

    boolean isSeeking = false;

    @Override
    public void recordFrame(int time) {
        if (getDuration() <= 0) {
            recordTime = 0;
            return;
        }
        if (time > getDuration()) {
            if (isRecordRelyView) {
                recordTime = getDuration();
            } else {
                recordTime = time % getDuration();
            }
        } else {
            recordTime = time;
        }
        isSeeking = true;
    }

    ;

    @Override
    public void stopRecord() {
        isRecordRelyView = false;
        recordTime = -1;
    }

    public GifFrameView copy() {
        GifFrameView gifView = new GifFrameView(getContext());
        if (!TextUtils.isEmpty(file)) {
            gifView.setMovieResource(file);
        } else {
        }
        gifView.url = url;
        gifView.file = file;
        return gifView;
    }

    public void setisGifEditMode(boolean isGifEditMode) {
        this.isGifEditMode = isGifEditMode;
    }
}
