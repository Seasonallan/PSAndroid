package com.season.lib;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;

public class BaseContext extends Application {
	private static Context sContext;
	private static Handler mHandler;
	public static Context getInstance() {
		return sContext;
	}

	public static void onCreate(Context context) {
		sContext = context;
		mHandler = new Handler(context.getMainLooper());
	}

	public static Context getContext(){
		return sContext;
	}

	public static DisplayMetrics getDisplayMetrics(){
		return sContext.getResources().getDisplayMetrics();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		onCreate(this);
	}

	public static Handler getHandler() {
		return mHandler;
	}


}
