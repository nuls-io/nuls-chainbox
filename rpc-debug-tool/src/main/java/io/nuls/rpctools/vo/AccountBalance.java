package io.nuls.rpctools.vo;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:33
 * @Description: 账户余额和nonce
 */
public class AccountBalance {


    String nonce;

    int nonceType;

    /**
     * 可用余额
     */
    BigInteger available;

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public int getNonceType() {
        return nonceType;
    }

    public void setNonceType(int nonceType) {
        this.nonceType = nonceType;
    }

    public BigInteger getAvailable() {
        return available;
    }

    public void setAvailable(BigInteger available) {
        this.available = available;
    }
}
