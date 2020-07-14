package com.season;


import android.app.Application;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.BookShelfPreLoader;
import com.season.example.EndCallService;
import com.season.example.JobSchedulerService;
import com.season.lib.BaseContext;
import com.season.plugin.PluginHelper;

public class SeasonApplication extends Application {

	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onCreate() {
		super.onCreate();
		ARouter.init(this);

		BaseContext.init(this);
		PluginHelper.getInstance().startPlugin(this);
		BookShelfPreLoader.getInstance().preLoad();

		startService(new Intent(this, EndCallService.class));

		JobSchedulerService.schedule(this);
	}


}
