package com.season.example.broken;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import com.season.lib.support.bitmap.BitmapUtil;
import com.season.lib.util.LogUtil;


/**
 * 碎屏
 *
 */
public class StartBrokenView extends View {

    public StartBrokenView(Context context) {
        super(context);
        init();
    }
    public StartBrokenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StartBrokenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private BrokenAnimator bAnim;
    private BrokenConfig config;
    private void init(){
        config = new BrokenConfig();
        refreshThread.start();
    }

    volatile boolean on = true;
    private Thread refreshThread = new Thread(){
        @Override
        public void run() {
            while (on){
                if (isInterrupted()){
                    return;
                }
                postInvalidate();
                try {
                    sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void start(Point point){
        bAnim = new BrokenAnimator(bitmap, point, config);
        bAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LogUtil.e("onAnimationEnd " + bAnim.getStage() +"  ,"+ isFinish );
                if (isFinish){
                    if(bAnim.getStage() == BrokenAnimator.STAGE_BREAKING) {
                        onBrokenFallingEnd();
                    } else if(bAnim.getStage() == BrokenAnimator.STAGE_FALLING) {
                        bAnim.setStage(BrokenAnimator.STAGE_BREAKING);
                        bAnim.changeReverse(true);
                        bAnim.setInterpolator(new DecelerateInterpolator(2.0f));
                        bAnim.setDuration(config.breakDuration);
                        bAnim.start();
                    }
                }else{

                    if(bAnim.getStage() == BrokenAnimator.STAGE_BREAKING) {
                        bAnim.setInterpolator(new LinearInterpolator());
                        bAnim.setStage(BrokenAnimator.STAGE_FALLING);
                        bAnim.setDuration(config.fallDuration);
                        bAnim.start();
                    } else if(bAnim.getStage() == BrokenAnimator.STAGE_FALLING) {
                        onBrokenFallingEnd();
                    }
                }
            }
        });

        bAnim.setFloatValues(0f,1f);
        bAnim.setInterpolator(new AccelerateInterpolator(2.0f));
        bAnim.setDuration(config.breakDuration);
        bAnim.start();
    }


    boolean isFinish = false;
    public void reverse(){
        if (isFinish){
            return;
        }
        this.enable = true;
        this.isFinish = true;
        bAnim.reverse();
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    private Bitmap bitmap;
    private void drawBitmap(Canvas canvas){
        if (BitmapUtil.isBitmapAvaliable(bitmap))
            canvas.drawBitmap(bitmap, 0, 0, null);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (!enable){
            return;
        }
        if (bAnim == null || !bAnim.isStarted()){
            drawBitmap(canvas);
        }else{
            switch (bAnim.getStage()){
                case BrokenAnimator.STAGE_BREAKING:
                    drawBitmap(canvas);
                    bAnim.drawBreaking(canvas);
                    break;
                case BrokenAnimator.STAGE_FALLING:
                    bAnim.drawFalling(canvas);
                    break;
            }
        }

    }


    boolean enable = true;
    public void onLoaded(){
        enable = false;
    }

    public void onFinish(){
        bAnim.removeAllListeners();
        bAnim.release();
        bAnim = null;
        on = false;
        refreshThread.interrupt();
    }

    public void onBrokenFallingEnd() {
        if (isFinish){
            onFinish();
        }else {
            onLoaded();
        }
    }

    public void release() {
        BitmapUtil.recycleBitmaps(bitmap);
    }
}
