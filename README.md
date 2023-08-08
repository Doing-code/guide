# Java Guide

## 已办

- [x] [Dubbo](JavaGuide/Dubbo.md)
- [x] [JavaGuide](JavaGuide/JavaGuide.md)
- [x] [JUC](JavaGuide/JUC.md)
- [x] [JVM](JavaGuide/JVM.md)
- [x] [Kafka](JavaGuide/Kafka.md)
- [x] [MyBatis](https://github.com/Doing-code/MyBatisFramework)
- [x] [Spring](https://github.com/Doing-code/SpringFramework)
- [x] [MySQL](JavaGuide/MySQL.md)
- [x] [Netty](JavaGuide/Netty.md)
- [x] [Redis](JavaGuide/Redis.md)
- [x] [SpringBoot](JavaGuide/SpringBoot.md)
- [x] [Zookeeper](JavaGuide/Zookeeper.md)

## 待办
- [ ] 需重新梳理 JavaGuide 
- [ ] Spring Cloud
- [ ] Nginx
- [ ] Tomcat
- [ ] ElasticSearch
- [ ] Flume
- [ ] Jenkins
- [ ] HTTP、HTTPS、Websocket、TCP
- [ ] MongoDB
- [ ] Kubernetes
- [ ] xxl-job
- [ ] devops

## 系统性能指标

### QPS

QPS（Query Per Second）：每秒查询率。指的是单位时间内查询或访问服务器的次数。是对一个特定的查询服务器在规定时间内所处理流量多少的衡量标准。

### TPS

TPS（Transaction Per Second）：每秒事务数。指的是单位时间内系统处理的事务数。一个事务量里可以包含多次查询。当多次查询或访问服务器时，一个TPS相当于多个QPS；当只查询或访问一次时，一个TPS则等价于一个QPS。如，访问一个页面会请求服务器2次，一次访问，产生一个"T"，产生2个"Q"。

一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。即客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数。

TPS = 总请求数 / 总时间

若想满足TPS在1200~1500的话，总时间为2h，那么有 总请求数 = 1200 * 2 * 3600 = 8,640,000

则满足每小时8,640,000的总请求数是一个相当高的要求，需要一台高性能的服务器来实现。以下是一个中高配置的参考值：

- CPU: 2 x Intel Xeon Gold 6230 20-Core 2.1GHz

- 内存: 128GB DDR4 ECC RAM

- 存储: 1TB NVMe SSD (可考虑使用 RAID 配置)

- 网络: 千兆以太网适配器

- ......

### RT

RT（Response Time）：响应时间。指的是执行一个请求从开始到最后收到响应数据所花费的总体时间。即从客户端发起请求到收到服务器响应结果的时间。

### 并发量

并发量指的是系统同时能处理的请求数，在同一个时间段内对系统发起的请求数量。（可以理解为系统的负载能力）

### 吞吐量

吞吐量指的是系统处理客户端请求数量的总和。吞吐率是单位时间内的吞吐量。系统吞吐量越低，系统承载压力的能力越差。
