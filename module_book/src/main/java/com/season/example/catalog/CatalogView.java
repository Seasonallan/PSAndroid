package com.season.example.catalog;

import java.util.ArrayList;

import android.app.Activity;
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
import com.season.example.popwindow.ViewPagerTabHost;
import com.season.lib.bean.BookDigests;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.BookMark;
import com.season.lib.bean.Catalog;
import com.season.lib.db.BookDigestsDB;
import com.season.lib.db.BookMarkDB;
import com.season.lib.dimen.ScreenUtils;

/**
 * 目录视图
 */
public class CatalogView extends FrameLayout{
	private static final String TAG = CatalogView.class.getSimpleName();
	public static final String TAG_CATALOG = "TAG_CATALOG";
	public static final String TAG_DIGEST = "TAG_DIGEST";
	public static final String TAG_BOOKMARK = "TAG_BOOKMARK";

	private Activity mContext;
	private BookInfo mBookInfo;
	private TextView mBookNameTV;
	private TextView mAuthorNameTV;
	private ViewPagerTabHost mTabHost;
	private CatalogViewPagerAdapter mViewPagerAdapter;
	protected ArrayList<Catalog> mCatalogList;
	private ArrayList<String> mTags = new ArrayList<String>();
	private IActionCallBack mCallBack;
	private View mGotoReaderBut;

	private CatalogAdapter catalogAdapter;
	private BookDigestsItemAdapter mBookDigestsAdapter;
	private BookmarkItemAdapter mBookMarkAdapter;
	public CatalogView(final Activity context, IActionCallBack actionCallBack) {
		super(context);
		mContext = context;
		mCallBack = actionCallBack;
		LayoutInflater.from(context).inflate(R.layout.reader_catalog, this, true);
		mBookNameTV = (TextView) findViewById(R.id.catalog_book_name_tv);
		mAuthorNameTV = (TextView) findViewById(R.id.catalog_author_name_tv);
		((SlideTabWidget) findViewById(android.R.id.tabs)).initialize(LayoutParams.FILL_PARENT,getResources().getDrawable(R.drawable.ic_reader_catalog_select_bg));
		mTabHost = (ViewPagerTabHost)findViewById(android.R.id.tabhost);
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
					mCallBack.selectCatalog(mCatalogList.get(position));
				}else if(tag.equals(CatalogView.TAG_DIGEST)){
					mCallBack.selectDigest(mBookDigestsAdapter.getItem(position));
				}else if(tag.equals(CatalogView.TAG_BOOKMARK)){
					mCallBack.selectBookmark(mBookMarkAdapter.getItem(position));
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


	public void setBookInfo(String bookName,String authorName){
		mBookNameTV.setText(bookName);
		mAuthorNameTV.setText(authorName);
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
		setVisibility(View.VISIBLE);
		isShowing = true;

		catalogAdapter.selectCatalog = mCatalogList.indexOf(currentCatalog);
		catalogAdapter.notifyDataSetChanged();
		mBookDigestsAdapter.setData(BookDigestsDB.getInstance().getListBookDigests(
				mBookInfo.id));
		mBookMarkAdapter.setData(BookMarkDB.getInstance().getUserBookMark(mBookInfo.id));
		startAnimation(showAnimation);
	}
	public void dismiss(){
		if (isDismissing){
			return;
		}
		isDismissing = true;
		setVisibility(View.VISIBLE);
		startAnimation(hideAnimation);
	}

	
	public interface IActionCallBack{
		void onDismiss();
		void selectCatalog(Catalog catalog);
		void selectBookmark(BookMark bookMark);
		void selectDigest(BookDigests bookDigests);
	}
}
