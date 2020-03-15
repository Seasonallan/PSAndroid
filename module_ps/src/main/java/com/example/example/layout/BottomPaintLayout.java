package com.example.example.layout;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.ps.R;
import com.example.lib.view.VerticalSeekBar;


public abstract class BottomPaintLayout extends BaseBottomView{

    @Override
    protected int getContentId() {
        return R.id.layout_paint;
    }


    private LinearLayout topLinear, bottomLinear;
    public BottomPaintLayout(Activity activity) {
        super(activity);

        findView(R.id.paint_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                onClose();
            }
        });
        topLinear = findView(R.id.paint_l1);
        bottomLinear =  findView(R.id.paint_l2);

        ((VerticalSeekBar)(findView(R.id.paint_sb))).setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int progress) {
                float i;
                if (progress < 50) {
                    i = (4 / 9.5f) + (((1 - (4 / 9.5f)) / 50) * progress);
                } else if (progress == 50) {
                    i = 1;
                } else {
                    i = 1 + ((((15 / 9.5f) - 1) / 50) * (progress - 50));
                }
                float size = (float) (3 + 15 * (progress / 100.0));
                onPaintSizeChange(size);
            }
        });

        addItem(topLinear, R.mipmap.icon_color_black);
        addItem(topLinear, R.mipmap.icon_color_white);
        addItem(topLinear, R.mipmap.icon_bic1);
        addItem(topLinear, R.mipmap.icon_bic2);
        addItem(topLinear, R.mipmap.icon_bic3);

        addItem(bottomLinear, R.mipmap.icon_bit1);
        addItem(bottomLinear, R.mipmap.icon_bit2);
        addItem(bottomLinear, R.mipmap.icon_bit3);
        addItem(bottomLinear, R.mipmap.icon_mosaic);
        addItem(bottomLinear, R.mipmap.icon_clear);
    }
    ImageView lastGouImageView;
    private void addItem(LinearLayout linearLayout, int drawableId){
        RelativeLayout relativeLayout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        itemParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ImageView imageView = new ImageView(activity);
        imageView.setImageResource(drawableId);
        relativeLayout.addView(imageView, itemParams);

        final ImageView gouImageView = new ImageView(activity);
        gouImageView.setImageResource(R.mipmap.icon_selected);
        gouImageView.setPadding(12,12,12,12);
        gouImageView.setVisibility(View.GONE);
        relativeLayout.addView(gouImageView, itemParams);

        if (lastGouImageView==null){
            lastGouImageView = gouImageView;
            gouImageView.setVisibility(View.VISIBLE);
        }

        relativeLayout.setTag(drawableId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        relativeLayout.setPadding(6,6,6,6);
        linearLayout.addView(relativeLayout, params);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastGouImageView != null){
                    lastGouImageView.setVisibility(View.GONE);
                }
                gouImageView.setVisibility(View.VISIBLE);
                lastGouImageView = gouImageView;
                int id = (int) v.getTag();
                if (id == R.mipmap.icon_color_black){
                    onColorChange(0xff000000);
                }else if(id == R.mipmap.icon_color_white){
                    onColorChange(0xffffffff);
                }else if(id == R.mipmap.icon_clear){
                    onClear();
                }else if(id == R.mipmap.icon_mosaic){
                    onMosaic();
                }else if(id == R.mipmap.icon_bic1){
                    onColorShape(0xff36e4da);
                }else if(id == R.mipmap.icon_bic2){
                    onColorShape(0xffffd325);
                }else if(id == R.mipmap.icon_bic3){
                    onColorShape(0xffff36c4);
                }else if(id == R.mipmap.icon_bit1){
                    onBitmapMode(R.mipmap.paint_dot_new);
                }else if(id == R.mipmap.icon_bit2){
                    onBitmapMode(R.mipmap.paint_xiexian_new);
                }else if(id == R.mipmap.icon_bit3){
                    onBitmapMode(R.mipmap.paint_fengye);
                }else if(id == R.mipmap.icon_bic3){
                    onColorShape(0xffff36c4);
                }
            }
        });
    }

    public abstract void onClose();
    public abstract void onColorChange(int color);
    public abstract void onClear();
    public abstract void onMosaic();
    public abstract void onBitmapMode(int drawableId);
    public abstract void onColorShape(int color);
    public abstract void onPaintSizeChange(float size);

}
