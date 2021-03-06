package com.season.ps.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;


/**
 * 制作的背景
 */

public class ColorPickView extends AppCompatRadioButton {

    Paint paint;

    public ColorPickView(Context context) {
        super(context);
    }

    public ColorPickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap bitmap;
    public void setDrawImage(Bitmap bitmap){
        this.bitmap = bitmap;
        invalidate();
    }

    float radius;
    float padding;
    float stroke;
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
        if (bitmap != null && !bitmap.isRecycled()){
            canvas.drawBitmap(bitmap, null, new RectF(0, 0, getWidth(), getHeight()), paint);
        }
        if (isChecked()){
            paint.setColor(0xFFFFFFFF);
            paint.setStrokeWidth(stroke);
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), stroke, stroke, paint);

            paint.setColor(0xFF2EDBC4);
            paint.setStrokeWidth(stroke);
            canvas.drawRoundRect(new RectF(padding, padding, getWidth() - padding, getHeight() - padding), radius, radius, paint);
        }

    }
}
