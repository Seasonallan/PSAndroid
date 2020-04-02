package com.season.example;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.lib.RoutePath;
import com.season.lib.util.NavigationBarUtil;
import com.season.myapplication.R;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(false){
            ARouter.getInstance().build(RoutePath.BOOK).navigation();
            finish();
            return;
        }

        NavigationBarUtil.hideNavigationBar(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, PagerActivity.class));
                finish();
            }
        }, 2400);
    }
}
