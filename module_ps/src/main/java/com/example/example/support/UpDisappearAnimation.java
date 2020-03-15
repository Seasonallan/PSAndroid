package com.example.example.support;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class UpDisappearAnimation extends Animation{

	private View mView;
	private LinearLayout.LayoutParams mParam;
	private int viewHeight,viewTopMargin;
	private boolean isVisible = false;
	public boolean isRunning;
	
	public UpDisappearAnimation(View view2Show, long durationMillis) {
		setDuration(durationMillis);
		setInterpolator(new LinearInterpolator());
		mView = view2Show;
		mParam = (LinearLayout.LayoutParams)mView.getLayoutParams();
		viewHeight=mView.getHeight();
	 
		isVisible = mView.getVisibility() == View.VISIBLE;
		if (isVisible) {
			viewTopMargin = 0;
		}else{
			if(viewHeight == 0){
				viewHeight = Math.abs(viewTopMargin);
			}
			mParam.setMargins(mParam.leftMargin, mParam.topMargin, mParam.rightMargin, -viewHeight);
			mView.requestLayout();
			viewTopMargin = -viewHeight;
			mView.setVisibility(View.VISIBLE);
		}
	}

	public static boolean isVisible(View view){
		return view.getVisibility() != View.GONE;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);

		if(interpolatedTime < 1.0f) {
			mParam.bottomMargin = viewTopMargin + (int)((viewTopMargin==0?(-viewHeight):viewHeight)* interpolatedTime);
			mView.requestLayout();
			isRunning = true;
		} else {
			mParam.bottomMargin = (viewTopMargin==0?(-viewHeight):0);
			mView.requestLayout();
			isRunning = false;
			if(isVisible) {
				mView.setVisibility(View.GONE);
			}
		}
	}

}
