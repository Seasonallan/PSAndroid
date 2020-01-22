package com.season.lib.epub.bean;

import java.io.Serializable;

/** 书籍信息
 * @author mingkg21
 * @email mingkg21@gmail.com
 * @date 2013-2-19
 */
public class BookInfo implements Serializable {
	public String title = "";
	public String id = "";
	public String author = "";
	public String publisher = "";
	/**章节数*/
	public int chapterNum;
	public boolean isMediaDecode;
	public boolean isCartoon = false;


	public BookInfo() {
	}

	public BookInfo(String bookId, String bookName, int chapterNum) {
		this.id = bookId;
		this.title = bookName;
		this.chapterNum = chapterNum;
	}

	@Override
	public String toString() {
		return "BookInfo [title=" + title + ", id=" + id + ", author=" + author
				+ ", publisher=" + publisher + ", isMediaDecode="
				+ isMediaDecode + "]";
	}
}
