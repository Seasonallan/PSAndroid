package com.season.lib.page.span.media;

import com.season.lib.page.span.ClickActionSpan;

public interface IMediaSpan extends ClickActionSpan, ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
