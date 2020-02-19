package com.season.lib.text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.text.TextUtils;

import com.season.lib.BaseContext;
import com.season.lib.epub.paser.epub.Resource;
import com.season.lib.epub.paser.epub.ContainerDecoder;
import com.season.lib.epub.paser.epub.CatalogNAVDecoder;
import com.season.lib.epub.paser.epub.CatalogNCXDecoder;
import com.season.lib.epub.paser.epub.EpubFileDecoder;
import com.season.lib.file.IOUtil;
import com.season.lib.file.XMLUtil;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.bean.Chapter;
import com.season.lib.book.EncryptUtils;
import com.season.lib.txtumd.support.ChapterControll;
import com.season.lib.util.LogUtil;

import com.season.lib.txtumd.support.BytesEncodingDetect;
/** EPUB格式书籍的解析
 * @author mingkg21
 * @email mingkg21@gmail.com
 * @date 2013-2-19
 */
public class TextPlugin{

    private String secretKey;

    /** 文件路径  */
    protected String filePath;
    /** 书籍信息 */
    protected BookInfo bookInfo;

    /** 书籍目录 */
    private ArrayList<Catalog> catalog = new ArrayList<Catalog>();

    public TextPlugin(String filePath) {
        this.filePath = filePath ;
    }

    private RandomAccessFile mRandomAccessFile;
    protected int mBufferLength = 0;
    private FileChannel mFileChannel;
    private MappedByteBuffer mMappedByteBuffer = null;
    protected String mEncode = "UTF-8";
    private TextChapter mChapterControll;
    public void init(String secretKey) throws Exception {
        this.secretKey = secretKey;

        File book_file = new File(filePath);
        mBufferLength = (int) book_file.length();
        mRandomAccessFile = new RandomAccessFile(book_file, "r");
        mFileChannel =  mRandomAccessFile.getChannel();
        mMappedByteBuffer = mFileChannel.map(
                FileChannel.MapMode.READ_ONLY, 0, mBufferLength);
        byte[] encodings = new byte[400];
        mRandomAccessFile.read(encodings);
        BytesEncodingDetect be = new BytesEncodingDetect();

        int position = be.detectEncoding(encodings);
        position = position==22?6:position;
        mEncode = BytesEncodingDetect.nicename[position];
        mChapterControll= new TextChapter(filePath, BaseContext.getInstance().getCacheDir()+"/chapters/");

        findChapters();
    }

    public String getEncode(){
        return mEncode;
    }

    int start = 0;
    private void findChapters(){
        try {
            List<Catalog> res = mChapterControll.readFile();
            if(res != null && res.size() > 0){
                start = Integer.MAX_VALUE;
                mChapterControll.addChapterList(res);
                catalog = (ArrayList<Catalog>) mChapterControll.getChapters();
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        start = 0;
        Pattern p = Pattern.compile("(^\\s*第)(.{1,9})[章节卷集部篇回](\\s*)(.*)(\n|\r|\r\n)");
        while (start < mBufferLength) {
            byte[] bts = readParagraphForward(start);
            String str;
            try {
                str = new String(bts, mEncode);
                Matcher matcher = p.matcher(str);
                if (matcher.find()) {
                    String name = matcher.group(0);
                    mChapterControll.addChapter(name, start);
                }
            } catch (UnsupportedEncodingException e) {
                return;
            }
            start += bts.length;
        }
        start = mBufferLength;
        if(mChapterControll.getChapters().size() == 0){
            mChapterControll.addChapter(ChapterControll.DEFAULT_CHAPTER, 0);
        }
        catalog = (ArrayList<Catalog>) mChapterControll.getChapters();
    }

    private int _lastEndPosition = -1;
    private int _lastStartPosition = -1;
    private int MAX_PARAGRAPH = 40 * 200;

    /**
     * 读取下一个段落
     */
    protected byte[] readParagraphForward(int nFromPos) {
        int nStart = nFromPos;
        int i = nStart;
        byte b0, b1;
        int byteCount = 0;
        if (nStart > _lastStartPosition && nStart < _lastEndPosition) {
            i = _lastEndPosition;
        } else {
            if (isUnicode()) {
                while (i < mBufferLength - 1) {
                    b0 = getContentByte(i++);
                    b1 = getContentByte(i++);
                    if (isEnterKey(b0, b1)) { // 回车
                        break;
                    } else if (byteCount > MAX_PARAGRAPH) {// 空格
                        if (b0 == 0x30 && b1 == 0x00) {
                            break;
                        }
                    }
                    byteCount++;
                }
            } else {
                while (i < mBufferLength) {
                    b0 = getContentByte(i++);
                    if (b0 == 0x0a) { // 回车
                        break;
                    }
                }
            }
            _lastStartPosition = nStart;
        }
        _lastEndPosition = i;
        int nParaSize = i - nStart;
        byte[] buf = new byte[nParaSize];
        // buf = getContentByte(nFromPos, nFromPos + i);
        for (i = 0; i < nParaSize; i++) {
            buf[i] = getContentByte(nFromPos + i);
        }
        return buf;
    }

    /**
     * 是否是回车字节
     *
     * @param b0
     * @param b1
     * @return
     */
    private boolean isEnterKey(byte b0, byte b1) {
        return (b0 == 0x20 && b1 == 0x29) || (b0 == 0x00 && b1 == 0x0a)
                || (b0 == 0x0a && b1 == 0x00);
    }

    private boolean isUnicode() {
        return mEncode.toUpperCase().equals("UNICODE");
    }

    public byte getContentByte(int position) {
        if(mMappedByteBuffer != null){
            return mMappedByteBuffer.get(position);
        }
        return 0x0a;
    }


    /** 获取书籍信息
     * @return the bookInfo
     */
    public BookInfo getBookInfo() {
        return bookInfo;
    }

    /** 获取书籍目录；不为NULL，如果没有目录，SIZE为0
     * @return the catalog
     */
    public ArrayList<Catalog> getCatalog() {
        return catalog;
    }


    public void recyle() {
        try {
            mFileChannel.close();
            mRandomAccessFile.close();
            if(start == mBufferLength){
                mChapterControll.save();
            }
        } catch (Exception e) {
        }
        start = Integer.MAX_VALUE;
        mMappedByteBuffer = null;
        System.gc();
    }

    public Catalog getCatalogByIndex(int index) {
        return getCatalog().get(index);
    }

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

    public Chapter getChapter(int chapterIndex) throws Exception {
        if(mMappedByteBuffer == null){
            return null;
        }
        int position = chapterIndex;
        ArrayList<Catalog> chapterIds = getCatalog();
        if (position >= 0){
            if (position >= chapterIds.size() - 1){
                byte[] bytes = new byte[mBufferLength - chapterIds.get(position).getIndex()];
                fillBytes(bytes, chapterIds.get(position).getIndex(), bytes.length);
                return new Chapter(chapterIds.get(position).getIndex()+"", chapterIds.get(position).getText(), bytes);
            }else{
                byte[] bytes = new byte[chapterIds.get(position + 1).getIndex() - chapterIds.get(position).getIndex()];
                fillBytes(bytes, chapterIds.get(position).getIndex(), bytes.length);
                return new Chapter(chapterIds.get(position).getIndex()+"", chapterIds.get(position).getText(), bytes);
            }
        }
        return null;
    }

    private void fillBytes(byte[] dst, int offset, int length){
        for (int i = 0; i < length; i++){
            dst[i] = mMappedByteBuffer.get(offset + i);
        }
    }

    public Resource findResource(String path){
        return null;
    }

    public int getChapterPosition(String chapterID) {
        int id = Integer.parseInt(chapterID);
        for (int i = 0; i<catalog.size();i++){
            if (catalog.get(i).getIndex() == id){
                return i;
            }
        }
        return 0;
    }
}
