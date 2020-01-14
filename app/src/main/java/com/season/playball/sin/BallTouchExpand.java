package com.season.playball.sin;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;

/**
 * Disc: 触摸浮窗
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 19:14
 */
public class BallTouchExpand {

    Ball ball;
    TextPaint textPaint;
    boolean expand = true;
    float maxRadius = 80;
    float radius = 0;
    float padding = 20;

    float degree = 0;
    String[] items = {"hide","stop","start","quick","slow"};

    int touchIndex = -1;
    void onDraw(Canvas canvas, float ex, float ey){
        if (textPaint == null){
            textPaint = new TextPaint();
            textPaint.setColor(Color.WHITE);
        }
        radius += 3;
        if (touchIndex < 0)
          degree += 0.0001;
        radius = Math.min(radius, maxRadius);
        textPaint.setTextSize(radius * 2 / 3);
        if (expand){
            touchIndex = -1;
            float toCenter = ball.radius + padding + radius;
            float currentDegree = degree;
            int index = 0;
            for (String item : items){
                float x = (float) (ball.cx + toCenter * Math.cos(180 * currentDegree / Math.PI));
                float y = (float) (ball.cy - toCenter * Math.sin(180 * currentDegree / Math.PI));
                if (isTouched(ex, ey, x, y)){
                    canvas.drawCircle(x, y, radius * 6/5, ball.paint);
                    touchIndex = index;
                }else{
                    canvas.drawCircle(x, y, radius, ball.paint);
                }

                Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
                float textX = textPaint.measureText(item) / 2;
                float textY  = (fontMetrics.descent - fontMetrics.ascent) / 4;
                canvas.drawText(item, x - textX, y + textY, textPaint);
                currentDegree += 60;
                index ++;
            }
        }
    }

    public int getTouchIndex(){
        return touchIndex;
    }

    boolean isTouched(float x, float y, float cx, float cy) {
        RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        if (rectF.contains(x, y)) {
            return true;
        }
        return false;
    }

}
