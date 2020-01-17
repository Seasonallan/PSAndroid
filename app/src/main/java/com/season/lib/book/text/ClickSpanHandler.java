package com.season.lib.book.text;

import android.graphics.RectF;

import com.season.lib.book.text.style.ClickActionSpan;

public interface ClickSpanHandler {
	public boolean onClickSpan(ClickActionSpan clickableSpan,RectF localRect,int x, int y);
}
