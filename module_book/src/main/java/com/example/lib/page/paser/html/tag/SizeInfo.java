package com.example.lib.page.paser.html.tag;

import android.graphics.Rect;

public class SizeInfo {
    public int mEmUnit;
    public int mPageWidth;
    public int mPageHeight;
    public Rect mPageRect;
    public SizeInfo(int emUnit, Rect pageRect) {
        mEmUnit = emUnit;
        mPageRect = pageRect;
        mPageWidth = pageRect.width();
        mPageHeight = pageRect.height();
    }
}
