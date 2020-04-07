package com.season.book.view;


import android.view.MotionEvent;
import android.view.View;

import com.season.book.bean.BookMark;
import com.season.book.event.AbsTextSelectHandler;
import com.season.book.bean.BookInfo;
import com.season.book.bean.Catalog;
/**
 * ReaderView需要实现的基础方法
 *
 */
public interface IReaderView {

    /**
     * 解析书籍并启动排版线程池，运行在子线程
     * @return
     */
     BookInfo decodeBookFromPlugin(int fRequestCatalogIndex, int fRequestPageCharIndex);

    /**
     * 回收释放资源
     */
     void release();

    /**
     * 当前页面是否绘制完毕
     * @return
     */
     boolean isCurrentPageDrawn();


    /**
     * 跳章（点击目录）
     * @param catalog 章节
     * @param isStartAnim 是否播放动画
     */
     void gotoChapter(Catalog catalog,boolean isStartAnim);
    /**
     * 跳转具体字符所在页
     * @param requestChapterIndex
     * @param requestCharIndex
     * @param isStartAnim
     */
    void gotoChar(int requestChapterIndex,int requestCharIndex,boolean isStartAnim);

    /**
     * 跳转具体页（主要用于进度条跳转）
     * @param requestProgress 具体页数或者百分比
     * @param isStartAnim 是否播放动画
     */
    void gotoPage(int requestProgress,boolean isStartAnim);

    /**
     * 是否有上一章
     * @return
     */
     boolean hasPreChapter();
    /**
     * 是否有下一章
     * @return
     */
     boolean hasNextChapter();

    /**
     * 跳转上一章
     */
     void gotoPreChapter();
    /**
     * 跳转下一章
     */
     void gotoNextChapter();

    /**
     * 跳转上一页
     */
    void gotoPrePage();

    /**
     * 跳转下一页
     */
    void gotoNextPage();

    /**
     * 生成用户书签
     * @return
     */
     BookMark newUserBookmark();

    /**
     * 最大阅读进度
     * @return
     */
     int getMaxReadProgress();
    /**
     * 当前阅读进度
     * @return
     */
     int getCurReadProgress();
    /**
     * 排版进度
     * @return
     */
     int getLayoutChapterProgress();
    /**
     * 排版进度最大值
     * @return
     */
     int getLayoutChapterMax();

    /**
     * 获取章节目录对象
     */
     Catalog getCurrentCatalog();
    /**
     * 当前章节
     */
     int getCurChapterIndex();

    /**
     * 长按笔记选中处理事件
     * @param event
     * @param touchEventDispatcher
     * @return
     */
     boolean handlerSelectTouchEvent(MotionEvent event, AbsTextSelectHandler.ITouchEventDispatcher touchEventDispatcher);
    /**
     * 处理触屏事件
     * @param event
     * @return
     */
     boolean handlerTouchEvent(MotionEvent event);
    /**
     * 派遣点击事件
     * @param event
     * @return
     */
     boolean dispatchClickEvent(MotionEvent event);

    /**
     * 获取内容View，如果自己就是View就返回自己
     * @return
     */
     View getContentView();


    /**
     * 当前页面是否是书签
     * @return
     */
    boolean isCurrentPageMarked();

    /**
     * 是否正在执行翻页动画
     * @return
     */
    boolean isAnimating();

}

