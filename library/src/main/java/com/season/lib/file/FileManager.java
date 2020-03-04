package com.season.lib.file;

import android.text.TextUtils;
import com.season.lib.BaseContext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileManager{

    /**
     * 获取PS文件夹
     * @return
     */
    public static File getPsDir() {
        File mediaStorageDir =  new File(BaseContext.getInstance().getCacheDir(), "ps");
        if (!mediaStorageDir.exists()){
            mediaStorageDir.mkdir();
        }
        return mediaStorageDir;
    }

    /**
     * 获取文件
     * @param name
     * @param type
     * @return
     */
    public static File getPsFile(String name, String type) {
        if (TextUtils.isEmpty(name)) {
            name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        }
        return new File(getPsDir(), name + "." + type);
    }


}
