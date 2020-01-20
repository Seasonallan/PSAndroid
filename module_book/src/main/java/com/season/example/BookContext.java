package com.season.example;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.season.lib.dimen.DimenUtil;

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
			DimenUtil.init(null);
			return;
		}
		sContext = context;
		mHandler = new Handler(context.getMainLooper());
        DimenUtil.init(context);
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
