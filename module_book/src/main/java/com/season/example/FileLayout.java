package com.season.example;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.season.book.R;

/**
 * @author laijp
 * @date 2014-1-7
 * @email 451360508@qq.com
 */
public class FileLayout extends BaseLayout{

	public FileLayout(ViewStub viewStub) {
		super(viewStub);
	}

	ListView listView;
	@Override
	protected View getView(Activity activity) {
		View view = activity.findViewById(R.id.file_main);
		listView = view.findViewById(R.id.file_lv);

		String[] files = activity.getCacheDir().list();
		listView.setAdapter(new ListAdapter(files));
		return view;
	}

	public class ListAdapter extends BaseAdapter {

		String[] items;
		public ListAdapter(String[] input){
			this.items = input;
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = parent.inflate(parent.getContext(), R.layout.item_file, null);
			TextView textView = view.findViewById(R.id.item_tv);
			textView.setText(items[position]);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					addFile(items[position]);
				}
			});
			return view;
		}
	}

	@Override
	protected void onStatusChange(int visible) {

	}

	protected void addFile(String filePath){

	}


}