package io.nuls;

import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;

import java.io.File;
import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 13:53
 * @Description: 功能描述
 */
@Configuration(domain = "mail-example")
public class Config {

    /**
     * 当前运行的chain id 来自配置文件
     */
    private int chainId;

    /**
     * 默认资产id
     */
    private int assetId;

    /**
     * 申请邮箱地址手续费
     */
    private BigInteger mailAddressFee;

    /**
     * 发送邮件手续费
     */
    private BigInteger sendMailFee;

    private String dataPath;


    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getMailAddressFee() {
        return mailAddressFee;
    }

    public void setMailAddressFee(BigInteger mailAddressFee) {
        this.mailAddressFee = mailAddressFee;
    }


    public String getDataPath() {
        return dataPath + File.separator + "mail";
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public BigInteger getSendMailFee() {
        return sendMailFee;
    }

    public void setSendMailFee(BigInteger sendMailFee) {
        this.sendMailFee = sendMailFee;
    }
}
