package com.example.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.lib.bitmap.BitmapUtil;
import com.example.lib.file.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Disc: 涂鸦
 * 几乎没怎么动过
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class CustomCanvas extends View{

    private DrawPath currentPath;
    private List<DrawPath> savePath;
    private List<DrawPath> savePath2;

    public Paint markPaint;
    public Paint paint = null;
    public Paint whitepaint = new Paint();
    private int view_height;
    private int view_width;
    private float preX;
    private float preY;
    private float[] points = new float[]{-1, -1, -1, -1};//用于裁剪bitmap用的点
    private float defaultPaintWidth = 10;//画笔粗细
    private int paintColor = Color.BLACK; // 画笔底色
    private final int isNormalColor = 1;//涂鸦式
    private final int isBitmap = 2;//橡皮擦模式
    private final int isHightLightPaint = 3;//荧光／笔模式
    private final int isEraser = 4;//橡皮擦模式
    private final int isMask = 5;//马赛克模式
    private int currentmode = isNormalColor;
    private Shader.TileMode mTileX = Shader.TileMode.REPEAT;
    private Shader.TileMode mTileY = Shader.TileMode.REPEAT;  // 镜像
    private Bitmap shaderbitmap;


    public CustomCanvas(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        init(context);
    }

    public CustomCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        init(context);
    }

    public void init(Context context) {

        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setStyle(Paint.Style.STROKE);
        markPaint.setAntiAlias(true);
        markPaint.setStrokeJoin(Paint.Join.ROUND);
        markPaint.setStrokeCap(Paint.Cap.ROUND);
        markPaint.setPathEffect(new CornerPathEffect(10));
        markPaint.setColor(Color.BLUE);

        view_height = context.getResources().getDisplayMetrics().heightPixels;
        view_width = context.getResources().getDisplayMetrics().widthPixels;
        savePath = new ArrayList<>();
        savePath2 = new ArrayList<>();

        paint = new Paint(Paint.DITHER_FLAG);
        paint.setColor(paintColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(defaultPaintWidth);
        paint.setAntiAlias(true);
        paint.setDither(true);
        whitepaint.setStyle(Paint.Style.STROKE);
        whitepaint.setStrokeJoin(Paint.Join.ROUND);
        whitepaint.setStrokeCap(Paint.Cap.ROUND);
        whitepaint.setStrokeWidth(defaultPaintWidth);
        whitepaint.setAntiAlias(true);
        whitepaint.setDither(true);
        whitepaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DrawPath path : savePath) {
            drawPath(canvas, path);
        }
        if (currentPath != null) {
            if (savePath.size() == 0) { //只有橡皮擦则不绘制
                if (currentPath.mode == isEraser) {
                    return;
                }
            }
            drawPath(canvas, currentPath);
        }
    }

    Bitmap bmTouchLayer, mosaicLayer;
    private void drawPath(Canvas canvas, DrawPath path) {
        if (path.mode == isMask) {
            if (mosaicLayer != null && !mosaicLayer.isRecycled()) {
                mosaicLayer.recycle();
            }
            mosaicLayer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas mosaicCanvas = new Canvas(mosaicLayer);

            if (bmTouchLayer != null && !bmTouchLayer.isRecycled()) {
                bmTouchLayer.recycle();
            }
            bmTouchLayer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas touchCanvas = new Canvas(bmTouchLayer);
            touchCanvas.drawPath(path.path, path.markPaint);

            mosaicCanvas.drawBitmap(path.bitmap, 0, 0, null);
            mosaicCanvas.drawBitmap(bmTouchLayer, 0, 0, path.paint);

            canvas.drawBitmap(mosaicLayer, 0, 0, null);
        } else {
            canvas.drawPath(path.path, path.paint);
            if (path.mode == isHightLightPaint) {
                canvas.drawPath(path.path, path.whitePaint);
            }
        }
    }

    String getColorStatus = null;
    Bitmap tBitmap;

    public void enableGetColor(String type, Bitmap videoBitmap, View parent, OnColorGetListener listener) {
        if (type == null) {
            getColorStatus = null;
            return;
        }
        tBitmap = videoBitmap;
        parent.setDrawingCacheEnabled(true);
        Bitmap screenCache = parent.getDrawingCache();
//        if (BuildConfig.DEBUG){
//            Logger.d("enableGetColor:videoBitmap是否为NULL:"+(videoBitmap==null)+",screenCache是否为null:"+(screenCache==null));
//        }
        if (videoBitmap != null){
            tBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tBitmap);
            if (videoBitmap!=null)
            canvas.drawBitmap(videoBitmap, 0, 0, null);
            if (screenCache!=null)
            canvas.drawBitmap(screenCache, 0, 0, null);
        }else{
            tBitmap = Bitmap.createBitmap(screenCache);
        }
//        parent.setDrawingCacheEnabled(false);
        getColorStatus = type;
        mOnColorGetListener = listener;
    }

    private OnColorGetListener mOnColorGetListener;


    public interface OnColorGetListener {
        void onColorGet(String param, int color);
    }


    private boolean enable = false;

    public void changeStatus(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 记录下边界
         */
        float x = event.getX();
        float y = event.getY();
        if (!TextUtils.isEmpty(getColorStatus)) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                int color = 0xffffff;
                try {
                    if (tBitmap != null) {
                        color = tBitmap.getPixel((int) x, (int) y);
                        tBitmap.recycle();
                        tBitmap = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mOnColorGetListener != null) {
                    mOnColorGetListener.onColorGet(getColorStatus, color);
                }
                getColorStatus = null;
            }
            return true;
        }

        if (enable) {
            if (y < 0 || y > getHeight()){
                //return true;
            }else{
                if (points[0] == -1) {
                    points[0] = x;
                    points[1] = x;
                    points[2] = y;
                    points[3] = y;
                } else {
                    if (x < points[0]) {
                        points[0] = x;
                    }
                    if (x > points[1]) {
                        points[1] = x;
                    }
                    if (y < points[2]) {
                        points[2] = y;
                    }
                    if (y > points[3]) {
                        points[3] = y;
                    }
                }
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentPath = new DrawPath(currentmode);
                    currentPath.setPaint(paint);
                    currentPath.setWhitePaint(whitepaint);
                    currentPath.setMarkPaint(markPaint);
                    currentPath.setBitmap(maskBitmap);

                    currentPath.path.moveTo(x, y);
                    preX = x;
                    preY = y;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(x - preX);
                    float dy = Math.abs(y - preY);
                    if (dx >= 5 || dy >= 5) {
                        currentPath.path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);//用于绘制圆滑曲线，即贝塞尔曲线。
                        preX = x;
                        preY = y;
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    currentPath.path.lineTo(x, y);
                    savePath.add(currentPath);
                    savePath2.clear();
                    currentPath = null;
                    invalidate();
                    if (callback != null) {
                        callback.onClick(this);
                    }
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    private OnClickListener callback;

    public void setCallback(OnClickListener listener) {
        this.callback = listener;
    }

    private void resetPaintParams() {
        markPaint.setStrokeWidth(defaultPaintWidth);
        paint.setStrokeWidth(defaultPaintWidth);
        whitepaint.setStrokeWidth(defaultPaintWidth);
        switch (currentmode) {
            case isNormalColor:
                paint.setColor(paintColor);
                paint.setXfermode(null);
                paint.setMaskFilter(null);
                paint.setShader(null);
                break;
            case isBitmap:
                BitmapShader bitmapShader = new BitmapShader(shaderbitmap, mTileX, mTileY);
                paint.setShader(bitmapShader);
                paint.setXfermode(null);
                paint.setMaskFilter(null);
                break;
            case isHightLightPaint:
                paint.setColor(paintColor);
                paint.setMaskFilter(new BlurMaskFilter(defaultPaintWidth * 10 / 9, BlurMaskFilter.Blur.SOLID));
                paint.setXfermode(null);
                paint.setShader(null);
                break;
            case isEraser:
                paint.setShader(null);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setMaskFilter(null);
                break;
            case isMask:
                paint.setShader(null);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                paint.setMaskFilter(null);
                break;
        }
    }

    private Paint copyPaint(Paint p) {
        Paint temp = new Paint(Paint.DITHER_FLAG);
        temp.setColor(p.getColor());
        temp.setStrokeWidth(p.getStrokeWidth());
        temp.setStyle(Paint.Style.STROKE);
        temp.setStrokeJoin(Paint.Join.ROUND);
        temp.setStrokeCap(Paint.Cap.ROUND);
        temp.setAntiAlias(true);
        temp.setDither(true);
        return temp;
    }

    //擦除
    public void erasure() {
        paint.setAntiAlias(true);
        paint.setDither(true);
        // paint.setTextcolor(Color.RED);
        //paint.setStrokeWidthByPercent(100);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    //bitmap
    public String getCacheFilePath() throws IOException {
        if (savePath.size() <= 0) {
            return null;
        }
        int padding = 16;
        /**
         * 切掉没有画笔的部分
         */
        //        if (points[0]!=-1){
        //
        //        }
        int x = (int) (points[0] - padding);//0,1是x;2,3是y
        int y = (int) (points[2] - padding);
        int width = (int) ((points[1] - points[0]) + padding + padding + (int) defaultPaintWidth / 2);

        int height = ((int) (points[3] - points[2]) + padding + padding + (int) defaultPaintWidth / 2);
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        Bitmap cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        //                    cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        Canvas cacheCanvas = new Canvas();
        cacheCanvas.setBitmap(cacheBitmap);
        draw(cacheCanvas);

        if (x + width > cacheBitmap.getWidth()) {
            width = cacheBitmap.getWidth() - x;
        }
        if (height + y > cacheBitmap.getHeight()) {
            height = cacheBitmap.getHeight() - y;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap, x, y, width, height);
        File file = FileManager.getPsFile(null, "png");
        if (file == null){
            return null;
        }
        String tuya1url = BitmapUtil.saveBitmap(file, bitmap);
        return tuya1url;
    }

    public float[] getBitmapcenter() {
        return points;
    }


    public boolean canUndo() {
        return savePath != null && savePath.size() > 0;
    }

    public boolean canRedo() {
        return savePath2 != null && savePath2.size() > 0;
    }

    /**
     * 撤销
     * 核心思想就是将画布清空
     * 将保存下来的Path路径最后一个移除掉
     * 重新将路径画在画布上面。
     */
    public void undo() {
        if (savePath != null && savePath.size() > 0) {
            // 移除最后一个path,相当于出栈操作
            savePath2.add(savePath.remove(savePath.size() - 1));
        }
        invalidate();// 刷新
    }

    //重做
    public void redo() {
        if (savePath2 != null && savePath2.size() > 0) {
            // 移除最后一个path,相当于出栈操作
            savePath.add(savePath2.remove(savePath2.size() - 1));
        }
        invalidate();// 刷新
    }

    //清空
    public void clear() {
        savePath.clear();
        savePath2.clear();

        points = new float[]{-1, -1, -1, -1};//用于裁剪bitmap用的点
        invalidate();
    }

    /**
     * 设置画笔粗细
     */
    public void setPaintSize(float width) {
        defaultPaintWidth = width * 2;
        paint.setStrokeWidth(defaultPaintWidth);
        markPaint.setStrokeWidth(defaultPaintWidth);
        whitepaint.setStrokeWidth(defaultPaintWidth);
    }

    public void paintMode() {
        if (paint == null)
            return;
        paint.setXfermode(null);
    }

    //    public GraffitiColor getColor()
    //    {
    //        return mColor;
    //    }

    /**
     * 设置画笔底色
     *
     * @param color
     */
    public void setColor(int color, boolean isHightLightornot) {
        paintColor = color;
        if (isHightLightornot) {
            currentmode = isHightLightPaint;
        } else {
            currentmode = isNormalColor;
        }
        resetPaintParams();
    }


    /**
     * 设置图片刷
     *
     * @param bitmap
     */
    public void setColor(Bitmap bitmap) {

        currentmode = isBitmap;
        shaderbitmap = bitmap;
        resetPaintParams();
    }

    public void setEraserMode() {
        currentmode = isEraser;
        resetPaintParams();
    }

    private Bitmap maskBitmap;

    public void setMosaic(Bitmap bitmap) {
        currentmode = isMask;
        maskBitmap = bitmap;
        resetPaintParams();
    }


    private class DrawPath {
        public int mode;
        private Path path;
        private Paint paint;
        private Paint whitePaint;
        private Paint markPaint;
        private Bitmap bitmap;

        DrawPath(int mode) {
            this.mode = mode;
            this.path = new Path();
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public void setPaint(Paint paint) {
            this.paint = new Paint();
            this.paint.set(paint);
        }

        public void setWhitePaint(Paint whitePaint) {
            this.whitePaint = new Paint();
            this.whitePaint.set(whitePaint);
        }

        public void setMarkPaint(Paint markPaint) {
            this.markPaint = new Paint();
            this.markPaint.set(markPaint);
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
