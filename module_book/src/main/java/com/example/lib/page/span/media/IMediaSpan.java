package com.example.lib.page.span.media;

import com.example.lib.page.span.ClickActionSpan;

public interface IMediaSpan extends ClickActionSpan, ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
