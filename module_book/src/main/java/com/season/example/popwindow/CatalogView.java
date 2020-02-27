package com.season.example.popwindow;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.book.R;
import com.season.example.adapter.BookDigestsItemAdapter;
import com.season.example.adapter.BookmarkItemAdapter;
import com.season.example.adapter.CatalogAdapter;
import com.season.example.adapter.CatalogViewPagerAdapter;
import com.season.lib.bean.BookDigests;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.BookMark;
import com.season.lib.bean.Catalog;
import com.season.lib.db.BookDigestsDB;
import com.season.lib.db.BookMarkDB;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.util.SimpleAnimationListener;

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
	private boolean isShowing = false;
	private boolean isDismissing = false;
	private View mGotoReaderBut;

	private CatalogAdapter catalogAdapter;
	private BookDigestsItemAdapter mBookDigestsAdapter;
	private BookmarkItemAdapter mBookMarkAdapter;
	public CatalogView(final Activity context, IActionCallBack actionCallBack) {
		super(context);
		mContext = context;
		mCallBack = actionCallBack;
		mBookInfo = mCallBack.getBookInfo();
		LayoutInflater.from(context).inflate(R.layout.pager_tabs, this, true);
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
				mCallBack.showReaderContentView();
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
				mCallBack.showReaderContentView();
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


	public void show(){
		if (isShowing){
			return;
		}
		setVisibility(View.VISIBLE);
		isShowing = true;

		catalogAdapter.selectCatalog = mCatalogList.indexOf(mCallBack.getCurrentCatalog());
		catalogAdapter.notifyDataSetChanged();
		mBookDigestsAdapter.setData(BookDigestsDB.getInstance().getListBookDigests(
				mBookInfo.id));
		mBookMarkAdapter.setData(BookMarkDB.getInstance().getUserBookMark(mBookInfo.id));

		Animation trans1 = new TranslateAnimation(
				Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(), Animation.ABSOLUTE,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		trans1.setDuration(600);
		trans1.setAnimationListener(new Animation.AnimationListener() {

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
		startAnimation(trans1);
	}
	public void dismiss(final SimpleAnimationListener listener){
		if (isDismissing){
			return;
		}
		isDismissing = true;
		setVisibility(View.INVISIBLE);
		Animation trans1 = new TranslateAnimation(Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(),
				Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		trans1.setDuration(350);
		trans1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isDismissing = false;
				listener.onAnimationEnd(animation);
			}
		});
		startAnimation(trans1);
	}

	
	public interface IActionCallBack{

		Catalog getCurrentCatalog();

		void showReaderContentView();

		void selectCatalog(Catalog catalog);
		void selectBookmark(BookMark bookMark);
		void selectDigest(BookDigests bookDigests);

		BookInfo getBookInfo();

	}
}
