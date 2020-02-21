package com.season.lib.plugin.umd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.season.lib.bean.Catalog;
import com.season.lib.plugin.PluginManager;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * UMD格式书籍的解析
 */
public class UmdPlugin extends PluginManager {

    public UmdPlugin(String filePath) {
        super(filePath);
    }

    /** UMD file format */
    public static final long UMD_FORMAT = 0xde9a9b89L;
    /** UMD file type: text and picture */
    public static final byte UMD_BOOK_TYPE_TEXT = 1;
    public static final byte UMD_BOOK_TYPE_PICTURE = 2;

    private byte bookType;
    public int contentLength;
    private long additionalCheck;

    public int[] chapOff;
    public String mTitle, mAuthor;
    private ArrayList<UmdDataBlock.BlockEntity> contentArr;
    private long currentPoint;
    private UmdDataBlock mUmdDataBlock;

    @Override
    public void init(String secretKey) throws Exception {
        contentArr = new ArrayList<UmdDataBlock.BlockEntity>();
        currentPoint = 0L;
        contentLength = 0;
        additionalCheck = 0L;
        InputStream is = null;
        try {
            is = new DataInputStream(new FileInputStream(filePath));
            read(is);
            mUmdDataBlock = new UmdDataBlock(contentLength,
                    chapOff, contentArr, filePath);
            if (bookType == UMD_BOOK_TYPE_PICTURE) {// 暂不支持漫画格式
                throw new NullPointerException("抱歉，不支持漫画格式的UMD书籍！");
            }
        } catch (Exception e) {
            String str = "";
            if (e != null) {
                str = e.getMessage();
            }
            throw new NullPointerException(str);
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                }
            }
        }
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


    /**
     * parse the UMD file and read the content of UMD file
     *
     * @param is
     * @throws Exception
     */
    public void read(InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);
        long header;
        // check UMD format
        header = readUInt32(dis);
        if (UMD_FORMAT != header) {
            throw new Exception("不是正确的UMD格式!");
        }
        byte[] symbol = new byte[1];
        // int eof = dis.read(symbol);
        int eof = readBytes(dis, symbol);
        while (eof != -1 && 0x23 == symbol[0]) {// 0x23 '#'
            short id = readInt16(dis);
            // byte num3 = dis.readByte();
            // byte length = (byte) (dis.readByte() - 5);
            readByte(dis);
            byte length = (byte) (readByte(dis) - 5);
            readSection(id, length, dis);
            // eof = dis.read(symbol);
            eof = readBytes(dis, symbol);
            if ((0xf1 == id) || (10 == id)) {
                id = 0x84;
            }
            // System.out.println("eof " + symbol[0]);
            while (eof != -1 && '$' == symbol[0]) {
                // System.out.println("start $");
                long num5 = readUInt32(dis);
                long num6 = readUInt32(dis) - 9;
                readAdditional(id, num5, num6, dis);
                // eof = dis.read(symbol);
                eof = readBytes(dis, symbol);
                // System.out.println("end $");
                // System.out.println("eof " + symbol[0]);
            }
        }
        dis.close();
    }
    public Bitmap mBookCover;
    /**
     * read the content
     *
     * @param id
     * @param check
     * @param length
     * @param dis
     * @throws Exception
     */
    private void readAdditional(short id, long check, long length,
                                DataInputStream dis) throws Exception {
        int num1;
        int num2;
        byte[] buffer1;
        // LogUtil.v(TAG, "id " + id + "; check " + check + "; length " +
        // length);
        switch (id) {
            case 0x0E:// only picture
                if (UMD_BOOK_TYPE_PICTURE == bookType) {
                    this.contentArr.add(new UmdDataBlock.BlockEntity(currentPoint, length));
                }
                readBytes(dis, (int) length);
                return;
            case 0x0F:// only text
                return;
            case 0x81:
                readBytes(dis, (int) length);
                return;
            case 0x82:// cover image 130
                mBookCover = Bytes2Bimap(readBytes(dis, (int) length));
                return;
            case 0x83:// each chapter length
                int chapOffLen = (int) (length / 4);
                this.chapOff = new int[chapOffLen];
                num1 = 0;
                while (num1 < chapOffLen) {
                    this.chapOff[num1] = readInt32(dis);
                    num1++;
                }
                return;
            case 0x84:
                // read the content
                if (this.additionalCheck != check) {
                    this.contentArr.add(new UmdDataBlock.BlockEntity(currentPoint, length));
                    readBytes(dis, (int) length);
                    return;
                }
                num2 = 0;
                buffer1 = readBytes(dis, (int) length);
                if (null == buffer1) {
                    return;
                }
                // read the chapter's name
                while (num2 < buffer1.length) {
                    byte num3 = buffer1[num2];
                    byte[] temp = new byte[num3];
                    ++num2;
                    for (int i = 0; i < num3; ++i) {
                        temp[i] = buffer1[i + num2];
                    }
                    String title = new String(IntergerUtil.getReverseBytes(temp),
                            "UNICODE");
                    this.catalog.add(new Catalog(title, catalog.size()));
                    num2 += num3;
                }
                return;
            default:
                readBytes(dis, (int) length);
                return;
        }
    }

    private Bitmap Bytes2Bimap(byte[] b){
        if(b.length!=0){
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        else {
            return null;
        }
    }

    private void readSection(short id, byte length, DataInputStream dis)
            throws Exception {
        // LogUtil.v(TAG, "id " + id + "; b " + b + "; length " + length);
        switch (id) {
            case 1:// type
                bookType = readByte(dis);
                readInt16(dis);// 随机数
                return;
            case 2:// title
                mTitle = readString(dis, length);
                return;
            case 3:// author
                mAuthor = readString(dis, length);
                return;
            case 4:// year
                readString(dis, length);
                return;
            case 5:// month
                readString(dis, length);
                return;
            case 6:// day
                readString(dis, length);
                return;
            case 7:// gender
                readString(dis, length);
                return;
            case 8:// publisher
                readString(dis, length);
                return;
            case 9:// vendor
                readString(dis, length);
                return;
            case 10:
                readInt32(dis);
                return;
            case 11:// 文章长度
                this.contentLength = readInt32(dis);
                return;
            case 12:
                readUInt32(dis);
                return;
            case 0x81:
            case 0x83:
            case 0x84:
                this.additionalCheck = readUInt32(dis);
                return;
            case 0x0E:
                readByte(dis);
                return;
            case 0x0F:
                readByte(dis);
                return;
            case 0x82:// 130
                readByte(dis);
                this.additionalCheck = readUInt32(dis);
                return;
        }
        readBytes(dis, length);
    }

    private long readUInt32(DataInputStream dis) throws Exception {
        return IntergerUtil.int2long(IntergerUtil.getInt(readBytes(dis, 4)));
    }

    private int readInt32(DataInputStream dis) throws Exception {
        return IntergerUtil.getInt(readBytes(dis, 4));
    }

    private short readInt16(DataInputStream dis) throws Exception {
        return IntergerUtil.getShort(readBytes(dis, 2));
    }

    private byte[] readBytes(DataInputStream dis, int num) throws Exception {
        if (num <= 0 || null == dis) {
            return null;
        }
        byte[] value = new byte[num];
        long temp = dis.read(value);
        currentPoint += temp;
        return value;
    }

    private int readBytes(DataInputStream dis, byte[] bytes) throws Exception {
        currentPoint += bytes.length;
        return dis.read(bytes);
    }

    private byte readByte(DataInputStream dis) throws Exception {
        currentPoint += 1;
        return dis.readByte();
    }

    private String readString(DataInputStream dis, byte length)
            throws Exception {
        return new String(IntergerUtil.getReverseBytes(readBytes(dis, length)),
                "UNICODE");
    }



    @Override
    public void recyle() {
        if(mUmdDataBlock != null){
            mUmdDataBlock.release();
        }
        mUmdDataBlock = null;
        if (contentArr != null) {
            contentArr.clear();
        }
        contentArr = null;
        chapOff = null;
        System.gc();
    }


    @Override
    public String getChapter(int chapterIndex) throws Exception {
        int mBufferLength = mUmdDataBlock.getCurrentChapterLength(chapterIndex);
        mUmdDataBlock.setCurrentChapterIndex(chapterIndex);
        byte[] bytes = new byte[mBufferLength];
        fillBytes(bytes, 0, bytes.length);
        return new String(bytes, "UNICODE");

    }

    private void fillBytes(byte[] dst, int offset, int length) throws Exception {
        for (int i = 0; i < length; i++){
            dst[i] = mUmdDataBlock.getByteAtPosition(offset + i);
        }
    }

}
