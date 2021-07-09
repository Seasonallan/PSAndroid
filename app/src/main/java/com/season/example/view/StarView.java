package com.season.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.season.lib.support.math.MathUtil;
import com.season.lib.util.LogUtil;
import com.season.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarView extends View {


    public StarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        for (int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);
            star.move();
            star.draw(canvas);
        }
        startAnimation();
    }

    private void startAnimation(){
        if (refreshThread == null){
            synchronized (this){
                if (refreshThread == null){
                    refreshThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (running){
                                if (stars.size() <= 0 && getWidth() > 0){
                                    for (int i = 0; i < 100; i++) {
                                        stars.add(new Star(getWidth(), getHeight()));
                                    }
                                }
                                postInvalidate();
                                try {
                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    refreshThread.start();
                }
            }
        }
    }


    List<Star> stars = new ArrayList<>();


    static class Star{
        int left, top;//开始点
        float radius = 1;
        int endTop, endLeft; //结束点

        float startProgress = 0;
        float progress = 0;
        int width,  height;
        public Star(int width, int height) {
            this.width = width;
            this.height = height;
            reset();
        }

        public void reset(){
            double r = Math.sqrt(width/2 * width/2 + height/2 * height/2);
            endLeft = (int) (new Random().nextInt((int) r * 2) - (r - width/2));

            endTop = (int) Math.sqrt(r * r - (width/2 - endLeft) * (width/2 - endLeft));
            if (new Random().nextBoolean()){
                endTop += height/2;
            }else{
                endTop = height/2 - endTop;
            }

            startProgress = new Random().nextInt(100);
            progress = startProgress;
        }

        public void move() {
            progress += 1.1f;

            if (endLeft > width/2){
                left = width/2 + (int) (progress * (endLeft - width/2)/100);
            }else{
                left = width/2 - (int) (progress * (width/2 - endLeft)/100);
            }
            if (endTop > height/2){
                top = height/2 + (int) (progress * (endTop - height/2)/100);
            }else{
                top = height/2 - (int) (progress * (height/2 - endTop)/100);
            }
            radius = 1 + 20 * (progress - startProgress ) /168;
            ///radius = 50;
            if (progress > 100){
                reset();
            }
        }


        Paint paint;
        public void draw(Canvas canvas) {
            if (paint == null){
                paint = new Paint();
                paint.setColor(Color.WHITE);
            }

            canvas.drawCircle(left, top, radius, paint);
            //RadialGradient radialGradient = new RadialGradient(left + radius, top+ radius,
            //       radius, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            //paint.setShader(radialGradient);
            //canvas.drawRect(left, top, radius + left, radius + top, paint);
        }
    }

    Thread refreshThread;

    volatile boolean running = true;
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        running = false;

    }
}
