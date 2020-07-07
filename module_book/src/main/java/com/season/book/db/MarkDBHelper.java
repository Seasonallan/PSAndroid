package com.season.book.db;

import com.season.book.bean.BookMark;
import com.season.lib.BaseContext;
import com.season.lib.support.dbase.base.BaseDBHelper;

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


