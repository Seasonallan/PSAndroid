package com.season.lib;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.season.lib.bitmap.BitmapUtil;

public class RoutePath {
    public static final String BOOK = "/season/epub_book/main";

    public static final String PS = "/ps/main";
    public static final String PS_CROP = "/ps/crop";

    public static final String PLUGIN = "/plugin/main";

    private static Bitmap sCacheBitmap;
    public static void putCacheBitmap(Bitmap bitmap){
        sCacheBitmap = bitmap;
    }

    public static Bitmap getCacheBitmap(){
        if (BitmapUtil.isBitmapAvaliable(sCacheBitmap)){
            Bitmap resultBitmap = Bitmap.createBitmap(sCacheBitmap.getWidth(), sCacheBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(sCacheBitmap, 0,0, null);
            return resultBitmap;
        }
        return null;
    }

}
