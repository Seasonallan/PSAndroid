package com.season.example;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

public class SeasonApplication extends Application {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);
	}


}
