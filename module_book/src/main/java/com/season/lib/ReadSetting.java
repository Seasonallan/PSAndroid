package com.season.lib;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.season.lib.anim.PageAnimController;


/**
 * 阅读界面设置管理
 * @author lyw
 */
public class ReadSetting{
    /** 是否是简体 */
    public static final String SETTING_TYPE_FONT_SIM = "SETTING_TYPE_FONT_SIM";
    /** 主题设置*/
    public static final String SETTING_TYPE_THEME = "SETTING_TYPE_THEME";
    /** 亮度设置*/
    public static final String SETTING_TYPE_BRIGHTESS_LEVEL = "SETTING_TYPE_BRIGHTESS_LEVEL";
    /** 动画设置*/
    public static final String SETTING_TYPE_ANIM = "SETTING_TYPE_ANIM";
    /** 自动播放设置 播放暂停之后相关设置不会发起通知*/
    public static final String SETTING_TYPE_AUTO = "SETTING_TYPE_AUTO";
    /** 摇一摇切换白天黑夜*/
    public static final String SETTING_SHAKE_SWITCH = "SETTING_SHAKE_SWITCH";
    /** 自动播放延迟设置*/
    private static final String SETTING_TYPE_AUTO_DELAYED_UD = "SETTING_TYPE_AUTO_DELAYED_UD";
    /** 自动播放延迟设置*/
    private static final String SETTING_TYPE_AUTO_DELAYED_LR = "SETTING_TYPE_AUTO_DELAYED_LR";
    /** 横竖屏切换*/
    public static final String SETTING_TYPE_ORIENTATION = "SETTING_TYPE_ORIENTATION";
    /*		自动播放类型		*/
    public static final int AUTO_TYPE_NU = 0;
    public static final int AUTO_TYPE_UD = 1;
    public static final int AUTO_TYPE_LR = 2;

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
    private static ReadSetting this_;
    private Context mContext;
    private LinkedList<WeakReference<SettingListener>> mSettingListenerList;
    private Handler mHandler;
    private SharedPreferences mSharedPreferences;
    private boolean isSimplified;
    private int mThemeType;
    private int mBrightessLevel;
    private int mAnimType;
    private int mAutoType;
    private int mAutoDelayedUD;
    private int mAutoDelayedLR;
    private boolean isAutoPause;
    private boolean isShakeSwitch;
    private int mOrientationType;

    public static ReadSetting getInstance(Context context){
        if(this_ == null){
            this_ = new ReadSetting(context);
        }
        return this_;
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
        mBrightessLevel = loadBrightessLevel();
        isShakeSwitch = loadShakeSwitch();
        mAnimType = loadAnimType();
        mAutoDelayedUD = loadAutoDelayed(SETTING_TYPE_AUTO_DELAYED_UD);
        mAutoDelayedLR = loadAutoDelayed(SETTING_TYPE_AUTO_DELAYED_LR);
        mOrientationType = loadOrientationType();
        mAutoType = AUTO_TYPE_NU;
        mSettingListenerList = new LinkedList<WeakReference<SettingListener>>();
        mHandler = new Handler(Looper.getMainLooper());
        isSimplified = loadFontFontSimplify();
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
    public void setSimplified(boolean isSimplified){
        this.isSimplified = isSimplified;
        saveFontSimplify(isSimplified);
        notify(SETTING_TYPE_FONT_SIM);
    }

    /**
     * 获取字体是否为简体
     */
    public boolean isSimplified(){
        return isSimplified;
    }


    private void saveFontSimplify(boolean isSimplified){
        mSharedPreferences.edit().putBoolean(SETTING_TYPE_FONT_SIM, isSimplified).commit();
        notify(SETTING_TYPE_FONT_SIZE);
    }

    private boolean loadFontFontSimplify(){
        return mSharedPreferences.getBoolean(SETTING_TYPE_FONT_SIM, true);
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
        return mBrightessLevel;
    }

    public void setBrightess(Window window){
        setBrightess(getBrightessLevel(),window);
    }

    public void setBrightess(int value,Window window){
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = value * 1.0f / 100.0f;
        if(lp.screenBrightness < 0.17){
            lp.screenBrightness = 0.17f;
        }
        window.setAttributes(lp);
    }

    public void setBrightessLevel(int level){
        if(mBrightessLevel == level){
            return;
        }
        mBrightessLevel = level;
        saveBrightessLevel(level);
    }

    private int loadBrightessLevel(){
        return mSharedPreferences.getInt(SETTING_TYPE_BRIGHTESS_LEVEL, 40);
    }

    private void saveBrightessLevel(int level){
        mSharedPreferences.edit().putInt(SETTING_TYPE_BRIGHTESS_LEVEL, level).commit();
        notify(SETTING_TYPE_BRIGHTESS_LEVEL);
    }

    public int getAutoDelayedLR(){
        return mAutoDelayedLR;
    }

    public void setAutoDelayedLR(int index){
        if(mAutoDelayedLR == index){
            return;
        }
        mAutoDelayedLR = index;
        saveAutoDelayed(SETTING_TYPE_AUTO_DELAYED_LR,index);
    }

    public int getAutoDelayedUD(){
        return mAutoDelayedUD;
    }

    public void setAutoDelayedUD(int index){
        if(mAutoDelayedUD == index){
            return;
        }
        mAutoDelayedUD = index;
        saveAutoDelayed(SETTING_TYPE_AUTO_DELAYED_UD,index);
    }

    private int loadAutoDelayed(String key){
        return mSharedPreferences.getInt(key,5);
    }

    private void saveAutoDelayed(String key,int index){
        mSharedPreferences.edit().putInt(key,index).commit();
        if(!isAutoPause){
            notify(SETTING_TYPE_AUTO);
        }
    }

    public boolean isAutoStart(){
        return mAutoType != AUTO_TYPE_NU;
    }

    public int getAutoType(){
        return mAutoType;
    }

    public void setAutoType(){
        setAutoType(loadAutoType());
    }

    public void setAutoType(int type){
        if(mAutoType == type){
            return;
        }
        mAutoType = type;
        saveAutoType(type);
    }

    public void clearAutoType(){
        setAutoType(AUTO_TYPE_NU);
    }

    public boolean isPause(){
        return isAutoPause;
    }

    public void setAutoPause(boolean isPause){
        if(isAutoPause == isPause){
            return;
        }
        if(mAutoType != AUTO_TYPE_NU){
            isAutoPause = isPause;
            notify(SETTING_TYPE_AUTO);
        }else{
            isAutoPause = false;
        }
    }

    private int loadAutoType(){
        return mSharedPreferences.getInt(SETTING_TYPE_AUTO,AUTO_TYPE_UD);
    }

    private void saveAutoType(int type){
        if(type == AUTO_TYPE_NU){
            isAutoPause = false;
        }else{
            mSharedPreferences.edit().putInt(SETTING_TYPE_AUTO, type).commit();
        }
        if(!isAutoPause){
            notify(SETTING_TYPE_AUTO);
        }
    }

    public void clearTempSetting(){
        mAutoType = AUTO_TYPE_NU;
        isAutoPause = false;
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

    public boolean isShakeSwitch(){
        return isShakeSwitch;
    }

    public void setShakeSwitch(boolean shakeSwitch){
        if(isShakeSwitch == shakeSwitch){
            return;
        }
        isShakeSwitch = shakeSwitch;
        saveShakeSwitche(shakeSwitch);
    }

    private boolean loadShakeSwitch(){
        return mSharedPreferences.getBoolean(SETTING_SHAKE_SWITCH,false);
    }

    private void saveShakeSwitche(boolean isShakeSwitch){
        mSharedPreferences.edit().putBoolean(SETTING_SHAKE_SWITCH, isShakeSwitch).commit();
        notify(SETTING_SHAKE_SWITCH);
    }

    public int getOrientationType() {
        return mOrientationType;
    }

    public void changeOrientationType() {
        if(getOrientationType() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mOrientationType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (getOrientationType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mOrientationType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        saveOrientationType(mOrientationType);
    }

    private int loadOrientationType() {
        return mSharedPreferences.getInt(SETTING_TYPE_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void saveOrientationType(int orientationType) {
        mSharedPreferences.edit().putInt(SETTING_TYPE_ORIENTATION, orientationType);
        notify(SETTING_TYPE_ORIENTATION);
    }


    private final void runOnUiThread(Runnable action) {
        mHandler.post(action);
    }

    public interface SettingListener{
        public void onSettingChange(ReadSetting readSetting,String type);
    }
}
