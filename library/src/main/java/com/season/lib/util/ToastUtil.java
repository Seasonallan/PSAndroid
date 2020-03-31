package com.season.lib.util;

import android.widget.Toast;

import com.season.lib.BaseContext;

public class ToastUtil {
	static Toast mToast;

	public static void showToast(int contentId){
		showToast(BaseContext.getInstance().getString(contentId));
	}

	static long recordTime;
	static String recordContent;
	public static void showToast(String content){
		if (content.equals(recordContent)){
			if (System.currentTimeMillis() - recordTime < 1000){
				return;
			}
		}
		if(mToast == null){
			mToast = Toast.makeText(BaseContext.getInstance(), content, Toast.LENGTH_SHORT);
		}else{
			mToast.setText(content);
		}
		mToast.show();
		recordContent = content;
		recordTime = System.currentTimeMillis();
	}

}
