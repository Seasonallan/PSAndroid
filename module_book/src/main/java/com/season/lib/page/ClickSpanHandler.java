package com.season.lib.page;

import android.graphics.RectF;

import com.season.lib.page.span.ClickActionSpan;

public interface ClickSpanHandler {
	public boolean onClickSpan(ClickActionSpan clickableSpan, RectF localRect, int x, int y);
}
