package com.season.example;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.example.layout.BottomPaintLayout;
import com.season.example.layout.BottomTextLayout;
import com.season.example.layout.BottomTucengLayout;
import com.season.example.layout.BottomVipLayout;
import com.season.example.layout.PSBgColorGroup;
import com.season.example.support.MosaicUtil;
import com.season.example.video.VideoActivity;
import com.season.lib.BaseStartPagerActivity;
import com.season.lib.RoutePath;
import com.season.ps.bean.LayerItem;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.dimen.ColorUtil;
import com.season.ps.gif.GifMaker;
import com.season.lib.util.LogUtil;
import com.season.lib.util.ToastUtil;
import com.season.example.layout.PopInputLayout;
import com.season.ps.bean.LayerBackground;
import com.season.ps.bean.LayerEntity;
import com.season.ps.view.ps.ILayer;
import com.season.ps.view.ps.PSLayer;
import com.season.lib.file.FileUtils;
import com.season.lib.dimen.ScreenUtils;
import com.season.ps.view.ps.PSCanvas;
import com.season.example.layout.TopDeleteLayout;
import com.season.ps.view.ps.PSCoverView;
import com.season.ps.view.ps.CustomTextView;
import com.season.ps.view.ps.CustomCanvas;
import com.season.ps.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Route(path= RoutePath.PS)
public class PsActivity extends BaseStartPagerActivity implements View.OnClickListener {

    PSCoverView mPsCoverView;//最外部覆盖
    PSCanvas mPsCanvas;//ps画布
    CustomCanvas customCanvas;//涂鸦
    TopDeleteLayout topDeleteLayout;

    PSBgColorGroup mPsbgColorGroup;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_ps;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (false){
            VideoActivity.start(this);
            finish();
            return;
        }
        if (false){
            BigGifActivity.start(this,"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583301994914&di=e9d75711d7e4528212a23043dc5bbb45&imgtype=0&src=http%3A%2F%2Fhiphotos.baidu.com%2F%2587%25E5%25C1%25B6%2Fpic%2Fitem%2F4659b945ad345982cdfbd72f0cf431adcaef849b.jpg");
            finish();
            return;
        }

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
            public void onFocusGet(ViewGroup view) {
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

    private View vipView;
    private void initView(){
        mPsCoverView = findViewById(R.id.layout_container);
        mPsCanvas = findViewById(R.id.layout_stickLayout);
        customCanvas = findViewById(R.id.tuya);

        vipView = findViewById(R.id.vip);
        del_container = findViewById(R.id.del_container);

        iv_redo = findViewById(R.id.iv_redo);
        iv_undo = findViewById(R.id.iv_undo);
        iv_delete = findViewById(R.id.iv_delete);

        iv_back = findViewById(R.id.iv_back);
        iv_next = findViewById(R.id.iv_next);
        iv_close = findViewById(R.id.iv_close);
        iv_confirm = findViewById(R.id.iv_confirm);

        vipView.setOnClickListener(this);
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
        findViewById(R.id.bt_tc).setOnClickListener(this);
    }

    ViewExtend viewExtend;
    int videoWidthHeight, offsetX, offsetY;
    private void fixCanvasHeight() {
        View canvasArea = findViewById(R.id.opview);
        int height = canvasArea.getHeight();
        int screenWidth = ScreenUtils.getScreenWidth();
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
        delParams.height = Math.max(offsetY, 128);
        del_container.requestLayout();

        mPsCanvas.videoWidthHeight = videoWidthHeight;
        mPsCanvas.setOffsetX(offsetX);
        mPsCanvas.setOffsetY(offsetY);
        viewExtend = new ViewExtend(this, videoWidthHeight, offsetX, offsetY){
            //post一个一个显示图层，比较好看
            @Override
            public void addViewPost(final PSLayer PSLayer, int delay, final boolean isLast) {
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
            @Override
            public void addLayer(PSLayer view) {
                mPsCanvas.addView(view);
                if (view.getChildCount() > 0){
                    View cView = view.getChildAt(0);
                    if (cView instanceof ILayer){
                        ((ILayer) cView).setStartTime(0);
                        ((ILayer) cView).setEndTime(mPsCanvas.maxDuration);
                    }
                }
                resetStatus();
            }

            @Override
            protected void setBackground(String url, String filePath) {
                File file = new File(filePath);
                if (file.isFile() && file.length() > 0) {
                    mPsCanvas.showImage(url, filePath);
                }
                resetStatus();
            }
        };
        LogUtil.LOG(""+ offsetX +"----"+ offsetY);
    }

    private void bindData2Canvas() {
        try {
            LayerEntity  layerEntity = null;
            Object localData = FileUtils.getSerialData("layerInfo");
            if (localData instanceof  LayerEntity){
                layerEntity = (LayerEntity) localData;
            }
            if (layerEntity != null){
                LayerBackground backgroundInfo = layerEntity.getBackInfoModel();
                List<LayerItem> items = layerEntity.getItemArray();
                viewExtend.showLayers(layerEntity, items);
                showBg(backgroundInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
}

    private void showBg(LayerBackground backgroundInfo) {
        if (backgroundInfo.isColor()) {
            //背景是透明图层
            final String colorStr = backgroundInfo.backColorString;
            if (!TextUtils.isEmpty(colorStr)) {//背景是纯色
                mPsCanvas.showBackground(ColorUtil.getColor(colorStr, 0x000000));
                resetStatus();
            }
        } else {
            if (backgroundInfo.isStaticImage()) {
                //背景是图片
                viewExtend.addLocalMessage(backgroundInfo.imgURLPath, backgroundInfo.imageURLPathFile);
            } else {
                //背景是GIf
                viewExtend.addLocalMessage(backgroundInfo.gifURLPath, backgroundInfo.gifURLPathFile);
            }

        }
    }

    private boolean editTextView(View view) {
        if (view != null && view instanceof CustomTextView) {
            addTextPopInput(((CustomTextView) view).getText());
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
        if (customCanvas.isEnable()) {
            closeTuya();
            bottomPaintLayout.hide();
            return;
        }
        if (bottomVipLayout.isShowing()){
            bottomVipLayout.hide();
            return;
        }
        if (bottomTucengLayout.isShowing()){
            bottomTucengLayout.hide();
            return;
        }
        if (bottomTextLayout.isShowing()){
            bottomTextLayout.hide();
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
                    resetBottomStatus();
                } else {
                    viewExtend.addTextView(text, mPsCanvas.backgroundView.isBackgroundVideoImageViewVisible());
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
                    vipView.setVisibility(View.GONE);
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
        if (view == null){
            vipView.setVisibility(View.GONE);
            bottomTucengLayout.hide();
            bottomVipLayout.hide();
        }else{
            vipView.setVisibility(View.VISIBLE);
        }
        bottomVipLayout.statusChange(mPsCanvas);
        bottomTucengLayout.statusChange(mPsCanvas.getViewIndex(), mPsCanvas.getChildCount());
        if (bottomVipLayout.isShowing() || bottomTucengLayout.isShowing()){
            bottomTextLayout.hide();
        }else{
            if (view != null && view instanceof CustomTextView) {
                bottomTextLayout.show();
                bottomTextLayout.select(((CustomTextView) view).currentType);
            }else{
                bottomTextLayout.hide();
            }
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
        try {
            if (mPsCanvas != null) {
                FileUtils.saveSerialData("layerInfo", mPsCanvas.getLayerMessage());
                mPsCanvas.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BottomPaintLayout bottomPaintLayout;
    private BottomTextLayout bottomTextLayout;
    private BottomTucengLayout bottomTucengLayout;
    private BottomVipLayout bottomVipLayout;
    private void initBottomLayout() {
        bottomTextLayout = new BottomTextLayout(this) {
            @Override
            public void onItemClick(int position) {
                mPsCanvas.setTextAnimationType(position);
            }
        };
        bottomVipLayout = new BottomVipLayout(this);
        bottomTucengLayout = new BottomTucengLayout(this){
            @Override
            public void onCopy() {
                PSLayer scaleView = mPsCanvas.getView();
                if (scaleView != null) {
                    PSLayer copyView = scaleView.copy();
                    mPsCanvas.addView(copyView);
                    resetStatus();
                } else {
                    ToastUtil.showToast("未选中图层");
                }
            }

            @Override
            public void onUpLayer() {
                PSLayer focusView = mPsCanvas.getView();
                if (focusView != null){
                    mPsCanvas.upLayer(mPsCanvas.getView(), true);
                    resetBottomStatus();
                }else{
                    ToastUtil.showToast("未选中图层");
                }
            }

            @Override
            public void onDownLayer() {
                PSLayer focusView = mPsCanvas.getView();
                if (focusView != null){
                    mPsCanvas.downLayer(mPsCanvas.getView(), true);
                    resetBottomStatus();
                }else{
                    ToastUtil.showToast("未选中图层");
                }
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
        BitmapUtil.recycleBitmaps(srcBitmap);
        customCanvas.setMosaic(bit);
    }

    public void setBrushColor(int color, boolean isMask) {
        customCanvas.setColor(color, isMask);
    }

    public void setBrushColor(Bitmap bitmap) {
        customCanvas.setColor(bitmap);
    }

    private ProgressDialog progressDialog;
    int testPoi = 0;
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            closeTuya();
            bottomPaintLayout.hide();
        } else if (i == R.id.vip) {
            if (bottomVipLayout.isShowing()){
                bottomVipLayout.hide();
            }else{
                bottomVipLayout.show();
                bottomVipLayout.statusChange(mPsCanvas);
            }
        } else if (i == R.id.bt_paint) {
            startTuya();
            bottomPaintLayout.show();
        } else if (i == R.id.bt_image) {
            testPoi++;testPoi=testPoi>2?0:testPoi;
            if (testPoi == 0) viewExtend.addImageOrGifFromUrl("https://pics6.baidu.com/feed/d01373f082025aaf0c2b05650a0fa262024f1a6f.jpeg?token=6649fdc0f364b5ff0c55c18cb4e1ff65");
            if (testPoi == 1) viewExtend.addImageOrGifFromUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583398707311&di=0d84c3ca3c68291c6a78e533f20c4fb0&imgtype=0&src=http%3A%2F%2Fn.sinaimg.cn%2Fsinacn17%2F480%2Fw240h240%2F20180618%2F5c4d-heauxvz1011485.gif");
            else    viewExtend.addImageOrGifFromUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583301994914&di=e9d75711d7e4528212a23043dc5bbb45&imgtype=0&src=http%3A%2F%2Fhiphotos.baidu.com%2F%2587%25E5%25C1%25B6%2Fpic%2Fitem%2F4659b945ad345982cdfbd72f0cf431adcaef849b.jpg");

        }  else if (i == R.id.bt_text) {
            View view = mPsCanvas.getFocusView();
            if (view != null && view instanceof CustomTextView) {
                bottomTextLayout.show();
                bottomTextLayout.select(((CustomTextView) view).currentType);
            }else{
                addTextPopInput("");
            }
        }  else if (i == R.id.bt_tc) {
            bottomTucengLayout.show();
            bottomTucengLayout.statusChange(mPsCanvas.getViewIndex(), mPsCanvas.getChildCount());
        } else if (i == R.id.iv_confirm) {
            finishTuya();
            bottomPaintLayout.hide();
        } else if (i == R.id.iv_back) {
            if (mPsCoverView.getChildCount() > 0) {
                mPsCoverView.removeAllViews();
                return;
            }
            onPagerFinish();
        } else if (i == R.id.iv_next) {
            if (mPsCanvas.getChildCount() == 0) {
                if (mPsCanvas.backgroundView.currentOperate != null) {
                    if (mPsCanvas.backgroundView.currentOperate.visible1 == View.VISIBLE) {
                        ToastUtil.showToast("画板上没有东西");
                        return;
                    }
                }
            }
            mPsCanvas.start(2, new GifMaker.OnGifMakerListener() {
                @Override
                public void onMakeGifStart() {
                    if (progressDialog == null){
                        progressDialog = new ProgressDialog(PsActivity.this);
                        //依次设置标题,内容,是否用取消按钮关闭,是否显示进度
                        progressDialog.setTitle("正在合成中");
                        progressDialog.setCancelable(true);
                        //这里是设置进度条的风格,HORIZONTAL是水平进度条,SPINNER是圆形进度条
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMax(100);
                    }
                    progressDialog.show();
                }

                @Override
                public void onMakeProgress(int index, int count) {
                    LogUtil.LOG(count + "onMakeProgress" + index);
                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.setProgress(index * 100/count);
                    }
                }

                @Override
                public void onMakeGifSucceed(String outPath) {
                    LogUtil.LOG(outPath);
                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    CropActivity.start(PsActivity.this, outPath);
                }

                @Override
                public void onMakeGifFail() {
                    LogUtil.LOG("onMakeGifFail");
                    if (progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    ToastUtil.showToast("fail");
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

    //涂鸦完成，生成图片添加到画布
    public void finishTuya() {
        statesbarGoInEditMode(false);
        try {
            String filePath = customCanvas.getCacheFilePath();
            if (filePath != null) {
                float[] center = customCanvas.getBitmapcenter();
                float centerX = (center[0] + center[1]) / 2;
                float centerY = (center[2] + center[3]) / 2;
                viewExtend.addImageViewFromFile(true, null, filePath, centerX, centerY);
            }
            customCanvas.clear();
            customCanvas.changeStatus(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resetStatus();
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
