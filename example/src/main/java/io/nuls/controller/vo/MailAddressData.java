package io.nuls.controller.vo;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-14 11:48
 * @Description: 功能描述
 */
public class MailAddressData {

    private String address;

    private String pubKey;

    private String mailAddress;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }
}
