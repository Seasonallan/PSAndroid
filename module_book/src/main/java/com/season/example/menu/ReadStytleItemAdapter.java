package com.season.example.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.book.R;
import com.season.lib.ReadSettingThemeColor;


public class ReadStytleItemAdapter extends BaseAdapter {


        private LayoutInflater inflater;
        private int[] readStyleItems;

        public ReadStytleItemAdapter(Context context, int... readStyleItems){
            super();
            inflater = LayoutInflater.from(context);
            this.readStyleItems = readStyleItems;
        }

        @Override
        public int getCount() {
            if(readStyleItems != null){
                return readStyleItems.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if(position < getCount()){
                return readStyleItems[position];
            }
            return -1;
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
            int type = readStyleItems[position];
//            int bgRes = ReadSettingThemeColor.getThemeBGImgResThumb(type);
//            if(bgRes == -1){
//                viewHolder.contentIV.setBackgroundColor(ReadSettingThemeColor.getThemeBGColor(type));
//            }else{
//                viewHolder.contentIV.setImageResource(bgRes);
//            }
            viewHolder.contentIV.setBackgroundColor(ReadSettingThemeColor.getThemeBGColor(type));
            if(type == selectedType){
                viewHolder.selectTV.setVisibility(View.VISIBLE);
            }else{
                viewHolder.selectTV.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        private View newView(){
            return inflater.inflate(R.layout.reader_menu_theme_item, null);
        }

        public int selectedType = -1;

        private class ViewHolder {
            public ImageView contentIV;
            public ImageView selectTV;

        }
}
