package com.season.ps.view.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.season.lib.support.bitmap.BitmapUtil;
import com.season.ps.gif.frame.GifDecoderOneByOne;
import com.season.ps.gif.movie.FrameDecoder;
import com.season.ps.view.ps.PSLayer;


/**
 *
 * 可解析大GIF图片，只缓存两帧bitmap
 * @see GifFrameView
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 18:37
 */
public class GifFrameView extends View{

    public String url;
    private int position;

    public GifFrameView(Context context) {
        super(context);
    }

    public GifFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private GifDecoderOneByOne gifDecoder = null;
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
        gifDecoder = new GifDecoderOneByOne();
        gifDecoder.setGifImage(file);
        gifDecoder.start();
        requestLayout();
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

    public void onRelease() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.release();
            gifDecoder.interrupt();
        }
        gifDecoder = null;
    }


    @Override
    public void onDraw(Canvas canvas) {
        if (gifDecoder==null){
            return;
        }
        Bitmap gifFrame = gifDecoder.getFrame(position);
        if (gifFrame != null && gifFrame.isRecycled() == false) {
            canvas.drawBitmap(gifFrame, 0, 0, null);
        }
        invalidate();
    }

    public void setPosition(int i) {
        this.position = i;
    }
}
