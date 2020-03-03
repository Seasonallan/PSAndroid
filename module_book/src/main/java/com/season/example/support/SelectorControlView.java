package com.season.example.support;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import com.example.book.R;
import com.season.example.popwindow.BookDigestsPopWin;
import com.season.example.popwindow.BookDigestsRemarksDialog;
import com.season.lib.event.AbsTextSelectHandler;
import com.season.lib.bean.BookDigests;


public class SelectorControlView implements AbsTextSelectHandler.ISelectorListener {
	
	private BookDigestsPopWin mBookDigestsPopWin;
		
	private View mReadView;
	
	private Activity mActivity;
	
	public SelectorControlView(View readView , Activity activity){
		mReadView = readView;
		mActivity = activity;
	}
	
	@Override
	public void onInit(float x, float y, Bitmap bitmap,
			AbsTextSelectHandler textSelectHandler) {
		
		if(mBookDigestsPopWin == null){
			mBookDigestsPopWin = new BookDigestsPopWin(mReadView
					, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,mActivity, textSelectHandler);
		}
		mBookDigestsPopWin.show(BookDigestsPopWin.VIEW_TYPE_MAGNIFIER, x, y, bitmap);
	}
		
	@Override
	public void onChange(float x, float y, Bitmap bitmap,
			AbsTextSelectHandler textSelectHandler) {
		if(mBookDigestsPopWin == null){
			return;
		}
		mBookDigestsPopWin.show(BookDigestsPopWin.VIEW_TYPE_MAGNIFIER, x, y, bitmap);
		mBookDigestsPopWin.setmTextSelectHandler(textSelectHandler);
		
	}

	@Override
	public void onPause(float x, float y,AbsTextSelectHandler textSelectHandler) {
		if(mBookDigestsPopWin == null){
			return;
		}
		mBookDigestsPopWin.show(BookDigestsPopWin.VIEW_TYPE_MENU_1, x, y);
		mBookDigestsPopWin.setmTextSelectHandler(textSelectHandler);
		
	}

	@Override
	public void onStop(AbsTextSelectHandler textSelectHandler) {
		if(mBookDigestsPopWin == null){
			return;
		}
		if(mBookDigestsPopWin != null){
			mBookDigestsPopWin.dismiss();
		}
//		textSelectHandler.noticeDataChanges();
	}

	@Override
	public void onOpenEditView(float x, float y, BookDigests bookDigests,
                               AbsTextSelectHandler textSelectHandler) {
		mBookDigestsPopWin = new BookDigestsPopWin(mReadView
				, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,mActivity, textSelectHandler);
		mBookDigestsPopWin.setmBookDigests(bookDigests);
		mBookDigestsPopWin.setTouchable(true);
		mBookDigestsPopWin.show(BookDigestsPopWin.VIEW_TYPE_MENU_2, x, y);
		
	}

	@Override
	public void onCloseEditView(AbsTextSelectHandler textSelectHandler) {
		if(mBookDigestsPopWin == null){
			return;
		}
		mBookDigestsPopWin.dismiss();
	}

	@Override
	public void onOpenDigestView(BookDigests bookDigests, AbsTextSelectHandler textSelectHandler) {

		final BookDigestsRemarksDialog mDialog=new BookDigestsRemarksDialog(mActivity, R.style.remark_Dialog,
				textSelectHandler , bookDigests);
		mDialog.show();
		mDialog.setOnCloseDialogLisenter(new BookDigestsRemarksDialog.OnCloseDialogLisenter(){

			@Override
			public void onCloseDialog(boolean isCloseDialog) {
				if(isCloseDialog){
					mDialog.dismiss();
				}
			}
		});
	}

}
