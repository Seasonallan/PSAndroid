package com.season.example.bookreader.tagspan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.season.example.BaseApplication;
import com.season.lib.book.text.style.ClickActionSpan;
import com.season.lib.book.text.style.ReplacementSpan;
import com.season.lib.book.text.style.ResourceSpan;
import com.season.myapplication.R;

public class NoteSpan extends ReplacementSpan implements ResourceSpan,ClickActionSpan{
	private String mNote;
	private int mWidth;
	private Drawable mDrawable;
	private float mSize;
	public NoteSpan(String note){
		mNote = note;
		mSize = -1;
	}
	
	public void setNote(String note){
		mNote = note;
	}
	
	public String getNote() {
		return mNote;
	}

	@Override
	public boolean isClickable() {
		return !TextUtils.isEmpty(mNote);
	}

	@Override
	public void release() {
		mDrawable = null;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			int left, int top, int right, int bottom, int maxW,int maxH,Paint paint) {
		if(mDrawable == null){
			mDrawable = BaseApplication.getInstance().getResources().getDrawable(R.drawable.note);
		}
		mDrawable.setBounds(left, top, right, bottom);
		mDrawable.draw(canvas);
	}

	@Override
	public void getSize(Paint paint, CharSequence text, int start, int end,
			int maxW, int maxH, Rect container) {
		if(mSize != paint.getTextSize()){
			mWidth = (int) paint.measureText("测");
			mSize = paint.getTextSize();
		}
		container.set(0, 0, mWidth, mWidth);
	}

	@Override
	public void checkContentRect(RectF rect) {
	}
}
