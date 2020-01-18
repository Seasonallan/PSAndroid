package com.example.book.bookreader.tagspan;

import com.season.book.text.style.ClickActionSpan;
import com.example.book.bookreader.media.IMedia;

public interface IMediaSpan extends ClickActionSpan,ReaderMediaPlayer.PlayerListener,IMedia {
	public boolean isPlay();
	
	public long computePositionByLocal(int x,int y);
}
