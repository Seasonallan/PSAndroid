package com.season.lib.page.span;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;


public class ColorSpan extends CharacterStyle{
	private int mColor;
	private String mDigest;
	public ColorSpan(int color){
		mColor = color;
	}
	public void setDigest(String content){
		this.mDigest = content;
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
