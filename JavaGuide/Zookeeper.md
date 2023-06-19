# Zookeeper

## 集群模式
`vim zoo.cfg`，增加集群节点。
```cfg
server.1=192.168.44.131:2888:3888
server.2=192.168.44.132:2888:3888
server.3=192.168.44.129:2888:3888
```
参数解读：`server.A=B:C:D`，A表示这是第几号服务器，B表示服务器地址，C表示Follower与Leader交换信息的端口，D表示执行选举时服务器相互通信的端口。

### zookeeper选举机制第一次启动
![](../image/zookeeper_选举机制_第一次启动.png)

Zookeeper第一次启动总结：
1. 服务器1启动，发起一次选举。服务器1投给自己一票。此时服务器1票数为一票，发现票数不够半数（3票），选举无法完成，服务器1的状态保持为`LOOKING`。
2. 服务器2启动，发起一次选举。服务器1和2分别投给自己一票，并交换选票信息。交换选票之后服务器1发现服务器2的`myid`比自己目前投票选举的（服务器1）大，更改选票为推举服务器2。此时服务器1为0票，服务器2为2票，还是没有半数以上结果，选举无法完成，服务器1和2的状态保持`LOOKING`。
3. 服务器3启动，发起一次选举。服务器1、2和3分别投给自己一票，并交换选票信息。而此时服务器1和2都会更改选票为服务器3。`此次投票结果：服务器1为0票，服务器2为0票，服务器3为3票`。此时服务器3的票数已经超过半数，服务器3当选`Leader`。服务器1和2更改状态为`FOLLOWING`，服务器3更改状态为`LEADING`。
4. 服务器4启动，发起一次选举。此时服务器1，2，3已经不是`LOOKING`状态，不会更改选票信息。交换选票信息结果：`服务器3为3票，服务器4为1票`。此时服务器4服从多数，更改选票信息为服务器3，并更改状态为`FOLLOWING`。
5. 服务器 5启动，同4一样当`FOLLOWING`。

- SID：服务id，用来唯一标识一台Zookeeper集群中的机器，且不能重复，和myid一致。
- ZXID：事务id，用来标识依次服务状态的变更。在某一时刻，集群中的每台ZXID不一定完全一致，这和Zookeeper服务器对于客户端`更新请求`的处理逻辑有关。每次写操作都有事务id（zxid）。
- Epoch：每个Leader任期的代号。没有Leader时同一轮投票过程中的逻辑时钟时相同的，每投完一次选票这个数据就会增加。

<span style="color: red">注意：</span>两个节点各自生成各自的选票，选票就是SID、ZXID，两个节点各自选出ZXID/SID最大的，放到投票箱当中。初始化选举Leader规则：①事务id大的胜出 ；②事务id相同，服务器id大的胜出

### zookeeper选举机制非第一次启动
当Zookeeper集群中的一台服务器出现`服务器初始化启动或者服务器运行期间无法和Leader保持连接`时就会开始Leader选举。

而当一台机器开始Leader选举流程时，当前集群也可能会处于以下两种状态：
1. 集群中已经存在一个Leader：机器视图去选举Leader时，会被告知当前服务器的Leader信息，对于该机器而言，仅仅需要和Leader机器建立连接并进行状态同步即可。
2. 集群中不存在Leader：选举Leader规则：①EPOCH大的直接胜出；②EPOCH相同，事务id大的胜出；③事务id相同，服务器id大的胜出。
```txt
在某一时刻，服务器3（Leader）和服务器5出现故障，因此开始Leader选举。
SID为1、2、4的机器的（EPOCH，ZXID，SID ）情况：
    服务器1：（1，8，1）
    服务器2：（1，7，2）
    服务器4：（1，8，4） 
根据选举规则，服务器4为新的Leader
```

### 启动停止脚本
```shell
#!/bin/bash

#zookeeper节点
zookeeperServers='192.168.44.129 192.168.44.131 192.168.44.132'

case $1 in 
"start") {
	for zoo in $zookeeperServers
	do
		echo ---------- Zookeeper $zoo 启动 ----------
		ssh $zoo "/mydata/zookeeper/zookeeper-3.5.7/bin/zkServer.sh start"
	done
}
;;
case $1 in 
"stop") {
	for zoo in $zookeeperServers
	do
		echo ---------- Zookeeper $zoo 停止 ----------
		ssh $zoo "/mydata/zookeeper/zookeeper-3.5.7/bin/zkServer.sh stop"
	done
}
;;
case $1 in 
"status") {
	for zoo in $zookeeperServers
	do
		echo ---------- Zookeeper $zoo 状态 ----------
		ssh $zoo "/mydata/zookeeper/zookeeper-3.5.7/bin/zkServer.sh status"
	done
}
;;
esac
```
运行脚本期间可能会遇到两个问题：1、J`AVA_HOME`找不到路径，修改`zkEnv.sh`文件指定`JAVA_HOME`即可；2、每次ssh都需要输入密码（设置免密登录即可）。

### 客户端操作
#### 命令行语法
|   语法   |    描述  |
| ---- | ---- |
|   help   |   显式索引操作命令   |
|   ls path   |   查看当前 `znode` 的子节点 <br/>`-w` 监听子节点变化 <br/>`-s` 附加次级信息   |
|   create   |   创建 `znode` 节点 <br/>`-s` 带有序列的节点 <br/> `-e` 临时节点（重启或超时消失）  |
|   get path   |   获取节点的值 <br/> `-w` 监听节点内容变化 <br/>`-s` 附加信息   |
|   set   |   设置节点的具体值   |
|   stat   |   查看节点状态   |
|   delete   |   删除节点   |
|   deleteall   |   递归删除节点   |

#### znode节点数据信息
```txt
[zk: 192.168.44.131:2181(CONNECTED) 6] ls -s /
[zookeeper]cZxid = 0x0
# 创建节点的事务 zxid，每次修改 zk 状态都会产生一个 zk 事务id，事务id是 zk 中所有修改的总的次序。
# 每次修改都有唯一的 zxid，如果 zxid1 小于 zxid2，那么 zxid1 在 zxid2 之前发生
cZxid = 0x0

# znode 被创建的毫秒数（从1970年开始）
ctime = Thu Jan 01 08:00:00 CST 1970

# znode 最后更新的事务 zxid
mZxid = 0x0

# znode 最后修改的毫秒数（从1970年开始）
mtime = Thu Jan 01 08:00:00 CST 1970

# znode 最后更新的子节点 zxid
pZxid = 0x0

# znode 子节点版本号，znode 子节点修改次数
cversion = -1

# znode 数据版本号
dataVersion = 0

# 访问控制列表的版本号
aclVersion = 0

# 如果时临时节点，该值是 znode 拥有者的 session id。如果不是临时节点则是0
ephemeralOwner = 0x0

# znode 的数据长度
dataLength = 0

# znode 子节点数量
numChildren = 1
```

#### 节点类型
持久：客户端和服务端断开连接后，创建的节点不会删除。

临时：客户端和服务端断开连接后，创建的节点会删除。

序列：Zookeeper 给该节点名称进行顺序编号，创建 znode 时设置的顺序标识。顺序号是一个单调递增的计数器，由父节点维护。

持久节点、持久序列节点、临时节点、临时序列节点

#### 监听器及节点删除
![](../image/zookeeper_监听器原理.png)

监听器原理总结：
1. 启动 Zookeeper 客户端。
2. 在客户端启动过程中，会创建两个线程。一个辅助网络连接通信（connect），一个负责监听（listener）。
3. 通过`connect`线程将注册的监听事件发送给服务端。
4. 在服务端的注册监听器列表中将注册的监听事件添加到列表中。
5. 服务端监听到有数据或路径发生变化，就会将这个消息发送给`listener`线程。
6. `listener`线程内部调用`process()`方法。

常见的监听方式：
1. 监听节点数据的变化：`get path [watch]`
2. 监听子节点增减的变化：`ls path [watch]`

注册一次，监听一次。想再次监听，需要再次注册。但是不会对二级子节点进行监听

#### 客户端API

#### 客户端向服务端写数据流程
Zookeeper 遵循半数机制，集群中超过半数成功就会做出响应。

##### 客户端写请求发送给leader节点
![](../image/zookeeper_客户端写请求发送给leader节点.png)

客户端写请求发送给leader节点大概步骤：
1. 客户端向 Leader 节点发送写请求（写请求可以是创建节点、更新节点数据、删除节点等操作。）。
2. Leader 节点接收并处理写请求，Leader 节点会将事务广播给 ZooKeeper 集群中的其他 Follower 节点，以便进行数据同步。
3. Follower 节点接收到 Leader 节点广播的事务后，Follower 节点会将事务添加到自己的事务日志中，确保事务持久化到磁盘。然后 Follower 节点会向 Leader 节点发送 ACK（确认）信息。
4. Leader 节点等待大多数节点（包括自己）的 ACK。
5. 客户端收到写请求的响应。

##### 客户端写请求发送给follower节点
![](../image/zookeeper_客户端写请求发送给follower节点.png)

客户端写请求发送给follower节点大概步骤：
1. 客户端向 Follower 节点发送写请求（写请求可以是创建节点、更新节点数据、删除节点等操作。）。
2. Follower 节点收到客户端发送的写请求后，会将写请求转发给 Leader 节点，因为只有 Leader 节点才能处理写操作。
3. Leader 节点接收并处理写请求：Leader 节点收到来自 Follower 节点的写请求后，会按照`客户端写请求发送给leader节点流程的2-4步骤`处理该请求。
4. Leader 节点将响应发送给 Follower 节点。
5. Follower 节点将响应发送给客户端。

## 服务器动态上下限监听

## 分布式锁
![](../image/zookeeper_分布式锁实现.png)

![](../image/zookeeper_分布式锁实现.png)

zookeeper分布式锁流程：
1. 客户端连接 zookeeper 集群，并创建临时序列节点。
2. 判断自己创建的节点是不是当前节点下最小序列的节点：
    1. 是，获取到锁。
    2. 不是，对前一个节点进行监听。
3. 获取到锁，处理完业务后，delete删除节点释放锁。
4. 然后其他发起监听的客户端会判断释放的节点是不是自己前一个节点，如果是则唤醒当前节点，否则继续阻塞。

- 分布式共享锁实现
```java
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author cristina
 */
public class DistributedLock {

    private final String connectString = "192.168.44.131:2181,192.168.44.132:2181,192.168.44.129:2181";
    private final CuratorFramework client;
    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final String rootPath = "/locks";
    private String waitPath;
    private static final String DISTRIBUTED_LOCK = "seq-";
    private String currentNode;
    /**
     * ExponentialBackoffRetry：重试3次，每次间隔1秒
     */
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    public DistributedLock() {
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();

        // 监听根节点路径变化
        listenerChildrenNode(rootPath);

        try {
            // 如果根节点不存在则创建
            Stat stat = client.checkExists().forPath(rootPath);
            if (Objects.isNull(stat)) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(rootPath, "locks".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        try {
            // 创建临时序列接待你
            currentNode = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(String.format("%s/%s", rootPath, DISTRIBUTED_LOCK));

            // 获取根节点的所有子节点
            List<String> childrenNode = client.getChildren().forPath(rootPath);

            if (childrenNode.size() == 1) {
                // have no competition.
                return;
            }
            Collections.sort(childrenNode);
            String node = currentNode.substring("/locks/".length());
            int index = childrenNode.indexOf(node);

            if (index == 0) {
                // have no competition.
                return;
            }
            // 监听前一个节点
            waitPath = String.format("%s/%s", rootPath, childrenNode.get(index - 1));
            // 阻塞
            waitLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        try {
            client.delete().deletingChildrenIfNeeded().withVersion(-1).forPath(currentNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listenerChildrenNode(String path) {
        try {
            TreeCache cache = new TreeCache(client, path);
            cache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
                    // 排除不关心的事件
                    if (Objects.isNull(event.getData())) {
                        return;
                    }
                    if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                        // 当前节点为阻塞状态，只有当前节点的前一个序列节点释放后，当前节点才会被唤醒
                        if (event.getData().getPath().equals(waitPath)) {
                            waitLatch.countDown();
                            System.out.println("remove: " + event.getData().getPath());
                        }
                    }
                }
            });
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

Curator 客户端提供一个`InterProcessMutex`排他锁。省去造轮子的过程。
## Zookeeper 算法 
### paxos 算法
paxos 算法是一种居于消息传递且具有高度容错性的一致性算法。

paxos 算法所解决的问题就是如何快速正确的在分布式系统中对某个数据值达成一致，并且保证不论发生任何异常，都不会破坏整个系统的一致性。

![](../image/zookeeper_paxos_算法流程.png)

在一个Paxos系统中，首先将所有节点划分为Proposer（提议者）、Acceptor（接收者）和Learner（学习者）。且每个节点都可以有多个角色。

Paxos算法流程分为三个阶段：
1. Prepare准备阶段
   1. Proposer向多个Acceptor发出Propose请求Promise。
   2. Acceptor针对接收到的Propose请求进行Promise。
2. Accept接收阶段
   1. Proposer接收到多数Acceptor的Promise后，向Acceptor发出Propose请求。
   2. Acceptor针对接收到的Propose请求进行Accept处理。
3. Learn学习阶段：Proposer将表决的决议发送给所有Learners。

![](../image/zookeeper_paxos_算法流程解释.png)

1. Prepare：`Proposer`生成全局唯一且递增的`Proposal ID`，向所有`Acceptor`发送`Propose`请求，无需携带提案内容，只携带`Proposal ID`即可。
2. Promise：`Acceptor`收到`Propose`请求后，做出`两个承诺，一个应答`。
   1. 不再接受`Proposal ID`小于等于当前请求的Propose请求。
   2. 不再接受`Proposal ID`小于当前请求的Accept请求。
   3. 不违背以前做出的`Promise`承诺下，响应已经`Accept`过的提案中`Proposal ID`最大的哪个提案的`Value`和`Proposal ID`，没有则返回 null。（Acceptor Promise）
3. Propose：`Proposer`收到多数`Acceptor`的`Promise`应答后，从应答中选择`Proposal ID`最大的提案的`Value`，作为 本次要发起的提案。如果所有应答的提案`Value`都为null，则可以自己指定提案`Value`。然后携带当前`Proposal ID`，向所有`Acceptor`发送`Propose`请求。
4. Accept：`Acceptor`收到`Propose`请求后，在不违背之前做出的承诺下，接受并持久化当前`Proposal ID`和提案`Value`。
5. Learn：`Proposer`收到多数`Acceptor`的`Accept`后，决议形成，将决议发送给所有`Learner`。

### ZAB 协议
ZAB协议借鉴了Paxos算法，是专门为Zookeeper设计的支持崩溃恢复的原子广播协议。基于该协议，Zookeeper设计为只有一台客户端（Leader）负责处理外部的写事务请求，然后Leader客户端将数据同步到其他Follower节点。在Zookeeper中只有一个Leader可以发起提案。

zab协议包括两种基本模式：消息广播、崩溃恢复。

![](../image/zookeeper_zab协议_消息广播.png)

消息广播总结：
1. 客户端发起一个写操作请求。
2. `Leader`服务器将客户端的请求转化为事务`Proposal`提案，同时为每个`Proposal`分配一个全局的ID，即zxid。
3. `Leader`服务器会为每个`Follower`服务器都分配一个队列，然后将需要广播的`Proposal`依次放到队列中，并且根据`FIFO`策略进行消息发送。
4. `Follower`接收到`Proposal`后，首先会将其以事务日志的方式写入本地磁盘中，写入成功后向`Leader`响应一个`Ack`消息。
5. `Leader`接收到超过半数以上`Follower`的`Ack`响应消息后（包括Leader自身），即认为消息发送成功，可以发送`commit`消息。
6. `Leader`向所有`Follower`广播`commit`消息，同时`Leader`也会完成事务提交。`Follower`接收到`commit`消息后，会将上一条事务提交。

Zookeeper采用Zab协议的核心，就是只要有一台服务器提交了Proposal，就要确保所有的服务器最终都能正确提交Proposal。

个人理解：Proposal可以理解为客户端的一次写操作（创建节点、修改节点、删除节点等），只有Leader有写操作权限，所以由Leader将操作具体内容（创建节点、修改节点、删除节点等）广播给Follower。提交事务就是将数据永久持久化到磁盘中。

ZAB协议针对事务请求的处理过程类似于两阶段提交过程：
1. 广播事务阶段。
2. 广播提交操作。

### CAP理论

## 源码
### 服务端初始化

### 选举机制

### 服务端Leader启动

### 服务端Follower启动

### 客户端启动

## 附录

### 本地模式

### 配置参数解读
`zoo.cfg`
```cfg
# 通信心跳时间，Zookeeper服务器与客户端心跳时间，单位毫秒。也可以是服务器与服务器之间的心跳时间
1. tickTime=2000;

# LF初始通信时限。Leader和Follower初始连接时能容忍的最多心跳数
2. initLimit=10；

# LF同步通信时限。Leader和Follower之间通信时间如果超过(syncLimit*tickTime)，Leader认为Follower宕机，从服务器列表中删除Follower。
3. syncLimit

# 保存Zookeeper中的数据
4. dataDir=/xxx/xxx.log

# 客户端连接端口
5. clientPort=2181
```

### 命令
```bin
# 启动 Zookeeper 服务器
bin/zkServer.sh start

# 查看 Zookeeper 服务器状态
bin/zkServer.sh status

# 停止 Zookeeper 服务器
bin/zkServer.sh stop

# 启动客户端
bin/zkCli.sh -server 192.168.44.131:2181

# 创建一个持久节点
[zk: 192.168.44.131:2181(CONNECTED) 0] create /address "127.0.0.1:8000"
Created /address

# 获取节点的数据
[zk: 192.168.44.131:2181(CONNECTED) 1] get /address
127.0.0.1:8000

# 获取节点的数据，包括附加信息，zxid等
[zk: 192.168.44.131:2181(CONNECTED) 2] get -s /address
127.0.0.1:8000
cZxid = 0x800000002
ctime = Sun Jun 18 13:32:17 CST 2023
mZxid = 0x800000002
mtime = Sun Jun 18 13:32:17 CST 2023
pZxid = 0x800000003
cversion = 1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 14
numChildren = 1

# 创建持久序列节点
[zk: 192.168.44.131:2181(CONNECTED) 3] create -s /address/city/Chengdu "chengdu"   
Created /address/city/Chengdu0000000000

# 查看子节点
[zk: 192.168.44.131:2181(CONNECTED) 4] ls /
[address, zookeeper]

# 修改节点值
[zk: 192.168.44.131:2181(CONNECTED) 1] set /address "192.168.0.1:80"
[zk: 192.168.44.131:2181(CONNECTED) 2] get /address
192.168.0.1:80

# 删除节点
[zk: 192.168.44.132(CONNECTED) 7] delete /address/city/fujian

# 递归删除节点
[zk: 192.168.44.132(CONNECTED) 9] deleteall /address/city

# 查看节点状态
[zk: 192.168.44.129(CONNECTED) 5] stat /address
cZxid = 0x800000002
ctime = Sun Jun 18 13:32:17 CST 2023
mZxid = 0x80000000c
mtime = Sun Jun 18 14:38:28 CST 2023
pZxid = 0x800000012
cversion = 2
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 0
```

### 写数据原理

### 选举机制
半数机制，超过半数的投票则通过选举。
1. 第一次启动选举规则：
   1. 投票过半数时，服务器id的胜出。
2. 非第一次启动选举规则：
   1. EPOCH大的直接胜出。
   2. EPOCH相同，zxid大的胜出。
   3. zxid相同，服务器id大的胜出。
   
### 生产环境集群安装多数zk合适？
- 10台服务器：3台zk
- 20台服务器：5台zk
- 100台服务器：1台zk
- 200台服务器：11台zk

服务器越多，提高可靠性，但是也会提高通信延时。

### 常用命令
ls、get、create、delete ...











