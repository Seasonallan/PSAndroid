package com.season.ps.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.season.lib.bitmap.BitmapUtil;
import com.season.ps.gif.movie.FrameDecoder;
import com.season.lib.util.LogUtil;
import com.season.ps.view.gif.GifPlugin;
import com.season.ps.view.gif.MoviePlugin;


/**
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

    private GifPlugin gifPlugin;
    public String file;
    private int width = 0, height = 0;

    public void setMovieResource(String strFileName) {
        this.file = strFileName;
        FrameDecoder frameDecoder  = new FrameDecoder(file);
        Bitmap firstFrame = frameDecoder .getFrame();
        if (firstFrame != null) {
            width = firstFrame.getWidth();
            height = firstFrame.getHeight();
            BitmapUtil.recycleBitmaps(firstFrame);
        }
        gifPlugin = GifPlugin.getPlugin(frameDecoder.getTransIndex() >= 0, file, autoPlay);
        if (gifPlugin instanceof MoviePlugin){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.setLayerType(LAYER_TYPE_SOFTWARE,null);
            }
        }
        requestLayout();
        LogUtil.e(""+ gifPlugin.getDescription());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
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
        gifPlugin.onRelease();
    }


    @Override
    public void drawCanvasTime(Canvas canvas, int time) {
        gifPlugin.drawCanvasTime(canvas, time);
    }


    @Override
    public int getViewWidth() {
        return width;
    }

    @Override
    public int getViewHeight() {
        return height;
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
