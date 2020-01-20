package com.season.lib.bean;

import java.io.Serializable;

public  class LayerBackground implements Serializable {


    public String imageURLPathFile;
    public String gifURLPathFile;
    String backColorString = "";
    String imgURLPath = "";
    String gifURLPath ="";

    public String getGifURLPath() {
        return gifURLPath;
    }

    public void setGifURLPath(String gifURLPath) {
        this.gifURLPath = gifURLPath;
    }

    public String getBackColorString()
    {
        return backColorString;
    }

    public void setBackColorString(String backColorString)
    {
        this.backColorString = backColorString;
    }

    public String getImgURLPath()
    {
        return imgURLPath;
    }

    public void setImgURLPath(String imgURLPath)
    {
        this.imgURLPath = imgURLPath;
    }

}
