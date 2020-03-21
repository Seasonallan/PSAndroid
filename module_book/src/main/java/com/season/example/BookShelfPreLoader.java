package com.season.example;

import android.content.Context;

import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.book.db.BookMarkDB;
import com.season.lib.file.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * 书架书籍预加载
 *
 */
public class BookShelfPreLoader {

    private static BookShelfPreLoader sInstance = null;

    BookInfo mBook;
    Context mContext;
    private BookShelfPreLoader(Context context) {
        this.mContext = context;
        mBook = new BookInfo();
        boolean netTest = true;
        if (netTest){
            mBook.id = "10002";
            mBook.title = "斗破苍穹";
            mBook.author = "天蚕土豆";
            mBook.netIndex = 873530;
        }else{
            mBook.id = "00002";
        }
    }

    public static final BookShelfPreLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BookShelfPreLoader(context);
        }
        return sInstance;
    }

    public BookInfo getBookPreloaded(){
        return mBook;
    }

    public void preLoad(){
        new Thread() {
            @Override
            public void run() {
                InputStream is;
                if (mBook.id.equals("00001")){
                    is = mContext.getResources().openRawResource(R.raw.epub_book);
                    mBook.path = getBookFielPath(".epub");
                }else if (mBook.id.equals("00002")){
                    is = mContext.getResources().openRawResource(R.raw.text_book);
                    mBook.path = getBookFielPath(".txt");
                }else{
                    is = mContext.getResources().openRawResource(R.raw.umd_book);
                    mBook.path = getBookFielPath(".umd");
                }
                if(!FileUtils.copyFileToFile(mBook.path, is)){
                    return;
                }
                BookMarkDB.getInstance().loadBookMarks(mBook.id);
            }
        }.start();
    }


    private String getBookFielPath(String fend){
        String pathDir = mContext.getCacheDir() + File.separator;
        String path =pathDir + "cache"+fend;
        File fileDir = new File(pathDir);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        return path;
    }
}
