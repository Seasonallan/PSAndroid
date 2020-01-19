/*
 *          Copyright (C) 2016 jarlen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.season.lib.bean.LayerItem;
import com.season.lib.util.Util;
import com.season.lib.animation.AnimationProvider;
import com.season.lib.util.FileManager;
import com.season.lib.util.ScreenUtils;
import com.season.lib.util.ToolPaint;
import com.season.lib.util.Logger;
import com.season.lib.util.AutoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconHandler;

/**
 * Disc: 文字图层，流程为1、measure>> text改变后getEmojis获取到每个字或表情的位置后calculateWidthHeight()获取宽高 后
 * 2、layout >> 用于控制添加文字的位置和文字改变后的位置矫正(通过位移达到中心点不变)
 * 3  draw   >> 绘制每个字或表情，有动画的时候进行画布矩阵调整
 * 注：动效的刷新原理都是外部ContainerView进行的统一刷新，线程循环调用invalidate，然后执行的TextStyleView的onDraw，
 * TextStyleView通过记录开始时间和当前onDraw时间获取每个字的位置进行绘制，达成动画效果
 * 所以TextStyleView单独提出去用的时候动效无法使用，需要配合ContainerView刷新或者自己在TextStyleView写刷新线程
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class CustomTextView extends View implements ILayer {
    private int offsetY;//由于Android系统的drawText中的y无法确定，这个值是调节的比较居中的值
    private boolean nullInput = false;
    public boolean isAudio = false;
    private boolean isDiyBottom = false;//在制作界面，如果有背景就局下，否则居中
    private boolean isPreViewBottom = false;//在预览界面界面
    //背景图片
    private int backgroudRes = 0;
    private int preBackgroundInfo;
    private Bitmap backgroundBitmap;

    private String text;
    private Context context;
    public Paint paint = new Paint();
    public Paint strokepaint = new Paint();
    public String fontName = "";
    private int paddingLeft, paddingTop, textSpacing, lineSpacing;
    private int emojiWidth = 100;

    private String[] ids = {"text_style_0", "text_style_1"};
    private String startColorStr;
    private String endColorStr;
    private float defaul_textColorSize=0.1f;
    private float HeightPercent=3f/4;
    private int offsetY4Diy;

    public boolean isDiyBottom() {
        return isDiyBottom;
    }

    public void setisDiyBottom(boolean diyBottom) {
        isDiyBottom = diyBottom;
    }
    public void setisDiyBottomHeightPercent(float HeightPercent) {
        this.HeightPercent = HeightPercent;
    }
    public float getisDiyBottomHeightPercent() {
        return HeightPercent;
    }

    public boolean isPreViewBottom() {
        return isPreViewBottom;
    }

    public void setPreViewBottom(boolean preViewBottom) {
        isPreViewBottom = preViewBottom;
    }

    public void setFontHistory() {

    }

    public CustomTextView copy() {
        CustomTextView customTextView = new CustomTextView(context);
        customTextView.paint = new Paint();
        customTextView.paint.set(paint);
        customTextView.strokepaint = new Paint();
        customTextView.strokepaint.set(strokepaint);
        customTextView.backgroudRes = backgroudRes;
        customTextView.fontName = fontName;
        customTextView.text = text;
        customTextView.startColorStr=startColorStr;
        customTextView.endColorStr=endColorStr;
        customTextView.fixEmoji();
        customTextView.calculateWidthHeight();
        customTextView.setTextAnimationType(currentType, duration, delay, speed);
        customTextView.resetAnimationPaint();
        customTextView.addEvent();
        return customTextView;
    }


    public boolean setTextEntry(LayerItem item, float width) {
        int opViewWidth = ScreenUtils.getScreenWidth(getContext());
        //TODO 如果需要对旧版本单行超过8字符文字进行8个字符换行，在这里修改。
        this.text = item.getText();
        fixEmoji();
        this.fontName = item.getTextFontName();

        paint.setTypeface(getTypeface(Typeface.DEFAULT));
        strokepaint.setTypeface(getTypeface(Typeface.DEFAULT));


        float maxLength = (float) (item.getSizeWidth() * item.getXScale() * opViewWidth / width);
        calculateWidthHeight();

        item.setXScale((maxLength / finalWidth) * width / opViewWidth);
        item.setYScale((maxLength / finalWidth) * width / opViewWidth);
        item.setSizeWidth(finalWidth);
        item.setSizeHeight(finalHeight);

        addEvent();
        return true;
    }


    /**
     * 在设置字体粗细的时候，描边的粗细要跟着变
     * 最小值
     */
    public boolean setPaintWidthByPercent(float paintwidthPercent) {
        float paintSizeParams = paintwidthPercent / 100;
        float oriParam = getPaintStrokeWidthParam();
        float newPaintWidth = getPaintStrokeWidth(paintSizeParams);
        float pw = paint.getStrokeWidth();
        if (pw == newPaintWidth) {
            return false;
        }
        paint.setStrokeWidth(newPaintWidth);
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(paintSizeParams, getStrokePaintStrokeWidthParam(oriParam)));
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setStrokeWidthByPercent(float strokeWidthPercent) {
        float newParams = strokeWidthPercent / 100;
        float paintParams = getPaintStrokeWidthParam();
        float oldParams = getStrokePaintStrokeWidthParam(paintParams);
        if (newParams == oldParams) {
            return false;
        }
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(paintParams, newParams));
        resetAnimationPaint();
        invalidate();
        return false;
    }


    //外部描边百分比得到描边宽度
    private float getStrokePaintStrokeWidth(double paintSizeParams, double strokePaintSizeParams) {
        return (float) (strokePaintSizeParams * ToolPaint.getDefault().getPaintWidth(getContext()) + getPaintStrokeWidth(paintSizeParams));
    }

    //描边宽度得到外部描边百分比
    public float getStrokePaintStrokeWidthParam(float paintSizeParams) {
        return (strokepaint.getStrokeWidth() - getPaintStrokeWidth(paintSizeParams)) / ToolPaint.getDefault().getPaintWidth(getContext());
    }

    //内部描边百分比得到描边宽度
    private float getPaintStrokeWidth(double params) {
        return (float) (params * ToolPaint.getDefault().getStrokeWidth());
    }

    //描边宽度得到内部描边百分比
    public float getPaintStrokeWidthParam() {
        return paint.getStrokeWidth() / ToolPaint.getDefault().getStrokeWidth();
    }


    public CustomTextView(Context context) {
        super(context);
        init(context);
    }

    public CustomTextView(Context context, String fontName) {
        super(context);
        this.fontName = fontName;
        init(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    long drawingCacheSize;
    String tag = "textstyleview:";

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        drawingCacheSize = ViewConfiguration.get(context).getScaledMaximumDrawingCacheSize();
        Logger.d(tag + drawingCacheSize);//onePlus:8294400;honor:3686400
        textSpacing = AutoUtils.getPercentWidthSize(4);
        lineSpacing = AutoUtils.getPercentWidthSize(16);
        paddingLeft = AutoUtils.getPercentWidthSize(24);
        paddingTop = AutoUtils.getPercentWidthSize(24);
        offsetY = AutoUtils.getPercentWidthSize(10);

        this.context = context;
        paint.setDither(true);//防抖
        paint.setAntiAlias(true);
        paint.setTypeface(getTypeface(Typeface.DEFAULT));
        paint.setColor(Color.WHITE);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(getPaintStrokeWidth(defaul_textColorSize));
        paint.setAlpha(255);
        paint.setShader(null);
        //文字居中写,因为现在是的canva直接在具体的x,y坐标下点，进行居中绘制
        paint.setTextAlign(Paint.Align.CENTER);
        strokepaint.setTextAlign(Paint.Align.CENTER);

        strokepaint.setDither(true);
        strokepaint.setAntiAlias(true);
        strokepaint.setTypeface(getTypeface(Typeface.DEFAULT));
        strokepaint.setColor(Color.BLACK);

        strokepaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.2, 0.4));
        strokepaint.setAlpha(255);
        paint.setShader(null);

        //当前做法是字体尽量设计大一点，然后通过scaleView对其进行缩小，以达成放大文字不会锯齿的效果
        paint.setTextSize(ToolPaint.getDefault().getPaintSize(getContext()));
        strokepaint.setTextSize(ToolPaint.getDefault().getPaintSize(getContext()));
    }

    //获取最大放大倍数，超过的话文字会显示锯齿
    public float getMaxScale() {
        if (paint == null) {
            paint = new Paint();
            paint.setTextSize(ToolPaint.getDefault().getPaintSize(getContext()));
        }
        int width = (int) paint.measureText("情");
        return ScreenUtils.getScreenWidth(getContext()) / 2 * 1.0f / width;
    }

    private int finalWidth = 0, finalHeight = 0, lineHeight = 1;
    int row = 0;
    int xoffsetCenter = 0;

    /**
     * 计算每个字的长度，最后算出要显示的宽高
     */
    //为了做行动画，我们先可以在这里确定每一个字符的行数。
    //先确认"\n"字符是否占位置，我们先标记出换行符的位置，然后判断位置，确定每一个字符是第几行
    private void calculateWidthHeight() {
        if (textEmojiList == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        emojiWidth = (int) paint.measureText("情");
        finalWidth = 0;
        row = 0;//emoji的行号，从0开始
        int offsetX = paddingLeft;
        //******为了让文字居中start
        xoffsetCenter = 0;
        String longestRow = "";
        //int longestCount = 0;
        int widest = 0;
        int currentRowleng = 0;
        int offset = 0;
        //外部判断了,text一定不为null
        String[] split = text.split("\n");
        if (split != null && split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                if (!TextUtils.isEmpty(split[i])) {
                    //emoji算一个字符
                    List<EmojiconHandler.TextEmoji> longestList = EmojiconHandler.getEmojis(context, longestRow);
                    List<EmojiconHandler.TextEmoji> currentlist = EmojiconHandler.getEmojis(context, split[i]);
                    if (longestList.size() < currentlist.size()) {
                        longestRow = split[i];
                    }
                }
            }
            //数字和文字的大小不同
//           widest = longestRow.length() * (emojiWidth + textSpacing);//固定
            widest = (int) paint.measureText(longestRow) + 1;//固定
            if (longestRow.length() > 0) {
                widest += (longestRow.length() - 1) * textSpacing;
            }
            String currentrow = split[row];
            if (!TextUtils.isEmpty(currentrow)) {
//                currentRowleng = currentrow.length() * (emojiWidth + textSpacing);
                currentRowleng = (int) paint.measureText(currentrow);
                if (currentrow.length() > 0) {
                    offset = (currentrow.length() - 1) * textSpacing;
                    currentRowleng += offset;
                }
                if (widest >= currentRowleng) {
                    xoffsetCenter = (widest - currentRowleng) / 2;
                    offsetX += xoffsetCenter;//校正居中偏移量
                }
            }
        }
        Logger.d("第0行：" + xoffsetCenter + "，widest：" + widest + "，currentRowleng:" + currentRowleng + ",offset:" + offset);
        //******为了让文字居中end
        finalHeight = paddingTop;
        int size = textEmojiList.size();
        lineHeight = paddingTop + emojiWidth + paddingTop + lineSpacing;
        for (int i = 0; i < size; i++) {
            EmojiconHandler.TextEmoji emoji = textEmojiList.get(i);
            if (emoji.icon != null) {
                //emoji
                emoji.row = row;//设置行
                emoji.offsetX = offsetX;
                emoji.offsetY = finalHeight;
                emoji.setSize(emojiWidth);
                offsetX += (emoji.width + textSpacing);//x坐标加一个字符宽度和文件间隙
                //这里做自动换行
                if (offsetX >= ToolPaint.getDefault().getMaxTextLength(getContext()) && i < size - 1) {//fix problem: OpenGLRenderer: Bitmap too
                    // large to be uploaded into a texture 宽度太大无法绘制问题
                    EmojiconHandler.TextEmoji emojiNext = textEmojiList.get(i + 1);
                    if (emojiNext.text == null || !emojiNext.text.equals("\n")) {
                        EmojiconHandler.TextEmoji emojiEnter = new EmojiconHandler.TextEmoji();
                        emojiEnter.text = "\n";
                        textEmojiList.add(i + 1, emojiEnter);
                        size++;
                    }
                }
            } else {
                //文字
                String itemText = emoji.text;
                if (itemText.equals("\n")) {
                    //加一个判断 if (split.length <= row)
                    //如果最后一行是空换行，可能就下标越界了，for example:啊哈哈\n哈哈\n 实际上我们拆分==>用"\n"来拆分{啊哈哈,n哈哈}
                    row++;
                    if (split.length - 1 >= row) {
                        finalWidth = Math.max(finalWidth, offsetX);
                        offsetX = paddingLeft;//换行的时候重置一下offsetX
                        //这里做换行
                        if (animationProvider == null || !animationProvider.isSingleLine()) {
                            //如果没有动画或者不是单行显示
                            finalHeight += (emojiWidth + lineSpacing);
                        }
                        String currentrow = split[row];
                        if (!TextUtils.isEmpty(currentrow)) {
//                        currentRowleng = currentrow.length() * (emojiWidth + textSpacing);
                            currentRowleng = (int) paint.measureText(currentrow);
                            if (currentrow.length() > 0) {
                                offset = (currentrow.length() - 1) * textSpacing;
                                currentRowleng += offset;
                            }
                            if (widest >= currentRowleng) {
                                xoffsetCenter = (widest - currentRowleng) / 2;
                                offsetX += xoffsetCenter;//校正居中偏移量
                            }
                        }
                        Logger.d("第" + row + "行：" + xoffsetCenter + "，widest：" + widest + "，currentRowleng:" + currentRowleng + ","
                                + "offset:" + offset);
                    }
                } else {
                    emoji.row = row;//设置行
                    int fontTotalWidth = (int) paint.measureText(itemText);
                    emoji.offsetX = offsetX;
                    emoji.offsetY = finalHeight;
                    emoji.fontTotalWidth = (int) paint.measureText(itemText);
                    emoji.fontTotalHeight = emojiWidth;
                    offsetX += fontTotalWidth + textSpacing;//x坐标加一个字符宽度和文件间隙
//                    if (offsetX >= ToolPaint.getDefault().getMaxTextLength() && i < size - 1) {//fix problem: OpenGLRenderer: Bitmap
//                        // too large to be uploaded into a texture 宽度太大无法绘制问题
//                        EmojiconHandler.TextEmoji emojiNext = textEmojiList.TopicGroupAPIAOP(i + 1);
//                        if (emojiNext.text == null || !emojiNext.text.equals("\n")) {
//                            EmojiconHandler.TextEmoji emojiEnter = new EmojiconHandler.TextEmoji();
//                            emojiEnter.text = "\n";
//                            textEmojiList.add(i + 1, emojiEnter);
//                            size++;
//                            //自动换行了，split必须变更，否则可能越界
//                        }
//                    }
                }
            }
            emoji.ready = true;
        }
        finalHeight += (emojiWidth + lineSpacing);
        finalWidth = Math.max(finalWidth, offsetX);
        finalWidth += paddingLeft;
        finalHeight += paddingTop;
        resetAnimationPaint();

        ViewParent parent = getParent();
        if (parent != null && parent instanceof PSLayer) {
            ((PSLayer) parent).disableHardWareWhenText2Long(this);
        }
    }

    //重置动画画笔，用于带有透明度的动画
    public void resetAnimationPaint() {
        if (animationProvider != null) {
            animationProvider.resetPaint(paint, strokepaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(finalWidth, finalHeight);
    }


    public boolean resetPosition = false; // 由于初始位置没有确定，不矫正位置
    int preWidth, preHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getParent() instanceof PSLayer) {
            int width = right - left;
            int height = bottom - top;
            if (preWidth == width && preHeight == height) {
                ((PSLayer) getParent()).rebindOpView();
                return;
            }
            preWidth = width;
            preHeight = height;
            if (width > 0 && height > 0) {
                if (nullInput || isAudio) {
                    resetPosition = true;
//                    int offsetY = 0;
                    int offsetY = 0;
//                    int offsetY = AutoUtils.getPercentWidthSize(0);
//                    int offsetY = AutoUtils.getPercentWidthSize(20);
                    float scale = ((PSLayer) getParent()).showBottomCenter(width, height, offsetY, isAudio ? getText() : null);
                    ((PSLayer) getParent()).rebindOpView(height, scale);
                    isAudio = false;
//                    ((ScaleView) getParent()).showBottomCenter(width, height, offsetY, isAudio ? getText() : null);
//                    ((ScaleView) getParent()).rebindOpView();
//                } else if (isDiyBottom){
//                    resetPosition = true;
//                    int offsetY = AutoUtils.getPercentWidthSize(30);
//                    ((ScaleView) getParent()).showBottomCenter(width, height, offsetY, isDiyBottom?getText():null);
//                    isDiyBottom = false;
//                    ((ScaleView) getParent()).rebindOpView();
                } else {
                    float[] offset = ((PSLayer) getParent()).rebindOpView();
                    if (offset != null && offset.length == 2) {
                        if (offset[0] == 0 && offset[1] == 0) {
                        } else {
                            //文字长度变化的时候，对位置进行矫正
                            if (resetPosition) {
                                Logger.d("对位置进行矫正");
                                ((PSLayer) getParent()).changeOffset(getText(), offset[0], offset[1]);
                            }
                            resetPosition = true;
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        if (BuildConfig.DEBUG) {
//            canvas.drawColor(getResources().getColor(R.color.xml.scroll_red));
//        }
        drawCanvas(canvas);
    }

    @Override
    public void drawCanvas(Canvas canvas) {
        if (textEmojiList == null || textEmojiList.size() == 0) {
            return;
        }
//        Log.d("drawtext", "字符数：" + textEmojiList.size());
        drawBackground(canvas);
        if (animationProvider == null || !AnimationProvider.isDurationValiable(duration, speed)) {
            //没有动画绘制文字
            drawText(canvas, textEmojiList.size(), paint, strokepaint, startColorStr, endColorStr, lineSpacing);
        } else {
            //更新时间
            updateAnimationTime();
            if (animationProvider.isRowSplited()) {
                //设置行号
                if (!TextUtils.isEmpty(text)) {

                    int drawTextRow;
                    int perRowTime = animationProvider.getPerRowTime();
                    if (recordTime >= 0) {
                        drawTextRow = (recordTime / perRowTime);
                    } else {
                        drawTextRow = (mCurrentAnimationTime / perRowTime);
                    }
//                    Logger.d("drawTextRow:"+drawTextRow+",allrownum:"+allrownum);
                    for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
//                        Logger.d("drawText:" + emoji.text + ",row:" + emoji.row);
                        if (emoji.row == drawTextRow) {
                            int drawTextCount;
                            if (recordTime >= 0) {
                                drawTextCount = animationProvider.setTime(recordTime, true);
                            } else {
                                drawTextCount = animationProvider.setTime(mCurrentAnimationTime, false);
                            }
//                            if (j > drawTextCount) {
//                                break;
//                            }
                            animationProvider.preCanvas(canvas, emoji.offsetX + emojiWidth / 2, emoji.offsetY + emojiWidth / 2);
                            emoji.onDraw(canvas, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint),
                                    offsetY, startColorStr, endColorStr, lineSpacing);
                            animationProvider.proCanvas(canvas);
                        }
                    }
                }
            } else {
                if (animationProvider.isWordSplited()) {//文字如果每个字的动画不一样的话，需要对每个字进行动画
                    int i = 0;
                    for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
                        i++;
                        animationProvider.setPosition(i - 1);//设置positon,进而影响setTime（）产生的效果。来达到对每个字动画的控制。
                        int drawTextCount;
                        if (recordTime >= 0) {
                            drawTextCount = animationProvider.setTime(recordTime, true);
                        } else {
                            drawTextCount = animationProvider.setTime(mCurrentAnimationTime, false);
                        }
                        if (i > drawTextCount) {
                            break;
                        }
                        animationProvider.preCanvas(canvas, emoji.offsetX + emojiWidth / 2, emoji.offsetY + emojiWidth / 2);
                        emoji.onDraw(canvas, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint),
                                offsetY, startColorStr, endColorStr, lineSpacing);

                        animationProvider.proCanvas(canvas);
                    }
                } else {
                    //文字动画统一处理
                    int showTextCount;
                    if (recordTime >= 0) {
                        showTextCount = animationProvider.setTime(recordTime, true);
                        if (animationProvider.clipPath()) {
                            //受硬件加速的影响
                            canvas.clipRect(new Rect(0, 0, finalWidth, finalHeight));
                        }
                    } else {
                        //TODO 从天而降的动画，在预览界面不加clipPath相关代码，效果和制作界面不同，为什么呢？
                        //可能原因是预览页面，ScaleView-->MatchParent。而制作页面ScaleView-->WrapContent
                        //但是是否关闭硬件加速，也产生了影响，这也奇怪。
                        if (animationProvider.clipPath()) {
                            //受硬件加速的影响
                            canvas.clipRect(new Rect(0, 0, finalWidth, finalHeight));
                        }
                        showTextCount = animationProvider.setTime(mCurrentAnimationTime, false);
                    }
                    animationProvider.preCanvas(canvas, getViewWidth() / 2, getViewHeight() / 2);
                    drawText(canvas, showTextCount, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint)
                            , startColorStr, endColorStr, lineSpacing);
                    animationProvider.proCanvas(canvas);
                }
            }
        }
        isSeeking = false;
    }

    void drawText(Canvas canvas, int drawTextCount, Paint paint, Paint strokePaint, String startColorStr, String endColorStr, int
            lineSpacing) {
        int i = 0;
        for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
            i++;
            if (i > drawTextCount) {
                break;
            }
            emoji.onDraw(canvas, paint, strokePaint, offsetY, startColorStr, endColorStr, lineSpacing);
        }
    }

    private long mMovieStart = 0;
    private int mCurrentAnimationTime = 0;

    //重要方法，刷新当前动效的时间
    private void updateAnimationTime() {
        long now = System.currentTimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        if (getDuration() > 0) {
            mCurrentAnimationTime = (int) ((now - mMovieStart) % getDuration());
        }
    }

    void drawBackground(Canvas canvas) {
        if (backgroudRes != 0) {
            if (preBackgroundInfo != backgroudRes) {
                Util.recycleBitmaps(backgroundBitmap);
                backgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroudRes);
            }
            if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
                canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), paint);
            }
        }
        preBackgroundInfo = backgroudRes;
    }

    public AnimationProvider animationProvider;
    public int currentType = 0;
    private int duration = 1600;
    public float speed = 1.0f;
    private int delay = 80;

    public boolean setTextAnimationType(int type, int duration, int delay, float speed) {
        Logger.d("textviewAnime:" + type);
        if (currentType == type) {
            return false;
        }
        setTextAnimationType(type, duration, speed, delay, true);
        return true;
    }

    //是否有动效，且时长够以运行动效
    public boolean canAnimating() {
        if (currentType > 0) {
            if (AnimationProvider.isDurationValiable(duration, speed)) {
                return true;
            }
        }
        return false;
    }

    private void setTextAnimationType(int type, int duration, float speed, int delay, boolean addEvent) {
        currentType = type;
        mMovieStart = System.currentTimeMillis();
        animationProvider = AnimationProvider.getProvider(type);
        changeAnimationTime(duration, delay, speed);
        /**
         * 单行显示
         */
//        if (animationProvider.isSingleLine()){
        calculateWidthHeight();
//        if (animationProvider==null){
//            Logger.d("==null");
//        }else {
//            Logger.d("=!=null");
//        }
        if (animationProvider != null && animationProvider.isRowSplited()) {
            int allrownum = 1;
            String[] split = text.split("\n");
            if (split == null || split.length >= 2) {
                allrownum = split.length;
            }
            if (!TextUtils.isEmpty(split[split.length - 1])) {
                allrownum++;//多一帧的不显示
            }
//                    animationProvider.
            int drawTextRow;//正在绘制的行
            int perRowTime = getDuration() / allrownum;
            animationProvider.setPerRowTime(perRowTime);
        }
        requestLayout();
//        invalidate();
//        }
        if (addEvent) {
            addEvent();
        }
    }

    /**
     * 重要方法：视频的时长，视频的播放快慢都将影响到文字动效。在时长和播放速度变化变化的时候，都要调用这个方法。
     *
     * @param duration
     * @param delay
     * @param speed
     */
    public void changeAnimationTime(int duration, int delay, float speed) {
        this.duration = duration;//视频时长
        this.delay = delay;
        this.speed = speed;
        if (animationProvider != null) {
            animationProvider.setDurationDelay(duration, delay);
            resetAnimationParams();
        }
        CustomTextView.this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    private void resetAnimationParams() {
        if (animationProvider != null) {
            animationProvider.setTextWidthHeight(finalWidth, finalHeight);
            animationProvider.setTextCount(textEmojiList.size());
            animationProvider.init();
        }
    }

    public int getTotalTime() {
        return duration;
    }

    public boolean isRepeat() {
        if (animationProvider != null) {
            return animationProvider.isRepeat();
        }
        return false;
    }
//    java.lang.NullPointerException: Attempt to invoke virtual method 'int com.biaoqing.BiaoQingShuoShuo.ui.activity.diy.ui.view.a
// .a.f()' on a null object reference
//    at com.seaon.lib.view.TextStyleView.getVideoDuration(TextStyleView.java:778)
//    at com.biaoqing.library.diy.ui.view.scale.ScaleView.c(ScaleView.java:311)
//    at com.biaoqing.BiaoQingShuoShuo.ui.activity.diy.ui.view.ContainerView.o(ContainerView.java:494)
//    at com.biaoqing.BiaoQingShuoShuo.ui.activity.diy.ui.view.ContainerView.g(ContainerView.java:62)
//    at com.biaoqing.BiaoQingShuoShuo.ui.activity.diy.ui.view.ContainerView$d.run(ContainerView.java:598)

    /**
     * 非常奇怪的一个异常，按道理来说已经已经判断好了，不可能出现。出现在一加5，8.0.0系统，暂时加一个异常捕获
     *
     * @return
     */
    @Override
    public int getDuration() {
        try {
            if (animationProvider == null || !AnimationProvider.isDurationValiable(duration, speed)) {
                return 0;
            }
            return animationProvider.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void startRecord() {
    }

    private int recordTime = -1;

    @Override
    public boolean isSeeking() {
        return isSeeking;
    }

    boolean isSeeking = false;

    @Override
    public void recordFrame(int time) {
        recordTime = time;
        isSeeking = true;
    }

    @Override
    public void stopRecord() {
        recordTime = -1;
    }

    @Override
    public int getDelay() {
        if (animationProvider == null) {
            return 0;
        }
        return animationProvider.getDelay();
    }


    public int getVideoDelay() {
        return (int) (120 / speed);
    }

    public Typeface getTypeface(Typeface typefaceDefault) {
        if (!TextUtils.isEmpty(fontName)) {
            File fontfile = FileManager.getDiyFontFile(getContext(), fontName);
            if (fontfile != null && fontfile.exists()) {
                try {
                    Typeface typeface = Typeface.createFromFile(fontfile);
                    if (typeface != null) {
                        return typeface;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //把错误的字体文件删除，以便重新下载
                    boolean delete = fontfile.delete();
                    Logger.d("deletefile:" + delete);
                }
            }
        }
        return typefaceDefault;
    }

    @Override
    public void onRelease() {
        Util.recycleBitmaps(backgroundBitmap);
    }

    public float getTextSize() {
        return paint.getTextSize();
    }

    public String getText() {
        return text;
    }

    public boolean setTexttypeface(Typeface texttypeface) {
        Typeface typeface = paint.getTypeface();
        if (texttypeface.equals(typeface)) {
            return false;
        }
        fontName = null;
        paint.setTypeface(getTypeface(texttypeface));
        strokepaint.setTypeface(getTypeface(texttypeface));
        calculateWidthHeight();
        addEvent();
        requestLayout();
        invalidate();
        return true;
    }


    List<EmojiconHandler.TextEmoji> textEmojiList;

    private void fixEmoji() {
        // \n的算一个字符，emoji算两个字符，但是这里做了fix
        textEmojiList = EmojiconHandler.getEmojis(getContext(), text);
    }


    public void setPaintColorReverse(int color, int strokeColor) {
        paint.setColor(color);
        strokepaint.setColor(strokeColor);
        resetAnimationPaint();
    }

    public boolean setStrokecolor(String strokecolor) {
        int colorNew = Util.getColor(strokecolor, paint.getColor());
        int color = strokepaint.getColor();
        if (color == colorNew) {
            return false;
        }

        //setColor之后透明度会重置为255, 需要重新设置
        int alpha = strokepaint.getAlpha();
        strokepaint.setColor(colorNew);
        strokepaint.setAlpha(alpha);

        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean isText2Long() {
        return finalWidth * finalHeight * 4 > drawingCacheSize;
    }

    public int setText(String text) {
        this.text = text;
        fixEmoji();
        calculateWidthHeight();

        addEvent();
        return finalWidth;
    }

    public int editText(String text) {
        if (text != null && text.equals(this.text)) {
            return finalWidth;
        }
        this.text = text;
        fixEmoji();
        calculateWidthHeight();
        resetAnimationParams();
//        if (paint.getShader() != null && paint.getShader() instanceof LinearGradient) {
//            setLinearGradient(startColorStr, endColorStr);
//           LinearGradient shader = (LinearGradient) paint.getShader();
//            try {
//                Bitmap bitmap = Bitmap.createBitmap(8, getViewHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
//                String startcolor = Util.getColorStr(bitmap.getPixel(0, 0));
//                String endcolor = Util.getColorStr(bitmap.getPixel(bitmap.getWidth()-1 , getViewHeight()-1));
//                Logger.d("startcolor:"+startcolor+",endcolor:"+endcolor);
//                setLinearGradient(startcolor,endcolor );
//                Util.recycleBitmaps(bitmap);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Logger.d("startcolor:"+e.toString());
//            }
//        }
        addEvent();
        boolean hardwareAccelerated = this.isHardwareAccelerated();
        Logger.d("硬件加速2：" + hardwareAccelerated);
        requestLayout();
        invalidate();
        return finalWidth;
    }

    public int editTextWithCal(String text) {
        if (text != null && text.equals(this.text)) {
            return finalWidth;
        }
        this.text = text;
        fixEmoji();

        paint.setStrokeWidth(getPaintStrokeWidth(0.2f));
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.2f, 0.4f));

        calculateWidthHeight();
        resetAnimationParams();

        boolean hardwareAccelerated = this.isHardwareAccelerated();
        Logger.d("硬件加速2：" + hardwareAccelerated);

        addEvent();
        requestLayout();
        invalidate();
        return finalWidth;
    }


    public boolean setStrokealpha(int strokealpha) {
        int alpha = strokepaint.getAlpha();
        if (alpha == strokealpha) {
            return false;
        }
        strokepaint.setAlpha(strokealpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setStrokecolor(int strokecolor) {
        int color = strokepaint.getColor();
        if (color == strokecolor) {
            return false;
        }
        //setColor之后透明度会重置为255, 需要重新设置
        int alpha = strokepaint.getAlpha();
        strokepaint.setColor(strokecolor);
        strokepaint.setAlpha(alpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }


    public boolean setTextcolor(int textcolor) {
        Logger.d("textcolor:" + textcolor);
        if (paint.getShader() != null) {
//            paint.setShader(null);
            startColorStr="";
            endColorStr="";
            paint.setColor(textcolor);
            resetAnimationPaint();
            invalidate();
            return true;
        }
        int color = paint.getColor();
        if (color != textcolor) {
            //setColor之后透明度会重置为255, 需要重新设置
            int alpha = paint.getAlpha();
            paint.setColor(textcolor);
            paint.setAlpha(alpha);
            resetAnimationPaint();
            invalidate();
            //TODO
            requestLayout();
            return false;
        }
        return false;
    }

    public boolean setTextalpha(int textalpha) {
        if (paint.getAlpha() == textalpha) {
            return false;
        }
        paint.setAlpha(textalpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setTextcolor(String textcolor) {
        startColorStr = "";
        endColorStr = "";
        int color = paint.getColor();
        if (!TextUtils.isEmpty(textcolor)) {
            int colorNew = Util.getColor(textcolor, paint.getColor());
            if (paint.getShader() != null) {
                paint.setShader(null);
                paint.setColor(colorNew);
                resetAnimationPaint();
                invalidate();
                return true;
            }
            if (color != colorNew) {
                //setColor之后透明度会重置为255, 需要重新设置
                int alpha = paint.getAlpha();
                paint.setColor(colorNew);
                paint.setAlpha(alpha);
                resetAnimationPaint();
                invalidate();
                return false;
            }
        }
        return false;
    }


    @Override
    public int getViewWidth() {
        return finalWidth;
    }

    @Override
    public int getViewHeight() {
        return finalHeight;
    }


    int position = -1;
    List<TextOp> list = new ArrayList<>();

    /**
     * 记录文字的历史记录
     */
    private void addEvent() {
        if (position < list.size() - 1) {
            for (int i = list.size() - 1; i > position; i--) {
                list.remove(i);
            }
        }
        list.add(new TextOp(getText(), paint, strokepaint, backgroudRes, fontName, currentType, startColorStr, endColorStr));
        position = list.size() - 1;
    }

    public void pre() {
        position--;
        if (position < 0) {
            position = 0;
        }
        TextOp op = list.get(position);
        reset(op);
    }

    public void pro() {
        position++;
        if (position > list.size() - 1) {
            position = list.size() - 1;
        }
        TextOp op = list.get(position);
        reset(op);
    }

    private void reset(TextOp op) {
        this.paint = new Paint();
        this.paint.set(op.paint);
        this.strokepaint = new Paint();
        this.strokepaint.set(op.strokePaint);
        this.backgroudRes = op.background;
        this.fontName = op.fontName;
        this.text = op.text;
        this.startColorStr = op.startColorStr;
        this.endColorStr = op.endColorStr;
        fixEmoji();
        calculateWidthHeight();
        this.currentType = op.animationType;
        setTextAnimationType(currentType, duration, speed, delay, false);
        requestLayout();
        invalidate();
    }

    public void setOffsetY4Diy(int offsetY4Diy) {
        this.offsetY4Diy = offsetY4Diy;
    }
    public int getOffsetY4Diy() {
       return this.offsetY4Diy;
    }

    //文字历史记录类
    class TextOp {
        public Paint paint;
        public Paint strokePaint;
        public int background;
        public String text;
        public String fontName;
        public int animationType;
        public String startColorStr;
        public String endColorStr;

        TextOp(String text, Paint p, Paint sp, int bg, String fontName, int animationType, String startColorStr, String endColorStr) {
            this.text = text;
            this.fontName = fontName;
            this.paint = new Paint();
            this.paint.set(p);
            this.strokePaint = new Paint();
            this.strokePaint.set(sp);
            this.background = bg;
            this.animationType = animationType;
            this.startColorStr = startColorStr;
            this.endColorStr = endColorStr;
        }
    }


    public boolean getNullInput() {
        return nullInput;
    }


    public void setNullInput(boolean nullInput) {
        if (nullInput == this.nullInput) {
            return;
        }
        init(getContext());
        this.nullInput = nullInput;
        paint.setColor(Color.WHITE);
        if (nullInput) {
            strokepaint.setColor(Color.parseColor("#474747"));
            paint.setStrokeWidth(getPaintStrokeWidth(0.4f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.4f, 0.2f));
            paint.setAlpha(100);
            strokepaint.setAlpha(108);
        } else {
            strokepaint.setColor(Color.BLACK);
            paint.setStrokeWidth(getPaintStrokeWidth(0.4f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.4f, 0.2f));
            paint.setAlpha(255);
            strokepaint.setAlpha(255);
        }
    }

    public void setIsAudio(boolean isAudio) {
        init(getContext());
        this.isAudio = isAudio;
        paint.setColor(Color.WHITE);
        strokepaint.setColor(Color.BLACK);
        if (isAudio) {
            paint.setStrokeWidth(getPaintStrokeWidth(0.2f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.2f, 0.4f));
        }
        paint.setAlpha(255);
        strokepaint.setAlpha(255);
    }

    //TODO 进阶，对每个字Shader
    public void setLinearGradient(String startcolorStr, String endcolorStr) {
        //字体颜色是双色
        int startcolor = Color.parseColor("#" + startcolorStr);
        int endcolor = Color.parseColor("#" + endcolorStr);
        int[] colrs = {startcolor, startcolor, endcolor};
        float[] positions = {};
        //更适合多行文字的效果
        LinearGradient linearGradient = new LinearGradient(getViewWidth() / 2, 0, getViewWidth() / 2, getViewHeight(), startcolor,
                endcolor, Shader.TileMode.REPEAT);
//        LinearGradient linearGradient = new LinearGradient(getViewWidth() / 2, 0, getViewWidth() / 2, lineHeight, startcolor,
// endcolor,
//                Shader.TileMode.REPEAT);
        paint.setShader(linearGradient);
    }

}
