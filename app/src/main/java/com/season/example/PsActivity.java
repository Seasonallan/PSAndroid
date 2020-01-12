package com.season.example;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.season.lib.bean.LayerItem;
import com.season.lib.view.ps.CustomGifFrame;
import com.season.lib.util.Util;
import com.season.lib.view.ps.ILayer;
import com.season.lib.util.Logger;
import com.season.lib.http.DownloadAPI;
import com.season.example.layout.TextLayout;
import com.season.lib.view.ps.CustomGifMovie;
import com.season.lib.bean.LayerBackground;
import com.season.lib.bean.LayerEntity;
import com.season.lib.view.ps.PSBackground;
import com.season.lib.view.ps.PSLayer;
import com.season.lib.util.Constant;
import com.season.lib.util.FileManager;
import com.season.lib.util.FileUtils;
import com.season.lib.util.ScreenUtils;
import com.season.lib.view.ColorPickView;
import com.season.lib.view.ps.PSCanvas;
import com.season.example.layout.TopDeleteLayout;
import com.season.lib.view.ps.PSCoverView;
import com.season.lib.view.ps.CustomImageView;
import com.season.lib.view.ps.CustomTextView;
import com.season.lib.view.ps.CustomCanvas;
import com.season.myapplication.BuildConfig;
import com.season.myapplication.R;
import com.season.lib.util.AutoUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PsActivity extends Activity implements View.OnClickListener {

    PSCoverView mLayoutContainer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ps);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mLayoutContainer = findViewById(R.id.layout_container);
        opview = findViewById(R.id.opview);
        rl_op_top = findViewById(R.id.rl_op_top);
        ll_op_bottom = findViewById(R.id.ll_op_bottom);
        opviewContainer = findViewById(R.id.opviewContainer);
        del_container = findViewById(R.id.del_container);
        mStickerLayout = findViewById(R.id.layout_stickLayout);
        customCanvas = findViewById(R.id.tuya);
        iv_redo = findViewById(R.id.iv_redo);
        iv_undo = findViewById(R.id.iv_undo);
        iv_delete = findViewById(R.id.iv_delete);
        rgAutoBg = findViewById(R.id.rg_auto_bg);
        vColor = findViewById(R.id.v_color);
        rb_vid_pic = findViewById(R.id.rb_vid_pic);

        iv_back = findViewById(R.id.iv_back);
        iv_next = findViewById(R.id.iv_next);
        iv_share = findViewById(R.id.iv_share);
        iv_close = findViewById(R.id.iv_close);
        iv_confirm = findViewById(R.id.iv_confirm);
        iv_lable = findViewById(R.id.iv_lable);

        initListener();

        initBg();
        initStickerLayout();
        opview.post(new Runnable() {
            @Override
            public void run() {
                initUI();
                initData();
            }
        });
    }

    ImageView iv_back;
    ImageView iv_next;
    ImageView iv_share;
    ImageView iv_close;
    ImageView iv_confirm;

    int videoWidthHeight, offsetX, offsetY;
    PSCanvas mStickerLayout;
    View rl_op_top;
    View ll_op_bottom;
    LinearLayout del_container;

    View opview;View opviewContainer;
    CustomCanvas customCanvas;
    ImageView iv_redo;
    ImageView iv_undo;
    ImageView iv_delete;
    RadioGroup rgAutoBg;
    ImageView vColor;View iv_lable;
    ColorPickView rb_vid_pic;
    private void initUI() {
        int height = opview.getHeight();
        int screenWidth = ScreenUtils.getScreenWidth(this);
        videoWidthHeight = Math.min(height, screenWidth);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) opview.getLayoutParams();
        params.width = videoWidthHeight;
        params.height = videoWidthHeight;
        opview.requestLayout();
        if (videoWidthHeight >= screenWidth) {//高度足够，适配宽度显示为正方形。
            int opviewContainerHeight = opviewContainer.getHeight();
            offsetX = 0;
            offsetY = rl_op_top.getHeight() + (opviewContainerHeight - videoWidthHeight) / 2;

            RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) rl_op_top
                    .getLayoutParams();
            topParams.height = offsetY;
            rl_op_top.requestLayout();

            RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) ll_op_bottom
                    .getLayoutParams();
            bottomParams.height =
                    ll_op_bottom.getHeight() + (opviewContainerHeight - videoWidthHeight) / 2;
            ll_op_bottom.requestLayout();

        } else {//高度不够，宽度适配高度显示为正方形。可在PAD上测试
            offsetX = (screenWidth - videoWidthHeight) / 2;
            offsetY = rl_op_top.getHeight();
        }

        RelativeLayout.LayoutParams delParams = (RelativeLayout.LayoutParams) del_container
                .getLayoutParams();
        delParams.height = Math.max(offsetY, AutoUtils.getPercentWidthSize(128));
        del_container.requestLayout();

        mStickerLayout.videoWidthHeight = videoWidthHeight;
        mStickerLayout.setOffsetX(offsetX);
        mStickerLayout.setOffsetY(offsetY);
        Logger.LOG(""+ offsetX +"----"+ offsetY);
    }

    private void initData() {
        initViewFromData(false, false, true);
    }

    private Handler handler = new Handler();

    //图层信息转化为画布视图
    //来自预览界面就copyfile,saveHistory，但是不saveOrignal
    private void initViewFromData(boolean copyFile, boolean saveHistory,
                                  boolean saveOrignal) {

        try {
            LayerEntity  layerEntity = null;
            Object localData = FileUtils.getSerialData(this, "layerInfo");
            if (localData instanceof  LayerEntity){
                layerEntity = (LayerEntity) localData;
            }
            if (layerEntity != null){
                LayerBackground backgroundInfo = layerEntity.getBackInfoModel();
                List<LayerItem> items = layerEntity.getItemArray();
//            filePath = backgroundInfo.assetPathFile;
//            String bgUrl = backgroundInfo.getAssetPath();
                /**
                 * 把TAG保存到Container类中，跳转到发布的时候，需要重新构造LayerEntity
                 */
                mStickerLayout.relateType = layerEntity.relateType;
                mStickerLayout.audioId = layerEntity.audioId;
                mStickerLayout.type = layerEntity.type;
                //保存文字描述
                mStickerLayout.textPublishDescribe = layerEntity.getTextPublishDescribe();
                if (saveOrignal) {
                    mStickerLayout.orignalImageUrl = backgroundInfo.getImgURLPath();
                    mStickerLayout.orignalVideoUrl = backgroundInfo.getAssetPath();
                    mStickerLayout.orignalGifUrl = backgroundInfo.getOrignalGIfUrl();
                    mStickerLayout.originalId = layerEntity.originalId;
                    mStickerLayout.originId = layerEntity.originId;
                }
                showLayers(layerEntity, items);
                showBg(copyFile, saveHistory, backgroundInfo);
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

    private void showBg(boolean copyFile, boolean saveHistory, LayerBackground backgroundInfo) {
        if (!TextUtils.isEmpty(backgroundInfo.assetPathFile)) {
            //背景是视频
            String videoFilePath = backgroundInfo.assetPathFile;
            String url = backgroundInfo.getAssetPath();
            if (copyFile) {//此处代表是否拷贝视频到新地址，因为拍摄的视频filePath都是一样的，所以要拷贝用于历史记录
                File desFile = FileManager.getDiyBackgroundFile(this, null, "mp4");
                if (desFile != null) {
                    Util.copyFile(videoFilePath, desFile.toString());
                    addLocalMessageUio(url, desFile.toString(), true, backgroundInfo.getRate(), saveHistory);
                } else {
                    addLocalMessageUio(url, videoFilePath, true, backgroundInfo.getRate(), saveHistory);
                }
                //ProgressDialogUtils.Show(PsActivity.this, "视频处理中，请稍候");
                //addKeyFrameMp4(bgUrl, filePath, true, backgroundInfo.getRate(), saveHistory);
            } else {
                addLocalMessageUio(url, videoFilePath, true, backgroundInfo.getRate(), saveHistory);
            }
        } else {
            //背景不是视频
            if (TextUtils.isEmpty(backgroundInfo.imageURLPathFile) && TextUtils
                    .isEmpty(backgroundInfo.gifFilePath)) {
                //背景是透明图层,底图为null
                final String colorStr = backgroundInfo.getBackColorString();
                if (!TextUtils.isEmpty(colorStr)) {//背景是纯色
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStickerLayout.showBackground(Util.getColor(colorStr, 0x000000));
                            resetStatus();
                        }
                    });
                }

            } else {
                //来自预览界面，
                if (!TextUtils.isEmpty(backgroundInfo.imageURLPathFile)) {
                    //背景是图片
                    String imagepath = backgroundInfo.imageURLPathFile;
                    if (copyFile) {//此处代表是否拷贝图片到新地址，因为相册选取的图片filePath都是一样的，所以要拷贝用于历史记录
                        File desFile = FileManager
                                .getDiyBackgroundFile(this, null, Util.getFileTri(backgroundInfo.imageURLPathFile));
                        if (desFile != null) {
                            Util.copyFile(imagepath, desFile.toString());
                            addLocalMessageUio("", desFile.toString(), false, backgroundInfo.getRate(),
                                    saveHistory);
//                            addLocalMessageUio(bgUrl, desFile.toString(), false, backgroundInfo.getRate(), saveHistory);
                        } else {
                            addLocalMessageUio("", imagepath, false, backgroundInfo.getRate(), saveHistory);
//                            addLocalMessageUio(bgUrl, filePath, false, backgroundInfo.getRate(), saveHistory);
                        }
                    } else {
                        //解决 改图重复上传本地资源bug，因为把视频bgurl给了图片，使得网络url变成""
                        addLocalMessageUio(backgroundInfo.getImgURLPath(), imagepath, false,
                                backgroundInfo.getRate(), saveHistory);
                    }
                } else {
                    //背景是GIf
                    String gifFilePath = backgroundInfo.getGifFilePath();
                    if (copyFile) {//此处代表是否拷贝图片到新地址，因为相册选取的图片filePath都是一样的，所以要拷贝用于历史记录
                        File desFile = FileManager.getDiyBackgroundFile(this, null, Util.getFileTri(gifFilePath));
                        if (desFile != null) {
                            Util.copyFile(gifFilePath, desFile.toString());
                            addLocalMessageUio("", desFile.toString(), false, backgroundInfo.getRate(),
                                    saveHistory);
//                            addLocalMessageUio(bgUrl, desFile.toString(), false, backgroundInfo.getRate(), saveHistory);
                        } else {
                            addLocalMessageUio("", gifFilePath, false, backgroundInfo.getRate(), saveHistory);
//                            addLocalMessageUio(bgUrl, filePath, false, backgroundInfo.getRate(), saveHistory);
                        }
                    } else {
                        addLocalMessageUio(backgroundInfo.getGifURLPath(), gifFilePath, false,
                                backgroundInfo.getRate(), saveHistory);
                    }
                }

            }
        }
    }

    private void showLayers(LayerEntity layerEntity, List<LayerItem> items) {
        if (items.size() > 0) {//图层信息转化
            for (int i = 0; i < items.size(); i++) {
                int position = i;
                LayerItem item = items.get(i);
                PSLayer PSLayer = new PSLayer(this, handler);
                if (item.getContentViewType() != Constant.contentViewType.ContentViewTypeTextbox) {//不是文字图层
                    String path = item.filePath;
                    String url = item.getImageURL();
                    File file = new File(path);
                    if (file.isFile() && file.length() > 0) {
                        String fileType = Util.getFileType(path);
                        Logger.d("fileType=" + fileType);
                        switch (item.getContentViewType()) {
                            //本地素材 绘图 要上传图片
                            case Constant.contentViewType.ContentViewTypeImage:
                            case Constant.contentViewType.ContentViewTypeLocaImage://贴纸图层
                                int imageWidth;
                                int imageHeight;
                                if (Util.isGif(fileType)) {//图片是Gif
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
                       // animationType = item.getTextStyleModel().animationType;
                        duration = mStickerLayout.backgroundView.getDuration();
                       // delayVideo = mStickerLayout.backgroundView.videoView.getDelay();
                        speed = mStickerLayout.backgroundView.getSpeed();
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
                    mStickerLayout.addView(PSLayer);
                    if (isLast) {
                        resetStatus();
                        checkSelectedPosition(PSLayer.getChildAt(0));
                    }
                }
                if (isLast) {
                    resetStatus();
                }
            }
        }, delay);
    }

    private void checkSelectedPosition(View view) { // 底部操作栏位置
    }

    private void addLocalMessageUio(String url, String filePath, boolean isMp4, float rate,
                                    boolean saveLocal) {
        if (BuildConfig.DEBUG) {
            Logger.d("addLocalMessageUio:" + System.currentTimeMillis());
        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        addLocalMessage(url, filePath, isMp4, rate, saveLocal);
//            }
//        });
    }

    //背景视频或图片 添加
    private void addLocalMessage(final String url, final String filePath, final boolean isMp4, final float rate,
                                 final boolean saveLocal) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Logger.d("addLocalMessage,url:" + url + ",filePath:" + filePath);
                    Logger.d("addLocalMessage,1:" + System.currentTimeMillis());
                }
                if (!TextUtils.isEmpty(filePath)) {
                    File file = new File(filePath);
                    if (file.isFile() && file.length() > 0) {
                        int lastDot = filePath.lastIndexOf(".");
                        String name = filePath.substring(lastDot + 1).toUpperCase();
                        if (isMp4 || name.equals("MP4") || FileUtils.isMp4File(filePath)) {
//                if (name.equals("MP4")) {
                            mStickerLayout.showVideoView(url, filePath, rate);
                            resetStatus();
                        } else {
                            // 暂时没有GIF背景需求
                            String fileHeader = FileUtils.getFileHeader(filePath);
                            if (fileHeader.equals(Constant.FileSuffix.PNG) || fileHeader
                                    .equals(Constant.FileSuffix.JPG)) {
                                mStickerLayout.showImage(url, filePath);
                                Logger.d("addLocalMessage,2:" + System.currentTimeMillis());
                            } else {
                                Logger.d("addLocalMessage,3:" + System.currentTimeMillis());
                                //格式异常了
                                if (filePath.endsWith(".gif") || FileUtils.isGifFile(filePath)) {
                                    mStickerLayout.showGIf(url, filePath);
                                } else {
                                    mStickerLayout.showImage(url, filePath);
                                }
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

    private void initBg() {
        mLayoutContainer.setOnActionUpListener(new PSCoverView.OnUpListener() {
            @Override
            public void onActionUp(float x, float y) {
                //如果选择背景是展开的，关闭它
                Logger.LOG("setOnActionUpListener "+ rgAutoBg.getVisibility() );
                if (rgAutoBg.getVisibility() == View.VISIBLE) {
                    int[] location = new int[2];
                    rgAutoBg.getLocationOnScreen(location);
                    int left = location[0];
                    int top = location[1];
                    int right = left + rgAutoBg.getMeasuredWidth();
                    int bottom = top + rgAutoBg.getMeasuredHeight();
                    if (y >= top && y <= bottom && x >= 0 && x <= right) {
                        Logger.LOG("N ");
                    } else {
                        vColor.setVisibility(View.VISIBLE);
                        rgAutoBg.setVisibility(View.GONE);
                    }
                }
            }
        });
        rgAutoBg.getLayoutParams().height = AutoUtils.getPercentWidthSize(47);
        rgAutoBg.requestLayout();
        //改背景
        rgAutoBg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setCanvaBackGround(checkedId);
                resetStatus();
            }
        });
    }

    TopDeleteLayout topDeleteLayout;
    private void initStickerLayout() {
        mStickerLayout.bindBgView(opview, new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
               // resetTextFragment();
            }
        });
        topDeleteLayout = new TopDeleteLayout(del_container, iv_back, iv_share, iv_next);
        mStickerLayout.setOnMoveListener(new PSCanvas.IOnMoveListener() {
            @Override
            public boolean onMove(MotionEvent event) {
                return topDeleteLayout.checkPosition(event);
            }

            @Override
            public void onMoveEnd() {
                topDeleteLayout.hide();
            }
        });
        initStickerlayoutOnClick();
    }

    private void initStickerlayoutOnClick() {
        mStickerLayout.setClickListener(new PSLayer.OnClickListener() {
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
        mStickerLayout.setFocusChangeListener(new PSCanvas.IFocusChangeListener() {
            @Override
            public void onFocusLose(ViewGroup view) {
            }

            @Override
            public void onFocusGet(ViewGroup parent) {
                resetBottomStatus();
                if (parent.getChildCount() > 0) {
                    checkSelectedPosition(parent.getChildAt(0));
                }
            }

            @Override
            public void onFocusClear() {
              //  mTextFragment.closeOpened();
                resetBottomStatus();
            }
        });
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

    TextLayout textLayout;
    boolean isEditText;
    //文字编辑弹窗
    private void addTextPopInput(String text) {
        mLayoutContainer.removeAllViews();
        textLayout = new TextLayout(PsActivity.this, "");
        if (TextUtils.isEmpty(text)) {
            isEditText = false;
        } else {
            textLayout.setText(text);
            isEditText = true;
        }
        mLayoutContainer.addView(textLayout);
    }









    public void addGifFromFile(String filePath) {
        CustomGifMovie customGifMovie = new CustomGifMovie(PsActivity.this, false);
        boolean res = customGifMovie.setMovieResource(filePath);
        if (res) {//sometimes movie decode gif error url duration = 0
//                gifMovieView.url = url;
            addView(customGifMovie, getInitScale(customGifMovie));
        } else {
            CustomGifFrame customGifFrame = new CustomGifFrame(PsActivity.this);
            customGifFrame.setMovieResource(filePath);
//                gifFrameView.url = url;
            addView(customGifFrame, getInitScale(customGifFrame));
        }
//        if (saveHistory && mImageFragment != null && !TextUtils.isEmpty(filePath)) {
//            mImageFragment.addImageHistory(filePath);
//        }
    }

    //TODO
    private float getInitScale(ILayer scaleView) {
        float width = scaleView.getViewWidth() * 1.0f;
        if (width >= Constant.Camerasettings.DIY_STICKER_BASE_SIZE) {
            return videoWidthHeight / width;
        } else {
            return videoWidthHeight / Constant.Camerasettings.DIY_STICKER_BASE_SIZE;
        }
    }


    private void addView(View view, float scale) {
        PSLayer PSLayer = new PSLayer(this);
        PSLayer.scaleInit(scale);
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        mStickerLayout.addView(PSLayer);
        resetStatus();
    }

    private void addView(View view) {
        PSLayer PSLayer = new PSLayer(this);
//        scaleView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        PSLayer.addView(view,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT));
        mStickerLayout.addView(PSLayer);
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
                        isDelete() ? R.drawable.selector_crop_clear : R.mipmap.icon_top_delete_sel);
                iv_undo.setImageResource(
                        mStickerLayout.canPre() ? R.drawable.selector_crop_pre : R.mipmap.icon_op_pre_sel);
                iv_redo.setImageResource(
                        mStickerLayout.canPro() ? R.drawable.selector_crop_pro : R.mipmap.icon_op_pro_sel);
                resetRgAutoBg();
                resetBottomStatus();
            }
        });
    }

    /**
     * 刷新底部操作状态
     */
    private void resetBottomStatus() {
       // mLayerFragment.reset(mStickerLayout.getViewIndex(), 0, mStickerLayout.getChildCount() - 1);
    }

    private void resetRgAutoBg() {
        PSBackground.BgOperate op = mStickerLayout.backgroundView.currentOperate;
        if (op != null) {
            int checkId = R.id.rb_vid_pic;
            if (op.visible1 == View.VISIBLE) {
                int color = op.color;
                checkId = R.id.rb_translate;
                if (color == Color.WHITE) {
                    checkId = R.id.rb_white;
                } else if (color == Color.TRANSPARENT) {
                    checkId = R.id.rb_translate;
                } else if (color == Color.GRAY) {
                    checkId = R.id.rb_hui;
                } else if (color == Color.BLACK) {
                    checkId = R.id.rb_black;
                }
            } else {
                if (rgAutoBg.getCheckedRadioButtonId() == checkId) {
                    setCanvaBackGround(checkId);
                }
            }
            ((RadioButton) rgAutoBg.findViewById(checkId)).setChecked(true);
        }
    }

    private void setCanvaBackGround(int i) {
        if (i == R.id.rb_white) {
            vColor.setImageResource(R.mipmap.icon_color_white);
            //setBrushColor(Color.WHITE, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mStickerLayout.showBackground(Color.WHITE);
            }

        } else if (i == R.id.rb_translate) {
            vColor.setImageResource(R.mipmap.icon_color_transparent);
            //setBrushColor(Color.TRANSPARENT, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mStickerLayout.showBackground(Color.TRANSPARENT);
            }

        } else if (i == R.id.rb_hui) {
            vColor.setImageResource(R.mipmap.icon_color_gray);
            //setBrushColor(Color.GRAY, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mStickerLayout.showBackground(Color.GRAY);
            }

        } else if (i == R.id.rb_black) {
            vColor.setImageResource(R.mipmap.icon_color_black);
            //setBrushColor(Color.BLACK, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mStickerLayout.showBackground(Color.BLACK);
            }

        } else if (i == R.id.rb_vid_pic) {
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mStickerLayout.showVideoOrImage();
            }
            if (mStickerLayout.backgroundView.currentOperate != null) {
                if (mStickerLayout.backgroundView.currentOperate.bitmap != null) {
                    rb_vid_pic.setDrawImage(mStickerLayout.backgroundView.currentOperate.bitmap);
                    vColor.setImageBitmap(mStickerLayout.backgroundView.currentOperate.bitmap);
                    rb_vid_pic.setVisibility(View.VISIBLE);
                }
            }
        }
        vColor.setVisibility(View.VISIBLE);
        rgAutoBg.setVisibility(View.GONE);
    }
    /**
     * 是否允许删除
     */
    public boolean isDelete() {
        try {
            ImageView picture = mStickerLayout.backgroundView.picture;
            if (mStickerLayout.getChildCount() == 0 && (
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
            if (mStickerLayout != null) {
                LayerEntity layerEntity = mStickerLayout.getLayerMessage();
                if (!TextUtils.isEmpty(getIntent().getStringExtra(mStickerLayout.textPublishDescribe))) {
                    layerEntity.setTextPublishDescribe(mStickerLayout.textPublishDescribe);
                }
                FileUtils.saveSerialData(this, "layerInfo", layerEntity);
                mStickerLayout.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            closeTuya();
        } else if (i == R.id.iv_confirm) {
            finishTuya();

        } else if (i == R.id.iv_lable || i == R.id.v_color) {
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                rgAutoBg.setVisibility(View.GONE);
                vColor.setVisibility(View.VISIBLE);
                //                    ivCanvasSize.setVisibility(View.VISIBLE);
            } else {
                rgAutoBg.setVisibility(View.VISIBLE);
                vColor.setVisibility(View.GONE);
                //                    ivCanvasSize.setVisibility(View.GONE);
            }

        } else if (i == R.id.iv_back) {
            back();

        } else if (i == R.id.iv_next) {

        } else if (i == R.id.iv_share) {//                isSaveVideo = false;
            if (mStickerLayout.getChildCount() == 0) {
                if (mStickerLayout.backgroundView.currentOperate != null) {
                    if (mStickerLayout.backgroundView.currentOperate.visible1 == View.VISIBLE) {
                        //ToastUtil.show("画板上没有东西");
                        return;
                    }
                }
            }
            int type;
            if (mStickerLayout.hasGif()) {
                type = Constant.ShareOriginalType.Gif;
            } else {
                type = Constant.ShareOriginalType.Photo;
            }
            if (BuildConfig.DEBUG) {
                Logger.d("sharetype==" + type);
            }
        } else if (i == R.id.iv_undo) {//我们得知道最后的状态，然后对StickerLayer进行相应对处理
            undo();
        } else if (i == R.id.iv_redo) {
            redo();
        } else if (i == R.id.iv_delete) {
            if (!isDelete()) {
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
        if (mStickerLayout.canPro()) {
            mStickerLayout.pro();
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
        if (mStickerLayout.canPre()) {
            mStickerLayout.pre();
        }
        resetStatus();
    }

    //返回键
    private void back() {
        if (mLayoutContainer.getChildCount() > 0) {
            mLayoutContainer.removeAllViews();
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
        final File file = FileManager.getDiyMaterialFile(this,url.hashCode() + "", Util.getFileTri(url));
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
        String fileType = Util.getFileType(filePath);
        Logger.d("fileType>> " + fileType);
        if (Util.isGif(fileType)) {
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
        float aspect = opview.getHeight() * 1f / mStickerLayout.getHeight();
        float v = (1f - aspect) / 2 + aspect;
        //
        CustomTextView customTextView = new CustomTextView(PsActivity.this);
        if (mStickerLayout.backgroundView.isBackgroundVideoImageViewVisible()) {
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
        mStickerLayout.deleteFocus();
        resetStatus();
    }

    private void initListener() {
        iv_close.setOnClickListener(this);
        iv_confirm.setOnClickListener(this);
        vColor.setOnClickListener(this);
        iv_lable.setOnClickListener(this);

        iv_back.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        iv_undo.setOnClickListener(this);
        iv_redo.setOnClickListener(this);
        iv_delete.setOnClickListener(this);
    }

    //清空画布
    private void delete() {
        if (customCanvas.isEnable()) {
            return;
        }
        mStickerLayout.reset();
        statesbarGoInEditMode(false);
        resetStatus();
    }
    private void statesbarGoInEditMode(boolean isedit) {
        iv_back.setVisibility(isedit ? View.GONE : View.VISIBLE);
        iv_next.setVisibility(isedit ? View.GONE : View.VISIBLE);
        iv_share.setVisibility(isedit ? View.GONE : View.VISIBLE);
        iv_confirm.setVisibility(isedit ? View.VISIBLE : View.GONE);
        iv_close.setVisibility(isedit ? View.VISIBLE : View.GONE);
    }
}
