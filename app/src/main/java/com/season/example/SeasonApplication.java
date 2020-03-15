package com.season.example;


import com.alibaba.android.arouter.launcher.ARouter;
import com.season.plugin.PluginSupportApplication;

public class SeasonApplication extends PluginSupportApplication {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);
	}


}
