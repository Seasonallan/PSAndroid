package com.season.example;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.book.plugin.epub.EpubPlugin;
import com.season.book.plugin.umd.UmdPlugin;
import com.season.lib.BaseContext;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.file.FileUtils;
import com.season.lib.util.LogUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 书架书籍预加载
 *
 */
public class BookShelfPreLoader {

    public interface ICallback{
        void onBookLoaded(List<BookInfo> bookLists);
    }

    private static BookShelfPreLoader sInstance = null;
    private List<BookInfo> bookLists;
    private ICallback iCallback;
    private boolean isPreLoaded = false;

    private BookShelfPreLoader() {
    }

    public void getBookLists(ICallback callback){
        if (isPreLoaded){
            if (bookLists != null){
                callback.onBookLoaded(bookLists);
            }else{
                this.iCallback = callback;
                isPreLoaded = false;
                preLoad();
            }
        }else{
            this.iCallback = callback;
        }
    }

    public static final BookShelfPreLoader getInstance() {
        if (sInstance == null) {
            sInstance = new BookShelfPreLoader();
        }
        return sInstance;
    }


    public void saveLocal(final Object list){
        new Thread() {
            @Override
            public void run() {
                FileUtils.saveSerialData("cacheBookLists", list);
            }
        }.start();
        bookLists = null;
    }

    public void preLoad(){
        new Thread() {
            @Override
            public void run() {
                Object object = FileUtils.getSerialData("cacheBookLists");
                if (object instanceof List){
                    bookLists = (List<BookInfo>)object ;
                }
                if (bookLists == null || bookLists.size() == 0){
                    bookLists = new ArrayList<>();
                    bookLists.add(new BookInfo("00001", "1.epub", R.raw.epub_book));
                    bookLists.add(new BookInfo("00011", "2.epub", R.raw.epub_book2));
                    bookLists.add(new BookInfo("00012", "3.epub", R.raw.santi));
                    bookLists.add(new BookInfo("00013", "4.epub", R.raw.zuoer));
                    bookLists.add(new BookInfo("00014", "5.epub", R.raw.ssssslth));
                    bookLists.add(new BookInfo("00002", "浪漫满屋.txt", R.raw.text_book));
                    bookLists.add(new BookInfo("00022", "爱在何方，家在何处.txt", R.raw.azhf));
                    bookLists.add(new BookInfo("00003", "book.umd", R.raw.umd_book));
                    BookInfo netBook = new BookInfo("10002", "斗破苍穹");
                    netBook.author = "天蚕土豆";
                    netBook.netIndex = 873530;
                    netBook.cover = "https://bkimg.cdn.bcebos.com/pic/0ff41bd5ad6eddc448be31f537dbb6fd52663366?x-bce-process=image/watermark,g_7,image_d2F0ZXIvYmFpa2UxMTY=,xp_5,yp_5";
                    bookLists.add(netBook);
                }
                for (BookInfo book:bookLists){
                    if (book.rawId != -1 && TextUtils.isEmpty(book.filePath)){
                        InputStream is = BaseContext.getInstance().getResources().openRawResource(book.rawId);
                        String filePath = getBookFielPath(book.title);
                        if(FileUtils.copyFileToFile(filePath, is)){
                            book.filePath = filePath;
                        }
                    }
                    if (TextUtils.isEmpty(book.cover)){
                        if (!TextUtils.isEmpty(book.filePath)){
                            if (book.filePath.endsWith(".epub")){
                                EpubPlugin epubPlugin = new EpubPlugin(book.filePath);
                                try {
                                    epubPlugin.init("");
                                    BookInfo decodeBook = epubPlugin.getBookInfo(book);
                                    book.id = decodeBook.id;
                                    book.title = decodeBook.title;
                                    book.author = decodeBook.author;
                                    book.publisher = decodeBook.publisher;

                                    InputStream inputStream = epubPlugin.getCoverStream();
                                    if (inputStream != null){
                                        String fileName = getBookFielPath("cover" + book.filePath.hashCode());
                                        if(FileUtils.copyFileToFile(fileName, inputStream)){
                                            book.cover = fileName;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (book.filePath.endsWith(".umd")){
                                UmdPlugin umdPlugin = new UmdPlugin(book.filePath);
                                try {
                                    umdPlugin.init("");
                                    book.title = umdPlugin.mTitle +".umd";
                                    book.author = umdPlugin.mAuthor;
                                    Bitmap bitmap  = umdPlugin.mBookCover;
                                    String fileName = getBookFielPath("cover" + book.filePath.hashCode());
                                    book.cover = BitmapUtil.saveBitmap(new File(fileName), bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    LogUtil.i(book.toString());
                }
                isPreLoaded = true;
                if (iCallback != null){
                    BaseContext.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            iCallback.onBookLoaded(bookLists);
                            iCallback = null;
                        }
                    });
                }
            }
        }.start();
    }


    private String getBookFielPath(String fend){
        String pathDir = BaseContext.getInstance().getCacheDir() + File.separator;
        String path =pathDir + "cache"+fend;
        File fileDir = new File(pathDir);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        return path;
    }
}
