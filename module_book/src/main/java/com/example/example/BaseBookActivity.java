package com.example.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
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

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.book.R;
import com.example.lib.bean.BookMark;
import com.example.lib.db.BookMarkDB;
import com.example.lib.event.AbsTextSelectHandler;
import com.example.lib.ReadSetting;
import com.example.example.catalog.CatalogView;
import com.example.example.popwindow.ImgViewerPopWin;
import com.example.example.popwindow.NotePopWin;
import com.example.example.menu.ReaderMenuPopWin;
import com.example.example.support.SelectorControlView;
import com.example.lib.dimen.ScreenUtils;
import com.example.lib.page.span.media.IMediaSpan;
import com.example.lib.page.span.NoteSpan;
import com.example.lib.page.span.media.ReaderMediaPlayer;
import com.example.lib.page.span.media.VideoSpan;
import com.example.example.popwindow.VideoWindow;
import com.example.lib.page.span.AsyncDrawableSpan;
import com.example.lib.page.span.ClickActionSpan;
import com.example.lib.page.span.ClickAsyncDrawableSpan;
import com.example.lib.page.span.UrlSpna;
import com.example.lib.view.IReadCallback;
import com.example.lib.view.ReadView;
import com.example.lib.view.IReaderView;
import com.example.lib.view.PullRefreshLayout;
import com.example.lib.bean.BookInfo;
import com.example.lib.bean.Catalog;
import com.example.lib.BaseContext;
import com.example.lib.RoutePath;
import com.example.lib.file.FileUtils;
import com.example.lib.util.NavigationBarUtil;
import com.example.lib.util.ToastUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

@Route(path= RoutePath.BOOK)
public class BaseBookActivity extends Activity implements
		IReadCallback, PullRefreshLayout.OnPullStateListener{

    private FrameLayout mReadContainerView;
    private IReaderView mReadView;
	private CatalogView mCatalogView;
	private RelativeLayout mCatalogLay;
	private BookInfo mBook;
	private ReaderMenuPopWin mReaderMenuPopWin;
	private boolean isInit;
	private RectF centerRect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BaseContext.init(getApplicationContext());
        NavigationBarUtil.hideNavigationBar(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}

		//StatusBarUtil.setTranslucent(this, 125);
        //StatusBarUtil.setTranslucent(this);
        // StatusBarUtil.setColor(this, 0xff30302E);
        // StatusBarUtil.setTranslucentForCoordinatorLayout(this, 122);

		mBook = new BookInfo();
		centerRect = new RectF();
		centerRect.left = ScreenUtils.getScreenWidth()/4;
		centerRect.right = ScreenUtils.getScreenWidth()*3/4;
		centerRect.top = 0;
		centerRect.bottom = ScreenUtils.getScreenHeight() * 5/6;

		setContentView(R.layout.activity_reader);
        mReadContainerView = findViewById(R.id.read_view);
		mCatalogLay =  findViewById(R.id.content_lay);

        mReadContainerView.setBackgroundColor(ReadSetting.getInstance(this).getThemeBGColor());

		initClickDetector();
		initReadView();
        initPullView();

        overridePendingTransition(0, 0);
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
					public void onBackPressed() {
						finish();
					}
					@Override
					public void onDismiss() {
						mCatalogLay.setVisibility(View.GONE);
					}
				});
	}

	private void initReaderCatalogView() {
		mCatalogView = new CatalogView(this, mReadView,  new CatalogView.IActionCallBack() {
			@Override
			public void onDismiss() {
				mCatalogLay.setVisibility(View.GONE);
			}
		});
	}

	private void showMenu() {
    	if (mReaderMenuPopWin == null){
    		return;
		}
		if (mReaderMenuPopWin.getParent() == null) {
			mCatalogLay.addView(mReaderMenuPopWin);
		}
		mCatalogLay.setVisibility(View.VISIBLE);
		mReaderMenuPopWin.show();
	}

	protected void showReaderCatalogView() {
		if (mCatalogView.getParent() == null) {
			mCatalogLay.addView(mCatalogView);
		}
		mCatalogLay.setVisibility(View.VISIBLE);
		mCatalogView.show(mBook, mReadView.getCurrentCatalog());
	}

	@Override
	public void onBackPressed() {
		if(mCatalogView != null && mCatalogView.isShown()){
			mCatalogView.dismiss(true);
			return;
		}
		if(mReaderMenuPopWin != null && mReaderMenuPopWin.isShown()){
			mReaderMenuPopWin.dismiss(true, true);
			return;
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

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isInit) {
			return false;
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN){
			NavigationBarUtil.hideNavigationBar(this);
		}
		if(!mReadView.isCurrentPageDrawn()){
			return false;
		}
		if(mCatalogLay.isShown()){
			return super.dispatchTouchEvent(ev);
		}
		mReadView.handlerSelectTouchEvent(ev, dispatcher);
		return false;
		//return super.dispatchTouchEvent(ev);
	}

    private String getBookFielPath(String fend){
        String pathDir = getCacheDir() + File.separator;
        String path =pathDir + "cache"+fend;
        File fileDir = new File(pathDir);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        return path;
    }


    private void initReadView() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //InputStream is = getResources().openRawResource(R.raw.epub_book);mBook.id = "00001";mBook.path = getBookFielPath(".epub");
					InputStream is = getResources().openRawResource(R.raw.text_book);	mBook.id = "00002";mBook.path = getBookFielPath(".txt");
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
                            mReadContainerView.addView(mReadView.getContentView(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
							initReaderCatalogView();
							initMenu();
                            isInit = true;
                            new Thread() {
                                @Override
                                public void run() {
                                	int[] index = ReadSetting.getInstance(BaseBookActivity.this).getBookReadProgress(mBook.id);
									mBook = mReadView.decodeBookFromPlugin(index[0], index[1], "");
									mCatalogView.setBookInfo(mBook.title, mBook.author);
									//mReadView.decodeBookFromPlugin(0, 0, "");
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

    public boolean isPullEnabled() {
        return !mReadView.isAnimating();
    }

}
