package com.season.example.alive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.season.example.SplashActivity;
import com.season.lib.util.LogUtil;
import com.season.myapplication.KeepAliveConnection;
import com.season.myapplication.R;

import java.util.Random;

public class LocalService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private KeepAliveConnection.Stub stub = new KeepAliveConnection.Stub() {
        @Override
        public String getDesc() throws RemoteException {
            return "Local>>" + count;
        }
    };

    ServiceConnection serviceConnection;
    boolean mBound = false;

    @Override
    public void onCreate() {
        super.onCreate();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent nfIntent = new Intent(this, SplashActivity.class);
            Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                    .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText("正在运行") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

            NotificationChannel notificationChannel = new NotificationChannel("1024", "1024", NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId("1024");

            Notification notification = builder.build(); // 获取构建好的Notification
            notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

            startForeground(1, notification);
        }

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBound = true;
                LogUtil.e("alive","LocalService onServiceConnected");
                final KeepAliveConnection keepAliveConnection = KeepAliveConnection.Stub.asInterface(service);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LogUtil.e("alive", "LocalService>>>"+ keepAliveConnection.getDesc());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Random().nextInt(100000));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;

                LogUtil.e("alive","LocalService onServiceDisconnected ");
            }
        };
        bindService(new Intent(this, EndCallService.class), serviceConnection, BIND_AUTO_CREATE);

        LogUtil.e("alive","Local onCreate");
        mRadThread.start();

    }

    private int count = 1;

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public Thread mRadThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()){
                count ++;
                LogUtil.e("alive","count="+count);
                try {
                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("alive", "onDestroy");
        mRadThread.interrupt();
        if (mBound)
            unbindService(serviceConnection);
    }

}