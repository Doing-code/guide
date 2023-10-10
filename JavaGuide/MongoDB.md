# MongoDB

> 官方文档：https://www.mongodb.com/docs/manual/
>
> MongoDb的shell就是javascript实现的,可以使用js管理数据库。这意味着可以使用js脚本进行复杂的管理。

- 理解MongoDB的业务场景，熟悉MongoDB的概念、特点、体系结构以及数据结构等。

- 掌握MongoDB基本常用命令实现数据的CRUD。

- 掌握MongoDB的索引类型、索引管理、执行计划。

- MongoDB的副本集：故障转移、选举规则。

- MongoDB的分片集群：分片策略、故障转移。

- MongoDB的安全认证。

## MongoDB概念

### 业务场景

传统的关系型数据库（如MySQL），在数据操作的“三高”需求以及应对Web2.0的网站需求面前，显得力不从心。

"三高"需求：

- High performance - 对数据库高并发读写的需求。

- Huge Storage - 对海量数据的高效率存储和访问的需求。

- High Scalability && High Availability- 对数据库的高可扩展性和高可用性的需求。

而MongoDB可应对"三高"需求。

具体的应用场景如：

1. 社交场景，使用 MongoDB 存储存储用户信息，以及用户发表的朋友圈信息，通过地理位置索引实现附近的人、地点等功能。

2. 游戏场景，使用 MongoDB 存储游戏用户信息，用户的装备、积分等直接以内嵌文档的形式存储，方便查询、高效率存储和访问。

3. 物流场景，使用 MongoDB 存储订单信息，订单状态在运送过程中会不断更新，以 MongoDB 内嵌数组的形式来存储，一次查询就能将订单所有的变更读取出来。

4. 物联网场景，使用 MongoDB 存储所有接入的智能设备信息，以及设备汇报的日志信息，并对这些信息进行多维度的分析。

5. 视频直播，使用 MongoDB 存储用户信息、点赞互动信息等。

而这些应用场景中，数据操作方面的共同特点是：

1. 数据量大。

2. 写入操作频繁（且读写都很频繁）。

3. 价值较低的数据，对事务性要求不高。

对于这样的数据，我们更适合使用MongoDB来实现数据的存储。那什么适合选择MongoDB?

在架构选型上，除了上述的三个特点外，如果你还犹豫是否要选择它？可以考虑以下的一些问题：

1. 应用不需要事务及复杂 join 支持。

2. 新应用，需求会变，数据模型无法确定，想快速迭代开发。

3. 应用需要2000-3000或更高的读写QPS。

4. 应用需要TB甚至PB级别数据存储。

5. 应用发展迅速，需要能快速水平扩展。

6. 应用要求存储的数据不丢失。

7. 应用需要99.999%高可用。

8. 应用需要大量的地理位置查询、文本查询。

如果上述有1个符合，可以考虑 MongoDB，2个及以上的符合，选择 MongoDB 绝不会后悔。

相对MySQL，可以以更低的成本解决问题（包括学习、开发、运维等成本）。

### MongoDB是什么?

MongoDB是一个开源、高性能、无模式（不需要像关系型数据库创建表时指定列）的文档型数据库，当初的设计就是用于简化开发和方便扩展，是NoSQL数据库产品中的一种。是最像关系型数据库（MySQL）的非关系型数据库。

它支持的数据结构非常松散，是一种类似于JSON的格式叫BSON，所以它既可以存储比较复杂的数据类型，又相当的灵活。

MongoDB中的记录是一个文档，它是一个由字段和值对（field:value）组成的数据结构。MongoDB文档类似于JSON对象，即一个文档认为就是一个对象。字段的数据类型是字符型，它的值除了使用基本的一些类型外，还可以包括其他文档、普通数组和文档数组。

### MongoDB结构

| SQL术语/概念  | MongoDB术语/概念 | 解释/说明|
|-------------|-------------|-------------|
| database    | database    |数据库|
| table       | collection  |数据库表/集合|
| row         | document    |数据记录行/文档|
| column      | field       |数据字段/域|
| index       | index       |索引|
| table joins |             |表连接,MongoDB不支持|
|             | 嵌入文档     |MongoDB通过嵌入式文档来替代多表连接|
| primary     | key primary key |主键,MongoDB自动将_id字段设置为主键|

### 数据模型

MongoDB的最小存储单位就是文档(document)对象。文档(document)对象对应于关系型数据库的行。数据在MongoDB中以BSON（Binary-JSON）文档的格式存储在磁盘上。

BSON（Binary Serialized Document Format）是一种类json的一种二进制形式的存储格式，简称Binary JSON。BSON和JSON一样，支持内嵌的文档对象和数组对象，但是BSON有JSON没有的一些数据类型，如Date和BinData类型。

而BSON采用了类似于C语言结构体的名称。对表示方法，支持内嵌的文档对象和数组对象，具有轻量性、可遍历性、高效性的三个特点，可以有效描述非结构化数据和结构化数据。这种格式的优点是灵活性高，但它的缺点是空间利用率不是很理想。

Bson中，除了基本的JSON类型：string,integer,boolean,double,null,array和object。mongo还使用了特殊的数据类型。这些类型包括date,object id,binary data,regular expression和code。

每一个驱动都以特定语言的方式实现了这些类型，查看驱动的文档来获取详细信息。

| 数据类型      | 描述                                       | 举例                               |
| --------- | ---------------------------------------- | -------------------------------- |
| 字符串       | UTF-8字符串都可表示为字符串类型的数据                    | {"x" : "foobar"}                 |
| 对象id      | 对象id是文档的12字节的唯一ID（UUID写法）                       | {"X" :ObjectId() }               |
| 布尔值       | 真或者假：true或者false                         | {"x":true}+                      |
| 数组        | 值的集合或者列表可以表示成数组                          | {"x" ： ["a", "b", "c"]}          |
| 32位整数     | 类型不可用。JavaScript仅支持64位浮点数，所以32位整数会被自动转换。 | shell是不支持该类型的，shell中默认会转换成64位浮点数 |
| 64位整数     | 不支持这个类型。shell会使用一个特殊的内嵌文档来显示64位整数，       | shell是不支持该类型的，shell中默认会转换成64位浮点数 |
| 64位浮点数    | shell中的数字就是这一种类型                         | {"x"：3.14159，"y"：3}              |
| null      | 表示空值或者未定义的对象                             | {"x":null}                       |
| undefined | 文档中也可以使用未定义类型                            | {"x":undefined}                  |
| 符号        | shell不支持，shell会将数据库中的符号类型的数据自动转换成字符串     |                                  |
| 正则表达式     | 文档中可以包含正则表达式，采用JavaScript的正则表达式语法        | {"x" ： /foobar/i}                |
| 代码        | 文档中还可以包含JavaScript代码，类似于存储过程的功能                     | {"x" ： function() { /* …… */ }}  |
| 二进制数据     | 二进制数据可以由任意字节的串组成，不过shell中无法使用            |                                  |
| 最大值/最小值   | BSON包括一个特殊类型，表示可能的最大值。shell中没有这个类型。      |                                  |

shell默认使用64位浮点型数值。{"x"：3.14}或{"x"：3}。对于整型值，可以使用NumberInt（4字节符号整数）或NumberLong（8字节符号整数），{"x":NumberInt("3")}{"x":NumberLong("3")}

### MongoDB特点

- 高性能：

MongoDB提供高性能的数据持久性。特别是对嵌入式数据模型的支持减少了数据库系统上的I/O活动。

索引支持更快的查询，并且可以包含来自嵌入式文档和数组的键。（文本索引解决搜索的需求、TTL索引解决历史数据自动过期的需求、地理位置索引可用于构建各种O2O应用）

如mmapv1、wiredtiger、mongorocks（rocksdb）、in-memory等多引擎支持满足各种场景需求。Gridfs解决文件存储的需求。

- 高可用性：

MongoDB的复制工具称为副本集（replica set），它可提供自动故障转移和数据冗余。

- 高扩展性：

MongoDB提供了水平可扩展性作为其核心功能的一部分。分片将数据分布在一组集群的机器上。（海量数据存储，服务能力水平扩展）

从3.4开始，MongoDB支持基于片键创建数据区域。在一个平衡的集群中，MongoDB将一个区域所覆盖的读写只定向到该区域内的那些片。

- 多样化的查询支持：

MongoDB支持丰富的查询语言，支持读和写操作(CRUD)，比如数据聚合、文本搜索和地理空间查询等。

......

## 常用命令

### 数据库操作

#### 选择和创建数据库

- 语法：

```text
# use 数据库名称;

use student;
```

如果数据库不存在则创建。

#### 查看所有数据库

- 语法：`show dbs;`或者`show databases;`。

两个命令都可以查询数据库。

但是在MongoDB中，Collection只有在内容插入后才会创建。即创建Collection（数据表）后需要再插入一个文档（row），Collection才会被真正的创建。

MongoDB有几个默认数据库：

- admin： 从权限的角度来看，这是"root"数据库。要是将一个用户添加到这个数据库，这个用户自动继承所有数据库的权限。一些特定的服务器端命令也只能从这个数据库运行，比如列出所有的数据库或者关闭服务器。

- local: 这个数据永远不会被复制，可以用来存储限于本地单台服务器的任意集合。

- config: 当Mongo用于分片设置时，config数据库在内部使用，用于保存分片的相关信息。

#### 删除数据库

- 语法：

```text
db.dropDatabase();
```

### 集合操作

> Collection，类比关系型数据库中的表。

Collection的创建有两种方式：显示创建（了解即可）、隐式创建。

#### Collection显示创建

- 语法：`db.createCollection(name)`。name是要创建的集合名称。

例如：创建一个名为 mycollection 的普通集合。

```text
db.createCollection("mycollection")
```

查看当前库中的表：`show collections;`或者`show tables;`。

#### Collection隐式创建

当向一个集合中插入一个文档的时候，如果集合不存在，则会自动创建集合。通常使用隐式创建文档即可。

- 语法：

```text
db.collection.insert(
    <document or array of documents>,
    {
        writeConcern: <document>,
        ordered: <boolean>
    }
)
```

示例：如果comment集合如果不存在，则会隐式创建。

```text
db.comment.insert({
    "articleid":"100000",
    "content":"今天天气真好，阳光明媚",
    "userid":"1001",
    "nickname":"Rose",
    "createdatetime":new Date(),
    "likenum":NumberInt(10),
    "state":null
})
```

#### Collection删除

- 语法：`db.<collectionName>.drop()`。collectionName是要删除的集合名称。

例如：要删除mycollection集合：

```text
db.mycollection.drop()
```

### 文档操作

文档（document）的数据结构和 JSON 基本一样。所有存储在集合中的数据都是 BSON 格式。

#### 文档新增

##### 单文档插入

- 语法：

```text
db.collection.insert(
    <document or array of documents>,
    {
        writeConcern: <document>,
        ordered: <boolean>
    }
)
# 或者
db.collection.save(
    <document or array of documents>,
    {
        writeConcern: <document>,
        ordered: <boolean>
    }
)
```

| 参数 | 类型 | 说明|
| --------- | ---- | -----------|
| document | document or array | 要插入到集合中的文档或文档数组。（(json格式）|
| writeConcern | document Optional. | 一种表达书写问题的文件。省略以使用默认的写问题。性能和可靠性的级别|
| ordered | boolean | 可选。如果为真，则按顺序插入数组中的文档，如果其中一个文档出现错误，MongoDB将返回而不处理数组中的其余文档。如果为假，则执行无序插入，如果其中一个文档出现错误，则继续处理数组中的主文档。在版本2.6+中默认为true|

- 示例：向comment的集合(表)中插入一条测试数据。

```text
db.comment.insert({
    "articleid":"100000",
    "content":"今天天气真好，阳光明媚",
    "userid":"1001",
    "nickname":"Rose",
    "createdatetime":new Date(),
    "likenum":NumberInt(10),
    "state":null
})
```

- 注意事项：

1. comment集合如果不存在，则会隐式创建。

2. mongo中的数字，默认情况下是double类型，如果要存整型，必须使用函数NumberInt(num)，否则取出来就有问题了。

3. 插入当前日期使用 new Date()。

4. 插入的数据没有指定 _id ，会自动生成主键值。

5. 如果某字段没值，可以赋值为null，或不写该字段。

6. 文档中的键/值对是有序的。

7. 文档中的值支持多种数据类型。

8. MongoDB区分类型和大小写。

9.  MongoDB的文档不能有重复的键。

##### 批量插入

- 语法：

```text
db.collection.insertMany(
    [ <document 1> , <document 2>, ... ],
    {
        writeConcern: <document>,
        ordered: <boolean>
    }
)
```

- 示例：批量插入多条测试数据。

```text
db.comment.insertMany([
    {
        "_id":"1",
        "articleid":"100001",
        "content":"我们不应该把清晨浪费在手机上，健康很重要，一杯温水幸福你我他。",
        "userid":"1002",
        "nickname":"相忘于江湖",
        "createdatetime":new Date("2019-08-05T22:08:15.522Z"),
        "likenum":NumberInt(1000),
        "state":"1"
    },
    {
        "_id":"2",
        "articleid":"100001",
        "content":"我夏天空腹喝凉开水，冬天喝温开水",
        "userid":"1005",
        "nickname":"伊人憔悴",
        "createdatetime":new Date("2019-08-05T23:58:51.485Z"),
        "likenum":NumberInt(888),
        "state":"1"
    },
    {
        "_id":"3",
        "articleid":"100001",
        "content":"我一直喝凉开水，冬天夏天都喝。",
        "userid":"1004",
        "nickname":"杰克船长",
        "createdatetime":new Date("2019-08-06T01:05:06.321Z"),
        "likenum":NumberInt(666),
        "state":"1"
    }
]);
```

如果在插入时指定了 _id ，则主键就是该值。如果某条数据插入失败，将会终止插入，但已经插入成功的数据不会回滚掉。因为批量插入由于数据较多容易出现失败，因此，可以使用try catch进行异常捕捉处理。

```text
try {
    db.comment.insertMany([
        {
            "_id":"1",
            "articleid":"100001",
            "content":"我们不应该把清晨浪费在手机上，健康很重要，一杯温水幸福你我他。",
            "userid":"1002",
            "nickname":"相忘于江湖",
            "createdatetime":new Date("2019-08-05T22:08:15.522Z"),
            "likenum":NumberInt(1000),
            "state":"1"
        },
        {
            "_id":"2",
            "articleid":"100001",
            "content":"我夏天空腹喝凉开水，冬天喝温开水",
            "userid":"1005",
            "nickname":"伊人憔悴",
            "createdatetime":new Date("2019-08-05T23:58:51.485Z"),
            "likenum":NumberInt(888),
            "state":"1"
        },
        {
            "_id":"3",
            "articleid":"100001",
            "content":"我一直喝凉开水，冬天夏天都喝。",
            "userid":"1004",
            "nickname":"杰克船长",
            "createdatetime":new Date("2019-08-06T01:05:06.321Z"),
            "likenum":NumberInt(666),
            "state":"1"
        }
    ]);

} catch(e) {
    // 打印异常信息，插入失败后根据业务采取措施
    print(e)
}
```

#### 文档查询

- 语法：

```text
db.collection.find(<query>, [projection])
```

| 参数 | 类型 | 说明|
| --------- | ---- | -----------|
| query | document | 可选。使用查询运算符指定选择筛选器。若要返回集合中的所有文档，请省略此参数或传递空文档( {} )。|
| projection | document | 可选。指定要在与查询筛选器匹配的文档中返回的字段（投影）。若要返回匹配文档中的所有字段，请省略此参数。|

##### select *

查询comment文档的所有记录。使用命令`db.comment.find()`或者`db.comment.find({})`。

执行查询文档命令后，会发现每条文档会有一个叫`_id`的字段，这个相当于我们原来关系数据库中表的主键，当你在插入文档记录时没有指定该字段，MongoDB会自动创建，其类型是ObjectID类型。

如果在插入文档记录时指定该字段也可以，其类型可以是ObjectID类型，也可以是MongoDB支持的任意类型。如订单id、用户id等。

##### 条件查询

查询userid为1003的记录。

```text
db.comment.find({userid:'1003'})
```

如果只需要返回符合条件的第一条数据，可以使用findOne命令来实现，语法和find一样。

```text
db.comment.findOne({userid:'1003'})
```

##### 投影查询

如果要查询结果返回部分字段，则需要使用投影查询（不显示所有字段，只显示指定的字段）。

- 查询结果只显示`_id`、`userid`、`nickname`（默认`_id`会显示） :

```text
db.comment.find(
    {userid:"1002"},
    {userid:1,nickname:1}
);
```

- 查询结果只显示`userid`、`nickname`，不显示`_id`：

```text
db.comment.find(
    {userid:"1002"},
    {userid:1,nickname:1,_id:0}
);
```

- 查询所有数据，但只显`_id`、`userid`、`nickname`：

```text
db.comment.find(
    {},
    {userid:1,nickname:1}
)
```

#### 文档更新

- 语法：

```text
db.collection.update(
    <query>,
    <update>,
    {
        upsert: <boolean>,
        multi: <boolean>,
        writeConcern: <document>,
        collation: <document>,
        arrayFilters: [ <filterdocument1>, ... ],
        hint: <document|string> // Available starting in MongoDB 4.2
    }
)
```

| 参数           | 类型                             | 说明                                                                                                                                    |
| ------------ | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------- |
| query        | document                       | 更新的选择条件。可以使用与find（）方法中相同的查询选择器，类似sql update查询内where后面的。。在3.0版中进行了更改：当使用upsert:true执行update（）时，如果查询使用点表示法在_id字段上指定条件，则MongoDB将拒绝插入新文档。 |
| update       | document <br/>or<br/>pipeline | 要应用的修改。类比sql update的 set xxx=xxx                                                                                                      |
| upsert       | boolean                        | 可选。如果设置为true，则在没有与查询条件匹配的文档时创建新文档。默认值为false，如果找不到匹配项，则不会插入新文档。                                                                        |
| multi        | boolean                        | 可选。如果设置为true，则更新符合查询条件的多个文档。如果设置为false，则更新一个文档。默认值为false。                                                                             |
| writeConcern | document                       | 可选。表示写问题的文档。抛出异常的级别。                                                                                                                  |
| collation    | document                       |可选。<br/>指定要用于操作的校对规则。<br/>校对规则允许用户为字符串比较指定特定于语言的规则，例如字母大小写和重音标记的规则。<br/>校对规则选项具有以下语法：<br/>校对规则：{<br/>区域设置：，<br/>caseLevel:，<br/>caseFirst:，<br/>强度：，<br/>numericordering:，<br/>替代：，<br/>最大变量：，<br/>向后：<br/>}<br/>指定校对规则时，区域设置字段是必需的；所有其他校对规则字段都是可选的。有关字段的说明，请参阅校对规则文档。<br/>如果未指定校对规则，但集合具有默认校对规则（请参见db.createCollection（）），则该操作将使用为集合指定的校对规则。<br/>如果没有为集合或操作指定校对规则，MongoDB将使用以前版本中使用的简单二进制比较进行字符串比较。不能为一个操作指定多个校对规则。例如，不能为每个字段指定不同的校对规则，或者如果使用排序执行查找，则不能将一个校对规则用于查找，另一个校对规则用于排序。<br/>3.4版新增。|
| arrayFilters | array                          | 可选。一个筛选文档数组，用于确定要为数组字段上的更新操作修改哪些数组元素。                                                                                                 |
| hint         | Document<br/>or string        | 可选。指定用于支持查询谓词的索引的文档或字符串。                                                                                                              |

但主要关注前四个参数即可。

##### 局部修改

修改`_id`为1的记录，将点赞量修改为1001。

```text
db.comment.updateOne({_id:"1"}, {$set: {likenum:NumberInt(1001)}});
```

##### 批量修改

```text
//默认只修改第一条数据
db.comment.update({userid:"1002"},{$set:{nickname:"凯撒2"}})

//修改所有符合条件的数据
db.comment.update({userid:"1002"},{$set:{nickname:"凯撒大帝"}},{multi:true})
```

##### 列值自增长的修改

如果想实现对某列值在原有值的基础上进行增加或减少，可以使用 $inc 运算符来实现。

对3号数据的点赞数，每次递增1

```text
db.comment.update({_id:"3"},{$inc:{likenum:NumberInt(1)}})
```

递减则修改为负数即可。

#### 文档删除

- 语法：`db.collectionName.remove(条件)`

- 示例：

全部删除：

```text
db.comment.remove({})
```

删除_id=1的记录：

```text
db.comment.remove({_id:"1"})
```

### 文档复杂查询

#### 统计查询

- 语法：`db.collection.count(query, options)`

| 参数 | 类型 | 说明|
| --------- | ---- | -----------|
| query | document | 查询选择条件。|
| options | document | 可选。用于修改计数的额外选项。|

- 示例：

统计所有记录数：统计comment集合的所有的记录数。

```text
db.comment.count()
```

按条件统计记录数：统计userid为1003的记录条数。

```text
db.comment.count({userid:"1003"})
```

#### 分页查询

可以使用limit()方法来读取指定数量的数据，使用skip()方法来跳过指定数量的数据。

- 语法：`db.COLLECTION_NAME.find().limit(NUMBER).skip(NUMBER)`

返回指定数量的记录。可以在find方法后调用limit来返回结果(TopN)，limit()默认值是20：

```text
db.comment.find().limit(3)
```

skip方法同样接受一个数字参数作为跳过的记录条数。（前N个不要）,默认值是0

```text
db.comment.find().skip(3)
```

分页需求：每页2条记录。

```text
//第一页
db.comment.find().skip(0).limit(2)
//第二页
db.comment.find().skip(2).limit(2)
//第三页
db.comment.find().skip(4).limit(2)
```

#### 排序查询

sort() 方法对数据进行排序，sort() 方法可以通过参数指定排序的字段，并使用 1 和 -1 来指定排序的方式，其中 1 为升序排列，而 -1 是用于降序排列。

- 语法：`db.COLLECTION_NAME.find().sort({KEY:1})`

对userid降序排列，并对访问量进行升序排列：

```text
db.comment.find().sort({userid:-1,likenum:1})
```

如果userid相同，则按likenum升序排序。

当skip(), limit(), sort()三个放在一起执行的时候，执行的顺序是先 sort(), 然后是 skip()，最后是显示的 limit()，和命令编写顺序无关。

#### 模糊查询

MongoDB的模糊查询是通过正则表达式的方式实现的。正则表达式是js的语法，直接量的写法。

- 语法：`db.collection.find({field:/正则表达式/})`

- 示例：

查询内容包含"开水"的所有文档：

```text
db.comment.find({content:/开水/})
```

查询内容中以"专家"开头的：

```text
db.comment.find({content:/^专家/})
```

#### 比较查询

<, <=, >, >= 这个操作符也是很常用的，格式如下：

```text
db.集合名称.find({ "field" : { $gt: value }}) // 大于: field > value
db.集合名称.find({ "field" : { $lt: value }}) // 小于: field < value
db.集合名称.find({ "field" : { $gte: value }}) // 大于等于: field >= value
db.集合名称.find({ "field" : { $lte: value }}) // 小于等于: field <= value
db.集合名称.find({ "field" : { $ne: value }}) // 不等于: field != value
```

示例：查询评论点赞数量大于700，小于1000的记录。

```text
db.comment.find({likenum:{$gt:NumberInt(700),$lt: NumberInt(1000)}})
```

#### 包含查询

包含使用`$in`操作符。不包含使用`$nin`操作符。

示例：查询集合中userid字段包含1003或1004的文档。

```text
db.comment.find({userid:{$in:["1003","1004"]}})
```

示例：查询集合中userid字段不包含1003或1004的文档。

```text
db.comment.find({userid:{$nin:["1003","1004"]}})
```

#### 条件连接查询

类比SQL中的and和or用法。使用`$and`操作符和`$or`操作符进行关联。

- 语法：

  - `$and:[ { },{ },{ } ]`
  
  - `$or:[ { },{ },{ } ]`

示例：查询集合中likenum大于等于700 并且小于2000的文档：

```text
db.comment.find({$and:[{likenum:{$gte:NumberInt(700)}},{likenum:{$lt:NumberInt(2000)}}]})
```

示例：查询集合中userid为1003，或者点赞数小于1000的文档记录。

```text
db.comment.find({$or:[ {userid:"1003"} ,{likenum:{$lt:1000} }]})
```

### 常用命令小结

```text
选择切换数据库：use articledb
插入数据：db.comment.insert({bson数据})
查询所有数据：db.comment.find();
条件查询数据：db.comment.find({条件})
查询符合条件的第一条记录：db.comment.findOne({条件})
查询符合条件的前几条记录：db.comment.find({条件}).limit(条数)
查询符合条件的跳过的记录：db.comment.find({条件}).skip(条数)
修改数据：db.comment.update({条件},{$set:{要修改部分的字段:数据})
修改数据并自增某字段值：db.comment.update({条件},{$inc:{自增的字段:步进值}})
删除数据：db.comment.remove({条件})
统计查询：db.comment.count({条件})
模糊查询：db.comment.find({字段名:/正则表达式/})
条件比较运算：db.comment.find({字段名:{$gt:值}})
包含查询：db.comment.find({字段名:{$in:[值1，值2]}})或db.comment.find({字段名:{$nin:[值1，值2]}})
条件连接查询：db.comment.find({$and:[{条件1},{条件2}]})或db.comment.find({$or:[{条件1},{条件2}]})
```

## 索引

### 概述

官网文档：`https://docs.mongodb.com/manual/indexes/`

索引支持在MongoDB中高效地执行查询。如果没有索引，MongoDB必须执行全集合扫描，即扫描集合中的每个文档，以选择与查询语句匹配的文档。

这种扫描全集合的查询效率是非常低的，特别在处理大量的数据时，查询可以要花费几十秒甚至几分钟，这对网站的性能是非常致命的。

如果查询时存在适当的索引，MongoDB可以使用该索引限制必须检查的文档数。

索引是特殊的数据结构，它以易于遍历的形式存储集合数据集的一小部分。索引存储特定字段或一组字段的值，按字段值排序。索引项的排序支持有效的相等匹配和基于范围的查询操作。此外，MongoDB还可以使用索引中的排序返回排序结果。

MongoDB索引使用B树数据结构（确切的说是B Tree，MySQL是B+Tree）

### 索引的类型

#### 单字段索引

MongoDB支持在文档的单个字段上创建用户定义的升序/降序索引，称为单字段索引（Single Field Index）。

对于单个字段索引和排序操作，索引键的排序顺序（即升序或降序）并不重要，因为MongoDB可以在任何方向上遍历索引。

#### 复合索引

MongoDB还支持多个字段的用户定义索引，即复合索引（Compound Index）。

复合索引中列出的字段顺序具有重要意义。例如，如果复合索引由`{ userid: 1, score: -1 }`组成，则索引首先按userid正序排序，然后在每个userid的值内，按score倒序排序。

#### 其它索引

地理空间索引（Geospatial Index）、文本索引（Text Indexes）、哈希索引（Hashed Indexes）。

地理空间索引（Geospatial Index）：为了支持对地理空间坐标数据的有效查询，MongoDB提供了两种特殊的索引：返回结果时使用平面几何的二维索引和返回结果时使用球面几何的二维球面索引。

文本索引（Text Indexes）：MongoDB提供了一种文本索引类型，支持在集合中搜索字符串内容。这些文本索引不存储特定于语言的停止词（例如'the'、'a'、'or'），而将集合中的词作为词干，只存储根词。

哈希索引（Hashed Indexes）：为了支持基于散列的分片，MongoDB提供了散列索引类型，它对字段值的散列进行索引。这些索引在其范围内的值分布更加随机，但只支持相等匹配，不支持基于范围的查询。

### 索引操作

#### 查看索引

- 语法：`db.collectionName.getIndexes()`

查看comment集合中所有的索引情况：

```text
db.comment.getIndexes();

[
    {
      "v": 2,
      "key": {
        "_id": 1
      },
      "name": "_id_"
    }
]
```

结果中显示的是默认 `_id` 索引。

MongoDB在创建集合的过程中，在 `_id` 字段上创建一个唯一的索引，默认名字为 `_id_` ，该索引可防止客户端插入两个具有相同值的文档，您不能在_id字段上删除此索引。

注意：该索引是唯一索引，因此值不能重复，即 `_id` 值不能重复的。在分片集群中，通常使用 `_id` 作为片键。

#### 创建索引

- 语法：`db.collectionName.createIndex(keys, options)`。

| 参数 | 类型 | 说明|
| --- | ---- | ----|
| keys | document | 包含字段和值对的文档，其中字段是索引键，值描述该字段的索引类型。对于字段上的升序索引，请指定值1；对于降序索引，请指定值-1。比如： {字段:1或-1} ，其中1 为指定按升序创建索引，如果你想按降序来创建索引指定为 -1 即可。另外，MongoDB支持几种不同的索引类型，包括文本、地理空间和哈希索引。|
| options | document | 可选。包含一组控制索引创建的选项的文档。有关详细信息，请参见选项详情列表。|

options选项列表：

| 参数 | 类型 | 说明|
| --- | --- | ---|
| background | Boolean | 建索引过程会阻塞其它数据库操作，background可指定以后台方式创建索引，即增加"background" 可选参数。 "background" 默认值为false。|
| unique | Boolean | 建立的索引是否唯一。指定为true创建唯一索引。默认值为false.|
| name | string | 索引的名称。如果未指定，MongoDB的通过连接索引的字段名和排序顺序生成一个索引名称。|
| dropDups | Boolean | 3.0+版本已废弃。在建立唯一索引时是否删除重复记录,指定 true 创建唯一索引。默认值为false.|
| sparse | Boolean | 对文档中不存在的字段数据不启用索引；这个参数需要特别注意，如果设置为true的话，在索引字段中不会查询出不包含对应字段的文档.。默认值为 false.|
| expireAfterSeconds | integer | 指定一个以秒为单位的数值，完成 TTL设定，设定集合的生存时间。|
| v | index version | 索引的版本号。默认的索引版本取决于mongod创建索引时运行的版本。|
| weights | document | 索引权重值，数值在 1 到 99,999 之间，表示该索引相对于其他索引字段的得分权重。|
| default_language | string | 对于文本索引，该参数决定了停用词及词干和词器的规则的列表。 默认为英语|
| language_override | string | 对于文本索引，该参数指定了包含在文档中的字段名，语言覆盖默认的language，默认值为language.|

注意在 3.0.0 版本前创建索引方法为 db.collection.ensureIndex() ，之后的版本使用了 db.collection.createIndex() 方法，ensureIndex() 还能用，但只是 createIndex() 的别名。

##### 单字段索引

按升序创建索引：

```text
db.comment.createIndex({userid:1});
```

查看索引：

```text
db.comment.getIndexes();

[
    {
      "v": 2,
      "key": {
        "_id": 1
      },
      "name": "_id_"
    },
    {
      "v": 2,
      "key": {
        "userid": 1
      },
      "name": "userid_1"
    }
]
```

##### 复合索引

```text
db.comment.createIndex({userid:1,nickname:-1})
```

查看索引：

```text
[
    {
      "v": 2,
      "key": {
        "_id": 1
      },
      "name": "_id_"
    },
    {
      "v": 2,
      "key": {
        "userid": 1
      },
      "name": "userid_1"
    },
    {
      "v": 2,
      "key": {
        "userid": 1,
        "nickname": -1
      },
      "name": "userid_1_nickname_-1"
    }
]
```

#### 删除索引

可以移除指定的索引，或移除所有索引。

- 指定索引的移除，语法：

```text
db.collection.dropIndex(index)
```

| 参数 | 类型 | 说明|
| ---- | ---- | ----|
| index | string or document | 指定要删除的索引。可以通过索引名称或索引规范文档指定索引。若要删除文本索引，请指定索引名称。|

示例：删除 `comment` 集合中 `userid` 字段上的升序索引：

```text
db.comment.dropIndex({userid:1})
```

- 所有索引的移除，语法：`db.collection.dropIndexes()`

示例：删除集合中所有索引。

```text
db.comment.dropIndexes()
```

`_id` 的字段的索引是无法删除的，只能删除非 `_id` 字段的索引。

### 索引的应用

分析查询性能（Analyze Query Performance）通常使用执行计划（解释计划、Explain Plan）来查看查询的情况，如查询耗费的时间、是否基于索引查询等。

想要直到建立的索引是否有效，效果如何，都需要通过执行计划查看。

#### 执行计划

- 语法：`db.collection.find(query,options).explain(options)`

查看根据userid查询数据的情况（未使用索引）：

```text
db.comment.find({userid:"1003"}).explain();

{
	"explainVersion" : "1",
	"queryPlanner" : {
		"namespace" : "articledb.comment",
		"indexFilterSet" : false,
		"parsedQuery" : {
			"userid" : {
				"$eq" : "1003"
			}
		},
		......
		"winningPlan" : {
			"stage" : "COLLSCAN",
			"filter" : {
				"userid" : {
					"$eq" : "1003"
				}
			},
			"direction" : "forward"
		},
		"rejectedPlans" : [ ]
	},
	"command" : {
		......
	},
	"serverInfo" : {
		"host" : "DESKTOP-QVKUQLK",
		"port" : 27017,
		"version" : "6.0.3",
		"gitVersion" : "f803681c3ae19817d31958965850193de067c516"
	},
	"serverParameters" : {
		......
	},
	"ok" : 1
}
```

观察`queryPlanner.winningPlan.stage`的值，`"stage" : "COLLSCAN"`表示全集合扫描。

查看根据userid查询数据的情况（使用索引）：

```text
db.comment.createIndex({userid:1})

db.comment.find({userid:"1003"}).explain();

{
	"explainVersion" : "1",
	"queryPlanner" : {
		"namespace" : "articledb.comment",
		"indexFilterSet" : false,
		"parsedQuery" : {
			"userid" : {
				"$eq" : "1003"
			}
		},
		......
		"winningPlan" : {
			"stage" : "FETCH",
			"inputStage" : {
				"stage" : "IXSCAN",
				"keyPattern" : {
					"userid" : 1
				}
                ......
			}
		},
		"rejectedPlans" : [ ]
	},
	"command" : {
		......
	},
	"serverInfo" : {
		"host" : "DESKTOP-QVKUQLK",
		"port" : 27017,
		"version" : "6.0.3",
		"gitVersion" : "f803681c3ae19817d31958965850193de067c516"
	},
	"serverParameters" : {
		......
	},
	"ok" : 1
}
```

查询字段建立索引后的执行计划，`"stage" : "IXSCAN"`，表示基于索引的扫描。

#### 覆盖索引

> 类比MySQL的覆盖索引，避免回表查询。

当查询条件和查询的投影仅包含索引字段时，MongoDB直接从索引返回结果，而不扫描任何文档或将文档带入内存。这些覆盖的查询可以非常有效。

## 集群

### 副本集

#### 概述

MongoDB中的副本集（Replica Set）是一组维护相同数据集的mongod服务。副本集可提供冗余和高可用性。

也可以说，副本集类似于有自动故障恢复功能的主从集群。通俗的讲就是用多台机器进行同一数据的异步同步，从而使多台机器拥有同一数据的多个副本，并且当主库故障时在不需要用户干预的情况下自动切换其他备份服务器做主库。而且还可以利用副本服务器做只读服务器，实现读写分离，提高负载。

- 冗余和数据可用性

复制提供冗余并提高数据可用性。 通过在不同数据库服务器上提供多个数据副本，复制可提供一定级别的容错功能，以防止丢失单个数据库服务器。

在某些情况下，复制可以提高读性能，因为客户端可以将读取操作发送到不同的服务上，在不同数据中心维护数据副本可以增加分布式应用程序的数据位置和可用性。还可以为专用目的维护其他副本，例如灾难恢复，报告或备份。

- MongoDB中的复制

副本集是一组维护相同数据集的mongod实例。副本集包含多个数据承载节点和可选的一个仲裁节点。在承载数据的节点中，一个且仅一个成员被视为主节点，而其他节点被视为次要（从）节点。

主节点接收所有写操作。副本集只能有一个主要能够确认具有{w：“most”}写入关注的写入; 虽然在某些情况下，另一个mongod实例可能暂时认为自己也是主要的。主要记录其操作日志中的数据集的所有更改，即oplog。

辅助(副本)节点复制主节点的oplog并将操作应用于其数据集，以使辅助节点的数据集反映主节点的数据集。如果主节点故障，则符合条件的从节点将进行选举以选出新的主节点对外提供服务。

- 主从复制和副本集区别

主从集群和副本集最大的区别就是副本集没有固定的“主节点”；整个集群会选出一个“主节点”，当其挂掉后，又在剩下的从节点中选中其他节点为“主节点”，副本集总有一个活跃点(主、primary)和一个或多个备份节点(从、secondary)。

MongoDB的副本集可以理解为Redis的主从同步+哨兵机制。

#### 副本集的角色

副本集有两种节点类型和三种节点角色。

两种类型：

- 主节点（Primary）类型：数据操作的主要连接点，可读写。

- 次要（辅助、从）节点（Secondaries）类型：数据冗余备份节点，可以读或选举。

三种角色：

- 主要成员（Primary）：主要接收所有写操作。就是主节点。

- 副本成员（Replicate）：从主节点通过复制操作以维护相同的数据集，即备份数据，不可写操作，但可以读操作（需要配置）。是默认的一种从节点类型。

- 仲裁者（Arbiter）：不保留任何数据的副本，只具有投票选举作用。当然也可以将仲裁服务器维护为副本集的一部分，即副本成员同时也可以是仲裁者。也是一种从节点类型。

关于仲裁者角色的额外说明：

可以将额外的mongod实例添加到副本集作为仲裁者。仲裁者不维护数据集。仲裁者的目的是通过响应其他副本集成员的心跳和选举请求来维护副本集中的仲裁。因为它们不存储数据集，所以仲裁器可以是提供副本集仲裁功能的好方法，其资源成本比具有数据集的全功能副本集成员更便宜。

如果您的副本集具有偶数个成员，请添加仲裁者以获得主要选举中的“大多数”投票。仲裁者不需要专用硬件。仲裁者将永远是仲裁者，而主节点可能会退出并成为从节点，而从节点可能成为选举期间的主节点。

如果你的副本+主节点的个数是偶数，建议加一个仲裁者，形成奇数，容易满足大多数的投票。

如果你的副本+主节点的个数是奇数，可以不加仲裁者。

#### 搭建副本集

> 环境搭建
>
> https://github.com/Doing-code/guide/blob/main/doc/mongodb_base.pdf
>
> https://github.com/Doing-code/guide/blob/main/doc/mongodb_advance.pdf

##### 主节点

建立存放数据和日志的目录：

```shell script
mkdir -p /mongodb/replica_sets/myrs_27017/log \ & mkdir -p /mongodb/replica_sets/myrs_27017/data/db
```

创建配置文件：

```shell script
vim /mongodb/replica_sets/myrs_27017/mongod.conf

systemLog:
  #MongoDB发送所有日志输出的目标指定为文件
  destination: file
  #mongod或mongos应向其发送所有诊断日志记录信息的日志文件的路径
  path: "/mongodb/replica_sets/myrs_27017/log/mongod.log"
  #当mongos或mongod实例重新启动时，mongos或mongod会将新条目附加到现有日志文件的末尾。
  logAppend: true
storage:
  #mongod实例存储其数据的目录。storage.dbPath设置仅适用于mongod。
  dbPath: "/mongodb/replica_sets/myrs_27017/data/db"
  journal:
    #启用或禁用持久性日志以确保数据文件保持有效和可恢复。
    enabled: true
processManagement:
  #启用在后台运行mongos或mongod进程的守护进程模式。
  fork: true
  #指定用于保存mongos或mongod进程的进程ID的文件位置，其中mongos或mongod将写入其PID
  pidFilePath: "/mongodb/replica_sets/myrs_27017/log/mongod.pid"
net:
  #服务实例绑定所有IP，有副作用，副本集初始化的时候，节点名字会自动设置为本地域名，而不是ip
  #bindIpAll: true
  #服务实例绑定的IP
  bindIp: localhost,192.168.44.140
  #bindIp
  #绑定的端口
  port: 27017
replication:
  #副本集的名称
  replSetName: myrs
```

启动节点服务：

```shell script
/usr/local/mongodb/bin/mongod -f /mongodb/replica_sets/myrs_27017/mongod.conf
```

##### 副本节点

建立存放数据和日志的目录：

```shell script
mkdir -p /mongodb/replica_sets/myrs_27018/log \ & mkdir -p /mongodb/replica_sets/myrs_27018/data/db
```

创建配置文件：

```shell script
vim /mongodb/replica_sets/myrs_27018/mongod.conf

systemLog:
  #MongoDB发送所有日志输出的目标指定为文件
  destination: file
  #mongod或mongos应向其发送所有诊断日志记录信息的日志文件的路径
  path: "/mongodb/replica_sets/myrs_27018/log/mongod.log"
  #当mongos或mongod实例重新启动时，mongos或mongod会将新条目附加到现有日志文件的末尾。
  logAppend: true
storage:
  #mongod实例存储其数据的目录。storage.dbPath设置仅适用于mongod。
  dbPath: "/mongodb/replica_sets/myrs_27018/data/db"
  journal:
    #启用或禁用持久性日志以确保数据文件保持有效和可恢复。
    enabled: true
processManagement:
  #启用在后台运行mongos或mongod进程的守护进程模式。
  fork: true
  #指定用于保存mongos或mongod进程的进程ID的文件位置，其中mongos或mongod将写入其PID
  pidFilePath: "/mongodb/replica_sets/myrs_27018/log/mongod.pid"
net:
  #服务实例绑定所有IP，有副作用，副本集初始化的时候，节点名字会自动设置为本地域名，而不是ip
  #bindIpAll: true
  #服务实例绑定的IP
  bindIp: localhost,192.168.44.140
  #bindIp
  #绑定的端口
  port: 27018
replication:
  #副本集的名称
  replSetName: myrs
```

启动节点服务：

```shell script
/usr/local/mongodb/bin/mongod -f /mongodb/replica_sets/myrs_27018/mongod.conf
```

##### 仲裁节点

建立存放数据和日志的目录：

```shell script
mkdir -p /mongodb/replica_sets/myrs_27019/log \ & mkdir -p /mongodb/replica_sets/myrs_27019/data/db
```

创建配置文件：

```shell script
vim /mongodb/replica_sets/myrs_27019/mongod.conf

systemLog:
  #MongoDB发送所有日志输出的目标指定为文件
  destination: file
  #mongod或mongos应向其发送所有诊断日志记录信息的日志文件的路径
  path: "/mongodb/replica_sets/myrs_27019/log/mongod.log"
  #当mongos或mongod实例重新启动时，mongos或mongod会将新条目附加到现有日志文件的末尾。
  logAppend: true
storage:
  #mongod实例存储其数据的目录。storage.dbPath设置仅适用于mongod。
  dbPath: "/mongodb/replica_sets/myrs_27019/data/db"
  journal:
    #启用或禁用持久性日志以确保数据文件保持有效和可恢复。
    enabled: true
processManagement:
  #启用在后台运行mongos或mongod进程的守护进程模式。
  fork: true
  #指定用于保存mongos或mongod进程的进程ID的文件位置，其中mongos或mongod将写入其PID
  pidFilePath: "/mongodb/replica_sets/myrs_27019/log/mongod.pid"
net:
  #服务实例绑定所有IP，有副作用，副本集初始化的时候，节点名字会自动设置为本地域名，而不是ip
  #bindIpAll: true
  #服务实例绑定的IP
  bindIp: localhost,192.168.44.140
  #bindIp
  #绑定的端口
  port: 27019
replication:
  #副本集的名称
  replSetName: myrs
```

启动节点服务：

```shell script
/usr/local/mongodb/bin/mongod -f /mongodb/replica_sets/myrs_27019/mongod.conf
```

##### 配置副本集和主节点

集群环境搭建完成后，开始初始化以及配置主从节点。

1. 客户端连接任一节点（当作主节点）：

```shell script
/usr/local/mongodb/bin/mongo --host=192.168.44.140 --port=27017
```

2. 初始化的副本集（主节点执行）：

```shell script
# 使用默认的配置来初始化副本集
rs.initiate()
```

“ok”的值为1，说明创建成功。稍等片刻，回车，变成主节点。

3. 执行副本集中当前节点的默认节点配置（主节点执行）：

```shell script
rs.conf()
```

此时，在主节点上就可以执行读写操作了。

4. 添加副本节点（主节点执行）：

```shell script
rs.add("192.168.44.140:27018")
```

"ok" : 1 ：说明添加成功。

5. 添加仲裁从节点（主节点执行）：

```shell script
rs.addArb("192.168.44.140:27019")
```

"ok" : 1 ：说明添加成功。

6. 查看副本集状态：

```shell script
rs.status()

{
	"set" : "myrs",
	"date" : ISODate("2023-09-08T12:22:19.189Z"),
	"myState" : 1,
	"term" : NumberLong(1),
	"syncingTo" : "",
	"syncSourceHost" : "",
	"syncSourceId" : -1,
	"heartbeatIntervalMillis" : NumberLong(2000),
	"optimes" : {
		"lastCommittedOpTime" : {
			"ts" : Timestamp(1694175732, 2),
			"t" : NumberLong(1)
		},
		"readConcernMajorityOpTime" : {
			"ts" : Timestamp(1694175732, 2),
			"t" : NumberLong(1)
		},
		"appliedOpTime" : {
			"ts" : Timestamp(1694175732, 2),
			"t" : NumberLong(1)
		},
		"durableOpTime" : {
			"ts" : Timestamp(1694175732, 2),
			"t" : NumberLong(1)
		}
	},
	"lastStableCheckpointTimestamp" : Timestamp(1694175732, 1),
	"members" : [
		{
			"_id" : 0,
			"name" : "192.168.44.140:27017",
			"health" : 1,
			"state" : 1,
			"stateStr" : "PRIMARY",
			"uptime" : 382,
			"optime" : {
				"ts" : Timestamp(1694175732, 2),
				"t" : NumberLong(1)
			},
			"optimeDate" : ISODate("2023-09-08T12:22:12Z"),
			"syncingTo" : "",
			"syncSourceHost" : "",
			"syncSourceId" : -1,
			"infoMessage" : "",
			"electionTime" : Timestamp(1694175610, 1),
			"electionDate" : ISODate("2023-09-08T12:20:10Z"),
			"configVersion" : 3,
			"self" : true,
			"lastHeartbeatMessage" : ""
		},
		{
			"_id" : 1,
			"name" : "192.168.44.140:27018",
			"health" : 1,
			"state" : 2,
			"stateStr" : "SECONDARY",
			"uptime" : 30,
			"optime" : {
				"ts" : Timestamp(1694175732, 2),
				"t" : NumberLong(1)
			},
			"optimeDurable" : {
				"ts" : Timestamp(1694175732, 2),
				"t" : NumberLong(1)
			},
			"optimeDate" : ISODate("2023-09-08T12:22:12Z"),
			"optimeDurableDate" : ISODate("2023-09-08T12:22:12Z"),
			"lastHeartbeat" : ISODate("2023-09-08T12:22:18.514Z"),
			"lastHeartbeatRecv" : ISODate("2023-09-08T12:22:18.693Z"),
			"pingMs" : NumberLong(0),
			"lastHeartbeatMessage" : "",
			"syncingTo" : "",
			"syncSourceHost" : "",
			"syncSourceId" : -1,
			"infoMessage" : "",
			"configVersion" : 3
		},
		{
			"_id" : 2,
			"name" : "192.168.44.140:27019",
			"health" : 1,
			"state" : 7,
			"stateStr" : "ARBITER",
			"uptime" : 6,
			"lastHeartbeat" : ISODate("2023-09-08T12:22:18.514Z"),
			"lastHeartbeatRecv" : ISODate("2023-09-08T12:22:18.686Z"),
			"pingMs" : NumberLong(0),
			"lastHeartbeatMessage" : "",
			"syncingTo" : "",
			"syncSourceHost" : "",
			"syncSourceId" : -1,
			"infoMessage" : "",
			"configVersion" : 3
		}
	],
	"ok" : 1,
	"operationTime" : Timestamp(1694175732, 2),
	"$clusterTime" : {
		"clusterTime" : Timestamp(1694175732, 2),
		"signature" : {
			"hash" : BinData(0,"AAAAAAAAAAAAAAAAAAAAAAAAAAA="),
			"keyId" : NumberLong(0)
		}
	}
}
```

7. 副本节点本身设置为从节点（从节点执行）：

```shell script
rs.slaveOk()
```

至此已实现读写分离，从节点可读不可写。仲裁者节点，不存放任何业务数据，只存放副本集配置等数据。

#### 主节点的选举规则

MongoDB在副本集中，会自动进行主节点的选举，而主节点选举的触发条件为：

1. 主节点故障。

2. 主节点网络不可达（默认心跳为10s）

3. 人工干预（rs.stepDown(600)）

选举规则是根据票数来决定谁获胜:

- 得票数最高，且超过半数副本节点投票支持的节点获胜。

  - 当复制集内存活成员数量不足大多数时（副本节点/2 + 1），整个复制集将无法选举出Primary，复制集将无法提供写服务，处于只读状态。
  
- 如果票数相同，且都超过半数副本节点投票支持，则数据更新的节点获胜。数据的完整程度通过oplog（操作日志）来对比。

在投票时，优先级（priority）参数影响重大。可以通过设置优先级（priority）来设置额外票数。优先级即权重，取值为0-1000。相当于可额外增加0-1000的票数。

优先级的值越大，就越可能获得多数成员的投票（votes）数。指定较高的值可使成员更有资格成为主要成员，更低的值可使成员更不符合条件。默认优先级是1。仲裁节点的优先级必须是0，不具备选举权，但具有投票权。

##### 故障测试

- 副本节点故障测试：

关闭27018副本节点，此时主节点和仲裁节点对27018的心跳失败。但是主节点还在，因此没有触发投票选举。

如果此时在主节点上执行写操作，稍后从节点故障恢复，主节点写入的数据会自动同步给从节点。

- 主节点故障测试：

关闭27017节点，从节点和仲裁节点对27017的心跳失败，当失败超过10秒，此时因为没有主节点了，会自动发起投票。

而副本节点只有27018，因此，候选人只有一个就是27018，开始投票。27019向27018投了一票，27018本身自带一票，因此共两票，超过了“大多数”，27019是仲裁节点，没有选举权，27018不向其投票，其票数是0。

最终结果，27018成为主节点。具备读写功能。

再启动27017节点，发现27017变成了从节点，27018仍保持主节点。登录27017节点，发现是从节点了，数据自动从27018同步。从而实现了高可用。

- 仲裁节点和主节点故障：

先关掉仲裁节点27019，再关掉的主节点27018。27017仍然是从节点，副本集中没有主节点了，导致此时副本集是只读状态，无法写入。

为啥不选举了？因为27017的票数，没有获得大多数，即没有大于等于2，它只有默认的一票（优先级是1）如果要触发选举，随便加入一个成员即可。

如果只加入27019仲裁节点成员，则主节点一定是27017，因为没得选了，仲裁节点不参与选举，但参与投票。

如果只加入27018节点，会发起选举。因为27017和27018都是两票，则按照谁数据新，谁当主节点。

- 仲裁节点和从节点故障：

先关掉仲裁节点27019，关掉副本节点27018，10秒后，27017主节点自动降级为副本节点。（服务降级）副本集不可写数据了，已经故障了。

### 分片集群

#### 概念

分片（sharding）是一种跨多台机器分布数据的方法，MongoDB使用分片来支持具有非常大的数据集和高吞吐量操作的部署。

换句话说：分片(sharding)是指将数据拆分，将其分散存在不同的机器上的过程。有时也用分区(partitioning)来表示这个概念。将数据分散到不同的机器上，不需要功能强大的大型计算机就可以储存更多的数据，处理更多的负载。

具有大型数据集或高吞吐量应用程序的数据库系统会挑战单个服务器的容量。例如，高查询率会耗尽服务器的CPU容量。工作集大小大于系统的RAM会强调磁盘驱动器的I/O容量。

有两种解决系统增长的方法：垂直扩展和水平扩展。

- 垂直扩展意味着增加单个服务器的容量，例如使用更强大的CPU，添加更多RAM或增加存储空间量。可用技术的局限性可能会限制单个机器对于给定工作负载而言足够强大。此外，基于云的提供商基于可用的硬件配置具有硬性上限。垂直缩放有实际的最大值。

- 水平扩展意味着划分系统数据集并加载多个服务器，添加其他服务器以根据需要增加容量。虽然单个机器的总体速度或容量可能不高，但每台机器处理整个工作负载的子集，可能提供比单个高速大容量服务器更高的效率。扩展部署容量只需要根据需要添加额外的服务器，这可能比单个机器的高端硬件的总体成本更低。权衡是基础架构和部署维护的复杂性增加。

MongoDB支持通过分片进行水平扩展。

MongoDB分片群集包含以下组件：

- 分片（存储）：每个分片包含分片数据的子集。 每个分片都可以部署为副本集。两个及以上节点。

- mongos（路由）：mongos充当查询路由器，在客户端应用程序和分片集群之间提供接口。一个及以上节点。

- config servers（“调度”的配置）：配置服务器存储群集的元数据和配置设置。从MongoDB 3.4开始，必须将配置服务器部署为副本集（CSRS）。

MongoDB在集合级别对数据进行分片，将集合数据分布在集群中的分片上。

#### 分片集群搭建

三个分片主节点 + 一个配置节点 + 一个路由节点。

> 分片集群搭建：https://github.com/Doing-code/guide/blob/main/doc/mongodb_advance.pdf
>
> 可以类比Redis分片集群，但Redis没有mongos和config servers的概念。Redis分片集群自带哨兵机制。

#### 分片策略

对集合进行分片时,你需要选择一个 片键（Shard Key） , shard key 是每条记录都必须包含的，且建立了索引的单个字段或复合字段，MongoDB会按照片键将数据划分到不同的数据块中。并将数据块均衡地分布到所有分片中。

为了按照片键划分数据块，MongoDB使用基于哈希的分片方式（随机平均分配）或者基于范围的分片方式（数值大小分配） 。

用任意字段当片键都可以，但一定是必填字段。

- 哈希策略：对于基于哈希的分片，MongoDB计算一个字段的哈希值，并用这个哈希值来创建数据块.

- 范围策略：对于基于范围的分片，MongoDB按照片键的范围把数据分成不同部分。假设有一个数字的片键（想象一个从负无穷到正无穷的直线，每一个片键的值都在直线上画了一个点）。MongoDB把这条直线划分为更短的不重叠的片段，并称之为数据块，每个数据块包含了片键在一定范围内的数据.

  - 在使用片键做范围划分的系统中，拥有”相近”片键的文档很可能存储在同一个数据块中，因此也会存储在同一个分片中.
  
注意事项：一个集合只能指定一个片键，否则报错。一旦对一个集合分片，分片键和分片值就不可改变。 如：不能给集合选择不同的分片键、不能更新分片键的值。

基于范围的分片方式与基于哈希的分片方式性能对比：

- 基于范围的分片方式提供了更高效的范围查询，给定一个片键的范围，分发路由可以很简单地确定哪个数据块存储了请求需要的数据，并将请求转发到相应的分片中.

- 不过，基于范围的分片会导致数据在不同分片上的不均衡，有时候分片带来的消极作用会大于查询性能的积极作用。比如，如果片键所在的字段是线性增长的，一定时间内的所有请求都会落到某个固定的数据块中，最终导致分布在同一个分片中。在这种情况下，一小部分分片承载了集群大部分的数据，系统并不能很好地进行扩展.

- 与此相比，基于哈希的分片方式以范围查询性能的损失为代价，保证了集群中数据的均衡。哈希值的随机性使数据随机分布在每个数据块中，因此也随机分布在不同分片中。但是也正由于随机性，一个范围查询很难确定应该请求哪些分片，通常为了返回需要的结果，需要请求所有分片.

- 如无特殊情况，一般推荐使用 Hash Sharding。而使用 `_id` 作为片键是一个不错的选择，因为它是必有的，你可以使用数据文档 `_id` 的哈希作为片键。

- 这个方案能够是的读和写都能够平均分布，并且它能够保证每个文档都有不同的片键所以数据块能够很精细。
  
- 似乎还是不够完美，因为这样的话对多个文档的查询必将命中所有的分片。虽说如此，这也是一种比较好的方案了。

## 安全认证

默认情况下，MongoDB实例启动运行时是没有启用用户访问权限控制的，这意味着在实例本机服务器上都可以随意连接到实例进行各种操作，MongoDB不会对连接客户端进行用户验证，这是非常危险的。

而为了能保障mongodb的安全可以做以下几个步骤：

1. 使用新的端口，默认的27017端口如果一旦知道了ip就能连接上，不太安全。

2. 设置mongodb的网络环境，最好将mongodb部署到公司服务器内网，这样外网是访问不到的。公司内部访问使用vpn等。

3. 开启安全认证。认证要同时设置服务器之间的内部认证方式，同时要设置客户端连接到集群的账号密码认证方式。

为了强制开启用户访问控制(用户验证)，需要在MongoDB实例启动时使用选项 `--auth` 或在指定启动配置文件中添加选项 `auth=true` 。

在开始之前需要了解一下概念：

1. 启用访问控制：

MongoDB使用的是基于角色的访问控制(Role-Based Access Control,RBAC)来管理用户对实例的访问。通过对用户授予一个或多个角色来控制用户访问数据库资源的权限和数据库操作的权限，在对用户分配角色之前，用户无法访问实例。

在实例启动时添加选项 `--auth` 或指定启动配置文件中添加选项 `auth=true` 。

2. 角色：在MongoDB中通过角色对用户授予相应数据库资源的操作权限，每个角色当中的权限可以显式指定，也可以通过继承其他角色的权限，或者两都都存在的权限。

3. 权限：权限由指定的数据库资源(resource)以及允许在指定资源上进行的操作(action)组成。

1. 资源(resource)包括：数据库、集合、部分集合和集群；

2. 操作(action)包括：对资源进行的增、删、改、查(CRUD)操作。

而在角色定义时可以包含一个或多个已存在的角色，新创建的角色会继承包含的角色所有的权限。在同一个数据库中，新创建角色可以继承其他角色的权限，如在 admin 数据库中创建的角色可以继承在其它任意数据库中角色的权限。

常用的内置角色：

```text
数据库用户角色：    read、readWrite;
所有数据库用户角色： readAnyDatabase、readWriteAnyDatabase、userAdminAnyDatabase、dbAdminAnyDatabase
数据库管理角色：    dbAdmin、dbOwner、userAdmin；
集群管理角色：      clusterAdmin、clusterManager、clusterMonitor、hostManager；
备份恢复角色：      backup、restore；
超级用户角色：      root
内部角色：         system
```

| 角色 | 权限描述|
|---|---|
| read  | 可以读取指定数据库中任何数据。|
| readWrite  | 可以读写指定数据库中任何数据，包括创建、重命名、删除集合。|
| readAnyDatabase  | 可以读取所有数据库中任何数据(除了数据库config和local之外)。|
| readWriteAnyDatabase  | 可以读写所有数据库中任何数据(除了数据库config和local之外)。|
| userAdminAnyDatabase  | 可以在指定数据库创建和修改用户(除了数据库config和local之外)。|
| dbAdminAnyDatabase  | 可以读取任何数据库以及对数据库进行清理、修改、压缩、获取统计信息、执行检查等操作(除了数据库config和local之外)。|
| dbAdmin  | 可以读取指定数据库以及对数据库进行清理、修改、压缩、获取统计信息、执行检查等操作。|
| userAdmin  | 可以在指定数据库创建和修改用户。|
| clusterAdmin  | 可以对整个集群或数据库系统进行管理操作。|
| backup  | 备份MongoDB数据最小的权限。|
| restore | 从备份文件中还原恢复MongoDB数据(除了system.profile集合)的权限。|
| root  | 超级账号，超级权限|

## 附录

### 常用命令

```text
// 数据库操作

use comment;

show dbs;

show databases;

db.dropDatabase();

// 集合操作

db.createCollection("comment")

show collections;

show tables;

db.comment.drop();

// 文档操作

db.comment.insert({
    "articleid":"100000",
    "content":"今天天气真好，阳光明媚",
    "userid":"1001",
    "nickname":"Rose",
    "createdatetime":new Date(),
    "likenum":NumberInt(10),
    "state":null
});

try {
    db.comment.insertMany([
        {
            "articleid":"100000",
            "content":"今天天气真好，阳光明媚",
            "userid":"1001",
            "nickname":"Rose",
            "createdatetime":new Date(),
            "likenum":NumberInt(10),
            "state":null
        }    
    ]);
} catch(e) {
    print(e)
}

db.comment.find({});

db.comment.findOne({userid:'1002'});

db.comment.find({userid:'1002'});

db.comment.find({userid:"1002"},{userid:1,nickname:1,_id:0});

db.comment.find({},{userid:1,nickname:1});

db.comment.find(
    {userid:"1002"},
    {userid:1,nickname:1}
);

db.comment.update({_id:"1"}, {$set: {likenum:NumberInt(1001)}});

//默认只修改第一条数据
db.comment.update({userid:"1002"},{$set:{nickname:"凯撒2"}})

//修改所有符合条件的数据
db.comment.update({userid:"1002"},{$set:{nickname:"凯撒大帝"}},{multi:true})

db.comment.update({_id:"3"},{$inc:{likenum:NumberInt(-1)}})
```

### MongoDB客户端连接语法

```text
mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
```

- mongodb:// 这是固定的格式，必须要指定。

- username:password@ 可选项，如果设置，在连接数据库服务器之后，驱动都会尝试登陆这个数据库。

- host1 必须的指定至少一个host, host1 是这个URI唯一要填写的。它指定了要连接服务器的地址。如果要连接复制集，请指定多个主机地址。

- portX 可选的指定端口，如果不填，默认为27017。

- /database 如果指定username:password@，连接并验证登陆指定数据库。若不指定，默认打开test 数据库。

- ?options 是连接选项。如果不使用/database，则前面需要加上/。所有连接选项都是键值对name=value，键值对之间通过&或;（分号）隔开。

标准的连接格式包含了多个选项(options)，如下所示：

| 选项 | 说明 |
| --- | --- |
| replicaSet=name | 验证replica set的名称。 Impliesconnect=replicaSet.|
| slaveOk=true or false | true:在connect=direct模式下，驱动会连接第一台机器，即使这台服务器不是主。在connect=replicaSet模式下，驱动会发送所有的写请求到主并且把读取操作分布在其他从服务器。false: 在connect=direct模式下，驱动会自动找寻主服务器. 在connect=replicaSet 模式下，驱动仅仅连接主服务器，并且所有的读写命令都连接到主服务器。|
| safe=true or false | true: 在执行更新操作之后，驱动都会发送getLastError命令来确保更新成功。(还要参考 wtimeoutMS).false: 在每次更新之后，驱动不会发送getLastError来确保更新成功。|
| w=n|  驱动添加 { w : n } 到getLastError命令. 应用于safe=true。|
| wtimeoutMS=ms | 驱动添加 { wtimeout : ms } 到 getlasterror 命令. 应用于 safe=true.|
| fsync=true or false | true: 驱动添加 { fsync : true } 到 getlasterror 命令.应用于safe=true.false: 驱动不会添加到getLastError命令中。|
| journal=true or false | 如果设置为 true, 同步到 journal (在提交到数据库前写入到实体中).应用于 safe=true|
| connectTimeoutMS=ms | 可以打开连接的时间。|
| socketTimeoutMS=ms | 发送和接受sockets的时间。|

### MongoDB持久化原理

> 类比Elasticsearch的持久化、MySQL的redo log日志。

MongoDB的持久化原理与其采用的存储引擎有关。MongoDB支持多种存储引擎，但默认情况下使用的是WiredTiger存储引擎。以下是MongoDB的持久化原理概述：

1. 写入操作：当数据被插入、更新或删除时，MongoDB首先将数据写入内存中的数据文件（Data Files）。内存中的数据文件是MongoDB的主要数据存储区域。

2. 内存映射：MongoDB使用一种称为"内存映射文件"的技术，它允许将数据文件映射到操作系统的虚拟内存中。这样做的好处是可以利用操作系统的文件缓存机制，提高读取性能。（Page Cache）

3. 写操作日志：MongoDB在数据写入内存中的数据文件之前，会先将写操作记录到一个称为"写操作日志"（Write-Ahead Log，WAL）的文件中。这个日志记录了所有写入操作的细节。（事务日志）

4. 数据刷回磁盘：MongoDB具有一种后台线程，负责将内存中的数据刷回磁盘，以保证数据持久性。刷回操作不会同步进行，MongoDB会根据一定的策略来决定何时将数据刷回磁盘。

### MongoDB主从同步数据

主从副本集之间通过`oplog`文件进行同步，从节点上主动拉取主节点的数据集进行同步。

MongoDB中，主节点（Primary Node）和副本节点（Secondary Node）之间的数据同步是通过复制集（Replica Set）来实现的。

1. 主节点写入数据：当应用程序向主节点写入数据时，主节点会记录这些写操作并将其应用到自身的数据集上。

2. 操作日志（Oplog）：主节点会将所有的写操作以操作日志（Oplog）的形式保存下来。Oplog是一个特殊的集合，它包含了主节点上的所有写操作的详细记录，以时间顺序排列。

3. 副本节点复制Oplog：副本节点会定期轮询主节点的Oplog，并将其中的写操作同步到自己的数据集上。这个过程被称为数据复制。

4. 数据应用和持续同步：一旦副本节点获得Oplog中的写操作，它会将这些操作应用到自己的数据上，确保数据与主节点保持一致。副本节点会持续监视Oplog，以确保数据的实时同步。