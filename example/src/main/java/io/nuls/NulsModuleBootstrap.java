package io.nuls;

import io.nuls.controller.core.RpcServerManager;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.TransactionTools;

import java.io.File;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-10 19:27
 * @Description: 功能描述
 */
@Service
public abstract class NulsModuleBootstrap extends RpcModule {

    @Autowired
    Config config;

    @Value("APP_NAME")
    private String moduleName;

    @Autowired
    TransactionTools transactionTools;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls",args);
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.LG),
                Module.build(ModuleE.TX),
                Module.build(ModuleE.NW)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(moduleName,ROLE);
    }

    @Override
    public boolean doStart() {
        System.out.println("do start");
        RpcServerManager.getInstance().startServer("0.0.0.0", 9999);
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        System.out.println("do running");
        //注册交易
        transactionTools.registerTx();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Running;
    }

    @Override
    public void init() {
        //初始化数据存储文件夹
        File file = new File(config.getDataPath());
        if(!file.exists()){
            file.mkdir();
        }
    }
}
