package com.season.example;

import android.app.Activity;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.season.book.R;
import com.season.example.transfer.util.Constants;

import java.io.File;

/**
 * @author laijp
 * @date 2014-1-7
 * @email 451360508@qq.com
 */
public abstract class BaseLayout {

	protected View parentView;
	protected ViewStub viewStub;
	public BaseLayout(ViewStub viewStub) {
		this.viewStub = viewStub;
	}

	public void inflate(Activity activity) {
		if (parentView != null){
			return;
		}
		viewStub.inflate();

		parentView = getView(activity);

		parentView.setVisibility(View.GONE);

		initAnimation();
	}



	private boolean isShowing = false;
	Animation showAnimation, hideAnimation;
	private void initAnimation() {
		showAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		showAnimation.setDuration(600);
		showAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isShowing = false;
			}
		});

		hideAnimation = new TranslateAnimation(Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, 0,
				Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 1.0f);
		hideAnimation.setDuration(600);
		hideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isShowing = false;
				parentView.setVisibility(View.GONE);
			}
		});
	}

	public void switchStatus(){
		if (isShowing){
			return;
		}
		isShowing = true;
		onStatusChange(parentView.getVisibility());
		if (parentView.getVisibility() == View.VISIBLE){
			parentView.startAnimation(hideAnimation);
		}else{
			parentView.setVisibility(View.VISIBLE);
			parentView.startAnimation(showAnimation);
		}
	}


	protected abstract View getView(Activity activity);
	protected abstract void onStatusChange(int visible);

	public boolean onBackPressed() {
		if (parentView != null && parentView.getVisibility() == View.VISIBLE){
			switchStatus();
			return true;
		}
		return false;
	}
}