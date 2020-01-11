package com.seaon.lib.scale;

import java.io.Serializable;


/**
 * Created by lizhongxin on 2017/5/9.
 * 参考value的textbook.plist（ios对应的mode）进行mode的设置。
 */

public class TextStyleEntity implements Serializable {

    //    <key>imageName</key>//背景图位置
    //		<string></string>
    //		<key>imageWidth</key>//背景图宽度
    //		<integer>0</integer>
    //		<key>imageHeight</key>//高度
    private String imageName="";//背景图资源 气泡框
    private float imageWidth;//背景图的宽
    private float imageHeight;//背景图的高
    private float textX;
    private float textY;
    private float textW;
    public int animationType = 0;
    private float textH;
    private String thumbnail="";//样式展示图，缩率图
    private String fontName="";
    //TODO
    private boolean isDownloading;

    public boolean isDownloading()
    {
        return isDownloading;
    }

    public void setDownloading(boolean download)
    {
        isDownloading = download;
    }

    /**
     *现有的代码逻辑，加入这个地址更好做。但是ios端对应的模型没有这个textFontPath参数。
     */
    private String textFontPath="";
    public String getTextFontPath()
    {
        return textFontPath;
    }
    public void setTextFontPath(String textFontPath)
    {
        this.textFontPath = textFontPath;
    }
    public String getFontName()
    {
        return fontName;
    }

    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }


    public float getImageWidth()
    {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth)
    {
        this.imageWidth = imageWidth;
    }

    public float getImageHeight()
    {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight)
    {
        this.imageHeight = imageHeight;
    }

    public float getTextX()
    {
        return textX;
    }

    public void setTextX(Integer textX)
    {
        this.textX = textX;
    }

    public float getTextW()
    {
        return textW;
    }

    public void setTextW(float textW)
    {
        this.textW = textW;
    }

    public float getTextY()
    {
        return textY;
    }

    public void setTextY(float textY)
    {
        this.textY = textY;
    }

    public float getTextH()
    {
        return textH;
    }

    public void setTextH(Integer textH)
    {
        this.textH = textH;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextStyleEntity that = (TextStyleEntity) o;

        if (Float.compare(that.imageWidth, imageWidth) != 0) return false;
        if (Float.compare(that.imageHeight, imageHeight) != 0) return false;
        if (Float.compare(that.textX, textX) != 0) return false;
        if (Float.compare(that.textY, textY) != 0) return false;
        if (Float.compare(that.textW, textW) != 0) return false;
        if (animationType != that.animationType) return false;
        if (Float.compare(that.textH, textH) != 0) return false;
        if (isDownloading != that.isDownloading) return false;
        if (imageName != null ? !imageName.equals(that.imageName) : that.imageName != null) return false;
        if (thumbnail != null ? !thumbnail.equals(that.thumbnail) : that.thumbnail != null) return false;
        if (fontName != null ? !fontName.equals(that.fontName) : that.fontName != null) return false;
        return textFontPath != null ? textFontPath.equals(that.textFontPath) : that.textFontPath == null;
    }

    @Override
    public int hashCode() {
        int result = imageName != null ? imageName.hashCode() : 0;
        result = 31 * result + (imageWidth != +0.0f ? Float.floatToIntBits(imageWidth) : 0);
        result = 31 * result + (imageHeight != +0.0f ? Float.floatToIntBits(imageHeight) : 0);
        result = 31 * result + (textX != +0.0f ? Float.floatToIntBits(textX) : 0);
        result = 31 * result + (textY != +0.0f ? Float.floatToIntBits(textY) : 0);
        result = 31 * result + (textW != +0.0f ? Float.floatToIntBits(textW) : 0);
        result = 31 * result + animationType;
        result = 31 * result + (textH != +0.0f ? Float.floatToIntBits(textH) : 0);
        result = 31 * result + (thumbnail != null ? thumbnail.hashCode() : 0);
        result = 31 * result + (fontName != null ? fontName.hashCode() : 0);
        result = 31 * result + (isDownloading ? 1 : 0);
        result = 31 * result + (textFontPath != null ? textFontPath.hashCode() : 0);
        return result;
    }
//    @Override
//    public String toString()
//    {
//        return "{" + "imageName='" + imageName + '\'' + ", imageWidth=" + imageWidth + ", " +
//                "imageHeight=" + imageHeight + ", textX=" + textX + ", textY=" + textY + ", textW=" + textW + ", " +
//                "textH=" + textH + ", thumbnail='" + thumbnail + '\'' + ", fontName='" + fontName + '\'' + ", " +
//                "styleModel=" + styleModel + ", isDownloading=" + isDownloading + ", textFontPath='" + textFontPath + '\'' + '}';
//    }
}
