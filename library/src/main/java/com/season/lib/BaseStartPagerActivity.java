package com.season.lib;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;

import com.season.lib.util.NavigationBarUtil;
import com.season.lib.view.StartPageView;

public abstract class BaseStartPagerActivity extends FragmentActivity {

    private StartPageView startPageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseContext.init(getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        NavigationBarUtil.hideNavigationBar(this);

        if (RoutePath.sCacheBitmap != null && !RoutePath.sCacheBitmap.isRecycled()){
            RelativeLayout relativeLayout = new RelativeLayout(this);
            setContentView(relativeLayout);
            startPageView = new StartPageView(this);
            startPageView.setBiamtp(RoutePath.sCacheBitmap);
            startPageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(0, 0);
                }
            });
            startPageView.post(new Runnable() {
                @Override
                public void run() {
                    startPageView.start();
                }
            });
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            View contentView = LayoutInflater.from(this).inflate(getLayoutId(), null);
            relativeLayout.addView(contentView, params);
            relativeLayout.addView(startPageView, params);
            overridePendingTransition(0, 0);
        }else{
            setContentView(getLayoutId());
        }
    }

    protected abstract int getLayoutId();

    protected void onPagerFinish(){
        if (startPageView != null){
            startPageView.finish();
            return;
        }
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (startPageView != null){
            startPageView.finish();
            return;
        }
        super.onBackPressed();
    }
}