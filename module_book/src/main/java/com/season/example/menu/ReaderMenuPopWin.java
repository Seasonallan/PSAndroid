package com.season.example.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.book.R;
import com.season.lib.ReadSetting;
import com.season.lib.bean.BookInfo;
import com.season.lib.page.span.media.ReaderMediaPlayer;
import com.season.lib.util.SimpleAnimationListener;
import com.season.lib.util.ToastUtil;
import com.season.lib.view.CheckedGridView;
import com.season.lib.view.CheckedGridView.OnItemCheckedStateChangeListener;
import com.season.lib.page.span.media.ReaderMediaPlayer.PlayerListener;

public class ReaderMenuPopWin extends FrameLayout implements PlayerListener{
	private static final int FONT_INCREASE_UNIT = 1;
	private static final int MAX_MENU_SIZE = 5;
	private Activity mActivity;
	private BookInfo mBook;
	private IActionCallback mActionCallback;
	private CheckedGridView mGridView;
	private View addBookmarkIB;
	private ViewGroup mChildMenuLayout;
	private TextView mTitleTV;
	private View mBuyBut;
	private boolean mHasBookmark;
	private ArrayList<MenuItem> mMoreMenuItems;
	private View mJumpPageView;
	private int mTotalPageNums;
	private TextView mJumpPageTip;
	private SeekBar mJumpSeekBar;
	private View mJumpPreBut;
	private View mJumpNextBut;
	private View mBrightessSettingView;
	private View mMoreView;
	private View mThemeView;
	private View mFontSettingView;
	private View mCutFontSizeBut;
	private View mAddFontSizeBut;
	private ReadSetting mReadSetting;
	private RadioGroup mLineSpacingRG;
	private View mVoiceLayout;
	private SeekBar mVoiceSeekBar;
	private TextView mVoiceMaxProgressTV;
	private TextView mVoiceProgressTV;
	private ImageButton mVoiceCloseBut;
	private ImageButton mVoiceStateBut;
	private boolean isVoicePlay;
	public ReaderMenuPopWin(Activity activity, IActionCallback callback) {
		super(activity);
		mActivity = activity;
		mReadSetting = ReadSetting.getInstance(mActivity);
		mActionCallback = callback;
		//设置屏幕亮度
		setScreenBrightess(mReadSetting.getBrightessLevel());
		ReaderMediaPlayer.getInstance().addPlayerListener(this);
		isVoicePlay = ReaderMediaPlayer.getInstance().isPlaying();
		onCreateContentView();
	}


	private boolean isShowing = false,isDismissing = false;
	private Animation dowAnimation, upAnimation;
	private int timeHalf = 200, time = 400;
	public void show(BookInfo book) {
		this.mBook = book;
		if (isShowing){
			return;
		}
		hideAllViews();
		if(!ReaderMediaPlayer.getInstance().isPlayerStop()){
			mVoiceLayout.setVisibility(View.GONE);
		}else{
			mVoiceLayout.setVisibility(View.VISIBLE);
		}
		updateState();
		mGridView.clearChoices();
		showJumpPageView();

		setVisibility(View.VISIBLE);

		isShowing = true;

		dowAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f);
		upAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f);
		dowAnimation.setDuration(time);
		dowAnimation.setAnimationListener(new SimpleAnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				isShowing = false;
			}
		});
		topView.startAnimation(dowAnimation);
		mChildMenuLayout.setVisibility(View.GONE);
		upAnimation.setDuration(timeHalf);
		upAnimation.setAnimationListener(new SimpleAnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				mGridView.clearAnimation();
				upAnimation.setAnimationListener(null);
				mChildMenuLayout.setVisibility(View.VISIBLE);
				mChildMenuLayout.startAnimation(upAnimation);
			}
		});
		mGridView.startAnimation(upAnimation);
	}


	public void dismiss(final boolean notify) {
		if (isDismissing){
			return;
		}
		isDismissing = true;
		setVisibility(View.VISIBLE);

		dowAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
		upAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0f,Animation.RELATIVE_TO_SELF, -1.0f);
		upAnimation.setDuration(time);
		upAnimation.setAnimationListener(new SimpleAnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				isDismissing = false;
				setVisibility(View.GONE);
				if (notify)
					mActionCallback.onDismiss();
			}
		});
		topView.startAnimation(upAnimation);
		dowAnimation.setDuration(timeHalf);
		dowAnimation.setAnimationListener(new SimpleAnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				mChildMenuLayout.clearAnimation();
				dowAnimation.setAnimationListener(null);
				mChildMenuLayout.setVisibility(View.GONE);
				mGridView.startAnimation(dowAnimation);
			}
		});
		mChildMenuLayout.startAnimation(dowAnimation);
	}


	public void validateBookmarkState(boolean hasBookmark) {
		mHasBookmark = hasBookmark;
		if(addBookmarkIB == null) {
			return;
		}
	}

	protected void onCreateContentView() {
		getLayoutInflater().inflate(R.layout.reader_menu, this, true);
		mVoiceLayout = findViewById(R.id.menu_reader_voice_layout);
		mVoiceStateBut = (ImageButton)findViewById(R.id.menu_reader_voice_state_but);
		mVoiceCloseBut = (ImageButton)findViewById(R.id.menu_reader_voice_close_but);
		mVoiceProgressTV = (TextView)findViewById(R.id.menu_reader_voice_progress_tv);
		mVoiceMaxProgressTV = (TextView)findViewById(R.id.menu_reader_voice_max_progress_tv);
		mVoiceSeekBar = (SeekBar)findViewById(R.id.menu_reader_voice_seek);
		final ReaderMediaPlayer voicePlayer = ReaderMediaPlayer.getInstance();
		onProgressChange(voicePlayer.getCurrentPosition(), voicePlayer.getDuration(), null);
		if(isVoicePlay){
			mVoiceStateBut.setImageResource(R.drawable.ic_menu_reader_voice_play);
		}else{
			mVoiceStateBut.setImageResource(R.drawable.ic_menu_reader_voice_pause);
		}
		mVoiceCloseBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voicePlayer.stop();
				Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_hide);
				animation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						mVoiceLayout.setVisibility(View.GONE);
					}
				});
				mVoiceLayout.startAnimation(animation);
			}
		});
		mVoiceStateBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isVoicePlay = !isVoicePlay;
				voicePlayer.setPlayState(isVoicePlay);
			}
		});
		mVoiceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				voicePlayer.seekTo(seekBar.getProgress());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				voicePlayer.pause();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				onProgressChange(progress, seekBar.getMax(), null);
			}
		});
		
		mChildMenuLayout = (ViewGroup) findViewById(R.id.menu_child_layout);
		findViewById(R.id.transparent_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowing || isDismissing){
					return;
				}
				dismiss(true);
			}
		});
		addBookmarkIB = findViewById(R.id.menu_add_bookmark_ib);
		addBookmarkIB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mHasBookmark){
					mActionCallback.onSaveUserBookmark();
				}else{
					mActionCallback.onDeleteUserBookmark();
				}
			}
		});
		if (mActionCallback.canAddUserBookmark()) {
			addBookmarkIB.setVisibility(View.VISIBLE);
		}
		
		mTitleTV = (TextView) findViewById(R.id.menu_body_title);
		mBuyBut = findViewById(R.id.menu_buy_but);
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_CATALOG, R.drawable.menu_icon_mark, getString(R.string.reader_menu_item_catalog_tip)));
		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_FONT, R.drawable.menu_icon_font, getString(R.string.reader_menu_item_font_tip)));
		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_THEME, R.drawable.menu_icon_background, getString(R.string.reader_menu_item_theme_tip)));
		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_BRIGHTNESS, R.drawable.menu_icon_brightness, getString(R.string.reader_menu_item_brightness_tip)));
//		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_SETTING, R.drawable.menu_icon_settings, getString(R.string.reader_menu_item_setting_tip)));
		if(menuItems.size() > MAX_MENU_SIZE){
			mMoreMenuItems = new ArrayList<MenuItem>(menuItems.subList(MAX_MENU_SIZE - 1, menuItems.size()));
			menuItems = new ArrayList<MenuItem>(menuItems.subList(0, MAX_MENU_SIZE - 1));
			menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_MORE, R.drawable.menu_icon_more, getString(R.string.reader_menu_item_more_tip)));
		}
		MenuItemAdapter adapter = new MenuItemAdapter(getContext(), menuItems);
		mGridView = (CheckedGridView) findViewById(R.id.reader_menu_gv);
		mGridView.setChoiceMode(CheckedGridView.CHOICE_MODE_SINGLE);
		mGridView.setAdapter(adapter);
		mGridView.setNumColumns(menuItems.size());
		mGridView.setOnItemCheckedStateChangeListener(new OnItemCheckedStateChangeListener() {
			@Override
			public void onItemCheckedStateChange(AdapterView<?> parent, int position,boolean isChecked) {
			}
			@Override
			public boolean onPreItemCheckedStateChange(AdapterView<?> parent,
					int position, boolean isChecked) {
				MenuItem item = (MenuItem) parent.getItemAtPosition(position);
				if(isChecked){
					return handlerMenuItemAction(item);
				}else{
					showJumpPageView();
				}
				return false;
			}
		});
		topView = findViewById(R.id.menu_header_layout);
	}
	private View topView;

	public LayoutInflater getLayoutInflater() {
		return LayoutInflater.from(getContext());
	}

	public String getString(int id, Object... formatArgs) {
		return getResources().getString(id, formatArgs);
	}
	
	private void updateState(){
		if(mActionCallback.isNeedBuy()){
			mBuyBut.setVisibility(View.VISIBLE);
			mBuyBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActionCallback.onGotoBuyBook();
					dismiss(true);
				}
			});
		}else{
			mBuyBut.setVisibility(View.GONE);
		}
		mTitleTV.setVisibility(View.VISIBLE);
		mTitleTV.setText(mBook.title);
		validateBookmarkState(mHasBookmark);
	}

	public void setLayoutChapterProgress(int progress,int max){
		if(mJumpPageView != null){
			if(progress == max){
				setJumpSeekBarProgress(mActionCallback.getCurPage(), mActionCallback.getPageNums());
				mJumpSeekBar.setSecondaryProgress(0);
				mJumpPageTip.setTextColor(getResources().getColor(R.color.common_white_2));
			}else{
				mJumpSeekBar.setProgress(0);
				mJumpSeekBar.setSecondaryProgress(progress);
				mJumpSeekBar.setMax(max);
				mJumpSeekBar.setEnabled(false);
				mJumpPreBut.setEnabled(mActionCallback.hasPreChapter());
				mJumpNextBut.setEnabled(mActionCallback.hasNextChapter());
				mJumpPageTip.setText(getString(R.string.reader_menu_item_seek_layouting_tip, (int)(progress * 1f / max * 100)+"%"));
				mJumpPageTip.setTextColor(Color.parseColor("#60ffffff"));
			}
		}
	}
	
	public void setJumpSeekBarProgress(int progress,int max){
		if(mJumpPageView != null){
			mTotalPageNums = max;
			updateJumpSeekBar(progress, mTotalPageNums);
		}
	}
	
	private void showPageNum(int curPage, int pageNums){
		if(pageNums > 1){
			curPage += 1;
		}
		if(mJumpPageTip != null){
			mJumpPageTip.setText(getString(R.string.reader_menu_item_seek_page_tip, curPage, pageNums));
		}
	}
	
	private void gotoPage(int page,int oldPage){
		mActionCallback.onGotoPage(page);
	}
	
	private void showJumpPageView(){
		int pageNums = mActionCallback.getPageNums();
		int curPage = mActionCallback.getCurPage();
		mTotalPageNums = pageNums;
		if(mJumpPageView == null){
			mJumpPageView = getLayoutInflater().inflate(R.layout.reader_menu_jump_page, null);
			mJumpPageTip = (TextView) mJumpPageView.findViewById(R.id.page_text);
			mJumpSeekBar = ((SeekBar) mJumpPageView.findViewById(R.id.jump_page_seek));
			mJumpSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				int oldPage = 0;
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					showPageNum(seekBar.getProgress(), mTotalPageNums);
					gotoPage(seekBar.getProgress(),oldPage);
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					oldPage = seekBar.getProgress();
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					showPageNum(seekBar.getProgress(), mTotalPageNums);
				}
			});
			mJumpPreBut = mJumpPageView.findViewById(R.id.jump_pre_but);
			mJumpPreBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActionCallback.gotoPreChapter();
					mJumpPreBut.setEnabled(mActionCallback.hasPreChapter());
					mJumpNextBut.setEnabled(mActionCallback.hasNextChapter());
				}
			});
			mJumpNextBut = mJumpPageView.findViewById(R.id.jump_next_but);
			mJumpNextBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActionCallback.gotoNextChapter();
					mJumpPreBut.setEnabled(mActionCallback.hasPreChapter());
					mJumpNextBut.setEnabled(mActionCallback.hasNextChapter());
				}
			});
		}
		updateJumpSeekBar(curPage, mTotalPageNums);
		showChildMenu(mJumpPageView);
	}
	
	private void updateJumpSeekBar(int curPage,int max){
		if(max < 0){
			setLayoutChapterProgress(mActionCallback.getLayoutChapterProgress(), mActionCallback.getLayoutChapterMax());
		}else{
			if(max == 1){
				curPage = 1;
				max = 1;
				mJumpSeekBar.setMax(max);
				mJumpSeekBar.setProgress(curPage);
				mJumpSeekBar.setEnabled(false);
			}else{
				mJumpSeekBar.setMax(max - 1);
				mJumpSeekBar.setProgress(curPage);
				mJumpSeekBar.setEnabled(true);
			}
			mJumpPreBut.setEnabled(mActionCallback.hasPreChapter());
			mJumpNextBut.setEnabled(mActionCallback.hasNextChapter());
			showPageNum(curPage, max);
		}
	}
	
	private void showBrightessSettingView(){
		SeekBar seekBar = null;
		if(mBrightessSettingView == null){
			mBrightessSettingView = getLayoutInflater().inflate(R.layout.reader_menu_brightness_setting, null);
			seekBar = (SeekBar) mBrightessSettingView.findViewById(R.id.brightness_seek);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					mReadSetting.setBrightessLevel(seekBar.getProgress());
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					setScreenBrightess(progress);
				}
			});
			mBrightessSettingView.findViewById(R.id.brightness_auto_but).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 亮度自动调整
                }
            });
		}
		seekBar = (SeekBar) mBrightessSettingView.findViewById(R.id.brightness_seek);
		WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
		float oldValue = lp.screenBrightness;
		if(oldValue < 0){
			seekBar.setProgress(50);
		}else{
			if(oldValue <= 0.17f){
				seekBar.setProgress(0);
			}else{
				seekBar.setProgress((int) (oldValue * 100));
			}
		}
		showChildMenu(mBrightessSettingView);
	}
	
	private void showThemeView(){
		if(mThemeView == null){
			mThemeView = getLayoutInflater().inflate(R.layout.reader_menu_theme, null);
		}
		GridView gridView  = (GridView) mThemeView;
		ArrayList<ReadStyleItem> readStyleItems = new ArrayList<ReadStyleItem>();
		int style = mReadSetting.getThemeType();
		readStyleItems.add(new ReadStyleItem(ReadSetting.THEME_TYPE_DAY, R.drawable.ic_read_style_day, 
				style == ReadSetting.THEME_TYPE_DAY));
		readStyleItems.add(new ReadStyleItem(ReadSetting.THEME_TYPE_OTHERS_1, R.drawable.ic_read_style_other_1, 
				style == ReadSetting.THEME_TYPE_OTHERS_1));
		readStyleItems.add(new ReadStyleItem(ReadSetting.THEME_TYPE_OTHERS_2, R.drawable.ic_read_style_other_2, 
				style == ReadSetting.THEME_TYPE_OTHERS_2));
		readStyleItems.add(new ReadStyleItem(ReadSetting.THEME_TYPE_OTHERS_3, R.drawable.ic_read_style_other_3, 
				style == ReadSetting.THEME_TYPE_OTHERS_3));
		readStyleItems.add(new ReadStyleItem(ReadSetting.THEME_TYPE_OTHERS_4, R.drawable.ic_read_style_other_4, 
				style == ReadSetting.THEME_TYPE_OTHERS_4));
		
		final ReadStytleItemAdapter adapter = new ReadStytleItemAdapter(getContext(), readStyleItems);
		gridView.setNumColumns(readStyleItems.size());
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.setSeleted(position);
				ReadStyleItem item = (ReadStyleItem) parent.getItemAtPosition(position);
				if(item != null){
					int styleId = item.id;
					mReadSetting.setThemeType(styleId);
//					updateLightState(styleId);
				}
			}
		});
		showChildMenu(mThemeView);
	}

	private void showFontSettingView(){
		if(mFontSettingView == null){
			//初始化字体大小设置逻辑
			mFontSettingView = getLayoutInflater().inflate(R.layout.reader_menu_font_settings, null);
			mCutFontSizeBut = mFontSettingView.findViewById(R.id.menu_settings_font_size_sut_but);
			mAddFontSizeBut = mFontSettingView.findViewById(R.id.menu_settings_font_size_add_but);
			mAddFontSizeBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int temtFontProgress = mReadSetting.getFontLevel();
					temtFontProgress += FONT_INCREASE_UNIT;
					if(temtFontProgress > 10){
						temtFontProgress = 10;
					}
					if(temtFontProgress == 10){
						ToastUtil.showToast("当前为最大字体");
						v.setEnabled(false);
					}
					mReadSetting.setFontLevel(temtFontProgress);
					if(!mCutFontSizeBut.isEnabled()){
						mCutFontSizeBut.setEnabled(true);
					}
				}
			});
			mCutFontSizeBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int temtFontProgress = mReadSetting.getFontLevel();
					temtFontProgress -= FONT_INCREASE_UNIT;
					if(temtFontProgress < 0){
						ToastUtil.showToast("当前为最小字体");
						temtFontProgress = 0;
					}
					if(temtFontProgress == 0){
						v.setEnabled(false);
					}
					mReadSetting.setFontLevel(temtFontProgress);
					if(!mAddFontSizeBut.isEnabled()){
						mAddFontSizeBut.setEnabled(true);
					}
				}
			});
			//初始化行距设置逻辑
			mLineSpacingRG = (RadioGroup)mFontSettingView.findViewById(R.id.menu_settings_line_spacing_rg);
			mLineSpacingRG.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					float lineSpaceType = ReadSetting.FONT_LINE_SPACE_TYPE_1;
					if(checkedId == R.id.menu_settings_line_spacing_but_1){
						lineSpaceType = ReadSetting.FONT_LINE_SPACE_TYPE_1;
					}else if(checkedId == R.id.menu_settings_line_spacing_but_2){
						lineSpaceType = ReadSetting.FONT_LINE_SPACE_TYPE_2;
					}else if(checkedId == R.id.menu_settings_line_spacing_but_3){
						lineSpaceType = ReadSetting.FONT_LINE_SPACE_TYPE_3;
					}
					mReadSetting.setLineSpaceType(lineSpaceType);
				}
			});
		}
		
		//同步字体大小设置状态
		int temtFontProgress = mReadSetting.getFontLevel();
		if(temtFontProgress == 10){
			mAddFontSizeBut.setEnabled(false);
		}else if(temtFontProgress == 0){
			mCutFontSizeBut.setEnabled(false);
		}
		//同步行距设置状态
		float lineSpaceType = mReadSetting.getLineSpaceType();
		if(lineSpaceType == ReadSetting.FONT_LINE_SPACE_TYPE_1){
			mLineSpacingRG.check(R.id.menu_settings_line_spacing_but_1);
		}else if(lineSpaceType == ReadSetting.FONT_LINE_SPACE_TYPE_2){
			mLineSpacingRG.check(R.id.menu_settings_line_spacing_but_2);
		}else if(lineSpaceType == ReadSetting.FONT_LINE_SPACE_TYPE_3){
			mLineSpacingRG.check(R.id.menu_settings_line_spacing_but_3);
		}
		//显示界面
		showChildMenu(mFontSettingView);
	}
	
	private void showMoreView(){
		if(mMoreView == null){
			mMoreView = getLayoutInflater().inflate(R.layout.reader_menu_more, null);
			if(mMoreMenuItems != null){
				MenuItemAdapter adapter = new MenuItemAdapter(getContext(), mMoreMenuItems);
				GridView gridView = (GridView) mMoreView;
				gridView.setAdapter(adapter);
				gridView.setNumColumns(mMoreMenuItems.size());
				gridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						MenuItem item = (MenuItem) parent.getItemAtPosition(position);
						handlerMenuItemAction(item);
					}
				});
			}
		}
		showChildMenu(mMoreView);
	}
	
	private void showChildMenu(View childContentView){
		hideAllViews();
		childContentView.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
		if(childContentView.getParent() == null){
			mChildMenuLayout.addView(childContentView);
		}else{
			childContentView.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideAllViews(){
		int count = mChildMenuLayout.getChildCount();
		View child = null;
		for(int i = 0;i < count;i++){
			child = mChildMenuLayout.getChildAt(i);
			child.setVisibility(View.GONE);
			child.setAnimation(null);
		}
	}
	
	/** 设置屏幕亮度
	 * @param value
	 */
	private void setScreenBrightess(int value){
		final WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
		lp.screenBrightness = value * 1.0f / 100.0f;
		if(lp.screenBrightness < 0.17){
			lp.screenBrightness = 0.17f;
		}
		mActivity.getWindow().setAttributes(lp);
	}

	private boolean handlerMenuItemAction(MenuItem item){
		dowAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f);
		upAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f);
		dowAnimation.setDuration(timeHalf);upAnimation.setDuration(timeHalf);
		if(item != null){
			ToastUtil.dismissToast();
			switch(item.id){
			case MenuItem.MENU_ITEM_ID_BRIGHTNESS:
				ToastUtil.showToast("若调节失败，请禁用系统自动亮度调节");
				dowAnimation.setAnimationListener(new SimpleAnimationListener(){
					@Override
					public void onAnimationEnd(Animation animation) {
						showBrightessSettingView();
						mChildMenuLayout.startAnimation(upAnimation);
					}
				});
				mChildMenuLayout.startAnimation(dowAnimation);
				break;
			case MenuItem.MENU_ITEM_ID_FONT:
				dowAnimation.setAnimationListener(new SimpleAnimationListener(){
					@Override
					public void onAnimationEnd(Animation animation) {
						showFontSettingView();
						mChildMenuLayout.startAnimation(upAnimation);
					}
				});
				mChildMenuLayout.startAnimation(dowAnimation);
				break;
			case MenuItem.MENU_ITEM_ID_SETTING:
				return true;
			case MenuItem.MENU_ITEM_ID_CATALOG:
				dismiss(false);
				mActionCallback.onShowReaderCatalog();
				return true;
			case MenuItem.MENU_ITEM_ID_MORE:
				showMoreView();
				break;
			case MenuItem.MENU_ITEM_ID_THEME:
				dowAnimation.setAnimationListener(new SimpleAnimationListener(){
					@Override
					public void onAnimationEnd(Animation animation) {
						showThemeView();
						mChildMenuLayout.startAnimation(upAnimation);
					}
				});
				mChildMenuLayout.startAnimation(dowAnimation);
				break;
			}
		}
		return false;
	}

	
	public interface IActionCallback{
		void onDismiss();
		int getLayoutChapterProgress();
		int getLayoutChapterMax();
		int getPageNums();
		int getCurPage();
		void onGotoPage(int pageNum);
		void onGotoBuyBook();
		boolean isNeedBuy();
		boolean canAddUserBookmark();
		void onSaveUserBookmark();
		void onDeleteUserBookmark();
		void onShowReaderCatalog();
		void gotoPreChapter();
		void gotoNextChapter();
		boolean hasPreChapter();
		boolean hasNextChapter();
	}

	@Override
	public void onPlayStateChange(int playState, String voiceSrc) {
		isVoicePlay = playState == ReaderMediaPlayer.STATE_START;
		if(mVoiceLayout != null){
			if(isVoicePlay){
				mVoiceStateBut.setImageResource(R.drawable.ic_menu_reader_voice_play);
			}else{
				mVoiceStateBut.setImageResource(R.drawable.ic_menu_reader_voice_pause);
			}
			if(!ReaderMediaPlayer.getInstance().isPlayerStop()){
				mVoiceLayout.setVisibility(View.GONE);
			}else{
				mVoiceLayout.setVisibility(View.VISIBLE);
			}
			mVoiceSeekBar.setEnabled(ReaderMediaPlayer.getInstance().isPrepare());
		}
	}

	@Override
	public void onProgressChange(long currentPosition, long maxPosition, String voiceSrc) {
		if(mVoiceLayout != null){
			mVoiceSeekBar.setMax((int) maxPosition);
			mVoiceSeekBar.setProgress((int) currentPosition);
			mVoiceProgressTV.setText(ReaderMediaPlayer.getTimeStr((int) (currentPosition / 1000)));
			mVoiceMaxProgressTV.setText(ReaderMediaPlayer.getTimeStr((int) (maxPosition / 1000)));
		}
	}
	
	public class ReadStyleItem {
		public int id;
		public int resId;
		public boolean isSelected;
		
		public ReadStyleItem(int id, int resId, boolean isSelected){
			this.id = id;
			this.resId = resId;
			this.isSelected = isSelected;
		}
	}
	
	public class ReadStytleItemAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<ReadStyleItem> readStyleItems;
		private int oldSelect = -1;
		public ReadStytleItemAdapter(Context context, ArrayList<ReadStyleItem> readStyleItems){
			super();
			inflater = LayoutInflater.from(context);
			this.readStyleItems = readStyleItems;
		}

		@Override
		public int getCount() {
			if(readStyleItems != null){
				return readStyleItems.size();
			}
			return 0;
		}

		@Override
		public ReadStyleItem getItem(int position) {
			if(position < getCount()){
				return readStyleItems.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder;
			if(convertView == null){
				convertView = newView();
				viewHolder = new ViewHolder();
				viewHolder.contentIV = (ImageView) convertView.findViewById(R.id.content_iv);
				viewHolder.selectTV = (ImageView) convertView.findViewById(R.id.select_iv);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			ReadStyleItem item = getItem(position);
			
			viewHolder.contentIV.setImageResource(item.resId);
			if(item.isSelected){
				oldSelect = position;
				viewHolder.selectTV.setVisibility(View.VISIBLE);
			}else{
				viewHolder.selectTV.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
		
		private View newView(){
			return inflater.inflate(R.layout.reader_menu_theme_item, null);
		}
		
		public void setSeleted(int position){
			if(position != oldSelect){
				if(oldSelect != -1){
					getItem(oldSelect).isSelected = false;
				}
				getItem(position).isSelected = true;
				oldSelect = position;
				notifyDataSetChanged();
			}
		}
		
		private class ViewHolder {
			
			public ImageView contentIV;
			public ImageView selectTV;
			
		}
	}
}
