package com.season.book.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;

import com.season.book.ReadSetting;
import com.season.lib.support.dimen.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BatteryView {
    private Paint mBatteryPait;
    private Paint mPowerPaint;
    private TextPaint mTextPaint;
    private float mBatteryStroke = 2.0f;

    /**
     * 电池参数
     *
     * @param context
     */
    private float mBatteryHeight;// 电池高度
    private float mBatteryWidth;// 电池的宽度
    private float mCapHeight;
    private float mCapWidth;
    private float mPowerWidth;// 电池身体的总宽度

    /**
     * 电池电量
     *
     * @param context
     */
    private float mPowerPadding = 2f;

    /**
     * 矩形
     */
    private RectF mBatteryRectF;
    private RectF mCapRectF;
    private RectF mPowerRectF;

    private float baseLineY;

    private Context mContext;
    private Runnable mRunnable;
    public BatteryView(Context context, ReadSetting readSetting, Runnable runnable){
        this.mContext = context;
        this.mRunnable = runnable;
        /**
         * 时间
         */
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize((float) (readSetting.getMinFontSize()));
        /**
         * 设置电池画笔
         */
        mBatteryPait = new Paint();
        mBatteryPait.setStrokeWidth(mBatteryStroke);
        mBatteryPait.setStyle(Paint.Style.STROKE);
        mBatteryPait.setAntiAlias(true);
        /**
         * 电量画笔
         */
        mPowerPaint = new Paint();
        mPowerPaint.setStyle(Paint.Style.FILL);
        mPowerPaint.setStrokeWidth(mBatteryStroke);
        mPowerPaint.setAntiAlias(true);

        baseLineY = Math.abs(mTextPaint.ascent() + mTextPaint.descent()) / 2;
        this.mBatteryWidth = mTextPaint.measureText("12:1");
        this.mBatteryHeight = mTextPaint.getFontSpacing() - 8;
        this.mCapHeight = mBatteryHeight/4;
        this.mCapWidth = mBatteryHeight/8;
        this.mPowerWidth = mBatteryWidth - mBatteryStroke/2 - mPowerPadding * 2;// 电池身体的总宽度
        /**
         * 设置电池矩形
         */
        mBatteryRectF = new RectF(mCapWidth, 0, mCapWidth + mBatteryWidth,
                mBatteryHeight);

        /**
         * 设置电池盖矩形
         */

        mCapRectF = new RectF(0, mBatteryHeight / 2 - mCapHeight / 2,
                mCapWidth, mBatteryHeight / 2 + mCapHeight / 2);

        /**
         * 设置电量的矩形
         */

        mPowerRectF = new RectF(1, mPowerPadding, mBatteryWidth + mCapWidth
                - mPowerPadding, mBatteryHeight - mPowerPadding);

        mHandler = new Handler(context.getMainLooper());
        start();
    }

    public void resetColor(ReadSetting readSetting) {
        int color = readSetting.getThemeDecorateTextColor();
        mTextPaint.setColor(color);
        mPowerPaint.setColor(color);
        mBatteryPait.setColor(color);
    }

    Handler mHandler;
    void start(){
        mStop = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStop){
                    return;
                }
                mRunnable.run();
                start();
            }
        }, 30 * 1000);
    }

    private boolean mStop = false;
    public void stop(){
        mStop = true;
    }


    private float radius = 4f;//圆角半径
    public void draw(Canvas canvas, int x, int y){
        canvas.save();
        canvas.translate(x, y - mBatteryHeight/2);
        canvas.drawRoundRect(mBatteryRectF, radius, radius, mBatteryPait);
        canvas.drawRoundRect(mCapRectF, radius, radius, mBatteryPait);

        mPowerRectF.left = mPowerRectF.right
                - mPowerWidth * ScreenUtils.getCurrentBattery(mContext) /100;
        canvas.drawRect(mPowerRectF, mPowerPaint);

        canvas.drawText(new SimpleDateFormat("HH:mm").format(new Date()), mBatteryWidth + mCapWidth * 4,
                  mBatteryHeight/2 + baseLineY, mTextPaint);
        canvas.restore();
    }
}
