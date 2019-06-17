package io.nuls.txhander;

import io.nuls.Config;
import io.nuls.Constant;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.controller.vo.MailAddressData;
import io.nuls.service.dto.MailAddress;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.rpc.LegderTools;
import io.nuls.rpc.vo.AccountBalance;
import io.nuls.service.MailAddressService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 20:00
 * @Description: 邮件地址存储交易处理器
 */
@Component
public class MailAddressProcessor implements TransactionProcessor {

    @Autowired
    MailAddressService mailAddressService;

    @Autowired
    LegderTools legderTools;

    @Autowired
    Config config;

    @Override
    public int getType() {
        return Constant.TX_TYPE_CREATE_MAIL_ADDRESS;
    }

    @Override
    public boolean validate(int chainId, Transaction tx, BlockHeader blockHeader) {
        Log.debug("validate tx", tx.getTxData());
        //验证交易hash是否一致
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            MailAddress mailAddress = new MailAddress();
            mailAddress.parse(new NulsByteBuffer(tx.getTxData()));
            //验证邮箱地址是否已被使用
            //验证当前地址是否已经绑定了邮箱地址
            if(mailAddressService.hasAddressOrMailAddress(AddressTool.getStringAddressByBytes(mailAddress.getAddress()),mailAddress.getMailAddress())){
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
            if(!Arrays.equals(cf.getAddress(),mailAddress.getAddress())){
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
            if(!((ct.getAmount().add(tx.getFee())).equals(cf.getAmount()) && ct.getAmount().equals(config.getMailAddressFee()))){
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
    public boolean commit(int chainId, Transaction tx, BlockHeader blockHeader) {
        //邮件地址与公钥的对应关系
        Log.info("commit tx");
//        throw new
        MailAddress mailAddress = new MailAddress();
        try {
            MailAddressData mailAddressData = new MailAddressData();
            mailAddress.parse(new NulsByteBuffer(tx.getTxData()));
            mailAddressData.setAddress(AddressTool.getStringAddressByBytes(mailAddress.getAddress()));
            mailAddressData.setMailAddress(mailAddress.getMailAddress());
            mailAddressData.setPubKey(HexUtil.encode(mailAddress.getPubKey()));
            mailAddressService.saveMailAddress(mailAddressData);
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, Transaction tx, BlockHeader blockHeader) {
        //删除邮件地址与公钥的对应关系
        Log.info("rollback tx");
        MailAddress mailAddress = new MailAddress();
        try {
            mailAddress.parse(new NulsByteBuffer(tx.getTxData()));
            mailAddressService.removeMailAddress(mailAddress.getMailAddress());
        } catch (NulsException e) {
            e.printStackTrace();
        }

        return true;
    }
}
