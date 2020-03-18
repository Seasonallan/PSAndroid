package com.season.plugin.stub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.season.lib.util.LogUtil;


/**
 * Disc: 壳 service基类
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public abstract class AbstractServiceStub extends Service {
    private static final String TAG = "AbstractServiceStub";

    private static ServcesManager mCreator = ServcesManager.getDefault();

    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        try {
            mCreator.onDestroy();
        } catch (Exception e) {
            handleException(e);
        }
        super.onDestroy();
        isRunning = false;
        try {
            synchronized (sLock) {
                sLock.notifyAll();
            }
        } catch (Exception e) {
        }
    }

    public static void startKillService(Context context, Intent service) {
        service.putExtra("ActionKillSelf", true);
        context.startService(service);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try {
            if (intent != null) {
                if (intent.getBooleanExtra("ActionKillSelf", false)) {
                    startKillSelf();
                    if (!ServcesManager.getDefault().hasServiceRunning()) {
                        stopSelf(startId);
                        boolean stopService = getApplication().stopService(intent);
                        LogUtil.i(TAG, "doGc Kill Process(pid=%s,uid=%s has exit) for %s onStart=%s intent=%s", android.os.Process.myPid(), android.os.Process.myUid(), getClass().getSimpleName(), stopService, intent);
                    } else {
                        LogUtil.i(TAG, "doGc Kill Process(pid=%s,uid=%s has exit) for %s onStart intent=%s skip,has service running", android.os.Process.myPid(), android.os.Process.myUid(), getClass().getSimpleName(), intent);
                    }

                } else {
                    mCreator.onStart(this, intent, 0, startId);
                }
            }
        } catch (Throwable e) {
            handleException(e);
        }
        super.onStart(intent, startId);
    }

    private Object sLock = new Object();

    private void startKillSelf() {
        if (isRunning) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (sLock) {
                            try {
                                sLock.wait();
                            } catch (Exception e) {
                            }
                        }
                        LogUtil.i(TAG, "doGc Kill Process(pid=%s,uid=%s has exit) for %s 2", android.os.Process.myPid(), android.os.Process.myUid(), getClass().getSimpleName());
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleException(Throwable e) {
        LogUtil.e(TAG, "handleException", e);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            if (rootIntent != null) {
                mCreator.onTaskRemoved(this, rootIntent);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        try {
            if (intent != null) {
                return mCreator.onBind(this, intent);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        try {
            if (intent != null) {
                mCreator.onRebind(this, intent);
            }
        } catch (Exception e) {
            handleException(e);
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            if (intent != null) {
                return mCreator.onUnbind(intent);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }
}
