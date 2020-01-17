package com.season.example.bookreader.dbase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.season.example.BaseApplication;
import com.season.lib.book.dbase.BaseDao;
import com.season.lib.book.dbase.IDbHelper;

public abstract class BaseDBHelper extends SQLiteOpenHelper implements IDbHelper {

	/**
	 * 获取需要创建在该库中的实体
	 * @return
	 */
    public abstract Class<?>[] getDaoLists();
    
    protected SQLiteDatabase mSqliteDatabase;
 
    protected BaseDBHelper(String dbName, int dbVersion) {
		super(BaseApplication.getInstance(), dbName, null, dbVersion);
	}
 

    @Override
    public SQLiteDatabase getDatabase(){
        if (mSqliteDatabase == null){
            mSqliteDatabase = getWritableDatabase();
        }
        return mSqliteDatabase;
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
        for (Class<?> cls : getDaoLists()){
            try { 
                ((BaseDao) cls.newInstance()).createTable(db);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
	}

    /**
     * 是否存在某列
     * @param db
     * @param columnName
     * @return
     */
    public static boolean contains(SQLiteDatabase db, String tableName ,String columnName){
        try{
            Cursor cursor = db.rawQuery( "select * from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName , "%" + columnName + "%"} );
            return null != cursor && cursor.moveToFirst() ;
        }catch (Exception e){
            return false;
        }
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<?> cls : getDaoLists()){
            try { 
                ((BaseDao) cls.newInstance()).onUpgrade(db, oldVersion, newVersion);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
	}
}
