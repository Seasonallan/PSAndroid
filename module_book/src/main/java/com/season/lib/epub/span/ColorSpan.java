package com.season.lib.epub.span;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class ColorSpan extends CharacterStyle {
	private int mColor;
	public ColorSpan(int color){
		mColor = color;
	}
	@Override
	public void updateDrawState(TextPaint tp) {
		tp.bgColor = mColor;
	}
}
