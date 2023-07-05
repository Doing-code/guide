# Kafka
## 入门
### 定义
- Kafka 是一个分布式事件流平台，分布的基于发布/订阅模式的消息队列，主要应用于实时处理领域。

应用场景：
- 缓存/消峰、解耦、异步通信等。

- 点对点模式：消费者主动拉取消息，消费者收到消息后，会 ack 消息队列，消息队列会删除消息。
- 发布/订阅模式：不会删除消息，每个消费者相互独立。

搭建集群。

修改三处配置`vim config/server.properties`：
```text
broker.id=0

log.dirs=/mydata/kafka/data/logs

zookeeper.connect=192.168.44.129:2181,192.168.44.131:2181,192.168.44.132:2181/kafka
```
### 生产者
![](../image/kafka_生产者_发送流程.png)

重试次数默认是 Integer 最大值。
#### 分区
Kafka可以将主题划分为多个分区（Partition），会根据分区规则选择把消息存储到哪个分区中，只要 如果分区规则设置的合理，那么所有的消息将会被均匀的分布到不同的分区中，这样就实现了负载均衡 和水平扩展。另外，多个订阅者可以从一个或者多个分区中同时消费数据，以支撑海量数据处理能力。

分区写入策略：所谓分区写入策略，即是生产者将数据写入到kafka主题后，kafka如何将数据分配到不同分区中的策略。

常见的有三种策略，轮询策略，随机策略，和按键hash取模保存策略。其中轮询策略是默认的分区策略。
##### 自定义分区器
1. 自定义类实现 `Partitioner` 接口。
2. 重写 `Partitioner` 接口方法，重点是 `partition()` 方法。
3. 修改默认分区器为自定义类。

一种方式是
```java
Properties properties = new Properties();
properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
```
#### 提高生产者吞吐量
以下配置根据实际业务场景灵活调整。

* `batch.size`：批量发送，默认16k。（等待达到16k再发送，）
* `linger.ms`：等待时间，默认0ms。（修改为 5~100 ms）。
* `compression.type`：压缩 snappy。
* `buffer.memory`：缓冲区大小，默认 32m（修改为 64m）
#### 数据可靠性
数据完全可靠条件：ACK级别设置为 -1，分区副本大于等于2，ISR应答的最小副本数量大于等于2.
##### ACK 应答
#### 数据去重
幂等性。事务
#### 数据有序
单分区内有序。多分区无序，分区与分区之间无序 。
#### 数据乱序
### Broker
### 消费者
### Eagle 监控
### Kraft 模式
## 外部系统集成
### 集成 Flume
### 集成 Flink
### 集成 SpringBoot
### 集成 Spark
## 调优
### 硬件配置选择
### 生产者
### Broker
### 消费者
## 源码 
### 生产者源码
### 消费者源码
### 服务器源码
## 附录
### Kafka 命令
```txt
# ----------------------kafka-console-producer.sh----------------------------
# 连接 Broker，并指定要往哪个主题写数据
[root@server7 bin]# ./kafka-console-producer.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo

# ----------------------kafka-topics.sh----------------------------
# 查看所有主题
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --list

# 创建一个名为 sanguo 的主题，1 个分区，3 个副本
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --create --partitions 1 --replication-factor 3

# 查看名为 sanguo 主题的详细信息
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --describe
Topic: sanguo	TopicId: EJah9n4zQFSGZq4xg7HW1g	PartitionCount: 1	ReplicationFactor: 3	Configs: segment.bytes=1073741824
	Topic: sanguo	Partition: 0	Leader: 1	Replicas: 1,0,2	Isr: 1,0,2
# Partition：1 个分区，从0开始。broker.id=1 的是 Leader。Replicas：副本分散在三台服务器上

[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --alter --partitions 3

# ----------------------kafka-console-consumer.sh----------------------------
# 连接 Broker，并指定要往哪个主题读数据，`--from-beginning`参数可以读取历史数据
[root@192 bin]# ./kafka-console-consumer.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --from-beginning



# Kafka 服务端启动
bin/kafka-server-start.sh -daemon config/server.properties
```
#### kafka-topics.sh
| 参数                                               | 描述                              |
| -------------------------------------------------- | --------------------------------- |
| --bootstrap-server <String: server to connect to>  | 连接 Kafka Broker 的 ip:prot 地址 |
| --topic <String: topic>                            | 指定 需要操作的 topic 名称        |
| --create                                           | 创建主题                          |
| --delete                                           | 删除主题                          |
| --alter                                            | 修改主题                          |
| --list                                             | 查看所有主题                      |
| --describe                                         | 查看 主题消息描述                 |
| --partitions <Integer: # of partitions>            | 设置分区数                        |
| --replication-factor <Integer: replication factor> | 设置分区副本                      |
| --config <String: name=value>                      | 更新系统默认的配置                |



### 启动脚本

```shell
#!/bin/bash

kafkaServers='192.168.44.129 192.168.44.131 192.168.44.132'
case $1 in 
"start")
	for k in $kafkaServers
	do
			echo ---------- Kafka $k 启动 ----------
			ssh $k "/mydata/kafka/bin/kafka-server-start.sh -daemon /mydata/kafka/config/server.properties"
	done
;;
"stop")
	for k in $kafkaServers
	do
			echo ---------- Kafka $k 停止 ----------
			ssh $k "/mydata/kafka/bin/kafka-server-stop.sh "
	done
;;
esac
```