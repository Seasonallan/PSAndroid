package com.season.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.plugin.PluginManager;
import com.season.lib.plugin.epub.Resource;
import com.season.lib.os.SinThreadPool;
import com.season.lib.page.paser.html.CssProvider;
import com.season.lib.page.paser.html.DataProvider;
import com.season.lib.page.paser.html.ICssProvider;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.book.EncryptUtils;
import com.season.lib.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadView extends BaseHtmlReadView {
	private String mSecretKey;
	private PluginManager mPlugin;
	private BookInfo mBookInfo;


	public ReadView(Context context, BookInfo book, IReadCallback readCallback) {
		super(context, book, readCallback);
	}

	@Override
	public void release() {
		super.release();
		mPlugin.recyle();
	}

	@Override
	protected boolean isLayoutAll() {
		return false;
	}

	@Override
	public int decodeBookFromPlugin(final int fRequestCatalogIndex, final int fRequestPageCharIndex, String secretKey) {
        mSecretKey = secretKey;
		try {
            try {
                mPlugin = PluginManager.getPlugin(mBook.path);
                mPlugin.init(secretKey);
            }catch (Exception e){
            }
			// 书籍信息
			mBookInfo = mPlugin.getBookInfo(mBook);
			mBookInfo.id = mBook.id;
            mReadCallback.setFreeStart_Order_Price(Integer.MAX_VALUE , true, null, null);
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
						initView(mPlugin.getCatalog().size(), fRequestCatalogIndex, fRequestPageCharIndex);
					}
				}
			});
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return ERROR_GET_CONTENT_INFO;
        }
        return SUCCESS;
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
			content = mPlugin.getChapter(chapterIndex);
		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
		if(TextUtils.isEmpty(content)){
			content = defaultString;
		}else if(content.indexOf("<html") == -1){
			content = mPlugin.getFixHtml(content);
		}
		return content;
	}

	@Override
	public ICssProvider getCssProvider() {
		return mCssProvider;
	}

	@Override
	public DataProvider getDataProvider() {
		return mDataProvider;
	}

	@Override
	public ArrayList<Catalog> getChapterList() {
		return mPlugin.getCatalog();
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

	private CssProvider mCssProvider = new CssProvider(new CssProvider.ICssLoader() {
		@Override
		public String load(String path) {
			try {
				Resource resource = mPlugin.findResource(path);
				byte[] data = null;
				if(resource != null){
					if (TextUtils.isEmpty(mSecretKey)) {
						data = resource.getData();
					}else {
						data = EncryptUtils.decryptByAES(resource.getData(),mSecretKey);
					}
				}
				if (data != null) {
					return new String(data);
				}
			} catch (Exception e) {
			}
			return null;
		}
	});
	
	private DataProvider mDataProvider = new DataProvider() {
		@Override
		public Drawable getDrawable(final String source,final DrawableContainer drawableContainer) {
			Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
            new SinThreadPool().addTask(new Runnable() {
                Bitmap bitmap = null;

                @Override
                public void run() {
                    if (drawableContainer.isInvalid()) {
                        return;
                    }
                    try {
                        bitmap = BitmapUtil.clipScreenBoundsBitmap(getDataStream(source));
                    } catch (Exception e) {
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BitmapDrawable bitmapDrawable = null;
                            if (bitmap != null) {
                                bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                            }
                            drawableContainer.setDrawable(bitmapDrawable);
                        }
                    });
                }
            });
			return drawable;
		}

		@Override
		public Context getContext() {
			return ReadView.this.getContext().getApplicationContext();
		}

		@Override
		public InputStream getDataStream(String source) throws IOException {
			Resource resource = mPlugin.findResource(source);
			if(resource != null){
				//目前media文件不加密。音视频
				if (source.endsWith(".mp3")||source.endsWith(".mp4")) {//服务器对此后缀不加密
					if (mBookInfo.isMediaDecode) {
						if (TextUtils.isEmpty(mSecretKey)) {
							return resource.getDataStream();
						}else {
							return EncryptUtils.decryptByAES(resource.getDataStream(),mSecretKey);
						}
					}else {
						return resource.getDataStream();
					}
				}else {
					if (TextUtils.isEmpty(mSecretKey)) {
						return resource.getDataStream();
					}else {
						return EncryptUtils.decryptByAES(resource.getDataStream(),mSecretKey);
					}
				}
			}
			return null;
			
		}

		@Override
		public boolean hasData(String source) {
			Resource resource = mPlugin.findResource(source);
			return resource != null;
		}
	};
}
