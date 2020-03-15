package com.season.book.anim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 水分平滑翻页动画
 *
 */
public class HorTranslationAnimController extends AbsHorGestureAnimController {
	private GradientDrawable mShadowDrawableL;
	private Integer mFromIndex;
	private Integer mToIndex;
	private Rect mNextRect;
	private Rect mCurrentRect;

	HorTranslationAnimController(Context context){
		this(context, null);
	}

	HorTranslationAnimController(Context context, Interpolator interpolator){
		super(context, interpolator);
		int[] frontShadowColors = new int[] { 0xa0111111, 0x111111 };
		mShadowDrawableL = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, frontShadowColors);
		mCurrentRect = new Rect();
		mNextRect = new Rect();
	}

	@Override
	public boolean interruptAnimationEnd(){
		return false;
	}

	@Override
	protected void onAnimStart(boolean isCancelAnim) {
		
	}

	@Override
	protected void onAnimEnd(boolean isCancelAnim) {
		mFromIndex = null;
		mToIndex = null;
	}

	@Override
	protected void onRequestPage(boolean isRequestNext,int fromIndex,int toIndex,float x,float y) {
		mFromIndex = fromIndex;
		mToIndex = toIndex;
	}

	@Override
	protected void onDrawAnim(Canvas canvas,boolean isCancelAnim,boolean isNext,PageCarver pageCarver) {
		int moveX = (int) (mDownTouchPoint.x - mLastTouchPoint.x);
		if(isNext){
			mCurrentRect.set(moveX
					, 0
					, mContentWidth
					, mContentHeight);
			mNextRect.set(mContentWidth - moveX
					, 0
					, mContentWidth
					, mContentHeight);
		}else{
			mCurrentRect.set(-moveX
					, 0
					, mContentWidth
					, mContentHeight);
			mNextRect.set(mContentWidth + moveX
					, 0
					, mContentWidth
					, mContentHeight);
		}
		
		canvas.save();
		if(!isNext){
			canvas.translate(-(mContentWidth + moveX), 0);
		}
		canvas.clipRect(mNextRect);
		pageCarver.drawPage(canvas,mToIndex);
		canvas.restore();
		
		canvas.save();
		if(isNext){
			canvas.translate(-moveX, 0);
		}
		canvas.clipRect(mCurrentRect);
		pageCarver.drawPage(canvas,mFromIndex);
		canvas.restore();
		
		canvas.save();
		int left = 0;
		if(isNext){
			left = mContentWidth - moveX;
		}else{
			left = - moveX;
		}
		mShadowDrawableL.setBounds(left, 0, left + 20, mContentHeight);
		mShadowDrawableL.draw(canvas);
		canvas.restore();
		//pageCarver.requestInvalidate();
	}
	
	@Override
	protected void setScroller(Scroller scroller,boolean isRequestNext,boolean isCancelAnim, PageCarver pageCarver){
		int dx = 0;
		int dy = 0;
		if(isCancelAnim){
			if(isRequestNext){
				dx = (int)(mDownTouchPoint.x - mLastTouchPoint.x);
			}else{
				dx = (int)-(mLastTouchPoint.x - mDownTouchPoint.x);
			}
//			LogUtil.i(TAG,"startCancelAnim dx="+dx+" isDown="+isTouchStart+" mLastTouchPoint.x="+mLastTouchPoint.x);
			dy = 0;
		}else{
			if(isRequestNext){
				dx = (int) -(pageCarver.getContentWidth() - (mDownTouchPoint.x - mLastTouchPoint.x));
			}else{
				dx = (int) (pageCarver.getContentWidth() - (mLastTouchPoint.x - mDownTouchPoint.x));
			}
			dy = 0;
		}
		scrollerDecorator(scroller, (int)mLastTouchPoint.x, (int)mLastTouchPoint.y, dx, dy, mDuration);

		if(isCancelAnim){
			if(!isRequestNext){
				scroller.setFinalX(scroller.getFinalX() - 20);
			}
		}else{
			if(isRequestNext){
				scroller.setFinalX(scroller.getFinalX() - 20);
			}
		}
	}
}
