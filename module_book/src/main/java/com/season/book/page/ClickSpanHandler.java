package com.season.book.page;

import android.graphics.RectF;

import com.season.book.page.span.ClickActionSpan;

public interface ClickSpanHandler {
	/**
	 * 点击到可点击的span
	 * @param clickableSpan
	 * @param localRect
	 * @param x
	 * @param y
	 * @return
	 */
	boolean onClickSpan(ClickActionSpan clickableSpan, RectF localRect, int x, int y);

	/**
	 * 检测是否是笔记span并进行操作
	 * @param i
	 * @return
	 */
	boolean checkDigestSpan(int i);
}
