package com.season.example.alive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import com.season.lib.util.LogUtil;
import com.season.myapplication.KeepAliveConnection;
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