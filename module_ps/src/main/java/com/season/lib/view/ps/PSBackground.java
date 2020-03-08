package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.example.ps.R;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.ToolBitmapCache;
import java.util.ArrayList;
import java.util.List;

/**
 * 背景管理
 */
public class PSBackground {

    protected Context context;
    public ImageView picture;
    public View bgView;
    public BgOperate currentOperate;
    int position = -1;
    List<BgOperate> list = new ArrayList<>();

    public PSBackground(View parentView) {
        this.context = parentView.getContext();
        picture = parentView.findViewById(R.id.picture);
        bgView = parentView.findViewById(R.id.bg_view);
        if (bgView != null) bgView.setBackgroundColor(Color.TRANSPARENT);
        init();
    }


    public boolean isBackgroundVideoImageViewVisible() {
        return false;
    }

    public boolean isBackgroundImageViewVisible() {
        return picture.getVisibility() == View.VISIBLE;
    }

    public void release() {
        if (list.size() > 0) {
            for (BgOperate op : list) {
                BitmapUtil.recycleBitmaps(op.bitmap);
            }
            list.clear();
        }
        ToolBitmapCache.getDefault().release();
        bgView = null;
        picture = null;
        currentOperate = null;
    }

    private void init() {
        BgOperate op = new BgOperate(View.VISIBLE, View.GONE,  Color.TRANSPARENT, null);
        reset(op);
        addEvent(op);
    }


    public boolean showImage(final String url, String path) {
        if (currentOperate != null) {
            if (currentOperate.visible2 == View.VISIBLE && path.equals(currentOperate.imageFile)) {
                return false;
            }
        }
        BgOperate op = new BgOperate(url, path);
        reset(op);
        addEvent(op);
        return true;
    }

    public boolean showBackground(int color) {
        if (currentOperate != null) {
            if (currentOperate.visible1 == View.VISIBLE && color == currentOperate.color) {
                return false;
            }
        }
        BgOperate op = new BgOperate(color);
        reset(op);
        addEvent(op);
        return true;
    }

    public boolean showVideoOrImage() {
        if (currentOperate != null) {
            if (currentOperate.visible1 == View.VISIBLE) {
                for (int i = position - 1; i >= 0; i--) {
                    BgOperate operate = list.get(i);
                    if (operate.visible1 == View.GONE) {
                        reset(operate);
                        addEvent(operate);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean reset(final BgOperate op) {
        currentOperate = op;

        if (picture != null) {
            picture.post(new Runnable() {
                @Override
                public void run() {
                    if (picture != null)
                        picture.setVisibility(op.visible2);
                }
            });
        }
        if (bgView != null) {
            bgView.post(new Runnable() {
                @Override
                public void run() {
                    if (bgView != null)
                    bgView.setVisibility(op.visible1);
                }
            });
        }
        if (op.visible1 == View.VISIBLE) if (bgView != null) {
            bgView.post(new Runnable() {
                @Override
                public void run() {
                    if (bgView != null)
                    bgView.setBackgroundColor(op.color);
                }
            });
        }
        if (!TextUtils.isEmpty(op.imageFile) && op.visible2 == View.VISIBLE) {
            if (op.bitmap != null && !op.bitmap.isRecycled()) {
            } else {
                currentOperate.bitmap = BitmapUtil.centerCropBitmap(BitmapFactory.decodeFile(op.imageFile));
            }
            if (picture != null) {
                picture.post(new Runnable() {
                    @Override
                    public void run() {
                        if (picture != null)
                        picture.setImageBitmap(currentOperate.bitmap);
                    }
                });
            }
        }
        return true;
    }

    public void reset() {
        BgOperate op = new BgOperate(View.VISIBLE,  View.GONE, Color.TRANSPARENT, null);
        reset(op);
        addEvent(op);
    }

    public static class BgOperate {
        //visible1 表示选了颜色背景，2代表图片
        public int visible1 = View.GONE, visible2 = View.GONE;
        public int color = -1;
        public String imageFile;
        public String url;
        public Bitmap bitmap;

        public BgOperate(int visible1, int visible2, int color, String imageFile) {
            this.visible1 = visible1;
            this.visible2 = visible2;
            this.color = color;
            this.imageFile = imageFile;
        }

        public BgOperate(String url, String imagePath) {
            this.url = url;
            if (!TextUtils.isEmpty(imagePath)) {
                this.visible2 = View.VISIBLE;
                this.imageFile = imagePath;
            }
        }

        public BgOperate(int color) {
            this.visible1 = View.VISIBLE;
            this.color = color;
        }
    }


    private void addEvent(BgOperate operate) {
        if (position < list.size() - 1) {
            for (int i = list.size() - 1; i > position; i--) {
                list.remove(i);
            }
        }
        list.add(operate);
        position = list.size() - 1;
    }

    public boolean pre() {
        position--;
        if (position < 0) {
            position = 0;
        }
        BgOperate op = list.get(position);
        return reset(op);
    }

    public boolean pro() {
        position++;
        if (position > list.size() - 1) {
            position = list.size() - 1;
        }
        BgOperate op = list.get(position);
        return reset(op);
    }
}
