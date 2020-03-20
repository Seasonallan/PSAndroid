package com.season.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.season.book.anim.PageAnimController;
import com.season.lib.RoutePath;
import com.season.lib.bitmap.BitmapUtil;

/**
 * 可动态添加的翻页类ViewPager
 * 阅读器的简易使用，不涉及高级排版和矩阵点击
 *
 */
public class StartPageView extends View implements PageAnimController.PageCarver {

    public StartPageView(Context context) {
        super(context);
        init();
    }
    public StartPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StartPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private PageAnimController mPageAnimController;

    private void init(){
        mPageAnimController = PageAnimController.create(getContext(), new LinearInterpolator(),
                PageAnimController.ANIM_TYPE_PAGE_TURNING);

        mPageAnimController.setDurationKeep(true);
        //mPageAnimController.setDuration(1200);
    }


    public void start(){
        if (!mPageAnimController.isAnimStop())
            mPageAnimController.stopAnim(this);
        mPageAnimController.startAnim(currentPage, 1, true, this);
    }

    boolean isFinish = false;
    public void finish(){
        this.isFinish = true;
        if (!mPageAnimController.isAnimStop())
            mPageAnimController.stopAnim(this);
        mPageAnimController.startAnim(1, currentPage, false, this);
    }

    private int currentPage = 0;

    public void setBiamtp(Bitmap bitmap) {
        currentPage = 0;
        this.bitmap = bitmap;
        invalidate();
    }

    private Bitmap bitmap;

    public void release(){
        BitmapUtil.recycleBitmaps(bitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if(mPageAnimController == null || !mPageAnimController.dispatchDrawPage(canvas, this)){
            if (isFinish)
                drawPage(canvas, currentPage);
        }
    }

    @Override
    public void drawPage(Canvas canvas, int index) {
        if (index == currentPage){
            canvas.drawBitmap(bitmap, 0, 0, null);
        }else{
            //canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
    }

    @Override
    public Integer requestPrePage() {
        return 1;
    }

    @Override
    public Integer requestNextPage() {
        return 1;
    }

    @Override
    public void requestInvalidate() {
        invalidate();
    }

    @Override
    public int getCurrentPageIndex() {
        return currentPage;
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
        return RoutePath.sCacheColor;
    }

    @Override
    public void onStartAnim(boolean isCancel) {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopAnim(boolean isCancel) {
        if (isFinish){
             performClick();
        }else {
            setVisibility(View.GONE);
        }
    }

}
