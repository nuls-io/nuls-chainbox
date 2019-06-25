package io.nuls.controller;

import io.nuls.controller.vo.CallRpcReq;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.*;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-25 15:46
 * @Description: 功能描述
 */
@Component
@Path("/rpc")
public class CallRpcController {

    @Path("/call")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Object call(CallRpcReq req){
        Log.debug("call {} rpc , method : {},param : {}",req.getModule(),req.getRpcName(),req.getParam());
        Response cmdResp = null;
        try {
            Request request = MessageUtil.newRequest(req.getRpcName(), req.getParam(), Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
            Message message = MessageUtil.basicMessage(MessageType.Request);
            message.setMessageData(request);
            message.setMessageID(String.valueOf(Long.parseLong(message.getMessageID()) + 1L));
            cmdResp = ResponseMessageProcessor.requestAndResponse(req.getModule(), req.getRpcName(), req.getParam());
            Log.debug("result : {}",cmdResp);
            return Map.of("request",message,"response",cmdResp);
        } catch (Exception e) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - message:{}", req.getModule(), req.getRpcName(), e.getMessage());
            return e.getMessage();
        }
    }

}
