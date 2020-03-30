package com.season;


import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.BookShelfPreLoader;
import com.season.plugin.PluginSupportApplication;

public class SeasonApplication extends PluginSupportApplication {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);

		BookShelfPreLoader.getInstance().preLoad();
	}


}
