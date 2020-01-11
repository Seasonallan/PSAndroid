package com.biaoqing.library.diy.ui.view.scale;

import android.graphics.Canvas;



/**
 * Disc: 用于合成的统一操作
 *
// * 继承这个接口的类有：
// * @see TextStyleView 文字图层
// * @see LayerImageView 静图图层，包含涂鸦
// * @see GifMovieView gif动图图层
// * @see GifFrameView gif动图图层，只有在GifMovieView解析失败的情况下会用
// * @see GifWebpView webp动图图层
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public interface IScaleView {
    /**
     * 获取显示的宽度
     * @return
     */
    int getViewWidth();

    /**
     * 湖区显示的高度
     * @return
     */
    int getViewHeight();

    /**
     * 获取动画的时长,单位毫秒
     * @return
     */
    int getDuration();

    /**
     * 获取动画每一帧的延迟
     * @return
     */
    int getDelay();

    /**
     * 开始合成，用于重置动画到第一帧和重置标志位
     */
    void startRecord();

    /**
     * 合成第几帧，通过时间算出显示的是第几帧
     * @param time
     */
    void recordFrame(int time);

    /**
     * 合成结束，重置标志位
     */
    void stopRecord();

    /**
     * 释放内存
     */
    void onRelease();

    /**
     * 是否正在绘制，当前合成模式没有用到这个方法
     * @return
     */
    @Deprecated
    boolean isSeeking();

    /**
     * 合成的时候绘制，之前是使用draw然后调用View的onDraw，由于锯齿问题，现在的合成是直接调用onDraw。
     * @param canvas
     */
    void drawCanvas(Canvas canvas);
}
