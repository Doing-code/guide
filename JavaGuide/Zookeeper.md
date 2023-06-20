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

#### 消息广播
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

两阶段提交有可能会因为Leader宕机带来数据不一致。如：
1. Leader发起一个事务Proposal后宕机了，Follower来不及Proposal。
2. Leader收到半数ACK后宕机，来不及向Follower发送commit；

一旦Leader服务器与过半Follower失去联系，那么就会进入崩溃恢复模式。
#### 崩溃恢复
![](../image/zookeeper_zab协议_崩溃恢复.png)

假设异常情况：
1. 一个事务在Leader提出之后，Leader宕机。
2. 一个事务在Leader提交后，且过半的Follower都响应Ack了，但是Leader在commit消息发出之前宕机。

Zab协议崩溃恢复需要满足以下两个要求：
1. 确保已经被Leader提交的提案Proposal，必须最终被所有的Follower服务器提交。（已经产生的提案，Follower必须执行）
2. 确保丢弃已经被Leader提出，但是还没有被提交的Proposal。

崩溃恢复主要包括两部分：Leader选举、数据恢复。
#####　Leader选举
![](../image/zookeeper_zab协议_崩溃恢复＿Leader选举.png)

Zab协议需要保证选举出来的Leader满足以下条件：
1. 新选举的Leader不能包含未提交的Proposal。（新Leader必须都是已经提交了Proposal的Follower节点（Ack））
2. 新选举的Leader节点中是最大的zxid。（可以避免Leader服务器检查Proposal的提交和丢弃工作）
#####　数据恢复
![](../image/zookeeper_zab协议_崩溃恢复＿Leader选举.png)

Zab数据同步：
1. 完成Leader选举后，在正式开始工作之前（即接收事务请求，然后提出新的Proposal），Leader会先确认事务日志中的所有Proposal是否已经被集群中过半的服务器commit。
2. Leader需要确保所有的Follower服务器能够接收到每一条事务的Proposal，并且能够将所有已提交的事务Proposal应用到内存数据中。等到Follower将所有尚未同步的事务Proposal都从Leader上同步，并且应用到内存数据中后，Leader才会把该Follower加入到真正可用的Follower列表中。

### CAP理论
1. 一致性（Consistency）：指数据在多个副本之间是否能够保持数据一致的特性。在一致性的需求下，当一个系统在数据一致性的状态下执行变更操作后，应该保证系统的数据仍处于一致的状态。
2. 可用性（Available）：指系统提供的服务必须一直处于可用状态。对于用户的每一次操作请求总能在有限时间内返回结果。
3. 分区容错性（Partition Tolerance）：指在遇到任何网络分区故障时，仍然需要能够保证对外提供满足一致性和可用性的服务。除非整个网络环境都出现故障。

Zookeeper保证的是CP：
1. Zookeeper不能保证每次服务请求的可用性。（在极端情况下，Zookeeper可能会丢弃以下请求，消费者需要重新请求才能获得结果）
2. Leader选举时集群都是不可用。

一个分布式系统不可能同时满足这三个。最多只能同时满足两个，P是必须的，CP或者AP。

## 源码
### 持久化源码
Leader和Follower的数据会在内存和磁盘中各保存一份，素以需要将内存中的数据持久化到磁盘中。

在` org.apache.zookeeper.server.persistence`包下的都是和序列化相关的代码。

![](../image/zookeeper_源码_持久化.png)

等到服务器空闲时会将内存数据写入到`TxnLog编辑日志`，再从`TxnLog编辑日志`写入到`snapShot快照`（磁盘）。

#### 快照
```java
public interface SnapShot {
    
    // 反序列化
    long deserialize(DataTree dt, Map<Long, Integer> sessions) 
        throws IOException;
    
    // 序列化
    void serialize(DataTree dt, Map<Long, Integer> sessions, 
            File name) 
        throws IOException;
    
    // 查找最近的快照文件
    File findMostRecentSnapshot() throws IOException;
    
    // 释放资源
    void close() throws IOException;
}
```
#### 操作日志
```java
public interface TxnLog {
    // 设置监控 fsync 的阈值
    void setServerStats(ServerStats serverStats);
    
    // 回滚日志
    void rollLog() throws IOException;
    // 追加日志
    boolean append(TxnHeader hdr, Record r) throws IOException;

    // 读取日志
    TxnIterator read(long zxid) throws IOException;
    
    // 获取事务id
    long getLastLoggedZxid() throws IOException;
    
    // 删除日志
    boolean truncate(long zxid) throws IOException;
    
    // 获取此事务日志的 dbid
    long getDbId() throws IOException;
    
    // 提交
    void commit() throws IOException;

    // 事务日志运行的同步时间(以毫秒为单位)
    long getTxnLogSyncElapsedTime();
   
    // 释放资源
    void close() throws IOException;
    
    // 用于读取事务日志的迭代接口
    public interface TxnIterator {
        TxnHeader getHeader();
        Record getTxn();
        boolean next() throws IOException;
        void close() throws IOException;
        long getStorageSize() throws IOException;
    }
}
```

### 序列化源码
`zookeeper-jute`是关于Zookeeper序列化相关源码。
```java
// 序列化实现 OutputArchive 接口
// 反序列化实现 InputArchive 接口

public interface Record {
    public void serialize(OutputArchive archive, String tag)
        throws IOException;
    public void deserialize(InputArchive archive, String tag)
        throws IOException;
}

public interface OutputArchive {
    public void writeByte(byte b, String tag) throws IOException;
    public void writeBool(boolean b, String tag) throws IOException;
    public void writeInt(int i, String tag) throws IOException;
    public void writeLong(long l, String tag) throws IOException;
    public void writeFloat(float f, String tag) throws IOException;
    public void writeDouble(double d, String tag) throws IOException;
    public void writeString(String s, String tag) throws IOException;
    public void writeBuffer(byte buf[], String tag)
        throws IOException;
    public void writeRecord(Record r, String tag) throws IOException;
    public void startRecord(Record r, String tag) throws IOException;
    public void endRecord(Record r, String tag) throws IOException;
    public void startVector(List<?> v, String tag) throws IOException;
    public void endVector(List<?> v, String tag) throws IOException;
    public void startMap(TreeMap<?,?> v, String tag) throws IOException;
    public void endMap(TreeMap<?,?> v, String tag) throws IOException;

}

public interface InputArchive {
    public byte readByte(String tag) throws IOException;
    public boolean readBool(String tag) throws IOException;
    public int readInt(String tag) throws IOException;
    public long readLong(String tag) throws IOException;
    public float readFloat(String tag) throws IOException;
    public double readDouble(String tag) throws IOException;
    public String readString(String tag) throws IOException;
    public byte[] readBuffer(String tag) throws IOException;
    public void readRecord(Record r, String tag) throws IOException;
    public void startRecord(String tag) throws IOException;
    public void endRecord(String tag) throws IOException;
    public Index startVector(String tag) throws IOException;
    public void endVector(String tag) throws IOException;
    public Index startMap(String tag) throws IOException;
    public void endMap(String tag) throws IOException;
}
```

### 服务端初始化
![img.png](../image/zookeeper_源码_服务端初始化_不包括选举机制.png)

#### 启动脚本
```shell
# zkServer.sh
if [ -e "$ZOOBIN/../libexec/zkEnv.sh" ]; then
  . "$ZOOBINDIR"/../libexec/zkEnv.sh
else
  . "$ZOOBINDIR"/zkEnv.sh
fi

if [ "x$JMXDISABLE" = "x" ] || [ "$JMXDISABLE" = 'false' ]
then
  echo "ZooKeeper JMX enabled by default" >&2
  if [ "x$JMXPORT" = "x" ]
  then
    # for some reason these two options are necessary on jdk6 on Ubuntu
    #   accord to the docs they are not necessary, but otw jconsole cannot
    #   do a local attach
    ZOOMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=$JMXLOCALONLY org.apache.zookeeper.server.quorum.QuorumPeerMain"
  else
    if [ "x$JMXAUTH" = "x" ]
    then
      JMXAUTH=false
    fi
    if [ "x$JMXSSL" = "x" ]
    then
      JMXSSL=false
    fi
    if [ "x$JMXLOG4J" = "x" ]
    then
      JMXLOG4J=true
    fi
    echo "ZooKeeper remote JMX Port set to $JMXPORT" >&2
    echo "ZooKeeper remote JMX authenticate set to $JMXAUTH" >&2
    echo "ZooKeeper remote JMX ssl set to $JMXSSL" >&2
    echo "ZooKeeper remote JMX log4j set to $JMXLOG4J" >&2
    ZOOMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMXPORT -Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL -Dzookeeper.jmx.log4j.disable=$JMXLOG4J org.apache.zookeeper.server.quorum.QuorumPeerMain"
  fi
else
    echo "JMX disabled by user request" >&2
    ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
 fi
 
 if [ "x$2" != "x" ]
then
    ZOOCFG="$ZOOCFGDIR/$2"
fi
    
# ... ...
case $1 in
start)
    echo  -n "Starting zookeeper ... "
    if [ -f "$ZOOPIDFILE" ]; then
      if kill -0 `cat "$ZOOPIDFILE"` > /dev/null 2>&1; then
         echo $command already running as process `cat "$ZOOPIDFILE"`.
         exit 1
      fi
    fi
    nohup "$JAVA" $ZOO_DATADIR_AUTOCREATE "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" \
    "-Dzookeeper.log.file=${ZOO_LOG_FILE}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
    -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError='kill -9 %p' \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
 ;;
# $ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
# $ZOOCFG 在 zkEnv.sh 文件中 = filepath/zoo.cfg

# zkEnv.sh
if [ "x$ZOOCFG" = "x" ]
then
    ZOOCFG="zoo.cfg"
fi

ZOOCFG="$ZOOCFGDIR/$ZOOCFG"
```

所以Zookeeper服务端的入口是`org.apache.zookeeper.server.quorum.QuorumPeerMain#main`。
#### 解析配置文件
1. `org.apache.zookeeper.server.quorum.QuorumPeerConfig#parse`
```java
public void parse(String path) throws ConfigException {
  LOG.info("Reading configuration from: " + path);
 
  try {
      // 获取文件路径
      File configFile = (new VerifyingFileFactory.Builder(LOG)
          .warnForRelativePath()
          .failForNonExistingPath()
          .build()).create(path);
          
      Properties cfg = new Properties();
      
      // IO流的形式读取配置文件
      FileInputStream in = new FileInputStream(configFile);
      try {
          // 加载配置文件的内容
          cfg.load(in);
          configFileStr = path;
      } finally {
          in.close();
      }
      
      // 解析zoo.cfg
      parseProperties(cfg);
  } catch (IOException e) {
      throw new ConfigException("Error processing " + path, e);
  } catch (IllegalArgumentException e) {
      throw new ConfigException("Error processing " + path, e);
  }   
  
  // 省略部分代码 .. 动态配置文件 ...
}
```
2. `org.apache.zookeeper.server.quorum.QuorumPeerConfig#parseProperties`
```java
public void parseProperties(Properties zkProp)
    throws IOException, ConfigException {
        int clientPort = 0;
        int secureClientPort = 0;
        String clientPortAddress = null;
        String secureClientPortAddress = null;
        VerifyingFileFactory vff = new VerifyingFileFactory.Builder(LOG).warnForRelativePath().build();
        
        // zoo.cfg 能够配置的所有配置项，遍历 Properties
        for (Entry<Object, Object> entry : zkProp.entrySet()) {
            String key = entry.getKey().toString().trim();
            String value = entry.getValue().toString().trim();
            if (key.equals("dataDir")) {
                dataDir = vff.create(value);
            } else if (key.equals("dataLogDir")) {
                dataLogDir = vff.create(value);
            } else if (key.equals("clientPort")) {
                clientPort = Integer.parseInt(value);
            } else if (key.equals("localSessionsEnabled")) {
                localSessionsEnabled = Boolean.parseBoolean(value);
            } else if (key.equals("localSessionsUpgradingEnabled")) {
                localSessionsUpgradingEnabled = Boolean.parseBoolean(value);
            } else if (key.equals("clientPortAddress")) {
                clientPortAddress = value.trim();
            } else if (key.equals("secureClientPort")) {
                secureClientPort = Integer.parseInt(value);
            } else if (key.equals("secureClientPortAddress")){
                secureClientPortAddress = value.trim();
            } else if (key.equals("tickTime")) {
                tickTime = Integer.parseInt(value);
            } else if (key.equals("maxClientCnxns")) {
                maxClientCnxns = Integer.parseInt(value);
            } else if (key.equals("minSessionTimeout")) {
                minSessionTimeout = Integer.parseInt(value);
            } else if (key.equals("maxSessionTimeout")) {
                maxSessionTimeout = Integer.parseInt(value);
            } else if (key.equals("initLimit")) {
                initLimit = Integer.parseInt(value);
            } else if (key.equals("syncLimit")) {
                syncLimit = Integer.parseInt(value);
            } else if (key.equals("electionAlg")) {
                electionAlg = Integer.parseInt(value);
            } else if (key.equals("quorumListenOnAllIPs")) {
                quorumListenOnAllIPs = Boolean.parseBoolean(value);
            } else if (key.equals("peerType")) {
                if (value.toLowerCase().equals("observer")) {
                    peerType = LearnerType.OBSERVER;
                } else if (value.toLowerCase().equals("participant")) {
                    peerType = LearnerType.PARTICIPANT;
                } else
                {
                    throw new ConfigException("Unrecognised peertype: " + value);
                }
            } else if (key.equals( "syncEnabled" )) {
                syncEnabled = Boolean.parseBoolean(value);
            } else if (key.equals("dynamicConfigFile")){
                dynamicConfigFileStr = value;
            } else if (key.equals("autopurge.snapRetainCount")) {
                snapRetainCount = Integer.parseInt(value);
            } else if (key.equals("autopurge.purgeInterval")) {
                purgeInterval = Integer.parseInt(value);
            } else if (key.equals("standaloneEnabled")) {
                if (value.toLowerCase().equals("true")) {
                    setStandaloneEnabled(true);
                } else if (value.toLowerCase().equals("false")) {
                    setStandaloneEnabled(false);
                } else {
                    throw new ConfigException("Invalid option " + value + " for standalone mode. Choose 'true' or 'false.'");
                }
            } else if (key.equals("reconfigEnabled")) {
                if (value.toLowerCase().equals("true")) {
                    setReconfigEnabled(true);
                } else if (value.toLowerCase().equals("false")) {
                    setReconfigEnabled(false);
                } else {
                    throw new ConfigException("Invalid option " + value + " for reconfigEnabled flag. Choose 'true' or 'false.'");
                }
            } else if (key.equals("sslQuorum")){
                sslQuorum = Boolean.parseBoolean(value);
            } else if (key.equals("portUnification")){
                shouldUsePortUnification = Boolean.parseBoolean(value);
            } else if (key.equals("sslQuorumReloadCertFiles")) {
                sslQuorumReloadCertFiles = Boolean.parseBoolean(value);
            } else if ((key.startsWith("server.") || key.startsWith("group") || key.startsWith("weight")) && zkProp.containsKey("dynamicConfigFile")) {
                throw new ConfigException("parameter: " + key + " must be in a separate dynamic config file");
            } else if (key.equals(QuorumAuth.QUORUM_SASL_AUTH_ENABLED)) {
                quorumEnableSasl = Boolean.parseBoolean(value);
            } else if (key.equals(QuorumAuth.QUORUM_SERVER_SASL_AUTH_REQUIRED)) {
                quorumServerRequireSasl = Boolean.parseBoolean(value);
            } else if (key.equals(QuorumAuth.QUORUM_LEARNER_SASL_AUTH_REQUIRED)) {
                quorumLearnerRequireSasl = Boolean.parseBoolean(value);
            } else if (key.equals(QuorumAuth.QUORUM_LEARNER_SASL_LOGIN_CONTEXT)) {
                quorumLearnerLoginContext = value;
            } else if (key.equals(QuorumAuth.QUORUM_SERVER_SASL_LOGIN_CONTEXT)) {
                quorumServerLoginContext = value;
            } else if (key.equals(QuorumAuth.QUORUM_KERBEROS_SERVICE_PRINCIPAL)) {
                quorumServicePrincipal = value;
            } else if (key.equals("quorum.cnxn.threads.size")) {
                quorumCnxnThreadsSize = Integer.parseInt(value);
            } else {
                System.setProperty("zookeeper." + key, value);
            }
        }
        // ... 省略部分代码，检查配置参数是否合法
        
        // backward compatibility - dynamic configuration in the same file as
        // static configuration params see writeDynamicConfig()
        if (dynamicConfigFileStr == null) {
            
            // 解析 myid、初始化 ClientPort 等信息
            setupQuorumPeerConfig(zkProp, true);
            if (isDistributed() && isReconfigEnabled()) {
                // we don't backup static config for standalone mode.
                // we also don't backup if reconfig feature is disabled.
                backupOldConfig();
            }
        }
    }
```
3. `org.apache.zookeeper.server.quorum.QuorumPeerConfig#setupQuorumPeerConfig`
```java
void setupQuorumPeerConfig(Properties prop, boolean configBackwardCompatibilityMode)
      throws IOException, ConfigException {
  quorumVerifier = parseDynamicConfig(prop, electionAlg, true, configBackwardCompatibilityMode);
  
  // 服务id 就是 myid
  setupMyId();
  setupClientPort();
  setupPeerType();
  checkValidity();
}

private void setupMyId() throws IOException {
  File myIdFile = new File(dataDir, "myid");
  // standalone server doesn't need myid file.
  if (!myIdFile.isFile()) {
      return;
  }
  BufferedReader br = new BufferedReader(new FileReader(myIdFile));
  String myIdString;
  try {
      myIdString = br.readLine();
  } finally {
      br.close();
  }
  try {
      // myid 赋值给 serverId
      serverId = Long.parseLong(myIdString);
      MDC.put("myid", myIdString);
  } catch (NumberFormatException e) {
      throw new IllegalArgumentException("serverid " + myIdString
              + " is not a number");
  }
}
```
主脉络的流程就分析完了。

#### 删除过期快照
1. `org.apache.zookeeper.server.DatadirCleanupManager#start`
```java
// config.getSnapRetainCount() 默认=3，config.getPurgeInterval() 默认=0
// DatadirCleanupManager purgeMgr = new DatadirCleanupManager(config.getDataDir(), config.getDataLogDir(), config.getSnapRetainCount(), config.getPurgeInterval());
// purgeMgr.start();

public void start() {
  // 如果清理任务正在执行中，返回
  if (PurgeTaskStatus.STARTED == purgeTaskStatus) {
      LOG.warn("Purge task is already running.");
      return;
  }
  // 默认关闭
  if (purgeInterval <= 0) {
      LOG.info("Purge task is not scheduled.");
      return;
  }

  // 创建定时任务，交给线程池来处理 删除过期快照，每隔 purgeInterval 小时检查一次
  timer = new Timer("PurgeTask", true);
  TimerTask task = new PurgeTask(dataLogDir, snapDir, snapRetainCount);
  timer.scheduleAtFixedRate(task, 0, TimeUnit.HOURS.toMillis(purgeInterval));

  purgeTaskStatus = PurgeTaskStatus.STARTED;
}

static class PurgeTask extends TimerTask {
  private File logsDir;
  private File snapsDir;
  private int snapRetainCount;

  public PurgeTask(File dataDir, File snapDir, int count) {
      logsDir = dataDir;
      snapsDir = snapDir;
      snapRetainCount = count;
  }

  @Override
  public void run() {
      LOG.info("Purge task started.");
      try {
          // 清除过期快照
          PurgeTxnLog.purge(logsDir, snapsDir, snapRetainCount);
      } catch (Exception e) {
          LOG.error("Error occurred while purging.", e);
      }
      LOG.info("Purge task completed.");
  }
}
```
简单看一下`PurgeTxnLog.purge()`源码：
```java
public static void purge(File dataDir, File snapDir, int num) throws IOException {
   // 最少保留3个快照
  if (num < 3) {
      throw new IllegalArgumentException(COUNT_ERR_MSG);
  }

  // 默认生成的文件夹名字是version-2
  FileTxnSnapLog txnLog = new FileTxnSnapLog(dataDir, snapDir);

  // 获取指定数量的快照
  List<File> snaps = txnLog.findNRecentSnapshots(num);
  int numSnaps = snaps.size();
  if (numSnaps > 0) {
      purgeOlderSnapshots(txnLog, snaps.get(numSnaps - 1));
  }
}
```
主脉络的流程就分析完了。

#### 通信初始化
1. `org.apache.zookeeper.server.ServerCnxnFactory#createFactory()`
```java
static public ServerCnxnFactory createFactory() throws IOException {
  // 默认就是 NIOServerCnxnFactory
  String serverCnxnFactoryName =
      System.getProperty(ZOOKEEPER_SERVER_CNXN_FACTORY);
  if (serverCnxnFactoryName == null) {
      serverCnxnFactoryName = NIOServerCnxnFactory.class.getName();
  }
  try {
      
      // 创建 NIOServerCnxnFactory
      ServerCnxnFactory serverCnxnFactory = (ServerCnxnFactory) Class.forName(serverCnxnFactoryName)
              .getDeclaredConstructor().newInstance();
      LOG.info("Using {} as server connection factory", serverCnxnFactoryName);
      return serverCnxnFactory;
  } catch (Exception e) {
      IOException ioe = new IOException("Couldn't instantiate "
              + serverCnxnFactoryName);
      ioe.initCause(e);
      throw ioe;
  }
}
```
2. `org.apache.zookeeper.server.NIOServerCnxnFactory#configure`
```java
public void configure(InetSocketAddress addr, int maxcc, boolean secure) throws IOException {
  if (secure) {
      throw new UnsupportedOperationException("SSL isn't supported in NIOServerCnxn");
  }
  configureSaslLogin();

  maxClientCnxns = maxcc;
  sessionlessCnxnTimeout = Integer.getInteger(
      ZOOKEEPER_NIO_SESSIONLESS_CNXN_TIMEOUT, 10000);

  cnxnExpiryQueue =
      new ExpiryQueue<NIOServerCnxn>(sessionlessCnxnTimeout);
  expirerThread = new ConnectionExpirerThread();

  int numCores = Runtime.getRuntime().availableProcessors();
  // 32 cores sweet spot seems to be 4 selector threads
  numSelectorThreads = Integer.getInteger(
      ZOOKEEPER_NIO_NUM_SELECTOR_THREADS,
      Math.max((int) Math.sqrt((float) numCores/2), 1));
  if (numSelectorThreads < 1) {
      throw new IOException("numSelectorThreads must be at least 1");
  }

  numWorkerThreads = Integer.getInteger(
      ZOOKEEPER_NIO_NUM_WORKER_THREADS, 2 * numCores);
  workerShutdownTimeoutMS = Long.getLong(
      ZOOKEEPER_NIO_SHUTDOWN_TIMEOUT, 5000);

  for(int i=0; i<numSelectorThreads; ++i) {
      selectorThreads.add(new SelectorThread(i));
  }

  // 默认 NIO 通信，绑定2181端口
  this.ss = ServerSocketChannel.open();
  ss.socket().setReuseAddress(true);
  LOG.info("binding to port " + addr);
  ss.socket().bind(addr);
  ss.configureBlocking(false);
  acceptThread = new AcceptThread(ss, addr, selectorThreads);
}
```
主脉络的流程就分析完了。
#### 加载编辑日志和快照
1. `org.apache.zookeeper.server.quorum.QuorumPeer#start`
```java
public synchronized void start() {
  if (!getView().containsKey(myid)) {
      throw new RuntimeException("My id " + myid + " not in the peer list");
   }
   
  // 加载日志数据到内存中
  loadDataBase();
  startServerCnxnFactory();
  try {
      
      /*
         启动adminServer，通过浏览器可以访问
         http://localhost:8080/commands/
      */
      adminServer.start();
  } catch (AdminServerException e) {
      LOG.warn("Problem starting AdminServer", e);
      System.out.println(e);
  }
  
  // 启动快速选举
  startLeaderElection();
  
  // 调用 Thread.start();
  super.start();
}
```
2. `org.apache.zookeeper.server.quorum.QuorumPeer#loadDataBase`
```java
private void    loadDataBase() {
  try {
  
      // 从磁盘将数据加载到内存中
      zkDb.loadDataBase();

      // 获取最新的 zxid
      long lastProcessedZxid = zkDb.getDataTree().lastProcessedZxid;
      
      // 获取 zxid 对应的 EPOCH（代号）
      long epochOfZxid = ZxidUtils.getEpochFromZxid(lastProcessedZxid);
      try {
          currentEpoch = readLongFromFile(CURRENT_EPOCH_FILENAME);
      } catch(FileNotFoundException e) {
         // ... 省略
      }
      /// ... 省略
  } catch(IOException ie) {
      LOG.error("Unable to load database on disk", ie);
      throw new RuntimeException("Unable to run quorum server ", ie);
  }
}
```
3. `org.apache.zookeeper.server.ZKDatabase#loadDataBase`
```java
public long loadDataBase() throws IOException {
  long zxid = snapLog.restore(dataTree, sessionsWithTimeouts, commitProposalPlaybackListener);
  initialized = true;
  return zxid;
}
```
4. `org.apache.zookeeper.server.persistence.FileTxnSnapLog#restore`
```java
public long restore(DataTree dt, Map<Long, Integer> sessions, PlayBackListener listener) throws IOException {
  // 1、恢复快照
  long deserializeResult = snapLog.deserialize(dt, sessions);
  FileTxnLog txnLog = new FileTxnLog(dataDir);

  // 2、恢复 编辑日志 数据到 DataTree
  RestoreFinalizer finalizer = () -> {
      long highestZxid = fastForwardFromEdits(dt, sessions, listener);
      return highestZxid;
  };

  // ... 省略

  return finalizer.run();
}
```
5. `org.apache.zookeeper.server.persistence.SnapShot#deserialize`
```java
public long deserialize(DataTree dt, Map<Long, Integer> sessions) throws IOException {
  // we run through 100 snapshots (not all of them)
  // if we cannot get it running within 100 snapshots
  // we should  give up
  List<File> snapList = findNValidSnapshots(100);
  if (snapList.size() == 0) {
      return -1L;
  }
  File snap = null;
  boolean foundValid = false;
  
  // 依次遍历每一个快照的数据
  for (int i = 0, snapListSize = snapList.size(); i < snapListSize; i++) {
      snap = snapList.get(i);
      LOG.info("Reading snapshot " + snap);
      
      // 反序列化 环境准备
      try (InputStream snapIS = new BufferedInputStream(new FileInputStream(snap));
           CheckedInputStream crcIn = new CheckedInputStream(snapIS, new Adler32())) {
          InputArchive ia = BinaryInputArchive.getArchive(crcIn);
          
          // 反序列化
          deserialize(dt, sessions, ia);
          long checkSum = crcIn.getChecksum().getValue();
          long val = ia.readLong("val");
          if (val != checkSum) {
              throw new IOException("CRC corruption in snapshot :  " + snap);
          }
          foundValid = true;
          break;
      } catch (IOException e) {
          LOG.warn("problem reading snap file " + snap, e);
      }
  }
  if (!foundValid) {
      throw new IOException("Not able to find valid snapshots in " + snapDir);
  }
  dt.lastProcessedZxid = Util.getZxidFromName(snap.getName(), SNAPSHOT_FILE_PREFIX);
  return dt.lastProcessedZxid;
}
```
6. `org.apache.zookeeper.server.persistence.FileSnap#deserialize`
```java
public void deserialize(DataTree dt, Map<Long, Integer> sessions,
      InputArchive ia) throws IOException {
  FileHeader header = new FileHeader();
  header.deserialize(ia, "fileheader");
  if (header.getMagic() != SNAP_MAGIC) {
      throw new IOException("mismatching magic headers "
              + header.getMagic() +
              " !=  " + FileSnap.SNAP_MAGIC);
  }
  
  // 反序列化快照
  SerializeUtils.deserializeSnapshot(dt,ia,sessions);
}
```
7. `org.apache.zookeeper.server.util.SerializeUtils#deserializeSnapshot`
```java
public static void deserializeSnapshot(DataTree dt,InputArchive ia,
      Map<Long, Integer> sessions) throws IOException {
  int count = ia.readInt("count");
  while (count > 0) {
      long id = ia.readLong("id");
      int to = ia.readInt("timeout");
      sessions.put(id, to);
      if (LOG.isTraceEnabled()) {
      }
      count--;
  }
  
  // 反序列化
  dt.deserialize(ia, "tree");
}
```
8. `org.apache.zookeeper.server.DataTree#deserialize`
```java
public void deserialize(InputArchive ia, String tag) throws IOException {
  aclCache.deserialize(ia);
  nodes.clear();
  pTrie.clear();
  String path = ia.readString("path");
  
  // 循环将快照数据恢复到 DataTree
  while (!"/".equals(path)) {
  
      // 每次循环创建一个节点对象
      DataNode node = new DataNode();
      ia.readRecord(node, "node");
      
      // 将 DataNode恢复到 DataTree
      nodes.put(path, node);
      synchronized (node) {
          aclCache.addUsage(node.acl);
      }
      int lastSlash = path.lastIndexOf('/');
      if (lastSlash == -1) {
          root = node;
      } else {
      
          // // 处理父节点
          String parentPath = path.substring(0, lastSlash);
          DataNode parent = nodes.get(parentPath);
          if (parent == null) {
              throw new IOException("Invalid Datatree, unable to find " +
                      "parent " + parentPath + " of path " + path);
          }
          
          // // 处理子节点
          parent.addChild(path.substring(lastSlash + 1));
          long eowner = node.stat.getEphemeralOwner();
          EphemeralType ephemeralType = EphemeralType.get(eowner);
          if (ephemeralType == EphemeralType.CONTAINER) {
              containers.add(path);
          } else if (ephemeralType == EphemeralType.TTL) {
              ttls.add(path);
          } else if (eowner != 0) {
              HashSet<String> list = ephemerals.get(eowner);
              if (list == null) {
                  list = new HashSet<String>();
                  ephemerals.put(eowner, list);
              }
              list.add(path);
          }
      }
      path = ia.readString("path");
  }
  nodes.put("/", root);
  // we are done with deserializing the
  // the datatree
  // update the quotas - create path trie
  // and also update the stat nodes
  setupQuota();

  aclCache.purgeUnused();
}
```
9. `org.apache.zookeeper.server.persistence.FileTxnSnapLog#fastForwardFromEdits`
```java
public long fastForwardFromEdits(DataTree dt, Map<Long, Integer> sessions, PlayBackListener listener) throws IOException {

  // 从快照的zxid + 1位置开始恢复
  TxnIterator itr = txnLog.read(dt.lastProcessedZxid+1);
  
  // 快照中最大的 zxid，在执行编辑日志时，这个值会不断更新，直到所有操作执行完
  long highestZxid = dt.lastProcessedZxid;
  TxnHeader hdr;
  try {
  
      // 从 lastProcessedZxid事务编号器开始，不断的从编辑日志中恢复剩下的还没有恢复的数据
      while (true) {
          // iterator points to
          // the first valid txn when initialized
          
          // 获取事务头信息，包括 zxid
          hdr = itr.getHeader();
          if (hdr == null) {
              //empty logs
              return dt.lastProcessedZxid;
          }
          if (hdr.getZxid() < highestZxid && highestZxid != 0) {
              LOG.error("{}(highestZxid) > {}(next log) for type {}",
                      highestZxid, hdr.getZxid(), hdr.getType());
          } else {
              highestZxid = hdr.getZxid();
          }
          try {
              
              // 根据编辑日志恢复数据到 DataTree 每 执行一次，对应的事务 idhighestZxid + 1
              processTransaction(hdr,dt,sessions, itr.getTxn());
          } catch(KeeperException.NoNodeException e) {
             throw new IOException("Failed to process transaction type: " +
                   hdr.getType() + " error: " + e.getMessage(), e);
          }
          listener.onTxnLoaded(hdr, itr.getTxn());
          if (!itr.next())
              break;
      }
  } finally {
      if (itr != null) {
          itr.close();
      }
  }
  return highestZxid;
}
```
10. `org.apache.zookeeper.server.persistence.FileTxnSnapLog#processTransaction`
```java
public void processTransaction(TxnHeader hdr,DataTree dt, Map<Long, Integer> sessions, Record txn)
  throws KeeperException.NoNodeException {
  ProcessTxnResult rc;
  switch (hdr.getType()) {
  case OpCode.createSession:
      sessions.put(hdr.getClientId(),
              ((CreateSessionTxn) txn).getTimeOut());
      if (LOG.isTraceEnabled()) {
          ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,
                  "playLog --- create session in log: 0x"
                          + Long.toHexString(hdr.getClientId())
                          + " with timeout: "
                          + ((CreateSessionTxn) txn).getTimeOut());
      }
      rc = dt.processTxn(hdr, txn);
      break;
  case OpCode.closeSession:
      sessions.remove(hdr.getClientId());
      if (LOG.isTraceEnabled()) {
          ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,
                  "playLog --- close session in log: 0x"
                          + Long.toHexString(hdr.getClientId()));
      }
      rc = dt.processTxn(hdr, txn);
      break;
  default:
  
      // 处理事务请求，创建节点、删除节点和其他的各种事务操作等
      rc = dt.processTxn(hdr, txn);
  }

  /**
   * Snapshots are lazily created. So when a snapshot is in progress,
   * there is a chance for later transactions to make into the
   * snapshot. Then when the snapshot is restored, NONODE/NODEEXISTS
   * errors could occur. It should be safe to ignore these.
   */
  if (rc.err != Code.OK.intValue()) {
      LOG.debug(
              "Ignoring processTxn failure hdr: {}, error: {}, path: {}",
              hdr.getType(), rc.err, rc.path);
  }
}
```
11. `org.apache.zookeeper.server.DataTree#processTxn`
```java
public ProcessTxnResult processTxn(TxnHeader header, Record txn, boolean isSubTxn)
    {
        ProcessTxnResult rc = new ProcessTxnResult();

        try {
            rc.clientId = header.getClientId();
            rc.cxid = header.getCxid();
            rc.zxid = header.getZxid();
            rc.type = header.getType();
            rc.err = 0;
            rc.multiResult = null;
            switch (header.getType()) {
                case OpCode.create:
                    CreateTxn createTxn = (CreateTxn) txn;
                    rc.path = createTxn.getPath();
                    
                    // 创建节点
                    createNode(
                            createTxn.getPath(),
                            createTxn.getData(),
                            createTxn.getAcl(),
                            createTxn.getEphemeral() ? header.getClientId() : 0,
                            createTxn.getParentCVersion(),
                            header.getZxid(), header.getTime(), null);
                    break;
                 // ... 省略 ...
            }
        } catch (KeeperException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed: " + header + ":" + txn, e);
            }
            rc.err = e.code().intValue();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed: " + header + ":" + txn, e);
            }
        }
        // ... 省略 ...
        return rc;
    }
```

### 选举机制
选举机制大致流程

![img.png](../image/zookeeper_源码_选举机制大致流程.png)

#### 选举准备
创建选票、通络通信监听、发送和接收的消息队列等等。

![img.png](../image/zookeeper_源码_选举机制准备.png)

1. `org.apache.zookeeper.server.quorum.QuorumPeer#startLeaderElection`
```java
synchronized public void startLeaderElection() {
 try {
     if (getPeerState() == ServerState.LOOKING) {
         currentVote = new Vote(myid, getLastLoggedZxid(), getCurrentEpoch());
     }
 } catch(IOException e) {
     RuntimeException re = new RuntimeException(e.getMessage());
     re.setStackTrace(e.getStackTrace());
     throw re;
 }

 // if (!getView().containsKey(myid)) {
//      throw new RuntimeException("My id " + myid + " not in the peer list");
  //}
  if (electionType == 0) {
      try {
          udpSocket = new DatagramSocket(getQuorumAddress().getPort());
          responder = new ResponderThread();
          responder.start();
      } catch (SocketException e) {
          throw new RuntimeException(e);
      }
  }
  this.electionAlg = createElectionAlgorithm(electionType);
}
```

#### 选举执行

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











