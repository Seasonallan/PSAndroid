package com.season.example.media;


public interface IMedia {
	public String getVoiceSrc();

	public long getStartPosition();
	
	public boolean contains(long position);
}
