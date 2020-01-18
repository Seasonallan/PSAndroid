package com.season.lib.ps.bean;


import java.io.Serializable;


/**
 * Created by lizhongxin on 2018/2/23.
 */


public class LayerItem implements Serializable {
    public int animationType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LayerItem that = (LayerItem) o;

        if (Double.compare(that.xScale, xScale) != 0) return false;
        if (index != that.index) return false;
        if (vMoveBtnPositionType != that.vMoveBtnPositionType) return false;
        if (hMoveBtnPositionType != that.hMoveBtnPositionType) return false;
        if (Double.compare(that.centerY, centerY) != 0) return false;
        if (contentViewType != that.contentViewType) return false;
        if (turnOverH != that.turnOverH) return false;
        if (Double.compare(that.angle, angle) != 0) return false;
        if (turnOverV != that.turnOverV) return false;
        if (Double.compare(that.centerX, centerX) != 0) return false;
        if (Double.compare(that.sizeWidth, sizeWidth) != 0) return false;
        if (Double.compare(that.sizeHeight, sizeHeight) != 0) return false;
        if (Float.compare(that.textFontSize, textFontSize) != 0) return false;
        if (Double.compare(that.yScale, yScale) != 0) return false;
        if (textFontPath != null ? !textFontPath.equals(that.textFontPath) : that.textFontPath != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (imageURL != null ? !imageURL.equals(that.imageURL) : that.imageURL != null) return false;
        if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) return false;
        return textFontName != null ? textFontName.equals(that.textFontName) : that.textFontName == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(xScale);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + index;
        result = 31 * result + vMoveBtnPositionType;
        result = 31 * result + hMoveBtnPositionType;
        temp = Double.doubleToLongBits(centerY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + contentViewType;
        result = 31 * result + (turnOverH ? 1 : 0);
        temp = Double.doubleToLongBits(angle);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (textFontPath != null ? textFontPath.hashCode() : 0);
        result = 31 * result + (turnOverV ? 1 : 0);
        temp = Double.doubleToLongBits(centerX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (textFontName != null ? textFontName.hashCode() : 0);
        temp = Double.doubleToLongBits(sizeWidth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sizeHeight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (textFontSize != +0.0f ? Float.floatToIntBits(textFontSize) : 0);
        temp = Double.doubleToLongBits(yScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * xScale : 1.467921
     * index : 0
     * vMoveBtnPositionType : 2
     * hMoveBtnPositionType : 3
     * centerY : 385.25
     * contentViewType : 2
     * turnOverH : false
     * angle : 0.003607564
     * textFontPath : https: //img.biaoqing.com/font/HappyZcool.ttf
     * textStyleModel : {"textX":12.38154,"thumbnail":"yangshi0","styleModel":{"textColor":"ffe63f",
     * "strokeColor":"5842ff","textColorSize":0,"miaobianAlpha":1,"textAlpha":1,"strokeSize":0.75},
     * "textH":47.73438,"textW":160.96,"textY":3.671875,"thumbnailW":98,"fontName":"CN000015","imageName":"",
     * "imageWidth":185.7231,"imageHeight":55.07812}
     * turnOverV : false
     * centerX : 187.5
     * text : 抖起来！
     * imageURL :
     * textFontName : CN000015
     * sizeWidth : 185.7231
     * sizeHeight : 55.07812
     * textFontSize : 40
     * yScale : 1.467921
     */
    //        contentViewType:
    //        CKYCustomizing  ContentViewTypeImage  = 0,
    //        CKYCustomizing  ContentViewTypeLocaImage=1,
    //        CKYCustomizing  ContentViewTypeTextbox=2,
    //        CKYCustomizing  ContentViewTypeDraw=3,
    //        本地素材 绘图 要上传图片

    //        typedef NS_ENUM(NSInteger, CKYRotationViewButtonPositionType) {
    //            CKYRotationViewButtonPositionTypeTop  = 0,
    //                    CKYRotationViewButtonPositionTypeLeft= 1,
    //                    CKYRotationViewButtonPositionTypeBottom= 2,
    //                    CKYRotationViewButtonPositionTypeRight= 3，
    //        };
    private double xScale;
    private int index;
    private int vMoveBtnPositionType;
    private int hMoveBtnPositionType;
    private double centerY;
    private int contentViewType;
    private boolean turnOverH;
    private double angle;
    private String textFontPath = "";
    private boolean turnOverV;
    private double centerX;
    private String text = "";
    private String imageURL = "";
    public String filePath = "";
    private String textFontName = "";
    private double sizeWidth;
    private double sizeHeight;
    private float textFontSize;
    private double yScale;

    public double getXScale() {
        return xScale;
    }

    public void setXScale(double xScale) {
        this.xScale = xScale;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getVMoveBtnPositionType() {
        return vMoveBtnPositionType;
    }

    public void setVMoveBtnPositionType(int vMoveBtnPositionType) {
        this.vMoveBtnPositionType = vMoveBtnPositionType;
    }

    public int getHMoveBtnPositionType() {
        return hMoveBtnPositionType;
    }

    public void setHMoveBtnPositionType(int hMoveBtnPositionType) {
        this.hMoveBtnPositionType = hMoveBtnPositionType;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public int getContentViewType() {
        return contentViewType;
    }

    public void setContentViewType(int contentViewType) {
        this.contentViewType = contentViewType;
    }

    public boolean isTurnOverH() {
        return turnOverH;
    }

    public void setTurnOverH(boolean turnOverH) {
        this.turnOverH = turnOverH;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public String getTextFontPath() {
        return textFontPath;
    }

    public void setTextFontPath(String textFontPath) {
        this.textFontPath = textFontPath;
    }

    public boolean isTurnOverV() {
        return turnOverV;
    }

    public void setTurnOverV(boolean turnOverV) {
        this.turnOverV = turnOverV;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTextFontName() {
        return textFontName;
    }

    public void setTextFontName(String textFontName) {
        this.textFontName = textFontName;
    }

    public double getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(double sizeWidth) {
        this.sizeWidth = sizeWidth;
    }

    public double getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(double sizeHeight) {
        this.sizeHeight = sizeHeight;
    }

    public float getTextFontSize() {
        return textFontSize;
    }

    public void setTextFontSize(float textFontSize) {
        this.textFontSize = textFontSize;
    }

    public double getYScale() {
        return yScale;
    }

    public void setYScale(double yScale) {
        this.yScale = yScale;
    }

}
