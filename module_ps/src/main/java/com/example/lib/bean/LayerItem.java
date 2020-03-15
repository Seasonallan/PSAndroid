package com.example.lib.bean;


import java.io.Serializable;


public class LayerItem implements Serializable {

    public interface ILayerType {
        int ContentViewTypeImage = 0; //网络素材
        int ContentViewTypeLocaImage = 1;//本地素材
        int ContentViewTypeTextbox = 2;//文字
        int ContentViewTypeDraw = 3;//涂鸦，绘图
    }

    public int startTime;
    public int endTime;

    public int animationType;
    private double xScale;
    private int index;
    private double centerY;
    private int contentViewType;
    private boolean turnOverH;
    private double angle;
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
