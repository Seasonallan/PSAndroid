package com.season.lib.book.dbase;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.season.example.bookreader.dbase.DBConfig;

/**
 * 数据访问路径.
 * URI一般由三部分组成：
         访问资源的命名机制。 content
         存放资源的主机名。 AUTHORITIES
         资源自身的名称，由路径表示。 PATH

 */
public class UriUtil {

    /**
     * 获取URI
     * @param className
     * @return
     */
    public static final Uri getPrefrenceUri(Class className, String path){
        String str = "content://"+ DBConfig.AUTHORITIES + "/" + path +"/";
        if(className != null){
            if(className.equals(Integer.class)){
                str += DBConfig.PrefrenceType.INT;
            }else if(className.equals(Float.class)){
                str += DBConfig.PrefrenceType.FLOAT;
            }else if(className.equals(Boolean.class)){
                str += DBConfig.PrefrenceType.BOOLEAN;
            }else if(className.equals(Long.class)){
                str += DBConfig.PrefrenceType.LONG;
            }else{
                str += DBConfig.PrefrenceType.STRING;
            }
        }else{
            str += DBConfig.PrefrenceType.STRING;
        }
        return Uri.parse(str);
    }
    
    public static String getFilePathFromUri(Uri aData, Activity aContext){
		Uri uri = aData;
		  
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor actualimagecursor = aContext.managedQuery(uri,proj,null,null,null);
		int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		actualimagecursor.moveToFirst();     
		String img_path = actualimagecursor.getString(actual_image_column_index);
		return img_path;
	}
}
