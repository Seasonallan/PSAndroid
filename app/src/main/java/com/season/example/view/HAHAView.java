package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;

import com.season.lib.util.LogUtil;


public class HAHAView extends View {

    public HAHAView(Context context) {
        super(context);
        init();
    }
    public HAHAView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HAHAView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private boolean mStop = true;
    public void stop() {
        pause = true;
    }

    public void destroy() {
        mStop = true;
        synchronized (mSync) {
            mSync.notify();
        }
        mRefreshThread = null;
    }

    private boolean pause = false;
    public void start() {
        current = System.currentTimeMillis();
        mStop = false;
        pause = false;
        if (mRefreshThread == null){
            mRefreshThread = new RefreshThread();
            mRefreshThread.start();
        }else{
            synchronized (mSync) {
                mSync.notify();
            }
        }
    }

    private RefreshThread mRefreshThread;
    private int[] mSync = new int[0];
    private class RefreshThread extends Thread {
        @Override
        public void run() {
            while (!mStop) {
                if (rect != null){
                    long nowD = System.currentTimeMillis();
                    long duration = nowD - current;
                    if (duration >= time){
                        duration = 0;
                        if (status == 2){
                            status = 1;
                        }else if (status == 1){
                            if (!roll){
                                status = 2;
                            }else{
                                status = 3;
                            }
                            roll = !roll;
                        }else{
                            status = 1;
                        }
                        current = nowD;
                    }
                    float percent = accelerateDecelerateInterpolator.getInterpolation(duration * 1.0f/time);
                    if (status == 1){
                        if (roll){
                            percent = 1- percent;
                        }
                        rect.left = width
                                * 0.32f
                                * percent;
                        rect.right = width - rect.left;
                        rect.top = height
                                * 0.32f
                                * percent;
                        rect.bottom = height - rect.top;
                    }else if (status == 2){
                        degree = 360 * percent;
                    }
                }
                postInvalidate();
                if(!pause) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                synchronized (mSync) {
                    try {
                        mSync.wait();
                    }catch(InterruptedException ignore){}
                }
            }
        }
    }
    int status = 3;
    boolean roll = false;
    long current = -1;
    int time = 1300;
    private RectF rect;
    private Paint paint;
    private TextPaint textPaint;
    private String desc = "PS图层";
    private float textWidth, baseLineY;
    AccelerateDecelerateInterpolator accelerateDecelerateInterpolator;
    private void init(){
        paint = new Paint();
        paint.setColor(0xfff1b136);
        textPaint = new TextPaint();
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(88);
        textWidth = textPaint.measureText(desc);
        baseLineY = Math.abs(textPaint.ascent() + textPaint.descent()) / 2;
        accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    }

    private int height, width;
    private float degree = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        if (height <= 0){
            width = getWidth();
            height = getHeight();
        }
        if (height <= 0){
            return;
        }
        if (rect == null){
            rect = new RectF(0,0,width, height);
        }
        canvas.rotate(degree, width/2, height/2);
        canvas.drawRect(rect, paint);
        canvas.drawText(desc, width/2 - textWidth/2, height/2 + baseLineY ,textPaint);
    }
}
