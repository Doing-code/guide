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

![](../image/redis_hash类型结构.png)

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

Redis的SortedSet时一个可排序的set集合，与Java中的TreeSet有些类似，但底层数据结构差异较大。SortedSet中的每一个元素都带有一个score属性，可以基于score属性对元素排序，底层的实现是一个跳表（SkipList）加hash表。SortedSet具备下列特性：

- 可排序

- 元素不重复

- 查询快

常用来实现排行榜这样的功能。

#### SortedSet类型的常见命令

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

## 业务场景

### 短信登录

#### 基于Session实现短信登录流程

![](../image/redis_实战_基于session实现登录.png)

#### Session共享问题

Session共享问题：多台Tomcat服务器并不共享Session存储空间，当请求切换到不同Tomcat服务时会导致数据丢失的问题。

而session的替代方案应该满足：

- 数据共享

- 内存存储

- key-value结构

#### 基于Redis实现共享Session登录流程

![](../image/redis_实战_基于redis实现共享session登录流程.png)

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
   
   需要考虑线程安全问题。但右侧方案发生的概率低于左侧。一般情况下会选择右侧方案（先修改数据库，再删除缓存）。

#### 缓存穿透

缓存穿透是指客户端请求的数据在缓存和数据库中都不存在，这样就会导致缓存永远不会生效，这些请求都会到达数据库。

常见的有两种解决方案：

- 缓存空对象
  
  - 优点：实现简单，维护方便。
  
  - 缺点：
    
    - 额外的内存消耗。
    
    - 可能会造成短期的不一致。

- 布隆过滤
  
  - 内存占用较少，没有多余的key。
  
  - 缺点：
    
    - 实现复杂。
    
    - 存在误判可能。

#### 缓存雪崩

缓存雪崩是指在同一时段内大量的缓存key同时失效（过期）或者Redis服务宕机，导致大量请求到达数据库，带来巨大压力。

解决方案：

- 给不同的key的TTL添加随机值。

- 利用Redis的集群（哨兵）提高服务的可用性。

- 给缓存业务添加降级限流策略。

- 给业务添加多级缓存。

#### 缓存击穿

缓存击穿问题也称热点key问题，即一个被高并发访问并且缓存重建业务较复杂的key突然失效了，无数的请求访问会在瞬间给数据库带来巨大的冲击。

常见的解决方案有两种：

- 互斥锁（分布式锁）。

- 逻辑过期（不设置TTL，额外存储过期时间字段，只不过是逻辑上的过期）。

![](../image/redis_实战_缓存击穿解决方案.png)

| 解决方案 | 优点                            | 缺点                           |
| ---- | ----------------------------- | ---------------------------- |
| 互斥锁  | 没有额外的内存消耗 <br/>保证一致性<br/>实现简单 | 线程需要等待，性能受影响<br/>可能有死锁风险     |
| 逻辑过期 | 线程无需等待，性能较好                   | 不保证一致性<br/>有额外的内存消耗<br/>实现复杂 |

### 优惠券秒杀

#### 全局唯一ID

全局唯一ID生成器，是一种在分布式系统下用来生成全局唯一ID的工具，一般需要满足下列特性：

- 唯一性。

- 高可用。

- 递增性。

- 安全性。

- 高性能。

![](../image/redis_实战_全局ID生成器.png)

```java
/**
    简易版全局id生成器
*/
public class IdWorker {
    /**
     * 左移32位
     */
    private static final int BITS = 32;

    /**
     * 初始时间戳，2023.1.1 0.0.0
     */
    private static final long BEGIN_TIMESTAMP = 1672531200L;

    private StringRedisTemplate stringRedisTemplate;

    public IdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * nextId
     * @param prefix key 前缀
     * @return
     */
    public long nextId(String prefix) {
        // 时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment(String.format("icr:%s:%s", prefix, date));

        // 拼接并返回
        return timestamp << BITS | count;
    }
}
```

全局唯一ID生成策略：

- UUID

- Redis自增

- Snowflake 算法

- 数据库自增

> 数据库自增：不是使用AUTO_INCREMENT，而是额外依赖第三张表，专门用于生成id，比如有5张表的id都从这额外的表中获取id。也能够保证全局唯一，但性能上稍逊于Redis自增。

#### 实现优惠券秒杀下单

下单时需要判断两点：

- 秒杀是否开始或结束，如果尚未开始或已结束则无法下单。

- 库存是否充足，不足则无法下单。

![](../image/redis_实战_实现优惠券秒杀下单.png)

#### 超卖问题

![](../image/redis_实战_超卖问题.png)

解决方案：悲观锁、乐观锁。

乐观锁的关键是判断之前查询得到的数据是否被修改了。常见的有两种方式：

- 版本号

![](../image/redis_实战_超卖问题_乐观锁_版本号.png)

- CAS

![](../image/redis_实战_超卖问题_乐观锁_cas.png)

在Java程序中，使用悲观锁（同步锁）的效率较低，线程长时间的争抢阻塞。而使用乐观锁（不加锁，在更新时判断是否有其它线程在修改）效率较高，但成功率较低。

改进：利用数据库的锁（MySQL行锁，悲观锁），`stock > 0`就不会出现超卖现象。但如果并发量很高的话，还是会给数据库造成压力。

#### 一人一单

![](../image/redis_实战_一人一单.png)

实现思路：要保证一人一单，首先要保证数据库记录只有对应的一条。

- 可以在Java程序中加锁（同步锁），锁对象是userId（`userId.toString().intern()`）。如果使用Spring事务，要想事务生效，除了`@Transactional`注解外，如果还封装其为方法时，需要通过事务代理对象来调用其方法（或者手动事务回滚也可以）。锁要包裹事务，事务不能包裹锁。

- 还可以利用数据库保证一人一单，给userId加唯一约束（不止userId一个字段，还要根据其它业务字段加约束），但高并发下，频繁访问数据库，数据库压力巨大。

```java
synchronized(userId.toString().intern()) {
    XxxService proxy = AopContext.currentProxy();
    proxy.method1(...);
}
```

上面的解决方案在单机情况下通过加锁可以解决，但是在集群环境下（部署多台服务器）就不行了。

#### 分布式锁

分布式锁：满足分布式系统或集群模式下多进程可见并且互斥的锁。

满足基本特性：

- 多进程可见。

- 互斥。

- 高可用。

- 高性能。

- 安全性。

分布式锁的核心时实现多进程之间的互斥，而满足这一点的方式有很多，常见的有三种：

|     | MySQL           | Redis          | Zookeeper        |
| --- | --------------- | -------------- | ---------------- |
| 互斥  | 利用MySQL自身的互斥锁机制 | 利用setnx这样的互斥命令 | 利用节点的唯一性和有序性实现互斥 |
| 高可用 | 好               | 好              | 好                |
| 高性能 | 一般              | 好              | 一般               |
| 安全性 | 断开连接，自动释放锁      | 利用锁超时时间，到期释放   | 临时节点，断开连接自动释放    |

##### 实现

定义接口

```java
import java.util.concurrent.TimeUnit;

public interface RedisDistributeLock {
    /**
     * 
     * 加锁 
     * @param key 主键
     * @param timeout 超时时间
     * @param unit     超时时间单位
     * @return boolean  true:加锁成功;false:加锁失败
     */
    boolean tryLock(String key, long timeout, TimeUnit unit);

    /**
     * 
     * 加锁和释放锁的线程必须保证是同一个。 
     * @param key     主键
     */
    void releaseLock(String key);
}
```

定义接口实现

```java
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisDistributeLockImpl implements RedisDistributeLock {

    private StringRedisTemplate stringRedisTemplate;

    public RedisDistributeLockImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        long threadId = Thread.currentThread().getId();
        return stringRedisTemplate.opsForValue().setIfAbsent(key, threadId + "", timeout, unit);
    }

    @Override
    public void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
```

定义的key能够锁同一个用户即可。

但是还是存在线程安全问题，业务还没有执行完锁就过期了，而其它线程尝试获取锁时就会获取到。会导致一开始获取到锁的那个对象，释放错误的锁，释放了其它线程的锁。

而且只存入线程id，多服务部署相同线程id又会冲突。所以需要再额外添加一个标识。

##### 改进1

防止错误释放锁，需再添加一个标识，判断锁是不是当前线程添加的。

![](../image/redis_实战_分布式锁_改进1.png)

```java
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisDistributeLockImpl implements RedisDistributeLock {

    private StringRedisTemplate stringRedisTemplate;
    private sttaic final String KEY_PREFIX = "lock:";
    private sttaic final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    public RedisDistributeLockImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String threadId = ID_PREFIX + Thread.currentThread().getId();

        // 加锁
        return stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + key, threadId, timeout, unit);
    }

    @Override
    public void releaseLock(String key) {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + key);
        // 判断标识是否一致
        if (!threadId.equals(id)) {
            return;
        }

        // 释放锁
        stringRedisTemplate.delete(KEY_PREFIX + key);
    }
}
```

但是还是会出现线程安全问题，错误释放锁（多线程环境下，因为阻塞导致的错误释放锁）。如果在释放锁时，通过了标识判断，由于JVM垃圾回收触发STW导致的阻塞，恰好阻塞时间超过了锁的过期时间，那么锁还是会被错误释放。究其原因，判断锁标识的动作和释放锁动作是两个动作，需要将这两个操作变成原子操作。

![](../image/redis_实战_分布式锁_错误释放锁.png)

##### 改进2

lua脚本保证原子操作。

> Redis使用同一个Lua解释器来执行所有命令，同时，Redis保证以一种原子性的方式来执行脚本：当lua脚本在执行的时候，不会有其他脚本和命令同时执行，这种语义类似于 MULTI/EXEC。从别的客户端的视角来看，一个lua脚本要么不可见，要么已经执行完。
> 
> 然而这也意味着，执行一个较慢的lua脚本是不建议的，由于脚本的开销非常低，构造一个快速执行的脚本并非难事。但是你要注意到，当你正在执行一个比较慢的脚本时，所以其他的客户端都无法执行命令。

```lua
-- 获取锁的线程标识，get key
local id = redis.call('get', KEYS[1]);
-- 判断线程标识与锁的标识是否一致
if (id == ARGV[1]) then
    -- 释放
    return redis.call('del', KEYS[1]);
end
return 0;
```

```java
private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
static {
    UNLOCK_SCRIPT = new DefaultRedisScript<>();
    DefaultRedisScript.setLocation(new ClassPathResource("unlock.lua"));
    UNLOCK_SCRIPT.setResultType(Long.class);
}

public void unlock(String key) {
    // 调用 lua 脚本
    stringRedisTemplate.execute(
        UNLOCK_SCRIPT,
        Collections.singletonList(KEY_PREFIX + key),
        ID_PREFIX + Thread.currentThread().getId()
    );
}
```

经过前两次的改进，不会出现错误释放锁的问题了。

但是会出现线程阻塞导致超时自动释放锁而引起的线程安全问题，这个需要衡量业务的耗时来设置TTL，或者给锁续期。

##### Redisson可重入锁原理

> 可以参考JDK的ReentrantLock实现，如果锁重入，则锁的重入次数+1，释放锁时-1，当锁重入次数为0时则可以释放锁。

Redisson 也是这样实现的，使用的是Hash数据结构。

![](../image/redis_实战_Redisson可重入锁原理.png)

这么些个操作，要想保证原子性，应该是使用了lua脚本。

![](../image/redis_实战_Redisson释放获取锁lua脚本.png)

为了验证猜想，查阅Redisson源码（获取锁）：

```java
// 入口
RLock lock = redissonClient.getLock("");
lock.tryLock();

// org.redisson.RedissonLock#tryLock()
public boolean tryLock() {
    return get(tryLockAsync());

}

public RFuture<Boolean> tryLockAsync() {
    return tryLockAsync(Thread.currentThread().getId());
}

public RFuture<Boolean> tryLockAsync(long threadId) {
    return tryAcquireOnceAsync(-1, -1, null, threadId);
}

/*
waitTime：重试获取锁等待的间隔时间
leaseTime：锁的过期时间
unit：时间单位
threadId：锁的标识
*/
private RFuture<Boolean> tryAcquireOnceAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
    if (leaseTime != -1) {
        return tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
    }
    RFuture<Boolean> ttlRemainingFuture = tryLockInnerAsync(waitTime,
                                                commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout(),
                                                TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
    ttlRemainingFuture.onComplete((ttlRemaining, e) -> {
        if (e != null) {
            return;
        }

        // lock acquired
        if (ttlRemaining) {
            scheduleExpirationRenewal(threadId);
        }
    });
    return ttlRemainingFuture;
}

<T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
    internalLockLeaseTime = unit.toMillis(leaseTime);

    return evalWriteAsync(getName(), LongCodec.INSTANCE, command,
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);",
            Collections.singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
}
```

释放锁源码：

```java
// org.redisson.RedissonLock#unlock
public void unlock() {
    try {
        get(unlockAsync(Thread.currentThread().getId()));
    } catch (RedisException e) {
        if (e.getCause() instanceof IllegalMonitorStateException) {
            throw (IllegalMonitorStateException) e.getCause();
        } else {
            throw e;
        }
    }
}

public RFuture<Void> unlockAsync(long threadId) {
    RPromise<Void> result = new RedissonPromise<Void>();
    RFuture<Boolean> future = unlockInnerAsync(threadId);

    future.onComplete((opStatus, e) -> {
        cancelExpirationRenewal(threadId);

        if (e != null) {
            result.tryFailure(e);
            return;
        }

        if (opStatus == null) {
            IllegalMonitorStateException cause = new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: "
                    + id + " thread-id: " + threadId);
            result.tryFailure(cause);
            return;
        }

        result.trySuccess(null);
    });

    return result;
}

protected RFuture<Boolean> unlockInnerAsync(long threadId) {
    return evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
            "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                    "return nil;" +
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                    "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 0; " +
                    "else " +
                    "redis.call('del', KEYS[1]); " +
                    "redis.call('publish', KEYS[2], ARGV[1]); " +
                    "return 1; " +
                    "end; " +
                    "return nil;",
            Arrays.asList(getName(), getChannelName()), LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId));
}
```

##### Redissson锁重试&WatchDog

Redisson的锁重试的实现是通过Redis的消息订阅和信号量完成的。收到锁释放的消息后再进行重试，不会无休止的重试，导致浪费CPU资源。

> Redisson锁重试源码：org.redisson.RedissonLock#tryLock(long, long, java.util.concurrent.TimeUnit)

Redisson锁的续期是通过WatchDog（看门狗）机制实现的。第一次获取锁就会创建一个定时任务，默认是10秒更新一次锁的有效期。

```java
// org.redisson.RedissonLock#renewExpiration
private void renewExpiration() {
    ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName());
    if (ee == null) {
        return;
    }

    Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
        @Override
        public void run(Timeout timeout) throws Exception {
            ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
            if (ent == null) {
                return;
            }
            Long threadId = ent.getFirstThreadId();
            if (threadId == null) {
                return;
            }

            RFuture<Boolean> future = renewExpirationAsync(threadId);
            future.onComplete((res, e) -> {
                if (e != null) {
                    log.error("Can't update lock " + getName() + " expiration", e);
                    return;
                }

                if (res) {
                    // reschedule itself
                    // 递归调用，可无限续期
                    renewExpiration();
                }
            });
        }
    }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);

    ee.setTimeout(task);
}
```

`internalLockLeaseTime`默认是30秒。

锁释放时，移除定时任务。

```java
void cancelExpirationRenewal(Long threadId) {
    ExpirationEntry task = EXPIRATION_RENEWAL_MAP.get(getEntryName());
    if (task == null) {
        return;
    }

    if (threadId != null) {
        task.removeThreadId(threadId);
    }

    if (threadId == null || task.hasNoThreads()) {
        Timeout timeout = task.getTimeout();
        if (timeout != null) {
            timeout.cancel();
        }
        EXPIRATION_RENEWAL_MAP.remove(getEntryName());
    }
}
```

##### Redisson分布式锁原理流程

![img.png](../image/redis_实战_Redisson分布式锁原理流程.png)

##### 总结

Redisson分布式锁原理：

- 可重入：利用hash结构记录线程id（锁标识）和重入次数。

- 可重试：利用信号量和PubSub（发布订阅）功能实现等待、唤醒，获取锁失败的重试机制。在设置的锁过期时间内尝试重试。

- 超时续约：利用WatchDog机制，每隔一段时间（`internalLockLeaseTime / 3`），重置超时时间。

#### 优化秒杀

##### Redis 优化秒杀

![](../image/redis_实战_优化秒杀.png)

##### 秒杀业务的优化思路是什么？

1. 先利用Redis完成库存余量、一人一单判断，完成抢单业务并响应给用户。

2. 再将下单业务放入阻塞队列（JDK自带的阻塞队列），利用独立线程异步下单。

#### Redis实现消息队列异步秒杀

##### PubSub

PubSub（发布订阅）是Redis2.0引入的消息传递模型。消费者可以订阅若干个channel，生产者向对应的channel发送消息后，所以订阅者都能收到相关消息。

- `subscribe channel [channel]`：订阅一个或多个频道 。

- `publish channel msg`：向一个频道发送消息。

- `psubscribe pattern [pattern]`：订阅与pattern（通配符）格式匹配的频道。

PubSub方式不支持数据持久化，会导致消息丢失。消息堆积有上限，超出上限会数据丢失。

##### Stream

Stream是Redis5.0引入的新的数据类型，可以实现消息队列。

- 创建消息的方式：`xadd`

![](../image/redis_实战_Stream_用法.png)

- 读取消息的方式：`xread`

![](../image/redis_实战_Stream_xread.png)

xread 会出现消息漏读的情况。

- 创建消费者组的方式：`xgroup`

![](../image/redis_实战_Stream_xgroup.png)

- 从消费者组读数据：`xreadgroup`

![](../image/redis_实战_Stream_xreadgroup.png)

`xreadgroup`特点：

- 消息可回溯。

- 同一个消费者组的消费者争抢消息（同一消费者组只会有一个消费者消费），加快消费速度。

- 可以阻塞读取。

- 没有消息漏读的风险。

- 有消息确认机制，保证 消息至少被消费一次。

![](../image/redis_实战_消息队列比较.png)

#### 总结

Redis在秒杀场景下的应用：

- 缓存。

- 分布式锁。

- 超卖问题。

- lua脚本保证原子性。

- Redis消息队列（可选的）。

### 达人探店

#### 点赞

同一个用户只能点赞一次，再次点击则取消点赞。

使用Set数据结构。

```shell
# 判断用户有没有点赞
sismember key userId

# 如果用户没有点赞，则添加到集合
sadd key userId

# 如果用户已点赞，取消点赞
srem key userId
```

#### 点赞排行榜

![](../image/redis_实战_点赞排行榜_数据结构选取.png)

```shell
# 获取SortedSet中指定元素的score值，相当于判断集合中是否存在指定元素，不存在则返回nil
zscore key userId

# 如果用户没有点赞，则添加到集合（表示已点赞）
zadd key score userId

# 如果用户已点赞，取消点赞
zrem key userId

# 排行榜，默认升序，只显示top5
zrange key 0 4
```

> `select * from tb_name where id in (5,1) order by field(id, 5, 1)`
> 
> 使用`order by field(id, 5, 1)`可以强制按照5, 1 的顺序结果显示。
> 
> 因为传入的参数顺序，in 不一定会保证其顺序，默认还是会按照id排序显示。

### 好友关注

#### 关注和取关

关注也是会有排序的，所以选择使用SortedSet数据结构。

```shell
# 获取SortedSet中指定元素的score值，相当于判断集合中是否存在指定元素，不存在则返回nil
zscore key userId

# 如果用户没有关注，则添加到集合（表示已关注）
zadd key score userId

# 如果用户已关注，取消关注
zrem key userId
```

#### 共同关注

共同关注用Set和SortedSet都可以实现，都可以获取交集。

```shell
# key 是当前用户userId，value 是关注的用户userId
# key-value 可以理解为我关注了哪些人。

127.0.0.1:6379> sadd k1 m1 m2
(integer) 2
127.0.0.1:6379> sadd k2 m2 m3
(integer) 2
127.0.0.1:6379> sinter k1 k2
1) "m2"

127.0.0.1:6379> zadd s1 1 m1 2 m2
(integer) 2
127.0.0.1:6379> zadd s2 1 m2 2 m3
(integer) 2
127.0.0.1:6379> ZINTER 2 s1 s2
1) "m2"
```

#### 关注推送

关注推送也叫做Feed流。为用户持续的提供"沉浸式"体验，通过无限下拉刷新获取新的信息。

![](../image/redis_实战_Feed模式.png)

Feed流产品有两种常见的模式：

- Timeline：不做内容筛选，简单的按照内容发布时间排序，常用于好友关注、朋友圈。
  
  - 优点：信息全面，实现相对简单。
  
  - 缺点：信息噪音较多，内容获取效率低。

- 智能排序：利用算法屏蔽掉违规的、用户不感兴趣的内容。推送用户感兴趣的信息来吸引用户。
  
  - 优点：用户粘度高，容易沉迷。
  
  - 如果算法不精准，可能会适得其反。

案例：基于关注的好友来做Feed流，采用Timeline模式。该模式的实现方案有三种：

- 拉模式：读扩散。

![](../image/redis_实战_Feed_push.png)

- 推模式：写扩散

![](../image/redis_实战_Feed_pull.png)

- 推拉模式

![](../image/redis_实战_Feed_push_pull.png)

选择使用推模式实现关注推送功能。

- 在有新消息的时候，将新的推送发送到粉丝的收件箱。

- 收件箱满足可以根据时间戳排序，可以使用Redis的SortedSet数据结构。

- 查询收件箱数据时，可以实现分页查询。

```shell
# 按照socre降序，redis实现滚动分页查询
# key
# max 最大score（第一次分页给一个最大值，非第一次分页则给上一次查询的最小的score）
# min 最小score（给0即可）
# withscores 结果显示score
# limit 分页查询
# offset 偏移量 （一般从最后一条记录的下一条开始读取）
#    第一次也分偏移量为0
#    之后偏移量offset取决于上一次查询中末尾最小的score相同个数。会存在score一样的情况。
#      如果只有一个，offset=1，如果有三个score相同，则 offset=3
# count 查询多少条记录
zrevrangebyscore key max min withscores limit offset count
```

### 附近商户

#### 概述

GEO数据结构：GEO（Geolocation）地理坐标。Redis在3.2版本中加入了对GEO的支持，允许存储地理坐标信息，通过经纬度检索数据。底层是基于SortedSet实现的。常见命令有：

| 命令               | 描述                                                                  |
| ---------------- | ------------------------------------------------------------------- |
| `GEOADD`         | 添加一个地理空间信息。包含：经度（longitude）、纬度（latitude）、值（member）                  |
| `GEODIST`        | 计算指定的两个点之间的距离并返回                                                    |
| `GEOHASH`        | 将指定的member的坐标转为hash字符串形式并返回                                         |
| `GEOPOS`         | 返回指定member的坐标                                                       |
| `GEORADIUS`      | 6.2以后已废弃。指定圆心、半径，找到该圆内包含的所有member，并按照与圆心之间的距离排序后返回                  |
| `GEOSEARCH`      | 6.2新功能。在指定范围内搜索member，并按照与指定点之间的距离排序后返回。范围可以是圆形或矩形                  |
| `GEOSEARCHSTORE` | 6.2新功能。与`GEOSEARCH`功能一致，唯一区别就是可以把结果存储到一个指定的key（key的类型一般是 SortedSet） |

演示：

```shell
# 添加 三个地理坐标
127.0.0.1:6379> GEOADD g1 116.378248 39.865275 bjn 116.42803 39.903738 bjz 116.322287 39.893729 bjx
(integer) 3
# 计算 北京南和北京西两点之间的距离，默认单位是米
127.0.0.1:6379> GEODIST g1 bjn bjx
"5729.9533"
127.0.0.1:6379> GEODIST g1 bjn bjx km
"5.7300"

127.0.0.1:6379> help GEOSEARCH

GEOSEARCH key [FROMMEMBER member] [FROMLONLAT longitude latitude] [BYRADIUS radius m|km|ft|mi] [BYBOX width height m|km|ft|mi] [ASC|DESC] [COUNT count [ANY]] [WITHCOORD] [WITHDIST] [WITHHASH]
summary: Query a sorted set representing a geospatial index to fetch members inside an area of a box or a circle.
since: 6.2
group: geo

# 计算 g1 距离天安门 10 km范围内的所有火车站，并显示距离
127.0.0.1:6379> GEOSEARCH g1 FROMLONLAT 116.397904 39.909005 BYRADIUS 10 km withdist
1) 1) "bjz"
   2) "2.6361"
2) 1) "bjn"
   2) "5.1452"
3) 1) "bjx"
   2) "6.6723"

127.0.0.1:6379> GEOPOS g1 bjn
1) 1) "116.3782462477684021"
   2) "39.86527441178797204"

127.0.0.1:6379> GEOHASH g1 bjn
1) "wx4fb320190"
```

GEO没有提供分页功能，所以每次需要查询全部数据，然后再进行逻辑分页，进行截取即可。

### 用户签到

用户签到功能可以用bit位0或1表示，一个月最多有31天，只需要31个bit位，4个字节，内存占用少。

#### 概述

BitMap用法：

Redis中是利用string类型的数据结构实现的BitMap，因此最大上限也是512M，转换为bit则是2^32个bit位。BitMap的操作命令有：

| 命令            | 描述                                                 |
| ------------- | -------------------------------------------------- |
| `SETBIT`      | 向指定位置（offset）存入一个0或1                               |
| `GETBIT`      | 获取指定位置（offset）的bit值                                |
| `BITCOUNT`    | 统计BitMap中值为1的bit位的数量                               |
| `BITFIELD`    | 操作（查询、修改、自增）BitMap中bit数组中的指定位置（offset）的值，并以十进制形式返回 |
| `BITFIELD_RO` | 获取BitMap中的bit数组，并以十进制形式返回                          |
| `BITOP`       | 将多个BitMap的结果做位运算（与（&）、或（\|）、异或（^））                 |
| `BITPOS`      | 查找bit数组中指定范围内第一个0或1出现的位置                           |

```shell
127.0.0.1:6379> help SETBIT

  SETBIT key offset value
  summary: Sets or clears the bit at offset in the string value stored at key
  since: 2.2.0
  group: string

# 11001111
127.0.0.1:6379> SETBIT bm1 0 1
(integer) 0
127.0.0.1:6379> SETBIT bm1 1 1
(integer) 0
127.0.0.1:6379> SETBIT bm1 4 1
(integer) 0
127.0.0.1:6379> SETBIT bm1 5 1
(integer) 0
127.0.0.1:6379> SETBIT bm1 6 1
(integer) 0
127.0.0.1:6379> SETBIT bm1 7 1
(integer) 0

127.0.0.1:6379> GETBIT bm1 2
(integer) 0
127.0.0.1:6379> GETBIT bm1 4
(integer) 1
127.0.0.1:6379> BITCOUNT bm1
(integer) 6
127.0.0.1:6379> help BITFIELD

  BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL]
  summary: Perform arbitrary bitfield integer operations on strings
  since: 3.2.0
  group: string

# u：无符号；i:有符号；
# u2表示获取2个bit位，u3表示获取3个bit位；0表示offset从第0个bit位开始获取
# 返回十进制
127.0.0.1:6379> BITFIELD bm1 get u2 0
1) (integer) 3
127.0.0.1:6379> BITFIELD bm1 get u3 0
1) (integer) 6
127.0.0.1:6379> BITFIELD bm1 get u4 0
1) (integer) 12

127.0.0.1:6379> BITPOS bm1 0
(integer) 2
127.0.0.1:6379> BITPOS bm1 1
(integer) 0
```

### UV统计

#### 概述

- UV：Unique Visitor，独立访客量，是指通过互联网访问，浏览这个网页的自然人。一天内同一个用户多次访问该网站，只记录一次。

- PV：Page View，页面访问量或点击量，用于每访问一次网站的一个页面，记录一次PV，多次打开页面则记录多次PV。常用来衡量网站的流量。

HyperLogLog用法：

HyperLogLog（HLL）是从LogLog算法派生的概率算法，用于确定非常大的集合的基数，而不需要存储所有的值。Redis中的HLL是基于string结构实现的，单个HLL的内存永远小于16kb。但其测量结果是概率性的，有小于0.81%的误差。会过滤重复元素。

```shell
PFADD key element [element ...]
summary: Adds the specified elements to the specified HyperLogLog.
since: 2.8.9
group: hyperloglog

PFCOUNT key [key ...]
summary: Return the approximated cardinality of the set(s) observed by the HyperLogLog at key(s).
since: 2.8.9
group: hyperloglog

PFMERGE destkey sourcekey [sourcekey ...]
summary: Merge N different HyperLogLogs into a single one.
since: 2.8.9
group: hyperloglog
```

## 分布式缓存

### Redis持久化

#### RDB持久化

RDB（Redis Database Backup file）Redis数据备份文件。也被叫做Redis数据快照。简单来说就是把内存中的所有数据都记录到磁盘中。当Redis实例故障重启后，从磁盘读取快照文件，恢复数据。

快照文件被称为RDB文件，默认是保存在当前运行目录。

```shell
[root@server7 src]# ./redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> auth 123456
OK
127.0.0.1:6379> save   # 由Redis主进程来执行RDB，会阻塞所有命令
OK
127.0.0.1:6379> bgsave # 开启子进程执行RDB，避免主进程收到影响
Background saving started
```

Redis停机时会执行一次RDB，但是Redis宕机就没办法触发RDB。其实在Redis内部由触发RDB的机制，可以在redis.conf文件中进行配置：

```conf
save 5 1
# 900秒内，如果至少有1个key被修改，则执行bgsave，如果是save "" 则表示禁用RDB
# save 900 1
# save 300 10
# save 60 10000
```

RDB的其它配置：

```conf
# 是否压缩，建议不开启，压缩也会消耗CPU
rdbcompression no

# RDB文件名称
dbfilename dump.rbd

# 文件保存的路径目录, 默认 ./ 运行的当前
dir /mydata/redis-6.2.12/rdb_dump
```

设置RDB多久触发一次合适呢？一般默认即可，30秒、60秒。

##### RDB的fork原理

bgsave开始时会fork主进程得到子进程（fork过程主进程是阻塞的），子进程共享主进程的内存数据。完成fork后，读取内存数并写入RDB文件（异步执行）。

![](../image/redis_持久化_RDB的fork原理.png)

fork采用的是copy-on-write技术：

- 当主进程执行读操作时，访问共享内存；

- 当主进程执行写操作时，则会拷贝一份数据，执行写操作，之后对该数据的读，也会映射到拷贝的这个副本上。

极端情况：当子进程执行RDB较慢时，主进程不断接收写请求，那么都会对共享内存中的数据进行拷贝，会导致占用双倍的内存，最终内存溢出。

页表是虚拟内存。

RDB方式bgsave的基本流程？

- fork主进程得到一个子进程（主进程阻塞），共享内存空间。

- 子进程读取内存数据并写入新的RDB文件（异步执行）。

- 用新的RDB文件替换旧的RDB文件。

RDB会在什么时候执行？

- 默认是服务停止时触发。

- 可以设置为n秒内至少执行n次修改则触发RDB。

RDB的缺点？

- RDB执行间隔时间长，两次RDB之间写入数据有丢失的风险。

- for子进程、压缩、写出RDB文件都比较耗时。

#### AOF持久化

AOF：Append Only File（追加文件）。Redis处理的每一个写命令都会记录在AOF文件中，可以看作是命令日志文件。（类似于MySQL的redolog）

AOF默认是关闭的，需要修改redis.conf配置文件来开启AOF：

```conf
# 是否开启AOF功能，默认no
appendonly yes

# AOF文件名称
appendfilename "appendonly.aof"

# 文件保存的路径目录, 默认 ./ 运行的当前
dir /mydata/redis-6.2.12/rdb_dump
```

AOF命令记录的频率也可以通过redis.conf来配置：

```conf
# 每执行一次写命令，立即记录到AOF文件
appendsync always

# 默认的，写命令执行完先放入AOF缓冲区，然后每隔1秒将缓冲区数据写到AOF文件
appendsync everysec

# 写命令执行完先放入AOF缓冲区，由操作系统决定何时将缓冲区内存写回磁盘
appendsync no
```

因为是记录命令，AOF文件会比RDB文件大得多。而且AOF会记录对同一个key的多次写操作，但只有最后一次写操作才有意义。可以通过执行`bgrewitreaof`命令，可以让AOF文件执行重写功能，用最少的命令达到相同效果。

```shell
127.0.0.1:6379> BGREWRITEAOF # BGREWRITEAOF 命令是后台执行的
Background append only file rewriting started

# BGREWRITEAOF 执行后，aof文件就是经过压缩和编码后的文件
root@server7 redis-6.2.12]# vi rdb_dump/appendonly.aof
REDIS0009ú      redis-ver^F6.2.12ú
redis-bitsÀ@ú^EctimeÂ£ü±dú^Hused-memÂp¨^M^@ú^Laof-preambleÀ^Aþ^@û^A^@^@^CnumÀ{ÿN¢<92>B Ü"ô
```

Redis会在触发阈值时自动去重写AOF文件。阈值也可以在redis.conf中配置：

```conf
# 默认的，AOF文件比上次文件增长超过多少百分比则触发重写，默认100%，增长一倍
auto-aof-rewrite-percentage 100

# 默认的，AOF体积占用达到阈值才触发重写
auto-aof-rewrite-min-size 64mb
```

#### RDB&AOF对比

RDB和AOF各有优缺点，如果对数据安全性要求较高，在实际开发中往往会结合两者来使用。

![](../image/redis_持久化_rdb_aof.png)

### Redis主从

#### 搭建主从架构

单节点Redis的并发能力是有上限的，要想进一步提高Redis的并发能力，就需要搭建主从集群，实现读写分离。

![](../image/redis_主从.png)

##### 准备实例和配置

设备有限，所以在一台虚拟机开启3个实例模拟主从集群。

要想在一台虚拟机开启3个实例，必须只能被三份不同的配置文件和目录，配置文件所在的目录即工作目录。

1. 创建目录

```shell
cd /tmp
mkdir 7001 7002 7003

[root@server7 tmp]# ll
总用量 0
drwxr-xr-x. 2 root root  6 7月  15 10:29 7001
drwxr-xr-x. 2 root root  6 7月  15 10:29 7002
drwxr-xr-x. 2 root root  6 7月  15 10:29 7003
```

2. 拷贝配置文件到每个实例目录

```shell
# 方式1：逐个拷贝
cp redis.conf 7001
cp redis.conf 7002
cp redis.conf 7003
# 方式2：管道组合命令，一键拷贝
echo 7001 7002 7003 | xargs -t -n 1 cp redis.conf
```

3. 修改每个实例的端口、工作目录（不是必须的，如果是多台服务器则不需要设置）

```shell
# s 表示替换 /g 表示全局替换
sed -i -e 's/6379/7001/g' -e 's/dir .\//dir \/tmp\/7001\//g' 7001/redis.conf
sed -i -e 's/6379/7002/g' -e 's/dir .\//dir \/tmp\/7002\//g' 7002/redis.conf
sed -i -e 's/6379/7003/g' -e 's/dir .\//dir \/tmp\/7003\//g' 7003/redis.conf
```

4. 修改每个实例的声明IP（不是必须的，如果是多台服务器则不需要设置）

```shell
# 逐一执行
sed -i 'la replica-announce-ip 192.168.44.149' 7001/redis.conf
sed -i 'la replica-announce-ip 192.168.44.149' 7002/redis.conf
sed -i 'la replica-announce-ip 192.168.44.149' 7003/redis.conf

# 批量执行
printf '%s\n' 7001 7002 7003 | xargs -I{} -t sed -i 'la replica-announce-ip 192.168.44.149' {}/redis.conf
```

##### 启动

```shell
redis-server 7001/redis.conf
redis-server 7002/redis.conf
redis-server 7003/redis.conf
```

##### 开启主从关系

要配置主从可以使用`replicaof`或者`slaveof`（5.0以前）命令。

有临时和永久两种模式：

- 修改配置文件（永久生效）
  
  - 在从节点redis.conf中添加一行配置`slaveof <masterip> <masterport>`

- 使用redis.cli客户端连接到redis服务后，从节点执行`slaveof`命令，重启后失效：
  
  ```shell
  # 当前节点要成为 masterip:masterport 的 slave
  slaveof <masterip> <masterport>
  ```

```shell
# 将 7002 7003 设置为 7001 的 从节点，7001 是主节点
[root@server7 src]# redis-cli -p 7002
127.0.0.1:7002> SLAVEOF 192.168.44.149 7001
OK
127.0.0.1:7003>
[root@server7 src]# redis-cli -p 7003
127.0.0.1:7003> SLAVEOF 192.168.44.149 7001
OK

# 副本信息，7001 挂载两个从节点 7002 7003 状态是在线的
[root@server7 src]# redis-cli -p 7001
127.0.0.1:7001> INFO replication
# Replication
role:master
connected_slaves:2
slave0:ip=192.168.44.149,port=7002,state=online,offset=364,lag=1
slave1:ip=192.168.44.149,port=7003,state=online,offset=364,lag=1
master_failover_state:no-failover
master_replid:5d3bd5761e059a8da4fab71d297c0bde303f1740
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:364
second_repl_offset:-1
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:1
repl_backlog_histlen:364
```

测试：

```shell
# 主节点写
127.0.0.1:7001> set k1 v1
OK
127.0.0.1:7001> 

# 从节点从主节点同步数据，也能获取到 k1
[root@server7 src]# redis-cli -p 7002
127.0.0.1:7002> get k1
"v1"
127.0.0.1:7002> 
[root@server7 src]# redis-cli -p 7003
127.0.0.1:7003> get k1
"v1"

# 从节点不能执行写操作，从节点是只读的副本
127.0.0.1:7003> set k2 v2
(error) READONLY You can't write against a read only replica.
```

#### 主从数据同步原理

##### 全量同步

主从第一次同步是全量同步

![](../image/redis_主从_数据同步原理.png)

> repl_baklog缓冲区，本质是一个数组

master如何判断salve是不是第一次同步数据？

- Replication Id：简称replid，是数据集的标记，id一致说明是同一数据集。每一个master都有唯一的 replid，slave会继承master节点replid。

- offset：偏移量，offset会随着记录在repl_baklog中的数据增多而逐渐增大。slave完成同步时也会记录当前同步的offset。如果slave的offset小于master的offset，说明slave数据落后于master，需要更新。

master通过slave记录的replid判断是否一致从而同步数据。replid一致则只需增量更新，replid不一致则需要全量更新。

因此slave做数据同步时，必须向master声明自己的replication id 和 offset，master才可以判断需要同步哪些数据。

第一次配置主从同步的日志文件：

`7002 从节点`

```shell
20253:M 15 Jul 2023 11:09:42.109 # WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn is set to the lower value of 128.
20253:M 15 Jul 2023 11:09:42.109 # Server initialized
20253:M 15 Jul 2023 11:09:42.109 # WARNING Memory overcommit must be enabled! Without it, a background save or replication may fail under low memory condition. Being disabled, it can can also cause failures without low memory condition, see https://github.com/jemalloc/jemalloc/issues/1328. To fix this issue add 'vm.overcommit_memory = 1' to /etc/sysctl.conf and then reboot or run the command 'sysctl vm.overcommit_memory=1' for this to take effect.
20253:M 15 Jul 2023 11:09:42.110 * Ready to accept connections
20253:S 15 Jul 2023 11:09:56.212 * Before turning into a replica, using my own master parameters to synthesize a cached master: I may be able to synchronize with the new master with just a partial transfer.

# 1.0、执行slaveof/replicationof 命令，建立连接
20253:S 15 Jul 2023 11:09:56.212 * Connecting to MASTER 192.168.44.149:7001
20253:S 15 Jul 2023 11:09:56.212 * MASTER <-> REPLICA sync started
20253:S 15 Jul 2023 11:09:56.212 * REPLICAOF 192.168.44.149:7001 enabled (user request from 'id=3 addr=127.0.0.1:55054 laddr=127.0.0.1:7002 fd=7 name= age=5 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=48 qbuf-free=40906 argv-mem=25 obl=0 oll=0 omem=0 tot-mem=61489 events=r cmd=slaveof user=default redir=-1')
20253:S 15 Jul 2023 11:09:56.212 * Non blocking connect for SYNC fired the event.
20253:S 15 Jul 2023 11:09:56.213 * Master replied to PING, replication can continue...

# 1.1、psync replid offset，尝试做一次局部同步 
20253:S 15 Jul 2023 11:09:56.213 * Trying a partial resynchronization (request e235eaae1b73bfda55612799917518acde8580c7:1).

# 1.4、保存主节点的版本信息（replid和offset）
20253:S 15 Jul 2023 11:09:56.215 * Full resync from master: 5d3bd5761e059a8da4fab71d297c0bde303f1740:0
20253:S 15 Jul 2023 11:09:56.215 * Discarding previously cached master state.

# 接收数据
20253:S 15 Jul 2023 11:09:56.223 * MASTER <-> REPLICA sync: receiving 176 bytes from master to disk

# 2.3、清空本地数据，加载RDB文件
20253:S 15 Jul 2023 11:09:56.224 * MASTER <-> REPLICA sync: Flushing old data
20253:S 15 Jul 2023 11:09:56.224 * MASTER <-> REPLICA sync: Loading DB in memory
20253:S 15 Jul 2023 11:09:56.225 * Loading RDB produced by version 6.2.12
20253:S 15 Jul 2023 11:09:56.225 * RDB age 0 seconds
20253:S 15 Jul 2023 11:09:56.225 * RDB memory usage when created 1.83 Mb
20253:S 15 Jul 2023 11:09:56.225 # Done loading RDB, keys loaded: 0, keys expired: 0.
20253:S 15 Jul 2023 11:09:56.225 * MASTER <-> REPLICA sync: Finished with success
```

`7001 主节点`

```shell
20248:M 15 Jul 2023 11:09:29.227 # WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn is set to the lower value of 128.
20248:M 15 Jul 2023 11:09:29.227 # Server initialized
20248:M 15 Jul 2023 11:09:29.228 # WARNING Memory overcommit must be enabled! Without it, a background save or replication may fail under low memory condition. Being disabled, it can can also cause failures without low memory condition, see https://github.com/jemalloc/jemalloc/issues/1328. To fix this issue add 'vm.overcommit_memory = 1' to /etc/sysctl.conf and then reboot or run the command 'sysctl vm.overcommit_memory=1' for this to take effect.
20248:M 15 Jul 2023 11:09:29.228 * Ready to accept connections

# 1.1、接收到同步请求
20248:M 15 Jul 2023 11:09:56.213 * Replica 192.168.44.149:7002 asks for synchronization

# 1.2 不接受局部同步，replid 不一致，执行全量同步
20248:M 15 Jul 2023 11:09:56.213 * Partial resynchronization not accepted: Replication ID mismatch (Replica asked for 'e235eaae1b73bfda55612799917518acde8580c7', my replication IDs are 'fa0826cebf59df7cad31bc236d5e5835f9c09a9c' and '0000000000000000000000000000000000000000')

# 1.3、第一次同步，返回主节点的 replid和offset
20248:M 15 Jul 2023 11:09:56.213 * Replication backlog created, my new replication IDs are '5d3bd5761e059a8da4fab71d297c0bde303f1740' and '0000000000000000000000000000000000000000'

# 2.1、执行 BGSAVE，生成 RDB
20248:M 15 Jul 2023 11:09:56.213 * Starting BGSAVE for SYNC with target: disk
20248:M 15 Jul 2023 11:09:56.214 * Background saving started by pid 20264
20264:C 15 Jul 2023 11:09:56.218 * DB saved on disk

# 2.2、发送RDB文件
20264:C 15 Jul 2023 11:09:56.219 * RDB: 6 MB of memory used by copy-on-write
20248:M 15 Jul 2023 11:09:56.223 * Background saving terminated with success
20248:M 15 Jul 2023 11:09:56.223 * Synchronization with replica 192.168.44.149:7002 succeeded
```

##### 增量同步

主从第一次同步是全量同步，但如个slave重启后同步，则执行增量同步。

![](../image/redis_主从_增量同步.png)

但是repl_baklog大小也是有上限的（本质是一个数组），写满后会覆盖最早的数据。如果slave断开世间过久，导致之前尚未备份的数据被覆盖，则无法基于repl_baklog做增量同步，只能再次全量同步。

这种清空没有办法去解决的，只能是尽可能的减小发生的概率。

Redis主从集群的优化（优化全量同步的性能，减少全量同步的次数）：

- 在master中配置`repl-diskless-sync yes`启动无磁盘复制，避免全量同步时先写入磁盘，减少一次磁盘复制。适用于IO较慢，网络带宽很快的Redis服务器配置。（避免全量同步时的磁盘IO）

- 在master中配置`maxmemory <bytes>`内存大小，单位字节。Redis单节点上的内存占用不要太大。（减少RDB（Redis Database backup file Redis数据备份文件，也叫快照）导致的过多磁盘IO）。如果不设置内存大小或者设置内存大小为0，在64位操作系统下不限制内存大小，在32位操作系统下最多使用3GB内存。Redis一般推荐设置内存为最大物理内存的四分之三。

- 在master中配置`repl-backlog-size <size>`修改默认大小，默认大小是1M。适当提高repl_baklog的大小，发现slave宕机时应该尽快实现故障恢复，尽可能避免全量同步。

```txt
repl_backlog_buffer(repl-backlog-size) = second * write_size_per_second
second：从服务器断开重连主服务器所需的平均时间；
write_size_per_second：master 平均每秒产生的命令数据量大小（写命令和数据大小总和）；
例如，如果主服务器平均每秒产生 1 MB 的写数据，而从服务器断线之后平均要 5 秒才能重新连接上主服务器，那么复制积压缓冲区的大小就不能低于 5 MB。
```

- 限制一个master上的slave节点数量，如果实在是太多slave，则可以采用主-从-从链式结构，减少master压力。

![](../image/redis_主从_主-从-从链式结构.png)

#### 总结

##### 简述全量同步的流程？

- slave节点尝试请求增量同步。

- master节点判断replid，发现不一致，拒绝增量同步。

- master将完整内存数据生成RDB，发送RDB到slave。

- slave清空本地数据，加载master的RDB。

- master将RDB期间的命令记录在repl_baklog，并持续将log中的命令发送给slave。

- slave执行接收到的命令，保持与master之间的同步。

##### 简述全量同步和增量同步的区别？

- 全量同步：master将完整内存数据生成RDB，发送RDB到slave。后续命令则记录在repl_baklog，批量发送给slave。

- 增量同步：slave提交自己的offset到master，master获取repl_baklog中从offset之后的命令给slave。

##### 何时执行全量同步？

- slave节点第一次连接master节点时。

- slave节点断开时间过久，导致repl_baklog中的offset已经被覆盖时。

##### 何时执行增量同步？

- slave节点断开又恢复，并且在repl_baklog中能够找到offset时。

### Redis哨兵

### Redis分片集群

## 附录

### 命令行启动

```shell
[root@server7 ~]# cd /mydata/redis-6.2.12/src/
[root@server7 src]# ./redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> auth 123456
OK
```