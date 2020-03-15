package com.season.lib.util;

import android.widget.Toast;

import com.season.lib.BaseContext;

public class ToastUtil {
	static Toast mToast;

	public static void show(String contentId){
		if(mToast == null){
			mToast = Toast.makeText(BaseContext.getInstance(), contentId, Toast.LENGTH_SHORT);
		}else{
			mToast.setText(contentId);
		}
		mToast.show();
	}

	public static void showToast(int contentId){
		showToast(BaseContext.getInstance().getString(contentId));
	}

	public static void showToast(String contentId){
		if(mToast == null){
			mToast = Toast.makeText(BaseContext.getInstance(), contentId, Toast.LENGTH_SHORT);
		}else{
			mToast.setText(contentId);
		}
		mToast.show();
	}

	public static void dismissToast() {
		if(mToast != null){
			mToast.cancel();
		}
	}
}
