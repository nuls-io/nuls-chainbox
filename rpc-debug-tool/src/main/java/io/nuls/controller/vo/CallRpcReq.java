package io.nuls.controller.vo;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-25 15:48
 * @Description: 功能描述
 */
public class CallRpcReq {

    private String module;

    private String rpcName;

    private Map<String,Object> param;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
}
