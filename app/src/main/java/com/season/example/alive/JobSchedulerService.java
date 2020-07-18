package com.season.example.alive;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;

import com.season.lib.BaseContext;
import com.season.lib.support.file.FileUtils;
import com.season.lib.util.LogUtil;

import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        // 返回true，表示该工作耗时，同时工作处理完成后需要调用jobFinished销毁
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    // 创建一个handler来处理对应的job
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        // 在Handler中，需要实现handleMessage(Message msg)方法来处理任务逻辑。
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean handleMessage(Message msg) {
            BaseContext.showToast("JobService active");
            LogUtil.e("alive", "start service>>>>>");
            FileUtils.writeStr2File("----执行一次调度----" + new Date().toLocaleString() +"----", getCacheDir() +"/job.txt");
            startService(new Intent(JobSchedulerService.this, EndCallService.class));
            startService(new Intent(JobSchedulerService.this, LocalService.class));

            // 调用jobFinished
            jobFinished((JobParameters) msg.obj, false);

           // schedule(JobSchedulerService.this);
            return true;
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void schedule(Context context){
        JobScheduler mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(10240,//new Random().nextInt(Integer.MAX_VALUE)
                new ComponentName(context.getPackageName(), JobSchedulerService.class.getName()));
       // builder.setMinimumLatency(6000);
        builder.setPeriodic(15 * 60 * 1000, 5 * 60 *1000);
        // 设置每3秒执行一下任务
       // builder.setPeriodic(3000);
        // 设置设备重启时，执行该任务
        builder.setPersisted(true);
        // 当插入充电器，执行该任务
        //builder.setRequiresCharging(true);

        mJobScheduler.schedule(builder.build());
    }

}