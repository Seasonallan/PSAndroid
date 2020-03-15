package com.season.book.page.span.media;

import com.season.book.page.span.ClickActionSpan;

public interface IMediaSpan extends ClickActionSpan, ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
