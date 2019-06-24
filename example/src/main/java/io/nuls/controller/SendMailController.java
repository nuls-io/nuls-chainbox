package io.nuls.controller;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.controller.core.BaseController;
import io.nuls.controller.core.Result;
import io.nuls.controller.vo.MailAddressData;
import io.nuls.service.dto.MailContent;
import io.nuls.controller.vo.SendMailReq;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECIESUtil;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.rpc.AccountTools;
import io.nuls.rpc.LegderTools;
import io.nuls.rpc.TransactionTools;
import io.nuls.rpc.vo.Account;
import io.nuls.rpc.vo.AccountBalance;
import io.nuls.service.MailAddressService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 21:03
 * @Description: 功能描述
 */
@Path("/")
@Component
public class SendMailController implements BaseController {

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
    @Path("sendMail")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Result<String> sendMail(SendMailReq req){
        return call(()->{
            Objects.requireNonNull(req.getMailAddress(),"mail address can't null");
            Objects.requireNonNull(req.getSenderAddress(), "sender address can't null");
            Objects.requireNonNull(req.getPassword(),"sender address password can't null");
            Objects.requireNonNull(req.getTitle(),"title can't null");
            int chainId = config.getChainId();
            //验证账户有效性
            if (!AddressTool.validAddress(chainId, req.getSenderAddress())) {
                throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR);
            }
            accountTools.accountValid(config.getChainId(), req.getSenderAddress(), req.getPassword());
            Optional<MailAddressData> mailAddressData = mailAddressService.getMailAddress(req.getSenderAddress());
            if(mailAddressData.isEmpty()){
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED,"you must apply for mail address for your account");
            }
            Account account = accountTools.getAccountByAddress(req.getSenderAddress());
            Transaction tx = createSendMailTrasaction(account, req);
            //签名别名交易
            signTransaction(tx, account, req.getPassword());
            if (!transactionTools.newTx(tx)) {
                throw new NulsRuntimeException(CommonCodeConstanst.FAILED);
            }
            return new Result<>(tx.getHash().toHex());
        });
    }

    /**
     * 组装交易
     * @param account
     * @param req
     * @return
     */
    private Transaction createSendMailTrasaction(Account account,SendMailReq req) throws IOException, NulsException {
        Optional<MailAddressData> mailAddressData = mailAddressService.getMailAddressPubKey(req.getMailAddress());
        if(mailAddressData.isEmpty()){
            throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR,"can't found mail address : " + req.getMailAddress() );
        }
        //获取邮箱地址对应的账户和公钥
        byte[] receiverAddress = AddressTool.getAddress(mailAddressData.get().getAddress());
        byte[] senderAddress = AddressTool.getAddress(req.getSenderAddress());
        if(Arrays.equals(receiverAddress,senderAddress)){
            throw new NulsRuntimeException(CommonCodeConstanst.PARAMETER_ERROR,"sender equals receiver");
        }
        Transaction tx = new Transaction();
        tx.setType(Constant.TX_TYPE_SEND_MAIL);
        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
        tx.setTxData(buildTxData(account,mailAddressData.get(),receiverAddress,senderAddress,req));
        tx.setCoinData(buildCoinData(tx,senderAddress));
        return tx;
    }

    private byte[] buildTxData(Account account, MailAddressData mailAddressData, byte[] receiverAddress, byte[] senderAddress, SendMailReq req) throws IOException {
        //生成发件人和收件人阅读权限的key
        //生成临时私钥
        ECKey key = new ECKey();
        byte[] receiverKey = ECIESUtil.encrypt(HexUtil.decode(mailAddressData.getPubKey()),key.getPrivKeyBytes());
        byte[] senderKey = ECIESUtil.encrypt(HexUtil.decode(account.getPubkeyHex()),key.getPrivKeyBytes());
        MailContent content = new MailContent();
        content.setContent(ECIESUtil.encrypt(key.getPubKey(),req.getContent().getBytes(StandardCharsets.UTF_8)));
        content.setTitle(ECIESUtil.encrypt(key.getPubKey(),req.getTitle().getBytes(StandardCharsets.UTF_8)));
        content.setReceiverAddress(receiverAddress);
        content.setReceiverKey(receiverKey);
        content.setSenderAddress(senderAddress);
        content.setSenderKey(senderKey);
        content.setTimestamp(System.currentTimeMillis());
        return content.serialize();
    }

    private byte[] buildCoinData(Transaction tx,byte[] senderAddress) throws IOException, NulsException {
        AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), AddressTool.getStringAddressByBytes(senderAddress), config.getChainId(), config.getAssetId());
        byte locked = 0;
        byte[] nonce = RPCUtil.decode(accountBalance.getNonce());
        CoinFrom coinFrom = new CoinFrom(senderAddress, config.getChainId(), config.getAssetId(), config.getSendMailFee(), nonce, locked);
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS), config.getChainId(), config.getAssetId(), config.getSendMailFee());
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = config.getSendMailFee().add(fee);
        //验证发件人余额是否足够
        if(accountBalance.getAvailable().min(config.getSendMailFee().add(tx.getFee())).equals(accountBalance.getAvailable())){
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "insufficient fee");
        }
        coinFrom.setAmount(totalAmount);
        CoinData coinData = new CoinData();
        coinData.setFrom(List.of(coinFrom));
        coinData.setTo(List.of(coinTo));
        return coinData.serialize();
    }




}
