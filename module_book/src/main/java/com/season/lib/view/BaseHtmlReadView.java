package com.season.lib.view;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.season.lib.bean.BookMark;
import com.season.lib.AbsTextSelectHandler;
import com.season.lib.bean.BookDigests;
import com.season.lib.page.span.ColorSpan;
import com.season.lib.TextSelectHandler;
import com.season.lib.page.span.media.ReaderMediaPlayer;
import com.season.lib.dbase.DBConfig;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.page.ClickSpanHandler;
import com.season.lib.page.PageManager;
import com.season.lib.page.SettingParam;
import com.season.lib.page.span.ClickActionSpan;
import com.season.lib.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public abstract class BaseHtmlReadView extends BaseReadView implements ReaderMediaPlayer.PlayerListener, PageManager.PageManagerCallback, ClickSpanHandler {
	protected PageManager mPageManager;
	private int mRequestCharIndex;
	private Integer mChapterSize;
	private int mOrientation;
	private boolean isKeyguard;
	private TextSelectHandler mTextSelectHandler;
	private Bitmap mCacheBitmap;
	private Canvas mCacheBitmapCanvas;
	private int mRequestDrawResult;
	private Integer mSelectTextsChapterIndex;
	
	public BaseHtmlReadView(Context context, BookInfo book, IReadCallback readCallback) {
		super(context, book, readCallback);
        mTextSelectHandler = new TextSelectHandler(0, 0);

		ReaderMediaPlayer.init(getDataProvider());
		mPageManager = new PageManager(getContext(),this, isLayoutAll());
		setDrawingCacheEnabled(false);
		mCurrentPageIndex = INDEX_INITIAL_CONTENT;
		mRequestPageIndex = REQUEST_INDEX_INITIAL_CONTENT;
		mCurrentChapterIndex = INDEX_INITIAL_CONTENT;
		mRequestChapterIndex = INDEX_INITIAL_CONTENT;
		mRequestCharIndex = -1;
		ReaderMediaPlayer.getInstance().addPlayerListener(this);
	}


	protected void initView(int chapterSize,int requestChapterIndex,int requestCharIndex){
		LogUtil.e(TAG, "initView");
		mChapterSize = chapterSize;
        requestCharIndex = requestCharIndex < 0 ? 0:requestCharIndex;
        requestChapterIndex = requestChapterIndex < 0 ? 0:requestChapterIndex;
        requestChapterIndex =  requestChapterIndex >= chapterSize ? chapterSize -1:requestChapterIndex;
		mCurrentChapterIndex = requestChapterIndex;
		mRequestChapterIndex = requestChapterIndex;
		mRequestCharIndex = requestCharIndex;
		mCurrentPageIndex = INDEX_INITIAL_CONTENT;
		mRequestPageIndex = REQUEST_INDEX_INITIAL_CONTENT;
		setUnInit();
		requestLayout();
	}

    @Override
    protected boolean isPageMarked(int chapterIndex, int pageIndex){
        int pageStart = getPageStartIndex(chapterIndex, pageIndex);
        int pageEnd = mPageManager.findPageLastIndex(chapterIndex, pageIndex);
        pageEnd = pageEnd < pageStart? pageStart: pageEnd;
        return mReadCallback.hasShowBookMark(chapterIndex, pageStart, pageEnd);
    }

    private void setUnInit(){
        if (mPageManager != null){
            mPageManager.setUnInit();
        }
		mRequestDrawResult = PageManager.RESULT_UN_INIT;
	}
	
	@Override
	public void release() {
        super.release();
		ReaderMediaPlayer player = ReaderMediaPlayer.getInstance();
		if(player != null){
			player.release();
		}
        if (mCacheBitmap != null){
            if (!mCacheBitmap.isRecycled()){
                mCacheBitmap.recycle();
                mCacheBitmap = null;
            }
        }
        if(mPageManager != null){
        	mPageManager.release();
        	mPageManager = null;
        }
	}

	@Override
	protected void onLoadStyleSetting(boolean isReLayout) {
		super.onLoadStyleSetting(isReLayout);
		if(mPageManager == null){
			return;
		}
		if(isReLayout){
			int requestCharIndex = mPageManager.findPageFirstIndex(mCurrentChapterIndex,mCurrentPageIndex);
			if(requestCharIndex < 0){
				if(mRequestCharIndex == -1){
					mRequestCharIndex = 0;
				}
			}else{
				mRequestCharIndex = requestCharIndex;
			}
			setUnInit();
			mCurrentPageIndex = INDEX_INITIAL_CONTENT;
			mRequestPageIndex = REQUEST_INDEX_INITIAL_CONTENT;
			requestLayout();
		}else{
			mPageManager.invalidateCachePage();
		}
	}


	@Override
	public boolean onActivityDispatchTouchEvent(MotionEvent ev) {
		if (!mPageManager.isFirstDraw()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onActivityDispatchKeyEvent(KeyEvent event) {
		if (!mPageManager.isFirstDraw()) {
			return false;
		}
		if(mTextSelectHandler != null && mTextSelectHandler.isSelect()){
			mTextSelectHandler.setSelect(false);
			return true;
		}
		return false;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
        if (mPageManager.isInit()){
            return;
        }
        if (right - left < 10){
            return;
        }
		KeyguardManager mKeyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
	    if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
	    	isKeyguard = true;
	        return;
	    }
		boolean isChanged = changed;
		if(isKeyguard){
		    isKeyguard = false;
			isChanged = false;
		}
		int orientation = ((Activity) getContext()).getRequestedOrientation();
		if(mOrientation != orientation){
			isChanged = false;
		}
		mOrientation = orientation;
		if(isChanged){
			setUnInit();
		}


		createTextSelectHandler();
		createPageManager();
	}

	private void createPageManager(){
		if(mChapterSize != null && !mPageManager.isInit()){
			Rect fullPageRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
			SettingParam settingParam = new SettingParam(mBook.id, mTextPaint
					,newPageContenRect() ,fullPageRect
					,mReadSetting.getLineSpaceSize(),mReadSetting.getParagraphSpaceSize(), mReadSetting.getIndentSizeSize()
					,this);
			mPageManager.init(settingParam, mChapterSize, mCurrentChapterIndex);
			invalidate();
		}
	}

	private void createTextSelectHandler(){
		if(mTextSelectHandler == null || mTextSelectHandler.isChangeSize(getMeasuredWidth(),getMeasuredHeight())){
			if(mTextSelectHandler != null){
				mTextSelectHandler.releaseSpan();
			}
			mSelectTextsChapterIndex = null;
			mTextSelectHandler = new TextSelectHandler(getMeasuredWidth(),getMeasuredHeight());
			mTextSelectHandler.setSelectorLocationListener(mReadCallback.getSelecter());
			mTextSelectHandler.setViewInformer(new AbsTextSelectHandler.IViewInformer(){
				@Override
				public int getCurrentPage() {
					return mCurrentPageIndex;
				}

				@Override
				public String getData(int start, int end) {
					return mPageManager.subSequence(mCurrentChapterIndex, start, end);
				}
				
				@Override
				public void onInvalidate() {
					mPageManager.invalidateCachePage();
				}

				@Override
				public int findIndexByLocation(int pageIndex, int x, int y) {
					if(mPageManager == null){
						return -1;
					}
					return mPageManager.findIndexByLocation(mCurrentChapterIndex,pageIndex, x, y, false);
				}

				@Override
				public Rect findRectByPosition(int pageIndex, int position) {
					if(mPageManager == null){
						return null;
					}
					return mPageManager.findRectByPosition(mCurrentChapterIndex,pageIndex, position);
				}

				@Override
				public void setBookDigestsSpan(ColorSpan span, int start, int end) {
					mPageManager.setSpan(mCurrentChapterIndex, span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				@Override
				public void deleteBookDigestsSpan(ColorSpan span) {
					mPageManager.removeSpan(mCurrentChapterIndex,span);
				}

				@Override
				public void stopAinm() {
					BaseHtmlReadView.this.stopAnim();
				}

                @Override
                public BookDigests newBookDigest(BookDigests digest) {
                    digest.setPosition4Txt(-1);
                    return digest;
                }
            });
		}
		if(mChapterSize != null){
			updateSelectTexts(mCurrentChapterIndex);
		}
        Rect displayFrame = new Rect();
        getWindowVisibleDisplayFrame(displayFrame);
        //mTextSelectHandler.setRectOffset(0, -displayFrame.top);
        mTextSelectHandler.setRectOffset(0, 0);
	}

	@Override
	public void gotoChapter(Catalog catalog, boolean isStartAnim) {
		gotoPage(getChapterIndex(catalog), 0, isStartAnim);
	}
	
	@Override
	public void gotoPage(int requestProgress, boolean isStartAnim) {
		if(mPageManager.isLayoutAll()){
			int[] locals = mPageManager.findPageIndexByTotal(requestProgress);
			if(locals != null){
				gotoPage(locals[0], locals[1], isStartAnim);
			}
		}else{
			gotoPage(mCurrentChapterIndex, requestProgress, isStartAnim);
		}
	}

	@Override
	public void gotoChar(int requestChapterIndex, int requestCharIndex,boolean isStartAnim) {
		int requestPage = mPageManager.findPageIndex(requestChapterIndex, requestCharIndex);
		if(requestPage >= 0){
			gotoPage(requestChapterIndex, requestPage, isStartAnim);
		}else{
			mRequestCharIndex = requestCharIndex;
			gotoPage(requestChapterIndex,INDEX_INITIAL_CONTENT, isStartAnim);
		}
	}



	@Override
	public BookMark newUserBookmark() {
		if(mChapterSize != null && mBook != null && mCurrentChapterIndex >= 0 && mCurrentChapterIndex <= mChapterSize - 1){
			BookMark bookMark = new BookMark();
			bookMark.setAuthor(mBook.author);
			bookMark.setContentID(mBook.id);
			bookMark.setContentName(mBook.title);
			bookMark.setSoftDelete(DBConfig.BOOKMARK_STATUS_SOFT_DELETE_NO);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			bookMark.setCreateTime(sdf.format(date));

			int chapterIndex = mCurrentChapterIndex;
			int start = getCurPageStartIndex();
			int end = start + 30;
			bookMark.setBookmarkType(DBConfig.BOOKMARK_TYPE_USER);
			bookMark.setChapterID(getCurChapterIndex());
			bookMark.setChapterName(getChapterName(getCurChapterIndex()));
			bookMark.setPosition(start);
			String subString = mPageManager.subSequence(chapterIndex, start, end);
			bookMark.setBookmarkName(subString);

			return bookMark;
		}
        return null;
	}

	@Override
	public int getMaxReadProgress() {
		if(mPageManager.isLayoutAll()){
			return mPageManager.getTotalPageSize();
		}else{
			return mPageManager.getChapterPageSize(mCurrentChapterIndex);
		}
	}

	@Override
	public int getCurReadProgress() {
		if(mPageManager.isLayoutAll()){
			return mPageManager.getTotalPageIndex(mCurrentChapterIndex, mCurrentPageIndex);
		}else{
			return mCurrentPageIndex;
		}
	}

	@Override
	public int getLayoutChapterProgress() {
		return mPageManager.getLayoutChapterProgress();
	}

	@Override
	public int getLayoutChapterMax() {
		return mPageManager.getLayoutChapterMax();
	}

	@Override
	public Catalog getCurrentCatalog() {
		return getCatalogByIndex(mCurrentChapterIndex);
	}

	@Override
	public int getCurChapterIndex() {
		return mCurrentChapterIndex;
	}

    protected int getCurPageStartIndex(){
        return getPageStartIndex(mCurrentChapterIndex, mCurrentPageIndex);
    }

	/**
	 * 获取某一个页面的开始index
	 * @param chapterIndex
	 * @param pageIndex
	 * @return
	 */
	protected int getPageStartIndex(int chapterIndex, int pageIndex) {
		int requestPageIndex = mPageManager.findPageFirstIndex(chapterIndex, pageIndex);
		if(requestPageIndex >= 0){
			return requestPageIndex;
		}
		return 0;
	}

	@Override
	public boolean handlerSelectTouchEvent(MotionEvent event,AbsTextSelectHandler.ITouchEventDispatcher touchEventDispatcher) {
		if(mPageManager == null || mCurrentPageIndex == INDEX_INITIAL_CONTENT || mRequestDrawResult != PageManager.RESULT_SUCCESS){
			return false;
		}
		if(mTextSelectHandler != null && mTextSelectHandler.handlerTouch(event,touchEventDispatcher)){
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchClickEvent(MotionEvent event) {
		if(!isAnimStop()){
			stopAnim();
		}
		final Rect displayFrame = new Rect();
		getWindowVisibleDisplayFrame(displayFrame);
		int x = (int)event.getX();
		int y = (int)event.getY() - displayFrame.top;
		if(mPageManager != null 
				&& mPageManager.dispatchClick(this,x,y,mCurrentChapterIndex, mCurrentPageIndex)){
			return true;
		}
		return false;
	}

	@Override
	public boolean onClickSpan(ClickActionSpan clickableSpan, RectF localRect,
							   int x, int y) {
		return mReadCallback.onSpanClicked(clickableSpan, localRect, x, y);
	}

	@Override
	public boolean checkDigestSpan(int i) {
		return mTextSelectHandler.postLongClick(i);
	}

	@Override
	protected boolean interceptGotoPage(int chapterIndex, int pageIndex) {
		return mReadCallback.checkNeedBuy(chapterIndex, false);
	}

	@Override
	public void drawPage(Canvas canvas, int requestPage) {
		handleRequestCharIndex();
		super.drawPage(canvas, requestPage);
	}

	@Override
	protected boolean onDrawPage(Canvas canvas, boolean isCurrentPage,int chapterIndex, int pageIndex) {
		if(mTextSelectHandler != null && mTextSelectHandler.isSelect()){
			if(mCacheBitmap == null){
				mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.RGB_565);
				mCacheBitmapCanvas = new Canvas(mCacheBitmap);
			}
			mCacheBitmapCanvas.drawColor(0, Mode.CLEAR);
			mTextSelectHandler.handlerDrawPre();
			mPageManager.requestDrawPage(mCacheBitmapCanvas, chapterIndex,pageIndex,mRequestChapterIndex, -mRequestPageIndex - 1);
			canvas.drawBitmap(mCacheBitmap, 0, 0, null);
			mTextSelectHandler.handlerDrawPost(canvas,mCacheBitmap);
		}else{
			int result = PageManager.RESULT_UN_INIT;
			mCacheBitmap = null;
			mCacheBitmapCanvas = null;
			result = mPageManager.requestDrawPage(canvas, chapterIndex,pageIndex,mRequestChapterIndex, -mRequestPageIndex - 1);
			if(mRequestDrawResult != PageManager.RESULT_SUCCESS && result == PageManager.RESULT_SUCCESS && mTextSelectHandler != null){
				mTextSelectHandler.reLoadView();
			}
			mRequestDrawResult = result;
		}
		return true;
	}
	
	@Override
	protected void onSetPageProgress(int chapterIndex, int pageIndex) {
        if (mPageManager == null){//该方法为延迟调用，可能出现资源已回收导致的空指针异常
            return;
        }
		if(mPageManager.isLayoutAll()){
			int totalPageSize = mPageManager.getTotalPageSize();
			if(totalPageSize > 0){
				mReadCallback.onPageChange(mPageManager.getTotalPageIndex(chapterIndex,pageIndex),totalPageSize);
			}
		}else{
			int totalPageSize = mPageManager.getChapterPageSize(chapterIndex);
			if(totalPageSize > 0){
				mReadCallback.onPageChange(pageIndex,totalPageSize);
			}
		}
	}

	@Override
	protected void setPageProgress(int chapterIndex,int pageIndex){
		super.setPageProgress(chapterIndex, pageIndex);
	}
	
	private void updateSelectTexts(int requestChapterIndex){
		if(mTextSelectHandler == null){
			return;
		}
		if(mSelectTextsChapterIndex == null || mSelectTextsChapterIndex != requestChapterIndex){
			mTextSelectHandler.updateSelectTexts(mBook.id, mRequestChapterIndex, mBook.title
					, getChapterName(mRequestChapterIndex), 0, mBook.author);
			mTextSelectHandler.reLoadView();
			mSelectTextsChapterIndex = requestChapterIndex;
		}
	}

	@Override
	public void onStopAnim(boolean isCancel) {
		super.onStopAnim(isCancel);
		if(!isCancel){
			updateSelectTexts(mCurrentChapterIndex);
		}
	}
	
	@Override
	protected void onGotoPage(int chapterIndex,int pageIndex,boolean isStartAnim) {
		if(!isStartAnim){
			updateSelectTexts(chapterIndex);
		}
		if(!mPageManager.isLayoutAll()){
			if(mCurrentChapterIndex != chapterIndex){
				int totalPageSize = mPageManager.getChapterPageSize(chapterIndex);
				if(totalPageSize > 0){
					mReadCallback.onLayoutProgressChange(totalPageSize,totalPageSize);
				}else{
					mReadCallback.onLayoutProgressChange(0, 1);
				}
			}
		}
	}

	@Override
	protected int[] requestNextPage(int chapterIndex, int pageIndex) {
		return mPageManager.requestNextPage(chapterIndex, pageIndex);
	}

	@Override
	protected int[] requestPrePage(int chapterIndex, int pageIndex) {
		return mPageManager.requestPretPage(chapterIndex, pageIndex);
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
	protected Integer getChapterSize() {
		return mChapterSize;
	}

	@Override
	public void onPlayStateChange(int state, String voiceSrc) {
		if(state == ReaderMediaPlayer.STATE_COMPLETION){
			String newPlaySrc = null;
			if(!TextUtils.isEmpty(voiceSrc)){
				try {
					int start = voiceSrc.lastIndexOf("-") + 1;
					int end = voiceSrc.lastIndexOf(".");
					String indexStr = voiceSrc.substring(start, end);
					if(TextUtils.isDigitsOnly(indexStr)){
						int voiceIndex = Integer.valueOf(indexStr);
						if(voiceIndex >= 0){
							newPlaySrc = voiceSrc.substring(0, start) 
									+ (voiceIndex + 1)
									+ voiceSrc.substring(end, voiceSrc.length());
							if(!getDataProvider().hasData(newPlaySrc)){
								newPlaySrc = null;
							}
						}
					}
				} catch (Exception e) {}
			}
			if(newPlaySrc != null){
				ReaderMediaPlayer.getInstance().startVioce(newPlaySrc);
			}else{
				ReaderMediaPlayer.getInstance().stop();
			}
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mPageManager != null){
					mPageManager.invalidateCachePage();
				}
			}
		});
	}

	@Override
	public void onProgressChange(long currentPosition, long maxPosition,
			String voiceSrc) {
	}
	

	@Override
	public void invalidateView(Rect dirty) {
		invalidate();
	}

	@Override
	public void onLayoutPageFinish(int chapterIndex, int pageIndex,int curChar, int maxChar) {
		if(mCurrentChapterIndex == chapterIndex){
			handleRequestCharIndex();
		}
		if(!mPageManager.isLayoutAll() && mRequestChapterIndex == chapterIndex){
			mReadCallback.onLayoutProgressChange(curChar, maxChar);
		}
	}

	private void handleRequestCharIndex(){
		if(mRequestCharIndex != -1){
			int newPageIndex = mPageManager.findPageIndex(mCurrentChapterIndex,mRequestCharIndex);
			if(newPageIndex >= 0){
                if (mRequestCharIndex == 0){
                    newPageIndex = 0;//修正 《爸爸去哪儿》等类似书籍 由于空白字符引起的起始位置非0错误
                }
                interceptGotoPage(mCurrentChapterIndex, newPageIndex);
				if(mCurrentPageIndex == INDEX_INITIAL_CONTENT){
					mCurrentPageIndex = newPageIndex;
				}
				if(mRequestPageIndex == REQUEST_INDEX_INITIAL_CONTENT){
					mRequestPageIndex = -(newPageIndex + 1);
				}
				mRequestCharIndex = -1;
				mPageManager.invalidateCachePage();
			}
		}
	}

	/**
	 * 获取章节列表
	 */
	abstract ArrayList<Catalog> getChapterList();

	@Override
	public void onLayoutChapterFinish(int chapterIndex,int progress,int max) {
		if(progress == 0){
			ArrayList<Catalog> catalogs = getChapterList();
			for (Catalog catalog : catalogs) {
				catalog.setPageIndex(null);
			}
			mReadCallback.onChapterChange(catalogs);
		}else if(progress == max){
			ArrayList<Catalog> catalogs = getChapterList();
			for (Catalog catalog : catalogs) {
				int index = mPageManager.getTotalPageIndex(getChapterIndex(catalog), 0);
				if(index >= 0){
					catalog.setPageIndex(++index);
				}
			}
			mReadCallback.onChapterChange(catalogs);
		}
		if(mPageManager.isLayoutAll()){
			mReadCallback.onLayoutProgressChange(progress, max);
			if(progress == max){
				mPageManager.invalidateCachePage();
			}
		}else{
			if(chapterIndex == mRequestChapterIndex){
				mReadCallback.onLayoutProgressChange(progress, progress);
				mPageManager.invalidateCachePage();
			}
		} 
	}

	@Override
	public void drawWaitingContent(Canvas canvas, int chapterIndex,boolean isFirstDraw) {
		drawWaitPage(canvas,isFirstDraw);
	}

	@Override
	public void onPostDrawContent(Canvas canvas, int chapterIndex,int pageIndex, boolean isFullScreen) {
		if(!isFullScreen){
			if(pageIndex == 0){
				drawBookName(canvas,mBook.title);
			}else{
				drawChapterName(canvas,getChapterName(chapterIndex));
			}
			String pageSizeStr = "-/-";
			if(mPageManager.isLayoutAll()){
				int totalPageSize = mPageManager.getTotalPageSize();
				if(totalPageSize > 0){
					pageSizeStr = (mPageManager.getTotalPageIndex(chapterIndex, pageIndex) + 1) + " / " + totalPageSize;
				}
			}else{
				int totalPageSize = mPageManager.getChapterPageSize(chapterIndex);
				if(totalPageSize > 0){
					pageSizeStr = (pageIndex + 1) + " / " + totalPageSize;
				}
			}
			drawReadPercent(canvas, pageSizeStr);
		}
	}

	@Override
	public void onPreDrawContent(Canvas canvas, int chapterIndex,
			int pageIndex, boolean isFullScreen) {
		drawBackground(canvas);
		if(isFullScreen){
			canvas.drawColor(Color.BLACK);
		}
	}

	@Override
	public void saveDataDB(String contentId, String key, String data) {
//		ReaderLayoutDB.getInstance(getInstance()).saveData(contentId, key, data);
	}

	@Override
	public String getDataDB(String contentId, String key) {
		return null;
//		return ReaderLayoutDB.getInstance(getInstance()).getData(contentId, key);
	}

	@Override
	public boolean hasDataDB(String contentId, String key) {
//		return ReaderLayoutDB.getInstance(getInstance()).hasData(contentId, key);
		return false;
	}
	
	@Override
	public boolean handRequestIndex(Canvas canvas, int chapterIndex,int pageIndex, int bindChapterIndex, int bindPageIndex) {
		if(pageIndex == INDEX_INITIAL_CONTENT){
			drawWaitPage(canvas,mPageManager.isFirstDraw());
			return true;
		}
		return false;
	}

	@Override
	public BookInfo getPaserExceptionInfo() {
		return new BookInfo(mBook.id, mBook.title, mCurrentChapterIndex);
	}

    protected boolean isLayoutAll(){
        return true;
    }

    /**
     * 获取内容[简繁体切换]
     * @param chapterIndex
     * @return
     */
    @Override
    public String getChapterInputStream(int chapterIndex) {
        String content = getChapterInputStream_(chapterIndex);
        if (!mReadSetting.isSimplified()){
            try {
             //   return JChineseConvertor.getInstance().s2t(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * 获取内容[简体]
     * @param chapterIndex
     * @return
     */
    protected abstract String getChapterInputStream_(int chapterIndex);

    protected abstract int getChapterIndex(Catalog catalog);
	
	protected abstract Catalog getCatalogByIndex(int chapterIndex);
	
	protected abstract String getChapterName(int chapterIndex);
	
	protected abstract int loadCatalogID(String chapterID);
}
