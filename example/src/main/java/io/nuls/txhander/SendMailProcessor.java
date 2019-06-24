package io.nuls.txhander;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.service.dto.MailContent;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.rpc.LegderTools;
import io.nuls.rpc.vo.AccountBalance;
import io.nuls.service.MailAddressService;
import io.nuls.service.SendMailService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 20:39
 * @Description: 功能描述
 */
@Component
public class SendMailProcessor implements TransactionProcessor {

    @Autowired
    MailAddressService mailAddressService;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    Config config;

    @Autowired
    LegderTools legderTools;

    @Override
    public int getType() {
        return Constant.TX_TYPE_SEND_MAIL;
    }

    @Override
    public boolean validate(int chainId, Transaction tx, BlockHeader blockHeader) {
        Log.info("validate send mail");
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            MailContent mailContent = new MailContent();
            mailContent.parse(new NulsByteBuffer(tx.getTxData()));
            //验证当前地址是否已经绑定了邮箱地址
            if(!mailAddressService.hasAddressOrMailAddress(AddressTool.getStringAddressByBytes(mailContent.getReceiverAddress()),null)){
                return false;
            }
            //验证是否转入指定资产到手续费账户
            CoinData coinData = new CoinData();
            coinData.parse(new NulsByteBuffer(tx.getCoinData()));
            List<CoinFrom> from = coinData.getFrom();
            //支付账户只能有一个
            if(from.size() != 1) {
                return false;
            }
            CoinFrom cf = from.get(0);
            //验证支付账户是否和申请邮箱地址的账户是同一个
            if(!Arrays.equals(cf.getAddress(),mailContent.getSenderAddress())){
                return false;
            }
            List<CoinTo> to = coinData.getTo();
            //收款账户只能有一个
            if(to.size() != 1) {
                return false;
            }
            CoinTo ct = to.get(0);
            //验证收款地址是否是手续费地址
            if(!Arrays.equals(ct.getAddress(),AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS))){
                return false;
            }
            //验证支付的申请费用是否正确 出金 = 入金 + 手续费
            if(!((ct.getAmount().add(tx.getFee())).equals(cf.getAmount()) && ct.getAmount().equals(config.getSendMailFee()))){
                return false;
            }
            //验证余额是否足够支付申请费用和交易手续费
            AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), AddressTool.getStringAddressByBytes(cf.getAddress()), config.getChainId(), config.getAssetId());
            if(accountBalance.getAvailable().min(ct.getAmount().add(tx.getFee())).equals(accountBalance.getAvailable())){
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean commit(int chainId, Transaction tx,BlockHeader blockHeader) {
        Log.info("commit send mail");
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            MailContent mailContent = getTxData(tx);
            sendMailService.saveMail(tx.getHash().toHex(),mailContent);
        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, Transaction tx,BlockHeader blockHeader) {

        return false;
    }


    public MailContent getTxData(Transaction tx) throws NulsException {
        MailContent mailContent = new MailContent();
        mailContent.parse(new NulsByteBuffer(tx.getTxData()));
        return mailContent;
    }

}
