package com.season.example.transfer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;

import com.season.example.transfer.receiver.OnWsListener;
import com.season.example.transfer.receiver.WSReceiver;
import com.season.example.transfer.serv.WebServerThread.OnWebServListener;

/** 
 *  绑定Web Service的抽象组件
 * @author laijp
 * @date 2014-1-7
 * @email 451360508@qq.com
 */
public abstract class WebServiceModule implements OnWebServListener,
		OnWsListener {

	/**
	 * 服务启动 
	 */
    protected abstract void onServiceStarted(String ipPort);
    /**
     * 服务异常关闭，如sd卡未挂载或wifi断网
     */
    protected abstract void onServiceStopped();
    /**
     * 文件上传进度 
     */
    protected abstract void onFileUpload(String fileName, int progress);
    /**
     * 文件上传成功
     */
    protected abstract void onFileAdded(String fileName);
    /**
     *  文件删除成功
     */
    protected abstract void onFileDeleted(String fileName);
    /**
     *  文件上传异常
     */
    protected abstract void onFileUploadError(String fileName,String error);
    /**
     *  电脑连接成功
     */
    protected abstract void onComputerConnected();
    

	public String getPackageName(){
		return mActivity.getPackageName();
	}
	public Resources getResources(){
		return mActivity.getResources();
	}
	public String getString(int resId, Object... params){
		return mActivity.getString(resId);
	}
	
	private Activity mActivity;

	public WebServiceModule(Activity activity) {
		mActivity = activity;
	}

	static final String TAG = "WebServActivity";

	protected WebService webService;
	private boolean isBound = false;
 
	protected boolean isWebServiceRunning() {
		return webService != null && webService.isRunning();
	}

	private ServiceConnection servConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			webService = ((WebService.LocalBinder) service).getService();
			webService.setOnWebServListener(WebServiceModule.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			webService = null;
		}
	};

	public void onCreate() {
		WSReceiver.register(mActivity, this);
		doBindService();
	}

	@Override
	public void onStarted(final String ip) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onServiceStarted(ip);
			}
		});
	}

	@Override
	public void onStopped() { 
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onServiceStopped();
			}
		});
	}

	@Override
	public void onError(int code) { 
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onServUnavailable();
			}
		});
	}

	@Override
	public void onPercent(final String fileName, final int percent) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onFileUpload(fileName, percent);
			}
		});
	}

	@Override
	public void onServAvailable() {
		doBindService();
	}

	@Override
	public void onServUnavailable() { 
		if (isWebServiceRunning()) {
			onStopped();
			// doUnbindService();
		}
	} 
 
	protected boolean isBound() {
		return this.isBound;
	}

	protected void doBindService() { 
		mActivity.bindService(new Intent(mActivity, WebService.class),
				servConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	protected void doUnbindService() { 
		if (isBound) {
			mActivity.unbindService(servConnection);
			isBound = false;
		}
	}

	public void onDestroy() {
		doUnbindService();
		WSReceiver.unregister(mActivity);
	}

	@Override
	public void onWebFileAdded(final String fileName) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onFileAdded(fileName);
			}
		});
	}

	@Override
	public void onLocalFileDeleted(final String fileName) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onFileDeleted(fileName);
			}
		});
	}

	@Override
	public void onWebFileUploadError(final String fileName, final String error) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				onFileUploadError(fileName, error);
			}
		});
	}

	@Override
	public void onComputerConnect() {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				 onComputerConnected(); 
			}
		});
	}
}
