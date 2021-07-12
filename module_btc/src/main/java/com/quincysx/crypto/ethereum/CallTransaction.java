/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.quincysx.crypto.ethereum;

import com.quincysx.crypto.ethereum.solidity.SolidityType;
import com.quincysx.crypto.ethereum.utils.ByteUtil;
import com.quincysx.crypto.utils.KECCAK256;

import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import static com.quincysx.crypto.ethereum.solidity.SolidityType.IntType;
import static java.lang.String.format;

/**
 * Creates a contract function call transaction.
 * Serializes arguments according to the function ABI .
 * <p>
 * Created by Anton Nashatyrev on 25.08.2015.
 */
public class CallTransaction {

    public static EthTransaction createRawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String toAddress,
                                                      BigInteger value, byte[] data) {
        EthTransaction tx = new EthTransaction(BigIntegers.asUnsignedByteArray(nonce),
                BigIntegers.asUnsignedByteArray(gasPrice),
                BigIntegers.asUnsignedByteArray(gasLimit),
                toAddress == null ? null : Hex.decode(toAddress),
                BigIntegers.asUnsignedByteArray(value),
                data,
                null);
        return tx;
    }


    /**
     * 创建交易
     *
     * @param nonce     nonce
     * @param gasPrice  gasPrice
     * @param gasLimit  gasLimit
     * @param toAddress toAddress
     * @param value     value
     * @param callFunc  调用合约方法
     * @param funcArgs  合约参数（Int 数值请使用BigInteger或字符串）
     * @return 交易
     */
    public static EthTransaction createCallTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String toAddress,
                                                       BigInteger value, Function callFunc, Object... funcArgs) {
        byte[] callData = callFunc.encode(funcArgs);
        return createRawTransaction(nonce, gasPrice, gasLimit, toAddress, value, callData);
    }

    public static class Param {
        public Boolean indexed;
        public String name;
        public SolidityType type;

        public String getType() {
            return type.getName();
        }
    }

    public enum FunctionType {
        constructor,
        function,
        event,
        fallback
    }

    public static class Function {
        public boolean anonymous;
        public boolean constant;
        public boolean payable;
        public String name = "";
        public Param[] inputs = new Param[0];
        public Param[] outputs = new Param[0];
        public FunctionType type;

        private Function() {
        }

        public byte[] encode(Object... args) {
            return ByteUtil.merge(encodeSignature(), encodeArguments(args));
        }

        public byte[] encodeArguments(Object... args) {
            if (args.length > inputs.length)
                throw new RuntimeException("Too many arguments: " + args.length + " > " + inputs.length);

            int staticSize = 0;
            int dynamicCnt = 0;
            // calculating static size and number of dynamic params
            for (int i = 0; i < args.length; i++) {
                Param param = inputs[i];
                if (param.type.isDynamicType()) {
                    dynamicCnt++;
                }
                staticSize += param.type.getFixedSize();
            }

            byte[][] bb = new byte[args.length + dynamicCnt][];

            int curDynamicPtr = staticSize;
            int curDynamicCnt = 0;
            for (int i = 0; i < args.length; i++) {
                if (inputs[i].type.isDynamicType()) {
                    byte[] dynBB = inputs[i].type.encode(args[i]);
                    bb[i] = IntType.encodeInt(curDynamicPtr);
                    bb[args.length + curDynamicCnt] = dynBB;
                    curDynamicCnt++;
                    curDynamicPtr += dynBB.length;
                } else {
                    bb[i] = inputs[i].type.encode(args[i]);
                }
            }
            return ByteUtil.merge(bb);
        }

        private Object[] decode(byte[] encoded, Param[] params) {
            Object[] ret = new Object[params.length];

            int off = 0;
            for (int i = 0; i < params.length; i++) {
                if (params[i].type.isDynamicType()) {
                    ret[i] = params[i].type.decode(encoded, IntType.decodeInt(encoded, off).intValue());
                } else {
                    ret[i] = params[i].type.decode(encoded, off);
                }
                off += params[i].type.getFixedSize();
            }
            return ret;
        }

        public Object[] decode(byte[] encoded) {
            //import static org.apache.commons.lang3.ArrayUtils.subarray;
            //return decode(subarray(encoded, 4, encoded.length), inputs);
            return decode(ByteUtils.subArray(encoded, 4, encoded.length), inputs);
        }

        public Object[] decodeResult(byte[] encodedRet) {
            return decode(encodedRet, outputs);
        }

        public String formatSignature() {
            StringBuilder paramsTypes = new StringBuilder();
            for (Param param : inputs) {
                paramsTypes.append(param.type.getCanonicalName()).append(",");
            }
            //import static org.apache.commons.lang3.StringUtils.stripEnd;
            //return format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
            return format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
        }

        public static String stripEnd(final String str, final String stripChars) {
            int end;
            if (str == null || (end = str.length()) == 0) {
                return str;
            }


            if (stripChars == null) {
                while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                    end--;
                }
            } else if (stripChars.isEmpty()) {
                return str;
            } else {
                while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                    end--;
                }
            }
            return str.substring(0, end);
        }

        public byte[] encodeSignatureLong() {
            String signature = formatSignature();
//            byte[] sha3Fingerprint = sha3(signature.getSignBytes());
            byte[] sha3Fingerprint = KECCAK256.keccak256(signature.getBytes());
            return sha3Fingerprint;
        }

        public byte[] encodeSignature() {
            return Arrays.copyOfRange(encodeSignatureLong(), 0, 4);
        }

        @Override
        public String toString() {
            return formatSignature();
        }


        public static Function fromSignature(String funcName, String... paramTypes) {
            return fromSignature(funcName, paramTypes, new String[0]);
        }

        public static Function fromSignature(String funcName, String[] paramTypes, String[] resultTypes) {
            Function ret = new Function();
            ret.name = funcName;
            ret.constant = false;
            ret.type = FunctionType.function;
            ret.inputs = new Param[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                ret.inputs[i] = new Param();
                ret.inputs[i].name = "param" + i;
                ret.inputs[i].type = SolidityType.getType(paramTypes[i]);
            }
            ret.outputs = new Param[resultTypes.length];
            for (int i = 0; i < resultTypes.length; i++) {
                ret.outputs[i] = new Param();
                ret.outputs[i].name = "res" + i;
                ret.outputs[i].type = SolidityType.getType(resultTypes[i]);
            }
            return ret;
        }


    }

}
