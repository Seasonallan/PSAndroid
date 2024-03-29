package com.season.book.plugin.text;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.season.book.bean.Catalog;
import com.season.book.plugin.PluginManager;

/**
 * TXT格式书籍的解析
 */
public class TextPlugin extends PluginManager {

    private RandomAccessFile mRandomAccessFile;
    protected int mBufferLength = 0;
    private FileChannel mFileChannel;
    private MappedByteBuffer mMappedByteBuffer = null;
    protected String mEncode = "UTF-8";

    public TextPlugin(String filePath) {
        super(filePath);
    }

    public void init() throws Exception {
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

        findChapters();
    }

    public String getEncode(){
        return mEncode;
    }

    private void findChapters(){
        int start = 0;
        try {
            List<Catalog> res = TextChapterCache.readFile(filePath);
            if(res != null && res.size() > 0){
                catalog = (ArrayList<Catalog>) res;
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
                    catalog.add(new Catalog(name, start));
                }
            } catch (UnsupportedEncodingException e) {
                return;
            }
            start += bts.length;
        }
        if(catalog.size() == 0){
            catalog.add(new Catalog("正文", 0));
        }
        TextChapterCache.save(filePath, catalog);
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


    @Override
    public void recyle() {
        try {
            mFileChannel.close();
            mRandomAccessFile.close();
        } catch (Exception e) {
        }
        mMappedByteBuffer = null;
        System.gc();
    }

    @Override
    public String getChapter(int chapterIndex) throws Exception {
        if(mMappedByteBuffer == null){
            return null;
        }
        int position = chapterIndex;
        ArrayList<Catalog> chapterIds = getCatalog();
        if (position >= 0){
            if (position >= chapterIds.size() - 1){
                byte[] bytes = new byte[mBufferLength - chapterIds.get(position).getIndex()];
                fillBytes(bytes, chapterIds.get(position).getIndex(), bytes.length);
                return new String(bytes, mEncode);
            }else{
                byte[] bytes = new byte[chapterIds.get(position + 1).getIndex() - chapterIds.get(position).getIndex()];
                fillBytes(bytes, chapterIds.get(position).getIndex(), bytes.length);
                return new String(bytes, mEncode);
            }
        }
        return null;
    }

    @Override
    public String getFixHtml(String content) {
        StringBuffer temp = new StringBuffer();
        temp.append("<html><body><p>");
        content = content.replaceAll("\r\n", "</p><p>");
        content = content.replaceAll("\n", "</p><p>");
        content = content.replaceAll("\r", "</p><p>");
        temp.append(content);
        temp.append("</p></body></html>");
        return temp.toString();
    }

    private void fillBytes(byte[] dst, int offset, int length){
        for (int i = 0; i < length; i++){
            dst[i] = mMappedByteBuffer.get(offset + i);
        }
    }

}
