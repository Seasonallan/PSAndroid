package com.season.lib.text;

import android.graphics.RectF;

import com.season.lib.text.style.ClickActionSpan;

public interface ClickSpanHandler {
	public boolean onClickSpan(ClickActionSpan clickableSpan, RectF localRect, int x, int y);
}
