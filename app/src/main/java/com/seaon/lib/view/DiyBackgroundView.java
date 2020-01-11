package com.seaon.lib.view;

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

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.seaon.lib.movie.GifMovieView;
import com.seaon.lib.util.ScreenUtils;
import com.biaoqing.library.diy.gifmaker.frame.GifFrameView;
import com.biaoqing.library.diy.ui.view.scale.IScaleView;
import com.biaoqing.library.diy.gifmaker.utils.Util;
import com.seaon.lib.util.ToolBitmapCache;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/12.
 */

public class DiyBackgroundView {

    Context context;
    public ImageView picture;
    public GifMovieView gifMovieView;
    public GifFrameView gifFrameview;
    public View bgView;
    public BgOperate currentOperate;
    int position = -1;
    List<BgOperate> list = new ArrayList<>();

    public DiyBackgroundView(View view) {
        context = view.getContext();
        picture = (ImageView) view.findViewById(R.id.picture);
        bgView = view.findViewById(R.id.bg_view);
        gifMovieView = view.findViewById(R.id.gifview);
        gifFrameview = view.findViewById(R.id.gifFrameview);
        if (bgView != null) bgView.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public void refreshGifbg() {
//        if (BuildConfig.DEBUG) {
//            Logger.t("testGif").d("gifMovieView != null==>" + (gifMovieView != null));
//            Logger.t("testGif").d("gifMovieView.getVisibility() == View.VISIBLE==>" + (gifMovieView.getVisibility() == View.VISIBLE));
//            Logger.t("testGif").d("gifFrameview != null==>" + (gifFrameview != null));
//            Logger.t("testGif").d("gifFrameview.getVisibility() == View.VISIBLE==>" + (gifFrameview.getVisibility() == View.VISIBLE));
//            Logger.t("testGif").d("isGif()==>" + (isGif()));
//        }
        if (isGif()) {
            if (gifMovieView != null && gifMovieView.getVisibility() == View.VISIBLE) {
                gifMovieView.postInvalidate();
            }
            if (gifFrameview != null && gifFrameview.getVisibility() == View.VISIBLE) {
                gifFrameview.postInvalidate();
            }
        }
    }

    public IScaleView getGifView() {
        if (gifMovieView != null && gifMovieView.getVisibility() == View.VISIBLE) {
            return gifMovieView;
        }
        if (gifFrameview != null && gifFrameview.getVisibility() == View.VISIBLE) {
            return gifFrameview;
        }
        return null;
    }

    public boolean isVideo() {
        return false;
//        return (videoView!=null&&currentOperate.visible3==View.VISIBLE);
    }

    public boolean isPhoto() {
        return (picture != null && picture.getVisibility() == View.VISIBLE);
//        return (videoView!=null&&currentOperate.visible3==View.VISIBLE);
    }

    public boolean isGif() {
        return ((gifMovieView != null && gifMovieView.getVisibility() == View.VISIBLE) || (gifFrameview != null && gifFrameview
                .getVisibility() == View.VISIBLE));
    }

    public int getDuration() {
        return 0;
    }

    public int getGifWidth() {
        if (isGif()) {
            if (gifMovieView.getVisibility() == View.VISIBLE) {
                return gifMovieView.getViewWidth();
            }
            if (gifFrameview.getVisibility() == View.VISIBLE) {
                return gifFrameview.getViewWidth();
            }
        }
        return 0;
    }

    public int getGifHeight() {
        if (isGif()) {
            if (gifMovieView.getVisibility() == View.VISIBLE) {
                return gifMovieView.getViewHeight();
            }
            if (gifFrameview.getVisibility() == View.VISIBLE) {
                return gifFrameview.getViewHeight();
            }
        }
        return 0;
    }

    public float getSpeed() {
//        if (videoView == null) {
//            return 1.0f;
//        }
//        if (videoView.getVisibility() == View.VISIBLE) {
//            return videoView.getSpeed();
//        }
        return 1.0f;
    }

    public IScaleView getBackgroundView() {
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
            //Canvas: trying to use a recycled bitmap android.graphics.Bitmap@126543a
            for (BgOperate op : list) {
                Util.recycleBitmaps(op.bitmap);
            }
            list.clear();
        }
        ToolBitmapCache.getDefault().release();
        bgView = null;
        picture = null;
        currentOperate = null;
        if (gifMovieView != null) {
            gifMovieView.onRelease();
        }
        if (gifFrameview != null) {
            gifFrameview.onRelease();
        }
        gifMovieView = null;
        gifFrameview = null;
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
            if (gifMovieView != null&&currentOperate.visibleGif == View.VISIBLE && gifMovieView.equals(currentOperate.gifFile)) {
                return false;
            }
            if (gifFrameview != null&&currentOperate.visibleGif == View.VISIBLE && gifFrameview.equals(currentOperate.gifFile)) {
                return false;
            }
        }
        BgOperate op = new BgOperate(url, path, true);
        reset(op);
        addEvent(op);
        return true;
    }

    //08-10 15:57:50.414 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,1:1533887870413
//            08-10 15:57:50.415 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,3:1533887870414
//            08-10 15:57:50.416 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,showImage1:1533887870415
//            08-10 15:57:50.416 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,showImage2:1533887870416
//            08-10 15:57:50.453 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,showImage3:1533887870453
    //????这里怎么会耗时那么九？
//            08-10 15:57:52.842 26757-27278/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,reset:1533887872841
//            08-10 15:57:52.843 26757-26757/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ addLocalMessage,
// setImageBitmap:1533887872842
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
        if (BuildConfig.DEBUG) {
            //内含BITMAP,可能显示非常慢
//            Gson gson = BQUtils.getGson();
//            Logger.d("diy,reset,op:" + gson.toJson(op));
            Logger.d("addLocalMessage,reset:" + System.currentTimeMillis());
        }
        currentOperate = op;

        if (picture != null) {
            picture.post(new Runnable() {
                @Override
                public void run() {
                    Logger.d("addLocalMessage,visible:" + System.currentTimeMillis());
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
                        Logger.d("addLocalMessage,setImageBitmap:" + System.currentTimeMillis());
                        if (picture != null)
                        picture.setImageBitmap(op.bitmap);
                    }
                });
            }
        }
//        String gifFile = op.gifFile;
//        boolean b = op.visibleGif == View.VISIBLE;
        if (!TextUtils.isEmpty(op.gifFile) && op.visibleGif == View.VISIBLE) {
            if (gifMovieView != null) {
                gifMovieView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (gifMovieView == null){
                            return;
                        }
                        ViewGroup.LayoutParams layoutParams = null;
                        int height = 0;
                        int width = 0;

                        boolean isSuccess = gifMovieView.setMovieResource(op.gifFile);
                        if (isSuccess) {
                            gifMovieView.setVisibility(View.VISIBLE);
                            gifMovieView.setisGifEditMode(true);
                            layoutParams = gifMovieView.getLayoutParams();
                            width = gifMovieView.getViewWidth();
                            height = gifMovieView.getViewHeight();
                        } else {
                            gifFrameview.setVisibility(View.VISIBLE);
                            gifFrameview.setisGifEditMode(true);
                            gifFrameview.setMovieResource(op.gifFile);
                            layoutParams = gifFrameview.getLayoutParams();
                            width = gifFrameview.getViewWidth();
                            height = gifFrameview.getViewHeight();
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
            //TODO reset（）滥用，预览界面在使用gifFrameView　如何执行下列代码，会把gifFrameview隐藏
//            if (gifMovieView != null) {
//                gifMovieView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        gifMovieView.setVisibility(View.GONE);
//                    }
//                });
//            }
//            if (gifFrameview != null) {
//                gifFrameview.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        gifFrameview.setVisibility(View.GONE);
//                    }
//                });
//            }
        }
        if (!TextUtils.isEmpty(op.videoFile) && op.visible3 == View.VISIBLE) {

        }
        //recycle
        if (op.visible3 != View.VISIBLE) {

        }
        return op.visible3 != View.VISIBLE;
    }

    public void reset() {
        if (BuildConfig.DEBUG) Logger.d("diy==>diybg_reset():" + System.currentTimeMillis());
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
