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