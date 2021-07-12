package com.ripple.core.coretypes.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.fields.UInt128Field;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt128 extends UInt<UInt128> {
    public final static UInt128 ZERO = new UInt128(0);

    public static TypeTranslator<UInt128> translate = new UINTTranslator<UInt128>() {
        @Override
        public UInt128 newInstance(BigInteger i) {
            return new UInt128(i);
        }

        @Override
        public int byteWidth() {
            return 16;
        }
    };

    public UInt128(byte[] bytes) {
        super(bytes);
    }

    public UInt128(BigInteger value) {
        super(value);
    }

    public UInt128(Number s) {
        super(s);
    }

    public UInt128(String s) {
        super(s);
    }

    public UInt128(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 16;
    }

    @Override
    public UInt128 instanceFrom(BigInteger n) {
        return new UInt128(n);
    }

    @Override
    public BigInteger value() {
        return bigInteger();
    }

    private UInt128(){}

    private static UInt128Field int128Field(final Field f) {
        return new UInt128Field(){ @Override public Field getField() {return f;}};
    }

    static public UInt128Field IndexNext = int128Field(Field.IndexNext);
    static public UInt128Field IndexPrevious = int128Field(Field.IndexPrevious);
    static public UInt128Field BookNode = int128Field(Field.BookNode);
    static public UInt128Field OwnerNode = int128Field(Field.OwnerNode);
    static public UInt128Field BaseFee = int128Field(Field.BaseFee);
    static public UInt128Field ExchangeRate = int128Field(Field.ExchangeRate);
    static public UInt128Field LowNode = int128Field(Field.LowNode);
    static public UInt128Field HighNode = int128Field(Field.HighNode);

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        translate.toBytesSink(this, to);
    }

    @Override
    public Type type() {
        return Type.UInt128;
    }
}
