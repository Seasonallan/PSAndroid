package com.season.lib.epub.page;

import android.graphics.RectF;

import com.season.lib.epub.span.ClickActionSpan;

public interface ClickSpanHandler {
	public boolean onClickSpan(ClickActionSpan clickableSpan, RectF localRect, int x, int y);
}
