package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.season.lib.anim.PageAnimController;
import com.season.lib.util.LogUtil;
import com.season.lib.view.AbsReadView;

import java.util.logging.Handler;


public class LRLRView extends View implements PageAnimController.PageCarver {

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
    public void destroy() {
        mStop = true;
    }

    public void start(){
        mStop = false;
        startDelay();
    }

    public void startDelay() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStop){
                    return;
                }
                if (getWidth() <= 0){
                    return;
                }
                if (mPageAnimController == null){
                    mPageAnimController = PageAnimController.create(getContext(), new LinearInterpolator(),
                            PageAnimController.ANIM_TYPE_PAGE_TURNING);
                    // mPageAnimController.setDuration(2468);
                }
                if (mPageAnimController.isAnimStop())
                    mPageAnimController.startAnim(current, current==0?1:0, true, LRLRView.this);
            }
        }, 2000);
    }

    private int current = 0;
    private Paint paint;
    private TextPaint textPaint;
    private String desc = "书籍阅读";
    private float textWidth, baseLineY;
    private void init(){
        paint = new Paint();
        paint.setColor(0xff13b0a5);
        textPaint = new TextPaint();
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(88);
        baseLineY = Math.abs(textPaint.ascent() + textPaint.descent()) / 2;
        textWidth = textPaint.measureText(desc);
    }
    private PageAnimController mPageAnimController;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPageAnimController == null || !mPageAnimController.dispatchDrawPage(canvas, this)){
            drawPage(canvas, current);
        }
    }

    private int height, width;
    private RectF rect;
    @Override
    public void drawPage(Canvas canvas, int index) {
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
        paint.setColor(index == 0?0xff13b0a5:0xfffc9d9a);
        canvas.drawRect(rect, paint);
        canvas.drawText(desc, width/2 - textWidth/2, height/2 + baseLineY ,textPaint);
    }

    @Override
    public Integer requestPrePage() {
        return null;
    }

    @Override
    public Integer requestNextPage() {
        return null;
    }

    @Override
    public void requestInvalidate() {
        invalidate();
    }

    @Override
    public int getCurrentPageIndex() {
        return 0;
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }

    @Override
    public int getContentHeight() {
        return getHeight();
    }

    @Override
    public int getScreenWidth() {
        return getWidth();
    }

    @Override
    public int getScreenHeight() {
        return getHeight();
    }

    @Override
    public int getPageBackgroundColor() {
        return 0;
    }

    @Override
    public void onStartAnim(boolean isCancel) {

    }

    @Override
    public void onStopAnim(boolean isCancel) {
        current = current==0?1:0;
        startDelay();
    }
}
