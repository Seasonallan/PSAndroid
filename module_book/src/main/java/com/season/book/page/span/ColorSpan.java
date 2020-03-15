package com.season.book.page.span;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;


public class ColorSpan extends CharacterStyle{
	private int mColor;
	private String mDigest;
	private int mEnd;
	public ColorSpan(int color){
		mColor = color;
	}
	public void setDigest(String content, int end){
		this.mDigest = content;
		this.mEnd = end;
	}

	public int getEnd(){
		return mEnd;
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		if (TextUtils.isEmpty(mDigest)){
			tp.bgColor = mColor;
		}else{
			tp.setFlags(Paint.UNDERLINE_TEXT_FLAG);
			tp.linkColor =  mColor;
		}
	}

}
