package com.season.lib.gif.frame;

import android.graphics.Bitmap;

/**
 * GIF图片每一帧的信息，包含图片和延迟时间
 */
public class GifFrame{
	public GifFrame(Bitmap im, int del) {
		image = im;
		delay = del;
	}

	public Bitmap image;
	public int delay;

	public void release(){
		if (image != null && !image.isRecycled()){
			image.recycle();
			image = null;
		}
	}

}
