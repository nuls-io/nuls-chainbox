package io.nuls.controller;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.controller.core.BaseController;
import io.nuls.controller.core.Result;
import io.nuls.controller.vo.CreateMailAddressReq;
import io.nuls.service.dto.MailAddress;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.rpc.AccountTools;
import io.nuls.rpc.LegderTools;
import io.nuls.rpc.TransactionTools;
import io.nuls.rpc.vo.Account;
import io.nuls.rpc.vo.AccountBalance;
import io.nuls.service.MailAddressService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 21:03
 * @Description: 功能描述
 */
@Path("/")
@Component
public class CreateMailAddressController implements BaseController {


    @Autowired
    Config config;

    @Autowired
    AccountTools accountTools;

    @Autowired
    LegderTools legderTools;

    @Autowired
    TransactionTools transactionTools;

    @Autowired
    MailAddressService mailAddressService;


    /**
     * 生成一个邮件收件地址
     * 需要扣除1个NULS作为手续费
     *
     * @param req
     * @return
     */
    @Path("createMailAddress")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<String> createMailAddress(CreateMailAddressReq req)  {
        return call(() -> {
            Transaction tx = null;
            int chainId = config.getChainId();
            if (!AddressTool.validAddress(chainId, req.getAddress())) {
                throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR);
            }
            if (!validMailAddress(req.getMailAddress())) {
                throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR);
            }
            //验证账户有效性
            accountTools.accountValid(config.getChainId(), req.getAddress(), req.getPassword());
            Account account = accountTools.getAccountByAddress(req.getAddress());
            if (mailAddressService.hasAddressOrMailAddress(req.getAddress(), req.getMailAddress())) {
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "address  already set mail address : " + req.getMailAddress());
            }
            tx = createMailAddressTrasactionWithoutSign(account, req.getMailAddress());
//        //签名别名交易
            signTransaction(tx, account, req.getPassword());
            if (!transactionTools.newTx(tx)) {
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED);
            }
            return new Result<>(tx.getHash().toHex());
        });
    }

    private Transaction createMailAddressTrasactionWithoutSign(Account account, String mailAddress) throws NulsException, IOException {
        Transaction tx = null;
        tx = new Transaction();
        tx.setType(Constant.TX_TYPE_CREATE_MAIL_ADDRESS);
        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
        MailAddress ma = new MailAddress();
        ma.setAddress(AddressTool.getAddress(account.getAddress()));
        ma.setMailAddress(mailAddress);
        ma.setPubKey(HexUtil.decode(account.getPubkeyHex()));
        tx.setTxData(ma.serialize());
        //设置别名烧毁账户所属本链的主资产
        int assetsId = config.getAssetId();
        //查询账本获取nonce值
        AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), account.getAddress(), config.getChainId(), assetsId);
        byte[] nonce = RPCUtil.decode(accountBalance.getNonce());
        byte locked = 0;
        CoinFrom coinFrom = new CoinFrom(ma.getAddress(), config.getChainId(), assetsId, config.getMailAddressFee(), nonce, locked);
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS), config.getChainId(), assetsId, config.getMailAddressFee());
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = config.getMailAddressFee().add(fee);
        coinFrom.setAmount(totalAmount);
        //检查余额是否充足
        BigInteger mainAsset = accountBalance.getAvailable();
        //余额不足
        if (BigIntegerUtils.isLessThan(mainAsset, totalAmount)) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "insufficient fee");
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(Arrays.asList(coinFrom));
        coinData.setTo(Arrays.asList(coinTo));
        tx.setCoinData(coinData.serialize());
        //计算交易数据摘要哈希
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        return tx;
    }

    private boolean validMailAddress(String mailAddress) {
        return StringUtils.isNotBlank(mailAddress) && mailAddress.trim().length() < 50;
    }

    /**
     * 获取指定地址的邮箱地址
     *
     * @param address
     * @return
     */
    @Path("getMailAddress/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Result<String> getMailAddress(@PathParam("address") String address) {
        return call(()-> new Result(mailAddressService.getMailAddress(address).get().getMailAddress()));
    }


}
