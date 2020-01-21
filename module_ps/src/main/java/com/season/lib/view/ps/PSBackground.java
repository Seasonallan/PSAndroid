package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.ps.R;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.http.DownloadAPI;
import com.season.lib.file.FileManager;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.ToolBitmapCache;

import java.io.File;
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
                BitmapUtil.recycleBitmaps(op.bitmap);
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
        BgOperate op = new BgOperate(View.VISIBLE, View.GONE,  Color.TRANSPARENT, null, null);
        reset(op);
        addEvent(op);
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

    public boolean showImage(final String url, String path) {
        if (currentOperate != null) {
            if (currentOperate.visible2 == View.VISIBLE && path.equals(currentOperate.imageFile)) {
                return false;
            }
        }
        BgOperate op = new BgOperate(url, path, false);
        if (context != null && picture != null && !TextUtils.isEmpty(url)) {
            picture.post(new Runnable() {
                @Override
                public void run() {
                    picture.setVisibility(View.VISIBLE);
                    final File file = FileManager.getPsFile(null, ".png");
                    DownloadAPI.downloadFile(url, file, new DownloadAPI.IDownloadListener() {
                        @Override
                        public void onCompleted() {
                            currentOperate.bitmap = BitmapFactory.decodeFile(file.toString());
                            picture.setImageBitmap(currentOperate.bitmap);
                        }

                        @Override
                        public void onError() {

                        }
                    });
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
                        if (layoutParams == null) {
                            return;
                        }
                        if (height == 0 || width == 0) {
                            return;
                        }
                        int screenWidth = ScreenUtils.getScreenWidth();
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
        return true;
    }

    public void reset() {
        BgOperate op = new BgOperate(View.VISIBLE,  View.GONE, Color.TRANSPARENT, null, null);
        reset(op);
        addEvent(op);
    }

    public static class BgOperate {
        //visible1 表示选了颜色背景，2代表图片，,4代表Gif
        public int visible1 = View.GONE, visible2 = View.GONE, visibleGif = View.GONE;
        public int color = -1;
        public String imageFile;
        public String gifFile;
        public String url;
        public Bitmap bitmap;

        public BgOperate(int visible1, int visible2, int color, String imageFile, String gifFile) {
            this.visible1 = visible1;
            this.visible2 = visible2;
            this.color = color;
            this.imageFile = imageFile;
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
