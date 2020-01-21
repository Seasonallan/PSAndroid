package com.season.lib;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;

/**
 *
 * library 基于的上下文，applicatoin基类，或初始化一次
 *
 * */
public class BaseContext extends Application {
	private static Context sContext;
	private static Handler mHandler;

	public static void init(Context context) {
		sContext = context;
		mHandler = new Handler(context.getMainLooper());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init(this);
	}

	public static Context getInstance(){
		if (sContext == null){
			throw new UnsupportedOperationException("u should init me first");
		}
		return sContext;
	}

	public static DisplayMetrics getDisplayMetrics(){
		if (sContext == null){
			throw new UnsupportedOperationException("u should init me first");
		}
		return sContext.getResources().getDisplayMetrics();
	}

	public static Handler getHandler() {
		if (sContext == null){
			throw new UnsupportedOperationException("u should init me first");
		}
		return mHandler;
	}


}
