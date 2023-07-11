# Redis

## Redis 常见数据结构

### Redis 数据结构介绍

Redis 是一个 key-value 的数据库，key 一般是 String 类型，不过 value 的类型多种多样。

官方命令帮助文档：https://redis.io/commands/

### Redis 通用命令

通用命令是部分数据类型都可以使用的指令，常见的有：

- `KEYS`：查看符合模板的所以key，（通配符，模糊匹配或者精确匹配）不建议在生产环境设备上使用。
  
  ```shell
  127.0.0.1:6379> help KEYS
  
  KEYS pattern
  summary: Find all keys matching the given pattern
  since: 1.0.0
  group: generic
  ```

- `DEL`：删除一个或若干个指定的key。
  
  ```shell
  127.0.0.1:6379> help DEL
  
  # [] 表示可以传入1个或多个
  DEL key [key ...]
  summary: Delete a key
  since: 1.0.0
  group: generic
  ```

- `EXISTS`：判断一个或若干个key是否存在。1是存在，0是不存在
  
  ```shell
  127.0.0.1:6379> help EXISTS
  
  EXISTS key [key ...]
  summary: Determine if a key exists
  since: 1.0.0
  group: generic
  ```

- `EXPIRE`：给一个key 设置有效期，有效期到期时该key会自动删除。
  
  ```shell
  127.0.0.1:6379> help EXPIRE
  
  EXPIRE key seconds
  summary: Set a key's time to live in seconds
  since: 1.0.0
  group: generic
  ```

- `TTL`：查看一个key的剩余有效期

使用`help @generic`命令可以查看所有通用命令：

```shell
127.0.0.1:6379> help @generic
```

或使用`help [command]`查看一个命令的用法：

```shell
127.0.0.1:6379> help keys

  KEYS pattern
  summary: Find all keys matching the given pattern
  since: 1.0.0
  group: generic
```

### String 类型

#### 概述

String 类型，也就是字符串类型，是 Redis 中最简单的存储类型。

其 value 是字符串，不过根据字符串的格式不同，又可以分为三类：

- string：普通字符串。
- int：整数类型，可以自增、自减。
- float：浮点类型，可以自增、自减。

| key   | value       |
| ----- | ----------- |
| msg   | hello world |
| num   | 20          |
| score | 82.8        |

但不管是哪种格式，底层都是采用字节数组形式存储，只不过是编码方式不同。字符串类型的最大空间不能超过 512 Mb。

#### String 类型的常见命令

| 命令            | 描述                                         |
| ------------- | ------------------------------------------ |
| `SET`         | 添加或修改已存在的一个String类型的键值对                    |
| `GET`         | 根据key获取String类型的value                      |
| `MSET`        | 批量添加多个String类型的键值对                         |
| `MGET`        | 根据多个key获取多个Strng类型的value                   |
| `INCR`        | 让一个整形的key自增1                               |
| `INCRBY`      | 让一个整形的key自增指定的步长。如果 incrby num 2，让 num 自增2 |
| `INCRBYFLOAT` | 让一个浮点类型的数字自增指定的步长                          |
| `SETNX`       | 添加一个String类型的键值对，前提是这个key不存在，否则不执行         |
| `SETEX`       | 添加一个String类型的键值对，并指定有效期                    |

#### key的层级格式

Redis的key允许类有多个词条形成层级结构，词条之间用`:`隔开，格式如下：

```txt
项目名:业务名:类型:id

set sanguo:wu:sunquan dongwu
set sanguo:wei:caocao caowei
set sanguo:shu:liubei shuhan
```

### Hash 类型

#### 概述

Hash类型，也叫散列，其value是一个无序字典，类似于Java中的HashMap结构。

Hash结构可以将对象中的每个字段独立存储，可以针对单个字段做CRUD。

<img title="" src="..\image\redis_hash类型结构.png" alt="" data-align="inline">

#### Hash类型的常见命令

| 命令                     | 描述                                        |
| ---------------------- | ----------------------------------------- |
| `HSET key field value` | 添加或修改已存在的一个hash类型key的field的值              |
| `HGET key field`       | 获取一个hash类型key的field的值                     |
| `HMSET`                | 批量添加多个hash类型key的field的值                   |
| `HMGET`                | 批量获取多个hash类型key的field的值                   |
| `HGETALL`              | 获取一个hash类型的key中的所有field和value             |
| `HKEYS`                | 获取一个hash类型的key中所有的field                   |
| `HVALS`                | 获取一个hash类型的key中所有的value                   |
| `HINCRBY`              | 让一个hash类型的key的字段值自增指定步长                   |
| `HSETNX`               | 添加一个hash类型的key的field值，前提是这个field不存在，否则不执行 |

### List 类型

#### 概述

Redis中的List类型与Java中的LinkedList类似，可以看作是一个双向链表结构，既可以支持正向检索也可以支持反向检索。

特征也与LinkedList类似：

- 有序

- 元素可重复 

- 插入和删除快

- 查询速度一般

#### List类型的常见命令

| 命令                     | 描述                                               |
| ---------------------- | ------------------------------------------------ |
| `LPUSH key element...` | 向列表左侧插入一个或多个元素（L：Left）                           |
| `LPOP key`             | 移除并返回左侧的第一个元素，没有则返回nil                           |
| `RPUSH key element...` | 向列表右侧插入一个或多个元素（R：Right）                          |
| `RPOP key`             | 移除并返回右侧的第一个元素                                    |
| `LRANGE key star end`  | 返回`star end`角标范围内的所有元素（角标从0开始，包头包尾）              |
| `BLPOP`和`BRPOP`        | 与`LPOP`和`RPOP`类似，在没有元素时等待指定时间，而不是直接返回nil（类似阻塞队列） |

### Set 类型

#### 概述

Redis的Set结构与Java中的HashSet类似，可以看作是一个value为null的HashMap。是一个hash表，因此也具备与HashSet类似的特征：

- 无序

- 元素不可重复

- 查找快

- 支持交集、并集、差集等功能

#### Set类型的常见命令

| 命令                     | 描述                                  |
| ---------------------- | ----------------------------------- |
| `SADD key member...`   | 向set中添加一个或多个元素                      |
| `SREM key member...`   | 移除set中一个或多个指定的元素                    |
| `SCARD key`            | 返回set中元素的个数                         |
| `SISMEMBER key member` | 判断一个元素在set中是否存在                     |
| `SMEMBERS`             | 获取set中的所有元素                         |
| `SINTER key1 key2 ...` | 获取key1于key2 ... 的交集                 |
| `SDIFF key1 key2 ...`  | 获取key1于key2 ... 的差集（key1有的，key2没有的） |
| `SUNION key1 key2 ...` | 获取key1于key2 ... 的并集                 |

### SortedSet 类型

#### 概述

Redis的SortedSet时一个可排序的set集合，与Java中的TreeSet有些类似，但底层数据结构差异较大。SortedSet中的每一个元素都带有一个score属性，可以基于score属性对元素排序，底层的实现是一个调表（SkipList）加hash表。SortedSet具备下列特性：

- 可排序

- 元素不重复

- 查询快

常用来实现排行榜这样的功能。

#### Sorted类型的常见命令

| 命令                             | 描述                                       |
| ------------------------------ | ---------------------------------------- |
| `ZADD key score member`        | 添加一个或多个元素到SortedSet，如果存在则更新其 score 值     |
| `ZREM key member`              | 删除SortedSet中指定元素                         |
| `ZSCORE key member`            | 获取SortedSet中指定元素的score值                  |
| `ZRANK key member`             | 获取SortedSet中指定元素的排名                      |
| `ZCARD key`                    | 获取SortedSet中的元素个数                        |
| `ZCOUNT key min max`           | 统计score值在给定范围内的所有元素个数                    |
| `ZINCRBY key increment member` | 让SortedSet中指定元素自增指定步长                    |
| `ZRANGE key min max`           | 按照score排序后，获取指定排名范围内的元素                  |
| `ZRANGEBYSCORE key min max`    | 按照score排序后，获取指定score范围内的元素               |
| `ZINTER numkeys key1 key2 ...` | 获取key1于key2 ... 的交集，（numkeys：为参与计算的集合数量） |
| `ZDIFF numkeys key1 key2 ...`  | 获取key1于key2 ... 的差集（key1有的，key2没有的）      |
| `ZUNION numkeys key1 key2 ...` | 获取key1于key2 ... 的并集                      |

排名默认升序，如果要降序则需要在命令的`Z`后面添加`REV`即可。比如`ZREVRANGE、ZREVRANGEBYSCORE ...` 

## 实战篇

### 短信登录

#### 基于Session实现短信登录流程

![](..\image\redis_实战_基于session实现登录.png)

#### Session共享问题

Session共享问题：多台Tomcat服务器并不共享Session存储空间，当请求切换到不同Tomcat服务时会导致数据丢失的问题。

而session的替代方案应该满足：

- 数据共享

- 内存存储

- key-value结构

#### 基于Redis实现共享Session登录流程

![](..\image\redis_实战_基于redis实现共享session登录流程.png)

### 查询缓存

#### 缓存更新策略

|      | 内存淘汰                                             | 超时剔除                              | 主动更新                   |
| ---- | ------------------------------------------------ | --------------------------------- | ---------------------- |
| 描述   | 不用自己维护，利用Redis的内部淘汰机制，当内存不足时会自动淘汰部分数据。下次查询时更新缓存。 | 给缓存数据添加TTL时间，到期后自动删除缓存。下次查询时更新缓存。 | 编写业务逻辑，在修改数据库的同时，更新缓存。 |
| 一致性  | 差                                                | 一般                                | 好                      |
| 维护成本 | 无                                                | 低                                 | 高                      |

业务场景：

- 低一致性需求：使用内存淘汰机制。

- 高一致性需求：主动更新，并以超时剔除作为兜底方案。
  
  - 读操作：
    
    - 设定缓存超时时间。
  
  - 写操作：
    
    - 确保数据库与缓存操作的原子性。

##### 主动更新策略

![](..\image\redis_实战_主动更新策略.png)

一般会选择第一种方案，可控性较高。

操作缓存和数据库时有三个问题需要考虑：

1. 删缓存还是更新缓存？
   
   1. 更新缓存：每次更新数据库都需要更新缓存，无效写操作较多。（假如修改多查询少）
   
   2. 删除缓存：更新数据库时让缓存失效，等到查询时再更新缓存。
   
   一般会考虑使用删除缓存方案。

2. 如何保证缓存与数据库的操作同时成功或失败？
   
   1. 单体系统：将缓存与数据库操作放在一个事务。
   
   2. 分布式系统：利用TCC（两阶段）等分布式事务方案。
   
   需要保证原子性。

3. 先操作缓存还是先操作数据库？
   
   ![](..\image\redis_实战_操作缓存与数据库先后问题.png)
   
   需要考虑线程安全问题。但右侧方案发生的概率低于左侧。一般情况下会选择右侧方案。

#### 缓存穿透

#### 缓存雪崩

#### 缓存击穿

#### 缓存工具封装

### 优惠券秒杀

### 达人探店

### 好友关注

### 附近商户

### 用户签到

### UV统计

## 附录

### 命令行启动

```shell
[root@server7 ~]# cd /mydata/redis-6.2.12/src/
[root@server7 src]# ./redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> auth 123456
OK
```