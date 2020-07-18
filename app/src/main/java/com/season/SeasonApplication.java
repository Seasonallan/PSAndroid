package com.season;


import android.app.Application;
import android.os.Build;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.BookShelfPreLoader;
import com.season.example.alive.JobSchedulerService;
import com.season.lib.BaseContext;
import com.season.lib.util.LogUtil;
import com.season.plugin.PluginHelper;

public class SeasonApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		LogUtil.e("alive", "SeasonApplication onCreate");

		ARouter.init(this);

		BaseContext.init(this);
		PluginHelper.getInstance().startPlugin(this);
		BookShelfPreLoader.getInstance().preLoad();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			JobSchedulerService.schedule(this);
		}


	}

}
