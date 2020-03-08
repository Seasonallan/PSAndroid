package com.season.lib.view.gif;

import android.graphics.Canvas;

import com.season.lib.gif.frame.GifDecoder;
import com.season.lib.gif.frame.GifFrame;

public class FramePlugin extends GifPlugin {
    private GifDecoder gifDecoder = null;

    @Override
    public boolean init(String file) {
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(file);
        gifDecoder.start();
        return true;
    }


    @Override
    public void onRelease() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.interrupt();
            gifDecoder.destroy();
        }
        gifDecoder = null;
    }


    public void drawCanvasTime(Canvas canvas, int time) {
        if (gifDecoder==null){
            return;
        }
        GifFrame gifFrame = gifDecoder.getFrame(time);
        if (gifFrame != null) {
            if (gifFrame.image != null && gifFrame.image.isRecycled() == false) {
                canvas.drawBitmap(gifFrame.image, 0, 0, null);
            }
        }
    }

    @Override
    public String getDescription() {
        return "frameDecoder";
    }


    @Override
    public int getDuration() {
        return gifDecoder.getDuration();
    }

    @Override
    public int getDelay() {
        return gifDecoder.getDelay();
    }

}
