package com.season.lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.season.lib.scale.ScaleView;


public class LayerImageView extends AppCompatImageView
        implements LayerInfoI, IScaleView {
    private LayerInfoI mLayerInfoI;
    private float centerX;
    private float centerY;
    public String url;

    public LayerImageView copy(){
        LayerImageView layerImageView = new LayerImageView(getContext());
        layerImageView.setBitmap(bitmap);
        layerImageView.url = url;
        layerImageView.isTuya = isTuya;
        layerImageView.filePath = filePath;
        return layerImageView;
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
        if (getParent() instanceof ScaleView){
            int width = right - left;
            int height = bottom - top;
            if (width > 0 && height > 0){
                ((ScaleView) getParent()).rebindOpView();
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

    public LayerImageView(Context context)
    {
        super(context);
        init();
    }

    public LayerImageView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init();
    }


    private void init()
    {
        mLayerInfoI = new LayerInfoImp();
    }

    @Override
    public long getViewId()
    {
        return mLayerInfoI.getViewId();
    }

    @Override
    public void setViewId(long id)
    {
        mLayerInfoI.setViewId(id);
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
        this.setImageBitmap(bitmap);
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
    public void startRecord() {

    }

    @Override
    public void recordFrame(int time) {

    }

    @Override
    public void stopRecord() {

    }

    @Override
    public int getDelay() {
        return 0;
    }


    @Override
    public void onRelease() {
        if (bitmap != null){
            if (!bitmap.isRecycled()){
                bitmap.recycle();
            }
            bitmap = null;
        }
    }

    @Override
    public boolean isSeeking() {
        return false;
    }

    @Override
    public void drawCanvas(Canvas canvas) {
        draw(canvas);
    }

}
