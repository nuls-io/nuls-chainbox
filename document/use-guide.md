# 目录结构介绍
## tools
NULS引擎操作入口，提供获取程序、集成打包等操作。[命令参数文档](#cmd-doc)。
## document
文档列表
## example
基于java模块模板开发的一个加密邮件的示例模块程序源码。
## template
存储目前支持的语言的模板列表
# 准备开发环境
1. 暂时只支持macOS,centos,ubuntu等linux内核的操作系统。
2. NULS引擎会通过git命令在github.com上拉取代码，需要提前安装git客户端程序。
3. NULS2.0是基于JAVA语言开发，要运行NULS2.0节点需要安装JDK，目前依赖的JDK版本是jdk-11.0.2

# 获取NULS2.0运行环境
NULS2.0运行环境包含一套最基础的区块链程序，里面包含了账户、账本、区块、网络、交易、共识（poc）6大核心模块。运行NULS2.0基础运行环境你可以得到包含账户模型、转账交易、POC共识激励等区块链底层的核心功能。如果只想发一条简单的转账交易的链，修改一下配置文件就完成了（完整的配置列表）。你可以在基础环境中集成自己的业务模块，通过扩展一个新的交易类型的方式完成自己的业务，在下面一个段落中我会详细介绍如何构建自己的业务。

使用tools脚本获取NULS2.0运行环境

```
./tools -n
```
脚本会首先检查当前环境，然后从拉取NULS2.0在github仓库里的代码，执行package完成NULS2.0编译打包，将可运行程序输出到./NULS-WALLET-RUNTIME目录中。
当看到以下内容时表示打包完成。

```
============ ~/nuls-engine/NULS-WALLET-RUNTIME PACKAGE FINISH 🍺🍺🍺🎉🎉🎉 ===============
```
## NULS-WALLET-RUNTIME目录结构
### start-mykernel
启动节点
### stop-mykernel
停止节点
### check-status 
检查各个模块运行状态
### cmd
命令行启动脚本
### create-address
创建地址工具
### nuls.ncf
配置文件（首次运行start-mykernel脚本后创建）
#### 更多使用方法参考（NULS2.0钱包使用手册）
## 如何开发自己的模块
NULS2.0是用JAVA语言编写的分布式微服务架构的程序，整个节点程序由多个模块组成，每个模块之间通过websocket协议通信。NULS2.0定义了一套标准的[模块通信协议](https://github.com/nuls-io/nuls-v2-docs/blob/master/design-zh-CHS/r.rpc-tool-websocket%E8%AE%BE%E8%AE%A1v1.3.md)，可以通过各种开发语言实现此标准协议与其他模块通信，进而实现自己的业务逻辑。扩展自己的业务逻辑主要是通过扩展新的交易类型实现，在交易的txData中存储自己的业务数据，txData将跟随交易存储在链上。
### 创建交易流程
![节点创建交易](./document/images/createtx.png)
### 处理网络交易流程
![处理网络广播交易](./document/images/handnetworktx.png)

从图中可以看出扩展一个新的业务模块主要需要做4件事
1. 在交易模块注册自己的交易类型。
2. 组装交易数据，调用交易模块创建新的交易。
3. 验证交易中的业务数据是否合法。
4. 将交易中的业务数据保存到节点数据库中。

当然除了上面4步，还需要根据具体的业务需求对业务数据进行使用。下面我就对以上4步进行详细介绍。

在系统中每种交易都需要定义一个整数类型的唯一的交易类型（扩展的交易通常用200以上的值），用于区分处理的交易的回调函数。通常应该在模块启动的时候调用交易模块提供的注册交易接口（请查看交易模块的RPC接口文档）。当交易模块拿到一条待处理的交易时，会根据交易类型路由到注册的验证函数对交易业务数据的合法性进行校验。除了验证以外还有commitTx(保存交易业务数据）、rollbackTx（回滚交易的业务数据）两个函数。

通常由业务模块组装自己扩展的交易类型，一条合法的交易中包括交易类型、时间戳、CoinData、txData、备注、签名几个部分。其中CoinData中包含了转账数据，转出账户、转入账户、转账金额、资产信息等。而txData中主要用了保存业务数据，底层不会对txData字段进行验证和处理，业务模块根据业务设计在txData中存储自己的业务数据。签名字段通过椭圆形曲线算法对所有交易数据进行签名，确保在传输过程中数据不被串改。组装完成后，调用交易模块接口创建交易。

交易模块会通过当前节点自己创建获得交易，也会通过网络模块接收其他节点广播过来的交易。交易模块拿到交易后，首先会对交易数据的参数是否合法，然后检查账户余额是否足够支付交易手续费，然后验证账户的nonce值(通过控制交易顺序来保证余额不被重复使用的一种算法）是否合法。验证通过后根据交易类型找到业务验证的回调函数，对交易进行业务验证。

最后当交易打入区块，并且区块已经确认后，将在通过交易类型找到存储业务数据的回调函数，通知业务模块可以保持业务数据导节点本地。有些情况可能会出现区块回滚。当区块发生回滚时，也会通过交易类型匹配到对应的交易回滚回调函数，对业务数据进行回滚处理。

以上就是扩展一种交易类型需要完成的几个核心步骤。验证交易、保存业务数据、回滚业务数据3个接口由业务模块实现，查看具体[接口协议](#registerTx)。
## 获取各种开发语言的模块开发模板
理论上只要通过websocket与模块建立连接，然后按照约定的协议与模块进行信息交换就可以实现业务模块的扩展。但是这样从头造轮子的方式效率比较低，门槛也比较高，为了降低模块开发的难度，我们将为各种语言提供快速开始的模板(目前只提供了java），开发人员只需要在模板中的指定位置插入具体的业务逻辑代码就可以完成扩展模块的开发。

通过tools脚本可以非常简单的获取到指定的语言的模块开发模板。

```
tools -t java 
```
执行完成后，会在当前目录创建一个nuls-module-java的文件夹，导入常用的开发工具就可以开始开发业务了。每个模板里都会有对应的使用文档。
### 模块调试方法
在模块开发过程中需要与基础模块进行联调，获取到NULS2.0运行环境后，执行start-mykernel脚本启动NULS2.0基础模块，然后在业务模块中向ws://127.0.0.1:7771地址进行注册，注册协议。完成注册后，就可以获取到所依赖的各个模块的通信地址，调用模块的接口。
## 将业务模块集成到NULS2.0运行环境中
业务模块开发完成后，需要将业务模块集成到NULS2.0运行环境中，然后将输出的程序部署到生产环境中或输出到外部节点运行。使用tools脚本完成集成需要满足以下几个约定。
1. 打包好的可运行程序应该放在模块开发目录下的outer目录下。
2. outer目录中必须有一个文件名为Module.ncf的配置文件（注意M大写）。文件内容如下（以java为例）

    ```
    [Core]
    Language=JAVA      # 注明开发语言
    Managed=1          # 1表示模块跟随节点程序一起启动，0表示手动启动
    
    [Libraries]
    JRE=11.0.2         # 模块运营环境版本
    
    [Script]
    Scripted=1         # 是否使用脚本启动  1表示是
    StartScript=start  # 启动模块脚本(start必须在outer目录下)
    StopScript=stop    # 停止模块脚本(stop必须在outer目录下)
    
    ```
3. 可以通过2中配置的脚本启动模块和停止模块。
#### 如果使用模块开发模板创建模块不用关心以上约定。
# 附录
## <span id="cmd-doc">tools脚本使用手册</span>
### 获取NULS2.0运行环境
#### 命令：tools -n
#### 参数列表
无
#### 示例
```
tools -n
```
### 获取指定语言模块开发模板
#### 命令:tools -t &lt;language> [out folder]
#### 参数列表
| 参数 | 说明 |
| --- | --- |
| &lt;language> | 语言模板名称 |
| [out folder] | 输出的文件夹名 |
#### 示例
```
tools -t java demo
```
### 查看可用模板列表
#### 命令：tools -l
#### 参数列表
无
##### 示例

```
doto
```
### 将模块集成到NULS2.0运行环境
#### 命令:tools -p &lt;module folder>
#### 参数列表
| 参数 | 说明 |
| --- | --- |
| &lt;out folder> | 模块的文件夹名 |
#### 示例
```
./tools -p demo
```
## <span id="registerTx">业务模块相关接口协议</span>
业务模块需要给交易模块提供3个回调函数，交易模块会通过websocket调用这3个函数，3个函数的参数相同，命名不同。
### 验证交易
cmd名称：txValidator

用于业务模块验证txData数据是否合法，同时也可以验证coinData等数据是否符合业务要求。如果验证不通过，交易模块将丢弃此笔交易。
### 保存交易业务数据
cmd名称：txCommit

用于将交易中的业务数据保存到节点本地数据库，或做相应的业务逻辑处理。到达此步的交易都是达成共识的数据。
### 回滚交易业务数据
cmd名称：txRollback

当区块发生回滚时，会触发回调此函数，业务模块应该在函数中清除掉此笔交易相关的业务数据，或做相应的逆向处理。
### 回调函数参数列表
| 参数名称 | 类型 | 参数说明 |
| --- | --- | --- |
| chainId | int | 链id（节点运行多链时区分数据来源） |
| txList | list | 交易列表 |
| blockHeader | object | 区块头 |

####  反序列化，[通用协议](https://github.com/nuls-io/nuls-v2-docs/blob/master/design-zh-CHS/h.%E9%80%9A%E7%94%A8%E5%8D%8F%E8%AE%AE%E8%AE%BE%E8%AE%A1v1.3.md)
txList和blockHeader两个参数的数据是通过16进制数据的形式传输，首先需要将16进制转换成byte数组，然后再根据不同的规则反序列化成结构化数据。
##### [Transaction](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/data/Transaction.java)
txList存储的是一个Transaction对象的列表，每一个item里面是Transaction对象序列化成16进制的字符串。反序列化txList首先从[通用协议](https://github.com/nuls-io/nuls-v2-docs/blob/master/design-zh-CHS/r.rpc-tool-websocket%E8%AE%BE%E8%AE%A1v1.3.md)中取出txList参数的值，是一个json的字符串数组，然后遍历数组取得单个Transaction对象的序列化值。将序列化值转换成byte数组。再从byte数组中逐个取出对应的数据值。
byte数组中读取数据的规则如下：
1. 2个byte存储无符号的16位int保存交易类型。
2. 4个byte存储无符号的32位int保存交易时间戳（1970年1月1日到当前的秒数）
3. 变长类型存储remark字符串，见[变长类型读取方式](#变长类型)
4. 变成类型存储txData字符串，业务自定义，但任然需要先转换成byte数组。
5. 变长类型存储coinData字符串，为coinData对象序列化后的16进制的字符串。见[CoinData反序列化方法](#CoinData)
6. 变长类型存储交易签名字符串,为TransactionSignature对象序列化后的16进制的字符串。

##### <span id="CoinData">[CoinData](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/data/CoinData.java)</span>
CoinData对象存储了一笔交易中出入金关系，一笔交易出金账户和入金账户支持多对多的关系，只要出金总额大于等于入金总额加手续费交易就可以成立。
1. [varint](https://learnmeabitcoin.com/glossary/varint)类型存储出金账户信息的列表个数。
2. 按顺序存储出金账户信息列表，出金账户信息为CoinFrom对象，注意此处并没有对CoinFrom对象进行16进制字符串处理。
3. [varint](https://learnmeabitcoin.com/glossary/varint)类型存储入金账户信息的列表个数。
4. 按顺序存储入金账户信息列表，入金账户信息为CoinTo对象，注意此处并没有对CoinTo对象进行16进制字符串处理。

##### <span id="CoinFrom">[CoinFrom](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/data/CoinFrom.java)</span>
1. 变长类型存储账户地址。[Address序列化代码](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/basic/AddressTool.java)
2. 2个byte存储无符号16位int保存资产链id。
3. 2个byte存储无符号16位int保存资产id。
4. 32个byte存储BigInteger类型的数值数据保存出金资产数量。
5. 变长类型存储账户nonce值。
6. 1个byte存储锁定状态（共识用）

##### <span id="CoinTo">[CoinTo](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/data/CoinTo.java)</span>
1. 变长类型存储账户地址。[Address序列化代码](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/basic/AddressTool.java)
2. 2个byte存储无符号16位int保存资产链id。
3. 2个byte存储无符号16位int保存资产id。
4. 32个byte存储BigInteger类型的数值数据保存出金资产数量。
5. 8个byte存储带符号的64位long保存锁定时间（锁定资产的时间）

##### <span id="TransactionSignature">[TransactionSignature](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/signture/TransactionSignature.java)</span>
交易前面会存在多人签名的情况，所以TransactionSignature里面存储的实际上是签名数据列表。byte数组中按顺序依次存储多个签名。反序列化时依次轮训。
1. 1个byte存储公钥长度。
2. 公钥数据（长度根据1中获取）
3. 变长类型存储签名数据。

##### <span id="BlockHeader">[BlockHeader](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/data/BlockHeader.java)</span>
BlockHeader为区块头对象，主要存储前一块的hash值、[merkle tree](https://en.wikipedia.org/wiki/Merkle_tree)的根hash值、出块时间戳、区块高度、块中的交易总数、区块签名、扩展数据。
序列化规则：
1. 32个byte存储前一个块的hash值。
2. 32个byte存储merkle根的hash值。
3. 4个byte存储无符号的32位int保存出块时间戳（1970年1月1日到当前的秒数）。
4. 4个byte存储无符号的32位int保存区块高度。
5. 4个byte存储无符号的32位int保存当前块中的交易总数。
6. 变长类型存储扩展数据。
7. 变长类型存储交易签名字符串,为BlockSignature对象序列化后的16进制的字符串。

##### <span id="BlockSignature">[BlockSignature](https://github.com/nuls-io/nuls-v2/blob/master/common/nuls-base/src/main/java/io/nuls/base/signture/BlockSignature.java)</span>
1. 1个byte存储公钥长度。
2. 公钥数据（长度根据1中获取）
3. 变长类型存储签名数据。



