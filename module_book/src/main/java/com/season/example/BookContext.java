package com.season.example;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.season.lib.util.ContextUtil;

public class BookContext extends Application {
	private static Context sContext;
	private static Handler mHandler;
	public static Context getInstance() {
		return sContext;
	}

	public static void onCreate(Context context) {
		if (context == null){
			sContext = null;
			mHandler = null;
			ContextUtil.init(null);
			return;
		}
		sContext = context;
		mHandler = new Handler(context.getMainLooper());
        ContextUtil.init(context);
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
