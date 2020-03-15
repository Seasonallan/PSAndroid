package com.example.lib.bean;

import android.text.TextUtils;

import java.io.Serializable;

public  class LayerBackground implements Serializable {


    public String backColorString;
    public String imgURLPath;
    public String imageURLPathFile;
    public String gifURLPath;
    public String gifURLPathFile;

    public boolean isStaticImage(){
        return !TextUtils.isEmpty(imgURLPath) ||
                !TextUtils.isEmpty(imageURLPathFile);
    }

    public boolean isColor(){
        return TextUtils.isEmpty(imgURLPath) &&
                TextUtils.isEmpty(imageURLPathFile)&&
                TextUtils.isEmpty(gifURLPath)&&
                TextUtils.isEmpty(gifURLPathFile);
    }

}
