package com.season.example.transfer.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * wifi传书数据记录控制
 * @author laijp
 * @date 2013-12-31
 * @email 451360508@qq.com
 */
public class SaverUtil {

	private static final String S_NAME = "transFiles";
	private static final String S_KEY = "transFiles";
	private static final String REGULAR_EXPRESSION = "-,-";
	private String mExtraKey = "";
	private static SaverUtil mCommonUtil;
	private SharedPreferences mSharedPreferences;

	public static SaverUtil getSingleton(Context context) {
		if (mCommonUtil == null) {
			mCommonUtil = new SaverUtil(context);
		}
		return mCommonUtil;
	}

	private SaverUtil(Context context) {
		mListeners = new ArrayList<FileChangeListener>();
		mSharedPreferences = context.getSharedPreferences(S_NAME,
				Context.MODE_PRIVATE);
	}

	public void setExtraKey(String key){
		this.mExtraKey = key;
	}
	
	public List<String> getHistoryFileLists() {
		List<String> res = new ArrayList<String>();
		String fileLists = mSharedPreferences.getString(S_KEY + mExtraKey, null);
		if(fileLists != null){
			String[] strings = fileLists.split(REGULAR_EXPRESSION); 
			for (int i = 0; i < strings.length; i++) { 
				if(!contains(strings[i], res) && !TextUtils.isEmpty(strings[i])){
					res.add(strings[i]);
				}
			} 
		}
		return res;
	}
	private void saveHistoryFileLists(List<String> list){
		if(list != null){
			StringBuffer resBuffer = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {
				resBuffer.append(list.get(i)+(i == list.size()-1? "": REGULAR_EXPRESSION));
			}
			mSharedPreferences.edit().putString(S_KEY + mExtraKey, resBuffer.toString()).commit();
		}
	}

	private boolean contains(String key, List<String> set){
		for (String string : set) {
			if(key.equals(string)){
				return true;
			}
		}
		return false;
	}
	
	public boolean addFile2List(String filePath) { 
		List<String> fileList = getHistoryFileLists();
		if(contains(filePath, fileList)){
			return false;
		}
		fileList.add(filePath);
		saveHistoryFileLists(fileList);
		notifyFileAdd(filePath);
		return true;
	}

	public boolean deleteFromListBuFileName(String filePath) {
		List<String> fileList = getHistoryFileLists();
		if(!contains(filePath, fileList)){
			return false;
		}
		fileList.remove(filePath);
		saveHistoryFileLists(fileList);
		notifyFileDelete(filePath); 
		return true;
	}
	
	public boolean deleteFileFromList(String filePath) {
		List<String> fileList = getHistoryFileLists();
		if(!contains(filePath, fileList)){
			return false;
		}
		fileList.remove(filePath);
		saveHistoryFileLists(fileList);
		notifyFileDelete(filePath); 
		return true;
	}

	private void notifyFileAdd(String filePath) {
		if (mListeners != null) {
			for (FileChangeListener listener : mListeners) {
				listener.onFileAdded(filePath);
			}
		}
	}

	private void notifyFileDelete(String filePath) {
		if (mListeners != null) {
			for (FileChangeListener listener : mListeners) {
				listener.onFileDelete(filePath);
			}
		}
	}

	public void setOnFileChangeListener(FileChangeListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<FileChangeListener>();
		}
		mListeners.add(listener);
	}

	private List<FileChangeListener> mListeners;

	public static interface FileChangeListener {
		public void onFileAdded(String filePath);

		public void onFileDelete(String filePath);
	}

}
