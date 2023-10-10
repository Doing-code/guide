# RocketMQ

> RocketMQ借鉴了Kafka

## RocketMQ入门

### 基本概念

- 消息：消息系统所传输信息的物理载体，是生产和消费数据的最小单位，每条消息必须属于某一个主题。

- 主题（topic）：topic表示一类消息的集合，每个主题包含若干条消息，每条消息只能属于一个主题，topic是RocketMQ进行消息订阅的基本单位。（Topic可以理解为一级分类）

  - 一个生产者可以同时发送多个Topic的消息，而一个消费者只对某个特定的Topic感兴趣，即只可以订阅和消费一个Topic的消息。
  
- 标签（Tag）：为消息设置的标签，用于同一主题下区分不同类型的消息。（Tag可以理解为二级分类）

  - 来自同一业务单元的消息，可以根据不同业务目的在同一主题下设置不同标签。消费者可以根据Tag实现对子主题的不同消费逻辑。
  
- 队列（Queue）:存储消息的物理实体。一个Topic中可以包含多个Queue，每个Queue中存放的就是该Topic的消息。一个Topic的Queue也被称为一个Topic中消息的分区（Partition）。

  - 一个Topic的Queue中的消息只能被一个消费者组中的一个消费者消费。一个Queue中的消息不允许同一个消费者组中的多个消费者同时消费。

- 消息标识（MessageId/Key）:RocketMQ中每个消息拥有唯一的MessageId，且可以携带具有业务标识的Key，以方便对消息的查询。

  - 不过需要注意的是，MessageId有两个：在生产者send()消息时会自动生成一个MessageId（msgId)，当消息到达Broker后，Broker也会自动生成一个MessageId(offsetMsgId)。msgId、offsetMsgId与key都称为消息标识。

  - msgId：由producer端生成，其生成规则为：producerIp + 进程pid + MessageClientIDSetter类的ClassLoader的hashCode + 当前时间 + AutomicInteger自增计数器
  
  - offsetMsgId：由broker端生成，其生成规则为：brokerIp + 物理分区的offset（Queue中的偏移量）
  
  - key：由用户指定的业务相关的唯一标识

### 系统架构

> 架构图来自RocketMQ官网

![](https://rocketmq.apache.org/zh/assets/images/RocketMQ%E9%83%A8%E7%BD%B2%E6%9E%B6%E6%9E%84-ee0435f80da5faecf47bca69b1c831cb.png)

RocketMQ 部署架构上主要分为四部分：

- 生产者 Producer：发布消息的角色。Producer通过 MQ 的负载均衡模块选择相应的 Broker 集群队列进行消息投递，投递的过程支持快速失败和重试。

- 消费者 Consumer：消息消费的角色。

  - 支持以推（push），拉（pull）两种模式对消息进行消费。
   
  - 同时也支持集群方式和广播方式的消费。
 
  - 提供实时消息订阅机制。
 
> 消费者组是同一类消费者的集合，这类Consumer消费的是同一个Topic类型的消息。
>
> 消费者组只能消费一个Topic的消息，不能同时消费多个Topic消息；一个消费者组中的消费者必须订阅完全相同的Topic
>
> 一个Topic类型的消息可以被多个消费者组同时消费。但一个消费者组只能消费一个Topic的消息。
 
- 名字服务器 NameServer：NameServer是一个简单的 Topic 路由注册中心，支持 Topic、Broker 的动态注册与发现。（可以理解为Kafka旧版本依赖的Zookeeper注册中心，但有所不同）

  主要包括两个功能：

  - **Broker管理**，NameServer接受Broker集群的注册信息并且保存下来作为路由信息的基本数据。然后提供心跳检测机制，检查Broker是否还存活；
  
  - **路由信息管理**，每个NameServer将保存关于 Broker 集群的整个路由信息和用于客户端查询的队列信息。Producer和Consumer通过NameServer就可以知道整个Broker集群的路由信息，从而进行消息的投递和消费。
  
  NameServer通常会有多个实例部署，各实例间相互不进行信息通讯。Broker是向每一台NameServer注册自己的路由信息，所以每一个NameServer实例上面都保存一份完整的路由信息。当某个NameServer因某种原因下线了，客户端仍然可以向其它NameServer获取路由信息。
  
- 代理服务器 Broker：Broker主要负责消息的存储、投递和查询以及服务高可用保证。（可以理解为Kafka的Broker）。Broker又可以搭建主从架构。

> 每个 Broker 与 NameServer 集群中的所有节点建立长连接，定时注册 Topic 信息到所有 NameServer。
>
> Producer 与 NameServer 集群中的其中一个节点建立长连接，定期从 NameServer 获取Topic路由信息，并向提供 Topic 服务的 Master 建立长连接，且定时向 Master 发送心跳。Producer 完全无状态。
>
> Consumer 与 NameServer 集群中的其中一个节点建立长连接，定期从 NameServer 获取 Topic 路由信息，并向提供 Topic 服务的 Master、Slave 建立长连接，且定时向 Master、Slave发送心跳。Consumer 既可以从 Master 订阅消息，也可以从Slave订阅消息。

  Broker Server的功能模块：
  
  - Remoting Module：整个Broker的实体，负责处理来自clients端的请求。而这个Broker实体则由以下模块构成。
  
  - Client Manager：客户端管理器。负责接收、解析客户端(Producer/Consumer)请求，管理客户端。例如，维护Consumer的Topic订阅信息。
  
  - Store Service：存储服务。提供方便简单的API接口，处理消息存储到物理硬盘和消息查询功能。
  
  - HA Service：高可用服务，提供Master Broker 和 Slave Broker之间的数据同步功能。
  
  - Index Service：索引服务。根据特定的Message key，对投递到Broker的消息进行索引服务，同时也提供根据Message Key对消息进行快速查询的功能。

#### 工作流程

1. 启动NameServer，NameServer启动后开始监听端口，等待Broker、Producer、Consumer连接。

2. 启动Broker时，Broker会与所有的NameServer建立并保持长连接，然后每30秒向NameServer定时发送心跳包。

3. 发送消息前，可以先创建Topic，创建Topic时需要指定该Topic要存储在哪些Broker上，当然，在创建Topic时也会将Topic与Broker的关系写入到NameServer中。不过，这个步骤是可选的，也可以在发送消息时自动创建Topic。

4. Producer发送消息，启动时先跟NameServer集群中的其中一台建立长连接，并从NameServer中获取路由信息，即当前发送的Topic消息的Queue与Broker的地址（IP+Port）的映射关系。然后根据算法策略从队选择一个Queue，与队列所在的Broker建立长连接从而向Broker发消息。当然，在获取到路由信息后，Producer会首先将路由信息缓存到本地，再每30秒从NameServer更新一次路由信息。

5. Consumer跟Producer类似，跟其中一台NameServer建立长连接，获取其所订阅Topic的路由信息，然后根据算法策略从路由信息中获取到其所要消费的Queue，然后直接跟Broker建立长连接，开始消费其中的消息。Consumer在获取到路由信息后，同样也会每30秒从NameServer更新一次路由信息。不过不同于Producer的是，Consumer还会向Broker发送心跳，以确保Broker的存活状态。

### 数据复制与刷盘策略

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_数据复制和刷盘策略.png)

#### 复制策略

复制策略是Broker的Master与Slave间的数据同步方式。分为同步复制与异步复制：

  - 同步复制：消息写入master后，master会等待slave同步数据成功后才向producer返回成功ACK。
  
  - 异步复制：消息写入master后，master立即向producer返回成功ACK，无需等待slave同步数据成功。

#### 刷盘策略

刷盘策略指的是broker中消息的落盘方式，即消息发送到broker内存后消息持久化到磁盘的方式。分为同步刷盘与异步刷盘：

- 同步刷盘：当消息持久化到broker的磁盘后才算是消息写入成功。

- 异步刷盘：当消息写入到broker的内存后即表示消息写入成功，无需等待消息持久化到磁盘。

  - 消息写入到Broker的内存，一般是写入到了PageCache。

  - 对于异步刷盘策略，消息会写入到Page Cache后立即返回成功ACK。但并不会立即做落盘操作，而是当PageCache到达一定量时会自动进行落盘。

### Broker集群模式

#### 单Master

只有一个broker（其本质上就不能称为集群）。这种方式也只能是在测试时使用，生产环境下不能使用，因为存在单点故障问题。

#### 多Master

broker集群仅由多个master构成，不存在Slave。同一Topic的各个Queue会平均分布在各个master节点上。

- 优点：配置简单，单个Master宕机或重启维护对应用无影响，在磁盘配置为RAID10时，即使机器宕机不可恢复情况下，由于RAID10磁盘非常可靠，消息也不会丢（异步刷盘丢失少量消息，同步刷盘一条不丢），性能最高；（基于Master配置了RAID磁盘阵列）

- 缺点：单台机器宕机期间，这台机器上未被消费的消息在机器恢复之前不可订阅（不可消费），消息实时性会受到影响。

#### 多Master多Slave模式-异步复制

broker集群由多个master构成，每个master又配置了多个slave。master与slave的关系是主备关系，即master负责处理消息的读写请求，而slave仅负责消息的备份与master宕机后的角色切换。

异步复制，即消息写入master成功后，master立即向producer返回成功ACK，无需等待slave同步数据成功。

该模式的最大特点之一是，当master宕机后slave能够自动切换为master。不过由于slave从master的同步具有短暂的延迟（毫秒级），所以当master宕机后，这种异步复制方式可能会存在少量消息的丢失问题。

#### 多Master多Slave模式-同步双写

该模式是多Master多Slave模式的同步复制实现。所谓同步双写，指的是消息写入master成功后，master会等待slave同步数据成功后才向producer返回成功ACK，即master与slave都要写入成功后才会返回成功ACK，也即双写。

该模式与异步复制模式相比，优点是消息的安全性更高，不存在消息丢失的情况。但单个消息的RT（响应时间）略高，从而导致性能要略低（大约低10%）。

## RocketMQ工作原理

### 消息的生产

#### 消息的生产过程

Producer可以将消息写入到某Broker中的某Queue中，其经历了如下过程：

1. Producer发送消息之前，会先向NameServer发出获取消息Topic的路由信息的请求。

2. NameServer返回该Topic的路由表及Broker列表。

3. Producer根据代码中指定的Queue选择策略，从Queue列表中选出一个队列，用于后续存储消息。

4. Producer对消息做一些特殊处理，例如，消息本身超过4M，则会对其进行压缩。

5. Producer向选择出的Queue所在的Broker发出RPC请求，将消息发送到选择出的Queue。

**路由表**：实际是一个Map，key为Topic名称，value是一个QueueData实例列表。QueueData并不是一个Queue对应一个QueueData，而是一个Broker中该Topic的所有Queue对应一个QueueData。即，只要涉及到该Topic的Broker，一个Broker对应一个QueueData。QueueData中包含brokerName。简单来说，路由表的key为Topic名称，value则为所有涉及该Topic的BrokerName列表。

> brokerName 可以理解为集群名称，类比Elasticsearch配置文件的`cluster.name: elasticsearch`
>
> 假设一个Topic（t1）在创建时设置了4个Queue（写队列），那么Topic的4个Queue构成一个QueueData。
>
> Topic假设在Broker1中，那么Broker1中的Topic对应一个QueueData（由4个Queue构成），假设Broker1处于集群名称为broker-a的集群中
>
> 则有路由表> t1 : broker-a

**Broker列表**：其实际也是一个Map。key为brokerName，value为BrokerData。一个brokerName名称相同的Master-Slave小集群对应一个BrokerData。BrokerData中包含brokerName及一个map。该map的key为brokerId，value为该broker对应的地址。brokerId为0表示该broker为Master，非0表示Slave。（可以理解为服务地址，服务发现）

#### Queue选择策略

对于无序消息，其Queue选择算法，也称为消息投递算法，常见的有两种：

- 轮询：默认选择算法。该算法保证了每个Queue中可以均匀的获取到消息。

> 该算法存在一个问题：由于某些原因，在某些Broker上的Queue可能投递延迟较严重。从而导致Producer的缓存队列中出现较大的**消息积压**，影响消息的投递性能。

- 最小投递延迟：该算法会统计每次消息投递的时间延迟，然后根据统计出的结果将消息投递到时间延迟最小的Queue。如果延迟相同，则采用轮询算法投递。

> 该算法也存在一个问题：消息在Queue上的分配不均匀。投递延迟小的Queue其可能会存在大量的消息。而**对该Queue的消费者压力会增大，降低消息的消费能力**，可能会导致MQ中**消息的堆积**。

### 消息的存储

RocketMQ中的消息存储在本地文件系统中，这些相关文件默认在当前用户主目录下的store目录中。

> abort：该文件在Broker启动后会自动创建，正常关闭Broker，该文件会自动消失。若在没有启动Broker的情况下，发现这个文件是存在的，则说明之前Broker的关闭是非正常关闭。
>
> checkpoint：其中存储着commitlog、consumequeue、index文件的最后刷盘时间戳
>
> commitlog：其中存放着commitlog文件，而消息是写在commitlog文件中的
>
> config：存放着Broker运行期间的一些配置数据
>
> consumequeue：其中存放着consumequeue文件，队列就存放在这个目录中
>
> index：其中存放着消息索引文件indexFile
>
> lock：运行期间使用到的全局资源锁

#### commitlog

该文件在源码中被命名为mappedFile。commitlog目录中存放着很多的mappedFile文件，当前Broker中的所有消息都是落盘到这些mappedFile文件中的。mappedFile文件大小为1G（小于等于1G），文件名由20位十进制数构成，表示当前文件的第一条消息的起始位移偏移量。

> 第一个文件名一定是20位0构成的。因为第一个文件的第一条消息的偏移量commitlog offset为0。
>
> 当第一个文件放满时，则会自动生成第二个文件继续存放消息。假设第一个文件大小是1073741820字节（1G = 1073741824字节），则第二个文件名就是00000000001073741824。
>
> 以此类推，第n个文件名应该是前n-1个文件大小之和。一个Broker中所有mappedFile文件的commitlog offset是连续的。

一个Broker中仅包含一个commitlog目录，所有的mappedFile文件都是存放在该目录中。无论当前Broker中存放着多少Topic的消息，这些消息都是被顺序写入到了mappedFile文件中的。这些消息在Broker中存放时并没有被按照Topic进行分类存放。

> mappedFile文件是顺序读写的文件，所有其访问效率很高。

##### 消息单元

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_消息单元.png)

mappedFile文件内容由一个个的消息单元构成。每个消息单元中包含消息总长度、消息的物理位置、消息体等近20余项消息相关属性。

消息单元中是包含Queue相关属性的。需要搞清楚意commitlog与queue间的关系是什么

> 一个mappedFile文件中第m+1个消息单元的commitlog offset偏移量：L(m+1) = L(m) + MsgLen(m) (m >= 0)

#### consumequeue

Consume Queue存储消息在Commit log中的偏移位置信息。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_consumerqueue.png)

consumequeue文件是commitlog的索引文件，可以根据consumequeue定位到具体的消息。

每个consumequeue文件可以包含30w个索引条目，每个索引条目包含了三个消息重要属性：消息在mappedFile文件中的偏移量CommitLog Offset、消息长度、消息Tag的hashcode值。这三个属性占20个字节，所以每个文件的大小是固定的30w * 20字节。

一个consumequeue文件中所有消息的Topic一定是相同的。但每条消息的Tag可能是不同的。

```text
|-------------------|------|----------------------|
|      8 Byte       |4 Byte|      8 Byte          |
|-------------------|------|----------------------|
| Commit Log Offset | Size | Message Tag HashCode |
|-------------------|------|----------------------|
```

#### 文件的读写

在RocketMQ中，无论是消息本身还是消息索引，都是存储在磁盘上的。

首先，RocketMQ对文件的读写操作是通过mmap零拷贝进行的，将对文件的操作转化为直接对内存地址进行操作，从而极大地提高了文件的读写效率。

其次，consumequeue中的数据是顺序存放的，还引入了PageCache的预读取机制，使得对consumequeue文件的读取几乎接近于内存读取，即使在有消息堆积情况下也不会影响性能。

而RocketMQ中可能会影响性能的是对commitlog文件的读取。因为对commitlog文件来说，读取消息时会产生大量的随机访问，而随机访问会严重影响性能。不过，如果选择合适的系统IO调度算法，比如设置调度算法为Deadline（采用SSD固态硬盘的话），随机读的性能也会有所提升。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_文件读写.png)

##### 消息写入

一条消息进入到Broker后经历了以下几个过程才最终被持久化：

1. Broker根据queueId，获取到该消息对应索引条目（indexFile）要在consumequeue目录中的写入偏移量，即QueueOffset。

2. 将queueId、queueOffset等数据，与消息一起封装为消息单元。

3. 将消息单元写入到commitlog。

4. 同时，形成消息索引条目。

5. 将消息索引条目分发到相应的consumequeue。

##### 消息拉取

当Consumer来拉取消息时会经历以下几个步骤：

1. Consumer获取到其要消费消息所在Queue的**消费偏移量offset**，计算出其要消费消息的**消息offset**。

> 消费offset即消费进度，consumer对某个Queue的消费offset，即消费到了该Queue的第几条消息。消息offset = 消费offset + 1

2. Consumer向Broker发送拉取请求，其中会包含其要拉取消息的Queue、消息offset及消息Tag。

3. Broker计算在该consumequeue中的queueOffset。（queueOffset = 消息offset * 20字节）

4. 从该queueOffset处开始向后查找第一个指定Tag的索引条目。

5. 解析该索引条目的前8个字节，即可定位到该消息在commitlog中的commitlog offset

6. 从对应commitlog offset中读取消息单元，并发送给Consumer。

#### 与Kafka的对比

RocketMQ的很多思想来源于Kafka，比如其中的commitlog与consumequeue。

RocketMQ中的commitlog目录与consumequeue的结合就类似于Kafka中的partition分区目录。而mappedFile文件就类似于Kafka中的segment段。

- Kafka中的Topic的消息被分割为一个或多个partition。partition是一个物理概念，对应到系统上就是topic目录下的一个或多个目录。每个partition中包含的文件称为segment，是具体存放消息的文件。

- Kafka中消息存放的目录结构是：topic目录下有partition目录，partition目录下有segment文件。

- Kafka中没有二级分类标签Tag这个概念。且Kafka中无需索引文件。因为生产者是将消息直接写在了partition中的，消费者也是直接从partition中读取数据的。

### indexFile

除了通过通常的指定Topic进行消息消费外，RocketMQ还提供了根据key进行消息查询的功能。该查询是通过store目录中的index子目录中的indexFile进行索引实现的快速查询。当然，这个indexFile中的索引数据是在包含了key的消息被发送到Broker时写入的。如果消息中没有包含key，则不会写入。

> 可以理解为Kafka中的写入指定分区（待验证）

#### indexFile结构

每个Broker中会包含一组indexFile，每个indexFile都是以一个时间戳命名的（这个indexFile被创建时的时间戳）。每个indexFile文件由三部分构成：indexHeader，slots槽位，indexes索引数据。每个indexFile文件中包含500w个slot槽。而每个slot槽又可能会挂载很多的index索引单元。

在根据业务key进行查询时，查询条件除了key之外，还需要指定一个要查询的时间戳，表示要查询不大于该时间戳的最新的消息，即查询指定时间戳之前存储的最新消息。

indexFile文件是何时创建的？其创建的条件（时机）有两个：

- 当第一条带key的消息发送来后，系统发现没有indexFile，此时会创建第一个indexFile文件。

- 当一个indexFile中挂载的index索引单元数量超出2000w个时，会创建新的indexFile。

  - 当带key的消息发送到来后，系统会找到最新的indexFile，并从其indexHeader的最后4字节中读取到indexCount。若indexCount >= 2000w时，会创建新的indexFile。
  
由于可以推算出，一个indexFile的最大大小是：(40 + 500w * 4 + 2000w * 20)字节

```text
|-------------|-------|---------|
| indexHeader | slots | indexes |
|-------------|-------|---------|
```

1. indexHeader固定40个字节，其中存放着如下数据：（indexHeader结构示意图）

```text
|----------------|--------------|----------------|--------------|---------------|------------|
|     8 bytes    |     8 bytes  |      8 bytes   |    8 bytes   |     4 bytes   |   4 bytes  |
|----------------|--------------|----------------|--------------|---------------|------------|
| beginTimestamp | endTimestamp | beginPhyoffset | endPhyoffset | hashSlotCount | indexCount |
|----------------|--------------|----------------|--------------|---------------|------------|
```

- beginTimestamp：该indexFile中第一条消息的存储时间。

- endTimestamp：该indexFile中最后一条消息存储时间。

- beginPhyoffset：该indexFile中第一条消息在commitlog中的偏移量commitlog offset。

- endPhyoffset：该indexFile中最后一条消息在commitlog中的偏移量commitlog offset。

- hashSlotCount：已经填充有index的slot数量（并不是每个slot槽下都挂载有index索引单元，这里统计的是所有挂载了index索引单元的slot槽的数量）。

- indexCount：该indexFile中包含的索引单元个数（统计出当前indexFile中所有slot槽下挂载的所有index索引单元的数量之和）。

2. indexFile中最复杂的是Slots与Indexes间的关系。在实际存储时，Indexes是在Slots后面的，但为了便于理解，将它们的关系展示为如下形式：

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_slots与indexes关系结构示意图.png)

而key的hash值 % 500w的结果即为slot槽位，然后将该slot值修改为该index索引单元的indexNo，根据这个indexNo可以计算出该index单元在indexFile中的位置。

不过，该取模结果的重复率是很高的，为了解决该问题，在每个index索引单元中增加了preIndexNo，用于指定该slot中当前index索引单元的前一个index索引单元。

而slot中始终存放的是其下最新的index索引单元的indexNo，这样的话，只要找到了slot就可以找到其最新的index索引单元，而通过这个index索引单元就可以找到其之前的所有index索引单元。

> indexNo是一个在indexFile中的流水号，从0开始依次递增。即在一个indexFile中所有indexNo是依次递增的。indexNo在index索引单元中是没有体现的，其是通过indexes中依次数出来的。

3. index索引单元默认20个字节，其存放这以下四个属性：（index索引单元结构示意图）

```text
|---------|-----------|----------|------------|
| 4 bytes |  8 bytes  | 4 bytes  |  4 bytes   |
|---------|-----------|----------|------------|
| keyHash | phyOffset | timeDiff | preIndexNo |
|---------|-----------|----------|------------|
```

- keyHash：消息中指定的业务key的hash值。

- phyOffset：**当前key对应的消息在commitlog中的偏移量commitlog offset**。

- timeDiff：当前key对应消息的存储时间与当前indexFile创建时间的时间差。

- preIndexNo：当前slot下当前index索引单元的前一个index索引单元的indexNo。

#### 查询流程

计算指定消息key的slot槽位序号：`slot槽位序号 = key的hash % 500w`。

计算槽位序号为n的slot在indexFile中的起始位置：`slot(n)位置 = 40 + (n - 1) * 4`。

计算indexNo为m的index在indexFile中的位置：`index(m)位置 = 40 + 500w * 4 + (m - 1) * 20`。

> 40为indexFile中indexHeader的字节数；4为一个slot占用字节数；500w * 4 是所有slots所占的字节数。

具体查询流程如下：

![](https://github.com/Doing-code/guide/blob/main/image/根据key查询的流程.png)

### 消息的消费

消费者从Broker中获取消息的方式有两种：pull拉取方式和push推动方式。消费者组对于消息消费的模式又分为两种：集群消费Clustering和广播消费Broadcasting。

#### 消费类型

- pull

**Consumer主动从Broker中拉取消息**，主动权由Consumer控制。一旦获取了批量消息，就会启动消费过程。不过，该方式的实时性较弱，即Broker中有了新的消息时消费者并不能及时发现并消费。

- push

该模式下**Broker收到数据后会主动推送给Consumer**。该获取方式一般实时性较高。

该获取方式是典型的**发布-订阅**模式，即Consumer向其关联的Queue注册了监听器，一旦发现有新的消息到来就会触发回调的执行，回调方法是Consumer去Queue中拉取消息。而这些都是基于Consumer与Broker间的长连接的。长连接的维护是需要消耗系统资源的。

pull：需要应用去实现对关联Queue的遍历，实时性差；但便于应用控制消息的拉取。

push：封装了对关联Queue的遍历，实时性强，但会占用较多的系统资源。

#### 消费模式

- 广播消费

广播消费模式下，相同Consumer Group的每个Consumer实例都接收同一个Topic的全量消息。即每条消息都会被发送到Consumer Group中的每个Consumer。

- 集群消费

集群消费模式下，相同Consumer Group的每个Consumer实例平均分摊同一个Topic的消息。即每条消息只会被发送到Consumer Group中的某个Consumer。

##### 消息进度

- 广播模式：消费进度保存在consumer端。因为广播模式下consumer group中每个consumer都会消费所有消息，但它们的消费进度是不同。所以consumer各自保存各自的消费进度。

- 集群模式：消费进度保存在broker中。consumer group中的所有consumer共同消费同一个Topic中的消息，同一条消息只会被消费一次。消费进度会参与到了消费的负载均衡中，故消费进度是需要共享的。下图是broker中存放的各个Topic的各个Queue的消费进度。

#### Rebalance机制-(分区再平衡)

> 可以理解为Kafka中分区的再分配

Rebalance机制的前提是：集群消费。

Rebalance即再均衡，指的是，将⼀个Topic下的多个Queue在同⼀个Consumer Group中的多个Consumer间进行重新分配的过程。

Rebalance机制的本意是为了提升消息的并行消费能力。例如，⼀个Topic下5个队列，在只有1个消费者的情况下，这个消费者将负责消费这5个队列的消息。如果此时我们增加⼀个消费者，那么就可以给其中⼀个消费者分配2个队列，给另⼀个分配3个队列，从而提升消息的并行消费能力。

##### Rebalance限制

由于**⼀个队列最多分配给⼀个消费者**，因此当某个消费者组下的消费者实例数量大于队列的数量时，多余的消费者实例将分配不到任何队列。

##### Rebalance问题

Rebalance的在提升消费能力的同时，也带来一些问题：

- 消费暂停：在只有一个Consumer时，其负责消费所有队列；但是在新增了一个Consumer后会触发Rebalance的发生。此时原Consumer就需要暂停部分队列的消费，等到这些队列分配给新的Consumer后，这些暂停消费的队列才能继续被消费。

- 消费重复：Consumer 在消费新分配给自己的队列时，必须接着之前Consumer 提交的消费进度的offset继续消费。（但是在提交前进行了Rebalance，已经消费的消息又要在消费一次）然而默认情况下，offset是异步提交的，这个异步性导致提交到Broker的offset与Consumer实际消费的消息并不一致。这个不一致的差值就是可能会重复消费的消息。

- 消费突刺：由于Rebalance可能导致重复消费，如果需要重复消费的消息过多，或者因为Rebalance暂停时间过长从而导致积压了部分消息。那么有可能会导致在Rebalance结束之后瞬间需要消费很多消息。

##### Rebalance产生的原因

导致Rebalance产生的原因，无非就两个：**消费者所订阅Topic的Queue数量发生变化**，或**消费者组中消费者的数量发生变化**。

- Queue数量发生变化的场景：Broker扩容或缩容、Broker升级运维、Broker与NameServer间的网络异常、Queue扩容或缩容。

- 消费者数量发生变化的场景：Consumer Group扩容或缩容、Consumer升级运维、Consumer与NameServer间网络异常。

##### Rebalance过程

在Broker中维护着多个Map集合，这些集合中动态存放着当前Topic中Queue的信息、Consumer Group中Consumer实例的信息。一旦发现**消费者所订阅的Queue数量发生变化**，或**消费者组中消费者的数量发生变化**，立即向Consumer Group中的每个实例发出Rebalance通知。

> TopicConfigManager：key是topic名称，value是TopicConfig，TopicConfig中维护着该Topic中所有Queue的数据。
>
> ConsumerManager：key是Consumer Group Id，value是ConsumerGroupInfo。ConsumerGroupInfo中维护着该Group中所有Consumer实例数据。
>
> ConsumerOffsetManager：：key为Topic与订阅该Topic的Group的组合,即topic@group。value是一个内层Map。内层Map的key为QueueId，内层Map的value为该Queue的消费进度offset。

Consumer实例在接收到通知后会采用Queue分配算法自己获取到相应的Queue，即由Consumer实例自主进行Rebalance。

##### 与Kafka对比

> 可以参考：https://github.com/Doing-code/guide/blob/main/JavaGuide/Kafka.md#分区的分配以及再平衡

在Kafka中，一旦发现出现了Rebalance条件，Broker会调用Group Coordinator来完成Rebalance。Coordinator是Broker中的一个进程。Coordinator会在Consumer Group中选出一个Group Leader。由这个Leader根据自己所在组情况完成Partition分区的再分配。这个再分配结果会上报给Coordinator，并由Coordinator同步给Group中的所有Consumer实例。

Kafka中的Rebalance是由Consumer Leader完成的。而RocketMQ中的Rebalance是由**每个Consumer自身完成的**，Group中不存在Leader。

#### Queue分配策略

> 分区（Queue）: 消费者（Consumer）= 1:1；消费者（Consumer）: 分区（Queue）= 1:n

**一个Topic中的Queue只能由Consumer Group中的一个Consumer进行消费，而一个Consumer可以同时消费多个Queue中的消息**。那么Queue与Consumer间的配对关系是如何确定的，即Queue要分配给哪个Consumer进行消费，这是存在算法策略的。

常见的有四种策略。这些策略是通过在创建Consumer时的构造器传进去的。

##### 平均分配策略

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_平均分配策略.png)

该算法是要根据**avg = QueueCount / ConsumerCount** 的计算结果进行分配的。如果能够整除，则按顺序将avg个Queue逐个分配Consumer；如果不能整除，则将多余出的Queue按照Consumer顺序逐个分配。

##### 环形平均策略

环形平均算法是指，根据消费者的顺序，依次在由queue队列组成的环形图中逐个分配。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_环形平均策略.png)

##### 一致性hash策略

![](https://github.com/Doing-code/guide/blob/main/image/rocketma_一致性hash策略.png)

该算法会将consumer的hash值作为Node节点存放到hash环上，然后将queue的hash值也放到hash环上，通过顺时针方向，距离queue最近的那个consumer就是该queue要分配的consumer。（会导致分配不均）

##### 同机房策略

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_同机房策略.png)

该算法会根据queue的部署机房位置和consumer的位置，过滤出当前consumer相同机房的queue。然后按照平均分配策略或环形平均策略对同机房queue进行进一步的分配。如果没有同机房queue，则按照平均分配策略或环形平均策略对所有queue进行分配。

##### 对比

一致性hash算法存在的问题：两种平均分配策略的分配效率较高，一致性hash策略的较低。因为一致性hash算法较复杂。另外，一致性hash策略分配的结果也很大可能上存在不平均的情况。

一致性hash算法存在的意义：其可以有效减少由于消费者组扩容或缩容所带来的大量的Rebalance。

一致性hash算法的应用场景：Consumer数量变化较频繁的场景。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_queue分配策略对比.png)

#### 至少一次原则

RocketMQ有一个原则：每条消息必须要被成功消费一次。

那么什么是成功消费呢？Consumer在消费完消息后会向其消费进度记录器提交其消费消息的offset，offset被成功记录到记录器中，那么这条消费就被成功消费了。

什么是消费进度记录器？对于广播消费模式来说，Consumer本身就是消费进度记录器；对于集群消费模式来说，Broker是消费进度记录器。

### 订阅关系的一致性

> RocketMP官网：https://rocketmq.apache.org/zh/docs/4.x/bestPractice/07subscribe

订阅关系：一个消费者组订阅一个 Topic 的某一个 Tag，这种记录被称为订阅关系。

订阅关系一致：**同一个消费者组下所有消费者实例所订阅的Topic、Tag必须完全一致**。如果订阅关系（消费者组名-Topic-Tag）不一致，会导致消费消息紊乱，甚至消息丢失。

比如以下就是一个订阅关系不一致的案例，同消费组，但是两个消费者订阅的主题不一样。（虽然这样的操作是被允许的，但是业务上应该不允许吧）

```text
Group1      A       B
          TopicA   TopicB
          Tag2     Tag3
```

### offset管理

> 这里的offset指的是Consumer的消费进度offset。
>
> Kafka的实现是存入Zookeeper：__consumer_offsets 主题里面采用 key 和 value 的方式存储数据。key 是 group.id+topic+分区号，value 就是当前 offset 的值。

消费进度offset是用来记录每个Queue的不同消费组的消费进度的。根据消费进度记录器的不同，可以分为两种模式：本地模式和远程模式。

#### offset本地管理模式

当消费模式为**广播消费**时，offset使用本地模式存储。因为每条消息会被所有的消费者消费，每个消费者管理自己的消费进度，各个消费者之间不存在消费进度的交集。

Consumer在广播消费模式下offset相关数据以json的形式持久化到Consumer本地磁盘文件中，默认文件路径为当前用户主目录下的`.rocketmq_offsets/${clientId}/${group}/Offsets.json`。其中`${clientId}`为当前消费者id，默认为`ip@DEFAULT`；`${group}`为消费者组名称。

#### offset远程管理模式

当消费模式为集群消费时，offset使用远程模式管理。所有Cosnumer实例对消息采用的是均衡消费，所有Consumer共享Queue的消费进度。

Consumer在集群消费模式下offset相关数据以json的形式持久化到Broker磁盘文件中，文件路径为当前用户主目录下的`store/config/consumerOffset.json`。

Broker启动时会加载这个文件，并写入到一个双层Map（ConsumerOffsetManager）。外层map的key为topic@group，value为内层map。内层map的key为queueId，value为offset。当发生Rebalance时，新的Consumer会从该Map中获取到相应的数据来继续消费。

集群模式下offset采用远程管理模式，主要是为了保证Rebalance机制。

#### offset用途

消费者是如何从最开始持续消费消息的？消费者要消费的第一条消息的起始位置是用户自己通过`consumer.setConsumeFromWhere()`方法指定的。

在Consumer启动后，其要消费的第一条消息的起始位置常用的有三种，这三种位置可以通过枚举类型常量设置。这个枚举类型为ConsumeFromWhere。

```java
public enum ConsumeFromWhere {
    CONSUME_FROM_LAST_OFFSET,
    CONSUME_FROM_FIRST_OFFSET,
    CONSUME_FROM_TIMESTAMP;

    private ConsumeFromWhere() {
    }
}
```

- CONSUME_FROM_LAST_OFFSET：从queue的当前最后一条消息开始消费。（从最新的下一条数据开始读）

- CONSUME_FROM_FIRST_OFFSET：从queue的第一条消息开始消费。（从偏移量0处开始读）

- CONSUME_FROM_TIMESTAMP：从指定的具体时间戳位置的消息开始消费。这个具体时间戳是通过另外一个语句指定的。`consumer.setConsumeTimestamp("20210701080000")，格式：yyyyMMddHHmmss` 

当消费完一批消息后，Consumer会提交其消费进度offset给Broker，Broker在收到消费进度后会将其更新到那个双层Map（ConsumerOffsetManager）及consumerOffset.json文件中，然后向该Consumer进行ACK，而ACK内容中包含三项数据：当前消费队列的最小offset（minOffset）、最大offset（maxOffset）、及下次消费的起始offset（nextBeginOffset）。

#### 重试队列

当RocketMQ对消息的消费出现异常时，会将发生异常的消息的offset提交到Broker中的重试队列。系统在发生消息消费异常时会为当前的topic@group创建一个重试队列，该队列以`%RETRY%`开头，到达重试时间后进行消费重试。

#### offset位移

集群消费模式下，Consumer消费完消息后会向Broker提交消费进度offset，其提交方式分为两种：

- 同步提交：消费者在消费完一批消息后会向broker提交这些消息的offset，然后等待broker的成功响应。若在等待超时之前收到了成功响应，则继续读取下一批消息进行消费（从ACK中获取nextBeginOffset）。若没有收到响应，则会重新提交，直到获取到响应。而在这个等待过程中，消费者是阻塞的。其严重影响了消费者的吞吐量。

- 异步提交：消费者在消费完一批消息后向broker提交offset，但无需等待Broker的成功响应，可以继续读取并消费下一批消息。这种方式增加了消费者的吞吐量。但需要注意，broker在收到提交的offset后，还是会向消费者进行响应的。可能还没有收到ACK，此时Consumer会从Broker中直接获取nextBeginOffset。

### 消费幂等

> RocketMQ能够保证消息不丢失，但不能保证消息不重复

幂等：若某操作执行多次与执行一次对系统产生的影响是相同的，则称该操作是幂等的。

那什么情况下可能会出现消息被重复消费呢？最常见的有以下三种情况：

- **发送时消息重复**：当一条消息已被成功发送到Broker并完成持久化，但因为网络波动**导致Broker对Producer应答失败**。如果此时Producer意识到消息发送失败并尝试再次发送消息，此时Broker中就可能会出现两条内容相同并且Message ID也相同的消息，那么后续Consumer就一定会消费两次该消息。

- **消费时消息重复**：消息已投递到Consumer并完成业务处理，但因为网络波动**导致Consumer给Broker反馈应答失败**。Broker没有接收到消费成功响应。为了保证消息至少被消费一次的原则，Broker将在网络恢复后再次尝试投递之前已被处理过的消息。此时消费者就会收到与之前处理过的内容相同、Message ID也相同的消息。

- **Rebalance时消息重复**：当Consumer Group中的Consumer数量发生变化时，或其订阅的Topic的Queue数量发生变化时，会触发Rebalance，此时Consumer可能会收到曾经被消费过的消息。（触发Rebalance时，Consumer会被暂停）

#### 解决方案

幂等解决方案的设计中涉及到两项要素：幂等令牌与唯一性处理。只要充分利用好这两要素，就可以设计出好的幂等解决方案。

- 幂等令牌：是生产者和消费者两者中的既定协议，通常指具备唯⼀业务标识的字符串。例如，订单号、流水号。一般由Producer随着消息一同发送来的。

- 唯一性处理：服务端通过采用⼀定的算法策略，保证同⼀个业务逻辑不会被重复执行成功多次。例如，对同一笔订单的多次支付操作，只会成功一次。

对于常见的系统，幂等性操作的通用性解决方案是：

1. 首先通过缓存去重。在缓存中如果已经存在了某幂等令牌，则说明本次操作是重复性操作；若缓存没有命中，则进入下一步。

2. 在唯一性处理之前，先在数据库中查询幂等令牌作为索引的数据是否存在。若存在，则说明本次操作为重复性操作；若不存在，则进入下一步。

3. 在同一事务中完成三项操作：唯一性处理后，将幂等令牌写入到缓存，并将幂等令牌作为唯一索引的数据写入到DB中。

> 该解决方案存在bug，还不够稳定

以支付场景为例：

1. 当支付请求到达后，首先在Redis缓存中却获取key为支付流水号的缓存value。若value不空，则说明本次支付是重复操作，业务系统直接返回调用侧重复支付标识；若value为空，则进入下一步操作。

2. 到DBMS中根据支付流水号查询是否存在相应实例。若存在，则说明本次支付是重复操作，业务系统直接返回调用侧重复支付标识；若不存在，则说明本次操作是首次操作，进入下一步完成唯一性处理。

3. 在分布式事务中完成三项操作

   - 完成支付任务。
  
   - 将当前支付流水号作为key，任意字符串作为value，通过set(key, value, expireTime)将数据写入到Redis缓存。
  
   - 将当前支付流水号作为主键，与其它相关数据共同写入到DBMS。

#### 消费幂等实现

消费幂等的解决方案很简单：为消息指定不会重复的唯一标识。因为Message ID有可能出现重复的情况，所以真正安全的幂等处理，不建议以Message ID作为处理依据。最好的方式是以业务唯一标识作为幂等处理的关键依据，而业务的唯一标识可以通过消息Key设置。

### 消息堆积与消费延迟

消息处理流程中，如果Consumer的消费速度跟不上Producer的发送速度，MQ中未处理的消息会越来越多（进的多出的少），这部分消息就被称为**堆积消息**。消息出现堆积进而会造成消息的**消费延迟**。（一旦消息堆积，消费者消费能力有限，那么必然会造成消息处理缓慢）

以下场景需要重点关注消息堆积和消费延迟问题：

- 业务系统上下游能力不匹配造成的持续堆积，且无法自行恢复。

- 业务系统对消息的消费实时性要求较高，即使是短暂的堆积造成的消费延迟也无法接受。

#### 产生原因

Consumer使用长轮询Pull模式消费消息时，分为以下两个阶段：

- 消息拉取：Consumer通过长轮询Pull模式批量拉取的方式从服务端获取消息，将拉取到的消息缓存到本地缓冲队列中。对于拉取式消费，在内网环境下会有很高的吞吐量，所以这一阶段一般不会成为消息堆积的瓶颈。

- 消息消费：Consumer将本地缓存的消息提交到消费线程中，使用业务消费逻辑对消息进行处理，处理完毕后获取到一个结果。这是真正的消息消费过程。此时Consumer的消费能力就完全依赖于消息的**消费耗时**和**消费并发度**了。如果由于业务处理逻辑复杂等原因，导致处理单条消息的耗时较长，则整体的消息吞吐量肯定不会高，此时就会导致Consumer本地缓冲队列达到上限，停止从服务端拉取消息。

消息堆积的主要瓶颈在于客户端的消费能力，**而消费能力由消费耗时和消费并发度决定**。注意，消费耗时的优先级要高于消费并发度。即在保证了消费耗时的合理性前提下，再考虑消费并发度问题。

#### 消息耗时

影响消息处理时长的主要因素是代码逻辑。而代码逻辑中可能会影响处理时长代码主要有两种类型：**CPU内部计算型代码**和**外部I/O操作型代码**。

通常情况下代码中如果没有复杂的递归和循环的话，内部计算耗时相对外部I/O操作来说几乎可以忽略。所以外部IO型代码是影响消息处理时长的主要症结所在。

外部IO操作型代码举例：

- 读写外部数据库，例如对远程MySQL的访问。

- 读写外部缓存系统，例如对远程Redis的访问。

- 下游系统调用，例如Dubbo的RPC远程调用，Spring Cloud的对下游系统的Http接口调用。

关于下游系统调用逻辑需要进行提前梳理，掌握每个调用操作预期的耗时，这样做是为了能够判断消费逻辑中IO操作的耗时是否合理。通常消息堆积是由于下游系统出现了**服务异常**或达到了**DBMS容量限制**（数据库连接数上限），导致消费耗时增加。

#### 消息并发度

一般情况下，消费者端的消费并发度由单节点线程数和节点数量共同决定，其值为`单节点线程数 * 节点数量`。不过，通常需要优先调整单节点的线程数，若单机硬件资源达到了上限，则需要通过横向扩展来提高消费并发度。

- 单节点线程数，即单个Consumer所包含的线程数量。（多线程处理消息）

- 节点数量，即Consumer Group所包含的Consumer数量。（可以考虑增加Topic的分区数，并且同时提升消费组的消费者数量，消费者数 = 分区数）

对于普通消息、延时消息及事务消息，并发度计算都是单节点线程数*节点数量。但对于顺序消息则是不同的。顺序消息的消费并发度等于Topic的Queue分区数量。

- 全局顺序消息：**该类型消息的Topic只有一个Queue分区**。其可以保证该Topic的所有消息被顺序消费。为了保证这个全局顺序性，Consumer Group中在同一时刻只能有一个Consumer的一个线程进行消费。所以其并发度为1。

- 分区顺序消息：该类型消息的Topic有多个Queue分区。**其仅可以保证该Topic的每个Queue分区中的消息被顺序消费，不能保证整个Topic中消息的顺序消费**。为了保证这个分区顺序性，每个Queue分区中的消息在Consumer Group中的同一时刻只能有一个Consumer的一个线程进行消费。即，一个消费者只能处理一个Queue。所以其并发度为Topic的分区数量。（指定分区消费）

#### 如何避免

为了避免在业务使用时出现非预期的消息堆积和消费延迟问题，需要在前期设计阶段对整个业务逻辑进行完善的排查和梳理。其中最重要的就是**梳理消息的消费耗时**和**设置消息消费的并发度**。

- 梳理消息的消费耗时：通过压测获取消息的消费耗时，并对耗时较高的操作的代码逻辑进行分析。梳理消息的消费耗时需要关注以下信息：

  - 消息消费逻辑的计算复杂度是否过高，代码是否存在无限循环和递归等缺陷。
  
  - 消息消费逻辑中的I/O操作是否是必须的，能否用本地缓存等方案规避。
  
  - 消费逻辑中的复杂耗时的操作是否可以做异步化处理。如果可以，是否会造成逻辑错乱。
  
- 设置消费并发度

  - 逐步调大单个Consumer节点的线程数，并观测节点的系统指标，得到单个节点最优的消费线程数和消息吞吐量。
  
  - 根据上下游链路的流量峰值计算出需要设置的节点数。（节点数 = 流量峰值 / 单个节点消息吞吐量）

### 消息的清理

消息被消费过后会被清理掉吗？不会的。但是会有过期清理机制。

消息是被顺序存储在commitlog文件的，且消息大小不定长，所以消息的清理是不可能以消息为单位进行清理的，而是以commitlog文件为单位进行清理的。否则会急剧下降清理效率，并实现逻辑复杂。

commitlog文件存在一个过期时间，默认为72小时，即三天。除了用户手动清理外，在以下情况下也会被自动清理，无论文件中的消息是否被消费过：

- 文件过期，且到达清理时间点（默认为凌晨4点）后，自动清理过期文件。

- 文件过期，且磁盘空间占用率已达过期清理警戒线（默认75%）后，无论是否达到清理时间点，都会自动清理过期文件。

- 磁盘占用率达到清理警戒线（默认85%）后，开始按照设定好的规则清理文件，无论是否过期。默认会从最旧的文件开始清理。

- 磁盘占用率达到系统危险警戒线（默认90%）后，Broker将拒绝消息写入。

对于RocketMQ系统来说，删除一个1G大小的文件，是一个压力巨大的IO操作。在删除过程中，系统性能会骤然下降。所以，其默认清理时间点为凌晨4点，访问量最小的时间。也正因如果，我们要保障磁盘空间的空闲率，不要使系统出现在其它时间点删除commitlog文件的情况。

官方建议RocketMQ服务的Linux文件系统采用ext4。因为对于文件删除操作，ext4要比ext3性能更好。

## Rocket应用

### 普通消费消息

- 同步发送消息：同步发送消息是指，Producer发出⼀条消息后，会在收到MQ返回的ACK之后才发下⼀条消息。该方式的消息可靠性最高，但消息发送效率太低。（同步响应ACK）

- 异步发送消息：异步发送消息是指，Producer发出消息后无需等待MQ返回ACK，直接发送下⼀条消息。该方式的消息可靠性可以得到保障，消息发送效率也可以。（异步响应ACK）

- 单向发送消息：单向发送消息是指，Producer仅负责发送消息，不等待、不处理MQ的ACK。该发送方式时MQ也不返回ACK。该方式的消息发送效率最高，但消息可靠性较差。

> 在API体现为：同步send()、异步send(new SendCallback)、单向sendOneway()


### 顺序消费消息

顺序消息指的是，严格按照消息的发送顺序进行消费的消息(FIFO)。

默认情况下生产者会把消息以Round Robin轮询方式发送到不同的Queue分区队列；而消费消息时会从多个Queue上拉取消息，这种情况下的发送和消费是不能保证顺序的。如果将消息仅发送到同一个Queue中，消费时也只从这个Queue上拉取消息，就严格保证了消息的顺序性。

为什么需要顺序消费消息？

假设此时有Topic ORDER_STATUS (订单状态)，其下有4个Queue队列，该Topic中的不同消息用于描述当前订单的不同状态。假设订单有状态：未支付、已支付、发货中、发货成功、发货失败。

根据以上订单状态，生产者从时序上可以生成如下几个消息：`订单T0000001:未支付 --> 订单T0000001:已支付 --> 订单T0000001:发货中 --> 订单T0000001:发货失败`。消费消息的可能顺序如下：

> 正确：订单T0000001:未支付 --> 订单T0000001:已支付 --> 订单T0000001:发货中 --> 订单T0000001:发货失败
>
> 偏差：订单T0000001:已支付 --> 订单T0000001:未支付 --> 订单T0000001:发货失败 --> 订单T0000001:发货中
>
> 偏差：订单T0000001:发货失败 --> 订单T0000001:发货中 --> 订单T0000001:未支付 --> 订单T0000001:已支付
>
> 偏差：订单T0000001:发货失败 --> 订单T0000001:未支付 --> 订单T0000001:发货中  --> 订单T0000001:已支付

基于上述的情况，可以设计如下方案：对于相同订单号的消息，通过一定的策略，将其放置在一个Queue中，然后消费者再采用一定的策略（例如，一个线程独立处理一个queue，保证处理消息的顺序性），能够保证消费的顺序性。

#### 有序性分类

##### 全局有序

当发送和消费参与的Queue只有一个时所保证的有序是整个Topic中消息的顺序，称为全局有序。

在创建Topic时指定Queue的数量。有三种指定方式：

1. 在代码中创建Producer时，可以指定其自动创建的Topic的Queue数量。

2. 在RocketMQ可视化控制台中手动创建Topic时指定Queue数量。

3. 使用mqadmin命令手动创建Topic时指定Queue数量。

##### 分区有序

如果有多个Queue参与，其仅可保证在该Queue分区队列上的消息顺序，则称为分区有序。

如何实现Queue的选择？在定义Producer时我们可以指定消息队列选择器，而这个选择器是我们自己实现了MessageQueueSelector接口定义的。

在定义选择器的选择算法时，一般需要使用选择key。这个选择key可以是消息key也可以是其它数据。但无论谁做选择key，都不能重复，都是唯一的。

一般性的选择算法是，让选择key（或其hash值）与该Topic所包含的Queue的数量取模，其结果即为选择出的Queue的QueueId。

### 延时消费消息

当消息写入到Broker后，在指定的时长后才可被消费处理的消息，称为延时消息。

采用RocketMQ的延时消息可以实现定时任务的功能，而无需使用定时器。典型的应用场景是，电商交易中超时未支付关闭订单的场景，12306平台订票超时未支付取消订票的场景。

> eg：在电商平台中，订单创建时会发送一条延迟消息。这条消息将会在30分钟后投递给后台业务系统（Consumer），后台业务系统收到该消息后会判断对应的订单是否已经完成支付。如果未完成，则取消订单，将商品再次放回到库存；如果完成支付，则忽略。

延时消息的延迟时长不支持随意时长的延迟，而是通过特定的延迟等级来指定的。延时等级定义在RocketMQ服务端的MessageStoreConfig类中的如下变量中：

```java
private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
```

即，若指定的延时等级为3，则表示延迟时长为10s，即延迟等级是从1开始计数的。

当然，如果需要自定义的延时等级，可以通过在broker加载的配置中新增如下配置（例如下面增加了1天这个等级1d）。配置文件在RocketMQ安装目录下的conf目录中。

```conf
messageDelayLevel = 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h 1d
```

#### 延时消息实现原理

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_延时消息实现原理流程.png)

##### 修改消息

> 属于步骤1、2

Producer将消息发送到Broker后，Broker会首先将消息写入到commitlog文件，然后需要将其分发到相应的consumequeue。不过，在分发之前，系统会先判断消息中是否带有延时等级。若没有，则直接正常分发；若有则需要经历一个复杂的过程：

- 修改消息的Topic为SCHEDULE_TOPIC_XXXX
  
- 根据延时等级，在consumequeue目录中SCHEDULE_TOPIC_XXXX主题下创建出相应的queueId目录与consumequeue文件（若不存在则创建）。
  
  - 延迟等级delayLevel与queueId的对应关系为queueId = delayLevel -1。
    
  - 需要注意，在创建queueId目录时，并不是一次性地将所有延迟等级对应的目录全部创建完毕，而是用到哪个延迟等级创建哪个目录。
  
- 修改消息索引单元内容。索引单元中的Message Tag HashCode部分原本存放的是消息的Tag的Hash值。现修改为消息的投递时间。**投递时间是指该消息被重新修改为原Topic后再次被写入到commitlog中的时间**。`投递时间 = 消息存储时间 + 延时等级时间`。消息存储时间指的是消息被发送到Broker时的时间戳。

```text
|-------------------|------|----------------------|
|      8 Byte       |4 Byte|      8 Byte          |
|-------------------|------|----------------------|
| Commit Log Offset | Size | Message Tag HashCode |
|-------------------|------|----------------------|
```

- 将消息索引写入到SCHEDULE_TOPIC_XXXX主题下相应的consumequeue中。

  - SCHEDULE_TOPIC_XXXX目录中各个延时等级Queue中的消息是按照消息投递时间排序的。一个Broker中同一等级的所有延时消息会被写入到consumequeue目录中SCHEDULE_TOPIC_XXXX目录下相同Queue中。即一个Queue中消息投递时间的延迟等级时间是相同的。那么**投递时间就取决于于消息存储时间**了。即按照消息被发送到Broker的时间进行排序的。

##### 投递延时消息

> 属于步骤3

Broker内部有⼀个延迟消息服务类ScheduleMessageService，其会消费SCHEDULE_TOPIC_XXXX中的消息，即按照每条消息的投递时间，将延时消息投递到⽬标Topic中。不过，在投递之前会从commitlog中将原来写入的消息再次读出，并将其原来的延时等级设置为0，即原消息变为了一条不延迟的普通消息。然后再次将消息投递到目标Topic中。

- ScheduleMessageService在Broker启动时，会创建并启动一个定时器Timer，用于执行相应的定时任务。系统会根据延时等级的个数，定义相应数量的TimerTask，每个TimerTask负责一个延迟等级消息的消费与投递。

- 每个TimerTask都会检测相应Queue队列的第一条消息是否到期。若第一条消息未到期，则后面的所有消息更不会到期（消息是按照投递时间排序的）；若第一条消息到期了，则将该消息投递到目标Topic，即消费该消息。

##### 将消息重新写入commitlog

> 属于步骤4

延迟消息服务类ScheduleMessageService将延迟消息再次发送给了commitlog，并再次形成新的消息索引条目，分发到相应Queue。

- 这其实就是一次普通消息发送。只不过这次的消息Producer是延迟消息服务类ScheduleMessageService。

### 事务消息

> 事务消息不支持延时消息
>
> 对于事务消息要做好幂等性检查，因为事务消息可能不止一次被消费（因为存在回滚后再提交的情况）
>
> RocketMQ通过TransactionListener实现的（Producer端）

#### 问题引出

需求场景是：工行用户A向建行用户B转账1万元。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_事务问题引入.png)

1. 工行系统发送一个给B增款1万元的同步消息M给Broker。

2. 消息被Broker成功接收后，向工行系统发送成功ACK。

3. 工行系统收到成功ACK后从用户A中扣款1万元。

4. 建行系统从Broker中获取到消息M。

5. 建行系统消费消息M，即向用户B中增加1万元。

若第3步中的扣款操作失败，但消息已经成功发送到了Broker。对于MQ来说，只要消息写入成功，那么这个消息就可以被消费。此时建行系统中用户B增加了1万元。出现了数据不一致问题。

#### 解决方案

解决思路是，让第1、2、3步具有原子性，要么全部成功，要么全部失败。即消息发送成功后，必须要保证扣款成功。如果扣款失败，则回滚发送成功的消息。而该思路即使用事务消息。这里要使用分布式事务解决方案。

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_分布式事务.png)

1. 事务管理器TM向事务协调器TC发起指令，开启全局事务。

2. 工行系统发一个给B增款1万元的事务消息M给TC。

3.  TC会向Broker**发送半事务消息prepareHalf**，将消息M预提交到Broker。此时的建行系统是看不到Broker中的消息M的。

4. Broker会将预提交执行结果Report给TC。

5. 如果预提交成功，TC会调用工行系统的回调操作，去完成工行用户A的预扣款1万元的操作；如果预提交失败，则TC会向TM上报预提交失败的响应，全局事务结束；

6. 工行系统会向TC发送预扣款执行结果，即本地事务的执行状态。

    ```java
    // 描述本地事务执行状态
    public enum LocalTransactionState {
        COMMIT_MESSAGE,     // 本地事务执行成功
        ROLLBACK_MESSAGE,   // 本地事务执行失败
        UNKNOW; // 不确定，表示需要进行回查以确定本地事务的执行结果
    
        private LocalTransactionState() {
        }
    }
    ```

7. TC收到预扣款执行结果后，会将结果上报给TM。

8. TM会根据上报结果向TC发出不同的确认指令。

    - 若预扣款成功（本地事务状态为COMMIT_MESSAGE），则TM向TC发送Global Commit指令。
    
    - 若预扣款失败（本地事务状态为ROLLBACK_MESSAGE），则TM向TC发送Global Rollback指令

    - 若现未知状态（本地事务状态为UNKNOW），则会触发工行系统的本地事务状态回查操作。回查操作会将回查结果，即COMMIT_MESSAGE或ROLLBACK_MESSAGE Report给TC。TC将结果上报给TM，TM会再向TC发送最终确认指令Global Commit或Global Rollback。
    
9. TC在接收到指令后会向Broker与工行系统发出确认指令。

    - TC接收的若是Global Commit指令，则向Broker与工行系统发送Branch Commit指令。此时Broker中的消息M才可被建行系统看到；此时的工行用户A中的扣款操作才真正被确认。
    
    - TC接收到的若是Global Rollback指令，则向Broker与工行系统发送Branch Rollback指令。此时Broker中的消息M将被撤销；工行用户A中的扣款操作将被回滚。
    
以上方案就是为了确保消息投递与扣款操作能够在一个事务中，要成功都成功，有一个失败，则全部回滚。但并不是一个典型的XA模式。因为XA模式中的分支事务是异步的，而事务消息方案中的消息预提交与预扣款操作间是同步的。
    
#### 事务消息基础

- 分布式事务：对于分布式事务，通俗地说就是，一次操作由若干分支操作组成，这些分支操作分属不同应用，分布在不同服务器上。分布式事务需要保证这些分支操作要么全部成功，要么全部失败。分布式事务与普通事务一样，就是为了保证操作结果的一致性。

- 事务消息：RocketMQ提供了类似X/Open XA的分布式事务功能，通过事务消息能达到分布式事务的最终一致。XA是一种分布式事务解决方案，一种分布式事务处理模式。

- 半事务消息：暂不能投递的消息，发送方已经成功地将消息发送到了Broker，但是Broker未收到最终确认指令，此时该消息被标记成“暂不能投递”状态，即不能被消费者看到。处于该种状态下的消息即半事务消息。

- 本地事务状态：Producer回调操作执行的结果为本地事务状态，其会发送给TC，而TC会再发送给TM。TM会根据TC发送来的本地事务状态来决定全局事务确认指令。

- 消息回查：消息回查，即重新查询本地事务的执行状态。

  ![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_消息回查.png)
  
  - 消息回查不是重新执行回调操作。回调操作是进行预扣款操作，而消息回查则是查看预扣款操作执行的结果。引发消息回查的原因最常见的有两个：回调操作返回UNKNWON、TC没有接收到TM的最终全局事务确认指令。
  
  - 关于消息回查，有三个常见的属性设置。它们都在broker加载的配置文件中设置，例如：
  
    - transactionTimeout=20，指定TM在20秒内应将最终确认状态发送给TC，否则引发消息回查。默认为60秒。
    
    - transactionCheckMax=5，指定最多回查5次，超过后将丢弃消息并记录错误日志。默认15次。
    
    - transactionCheckInterval=10，指定设置的多次消息回查的时间间隔为10秒。默认为60秒。

#### XA协议

XA（Unix Transaction）是一种分布式事务解决方案，一种分布式事务处理模式，是基于XA协议的。

XA模式中有三个重要组件：TC、TM、RM。

TC：Transaction Coordinator，事务协调者。维护全局和分支事务的状态，驱动全局事务提交或回滚。RocketMQ中Broker充当着TC。

TM：Transaction Manager，事务管理器。定义全局事务的范围：开始全局事务、提交或回滚全局事务。它实际是全局事务的发起者。RocketMQ中事务消息的Producer充当着TM。

RM：Resource Manager，资源管理器。管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。RocketMQ中事务消息的Producer及Broker均是RM。

#### XA模式架构

![](https://github.com/Doing-code/guide/blob/main/image/rocketmq_XA架构.png)

XA模式是一个典型的2PC，其执行原理如下：

1. TM向TC发起指令，开启一个全局事务。

2. 根据业务要求，各个RM会逐个向TC注册分支事务，然后TC会逐个向RM发出预执行指令。

3. 各个RM在接收到指令后会在进行本地事务预执行。

4. RM将预执行结果Report给TC。当然，这个结果可能是成功，也可能是失败。

5. TC在接收到各个RM的Report后会将汇总结果上报给TM，根据汇总结果TM会向TC发出确认指令。

    - 若所有结果都是成功响应，则向TC发送Global Commit指令。
    
    - 只要有结果是失败响应，则向TC发送Global Rollback指令。
    
6. TC在接收到指令后再次向RM发送确认指令。

### 批量消息

#### 批量发送消息

在对吞吐率有一定要求的情况下，RocketMQ可以将一些消息聚成一批以后进行发送，可以增加吞吐率，并减少API和网络调用次数。

![](https://rocketmq.apache.org/zh/assets/images/batch-241308ac9ed97b3a1fbf0e5e6417f74d.png)

不过需要注意以下几点：

- 批量发送的消息必须具有相同的Topic。

- 批量发送的消息必须具有相同的刷盘策略。

- 批量发送的消息不能是延时消息与事务消息。

默认情况下，一批发送的消息总大小不能超过4MB字节。如果想超出该值，有两种解决方案：

- 方案一：将批量消息进行拆分，拆分为若干不大于4M的消息集合分多次批量发送。

- 方案二：在Producer端与Broker端修改属性。

  - Producer端需要在发送之前设置Producer的maxMessageSize属性。
  
  - Broker端需要修改其加载的配置文件中的maxMessageSize属性。

生产者通过send()方法发送的Message，并不是直接将Message序列化后发送到网络上的，而是通过这个Message生成了一个字符串发送出去的。这个字符串由四部分构成：Topic、消息Body、消息日志（占20字节），及用于描述消息的一堆属性key-value。这些属性中包含例如生产者地址、生产时间、要发送的QueueId等。最终写入到Broker中消息单元中的数据都是来自于这些属性。

```text
|-------|------|---------|------------|
|       |      | 20 byte |            |
|-------|------|---------|------------|
| Topic | Body |   Log   | Properties |
|-------|------|---------|------------|
```

#### 批量消费消息

Consumer的MessageListenerConcurrently监听接口的consumeMessage()方法的第一个参数为消息列表，但默认情况下每次只能消费一条消息。

若要使其一次可以消费多条消息，则可以通过修改Consumer的consumeMessageBatchMaxSize属性来指定。不过，该值不能超过32。因为默认情况下消费者每次可以拉取的消息最多是32条。若要修改一次拉取的最大值，则可通过修改Consumer的pullBatchSize属性来指定。

pullBatchSize值设置的越大，Consumer每拉取一次需要的时间就会越长，且在网络上传输出现问题的可能性就越高。若在拉取过程中若出现了问题，那么本批次所有消息都需要全部重新拉取。

consumeMessageBatchMaxSize值设置的越大，Consumer的消息并发消费能力越低，且这批被消费的消息具有相同的消费结果。因为consumeMessageBatchMaxSize指定的一批消息只会使用一个线程进行处理，且在处理过程中只要有一个消息处理异常，则这批消息需要全部重新再次消费处理。

### 消息过滤

消息者在进行消息订阅时，除了可以指定要订阅消息的Topic外，还可以对指定Topic中的消息根据指定条件进行过滤，即可以订阅比Topic更加细粒度的消息类型。

对于指定Topic消息的过滤有两种过滤方式：Tag过滤与SQL过滤。

#### Tag过滤

通过consumer的subscribe()方法指定要订阅消息的Tag。如果订阅多个Tag的消息，Tag间使用或运算符(双竖线||)连接。

```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("CID_EXAMPLE");
consumer.subscribe("TOPIC", "TAGA || TAGB || TAGC");
```

#### SQL过滤

```java
// 定义一个pull消费者
// DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("cg");
// 定义一个push消费者
// DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cg");
```

SQL过滤是一种通过特定表达式对事先埋入到消息中的用户属性进行筛选过滤的方式。通过SQL过滤，可以实现对消息的复杂过滤。不过，只有使用PUSH模式的消费者才能使用SQL过滤。

SQL过滤表达式中支持多种常量类型与运算符。

支持的常量类型：

- 数值：比如：123，3.1415

- 字符：必须用单引号包裹起来，比如：'abc'

- 布尔：TRUE 或 FALSE

- NULL：特殊的常量，表示空

支持的运算符有：

- 数值比较：>，>=，<，<=，BETWEEN，=

- 字符比较：=，<>，IN

- 逻辑运算 ：AND，OR，NOT

- NULL判断：IS NULL 或者 IS NOT NULL

默认情况下Broker没有开启消息的SQL过滤功能，需要在Broker加载的配置文件中添加如下属性，以开启该功能：

```conf
enablePropertyFilter = true
```

### 消费重试

#### 顺序消息的消费重试

对于顺序消息，当Consumer消费消息失败后，为了保证消息的顺序性，其会自动不断地进行消息重试，直到消费成功。消费重试默认间隔时间为1000毫秒。重试期间应用会出现消息消费被阻塞的情况。

```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cg");
// 顺序消息消费失败的消费重试时间间隔，单位毫秒，默认为1000，其取值范围为[10,30000]
consumer.setSuspendCurrentQueueTimeMillis(100);
```

由于对顺序消息的重试是无休止的，不间断的，直至消费成功，所以，对于顺序消息的消费，务必要保证应用能够及时监控并处理消费失败的情况，避免消费被永久性阻塞。

但请注意，顺序消息没有发送失败重试机制，但具有消费失败重试机制。

#### 无序消息的消费重试

对于无序消息（普通消息、延时消息、事务消息），当Consumer消费消息失败时，可以通过设置返回状态达到消息重试的效果。**但无序消息的重试只对集群消费方式生效**，广播消费方式不提供失败重试特性。即对于广播消费，消费失败后，失败消息不再重试，继续消费后续消息。

对于无序消息集群消费下的重试消费，每条消息默认最多重试16次，但每次重试的间隔时间是不同的，会逐渐变长。每次重试的间隔时间如下表

| 重试次数 | 与上次重试的间隔时间 | 重试次数 | 与上次重试的间隔时间 |
|---|---|---|---|
| 1 | 10秒 | 9 | 7分钟 |
| 2 | 30秒 | 10 | 8分钟 |
| 3 | 1分钟 | 11 | 9分钟 |
| 4 | 2分钟 | 12 | 10分钟 |
| 5 | 3分钟 | 13 | 20分钟 |
| 6 | 4分钟 | 14 | 30分钟 |
| 7 | 5分钟 | 15 | 1小时 |
| 8 | 6分钟 | 16 | 2小时 |

若一条消息在一直消费失败的前提下，将会在正常消费后的第**4小时46分**后进行第16次重试。若仍然失败，则将消息投递到**死信队列**。

修改消费重试次数：

```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cg");
// 修改消费重试次数
consumer.setMaxReconsumeTimes(10);
```

对于修改过的重试次数，将按照以下策略执行：

- 若修改值小于16，则按照指定间隔进行重试。

- 若修改值大于16，则超过16次的重试时间间隔均为2小时。

对于Consumer Group，若仅修改了一个Consumer的消费重试次数，则会应用到该Group中所有其它Consumer实例。若出现多个Consumer均做了修改的情况，则采用覆盖方式生效。即最后被修改的值会覆盖前面设置的值。

#### 重试队列

当RocketMQ对消息的消费出现异常时，会将发生异常的消息的offset提交到Broker中的重试队列。

对于需要重试消费的消息，并不是Consumer在等待了指定时长后再次去拉取原来的消息进行消费，而是将这些需要重试消费的消息放入到了一个特殊Topic的队列中，而后进行再次消费的。这个特殊的队列就是重试队列。

当出现需要进行重试消费的消息时，Broker会为每个消费组都设置一个Topic名称为`%RETRY%consumerGroup@consumerGroup` 的重试队列。

- 这个重试队列是针对消息才建立的，而不是针对每个Topic设置的（一个Topic的消息可以让多个消费者组进行消费，所以会为这些消费者组各创建一个重试队列）。

- 只有当出现需要进行重试消费的消息时，才会为该消费者组创建重试队列。

消费重试的时间间隔与**延时消费的延时等级**十分相似，除了没有延时等级的前两个时间外，其它的时间都是相同的。

Broker对于重试消息的处理是通过延时消息实现的。先将消息保存到SCHEDULE_TOPIC_XXXX延迟队列中，延迟时间到后，会将消息投递到`%RETRY%consumerGroup@consumerGroup`重试队列中。

#### 消费重试配置方式

集群消费方式下，消息消费失败后若希望消费重试，则需要在消息监听器接口的实现中明确进行如下三种方式之一的配置：

- 方式1：返回`ConsumeConcurrentlyStatus.RECONSUME_LATER`（推荐）

- 方式2：返回Null

- 方式3：抛出异常

```java
consumer.registerMessageListener(new MessageListenerConcurrently(){...});
```

#### 消费不重试配置方式

集群消费方式下，消息消费失败后若不希望消费重试，则在捕获到异常后同样也返回与消费成功后的相同的结果，即`ConsumeConcurrentlyStatus.CONSUME_SUCCESS`，则不进行消费重试。

### 死信队列

当一条消息初次消费失败，消息队列会自动进行消费重试；达到最大重试次数后，若消费依然失败，则表明消费者在正常情况下无法正确地消费该消息，此时，消息队列不会立刻将消息丢弃，而是将其发送到该消费者对应的特殊队列中。

这个队列就是死信队列（Dead-Letter Queue，DLQ），而其中的消息则称为死信消息（Dead-Letter Message，DLM）。（死信队列是用于处理无法被正常消费的消息的）

死信队列具有如下特征：

- 死信队列中的消息不会再被消费者正常消费，即DLQ对于消费者是不可见的。

- 死信存储有效期与正常消息相同，均为 3 天（commitlog文件的过期时间），3 天后会被自动删除。

- 死信队列就是一个特殊的Topic，名称为%DLQ%consumerGroup@consumerGroup ，即每个消费者组都有一个死信队列。

- 如果⼀个消费者组未产生死信消息，则不会为其创建相应的死信队列。

实际上，当⼀条消息进入死信队列，就意味着系统中某些地方出现了问题，从而导致消费者无法正常消费该消息，比如代码中原本就存在Bug。因此，对于死信消息，通常需要开发人员进行特殊处理。最关键的步骤是要排查可疑因素，解决代码中可能存在的Bug，然后再将原来的死信消息再次进行投递消费。

### 发送重试

Producer对发送失败的消息进行重新发送的机制，称为消息发送重试机制，也称为消息重投机制。

消息发送重试有三种策略可以选择：同步发送失败策略、异步发送失败策略、消息刷盘失败策略。

对于消息重投，需要注意以下几点：

- 生产者在发送消息时，若采用同步或异步发送方式，发送失败会重试，但oneway消息发送方式发送失败是没有重试机制的。

- 只有普通消息具有发送重试机制，顺序消息是没有的。

- 消息重投机制可以保证消息尽可能发送成功、不丢失，但可能会造成消息重复。**消息重复在RocketMQ中是无法避免的问题**。

- 消息重复在一般情况下不会发生，当出现消息量大、网络抖动，消息重复就会成为大概率事件。

- producer主动重发、consumer负载变化（发生Rebalance，不会导致消息重复，但可能出现重复消费）也会导致重复消息。

- 消息重复无法避免，但要避免消息的重复消费。

- 避免消息重复消费的解决方案是，为消息添加唯一标识（例如消息key），使消费者对消息进行消费判断来避免重复消费。

#### 同步发送失败策略

对于普通消息，消息发送默认采用round-robin策略来选择所发送到的队列。如果发送失败，默认重试2次。但在重试时是不会选择上次发送失败的Broker，而是选择其它Broker。当然，若只有一个Broker其也只能发送到该Broker，但其会尽量发送到该Broker上的其它Queue。

即Broker失败隔离功能，使Producer尽量选择未发生过发送失败的Broker作为目标Broker。其可以保证其它消息尽量不发送到问题Broker，为了提升消息发送效率，降低消息发送耗时。

```java
// 创建一个producer，参数为Producer Group名称
DefaultMQProducer producer = new DefaultMQProducer("pg");
// 指定nameServer地址
producer.setNamesrvAddr("rocketmqOS:9876");
// 设置同步发送失败时重试发送的次数，默认为2次
producer.setRetryTimesWhenSendFailed(3);
// 设置发送超时时限为5s，默认3s
producer.setSendMsgTimeout(5000);
```

如果超过重试次数，则抛出异常，由Producer去保证消息不丢。当然当生产者出现RemotingException、MQClientException和MQBrokerException时，Producer会自动重投消息。

#### 异步发送失败策略

异步发送失败重试时，异步重试不会选择其他broker，仅在同一个broker上做重试，所以该策略无法保证消息不丢。

```java
DefaultMQProducer producer = new DefaultMQProducer("pg");
producer.setNamesrvAddr("rocketmqOS:9876");
// 指定异步发送失败后不进行重试发送
producer.setRetryTimesWhenSendAsyncFailed(0);
```

#### 消息刷盘失败策略

消息刷盘超时（Master或Slave）或slave不可用（slave在做数据同步时向master返回状态不是SEND_OK）时，默认是不会将消息尝试发送到其他Broker的。不过，对于重要消息可以通过在Broker的配置文件设置retryAnotherBrokerWhenNotStoreOK属性为true来开启。

## 源码篇

## 附录

### 页缓存

PageCache机制，页缓存机制，是OS对文件的缓存机制，用于加速对文件的读写操作。一般来说，程序对文件进行顺序读写的速度几乎接近于内存读写速度，主要原因是由于OS使用PageCache机制对读写访问操作进行性能优化，将一部分的内存用作PageCache。

- 写操作：OS会先将数据写入到PageCache中，随后会以异步方式由pdflush（page dirty flush)内核线程将Cache中的数据刷盘到物理磁盘。

- 读操作：若用户要读取数据，其首先会从PageCache中读取，若没有命中，则OS在从物理磁盘上加载该数据到PageCache的同时，也会顺序对其相邻数据块中的数据进行预读取。