package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.ps.R;
import com.season.lib.util.ScreenUtils;
import com.season.lib.util.Util;
import com.season.lib.ToolBitmapCache;
import com.season.lib.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/12.
 */

public class PSBackground {

    protected Context context;
    public ImageView picture;
    public CustomGifMovie customGifMovie;
    public CustomGifFrame customGifFrameview;
    public View bgView;
    public BgOperate currentOperate;
    int position = -1;
    List<BgOperate> list = new ArrayList<>();

    public PSBackground(View parentView) {
        this.context = parentView.getContext();
        picture = parentView.findViewById(R.id.picture);
        bgView = parentView.findViewById(R.id.bg_view);
        customGifMovie = parentView.findViewById(R.id.gifview);
        customGifFrameview = parentView.findViewById(R.id.gifFrameview);
        if (bgView != null) bgView.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public void refreshGifbg() {
        if (isGif()) {
            if (customGifMovie != null && customGifMovie.getVisibility() == View.VISIBLE) {
                customGifMovie.postInvalidate();
            }
            if (customGifFrameview != null && customGifFrameview.getVisibility() == View.VISIBLE) {
                customGifFrameview.postInvalidate();
            }
        }
    }

    public ILayer getGifView() {
        if (customGifMovie != null && customGifMovie.getVisibility() == View.VISIBLE) {
            return customGifMovie;
        }
        if (customGifFrameview != null && customGifFrameview.getVisibility() == View.VISIBLE) {
            return customGifFrameview;
        }
        return null;
    }

    public boolean isPhoto() {
        return (picture != null && picture.getVisibility() == View.VISIBLE);
//        return (videoView!=null&&currentOperate.visible3==View.VISIBLE);
    }

    public boolean isGif() {
        return ((customGifMovie != null && customGifMovie.getVisibility() == View.VISIBLE) || (customGifFrameview != null && customGifFrameview
                .getVisibility() == View.VISIBLE));
    }

    public int getDuration() {
        return 0;
    }

    public int getGifWidth() {
        if (isGif()) {
            if (customGifMovie.getVisibility() == View.VISIBLE) {
                return customGifMovie.getViewWidth();
            }
            if (customGifFrameview.getVisibility() == View.VISIBLE) {
                return customGifFrameview.getViewWidth();
            }
        }
        return 0;
    }

    public int getGifHeight() {
        if (isGif()) {
            if (customGifMovie.getVisibility() == View.VISIBLE) {
                return customGifMovie.getViewHeight();
            }
            if (customGifFrameview.getVisibility() == View.VISIBLE) {
                return customGifFrameview.getViewHeight();
            }
        }
        return 0;
    }

    public float getSpeed() {
        return 1.0f;
    }

    public ILayer getBackgroundView() {
        return null;
    }

    public boolean isBackgroundVideoImageViewVisible() {
        return false;
    }

    public boolean isBackgroundImageViewVisible() {
        return picture.getVisibility() == View.VISIBLE;
    }

    public void release() {
        if (list.size() > 0) {
            for (BgOperate op : list) {
                Util.recycleBitmaps(op.bitmap);
            }
            list.clear();
        }
        ToolBitmapCache.getDefault().release();
        bgView = null;
        picture = null;
        currentOperate = null;
        if (customGifMovie != null) {
            customGifMovie.onRelease();
        }
        if (customGifFrameview != null) {
            customGifFrameview.onRelease();
        }
        customGifMovie = null;
        customGifFrameview = null;
    }

    private void init() {
        BgOperate op = new BgOperate(View.VISIBLE, View.GONE, View.GONE, Color.TRANSPARENT, null, null, null);
        reset(op);
        addEvent(op);
    }

    public boolean showVideoView(String url, String videoPath, float rate) {
        if (currentOperate != null) {
            if (currentOperate.visible3 == View.VISIBLE && videoPath.equals(currentOperate.videoFile)) {
                return false;
            }
        }
        BgOperate op = new BgOperate(url, videoPath, rate);
        op.videoFile = videoPath;
        reset(op);
        addEvent(op);
        return true;
    }

    public interface decoderGifDoneListener {
        void decoder(int offsetX, int offsetY);
    }

    decoderGifDoneListener gifDoneListener;

    public boolean showGIf(String url, String path, decoderGifDoneListener decoderGifDoneListener) {
        this.gifDoneListener = decoderGifDoneListener;
        if (currentOperate != null) {
            if (customGifMovie != null&&currentOperate.visibleGif == View.VISIBLE && customGifMovie.equals(currentOperate.gifFile)) {
                return false;
            }
            if (customGifFrameview != null&&currentOperate.visibleGif == View.VISIBLE && customGifFrameview.equals(currentOperate.gifFile)) {
                return false;
            }
        }
        BgOperate op = new BgOperate(url, path, true);
        reset(op);
        addEvent(op);
        return true;
    }

    public boolean showImage(String url, String path) {
        Logger.d("addLocalMessage,showImage1:" + System.currentTimeMillis());
        if (currentOperate != null) {
            if (currentOperate.visible2 == View.VISIBLE && path.equals(currentOperate.imageFile)) {
                return false;
            }
        }
        Logger.d("addLocalMessage,showImage2:" + System.currentTimeMillis());
        BgOperate op = new BgOperate(url, path, false);
        Logger.d("addLocalMessage,showImage3:" + System.currentTimeMillis());
        //TODO 不知道为什么这里显示的特别慢，调用到reset()方法太慢了，这里暂时用Glide来加速显示
        if (context != null && picture != null && !TextUtils.isEmpty(url)) {
            picture.post(new Runnable() {
                @Override
                public void run() {
                    picture.setVisibility(View.VISIBLE);
                    Logger.d("addLocalMessage,Glide:" + System.currentTimeMillis());
                 //   Glide.with(context).load(url).into(picture);
                }
            });
        }
        reset(op);
        addEvent(op);
        return true;
    }

    public boolean showBackground(int color) {
        if (currentOperate != null) {
            if (currentOperate.visible1 == View.VISIBLE && color == currentOperate.color) {
                return false;
            }
        }
        BgOperate op = new BgOperate(color);
        reset(op);
        addEvent(op);
        return true;
    }

    public boolean showVideoOrImage() {
        if (currentOperate != null) {
            if (currentOperate.visible1 == View.VISIBLE) {
                for (int i = position - 1; i >= 0; i--) {
                    BgOperate operate = list.get(i);
                    if (operate.visible1 == View.GONE) {
                        reset(operate);
                        addEvent(operate);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean reset(final BgOperate op) {
        currentOperate = op;

        if (picture != null) {
            picture.post(new Runnable() {
                @Override
                public void run() {
                    if (picture != null)
                        picture.setVisibility(op.visible2);
                }
            });
        }
        if (bgView != null) {
            bgView.post(new Runnable() {
                @Override
                public void run() {
                    if (bgView != null)
                    bgView.setVisibility(op.visible1);
                }
            });
        }
        if (op.visible1 == View.VISIBLE) if (bgView != null) {
            bgView.post(new Runnable() {
                @Override
                public void run() {
                    if (bgView != null)
                    bgView.setBackgroundColor(op.color);
                }
            });
        }
        if (!TextUtils.isEmpty(op.imageFile) && op.visible2 == View.VISIBLE) {
            if (Looper.myLooper()==Looper.getMainLooper()){
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (op.bitmap != null && !op.bitmap.isRecycled()) {
                        } else {
                            op.bitmap = BitmapFactory.decodeFile(op.imageFile);
                        }
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                if (op.bitmap != null && !op.bitmap.isRecycled()) {
                } else {
                    op.bitmap = BitmapFactory.decodeFile(op.imageFile);
                }
            }
            if (picture != null) {
                picture.post(new Runnable() {
                    @Override
                    public void run() {
                        if (picture != null)
                        picture.setImageBitmap(op.bitmap);
                    }
                });
            }
        }
//        String gifFile = op.gifFile;
//        boolean b = op.visibleGif == View.VISIBLE;
        if (!TextUtils.isEmpty(op.gifFile) && op.visibleGif == View.VISIBLE) {
            if (customGifMovie != null) {
                customGifMovie.post(new Runnable() {
                    @Override
                    public void run() {
                        if (customGifMovie == null){
                            return;
                        }
                        ViewGroup.LayoutParams layoutParams = null;
                        int height = 0;
                        int width = 0;

                        boolean isSuccess = customGifMovie.setMovieResource(op.gifFile);
                        if (isSuccess) {
                            customGifMovie.setVisibility(View.VISIBLE);
                            customGifMovie.setisGifEditMode(true);
                            layoutParams = customGifMovie.getLayoutParams();
                            width = customGifMovie.getViewWidth();
                            height = customGifMovie.getViewHeight();
                        } else {
                            customGifFrameview.setVisibility(View.VISIBLE);
                            customGifFrameview.setisGifEditMode(true);
                            customGifFrameview.setMovieResource(op.gifFile);
                            layoutParams = customGifFrameview.getLayoutParams();
                            width = customGifFrameview.getViewWidth();
                            height = customGifFrameview.getViewHeight();
                        }
                        Logger.d("showGif,isSuccess:" + isSuccess + ",width:" + width + ",height:" + height);
                        if (layoutParams == null) {
                            return;
                        }
                        if (height == 0 || width == 0) {
                            return;
                        }
                        int screenWidth = ScreenUtils.getScreenWidth(context);
                        if (width >= height) {
                            int newH = screenWidth * height / width;
                            layoutParams.width = screenWidth;
                            layoutParams.height = newH;
                            if (gifDoneListener != null) gifDoneListener.decoder(0, (screenWidth - newH) / 2);
                        } else {
                            int newW = screenWidth * width / height;
                            layoutParams.height = screenWidth;
                            layoutParams.width = newW;
                            if (gifDoneListener != null) gifDoneListener.decoder((screenWidth - newW) / 2, 0);
                        }
                        if (layoutParams instanceof RelativeLayout.LayoutParams) {
                            ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.CENTER_IN_PARENT);
                        } else if (layoutParams instanceof LinearLayout.LayoutParams) {
                            ((LinearLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                        }
                    }
                });
            }
        } else {
        }
        if (!TextUtils.isEmpty(op.videoFile) && op.visible3 == View.VISIBLE) {

        }
        if (op.visible3 != View.VISIBLE) {

        }
        return op.visible3 != View.VISIBLE;
    }

    public void reset() {
        BgOperate op = new BgOperate(View.VISIBLE, View.GONE, View.GONE, Color.TRANSPARENT, null, null, null);
        reset(op);
        addEvent(op);
    }

    public static class BgOperate {
        //visible1 表示选了颜色背景，2代表图片，3代表视频,4代表Gif
        public int visible1 = View.GONE, visible2 = View.GONE, visible3 = View.GONE, visibleGif = View.GONE;
        public int color = -1;
        public String imageFile;
        public String gifFile;
        public String videoFile;
        public String url;
        public Bitmap bitmap;
        public float rate;

        public BgOperate(int visible1, int visible2, int visible3, int color, String imageFile, String videoFile, String gifFile) {
            this.visible1 = visible1;
            this.visible2 = visible2;
            this.visible3 = visible3;
            this.color = color;
            this.imageFile = imageFile;
            this.videoFile = videoFile;
            this.gifFile = gifFile;
        }

        public BgOperate(String url, String imagePath, boolean isGif) {
            this.url = url;
            if (!TextUtils.isEmpty(imagePath)) {
                if (isGif) {
                    this.visibleGif = View.VISIBLE;
                    this.gifFile = imagePath;
                } else {
                    this.visible2 = View.VISIBLE;
                    this.imageFile = imagePath;
                }
//                bitmap = ToolBitmapCache.getDefault().getBitmapFromFile(imagePath);
                //图片或者Gif
                bitmap = BitmapFactory.decodeFile(imagePath);
            }
        }

//        public BgOperate(String imagePath) {
//            if (!TextUtils.isEmpty(imagePath)) {
//                this.visible2 = View.VISIBLE;
//                this.imageFile = imagePath;
//                bitmap = ToolBitmapCache.getDefault().getBitmapFromFile(imagePath);
//            }
//        }

        public BgOperate(String url, String videoPath, float rate) {
            this.url = url;
            this.rate = rate;
            if (!TextUtils.isEmpty(videoPath)) {
                this.visible3 = View.VISIBLE;
                this.videoFile = videoPath;
                try {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    media.setDataSource(videoPath);
                    bitmap = media.getFrameAtTime();
                    media.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            if (!TextUtils.isEmpty(gifPath)){//这个需求暂时没有，背景只有视频
//                this.visible3 = View.VISIBLE;
//                this.gifFile = gifPath;
//                bitmap = new FrameDecoder(videoPath).getFrame();
//            }
        }

        public BgOperate(int color) {
            this.visible1 = View.VISIBLE;
            this.color = color;
        }
    }


    private void addEvent(BgOperate operate) {
        if (position < list.size() - 1) {
            for (int i = list.size() - 1; i > position; i--) {
                list.remove(i);
            }
        }
        list.add(operate);
        position = list.size() - 1;
    }

    public boolean pre() {
        position--;
        if (position < 0) {
            position = 0;
        }
        BgOperate op = list.get(position);
        return reset(op);
    }

    public boolean pro() {
        position++;
        if (position > list.size() - 1) {
            position = list.size() - 1;
        }
        BgOperate op = list.get(position);
        return reset(op);
    }
}
