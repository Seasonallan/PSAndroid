package com.season.example.popwindow;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;


import com.example.book.R;
import com.season.lib.AbsTextSelectHandler;
import com.season.lib.bean.BookDigests;
import com.season.lib.util.NavigationBarUtil;

import java.util.Date;

public class BookDigestsRemarksDialog extends Dialog {
	public final static int TRANS = 0xFF000000;
	public final static int YELLOW = Color.parseColor("#e6ef50") + TRANS;
	public final static int ORANGE = Color.parseColor("#f1b136") + TRANS;
	public final static int GREEN = Color.parseColor("#66cc00") + TRANS;
	public final static int BLUE = Color.parseColor("#13b0a5") + TRANS;
	public final static int PINK = Color.parseColor("#fc9d9a") + TRANS;
    public final static int HSPAC = 5;
	private EditText mRemarks_et;
	private AbsTextSelectHandler mTextSelectHandler;
	private Activity mContext;
	private BookDigests mBookDigests;
	private OnCloseDialogLisenter mOnCloseDialogLisenter;
	
	public BookDigestsRemarksDialog(Activity context,int theme, AbsTextSelectHandler textSelectHandler) {
		super(context,theme);
		this.mTextSelectHandler = textSelectHandler;
		this.mContext = context;
	}
	
	public BookDigestsRemarksDialog(Activity context, int theme,AbsTextSelectHandler textSelectHandler, BookDigests bookDigests) {
		super(context,theme);
		this.mTextSelectHandler = textSelectHandler;
		this.mContext = context;
		this.mBookDigests = bookDigests;
	}
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.pop_digest);

		mRemarks_et = (EditText) findViewById(R.id.remarks_edit);
		EditText content = (EditText) findViewById(R.id.digests_content);
		if(mBookDigests == null){
			mBookDigests = mTextSelectHandler.getCurrentBookDigests();
		}
		if(mBookDigests.getContent() == null){
			mBookDigests.setContent(mTextSelectHandler.getData(mBookDigests));
		}
		content.setText(mBookDigests.getContent());
		
		if(mBookDigests.getMsg() != null){
			
			mRemarks_et.setText(mBookDigests.getMsg());
		}
		preColorView();

	}

	private int[] colors = {YELLOW, ORANGE, GREEN, BLUE, PINK};
	BookDigestColorItemAdapter adapter;
	private void preColorView() {
		adapter = new BookDigestColorItemAdapter(mContext, colors);
		adapter.selectedColor = mBookDigests.getBGColor();
		GridView gridView = (GridView) findViewById(R.id.color_gv);
		gridView.setHorizontalSpacing(HSPAC);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int color = colors[position];
				if (color != adapter.selectedColor){
					adapter.selectedColor = color;
					adapter.notifyDataSetChanged();
				}
			}
		});
		 
		Button leftButton = (Button) findViewById(R.id.remarks_save_btn);
		leftButton.setOnClickListener(choseListener);
		Button rightButton = (Button) findViewById(R.id.remarks_cancel_btn);
		rightButton.setOnClickListener(choseListener);
		
		if(Build.VERSION.SDK_INT >= 14){//4.0
			String tempStr = leftButton.getText().toString();
			int tempId = leftButton.getId();
			leftButton.setText(rightButton.getText());
			leftButton.setId(rightButton.getId());
			rightButton.setText(tempStr);
			rightButton.setId(tempId);
		}
	}
	
	private View.OnClickListener choseListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.remarks_save_btn){
				mBookDigests.setMsg(mRemarks_et.getText().toString());
				if(adapter.selectedColor != -1){
					mBookDigests.setBGColor(adapter.selectedColor);
				}
				if(mTextSelectHandler.isSelect()){
					mTextSelectHandler.setSelect(false);
				}
				mBookDigests.setDate(new Date().getTime());
				mTextSelectHandler.createOrUpdateBookDigests(mBookDigests); 
				closeDialog(true);
			}else if(id == R.id.remarks_cancel_btn){
				closeDialog(true);
			}
		}
			
	};

	@Override
	public void dismiss() {
		super.dismiss();
		NavigationBarUtil.hideNavigationBar(mContext);
	}

	public void setOnCloseDialogLisenter(OnCloseDialogLisenter onCloseDialog){
		   mOnCloseDialogLisenter = onCloseDialog;
	    }
	    private void closeDialog(boolean isCloseDialog){

	    	if(mOnCloseDialogLisenter != null){
	    		mOnCloseDialogLisenter.onCloseDialog(isCloseDialog);
	    	}
	    }
	    public interface OnCloseDialogLisenter{
	    	public void onCloseDialog(boolean isCloseDialog);
	    }
}
