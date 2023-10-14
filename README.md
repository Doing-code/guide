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

### Go

### Vue

## 版本控制系统

- [x] [`Git`](JavaGuide/Git_命令速查.md)

## 仓库托管服务

- [ ] Github
- [ ] Gitlab
- [ ] Bitbucket

## 关系型数据库

- [ ] `PostgreSQL`
- [x] [`MySQL`](JavaGuide/MySQL.md)
- [ ] `Oracle`

## NoSQL 数据库

- [ ] [`MongoDB`](JavaGuide/MongoDB.md)
- [ ] RethinkDB
- [ ] CouchDB
- [ ] DynamoDB

## 扩展数据库

- [ ] 数据库规范化

- [ ] 索引及其工作机制

## 学习 API

### 身份验证

- [ ] Cookie
- [ ] `OAuth`
- [ ] 基本验证
- [ ] Token验证
- [ ] JWT

## 缓存

- [ ] CDN
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