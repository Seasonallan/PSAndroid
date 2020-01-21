package com.season.lib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;


/**
 * Created by Administrator on 2017/10/13.
 */

public class ColorImageView extends AppCompatImageView {

    Paint paint;
    int padding;
    public ColorImageView(Context context) {
        super(context);
    }

    public ColorImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(47,47);
    }

    float stroke;
    float radius;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint == null){
            radius = 8;
            stroke = 4;
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            padding = (int) (stroke/2);
        }

        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(stroke);
        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), stroke, stroke, paint);

        paint.setColor(0xFF2EDBC4);
        paint.setStrokeWidth(stroke);
        canvas.drawRoundRect(new RectF(padding, padding, getWidth() - padding, getHeight() - padding), radius, radius, paint);

    }
}
