package com.season.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.book.R;
import com.season.lib.anim.AbsVerGestureAnimController;
import com.season.lib.bean.BookDigests;
import com.season.lib.bean.BookMark;
import com.season.lib.db.BookMarkDB;
import com.season.lib.AbsTextSelectHandler;
import com.season.lib.ReadSetting;
import com.season.example.catalog.CatalogView;
import com.season.example.popwindow.ImgViewerPopWin;
import com.season.example.popwindow.NotePopWin;
import com.season.example.menu.ReaderMenuPopWin;
import com.season.example.support.SelectorControlView;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.page.span.media.IMediaSpan;
import com.season.lib.page.span.NoteSpan;
import com.season.lib.page.span.media.ReaderMediaPlayer;
import com.season.lib.page.span.media.VideoSpan;
import com.season.example.popwindow.VideoWindow;
import com.season.lib.page.span.AsyncDrawableSpan;
import com.season.lib.page.span.ClickActionSpan;
import com.season.lib.page.span.ClickAsyncDrawableSpan;
import com.season.lib.page.span.UrlSpna;
import com.season.lib.util.SimpleAnimationListener;
import com.season.lib.view.BaseReadView;
import com.season.lib.view.ReadView;
import com.season.lib.view.IReaderView;
import com.season.lib.view.PullRefreshLayout;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.BaseContext;
import com.season.lib.RoutePath;
import com.season.lib.file.FileUtils;
import com.season.lib.util.LogUtil;
import com.season.lib.util.NavigationBarUtil;
import com.season.lib.util.StatusBarUtil;
import com.season.lib.util.ToastUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

@Route(path= RoutePath.BOOK)
public class BaseBookActivity extends Activity implements
        IReaderView.IReadCallback, AbsTextSelectHandler.ITouchEventDispatcher,
        PullRefreshLayout.OnPullListener, PullRefreshLayout.OnPullStateListener{

	private BaseBookActivity this_ = this;
    private FrameLayout mReadContainerView;
    private BaseReadView mReadView;
	private CatalogView mCatalogView;
	private RelativeLayout mCatalogLay;
	private BookInfo mBook;
	private ReaderMenuPopWin mReaderMenuPopWin;
	private boolean isInit;
	private ClickDetector mClickDetector;
	private RectF centerRect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BaseContext.init(getApplicationContext());
		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
        new NavigationBarUtil(this).hideNavigationBar();
        StatusBarUtil.setColor(this, 0xff30302E);
       // StatusBarUtil.setTranslucentForCoordinatorLayout(this, 122);

		mBook = new BookInfo();
		centerRect = new RectF();
		centerRect.left = ScreenUtils.getScreenWidth()/4;
		centerRect.right = ScreenUtils.getScreenWidth()*3/4;
		centerRect.top = ScreenUtils.getScreenHeight()/4;
		centerRect.bottom = ScreenUtils.getScreenHeight()*3/4;

		setContentView(R.layout.activity_reader_lay);
        mReadContainerView = findViewById(R.id.read_view);
		mCatalogLay =  findViewById(R.id.content_lay);
		showReaderContentView();
		initClickDetector();
		init();
        initPullView();

        overridePendingTransition(0, 0);
        LogUtil.e("status  onCreated");
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
        mRotateUpAnimation = AnimationUtils.loadAnimation(this_,
                R.anim.rotate_up);
        mRotateDownAnimation = AnimationUtils.loadAnimation(this_,
                R.anim.rotate_down);

        mPullLayout = (PullRefreshLayout) findViewById(R.id.pull_container);
        mPullLayout.setOnActionPullListener(this);
        mPullLayout.setOnPullStateChangeListener(this);

        mProgress = findViewById(android.R.id.progress);
        mActionImage = findViewById(android.R.id.icon);
        mActionText = (TextView) findViewById(R.id.pull_note);
        mTimeText = (TextView) findViewById(R.id.refresh_time);
        mBookMarkSignImage = (ImageView) findViewById(R.id.iv_book_mark_sign);

        mTimeText.setText(R.string.note_not_update);
        mActionText.setText(R.string.note_pull_down);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
        LogUtil.e("status  onDestroy");
		if(mReadView != null){
			mReadView.onDestroy();
		}
	}

	private void initClickDetector(){
		mClickDetector = new ClickDetector(new ClickDetector.OnClickCallBack() {
			@Override
			public boolean onLongClickCallBack(MotionEvent event) {
				return false;
			}
			
			@Override
			public boolean onClickCallBack(MotionEvent ev) {
                LogUtil.e("key>> onClickCallBack "+ "11" );
				float x = ev.getX();
				float y = ev.getY();
				if (mReadView.dispatchClickEvent(ev)) {
                    LogUtil.e("key>> onClickCallBack "+ "22" );
					return true;
				} else if (centerRect.contains(x, y)) {
                    LogUtil.e("key>> onClickCallBack "+ "44" );
					showMenu();
					return true;
				}
				return false;
			}
			
			@Override
			public void dispatchTouchEventCallBack(MotionEvent event) {
                LogUtil.e("key>> onClickCallBack "+ "4444" );
				onTouchEvent(event);
			}
		},false);
	}
	
	private void initMenu() {
		mReaderMenuPopWin = new ReaderMenuPopWin(mReadView, this, mBook,
				new ReaderMenuPopWin.IActionCallback() {
					@Override
					public void onShowReaderCatalog() {
						showReaderCatalogView();
					}

					@Override
					public void onSaveUserBookmark() {
						// TODO Auto-generated method stub
					}

					@Override
					public void onGotoPage(int pageNum) {
						mReadView.gotoPage(pageNum, true);
					}

					@Override
					public void onGotoBuyBook() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onDeleteUserBookmark() {
						// TODO Auto-generated method stub

					}

					@Override
					public boolean isNeedBuy() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public int getPageNums() {
						return mReadView.getMaxReadProgress();
					}

					@Override
					public int getCurPage() {
						return mReadView.getCurReadProgress();
					}

					@Override
					public boolean canAddUserBookmark() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public void gotoPreChapter() {
						mReadView.gotoPreChapter();
					}

					@Override
					public void gotoNextChapter() {
						mReadView.gotoNextChapter();
					}

					@Override
					public boolean hasPreChapter() {
						return mReadView.hasPreChapter();
					}

					@Override
					public boolean hasNextChapter() {
						return mReadView.hasNextChapter();
					}

					@Override
					public int getLayoutChapterProgress() {
						return mReadView.getLayoutChapterProgress();
					}

					@Override
					public int getLayoutChapterMax() {
						return mReadView.getLayoutChapterMax();
					}
				});
	}

	private void initReaderCatalogView() {
		mCatalogView = new CatalogView(this_, new CatalogView.IActionCallBack() {

			@Override
			public void showReaderContentView() {
				this_.showReaderContentView();
			}

			@Override
			public void selectCatalog(Catalog catalog) {
				mReadView.gotoChapter(catalog, true);
			}

			@Override
			public void selectBookmark(BookMark bookMark) {
				mReadView.gotoBookmark(bookMark, true);
			}

			@Override
			public void selectDigest(BookDigests bookDigests) {
				mReadView.gotoChar(bookDigests.getChaptersId(), bookDigests.getPosition(), true);
			}

			@Override
			public Catalog getCurrentCatalog() {
				return mReadView.getCurrentCatalog();
			}

			@Override
			public BookInfo getBookInfo() {
				return mBook;
			}
		});
	}

	private void showMenu() {
		if (!mReaderMenuPopWin.isShowing() && !mCatalogLay.isShown()) {
			mReaderMenuPopWin.showAtLocation();
		}
	}

	private void dismissMenu() {
		if (mReaderMenuPopWin.isShowing()) {
			mReaderMenuPopWin.dismiss();
		}
	}

	protected boolean showReaderContentView() {
		if(mCatalogView != null && mCatalogView.isShown()){
			dismissReaderCatalogView();
			return true;
		}
		return false;
	}

	protected void showReaderCatalogView() {
		if (mCatalogView != null) {
			if (mCatalogView.getParent() == null) {
				mCatalogLay.addView(mCatalogView);
			}
			mCatalogLay.setVisibility(View.VISIBLE);
			mCatalogView.show();
		}
	}
	
	protected void dismissReaderCatalogView() {
		if (mCatalogView != null) {
			mCatalogView.dismiss(new SimpleAnimationListener(){
				@Override
				public void onAnimationEnd(Animation animation) {
					mReadView.setVisibility(View.VISIBLE);
					mCatalogLay.setVisibility(View.GONE);
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		if (showReaderContentView()){
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(mReadView.onActivityDispatchKeyEvent(event)){
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isInit) {
			return false;
		}
		if(mReadView.onActivityDispatchTouchEvent(ev)){
			return false;
		}
		if(mCatalogLay.isShown()){
			return super.dispatchTouchEvent(ev);
		}
        if(mReadView.handlerSelectTouchEvent(ev, this)){
            return false;
        }
		if(mClickDetector.onTouchEvent(ev, false)){
			return false;
		}
		return super.dispatchTouchEvent(ev);
	}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

	private AbsVerGestureAnimController mAbsVerGestureAnimController = new AbsVerGestureAnimController();
    @Override
	public boolean onTouchEvent(MotionEvent ev) {
        if(isPullEnabled() && mAbsVerGestureAnimController.handlerTouch(ev, new  AbsVerGestureAnimController.IVertialTouchEventDispatcher (){
			@Override
			public void verticalTouchEventCallBack(MotionEvent ev) {
				mPullLayout.dispatchTouchEvent(ev);
			}

			@Override
			public void unVerticalTouchEventCallBack(MotionEvent ev) {
				onTouchEvent(ev);
			}
		})){
            return false;
        }
		return mReadView.handlerTouchEvent(ev);
	}

    private String getBookFielPath(String fend){
        String pathDir = getCacheDir() + File.separator;
        String path =pathDir + "epub_book."+fend;
        File fileDir = new File(pathDir);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        return path;
    }


    private void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = getResources().openRawResource(R.raw.epub_book);mBook.id = "00001";mBook.path = getBookFielPath(".epub");
					//InputStream is = getResources().openRawResource(R.raw.text_book);	mBook.id = "00002";mBook.path = getBookFielPath(".txt");
					//InputStream is = getResources().openRawResource(R.raw.umd_book);	mBook.id = "00003";mBook.path = getBookFielPath(".umd");

					if(!FileUtils.copyFileToFile(mBook.path, is)){
                        finish();
                        return;
                    }
					BookMarkDB.getInstance().loadBookMarks(mBook.id);
                    // 读章节信息
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
							mReadView = new ReadView(BaseBookActivity.this, mBook, BaseBookActivity.this);
                            mReadContainerView.addView(mReadView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            isInit = true;
                            mReadView.onCreate(null);

							initReaderCatalogView();
                            initMenu();

                            new Thread() {
                                @Override
                                public void run() {
									mReadView.onInitReaderInBackground(0, 0, "");
									//mReadView.onInitReaderInBackground(0, 0, "");
                                }
                            }.start();
                        }
                    });
                } catch (Exception e) {
					e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onChapterChange(ArrayList<Catalog> chapters) {
        if(mCatalogView != null){
            mCatalogView.setCatalogData(chapters);
        }
    }

    @Override
	public void onPageChange(int totalPageIndex,int max) {
		mReaderMenuPopWin.setJumpSeekBarProgress(totalPageIndex, max);
	}

    @Override
    public void onLayoutProgressChange(int progress, int max) {

    }

	@Override
	public AbsTextSelectHandler.ISelectorListener getSelecter(){
    	return new SelectorControlView(mReadView, this);
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
    public void showLoadingDialog(int resId) {

    }

    @Override
    public void hideLoadingDialog() {

    }

    @Override
    public boolean hasShowBookMark(int chapterId, int pageStart, int pageEnd) {
        return BookMarkDB.getInstance().isPageMarked(chapterId, pageStart, pageEnd);
    }

    @Override
    public boolean setFreeStart_Order_Price(int feeStart, boolean isOrdered, String price, String limitPrice) {
        return false;
    }

    @Override
    public void setCebBookId(String cebBookId) {

    }

    @Override
    public void dispatchTouchEventCallBack(MotionEvent ev) {
        dispatchTouchEvent(ev);
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
						(int)localRect.bottom),mReadView);
				return true;
			}else if(clickableSpan instanceof NoteSpan){
				if(mNotePopWin == null){
					mNotePopWin = new NotePopWin(mReadView);
				}
				mNotePopWin.showNote(((NoteSpan) clickableSpan).getNote(),localRect, ReadSetting.getInstance(this).getMinFontSize());
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

	public void closeVideo(){
		if(mVideoWindow != null){
			mVideoWindow.dismiss();
		}
	}

	public boolean dispatchVideoKeyEvent(KeyEvent event){
		return mVideoWindow != null && mVideoWindow.dispatchKeyEvent(event);
	}

	public boolean dispatchVideoTouchEvent(MotionEvent ev){
		return mVideoWindow != null && mVideoWindow.dispatchTouchEvent(ev);
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
            mTimeText.setText(this_.getString(R.string.note_update_at, getCurrentTimeByMDHM()));
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
    @Override
    public void onShow() {

    }

    @Override
    public void onHide() {

    }

    @Override
    public boolean isPullEnabled() {
        return !mReadView.isAnimating() && mReadView.getTextSelectHandler() != null;
    }

}
