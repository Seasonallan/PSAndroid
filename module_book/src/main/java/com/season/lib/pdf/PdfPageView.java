package com.season.lib.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

import com.season.lib.pdf.jni.LinkInfo;
import com.season.lib.pdf.jni.MuPDFCore;

public class PdfPageView extends PageView {
	private final MuPDFCore mCore;
	
	public PdfPageView(Context c, MuPDFCore core, Point parentSize, PageViewCallback pageViewCallback) {
		super(c, parentSize, pageViewCallback);
		mCore = core;
	}
	
	public int hitLinkPage(float x, float y) {
		// Since link highlighting was implemented, the super class
		// PageView has had sufficient information to be able to
		// perform this method directly. Making that change would
		// make MuPDFCore.hitLinkPage superfluous.
		float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
		float docRelX = (x - getLeft())/scale;
		float docRelY = (y - getTop())/scale;
		return mCore.hitLinkPage(mPageNumber, docRelX, docRelY);
	}

	@Override
	protected void drawPage(Bitmap bm, int sizeX, int sizeY, int patchX,
			int patchY, int patchWidth, int patchHeight) {
		mCore.drawPage(mPageNumber, bm, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
	}

	@Override
	protected LinkInfo[] getLinkInfo() {
		return mCore.getPageLinks(mPageNumber);
	}

}
