package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.season.lib.bean.LayerBackground;
import com.season.lib.bean.LayerItem;
import com.season.lib.bean.LayerEntity;
import com.season.lib.util.Constant;
import com.season.lib.file.FileManager;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.gif.GifMaker;
import com.season.lib.util.PsUtil;
import com.season.lib.bitmap.AreaAveragingScale;
import com.season.lib.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Disc: 制图主容器(画布)， 功能1、循环线程RefreshRecordThread 控制子View(GIF,WEBP,文字动效)的刷新和最终的GIF合成
 * 2、记录行为数组用于操纵用户操作记录，用于回撤和重做操作
 * <p>
 * 合成流程说明：       图片帧数<=1则直接生成一个PNG文件， 否则则是循环绘制每一帧（通过先绘制背景信息，然后绘制图层信息，没背景则对图片进行裁剪），
 *
 * @see GifMaker       绘制成Bitmap后添加到GifMaker之中，同时多个线程解析bitmap信息。最终生成Gif文件
 * @see PSLayer 标注：它的一级子View都是ScaleView，代表图层 代表图层 代表图层 ,重要的事情说三遍，ScaleView是图层，ContainerView是画布
 * 标注2：当前合成方式是直接绘制360大小的图片，无需裁剪， 没背景信息的时候也是直接绘制如320大小的图片，无需裁剪，只是对画布进行操作而已。
 * <p>
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class PSCanvas extends RelativeLayout{
    private String TAG = "ContainerView,";
    private long startTime;//合成开始时间，用于超时判断，超时则中断合成并提示合成失败
    private int offsetX = 0, offsetY = 0;//画布边界和屏幕的左边和顶部的距离
    public int videoWidthHeight;//正中画布的宽高

    public boolean isMakingGifOrNot = false; //是否正在合成
    private PSLayer focusView;//当前获取焦点拥有操作框的图层

    private GifMaker mGifMaker;//Gif合成功能类
    private ILayer relyView;//Gif合成需要依赖的图层，没有视频或者Gif的情况下，找出所有图层中时长duration最长的图层作为relyView.合成的duration，delay等以relyView为基准。
    private boolean isFullScreen = false;//合成是否是全屏，不是全屏的情况下需要剪切最后的图片
    private float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = Float.MIN_VALUE, bottom = Float.MIN_VALUE;
    private int makeType = 1; //1是默认静图720 动图360   2是微信分享静图300 动图240   3是本地，文件保存地址修正
    private int finalGifWidthHeight = 480;
    public static final int WEIXIN = 2;
    public static final int LOCAL = 3;
    public static final int ONLY_LAYER = 4;//不绘制底图，只绘制图层
    int maxCount = 100;
    int resortCount = 1;//由于数量太多缩小合成的数量倍数
    private boolean isPreview;
    private GifMaker.OnGifMakerListener onGifMakerListener;
    int position = -1;
    //所有的操作列表
    List<Operate> list = new ArrayList<>();
    //背景控制
    public PSBackground backgroundView;
    private List<Bitmap> bitmapListVideoToGif;

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    //获取当前画布的截图，用于涂鸦中的马赛克功能
    public Bitmap getCacheBitmap() {
        Bitmap bitmap;
        bitmap = backgroundView.currentOperate.bitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            Bitmap bitmapResult = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
            bitmap = scaleBitmap(bitmap, getWidth(), getHeight());
            Canvas canvas = new Canvas(bitmapResult);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, (getWidth() - bitmap.getWidth()) / 2, (getHeight() - bitmap.getHeight()) / 2, null);
            return bitmapResult;
        }
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        if (backgroundView.currentOperate.color == Color.TRANSPARENT) {
            canvas.drawColor(Color.WHITE);
        } else {
            canvas.drawColor(backgroundView.currentOperate.color);
        }
        draw(canvas);
        return bitmap;
    }


    /**
     * 开始合成GIF
     *
     * @param makeType //1是默认静图720 动图360   2是微信分享静图300 动图240   3是本地，文件保存地址修正
     * @param listener 合成监听器
     */
    public void start(int makeType, final GifMaker.OnGifMakerListener listener) {
        if (isMakingGifOrNot) {
            return;
        }
        this.onGifMakerListener = listener;
        this.makeType = makeType;
        // deleteFocus();
        if (PSCanvas.this != null) {
            PSCanvas.this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startDelay(listener);
                }
            }, 300);
        }
    }



    public boolean hasGif() {
        for (int i = 0; i < getChildCount(); i++) {
            View scaleView = getChildAt(i);
            if (scaleView instanceof PSLayer && ((PSLayer) scaleView).getChildCount() > 0) {
                View view = ((PSLayer) scaleView).getChildAt(0);
                if (view != null) {
                    if (view instanceof CustomGifMovie) {
                        String filePath = ((CustomGifMovie) view).file;
                        Logger.d("hasGif1:" + filePath);
                        if (TextUtils.isEmpty(filePath)) {
                            continue;
                        }
                        String fileType = PsUtil.getFileType(filePath);
                        if (PsUtil.isGif(fileType)) {
                            return true;
                        }
                    }
                    if (view instanceof CustomGifFrame) {
                        String filePath = ((CustomGifFrame) view).file;
                        Logger.d("hasGif2:" + filePath);
                        if (TextUtils.isEmpty(filePath)) {
                            continue;
                        }
                        String fileType = PsUtil.getFileType(filePath);
                        if (PsUtil.isGif(fileType)) {
                            return true;
                        }
                    }
                    //TODO WEB?
                }
            }
            return false;
        }
        return false;
    }

    void startDelay(GifMaker.OnGifMakerListener listener) {
        if (mGifMaker != null) {
            mGifMaker.reset();
            mGifMaker = null;
        }
        //遍历图层，判断有没有动态图层
        boolean LayerhasGif = LayerhasGif();
//        boolean hasLayer = getChildCount() > 0;
        Logger.d("LayerhasGif:" + LayerhasGif);
        relyView = backgroundView.getBackgroundView();
        if (false) {//背景是视频,且是gif图层或者没有图层

        } else {//背景不是视频，找到时长最长的那个图层
            isFullScreen = backgroundView.isBackgroundImageViewVisible();//背景是图片的情况下，合成的是全屏的
            left = videoWidthHeight + offsetX;
            top = videoWidthHeight + offsetY;
            right = offsetX;
            bottom = offsetY;

            if (backgroundView.isGif()) {
                //如果是Gif图层，以底图Gif为relyView
                relyView = backgroundView.getGifView();
            } else {
                for (int i = 0; i < getChildCount(); i++) {
                    View scaleView = getChildAt(i);
                    if (scaleView instanceof PSLayer && ((PSLayer) scaleView).getChildCount() > 0) {
                        //((ScaleView) scaleView).startRecord();
                        View view = ((PSLayer) scaleView).getChildAt(0);
                        if (!isFullScreen) {//不是全屏，需要定位上下左右用于剪切
                            float[] points = ((PSLayer) scaleView).mPSOpView.desPoints;
                            checkPoint(points[0], points[1]);
                            checkPoint(points[2], points[3]);
                            checkPoint(points[4], points[5]);
                            checkPoint(points[6], points[7]);
                        }
                        if (view instanceof ILayer) {//找到时长最长的那个图层
                            if (relyView == null) {
                                relyView = (ILayer) view;
                            }
                            int duration = ((ILayer) view).getDuration();
                            if (relyView.getDuration() < duration) {
                                relyView = (ILayer) view;
                            }
                        }
                    }
                }
            }

            if (isFullScreen) {
                left = offsetX;
                right = videoWidthHeight + offsetX;
                top = offsetY;
                bottom = videoWidthHeight + offsetY;
            } else {//边界判定
                left = Math.max(offsetX, left);
                right = Math.min(right, videoWidthHeight + offsetX);
                top = Math.max(offsetY, top);
                bottom = Math.min(bottom, videoWidthHeight + offsetY);
            }
            if (relyView == null || relyView.getDuration() <= 0) {//图层最长时长为0表示静图，直接合成一张PNG图片
                get1PngImage(listener);
                return;
            }
            startGifMaker(listener, false);
        }
    }

    public boolean LayerhasGif() {
        for (int i = 0; i < getChildCount(); i++) {
            View scaleView = getChildAt(i);
            if (scaleView instanceof PSLayer) {
                View view = ((PSLayer) scaleView).getChildAt(0);
                if (view instanceof CustomTextView) {
                    if (((CustomTextView) view).animationProvider != null) {
                        return true;
                    }
                } else if ((view instanceof CustomGifMovie) || (view instanceof CustomGifFrame)) {
                    return true;
                }
            }
        }
        return false;
    }


    //启动合成GIF
    private void startGifMaker(GifMaker.OnGifMakerListener listener, boolean isVideo) {
        File outFile;
        if (makeType == LOCAL) {
            outFile = FileManager.getShareLocalFile(".gif");
        } else {
            outFile = FileManager.getDiyFile(getContext(), ".gif");
        }
        if (outFile == null) {
            listener.onMakeGifFail();
            return;
        }
        String absolutePath = outFile.getAbsolutePath();
        int duration = relyView.getDuration();
        int delay = relyView.getDelay();
        int count = 1;
        /**
         * 根据总时长和间隔时间，确定出总帧数
         * Gifmaker中添加的帧数等于总帧数，就结束添加帧了。
         */
        if (delay != 0) {
            count = duration / delay;
            if (duration % delay != 0) {
                count++;
            }
            //fix 部分gif 只显示一帧
            if (count == 1 && duration < delay) {
                count = 2;
            }

        }
        if (!isVideo) {
            if (count <= 1) {//校验，如果duration小于delay的话，表示只有一帧，直接合成PNG图片，跳过
                get1PngImage(listener);
                return;
            }
        }
        //不是视频的情况下，有长gif的情况下：
        if (count > maxCount) {
            resortCount = count / maxCount;
            resortCount++;
        }

        makeSize(true);
        relyView.startRecord();
        int repeatCount = 1;
//        if (relyView.isRepeat()) {
        if (relyView.getDuration() == 0) {
            listener.onMakeGifFail();
            return;
        }
        repeatCount = duration / relyView.getDuration();
        if (duration % relyView.getDuration() > 0) {
            repeatCount++;
        }
        long maxTime = 0;
        if (makeType == PSCanvas.ONLY_LAYER && isVideo) {
            repeatCount = 1;
            //relyView不是视频，是最长的图层，如果图层时间比视频时长还长，那就用视频的长度
            for (int i = 0; i < getChildCount(); i++) {
                View scaleView = getChildAt(i);
                if (scaleView instanceof PSLayer && ((PSLayer) scaleView).getChildCount() > 0) {
                    //((ScaleView) scaleView).startRecord();
                    View view = ((PSLayer) scaleView).getChildAt(0);
                    if (view instanceof ILayer) {//找到时长最长的那个图层
                        int layerduration = ((ILayer) view).getDuration();
                        if (maxTime < layerduration) {
                            //找出最长的图层的时长
                            maxTime = layerduration;
                        }
                    }
                }
            }
            if (relyView.getDuration() < maxTime) {
                maxTime = relyView.getDuration();
            }
            if (delay != 0) {
                count = (int) (maxTime / delay);
                if (duration % delay != 0) {
                    count++;
                }
            }

        }
        //TODO 120毫秒对于文字动画有点卡顿，但是缩小帧间隔，意味着要画更多的帧，这时候我们要绘制的是480*480分辨率的argb8888的bitmap，
        //TODO 内存压力比较大， 如果平衡内存的问题 是否可以对文字类采用类似videoview 画完一帧，再seek到下一帧进行绘制？
       // Logger.t(TAG).e("diy_gifmake_makeType:" + makeType + ",duration:" + duration + ",delay" + delay + ",count:" + count);
       // Logger.t(TAG).e("repeatCount=" + repeatCount + "," + "resortCount:" + resortCount + ",maxTime:" + maxTime);
        //确定合成总帧数和帧与帧之间的延迟时间
        mGifMaker = new GifMaker(count / resortCount, delay * resortCount, Executors.newCachedThreadPool()).setOutputPath
                (absolutePath);
        if (makeType == PSCanvas.ONLY_LAYER) {
            //4 only gif
            mGifMaker.setRepeatCount(repeatCount);
        }
        if (mGifMaker != null) {
            if (mGifMaker.isGifMaded) {
                listener.onMakeGifSucceed(mGifMaker.mOutputPath);
            } else {
                mGifMaker.setGifMakerListener(listener);
            }
        }
        isMakingGifOrNot = true;//启动标志位，开始合成
        startTime = System.currentTimeMillis();
    }

    //启动合成GIF

    //确定合成最终的尺寸
    void makeSize(boolean isGif) {
        if (isGif) {
            finalGifWidthHeight = makeType == WEIXIN ? Constant.SHARE_WECHAT_GIF_RESOLUTION : ((makeType ==
                    PSCanvas.ONLY_LAYER) ? Constant.IDEAL_VIDEO_RESOLUTION : Constant.IDEAL_GIF_RESOLUTION);//正好gif分辨率和照片分辨率相等
        } else {
            finalGifWidthHeight = makeType == WEIXIN ? Constant.PHOTO_RESOLUTION_WECHAT_SHARE : Constant
                    .PHOTO_RESOLUTION;
        }
    }

    //合成一张PNG图片
    private void get1PngImage(final GifMaker.OnGifMakerListener listener) {
        makeSize(false);
        PSCanvas.this.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (backgroundView != null && backgroundView.currentOperate != null) {
                    if (backgroundView.currentOperate.visible2 == View.VISIBLE) {
                        //背景是图片，绘制bitmap
//                        Bitmap bitmap = backgroundView.currentOperate.bitmap;
                        Bitmap bitmap = getImageBitmap();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            Bitmap tBitmap = Bitmap.createBitmap(finalGifWidthHeight, finalGifWidthHeight, Bitmap.Config.RGB_565);
                            Canvas canvas = new Canvas(tBitmap);
                            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                            canvas.drawBitmap(bitmap, null, new RectF(0, 0, finalGifWidthHeight, finalGifWidthHeight), null);
                            drawItem(canvas, videoWidthHeight, offsetX, offsetY);

                            //overlay
                            saveAndNotify(tBitmap, listener);
                            return;
                        } else {
                           // ProgressDialogUtils.Cancel();
                        }
                    } else if (backgroundView.currentOperate.color != Color.TRANSPARENT) {//背景是纯色，绘制颜色
                        float realShowWidth = right - left;
                        float realShowHeight = bottom - top;
                        float height = realShowHeight * finalGifWidthHeight / realShowWidth;
                        Bitmap tBitmap = Bitmap.createBitmap(finalGifWidthHeight, (int) height, Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(tBitmap);
                        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                        canvas.drawColor(backgroundView.currentOperate.color);
                        drawItem(canvas, realShowWidth, left, top);
                        saveAndNotify(tBitmap, listener);
                        return;
                    }
                }
                //背景是透明，需要带透明的bitmap Bitmap.Config.ARGB_88881
                float realShowWidth = right - left;
                float realShowHeight = bottom - top;
                float height = realShowHeight * finalGifWidthHeight / realShowWidth;
                Bitmap tBitmap = Bitmap.createBitmap(finalGifWidthHeight, (int) height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(tBitmap);
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                drawItem(canvas, realShowWidth, left, top);
                saveAndNotify(tBitmap, listener);
            }
        }, 300);
    }

    private Bitmap getImageBitmap() {
        return backgroundView.currentOperate.bitmap;
    }


    //保存PNG图片，监听器回调
    private void saveAndNotify(Bitmap bitmap, GifMaker.OnGifMakerListener listener) {
        File file;
        if (makeType == LOCAL) {
            file = FileManager.getShareLocalFile(".png");
        } else {
            file = FileManager.getDiyFile(getContext(), ".png");
        }
        String filePath = PsUtil.saveBitmap(file, bitmap);
        if (filePath == null) {
            listener.onMakeGifFail();
            return;
        }
        listener.onMakeGifSucceed(filePath);
    }


    //合成完成，重置标志位
    private void onRecordFinish() {
        isMakingGifOrNot = false;
        if (relyView instanceof CustomGifMovie || relyView instanceof CustomGifFrame) {
            relyView.stopRecord();
        }
        recordViewFinish();
        if (bitmapListVideoToGif != null) {
            bitmapListVideoToGif = null;
        }
    }

    //获取剪切上下左右位置
    private void checkPoint(float x, float y) {
        if (Float.isNaN(x) || Float.isNaN(y)) {
            return;
        }
        left = Math.min(left, x);
        right = Math.max(right, x);
        top = Math.min(top, y);
        bottom = Math.max(bottom, y);
    }

    //移除获取焦点的图层的焦点
    public void deleteFocus() {
        if (focusView != null) {
            focusView.removeFocus();
            focusView = null;
        }
    }

    //获取获取焦点的图层的图层位置
    public int getViewIndex() {
        if (focusView == null) {
            return -1;
        }
        return indexOfChild(focusView);
    }

    //中断合成，当前没什么用
    public void stop() {
        mGifMaker = null;
    }

    public void stopThread() {
        if (refreshThread != null) {
            refreshThread.interrupt();
        }
    }

    //获取获取焦点的图层下的视图信息
    public View getFocusView() {
        if (running != 1) {
            return null;
        }
        if (focusView != null) {
            return focusView.getChildAt(0);
        }
        return focusView;
    }

    //获取获取焦点的图层
    public PSLayer getView() {
        return focusView;
    }

    public PSCanvas(Context context) {
        super(context);
        init();
    }

    public PSCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PSCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    int running = -1;

    @Override
    public void dispatchWindowVisibilityChanged(int visibility) {
        if (visibility == GONE || visibility == INVISIBLE) {
            running = 0;
        } else if (visibility == VISIBLE) {
            running = 1;
        }
        super.dispatchWindowVisibilityChanged(visibility);
    }

    private void refreshView() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                ((PSLayer) view).refresh();
            }
        }
    }

    private void recordView(int time) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                ((PSLayer) view).record(time);
            }
        }
    }

    private void recordViewFinish() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                ((PSLayer) view).recordFinish();
            }
        }
    }

    private void delay(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isDrawing = false;

    private void drawGif() {
        try {
            //一种是Gif作为底图，或者是静态图作为底图，图层是动态的Gif
            if (backgroundView == null) {
                if (onGifMakerListener != null) {
                    onGifMakerListener.onMakeGifFail();
                }
                return;
            }
            int width = backgroundView.getGifWidth();
            int height = backgroundView.getGifHeight();
            if (width == 0 || height == 0) {
                return;
            }
            isDrawing = true;
            Bitmap tBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(tBitmap);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

            canvas.save();
            Matrix matrix = new Matrix();
            float scale = 1f;
            /**
             * 必须用Gifmoveie的layoutParams做缩放比例
             */
            if (width >= height) {
                scale = width * 1f / videoWidthHeight * 1.0f;
            } else {
                scale = height * 1f / videoWidthHeight * 1.0f;
            }
            matrix.postScale(scale, scale);
            canvas.concat(matrix);
            relyView.drawCanvas(canvas);
            canvas.restore();
            drawItem4GIf(canvas, width, height, offsetX, offsetY, scale);
            mGifMaker.addBitmap(tBitmap);
            isDrawing = false;
        } catch (Exception e) {
            //PAD必须在主线程中调用TextureView.getBitmap
            //否则出现错误java.lang.IllegalStateException: Hardware acceleration can only be used url a single UI thread.
            e.printStackTrace();
            Logger.d("drawGIf_e:" + e.toString());
            if (onGifMakerListener != null) {
                onGifMakerListener.onMakeGifFail();
            }
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            while (isDrawing) {
                Logger.d("drawGIf delay");
                delay(10);
            }
        }
    }

    private void drawItem(Canvas canvas, float showWidth, float left, float top) {
        float scale = finalGifWidthHeight * 1.0f / showWidth;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                PSLayer PSLayer = (PSLayer) view;
                Matrix matrix = new Matrix();
                matrix.set(PSLayer.mCurrentMatrix);
                matrix.postTranslate(-left, -top);
                matrix.postScale(scale, scale, 0, 0);
                //matrix.postTranslate(-left * scale, -top * scale);
                canvas.save();
                canvas.concat(matrix);
                if (PSLayer.getChildCount() > 0) {
                    View childView = PSLayer.getChildAt(0);
                    if (childView instanceof ILayer) {
                        ((ILayer) childView).drawCanvas(canvas);
                    }
                }
                canvas.restore();
            }
        }
    }

    private void drawItem4GIf(Canvas canvas, float width, float height, float left, float top, float scale) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                PSLayer PSLayer = (PSLayer) view;
                Matrix matrix = new Matrix();
                matrix.set(PSLayer.mCurrentMatrix);
                matrix.postTranslate(-left, -top);
                matrix.postScale(scale, scale, 0, 0);
                canvas.save();
                canvas.concat(matrix);
                if (PSLayer.getChildCount() > 0) {
                    View childView = PSLayer.getChildAt(0);
                    if (childView instanceof ILayer) {
                        ((ILayer) childView).drawCanvas(canvas);
                    }
                }
                canvas.restore();
            }
        }
    }

    class RefreshRecordThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (running < 0) {
                    return;
                }
                if (running == 1 && !isMakingGifOrNot) {
                    refreshView();
                    refreshGifBg();
                }
                if (running == 1 && isMakingGifOrNot) {
                    Logger.d("cur:" + System.currentTimeMillis());
                    if (mGifMaker != null && !mGifMaker.isBitmapFull()) {
                        //最大合成时间控制
                        if (System.currentTimeMillis() - startTime > mGifMaker.getTotalSize() * 1000) {
                            onRecordFinish();
                            if (mGifMaker != null && mGifMaker.mOnGifMakerListener != null) {
                                if (PSCanvas.this != null) {
                                    PSCanvas.this.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGifMaker.mOnGifMakerListener.onMakeGifFail();
                                            mGifMaker.reset();
                                        }
                                    }, 300);
                                }
                            }
                        } else if (backgroundView != null && backgroundView.isGif()) {
                            int time = mGifMaker.getFrameCountNow() * relyView.getDelay() * resortCount;

                            relyView.recordFrame(time);
                            recordView(time);
                            drawGif();
                        } else {
                            int time = mGifMaker.getFrameCountNow() * relyView.getDelay() * resortCount;
                            recordView(time);
                            Bitmap tBitmap = null;
                            if (backgroundView.currentOperate != null) {
                                //图片
                                if (backgroundView.currentOperate.visible2 == View.VISIBLE) {
                                    Bitmap bitmap = getImageBitmap();
                                    if (bitmap != null && !bitmap.isRecycled()) {
                                        tBitmap = Bitmap.createBitmap(finalGifWidthHeight, finalGifWidthHeight, Bitmap.Config.RGB_565);
                                        Canvas canvas = new Canvas(tBitmap);
                                        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                                                .FILTER_BITMAP_FLAG));
                                        canvas.drawBitmap(bitmap, null, new RectF(0, 0, finalGifWidthHeight, finalGifWidthHeight),
                                                null);
                                        drawItem(canvas, videoWidthHeight, offsetX, offsetY);
                                    }
                                } else if (backgroundView.currentOperate.color != Color.TRANSPARENT) {
                                    //颜色
                                    float realShowWidth = right - left;
                                    float realShowHeight = bottom - top;
                                    float height = realShowHeight * finalGifWidthHeight / realShowWidth;
                                    tBitmap = Bitmap.createBitmap(finalGifWidthHeight, (int) height, Bitmap.Config.RGB_565);
                                    Canvas canvas = new Canvas(tBitmap);
                                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                                            .FILTER_BITMAP_FLAG));
                                    canvas.drawColor(backgroundView.currentOperate.color);//画上背景色，黑色或者白色
                                    drawItem(canvas, realShowWidth, left, top);
                                }
                            }

                            if (tBitmap == null) {
                                //透明
                                float realShowWidth = right - left;
                                float realShowHeight = bottom - top;
                                float height = realShowHeight * finalGifWidthHeight / realShowWidth;
                                try {
                                    tBitmap = Bitmap.createBitmap(finalGifWidthHeight, (int) height, Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(tBitmap);
                                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                                            .FILTER_BITMAP_FLAG));
                                    drawItem(canvas, realShowWidth, left, top);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            /**
                             * 为底图是静态图或者透明层，做GIF图
                             */
                            if (mGifMaker != null) mGifMaker.addBitmap(tBitmap);
                        }
                    } else {
                        onRecordFinish();
                        delay(10);
                    }
                } else {
                    delay(10);
                }
            }
        }
    }

    private void refreshGifBg() {
        if (backgroundView != null) {
            backgroundView.refreshGifbg();
        }
    }

    //    Handler handler;
    Thread refreshThread;

    //初始化，启动线程
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        videoWidthHeight = ScreenUtils.getScreenWidth(getContext());//正中画布的宽高
        running = 1;
        refreshThread = new RefreshRecordThread();
        refreshThread.start();
//        handler = new Handler();
        // mHandler.sendEmptyMessageDelayed(0, 10);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (child instanceof PSLayer) {//添加图层的时候，居中图层
            boolean callback = ((PSLayer) child).initViewOffset(getWidth(), getHeight());//居中图层
            startOp((PSLayer) child, true, callback);//给图层焦点
            ((PSLayer) child).getFocus();//图层标志位重置
            isEventAttaching = false;//多点触控标志位重置
            ((PSLayer) child).setClickListener(new PSLayer.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onClick(view);
                    }
                }

                @Override
                public void onDoubleClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onDoubleClick(view);
                    }
                }

                @Override
                public void onDelete(View view) {
                    deleteView((PSLayer) view);
                }
            });
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        if (view == focusView) {//移除焦点图层的时候需要重置为null
            focusView = null;
        }
    }


    private PSLayer.OnClickListener mClickListener;

    public void setClickListener(PSLayer.OnClickListener listener) {
        mClickListener = listener;
    }

    //重置所有图层，清空
    public void reset() {
        Operate oprate = new Operate(IType.RESET);
        int count = getChildCount();
        oprate.childs = new View[count];
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof PSLayer) {
                oprate.childs[i] = child;
            }
        }
        for (int i = 0; i < oprate.childs.length; i++) {
            if (oprate.childs[i] != null) removeView(oprate.childs[i]);
        }
        addEvent(oprate);
        backgroundView.reset();
        //removeAllViews();
    }

    //移动图层的时候，图层会调用该方法添加一条操作
    public void onMove(PSLayer view, MotionEvent event) {
        if (moveListener != null) {
            boolean isInDelRect = moveListener.onMove(event);
            view.setDelPosition(isInDelRect);
        }
    }

    //图层移动结束，需要隐藏拖动删除垃圾箱，并且重置多点标志位
    public void onMoveEnd() {
        if (moveListener != null) {
            moveListener.onMoveEnd();
        }
        isEventAttaching = false;
    }

    private IOnMoveListener moveListener;
    public interface IOnMoveListener{
        boolean onMove(MotionEvent event);
        void onMoveEnd();
    }

    public void setOnMoveListener(IOnMoveListener listener){
        this.moveListener = listener;
    }

    /**
     * 图层操作类型
     */
    public interface IType {
        int RESET = 0;
        int ADD = 1;
        int REMOVE = 2;
        int MOVE = 3;
        int UP_LAYER = 4;
        int DOWN_LAYER = 5;

        int BACKGROUND = 6;
        int TEXT_OBJECT = 7;
    }

    /**
     * 图层操作记录
     */
    class Operate {
        public int type;
        public PSLayer PSLayer;
        public CustomTextView textView;
        public View[] childs;
        public float[] matrix;

        public Operate(int op, PSLayer PSLayer, Matrix ma) {
            matrix = new float[9];
            if (ma != null) {
                ma.getValues(matrix);
            }
            type = op;
            this.PSLayer = PSLayer;
        }

        public Operate(int op) {
            this.type = op;
        }

        public Operate(CustomTextView textView) {
            this.type = IType.TEXT_OBJECT;
            this.textView = textView;
        }
    }

    //删除图层
    public void deleteView(PSLayer view) {
        if (view.getParent() != null) {
            addEvent(IType.REMOVE, view, view.mCurrentMatrix);
            removeView(view);
        }
    }

    /**
     * 图层焦点监听器
     */
    public interface IFocusChangeListener {
        /**
         * 图层焦点失去
         *
         * @param view 失去焦点的图层
         */
        void onFocusLose(ViewGroup view);

        /**
         * 某一个图层获取到焦点
         *
         * @param view 获取焦点的图层
         */
        void onFocusGet(ViewGroup view);

        /**
         * 所有图层的焦点都没有拿到
         */
        void onFocusClear();
    }

    private IFocusChangeListener mOnFocusChangeListener;

    //设置图层焦点监听器
    public void setFocusChangeListener(IFocusChangeListener listener) {
        mOnFocusChangeListener = listener;
    }

    //焦点切换，触发监听器
    public void startOp(PSLayer view, boolean isFocus, boolean callback) {
        if (isFocus) {
            if (this.focusView != null && this.focusView != view) {
                this.focusView.removeFocus();
            }
            this.focusView = view;
            if (mOnFocusChangeListener != null && callback) {
                mOnFocusChangeListener.onFocusGet(view);
            }
            isEventAttaching = true;
        } else {
            if (view == this.focusView) {
                this.focusView.removeFocus();
                this.focusView = null;
                if (mOnFocusChangeListener != null && callback) {
                    mOnFocusChangeListener.onFocusLose(view);
                }
            }
            int index = indexOfChild(view);
            if (index == 0) {
                if (focusView == null && mOnFocusChangeListener != null && callback) {
                    mOnFocusChangeListener.onFocusClear();
                }
            }
        }
    }

    //多点的时候拦截全部down到第一点的焦点VIew
    public boolean isEventAttaching = false;

    public boolean isEventAttaching() {
        if (focusView == null) {
            return false;
        }
        return isEventAttaching;
    }

    //向上一层 图层
    public int upLayer(View view, boolean event) {
        if (view == null) {
            return -1;
        }
        int i = indexOfChild(view);
        if (i != -1) {
            if (i >= getChildCount() - 1) {
                return 0;
            } else {
                removeViewAt(i);
                addView(view, i + 1);
                if (event) addEvent(IType.UP_LAYER, view, null);
                return i - 1;
            }
        }
        return -1;
    }

    //图层向下一层
    public int downLayer(View view, boolean event) {
        if (view == null) {
            return -1;
        }
        int i = indexOfChild(view);
        if (i != -1) {
            if (i <= 0) {
                return 0;
            } else {
                removeViewAt(i);
                addView(view, i - 1);
                if (event) addEvent(IType.DOWN_LAYER, view, null);
                return i - 0;
            }
        }
        return -1;
    }

    //添加操作事件
    private void addEvent(Operate operate) {
        if (position < list.size() - 1) {
            for (int i = list.size() - 1; i > position; i--) {
                list.remove(i);
            }
        }
        list.add(operate);
        position = list.size() - 1;
    }

    //添加所有类型操作的事件
    public void addEvent(int type, View scaleView, Matrix matrix) {
        if (scaleView instanceof PSLayer) {
            addEvent(new Operate(type, (PSLayer) scaleView, matrix));
        }
    }

    //是否可以回撤
    public boolean canPre() {
        return position >= 0;
    }

    //是否可以重做
    public boolean canPro() {
        return position < list.size() - 1;
    }

    //获取上一个与当前一样的图层记录
    private int getPreScaleViewPosition(int position) {
        PSLayer PSLayer = list.get(position).PSLayer;
        for (int i = position - 1; i >= 0; i--) {
            if (list.get(i).PSLayer == PSLayer) {
                return i;
            }
        }
        return position;
    }

    //回撤操作
    public void pre() {
        switch (list.get(position).type) {
            case IType.MOVE:
                int pos = getPreScaleViewPosition(position);
                PSLayer PSLayer = list.get(pos).PSLayer;
                float[] data = list.get(pos).matrix;
                PSLayer.resetMatrix(data);
                break;
            case IType.ADD:
                removeView(list.get(position).PSLayer);
                break;
            case IType.REMOVE:
                int posPre = getPreScaleViewPosition(position);
                PSLayer PSLayerPre = list.get(position).PSLayer;
                float[] dataPre = list.get(posPre).matrix;
                PSLayerPre.resetMatrix(dataPre);
                int index = list.get(position).PSLayer.index;
                if (index == 0) {
                    addView(PSLayerPre, 0);
                } else {
                    addView(PSLayerPre);
                }
                break;
            case IType.DOWN_LAYER:
                upLayer(list.get(position).PSLayer, false);
                break;
            case IType.UP_LAYER:
                downLayer(list.get(position).PSLayer, false);
                break;
            case IType.RESET:
                View[] views = list.get(position).childs;
                for (int i = 0; i < views.length; i++) {
                    if (views[i] != null) addView(views[i]);
                }
                if (backgroundView.pre()) {
                    changeAnimationTime();
                }
                break;
            case IType.BACKGROUND:
                if (backgroundView.pre()) {
                    changeAnimationTime();
                }
                break;
            case IType.TEXT_OBJECT:
                CustomTextView textView = list.get(position).textView;
                textView.pre();
                break;
        }
        position--;
    }

    //重做操作
    public void pro() {
        position++;
        switch (list.get(position).type) {
            case IType.MOVE:
                PSLayer PSLayer = list.get(position).PSLayer;
                float[] data = list.get(position).matrix;
                PSLayer.resetMatrix(data);
                break;
            case IType.ADD:
                int index = list.get(position).PSLayer.index;
                if (index == 0) {
                    addView(list.get(position).PSLayer, 0);
                } else {
                    addView(list.get(position).PSLayer);
                }
                break;
            case IType.REMOVE:
                removeView(list.get(position).PSLayer);
                break;
            case IType.DOWN_LAYER:
                downLayer(list.get(position).PSLayer, false);
                break;
            case IType.UP_LAYER:
                upLayer(list.get(position).PSLayer, false);
                break;
            case IType.RESET:
                View[] views = list.get(position).childs;
                for (int i = 0; i < views.length; i++) {
                    if (views[i] != null) removeView(views[i]);
                }
                if (backgroundView.pro()) {
                    changeAnimationTime();
                }
                //removeAllViews();
                break;
            case IType.BACKGROUND:
                if (backgroundView.pro()) {
                    changeAnimationTime();
                }
                break;
            case IType.TEXT_OBJECT:
                CustomTextView textView = list.get(position).textView;
                textView.pro();
                break;
        }
    }


    //绑定背景控制视图
    public void bindBgView(View parentView) {
        backgroundView = new PSBackground(parentView);
    }

    public void showImage(String url, String path) {
        if (backgroundView != null) {
            if (backgroundView.showImage(url, path)) addEvent(new Operate(IType.BACKGROUND));
        }
        changeAnimationTime();
    }

    public void showGIf(String url, String path) {
        if (backgroundView != null) {
            if (backgroundView.showGIf(url, path, new PSBackground.decoderGifDoneListener() {
                @Override
                public void decoder(int offset_x, int offset_y) {
                    offsetX += offset_x;
                    offsetY += offset_y;
                }
            })) addEvent(new Operate(IType.BACKGROUND));
        }
        changeAnimationTime();
    }

    public void showBackground(int color) {
        if (backgroundView == null) {
            return;
        }
        if (backgroundView.showBackground(color)) addEvent(new Operate(IType.BACKGROUND));
        changeAnimationTime();
    }

    public void showVideoOrImage() {
        if (backgroundView == null) {
            return;
        }
        if (backgroundView.showVideoOrImage()) addEvent(new Operate(IType.BACKGROUND));
    }


    public void setTextAnimationType(int type) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            int duration = backgroundView.getDuration();
            if (textObjectView.setTextAnimationType(type, duration, 300, 10)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    /**
     * 改变所有文字动效的时间
     */
    public void changeAnimationTime() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof PSLayer) {
                PSLayer PSLayer = (PSLayer) view;
                if (PSLayer.getChildCount() > 0) {
                    View childView = PSLayer.getChildAt(0);
                    if (childView instanceof CustomTextView) {
                        ((CustomTextView) childView).changeAnimationTime(backgroundView.getDuration(), 10, backgroundView.getSpeed());
                    }
                }
            }
        }
    }

    public void setTextWidth(float size) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setPaintWidthByPercent(size)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setTextColor(String textColor) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setTextcolor(textColor)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setTextColor(int textColor) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView customTextView = (CustomTextView) view;
            if (customTextView.setTextcolor(textColor)) {
                addEvent(new Operate(customTextView));
            }
        }
    }

    public void setTextColorAlpha(int alpha) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setTextalpha(alpha)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void editText(String params) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView customTextView = (CustomTextView) view;
            customTextView.setFontHistory();//设置字体历史记录
            customTextView.editText(params);
            customTextView.requestLayout();
            addEvent(new Operate(customTextView));
        }
    }

    public void setStrokeSize(float size) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setStrokeWidthByPercent(size)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setStrokeColorAlpha(int alpha) {
        View view = getFocusView();
        if (view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setStrokealpha(alpha)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setStrokeColor(String color) {
        View view = getFocusView();
        if (view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setStrokecolor(color)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setStrokeColor(int color) {
        View view = getFocusView();
        if (view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setStrokecolor(color)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }

    public void setTypeFace(Typeface typeface) {
        View view = getFocusView();
        if (view != null && view instanceof CustomTextView) {
            CustomTextView textObjectView = (CustomTextView) view;
            if (textObjectView.setTexttypeface(typeface)) {
                addEvent(new Operate(textObjectView));
            }
        }
    }


    //获取当前的所有图层信息
    public LayerEntity getLayerMessage() {
        LayerEntity layerEntity = new LayerEntity();

        layerEntity.setWidth(getWidth());
        layerEntity.setHeight(getHeight());

        LayerBackground backInfoModel = new LayerBackground();
        PSBackground.BgOperate bgOperate = backgroundView.currentOperate;
        if (bgOperate != null) {
            if (bgOperate.visible1 == View.VISIBLE) {
                int bgColor = bgOperate.color;
                if (bgColor != Color.TRANSPARENT) {
                    backInfoModel.setBackColorString(PsUtil.getColorStr(bgColor));
                }
            } else if (bgOperate.visible2 == View.VISIBLE) {
                backInfoModel.imageURLPathFile = bgOperate.imageFile;
                backInfoModel.setImgURLPath(bgOperate.url);
            } else {
                backInfoModel.gifURLPathFile = bgOperate.gifFile;
                backInfoModel.setGifURLPath(bgOperate.url);
            }
        }
        layerEntity.setBackInfoModel(backInfoModel);

        List<LayerItem> list = new ArrayList<>();
        int layer = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View scaleView = getChildAt(i);
            if (scaleView instanceof PSLayer) {
                LayerItem layerItem = ((PSLayer) scaleView).getItemInfro(layer++);
                list.add(layerItem);
            }
        }
        layerEntity.setItemArray(list);
        return layerEntity;
    }


    //停止刷新，释放内存
    public void release() {
        running = -1;
        if (backgroundView != null) backgroundView.release();
        release(this);
        removeAllViews();
        stopThread();
    }

    private void release(ViewGroup parent) {
        for (int i = 0; i < list.size(); i++) {
            Operate view = list.get(i);
            if (view.PSLayer != null && view.PSLayer.getChildCount() > 0) {
                View childView = view.PSLayer.getChildAt(0);
                if (childView instanceof ILayer) {
                    ((ILayer) childView).onRelease();
                }
            }
            if (view.textView != null) {
                view.textView.onRelease();
            }
        }
        if (true) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof ViewGroup) {
                release((ViewGroup) view);
            } else {
                if (view instanceof ILayer) {
                    ((ILayer) view).onRelease();
                }
            }
        }
    }


    /**
     */
    public Bitmap scaleBitmap(Bitmap bitmap, float width, float height) {
        if (false) {
            AreaAveragingScale areaAveragingScale = new AreaAveragingScale(bitmap);
            Bitmap bitmapRes = areaAveragingScale.getScaledBitmap(width, height);
            return bitmapRes;
        }
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scale = width / w;
            float scale2 = height / h;
            scale = scale < scale2 ? scale : scale2;
            matrix.postScale(scale, scale);

            return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
