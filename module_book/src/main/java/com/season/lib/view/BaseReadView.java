package com.season.lib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.View;

import com.example.book.R;
import com.season.lib.bean.BookInfo;

public abstract class BaseReadView extends AbsReadView implements IReaderView{
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
        mBookMarkTip = null;
        if (batteryView != null){
            batteryView.stop();
            batteryView = null;
        }
    }

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
                Rect bounds = new Rect(getWidth() - PADDING_LEFTRIGHT - PADDING_LEFTRIGHT - bookMarkW, 0,
                        getWidth() - PADDING_LEFTRIGHT, bookMarkH);
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
        String str = getResources().getString(R.string.reader_transition_tip);
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
            Rect bounds = new Rect(getWidth() - PADDING_CONTENT_BOTTOM - bookMarkW, 0, getWidth() - PADDING_CONTENT_BOTTOM, bookMarkH);
            mBookMarkTip.setBounds(bounds);
        }
        int bookMarkW = 0;//mBookMarkTip.getIntrinsicWidth();

        FontMetricsInt fm = mTempTextPaint.getFontMetricsInt();
        int x = getWidth() - PADDING_LEFTRIGHT - bookMarkW;
        int y = getHeight() - PADDING_TOPBOTTOM + fm.bottom;
        canvas.drawText(pageSizeStr, x, y, mTempTextPaint);

    }


    @Override
    protected void drawBatteryTime(Canvas canvas){
        if (batteryView == null){
            batteryView = new BatteryView(getContext(), mReadSetting, new Runnable() {
                @Override
                public void run() {
                    if (!isAnimating()){
                        postInvalidate();
                    }
                }
            });
        }
        batteryView.draw(canvas, PADDING_LEFTRIGHT, getHeight() - PADDING_TOPBOTTOM);
    }

    protected void drawChapterName(Canvas canvas,String title){
        if(title == null){
            title = "";
        }
        mTempTextPaint.setTextSize((float) (mReadSetting.getMinFontSize()));
        mTempTextPaint.setTextAlign(Align.LEFT);
        mTempTextPaint.setColor(mReadSetting.getThemeDecorateTextColor());
        FontMetricsInt fm = mTempTextPaint.getFontMetricsInt();
        int x = PADDING_LEFTRIGHT;
        int y = PADDING_TOPBOTTOM - fm.top;
        int w = (int) mTempTextPaint.measureText(title);
        int titleMaxWidth = (getWidth() - (PADDING_LEFTRIGHT + PADDING_LEFTRIGHT)) * 8 / 10;//标题宽度最宽不能超过去掉padding后屏幕宽度的80%
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
        int x = PADDING_LEFTRIGHT;
        int y = PADDING_TOPBOTTOM - fm.top;
        int w = (int) mTempTextPaint.measureText(title);
        int titleMaxWidth = (getWidth() - (PADDING_LEFTRIGHT + PADDING_LEFTRIGHT)) * 8 / 10;//标题宽度最宽不能超过去掉padding后屏幕宽度的80%
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
