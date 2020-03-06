package com.season.example.layout;

import android.app.Activity;
import android.view.View;


public abstract class BaseBottomView {

    protected View containerView;
    protected Activity activity;
    protected  <T extends View> T findView(int id){
        return activity.findViewById(id);
    }
    public boolean isShowing(){
        return containerView.getVisibility() == View.VISIBLE;
    }

    public void show() {
        containerView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        containerView.setVisibility(View.GONE);
    }

    public BaseBottomView(Activity activity) {
        this.activity = activity;
        containerView = findView(getContentId());
        containerView.setVisibility(View.GONE);
    }

    protected abstract int getContentId();

}
