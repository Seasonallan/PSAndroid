package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;


public class LRLRView extends View {

    public LRLRView(Context context) {
        super(context);
        init();
    }
    public LRLRView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LRLRView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                        if (status == 1){
                            status = 3;
                        }else{
                            status = 1;
                        }
                        current = nowD;
                    }
                    float percent = accelerateDecelerateInterpolator.getInterpolation(duration * 1.0f/time);
                    if (status == 1){
                        dx = -width * percent;
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
    float dx = 0;
    int status = 3;
    long current = -1;
    int time = 1300;
    private RectF rect;
    private Paint paint;
    AccelerateDecelerateInterpolator accelerateDecelerateInterpolator;
    private void init(){
        paint = new Paint();
        paint.setColor(0xff13b0a5);
        accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    }

    private int height, width;
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
        canvas.translate(dx, 0);
        canvas.drawRect(rect, paint);
    }
}
