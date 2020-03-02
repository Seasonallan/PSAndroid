package com.season.example.menu;

import java.util.ArrayList;

import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
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
import com.season.lib.view.IReaderView;

public class ReaderMenuPopWin extends FrameLayout implements PlayerListener{
	private static final int MAX_MENU_SIZE = 5;
	private Activity mActivity;
	private IReaderView mReadView;
	private BookInfo mBook;
	private IActionCallback mActionCallback;
	private CheckedGridView mGridView;
	private ViewGroup mChildMenuLayout;
	private ArrayList<MenuItemAdapter.MenuItem> mMoreMenuItems;
	private View mJumpPageView;
	private int mTotalPageNums;
	private TextView mJumpPageTip;
	private SeekBar mJumpSeekBar;
	private View mJumpPreBut;
	private View mJumpNextBut;
	private View mBrightessSettingView;
	private View mMoreView;
	private View mThemeView;
	private ReadSetting mReadSetting;
	private View mVoiceLayout;
	private SeekBar mVoiceSeekBar;
	private TextView mVoiceMaxProgressTV;
	private TextView mVoiceProgressTV;
	private ImageButton mVoiceCloseBut;
	private ImageButton mVoiceStateBut;
	private boolean isVoicePlay;

	public ReaderMenuPopWin(Activity activity, IReaderView readView, IActionCallback callback) {
		super(activity);
		mReadView = readView;
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

	protected void onCreateContentView() {
		getLayoutInflater().inflate(R.layout.reader_menu, this, true);
		findViewById(R.id.menu_back).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mActionCallback.onBackPressed();
            }
        });
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

		ArrayList<MenuItemAdapter.MenuItem> menuItems = new ArrayList<MenuItemAdapter.MenuItem>();
		menuItems.add(new MenuItemAdapter.MenuItem(MenuItemAdapter.MenuItem.MENU_ITEM_ID_CATALOG, R.drawable.menu_icon_mark, "目录"));
		menuItems.add(new MenuItemAdapter.MenuItem(MenuItemAdapter.MenuItem.MENU_ITEM_ID_FONT, R.drawable.menu_icon_font, "排版"));
		menuItems.add(new MenuItemAdapter.MenuItem(MenuItemAdapter.MenuItem.MENU_ITEM_ID_THEME, R.drawable.menu_icon_background, "背景"));
		menuItems.add(new MenuItemAdapter.MenuItem(MenuItemAdapter.MenuItem.MENU_ITEM_ID_BRIGHTNESS, R.drawable.menu_icon_brightness, "亮度"));
//		menuItems.add(new MenuItem(MenuItem.MENU_ITEM_ID_SETTING, R.drawable.menu_icon_settings, "设置"));
		if(menuItems.size() > MAX_MENU_SIZE){
			mMoreMenuItems = new ArrayList<MenuItemAdapter.MenuItem>(menuItems.subList(MAX_MENU_SIZE - 1, menuItems.size()));
			menuItems = new ArrayList<MenuItemAdapter.MenuItem>(menuItems.subList(0, MAX_MENU_SIZE - 1));
			menuItems.add(new MenuItemAdapter.MenuItem(MenuItemAdapter.MenuItem.MENU_ITEM_ID_MORE, R.drawable.menu_icon_more, "更多"));
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
				MenuItemAdapter.MenuItem item = (MenuItemAdapter.MenuItem) parent.getItemAtPosition(position);
				if(isChecked){
					return handlerMenuItemAction(item);
				}else{
					dowAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f);
					upAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0,
							Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f);
					dowAnimation.setDuration(timeHalf);upAnimation.setDuration(timeHalf);
					dowAnimation.setAnimationListener(new SimpleAnimationListener(){
						@Override
						public void onAnimationEnd(Animation animation) {
							showJumpPageView();
							mChildMenuLayout.startAnimation(upAnimation);
						}
					});
					mChildMenuLayout.startAnimation(dowAnimation);
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

	public void setLayoutChapterProgress(int progress,int max){
		if(mJumpPageView != null){
			if(progress == max){
				setJumpSeekBarProgress(mReadView.getCurReadProgress(), mReadView.getMaxReadProgress());
				mJumpSeekBar.setSecondaryProgress(0);
				mJumpPageTip.setTextColor(getResources().getColor(R.color.common_white_2));
			}else{
				mJumpSeekBar.setProgress(0);
				mJumpSeekBar.setSecondaryProgress(progress);
				mJumpSeekBar.setMax(max);
				mJumpSeekBar.setEnabled(false);
				mJumpPreBut.setEnabled(mReadView.hasPreChapter());
				mJumpNextBut.setEnabled(mReadView.hasNextChapter());//正在排版中...%1$s
				mJumpPageTip.setText("正在排版中..."+(int)(progress * 1f / max * 100)+"%");
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
			mJumpPageTip.setText( curPage +"/"+pageNums);
		}
	}

	private void gotoPage(int page,int oldPage){
		mReadView.gotoPage(page, true);
	}

	private void showJumpPageView(){
		int pageNums = mReadView.getMaxReadProgress();
		int curPage = mReadView.getCurReadProgress();
		mTotalPageNums = pageNums;
		if(mJumpPageView == null){
			mJumpPageView = getLayoutInflater().inflate(R.layout.reader_menu_jump_page, null);
			mJumpPageTip = (TextView) mJumpPageView.findViewById(R.id.page_text);
			mJumpSeekBar = ((SeekBar) mJumpPageView.findViewById(R.id.jump_page_seek));
			mJumpSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				int oldPage = 0;
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					mJumpPageTip.setVisibility(View.GONE);
					showPageNum(seekBar.getProgress(), mTotalPageNums);
					gotoPage(seekBar.getProgress(),oldPage);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					oldPage = seekBar.getProgress();
					mJumpPageTip.setVisibility(View.VISIBLE);
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
					mReadView.gotoPreChapter();
					mJumpPreBut.setEnabled(mReadView.hasPreChapter());
					mJumpNextBut.setEnabled(mReadView.hasNextChapter());
				}
			});
			mJumpNextBut = mJumpPageView.findViewById(R.id.jump_next_but);
			mJumpNextBut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReadView.gotoNextChapter();
					mJumpPreBut.setEnabled(mReadView.hasPreChapter());
					mJumpNextBut.setEnabled(mReadView.hasNextChapter());
				}
			});
		}
		updateJumpSeekBar(curPage, mTotalPageNums);
		showChildMenu(mJumpPageView);
	}

	private void updateJumpSeekBar(int curPage,int max){
		if(max < 0){
			setLayoutChapterProgress(mReadView.getLayoutChapterProgress(), mReadView.getLayoutChapterMax());
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
			mJumpPreBut.setEnabled(mReadView.hasPreChapter());
			mJumpNextBut.setEnabled(mReadView.hasNextChapter());
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

	private int[] themeTypes = {ReadSetting.THEME_TYPE_DAY, ReadSetting.THEME_TYPE_OTHERS_1, ReadSetting.THEME_TYPE_OTHERS_2
			, ReadSetting.THEME_TYPE_OTHERS_3, ReadSetting.THEME_TYPE_OTHERS_4};
	private void showThemeView(){
		if(mThemeView == null){
			mThemeView = getLayoutInflater().inflate(R.layout.reader_menu_theme, null);
		}
		GridView gridView  = (GridView) mThemeView;
		final ReadStytleItemAdapter adapter = new ReadStytleItemAdapter(getContext(), themeTypes);
		adapter.selectedType = mReadSetting.getThemeType();
		gridView.setNumColumns(themeTypes.length);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int selectedType = themeTypes[position];
				if (selectedType != mReadSetting.getThemeType()){
					adapter.selectedType = selectedType;
					adapter.notifyDataSetChanged();
					mReadSetting.setThemeType(selectedType);
				}
			}
		});
		showChildMenu(mThemeView);
	}


	private View mFontSettingView;
	private SizeChangeTool mFontSizeTool, mLineSpaceTool, mParagraphTool, mTopBottomTool, mLeftRightTool, mIndentTool;
	private void showFontSettingView(){
		if(mFontSettingView == null){
			mFontSettingView = getLayoutInflater().inflate(R.layout.reader_menu_font_settings, null);
			mFontSizeTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.font_setting)) {
				@Override
				public String getDesc() {
					return "字号";
				}
				@Override
				public int getLevel() {
					return mReadSetting.getFontLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setFontLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getFontSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.FONT_SIZE_COUNT;
				}
			};
			mLineSpaceTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.line_setting)) {
				@Override
				public String getDesc() {
					return "行间距";
				}

				@Override
				public int getLevel() {
					return mReadSetting.getLineSpaceLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setLineSpaceLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getLineSpaceSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.LINE_SPACE_COUNT;
				}
			};
			mParagraphTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.paragraph_setting)) {
				@Override
				public String getDesc() {
					return "段间距";
				}

				@Override
				public int getLevel() {
					return mReadSetting.getParagraphSpaceLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setParagraphSpaceLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getParagraphSpaceSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.PARAGRAPH_SPACE_COUNT;
				}
			};
			mTopBottomTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.top_bottom_setting)) {
				@Override
				public String getDesc() {
					return "上下边距";
				}

				@Override
				public int getLevel() {
					return mReadSetting.getTopBottomSpaceLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setTopBottomSpaceLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getTopBottomSpaceSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.TOPBOTTOM_SPACE_COUNT;
				}
			};
			mLeftRightTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.left_right_setting)) {
				@Override
				public String getDesc() {
					return "左右边距";
				}

				@Override
				public int getLevel() {
					return mReadSetting.getLeftRightSpaceLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setLeftRightSpaceLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getLeftRightSpaceSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.LEFTRIGHT_SPACE_COUNT;
				}
			};
			mIndentTool = new SizeChangeTool(mFontSettingView.findViewById(R.id.indent_setting)) {
				@Override
				public String getDesc() {
					return "首行缩进";
				}

				@Override
				public int getLevel() {
					return mReadSetting.getIndentSizeLevel();
				}

				@Override
				public void setLevel(int level) {
					mReadSetting.setIndentSizeLevel(level);
				}

				@Override
				public int getValue() {
					return mReadSetting.getIndentSizeSize();
				}

				@Override
				public int getLevelCount() {
					return mReadSetting.INDENT_SIZE_COUNT;
				}
			};
		}
		mFontSizeTool.resetStatus();
		mLineSpaceTool.resetStatus();
		mParagraphTool.resetStatus();
		mTopBottomTool.resetStatus();
		mLeftRightTool.resetStatus();
		mIndentTool.resetStatus();
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
						MenuItemAdapter.MenuItem item = (MenuItemAdapter.MenuItem) parent.getItemAtPosition(position);
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

	private boolean handlerMenuItemAction(MenuItemAdapter.MenuItem item){
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
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_BRIGHTNESS:
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
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_FONT:
				dowAnimation.setAnimationListener(new SimpleAnimationListener(){
					@Override
					public void onAnimationEnd(Animation animation) {
						showFontSettingView();
						mChildMenuLayout.startAnimation(upAnimation);
					}
				});
				mChildMenuLayout.startAnimation(dowAnimation);
				break;
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_SETTING:
				return true;
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_CATALOG:
				dismiss(false);
				mActionCallback.onShowReaderCatalog();
				return true;
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_MORE:
				showMoreView();
				break;
			case MenuItemAdapter.MenuItem.MENU_ITEM_ID_THEME:
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
		void onShowReaderCatalog();
        void onBackPressed();
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

}
