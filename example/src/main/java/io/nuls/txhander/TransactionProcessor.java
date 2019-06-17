package io.nuls.txhander;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-11 20:04
 * @Description: 功能描述
 */
public interface TransactionProcessor {

    int getType();

    /**
     * 验证接口
     *
     * @param chainId       链Id
     * @param tx           类型为{@link #getType()}的所有交易
     * @param blockHeader   区块头
     * @return 未通过验证的交易,需要丢弃
     */
    boolean validate(int chainId, Transaction tx, BlockHeader blockHeader);

    /**
     * 提交接口
     *
     * @param chainId       链Id
     * @param tx           类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否提交成功
     */
    boolean commit(int chainId, Transaction tx, BlockHeader blockHeader);

    /**
     * 回滚接口
     *
     * @param chainId       链Id
     * @param tx          类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否回滚成功
     */
    boolean rollback(int chainId, Transaction tx, BlockHeader blockHeader);




}
