package com.season.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.example.layout.BottomPaintLayout;
import com.season.example.layout.BottomTextLayout;
import com.season.example.layout.PSBgColorGroup;
import com.season.example.support.MosaicUtil;
import com.season.lib.RoutePath;
import com.season.lib.bean.LayerItem;
import com.season.lib.gif.GifMaker;
import com.season.lib.util.PsUtil;
import com.season.lib.view.ps.CustomGifFrame;
import com.season.lib.view.ps.ILayer;
import com.season.lib.log.Logger;
import com.season.lib.http.DownloadAPI;
import com.season.example.layout.PopInputLayout;
import com.season.lib.view.ps.CustomGifMovie;
import com.season.lib.bean.LayerBackground;
import com.season.lib.bean.LayerEntity;
import com.season.lib.view.ps.PSLayer;
import com.season.lib.util.Constant;
import com.season.lib.file.FileManager;
import com.season.lib.file.FileUtils;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.view.ps.PSCanvas;
import com.season.example.layout.TopDeleteLayout;
import com.season.lib.view.ps.PSCoverView;
import com.season.lib.view.ps.CustomImageView;
import com.season.lib.view.ps.CustomTextView;
import com.season.lib.view.ps.CustomCanvas;
import com.example.ps.R;
import com.season.lib.dimen.AutoUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@Route(path= RoutePath.PS)
public class PsActivity extends FragmentActivity implements View.OnClickListener {

    PSCoverView mPsCoverView;//最外部覆盖
    PSCanvas mPsCanvas;//ps画布
    CustomCanvas customCanvas;//涂鸦
    TopDeleteLayout topDeleteLayout;

    PSBgColorGroup mPsbgColorGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ps);

        initView();
        initBottomLayout();

        mPsbgColorGroup = new PSBgColorGroup(this, mPsCanvas) {
            @Override
            public void onBgChanged() {
                resetStatus();
            }
        };

        mPsCoverView.setOnActionUpListener(new PSCoverView.OnUpListener() {
            @Override
            public boolean onActionUp(float x, float y) {
                //如果选择背景是展开的，关闭它
                return mPsbgColorGroup.closeIfOpened(x, y);
            }
        });

        mPsCanvas.bindBgView(findViewById(R.id.opview));
        topDeleteLayout = new TopDeleteLayout(del_container, iv_back, iv_next);
        mPsCanvas.setOnMoveListener(new PSCanvas.IOnMoveListener() {
            @Override
            public boolean onMove(MotionEvent event) {
                return topDeleteLayout.checkPosition(event);
            }

            @Override
            public void onMoveEnd() {
                topDeleteLayout.hide();
            }
        });
        mPsCanvas.setClickListener(new PSLayer.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextView(view);
            }

            @Override
            public void onDoubleClick(View view) {
                editTextView(view);
            }

            @Override
            public void onDelete(View view) {
            }
        });
        mPsCanvas.setFocusChangeListener(new PSCanvas.IFocusChangeListener() {
            @Override
            public void onFocusLose(ViewGroup view) {
            }

            @Override
            public void onFocusGet(ViewGroup parent) {
                resetBottomStatus();
            }

            @Override
            public void onFocusClear() {
                resetBottomStatus();
            }
        });

        mPsCoverView.post(new Runnable() {
            @Override
            public void run() {
                fixCanvasHeight();
                bindData2Canvas();
            }
        });
    }

    ImageView iv_back;
    ImageView iv_next;
    ImageView iv_close;
    ImageView iv_confirm;

    LinearLayout del_container;

    ImageView iv_redo;
    ImageView iv_undo;
    ImageView iv_delete;
    private void initView(){
        mPsCoverView = findViewById(R.id.layout_container);
        mPsCanvas = findViewById(R.id.layout_stickLayout);
        customCanvas = findViewById(R.id.tuya);

        del_container = findViewById(R.id.del_container);

        iv_redo = findViewById(R.id.iv_redo);
        iv_undo = findViewById(R.id.iv_undo);
        iv_delete = findViewById(R.id.iv_delete);

        iv_back = findViewById(R.id.iv_back);
        iv_next = findViewById(R.id.iv_next);
        iv_close = findViewById(R.id.iv_close);
        iv_confirm = findViewById(R.id.iv_confirm);

        iv_close.setOnClickListener(this);
        iv_confirm.setOnClickListener(this);

        iv_back.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_undo.setOnClickListener(this);
        iv_redo.setOnClickListener(this);
        iv_delete.setOnClickListener(this);
        findViewById(R.id.bt_paint).setOnClickListener(this);
        findViewById(R.id.bt_text).setOnClickListener(this);
        findViewById(R.id.bt_image).setOnClickListener(this);
    }

    int videoWidthHeight, offsetX, offsetY;
    private void fixCanvasHeight() {
        View canvasArea = findViewById(R.id.opview);
        int height = canvasArea.getHeight();
        int screenWidth = ScreenUtils.getScreenWidth(this);
        videoWidthHeight = Math.min(height, screenWidth);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) canvasArea.getLayoutParams();
        params.width = videoWidthHeight;
        params.height = videoWidthHeight;
        canvasArea.requestLayout();


        View topView = findViewById(R.id.rl_op_top);
        if (videoWidthHeight >= screenWidth) {//高度足够，适配宽度显示为正方形。
            View opviewContainer = findViewById(R.id.opviewContainer);
            int opviewContainerHeight = opviewContainer.getHeight();
            offsetX = 0;
            offsetY = topView.getHeight() + (opviewContainerHeight - videoWidthHeight) / 2;

            RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) topView
                    .getLayoutParams();
            topParams.height = offsetY;
            topView.requestLayout();

            View bottomView = findViewById(R.id.ll_op_bottom);
            RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) bottomView
                    .getLayoutParams();
            bottomParams.height =
                    bottomView.getHeight() + (opviewContainerHeight - videoWidthHeight) / 2;
            bottomView.requestLayout();

        } else {//高度不够，宽度适配高度显示为正方形。可在PAD上测试
            offsetX = (screenWidth - videoWidthHeight) / 2;
            offsetY = topView.getHeight();
        }

        RelativeLayout.LayoutParams delParams = (RelativeLayout.LayoutParams) del_container
                .getLayoutParams();
        delParams.height = Math.max(offsetY, AutoUtils.getPercentWidthSize(128));
        del_container.requestLayout();

        mPsCanvas.videoWidthHeight = videoWidthHeight;
        mPsCanvas.setOffsetX(offsetX);
        mPsCanvas.setOffsetY(offsetY);
        Logger.LOG(""+ offsetX +"----"+ offsetY);
    }

    private Handler handler = new Handler();
    private void bindData2Canvas() {

        try {
            LayerEntity  layerEntity = null;
            Object localData = FileUtils.getSerialData(this, "layerInfo");
            if (localData instanceof  LayerEntity){
                layerEntity = (LayerEntity) localData;
            }
            if (layerEntity != null){
                LayerBackground backgroundInfo = layerEntity.getBackInfoModel();
                List<LayerItem> items = layerEntity.getItemArray();
                showLayers(layerEntity, items);
                showBg(backgroundInfo);
            }else{
                addImageOrGifFromUrl(false, "https://pics6.baidu.com/feed/d01373f082025aaf0c2b05650a0fa262024f1a6f.jpeg?token=6649fdc0f364b5ff0c55c18cb4e1ff65");
                addImageOrGifFromUrl(false, "http://img.gaoxiaogif.cn/GaoxiaoGiffiles/images/2015/08/08/laobabahaizitanchutanchuang.gif");
                addTextView("新年快乐");
            }
        } catch (Exception e) {
            e.printStackTrace();
            String s = e.toString();
            Logger.d("diy_error:" + s);
        }
}

    private void showBg(LayerBackground backgroundInfo) {
        if (TextUtils.isEmpty(backgroundInfo.imageURLPathFile) && TextUtils
                .isEmpty(backgroundInfo.gifURLPathFile)) {
            //背景是透明图层
            final String colorStr = backgroundInfo.getBackColorString();
            if (!TextUtils.isEmpty(colorStr)) {//背景是纯色
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPsCanvas.showBackground(PsUtil.getColor(colorStr, 0x000000));
                        resetStatus();
                    }
                });
            }
        } else {
            if (!TextUtils.isEmpty(backgroundInfo.getImgURLPath()) ||
                    !TextUtils.isEmpty(backgroundInfo.imageURLPathFile)) {
                //背景是图片
                addLocalMessage(backgroundInfo.getImgURLPath(), backgroundInfo.imageURLPathFile);
            } else {
                //背景是GIf
                addLocalMessage(backgroundInfo.getGifURLPath(), backgroundInfo.gifURLPathFile);
            }

        }
    }

    private void showLayers(LayerEntity layerEntity, List<LayerItem> items) {
        if (items.size() > 0) {//图层信息转化
            for (int i = 0; i < items.size(); i++) {
                LayerItem item = items.get(i);
                PSLayer PSLayer = new PSLayer(this, handler);
                if (item.getContentViewType() != Constant.contentViewType.ContentViewTypeTextbox) {//不是文字图层
                    String path = item.filePath;
                    String url = item.getImageURL();
                    File file = new File(path);
                    if (file.isFile() && file.length() > 0) {
                        String fileType = PsUtil.getFileType(path);
                        Logger.d("fileType=" + fileType);
                        switch (item.getContentViewType()) {
                            //本地素材 绘图 要上传图片
                            case Constant.contentViewType.ContentViewTypeImage:
                            case Constant.contentViewType.ContentViewTypeLocaImage://贴纸图层
                                int imageWidth;
                                int imageHeight;
                                if (PsUtil.isGif(fileType)) {//图片是Gif
                                    CustomGifMovie customGifMovie = new CustomGifMovie(PsActivity.this, false);
                                    boolean res = customGifMovie.setMovieResource(path);
                                    if (res) {//sometimes movie decode gif error url duration = 0
                                        customGifMovie.url = url;
                                        imageWidth = customGifMovie.getViewWidth();
                                        imageHeight = customGifMovie.getViewHeight();
                                        PSLayer.addView(customGifMovie,
                                                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    } else {
                                        CustomGifFrame customGifFrame = new CustomGifFrame(PsActivity.this);
                                        customGifFrame.setMovieResource(path);
                                        customGifFrame.url = url;
                                        imageWidth = customGifFrame.getViewWidth();
                                        imageHeight = customGifFrame.getViewHeight();
                                        PSLayer.addView(customGifFrame,
                                                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    }
                                } else {//图片是静图
                                    CustomImageView imageView = new CustomImageView(this);
                                    imageView.setImageFile(path);
                                    imageWidth = imageView.getViewWidth();
                                    imageHeight = imageView.getViewHeight();
                                    imageView.url = url;
                                    imageView.isTuya = false;
                                    PSLayer
                                            .addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                    .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                }
                                PSLayer.bindMatrixImage(item, videoWidthHeight, offsetX, offsetY,
                                        layerEntity.getWidth(),
                                        layerEntity.getHeight(), imageWidth, imageHeight);
                                break;
                            case Constant.contentViewType.ContentViewTypeDraw://图层是涂鸦，静图，设置标志位isTuya=true
                                CustomImageView customImageView = new CustomImageView(this);
                                customImageView.setImageFile(path);
                                customImageView.url = url;
                                customImageView.isTuya = true;
                                PSLayer
                                        .addView(customImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                PSLayer.bindMatrixImage(item, videoWidthHeight, offsetX, offsetY,
                                        layerEntity.getWidth(),
                                        layerEntity.getHeight(), customImageView.getViewWidth(),
                                        customImageView.getViewHeight());
                                break;
                        }
                        addViewPost(PSLayer, i * 100, i == items.size() - 1);
                    } else {
                        Logger.d("file missed");
                    }
                } else {
                    //图层是文字
                    CustomTextView customTextView = new CustomTextView(this);
                    boolean scaleOrNot = customTextView.setTextEntry(item, layerEntity.getWidth());
                    int duration = 0;
                    int delayVideo = 0;
                    int animationType = 0;
                    float speed = 1.0f;
                    try {//视频时长，用于动效效果
                        animationType = item.animationType;
                        duration = mPsCanvas.backgroundView.getDuration();
                       // delayVideo = mPsCanvas.backgroundView.videoView.getDelay();
                        speed = mPsCanvas.backgroundView.getSpeed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    customTextView.setTextAnimationType(animationType, duration, delayVideo, speed);
                    PSLayer.addView(customTextView,
                            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                                    .LayoutParams.WRAP_CONTENT));
                    PSLayer.bindMatrix(item, videoWidthHeight, offsetX, offsetY, layerEntity.getWidth(),
                            layerEntity.getHeight(),
                            scaleOrNot);
                    addViewPost(PSLayer, i * 100, i == items.size() - 1);
                }
            }
        }
    }

    //post一个一个显示图层，比较好看
    private void addViewPost(final PSLayer PSLayer, int delay, final boolean isLast) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PSLayer.getChildCount() > 0) {
                    mPsCanvas.addView(PSLayer);
                    if (isLast) {
                        resetStatus();
                    }
                }
                if (isLast) {
                    resetStatus();
                }
            }
        }, delay);
    }

    //背景图片 添加
    private void addLocalMessage(final String url, final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(filePath)) {
                    File file = new File(filePath);
                    if (file.isFile() && file.length() > 0) {
                        String fileHeader = FileUtils.getFileHeader(filePath);
                        if (fileHeader.equals(Constant.FileSuffix.PNG) || fileHeader
                                .equals(Constant.FileSuffix.JPG)) {
                            mPsCanvas.showImage(url, filePath);
                        } else {
                            //格式异常了
                            if (filePath.endsWith(".gif") || FileUtils.isGifFile(filePath)) {
                                mPsCanvas.showGIf(url, filePath);
                            } else {
                                mPsCanvas.showImage(url, filePath);
                            }
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                              //  ToastUtil.show("文件丢失");
                            }
                        });
                    }
                    resetStatus();
                }
            }
        }).start();
    }


    private boolean editTextView(View view) {
        if (view != null && view instanceof CustomTextView) {
            CustomTextView obj = (CustomTextView) view;
            String text = obj.getText();
            addTextPopInput(text);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mPsCoverView.getChildCount() > 0) {
            isEditText = false;
            if (mPsCoverView != null) {
                mPsCoverView.removeAllViews();
            }
            return;
        }
        super.onBackPressed();
    }

    PopInputLayout popInputLayout;
    boolean isEditText;
    //文字编辑弹窗
    private void addTextPopInput(String text) {
        mPsCoverView.removeAllViews();
        popInputLayout = new PopInputLayout(PsActivity.this) {
            @Override
            public void onRemove() {
                isEditText = false;
                if (mPsCoverView != null) {
                    mPsCoverView.removeAllViews();
                }
            }

            @Override
            public void onTextConfirm(String text) {
                if (mPsCoverView != null) {
                    mPsCoverView.removeAllViews();
                }
                //编辑文字
                if (isEditText) {
                    mPsCanvas.editText(text);
                    isEditText = false;
                } else {
                    float aspect = videoWidthHeight * 1f / mPsCanvas.getHeight();
                    float v = (1f - aspect) / 2 + aspect;
                    CustomTextView textStyleView = new CustomTextView(PsActivity.this);
                    if (mPsCanvas.backgroundView.isBackgroundVideoImageViewVisible()) {
                        textStyleView.setPaintColorReverse(Color.WHITE, Color.BLACK);
                        textStyleView.setisDiyBottom(true);
                        textStyleView.setisDiyBottomHeightPercent(v);
                        textStyleView.setOffsetY4Diy(offsetY);
                    } else {
                        textStyleView.setisDiyBottom(false);
                        textStyleView.setPaintColorReverse(Color.BLACK, Color.WHITE);
                    }
                    textStyleView.setText(text);
                    addLayer(textStyleView);
                }
            }
        };
        if (TextUtils.isEmpty(text)) {
            isEditText = false;
        } else {
            popInputLayout.setText(text);
            isEditText = true;
        }
        mPsCoverView.addView(popInputLayout);
    }

    private void addLayer(View view) {
        PSLayer scaleView = new PSLayer(this);
        scaleView.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        mPsCanvas.addView(scaleView);
        resetStatus();
    }

    private float getInitScale(ILayer scaleView) {
        float width = scaleView.getViewWidth() * 1.0f;
        if (width >= Constant.DIY_STICKER_BASE_SIZE) {
            return videoWidthHeight / width;
        } else {
            return videoWidthHeight / Constant.DIY_STICKER_BASE_SIZE;
        }
    }


    private void addView(View view, float scale) {
        PSLayer PSLayer = new PSLayer(this);
        PSLayer.scaleInit(scale);
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        mPsCanvas.addView(PSLayer);
        resetStatus();
    }

    private void addView(View view) {
        PSLayer PSLayer = new PSLayer(this);
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        mPsCanvas.addView(PSLayer);
        resetStatus();
    }


    /**
     * 重置操作状态，用于刷新所有状态
     */
    public void resetStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (customCanvas.isEnable()) {
                    iv_undo.setImageResource(
                            customCanvas.canUndo() ? R.drawable.selector_crop_pre : R.mipmap.icon_op_pre_sel);
                    iv_redo.setImageResource(
                            customCanvas.canRedo() ? R.drawable.selector_crop_pro : R.mipmap.icon_op_pro_sel);
                    iv_delete.setImageResource(R.mipmap.icon_top_delete_sel);
                    return;
                }

                iv_delete.setImageResource(
                        isCanvasEmpty() ? R.drawable.selector_crop_clear : R.mipmap.icon_top_delete_sel);
                iv_undo.setImageResource(
                        mPsCanvas.canPre() ? R.drawable.selector_crop_pre : R.mipmap.icon_op_pre_sel);
                iv_redo.setImageResource(
                        mPsCanvas.canPro() ? R.drawable.selector_crop_pro : R.mipmap.icon_op_pro_sel);
                mPsbgColorGroup.resetRgAutoBg();
                resetBottomStatus();
            }
        });
    }

    /**
     * 刷新底部操作状态
     */
    private void resetBottomStatus() {
        View view = mPsCanvas.getFocusView();
        if (view != null && view instanceof CustomTextView) {
            bottomTextLayout.show();
            bottomTextLayout.select(((CustomTextView) view).currentType);
        }else{
            bottomTextLayout.hide();
        }
    }


    /**
     * 是否允许删除, 画布是否是空的
     */
    public boolean isCanvasEmpty() {
        try {
            ImageView picture = mPsCanvas.backgroundView.picture;
            if (mPsCanvas.getChildCount() == 0 && (
                    picture.getVisibility() !=
                            View.VISIBLE)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("diy>> onDestroy");
        try {
            if (mPsCanvas != null) {
                FileUtils.saveSerialData(this, "layerInfo", mPsCanvas.getLayerMessage());
                mPsCanvas.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }


    private BottomPaintLayout bottomPaintLayout;
    private BottomTextLayout bottomTextLayout;
    private void initBottomLayout() {

        bottomTextLayout = new BottomTextLayout(this) {
            @Override
            public void onItemClick(int position) {
                mPsCanvas.setTextAnimationType(position);
            }
        };

        bottomPaintLayout = new BottomPaintLayout(this) {
            @Override
            public void onClose() {
                finishTuya();
            }

            @Override
            public void onColorChange(int color) {
                // 设置画笔颜色
                setBrushColor(color, false);
            }

            @Override
            public void onClear() {
                addEraser();
            }

            @Override
            public void onMosaic() {
                addMosaic();
            }

            @Override
            public void onBitmapMode(int drawableId) {
                setBrushColor(BitmapFactory.decodeResource(getResources(), drawableId));
            }

            @Override
            public void onColorShape(int color) {
                // 设置画笔颜色
                setBrushColor(color, true);
            }

            @Override
            public void onPaintSizeChange(float size) {
                customCanvas.setPaintSize(size);
            }
        };
    }

    //橡皮擦模式开启
    private void addEraser() {
        customCanvas.setEraserMode();
    }

    //马赛克模式开启
    private void addMosaic() {
        if (!customCanvas.isEnable()) {
            startTuya();
        }
        Bitmap srcBitmap = mPsCanvas.getCacheBitmap();
        Bitmap bit = MosaicUtil.getMosaic(srcBitmap);
        PsUtil.recycleBitmaps(srcBitmap);
        customCanvas.setMosaic(bit);
    }

    public void setBrushColor(int color, boolean isMask) {
        customCanvas.setColor(color, isMask);
    }

    public void setBrushColor(Bitmap bitmap) {
        customCanvas.setColor(bitmap);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            closeTuya();
            bottomPaintLayout.hide();
        } else if (i == R.id.bt_paint) {
            startTuya();
            bottomPaintLayout.show();
        }else if (i == R.id.bt_image) {
            int poi = new Random().nextInt(2);
            if (poi == 1) addImageOrGifFromUrl(false, "https://pics6.baidu.com/feed/d01373f082025aaf0c2b05650a0fa262024f1a6f.jpeg?token=6649fdc0f364b5ff0c55c18cb4e1ff65");
            else    addImageOrGifFromUrl(false, "http://img.gaoxiaogif.cn/GaoxiaoGiffiles/images/2015/08/08/laobabahaizitanchutanchuang.gif");

        }  else if (i == R.id.bt_text) {
            View view = mPsCanvas.getFocusView();
            if (view != null && view instanceof CustomTextView) {
                bottomTextLayout.show();
                bottomTextLayout.select(((CustomTextView) view).currentType);
            }else{
                addTextPopInput("");
            }
        } else if (i == R.id.iv_confirm) {
            finishTuya();
            bottomPaintLayout.hide();
        } else if (i == R.id.iv_back) {
            back();

        } else if (i == R.id.iv_next) {
            if (mPsCanvas.getChildCount() == 0) {
                if (mPsCanvas.backgroundView.currentOperate != null) {
                    if (mPsCanvas.backgroundView.currentOperate.visible1 == View.VISIBLE) {
                        //ToastUtil.show("画板上没有东西");
                        return;
                    }
                }
            }
            int type;
            if (mPsCanvas.hasGif()) {
                type = Constant.ShareOriginalType.Gif;
            } else {
                type = Constant.ShareOriginalType.Photo;
            }
            mPsCanvas.start(type, new GifMaker.OnGifMakerListener() {
                @Override
                public void onMakeProgress(int index, int count) {
                    Logger.LOG("onMakeProgress" + index);
                }

                @Override
                public void onMakeGifSucceed(String outPath) {
                    Logger.LOG(outPath);
                    CropActivity.start(PsActivity.this, outPath);
                }

                @Override
                public void onMakeGifFail() {
                    Logger.LOG("onMakeGifFail");
                }
            });
        }  else if (i == R.id.iv_undo) {//我们得知道最后的状态，然后对StickerLayer进行相应对处理
            undo();
        } else if (i == R.id.iv_redo) {
            redo();
        } else if (i == R.id.iv_delete) {
            if (!isCanvasEmpty()) {
                return;
            }
            if (customCanvas.isEnable()) {
                return;
            }
            delete();

        }
    }

    //重做
    private void redo() {
        if (customCanvas.isEnable()) {
            if (customCanvas.canRedo()) {
                customCanvas.redo();
            }
            resetStatus();
            return;
        }
        if (mPsCanvas.canPro()) {
            mPsCanvas.pro();
        }
        resetStatus();
    }

    //撤销
    private void undo() {
        if (customCanvas.isEnable()) {
            if (customCanvas.canUndo()) {
                customCanvas.undo();
            }
            resetStatus();
            return;
        }
        if (mPsCanvas.canPre()) {
            mPsCanvas.pre();
        }
        resetStatus();
    }

    //返回键
    private void back() {
        if (mPsCoverView.getChildCount() > 0) {
            mPsCoverView.removeAllViews();
            return;
        }
        finish();
    }

    //涂鸦完成，生成图片添加到画布
    public void finishTuya() {
        statesbarGoInEditMode(false);
        try {
            String filePath = customCanvas.getCacheFilePath();
            if (filePath != null) {
                float[] center = customCanvas.getBitmapcenter();
                float centerX = (center[0] + center[1]) / 2;
                float centerY = (center[2] + center[3]) / 2;
                addImageViewFromFile(true, null, filePath, centerX, centerY);
            }
            customCanvas.clear();
            customCanvas.changeStatus(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resetStatus();
    }


    private void addImageViewFromFile(boolean isTuya, String url, String filePath, float x, float y) {
        CustomImageView imageView = new CustomImageView(this);
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

    public void addImageOrGifFromUrl(boolean saveHistory, final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        final File file = FileManager.getDiyMaterialFile(this,url.hashCode() + "", PsUtil.getFileTri(url));
        if (file == null) {
            return;
        }
        Logger.d("1--->"+ file.length());
        if (file.length() > 0) {
            addImageOrGifFromFile(false, url, file.toString());
        } else {
            Logger.d("下载素材中，请稍候"+ file.length());
           // ToastUtil.show("下载素材中，请稍候");
            DownloadAPI.downloadFile(url, file, new DownloadAPI.IDownloadListener() {
                @Override
                public void onCompleted() {
                    Logger.d("2--->"+ file.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (file.length() > 0) {
                                addImageOrGifFromFile(false, url, file.toString());
                            }
                        }
                    });
                }

                @Override
                public void onError() {
                    Logger.d("onError--->"+ file.length());
                    file.deleteOnExit();
                   // ToastUtil.show("下载失败，请稍候重试");
                }

            });
        }
    }

    public void addImageOrGifFromFile(boolean saveHistory, String url, String filePath) {
        String fileType = PsUtil.getFileType(filePath);
        Logger.d("fileType>> " + fileType);
        if (PsUtil.isGif(fileType)) {
            CustomGifMovie customGifMovie = new CustomGifMovie(PsActivity.this, false);
            boolean res = customGifMovie.setMovieResource(filePath);
            if (res) {//sometimes movie decode gif error url duration = 0
                customGifMovie.url = url;
                addView(customGifMovie, getInitScale(customGifMovie));
            } else {
                CustomGifFrame customGifFrame = new CustomGifFrame(PsActivity.this);
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

    private void addTextView(String text){
        float aspect = videoWidthHeight * 1f / mPsCanvas.getHeight();
        float v = (1f - aspect) / 2 + aspect;
        //
        CustomTextView customTextView = new CustomTextView(PsActivity.this);
        if (mPsCanvas.backgroundView.isBackgroundVideoImageViewVisible()) {
            customTextView.setPaintColorReverse(Color.WHITE, Color.BLACK);
            customTextView.setisDiyBottom(true);
            customTextView.setisDiyBottomHeightPercent(v);
            customTextView.setOffsetY4Diy(offsetY);
        } else {
            customTextView.setisDiyBottom(false);
            customTextView.setPaintColorReverse(Color.BLACK, Color.WHITE);
        }
        customTextView.setText(text);
        addView(customTextView);
    }

    //取消涂鸦
    private void closeTuya() {
        customCanvas.clear();
        customCanvas.changeStatus(false);
        statesbarGoInEditMode(false);
        resetStatus();
    }

    //开始涂鸦
    public void startTuya() {
        customCanvas.setCallback(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatus();
            }
        });
        customCanvas.changeStatus(true);
        statesbarGoInEditMode(true);
        mPsCanvas.deleteFocus();
        resetStatus();
    }

    //清空画布
    private void delete() {
        if (customCanvas.isEnable()) {
            return;
        }
        mPsCanvas.reset();
        statesbarGoInEditMode(false);
        resetStatus();
    }
    private void statesbarGoInEditMode(boolean isedit) {
        iv_back.setVisibility(isedit ? View.GONE : View.VISIBLE);
        iv_next.setVisibility(isedit ? View.GONE : View.VISIBLE);
        iv_confirm.setVisibility(isedit ? View.VISIBLE : View.GONE);
        iv_close.setVisibility(isedit ? View.VISIBLE : View.GONE);
    }
}
