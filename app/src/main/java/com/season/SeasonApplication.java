package com.season;


import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.BookShelfPreLoader;
import com.season.lib.BaseContext;
import com.season.plugin.PluginHelper;

public class SeasonApplication extends Application {

	
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);

		BaseContext.init(this);
		PluginHelper.getInstance().startPlugin(this);
		BookShelfPreLoader.getInstance().preLoad();
	}


}
