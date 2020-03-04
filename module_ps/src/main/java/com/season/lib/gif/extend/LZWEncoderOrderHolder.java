package com.season.lib.gif.extend;


import com.season.lib.gif.base.LZWEncoder;

import java.io.ByteArrayOutputStream;

/**
 * CreateAt : 7/12/17
 * Describe :
 *
 * @author chendong
 */
public class LZWEncoderOrderHolder implements Comparable<LZWEncoderOrderHolder> {

    private int mOrder;
    private LZWEncoder mLZWEncoder;
    private ByteArrayOutputStream mByteArrayOutputStream;

    LZWEncoderOrderHolder(LZWEncoder lzwEncoder, int order) {
        this.mLZWEncoder = lzwEncoder;
        this.mOrder = order;
    }

    public LZWEncoderOrderHolder(LZWEncoder lzwEncoder, int order, ByteArrayOutputStream out) {
        this.mLZWEncoder = lzwEncoder;
        this.mOrder = order;
        this.mByteArrayOutputStream = out;
    }


    @Override
    public int compareTo(LZWEncoderOrderHolder another) {
        if (another == null){
            return 1;
        }
        return this.mOrder - another.mOrder;
    }

    public LZWEncoder getLZWEncoder() {
        return mLZWEncoder;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return mByteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.mByteArrayOutputStream = byteArrayOutputStream;
    }
}
