package io.nuls.controller.vo;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-13 18:14
 * @Description: 功能描述
 */
public class SendMailReq {

    private String mailAddress;

    private String title;

    private String content;

    private String password;

    private String senderAddress;

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }
}
