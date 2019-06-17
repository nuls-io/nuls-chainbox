package io.nuls.rpc;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.vo.Account;
import io.nuls.rpc.vo.AccountBalance;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:31
 * @Description: 功能描述
 */
@Component
public class LegderTools implements CallRpc {

    /**
     * 获取可用余额和nonce
     * Get the available balance and nonce
     */
    @SuppressWarnings("unchecked")
    public AccountBalance getBalanceAndNonce(int chainId, String address, int assetChainId, int assetId) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID,chainId);
        params.put("assetChainId", assetChainId);
        params.put("address", address);
        params.put("assetId", assetId);
        try {
            return  callRpc(ModuleE.LG.abbr, "getBalanceNonce", params,(Function<Map<String,Object>, AccountBalance>)  res->{
                if(res == null){
                    return null;
                }
                AccountBalance accountBalance = new AccountBalance();
                accountBalance.setAvailable(new BigInteger((String.valueOf(res.get("available")))));
                accountBalance.setNonce((String) res.get("nonce"));
                accountBalance.setNonceType((Integer) res.get("nonceType"));
                return accountBalance;
            });
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


}
