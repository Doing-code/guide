# roadmap

这份文档基本涵盖了 Java 开发所需的技能。`高亮部分`表示应聘 Java 中级（3-5年），在岗位招聘中所具备的能力。

尽管罗列了这么多的文档，但如果不涉及到实际应用的话，也是很难消化的，最快消化的方式是带入生产环境，或者带入面试场景。

> 社招的卷是无形的，每错过一个年龄段的职业生涯成长，就要更多时间弥补。当年龄达不到对应的岗位时，就有可能会被动下车。
> 
> 所有的学习都不是为了公司而学，都是为了自己在各方面的积累。让自己技术所长，远超公司所需，这样才有更多的跳槽、涨薪、议价机会。
>
> 那么，此时就不用纠结于学哪个，从哪个开始。陆续地都吸收给自己。

其实即使Java中级，面试时的技术点也就那些，重要的是有业务场景，比如秒杀，比如数据库大数据量的优化等，比如调优等。重要的是解题思路。

目前能够独立开发 Java、Golang、Python、C++ 相关的程序。但 Golang、Python、C++ 的熟练程度没有 Java 那么高。后续有时间会找几个开源项目进行练手。

面试了三个月，面试机会少得可怜，有学历的问题，也有简历上的问题，但我自认为至少从简历上来说，我有这个自信，但还是被拒了。索性继续往后面学，elasticsearch、docker、k8s、架构等。

用更多的项目磨练自己。

## 互联网

- [x] [互联网是如何工作的?](JavaGuide/互联网是如何工作的.md)
- [x] [什么是HTTP?](JavaGuide/什么是HTTP.md)
- [x] [浏览器是如何工作的?](https://web.dev/articles/howbrowserswork?hl=zh-cn)
- [x] [DNS是如何工作的?](JavaGuide/DNS是如何工作的.md)
- [x] [什么是域名?](JavaGuide/什么是域名.md)
- [x] 什么是托管? — 云服务器

## 操作系统和基本知识

- [x] [终端用法](https://developer.mozilla.org/zh-CN/docs/Learn/Tools_and_testing/Understanding_client-side_tools/Command_line)
- [ ] 操作系统通常如何工作
- [x] [进程管理](https://medium.com/@akhandmishra/operating-system-process-and-process-management-108d83e8ce60)
- [x] [线程与并发](https://www.javatpoint.com/concurrency-in-operating-system)
- [x] [基本的终端命令](https://www.hostinger.com/tutorials/linux-commands)
- [x] [内存管理](https://www.geeksforgeeks.org/memory-management-in-operating-system/)
- [x] [进程间通信](https://www.geeksforgeeks.org/inter-process-communication-ipc/)
- [x] [I/O管理](https://www.tutorialspoint.com/operating_system/os_io_hardware.htm)
- [x] [POSIX 基础](https://www.baeldung.com/linux/posix)
- [x] [基本的网络概念](https://aws.amazon.com/cn/what-is/computer-networking/)
- [x] [`Linux`](JavaGuide/Linux_命令速查.md)
- [x] [`Shell`](JavaGuide/Linux_Shell.md)

### `网络通信协议`

- [x] [UDP](JavaGuide/网络通信协议.md#UDP)
- [x] [TCP](JavaGuide/网络通信协议.md#TCP)
- [x] [HTTP](JavaGuide/网络通信协议.md#HTTP)
- [x] [HTTPS](https://zhuanlan.zhihu.com/p/43789231)

## 学习一门编程语言

> 确保学习它的特点。运行时的核心细节，例如并发、内存模型等等。

### Java

> Java 核心知识包括：Java基础知识、IO、多线程、集合、网络编程等。

- [x] [`Java核心`](JavaGuide/JavaGuide.md)
- [ ] [`数据结构与算法`](JavaGuide/DataStructureAndAlgorithms.md)
- [x] [`JVM`](JavaGuide/JVM.md)
- [x] [`并发编程`](JavaGuide/JUC.md)
- [x] [`Spring`](https://github.com/Doing-code/SpringFramework)
- [x] [`SpringMVC`](JavaGuide/SpringMVC.md)
- [x] [`MyBatis`](https://github.com/Doing-code/MyBatisFramework)
- [x] [`SpringBoot`](JavaGuide/SpringBoot.md)
- [x] [`SpringCloud`](README.md#微服务组件)
- [x] [网络编程之Netty](JavaGuide/Netty.md)

### Python

- [x] [Python3小时快速入门](JavaGuide/Python3小时快速入门.md)

### Go

- [x] [Golang8小时转职](JavaGuide/Golang8小时转职.md)

### C++

- [x] [C++快速入门](JavaGuide/C++快速入门.md)

### Vue

## 版本控制系统

- [x] [`Git`](JavaGuide/Git_命令速查.md)

## 仓库托管服务

- [ ] Github
- [ ] Gitlab
- [ ] Bitbucket

## 关系型数据库

- [x] [`PostgreSQL`](https://www.postgresql.org/docs/current/sql-syntax-lexical.html)
- [x] [`MySQL`](JavaGuide/MySQL.md)
- [x] [`Oracle`](https://docs.oracle.com/en/database/oracle/oracle-database/23/cncpt/tables-and-table-clusters.html#GUID-096986C4-9AD7-401D-BA6D-EF6CD4B494FE)

> PostgreSQL、Oracle、SQL Server 因工作中不常使用，仅作为开发时查阅文档。目前 MySQL 作为主力数据库开发

## NoSQL 数据库

> MongoDB roadmap: `https://roadmap.sh/mongodb`

- [x] [`MongoDB`](JavaGuide/MongoDB.md)
- [ ] RethinkDB - 实时数据库
- [ ] CouchDB - 文档数据库
- [ ] DynamoDB - Key-Value 数据库

## 扩展数据库

- [ ] 数据库规范化

- [x] [索引及其工作机制](https://www.freecodecamp.org/news/database-indexing-at-a-glance-bb50809d48bd/)

## 学习 API

> gRPC 可以直接使用服务的 IP 地址和端口号进行通信。可以不用注册中心。但是对于分布式系统，使用注册中心可以提供更好地服务发现和管理。gRPC 的官方推荐使用的注册中心是 etcd 或 Consul。如果您使用 Kubernetes，也可以使用 Kubernetes 自带的服务发现和负载均衡功能。

- [x] [gRPC](https://grpc.io/docs/what-is-grpc/introduction/)

### 身份验证

> Cookie：在客户端设备存储状态信息（小文本文件，唯一标识用户的会话），一般用于客户端/服务器，因为HTTP是无状态的。如Cookie记录用户登录状态，请求服务器时携带Cookie（又服务器生成，并将其附加到要返回给客户端的响应 cookie）。
>
> OAuth：开放的授权标准，任何人都可以实现它。OAuth 是 REST/API 的委托授权框架。它使应用程序能够在不泄露用户密码的情况下获得对用户数据的有限访问（范围）。
>
> Basic authentication（基本身份认证）：基本身份验证是HTTP规范的一部分，但不应将基本身份验证与标准用户名和密码身份验证混淆。它是如何工作的？答案是：由服务器的响应来控制（`WWW-Authenticate`）。
>
> Token认证：基于token的身份验证是一种允许用户验证其身份的协议。使用基于令牌的身份验证系统，访问者只需验证一次凭据。作为回报，他们将获得一个令牌，允许在您定义的时间段内进行访问。
>
> JWT（JSON Web Token）：JWT是基于token的身份认证的方案。
>
> 授权在 OAuth 中体现，授权是验证您有权访问的过程。而身份验证是验证身份的过程。

- [x] [Cookie](https://zh.wikipedia.org/wiki/Cookie)
- [X] [`OAuth`](https://developer.okta.com/blog/2017/06/21/what-the-heck-is-oauth#enter-openid-connect)
- [x] [基本验证](https://roadmap.sh/guides/http-basic-authentication)
- [x] [Token认证](https://www.okta.com/identity-101/what-is-token-based-authentication/)
- [x] [JWT](https://jwt.io/introduction)

## 缓存

> Memcached：类似于 HashMap，key-value存储结构，只支持 string 类型，且内存淘汰仅 lru（least recently used 最少最近使用）

- [x] [CDN](JavaGuide/什么是CDN.md)
- [x] [`Redis`](JavaGuide/Redis.md)
- [x] [Memcached](https://github.com/memcached/memcached/wiki/Commands#storage-commands)

## Web安全知识

> **MD5**：MD5（message-digest algorithm 消息摘要算法）哈希算法是**一种单向加密函数**。它接受任意长度的消息作为输入，并返回固定长度（128位）的摘要值作为输出，用于验证原始消息。不安全，会产生加密碰撞。
>
> **SHA**：Secure Hash Algorithm（安全散列算法），其中最常见的就是 SHA-256、SHA-512，数字表示输出的摘要长度。
>
> **scrypt**：密钥派生函数，加密算法。被用在密码货币的工作量证明算法上。
>
> **bcrypt**：是一种密码哈希函数。
>
> **内容安全政策**：Content Security Policy，它是基于 HTTP header（`Content-Security-Policy: policy`） 实现的，策略（policy）参数是一个包含了各种描述你的 CSP 策略指令的字符串。
>
> **CORS**：Cross-Origin Resource Sharing（跨源资源共享）是一种基于 HTTP 头的机制，使得浏览器允许这些源（`https://domain-a.com`）访问加载自己的资源(`https://domain-b.com/data.json`)
>
> **SSL/TLS**：是一种加密安全协议，但大多数现代 Web 浏览器已彻底不再支持 SSL，因其存在多个已知漏洞。SSL 是另一个称为 TLS（传输层安全性）的协议的直接前身。如今提供“SSL”的任何供应商提供的几乎肯定都是 TLS 保护。
>
> **OWASP**：Open Web Application Security Project（开放 Web 应用程序安全项目）。是一个在线社区，它生成 Web 应用程序安全领域的免费文章、方法、文档、工具和技术。**「暂时不对其深入」**

- [x] [MD5](https://zh.wikipedia.org/wiki/MD5)
- [x] [SHA](https://zh.wikipedia.org/wiki/SHA-2)
- [x] [scrypt](https://zh.wikipedia.org/wiki/Scrypt)
- [x] [bcrypt](https://auth0.com/blog/hashing-in-action-understanding-bcrypt/)
- [x] [`HTTPS`](https://zhuanlan.zhihu.com/p/43789231)
- [x] [内容安全政策](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CSP)
- [x] [CORS](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS)
- [x] [SSL/TLS](https://en.wikipedia.org/wiki/Transport_Layer_Security)
- [x] [OWASP 安全风险](https://github.com/OWASP/ASVS)

## 测试

> 集成测试：集成测试主要测试软件模块之间的交互和协作。
>
> 单元测试：单元测试是由开发人员在应用程序的开发（编码阶段）期间完成的。单元测试隔离一段代码并验证其正确性。单元可以是单独的功能、方法、过程、模块或对象。
>
> 功能测试：功能测试的目的是通过提供适当的输入、根据功能需求验证输出来测试软件应用程序的每个功能。功能测试主要涉及黑盒测试，不关心应用程序的源代码。此测试检查用户界面、API、数据库、安全性、客户端/服务器通信以及被测应用程序的其他功能。测试可以手动或使用自动化完成。

- [x] [什么是软件测试](JavaGuide/什么是软件测试.md)
- [x] [软件测试教程](https://www.guru99.com/software-testing.html)
- [x] [集成测试](https://www.guru99.com/integration-testing.html)
- [x] [单元测试](https://www.guru99.com/unit-testing-guide.html)
- [x] [功能测试](https://www.guru99.com/functional-testing.html)

## CI / CD

> DevOps：Development 和 Operations 的合成词。continuous integration（CI，持续集成（测试）），continuous delivery（CD，持续交付（部署））

- [x] [Jenkins](JavaGuide/Jenkins.md)

## 设计与开发原则

> 常见的设计模式有：代理模式、模板方法模式、策略模式、责任链模式（典型的有Sentinel的限流机制，责任链调用，只要不满足任一调用链，就不允许访问。（请求从一端进入，并不断从一个对象传递到另一个对象，直到找到合适的处理程序））
> 
> 领域驱动设计 和 测试驱动开发 现阶段有点难以理解（实习岗位），等到后面有机会接触到架构的机会再进行补充

- [x] [`设计模式`](https://github.com/kamranahmedse/design-patterns-for-humans)
- [ ] `领域驱动设计`
- [ ] 测试驱动开发

## 架构模式

> **分布式结构**：根据业务功能对系统进行拆分，每个业务模块作为独立项目开发，称为一个服务。
> 
> **微服务**：一种经过良好架构设计的**分布式**架构方案（本质还是分布式架构，只不过是最佳实践），拆分粒度更细，做到单一职责。
>
> **serverless**：无服务器是一种云计算应用程序开发和执行模型，使开发人员能够构建和运行应用程序代码，而无需配置或管理服务器或后端基础设施。“serverless”描述了服务器对开发人员是不可见的。使用云服务提供商的无服务器平台。
>
> **Service Mesh**：可简单理解为用于管理和保护微服务之间的通信，并提供负载平衡、服务发现和可观察性等功能。常见的有 Istio。

- [x] [单体应用](https://datamify.medium.com/monolithic-architecture-advantages-and-disadvantages-e71a603eec89)
- [x] [微服务](https://smartbear.com/learn/api-design/microservices/)
- [ ] [SOA](https://docs.oasis-open.org/soa-rm/soa-ra/v1.0/soa-ra.html)
- [ ] [无服务化](https://www.ibm.com/topics/serverless)
- [ ] [服务网格](https://www.nginx.com/blog/what-is-a-service-mesh/)

## 搜索引擎

> 现在都是 ELK 一套组合拳，Solr 可以等到将内存扩大后再进行对其的深入。

- [x] [`Elasticsearch`](JavaGuide/Elasticsearch.md)
- [ ] Solr

## 消息代理

- [x] [`Kafka`](JavaGuide/Kafka.md)
- [x] [`RocketMQ`](JavaGuide/RocketMQ.md)

## 容器化与虚拟化

- [x] [`Docker`](JavaGuide/Docker.md)
- [ ] LXC -- Linux Containers
- [x] `Kubernetes`

## GraphQL

- [ ] Apollo
- [ ] Relay Modern

## WebSockets

## Server Sent Events（服务器发送事件）

## 网页服务器

> Nginx使用C开发的；Caddy是Go开发的。Apache侧重于 HTTP Server，而Tomcat可以认为是Apache的补充，其侧重于Servlet。
>
> Nginx和Caddy提供的功能都是类似的

- [x] [`Nginx`](JavaGuide/Nginx.md)
- [ ] Apache
- [x] [Caddy](https://caddyserver.com/docs/getting-started)
- [x] [`Tomcat`](JavaGuide/Tomcat.md)

## `微服务组件`

> Nacos: https://zhuanlan.zhihu.com/p/527746095
>
> 负载均衡组件的Ribbon停止更新了，推荐使用 Loadbalancer，Spring官方提供的。

- [x] [Nacos](JavaGuide/Nacos源码分析.md)
- [x] [OpenFeign](JavaGuide/OpenFeign.md)
- [x] [Loadbalancer](https://blog.51cto.com/knifeedge/5807620)
- [x] [Sentinel](JavaGuide/Sentinel源码分析.md)
- [x] [Gateway](JavaGuide/Gateway.md)
- [ ] Seata

## `分布式组件`

- [x] [Zookeeper](JavaGuide/Zookeeper.md)
- [x] [Dubbo](JavaGuide/Dubbo.md)

## 项目构建

- [x] [`Maven`](JavaGuide/Maven快速入门.md)

## 定时任务

- [ ] xxl-job