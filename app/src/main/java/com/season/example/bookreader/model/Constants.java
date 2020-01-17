package com.season.example.bookreader.model;

import java.io.File;

import com.season.lib.util.FileUtil;

public class Constants {
	public static final String bookStoredDiretory = FileUtil.getExternalStorageDirectory()
			+ File.separator + "BookCoreTest" + File.separator;
	/** 存放LOG */
	public static final String FILE_LOG = bookStoredDiretory + "log.txt";
	/** 缓存目录 */
	public static final String BOOKS_TEMP = bookStoredDiretory + "temp/";
}
