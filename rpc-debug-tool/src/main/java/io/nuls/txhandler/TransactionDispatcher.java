package io.nuls.txhandler;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易回调函数转发器
 */
@Component
public final class TransactionDispatcher extends BaseCmd {

    @FunctionalInterface
    public interface Handler {
        boolean call(int chainId, Transaction tx, BlockHeader blockHeader);
    }

    @Autowired
    TransactionProcessor transactionProcessor;

    /**
     * 交易验证回调函数
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txValidator(Map params) {
        List<String> finalInvalidTxs = new ArrayList<>();
        Map<String, List<String>> resultMap = new HashMap<>(2);
        handle(params,(chainId,tx,blockHeader)-> {
            if(!transactionProcessor.validate(chainId, tx, blockHeader)){
                finalInvalidTxs.add(tx.getHash().toHex());
            }
            return true;
        });
        resultMap.put("list", finalInvalidTxs);
        return success(resultMap);
    }


    /**
     * 保存交易数据回调函数
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txCommit(Map params) {
        return handle(params,(chainId,tx,blockHeader)-> transactionProcessor.commit(chainId,tx,blockHeader));
    }

    /**
     * 回滚交易数据回调函数
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txRollback(Map params) {
        return handle(params,(chainId,tx,blockHeader)-> transactionProcessor.rollback(chainId,tx,blockHeader));
    }

    private Response handle(Map params, Handler handler){
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        String blockHeaderStr = (String) params.get("blockHeader");
        BlockHeader blockHeader = null;
        if (StringUtils.isNotBlank(blockHeaderStr)) {
            blockHeader = RPCUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        }
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        List<String> txList = (List<String>) params.get("txList");
        for (String txStr : txList) {
            Log.info(txStr);
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            handler.call(chainId,tx,blockHeader);
        }
        Map<String, Boolean> resultMap = new HashMap<>(2);
        resultMap.put("value", true);
        return success(resultMap);
    }

}
