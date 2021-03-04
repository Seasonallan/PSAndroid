package com.season.example;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.season.lib.ui.view.ToastView;
import com.season.nouse.test.ui.CalendarSelActivity;
import com.season.example.alive.EndCallService;
import com.season.example.alive.LocalService;
import com.season.mvp.ui.BaseTLEActivity;
import com.season.myapplication.R;

public class SplashActivity extends BaseTLEActivity {


    @Override
    protected boolean isTopTileEnable() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

       // NavigationBarUtil.hideNavigationBar(this);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //沉浸式状态栏
       // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);



        WindowManager.LayoutParams lp = getWindow().getAttributes();

        //下面图1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            //下面图2
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            //下面图3
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }
        getWindow().setAttributes(lp);

        if (false){
            return;
        }

        if (false){
            startActivity(new Intent(SplashActivity.this, CalendarSelActivity.class));
            finish();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, PagerActivity.class));
                finish();
            }
        }, 2400);



       // startService(new Intent(this, EndCallService.class));
       //  startService(new Intent(this, LocalService.class));


    }



}
