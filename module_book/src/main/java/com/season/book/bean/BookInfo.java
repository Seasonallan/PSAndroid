package com.season.book.bean;

import java.io.Serializable;

/** 书籍信息
 * @author mingkg21
 * @email mingkg21@gmail.com
 * @date 2013-2-19
 */
public class BookInfo implements Serializable {

	/** 书籍唯一ID */
	public String id = "";
	/** 书籍书名*/
	public String title = "";
	/** 书籍作者*/
	public String author = "";
	/** 书籍的封面*/
	public String cover = "";
	/** 书籍发布厂商*/
	public String publisher = "";
	/** 书籍本地文件地址*/
	public String filePath;

	/** 书籍网络ID，用语网络请求，目前测试是doupocangqiong1.com 斗破的书籍*/
	public int netIndex;

	/** 书籍raw文件*/
	public int rawId = -1;

	/**书籍内容是否加密*/
	public boolean isMediaDecode;
	@Deprecated
	public boolean isCartoon = false;


	public BookInfo() {
	}

	public BookInfo(String bookId, String bookName, int rawId) {
		this.id = bookId;
		this.rawId = rawId;
		this.title = bookName;
	}

	public BookInfo(String bookId, String bookName) {
		this.id = bookId;
		this.title = bookName;
	}

	public BookInfo(String bookId, String bookName, String cover) {
		this.id = bookId;
		this.title = bookName;
		this.cover = cover;
	}


	@Override
	public String toString() {
		return "BookInfo [title=" + title + ", id=" + id + ", author=" + author
				+ ", publisher=" + publisher + ", path="
				+ filePath + "]";
	}
}
