package com.season.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.season.lib.bitmap.BitmapUtil;

/**
 * 圆角ImageVew
 * 原理： 使用paint的BitmapShader
 */
public class CircleImageView  extends AppCompatImageView {


    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Paint mBitmapPaint, mBorderPaint;
    private int mRadius;
    private BitmapShader mBitmapShader;
    private int mWidth;


    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(4);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = mWidth / 2;
        setMeasuredDimension(mWidth, mWidth);

    }

    private void initBitmapShader() {
        if (mBitmapShader != null){
            return;
        }
        Bitmap bitmap = BitmapUtil.getBitmapFromDrawable(getDrawable());
        if (bitmap == null) {
            invalidate();
            return;
        }
        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        int bSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        float scale = mWidth * 1.0f / bSize;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        mBitmapShader.setLocalMatrix(matrix);
        mBitmapPaint.setShader(mBitmapShader);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        initBitmapShader();
        canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
        canvas.drawCircle(mRadius, mRadius, mRadius - 2, mBorderPaint);
    }
}
