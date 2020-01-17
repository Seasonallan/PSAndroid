package com.season.example.bookreader.tagspan;

import com.season.lib.book.text.style.ClickActionSpan;
import com.season.example.bookreader.media.IMedia;

public interface IMediaSpan extends ClickActionSpan,ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
