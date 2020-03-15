package com.example.lib.anim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.example.lib.util.LogUtil;

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
	private Scroller mScroller;
	private boolean isCancelAnim;
	private Boolean isRequestNextPage;
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
	protected Handler mHandler;

    private int mTouchSlopSquare;
	public void setDuration(int duration){
		this.mDuration = duration;
	}

    AbsHorGestureAnimController(Context context){
        this(context, null);
    }

    AbsHorGestureAnimController(Context context, Interpolator interpolator){
        super(context);
		mHandler = new Handler(context.getMainLooper());
        mScroller = new Scroller(context, interpolator);
        mLastTouchPoint = new PointF();
        mDownTouchPoint = new PointF();
        mContentWidth = -1;
        int touchSlop = ViewConfiguration.getTouchSlop();
        mTouchSlopSquare = touchSlop * touchSlop;
    }

	
	private void checkInit(PageCarver pageCarver){
		if(mContentWidth == -1 
//				|| mScreenWidth != pageCarver.getScreenWidth() 
//				|| mScreenHeight != pageCarver.getScreenHeight()
				){
			onMeasure(pageCarver);
		}
	}
	
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
	}
	
	@Override
	public void dispatchTouchEvent(MotionEvent event, PageCarver pageCarver) {
		if(!mScroller.isFinished()){
			LogUtil.i(TAG,"dispatchTouchEvent isAnimStop");
			stopAnim(pageCarver);
		}
		checkInit(pageCarver);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownTouchPoint.set(event.getX(), event.getY());
			mLastTouchPoint.set(event.getX(), event.getY());
			mLastMoveX = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) (mDownTouchPoint.x - event.getX());
			if(isRequestNextPage == null){
				if(!touchStickMode || Math.abs(moveX) > 5){
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
			}else{
				if(isRequestNextPage){
					if(moveX < 0){
						mDownTouchPoint.set(mDownTouchPoint.x - moveX, mDownTouchPoint.y);
					}
				}else{
					if(moveX > 0){
						mDownTouchPoint.set(mDownTouchPoint.x - moveX, mDownTouchPoint.y);
					}
				}
			}
            mLastMoveX = (int) (mLastTouchPoint.x - event.getX());
            final int deltaX = (int) (event.getX() - mLastTouchPoint.x);
            final int deltaY = (int) (event.getY() - mLastTouchPoint.y);
            int distance = (deltaX * deltaX) + (deltaY * deltaY);
            if (!touchStickMode || distance > mTouchSlopSquare) {
                mLastTouchPoint.set(event.getX(), event.getY());
                pageCarver.requestInvalidate();
            }
			break;
		case MotionEvent.ACTION_UP:
			if(isRequestNextPage != null){
				if(isRequestNextPage && mLastMoveX < 0){
					startCancelAnim(isRequestNextPage, pageCarver);
				}else if(!isRequestNextPage && mLastMoveX > 0){
					startCancelAnim(isRequestNextPage, pageCarver);
				}else{
					startAnim(isRequestNextPage, pageCarver);
				}
			}
			isTouchStart = false;
			break;
		}
	}

	@Override
	public boolean dispatchDrawPage(Canvas canvas, final PageCarver pageCarver) {
		if(isRequestNextPage == null){
			return false;
		}
		checkInit(pageCarver);
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
	
	private void dispatchAnimEnd(PageCarver pageCarver){
		LogUtil.e("dispatchAnimEnd");
        isFullAnimation = false;
		isAnimStart = false;
		isRequestNextPage = null;
		onAnimEnd(isCancelAnim);
		pageCarver.onStopAnim(isCancelAnim);
		isCancelAnim = false;
		pageCarver.requestInvalidate();
	}

	/**
	 * 滑动器时长 装饰
	 */
	protected void scrollerDecorator(Scroller scroller, int x, int y, int dx, int dy, int duration){
		double length =  Math.sqrt(dx * dx + dy * dy);
		int maxLength = mContentWidth;
		if (length < maxLength && touchStickMode){
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
