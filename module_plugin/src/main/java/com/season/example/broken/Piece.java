package com.season.example.broken;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.season.lib.BaseContext;
import com.season.lib.support.bitmap.BitmapUtil;

class Piece implements Comparable {
    Bitmap bitmap;
    Matrix matrix;
    private int x;
    private int y;
    private int rotateX;
    private int rotateY;
    private float angle;
    private float speed;
    private int shadow;
    private int limitY;
    public Piece(int x, int y, Bitmap bitmap, int shadow){
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.shadow = shadow;

        if(bitmap != null) {
            matrix = new Matrix();
            matrix.postTranslate(x, y);

            speed = RandomUtil.nextFloat(1,4);
            rotateX = RandomUtil.nextInt(bitmap.getWidth());
            rotateY = RandomUtil.nextInt(bitmap.getHeight());
            angle = RandomUtil.nextFloat(0.6f) * (RandomUtil.nextBoolean() ? 1 : -1);

            int bitmapW = bitmap.getWidth();
            int bitmapH = bitmap.getHeight();
            limitY = bitmapW > bitmapH ? bitmapW : bitmapH;
            limitY += BaseContext.getDisplayMetrics().heightPixels;
        }
    }

    @Override
    public int compareTo(Object another) {
        return shadow - ((Piece)another).shadow;
    }

    public boolean advance(float fraction){
        float s = (float) Math.pow(fraction * 1.1226f, 2) * 8 * speed;
        float zy =  y + s * BaseContext.getDisplayMetrics().heightPixels / 10;
        float r = fraction * fraction;

        matrix.reset();
        matrix.setRotate(angle * r * 360, rotateX, rotateY);
        matrix.postTranslate(x, zy);
        if(zy <= limitY)
            return true;
        else
            return false;
    }

    public void release(){
        matrix = null;
        BitmapUtil.recycleBitmaps(bitmap);
    }
}
