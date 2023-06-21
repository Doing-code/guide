# Java Guide


- [x] [Dubbo](JavaGuide/Dubbo.md)
- [x] [JavaGuide](JavaGuide/JavaGuide.md)
- [ ] [JUC](JavaGuide/JUC.md)
- [ ] [JVM](JavaGuide/JVM.md)
- [ ] [Kafka](JavaGuide/Kafka.md)
- [ ] [Linux](JavaGuide/Linux.md)
- [x] [MyBatis](https://github.com/Doing-code/MyBatisFramework)
- [x] [Spring](https://github.com/Doing-code/SpringFramework)
- [x] [MySQL](JavaGuide/MySQL.md)

[comment]: <> (- [ ] [Netty]&#40;JavaGuide/Netty.md&#41;)
- [ ] [Redis](JavaGuide/Redis.md)

[comment]: <> (- [ ] [RocketMQ]&#40;JavaGuide/RocketMQ.md&#41;)
- [ ] [SpringBoot](JavaGuide/SpringBoot.md)
- [ ] [Zookeeper](JavaGuide/Zookeeper.md)


| 文档     | 视频     | 时长     |
| -------- | -------- | -------- |
| Dubbo | `https://www.bilibili.com/video/BV1ns411c7jV/` | 4h17' |
| HashMap | `https://www.bilibili.com/video/BV1nJ411J7AA/` | 3h54' |
| JavaGuide |  |  |
| JUC | `1：https://www.bilibili.com/video/BV16J411h7Rd/` <br/> `2：https://www.bilibili.com/video/BV1ar4y1x727/` | 24h18' <br/> 32h36' |
| JVM | `https://www.bilibili.com/video/BV1yE411Z7AP/` | 17h35' |
| Kafka | `https://www.bilibili.com/video/BV1vr4y1677k/` | 12h57' |
| Linux | `https://www.bilibili.com/video/BV1WY4y1H7d3/` | 20h33' |
| MyBatis |  |  |
| MySQL | `https://www.bilibili.com/video/BV1Kr4y1i7ru/` | 29h52' |
| Netty | `https://www.bilibili.com/video/BV1py4y1E7oA/` | 23h47' |
| Redis | `1：https://www.bilibili.com/video/BV1cr4y1671t/` <br/> `2：https://www.bilibili.com/video/BV13R4y1v7sP/` | 42h46' <br/> 42h06' |
| RocketMQ | `https://www.bilibili.com/video/BV1cf4y157sz/` | 19h20' |
| SpringBoot | `https://www.bilibili.com/video/BV19K4y1L7MT/` | 26h12' |
| Zookeeper | `https://www.bilibili.com/video/BV1to4y1C7gw/` | 5h52' |
| SpringCloud | `https://www.bilibili.com/video/BV18E411x7eT/` | 25h36' |
|  |  | 249h479'（一天8h，32天） |


## 面试题整理
### Dubbo
1. 使用dubbo和spring cloud有什么区别
```txt
Dubbo适用于大规模分布式服务架构，注重性能和高可用性。
Spring Cloud适用于轻量级微服务架构，注重开发便利性和快速迭代

Dubbo使用RPC（远程过程调用）作为默认的服务调用方式。
Spring Cloud使用HTTP或者消息队列等方式进行服务调用。

springcloud 是一个微服务架构，提供了一整套解决方案；dubbo 是一个 rpc 框架；可以将 dubbo 作为 rpc 组件嵌入到 springcloud 中去。
```

2. 使用dubbo怎么做服务降级和熔断
```txt
服务降级：
    1. 在提供者端配置降级策略：可以通过在服务提供者端的配置文件中设置`<dubbo:service>`的`mock`属性，指定降级的实现类。当服务调用失败或超时时，将会执行降级逻辑。
    2. 在消费者端配置降级策略：可以通过在消费者端的配置文件中设置`<dubbo:reference>`的`mock`属性，指定降级的实现类。当服务调用失败或超时时，将会执行降级逻辑。
服务熔断：
    1. 使用Dubbo提供的`dubbo-hystrix`扩展：Dubbo整合了Hystrix，可以在提供者端通过添加@HystrixCommand注解来实现服务熔断的功能。当服务调用失败或超时达到一定阈值时，将会触发熔断，后续的调用会快速失败。
    2. 使用Dubbo提供的`dubbo-resilience4`j扩展：Dubbo还支持Resilience4j作为熔断器实现，可以通过配置`<dubbo:provider>`或`<dubbo:consumer>`中的circuitBreaker属性来启用。
```

3. 使用dubbo用的什么协议（dubbo协议），传输是否有上限
```txt
默认使用的是Dubbo协议进行服务的传输和通信，但也支持 http、rmi

Dubbo协议本身并没有明确的传输数据大小限制。但是 dubbo 是基于 tcp 的，TCP协议本身对传输的数据大小是有一定限制的，其中最大的限制是由TCP报文的最大长度限制所决定，通常为64KB。
```

4. dubbo的请求处理流程
```txt
1. 服务导出：
    服务提供者通过`Protocol`接口暴露服务，其中会创建并初始化相应的`Exporter`对象。
    `Exporter`对象封装了服务的URL、服务实现对象和底层的通信处理器等信息。
2. 服务引用：
    服务消费者通过`ReferenceConfig`配置服务引用，创建相应的`Invoker`对象。
    `Invoker`对象封装了服务的URL、代理对象和底层的通信处理器等信息。
3. 服务调用：
    服务消费者通过代理对象调用服务方法，最终会进入到`Invoker`对象的`invoke`方法。
    `Invoker`对象负责发起远程调用请求。
4. 请求分发：
    `Invoker`对象将请求发送到底层的通信处理器，通常是`Channel`。
    `Channel`将请求包装成`Request`对象，并发送到服务提供者端。
5. 服务执行：
    服务提供者端接收到请求后，通过`Protocol`的`export`方法找到相应的`Exporter`。
    `Exporter`负责将请求分发给具体的服务实现对象。
6. 结果返回：
    服务实现对象执行完请求后，将结果封装成`Response`对象返回给服务提供者端。
    `Response`对象经过底层的通信处理器发送给服务消费者端。
7. 结果处理：
    服务消费者端接收到`Response`对象后，将结果提取并返回给调用方。
```

5. dubbo的核心功能？
```txt
1. 服务注册与发现：
    Dubbo提供了服务注册中心的支持，可以方便地将服务注册到中心并进行服务的发现和管理。
2. 远程调用：
    Dubbo实现了远程调用的功能，可以通过网络将服务消费者和服务提供者连接起来，实现跨进程或跨网络的方法调用。
3. 负载均衡：
    Dubbo提供了多种负载均衡策略，可以在服务消费者调用服务提供者时进行负载均衡，从而实现请求的分发和均衡。
4. 服务容错：
    Dubbo支持服务容错机制，包括失败重试、熔断器等，可以在服务调用失败或异常时进行相应的处理，提高系统的可靠性和稳定性。
5. 配置管理：
    Dubbo支持动态的配置管理，可以通过配置中心进行配置的统一管理和动态更新，方便系统的配置和调整。
6. 高性能：
    Dubbo注重提供高性能的服务调用，通过优化网络通信、序列化等方面，提升系统的性能和效率。
7. 高度可扩展：
    Dubbo提供了丰富的扩展点和插件机制，可以方便地扩展和定制Dubbo的功能，满足不同场景和需求的定制化要求。
```

6. dubbo的核心组件？
```txt
1. 服务提供者（Provider）：
    负责提供具体的服务实现，并将服务注册到注册中心，接收并处理消费者的请求。
2. 服务消费者（Consumer）：
    负责消费服务，通过服务引用，调用远程的服务提供者，并处理返回的结果。
3. 注册中心（Registry）：
    用于服务的注册与发现，服务提供者将自己的地址信息注册到注册中心，服务消费者通过注册中心查找和获取可用的服务提供者。
4. 远程通信（Remoting）：
    负责服务提供者和服务消费者之间的远程通信，实现请求和响应的传输。
5. 集群容错（Cluster）：
    提供服务调用的容错能力，当某个服务提供者出现故障时，可以进行故障转移和容错处理。
6. 负载均衡（Load Balance）：
    用于在服务消费者选择服务提供者时进行负载均衡，根据负载均衡策略选择合适的服务提供者。
7. 服务路由（Router）：
    用于根据路由规则进行服务调用的路由选择，可以根据条件将请求路由到不同的服务提供者。
8. 配置中心（Config Center）：
    用于集中管理和动态更新Dubbo的配置，提供统一的配置管理。
9. 服务代理（Proxy）：
    用于生成服务的代理对象，将服务接口的调用转化为远程调用。
10. 监控（Monitor）：
    用于监控服务的调用情况和性能指标，收集和统计服务的运行数据。
```

7. dubbo注册和服务发现的流程
```txt
基本流程

1. 服务提供者在启动时会连接注册中心，并将自己的服务信息以一定的格式注册到注册中心上。这些信息包括服务接口名、版本号、IP地址、端口号等。
2. 注册中心接收到服务提供者的注册请求后，会将服务信息保存到注册中心的数据存储中，供后续的服务发现使用。
3. 服务消费者在启动时也会连接注册中心，并从注册中心获取可用的服务提供者列表。这些列表通常根据服务接口名进行分组，每个分组对应一个服务接口。
4. 服务消费者根据负载均衡策略选择一个服务提供者。常见的负载均衡策略包括随机、轮询、权重等。
5. 服务消费者通过网络发起远程调用请求到选择的服务提供者。调用请求通常包括服务接口名、方法名、参数等信息。
6. 服务提供者接收到远程调用请求后，根据接口名和方法名找到对应的服务实现，并执行相应的方法。
7. 服务提供者将方法执行的结果返回给服务消费者。
```

8. 请简述Dubbo框架的核心原理和架构。
```txt
核心原理：
    Dubbo 是一个分布式服务框架，采用面向接口的服务设计思想。它的核心原理就是通过服务的注册与发现、远程通信、负载均衡、降级、集群容错和服务调用等机制，实现了服务之间的透明通信和高效调用。
架构（包括以下几个核心组件）：
    1. 服务提供者：
        提供具体的服务实现，并向注册中心注册服务的地址信息。
    2. 注册中心：
        用于管理服务提供者的地址信息和服务消费者的订阅信息，实现服务的注册与发现功能。
    3. 服务消费者：
        通过注册中心获取可用的服务提供者地址，并发起远程调用请求，获取服务结果。
    4. 监控中心：
        用于监控和统计服务的调用次数、响应时间等指标，提供可视化的监控和报警功能。
    5. 配置中心：
        用于管理Dubbo框架的各种配置参数，支持动态配置和自动推送配置。
    6. 代理层：
        Dubbo利用动态代理技术，为服务消费者生成代理对象，实现服务接口的透明调用。
    7. 过滤器：
        Dubbo通过过滤器链来实现拦截和处理服务调用的过程，可以在不同的阶段进行扩展和自定义操作。
Dubbo 核心原理和架构主要围绕服务注册与发现、远程通信、负载均衡和高可用性展开。
```

9. Dubbo中的服务注册和发现是如何实现的？请说明其具体流程。
```txt
Dubbo中的服务注册和发现是基于注册中心实现的。
    1. 服务提供者将自己提供的服务注册到注册中心，包括服务接口、地址等信息。
    2. 服务消费者向注册中心发起服务查询请求，获取服务提供者的地址列表。
    3. 服务消费者根据负载均衡选择一个最优的服务提供者进行调用。
    4. 服务消费者和服务提供者建立连接，通过远程通信方式进行服务调用。【netty】
```

10. 请介绍Dubbo中的负载均衡策略有哪些，并分别说明其特点和适用场景。
```txt
1. 随机加权（Random）：随机选择一个服务提供者进行调用，适用于负载均衡要求不高的场景。
2. 轮询加权（RoundRobin）：根据服务提供者的权重值进行轮询选择，适用于不同服务提供者性能差异较大的场景。
3. 最少活跃调用（LeastActive）：选择活跃调用数最少的服务提供者进行调用，适用于需要动态调整负载的场景。
4， 一致性 hash：
```

11. Dubbo中的集群容错机制是如何实现的？请列举几种常见的容错策略。
```txt
Dubbo中的集群容错机制是如何实现的？
    Dubbo集群容错机制的大致实现原理是通过集群代理类和扩展点机制

常见的容错策略：
    1. 失败自动恢复(Failover)：当调用失败时，自动切换到另一个可用的服务提供者进行重试。
    2. 失败快速失败(Failfast)：在调用失败后立即返回异常，适用于对响应时间敏感的场景。
    3. 失败安全(Failsafe)：调用失败后，直接忽略错误，适用于日志记录等不重要的操作。
    4. 并行调用(Forking)：同时调用多个服务提供者，只要有一个调用成功即可返回结果，适用于实时性要求较高的场景。

自定义集群容错：实现Cluster接口，并按照Dubbo SPI机制进行配置，指定 cluster 参数为扩展点名称。
```

12. Dubbo中的服务调用是如何实现的？请说明其调用过程和相关的通信协议。
```txt
Dubbo中的服务调用是如何实现的？
    1. 服务消费者发起远程调用请求，包括服务接口、方法名和参数等信息。
    2. 服务消费者根据负载均衡策略选择一个服务提供者，并建立连接。
    3. 服务消费者将调用请求序列化为字节流，通过网络发送给服务提供者。
    4. 服务提供者接收请求并进行反序列化，解析请求的方法名和参数。
    5. 服务提供者根据方法名找到对应的服务实现，并执行服务方法。
    6. 服务提供者将方法执行结果序列化为字节流，通过网络发送给服务消费者。
    7. 服务消费者接收到结果，并进行反序列化，得到最终的调用结果。
通信协议：
    常用的有Dubbo协议、HTTP协议和RMI协议等
    常用的序列化协议有Hessian、JSON、Protobuf等
```

13. Dubbo中的服务版本控制是如何实现的？请说明其原理和使用方式。
```txt
原理：
    1. Dubbo允许为每个服务接口定义一个版本号。当服务提供者发布新版本时，可以在服务接口上增加版本号，以便与旧版本区分开来。
    2. 服务消费者在引用服务时可以指定所需的版本号，Dubbo会根据版本号选择合适的服务提供者进行调用。
    3. Dubbo的路由规则和负载均衡策略会考虑到版本号，以确保服务调用按照预期的版本进行。
使用方式：
    1. 在服务提供者的接口定义上添加版本号注解，例如 @DubboService(version = "1.0")。
    2. 在服务消费者的引用配置中指定所需的版本号，例如 <dubbo:reference id="xxxService" interface="com.example.XXXService" version="1.0"/>。
    3. 当服务消费者发起服务调用时，Dubbo会根据版本号匹配合适的服务提供者进行调用。
源码原理：
    1. 在Dubbo的服务注册和发现过程中，会根据版本号来进行服务匹配。Dubbo会通过版本号选择合适的服务提供者，并将其注册到本地的服务列表中。
    2. Dubbo的路由规则和负载均衡策略会根据版本号来进行筛选和选择，以确保服务调用按照指定的版本进行。
    3. Dubbo的服务调用过程中，会通过调用代理对象的方法来触发远程调用，其中会包含版本号的信息，用于路由和负载均衡的选择。
```

14. Dubbo中的参数验证和参数限制是如何实现的？请说明其使用方式和注意事项。
```txt
参数验证和参数限制可以通过Dubbo Validation模块来实现。

使用方式：
    1. 引入Dubbo Validation依赖：在项目的pom.xml文件中添加Dubbo Validation的依赖项。
    2. 定义验证规则：使用标准的Java Bean Validation（JSR-303）规范，通过注解在服务接口的方法参数上定义验证规则。
    3. 启用参数验证：在Dubbo配置文件中，通过配置<dubbo:service>或<dubbo:reference>的validation属性来启用参数验证。
注意事项：
    1. 参数验证需要在服务提供者和服务消费者两端都进行配置和启用，才能生效。（另一种回答：参数验证只能在服务提供者端起作用，服务消费者需要自行进行参数的校验。）
    2. 参数验证只能在服务接口方法的参数上进行配置，不能在返回值上配置。
    3. 使用JSR-303规范定义的注解来进行参数验证，如@NotNull、@Size、@Pattern等。
    4. 参数验证会在服务调用前进行验证，若参数验证不通过，将抛出验证异常。
    5. Dubbo还提供了扩展的验证注解，如@DubboValidated，用于在服务接口级别进行验证。
```

15. 请简述Dubbo中的异步调用和同步调用的区别，以及各自的适用场景。

16. Dubbo中的分组和分层是什么概念？请说明其作用和使用场景。
```txt
用于对服务进行分类和管理：
    分组(Group)：将服务提供者和消费者按照一定的规则分组，方便针对某一组进行配置和管理。例如，可以将某个服务的不同版本分组。
    分层(Layer)：将服务提供者和消费者按照服务层次进行分层，可以实现服务的细粒度拆分和管理。

作用和使用场景：
    分组可以实现服务版本控制，不同分组的服务提供者可以部署不同的实现版本。
    分层可以实现服务的细粒度划分和管理，例如将不同层次的服务部署在不同的服务器集群上。
```

17. Dubbo中的动态代理是如何实现的？请说明其原理和使用方式。

### JavaGuide
### JUC
### JVM
### Kafka
### Linux
### MyBatis
### MySQL
1. 对 MySQL 进行过调优吗？
```txt
`场景可以使用抽奖系统`

是的，我有对 MySQL 进行调优的经验。通过对其进行调优可以提高数据库的性能和吞吐量。
在进行 MySQL 调优时，我会考虑以下几个方面：
1. 索引优化
    分析查询语句，确定需要创建的索引，并确保索引的选择和使用时有效的。
2. 查询优化
    重构查询语句，以减少不必要的操作和数据量。将热点数据放入缓存。数据分页。
    记录慢查询日志
3. 配置优化
    根据服务器的硬件配置和应用需求，调整MySQL的缓冲区大小，减少磁盘IO操作。设置`innodb_buffer_pool_size`参数
    调整并发连接数，增大`max_connections`参数，需要注意控制并发连接数的上限，避免资源耗尽和性能下降。
    根据系统负载和硬件资源情况，调整线程池的大小。配置如`innodb_thread_concurrency`和`thread_cache_size`参数。
4. 数据库设计优化
5. 性能监控及调优    
    首先，我会分析慢查询日志（Slow Query Log），并评估其执行计划和性能瓶颈，例如添加索引、重构查询语句或者调整表结构。
    其次，我会利用 Performance Schema 来监控数据库的性能指标，如查询次数、锁等待、I/O操作等。如果发现某个表的锁等待时间过长，我可能需要优化并发控制，如使用更合适的事务隔离级别或调整锁策略。如果发现磁盘I/O负载过高，我可能需要考虑使用缓存技术或者调整存储引擎配置。
    此外，我还会监控数据库服务器的硬件资源使用情况，如CPU利用率、内存使用情况和磁盘IO等。如果发现资源瓶颈，我可能需要考虑升级硬件或者调整数据库配置参数，以提高性能和吞吐量。
```
2. 讲一下数据库分库分表的实现；
```txt
数据库分库分表的实现方式可以分为两个层面：垂直分库分表和水平分库分表。
1. 垂直分库
    将不同的数据表按照业务或功能划分到不同的数据库实例中，使每个数据库实例只负责特定的数据表。（如可以将用户表、订单表、商品表等根据其关联性拆分到不同的数据库中）
2. 垂直分表
    将一个大表按照列的划分规则，拆分为多个较小的表，每个拆分后的表只包含部分列数据（用主键或外键关联）。减少单表的数据量，提高查询性能。
3. 水平分库
    将数据按照给定规则划分到多个数据库实例中，每个数据库实例存储部分数据，通过分片将查询请求路由到相应的数据库实例。这样可以提高并发处理能力和数据存储容量。
4. 水平分表
    按照拆分策略，将一个表的数据拆分到多个表中
    每个库的表结构都一样
    每个库的数据都不一样
    所有表的并集构成全量数据
    
在实现数据库分库分表时，需要考虑以下几个关键点：
1. 数据分片规则
    确保数据能够均匀地分布到不同的数据库实例或表中，避免热点数据集中和数据倾斜的问题。
2. 数据一致性
    如果某个事务涉及到多个数据库实例或表的操作，需要使用分布式事务或者其它一致性机制来保证数据的一致性。
3. 跨节点查询
    当需要跨多个数据库实例或表进行查询时，需要设计合适的查询路由和跨节点的查询策略，确保查询的效率和正确性。
```
3. 索引对数据库的作用是什么？
```txt
B+树
1. 索引（index）是帮助 MySQL 高效获取数据的数据结构（有序）。在数据之外，数据库系统还维护着满足特定查找算法的数据结构，这些数据结构以某种方式引用（指向）数据。这种数据结构就是索引。
2. 优化排序和分组操作（有序且默认按照升序排序）。
3. 唯一性约束。
4. 减少数据存储空间（B+树索引只有叶子节点才存储数据，非叶子节点只用来索引）。
```
4. 索引的数据结构是什么？什么是 B+ 树？
```txt
索引在数据库中使用的数据结构通常使B+树。

B+树是一种自平衡的树型数据结构，它是一种基于 B树的一种变种，相较于B树，B+树在叶子节点上进行了优化，使得所有叶子节点按照键值的大小顺序形成一个有序链表。
B+树的特点如下：
    1. 非叶子节点都会出现在叶子节点的链表中，而且叶子节点之间由指针进行连接，形成一个有序的链表结构。
    2. 非叶子节点只用来索引，不存储数据记录。使得每个节点可以存储更多的索引信息。这样减少了树的层级，提高了查询效率。
```
5. MySQL 如何保证事务的一致性？
```txt
MySQL通过使用ACID（原子性、一致性、隔离性和持久性）属性来保证事务的一致性。
1. 原子性：MySQL 通过 undo log（回滚日志）来实现原子性。
2. 一致性：MySQL 通过 undo log（回滚日志）和 redo log（事务/重做日志）来实现一致性。
3. 隔离性：锁 + MVCC
4. 持久性：redo log

redo log：该日志文件由两部分组成：重做日志缓冲（redo log buffer）以及重做日志文件（redo log file），前者是在内存中，后者在磁盘中。当事务提交后会把所有修改信息都存到该日志文件中，用于在刷新脏页到磁盘发生错误时，进行数据恢复使用。
undo log：日志解决事务的原子性。事务的原子性依赖于 undo log。undo log和redo log记录物理日志不一样，undo log是逻辑日志。可以认为当 delete 一条记录时，undo log中会记录一条对应的 insert 记录，反之亦然，当 update 一条记录时，它记录一条对应相反的 update 记录。当执行 rollback 时，就可以从 undo log 中的逻辑记录中读取到相应的内容并进行回滚。（undo log 记录的是变更前的数据）
```
6. Redis 和 MySQL 有什么区别？
```txt
Redis和MySQL在数据模型、存储介质、数据一致性和应用场景等方面存在明显的区别。
1. Redis是基于键值对的内存数据库；MySQL是关系型数据库，关系型模型表明了数据库中所存储的数据之间的联系。
2. Redis主要使用内存作为存储介质（Redis也能够实现持久化）；MySQL存储在磁盘上，MySQL的数据存储方式通常更适合长期存储大量数据。
3. Redis通常不保证数据的强一致性；MySQL使用日志和锁等机制来保证数据的可靠性。
4. Redis适合用于高并发的读写场景，如缓存、排行榜，适合对响应时间有较高要求的场景；MySQL适合用于需要复杂查询和事务处理的场景，如电商。
```
7.一个 SQL 语句在 MySQL 中的执行流程。
```txt
1. 语法解析和词法分析：MySQL首先对SQL语句进行语法解析和词法分析，将其拆分成合法的语法单元，如关键字、表名、列名等。这个过程会检查语句的语法是否正确，以及各个元素的合法性。
2. 查询优化和执行计划生成：MySQL会对解析后的SQL语句进行查询优化，以确定最佳的执行计划。优化器会根据表的统计信息、索引情况和查询条件等，选择合适的索引、连接方式和操作顺序，生成一个执行计划。
3. 执行计划执行：MySQL接下来会执行生成的执行计划。具体的执行过程会根据SQL语句的类型而有所不同。
    - 对于查询语句（SELECT），MySQL会根据执行计划逐步获取需要的数据。它可能会使用索引进行快速定位，进行表扫描或索引扫描，根据查询条件过滤数据，并对结果进行排序、分组等操作。
    - 对于修改语句（INSERT、UPDATE、DELETE），MySQL会根据执行计划进行相应的数据修改操作。它可能会涉及表锁、行锁等并发控制机制，确保数据的一致性和完整性。
4. 结果返回：执行完成后，MySQL会将查询结果返回给客户端。对于SELECT语句，返回的是查询结果集；对于INSERT、UPDATE、DELETE语句，返回的是受影响的行数。

查询语句的执行流程：权限校验 ➡ 查询缓存（如果命中缓存，直接返回给客户端） ➡ 分析器 ➡ 优化器 ➡ 权限校验 ➡ 执行器 ➡ 引擎
查询缓存在 MySQL 8.0 之后被移除。

更新语句的执行流程：分析器 ➡ 权限校验 ➡ 执行器 ➡ 引擎 ➡ redo log（prepare 状态） ➡ binlog ➡ redo log（commit 状态）
```
8. MySQL 怎么删除数据。
```txt
可以使用DELETE语句来删除数据

DELETE语句的原理是通过以下步骤实现数据删除：
1. 解析 SQL 语句
    MySQL首先对DELETE语句进行语法解析和词法分析，确保语句的语法正确，并提取出要删除数据的表名和删除条件。
2. 锁定表（或者行）
    在执行DELETE语句之前，MySQL会根据事务隔离级别的设置以及表的锁定机制来锁定待删除的表，确保在删除过程中不会有其他并发操作对数据产生干扰。
3. 执行删除操作
    MySQL会根据DELETE语句中指定的表名和删除条件，逐行地遍历表中的数据。对于每一行数据，MySQL会检查是否满足删除条件。如果满足条件，就将该行数据从表中删除。
4. 日志记录
    binlog、undo log
5. 释放锁
```
9. delete和truncate的区别
```txt
1. 数据删除方式
    delete是数据删除操作，以行为单位，delete语句可以使用条件来指定要删除的数据，不带条件就是删除全部数据。
    truncate是表操作，通过截断（即删除）整个表来删除数据，truncate语句会删除表中的所有数据，但保留表结构。
2. 数据库日志和事务
    delete会产生日志，可以被回滚、撤销等操作。
    truncate也会产生日志，但它在删除操作之前会将整个表结构存储在日志中，由于truncate是DDL（数据定义语言）操作，它无法在事务中进行，也无法被回滚。
3. 数据库空间释放
    delete语句删除行数据时，数据库会将已删除数据所占用的空间标记为可重用，但不会立即释放空间。这意味着表的大小会保持不变，但已删除的数据所占用的空间可以被后续插入的数据重用。
    truncate table语句删除整个表的数据，并释放表所占用的空间。truncate操作将重置表的大小，并且表中的数据将无法恢复。
4. 删除速度
    truncate table通常比delete语句更快，因为它是通过截断整个表来删除数据，而不是逐行删除。truncate操作是一种DDL操作，直接操作表的元数据，因此效率较高。
    delete语句是一种DML操作，它需要逐行删除数据，执行速度可能相对较慢，特别是在删除大量数据时。
```
10. 写查询语句的时候应该从哪些方面考虑来注意性能。
```txt
1. 使用合适的索引
    通过分析查询条件和经常访问的列，可以确定是否需要添加索引。
2. 缩小查询范围
    避免全表扫描，使用WHERE子句来限制返回的数据行数，并确保查询条件能够使用索引。
3. 避免全表连接
    尽量避免在查询中使用多个表的全表连接操作，特别是当表的数据量很大时。如果必须进行连接操作，确保连接字段上有适当的索引。
4. 避免使用 select *
5. 考虑分页
...
```
11. 什么是联合索引，为什么要建联合索引？
```txt
联合索引（即一个索引包含了多个列）

在业务场景中，如果存在多个查询条件，考虑针对于查询字段建立索引时，建立建立联合索引，而非单列索引。
尽量使用联合索引，减少单列索引，查询时，联合索引很多时候可以覆盖索引，节省存储空间，避免回表，提高查询效率
```
12. a,b,c,d，四个字段，查询语句的where条件a=b，order by c。（mysql翻页越翻越慢怎么优化，满足a=b的字段很多，怎么高效的排序，分页查询）
```txt
mysql翻页越翻越慢怎么优化
通过覆盖索引和子查询能够比较好地提高性能
如：select s.* from tb_sku s, (select id from tb_sku order by id limit 2000000, 10) c where s.id = c.id

满足a=b的字段很多
如果 b 是字符串类型的字段，字段的长度较长，可以针对于字段的特点，建立前缀索引。

怎么高效的排序
排序字段建立索引

分页查询
通过覆盖索引和子查询能够比较好地提高性能
如：select s.* from tb_sku s, (select id from tb_sku order by id limit 2000000, 10) c where s.id = c.id
```
13. sql题：一个学生成绩表，里面有学号，科目，成绩，统计出总成绩前十个的学号。
```sql
SELECT 学号
FROM 学生成绩表
GROUP BY 学号
ORDER BY SUM(成绩) DESC
LIMIT 10;
```
14. SQL explain 会输出哪些信息？
```txt
EXPLAIN SELECT * FROM emp WHERE id=1;

    id  select_type  table   partitions  type    possible_keys  key      key_len  ref       rows  filtered  Extra   
------  -----------  ------  ----------  ------  -------------  -------  -------  ------  ------  --------  --------
     1  SIMPLE       emp     (NULL)      const   PRIMARY        PRIMARY  8        const        1    100.00  (NULL)  
     
id
    select 查询的序列号，表示查询中执行 select 子句(子查询)或者是操作表的顺序（id相同，执行顺序从上到下；id不同(子查询)，值越大，越先执行）。
select_type
    表示 select 的类型，常见的取值有 SIMPLE（简单表，即不适用表连接或者子查询）、PRIMARY（子查询，即外层的查询）、UNION（UNION中的第二个或者后面的查询语句）、SUBQUERY（select/where之后包含了子查询）等。
type
    表示连接类型，性能由好到差的连接类型为：NULL、system、const（主键或唯一索引出现）、er_ref、ref（非唯一索引会出现）、range、index、all。
possible_keys
    显示可能应用在这张表上的索引，一个或多个。
key
    实际使用的索引，如果为NULL，则没有使用索引。
...
```
15. sql怎么手动加锁
```txt
全局锁：
    flush tables with read lock;
表锁：
    lock tables 表名 read;
    select ... lock in share mode
    select ... for update
```
16. 介绍一些mysql底层结构？
```txt
连接层
    最上层的是一些客户端和连接服务，主要完成一些连接处理、授权认证以及相关的安全方案。服务器也会为安全接入的客户端验证它所具有的操作权限。
服务层
    服务层架构主要完成大多数的核心服务功能，如 SQL 接口、完成缓存的查询、SQL 的分析和优化、部分内置函数的执行。所有跨存储引擎的功能也在这一层实现。如存储过程、函数等。
引擎层
    存储引擎真正的负责了 MySQL 中数据的存储和提取，服务器通过 API 和存储引擎进行通信。不同的存储引擎具有不同的功能。我们可以根据自己的需求，来选择合适的存储引擎。
存储层
    主要是将数据存储再文件系统之上，并完成与存储引擎的交互。【持久化】
```
![](image/mysql_MySQL体系结构.png)

17. mysql设计索引的注意事项？ 
```txt
设计原则
1. 针对于数据量较大，且查询比较频繁的表建立索引。（百万以上的数据考虑建立索引）
2. 针对于常作为查询条件（where）、排序（order by）、分组（group by）操作的字段建立索引。
3 .尽量选择区分度高的列作为索引，尽量建立唯一索引，区分度越高，使用索引的效率越高。（比如员工id、身份证号、手机号等）
4 .如果是字符串类型的字段，字段的长度较长，可以针对于字段的特点，建立前缀索引。
5 .尽量使用联合索引，减少单列索引，查询时，联合索引很多时候可以覆盖索引，节省存储空间，避免回表，提高查询效率
6 .要控制索引的数量，索引越多，维护索引结构的代价也就越大，会影响增删改的效率。
7 .如果索引列不能存储 NULL 值，请在创建表时使用 NOT NULL 约束。当优化器知道每列是否包含 NULL 值时，它能够更好地确定哪个索引最有效地用于查询。
```

18. 你之前mysql怎么加锁的？
```txt
select ... lock in share mode

如：select * from score where id = 1 lock in share mode;
lock in share mode：为这一行加上行锁的共享锁，同时为 score 表加上意向共享锁
```

19. 慢SQL问题出现了要怎么解决？
```txt
1. 定位慢SQL
2. 分析慢SQL
3. 优化慢SQL
4. 使用缓存技术

索引优化、数据库配置优化等方面的工作
```

### Netty
### Redis
### RocketMQ
### SpringBoot
### Zookeeper
### SpringCloud

### 附录
Java 实现 MySQL 读写分离
> https://blog.csdn.net/Maxiao1204/article/details/87566166




