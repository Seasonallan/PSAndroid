package com.filecoinj.model;

import java.io.Serializable;
import java.math.BigInteger;

public class EasySend implements Serializable {
    /**
     * 转账方地址
     */
    private String from;
    /**
     * 收账方地址
     */
    private String to;
    /**
     * 转账金额
     */
    private BigInteger value;
    /**
     * 私钥
     */
    private String privatekey;
}
