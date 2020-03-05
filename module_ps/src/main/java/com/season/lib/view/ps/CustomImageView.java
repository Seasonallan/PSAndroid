package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;


public class CustomImageView extends CustomBaseView{
    private float centerX;
    private float centerY;
    public String url;

    public CustomImageView copy(){
        CustomImageView customImageView = new CustomImageView(getContext());
        customImageView.setBitmap(bitmap);
        customImageView.url = url;
        customImageView.isTuya = isTuya;
        customImageView.filePath = filePath;
        return customImageView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (bitmap != null){
            setMeasuredDimension(bitmap.getWidth(), bitmap.getHeight());
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getParent() instanceof PSLayer){
            int width = right - left;
            int height = bottom - top;
            if (width > 0 && height > 0){
                ((PSLayer) getParent()).rebindOpView();
            }
        }
    }

    public boolean changeCenter = false;
    public void setCenterXY(float x, float y)
    {
        this.changeCenter = true;
        this.centerX = x;
        this.centerY = y;
    }

    public float getCenterX()
    {
        return centerX;
    }

    public float getCenterY()
    {
        return centerY;
    }

    public CustomImageView(Context context)
    {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    private Bitmap bitmap;

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public String filePath;
    public void setImageFile(String filePath) {
        this.filePath = filePath;
        setBitmap(BitmapFactory.decodeFile(filePath));
    }


    public boolean isTuya = false;
    private void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

    @Override
    public int getViewWidth() {
        if (bitmap != null){
            return bitmap.getWidth();
        }
        return 0;
    }

    @Override
    public int getViewHeight() {
        if (bitmap != null){
            return bitmap.getHeight();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }


    @Override
    public int getDelay() {
        return 0;
    }


    @Override
    public void onRelease() {
        super.onRelease();
        if (bitmap != null){
            if (!bitmap.isRecycled()){
                bitmap.recycle();
            }
            bitmap = null;
        }
    }

    @Override
    public void drawCanvasTime(Canvas canvas, int time) {
        if (bitmap != null && !bitmap.isRecycled())
            canvas.drawBitmap(bitmap, 0, 0, null);
    }

}
