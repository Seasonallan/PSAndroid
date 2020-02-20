package com.season.lib.view;


import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.season.lib.bean.BookMark;
import com.season.lib.AbsTextSelectHandler;
import com.season.lib.TextSelectHandler;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.page.span.ClickActionSpan;

import java.util.ArrayList;

public interface IReaderView {

    /** 获取书籍信息失败 */
     static final int ERROR_GET_CONTENT_INFO = -1;
    /** 获取书籍目录失败 */
     static final int ERROR_GET_CATALOG_INFO = -2;
    /** 获取书籍章节信息失败 */
     static final int ERROR_GET_CHAPTER_INFO = -3;
    /** 书籍已下线 */
     static final int ERROR_BOOK_OFFLINE = -4;
    /** 书籍秘钥异常 */
     static final int ERROR_BOOK_SECRET_KEY = -5;
    /** 本地epub文件秘钥缺失 */
     static final int ERROR_LOCAL_EPUB_SECRET = -6;
    /** 成功 */
     static final int SUCCESS = 1;

    /**
     * 生命周期开始
     */
     void onCreate(Bundle savedInstanceState);
    /**
     * 初始化方法，运行在子线程，失败返回false退出阅读界面
     * @return
     */
     int onInitReaderInBackground(int fRequestCatalogIndex, int fRequestPageCharIndex ,String secretKey);
    /**
     * 生命周期结束
     */
     void onDestroy();
    /**
     * Activity Resume
     */
     void onActivityResume();
    /**
     * Activity Pause
     */
     void onActivityPause();
    /**
     * Activity onSaveInstanceState
     * @param outState
     */
     void onActivitySaveInstanceState(Bundle outState);
    /**
     * Activity onRetainNonConfigurationInstance
     * @return
     */
     Object onActivityRetainNonConfigurationInstance();
    /**
     * Activity  onRestoreInstanceState
     * @param savedInstanceState
     */
     void onActivityRestoreInstanceState(Bundle savedInstanceState);
    /**
     * 派遣Activity DispatchTouchEvent 事件
     * @param ev
     * @return
     */
     boolean onActivityDispatchTouchEvent(MotionEvent ev);
    /**
     * 派遣Activity DispatchKeyEvent 事件
     * @param event
     * @return
     */
     boolean onActivityDispatchKeyEvent(KeyEvent event);
    /**
     * 添加章节信息
     */
     ArrayList<Catalog> getChapterList();
    /**
     * 制订调整进度字符，返回空折使用默认
     * @param curPage
     * @param pageNums
     * @return
     */
     String getJumpProgressStr(int curPage, int pageNums);
    /**
     * 跳章（点击目录）
     * @param catalog 章节
     * @param isStartAnim 是否播放动画
     */
     void gotoChapter(Catalog catalog,boolean isStartAnim);
    /**
     * 跳章（点击目录）
     * @param requestChapterIndex 章节数
     * @param isStartAnim 是否播放动画
     */
     void gotoChapter(int requestChapterIndex,boolean isStartAnim);
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
     * 跳转下一页
     */
     void gotoNextPage();
    /**
     * 跳转上一页
     */
     void gotoPrePage();
    /**
     * 跳转上一章
     */
     void gotoPreChapter();
    /**
     * 跳转下一章
     */
     void gotoNextChapter();
    /**
     * 跳转具体页（主要用于进度条跳转）
     * @param requestProgress 具体页数或者百分比
     * @param isStartAnim 是否播放动画
     */
     void gotoPage(int requestProgress,boolean isStartAnim);
    /**
     * 跳转具体字符所在页
     * @param requestChapterIndex
     * @param requestCharIndex
     * @param isStartAnim
     */
     void gotoChar(int requestChapterIndex,int requestCharIndex,boolean isStartAnim);
//	/**
//	 * 跳转书摘
//	 * @param bookDigests
//	 * @param isStartAnim
//	 */
//	 void gotoBookDigest(BookDigests bookDigests,boolean isStartAnim);
    /**
     * 跳转书签
     * @param bookmark
     * @param isStartAnim
     */
     void gotoBookmark(BookMark bookmark, boolean isStartAnim);
    /**
     * 是否有上一级
     * @return
     */
     boolean hasPreSet();
    /**
     * 是否有下一集
     * @return
     */
     boolean hasNextSet();
    /**
     * 跳转上一级
     */
     void gotoPreSet();
    /**
     * 跳转下一级
     */
     void gotoNextSet();
    /**
     * 生成系统书签
     * @return
     */
     BookMark newSysBookmark();
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
     * 获取书记信息
     * @return
     */
     BookInfo getBookInfo();
    /**
     * 获取章节目录对象
     */
     Catalog getCurrentCatalog();
    /**
     * 当前章节
     */
     int getCurChapterIndex();
    /**
     * 当前某页开始字符位置
     * @return
     */
     int getPageStartIndex(int chapterIndex, int pageIndex);
//	/**
//	 * 当前页结束字符位置
//	 * @return
//	 */
//	 int getCurPageEndIndex();
//	/**
//	 * 关闭书摘
//	 * @return 有效操作
//	 */
//	 boolean closeTextSelect();
    /**
     * 书摘处理事件
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
     * 是否可添加用户书签
     * @return
     */
     boolean canAddUserBookmark();
    /**
     * 获取书摘处理者
     * @return
     */
     TextSelectHandler getTextSelectHandler();
    /**
     * 获取内容View，如果自己就是View就返回自己
     * @return
     */
     View getContentView();
    /**
     * 处理购买结果
     * @param chapterId
     */
     void dealBuyResult(int chapterId);
    /**
     * 触发长按事件
     */
     boolean onLongPress();
    /**
     * 搜索
     * @param direction	1 往后搜  -1  往前搜
     * @param keyWord 关键字
     */
     void search(int direction, String keyWord);

    /**
     * 章节目录信息转换
     * @param chaptersId
     * @return
     */
     String getChapterId(int chaptersId);
    /**
     * 对外接口
     * @author lyw
     */
     interface IReadCallback{
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
         * 显示等待对话框
         */
         void showLoadingDialog(int resId);
        /**
         * 隐藏等待对话框
         */
         void hideLoadingDialog();

        /**
         * 该页是否已经标注为用户书签
         */
         boolean hasShowBookMark(int chapterId, int pageStart, int pageEnd);

        /**
         * epub书籍目录收费章节起始修改
         */
         boolean setFreeStart_Order_Price(int feeStart, boolean isOrdered, String price, String limitPrice);

        /**
         * epub书籍id设置，用于弹窗购买窗口后挑战到书籍详情
         */
         void setCebBookId(String cebBookId);

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

        AbsTextSelectHandler.ISelectorListener getSelecter();
    }
}

