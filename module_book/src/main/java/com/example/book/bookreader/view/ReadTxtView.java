package com.example.book.bookreader.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;


import com.example.book.R;
import com.example.book.bookreader.bookmarks.BookMark;
import com.example.book.bookreader.digests.AbsTextSelectHandler;
import com.example.book.bookreader.digests.BookDigests;
import com.example.book.bookreader.digests.BookDigestsSpan;
import com.example.book.bookreader.digests.SelectorControlView;
import com.example.book.bookreader.digests.TextSelectHandler;
import com.example.book.bookreader.model.Book;
import com.example.book.bookreader.model.Constants;
import com.season.lib.dbase.DBConfig;
import com.season.book.bookformats.BookInfo;
import com.season.book.bookformats.Catalog;
import com.season.book.text.IPagePicture;
import com.season.book.text.PagePicture;
import com.season.book.txtumd.TxtUmdBasePlugin;
import com.season.book.txtumd.txt.TxtPlugin;
import com.season.book.txtumd.umd.UMDPlugin;
import com.season.lib.util.LogUtil;
import com.season.lib.util.PreferencesUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;


public class ReadTxtView extends BaseReadView implements TxtUmdBasePlugin.IScreenParam {
	private TxtUmdBasePlugin mPlugin;
	protected IPagePicture mCurrentPage;
	protected IPagePicture mRequestPage;
	private PreferencesUtil mPreferencesUtil;
	
	public ReadTxtView(Context context, Book book, IReadCallback readCallback) {
		super(context,book,readCallback);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mCurrentPageIndex = INDEX_INITIAL_CONTENT;
		mRequestPageIndex = REQUEST_INDEX_INITIAL_CONTENT;
		mCurrentChapterIndex = INDEX_INITIAL_CONTENT;
		mRequestChapterIndex = INDEX_INITIAL_CONTENT; 
		mPreferencesUtil = PreferencesUtil.getInstance(getContext());
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	protected void onGotoPage(int chapterIndex,int pageIndex,boolean isStartAnim) {
        if(mPlugin != null){
            if(chapterIndex != mCurrentChapterIndex){
                mPlugin.readChapterData(chapterIndex, pageIndex);
            }else{
                mPlugin.jumpPage(pageIndex, false);
            }
            if(!isStartAnim){
                updateSelectTexts(chapterIndex);
            }
        }
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		if(mPlugin != null){
			mPlugin.release();
            mPlugin = null;
		}
	}

	@Override
	public void onActivityResume() {
	}

	@Override
	public void onActivityPause() {
	}
	
	@Override
	public ArrayList<Catalog> getChapterList() {
		return translateChapter(mPlugin.getChapterList());
	}

	/**
	 * 目录类型批量转换
	 */
	private ArrayList<Catalog> translateChapter(ArrayList<String> chapter){
		if(chapter == null){
			return null;
		}
		ArrayList<Catalog> res = new ArrayList<Catalog>();
		for (int i = 0; i < chapter.size(); i++) {
			Catalog item = new Catalog(null, i);
			item.setText(chapter.get(i));
			res.add(item);
		}
		return res;
	}
	/**
	 * 目录类型转换
	 */
	private Catalog translaterChapter(String chapter, int position){
		Catalog item = new Catalog(null, position);
		item.setText(chapter);
		return item;
	}
	
	@Override
	public String getJumpProgressStr(int curPage, int pageNums) {
		DecimalFormat df = new DecimalFormat("0.00");
		float percent = curPage * 100.0f / (pageNums * 1.0f);
		if(percent > 100){
			percent = 100;
		}else if(percent < 0){
			percent = 0;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(df.format(percent));
		sb.append("%");
		return sb.toString();
	}

	@Override
	public void gotoPage(int requestProgress, boolean isStartAnim) {
		gotoPage(mCurrentChapterIndex, requestProgress, isStartAnim);
	}

	@Override
	public void gotoChar(int requestChapterIndex, int requestCharIndex,
			boolean isStartAnim) {
		
	}

	@Override
	public void gotoChapter(Catalog catalog, boolean isStartAnim) {
		gotoPage(catalog.getIndex(), 0, isStartAnim);
	}  

	@Override
	public boolean hasPreSet() {
		return false;
	}

	@Override
	public boolean hasNextSet() {
		return false;
	}

	@Override
	public void gotoPreSet() {
	}

	@Override
	public void gotoNextSet() {
	}

	@Override
	public void onActivitySaveInstanceState(Bundle outState) {
	}

	@Override
	public Object onActivityRetainNonConfigurationInstance() {
		return null;
	}

	@Override
	public void onActivityRestoreInstanceState(Bundle savedInstanceState) {
	}

	@Override
	public BookMark newSysBookmark() {
		BookMark bookMark = newBookMark(mBook);
		if(bookMark != null){
            bookMark.max = - mPlugin.getContentLength();
			bookMark.setBookmarkType(DBConfig.BOOKMARK_TYPE_SYSTEM);
			bookMark.setChapterID(mCurrentChapterIndex);
			bookMark.setPosition(getPageStartIndex(mCurrentChapterIndex, mCurrentPageIndex));
		}
		return bookMark; 
	}


	private BookMark newBookMark(Book mBookInfo) {
		BookMark bookMark = null;
		if(mBookInfo != null && mPlugin != null && mCurrentChapterIndex >= 0 && mCurrentChapterIndex < mPlugin.getChapterCount()){
			bookMark = new BookMark();
			bookMark.setAuthor(mBookInfo.getAuthor());
			bookMark.setContentID(mBookInfo.getBookId());
			bookMark.setBookmarkName(mBookInfo.getBookName());
		//	bookMark.setUserID(mPreferencesUtil.getUserId());
			bookMark.setSoftDelete(DBConfig.BOOKMARK_STATUS_SOFT_DELETE_NO);
		}
		return bookMark;
	}
	
	@Override
	public BookMark newUserBookmark() {
        BookMark bookMark = newBookMark(mBook);
        if(bookMark != null){
            bookMark.setBookmarkType(DBConfig.BOOKMARK_TYPE_USER);
            bookMark.setChapterID(mCurrentChapterIndex);
            bookMark.setPosition(getPageStartIndex(mCurrentChapterIndex, mCurrentPageIndex));
            bookMark.setChapterName(mPlugin.getCurrentChapterName());
            bookMark.setBookmarkName(mPlugin.getCurrentPageStringVector().get(0));
        }
        return bookMark;
	}
	
	@Override
	public int getMaxReadProgress() {
		return mPlugin.getContentLength();
	}

	@Override
	public int getCurReadProgress() {
		return mPlugin.mPageStart;
	} 

	@Override
	public int getCurChapterIndex() {
		return (mPlugin == null|| !mPlugin.isInit())? 0: mPlugin.getCurrentChapterIndex(true);
	}

	@Override
	public int getPageStartIndex(int chapterIndex, int pageIndex) {
		return pageIndex;
	}
 
	@Override
	public boolean dispatchClickEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean canAddUserBookmark() {
		return false;
	}
 
	@Override
	public View getContentView() {
		return this;
	}
 

	@Override
	public void dealBuyResult(int chapterId) {
	}

	@Override
	public boolean onLongPress() {
		return false;
	}
 

	@Override
	protected boolean interceptGotoPage(int chapterIndex, int pageIndex) {
		return false;
	}

	private IPagePicture createPagePicture(int chapterIndex, int pageIndex) {
		return new PagePicture(chapterIndex,pageIndex, null);
	}
	
	@Override
	public void onStopAnim(boolean isCancel) { 
		super.onStopAnim(isCancel);
		mPlugin.onAnimEnd(isCancel);
		if (!isCancel) { 
			mCurrentPage = mRequestPage;
            updateSelectTexts(mCurrentChapterIndex);
		} 
		mRequestPage = null;
	}

    private void draw(Canvas canvas,boolean isCurrentPage,int chapterIndex,int pageIndex){

        if (mCurrentPage == null || !mCurrentPage.equals(chapterIndex,pageIndex)) {
            mCurrentPage = createPagePicture(chapterIndex,pageIndex);
            drawPageContent(
                    mCurrentPage.getCanvas(getContentWidth(), getContentHeight()),
                    chapterIndex,pageIndex);
        }
        if (mCurrentPage != null) {
            mCurrentPage.onDraw(canvas);
        }

    }

    Bitmap mCacheBitmap;
	protected boolean onDrawPage(Canvas canvas,boolean isCurrentPage,int chapterIndex,int pageIndex) {
		if(mPlugin == null || !mPlugin.isInit()){
			return false;
		}
        LogUtil.i("onDrawPage  " +chapterIndex + "......" + pageIndex);
		if (isCurrentPage) {
            if(mTextSelectHandler != null && mTextSelectHandler.isSelect()){
                mTextSelectHandler.handlerDrawPre();
                mCacheBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transparent);
                mCurrentPage = null;
                draw(canvas, isCurrentPage, chapterIndex, pageIndex);
                mTextSelectHandler.handlerDrawPost(canvas, mCacheBitmap);
            }else{
                draw(canvas,isCurrentPage,chapterIndex,pageIndex);
                if(mCacheBitmap != null && mTextSelectHandler != null){
                    mCurrentPage = null;
                    mTextSelectHandler.reLoadView();
                }
                mCacheBitmap = null;
            }
		} else{
			if (mRequestPage == null || !mRequestPage.equals(chapterIndex,pageIndex)) {
				mRequestPage = createPagePicture(chapterIndex,pageIndex);
				drawPageContent(
						mRequestPage.getCanvas(getContentWidth(), getContentHeight()),
						chapterIndex,pageIndex);
			}
			if (mRequestPage != null) {
				mRequestPage.onDraw(canvas);
			}
		}
		return true; 
	}

	/**
	 * 绘制所有层
	 */
	private void drawPageContent(Canvas canvas,int chapterIndex, int pageIndex) {
		canvas.save();
		drawBackground(canvas);
		drawChapterName(canvas, mPlugin.getCurrentChapterName());
		drawReadPercent(canvas, getJumpProgressStr(getCurReadProgress(), getMaxReadProgress()));
        drawReadProgress(canvas,getCurReadProgress() , getMaxReadProgress());
		Rect contentRect = newPageContenRect();
		FontMetrics metrics = mTextPaint.getFontMetrics();
		float top = metrics.bottom - metrics.top;
		mPlugin.onDraw(canvas, contentRect.left, contentRect.top + top/2, mTextPaint,
				mTextPaint.getFontSpacing(), mReadSetting.getLineSpaceSize(), contentRect.width());
		canvas.restore();
	}

    @Override
    protected boolean isPageMarked(int chapterIndex, int pageIndex){
        int pageStart = getPageStartIndex(chapterIndex, pageIndex);
        int pageEnd = pageStart + 20;
        if (mPlugin != null){
            int[] startEnd =  mPlugin.getPageStartEndIndex(chapterIndex, pageIndex);
            if (startEnd != null && startEnd.length == 2){
                pageStart = startEnd[0];
                pageEnd = startEnd[1];
            }
        }
        pageEnd = pageEnd < pageStart? pageStart + 20: pageEnd;
        return mReadCallback.hasShowBookMark(chapterIndex, pageStart, pageEnd);
    }
  
	@Override
	protected void onLoadStyleSetting(boolean isReLayout) {
		if(mPlugin == null || !mPlugin.isInit()){
			return;
		}
		mCurrentPage = null;
		mRequestPage = null;
		if(isReLayout){
			//mPlugin.clearCache();
		} 
		mCurrentPage = null;
		mRequestPage = null;
		gotoPage(mRequestChapterIndex, mCurrentPageIndex, false);
	}

	@Override
	protected void onSetPageProgress(int chapterIndex, int pageIndex) {
        if (mPlugin != null){
            mReadCallback.onPageChange(getCurReadProgress(), getMaxReadProgress());
        }
	}

	@Override
	protected int[] requestNextPage(int chapterIndex, int pageIndex) {
		return mPlugin.getNextPageIndex(); 
	}

	@Override
	protected int[] requestPrePage(int chapterIndex, int pageIndex) {
		return mPlugin.getPrePageIndex(); 
	}

	@Override
	protected void onNotPreContent() {
		mReadCallback.onNotPreContent();
	}

	@Override
	protected void onNotNextContent() {
		mReadCallback.onNotNextContent();
	}

    @Override
    public void invalidateView(String text) {
        invalidate();
    }

    @Override
	public int calculateLineSize(String text) {
		int contentWidth = newPageContenRect().width();
		if(contentWidth <= 0){
			contentWidth = getResources().getDisplayMetrics().widthPixels - PADDING_LEFT - PADDING_RIGHT;
		}
		int size = mTextPaint.breakText(text, true, contentWidth, null);
		return size;
	}

	@Override
	public int calculateLineCount() {
		int contentHeight = newPageContenRect().height();
		if(contentHeight <= 0){
			contentHeight = getResources().getDisplayMetrics().heightPixels - mHeadspaceBottom - mHeadspaceTop;
		}
		return Math.round(contentHeight / (mTextPaint.getFontSpacing() + mReadSetting.getLineSpaceSize()));
		//return (int) (mContenRect.height() / (mTextPaint.getFontSpacing() + mReadSetting.getLineSpaceSize()));
	}

	@Override
	public void onChapterFounded(final ArrayList<String> chapter) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mReadCallback.onChapterChange(translateChapter(chapter));
			}
		});
	}

	@Override
	public void search(int direction, String keyWord) {
	}

    @Override
    public String getChapterId(int chaptersId) {
        return chaptersId+"";
    }

    @Override
	public int onInitReaderInBackground(final int fRequestCatalogIndex,final int fRequestPageCharIndex, String secretKey) {
		mRequestChapterIndex = fRequestCatalogIndex;
		mCurrentPageIndex = fRequestPageCharIndex;
        if(mBook.getPath().endsWith("umd")){
            mPlugin = new UMDPlugin(this){
                public Vector<String> getCurrentPageStringVector(){
                    Vector<String> res = super.getCurrentPageStringVector();
                    if (res != null && res.size() > 0){
                        if (!mReadSetting.isSimplified()){
                            Vector<String> tras = new Vector<String>(res.size());
                            try {
                                for (String str : res){
                                   // tras.add(JChineseConvertor.getInstance().s2t(str));
                                }
                                return tras;
                            } catch (Exception e) {
                            }
                        }
                    }
                    return res;
                }
            };
        }else{
            mPlugin = new TxtPlugin(this){
                public Vector<String> getCurrentPageStringVector(){
                    Vector<String> res = super.getCurrentPageStringVector();
                    if (res != null && res.size() > 0){
                        if (!mReadSetting.isSimplified()){
                            Vector<String> tras = new Vector<String>(res.size());
                            try {
                                for (String str : res){
                                   // tras.add(JChineseConvertor.getInstance().s2t(str));
                                }
                                return tras;
                            } catch (Exception e) {
                            }
                        }
                    }
                    return res;
                }
            };
        }
        try {
            mPlugin.openBook(mBook.getPath(), Constants.BOOKS_TEMP);
        } catch (Exception e) {
            e.printStackTrace();
            mPlugin = null;
        }
        LogUtil.e("init" + mPlugin);
		runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mPlugin != null) {
                    requestLayout();
                }
            }
        });
		return mPlugin != null? SUCCESS: ERROR_GET_CONTENT_INFO;
	}

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mTextSelectHandler == null){
            LogUtil.e("onLayout " + (bottom - top));
            createTextSelectHandler();
        }


    }

    @Override
	public boolean onActivityDispatchTouchEvent(MotionEvent ev) {
		return false;
	}

	@Override
	public boolean onActivityDispatchKeyEvent(KeyEvent event) {
        if(mTextSelectHandler != null && mTextSelectHandler.isSelect()){
            mTextSelectHandler.setSelect(false);
            return true;
        }
		return false;
	}

    private void createTextSelectHandler(){
        LogUtil.e("init mTextSelectHandler " + getMeasuredWidth()+", "+getMeasuredHeight());
        if(mTextSelectHandler == null || mTextSelectHandler.isChangeSize(getMeasuredWidth(), getMeasuredHeight())){
            if(mTextSelectHandler != null){
                mTextSelectHandler.releaseSpan();
            }
            LogUtil.e("init mTextSelectHandler " + getMeasuredWidth()+", "+getMeasuredHeight());
            mSelectTextsChapterIndex = null;
            mTextSelectHandler = new TextSelectHandler(getMeasuredWidth(),getMeasuredHeight());
            mTextSelectHandler.setSelectorLocationListener(new SelectorControlView(this,(Activity) getContext()));
            mTextSelectHandler.setViewInformer(new AbsTextSelectHandler.IViewInformer(){
                @Override
                public int getCurrentPage() {
                    return mCurrentPageIndex;
                }

                @Override
                public String getData(int start, int end) {
                    return mPlugin.subSequence(mCurrentChapterIndex, start, end);
                }

                @Override
                public void onInvalidate() {
                	if (mPlugin != null)
                    mPlugin.invalidateCachePage();
                }

                @Override
                public int findIndexByLocation(int pageIndex, int x, int y) {
                    if(mPlugin == null || !mPlugin.isInit()){
                        return -1;
                    }
                    return mPlugin.findIndexByLocation(mCurrentChapterIndex,pageIndex, x, y, false);
                }

                @Override
                public Rect findRectByPosition(int pageIndex, int position) {
                    if(mPlugin == null || !mPlugin.isInit()){
                        return null;
                    }
                    RectF rectF = mPlugin.findRectByPosition(mCurrentChapterIndex,pageIndex, position);
                    if(rectF != null){
                        return new Rect((int)rectF.left, (int)rectF.top, (int)rectF.right, (int)rectF.bottom);
                    }
                    return null;
                }

                @Override
                public void setBookDigestsSpan(BookDigestsSpan span, int start, int end) {
                    LogUtil.i("setBookDigestsSpan >> "+start+", "+end);
                    mCurrentPage = null;
                    mPlugin.setSpan(mCurrentChapterIndex,span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                @Override
                public void deleteBookDigestsSpan(BookDigestsSpan span) {
                    LogUtil.i("deleteBookDigestsSpan >> "+span);
                    mCurrentPage = null;
                    mPlugin.removeSpan(mCurrentChapterIndex,span);
                }

                @Override
                public void stopAinm() {
                    ReadTxtView.this.stopAnim();
                }

                @Override
                public BookDigests newBookDigest(BookDigests digest) {
                    digest.setPosition4Txt(mPlugin.mPageStart);
                    return digest;
                }
            });
        }
        updateSelectTexts(mCurrentChapterIndex);
        Rect displayFrame = new Rect();
        getWindowVisibleDisplayFrame(displayFrame);
        LogUtil.i("getWindowVisibleDisplayFrame  "+ displayFrame.top);
        //mTextSelectHandler.setRectOffset(0, -displayFrame.top);
        mTextSelectHandler.setRectOffset(0, 0);
    }

    private Integer mSelectTextsChapterIndex;
    private void updateSelectTexts(int requestChapterIndex){
        if(mTextSelectHandler == null || !mTextSelectHandler.isInit()){
            return;
        }
        if(mSelectTextsChapterIndex == null || mSelectTextsChapterIndex != requestChapterIndex){
            mTextSelectHandler.updateSelectTexts(mBook.getBookId(), mRequestChapterIndex, mBook.getBookName(), getChapterName(mRequestChapterIndex), 0, mBook.getAuthor());
            mTextSelectHandler.reLoadView();
            mSelectTextsChapterIndex = requestChapterIndex;
        }
    }

    protected String getChapterName(int chapterIndex) {
        Catalog catalog = getCurrentCatalog();
        if(catalog != null && !TextUtils.isEmpty(catalog.getText())){
            return catalog.getText();
        }
        return "";
    }

    @Override
	public void gotoBookmark(BookMark bookmark, boolean isStartAnim) {
		gotoPage(bookmark.getChapterID(), bookmark.getPosition(), true);
	}

	@Override
	public int getLayoutChapterProgress() {
		return 0;
	}

	@Override
	public int getLayoutChapterMax() {
		return 0;
	}

	@Override
	public void onShowContentView() {		
	}

	@Override
	public void onHideContentView() {
		
	}

	@Override
	public BookInfo getBookInfo() {
		BookInfo info = new BookInfo();
		info.author = mBook.getAuthor();
		info.id = mBook.getBookId();
		info.title = mBook.getBookName();
		return info;
	}

	@Override
	public Catalog getCurrentCatalog() {
		if(mPlugin != null){
			return translaterChapter(mPlugin.getCurrentChapterName(),
					mPlugin.getCurrentChapterIndex(false));
		}
		return null;
	}

	@Override
	public boolean handlerSelectTouchEvent(MotionEvent event,
			AbsTextSelectHandler.ITouchEventDispatcher touchEventDispatcher) {
        if(mTextSelectHandler != null && mTextSelectHandler.handlerTouch(event,touchEventDispatcher)){
            return true;
        }
		return false;
	}

	@Override
	protected Integer getChapterSize() {
		return mPlugin.getChapterCount();
	}

    private TextSelectHandler mTextSelectHandler;
	@Override
	public TextSelectHandler getTextSelectHandler() {
		return mTextSelectHandler;
	}
}
