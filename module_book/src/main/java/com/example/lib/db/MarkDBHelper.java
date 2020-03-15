package com.example.lib.db;

import com.example.lib.BaseContext;
import com.example.lib.bean.BookMark;
import com.example.lib.dbase.BaseDBHelper;
import com.example.lib.dbase.DBConfig;

public class MarkDBHelper extends BaseDBHelper {

    public MarkDBHelper() {
        super(BaseContext.getInstance(), DBConfig.DB_NAME, DBConfig.DB_VERSION);
    }

    private static MarkDBHelper sDbHelper;

    public static MarkDBHelper getDBHelper() {
        if (sDbHelper == null) {
            sDbHelper = new MarkDBHelper();
        }
        return sDbHelper;
    }


    /**
     * 获取需要创建在该库中的实体
     *
     * @return
     */
    public Class<?>[] getDaoLists() {
        return new Class<?>[]{BookMark.class};
    }
}


