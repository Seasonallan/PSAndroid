package com.season.example;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.WindowManager;

import com.season.example.alive.EndCallService;
import com.season.example.alive.LocalService;
import com.season.lib.util.LogUtil;
import com.season.lib.util.NavigationBarUtil;
import com.season.myapplication.KeepAliveConnection;
import com.season.myapplication.R;

import java.util.Random;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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



        startService(new Intent(this, EndCallService.class));
        startService(new Intent(this, LocalService.class));


    }



}
