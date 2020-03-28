package com.season.example.transfer.util;

import android.os.Environment;

/**
 * @brief 应用设置常量
 * @author join
 */
public final class Constants {

	public static String UPLOAD_DIR = Environment.getExternalStorageDirectory()
			+ "/sfreader/upload/";
	public static int PORT = 8888;

	/** 统一编码 */
	public static final String ENCODING = "UTF-8";

	/**
	 * The threshold, in bytes, below which items will be retained in memory and
	 * above which they will be stored as a file.
	 */
	public static final int THRESHOLD_UPLOAD = 1024 * 1024; // 1MB

	/** 缓冲字节长度=1024*4B */
	public static final int BUFFER_LENGTH = 4096;

}
