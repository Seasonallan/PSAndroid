package com.season.book.page;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;

import com.season.book.page.layout.Page;
import com.season.lib.util.LogUtil;

public class PageBitmapPicture implements IPagePicture{
	private int mPageIndex;
	private int mChapterIndex;
	private Page mPage;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	
	public PageBitmapPicture(int chapterIndex,int index,Page page){
		init(chapterIndex, index,page);
	}
	
	@Override
	public void init(int chapterIndex,int index,Page page){
		mChapterIndex = chapterIndex;
		mPageIndex = index;
		mPage = page;
	}

	@Override
	public void release() {
		if(mBitmap != null) {
			LogUtil.i("PageBitmapPicture", "<init> release bitmap");
			mBitmap.recycle();
			mBitmap = null;
			mCanvas = null;
		}
	}
	
	@Override
	public boolean equals(int chapterIndex, int index) {
		return mPageIndex == index && mChapterIndex == chapterIndex;
	}

	@Override
	public Bitmap getBitmap(){
		return mBitmap;
	}

	@Override
	public void setBitmap(Bitmap bitmap, int width, int height){
		this.mBitmap = Bitmap.createBitmap(width, height,Config.RGB_565);
		this.mCanvas = new Canvas(mBitmap);
		this.mCanvas.drawBitmap(bitmap, 0, 0, null);
	}


	@Override
	public Canvas getCanvas(int width, int height) {
		if(mBitmap == null){
			mBitmap = Bitmap.createBitmap(width, height,Config.RGB_565);
			mCanvas = new Canvas(mBitmap);
		}else{
			mCanvas.drawColor(0,Mode.CLEAR);
		}
		return mCanvas;
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

	@Override
	public boolean equals(Page page) {
		return page.equals(mPage);
	}
}
