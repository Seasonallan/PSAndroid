package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.gif.movie.FrameDecoder;
import com.season.lib.util.LogUtil;
import com.season.lib.view.gif.GifPlugin;
import com.season.lib.view.gif.MoviePlugin;


/**
 * Disc: 使用解码器GifDecoder解析出每一帧，然后逐帧显示刷新
 *
 * @see CustomGifView
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 18:37
 */
public class CustomGifView extends CustomBaseView{

    public String url;

    public CustomGifView(Context context) {
        super(context);
    }

    public CustomGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    GifPlugin gifPlugin;

    public Bitmap firstFrame;
    public String file;

    public void setMovieResource(String strFileName) {
        this.file = strFileName;
        FrameDecoder frameDecoder  = new FrameDecoder(file);
        firstFrame = frameDecoder .getFrame();
        gifPlugin = GifPlugin.getPlugin(frameDecoder.getTransIndex() >= 0, file, autoPlay);
        if (gifPlugin instanceof MoviePlugin){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            }
            requestLayout();
        }
        LogUtil.e(""+ gifPlugin.getDescription());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (firstFrame != null) {
            int movieWidth = firstFrame.getWidth();
            int movieHeight = firstFrame.getHeight();
            setMeasuredDimension(movieWidth, movieHeight);
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getParent() instanceof PSLayer) {
            int width = right - left;
            int height = bottom - top;
            if (width > 0 && height > 0) {
                ((PSLayer) getParent()).rebindOpView();
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        if (firstFrame != null) BitmapUtil.recycleBitmaps(firstFrame);
        gifPlugin.onRelease();
    }


    @Override
    public void drawCanvasTime(Canvas canvas, int time) {
        gifPlugin.drawCanvasTime(canvas, time);
    }


    @Override
    public int getViewWidth() {
        try {
            return firstFrame.getWidth();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getViewHeight() {
        try {
            return firstFrame.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        return gifPlugin.getDuration();
    }

    @Override
    public int getDelay() {
        return gifPlugin.getDelay();
    }


    public CustomGifView copy() {
        CustomGifView gifView = new CustomGifView(getContext());
        if (!TextUtils.isEmpty(file)) {
            gifView.setMovieResource(file);
        } else {
        }
        gifView.file = file;
        gifView.url = url;
        return gifView;
    }

}
