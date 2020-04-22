package com.season.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.season.book.R;
import com.season.book.bean.BookMark;
import com.season.book.db.BookMarkDB;
import com.season.book.event.AbsTextSelectHandler;
import com.season.book.ReadSetting;
import com.season.book.view.NetReadView;
import com.season.example.catalog.CatalogView;
import com.season.example.popwindow.ImgViewerPopWin;
import com.season.example.popwindow.NotePopWin;
import com.season.example.menu.ReaderMenuPopWin;
import com.season.example.support.SelectorControlView;
import com.season.lib.BaseStartPagerActivity;
import com.season.lib.dimen.ScreenUtils;
import com.season.book.page.span.media.IMediaSpan;
import com.season.book.page.span.NoteSpan;
import com.season.book.page.span.media.ReaderMediaPlayer;
import com.season.book.page.span.media.VideoSpan;
import com.season.example.popwindow.VideoWindow;
import com.season.book.page.span.AsyncDrawableSpan;
import com.season.book.page.span.ClickActionSpan;
import com.season.book.page.span.ClickAsyncDrawableSpan;
import com.season.book.page.span.UrlSpna;
import com.season.book.view.IReadCallback;
import com.season.book.view.ReadView;
import com.season.book.view.IReaderView;
import com.season.lib.view.PullRefreshLayout;
import com.season.book.bean.BookInfo;
import com.season.book.bean.Catalog;
import com.season.lib.util.NavigationBarUtil;
import com.season.lib.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BaseBookActivity extends BaseStartPagerActivity implements
		IReadCallback, PullRefreshLayout.OnPullStateListener{

	public static void open(Context context, BookInfo bookInfo){
		Intent intent = new Intent();
		intent.setClass(context, BaseBookActivity.class);
		intent.putExtra("book", bookInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

    private FrameLayout mReadContainerView;
    private IReaderView mReadView;
	private CatalogView mCatalogView;
	private RelativeLayout mCatalogLay;
	private BookInfo mBook;
	private ReaderMenuPopWin mReaderMenuPopWin;
	private RectF centerRect;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reader;
    }

	@Override
	protected boolean enablePager(){
		return true;
	}
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		centerRect = new RectF();
		centerRect.left = ScreenUtils.getScreenWidth()/4;
		centerRect.right = ScreenUtils.getScreenWidth()*3/4;
		centerRect.top = 0;
		centerRect.bottom = ScreenUtils.getScreenHeight() * 5/6;
		catalogWidth = ScreenUtils.getScreenWidth() * 4/5;

        mReadContainerView = findViewById(R.id.read_view);
		mCatalogLay =  findViewById(R.id.content_lay);

        mReadContainerView.setBackgroundColor(ReadSetting.getInstance().getThemeBGColor());

		initClickDetector();
		initPullView();

        if (getIntent().hasExtra("book")){
        	mBook = (BookInfo) getIntent().getSerializableExtra("book");
		}

        if (mBook == null || TextUtils.isEmpty(mBook.id)){
			BookShelfPreLoader.getInstance().getBookLists(new BookShelfPreLoader.ICallback() {
				@Override
				public void onBookLoaded(List<BookInfo> bookLists) {
					int position = ReadSetting.getInstance().getReadPosition();
					if (position >= bookLists.size()){
						position = bookLists.size() - 1;
					}
					if (position < 0){
						position = 0;
					}
					mBook = bookLists.get(position);
					if (isAnimationEnded){
						openBook();
					}
				}
			});
		}else{
        	if (isAnimationEnded){
				openBook();
			}
		}
	}

	@Override
	protected void onPagerEnded() {
    	super.onPagerEnded();
    	if (!isOpened){
			openBook();
		}
	}

	boolean isOpened = false;
	private void openBook(){
		initReadView();
		NavigationBarUtil.hideNavigationBar(BaseBookActivity.this);
		isOpened = true;
		new Thread() {
			@Override
			public void run() {
				int[] index = ReadSetting.getInstance().getBookReadProgress(mBook.id);
				mBook = mReadView.decodeBookFromPlugin(index[0], index[1]);
				mCatalogView.setBookInfo(mBook.title, mBook.author);
			}
		}.start();
	}


	private void initReadView() {
		if (mBook.netIndex > 0){
			mReadView = new NetReadView(BaseBookActivity.this, mBook, BaseBookActivity.this);
		}else{
			File file  = new File(mBook.filePath);
			if (file.isFile() && file.length() > 0){
				mReadView = new ReadView(BaseBookActivity.this, mBook, BaseBookActivity.this);
			}else{
				finish();
				ToastUtil.showToast("文件丢失");
				return;
			}
		}
		mReadContainerView.addView(mReadView.getContentView(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		initReaderCatalogView();
		initMenu();
	}

    private Animation mRotateUpAnimation;
    private Animation mRotateDownAnimation;
    private PullRefreshLayout mPullLayout;
    private TextView mActionText;
    private TextView mTimeText;
    private ImageView mBookMarkSignImage;
    private View mProgress;
    private View mActionImage;
    protected void initPullView() {
        mRotateUpAnimation = AnimationUtils.loadAnimation(this,
                R.anim.rotate_up);
        mRotateDownAnimation = AnimationUtils.loadAnimation(this,
                R.anim.rotate_down);

        mPullLayout = findViewById(R.id.pull_container);
        mPullLayout.setOnPullStateChangeListener(this);

        mProgress = findViewById(android.R.id.progress);
        mActionImage = findViewById(android.R.id.icon);
        mActionText = findViewById(R.id.pull_note);
        mTimeText = findViewById(R.id.refresh_time);
        mBookMarkSignImage = findViewById(R.id.iv_book_mark_sign);

        mTimeText.setText(R.string.note_not_update);
        mActionText.setText(R.string.note_pull_down);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mReadView != null){
			mReadView.release();
		}
	}
	
	private void initMenu() {
		mReaderMenuPopWin = new ReaderMenuPopWin(this, mReadView,
				new ReaderMenuPopWin.IActionCallback() {
					@Override
					public void onShowReaderCatalog() {
						showReaderCatalogView();
					}
					@Override
					public void onTopBackButtonClicked(boolean isLeft) {
						if (isLeft){
							onPagerFinish();
						}else{
							//startActivity(new Intent(BaseBookActivity.this, BookShelfActivity.class));
						}
					}

					@Override
					public void onDismiss(boolean hide) {
						if (hide)
							mCatalogLay.setVisibility(View.GONE);
						NavigationBarUtil.hideNavigationBar(BaseBookActivity.this);
					}
				});
	}

	private void initReaderCatalogView() {
		mCatalogView = new CatalogView(this, mCatalogLay, mReadView);
	}

	private void showMenu() {
    	if (mReaderMenuPopWin == null){
    		return;
		}
		if (mReaderMenuPopWin.getParent() == null) {
			mCatalogLay.addView(mReaderMenuPopWin);
		}
		mCatalogLay.setVisibility(View.VISIBLE);
		NavigationBarUtil.showNavigationBar(BaseBookActivity.this);
		mReaderMenuPopWin.show();
	}

	private int catalogWidth;
	protected void showReaderCatalogView() {
		if (mCatalogView.getParent() == null) {
			mCatalogLay.addView(mCatalogView, new RelativeLayout.LayoutParams(catalogWidth, ViewGroup.LayoutParams.MATCH_PARENT));
		}
		mCatalogLay.setVisibility(View.VISIBLE);
		mCatalogView.show(mBook, mReadView.getCurrentCatalog());

	}

	@Override
	public void onBackPressed() {
		if (false){
			if(mCatalogView != null && mCatalogView.isShown()){
				mCatalogView.dismiss(true);
				return;
			}
			if(mReaderMenuPopWin != null && mReaderMenuPopWin.isShown()){
				mReaderMenuPopWin.dismiss(true, true);
				return;
			}
		}
		super.onBackPressed();
	}

	AbsTextSelectHandler.ITouchEventDispatcher dispatcher;
	private void initClickDetector(){
		dispatcher = new AbsTextSelectHandler.ITouchEventDispatcher() {
			@Override
			public void verticalTouchEventCallBack(MotionEvent ev) {
				mPullLayout.dispatchTouchEvent(ev);
			}

			@Override
			public void unVerticalTouchEventCallBack(MotionEvent ev) {
				mReadView.handlerTouchEvent(ev);
			}

			@Override
			public void onClickCallBack(MotionEvent ev) {
				float x = ev.getX();
				float y = ev.getY();
				if (mReadView.dispatchClickEvent(ev)) {
					//笔记点击处理
				} else if (centerRect.contains(x, y)) {
					//笔记点击处理
					showMenu();
				}else {
					//上一页下一页
					if(x >= ScreenUtils.getScreenWidth()/2){
						mReadView.gotoNextPage();
					}else{
						mReadView.gotoPrePage();
					}
				}
			}
		};
	}

	boolean interrupt = false;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN){
			interrupt = false;
			if(!mCatalogLay.isShown()){
				NavigationBarUtil.hideNavigationBar(this);
			}
		}
		if(!mReadView.isCurrentPageDrawn() || interrupt){
			return false;
		}
		if(mCatalogLay.isShown()){
			if(mCatalogView != null && mCatalogView.isShown()){
				if (ev.getAction() == MotionEvent.ACTION_DOWN){
					if (ev.getX() > catalogWidth){
						interrupt = true;
						mCatalogView.dismiss(true);
						return false;
					}
				}
			}
			return super.dispatchTouchEvent(ev);
		}
		mReadView.handlerSelectTouchEvent(ev, dispatcher);
		return false;
		//return super.dispatchTouchEvent(ev);
	}


    @Override
    public void onChapterChange(ArrayList<Catalog> chapters) {
        if(mCatalogView != null){
            mCatalogView.setCatalogData(chapters);
        }
    }

    @Override
	public void onPageChange(int totalPageIndex,int max) {
		//mReaderMenuPopWin.setJumpSeekBarProgress(totalPageIndex, max);
	}

    @Override
    public void onLayoutProgressChange(int progress, int max) {

    }

	@Override
	public AbsTextSelectHandler.ISelectorListener getSelecter(){
    	return new SelectorControlView(mReadView.getContentView(), this);
	}

	@Override
	public void onNotPreContent() {
		ToastUtil.showToast("已经是第一页了");
	}
	
	@Override
	public void onNotNextContent() {
		ToastUtil.showToast("已经是最后一页了");
	}

    @Override
    public boolean checkNeedBuy(int catalogIndex, boolean isNeedBuy) {
        return false;
    }

    @Override
    public boolean hasShowBookMark(int chapterId, int pageStart, int pageEnd) {
        return BookMarkDB.getInstance().isPageMarked(chapterId, pageStart, pageEnd);
    }

    @Override
    public boolean setFreeStart_Order_Price(int feeStart, boolean isOrdered, String price, String limitPrice) {
        return false;
    }

	private ImgViewerPopWin mImgViewerPopWin;
	private NotePopWin mNotePopWin;
	private VideoWindow mVideoWindow;
    @Override
	public boolean onSpanClicked(ClickActionSpan clickableSpan, RectF localRect,
							  int x, int y) {
		if(!clickableSpan.isClickable()){
			return false;
		}
		try {
			if(clickableSpan instanceof ClickAsyncDrawableSpan){
				if(mImgViewerPopWin == null){
					mImgViewerPopWin = new ImgViewerPopWin(this);
				}
				mImgViewerPopWin.showImgViewer(((AsyncDrawableSpan)clickableSpan).getDrawable(), new Rect(
						(int)localRect.left,
						(int)localRect.top,
						(int)localRect.right,
						(int)localRect.bottom),mReadView.getContentView());
				return true;
			}else if(clickableSpan instanceof NoteSpan){
				if(mNotePopWin == null){
					mNotePopWin = new NotePopWin(mReadView.getContentView());
				}
				mNotePopWin.showNote(((NoteSpan) clickableSpan).getNote(),localRect, ReadSetting.getInstance().getMinFontSize());
				return true;
			}else if(clickableSpan instanceof UrlSpna){
				String urlStr = ((UrlSpna) clickableSpan).getUrl();
				if(!TextUtils.isEmpty(urlStr)){
					Uri uri = Uri.parse(urlStr);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
					startActivity(intent);
					return true;
				}
			}else if(clickableSpan instanceof IMediaSpan){
				if(clickableSpan instanceof VideoSpan){
					if(mVideoWindow == null && ReaderMediaPlayer.getInstance() != null){
						mVideoWindow = new VideoWindow(this);
					}
					if(mVideoWindow != null){
						mVideoWindow.handlerPlayVideo((VideoSpan) clickableSpan, localRect);
					}
				}
				return true;
			}
		} catch (Exception e) {}
		return false;
	}


    /**
     * 当前页面是否书签
     */
    private boolean isCurrentBookMarked(){
		return mReadView.isCurrentPageMarked();
    }

    private boolean mInLoading = false;
    @Override
    public void onPullOut() {
        if (!mInLoading) {
            if(isCurrentBookMarked()){
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_unsign);
                mActionText.setText(R.string.book_label_del_pull_refresh);
            }else{
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_sign);
                mActionText.setText(R.string.book_label_add_pull_refresh);
            }
            mActionImage.clearAnimation();
            mActionImage.startAnimation(mRotateUpAnimation);
        }

    }

    @Override
    public void onPullIn() {
        if (!mInLoading) {
            if(isCurrentBookMarked()){
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_sign);
                mActionText.setText(R.string.book_label_del_pull);
            }else{
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_unsign);
                mActionText.setText(R.string.book_label_add_pull);
            }

            mActionImage.clearAnimation();
            mActionImage.startAnimation(mRotateDownAnimation);
        }
    }

	@Override
	public void onPullUp() {
		showMenu();
	}

	@Override
    public void onSnapToTop() {
        if (!mInLoading) {
            mInLoading = true;
            mPullLayout.setEnableStopInActionView(false);
            mActionImage.clearAnimation();
            mActionImage.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            if(isCurrentBookMarked()){
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_unsign);
                mActionText.setText(R.string.book_label_del_pull_loading);
            }else{
                mBookMarkSignImage.setImageResource(R.drawable.book_mark_sign);
                mActionText.setText(R.string.book_label_add_pull_loading);
            }
            if(isCurrentBookMarked()){
				delBookLabel();
			}else{
				addBookLabel();
			}
            mTimeText.setText(getString(R.string.note_update_at, getCurrentTimeByMDHM()));
        }

    }

    public static String getCurrentTimeByMDHM() {
        SimpleDateFormat var0 = new SimpleDateFormat("MM-dd HH:mm");
        Calendar var1 = Calendar.getInstance();
        return var0.format(var1.getTime());
    }

    /**
     * 添加书签
     */
    private void addBookLabel(){
        BookMark userMark = mReadView.newUserBookmark();
        if(BookMarkDB.getInstance().addBookMark(userMark)){
            ToastUtil.showToast(R.string.book_label_add_success);
        }
        dataLoaded();
    }

    /**
     * 删除用户书签
     */
    private void delBookLabel(){
        BookMark userMark = mReadView.newUserBookmark();
        if(BookMarkDB.getInstance().deleteBookMark(userMark)){
            ToastUtil.showToast(R.string.book_label_del_success);
        }
        dataLoaded();
    }
    /**刷新加载结束*/
    private void dataLoaded() {
    	new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mInLoading) {
					mInLoading = false;
					mPullLayout.setEnableStopInActionView(false);
					mPullLayout.hideActionView();
					mActionImage.setVisibility(View.VISIBLE);
					mProgress.setVisibility(View.GONE);

					if (mPullLayout.isPullOut()) {
						mActionText.setText(R.string.note_pull_refresh);
						mActionImage.clearAnimation();
						mActionImage.startAnimation(mRotateUpAnimation);
					} else {
						mActionText.setText(R.string.note_pull_down);
					}
				}
				mReadView.getContentView().invalidate();
			}
		}, 300);
    }

}
