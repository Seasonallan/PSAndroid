package com.season.book.page;

import com.season.book.page.layout.Page;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public interface IPagePicture {
	 void init(int chapterIndex, int index, Page page);
	
	 boolean equals(int chapterIndex,int index);

	 boolean equals(Page page);
	
	 Canvas getCanvas(int width, int height);

	 Bitmap onDraw(Canvas canvas);
	
	 void release();
}

