package com.example.lib;

import com.example.book.R;

/**
 * 背景主题设置，关联绘制的背景和文字的颜色
 */
public class ReadSettingThemeColor {

    /*		定义主题类型		*/
    public static final int THEME_TYPE_DAY = 0;
    public static final int THEME_TYPE_NIGHT = 1;
    public static final int THEME_TYPE_IMAGE_1 = 2;
    public static final int THEME_TYPE_IMAGE_2 = 3;
    public static final int THEME_TYPE_IMAGE_3 = 4;
    public static final int THEME_TYPE_IMAGE_4 = 5;
    public static final int THEME_TYPE_IMAGE_5 = 6;

    public static final int THEME_TYPE_COLOR_1 = 10;
    public static final int THEME_TYPE_COLOR_2 = 11;
    public static final int THEME_TYPE_COLOR_3 = 12;
    public static final int THEME_TYPE_COLOR_4 = 13;


    public static final int[] sThemeTypes = {
            ReadSettingThemeColor.THEME_TYPE_DAY, ReadSettingThemeColor.THEME_TYPE_COLOR_1,
            ReadSettingThemeColor.THEME_TYPE_COLOR_2,ReadSettingThemeColor.THEME_TYPE_COLOR_3,ReadSettingThemeColor.THEME_TYPE_COLOR_4,
            ReadSettingThemeColor.THEME_TYPE_IMAGE_2, ReadSettingThemeColor.THEME_TYPE_IMAGE_5, ReadSettingThemeColor.THEME_TYPE_IMAGE_4,
            ReadSettingThemeColor.THEME_TYPE_IMAGE_1, ReadSettingThemeColor.THEME_TYPE_IMAGE_3};

    /**
     * 章节名和页码等装饰的字体颜色
     * @return
     */
    public static int getThemeDecorateTextColor(int themeType){
        int textColor = 0x88464646;
        switch (themeType) {
            case THEME_TYPE_NIGHT:
                textColor = 0xff1d2524;
                break;
            case THEME_TYPE_IMAGE_1:
            case THEME_TYPE_IMAGE_3:
                textColor = 0x88cccccc;
                break;
        }
        return textColor;
    }

    /**
     * 获取字体颜色
     * @return
     */
    public static int getThemeTextColor(int mThemeType){
        int textColor = 0xff464646;
        switch (mThemeType) {
            case THEME_TYPE_NIGHT:
                textColor = 0xff1d2524;
                break;
            case THEME_TYPE_IMAGE_1:
            case THEME_TYPE_IMAGE_3:
                textColor = 0xffcccccc;
                break;
        }
        return textColor;
    }
    /**
     * 获取背景图片资源ID
     * @return 返回-1 代表没有背景图片
     */
    public static int getThemeBGImgRes(int themeType){
        int bgImgRes = -1;
        switch (themeType) {
            case THEME_TYPE_IMAGE_1:
                bgImgRes = R.drawable.read_style_other_bg_1;
                break;
            case THEME_TYPE_IMAGE_2:
                bgImgRes = R.drawable.read_style_other_bg_2;
                break;
            case THEME_TYPE_IMAGE_3:
                bgImgRes = R.drawable.read_style_other_bg_3;
                break;
            case THEME_TYPE_IMAGE_4:
                bgImgRes = R.drawable.read_style_other_bg_4;
                break;
            case THEME_TYPE_IMAGE_5:
                bgImgRes = R.drawable.read_style_other_bg_5;
                break;
        }
        return bgImgRes;
    }

    /**
     * 获取背景图片资源ID
     * @return 返回-1 代表没有背景图片
     */
    public static int getThemeBGImgResThumb(int themeType){
        int bgImgRes = -1;
        switch (themeType) {
            case THEME_TYPE_IMAGE_1:
                bgImgRes = R.drawable.ic_read_style_other_1;
                break;
            case THEME_TYPE_IMAGE_2:
                bgImgRes = R.drawable.ic_read_style_other_2;
                break;
            case THEME_TYPE_IMAGE_3:
                bgImgRes = R.drawable.ic_read_style_other_3;
                break;
            case THEME_TYPE_IMAGE_4:
                bgImgRes = R.drawable.ic_read_style_other_4;
                break;
        }
        return bgImgRes;
    }


    /**
     * 获取背景颜色值
     * @return
     */
    public static int getThemeBGColor(int themeType){
        int bgColor = 0;
        switch (themeType) {
            case THEME_TYPE_DAY:
                bgColor = 0xffe6e4df;
                break;
            case THEME_TYPE_NIGHT:
                bgColor = 0xff282a2e;
                break;
            case THEME_TYPE_COLOR_1:
                bgColor = 0xffFFF2E2;
                break;
            case THEME_TYPE_COLOR_2:
                bgColor = 0xffc9e2cb;
                break;
            case THEME_TYPE_COLOR_3:
                bgColor = 0xffcee9eb;
                break;
            case THEME_TYPE_COLOR_4:
                bgColor = 0xffe5d4a8;
                break;
            case THEME_TYPE_IMAGE_1:
                bgColor = 0xff395268;
                break;
            case THEME_TYPE_IMAGE_2:
                bgColor = 0xfff4f3e7;
                break;
            case THEME_TYPE_IMAGE_3:
                bgColor = 0xff224a43;
                break;
            case THEME_TYPE_IMAGE_4:
                bgColor = 0xffdfc6a0;
                break;
            case THEME_TYPE_IMAGE_5:
                bgColor = 0xffeeeeee;
                break;
        }
        return bgColor;
    }
}
