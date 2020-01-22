package com.season.lib.epub.span.media;


public interface IMedia {
	public String getVoiceSrc();

	public long getStartPosition();
	
	public boolean contains(long position);
}
