package com.season.book.view;

import android.content.Context;
import android.text.TextUtils;
import com.season.book.bean.BookInfo;
import com.season.book.bean.Catalog;
import com.season.book.page.paser.html.DataProvider;
import com.season.book.page.paser.html.ICssProvider;
import com.season.lib.http.DownloadAPI;
import com.season.lib.util.LogUtil;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetReadView extends BaseHtmlReadView {

	public NetReadView(Context context, BookInfo book, IReadCallback readCallback) {
		super(context, book, readCallback);
	}

	@Override
	public void release() {
		super.release();
	}

	@Override
	protected boolean isLayoutAll() {
		return false;
	}

	ArrayList<Catalog> result = new ArrayList<>();
	@Override
	public BookInfo decodeBookFromPlugin(final int fRequestCatalogIndex, final int fRequestPageCharIndex, String secretKey) {
		try {
            mReadCallback.setFreeStart_Order_Price(Integer.MAX_VALUE , true, null, null);
            String httpString = DownloadAPI.getRequest("https://doupocangqiong1.com/1/");
            httpString = httpString.substring(httpString.indexOf("dirlist"));
			Pattern p = Pattern.compile("斗破苍穹 第(.*?)target");//正则表达式
			Matcher m = p.matcher(httpString);
			int index = 0;
			while (m.find()) {
				MatchResult mr=m.toMatchResult();
				if (mr.groupCount() > 0){
					Catalog catalog = new Catalog(index);
					String item = mr.group(0);
					item = item.substring(0, item.length() - 8);
					catalog.setText(item);
					result.add(catalog);
					index ++;
				}
			}
			// 读章节信息
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!mBook.isOrder && getBuyIndex() != -1){
						if (fRequestCatalogIndex >= getBuyIndex()) {
							initView(getBuyIndex() + 1, 0, fRequestPageCharIndex);
						}else {
							initView(getBuyIndex() + 1, fRequestCatalogIndex, fRequestPageCharIndex);
						}
					}else{
						initView(result.size(), fRequestCatalogIndex, fRequestPageCharIndex);
					}
				}
			});
        } catch (Exception e) {
            LogUtil.e(TAG, e);
			mBook.decodeResult = false;
        }
        return mBook;
	}
	
	/** 获取购买点*/
	private int getBuyIndex(){
		int feeIndex = -1;
		if (!TextUtils.isEmpty(mBook.feeStart)) {
			String feeStart = String.valueOf(mBook.feeStart);
			if(!TextUtils.isEmpty(feeStart) && !"null".equals(feeStart)){
				try {
					int start = feeStart.lastIndexOf("chapter") + 7;
					int end = feeStart.lastIndexOf(".");
					feeIndex = Integer.valueOf(feeStart.substring(start, end));
				} catch (Exception e) {
					LogUtil.e(e.getMessage());
				}
			}
		}else {
			feeIndex = -1;
		}
		return feeIndex;
	}
	
	@Override
	public String getChapterInputStream_(int chapterIndex) {
		String defaultString = "<html><body>无法阅读此章节.原因：<p>1.该章节未购买</p><p>2.书籍格式错误-请到书架删除后重新下载！</p></body></html>";
		String content = defaultString;
		try {
			//Thread.sleep(3000);

			Map<String, String> params = new HashMap<>();
			params.put("siteid", "0");
			params.put("bid", "1");
			int chapter = mBook.netIndex + chapterIndex;
			params.put("cid", chapter+"");
			content = DownloadAPI.postRequest("https://doupocangqiong1.com/novelsearch/chapter/transcode.html", params);

			JSONObject jsonObject = new JSONObject(content);
			content = jsonObject.getString("info");

		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
		if(TextUtils.isEmpty(content)){
			content = defaultString;
		}else if(content.indexOf("<html") == -1){
			StringBuffer temp = new StringBuffer();
			temp.append("<html><body><p>");
			content = content.replaceAll("<br>", "");
			content = content.replaceAll("\r\n", "</p><p>");
			content = content.replaceAll("\n", "</p><p>");
			content = content.replaceAll("\r", "</p><p>");
			temp.append(content);
			temp.append("</p></body></html>");
			return temp.toString();
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
		return result;
	}

	@Override
	protected int getChapterIndex(Catalog catalog) {
		return result.indexOf(catalog);
	}

	@Override
	protected Catalog getCatalogByIndex(int chapterIndex) {
        if (chapterIndex < 0){
            return null;
        }
		return result.get(chapterIndex);
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
		return Integer.parseInt(chapterID);
	}

}
