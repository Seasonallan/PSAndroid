package com.example.example;


import com.alibaba.android.arouter.launcher.ARouter;
import com.example.lib.BaseContext;

public class SeasonApplication extends BaseContext {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);
	}


}
