package com.season.example;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.season.lib.util.ContextUtil;

public class BaseApplication extends Application {
	private static BaseApplication mMyAndroidApplication;

	private Handler mHandler;
	
	public static Context getInstance() {
		return mMyAndroidApplication;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mMyAndroidApplication = this;
		mHandler = new Handler(getMainLooper());
        ContextUtil.init(this);
	}

	public static Handler getHandler() {
		return mMyAndroidApplication.mHandler;
	}


}
