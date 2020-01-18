package com.season.book.text.style;

import android.graphics.RectF;


public interface ClickActionSpan{
	public boolean isClickable();
	public void checkContentRect(RectF rect);
}
