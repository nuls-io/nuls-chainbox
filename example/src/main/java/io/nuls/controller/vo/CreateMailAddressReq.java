package io.nuls.controller.vo;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 21:10
 * @Description: 功能描述
 */
public class CreateMailAddressReq {

    public String mailAddress;

    public String address;

    public String password;

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
