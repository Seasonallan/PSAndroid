package com.filecoinj;

import java.io.Serializable;

public class FileTransaction implements Serializable {
    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public Long getNonce() {
        return nonce;
    }

    public String getValue() {
        return value;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public String getGasFeeCap() {
        return gasFeeCap;
    }

    public String getGasPremium() {
        return gasPremium;
    }

    public Long getMethod() {
        return method;
    }

    public String getParams() {
        return params;
    }

    /**
     * 收款地址
     */
    public String to;
    /**
     * 转账方地址
     */
    public String from;
    /**
     * nonce值
     */
    public Long nonce;
    /**
     * 转账金额
     */
    public String value;
    /**
     * 该笔交易能消耗的最大Gas量
     */
    public Long gasLimit;
    /**
     * 用户选择支付的总手续费率
     */
    public String gasFeeCap;
    /**
     * 用户选择支付给矿工的手续费率
     */
    public String gasPremium;
    public Long method;
    public String params;
}
