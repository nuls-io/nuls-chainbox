package io.nuls.controller.vo;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-13 18:14
 * @Description: 功能描述
 */
public class ViewMailReq {

    private String mailAddress;

    private String hash;

    private String password;

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
