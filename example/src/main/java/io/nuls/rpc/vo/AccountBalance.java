package io.nuls.rpc.vo;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:33
 * @Description: 功能描述
 */
public class AccountBalance {

    String nonce;

    int nonceType;

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
