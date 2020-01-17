package com.season.lib.ps.bean;

import android.text.TextUtils;

import com.season.myapplication.BuildConfig;
import com.season.lib.util.Logger;
import com.season.lib.util.Constant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lizhongxin on 2017/8/24.
 */
///* 背景webP */
//    //@property(nonatomic ,copy) NSString *webPURLPath;
//
//    /* 比例 */
//    @property(nonatomic ,assign) BQSCustomizingVCProportionType proportionType;
//
///* 存放元素的数组 */
//    @property(nonatomic ,strong) NSMutableArray<CKYRotationViewItem *> *itemArray;
//
//    /* 创作的尺寸 */
//    @property(nonatomic ,assign) CGFloat width;
//        /* 背景颜色 */
//        @property(nonatomic ,strong) NSString *backColorString;
///* 视频资源 */
//        @property(nonatomic ,strong) NSString *assetPath;
//        /* 视频速度 */
//        @property(nonatomic ,assign) CGFloat rate;
///* 图片地址 */
//        @property(nonatomic ,strong) NSString *imgURLPath;
public class LayerEntity implements Serializable {

    //    @Override
//    public String toString()
//    {
//        return "LayerEntity{" + "backInfoModel=" + backInfoModel + ", width=" + width + ", proportionType=" +
//                proportionType + ", itemArray=" + itemArray + '}';
//    }

    /**
     * itemArray : [{"xScale":1.467921,"index":0,"vMoveBtnPositionType":2,"hMoveBtnPositionType":3,"centerY":385.25,
     * "contentViewType":2,"turnOverH":false,"angle":0.003607564,"textFontPath":"https: //img.biaoqing
     * .com/font/HappyZcool.ttf","textStyleModel":{"textX":12.38154,"thumbnail":"yangshi0",
     * "styleModel":{"textColor":"ffe63f","strokeColor":"5842ff","textColorSize":0,"miaobianAlpha":1,"textAlpha":1,
     * "strokeSize":0.75},"textH":47.73438,"textW":160.96,"textY":3.671875,"thumbnailW":98,"fontName":"CN000015",
     * "imageName":"","imageWidth":185.7231,"imageHeight":55.07812},"turnOverV":false,"centerX":187.5,"text":"抖起来！",
     * "imageURL":"","textFontName":"CN000015","sizeWidth":185.7231,"sizeHeight":55.07812,"textFontSize":40,
     * "yScale":1.467921}]
     * backInfoModel : {"assetPath":"https: //img.biaoqing.com/video/20170817/16372400064.mp4","backColorString":"",
     * "imgURLPath":"","rate":1}
     * width : 375
     * proportionType : 1
     */
    private String textPublishDescribe;

    public String getTextPublishDescribe() {
        return textPublishDescribe;
    }

    public void setTextPublishDescribe(String textPublishDescribe) {
        this.textPublishDescribe = textPublishDescribe;
    }

    //    proportionType 比例
    private LayerBackground backInfoModel;
    private float width;
    private float height;
    private int proportionType;
    private List<LayerItem> itemArray = new ArrayList<>();
    public boolean isPrivate;
    public boolean notRecommend;
    public int audioId;
    public long faceId;//脸部贴纸
    public long followId;//跟拍
    public long originId;
    /**
     * 1,改图；2，跟拍
     */
    public long relateType;
    /**
     * 1，音频，2 视频的音频
     */
    public int type;

    public long originalId;

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public LayerBackground getBackInfoModel() {
        return backInfoModel;
    }

    public void setBackInfoModel(LayerBackground backInfoModel) {
        this.backInfoModel = backInfoModel;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public int getProportionType() {
        return proportionType;
    }

    public void setProportionType(int proportionType) {
        this.proportionType = proportionType;
    }

    public List<LayerItem> getItemArray() {
        return itemArray;
    }

    public void setItemArray(List<LayerItem> itemArray) {
        if (itemArray != null) {
            for (LayerItem bean : itemArray) {
                this.itemArray.add(bean);
            }
        } else {
            if (this.itemArray != null) this.itemArray.clear();
        }
    }

    public String getTextAllItem() {
        if (itemArray != null && itemArray.size() > 0) {
            String text = "";
            for (int i = 0; i < itemArray.size(); i++) {
                LayerItem layerItem = itemArray.get(i);
                if (layerItem.getContentViewType() == Constant.contentViewType.ContentViewTypeTextbox) {
                    String textString = layerItem.getText();
                    if (!TextUtils.isEmpty(textString)) {
                        String replace = textString.replace("\n", " ");
                        text += replace;
                    }
                }
            }
            return text;
        }
        return null;
    }

    public String getLayerText() {
        if (getItemArray() != null) {
            int textnum = 0;
            int index = 0;
            for (int i = 0; i < getItemArray().size(); i++) {
                if (getItemArray().get(i).getContentViewType() == Constant.contentViewType.ContentViewTypeTextbox) {
                    textnum++;
                    index = i;
                }
            }
            if (textnum == 1) {
                String text = getItemArray().get(index).getText();
                if (BuildConfig.DEBUG) Logger.d("follow==" + text);
                return text;
            }
        }
        return null;
    }
}
