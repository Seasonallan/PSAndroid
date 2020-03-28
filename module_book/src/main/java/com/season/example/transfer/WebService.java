package com.season.example.transfer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.season.example.transfer.receiver.NetworkReceiver;
import com.season.example.transfer.receiver.OnNetworkListener;
import com.season.example.transfer.receiver.WSReceiver;
import com.season.example.transfer.serv.WebServerThread;
import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.util.CommonUtil;
 
/** 
 *  Web Service后台
 * @author laijp
 * @date 2014-1-7
 * @email 451360508@qq.com
 */
public class WebService extends Service implements OnWebServListener, OnNetworkListener{ 

    /** 错误时自动恢复的次数。如果仍旧异常，则继续传递。 */
    private static final int RESUME_COUNT = 3;
    /** 错误时重置次数的时间间隔。 */
    private static final int RESET_INTERVAL = 3000;
    private int errCount = 0;
    private Timer mTimer = new Timer(true);
    private TimerTask resetTask;

    private WebServerThread webServer;
    private OnWebServListener mListener;

    private boolean isRunning = false;  

    private LocalBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public WebService getService() {
            return WebService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();  
        webServer = new WebServerThread(getApplicationContext());
        webServer.setOnWebServListener(this); 

        NetworkReceiver.register(this, this);

        CommonUtil mCommonUtil = CommonUtil.getSingleton(getApplicationContext());
        isNetworkAvailable = mCommonUtil.isNetworkAvailable(); 

        isWebServAvailable = isNetworkAvailable;
        notifyWebServAvailable(isWebServAvailable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        openWebServer();
        return mBinder;
    }

    private void openWebServer() {
        if (webServer != null) {
            webServer.setDaemon(true);
            webServer.start();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        closeWebServer();
        return super.onUnbind(intent);
    }

    private void closeWebServer() {
        if (webServer != null) {
            webServer.close();
            webServer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkReceiver.unregister(this);
        mListener = null;
    }

    @Override
    public void onStarted(String ip) {
        if (mListener != null) {
            mListener.onStarted(ip);
        }
        isRunning = true;
    }

    @Override
    public void onStopped() { 
        if (mListener != null) {
            mListener.onStopped();
        }
        isRunning = false;
    }

    @Override
    public void onError(int code) { 
        if (code != WebServerThread.ERR_UNEXPECT) {
            if (mListener != null) {
                mListener.onError(code);
            }
            return;
        }
        errCount++;
        restartResetTask(RESET_INTERVAL);
        if (errCount <= RESUME_COUNT) { 
            openWebServer();
        } else {
            if (mListener != null) {
                mListener.onError(code);
            }
            errCount = 0;
            cancelResetTask();
        }
    }

    private void cancelResetTask() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
    }

    private void restartResetTask(long delay) {
        cancelResetTask();
        resetTask = new TimerTask() {
            @Override
            public void run() {
                errCount = 0;
                resetTask = null; 
            }
        };
        mTimer.schedule(resetTask, delay);
    }
  

    public boolean isRunning() {
        return isRunning;
    }

    public void setOnWebServListener(OnWebServListener mListener) {
        this.mListener = mListener;
    }

	@Override
	public void onPercent(String fileName, int percent) { 
        if (mListener != null) {
            mListener.onPercent(fileName, percent);
        }
	}
 
    public boolean isWebServAvailable = false;

    private boolean isNetworkAvailable; 
    @Override
    public void onConnected(boolean isWifi) {
        isNetworkAvailable = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onDisconnected() {
        isNetworkAvailable = false;
        notifyWebServAvailableChanged();
    }
 
    private void notifyWebServAvailable(boolean isAvailable) { 
        // Notify if web service is available.
        String action = isAvailable ? WSReceiver.ACTION_SERV_AVAILABLE
                : WSReceiver.ACTION_SERV_UNAVAILABLE;
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void notifyWebServAvailableChanged() {
        boolean isAvailable = isNetworkAvailable;
        if (isAvailable != isWebServAvailable) {
            notifyWebServAvailable(isAvailable);
            isWebServAvailable = isAvailable;
        }
    }

	@Override
	public void onWebFileAdded(String fileName) { 
		 if (mListener != null) {
	            mListener.onWebFileAdded(fileName);
	        }
	}

	@Override
	public void onLocalFileDeleted(String fileName) {
		 if (mListener != null) {
	            mListener.onLocalFileDeleted(fileName);
	        }
	}

	@Override
	public void onWebFileUploadError(String fileName, String error) {
		 if (mListener != null) {
	            mListener.onWebFileUploadError(fileName, error);
	        }
	}

	@Override
	public void onComputerConnect() {
		 if (mListener != null) {
	            mListener.onComputerConnect();
	        }
	}

}
