package com.season.book.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.season.book.bean.BookInfo;
import com.season.book.bean.Catalog;
import com.season.book.plugin.PluginManager;
import com.season.book.plugin.epub.Resource;
import com.season.lib.os.SinThreadPool;
import com.season.book.page.paser.html.CssProvider;
import com.season.book.page.paser.html.DataProvider;
import com.season.book.page.paser.html.ICssProvider;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadView extends BaseHtmlReadView {
	private PluginManager mPlugin;


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
	public BookInfo decodeBookFromPlugin(final int fRequestCatalogIndex, final int fRequestPageCharIndex) {
		try {
            try {
                mPlugin = PluginManager.getPlugin(mBook.filePath);
                mPlugin.init();
            }catch (Exception e){
            }
            String id = mBook.id;
			// 书籍信息
			mBook = mPlugin.getBookInfo(mBook);
			mBook.id = id;
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
        }
        return mBook;
	}

	@Override
	public String getChapterInputStream_(int chapterIndex) {
		String defaultString = "<html><body>无法阅读此章节.原因：<p>1.文件异常</p><p>2.书籍格式错误-请到书架删除后重新下载！</p></body></html>";
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
					data = resource.getData();
					//data = EncryptUtils.decryptByAES(resource.getData());
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
					if (mBook.isMediaDecode) {
						return resource.getDataStream();
						//return EncryptUtils.decryptByAES(resource.getDataStream());
					}else {
						return resource.getDataStream();
					}
				}else {
					return resource.getDataStream();
					//return EncryptUtils.decryptByAES(resource.getDataStream());
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
