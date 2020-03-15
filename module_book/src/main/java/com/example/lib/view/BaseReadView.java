package com.example.lib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.View;

import com.example.book.R;
import com.example.lib.ReadSetting;
import com.example.lib.bean.BookInfo;

public abstract class BaseReadView extends AbsReadView{
    protected IReadCallback mReadCallback;
    protected BookInfo mBook;

    private int mLoadingPointSize;
    private long mLastDrawWaitTime;
    private Drawable mBookMarkTip;

    public BaseReadView(Context context, BookInfo book, IReadCallback readCallback) {
        super(context);
        mBook = book;
        mReadCallback = readCallback;
    }

    @Override
    public View getContentView() {
        return this;
    }


    @Override
    public boolean isAnimating(){
        return !mPageAnimController.isAnimStop();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void release() {
        super.release();
        ReadSetting.getInstance(getContext()).saveBookReadProgress(mBook.id, mCurrentChapterIndex, getCurPageStartIndex());
        mBookMarkTip = null;
        if (batteryView != null){
            batteryView.stop();
            batteryView = null;
        }
    }

    /**
     * 获取章节页面的开始char位置
     * @return
     */
    protected abstract int getCurPageStartIndex();

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);
    }

    @Override
    protected void onLoadStyleSetting(boolean isReLayout) {

    }


    /**
     * 获取某个页面是否被标记为书签
     */
    protected boolean isPageMarked(int chapterIndex, int pageIndex){
        return false;
    }

    @Override
    public boolean isCurrentPageMarked(){
        return isPageMarked(mCurrentChapterIndex, mCurrentPageIndex);
    }

    @Override
    protected void drawBookMarkTip(Canvas canvas, int chapterIndex, int pageIndex){
        if(isPageMarked(chapterIndex, pageIndex)){
            if(mBookMarkTip == null){
                mBookMarkTip = getResources().getDrawable(R.drawable.icon_shuqian_chang);
                int bookMarkW = mBookMarkTip.getIntrinsicWidth();
                int bookMarkH = mBookMarkTip.getIntrinsicHeight();
                Rect bounds = new Rect(getWidth() - mReadSetting.getLeftRightSpaceSize() - mReadSetting.getLeftRightSpaceSize() - bookMarkW, 0,
                        getWidth() - mReadSetting.getLeftRightSpaceSize(), bookMarkH);
                mBookMarkTip.setBounds(bounds);
            }
            mBookMarkTip.draw(canvas);
        }
    }

    /**
     * 绘制等待画面
     * @param canvas
     */
    @Override
    protected void drawWaitPage(Canvas canvas,boolean isFirstDraw){
        drawBackground(canvas);
        mTempTextPaint.setTextSize((float) (mReadSetting.getFontSize()));
        mTempTextPaint.setColor(mReadSetting.getThemeTextColor());
        if(!isFirstDraw){
            //书籍还未解析完成
        }
        long timeDifference = System.currentTimeMillis() - mLastDrawWaitTime;
        if(timeDifference  > 200 || timeDifference < 0){
            mLastDrawWaitTime = System.currentTimeMillis();
            mLoadingPointSize++;
            if(mLoadingPointSize > 3){
                mLoadingPointSize = 1;
            }
        }
        mTempTextPaint.setTextAlign(Align.CENTER);
        String str = "努力加载中，请稍候";
        for (int i = 0; i < mLoadingPointSize; i++) {
            str = " " + str + ".";
        }
        canvas.drawText(str, getWidth()/2, getHeight()/2, mTempTextPaint);
        postInvalidateDelayed(500);

    }

    BatteryView batteryView;
    protected void drawReadPercent(Canvas canvas, String pageSizeStr){
        mTempTextPaint.setTextSize((float) (mReadSetting.getMinFontSize()));
        mTempTextPaint.setTextAlign(Align.RIGHT);
        mTempTextPaint.setColor(mReadSetting.getThemeDecorateTextColor());

        if(mBookMarkTip == null){
            mBookMarkTip = getResources().getDrawable(R.drawable.icon_shuqian_chang);
            int bookMarkW = mBookMarkTip.getIntrinsicWidth();
            int bookMarkH = mBookMarkTip.getIntrinsicHeight();
            Rect bounds = new Rect(getWidth() - bookMarkW - bookMarkW, 0, getWidth() - bookMarkW, bookMarkH);
            mBookMarkTip.setBounds(bounds);
        }
        int bookMarkW = 0;//mBookMarkTip.getIntrinsicWidth();

        FontMetricsInt fm = mTempTextPaint.getFontMetricsInt();
        int x = getWidth() - mReadSetting.getLeftRightSpaceSize() - bookMarkW;
        int y = getHeight() - mReadSetting.getTopBottomSpaceSize()  - topChapterNameHeight*2/3 + fm.bottom;
        canvas.drawText(pageSizeStr, x, y, mTempTextPaint);

    }


    @Override
    protected void drawBatteryTime(Canvas canvas){
        if (batteryView == null){
            batteryView = new BatteryView(getContext(), mReadSetting, new Runnable() {
                @Override
                public void run() {
                    ReadSetting.getInstance(getContext()).saveBookReadProgress(mBook.id, mCurrentChapterIndex, getCurPageStartIndex());
                    if (!isAnimating()){
                        postInvalidate();
                    }
                }
            });
        }
        batteryView.resetColor(mReadSetting);
        batteryView.draw(canvas, mReadSetting.getLeftRightSpaceSize(), getHeight() - mReadSetting.getTopBottomSpaceSize()  - topChapterNameHeight*2/3);
    }

    protected void drawChapterName(Canvas canvas,String title){
        if(title == null){
            title = "";
        }
        mTempTextPaint.setTextSize((float) (mReadSetting.getMinFontSize()));
        mTempTextPaint.setTextAlign(Align.LEFT);
        mTempTextPaint.setColor(mReadSetting.getThemeDecorateTextColor());
        FontMetricsInt fm = mTempTextPaint.getFontMetricsInt();
        int x = mReadSetting.getLeftRightSpaceSize();
        int y = mReadSetting.getTopBottomSpaceSize() - fm.top;
        int w = (int) mTempTextPaint.measureText(title);
        int titleMaxWidth = (getWidth() - (mReadSetting.getLeftRightSpaceSize() + mReadSetting.getLeftRightSpaceSize())) * 8 / 10;//标题宽度最宽不能超过去掉padding后屏幕宽度的80%
        if(w > titleMaxWidth){
            title = validateTextWidth(mTempTextPaint, title, titleMaxWidth) + " ";
            w = (int) mTempTextPaint.measureText(title);
        }
        canvas.drawText(title, x, y, mTempTextPaint);
    }

    protected void drawBookName(Canvas canvas,String title){
        if(title == null){
            title = "";
        }
        mTempTextPaint.setTextSize((float) (mReadSetting.getMinFontSize()));
        mTempTextPaint.setTextAlign(Align.LEFT);
        mTempTextPaint.setColor(mReadSetting.getThemeDecorateTextColor());
        FontMetricsInt fm = mTempTextPaint.getFontMetricsInt();
        int x = mReadSetting.getLeftRightSpaceSize();
        int y = mReadSetting.getTopBottomSpaceSize() - fm.top;
        int w = (int) mTempTextPaint.measureText(title);
        int titleMaxWidth = (getWidth() - (mReadSetting.getLeftRightSpaceSize() + mReadSetting.getLeftRightSpaceSize())) * 8 / 10;//标题宽度最宽不能超过去掉padding后屏幕宽度的80%
        if(w > titleMaxWidth){
            title = validateTextWidth(mTempTextPaint, title, titleMaxWidth) + " ";
            w = (int) mTempTextPaint.measureText(title);
        }
        canvas.drawText(title, x, y, mTempTextPaint);
    }

    /**
     * 获取文本根据指定宽省略后文本(带省略号)
     * @param textPaint
     * @param text
     * @param totalWidth 指定最大宽度
     * @return
     */
    private String validateTextWidth(TextPaint textPaint, String text, int totalWidth){
        text = text.substring(0, text.length()-2);
        int textWidth = (int)textPaint.measureText(text);
        if(textWidth > totalWidth){
            return validateTextWidth(textPaint, text, totalWidth);
        }
        return text+"...";
    }

    @Override
    protected void drawBackground(Canvas canvas){
        super.drawBackground(canvas);
    }
}
