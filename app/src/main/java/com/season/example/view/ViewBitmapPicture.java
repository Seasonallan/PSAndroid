package com.season.example.view;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import com.season.lib.page.layout.Page;

public class ViewBitmapPicture{
	private int mViewIndex;
	private Page mPage;
	private Bitmap mBitmap;
	private Canvas mCanvas;

	public ViewBitmapPicture(int index){
		init(index);
	}

	public void init(int index){
		this.mViewIndex = index;
	}

	public void release() {
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
			mCanvas = null;
		}
	}

	public boolean equals(int index) {
		return mViewIndex == index;
	}

	public Canvas getCanvas(int width, int height) {
		if(mBitmap == null){
			mBitmap = Bitmap.createBitmap(width, height,Config.RGB_565);
			mCanvas = new Canvas(mBitmap);
		}else{
			mCanvas.drawColor(0,Mode.CLEAR);
		}
		return mCanvas;
	}

	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

}
