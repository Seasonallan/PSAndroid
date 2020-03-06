package com.season.example;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.season.lib.bean.LayerEntity;
import com.season.lib.bean.LayerItem;
import com.season.lib.file.FileManager;
import com.season.lib.file.FileUtils;
import com.season.lib.http.DownloadAPI;
import com.season.lib.util.ToastUtil;
import com.season.lib.view.ps.CustomGifFrame;
import com.season.lib.view.ps.CustomGifMovie;
import com.season.lib.view.ps.CustomImageView;
import com.season.lib.view.ps.CustomTextView;
import com.season.lib.view.ps.ILayer;
import com.season.lib.view.ps.PSLayer;

import java.io.File;
import java.util.List;

public abstract class ViewExtend {

    Context context;
    Handler handler;
    int videoWidthHeight, offsetX, offsetY;
    public ViewExtend(Context context, int videoWidthHeight, int offsetX, int offsetY){
        this.context = context;
        this.handler = new Handler();
        this.videoWidthHeight = videoWidthHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void showLayers(LayerEntity layerEntity, List<LayerItem> items) {
        if (items.size() > 0) {//图层信息转化
            for (int i = 0; i < items.size(); i++) {
                LayerItem item = items.get(i);
                PSLayer itemLayerView = new PSLayer(context);
                if (item.getContentViewType() != LayerItem.ILayerType.ContentViewTypeTextbox) {//不是文字图层
                    String path = item.filePath;
                    String url = item.getImageURL();
                    File file = new File(path);
                    if (file.isFile() && file.length() > 0) {
                        switch (item.getContentViewType()) {
                            //本地素材 绘图 要上传图片
                            case LayerItem.ILayerType.ContentViewTypeImage:
                            case LayerItem.ILayerType.ContentViewTypeLocaImage://贴纸图层
                                int imageWidth;
                                int imageHeight;
                                if (!FileUtils.isStaticImageFile(path)) {//图片是Gif
                                    CustomGifMovie customGifMovie = new CustomGifMovie(context, false);
                                    boolean res = customGifMovie.setMovieResource(path);
                                    if (res) {//sometimes movie decode gif error url duration = 0
                                        customGifMovie.url = url;
                                        imageWidth = customGifMovie.getViewWidth();
                                        imageHeight = customGifMovie.getViewHeight();
                                        itemLayerView.addView(customGifMovie,
                                                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    } else {
                                        CustomGifFrame customGifFrame = new CustomGifFrame(context);
                                        customGifFrame.setMovieResource(path);
                                        customGifFrame.url = url;
                                        imageWidth = customGifFrame.getViewWidth();
                                        imageHeight = customGifFrame.getViewHeight();
                                        itemLayerView.addView(customGifFrame,
                                                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    }
                                } else {//图片是静图
                                    CustomImageView imageView = new CustomImageView(context);
                                    imageView.setImageFile(path);
                                    imageWidth = imageView.getViewWidth();
                                    imageHeight = imageView.getViewHeight();
                                    imageView.url = url;
                                    imageView.isTuya = false;
                                    itemLayerView
                                            .addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                    .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                }
                                itemLayerView.bindMatrixImage(item, videoWidthHeight, offsetX, offsetY,
                                        layerEntity.getWidth(),
                                        layerEntity.getHeight(), imageWidth, imageHeight);
                                break;
                            case LayerItem.ILayerType.ContentViewTypeDraw://图层是涂鸦，静图，设置标志位isTuya=true
                                CustomImageView customImageView = new CustomImageView(context);
                                customImageView.setImageFile(path);
                                customImageView.url = url;
                                customImageView.isTuya = true;
                                itemLayerView
                                        .addView(customImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                itemLayerView.bindMatrixImage(item, videoWidthHeight, offsetX, offsetY,
                                        layerEntity.getWidth(),
                                        layerEntity.getHeight(), customImageView.getViewWidth(),
                                        customImageView.getViewHeight());
                                break;
                        }
                        if (itemLayerView.getChildCount() > 0){
                            View cView = itemLayerView.getChildAt(0);
                            if (cView instanceof ILayer){
                                ((ILayer) cView).setStartTime(item.startTime);
                                ((ILayer) cView).setEndTime(item.endTime);
                            }
                        }
                        addViewPost(itemLayerView, i * 100, i == items.size() - 1);
                    } else {
                    }
                } else {
                    //图层是文字
                    CustomTextView customTextView = new CustomTextView(context);
                    boolean scaleOrNot = customTextView.setTextEntry(item, layerEntity.getWidth());
                    int duration = 0;
                    int delayVideo = 0;
                    int animationType = item.animationType;
                    float speed = 1.0f;
                    customTextView.setTextAnimationType(animationType, duration, delayVideo, speed);
                    itemLayerView.addView(customTextView,
                            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                                    .LayoutParams.WRAP_CONTENT));
                    itemLayerView.bindMatrix(item, videoWidthHeight, offsetX, offsetY, layerEntity.getWidth(),
                            layerEntity.getHeight(),
                            scaleOrNot);
                    customTextView.setStartTime(item.startTime);
                    customTextView.setEndTime(item.endTime);
                    addViewPost(itemLayerView, i * 100, i == items.size() - 1);
                }
            }
        }
    }


    //背景图片 添加
    public void addLocalMessage(final String url, final String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            setBackground(url, filePath);
        }else{
            final File file = FileManager.getPsFile(url.hashCode() + "", FileUtils.getFileType(url));
            if (file == null) {
                return;
            }
            DownloadAPI.downloadFile(url, file, new DownloadAPI.IDownloadListener() {
                @Override
                public void onCompleted() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (file.length() > 0) {
                                addLocalMessage(url, file.toString());
                            }
                        }
                    });
                }

                @Override
                public void onError() {
                    file.deleteOnExit();
                    ToastUtil.show("下载失败，请稍候重试");
                }
            });
        }
    }



    public void addImageOrGifFromUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        final File file = FileManager.getPsFile(url.hashCode() + "", FileUtils.getFileType(url));
        if (file == null) {
            return;
        }
        if (file.length() > 0) {
            addImageOrGifFromFile(url, file.toString());
        } else {
            ToastUtil.show("下载素材中，请稍候");
            DownloadAPI.downloadFile(url, file, new DownloadAPI.IDownloadListener() {
                @Override
                public void onCompleted() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (file.length() > 0) {
                                addImageOrGifFromFile(url, file.toString());
                            }
                        }
                    });
                }

                @Override
                public void onError() {
                    file.deleteOnExit();
                    ToastUtil.show("下载失败，请稍候重试");
                }

            });
        }
    }


    public void addTextView(String text, boolean blackBg){
        CustomTextView customTextView = new CustomTextView(context);
        if (blackBg) {
            customTextView.setPaintColorReverse(Color.WHITE, Color.BLACK);
        } else {
            customTextView.setPaintColorReverse(Color.BLACK, Color.WHITE);
        }
        customTextView.setText(text);
        addView(customTextView);
    }

    public void addImageOrGifFromFile(String url, String filePath) {
        if (!FileUtils.isStaticImageFile(filePath)) {
            CustomGifMovie customGifMovie = new CustomGifMovie(context, false);
            boolean res = customGifMovie.setMovieResource(filePath);
            if (res) {//sometimes movie decode gif error url duration = 0
                customGifMovie.url = url;
                addView(customGifMovie, getInitScale(customGifMovie));
            } else {
                CustomGifFrame customGifFrame = new CustomGifFrame(context);
                customGifFrame.setMovieResource(filePath);
                customGifFrame.url = url;
                addView(customGifFrame, getInitScale(customGifFrame));
            }
        } else {
            addImageViewFromFile(false, url, filePath);
        }
    }

    public void addImageViewFromFile(boolean isTuya, String url, String filePath) {
        addImageViewFromFile(isTuya, url, filePath, -1, -1);
    }

    public void addImageViewFromFile(boolean isTuya, String url, String filePath, float x, float y) {
        CustomImageView imageView = new CustomImageView(context);
        imageView.setImageFile(filePath);
        imageView.isTuya = isTuya;
        imageView.url = url;
        if (x >= 0 && y >= 0) {
            imageView.setCenterXY(x, y);
        }
        if (isTuya) {
            addView(imageView);
        } else {
            addView(imageView, getInitScale(imageView));
        }
    }
    private void addView(View view, float scale) {
        PSLayer PSLayer = new PSLayer(context);
        PSLayer.scaleInit(scale);
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        addLayer(PSLayer);
    }

    public void addView(View view) {
        PSLayer PSLayer = new PSLayer(context);
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        addLayer(PSLayer);
    }


    int DIY_STICKER_BASE_SIZE = 480;//贴纸缩放比例尺寸
    private float getInitScale(ILayer scaleView) {
        float width = scaleView.getViewWidth() * 1.0f;
        if (width >= DIY_STICKER_BASE_SIZE) {
            return videoWidthHeight / width;
        } else {
            return videoWidthHeight / DIY_STICKER_BASE_SIZE;
        }
    }

    //post一个一个显示图层，比较好看
    public abstract void addViewPost(PSLayer PSLayer, int delay, boolean isLast);

    public abstract void addLayer(PSLayer view);

    protected abstract void setBackground(String url, String filePath);
}
