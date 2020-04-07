package com.season.book.plugin;

import com.season.book.bean.BookInfo;
import com.season.book.bean.Catalog;
import com.season.book.plugin.epub.EpubPlugin;
import com.season.book.plugin.epub.Resource;
import com.season.book.plugin.text.TextPlugin;
import com.season.book.plugin.umd.UmdPlugin;

import java.util.ArrayList;

/**
 * 书籍的解析
 */
public abstract class PluginManager {


    /**
     * 获取书籍解析器，根据文件信息
     * @param path
     * @return
     */
    public static PluginManager getPlugin(String path) {
        if (path.endsWith("epub")){
            return new EpubPlugin(path);
        }
        if (path.endsWith("txt")){
            return new TextPlugin(path);
        }
        if (path.endsWith("umd")){
            return new UmdPlugin(path);
        }
        return new EpubPlugin(path);
    }


    /** 文件路径  */
    protected String filePath;
    /** 书籍目录 */
    protected ArrayList<Catalog> catalog = new ArrayList<Catalog>();

    public PluginManager(String filePath) {
        this.filePath = filePath ;
    }

    /**
     * 解析开始，获取书籍目录和信息
     * @throws Exception
     */
    public abstract void init() throws Exception;


    /** 获取书籍目录；不为NULL，如果没有目录，SIZE为0
     * @return the catalog
     */
    public ArrayList<Catalog> getCatalog() {
        return catalog;
    }

    /**
     * 回收资源
     */
    public abstract void recyle();

    /**
     * 获取某位置目录信息
     * @param index
     * @return
     */
    public Catalog getCatalogByIndex(int index) {
        return getCatalog().get(index);
    }

    /**
     * 获取目录位置
     * @param catalog
     * @return
     */
    public int getChapterIndex(Catalog catalog) {
        int index = catalog.getIndex();
        ArrayList<Catalog> chapterIds = getCatalog();
        for (int i = 0;i < chapterIds.size();i++) {
            int id = chapterIds.get(i).getIndex();
            if(id == index){
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取某位置章节的内容
     * @param chapterIndex
     * @return
     * @throws Exception
     */
    public abstract String getChapter(int chapterIndex) throws Exception;

    /**
     * 获取修正内容信息
     * @param content
     * @return
     */
    public abstract String getFixHtml(String content);


    public Resource findResource(String path){
        return null;
    }

    /** 书籍信息 */
    protected BookInfo bookInfo;
    /** 获取书籍信息
     * @return the bookInfo
     */
    public BookInfo getBookInfo(BookInfo book) {
        if (bookInfo == null){
            return book;
        }
        return bookInfo;
    }

    /**
     * 获取章节位置
     * @param chapterID
     * @return
     */
    public int getChapterPosition(String chapterID){
        int id = Integer.parseInt(chapterID);
        for (int i = 0; i<catalog.size();i++){
            if (catalog.get(i).getIndex() == id){
                return i;
            }
        }
        return 0;
    }

    /**
     * 获取章节ID
     * @param chapterID
     * @return
     */
    public String getChapterId(int chapterID){
        return getCatalog().get(chapterID).getIndex()+"";
    }


}
