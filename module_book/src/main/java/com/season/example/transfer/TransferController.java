package com.season.example.transfer;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.season.example.transfer.util.Constants;
import com.season.book.R;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.util.ToastUtil;

import java.io.File;

/**
 * @author laijp
 * @date 2014-1-7
 * @email 451360508@qq.com
 */
public class TransferController {

	private final int INVALID = 1;
	private final int CONNECTED = 2;
	private final int UPLOADING = 3;

	private View mConnectContainerView, mTransferContainerView,
			mReadyContainerView, mUploadingContainerView;

	private ProgressBar mPercentView;
	private TextView mIpView,mTransferDesView,  mTransferResultView;
	private WebServiceModule mWebServiceModule;
	private int mSuccessCount = 0,mFailCount=0;

	private View parentView;
	public TransferController(Activity activity) {
		Constants.UPLOAD_DIR =  activity.getCacheDir() + File.separator;

		parentView = activity.findViewById(R.id.transfer_main);
		mConnectContainerView = parentView.findViewById(R.id.tranfer_bottom_connect);
		mTransferContainerView =  parentView.findViewById(R.id.tranfer_bottom_ready);
		mReadyContainerView =  parentView.findViewById(R.id.tranfer_bottom_ready_pre);
		mUploadingContainerView =  parentView.findViewById(R.id.tranfer_bottom_ready_run);

		mPercentView =  parentView.findViewById(R.id.transfer_progress);
		mIpView =  parentView.findViewById(R.id.urlText);
		mTransferDesView =  parentView.findViewById(R.id.transfer_desc);
		mTransferResultView = parentView.findViewById(R.id.transfer_res);
		mIpView = parentView.findViewById(R.id.urlText);

		onHttpStatusChange(INVALID);

		initAnimation();
		initWebSocket(activity);
	}

	private void initWebSocket(Activity activity){
		mWebServiceModule = new WebServiceModule(activity) {

			@Override
			protected void onComputerConnected() {
				if(!isTransfering && mWebServiceModule != null){
					onHttpStatusChange(CONNECTED);
				}
			}

			@Override
			protected void onServiceStarted(String ip) {
				if(mWebServiceModule != null){
					onHttpStatusChange(INVALID);
					mIpView.setText(ip);
				}
			}

			@Override
			protected void onServiceStopped() {
				if(mWebServiceModule != null){
					onHttpStatusChange(INVALID);
					isTransfering = false;
				}
			}

			@Override
			protected void onFileUpload(String fileName, int percent) {
				if (!TextUtils.isEmpty(fileName) && percent > 0) {
					if(percent > recordProgress && mWebServiceModule != null){
						recordProgress = percent;
						isTransfering = true;
						onHttpStatusChange(UPLOADING);
						mTransferDesView.setText(getResources().getString(R.string.transfer_ing, fileName));
						mPercentView.setProgress(recordProgress);
					}
				}
			}

			@Override
			public void onFileAdded(String fileName) {
				if(mWebServiceModule != null){
					recordProgress = -1;
					mSuccessCount++;
					mPercentView.setProgress(100);
					mTransferDesView.setText(getResources().getString(R.string.transfer_end, fileName));
					mTransferResultView.setText(getResources().getString(R.string.transfer_res, mSuccessCount+mFailCount, mFailCount));
				}
				addFile(Constants.UPLOAD_DIR + fileName);
			}

			@Override
			public void onFileDeleted(String fileName) {
				ToastUtil.showToast("成功删除文件 " );
			}

			@Override
			protected void onFileUploadError(String fileName, String error) {
				if(mWebServiceModule != null){
					recordProgress = -1;
					mFailCount++;
					mTransferDesView.setText(getResources().getString(R.string.transfer_error, fileName));
					mTransferResultView.setText(getResources().getString(R.string.transfer_res, mSuccessCount+mFailCount, mFailCount));
				}
			}

		};
	}

	protected void addFile(String filePath){

	}



	private boolean isShowing = false;
	Animation showAnimation, hideAnimation;
	private void initAnimation() {
		showAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		showAnimation.setDuration(600);
		showAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isShowing = false;
			}
		});

		hideAnimation = new TranslateAnimation(Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, 0,
				Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 1.0f);
		hideAnimation.setDuration(600);
		hideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isShowing = false;
				parentView.setVisibility(View.GONE);
			}
		});
	}

	public void switchStatus(){
		if (isShowing){
			return;
		}
		isShowing = true;
		if (parentView.getVisibility() == View.VISIBLE){
			parentView.startAnimation(hideAnimation);
			mWebServiceModule.onDestroy();
		}else{
			parentView.setVisibility(View.VISIBLE);
			parentView.startAnimation(showAnimation);
			mWebServiceModule.onCreate();
		}
	}



	private void onHttpStatusChange(int status){ 
		switch (status) {
		case INVALID: 
			mConnectContainerView.setVisibility(View.VISIBLE);
			mTransferContainerView.setVisibility(View.GONE);
			break;
		case CONNECTED: 
			mConnectContainerView.setVisibility(View.GONE);
			mTransferContainerView.setVisibility(View.VISIBLE);
			mReadyContainerView.setVisibility(View.VISIBLE);
			mUploadingContainerView.setVisibility(View.GONE);
			break;
		case UPLOADING: 
			mConnectContainerView.setVisibility(View.GONE);
			mTransferContainerView.setVisibility(View.VISIBLE);
			mReadyContainerView.setVisibility(View.GONE);
			mUploadingContainerView.setVisibility(View.VISIBLE); 
			break;
		}
	}
	
	private boolean isTransfering = false;
	private int recordProgress = -1;

	public boolean onBackPressed() {
		if (parentView.getVisibility() == View.VISIBLE){
			switchStatus();
			return true;
		}
		return false;
	}
}