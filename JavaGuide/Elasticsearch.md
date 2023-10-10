# Elasticsearch

理解一些新颖的概念比学习操作本身更重要，新颖不新颖取决于你的知识储备。

比如首先最重要的肯定是理解ES本身是做什么的，其次对于ES中索引、文档等的各种查询方式：

- 如果学习过MongoDB，对这种文档型数据库的概念必然不会陌生。

- 如果学习过 MySQL，对各种查询方式必然不会陌生（v8.X的SQL操作）

- 那么 ES 其实不就是将这些概念拼凑一下，形成了一个新的东西。（其实也不新）

计算机的学习速度主要取决于自己的知识储备，当你储备足够多的时候，就可以快速上手新的技术。如果有前面两个的知识储备，对 ES 的基本操作大概看一眼就能掌握的差不多了。

至于 ES 的具体语法肯定不是硬记下来，要么是平时用多了自然而然的记住，要么就是边翻文档边用。

## 入门 

Elasticsearch是基于Java开发的，所以本身也支持通过Java API的方式对Elasticsearch进行访问。但是Elasticsearch也提供RESTful风格的访问。

### 概念

Elasticsearch与MySQL概念类比：Index <--> Database。Type（类型） <--> Table（表），Document（文档） <--> Row（行），Fields（字段） <--> Colunm（列）。

但是在Elasticsearch7.X中，Type概念被移除。再6.X中，一个Index下只能包含一个Type。

原因是Elasticsearch是全文索引，使用的是倒排索引。如果有Type的概念就与全文索引相违背了。如：

正排（正向）索引

```text
id     content
-----------------
1001   my name is zhang san
1002   my name is li si
```

倒排索引

```text
keyword   id
-------------
name      1001, 1002
zhang     1001
```

### 创建索引

```text
PUT http://localhost:9200/shopping

# 响应
{
    "acknowledged": true,
    "shards_acknowledged": true,
    "index": "shopping"
}
```

如果使用POST请求则会响应405状态码：`Incorrect HTTP method for uri [/shopping] and method [POST], allowed: [PUT, GET, HEAD, DELETE]`。

原因是PUT请求具有幂等性，即多次请求的结果都是一样的。而POST请求不具备这样的特性。

### 查询&删除索引

查询指定索引

```text
GET http://localhost:9200/shopping

# 响应
{
    "shopping": {
        "aliases": {},
        "mappings": {},
        "settings": {
            "index": {
                "creation_date": "1693876997352",
                "number_of_shards": "1",
                "number_of_replicas": "1",
                "uuid": "ZvPwPpLITIex0M5o_k4wIg",
                "version": {
                    "created": "7080099"
                },
                "provided_name": "shopping"
            }
        }
    }
}
```

查询所有索引

```text
GET http://localhost:9200/_cat/indices?v

# 响应
health status index    uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   shopping ZvPwPpLITIex0M5o_k4wIg   1   1          0            0       208b           208b
```

删除指定索引

```text
DELETE http://localhost:9200/shopping

# 响应
{
    "acknowledge": true
}
```

### 创建文档

POST请求

```text
POST http://localhost:9200/shopping/_doc
{
    "title": "小米",
    "price": 1999.00
}

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "Mkn-YooBgxu02iafh13S",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 1
}
```

PUT请求

```text
PUT http://localhost:9200/shopping/_doc/1002
{
    "title": "小米1",
    "price": 1999.00
}

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1002",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 3,
    "_primary_term": 1
}
```

如需使用PUT请求，则需要在URL后指定id。默认使用POST请求，每次会随机生成一个id。

而请求路径中的`_doc`表示文档数据的意思。

### 查询文档

指定查询（id）

```text
GET http://localhost:9200/shopping/_doc/1002

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1002",
    "_version": 1,
    "_seq_no": 3,
    "_primary_term": 1,
    "found": true,
    "_source": {
        "title": "小米1",
        "price": 1999.00
    }
}
```

查询所有

```text
GET http://localhost:9200/shopping/_search

{
    "took": 3,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },

    # 命中的结果
    "hits": {
        "total": {
            "value": 3,
            "relation": "eq"
        },
        "max_score": 1.0,
        "hits": [
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "Mkn-YooBgxu02iafh13S",
                "_score": 1.0,
                "_source": {
                    "title": "小米",
                    "price": 1999.00
                }
            },
            ......
        ]
    }
}
```

### 修改&删除文档

全量更新

```text
PUT http://localhost:9200/shopping/_doc/1001
{
    "title": "小米",
    "price": 2999.00
}

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1001",
    "_version": 3,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 4,
    "_primary_term": 1
}
```

局部更新

`_update`表明是一个更新操作。

```text
POST http://localhost:9200/shopping/_update/1001
{
    "doc": {
        "title": "华为"
    }
}

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1001",
    "_version": 4,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 5,
    "_primary_term": 1
}
```

删除文档

```text
DELETE http://localhost:9200/shopping/_doc/1001

# 响应
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1001",
    "_version": 5,
    "result": "deleted",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 6,
    "_primary_term": 1
}
```

### 条件&分页&排序查询

GET请求也能接收请求体，但实际生产中并不会这么做。

条件查询（请求路径携带查询条件）

```text
GET http://localhost:9200/shopping/_search?q=title:小米

# 响应
{
    "took": 2,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 2,
            "relation": "eq"
        },
        "max_score": 0.28363907,
        "hits": [
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "Mkn-YooBgxu02iafh13S",
                "_score": 0.28363907,
                "_source": {
                    "title": "小米",
                    "price": 1999.00
                }
            },
            ......
        ]
    }
}
```

条件查询（请求体携带查询条件）

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match": {
            "title": "小米"
        }
    }
}
```

全量查询

```text
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_all": {

        }
    }
}
```

分页查询

> from的公式 =（页码 - 1） * size

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_all": {
        }
    },
    "from": 0,
    "size": 2
}
```

查询并展示指定字段

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_all": {
        }
    },
    "from": 0,
    "size": 2,
    "_source": ["title"]
}
```

查询排序

> ASC：升序；DESC：降序

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_all": {
        }
    },
    "from": 0,
    "size": 2,
    "_source": ["title"],
    "sort": {
        "price": {
            "order": "asc"
        }
    }
}
```

### 多条件&范围查询

多条件查询（must）

> must等同于 and，should等同于 or

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "title": "华为"
                    }
                },
                {
                    "match": {
                        "price": 3999.00
                    }
                }
            ]
        }
    }
}

# 响应
{
    "took": 2,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 1,
            "relation": "eq"
        },
        "max_score": 3.1189923,
        "hits": [
            ......
        ]
    }
}
```

多条件查询（or）

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "bool": {
            "should": [
                {
                    "match": {
                        "title": "华为"
                    }
                },
                {
                    "match": {
                        "title": "小米"
                    }
                }
            ]
        }
    }
}
```

范围查询

> should 和 filter 一起使用，会出现should之外的数据，满足filter，但不满足should，即filter和should语句组合查询，会导致should语句失效
>
> should 在与must或者filter同级时，默认是不需要满足should中的任何条件的
>
> 解决方案：1、添加参数 "minimum_number_should_match": 1 这个设置should语句的满足条件值；2、将should嵌在must语句中。解决方案请移步至附录。

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "bool": {
            "should": [
                {
                    "match": {
                        "title": "华为"
                    }
                },
                {
                    "match": {
                        "title": "小米"
                    }
                }
            ],
            "filter": {
                "range": {
                    "price": {
                        "gt": 2000
                    }
                }
            }
        }
    }
}
```

### 全文检索&完全匹配&高亮查询

ES会将数据文字进行分词拆解操作，并将拆解后的数据保存到倒排索引中，这样即使使用文字的一部分，也能将数据查询。这种方式称为全文检索。

全文检索（match）

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match": {
            "title": "小华"
        }
    }
}
```

全文检索会将"小华"拆解为"小"，"华"进行匹配。每一个文字都作为独立的个体进行匹配。

完全匹配（match_phrase）

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_phrase": {
            "title": "小华"
        }
    }
}
```

完全匹配会将"小华"作为整体进行匹配。

高亮查询

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    "query": {
        "match_phrase": {
            "title": "华为"
        }
    },
    "highlight": {
        "fields": {
            "title": {}
        }
    }
}

# 响应
{
    "took": 8,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 1,
            "relation": "eq"
        },
        "max_score": 2.1189923,
        "hits": [
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "1002",
                "_score": 2.1189923,
                "_source": {
                    "title": "华为",
                    "price": 3999.00
                },
                "highlight": {
                    "title": [
                        "<em>华</em><em>为</em>"
                    ]
                }
            }
        ]
    }
}
```

高亮查询就是将需要高亮显示的字段添加特殊的样式。

### 聚合查询

> 只列举两个常见聚合操作，更多聚合操作查询请参考官网：https://www.elastic.co/guide/en/elasticsearch/reference/8.9/search-aggregations.html

```text
# POST http://localhost:9200/shopping/_search
GET http://localhost:9200/shopping/_search
{
    // 聚合操作
    "aggs": {
        // 聚合名称，任意
        "price_group": {
            // 分组
            "terms": {
                // 聚合字段
                "field": "price"
            }
        },
        "price_avg": {
            // 平均值
            "avg": {
                "field": "price"
            }
        }
    }
}
```

响应

```text
{
    "took": 11,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 4,
            "relation": "eq"
        },
        "max_score": 1.0,
        "hits": [
            ......
        ]
    },
    # 聚合查询
    "aggregations": {
        "price_group": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
                {
                    "key": 1999.0,
                    "doc_count": 2
                },
                {
                    "key": 2999.0,
                    "doc_count": 1
                },
                {
                    "key": 3999.0,
                    "doc_count": 1
                }
            ]
        },
        "price_avg": {
            "value": 2749.0
        }
    }
}
```

### 映射关系

限定field的类型及长度等信息。

对于已经存在的映射字段，我们不能更新，更新必须创建新的索引进行数据迁移。

如果对已存在的index进行修改mapping，相对比较复杂。以下演示create时的映射关系。

```text
PUT http://localhost:9200/new_shopping/_search
{
    "mappings": {
        "properties": {
            "title": {
                # 全文检索
                "type": "text",
                "index": true
            },
            "name": {
                # 完全匹配
                "type": "keyword",
                "index": true
            },
            "price": {
                "type": "double",
                "index": false
            }
        }
    }
}
```

## 进阶

### 集群环境搭建

单台Elasticsearch服务器对外提供服务，往往都会有最大的负载能力，超过这个阈值，服务器性能就会大打折扣甚至不可用，所以生产环境中，一般都是以集群环境运行Elasticsearch。

除了负载能力，单机服务器也存在其他问题：

- 单台机器存储容量有限。

- 单服务器容易出现单点故障，无法实现高可用。

- 单服务的并发处理能力有限。

配置服务器集群时，集群中节点数量没有限制，大于等于两个节点就可以看作是集群。一般从高性能及高可用考虑的话，集群节点数都是3个以上。

集群Cluster：一个Elasticsearch集群有一个唯一标识，默认`elasticsearch`。

节点Node：一个节点就是集群中的一个服务器，作为集群的一部分，它存储数据，且参与 集群的索引和搜索功能。节点也有一个唯一标识。

#### Windows

node-1节点配置

```yaml
# Use a descriptive name for your cluster:
cluster.name: my-application
# Use a descriptive name for the node:
node.name: node-1
node.master: true
node.data: true
# ----------------------------------- Memory -----------------------------------
#bootstrap.memory_lock: true
network.host: localhost
# Set a custom port for HTTP:
http.port: 9201
transport.tcp.port: 9301
# 跨域配置 
http.cors.enabled: true
http.cors.allow-origin: "*"
```

node-2节点配置

```yaml
#节点 2 的配置信息：
#集群名称，节点之间要保持一致
cluster.name: my-elasticsearch
#节点名称，集群内要唯一
node.name: node-2
node.master: true
node.data: true
#ip 地址
network.host: localhost
#http 端口
http.port: 9202
#tcp 监听端口
transport.tcp.port: 9302
discovery.seed_hosts: ["localhost:9301"]
discovery.zen.fd.ping_timeout: 1m
discovery.zen.fd.ping_retries: 5
#集群内的可以被选为主节点的节点列表
#cluster.initial_master_nodes: ["node-1", "node-2","node-3"]
#跨域配置
#action.destructive_requires_name: true
http.cors.enabled: true
http.cors.allow-origin: "*"
```

node-3节点配置

```yaml
#节点 3 的配置信息：
#集群名称，节点之间要保持一致
cluster.name: my-elasticsearch
#节点名称，集群内要唯一
node.name: node-3
node.master: true
node.data: true
#ip 地址
network.host: localhost
#http 端口
http.port: 9203
#tcp 监听端口
transport.tcp.port: 9303
#候选主节点的地址，在开启服务后可以被选为主节点
discovery.seed_hosts: ["localhost:9301", "localhost:9302"]
discovery.zen.fd.ping_timeout: 1m
discovery.zen.fd.ping_retries: 5
#集群内的可以被选为主节点的节点列表
#cluster.initial_master_nodes: ["node-1", "node-2","node-3"]
#跨域配置
#action.destructive_requires_name: true
http.cors.enabled: true
http.cors.allow-origin: "*"
```

依次启动node-1、node-2、node-3。

```text
GET http://localhost:9201/_cluster/health

# 响应
{
    "cluster_name": "my-application",
    "status": "green",
    "timed_out": false,
    "number_of_nodes": 3,
    "number_of_data_nodes": 3,
    "active_primary_shards": 0,
    "active_shards": 0,
    "relocating_shards": 0,
    "initializing_shards": 0,
    "unassigned_shards": 0,
    "delayed_unassigned_shards": 0,
    "number_of_pending_tasks": 0,
    "number_of_in_flight_fetch": 0,
    "task_max_waiting_in_queue_millis": 0,
    "active_shards_percent_as_number": 100.0
}
```

status字段有三种取值（表示当前集群在总体上是否工作正常）：

1. green：所有的主分片和副本分片都正常运行。

2. yellow：所有的主分片都正常运行，但不是所有的副本分片都正常运行。

3. red：有主分片没能正常运行。

#### Linux

1. 解压

```shell script
tar -zxvf elasticsearch-7.8.0-linux-x86_64.tar.gz

# 重命名
# mv elasticsearch-7.8.0 es
```

2. 创建用户

因为安全问题，Elasticsearch不允许root用户直接运行，所以需要创建用户

```shell script
useradd es
passwd es

# 文件夹所有者
chown -R es:es /mydata/es
```

3. 修改配置文件

```shell script
vim /mydata/es/config/elasticsearch.yml
# 加入如下配置
#集群名称
cluster.name: cluster-es
#节点名称， 每个节点的名称不能重复
node.name: node-1
#ip 地址， 每个节点的地址不能重复
network.host: 192.168.44.154
#是不是有资格主节点
node.master: true
node.data: true
http.port: 9200
# head 插件需要这打开这两个配置
http.cors.allow-origin: "*"
http.cors.enabled: true
http.max_content_length: 200mb
#es7.x 之后新增的配置，初始化一个新的集群时需要此配置来选举 master
cluster.initial_master_nodes: ["node-1"]
#es7.x 之后新增的配置，节点发现
discovery.seed_hosts: ["192.168.44.154:9300","192.168.44.155:9300","192.168.44.156:9300"]
gateway.recover_after_nodes: 2
network.tcp.keep_alive: true
network.tcp.no_delay: true
transport.tcp.compress: true
#集群内同时启动的数据任务个数，默认是 2 个
cluster.routing.allocation.cluster_concurrent_rebalance: 16
#添加或删除节点及负载均衡时并发恢复的线程个数，默认 4 个
cluster.routing.allocation.node_concurrent_recoveries: 16
#初始化数据恢复时，并发恢复线程的个数，默认 4 个
cluster.routing.allocation.node_initial_primaries_recoveries: 16

vim /etc/security/limits.conf
# 在文件末尾中增加下面内容
# 每个进程可以打开的文件数的限制
es soft nofile 65536
es hard nofile 65536

vim /etc/security/limits.d/20-nproc.conf
# 在文件末尾中增加下面内容
# 每个进程可以打开的文件数的限制
es soft nofile 65536
es hard nofile 65536
# 操作系统级别对每个用户创建的进程数的限制
* hard nproc 4096
# 注： * 代表 Linux 所有用户名称

vim /etc/sysctl.conf
# 在文件中增加下面内容
# 一个进程可以拥有的 VMA(虚拟内存区域)的数量,默认值为 65536
vm.max_map_count=655360

# 重新加载
sysctl -p
```

4. 启动elasticsearch

```shell script
# 启动前需要切换用户，
su es

cd /mydata/es/

bin/elasticsearch
# 后台启动
bin/elasticsearch -d
```

在每一个es节点所处的服务器上都执行1~4步骤，其中步骤3能够通过文件分发，避免手动更改每一台服务器。但是还需要手动修改es节点的`network.host`。

集群搭建成功后访问`http://192.168.44.154:9200/_cat/nodes`，有响应说明搭建成功。

### 核心概念

#### 索引（Index）

索引能够关联数据。

一个索引就是多个文档的集合。能搜索的数据必须索引，这样的好处是可以提高查询速度。

Elasticsearch索引的精髓：一切设计都是为了提高搜索的性能。

#### 类型（Type ）

Elasticsearch 7.X版本中默认不再支持自定义索引类型，默认类型为`_doc`。

#### 文档（Document）

一个文档是一个可被索引的基础信息单元，即一条数据。文档以JSON格式表示。

在一个 index/type 中，可以存储任意多的文档。

#### 字段（Field）

类比为MySQL中Table的字段。

#### 映射（mapping）

mapping是处理数据的方式和规则方面做一些限制。如某个字段的数据类型、默认值、分析器、是否被索引等。

需要考虑如何建立映射才能对性能更好。

#### 分片（shards）

可以理解为Redis中的集群分片，MySQL中的分表。其思想就是将一个超大文件，分别存储到集群中的多个节点中，避免数据集中导致响应慢。所有分片合在一起就是完整的数据。

分片可以扩展容量、也能够在分片上进行分布式的、并行的操作，进而提高性能/吞吐量。（负载均衡）

每个分片本身也是一个功能完善并且独立的索引。

一个Lucene索引在Elasticsearch被称作分片。一个Elasticsearch索引是分片的集合。当Elasticsearch在索引中搜索的时候，它发送查询到每一个属于索引的分片（Lucene 索引），然后合并每个分片的结果到一个全局的结果集。

优先保证主分片的数据存储，然后副本分片和主分片进行同步。

#### 副本（Replicas）

Elasticsearch中的副本机制简单可以理解为主从复制。允许对数据进行拷贝。分片的副本。类比为Kafka的partition和replication。

在分片/节点故障的情况下，提供高可用。且副本一般不与master分片置于同一节点上。

扩展吞吐量。因为搜索可以在所有的副本上并行运行。

当分片所在的机器宕机时,Elasticsearch可以使用其副本进行恢复,从而避免数据丢失。

#### 分配（allocation）

将分片分配给节点的过程，包括分配主分片或副本。如果是副本，还包含从主分片复制数据的过程。这个过程是由master节点完成的。

### Elasticsearch架构

![](https://github.com/Doing-code/guide/blob/main/image/ElasticSearch架构.png)

### 分片集群

```text
PUT http://127.0.0.1:1001/users
{
    "settings" : {
        "number_of_shards" : 3,
        "number_of_replicas" : 1
    }
}
```

如上表示，创建名为users的索引，并且将分配3个主分片和一份副本，即每个主分片都拥有一个副本分片。

### 故障转移

单节点集群会有单点故障的问题。

当部署多节点组成集群后，当集群中任一节点出现故障，都不会导致集群的不可用，可以从另一个节点中获取数据。既可以从主分片又可以从副本分片上获得文档。

故障转移可以理解为当master节点不可用时，重新选举新的master节点对外提供服务。保证集群的高可用性。

当主节点宕机后，为了保持整个集群的可用性，从节点将会自动切换成为新的主节点。这个过程称为故障转移。

### 水平扩容

扩容针对的是副本，主分片的数量再索引创建时就确定了。分片数量定义了这个索引能够存储的最大数据量。读操作（搜索）和返回数据可以同时被主分片和副本分片所处理。

当集群中再添加一个节点，集群会为了分散负载而对分片进行重新分配。

```text
PUT http://127.0.0.1:1001/users/_settings
{
    "number_of_replicas" : 2
}
```

动态将索引users扩容为副本分片为2，即每个主分区都有两个副本分区。

更多的副本分片数提高了数据冗余量，可以保证在失去n-1个节点的情况下不丢失数据。（n为副本数）

### 应对故障

当集群中master节点故障下线，集群中其它节点会选举出一个新的master节点对外提供服务。当故障节点恢复后重启，作为从节点。如果故障节点恢复后依然拥有着之前的分片，它将尝试去重用它们，同时仅从主分片复制发生了修改的数据文件。

需要在配置文件中添加发现集群的配置：

```yaml
discovery.seed_hosts: ["192.168.44.155:9302", "192.168.44.156:9302"]
```

### 路由计算&分片控制

路由计算：决定文档数据被存储在哪个分片中。计算规则`shard = hash(routing) % number_of_primary_shards`，routing 是一个可变值，默认是文档的 _id ，也可以设置成一个自定义的值。number_of_primary_shards时主分片的数量。

分片控制：可以理解为负载均衡，默认分片控制是轮询。

且文档API可以接收routing路由参数可以通过该参数指定存储分片。

### 数据写流程

> 这部分流程可以类比Kafka的副本同步（Leader、Follower）。

1. 客户端请求集群任意节点，集群任意节点都是协调节点。

2. 协调节点通过路由计算得到应该存储到指定主分片上，并将请求转发到指定主分片所在的节点。

3. 由主分片处理数据存储等操作。

4. 主分片需要将数据发送给副本分片，完成副本备份。

5. 副本完成备份后，ack响应。

6. 主分片收到副本分片的ack后，将执行结果响应给客户端。

在客户端收到成功响应时，文档变更已经在主分片和所有副本分片中同步完成，变更是安全的。

新建、索引和删除请求都是写操作，必须要在主分片上执行操作后，才能被副本分片拷贝。

### 数据读流程

1. 客户端请求集群任意节点，集群任意节点都是协调节点。

2. 协调节点通过路由计算得到所在的分片以及所有副本。

3. ES通过负载均衡，轮询所有节点，并将请求转发到指定节点。

4. 节点返回查询结果，将结果响应给客户端。

### 更新流程&批量操作流程

![](https://github.com/Doing-code/guide/blob/main/image/es更新流程.png)

更新流程：

1. 客户端向集群中A节点发送更新请求。

2. A节点通过路由计算确定主分片所在的C节点。

3. C节点从主分片中检索文档，修改`_source`字段中的JSON，并且尝试重新索引主分片的文档，如果文档已经被另一个进程修改，那么C节点会继续重新索引主分片的文档，超过`retry_on_conflict`次后放弃。

4. 如果C节点成功地更新文档，它会将新版本的文档并行同步到所有的副本分片上，重新建立索引。一旦所有副本分片都响应成功，那么C节点向A节点（协调节点）响应成功，A节点向客户端响应成功。

当主分片把更改转发到副本分片时，它不会转发更新请求。相反，它转发完整文档的新版本。请记住，这些更改将会异步转发到副本分片，并且不能保证它们以发送它们相同的顺序到达。

批量操作流程：

mget和 bulk API的模式类似于单文档模式。区别在于协调节点知道每个文档存在于哪个分片中。它将多文档请求分解成每个分片的多文档请求，并且将这些请求并行转发到每个参与节点。

协调节点一旦收到来自每个节点的应答，就将每个节点的响应收集整理成单个响应，返回给客户端。

而这个批量操作，可以理解成Redis的pipeline，一次网络IO发送多个文档命令，节省网络资源的开销。

mget工作模式：

![](https://img-blog.csdnimg.cn/img_convert/b90ea9c79138d8361ca339cff205fdb0.png)

1. 客户端向集群中A节点发送mget请求。

2. A节点为每个分片构建多文档获取请求，然后并行转发这些请求到所需的主分片或者副本分片的节点上。一旦收到所有的答复，A节点构建响应并返回给客户端。

可以对docs数组中每个文档设置routing参数。

bulk API工作模式：允许在单个批量请求中执行多个创建、索引、删除和更新请求

![](https://img-blog.csdnimg.cn/img_convert/83499315a7b8ab81471a88f3e142f0a8.png)

1. 客户端向集群中A节点发送bulk请求。

2. A节点为每个节点创建一个批量请求，并将这些请求并行转发到每个包含主分片的节点上。

3. 主分片顺序执行每个操作，当每个操作执行成功时，主分片就会并行转发新文档到副本分片，然后再执行下一个操作。一旦所有的副本分片都响应成功，该节点则会向A节点响应成功，最后A节点将结构收集并整理响应给客户端。

### 倒排索引

```text
1001   my name is Tom

name 1001
Tom 1001
```

分词器

词条：索引中最小存储和查询单元。

词典：词条的集合，常见数据结构为B+tree、HashMap

倒排表：一个包含所有不重复词条的排序列表，然后列出每个词条出现在哪个文档。每个记录称为倒排项。

倒排索引搜索过程就是先去词典中找到要搜索的词条是否存在，如果存在则在倒排表中找到词条对应的指针。通过倒排表获取文档id，找到id就能找到内容。

### 文档搜索

早期的全文索引会为整个文档建立一个很大的倒排索引，并将其写入到磁盘中。一旦新的索引就绪，旧的就会被其替换，这样最近的变化便可以被检索到。

好处是写入倒排索引允许被压缩、能够读取文件系统缓存，避免直接命中磁盘...；不好的地方是不可变，如果让一个新的文档可被搜索，需要重建整个索引。

那么如何在保留不可变性的前提下实现倒排索引的更新呢？

答案是通过增加新的补充索引来反映变更，而不是直接重写整个倒排索引。每一个倒排索引都会被轮流查询到，最后再对结果进行合并。

Elasticsearch是基于Lucene，Lucene引入了按段搜索的概念。即每一个段本身都是一个倒排索引。索引在Lucene中除了表示所有段的集合外，还增加了提交点的概念，一个列出了所有已知段的文件。

按段搜索会以如下流程执行：

1. 新文档被收集到内存索引缓存。

2. 缓存被提交，一个新的段被写入磁盘。一个新的包含新段的提交点被写入磁盘。磁盘进行同步，所有在文件系统缓存中等待的写入都刷新到磁盘，以确保它们被写入物理文件。

3. 新的段被开启，确保它包含的文档可被搜索。

4. 内存缓存被清空，等待接收新的文档。

当一个查询被触发时，所有已知的段按顺序被查询。词项统计会对所有段的结果进行聚合，以保证每个词和每个文档的关联都被准确计算。此方式可以用相对较低的成本将新文档添加到索引。

段因为是不可改变的，所以既不能从把文档从旧的段中移除，也不能修改旧的段来进行反映文档的更新。取而代之的是，每个提交点会包含一个`.del`文件，文件中会列出这些被删除文档的段信息。

当一个文档被删除，它实际上只是在`.del`文件中被标记删除（逻辑删除）。一个被标记删除的文档仍然可以被查询匹配到，但它会在最终结果被返回前从结果集中移除。

文档更新也是类似的操作方式：当一个文档被更新时，旧版本文档被标记删除，文档的新版本被索引到一个新的段中。可能两个版本的文档都会被一个查询匹配到，但被删除的那个旧版本文档在结果集返回前就已经被移除。

不仅有创建倒排索引的概念，也有合并倒排索引的概念。避免倒排索引太多导致查询效率慢。es在Segment数量达到一定得临界点会自动force_merge。需要考虑实际得生产情况，避免极大得影响查询得效率。

执行force_merge操作，手动释放磁盘空间；在merge的过程中会占用极大的cpu，io资源，尽量选择系统低消费时间段force_merge。

### 文档刷新&刷写&合并

#### 文档刷新

![](https://img-blog.csdnimg.cn/img_convert/521c25f0f16247240234d1b8eb3c5f25.png)

> 提交（commit）一个新的段到磁盘需要一个fsync来确保段被物理性地写入磁盘

Lucene允许新段被写入和打开，使其包含的文档在未进行一次完整提交时便对搜索可见。并且在不影响性能的前提下可以被频繁地执行。

在Elasticsearch中，写入和打开（只要文件已经在文件系统缓存中，就可以像其它文件一样被打开和读取）一个新段的轻量过程叫做refresh。默认情况下每个分片会每秒自动刷新一次。Elasticsearch是近实时搜索，因为文档的变化并不是立即对搜索可见，但会在一秒之内变为可见。

手动刷新API：`/usersl_refresh`。

并不是所有的情况都需要每秒刷新。可能你正在使用Elasticsearch索引大量的日志文件。你可能想优化索引速度而不是近实时搜索，可以通过设置refresh_interval ，降低每个索引的刷新频率。

```text
{
    "settings": {
    	"refresh_interval": "30s"
    }
}
```

refresh_interval可以在索引上进行动态更新。

```text
# 关闭自动刷新
PUT /users/_settings
{ 
    "refresh_interval": -1 
}

# 每一秒刷新
PUT /users/_settings
{ 
    "refresh_interval": "1s" 
}
```

#### 文档刷写

> // MySQL是先写内存（Buffer pool），再写redo log buffer，再刷盘到redo log file。（存疑：MySQL先写入日志，再写入内存）
>
> Write Ahead Log策略，MySQL 是先写redo log（由Buffer pool负责写入redo log buffer，再刷盘到redo log file），再修改内存数据页。
>
> Redis AOP 刷盘流程：默认的，写命令执行完先放入AOF缓冲区，然后每隔1秒将缓冲区数据写到AOF文件
>
> ES是先写入内存，在写入日志

数据变化持久化到磁盘。在动态更新索引时，一次完整的提交会会将段刷写到磁盘，并写入一个包含所有段列表的提交点。Elasticsearch在启动或重新打开一个索引的过程中使用这个提交点来判断哪些段属于当前分片。

但是在文件被fsync（默认30分钟，但是es在新版本中是按照大小进行刷写）到磁盘前，被写入的文件在重启之后就会丢失。怎么办？不希望丢掉这些数据。Elasticsearch增加了一个translog（事务日志），在每一次对Elasticsearch进行操作时均进行了日志记录。

持久化流程：

1. 一个文档被索引之后，就会被添加到内存缓冲区，并且追加到了 translog。

2. 默认每秒将内存缓冲区的文档被写入到一个新的段中，刷写到文件系统缓存中。内存缓冲区会被清空。

3. 更多的文档被添加到内存缓冲区和追加到事务日志。

4. 每隔30分钟索引或者translog足够大时文件系统缓存被刷新（flush）到磁盘，旧的translog会被删除。并且一个提交点被写入磁盘。

translog提供数据还没有被刷到磁盘前的事务持久化。当es启动时，它会从磁盘中使用最后一个提交点去恢复已知的段，并且会恢复translog中所有在最后一次提交后发生的变更操作。

默认translog是每5秒被fsync刷新到硬盘，或者在每次写请求完成之后执行（e.g. index, delete, update, bulk）。translog 也被用来提供实时CRUD。当你试着通过ID查询、更新、删除一个文档，它会在尝试从相应的段中检索之前，首先检查translog任何最近的变更。这意味着它总是能够实时地获取到文档的最新版本。

但这个过程在主分片和复制分片都会发生。这意味着在整个请求被fsync到主分片和复制分片的translog之前，你的客户端不会得到一个200 OK响应。

#### 段合并

> force_merge

段合并的时候会将那些旧的已删除文档从文件系统中清除。被删除的文档（或被更新文档的旧版本）不会被拷贝到新的大段中。

当索引的时候，刷新（refresh）操作会创建新的段并将段打开以供搜索使用。

合并段时会选择一小部分大小相似的段，并且在后台将它们合并到更大的段中。这并不会中断索引和搜索。

一旦合并结束，老的段被删除。

### 文档分析

分析流程：

1. 将一块文本分成适合于倒排索引的独立的词条。

2. 将这些词条统一化为标准格式以提高它们的"可搜索性"。

而分析器就是用来处理分析流程的（一个分析器就是在一个包里面组合了三种函数的一个包装器，三种函数按照顺序被执行）：

1. 字符过滤器：首先，字符串按顺序通过每个 字符过滤器 。他们的任务是在分词前整理字符串。一个字符过滤器可以用来去掉 HTML，或者将 & 转化成 and。

2. 分词器：其次，字符串被分词器分为单个的词条。一个简单的分词器遇到空格和标点的时候，可能会将文本拆分成词条。

3. token 过滤器：最后，词条按顺序通过每个 token 过滤器 。这个过程可能会改变词条（例如，小写化Quick ），删除词条（例如， 像 a， and， the 等无用词），或者增加词条（例如，像jump和leap这种同义词）。

可以使用Elasticsearch内置的分析器，也可以使用插件IK分词器（中文分词器）。并且能够扩展词汇，在某些场景下需要将你认为的词语看作整体，则可以进行扩展词汇。

也可以自定义分析器：

```text
# 创建索引
#PUT http://localhost:9200/my_index

{
    "settings": {
        "analysis": {
            "char_filter": {
                "&_to_and": {
                    "type": "mapping", 
                    "mappings": [
                        "&=> and "
                    ]
                }
            }, 
            "filter": {
                "my_stopwords": {
                    "type": "stop", 
                    "stopwords": [
                        "the", 
                        "a"
                    ]
                }
            }, 
            "analyzer": {
                "my_analyzer": {
                    "type": "custom", 
                    "char_filter": [
                        "html_strip", 
                        "&_to_and"
                    ], 
                    "tokenizer": "standard", 
                    "filter": [
                        "lowercase", 
                        "my_stopwords"
                    ]
                }
            }
        }
    }
}

# 测试自定义分析器
# GET http://127.0.0.1:9200/my_index/_analyze
{
    "text":"The quick & brown fox",
    "analyzer": "my_analyzer"
}

# 响应
{
    "tokens": [
        {
            "token": "quick",
            "start_offset": 4,
            "end_offset": 9,
            "type": "<ALPHANUM>",
            "position": 1
        },
        {
            "token": "and",
            "start_offset": 10,
            "end_offset": 11,
            "type": "<ALPHANUM>",
            "position": 2
        },
        {
            "token": "brown",
            "start_offset": 12,
            "end_offset": 17,
            "type": "<ALPHANUM>",
            "position": 3
        },
        {
            "token": "fox",
            "start_offset": 18,
            "end_offset": 21,
            "type": "<ALPHANUM>",
            "position": 4
        }
    ]
}
```

### 文档控制

es采用乐观锁（CAS）解决并发问题，更新失败则重试更新、使用新数据、将失败原因反馈给用户等。

而GET和DELETE请求时，每个文档都有一个递增的版本号（_version），当文档被修改时版本号递增。Elasticsearch使用这个version号来确保变更以正确顺序得到执行。如果旧版本的文档在新版本之后到达，它可以被简单的忽略。

我们可以利用version号来确保应用中相互冲突的变更不会导致数据丢失。我们通过指定想要修改文档的version号来达到这个目的。如果该版本不是当前版本号，我们的请求将会失败。

在es新版本中，需要在请求信息张添加`if_seq_no=0&if_primary_term=0`参数，用于版本号修改。

### 文档展示&Kibana

## 优化

### 硬件选择

> RAID > SSD > 机械硬盘

Elasticsearch 的基础是 Lucene，所有的索引和文档数据是存储在本地的磁盘中，如果使用多块硬盘，Elasticsearch允许通过多个path data目录配置把数据条带化分配到它们上面。

```yaml
# 到存储数据的目录的路径（以逗号分隔多个位置）
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: /path/to/data
#
# Path to log files:
#
path.logs: /path/to/logs
```

### 分片策略

分片和副本的设计为 ES 提供了支持分布式和故障转移的特性，但并不意味着分片和副本是可以无限分配的。而且索引的分片完成分配后由于索引的路由机制，我们是不能重新修改分片数的。

一个分片是有代价的：

1. 一个分片的底层即为一个 Lucene 索引，会消耗一定文件句柄、内存、以及 CPU运转。

2. 每一个搜索请求都需要命中索引中的每一个分片，如果每一个分片都处于不同的节点还好， 但如果多个分片都需要在同一个节点上竞争使用相同的资源就有些糟糕了。

3. 用于计算相关度的词项统计信息是基于分片的。如果有许多分片，每一个都只有很少的数据会导致很低的相关度。

#### 合理设置分片数

一个业务索引具体分配多少分配需要架构师和开发人员对业务的增长有预先的判断，横向扩展应当分阶段进行，为下一阶段准备好足够的资源。一般来说，我们遵循如下一些原则：

1. 控制每个分片占用的硬盘容量不超过ES的最大JVM的堆空间设置（ES的JVM的堆配置一般设置不超过32G）。因此，如果索引的总容量在 500G 左右，那分片大小在 16 个左右即可；还需要同时考虑原则2。

2. 考虑一下 node 数量，一般一个节点有时候就是一台物理机，如果分片数过多，大大超过了节点数，很可能会导致一个节点上存在多个分片，一旦该节点故障，即使保持了 1 个以上的副本，同样有可能会导致数据丢失，集群无法恢复。所以， 一般都设置分片数不超过节点数的 3 倍。

3. 主分片，副本和节点最大数之间数量的分配关系：节点数 <= 主分片数 * (副本数 + 1)

#### 推迟分片分配

而对于节点瞬时中断的问题，默认情况下，集群会等待一分钟来查看节点是否会重新加入，如果这个节点在此期间重新加入，重新加入的节点会保持其现有的分片数据，不会触发新的分片分配。这样就可以减少 ES 在自动再平衡可用分片时所带来的极大开销。

过修改参数delayed_timeout，可以延长再均衡的时间。可以在全局设置也可以在索引级别进行修改：

```text
#PUT /_all/_settings
{
	"settings": {
		"index.unassigned.node_left.delayed_timeout": "5m"
	}
}
```

### 路由选择

> 路由公式：shard = hash(routing) % number_of_primary_shards。routing 默认值是文档的 id，也可以采用自定义值，比如用户 id。

如果不带routing查询，在查询的时候因为不知道查询的数据具体在哪个分片上，所以整个查询过程分为2个步骤：

1. 分发：请求到达协调节点后，协调节点将查询请求分发到每个分片上。

2. 聚合：协调节点搜集到每个分片上查询结果，在将查询的结果进行排序，之后给用户返回结果。

如果带routing查询，可以直接根据routing信息定位到某个分片查询，不需要查询所有的分片。如果routing设置为userid的话，就可以直接查询出数据来，效率提升很多。

### 写入优化

ES 的默认配置，是综合了数据可靠性、写入速度、搜索实时性等因素。所以在实际生产中，需要根据业务需求，进行偏向化的优化。

针对于搜索性能要求不高，但是对写入要求较高的场景，我们需要尽可能的选择恰当写优化策略。综合来说，可以考虑以下几个方面来提升写索引的性能：

1. 增加Translog Flush间隔，目的是降低磁盘每秒的输入输出，以及写锁操作。

  - Flush 的主要目的是把文件缓存系统中的段持久化到硬盘，当Translog的数据量达到512MB或者30分钟时，会触发一次 Flush。
  
  - index.translog.flush_threshold_size参数的默认值是512MB。可以进行修改。
  
  - 增加参数值意味着文件缓存系统中可能需要存储更多的数据，所以我需要为操作系统的文件缓存系统留下足够的空间。

2. 增加Index Refresh间隔，目的是减少Segment Merge的次数。

  - Lucene 在新增数据时，采用了延迟写入的策略，默认情况下索引的refresh_interval 为1 秒。
  
  - Lucene 将待写入的数据先写到内存中，超过1秒（默认）时就会触发一次Refresh，然后Refresh会把内存中的的数据刷新到操作系统的文件缓存系统中。
  
  - 如果对搜索的实效性要求不高，可以将Refresh周期延长，例如 30 秒。这样还可以有效地减少段刷新次数，但这同时意味着需要消耗更多的Heap内存。

3. 调整Bulk（批量处理）线程池和队列。

4. 优化节点间的任务分布。负载均衡。

5. 优化Lucene层的索引建立，目的是降低CPU及IO。

6. 优化存储设备，使用更好的如SSD、RAID等磁盘。

7. 减少副本的数量：ES 为了保证集群的可用性，提供了Replicas（副本）支持，然而每个副本也会执行分析、索引及可能的合并过程，所以Replicas的数量会严重影响写索引的效率。

  - 当写索引时，需要把写入的数据都同步到副本节点，副本节点越多，写索引的效率就越慢。
  
  - 如果在进行大批量写入操作时，能容忍丢失数据的风险，可以设置`index.number_of_replicas: 0`关闭副本，禁止Replica备份。待写入完成后，Replica修改回正常的状态。
  
  - 如果关闭副本期间，主分区宕机了，那么部分数据将丢失。
  
8. 合理使用段合并。

### 内存设置

es默认设置的内存是1GB，在es配置文件中包含一个`jvm.option`文件，可以修改es的堆大小以及其它jvm参数。

es堆内存的分配需要满足如下两个原则：

> Lucene 的设计目的是把底层 OS 里的数据缓存到内存中。Lucene 的段是分别存储到单个文件中的，这些文件都是不会变化的，所以很利于缓存，同时操作系统也会把这些段文件缓存起来，以便更快的访问。

1. 不要超过物理内存的50%，如果我们设置的堆内存过大，Lucene可用的内存将会减少，就会严重影响降低Lucene的全文本查询性能。

2. 堆内存大小建议不要超过32G。这个和操作系统底层的指针有关系，直接记结论。

而最终的es堆配置为：

```text
-Xms 31g
-Xmx 31g
```

假设你有个机器有 128 GB 的内存，你可以创建两个节点，每个节点内存分配不超过 32 GB。也就是说不超过 64 GB 内存给 ES 的堆内存，剩下的超过 64 GB 的内存给 Lucene。

### 重要配置项

| 参数名                                | 参数值           | 描述                                                                                                                                       |
|:----------------------------------:|:-------------:| ---------------------------------------------------------------------------------------------------------------------------------------- |
| cluster.name                       | elasticsearch | 配置ES的集群名称，默认值是elasticsearch，建议改成与所存数据相关的名称，ES会自动发现在同一网段下的 集群名称相同的节点。                                                                     |
| node.name                          | node-1        | 集群中的节点名，在同一个集群中不能重复。节点 的名称一旦设置，就不能再改变了。当然，也可以 设 置 成 服 务 器 的 主 机 名 称 ， 例 如 node.name:${HOSTNAME}。                                         |
| node.master                        | true          | 指定该节点是否有资格被选举成为 Master 节点，默 认是 True，如果被设置为 True，则只是有资格成为 Master 节点，具体能否成为 Master 节点，需要通 过选举产生。                                           |
| node.data                          | true          | 指定该节点是否存储索引数据，默认为 True。数据 的增、删、改、查都是在 Data 节点完成的。                                                                                        |
| index.number_of_shards             | 1             | 设置都索引分片个数，默认是 1 片。也可以在创建 索引时设置该值，具体设置为多大都值要根据数据 量的大小来定。如果数据量不大，则设置成 1 时效 率最高                                                             |
| index.number_of_replicas           | 1             | 设置默认的索引副本个数，默认为 1 个。副本数越多，集群的可用性越好，但是写索引时需要同步的 数据越多。                                                                                     |
| transport.tcp.compress             | true          | 设置在节点间传输数据时是否压缩，默认为 False， 不压缩                                                                                                           |
| discovery.zen.minimum_master_nodes | 1             | 设置在选举Master节点时需要参与的最少的候选主节点数，默认为 1。如果使用默认值，则当网络不稳定时有可能会出现脑裂。 合理的数值为 (master_eligible_nodes/2)+1 ， 其 中 master_eligible_nodes 表示集群中的候选主节点数 |
| discovery.zen.ping.timeout         | 3s            | 设置在集群中自动发现其他节点时 Ping 连接的超时时间，默认为3秒。 在较差的网络环境下需要设置得大一点，防止因误判该节点的存活状态而导致分片的转移                                                              |

## 面试题

### 为什么要使用Elasticsearch?

在百万级别的数据库中，查询效率是很低的，如果查询的是文档，而业务中往往采用模糊查询，在这种情况下，即使建立索引也还是查询慢。甚至模糊匹配操作不当还会导致索引失效，进而全表扫描。

而将海量数据放到ES索引库中，可以提高查询速度。

### 请说明Elasticsearch的master选举流程?

- Elasticsearch的选举是由ZenDiscovery模块负责的，主要包含Ping（节点之间通过这个RPC组件来通信）和Unicast（单播模块包含一个主机列表以控制哪些节点需要ping通，充当集群发现）这两部分。

- 对所有可以成为master的节点（`node.master: true`）根据nodeId字典排序。每次选举时每个节点都会对自己已知的节点排序，然后选出第一个节点（第0位，暂且认为它是master节点。（通过node.name指定或者默认使用UUID的前7位作为实例的nodeId，nodeId会被持久化，不会随着实例重启而发生变化。）

- 如果对某个节点的投票数超过半数时（n/2 + 1），并且该节点也选举自己，那么这个节点就是master。否则重新选举直到满足选举条件为止。（n为可以成为master的节点数）

- master节点的职责主要包括对集群、节点和索引的管理，但是不负责文档的管理；配置了`node.data: true`的节点才能够对文档进行管理。

### 请说明Elasticsearch集群的脑裂问题?

脑裂问题产生的原因：

1. 网络问题：集群间的网络延迟导致部分节点访问不到master，其它节点认为master宕机了从而选举出新的master，并对master上的分片和副本的健康值设置为red，重新分配新的主分片。（例如网络分区，master和部分节点不在同一个网络中）

2. 节点负载：主节点的角色既可以为master又可以dataNode，但在访问量较大时可能会导致es停止响应造成大面积的延迟，此时其它节点得不到主节点的响应则认为主节点宕机，然后重新选取主节点。

3. GC：es占用过大的堆内存引发Full GC垃圾回收，造成es进程失去响应。（GC过程中会造成不同程度的stw，但老年代GC和Full GC的stw较为严重）

脑裂问题解决方案：

1. 减少误判：`discovery.zen.ping_timeout`用于配置节点状态的响应时间，默认为3s。如果master在该响应时间内还没有响应应答，则判断该master不可用，触发选举。可以适当调大参数（如6s，`discovery.zen.ping_timeout:6`），适当减少误判。

2. 选举触发时机：`discovery.zen.minimum_master_nodes`参数用于控制选举行为发生的最小集群主节点数量。当备选主节点的个数大于该值，且备选主节点中有该值个数的节点认为主节点不可用，则进行选举。建议设置为`n/2 + 1`，n为有资格成为主节点的节点个数。

3. 角色分离：即master节点于data节点分离。

```text
主节点配置为：node master: true，node data: false
从节点配置为：node master: false，node data: true
```

### 请说明Elasticsearch索引文档的流程?

这个面试题不妨说是索引文档的持久化流程。

![](https://img-blog.csdnimg.cn/img_convert/1bdc6c30d1be9b1bff83a683c64d2ac7.png)

1. 首先客户端连接集群中其中一个节点，这个节点称为协调节点。协调节点默认使用文档ID参与路由计算，以便为数据存储提供合适的分片。（`shard = hash(document_id) % (num_of_primary_shards)`）

2. 当分片所在的节点（master）接收到协调节点的请求后，会将请求数据写入到Memory Buffer，然后默认每隔1s写入到Filesystem Cache（Page Cache），从Memory Buffer到Filesystem Cache的过程叫做refresh。

3. 同时也会将请求数据写入到translog，默认translog是每5秒被fsync刷新到硬盘。ES通过translog的机制来保证数据的可靠性的。

4. 每隔30分钟索引或者translog足够大时（默认为 512M）文件系统缓存被刷新（flush）到磁盘，旧的translog会被删除。并且一个提交点被写入磁盘。这个过程叫做 flush；

### 请说明Elasticsearch更新和删除文档的流程?

- 更新和删除都是写操作，但Elasticsearch中的已落盘的文档数据是不可变的，因此不能动态删除或修改原文档索引来反映其变更。

- 磁盘中的每个段都有一个对应的`.del`文件。当变更请求发送后，文档并没有被真正的删除，而是在`.del`文件中被标记为删除。该文档依然能匹配查询，但是会在结果聚合时会被过滤掉。当段合并时，在`.del`文件中被标记为删除的文档将不会被写入新段。

- 新的文档被创建时，Elasticsearch会为该文档指定一个版本号。而当执行更新时，旧版本的文档在.del文件中被标记为删除，新版本的文档被索引到一个新段。旧文档依然能匹配查询，但是会在结果聚合时会被过滤掉。

> 使用乐观锁机制解决并发问题，版本号。

### 请说明Elasticsearch搜索的流程?

![](https://img-blog.csdnimg.cn/img_convert/053a14eee04ace7b4e5aec0ce53a5284.png)

- 客户端发起一个搜索请求在es中被执行成一个两阶段过程：Query and Fetch。

- 在初始查询阶段，查询会广播到索引中每一个分片（主分片或者副本分片）。每个分片在本地执行搜索并构建一个匹配文档的大小（`from + size`）的优先队列。

- 每个分片返回自己的优先队列中所有文档的ID和排序值给协调节点。协调节点会合并这些结果到自己的优先队列中来产生一个全局排序后的结果列表。

- 在取回阶段，协调节点判断哪些文档需要被取回，并向相关的分片提交多个GET请求，如果有需要的话，接着返回文档给协调节点。一旦所有的文档都被取回了，协调节点返回结果给客户端。

> 在搜索的时候是会查询Filesystem Cache 的，但是有部分数据还在Memory Buffer，所以搜索是近实时的。

### Elasticsearch在部署时，有哪些优化方法?

- 64GB内存的机器是非常理想的，但是32GB和16GB机器也是很常见的。但是少于8GB会适得其反。

- 如果你要在更快的CPU和更多的核心之间选择，建议选择更多的核心会更好。多个内核提供的额外并发远胜过稍微快一点点的时钟频率。

- 如果你负担得起SSD，它将远远超出任何旋转介质。基于SSD的节点，查询和索引性能都有提升。

- 减少网络传输的性能损耗。

- 通过设置`gateway.recover_after_nodes`、`gateway.expected_nodes`、`gateway.recover_after_time`可以在集群重启的时候避免过多的分片交换，这可能会让数据恢复从数个小时缩短为几秒钟。

- 不要随意修改垃圾回收器（CMS）和各个线程池的大小。

- 把你的内存的（少于）一半给Lucene（但不要超过 32 GB！），可以通过`ES_HEAP_SIZE`环境变量设置。

- Lucene使用了大量的文件。同时Elasticsearch在节点和HTTP客户端之间进行通信也使用了大量的套接字。所有这一切都需要足够的文件描述符。你应该增加你的文件描述符，设置一个很大的值，如64,000。

补充：索引阶段的性能提升方法

- 使用批量请求时可以调整其大小，每次批量数据5-15MB是个不错的起始点。

- 段合并：Elasticsearch默认速率是20MB/s，对于机械硬盘没问题。但如果使用的是SSD，那么可以考虑将速率提高到100-200MB/s。如果在执行批量导入时，不在意搜索性能，可以暂时关闭合并限流。另外还可以增加`index.translog.flush_threshold_size`配置将默认的512MB调大一些，比如1GB，这可以在事务日志中积累出更大的段。减少了合并段的次数。

- 如果业务需求是搜索结果不需要近实时的准确度，可以考虑把每个索引的`index.refresh_interval`调整到30s。默认1s。

- 如果在执行大批量导入时，可以考虑设置`index.number_of_replicas: 0`关闭副本同步。

### GC方面，在使用Elasticsearch时需要注意什么?

1. 倒排词典的索引需要常驻内存，无法GC，需要监控data node上segment memory增长趋势。

2. 各类缓存如field cache、filter cache、indexing cache、bulk queue等等，要设置合理的大小，并且要应该根据最坏的情况来看heap是否够用，也就是各类缓存全部占满的时候，还有heap空间可以分配给其他任务吗？

3. 避免返回大量结果集的搜索与聚合。确实需要大量拉取数据的场景，可以采用scan&scroll api来实现。（`_search?search_type=scan`&`/_search/scroll`）

4. 集群主分片主流内存且无法水平扩展，超大规模集群可以考虑拆分多个集群并通过`tribe node`连接。

5. 想知道heap够不够，必须结合实际应用场景，并对集群的heap使用情况做持续的监控。

### Elasticsearch对于大数据量（上亿量级）的聚合如何实现?

> TODO 涉及到底层算法，等阅读源码后再来补充这部分

Elasticsearch 提供的首个近似聚合是 cardinality 度量。它提供一个字段的基数，即该字段的 distinct或者 unique 值的数目。它是基于 HLL 算法的。 

HLL 会先对我们的输入作哈希运算，然后根据哈希运算的结果中的 bits 做概率估算从而得到基数。其特点是：可配置的精度，用来控制内存的使用（更精确 ＝ 更多内存）；小的数据集精度是非常高的；

我们可以通过配置参数，来设置去重需要的固定内存使用量。无论数千还是数十亿的唯一值，内存使用量只与你配置的精确度相关。

### 并发情况下，Elasticsearch如何保证读写一致?

> ES读写一致关键词：MVCC乐观并发、写quorum/半数以上、读主从同步/异步Primary

- 通过版本号使用乐观并发控制，以确保新版本不会被旧版本覆盖，由应用层来处理具体的冲突；

- 对于写操作，一致性级别支持quorum/one/all，默认为quorum，即只有当大多数分片可用时才允许写操作。但即使大多数可用，也可能存在因为网络等原因导致写入副本失败，这样该副本被认为故障，分片将会在一个不同的节点上重建。

- 对于读操作，可以设置 replication 为 sync(默认)，这使得操作在主分片和副本分片都完成后才会返回。如果设置 replication 为 async 时，也可以通过设置搜索请求参数_preference 为 primary 来查询主分片，确保文档是最新版本。

### 如何监控Elasticsearch集群状态?

Kibana

### 请说明字典树?

常见的字典数据结构如下所示：

| 数据结构                           | 优缺点                                                        |
| ------------------------------ | ---------------------------------------------------------- |
| 排序列表 Array/List                | 使用二分法查找，不平衡                                                |
| HashMap/TreeMap                | 性能高，内存消耗大，几乎是原始数据的三倍                                       |
| Skip List                      | 跳表，可以快速查找词条，在Lucene、Redis、Hbase等均有实现。相对于TreeMap等结构更适合高并发场景 |
| Trie                           | 适合英文词典，如果系统中存在大量字符串且这些字符串基本没有公共前缀，则相应的Trie树会非常消耗内存         |
| Double Array Trie              | 适合中文分词，内存占用小，较多分词工具均采用该算法                                  |
| Ternary Search Tree            | 三叉树，每一个node有三个节点，兼具节省空间和查询快的特点                             |
| Finite State Transducers (FST) | 有限状态转移器，Lucene v4中有开源实现，并大量使用                              |

而字典树又称单词查找树，Trie树，是一种树形结构，是一种哈希树的变种。典型应用是用于统计，排序和保存大量的字符串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。它的优点是：利用字符串的公共前缀来减少查询时间，最大限度地减少无谓的字符串比较，查询效率比哈希树高。

Trie的核心思想是空间换时间，利用字符串的公共前缀来降低查询时间的开销以达到提高效率的目的。

![](https://github.com/Doing-code/guide/blob/main/image/es_Trie树结构.jpg)

> 图片来自https://bkimg.cdn.bcebos.com/pic/8cb1cb1349540923dd5444ffe40ec609b3de9d82e3ae

它有3个基本性质：

- 根节点不包含字符，除根节点外每一个节点都只包含一个字符；
 
- 从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串； 

- 每个节点的所有子节点包含的字符都不相同。

基本实现搜索的方式：

1. 从根结点开始一次搜索；

2. 取得要查找关键词的第一个字母，并根据该字母选择对应的子树并转到该子树继续进行检索；

3. 在相应的子树上，取得要查找关键词的第二个字母,并进一步选择对应的子树进行检索。

4. 迭代过程……

5. 在某个结点处，关键词的所有字母已被取出，则读取附在该结点上的信息，即完成查找。

而对于中文的字典树，每个节点的子节点用一个哈希表存储，避免浪费过多的内存，并且在查询上可以保留哈希表的时间复杂度O(1)。 

### Elasticsearch中的集群、节点、索引、文档、类型的概念是什么?

- 集群是一个或多个节点（服务器）的集合，它们共同保存整个数据，并提供跨所有节点的联合索引和搜索功能。集群有唯一的名称标识，默认情况下为"elasticsearch"。此名称很重要，因为如果节点设置为按名称加入集群，则该节点只能是集群的一部分。

- 节点是属于集群一部分的单个服务器。它存储数据并参与集群索引和搜索功能。

- 索引就像关系数据库中的"数据库"。它有一个定义多种类型的映射。索引是逻辑名称空间，映射到一个或多个主分片，并且可以有零个或多个副本分片。MySQL =>数据库，Elasticsearch=>索引。

- 文档类似于关系数据库中的一行。不同之处在于索引中的每个文档可以具有不同的结构(字段)，但是对于通用字段应该具有相同的数据类型。

- 类型是索引的逻辑类别/分区，其语义完全取决于用户。在v7.6x中移除了Type。默认Type为_doc，且不允许自定义。

### Elasticsearch中的倒排索引是什么?

倒排索引会在存储数据时将关键词和数据进行关联。

正排（正向）索引

```text
id     content
-----------------
1001   my name is zhang san
1002   my name is li si
```

倒排索引

```text
keyword   id
-------------
name      1001, 1002
zhang     1001
```

## 源码

## 附录

操作不存在的数据时会响应404。

### filter、should一起使用，导致的should语句失效

将should嵌在must语句中。

```text
{
    "query": {
        "bool": {
            "must": {
                "bool": {
                    "should": [
                        {
                            "match": {
                                "title": "华为"
                            }
                        },
                        {
                            "match": {
                                "title": "小米"
                            }
                        }
                    ]
                }
            },
            "filter": {
                "range": {
                    "price": {
                        "gt": 500
                    }
                }
            }
        }
    }
}
```

### Elasticsearch的查询过程

一个搜索请求必须询问请求的索引中所有分片的某个副本来进行匹配。

假设一个索引有5个主分片，每个主分片有1个副分片，共10个分片，一次搜索请求会由5个分片来共同完成，它们可能是主分片，也可能是副分片。也就是说，一次搜索请求只会命中所有分片副本中的一个。

Elasticsearch搜索的流程可以分为两个主要阶段：Query and Fetch，也可以称为两阶段搜索过程。

1、Query阶段：

- 查询解析（Query Parsing）： 用户提交一个搜索请求，包含查询字符串和相关的搜索条件。首先，Elasticsearch会解析这个查询字符串，将其转换成一个查询对象。

- 分片选择（Shard Selection）： Elasticsearch索引通常会分为多个分片，查询会被分发到包含相关数据的分片上。在查询阶段，系统选择哪些分片包含可能匹配的数据。

- 倒排索引搜索（Inverted Index Search）： Elasticsearch使用倒排索引来快速查找匹配的文档。在倒排索引中，每个词汇或术语都映射到包含该词汇的文档列表。搜索查询在倒排索引上执行，以找到匹配的文档。

- 评分和排名（Scoring and Ranking）： 查询结果中的文档会根据其与查询的匹配程度进行评分。Elasticsearch使用TF-IDF等算法来计算文档的相关性分数，然后根据分数对文档进行排序。

2、Fetch阶段：

- 文档检索（Document Retrieval）： 在Query阶段，只返回文档的ID和分数，而不是文档的实际内容。在Fetch阶段，Elasticsearch会根据查询结果中的文档ID，从相应的分片中检索实际的文档内容。

- 结果返回（Result Return）： 检索到的文档会被返回给用户，通常以JSON格式返回，包含文档的原始内容和相关的元数据。

这两个阶段协同工作，Query阶段用于快速确定匹配的文档，Fetch阶段用于检索文档的实际内容。这种两阶段搜索过程可以有效减少不必要的IO开销，提高搜索性能，特别是在大规模数据集上。