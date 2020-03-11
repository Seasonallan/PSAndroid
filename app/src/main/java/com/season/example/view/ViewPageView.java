package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.season.lib.anim.PageAnimController;
import com.season.lib.util.LogUtil;
import com.season.lib.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class ViewPageView extends View implements PageAnimController.PageCarver {

    public ViewPageView(Context context) {
        super(context);
        init();
    }
    public ViewPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /** 请求页缓存*/
    private ViewBitmapPicture mPagePicture;
    /** 当前绑定页缓存*/
    private ViewBitmapPicture mBindPagePicture;
    private List<View> viewList;
    private void init(){
        mTouchDownPoint = new PointF();
        mPageAnimController = PageAnimController.create(getContext(), new LinearInterpolator(),
                PageAnimController.ANIM_TYPE_PAGE_TURNING);
        //mPageAnimController.setDuration(1680);
        viewList = new ArrayList<>();
        mPagePicture = new ViewBitmapPicture(-1);
        mBindPagePicture = new ViewBitmapPicture(-1);
    }

    public void gotoNextPage(){
        if (!mPageAnimController.isAnimStop())
            mPageAnimController.stopAnim(this);
        mPageAnimController.startAnim(currentPage, requestNextPage(), true, ViewPageView.this);
    }



    private PointF mTouchDownPoint;
    private boolean isPressInvalid;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPageAnimController != null){
            mPageAnimController.dispatchTouchEvent(ev, this);
        }
        int key = ev.getAction();
        switch (key) {
            case MotionEvent.ACTION_DOWN:
                isPressInvalid = false;
                mTouchDownPoint.set(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) (mTouchDownPoint.x - ev.getX());
                int moveY = (int) (mTouchDownPoint.y - ev.getY());
                float move = PointF.length(moveX, moveY);

                if(move > ViewConfiguration.getTouchSlop()){
                    isPressInvalid = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isPressInvalid){
                    performClick();
                }
                break;
        }
        return true;
    }

    public int getCurrentPage(){
        return currentPage;
    }

    private int currentPage = 0;
    private PageAnimController mPageAnimController;


    public void addPageView(View view) {
        viewList.add(view);
        if (getWidth() > 0){
            view.measure(0,0);
            view.layout(getLeft(), getTop(),getRight(),getBottom());
        }else{
            requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (View view: viewList){
            view.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (View view: viewList){
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mPageAnimController == null || !mPageAnimController.dispatchDrawPage(canvas, this)){
            drawPage(canvas, currentPage);
        }
    }

    @Override
    public void drawPage(Canvas canvas, int index) {
        if (index == currentPage){
            if (!mBindPagePicture.equals(index)){
                mBindPagePicture.init(index);
                viewList.get(index).draw(mBindPagePicture.getCanvas(getWidth(), getHeight()));
            }
            mBindPagePicture.onDraw(canvas);
        }else{
            if (!mPagePicture.equals(index)){
                mPagePicture.init(index);
                viewList.get(index).draw(mPagePicture.getCanvas(getWidth(), getHeight()));
            }
            mPagePicture.onDraw(canvas);
        }
    }

    int requestPage;
    @Override
    public Integer requestPrePage() {
        if (currentPage == 0){
            ToastUtil.showToast("第一页");
            return null;
        }else{
            requestPage = currentPage - 1;
            return requestPage;
        }
    }

    @Override
    public Integer requestNextPage() {
        if (currentPage == viewList.size() - 1){
            ToastUtil.showToast("最后一页");
            return null;
        }else{
            requestPage = currentPage + 1;
            return requestPage;
        }
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

    //int[] colors = {0xff142537, 0xff13b0a5, 0xfffc9d9a};
    int[] colors = {0, 0xff13b0a5, 0xfffc9d9a};
    @Override
    public int getPageBackgroundColor() {
        if (requestPage > currentPage){
            if (currentPage > colors.length - 1){
                return colors[0];
            }
            return colors[currentPage];
        }else{
            if (requestPage > colors.length - 1){
                return colors[0];
            }
            return colors[requestPage];
        }
    }

    @Override
    public void onStartAnim(boolean isCancel) {

    }

    @Override
    public void onStopAnim(boolean isCancel) {
        if (!isCancel){
            currentPage = requestPage;
        }
        requestPage = -1;
    }

}
