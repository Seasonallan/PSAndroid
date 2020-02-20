package com.season.lib.page.span.media;


public interface IMedia {
	public String getVoiceSrc();

	public long getStartPosition();
	
	public boolean contains(long position);
}
