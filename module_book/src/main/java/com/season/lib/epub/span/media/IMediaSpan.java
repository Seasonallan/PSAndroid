package com.season.lib.epub.span.media;

import com.season.lib.epub.span.ClickActionSpan;

public interface IMediaSpan extends ClickActionSpan, ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
