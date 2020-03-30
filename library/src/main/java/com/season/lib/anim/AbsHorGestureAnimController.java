package com.season.lib.anim;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.season.lib.BaseContext;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.util.LogUtil;

/**
 * 水分平滑翻页动画
 * @see PageTurningAnimController 仿真动画
 * @see HorTranslationAnimController 水平平移
 *
 */
public abstract class AbsHorGestureAnimController extends PageAnimController {
	protected static final String TAG = "TouEvent";
	public static final int DURATION_DEFAULT = 600;

	protected int mDuration = DURATION_DEFAULT;
	protected Scroller mScroller, mScrollerStart;
	private boolean isCancelAnim;
	protected Boolean isRequestNextPage;
	private int mLastMoveX;
	protected int mHalfScreenWidth;
	protected int mHalfScreenHeight;
	protected int mHalfContentWidth;
	protected int mHalfContentHeight;
	protected int mScreenWidth;
	protected int mScreenHeight;
	protected int mContentWidth;
	protected int mContentHeight;
	protected PointF mLastTouchPoint;
	protected PointF mDownTouchPoint;
	protected boolean isAnimStart;
	protected boolean isTouchStart;

    private int mTouchSlopSquare;
	public void setDuration(int duration){
		this.mDuration = duration;
	}

    AbsHorGestureAnimController(){
        this(null);
    }

    AbsHorGestureAnimController(Interpolator interpolator){
        super();
		mScroller = new Scroller(BaseContext.getInstance(), interpolator);
		mScrollerStart = new Scroller(BaseContext.getInstance(), interpolator);
        mLastTouchPoint = new PointF();
        mDownTouchPoint = new PointF();
        mContentWidth = -1;
        int touchSlop = ViewConfiguration.getTouchSlop();
        mTouchSlopSquare = touchSlop;
    }

	
	private void checkInit(PageCarver pageCarver){
		if(mContentWidth == -1){
			onMeasure(pageCarver);
		}
	}


	Path contentPath;
	protected void onMeasure(PageCarver pageCarver) {
		mContentWidth = pageCarver.getContentWidth();
		mContentHeight = pageCarver.getContentHeight();
		mHalfContentWidth = mContentWidth >> 1;
		mHalfContentHeight = mContentHeight >> 1;
		mScreenWidth = pageCarver.getScreenWidth();
		mScreenHeight = pageCarver.getScreenHeight();
		mHalfScreenWidth = mScreenWidth >> 1;
		mHalfScreenHeight = mScreenHeight >> 1;
		LogUtil.e(TAG, "onMeasure: CW="+ mContentWidth +" CH="+ mContentHeight+" SW="+ mScreenWidth+" SH="+mScreenHeight);
		contentPath = new Path();
		contentPath.moveTo(0, 0);
		contentPath.lineTo(mContentWidth, 0);
		contentPath.lineTo(mContentWidth, mContentHeight);
		contentPath.lineTo(0, mContentHeight);
		contentPath.close();
	}

	protected boolean onTouchDown = false;
	private MotionEvent mCurrentMotionEvent;
	@Override
	public void dispatchTouchEvent(MotionEvent event, PageCarver pageCarver) {
		if(!mScroller.isFinished()){
			LogUtil.i(TAG,"dispatchTouchEvent isAnimStop");
			stopAnim(pageCarver);
		}
		mCurrentMotionEvent = event;
		checkInit(pageCarver);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownTouchPoint.set(event.getX(), event.getY());
			mLastTouchPoint.set(event.getX(), event.getY());
			mLastMoveX = 0;
			onTouchDown = true;
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) (mDownTouchPoint.x - event.getX());
			if(isRequestNextPage == null){
				Integer requestPageIndex = null;
				if(moveX > 0){
					requestPageIndex = pageCarver.requestNextPage();
					if(requestPageIndex != null){
						isRequestNextPage = true;
						int currentPageIndex = pageCarver.getCurrentPageIndex();
						onRequestPage(isRequestNextPage,currentPageIndex,requestPageIndex,mLastTouchPoint.x,mLastTouchPoint.y);
						isTouchStart = true;
					}else{
						isTouchStart = false;
					}
				}else{
					requestPageIndex = pageCarver.requestPrePage();
					if(requestPageIndex != null){
						isRequestNextPage = false;
						int currentPageIndex = pageCarver.getCurrentPageIndex();
						onRequestPage(isRequestNextPage,currentPageIndex,requestPageIndex,mLastTouchPoint.x,mLastTouchPoint.y);
						isTouchStart = true;
					}else{
						isTouchStart = false;
					}
				}
			}

            final int deltaX = (int) (event.getX() - mLastTouchPoint.x);
            final int deltaY = (int) (event.getY() - mLastTouchPoint.y);
            int distance = (deltaX * deltaX) + (deltaY * deltaY);
            if (!touchStickMode || distance > mTouchSlopSquare) {
				if (durationKeep){
					mLastTouchPoint.set(event.getX(), event.getY());
					pageCarver.requestInvalidate();
				}else{
					onPageStart(event.getX(), event.getY(), pageCarver);
				}
            }
			mLastMoveX = -deltaX;
			break;
		case MotionEvent.ACTION_UP:
			if(!mScrollerStart.isFinished()){
				mScrollerStart.abortAnimation();
				mLastTouchPoint.set(event.getX(), event.getY());
			}
			if(isRequestNextPage != null){
				if(isRequestNextPage && mLastMoveX < 0){
					startCancelAnim(true, pageCarver);
				}else if(!isRequestNextPage && mLastMoveX > 0){
					startCancelAnim(false, pageCarver);
				}else{
					startAnim(isRequestNextPage, pageCarver);
				}
			}
			isTouchStart = false;
			break;
		}
	}

	protected void onPageStart(final float x, final float y, final PageCarver pageCarver){
		mLastTouchPoint.set(x, y);
		pageCarver.requestInvalidate();
	}

	@Override
	public boolean dispatchDrawPage(Canvas canvas, final PageCarver pageCarver) {
		if(isRequestNextPage == null){
			return false;
		}
		checkInit(pageCarver);
		if (!mScrollerStart.isFinished() && mScrollerStart.computeScrollOffset()){
			mLastTouchPoint.set(mScrollerStart.getCurrX() - (mScrollerStart.getFinalX() - mCurrentMotionEvent.getX()),
					mScrollerStart.getCurrY() - (mScrollerStart.getFinalY() - mCurrentMotionEvent.getY()));
			onDrawAnim(canvas,isCancelAnim,isRequestNextPage, pageCarver);
			pageCarver.requestInvalidate();
			return true;
		}


		boolean isUnFinished = !mScroller.isFinished() && mScroller.computeScrollOffset();
		if(isUnFinished){
		    if (isFullAnimation && !isCancelAnim){
		        float percent = (mScroller.getCurrX() -  mScroller.getStartX()) * 1.0f
                        /(mScroller.getFinalX() - mScroller.getStartX());
                float y = Math.abs(mContentHeight/2 * percent);
                if (y >= mContentHeight/4){
                    y = mContentHeight/2 - y;
                }
                mLastTouchPoint.set(mScroller.getCurrX(), mContentHeight - y);
            }else{
                mLastTouchPoint.set(mScroller.getCurrX(), mScroller.getCurrY());
            }
		}
		if(!isUnFinished && !isTouchStart){
			dispatchAnimEnd(pageCarver);
			return false;
		}else{
			if (interruptAnimationEnd()){
				mScroller.abortAnimation();
				dispatchAnimEnd(pageCarver);
				return false;
			}else{
				onDrawAnim(canvas,isCancelAnim,isRequestNextPage, pageCarver);
				if (isAnimStart)
					pageCarver.requestInvalidate();
				return true;
			}
		}
	}

	/**
	 * 提前结束动画
	 * @return
	 */
	public boolean interruptAnimationEnd(){
		if (isCancelAnim){
			return false;
		}
		return isAnimStart && Math.abs(mScroller.getCurrX() - mScroller.getFinalX()) < 100;
	}
	
	private void dispatchAnimStart(PageCarver pageCarver){
		LogUtil.e("dispatchAnimStart");
		isAnimStart = true;
		onAnimStart(isCancelAnim);
		pageCarver.onStartAnim(isCancelAnim);
		pageCarver.requestInvalidate();
	}

	protected Bitmap cacheBitmap;
	private void dispatchAnimEnd(PageCarver pageCarver){
		LogUtil.e("dispatchAnimEnd");
        isFullAnimation = false;
		isAnimStart = false;
		isRequestNextPage = null;
		onAnimEnd(isCancelAnim);
		pageCarver.onStopAnim(isCancelAnim);
		isCancelAnim = false;
		pageCarver.requestInvalidate();
		BitmapUtil.recycleBitmaps(cacheBitmap);
	}

	/**
	 * 滑动器时长 装饰
	 */
	protected void scrollerDecorator(Scroller scroller, int x, int y, int dx, int dy, int duration){
		double length =  Math.sqrt(dx * dx + dy * dy);
		int maxLength = mContentWidth;
		if (length < maxLength && !durationKeep){
			duration = (int) (length * duration /maxLength);
		}
		scroller.startScroll(x, y, dx, dy, duration);
	}
	
	protected void startCancelAnim(boolean isRequestNext, PageCarver pageCarver){
		isCancelAnim = true;
		setScroller(mScroller, isRequestNext, true, pageCarver);
		dispatchAnimStart(pageCarver);
	}
	
	protected void setDefaultTouchPoint(boolean isNext){
		if(isNext){
			mDownTouchPoint.set(mContentWidth,0);
		}else{
			mDownTouchPoint.set(0,0);
		}
		mLastTouchPoint.set(mDownTouchPoint);
	}

	protected boolean isFullAnimation = false;
	@Override
	public void startAnim(int fromIndex,int toIndex,boolean isNext, PageCarver pageCarver) {
        isFullAnimation = true;
		stopAnim(pageCarver);
		checkInit(pageCarver);
		isRequestNextPage = isNext;
		setDefaultTouchPoint(isNext);
		onRequestPage(isRequestNextPage,fromIndex, toIndex,mLastTouchPoint.x,mLastTouchPoint.y);
		startAnim(isRequestNextPage, pageCarver);
	}
	
	protected void startAnim(boolean isRequestNext, PageCarver pageCarver) {
		isCancelAnim = false;
		setScroller(mScroller, isRequestNext, false, pageCarver);
		dispatchAnimStart(pageCarver);
	}

	@Override
	public void stopAnim(PageCarver pageCarver) {
		if(!mScroller.isFinished()){
			mScroller.abortAnimation();
			mLastTouchPoint.set(mScroller.getFinalX(), mScroller.getFinalY());
			dispatchAnimEnd(pageCarver);
		}
	}

	@Override
	public boolean isAnimStop() {
		return isRequestNextPage == null;
	}


	/**
	 * onRequestPage() -> onAnimStart() -> onAnimEnd()
	 */
	protected abstract void onRequestPage(boolean isRequestNext,int fromIndex,int toIndex,float x,float y);
	protected abstract void onAnimStart(boolean isCancelAnim);
	protected abstract void onAnimEnd(boolean isCancelAnim);

	/**
	 * View的onDraw中调用，绘制当前状态的画布
	 * @param canvas
	 * @param isCancelAnim
	 * @param isNext
	 * @param pageCarver
	 */
	protected abstract void onDrawAnim(Canvas canvas,boolean isCancelAnim,boolean isNext,PageCarver pageCarver);

	/**
	 * 设置动画开始结束点和时间
	 * @param scroller
	 * @param isRequestNext
	 * @param isCancelAnim
	 * @param pageCarver
	 */
	protected abstract void setScroller(Scroller scroller,boolean isRequestNext,boolean isCancelAnim, PageCarver pageCarver);

}
