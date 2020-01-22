package com.season.example.tagspan;

import com.season.lib.epub.span.ClickActionSpan;
import com.season.example.media.IMedia;

public interface IMediaSpan extends ClickActionSpan,ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
