package io.nuls.controller.vo;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-13 18:14
 * @Description: 功能描述
 */
public class GetMailListReq {

    private String address;

    private String password;

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
