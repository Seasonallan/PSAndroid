package com.season.example.menu;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.example.book.R;


/**
 * @author mingkg21
 * @email mingkg21@gmail.com
 * @date 2011-11-2
 */
public class MenuItemAdapter extends BaseAdapter {

	public static class MenuItem {
		public static final int MENU_ITEM_ID_CATALOG = 1;
		public static final int MENU_ITEM_ID_FONT = 2;
		public static final int MENU_ITEM_ID_THEME = 3;
		public static final int MENU_ITEM_ID_BRIGHTNESS = 4;
		public static final int MENU_ITEM_ID_SETTING = 5;
		public static final int MENU_ITEM_ID_MORE = 6;

		public int id;
		public int iconResId;
		public String name;

		public MenuItem(int id, int iconResId, String name){
			this.id = id;
			this.iconResId = iconResId;
			this.name = name;
		}

	}

	private LayoutInflater inflater;
	private ArrayList<MenuItem> menuItems;
	
	public MenuItemAdapter(Context context, ArrayList<MenuItem> menuItems){
		super();
		inflater = LayoutInflater.from(context);
		this.menuItems = menuItems;
	}

	@Override
	public int getCount() {
		if(menuItems != null){
			return menuItems.size();
		}
		return 0;
	}

	@Override
	public MenuItem getItem(int position) {
		if(position < getCount()){
			return menuItems.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = newView();
			viewHolder = new ViewHolder();
			viewHolder.imageView = (CheckedTextView) convertView.findViewById(R.id.menu_icon);
			viewHolder.textView = (TextView) convertView.findViewById(R.id.menu_name);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		MenuItem item = getItem(position);
		viewHolder.imageView.setBackgroundResource(item.iconResId);
		viewHolder.textView.setText(item.name);
		return convertView;
	}
	
	private View newView(){
		return inflater.inflate(R.layout.menu_tab_item, null);
	}
	
	private class ViewHolder {
		
		public CheckedTextView imageView;
		public TextView textView;
		
	}

}
