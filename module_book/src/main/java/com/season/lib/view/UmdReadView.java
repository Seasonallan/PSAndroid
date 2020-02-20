package com.season.lib.view;

import android.content.Context;
import android.text.TextUtils;

import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.bean.Chapter;
import com.season.lib.book.EncryptUtils;
import com.season.lib.page.paser.html.DataProvider;
import com.season.lib.page.paser.html.ICssProvider;
import com.season.lib.plugin.umd.UmdPlugin;
import com.season.lib.util.LogUtil;

import java.util.ArrayList;

public class UmdReadView extends BaseHtmlReadView {
	private String mSecretKey;
	private UmdPlugin mPlugin;
	private BookInfo mBookInfo;


	/**
	 * 是否为整本排版
	 */
	@Override
	public boolean isLayoutAll(){
		return false;
	}

	public UmdReadView(Context context, BookInfo book, IReadCallback readCallback) {
		super(context, book, readCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPlugin.recyle();
	}


	@Override
	public int onInitReaderInBackground(final int fRequestCatalogIndex,final int fRequestPageCharIndex, String secretKey) {
        mSecretKey = secretKey;
		try {
            try {
                mPlugin = new UmdPlugin(mBook.path);
                mPlugin.init(secretKey);
            }catch (Exception e){
            }
			// 书籍信息
			mBookInfo = mBook;
			mBookInfo.id = mBook.id;
            mReadCallback.setFreeStart_Order_Price(Integer.MAX_VALUE , true, null, null);
			// 读章节信息
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					initView(mPlugin.getCatalog().size(), fRequestCatalogIndex, fRequestPageCharIndex);
				}
			});
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return ERROR_GET_CONTENT_INFO;
        }
        return SUCCESS;
	}

	@Override
	public String getChapterInputStream_(int chapterIndex) {
		String defaultString = "<html><body>无法阅读此章节.原因：<p>1.该章节未购买</p><p>2.书籍格式错误-请到书架删除后重新下载！</p></body></html>";
		//用于触发购买点
		String content = defaultString;
		Chapter chapter = null;
		try {
			chapter = mPlugin.getChapter(chapterIndex);
		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
		if (chapter != null) {
			byte[] contentByte = null;
			try {
				if (TextUtils.isEmpty(mSecretKey)) {
					contentByte = chapter.getContent();
				}else {
					contentByte = EncryptUtils.decryptByAES(chapter.getContent(), mSecretKey);
				}
				if(contentByte != null){
					content = new String(contentByte, mPlugin.getEncode());
				}
			} catch (Exception e) {
				if(contentByte != null){
					content = new String(contentByte);
				}
			}
		}
		if(TextUtils.isEmpty(content)){
			content = defaultString;
		}else if(content.indexOf("<html") == -1){
			StringBuffer temp = new StringBuffer();
			temp.append("<html><body><p>");
			content = content.replaceAll("\r\n", "</p><p>");
			content = content.replaceAll("\n", "</p><p>");
			content = content.replaceAll("\r", "</p><p>");
			temp.append(content);
			temp.append("</p></body></html>");
			content = temp.toString();
		}
		return content;
	}

	@Override
	public ICssProvider getCssProvider() {
		return null;
	}

	@Override
	public DataProvider getDataProvider() {
		return null;
	}

	@Override
	public ArrayList<Catalog> getChapterList() {
		return mPlugin.getCatalog();
	}

	@Override
	public BookInfo getBookInfo() {
		return mBookInfo;
	}

	@Override
	public String getChapterId(int chapterIndex) {
		return chapterIndex +"";
	}

	@Override
	protected int getChapterIndex(Catalog catalog) {
		return mPlugin.getChapterIndex(catalog);
	}

	@Override
	protected Catalog getCatalogByIndex(int chapterIndex) {
        if (chapterIndex < 0){
            return null;
        }
		return mPlugin.getCatalogByIndex(chapterIndex);
	}

	@Override
	protected String getChapterName(int chapterIndex) {
		Catalog catalog = getCatalogByIndex(chapterIndex);
		if(catalog != null && !TextUtils.isEmpty(catalog.getText())){
			return catalog.getText();
		}
		return "";
	}

	@Override
	protected int loadCatalogID(String chapterID) {
		int index = mPlugin.getChapterPosition(chapterID);
		return index < 0 ? 0 : index;
	}

}
