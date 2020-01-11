package com.season.lib.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;

import com.season.lib.scale.ScaleDetector;

/**
 * Disc: 裁剪伪工厂模型父类
 * 其子类有
 * @see CropRectView 图形裁剪
 * @see CropPathView 多边形点击裁剪
 * @see CropPathFreeView 自由裁剪
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 16:44
 */
public class CropTool {

    protected Context context;
    protected int width, height;
    CropTool(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;
    }

    /**
     * 绘制内容
     * @param canvas
     */
    public void onDraw(Canvas canvas) {
    }

    /**
     * 用于拦截 图形裁剪的操作框区域操作
     * @param downEvent
     * @param currentEvent
     * @param distanceX
     * @param distanceY
     * @return
     */
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
        return false;
    }

    /**
     * 事件开始，标志位重置
     * @param ev
     */
    public void onTouchDown(MotionEvent ev) {
    }

    /**
     * 事件结束，添加操作记录，用于回撤重做
     * @param ev
     */
    public void onTouchUp(MotionEvent ev) {
    }


    /**
     * 是否可以重做
     * @return
     */
    public boolean canPro(){
        return false;
    }

    /**
     * 是否可以回撤
     * @return
     */
    public boolean canPre(){
        return false;
    }

    /**
     * 重做
     */
    public void redo(){

    }

    /**
     * 撤销操作
     */
    public void undo(){

    }

    /**
     * 是否正在画布上移动，用于非图片裁剪模式
     * @return
     */
    public boolean isMove() {
        return false;
    }

    /**
     * 是否正在操作框操作
     * @return
     */
    public boolean isOperation() {
        return false;
    }

    /**
     * 获取最终裁剪图片
     * @param bitmap
     * @param mViewMatrix
     * @param filePath
     */
    public void getCropImage(Bitmap bitmap, Matrix mViewMatrix, String filePath) {

    }

    /**
     * 释放内存
     */
    public void release() {
    }

    /**
     * 多指放大缩小操作拦截
     * @param detector
     * @return
     */
    public boolean onScale(ScaleDetector detector) {
        return false;
    }

    /**
     * 单机的时候，添加一条路径，用于多边形裁剪
     * @param e
     */
    public void onPathAdd(MotionEvent e) {
        
    }

    /**
     * 是否可以进行裁剪
     * @return
     */
    public boolean canCropBitmap() {
        return true;
    }


    /**
     * 是否可以返回，多边形点裁剪需要
     * @return
     */
    public boolean canBack() {
        return true;
    }
}
