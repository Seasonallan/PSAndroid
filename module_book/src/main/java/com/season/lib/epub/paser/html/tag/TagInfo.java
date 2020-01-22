package com.season.lib.epub.paser.html.tag;

import android.text.TextUtils;
import android.text.style.CharacterStyle;

import com.season.lib.dimen.DimenUtil;
import com.season.lib.epub.Border;
import com.season.lib.epub.StyleText;
import com.season.lib.epub.paser.html.css.PropertyValue;
import com.season.lib.epub.span.EmptySpan;

import org.xml.sax.Attributes;

import java.util.ArrayList;

public class TagInfo {
    public String mTag;
    public String mClass;
    public String mId;
    public Attributes mAttributes;
    public int mStart;
    public CharacterStyle[] mSpanArr;
    public int mSpanSize;
    public StyleText mStyleText;
    public StyleText mParentStyleText;
    public String mWidthValue;
    public String mHeightValue;
    public String mBGWidthValue;
    public String mBGHeightValue;
    public ArrayList<PropertyValue> mClassInfos;
    public SizeInfo mSizeInfo;
    public EmptySpan EMPTY_STYLE = new EmptySpan();
    public TagInfo(String tag, int start, Attributes attributes, SizeInfo sizeInfo){
        mClassInfos = new ArrayList<PropertyValue>();
        mSizeInfo = sizeInfo;
        init(tag, start, attributes);
    }

    public boolean isCover(){
        return mStyleText.isCover();
    }

    public Integer handleBorderSize(String string){
        if(TextUtils.isEmpty(string)){
            return null;
        }
        Integer s = handleSize(string);
        if(s == null){
            if("thin".equals(string)){
                s = DimenUtil.DIPToPX(1);
            }else if("medium".equals(string)){
                s = DimenUtil.DIPToPX(2);
            }else if("thick".equals(string)){
                s = DimenUtil.DIPToPX(3);
            }
        }
        return s;
    }

    public Integer handleSize(String value){
        if(!TextUtils.isEmpty(value)){
            try {
                if(value.length() > 2 && value.indexOf("em") != -1){
                    return (int) (Float.valueOf(value.substring(0, value.length() - 2)) * mSizeInfo.mEmUnit);
                }else if(value.length() > 2 && value.indexOf("px") != -1){
                    return (int) (Float.valueOf(value.substring(0, value.length() - 2)) * 1f);
                }else if(value.length() > 1 && value.indexOf("%") != -1){
                    return (int) (Float.valueOf(value.substring(0, value.length() - 1)) * mSizeInfo.mPageWidth / 100);
                }else if(TextUtils.isDigitsOnly(value)){
                    return (int) (Float.valueOf(value) * 1f);
                }
            } catch (Exception e) {}
        }
        return null;
    }

    public void init(String tag,int start,Attributes attributes){
        mTag = tag.toLowerCase();
        mClass = attributes.getValue("class");
        mId = attributes.getValue("id");
        mStart = start;
        mAttributes = attributes;
        mClassInfos.clear();
        mStyleText = null;
        mParentStyleText = null;
        mWidthValue = null;
        mHeightValue = null;
        mBGWidthValue = null;
        mBGHeightValue = null;
        mSpanSize = 0;
    }

    public void removeStyle(Class<?> clazz){
        for (int i = 0; i < mSpanSize; i++) {
            CharacterStyle object = mSpanArr[i];
            if(clazz.isInstance(object)){
                mSpanArr[i] = EMPTY_STYLE;
            }
        }
    }

    public <T>T findStyle(Class<T> clazz){
        for (int i = 0; i < mSpanSize; i++) {
            CharacterStyle object = mSpanArr[i];
            if(clazz.isInstance(object)){
                return (T) object;
            }
        }
        return null;
    }

    public void addStyle(CharacterStyle object){
        if(mSpanArr == null){
            mSpanArr = new CharacterStyle[10];
        }
        if(mSpanArr.length - mSpanSize < 1){
            CharacterStyle[] tempArr = new CharacterStyle[mSpanArr.length + 5];
            System.arraycopy(mSpanArr, 0, tempArr, 0, mSpanArr.length);
            mSpanArr = tempArr;
        }
        mSpanArr[mSpanSize] = object;
        mSpanSize++;
    }

    /**
     * @return the mClass
     */
    public String getClazz() {
        return mClass;
    }

    public String getId(){
        return mId;
    }
    /**
     * @return the mWidthValue
     */
    public String getWidthValue() {
        return mWidthValue;
    }

    /**
     * @return the mHeightValue
     */
    public String getHeightValue() {
        return mHeightValue;
    }

    /**
     * @return the mTag
     */
    public String getTag() {
        return mTag;
    }

    /**
     * @return the mTag
     */
    public Attributes getAttributes() {
        return mAttributes;
    }
    /**
     * @return the mPaddingLeft
     */
    public int getMarginLeft() {
        return mStyleText.getMarginLeft();
    }
    /**
     * @return the mMarginRight
     */
    public int getMarginRight() {
        return mStyleText.getMarginRight();
    }
    /**
     * @return the mMarginTop
     */
    public int getMarginTop() {
        return mStyleText.getMarginTop();
    }
    /**
     * @return the mMarginBottom
     */
    public int getMarginBottom() {
        return mStyleText.getMarginBottom();
    }
    /**
     * @return the mPaddingLeft
     */
    public int getPaddingLeft() {
        return mStyleText.getPaddingLeft();
    }
    /**
     * @return the mPaddingRight
     */
    public int getPaddingRight() {
        return mStyleText.getPaddingRight();
    }
    /**
     * @return the mPaddingTop
     */
    public int getPaddingTop() {
        return mStyleText.getPaddingTop();
    }
    /**
     * @return the mPaddingBottom
     */
    public int getPaddingBottom() {
        return mStyleText.getPaddingBottom();
    }
    /**
     * @return the mBorder
     */
    public Border getBorder() {
        return mStyleText.getBorder();
    }
    /**
     * @param border the mBorder to set
     */
    public void setBorder(Border border) {
        mStyleText.setBorder(border);
    }
}
