package com.season.example;


import com.alibaba.android.arouter.launcher.ARouter;
import com.season.lib.BaseContext;

public class SeasonApplication extends BaseContext {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);
	}


}
