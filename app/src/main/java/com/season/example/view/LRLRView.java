package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

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

    public void start() {
        if (getWidth() <= 0){
            return;
        }
        LogUtil.e("start");
        if (mPageAnimController == null){
            mPageAnimController = PageAnimController.create(getContext(),
                    PageAnimController.ANIM_TYPE_PAGE_TURNING);
        }
        mStop = false;
        mPageAnimController.startAnim(current, current==0?1:0, true, this);
    }

    private int current = 0;
    private Paint paint;
    private void init(){
        paint = new Paint();
        paint.setColor(0xff13b0a5);

    }
    PageAnimController mPageAnimController;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!mPageAnimController.dispatchDrawPage(canvas, this)){
            drawPage(canvas, current);
        }
    }

    private int height, width;
    private RectF rect;
    @Override
    public void drawPage(Canvas canvas, int index) {
        LogUtil.e("drawPage:"+index);

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
        paint.setColor(index == 0?0xff13b0a5:0xffc9c9c9);
        canvas.drawRect(rect, paint);
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
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStop){
                    return;
                }
                start();
            }
        }, 2000);
    }
}
