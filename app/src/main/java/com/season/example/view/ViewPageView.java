package com.season.example.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.season.lib.anim.PageAnimController;
import com.season.book.page.ViewBitmapPicture;
import com.season.lib.util.NavigationBarUtil;
import com.season.lib.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 可动态添加的翻页类ViewPager
 * 阅读器的简易使用，不涉及高级排版和矩阵点击
 */
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

    /**
     * 请求页缓存
     */
    private ViewBitmapPicture mPagePicture;
    /**
     * 当前绑定页缓存
     */
    private ViewBitmapPicture mBindPagePicture;
    private List<View> viewList;
    private PageAnimController mPageAnimController;

    private Scroller mScroller;
    private int mNavigationBarHeight;

    private void init() {
        mScroller = new Scroller(getContext(), new LinearInterpolator());
        mTouchDownPoint = new PointF();
        mPageAnimController = PageAnimController.create(getContext(), new LinearInterpolator(),
                PageAnimController.ANIM_TYPE_PAGE_TURNING);

        mPageAnimController.setDurationKeep(true);
        mPageAnimController.setTouchStickMode(false);
        //mPageAnimController.setDuration(1680);
        viewList = new ArrayList<>();
        mPagePicture = new ViewBitmapPicture(-1);
        mBindPagePicture = new ViewBitmapPicture(-1);

        mNavigationBarHeight = NavigationBarUtil.getNavigationBarHeight(getContext());
        scrollShowDelay(1000);
    }


    private MotionEvent createEvent(int action, int x, int y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int metaState = 0;
        return MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
    }

    /**
     * 展现动画
     *
     * @param delay
     */
    private void scrollShowDelay(int delay) {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTouchStart) {
                    return;
                }
                mPageAnimController.dispatchTouchEvent(createEvent(MotionEvent.ACTION_DOWN,
                        getWidth(), getHeight()), ViewPageView.this);
                mScroller.startScroll(getWidth(), getHeight(), -240, -240, 800);
                invalidate();
            }
        }, delay);
    }

    boolean isTouchStart = false;
    boolean isScrolling = false;

    @Override
    public void computeScroll() {
        if (isTouchStart) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            isScrolling = true;
            mPageAnimController.dispatchTouchEvent(createEvent(MotionEvent.ACTION_MOVE,
                    mScroller.getCurrX(), mScroller.getCurrY()), this);
        } else {
            if (isScrolling) {
                isScrolling = false;
                mPageAnimController.dispatchTouchEvent(createEvent(MotionEvent.ACTION_MOVE,
                        mScroller.getFinalX() + 1, mScroller.getFinalY() + 1), this);
                mPageAnimController.dispatchTouchEvent(createEvent(MotionEvent.ACTION_UP,
                        mScroller.getCurrX(), mScroller.getCurrY()), this);
                scrollShowDelay(3000);
            }
        }
    }

    /**
     * 跳转到下一页
     */
    public void gotoNextPage() {
        if (currentPage >= viewList.size() - 1) {
            ToastUtil.showToast("最后一页");
            return;
        }
        if (!mPageAnimController.isAnimStop())
            mPageAnimController.stopAnim(this);
        mPageAnimController.startAnim(currentPage, requestNextPage(), true, ViewPageView.this);
    }


    /**
     * 跳转到某一页
     */
    public void gotoPage(int page) {
        if (page == currentPage) {
            return;
        }
        if (!mPageAnimController.isAnimStop())
            mPageAnimController.stopAnim(this);
        requestPage = page;
        if (page > currentPage) {
            mPageAnimController.startAnim(currentPage, requestPage, true, ViewPageView.this);
        } else {
            mPageAnimController.startAnim(currentPage, requestPage, false, ViewPageView.this);
        }
    }


    private PointF mTouchDownPoint;
    private boolean isPressInvalid;
    private MotionEvent motionEvent;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        this.motionEvent = ev;
        isTouchStart = true;
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mPageAnimController.setTouchStickMode(true);
        mPageAnimController.setDurationKeep(false);
        int key = ev.getAction();
        switch (key) {
            case MotionEvent.ACTION_DOWN:
                isPressInvalid = false;
                mTouchDownPoint.set(ev.getX(), ev.getY());
                mPageAnimController.dispatchTouchEvent(ev, this);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) (mTouchDownPoint.x - ev.getX());
                int moveY = (int) (mTouchDownPoint.y - ev.getY());
                float move = PointF.length(moveX, moveY);

                if (move > ViewConfiguration.getTouchSlop()) {
                    isPressInvalid = true;
                }
                if (isPressInvalid) {
                    mPageAnimController.dispatchTouchEvent(ev, this);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isPressInvalid) {
                    performClick();
                } else {
                    mPageAnimController.dispatchTouchEvent(ev, this);
                }
                break;
        }
        return true;
    }

    private int currentPage = 0;

    /**
     * 获取当前页面页码
     *
     * @return
     */
    public int getCurrentPage() {
        return currentPage;
    }

    public View getCurrentView() {
        currentPage = currentPage < 0 ? 0 : currentPage;
        currentPage = (currentPage > viewList.size() - 1) ? viewList.size() - 1 : currentPage;
        return viewList.get(currentPage);
    }

    /**
     * 添加一个页面
     *
     * @param view
     */
    public void addPageView(View view) {
        viewList.add(view);
        if (getWidth() > 0) {
            view.measure(widthMeasureSpec, heightMeasureSpec);
            view.layout(getLeft(), getTop(), getRight(), getBottom());
        } else {
            requestLayout();
        }
    }

    public void release() {
        mBindPagePicture.release();
        mPagePicture.release();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (View view : viewList) {
            view.layout(left, top, right, bottom);
        }
    }

    int widthMeasureSpec;
    int heightMeasureSpec;
    RectF nextPageRect;
    int SIZE = 100;

    /**
     * 点击位置是否在右下角
     *
     * @return
     */
    public boolean isBottomRightClick() {
        nextPageRect = new RectF(getWidth() - SIZE, getHeight() - SIZE - mNavigationBarHeight, getWidth(), getHeight());
        if (motionEvent != null) {
            return nextPageRect.contains(motionEvent.getX(), motionEvent.getY());
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (View view : viewList) {
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPageAnimController == null || !mPageAnimController.dispatchDrawPage(canvas, this)) {
            drawPage(canvas, currentPage);
        }
    }

    public Bitmap getCurrentPageBitmap() {
        return mBindPagePicture.getBitmap();
    }

    @Override
    public void drawPage(Canvas canvas, int index) {
        if (index == currentPage) {
            if (!mBindPagePicture.equals(index)) {
                mBindPagePicture.init(index);
                viewList.get(index).draw(mBindPagePicture.getCanvas(getWidth(), getHeight()));
            }
            mBindPagePicture.onDraw(canvas);
        } else {
            if (!mPagePicture.equals(index)) {
                mPagePicture.init(index);
                viewList.get(index).draw(mPagePicture.getCanvas(getWidth(), getHeight()));
            }
            mPagePicture.onDraw(canvas);
        }
    }

    int requestPage;

    @Override
    public Integer requestPrePage() {
        if (currentPage == 0) {
            ToastUtil.showToast("第一页");
            return null;
        } else {
            requestPage = currentPage - 1;
            return requestPage;
        }
    }

    @Override
    public Integer requestNextPage() {
        if (currentPage == viewList.size() - 1) {
            ToastUtil.showToast("最后一页");
            return null;
        } else {
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

    @Override
    public int getPageBackgroundColor() {
        return -1;
    }

    @Override
    public void onStartAnim(boolean isCancel) {

    }

    @Override
    public void onStopAnim(boolean isCancel) {
        if (!isCancel) {
            currentPage = requestPage;
        }
        requestPage = -1;
    }

    public int getCurrentPageColor() {
        return -1;
    }
}
