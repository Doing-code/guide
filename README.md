# roadmap

这份文档基本涵盖了 Java 开发所需的技能。高亮部分表示应聘 Java 中级（3-5年），在岗位招聘中所具备的能力。如果学习到新的组件技术，也会在第一时间进行更新到此文档。

> Java 核心知识包括：Java基础知识、IO、多线程、集合、网络编程等。

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

### `网络通信协议`

- [x] [UDP](JavaGuide/网络通信协议.md#UDP)
- [x] [TCP](JavaGuide/网络通信协议.md#TCP)
- [x] [HTTP](JavaGuide/网络通信协议.md#HTTP)
- [x] [HTTPS](https://www.cloudflare.com/zh-cn/learning/ssl/what-is-https/)

## 学习一门编程语言

> 确保学习它的特点。运行时的核心细节，例如并发、内存模型等等。

### Java

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

- [x] [CDN](JavaGuide/什么是CDN.md)
- [x] [`Redis`](JavaGuide/Redis.md)
- [ ] Memcached

## Web安全知识

- [ ] MD5

- [ ] SHA

- [ ] scrypt

- [ ] bcrypt

- [ ] `HTTPS`

- [ ] 内容安全政策

- [ ] CORS

- [ ] SSL/TLS

- [ ] OWASP 安全风险

## 测试

- [ ] 集成测试
- [ ] 单元测试
- [ ] 功能测试

## CI / CD

- [x] [Jenkins](JavaGuide/Jenkins.md)

## 设计与开发原则

- [ ] `设计模式`
- [ ] `领域驱动设计`
- [ ] 测试驱动开发

## 架构模式

- [ ] 单体应用
- [ ] 微服务
- [ ] SOA
- [ ] 无服务化
- [ ] 服务网格

## 搜索引擎

- [x] [`Elasticsearch`](JavaGuide/Elasticsearch.md)
- [ ] Solr

## 消息代理

- [x] [`Kafka`](JavaGuide/Kafka.md)
- [x] [`RocketMQ`](JavaGuide/RocketMQ.md)

## 容器化与虚拟化

- [ ] [`Docker`](JavaGuide/Docker.md)
- [ ] LXC
- [ ] `Kubernetes`

## GraphQL

- [ ] Apollo
- [ ] Relay Modern

## Websockets
``
## 网页服务器

- [x] [`Nginx`](JavaGuide/Nginx.md)
- [ ] Apache
- [ ] Caddy
- [x] [`Tomcat`](JavaGuide/Tomcat.md)

## `微服务组件`

> Nacos: https://zhuanlan.zhihu.com/p/527746095

- [x] [Nacos](JavaGuide/Nacos源码分析.md)
- [ ] OpenFeign
- [ ] Ribbon
- [ ] [Sentinel](JavaGuide/Sentinel源码分析.md)
- [ ] Gateway
- [ ] Seata

## `分布式组件 `

- [x] [Zookeeper](JavaGuide/Zookeeper.md)
- [x] [Dubbo](JavaGuide/Dubbo.md)

## 项目构建

- [ ] `Maven`

## 定时任务

- [ ] xxl-job