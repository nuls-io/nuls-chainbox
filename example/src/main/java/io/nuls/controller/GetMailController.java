package io.nuls.controller;

import io.nuls.Config;
import io.nuls.base.basic.AddressTool;
import io.nuls.controller.core.Result;
import io.nuls.controller.vo.GetMailListReq;
import io.nuls.controller.vo.MailContentData;
import io.nuls.controller.vo.ViewMailReq;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.CryptoException;
import io.nuls.core.exception.NulsException;
import io.nuls.rpc.AccountTools;
import io.nuls.rpc.LegderTools;
import io.nuls.rpc.TransactionTools;
import io.nuls.service.MailAddressService;
import io.nuls.service.SendMailService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 21:03
 * @Description: 功能描述
 */
@Path("/")
@Component
public class GetMailController implements BaseController {

    @Autowired
    Config config;

    @Autowired
    AccountTools accountTools;

    @Autowired
    SendMailService sendMailService;

    /**
     * 生成一个邮件收件地址
     * 需要扣除1个NULS作为手续费
     *
     * @param req
     * @return
     */
    @Path("viewMail")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<MailContentData> viewMail(ViewMailReq req){
        return call(()->{
            Objects.requireNonNull(req.getAddress(),"address can't null");
            Objects.requireNonNull(req.getPassword(),"sender address password can't null");
            Objects.requireNonNull(req.getHash(),"hash can't null");
            String priKey = accountTools.getAddressPriKey(config.getChainId(),req.getAddress(),req.getPassword());
            ECKey ecKey = ECKey.fromPrivate(HexUtil.decode(priKey));
            MailContentData mcd = sendMailService.getMailContent(req.getHash(),ecKey,AddressTool.getAddress(req.getAddress()));
            return new Result<>(mcd);
        });
    }


    /**
     * 生成一个邮件收件地址
     * 需要扣除1个NULS作为手续费
     *
     * @param req
     * @return
     */
    @Path("getSendList")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<List<MailContentData>> getSendList(GetMailListReq req){
        return call(()->getMailList(req,true));
    }

    /**
     * 生成一个邮件收件地址
     * 需要扣除1个NULS作为手续费
     *
     * @param req
     * @return
     */
    @Path("getReceiveList")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<List<MailContentData>> getReceiveList(GetMailListReq req){
        return call(()-> getMailList(req,false));
    }

    public Result<List<MailContentData>> getMailList(GetMailListReq req,boolean isSender) throws NulsException, IOException, CryptoException {
        Objects.requireNonNull(req.getAddress(),"address can't null");
        Objects.requireNonNull(req.getPassword(),"sender address password can't null");
        String priKey = accountTools.getAddressPriKey(config.getChainId(),req.getAddress(),req.getPassword());
        ECKey ecKey = ECKey.fromPrivate(HexUtil.decode(priKey));
        List<MailContentData> res = sendMailService.getMailList(req.getAddress(),ecKey,isSender);
        return new Result<>(res);
    }




}
