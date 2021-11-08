package com.filecoinj.model;

import java.io.Serializable;

public class FileTransaction implements Serializable {
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasFeeCap() {
        return gasFeeCap;
    }

    public void setGasFeeCap(String gasFeeCap) {
        this.gasFeeCap = gasFeeCap;
    }

    public String getGasPremium() {
        return gasPremium;
    }

    public void setGasPremium(String gasPremium) {
        this.gasPremium = gasPremium;
    }

    public Long getMethod() {
        return method;
    }

    public void setMethod(Long method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
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

    public FileTransaction() {
    }
}
