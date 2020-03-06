package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 图层基类
 */
public abstract class CustomBaseView extends View implements ILayer {

    private long mMovieStart;
    private int startTime = Integer.MIN_VALUE;
    private int endTime = Integer.MAX_VALUE;
    private boolean autoPlay = false;
    private int currentTime = 0;

    public CustomBaseView(Context context) {
        super(context);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void recordFrame(int time) {
        currentTime = time;
    }

    public void setAutoPlay(){
        autoPlay = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (autoPlay){
            long now = android.os.SystemClock.uptimeMillis();
            if (mMovieStart == 0) {
                mMovieStart = now;
            }
            int dur = getDuration();
            if (dur == 0) {
                dur = 3000;
            }
            currentTime = (int) ((now - mMovieStart) % dur);
            drawCanvas(canvas);
            invalidate();
        }else{
            drawCanvas(canvas);
        }
    }

    @Override
    public void drawCanvas(Canvas canvas) {
        if (currentTime >= getStartTime() && currentTime <= getEndTime()) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            int time = currentTime - getStartTime();
            if (getDuration() > 0 && time > getDuration()){
                if (isRepeat()){
                    time = time % getDuration();
                }else{
                    time = getDuration();
                }
            }
            drawCanvasTime(canvas, time);
        }
    }

    /**
     * 是否
     * @return
     */
    public boolean isRepeat(){
        return true;
    }

    /**
     * 绘制某个时间点的画布
     * @param canvas
     * @param time
     */
    public abstract void drawCanvasTime(Canvas canvas, int time);

    @Override
    public void onRelease() {
        autoPlay = false;
    }

    @Override
    public int getStartTime() {
        if (startTime == Integer.MIN_VALUE){
            return 0;
        }
        return startTime;
    }

    @Override
    public int getEndTime() {
        if (endTime == Integer.MAX_VALUE){
            return getDuration();
        }
        return endTime;
    }


    @Override
    public boolean setStartTime(int time) {
        startTime = time;
        return true;
    }

    @Override
    public boolean setEndTime(int time) {
        endTime = time;
        return true;
    }

}
