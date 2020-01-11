package com.seaon.lib.scale;

import java.io.Serializable;


/**
 * Created by lizhongxin on 2018/2/23.
 */

public  class BackInfoModelBean implements Serializable {

    /**
     * assetPath : https: //img.biaoqing.com/video/20170817/16372400064.mp4
     * backColorString :
     * imgURLPath :
     * rate : 1
     */

    String assetPath = "";
    String backColorString = "";
    String imgURLPath = "";
    String gifURLPath ="";
    float rate;
    public String assetPathFile;
    public String imageURLPathFile;
    public String gifFilePath;
    public String orignalVideoUrl;
    public String orignalImageUrl;
    public String orignalGIfUrl;

    public String getGifURLPath() {
        return gifURLPath;
    }

    public void setGifURLPath(String gifURLPath) {
        this.gifURLPath = gifURLPath;
    }

    public String getGifFilePath() {
        return gifFilePath;
    }

    public void setGifFilePath(String gifFilePath) {
        this.gifFilePath = gifFilePath;
    }

    public String getOrignalGIfUrl() {
        return orignalGIfUrl;
    }

    public void setOrignalGIfUrl(String orignalGIfUrl) {
        this.orignalGIfUrl = orignalGIfUrl;
    }

    public String getAssetPath()
    {
        return assetPath;
    }

    public void setAssetPath(String assetPath)
    {
        this.assetPath = assetPath;
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

    public float getRate()
    {
        return rate;
    }

    public void setRate(float rate)
    {
        this.rate = rate;
    }

    public String getAssetPathFile() {
        return assetPathFile;
    }

    public void setAssetPathFile(String assetPathFile) {
        this.assetPathFile = assetPathFile;
    }

    public String getImageURLPathFile() {
        return imageURLPathFile;
    }

    public void setImageURLPathFile(String imageURLPathFile) {
        this.imageURLPathFile = imageURLPathFile;
    }

    public String getOrignalVideoUrl() {
        return orignalVideoUrl;
    }

    public void setOrignalVideoUrl(String orignalVideoUrl) {
        this.orignalVideoUrl = orignalVideoUrl;
    }

    public String getOrignalImageUrl() {
        return orignalImageUrl;
    }

    public void setOrignalImageUrl(String orignalImageUrl) {
        this.orignalImageUrl = orignalImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackInfoModelBean that = (BackInfoModelBean) o;

        if (Float.compare(that.rate, rate) != 0) return false;
        if (assetPath != null ? !assetPath.equals(that.assetPath) : that.assetPath != null) return false;
        if (backColorString != null ? !backColorString.equals(that.backColorString) : that.backColorString != null) return false;
        if (imgURLPath != null ? !imgURLPath.equals(that.imgURLPath) : that.imgURLPath != null) return false;
        if (gifURLPath != null ? !gifURLPath.equals(that.gifURLPath) : that.gifURLPath != null) return false;
        if (assetPathFile != null ? !assetPathFile.equals(that.assetPathFile) : that.assetPathFile != null) return false;
        if (imageURLPathFile != null ? !imageURLPathFile.equals(that.imageURLPathFile) : that.imageURLPathFile != null) return false;
        if (gifFilePath != null ? !gifFilePath.equals(that.gifFilePath) : that.gifFilePath != null) return false;
        if (orignalVideoUrl != null ? !orignalVideoUrl.equals(that.orignalVideoUrl) : that.orignalVideoUrl != null) return false;
        if (orignalImageUrl != null ? !orignalImageUrl.equals(that.orignalImageUrl) : that.orignalImageUrl != null) return false;
        return orignalGIfUrl != null ? orignalGIfUrl.equals(that.orignalGIfUrl) : that.orignalGIfUrl == null;
    }

    @Override
    public int hashCode() {
        int result = assetPath != null ? assetPath.hashCode() : 0;
        result = 31 * result + (backColorString != null ? backColorString.hashCode() : 0);
        result = 31 * result + (imgURLPath != null ? imgURLPath.hashCode() : 0);
        result = 31 * result + (gifURLPath != null ? gifURLPath.hashCode() : 0);
        result = 31 * result + (rate != +0.0f ? Float.floatToIntBits(rate) : 0);
        result = 31 * result + (assetPathFile != null ? assetPathFile.hashCode() : 0);
        result = 31 * result + (imageURLPathFile != null ? imageURLPathFile.hashCode() : 0);
        result = 31 * result + (gifFilePath != null ? gifFilePath.hashCode() : 0);
        result = 31 * result + (orignalVideoUrl != null ? orignalVideoUrl.hashCode() : 0);
        result = 31 * result + (orignalImageUrl != null ? orignalImageUrl.hashCode() : 0);
        result = 31 * result + (orignalGIfUrl != null ? orignalGIfUrl.hashCode() : 0);
        return result;
    }
}
