package com.season.lib.view;

import android.graphics.RectF;

import com.season.lib.AbsTextSelectHandler;
import com.season.lib.bean.Catalog;
import com.season.lib.page.span.ClickActionSpan;

import java.util.ArrayList;

/**
 * 对外接口，绑定activity和readerView
 *
 */
public interface IReadCallback {
    /**
     * 章节添加【txt章节获取】
     * */
    void onChapterChange(ArrayList<Catalog> chapters);

    /**
     * 当前页变化
     * */
    void onPageChange(int progress,int max);
    /**
     * 章节排版布局进度
     * @param progress 当前排版章节数
     * @param max 最大章节数
     */
    void onLayoutProgressChange(int progress, int max);
    /**
     * 没有上一页内容
     */
    void onNotPreContent();
    /**
     * 没有下一页内容
     */
    void onNotNextContent();
    /**
     * 检测是否需要购买
     * @param catalogIndex
     * @param isNeedBuy
     * @return
     */
    boolean checkNeedBuy(int catalogIndex, boolean isNeedBuy);

    /**
     * 该页是否已经标注为用户书签
     */
    boolean hasShowBookMark(int chapterId, int pageStart, int pageEnd);

    /**
     * epub书籍目录收费章节起始修改
     */
    boolean setFreeStart_Order_Price(int feeStart, boolean isOrdered, String price, String limitPrice);


    /**
     * 视频或语音等被点击
     * @param clickableSpan
     * @param localRect
     * @param x
     * @param y
     * @return
     */
    boolean onSpanClicked(ClickActionSpan clickableSpan, RectF localRect,
                          int x, int y);

    /**
     * 获取长按选择器弹窗插件
     * @return
     */
    AbsTextSelectHandler.ISelectorListener getSelecter();
}
