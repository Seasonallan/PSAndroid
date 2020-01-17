package com.season.lib.book.dbase;

import android.database.sqlite.SQLiteDatabase;

/**
 * 代表一个数据库
 */
public interface IDbHelper {
    /**
     * 获取操作数据库
     * @return
     */
    public SQLiteDatabase getDatabase();
}
