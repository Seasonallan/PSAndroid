package com.season.example.catalog;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.book.R;
import com.season.example.popwindow.SlideTabWidget;
import com.season.lib.bean.BookDigests;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.BookMark;
import com.season.lib.bean.Catalog;
import com.season.lib.db.BookDigestsDB;
import com.season.lib.db.BookMarkDB;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.view.IReaderView;

/**
 * 目录视图
 */
public class CatalogView extends FrameLayout{
	public static final String TAG_CATALOG = "TAG_CATALOG";
	public static final String TAG_DIGEST = "TAG_DIGEST";
	public static final String TAG_BOOKMARK = "TAG_BOOKMARK";

	private IReaderView mReadView;
	private BookInfo mBookInfo;
	private TextView mBookNameTV;
	private TextView mAuthorNameTV;
	private BaseViewPagerTabHost mTabHost;
	private CatalogViewPagerAdapter mViewPagerAdapter;
	protected ArrayList<Catalog> mCatalogList;
	private ArrayList<String> mTags = new ArrayList<String>();
	private IActionCallBack mCallBack;
	private View mGotoReaderBut;

	private CatalogAdapter catalogAdapter;
	private BookDigestsItemAdapter mBookDigestsAdapter;
	private BookmarkItemAdapter mBookMarkAdapter;
	public CatalogView(Context context, IReaderView readView, IActionCallBack actionCallBack) {
		super(context);
		this.mReadView = readView;
		mCallBack = actionCallBack;
		LayoutInflater.from(context).inflate(R.layout.reader_catalog, this, true);
		mBookNameTV = (TextView) findViewById(R.id.catalog_book_name_tv);
		mAuthorNameTV = (TextView) findViewById(R.id.catalog_author_name_tv);
		((SlideTabWidget) findViewById(android.R.id.tabs)).initialize(LayoutParams.FILL_PARENT,getResources().getDrawable(R.drawable.ic_reader_catalog_select_bg));
		mTabHost = (BaseViewPagerTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setBackgroundColor(getResources().getColor(R.color.window_bg));
		mTabHost.setup(); 
		mTabHost.setOffscreenPageLimit(2);
		mCatalogList = new ArrayList<Catalog>();
		catalogAdapter = new CatalogAdapter(context, mCatalogList);
		mBookDigestsAdapter = new BookDigestsItemAdapter(context);
		mBookMarkAdapter = new BookmarkItemAdapter(context);
		mGotoReaderBut = findViewById(R.id.left_suspension_but);
		mGotoReaderBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mTags.add(TAG_CATALOG);
		mTags.add(TAG_DIGEST);
		mTags.add(TAG_BOOKMARK);
		mViewPagerAdapter = new CatalogViewPagerAdapter(context, mTags){
			public void onItemClicked(String tag, ListView mListView, int position){
				if(tag.equals(CatalogView.TAG_CATALOG)){
					mReadView.gotoChapter(mCatalogList.get(position), true);
				}else if(tag.equals(CatalogView.TAG_DIGEST)){
					BookDigests bookDigests = mBookDigestsAdapter.getItem(position);
					mReadView.gotoChar(bookDigests.getChaptersId(), bookDigests.getPosition(), true);
				}else if(tag.equals(CatalogView.TAG_BOOKMARK)){
					BookMark bookMark = mBookMarkAdapter.getItem(position);
					mReadView.gotoChar(bookMark.getChapterID(), bookMark.getPosition(), true);
				}
				dismiss();
			}
			public ListAdapter getAdapter(String tag){
				if(tag.equals(CatalogView.TAG_CATALOG)){
					return catalogAdapter;
				}else if(tag.equals(CatalogView.TAG_DIGEST)){
					return mBookDigestsAdapter;
				}else if(tag.equals(CatalogView.TAG_BOOKMARK)){
					return mBookMarkAdapter;
				}
				return null;
			}
		};
		mTabHost.setAdapter(mViewPagerAdapter);

		initAnimation();
	}

	private boolean isShowing = false,isDismissing = false;
	Animation showAnimation, hideAnimation;
	private void initAnimation() {
		showAnimation = new TranslateAnimation(
				Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(), Animation.ABSOLUTE,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		showAnimation.setDuration(600);
		showAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				setAnimation(null);
				isShowing = false;
			}
		});

		hideAnimation = new TranslateAnimation(Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(),
				Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		hideAnimation.setDuration(350);
		hideAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isDismissing = false;
				setVisibility(View.GONE);
				mCallBack.onDismiss();
			}
		});
	}

	private String bookName, authorName;
	public void setBookInfo(String bookName,String authorName){
		this.bookName = bookName;
		this.authorName = authorName;
		this.bookName = TextUtils.isEmpty(bookName)?"书名":bookName;
		this.authorName = TextUtils.isEmpty(authorName)?"作者":authorName;
	}

	public void setCatalogData(ArrayList<Catalog> catalogs){
		if(catalogs == null){
			return;
		}
		mCatalogList.clear();
		mCatalogList.addAll(catalogs);
		catalogAdapter.notifyDataSetChanged();
	}


	public void show(BookInfo book, Catalog currentCatalog){
		mBookInfo = book;
		if (isShowing){
			return;
		}
		mBookNameTV.setText(bookName);
		mAuthorNameTV.setText(authorName);
		setVisibility(View.VISIBLE);
		isShowing = true;

		catalogAdapter.selectCatalog = mCatalogList.indexOf(currentCatalog);
		catalogAdapter.notifyDataSetChanged();

		mViewPagerAdapter.selectCatalog = catalogAdapter.selectCatalog;
		mViewPagerAdapter.notifyDataSetChanged();

		mBookDigestsAdapter.setData(BookDigestsDB.getInstance().getListBookDigests(
				mBookInfo.id));
		mBookMarkAdapter.setData(BookMarkDB.getInstance().getUserBookMark(mBookInfo.id));
		startAnimation(showAnimation);
	}
	public void dismiss(boolean force){
		if (!force && isDismissing){
			return;
		}
		isDismissing = true;
		setVisibility(View.VISIBLE);
		startAnimation(hideAnimation);
	}
	public void dismiss(){
		dismiss(false);
	}
	
	public interface IActionCallBack{
		void onDismiss();
	}
}
