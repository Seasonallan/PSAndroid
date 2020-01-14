package com.season.playball.mul;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 12:08
 */
public class BallModel {

    int width, height;

    int radius;
    int color;
    Paint paint;

    float cx, cy;
    int speed = 10;
    double slopDegree = 3;

    BallModel(int width, int height){
        this.width = width;
        this.height = height;
    }

    void randomSetUp(){
        if (radius <= 0) {
            radius = new Random().nextInt(width / 5);
            radius = Math.max(20, radius);
        }
        if (color <= 0) {
            color = 0xff000000 | new Random().nextInt(0x00ffffff);
        }
        if (paint == null) {
            paint = new Paint();
            paint.setColor(color);
        }
        speed = new Random().nextInt(8) + 8;
        slopDegree = new Random().nextInt(360);
        cx = new Random().nextInt(width - radius);
        cy = new Random().nextInt(height - radius);
        fixXY();
    }

    void fixXY() {
        if (cx <= radius || cx >= width - radius) {
            slopDegree = 180 - slopDegree;
        }
        if (cy >= height - radius || cy <= radius) {
            slopDegree = -slopDegree;
        }
        while (slopDegree < 0) {
            slopDegree += 360;
        }
        while (slopDegree > 360) {
            slopDegree -= 360;
        }

        cx = Math.max(radius, cx);
        cy = Math.max(radius, cy);
        cx = Math.min(cx, width - radius);
        cy = Math.min(cy, height - radius);
    }

    boolean isCrash(BallModel ballView) {
        double xy = (cx - ballView.cx) * (cx - ballView.cx) + (cy - ballView.cy) * (cy - ballView.cy);
        xy = Math.sqrt(xy);
        if (xy <= radius + ballView.radius) {
            return true;
        }
        return false;
    }

    void crashChanged(BallModel crashModel){
        double degree = Math.atan2((cy - crashModel.cy), (cx - crashModel.cx));
        degree = 180 * degree / Math.PI;
        slopDegree =  degree;// - slopDegree;// + slopDegree - degree ;
    }

    public void move() {
        cx += speed * Math.cos(slopDegree * Math.PI / 180);
        cy += speed * Math.sin(slopDegree * Math.PI / 180);
        fixXY();
    }

    void draw(Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, paint);
    }


}
