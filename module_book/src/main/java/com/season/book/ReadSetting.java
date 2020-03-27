package com.season.book;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.season.lib.anim.PageAnimController;


/**
 * 阅读界面设置管理
 */
public class ReadSetting{
    /** 是否是简体 */
    public static final String SETTING_TYPE_FONT_SIM = "SETTING_TYPE_FONT_SIMP";
    /** 主题设置*/
    public static final String SETTING_TYPE_THEME = "SETTING_TYPE_THEME";
    /** 亮度设置*/
    public static final String SETTING_TYPE_BRIGHTESS_LEVEL = "SETTING_TYPE_BRIGHTESS_LEVEL";
    /** 动画设置*/
    public static final String SETTING_TYPE_ANIM = "SETTING_TYPE_ANIM";

    /*		定义字体大小类型		*/
    public static final String SETTING_TYPE_FONT_SIZE = "SETTING_TYPE_FONT_SIZE";
    public static final int FONT_SIZE_MIN = 15;
    public static final int FONT_SIZE_COUNT = 10;
    private int mFontLevel;

    /*		定义行间距类型		*/
    public static final String SETTING_TYPE_FONT_LINE_SPACE_TYPE = "SETTING_TYPE_FONT_LINE_SPACE_TYPE";
    public static final int LINE_SPACE_MIN = 0;
    public static final int LINE_SPACE_COUNT = 8;
    private int mLineSpaceLevel;

    /*		定义段落间距类型		*/
    public static final String SETTING_TYPE_FONT_PARAGRAPH_SPACE_TYPE = "SETTING_TYPE_FONT_PARAGRAPH_SPACE_TYPE";
    public static final int PARAGRAPH_SPACE_MIN = 0;
    public static final int PARAGRAPH_SPACE_COUNT = 6;
    private int mParagraphSpaceLevel;

    /*		定义首行缩进		*/
    public static final String SETTING_TYPE_INDENT_SIZE_TYPE = "SETTING_TYPE_INDENT_SIZE_TYPE";
    public static final int INDENT_SIZE_MIN = 0;
    public static final int INDENT_SIZE_COUNT = 4;
    private int mIndentSizeLevel;

    /*		定义上下边距		*/
    public static final String SETTING_TYPE_TOPBOTTOM_SPACE_TYPE = "SETTING_TYPE_TOP_BOTTOM_SPACE_TYPE";
    public static final int TOPBOTTOM_SPACE_MIN = 0;
    public static final int TOPBOTTOM_SPACE_COUNT = 10;
    private int mTopBottomSpaceLevel;

    /*		定义上下边距		*/
    public static final String SETTING_TYPE_LEFTRIGHT_SPACE_TYPE = "SETTING_TYPE_LEFT_RIGHT_SPACE_TYPE";
    public static final int LEFTRIGHT_SPACE_MIN = 0;
    public static final int LEFTRIGHT_SPACE_COUNT = 10;
    private int mLeftRightSpaceLevel;

    private static final String PREFS_MODULE_INFO = "read_setting_prefs";
    private static ReadSetting sInstance;
    private Context mContext;
    private LinkedList<WeakReference<SettingListener>> mSettingListenerList;
    private Handler mHandler;
    private SharedPreferences mSharedPreferences;
    private int mSimplified;
    private int mThemeType;
    private int mBrightLevel;
    private int mAnimType;

    public static ReadSetting getInstance(Context context){
        if(sInstance == null){
            sInstance = new ReadSetting(context);
        }
        return sInstance;
    }

    private ReadSetting(Context context){
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences(PREFS_MODULE_INFO, Context.MODE_PRIVATE);
        mFontLevel =  mSharedPreferences.getInt(SETTING_TYPE_FONT_SIZE, FONT_SIZE_COUNT/2);
        mLineSpaceLevel = mSharedPreferences.getInt(SETTING_TYPE_FONT_LINE_SPACE_TYPE, LINE_SPACE_COUNT/2);
        mParagraphSpaceLevel = mSharedPreferences.getInt(SETTING_TYPE_FONT_PARAGRAPH_SPACE_TYPE, PARAGRAPH_SPACE_COUNT/2);
        mTopBottomSpaceLevel = mSharedPreferences.getInt(SETTING_TYPE_TOPBOTTOM_SPACE_TYPE, TOPBOTTOM_SPACE_COUNT/2);
        mLeftRightSpaceLevel = mSharedPreferences.getInt(SETTING_TYPE_LEFTRIGHT_SPACE_TYPE, LEFTRIGHT_SPACE_COUNT/2);
        mIndentSizeLevel = mSharedPreferences.getInt(SETTING_TYPE_INDENT_SIZE_TYPE, INDENT_SIZE_COUNT/2);
        mThemeType = loadThemeType();
        mBrightLevel = loadBrightessLevel();
        mAnimType = loadAnimType();
        mSettingListenerList = new LinkedList<WeakReference<SettingListener>>();
        mHandler = new Handler(Looper.getMainLooper());
        mSimplified = loadFontFontSimplify();
    }

    /**
     * 书架阅读书籍index
     * @return
     */
    public int getReadPosition(){
        return mSharedPreferences.getInt("readPosition", 2);
    }

    /**
     * 设置书架书籍阅读位置
     * @param position
     */
    public void setReadPosition(int position){
        mSharedPreferences.edit().putInt("readPosition", position).commit();
    }

    public void clearSetting(){
        mSharedPreferences.edit().clear();
    }

    private boolean containsSettingListeners(SettingListener l){
        for (WeakReference<SettingListener> settingListener : mSettingListenerList) {
            if(settingListener.get() != null && settingListener.get().equals(l)){
                return true;
            }
        }
        return false;
    }

    public void addDataListeners(SettingListener listener){
        if(listener == null){
            return;
        }
        if(!containsSettingListeners(listener)){
            mSettingListenerList.add(new WeakReference<ReadSetting.SettingListener>(listener));
        }
    }

    private void notify(final String type){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SettingListener listener = null;
                for (WeakReference<SettingListener> settingListener : mSettingListenerList) {
                    listener = settingListener.get();
                    if(listener != null){
                        listener.onSettingChange(ReadSetting.this , type);
                    }
                }
            }
        });
    }


    /**
     * 设置字体等级
     * @param level 1-10级
     */
    public void setFontLevel(int level){
        if(mFontLevel == level){
            return;
        }
        mFontLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_FONT_SIZE, level).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }
    public int getFontLevel(){
        return mFontLevel;
    }

    public int getFontSize(){
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        return (int) (dm.density * (FONT_SIZE_MIN + mFontLevel) + 0.5f);
    }

    public int getMinFontSize(){
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        return (int) (dm.density * FONT_SIZE_MIN + 0.5f);
    }

    /**
     * 设置行间距等级
     * @param level
     */
    public void setLineSpaceLevel(int level){
        if(mLineSpaceLevel == level){
            return;
        }
        mLineSpaceLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_FONT_LINE_SPACE_TYPE, mLineSpaceLevel).commit();
        notify(SETTING_TYPE_FONT_LINE_SPACE_TYPE);
    }

    public int getLineSpaceLevel(){
        return mLineSpaceLevel;
    }

    public int getLineSpaceSize(){
        int size = getFontSize();
        return size / 6 * (LINE_SPACE_MIN + mLineSpaceLevel)/2;
    }

    /**
     * 设置段落间距等级
     * @param level
     */
    public void setParagraphSpaceLevel(int level){
        if(mParagraphSpaceLevel == level){
            return;
        }
        mParagraphSpaceLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_FONT_PARAGRAPH_SPACE_TYPE, mParagraphSpaceLevel).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    public int getParagraphSpaceLevel(){
        return mParagraphSpaceLevel;
    }

    public int getParagraphSpaceSize(){
        int size = getFontSize();
        return size / 3 * (PARAGRAPH_SPACE_MIN + mParagraphSpaceLevel)/2;
    }


    /**
     * 设置首行等级
     * @param level
     */
    public void setIndentSizeLevel(int level){
        if(mIndentSizeLevel == level){
            return;
        }
        mIndentSizeLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_INDENT_SIZE_TYPE, mIndentSizeLevel).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    public int getIndentSizeLevel(){
        return mIndentSizeLevel;
    }

    public int getIndentSizeSize(){
        return INDENT_SIZE_MIN + mIndentSizeLevel;
    }


    /**
     * 设置页面上下间距等级
     * @param level
     */
    public void setTopBottomSpaceLevel(int level){
        if(mTopBottomSpaceLevel == level){
            return;
        }
        mTopBottomSpaceLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_TOPBOTTOM_SPACE_TYPE, mTopBottomSpaceLevel).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    public int getTopBottomSpaceLevel(){
        return mTopBottomSpaceLevel;
    }

    public int getTopBottomSpaceSize(){
        return TOPBOTTOM_SPACE_MIN + mTopBottomSpaceLevel * 8;
    }


    /**
     * 设置页面左右间距等级
     * @param level
     */
    public void setLeftRightSpaceLevel(int level){
        if(mLeftRightSpaceLevel == level){
            return;
        }
        mLeftRightSpaceLevel = level;
        mSharedPreferences.edit().putInt(SETTING_TYPE_LEFTRIGHT_SPACE_TYPE, mLeftRightSpaceLevel).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    public int getLeftRightSpaceLevel(){
        return mLeftRightSpaceLevel;
    }

    public int getLeftRightSpaceSize(){
        return LEFTRIGHT_SPACE_MIN + mLeftRightSpaceLevel * 8;
    }


    /**
     * 保存字体是否为简体，并通知界面更新
     */
    public void setSimplified(int isSimplified){
        if(mSimplified == isSimplified){
            return;
        }
        this.mSimplified = isSimplified;
        saveFontSimplify(isSimplified);
        notify(SETTING_TYPE_FONT_SIM);
    }

    /**
     * 获取字体是否为简体
     */
    public int isSimplified(){
        return mSimplified;
    }


    private void saveFontSimplify(int isSimplified){
        mSharedPreferences.edit().putInt(SETTING_TYPE_FONT_SIM, isSimplified).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    private int loadFontFontSimplify(){
        return mSharedPreferences.getInt(SETTING_TYPE_FONT_SIM, 0);
    }


    /**
     * 章节名和页码等装饰的字体颜色
     * @return
     */
    public int getThemeDecorateTextColor(){
        return ReadSettingThemeColor.getThemeDecorateTextColor(mThemeType);
    }
    /**
     * 获取字体颜色
     * @return
     */
    public int getThemeTextColor(){
       return ReadSettingThemeColor.getThemeTextColor(mThemeType);
    }
    /**
     * 获取背景颜色值
     * @return
     */
    public int getThemeBGColor(){
        return ReadSettingThemeColor.getThemeBGColor(mThemeType);
    }

    /**
     * 获取背景图片资源ID
     * @return 返回-1 代表没有背景图片
     */
    public int getThemeBGImgRes(){
        return ReadSettingThemeColor.getThemeBGImgRes(mThemeType);
    }


    public int getThemeType(){
        return mThemeType;
    }

    public void setThemeType(int type){
        if(mThemeType == type){
            return;
        }
        mThemeType = type;
        saveThemeType(type);
    }

    private int loadThemeType(){
        return mSharedPreferences.getInt(SETTING_TYPE_THEME, ReadSettingThemeColor.THEME_TYPE_IMAGE_2);
    }

    private void saveThemeType(int type){
        mSharedPreferences.edit().putInt(SETTING_TYPE_THEME, type).commit();
        notify(SETTING_TYPE_THEME);
    }


    public int getBrightessLevel(){
        return mBrightLevel;
    }

    public void setBrightess(int value, Window window){
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = value * 1.0f / 100.0f;
        if(lp.screenBrightness < 0.17){
            lp.screenBrightness = 0.17f;
        }
        window.setAttributes(lp);
    }

    public void setBrightessLevel(int level){
        if(mBrightLevel == level){
            return;
        }
        mBrightLevel = level;
        saveBrightessLevel(level);
    }

    private int loadBrightessLevel(){
        return mSharedPreferences.getInt(SETTING_TYPE_BRIGHTESS_LEVEL, 40);
    }

    private void saveBrightessLevel(int level){
        mSharedPreferences.edit().putInt(SETTING_TYPE_BRIGHTESS_LEVEL, level).commit();
        notify(SETTING_TYPE_BRIGHTESS_LEVEL);
    }


    public int getAnimType(){
        return mAnimType;
    }

    public void setAnimType(int type){
        if(mAnimType == type){
            return;
        }
        mAnimType = type;
        saveAnimType(type);
    }

    private int loadAnimType(){
        return mSharedPreferences.getInt(SETTING_TYPE_ANIM, PageAnimController.ANIM_TYPE_PAGE_TURNING);
    }

    private void saveAnimType(int type){
        mSharedPreferences.edit().putInt(SETTING_TYPE_ANIM, type).commit();
        notify(SETTING_TYPE_ANIM);
    }

    private final void runOnUiThread(Runnable action) {
        mHandler.post(action);
    }


    /**
     * 获取书籍阅读进度
     * @param id
     * @return
     */
    public int[] getBookReadProgress(String id){
        int[] res = {0, 0};
        String pageStr = mSharedPreferences.getString(id, "0-0");
        String[] strings = pageStr.split("-");
        if (strings.length == 2){
            res[0] = Integer.parseInt(strings[0]);
            res[1] = Integer.parseInt(strings[1]);
        }
        return res;
    }

    public void saveBookReadProgress(String id, int currentChapterIndex, int currentPageIndex) {
        mSharedPreferences.edit().putString(id, currentChapterIndex +"-"+ currentPageIndex).commit();
    }

    public interface SettingListener{
        void onSettingChange(ReadSetting readSetting,String type);
    }
}
