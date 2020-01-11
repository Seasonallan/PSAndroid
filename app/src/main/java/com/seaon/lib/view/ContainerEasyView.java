package com.seaon.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.biaoqing.library.diy.ui.view.scale.IScaleView;
import com.example.myapplication.BuildConfig;
import com.seaon.lib.scale.ScaleView;
import com.biaoqing.library.diy.gifmaker.GifMaker;
import com.biaoqing.library.diy.gifmaker.utils.Util;
import com.orhanobut.logger.Logger;
import com.seaon.lib.util.Constant;
import com.seaon.lib.util.FileManager;
import com.seaon.lib.util.FileUtils;

import java.io.File;
import java.util.concurrent.Executors;

import static com.seaon.lib.view.ContainerView.LOCAL;
import static com.seaon.lib.view.ContainerView.ONLY_LAYER;
import static com.seaon.lib.view.ContainerView.WEIXIN;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-09-27 14:44
 */
public class ContainerEasyView extends RelativeLayout {


    public boolean autoStart = false;
    private GifMaker mGifMaker;
    private int makeType = 1; //1是默认静图720 动图360   2是微信分享静图300 动图240   3是本地，文件保存地址修正
    private int widthHeight = 360;
    private GifMaker.OnGifMakerListener listener;

    void makeSize(boolean isGif) {
        if (isGif) {
            widthHeight = makeType == WEIXIN ? Constant.Camerasettings.SHARE_WECHAT_GIF_RESOLUTION : (makeType == ContainerView
                    .ONLY_LAYER ? Constant.Camerasettings.IDEAL_VIDEO_RESOLUTION : Constant.Camerasettings.IDEAL_GIF_RESOLUTION);
        } else {
            widthHeight = makeType == WEIXIN ? Constant.Camerasettings.PHOTO_RESOLUTION_WECHAT_SHARE : Constant.Camerasettings
                    .PHOTO_RESOLUTION;
        }
    }

    private File[] bitmapFileList;
    private Bitmap bitmap;
    private TextStyleView relyView;

    public void start(int makeType, Bitmap bitmap, TextStyleView textStyleView, GifMaker.OnGifMakerListener listener) {
        this.listener = listener;
        if (autoStart) {
            return;
        }
        bitmapFileList = null;
        this.bitmap = bitmap;//判断是否是静图
        if (bitmap == null && makeType != ContainerView.ONLY_LAYER) {
            File dir = FileManager.getCameraBitmapFileDir();
            if (dir != null) {
                bitmapFileList = dir.listFiles();
                if (BuildConfig.DEBUG) {
                    Logger.d("bitmaplist:" + bitmapFileList.length);
                }
            }
        }
        if (makeType == ContainerView.ONLY_LAYER) {
            //先把容器文件夹清空
            File layerFile = FileManager.getGifLayerFile();
            FileUtils.deleteFilesInDir(layerFile);
        }
        this.relyView = textStyleView;
        this.makeType = makeType;
        //TOOD
        startDelay(listener);
    }

    void startDelay(GifMaker.OnGifMakerListener listener) {
        if (mGifMaker != null) {
            mGifMaker.reset();
            mGifMaker = null;
        }
        makeSize(true);
        relyView.startRecord();
        startGifMaker(listener, false);
    }


    /**
     * 视频合成多1帧用于显示视频最后一帧
     *
     * @param listener
     * @param isVideo
     */
    private void startGifMaker(GifMaker.OnGifMakerListener listener, boolean isVideo) {
        File outFile;
        if (makeType == LOCAL) {
            outFile = FileManager.getShareLocalFile(".gif");
        } else if (makeType == ONLY_LAYER) {
            outFile = FileManager.getCameraFileName(Constant.CameraFileName.CameraFileNameTag.LAYER_GIF);
        } else {
            outFile = FileManager.getShareFile(".gif");
        }
        if (outFile == null) {
            listener.onMakeGifFail();
            return;
        }
        String absolutePath = outFile.getAbsolutePath();
        int duration = relyView.getTotalTime();//视频时长
        if (duration == 0) {
            duration = 1500;
        }
        int repeatCount = 1;
        if (relyView.isRepeat()) {
            repeatCount = duration / relyView.getDuration();//动画时长
            if (duration % relyView.getDuration() > 0) {
                repeatCount++;
            }
        }
        int delay = relyView.getDelay();
        if (delay == 0) {
            delay = 80;
        }
        if (makeType == ContainerView.ONLY_LAYER) {
            //由于新需求，需要上传合成结果的视频，且这个视频的原速的。
            //ONLY_LAYER模式下，生成和视频一样的长的gif，不受速度影响
            duration *= relyView.speed;
            delay *= relyView.speed;
        }
        int count = 1;
        if (delay != 0) {
            count = duration / delay;
            if (duration % delay != 0) {
                count++;
            }
        }
        if (isVideo) {
            count++;
        }
        //TODO
        if (makeType == ContainerView.ONLY_LAYER) {
            //如果动画的时长小于视频的时长，就一次动画的时间，去计算帧数
            //适用于短文字动画
            if (relyView.getDuration() <= duration && (relyView.getDuration() <= 1500 || (relyView.animationProvider != null &&
                    relyView.animationProvider.getisShort()))) {
                //准备用ffmpeg命令做循环
                repeatCount = 1;
                if (delay != 0) {
                    count = relyView.getDuration() / delay;
                    if (relyView.getDuration() % delay != 0) {
                        count++;
                    }
                }
                if (isVideo) {
                    count++;
                }
            }

            int limit = 25;
            if (count > limit) {
                //这时候是Bitmap配置是Bitmap.Config.ARGB_8888，占用内存较高，重置参数
                count = limit;
                delay = duration / limit;//如果是三秒，25帧，delay是120毫秒，但是这个时候部分动画的合成效果显得有点卡顿
                //30帧，delay是100毫秒，合成效果还是有点卡
            }
        }
        if (bitmapFileList != null && bitmapFileList.length > 0) {
            //如果是预览界面
            count = bitmapFileList.length;
            repeatCount = 1;
            delay = relyView.getVideoDelay();//视频速度的影响将对gif的帧间隔产生影响
            Logger.d("count:" + count);
        }
//        if (makeType == ContainerView.ONLY_LAYER) {
//            count = (int) ((1f * duration / 1000) * Constant.Camerasettings.GIF_FAMES_NUM_PUBLISH);
//            repeatCount = 1;
//            delay = relyView.getVideoDelay();
//            if (isVideo) {
//                count++;
//            }
//        }
//        duration:2000,delay60,count:34,relyView.getVideoDuration():499  repeatCount=5
        Logger.d("视频duration:" + duration + ",动画relyView.getVideoDuration():" + relyView.getDuration() + ",delay" + delay + ",count:" +
                count + "，repeatCount=" + repeatCount);
        mGifMaker = new GifMaker(count, delay, Executors.newCachedThreadPool()).setOutputPath(absolutePath);
        mGifMaker.setRepeatCount(repeatCount);
        if (mGifMaker != null) {
            if (mGifMaker.isGifMaded) {
                listener.onMakeGifSucceed(mGifMaker.mOutputPath);
            } else {
                mGifMaker.setGifMakerListener(listener);
            }
        }
        relyView.startRecord();
        autoStart = true;
        startTime = System.currentTimeMillis();
    }

    private long startTime;

    private void onRecordFinish() {
        autoStart = false;
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    setLayerType(View.LAYER_TYPE_HARDWARE, null);
//                }
//            }
//        });
        recordViewFinish();
    }

    public ContainerEasyView(Context context) {
        super(context);
        init();
    }

    public ContainerEasyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContainerEasyView(Context context, AttributeSet attrs, int defStyleAttr) {
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
            if (view instanceof ScaleView) {
                ((ScaleView) view).refresh();
            }
        }
    }

    private void recordView(int time) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ScaleView) {
                ((ScaleView) view).record(time);
            }
        }
    }

    private void recordViewFinish() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ScaleView) {
                ((ScaleView) view).recordFinish();
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

    class RefreshRecordThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (running < 0) {
                    return;
                }
                if (running == 1 && !autoStart) {
                    refreshView();//重要方法，刷新文字动效
                }
                if (running == 1 && autoStart) {
                    if (mGifMaker != null && !mGifMaker.isBitmapFull()) {
                        if (System.currentTimeMillis() - startTime > mGifMaker.getTotalSize() * 1000) {
                            onRecordFinish();
                            if (mGifMaker != null && mGifMaker.mOnGifMakerListener != null) {
                                if (ContainerEasyView.this != null) {
                                    ContainerEasyView.this.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGifMaker.mOnGifMakerListener.onMakeGifFail();
                                            mGifMaker.reset();
                                        }
                                    });
                                }
                            }
                        } else {
                            int time = mGifMaker.getFrameCountNow() * mGifMaker.getDelay();
                            recordView(time);
                            Logger.d("recordView " + time);
                            Bitmap tBitmap;
                            if (makeType == ContainerView.ONLY_LAYER) {
                                tBitmap = Bitmap.createBitmap(widthHeight, widthHeight, Bitmap.Config.ARGB_8888);//需要透明度
                            } else {
                                tBitmap = Bitmap.createBitmap(widthHeight, widthHeight, Bitmap.Config.RGB_565);
                            }
                            Canvas canvas = new Canvas(tBitmap);
                            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                            if (bitmap != null && !bitmap.isRecycled()) {
                                //底图静态图片
                                canvas.drawBitmap(bitmap, null, new RectF(0, 0, widthHeight, widthHeight), null);
                            } else {
                                //底图视频
                                if (makeType != ContainerView.ONLY_LAYER) {
                                    if (BuildConfig.DEBUG) {
                                        int length = bitmapFileList.length;
                                        Logger.d(" bitmapFileList.length:" + bitmapFileList.length);
                                    }
                                    File file = bitmapFileList[mGifMaker.getFrameCountNow()];
                                    Logger.d("bitmap:" + file.toString());
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
                                    canvas.drawBitmap(bitmap, null, new RectF(0, 0, widthHeight, widthHeight), null);
                                    Util.recycleBitmaps(bitmap);
                                    if (!BuildConfig.DEBUG) {
                                        file.delete();
                                    }
                                }
                            }
                            float scale = widthHeight * 1.0f / getWidth();
                            for (int i = 0; i < getChildCount(); i++) {
                                View view = getChildAt(i);
                                if (view instanceof ScaleView) {
                                    ScaleView scaleView = (ScaleView) view;
                                    Matrix matrix = new Matrix();
                                    matrix.set(scaleView.mCurrentMatrix);
                                    matrix.postScale(scale, scale, 0, 0);
                                    canvas.save();
                                    canvas.concat(matrix);
                                    if (scaleView.getChildCount() > 0) {
                                        View childView = scaleView.getChildAt(0);
                                        if (childView instanceof IScaleView) {
                                            ((IScaleView) childView).drawCanvas(canvas);
                                        }
                                    }
                                    canvas.restore();
                                }
                            }
                            /**
                             * GifLayer
                             */
//                            if (makeType == ContainerView.ONLY_LAYER) {
//                                int frameCountNow = mGifMaker.getFrameCountNow();
//                                Logger.d("giflayer:current:" + frameCountNow + ",all:" + mGifMaker.getTotalSize());
////                                mOnGifMakerListener.onMakeGifSucceed(mOutputPath);
////                                mOnGifMakerListener.onMakeGifFail();
////                                mOnGifMakerListener.onMakeProgress(msg.what, getTotalSize());
//                                String gifLayerName = FileManager.getGifLayerName(frameCountNow);
//                                boolean save = ImageUtils.save(tBitmap, gifLayerName, Bitmap.CompressFormat.PNG);
//                                if (save) {
//                                    if (listener != null) {
//                                        if (frameCountNow == mGifMaker.getTotalSize() - 1) {
//                                            File gifLayerFile = FileManager.getGifLayerFile();
//                                            float fps = 1000f / mGifMaker.getDelay();
//                                            FFmpegGif(gifLayerFile, fps);
//                                        }
//                                    }
//                                } else {
//                                    if (listener != null) {
//                                        listener.onMakeProgress(0, frameCountNow);
//                                    }
//                                }
//                                mGifMaker.addNull();
//                            } else {
                            mGifMaker.addBitmap(tBitmap);
//                            }
                            Logger.d("easyView,addbitmap,count:" + mGifMaker.getFrameCountNow() + ",all:" + mGifMaker.getTotalSize()
                                    + ",time:" + System.currentTimeMillis());
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

    //    Handler handler;
    Thread refreshThread;

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        running = 1;
        refreshThread = new RefreshRecordThread();
        refreshThread.start();
//        handler = new Handler();
    }

    public void release() {
        running = -1;
        removeAllViews();
        if (refreshThread!=null){
            refreshThread.interrupt();
        }
    }

}
