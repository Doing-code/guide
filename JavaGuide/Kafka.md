# Kafka

## 入门
### 定义
- Kafka 是一个分布式事件流平台，分布的基于发布/订阅模式的消息队列，主要应用于实时处理领域。

应用场景：
- 缓存/消峰、解耦、异步通信等。

- 点对点模式：消费者主动拉取消息，消费者收到消息后，会 ack 消息队列，消息队列会删除消息。
- 发布/订阅模式：不会删除消息，每个消费者相互独立。

搭建集群。

修改三处配置`vim config/server.properties`：
```text
broker.id=0

log.dirs=/mydata/kafka/data/logs

zookeeper.connect=192.168.44.129:2181,192.168.44.131:2181,192.168.44.132:2181/kafka
```
### 生产者
![](../image/kafka_生产者_发送流程.png)

重试次数默认是 Integer 最大值。
#### 分区
Kafka可以将主题划分为多个分区（Partition），会根据分区规则选择把消息存储到哪个分区中，只要 如果分区规则设置的合理，那么所有的消息将会被均匀的分布到不同的分区中，这样就实现了负载均衡 和水平扩展。另外，多个订阅者可以从一个或者多个分区中同时消费数据，以支撑海量数据处理能力。

分区写入策略：所谓分区写入策略，即是生产者将数据写入到kafka主题后，kafka如何将数据分配到不同分区中的策略。

常见的有三种策略，轮询策略，随机策略，和按键hash取模保存策略。其中轮询策略是默认的分区策略。
##### 自定义分区器
1. 自定义类实现 `Partitioner` 接口。
2. 重写 `Partitioner` 接口方法，重点是 `partition()` 方法。
3. 修改默认分区器为自定义类。

一种方式是
```java
Properties properties = new Properties();
properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
```
#### 提高生产者吞吐量
以下配置根据实际业务场景灵活调整。

* `batch.size`：批量发送，默认16k。（等待达到16k再发送，）
* `linger.ms`：等待时间，默认0ms。（修改为 5~100 ms）。
* `compression.type`：压缩 snappy。
* `buffer.memory`：缓冲区大小，默认 32m（修改为 64m）
#### 数据可靠性
数据完全可靠条件：ACK级别设置为 -1，分区副本大于等于2，ISR应答的最小副本数量大于等于2.
##### ACK 应答
#### 数据去重
幂等性。事务
#### 数据有序
单分区内有序。多分区无序，分区与分区之间无序 。
#### 数据乱序
### Broker
#### 存储哪些数据
![](../image/kafka_broker_存储的信息.png)
#### 工作原理总览
![](../image/kafka_broker_工作流程总览.png)
### 消费者
### Eagle 监控
### Kraft 模式
## 外部系统集成
### 集成 Flume
### 集成 Flink
### 集成 SpringBoot
### 集成 Spark
## 调优
### 硬件配置选择
### 生产者
### Broker
### 消费者
## 源码 
### 生产者源码
![](../image/kafka_生产者_发送流程.png)
#### 初始化
![](../image/kafka_生产者_初始化流程.png)
##### 程序入口
```java
Properties properties = new Properties();
properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());

// 1、初始化 KafkaProducer
// 2、初始化 sender 线程；4、发送数据
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

// 3、发送数据
producer.send(new ProducerRecord("", ""));
```
##### main 线程初始化
构造器重载
```java
public KafkaProducer(Properties properties) {
    this(properties, null, null);
}
public KafkaProducer(Properties properties, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
    this(Utils.propsToMap(properties), keySerializer, valueSerializer);
}
public KafkaProducer(Map<String, Object> configs, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
    this(new ProducerConfig(ProducerConfig.appendSerializerToConfig(configs, keySerializer, valueSerializer)),
            keySerializer, valueSerializer, null, null, null, Time.SYSTEM);
}
```
经过几次构造方法重载后，会进入下面这个构造方法：
```java
KafkaProducer(ProducerConfig config,
                  Serializer<K> keySerializer,
                  Serializer<V> valueSerializer,
                  ProducerMetadata metadata,
                  KafkaClient kafkaClient,
                  ProducerInterceptors<K, V> interceptors,
                  Time time) {
    try {
        // 自定义配置
        this.producerConfig = config;
        // 系统当前时间
        this.time = time;

        // 获取事务id，自定义的
        String transactionalId = config.getString(ProducerConfig.TRANSACTIONAL_ID_CONFIG);

        // 获取clientId，自定义的
        this.clientId = config.getString(ProducerConfig.CLIENT_ID_CONFIG);

        LogContext logContext;
        if (transactionalId == null)
            logContext = new LogContext(String.format("[Producer clientId=%s] ", clientId));
        else
            logContext = new LogContext(String.format("[Producer clientId=%s, transactionalId=%s] ", clientId, transactionalId));
        log = logContext.logger(KafkaProducer.class);
        log.trace("Starting the Kafka producer");

        // ========== 监控指标，不用到的话可以不用管 ==========
        Map<String, String> metricTags = Collections.singletonMap("client-id", clientId);
        MetricConfig metricConfig = new MetricConfig().samples(config.getInt(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG))
                .timeWindow(config.getLong(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG), TimeUnit.MILLISECONDS)
                .recordLevel(Sensor.RecordingLevel.forName(config.getString(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG)))
                .tags(metricTags);
        List<MetricsReporter> reporters = config.getConfiguredInstances(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG,
                MetricsReporter.class,
                Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId));
        JmxReporter jmxReporter = new JmxReporter();
        jmxReporter.configure(config.originals(Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId)));
        reporters.add(jmxReporter);
        MetricsContext metricsContext = new KafkaMetricsContext(JMX_PREFIX,
                config.originalsWithPrefix(CommonClientConfigs.METRICS_CONTEXT_PREFIX));
        this.metrics = new Metrics(metricConfig, reporters, time, metricsContext);
        
        // 核心组件1：分区器，用于决定消息被路由到 topic 的哪个分区
        this.partitioner = config.getConfiguredInstance(
                ProducerConfig.PARTITIONER_CLASS_CONFIG,
                Partitioner.class,
                Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId));
                
        // 重试间隔，默认 100ms
        long retryBackoffMs = config.getLong(ProducerConfig.RETRY_BACKOFF_MS_CONFIG);
        
        // key/value 序列化
        if (keySerializer == null) {
            this.keySerializer = config.getConfiguredInstance(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                                                                     Serializer.class);
            this.keySerializer.configure(config.originals(Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId)), true);
        } else {
            config.ignore(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
            this.keySerializer = keySerializer;
        }
        if (valueSerializer == null) {
            this.valueSerializer = config.getConfiguredInstance(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                                                                       Serializer.class);
            this.valueSerializer.configure(config.originals(Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId)), false);
        } else {
            config.ignore(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
            this.valueSerializer = valueSerializer;
        }

        // 拦截器
        List<ProducerInterceptor<K, V>> interceptorList = (List) config.getConfiguredInstances(
                ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                ProducerInterceptor.class,
                Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId));
        if (interceptors != null)
            this.interceptors = interceptors;
        else
            this.interceptors = new ProducerInterceptors<>(interceptorList);
        ClusterResourceListeners clusterResourceListeners = configureClusterResourceListeners(keySerializer,
                valueSerializer, interceptorList, reporters);
                
        // 一次请求的最大大小，默认 1m。
        this.maxRequestSize = config.getInt(ProducerConfig.MAX_REQUEST_SIZE_CONFIG);
        
        // 缓冲区大小，默认 32M。缓冲区满了会阻塞，通过 max.block.ms 控制，默认60s
        this.totalMemorySize = config.getLong(ProducerConfig.BUFFER_MEMORY_CONFIG);
        // 压缩（压缩类型），默认为none
        this.compressionType = CompressionType.forName(config.getString(ProducerConfig.COMPRESSION_TYPE_CONFIG));

        this.maxBlockTimeMs = config.getLong(ProducerConfig.MAX_BLOCK_MS_CONFIG);
        int deliveryTimeoutMs = configureDeliveryTimeout(config, log);

        this.apiVersions = new ApiVersions();
        
        // 事务管理器
        this.transactionManager = configureTransactionState(config, logContext);
        
        /*
        核心组件2：缓冲区对象，默认 32M
        发送消息的收集器    
            同一个分区的数据会被打包成一个batch，一个broker上多个分区对应的多个batch会被打包成一个request
        */
        this.accumulator = new RecordAccumulator(logContext,
        
                // batch.size 批次大小，默认 16K
                config.getInt(ProducerConfig.BATCH_SIZE_CONFIG),
                this.compressionType,
                
                /*
                    两次 request 间隔，如果消息非常小，很久都没有积累到 batch.size，如果到了 linger 时间就直接发送出去
                    默认 0，linger.ms
                */
                lingerMs(config), 
                retryBackoffMs,
                deliveryTimeoutMs,
                metrics,
                PRODUCER_METRIC_GROUP_NAME,
                time,
                apiVersions,
                transactionManager,
                
                // 缓冲区大小，默认 32m
                new BufferPool(this.totalMemorySize, config.getInt(ProducerConfig.BATCH_SIZE_CONFIG), metrics, time, PRODUCER_METRIC_GROUP_NAME));

        // bootstrap.servers，连接 Kafka 集群
        List<InetSocketAddress> addresses = ClientUtils.parseAndValidateAddresses(
                config.getList(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
                config.getString(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG));
        
        // 获取元数据
        if (metadata != null) {
            this.metadata = metadata;
        } else {
            /*
                metadata.max.age.ms 默认值 5 分钟。生产者每隔多久需要更新一下自己的元数据
                metadata.max.idle.ms 默认值 5 分钟。网络最多空闲时间设置，超过该阈值，就关闭该网络
            */
            this.metadata = new ProducerMetadata(retryBackoffMs,
                    config.getLong(ProducerConfig.METADATA_MAX_AGE_CONFIG),
                    config.getLong(ProducerConfig.METADATA_MAX_IDLE_CONFIG),
                    logContext,
                    clusterResourceListeners,
                    Time.SYSTEM);
            this.metadata.bootstrap(addresses);
        }
        this.errors = this.metrics.sensor("errors");
        
        // 核心组件3：Sender 线程，调用NetworkClient的方法进行消息发送，TCP请求
        this.sender = newSender(logContext, kafkaClient, this.metadata);
        String ioThreadName = NETWORK_THREAD_PREFIX + " | " + clientId;
        
        // 守护线程启动，参数一是参数名,参数二是serder,参数三设置为守护线程
        this.ioThread = new KafkaThread(ioThreadName, this.sender, true);
        
        // 启动发送消息线程
        this.ioThread.start();
        config.logUnused();
        AppInfoParser.registerAppInfo(JMX_PREFIX, clientId, metrics, time.milliseconds());
        log.debug("Kafka producer started");
    } catch (Throwable t) {
        // call close methods if internal objects are already constructed this is to prevent resource leak. see KAFKA-2121
        close(Duration.ofMillis(0), true);
        // now propagate the exception
        throw new KafkaException("Failed to construct kafka producer", t);
    }
}
```
##### sender 线程初始化
```java
Sender newSender(LogContext logContext, KafkaClient kafkaClient, ProducerMetadata metadata) {
    
    // 缓存请求的个数，默认是5个
    int maxInflightRequests = configureInflightRequests(producerConfig);
    
    // 请求超时时间，默认 30s
    int requestTimeoutMs = producerConfig.getInt(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG);
    ChannelBuilder channelBuilder = ClientUtils.createChannelBuilder(producerConfig, time, logContext);
    ProducerMetrics metricsRegistry = new ProducerMetrics(this.metrics);
    Sensor throttleTimeSensor = Sender.throttleTimeSensor(metricsRegistry.senderMetrics);
    
    /*
    核心组件4：创建 NetworkClient 对象
        clientId：客户端id
        maxInflightRequests：缓存请求个数，默认5个
        reconnect.backoff.ms：连接重试间隔，默认值 50ms
        reconnect.backoff.max.ms：总的重试时间，默认1000ms，每次重试失败时，呈指数增加重试时间，直至达到此最大值
        send.buffer.bytes：发送缓冲区大小，默认128k
        receive.buffer.bytes：接收数据缓存，默认32k
        request.timeout.ms：默认30s
        socket.connection.setup.timeout.ms：默认10s；生产者和服务器通信连接建立的时间。如果在超时之前没有建立连接，将关闭通信。
        socket.connection.setup.timeout.max.ms：默认30s；生产者和服务器通信，每次连续连接失败时，连接建立超时将呈指数增加，直至达到此最大值。
    */
    KafkaClient client = kafkaClient != null ? kafkaClient : new NetworkClient(
            new Selector(producerConfig.getLong(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG),
                    this.metrics, time, "producer", channelBuilder, logContext),
            metadata,
            clientId,
            maxInflightRequests,
            producerConfig.getLong(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG),
            producerConfig.getLong(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG),
            producerConfig.getInt(ProducerConfig.SEND_BUFFER_CONFIG),
            producerConfig.getInt(ProducerConfig.RECEIVE_BUFFER_CONFIG),
            requestTimeoutMs,
            producerConfig.getLong(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG),
            producerConfig.getLong(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG),
            time,
            true,
            apiVersions,
            throttleTimeSensor,
            logContext);
    
    /*
        acks 默认值是-1。
        acks=0, 生产者发送给 Kafka 服务器后，不需要应答
        acks=1，生产者发送给 Kafka 服务器后，Leader 接收后应答
        acks=-1（all），生产者发送给 Kafka 服务器后，Leader 和在 ISR 队列的所有 Follower 共同应答
    */
    short acks = configureAcks(producerConfig, log);
    
    /*
    创建 Sender 线程对象。
        max.request.size：一次请求最大的数据量，默认1m
        retries：重试次数，int最大值
        retry.backoff.ms：默认值 100ms。重试时间间隔
    */
    return new Sender(logContext,
            client,
            metadata,
            this.accumulator,
            maxInflightRequests == 1,
            producerConfig.getInt(ProducerConfig.MAX_REQUEST_SIZE_CONFIG),
            acks,
            producerConfig.getInt(ProducerConfig.RETRIES_CONFIG),
            metricsRegistry.senderMetrics,
            time,
            requestTimeoutMs,
            producerConfig.getLong(ProducerConfig.RETRY_BACKOFF_MS_CONFIG),
            this.transactionManager,
            apiVersions);
}
```

在初始化时会 start() Sender线程，所以分析其 run() 方法即可。在附录中可查看 run() 方法做了什么事。

##### 初始化时较重要的参数

| 参数                                    | 描述 |
| --------------------------------------- | ---- |
| retry.backoff.ms                  | 消息发送失败的重试间隔，默认值 100ms |
| metadata.max.age.ms                   | 元数据更新时间，默认是 5min，注意这个配置只是客户端定时去同步元数据(topic、partition、replica、leader、follow、isr等)，但并不是说一定是定时同步，大胆猜测一下当生产者发送消息时发现待发送的topic元数据没有缓存此时一定会去拉取一次且一定是阻塞模式；当 broker 发生变化触发元数据改变也会去拉取一次 |
| max.request.size                       | 一次请求最大的数据量，默认值 1M；这里的数据量限制不是说单条消息的大小，而是一次请求，kafka在发送消息时会将多条消息打包成一个 RecordBatch，且一个分区生成一个 RecordBatch，因分区大概率是在不同的 broker 中，因此 kafka 会将若干个 RecordBatch 按照 broker 打包成一个 request，这里的数据量是对 request 的限制 |
| buffer.memory                           | 缓冲区大小，默认值 32M；生产者对消息有打包的过程，在没有达到打包条件时生产者会将消息缓存在缓冲区中，当缓存的数据量超过该值生产者会阻塞，直到达到阻塞时间的最大值 |
| compression.type                      | 压缩，默认值 none |
| max.block.ms                          | 最大阻塞时间，默认值 60s。这里的时间从调用 send 方法开始一直到消息被发送的时间，包括上述说的缓冲区满产生的阻塞，以及元数据拉取时的阻塞都是包含在内，可以理解为一次完整的消息发送包含的时间 |
| batch.size                            | 批次大小，默认值 16K；就是 RecordBatch 的大小 |
| linger.ms                             | 两次发送的时间间隔，默认值 0；两次发送没有间隔，即来一条发送一条，这个参数主要防止当消息过小迟迟达不到 batch.size 的打包条件，导致数据延迟；因此这个配置在生产上建议配置，默认值导致生产者没有打包的操作，而极大地影响吞吐量，但又不能过大，过大会影响数据的时效性。通常的参考标准时根据 batch.size 估算数据量达到一个 batch 的时间 |
| connections.max.idle.ms               | 连接最大空闲时间，默认值 9min；为了减轻客户端服务端的压力，对于长时间不活跃的连接会根据 lru 算法进行关闭 |
| max.in.flight.requests.per.connection | 每个连接允许没有响应的最大请求数，默认值 5；消息发送成功后得到响应前的请求会被放置在内部的 in-flight 数组中，当得到响应后(无论是成功还是失败)会被从这里移除，特别是当消息发送失败后进行重试，因为不知道服务端什么时候接收成功，当该值大于 1 时会存在消息乱序的情况(即使topic只有一个分区)。 |
| reconnect.backoff.ms                  | 连接重试间隔，默认值 50ms |

#### 发送数据到缓冲区
![img.png](../image/kafka_生产者_发送数据到缓冲区流程.png)


```java
Properties properties = new Properties();
properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

producer.send(new ProducerRecord("", ""));
```
##### 发送总体流程
```java
public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
    return send(record, null);
}

public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
    // intercept the record, which can be potentially modified; this method does not throw exceptions
    
    // 拦截器，多个拦截器逐一拦截
    ProducerRecord<K, V> interceptedRecord = this.interceptors.onSend(record);
    
    // 发送消息
    return doSend(interceptedRecord, callback);
}
```
无论是同步发送还是异步发送，都会进入`send(ProducerRecord<K, V> record, Callback callback)`这个方法内。查看 doSend() 方法。实现异步地将记录发送到一个主题。
```java
private Future<RecordMetadata> doSend(ProducerRecord<K, V> record, Callback callback) {

    // 记录主题分区
    TopicPartition tp = null;
    try {
        throwIfProducerClosed();
        // first make sure the metadata for the topic is available
        long nowMs = time.milliseconds();
        ClusterAndWaitTime clusterAndWaitTime;
        try {
            /*
                从 Kafka 拉取元数据。maxBlockTimeMs 表示最多能等待多长时间
                获取 topic 的元数据，确保 topic 的元数据可用
            */
            clusterAndWaitTime = waitOnMetadata(record.topic(), record.partition(), nowMs, maxBlockTimeMs);
        } catch (KafkaException e) {
            if (metadata.isClosed())
                throw new KafkaException("Producer closed while send in progress", e);
            throw e;
        }
        nowMs += clusterAndWaitTime.waitedOnMetadataMs;
        
        // 剩余时间 = 最多能等待时间 - 用了多少时间；
        long remainingWaitMs = Math.max(0, maxBlockTimeMs - clusterAndWaitTime.waitedOnMetadataMs);
        
        // 更新集群元数据
        Cluster cluster = clusterAndWaitTime.cluster;
        
        // key/value 序列化操作
        byte[] serializedKey;
        try {
            serializedKey = keySerializer.serialize(record.topic(), record.headers(), record.key());
        } catch (ClassCastException cce) {
            throw new SerializationException("Can't convert key of class " + record.key().getClass().getName() +
                    " to class " + producerConfig.getClass(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).getName() +
                    " specified in key.serializer", cce);
        }
        byte[] serializedValue;
        try {
            serializedValue = valueSerializer.serialize(record.topic(), record.headers(), record.value());
        } catch (ClassCastException cce) {
            throw new SerializationException("Can't convert value of class " + record.value().getClass().getName() +
                    " to class " + producerConfig.getClass(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG).getName() +
                    " specified in value.serializer", cce);
        }
        
        /*
        分区分配操作：
            指定分区：直接返回；
            指定key：按key的hashcode对分区数取余；
            都没指定：粘性分区
            也可以自定义分区器，替换默认分区
        */
        int partition = partition(record, serializedKey, serializedValue, cluster);
        
        // 将消息发送到哪个主题，哪个分区
        tp = new TopicPartition(record.topic(), partition);

        // 设置头信息
        setReadOnly(record.headers());
        Header[] headers = record.headers().toArray();

        // 计算序列化后的消息大小
        int serializedSize = AbstractRecords.estimateSizeInBytesUpperBound(apiVersions.maxUsableProduceMagic(),
                compressionType, serializedKey, serializedValue, headers);
                
        /*
            校验发送信息是否超过最大值
            一次请求获取消息的最大值，默认是1m，缓冲区内存总大小，默认32m
        */
        ensureValidRecordSize(serializedSize);
        long timestamp = record.timestamp() == null ? nowMs : record.timestamp();
        if (log.isTraceEnabled()) {
            log.trace("Attempting to append record {} with callback {} to topic {} partition {}", record, callback, record.topic(), partition);
        }
        // producer callback will make sure to call both 'callback' and interceptor callback
        
        // 消息发送的回调函数，消息从 broker 确认后，要调用的拦截器
        Callback interceptCallback = new InterceptorCallback<>(callback, this.interceptors, tp);

        if (transactionManager != null && transactionManager.isTransactional()) {
            transactionManager.failIfNotReadyForSend();
        }
        
        /*
            将消息追加到消息累加器中的 batch 尾部
            向缓冲区发送数据，双端队列，内存默认 32m，里面是默认 16k 一个批次
        */
        RecordAccumulator.RecordAppendResult result = accumulator.append(tp, timestamp, serializedKey,
                serializedValue, headers, interceptCallback, remainingWaitMs, true, nowMs);

        if (result.abortForNewBatch) {
            int prevPartition = partition;
            partitioner.onNewBatch(record.topic(), cluster, prevPartition);
            partition = partition(record, serializedKey, serializedValue, cluster);
            tp = new TopicPartition(record.topic(), partition);
            if (log.isTraceEnabled()) {
                log.trace("Retrying append due to new batch creation for topic {} partition {}. The old partition was {}", record.topic(), partition, prevPartition);
            }
            // producer callback will make sure to call both 'callback' and interceptor callback
            interceptCallback = new InterceptorCallback<>(callback, this.interceptors, tp);

            result = accumulator.append(tp, timestamp, serializedKey,
                serializedValue, headers, interceptCallback, remainingWaitMs, false, nowMs);
        }

        // 如果是事务消息，设置事务管理器
        if (transactionManager != null && transactionManager.isTransactional())
            transactionManager.maybeAddPartitionToTransaction(tp);

        /*
        发送条件满足了（batch.size,批次大小已经满了，或者有一个新的批次创建），唤醒 sender 发送线程
            如果消息消息累加器中对应的分区满足一个批次（16k），则唤醒发送线程
            如果 linger.ms 时间到了，则需要创建新的批次，则唤醒发送线程
        */
        if (result.batchIsFull || result.newBatchCreated) {
            log.trace("Waking up the sender since topic {} partition {} is either full or getting a new batch", record.topic(), partition);
            this.sender.wakeup();
        }
        
        // 返回 future 对象
        return result.future;
        // handling exceptions and record the errors;
        // for API exceptions return them in the future,
        // for other exceptions throw directly
    } catch (ApiException e) {
        log.debug("Exception occurred during message send:", e);
        if (callback != null)
            callback.onCompletion(null, e);
        this.errors.record();
        this.interceptors.onSendError(record, tp, e);
        return new FutureFailure(e);
    } catch (InterruptedException e) {
        this.errors.record();
        this.interceptors.onSendError(record, tp, e);
        throw new InterruptException(e);
    } catch (KafkaException e) {
        this.errors.record();
        this.interceptors.onSendError(record, tp, e);
        throw e;
    } catch (Exception e) {
        // we notify interceptor about all exceptions, since onSend is called before anything else in this method
        this.interceptors.onSendError(record, tp, e);
        throw e;
    }
}
```

#### sender 线程发送数据
![img.png](../image/kafka_生产者_sender线程发送数据流程.png)

##### 流程
```java
public void run() {
    log.debug("Starting Kafka producer I/O thread.");

    // main loop, runs until close is called
    while (running) {
        try {
            // sender 线程从缓冲区准备拉取数据
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }
    
    // ... ...
}
```
runOnce()
```java
void runOnce() {
    
    // 事务相关操作，默认不开启，null
    if (transactionManager != null) {
        try {
            transactionManager.maybeResolveSequences();

            // do not continue sending if the transaction manager is in a failed state
            if (transactionManager.hasFatalError()) {
                RuntimeException lastError = transactionManager.lastError();
                if (lastError != null)
                    maybeAbortBatches(lastError);
                client.poll(retryBackoffMs, time.milliseconds());
                return;
            }

            // Check whether we need a new producerId. If so, we will enqueue an InitProducerId
            // request which will be sent below
            transactionManager.bumpIdempotentEpochAndResetIdIfNeeded();

            if (maybeSendAndPollTransactionalRequest()) {
                return;
            }
        } catch (AuthenticationException e) {
            // This is already logged as error, but propagated here to perform any clean ups.
            log.trace("Authentication exception while processing transactional request", e);
            transactionManager.authenticationFailed(e);
        }
    }

    long currentTimeMs = time.milliseconds();
    
    // 将准备号的数据发送到服务端
    long pollTimeout = sendProducerData(currentTimeMs);
    
    // 等待发送响应
    client.poll(pollTimeout, currentTimeMs);
}
```
sendProducerData()
```java
private long sendProducerData(long now) {

    // 获取集群元数据
    Cluster cluster = metadata.fetch();
    // get the list of partitions with data ready to send
    
    // 1、检查 32m缓存是否准备好（linger.ms 时间是否满足）
    RecordAccumulator.ReadyCheckResult result = this.accumulator.ready(cluster, now);

    // if there are any partitions whose leaders are not known yet, force metadata update
    
    // 如果不知道 Leader 信息，则不能发送数据
    if (!result.unknownLeaderTopics.isEmpty()) {
        // The set of topics with unknown leader contains topics with leader election pending as well as
        // topics which may have expired. Add the topic again to metadata to ensure it is included
        // and request metadata update, since there are messages to send to the topic.
        for (String topic : result.unknownLeaderTopics)
            this.metadata.add(topic, now);

        log.debug("Requesting metadata update due to unknown leader topics from the batched records: {}",
            result.unknownLeaderTopics);
        this.metadata.requestUpdate();
    }

    // remove any nodes we aren't ready to send to
    
    // 删除还没有准备好发送的数据
    Iterator<Node> iter = result.readyNodes.iterator();
    long notReadyTimeout = Long.MAX_VALUE;
    while (iter.hasNext()) {
        Node node = iter.next();
        if (!this.client.ready(node, now)) {
            iter.remove();
            notReadyTimeout = Math.min(notReadyTimeout, this.client.pollDelayMs(node, now));
        }
    }

    // create produce requests
    
    // 2、发送到同一个 broker 节点的数据，打包为一个请求批次
    Map<Integer, List<ProducerBatch>> batches = this.accumulator.drain(cluster, result.readyNodes, this.maxRequestSize, now);
    addToInflightBatches(batches);
    
    // 消息有序性，默认 true
    if (guaranteeMessageOrder) {
        // Mute all the partitions drained
        for (List<ProducerBatch> batchList : batches.values()) {
            for (ProducerBatch batch : batchList)
                this.accumulator.mutePartition(batch.topicPartition);
        }
    }

    accumulator.resetNextBatchExpiryTime();
    List<ProducerBatch> expiredInflightBatches = getExpiredInflightBatches(now);
    
    // 过期的批次
    List<ProducerBatch> expiredBatches = this.accumulator.expiredBatches(now);
    expiredBatches.addAll(expiredInflightBatches);

    // Reset the producer id if an expired batch has previously been sent to the broker. Also update the metrics
    // for expired batches. see the documentation of @TransactionState.resetIdempotentProducerId to understand why
    // we need to reset the producer id here.
    if (!expiredBatches.isEmpty())
        log.trace("Expired {} batches in accumulator", expiredBatches.size());
    for (ProducerBatch expiredBatch : expiredBatches) {
        String errorMessage = "Expiring " + expiredBatch.recordCount + " record(s) for " + expiredBatch.topicPartition
            + ":" + (now - expiredBatch.createdMs) + " ms has passed since batch creation";
        failBatch(expiredBatch, new TimeoutException(errorMessage), false);
        if (transactionManager != null && expiredBatch.inRetry()) {
            // This ensures that no new batches are drained until the current in flight batches are fully resolved.
            transactionManager.markSequenceUnresolved(expiredBatch);
        }
    }
    sensors.updateProduceRequestMetrics(batches);

    // If we have any nodes that are ready to send + have sendable data, poll with 0 timeout so this can immediately
    // loop and try sending more data. Otherwise, the timeout will be the smaller value between next batch expiry
    // time, and the delay time for checking data availability. Note that the nodes may have data that isn't yet
    // sendable due to lingering, backing off, etc. This specifically does not include nodes with sendable data
    // that aren't ready to send since they would cause busy looping.
    long pollTimeout = Math.min(result.nextReadyCheckDelayMs, notReadyTimeout);
    pollTimeout = Math.min(pollTimeout, this.accumulator.nextExpiryTimeMs() - now);
    pollTimeout = Math.max(pollTimeout, 0);
    if (!result.readyNodes.isEmpty()) {
        log.trace("Nodes with data ready to send: {}", result.readyNodes);
        // if some partitions are already ready to be sent, the select time would be 0;
        // otherwise if some partition already has some data accumulated but not ready yet,
        // the select time will be the time difference between now and its linger expiry time;
        // otherwise the select time will be the time difference between now and the metadata expiry time;
        pollTimeout = 0;
    }
    
    3、发送数据
    sendProduceRequests(batches, now);
    return pollTimeout;
}
```
ready()，检查32m缓存是否准备好（linger.ms 时间是否到）
```java
public ReadyCheckResult ready(Cluster cluster, long nowMs) {
    Set<Node> readyNodes = new HashSet<>();
    long nextReadyCheckDelayMs = Long.MAX_VALUE;
    Set<String> unknownLeaderTopics = new HashSet<>();

    boolean exhausted = this.free.queued() > 0;
    for (Map.Entry<TopicPartition, Deque<ProducerBatch>> entry : this.batches.entrySet()) {
        Deque<ProducerBatch> deque = entry.getValue();
        synchronized (deque) {
            // When producing to a large number of partitions, this path is hot and deques are often empty.
            // We check whether a batch exists first to avoid the more expensive checks whenever possible.
            ProducerBatch batch = deque.peekFirst();
            if (batch != null) {
                TopicPartition part = entry.getKey();
                Node leader = cluster.leaderFor(part);
                if (leader == null) {
                    // This is a partition for which leader is not known, but messages are available to send.
                    // Note that entries are currently not removed from batches when deque is empty.
                    unknownLeaderTopics.add(part.topic());
                } else if (!readyNodes.contains(leader) && !isMuted(part)) {
                    long waitedTimeMs = batch.waitedTimeMs(nowMs);
                    
                    // 如果不是第一次拉取该批次数据，且等待时间没有超过重试时间，backingOff=true
                    boolean backingOff = batch.attempts() > 0 && waitedTimeMs < retryBackoffMs;
                    
                    // 如果 backingOff=true，返回重试时间，如果不是重试，选择 lingerMs
                    long timeToWaitMs = backingOff ? retryBackoffMs : lingerMs;
                    boolean full = deque.size() > 1 || batch.isFull();
                    
                    // 如果等待的时间超过了 timeToWaitMs，expired=true，表示可以发送数据
                    boolean expired = waitedTimeMs >= timeToWaitMs;
                    boolean transactionCompleting = transactionManager != null && transactionManager.isCompleting();
                    boolean sendable = full
                        || expired
                        || exhausted
                        || closed
                        || flushInProgress()
                        || transactionCompleting;
                    if (sendable && !backingOff) {
                        readyNodes.add(leader);
                    } else {
                        long timeLeftMs = Math.max(timeToWaitMs - waitedTimeMs, 0);
                        // Note that this results in a conservative estimate since an un-sendable partition may have
                        // a leader that will later be found to have sendable data. However, this is good enough
                        // since we'll just wake up and then sleep again for the remaining time.
                        nextReadyCheckDelayMs = Math.min(timeLeftMs, nextReadyCheckDelayMs);
                    }
                }
            }
        }
    }
    return new ReadyCheckResult(readyNodes, nextReadyCheckDelayMs, unknownLeaderTopics);
}
```
sendProduceRequests()，发送请求
```java
private void sendProduceRequests(Map<Integer, List<ProducerBatch>> collated, long now) {
    for (Map.Entry<Integer, List<ProducerBatch>> entry : collated.entrySet())
        sendProduceRequest(now, entry.getKey(), acks, requestTimeoutMs, entry.getValue());
}

private void sendProduceRequest(long now, int destination, short acks, int timeout, List<ProducerBatch> batches) {
    if (batches.isEmpty())
        return;

    final Map<TopicPartition, ProducerBatch> recordsByPartition = new HashMap<>(batches.size());

    // find the minimum magic version used when creating the record sets
    byte minUsedMagic = apiVersions.maxUsableProduceMagic();
    for (ProducerBatch batch : batches) {
        if (batch.magic() < minUsedMagic)
            minUsedMagic = batch.magic();
    }
    ProduceRequestData.TopicProduceDataCollection tpd = new ProduceRequestData.TopicProduceDataCollection();
    for (ProducerBatch batch : batches) {
        TopicPartition tp = batch.topicPartition;
        MemoryRecords records = batch.records();

        // down convert if necessary to the minimum magic used. In general, there can be a delay between the time
        // that the producer starts building the batch and the time that we send the request, and we may have
        // chosen the message format based on out-dated metadata. In the worst case, we optimistically chose to use
        // the new message format, but found that the broker didn't support it, so we need to down-convert on the
        // client before sending. This is intended to handle edge cases around cluster upgrades where brokers may
        // not all support the same message format version. For example, if a partition migrates from a broker
        // which is supporting the new magic version to one which doesn't, then we will need to convert.
        if (!records.hasMatchingMagic(minUsedMagic))
            records = batch.records().downConvert(minUsedMagic, 0, time).records();
        ProduceRequestData.TopicProduceData tpData = tpd.find(tp.topic());
        if (tpData == null) {
            tpData = new ProduceRequestData.TopicProduceData().setName(tp.topic());
            tpd.add(tpData);
        }
        tpData.partitionData().add(new ProduceRequestData.PartitionProduceData()
                .setIndex(tp.partition())
                .setRecords(records));
        recordsByPartition.put(tp, batch);
    }

    String transactionalId = null;
    if (transactionManager != null && transactionManager.isTransactional()) {
        transactionalId = transactionManager.transactionalId();
    }

    ProduceRequest.Builder requestBuilder = ProduceRequest.forMagic(minUsedMagic,
            new ProduceRequestData()
                    .setAcks(acks)
                    .setTimeoutMs(timeout)
                    .setTransactionalId(transactionalId)
                    .setTopicData(tpd));
    RequestCompletionHandler callback = response -> handleProduceResponse(response, recordsByPartition, time.milliseconds());

    String nodeId = Integer.toString(destination);
    
    // 创建发送请求对象
    ClientRequest clientRequest = client.newClientRequest(nodeId, requestBuilder, now, acks != 0,
            requestTimeoutMs, callback);
            
    // 发送请求
    client.send(clientRequest, now);
    log.trace("Sent produce request to {}: {}", nodeId, requestBuilder);
}
```
org.apache.kafka.clients.NetworkClient#send()
```java
public void send(ClientRequest request, long now) {
    doSend(request, false, now);
}
private void doSend(ClientRequest clientRequest, boolean isInternalRequest, long now) {
    ensureActive();
    String nodeId = clientRequest.destination();
    if (!isInternalRequest) {
        // If this request came from outside the NetworkClient, validate
        // that we can send data.  If the request is internal, we trust
        // that internal code has done this validation.  Validation
        // will be slightly different for some internal requests (for
        // example, ApiVersionsRequests can be sent prior to being in
        // READY state.)
        if (!canSendRequest(nodeId, now))
            throw new IllegalStateException("Attempt to send a request to node " + nodeId + " which is not ready.");
    }
    AbstractRequest.Builder<?> builder = clientRequest.requestBuilder();
    try {
        NodeApiVersions versionInfo = apiVersions.get(nodeId);
        short version;
        // Note: if versionInfo is null, we have no server version information. This would be
        // the case when sending the initial ApiVersionRequest which fetches the version
        // information itself.  It is also the case when discoverBrokerVersions is set to false.
        if (versionInfo == null) {
            version = builder.latestAllowedVersion();
            if (discoverBrokerVersions && log.isTraceEnabled())
                log.trace("No version information found when sending {} with correlation id {} to node {}. " +
                        "Assuming version {}.", clientRequest.apiKey(), clientRequest.correlationId(), nodeId, version);
        } else {
            version = versionInfo.latestUsableVersion(clientRequest.apiKey(), builder.oldestAllowedVersion(),
                    builder.latestAllowedVersion());
        }
        // The call to build may also throw UnsupportedVersionException, if there are essential
        // fields that cannot be represented in the chosen version.
        
        // 发送请求，方法重载
        doSend(clientRequest, isInternalRequest, now, builder.build(version));
    } catch (UnsupportedVersionException unsupportedVersionException) {
        ...
    }
}

private void doSend(ClientRequest clientRequest, boolean isInternalRequest, long now, AbstractRequest request) {
    String destination = clientRequest.destination();
    RequestHeader header = clientRequest.makeHeader(request.version());
    if (log.isDebugEnabled()) {
        log.debug("Sending {} request with header {} and timeout {} to node {}: {}",
            clientRequest.apiKey(), header, clientRequest.requestTimeoutMs(), destination, request);
    }
    Send send = request.toSend(header);
    InFlightRequest inFlightRequest = new InFlightRequest(
            clientRequest,
            header,
            isInternalRequest,
            request,
            send,
            now);
    
    // 添加请求到 inFlint
    this.inFlightRequests.add(inFlightRequest);
    
    // 发送数据，触发写事件
    selector.send(new NetworkSend(clientRequest.destination(), send));
}
```
消息发送之后，需要等待响应，`client.poll(pollTimeout, currentTimeMs);`
```java
public List<ClientResponse> poll(long timeout, long now) {
    ensureActive();

    if (!abortedSends.isEmpty()) {
        // If there are aborted sends because of unsupported version exceptions or disconnects,
        // handle them immediately without waiting for Selector#poll.
        List<ClientResponse> responses = new ArrayList<>();
        handleAbortedSends(responses);
        completeResponses(responses);
        return responses;
    }

    long metadataTimeout = metadataUpdater.maybeUpdate(now);
    try {
        this.selector.poll(Utils.min(timeout, metadataTimeout, defaultRequestTimeoutMs));
    } catch (IOException e) {
        log.error("Unexpected error during I/O", e);
    }

    // process completed actions
    
    // 获取发送后的响应
    long updatedNow = this.time.milliseconds();
    List<ClientResponse> responses = new ArrayList<>();
    handleCompletedSends(responses, updatedNow);
    handleCompletedReceives(responses, updatedNow);
    handleDisconnections(responses, updatedNow);
    handleConnections();
    handleInitiateApiVersionRequests(updatedNow);
    handleTimedOutConnections(responses, updatedNow);
    handleTimedOutRequests(responses, updatedNow);
    completeResponses(responses);

    return responses;
}
```
### 消费者源码
#### 消费者组初始化流程
![](../image/kafka_consumer_消费者组初始化流程.png)
#### 消费者组详细消费流程
![](../image/kafka_consumer_消费者组详细消费流程.png)
#### 初始化
![](../image/kafka_consumer_消费者初始化.png)
##### 程序入口
```java
// 1、创建消费者的配置对象
Properties properties = new Properties();

// 2、给消费者配置对象添加参数
properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.228.147:9092,192.168.228.148:9092,192.168.228.149:9092");

// 配置序列化，必须
properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

ArrayList<String> startegys = new ArrayList<>();
startegys.add(StickyAssignor.class.getName());
properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, startegys);

// 配置消费者组（组名任意起）必须
properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");

// 手动提交
properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);

// 创建消费者对象
KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(properties);

// 注册要消费的主题（可以消费多个主题）
ArrayList<String> topics = new ArrayList<>();
topics.add("first");
kafkaConsumer.subscribe(topics);

// 拉取数据打印
while (true) {
    // 设置1s中消费一批数据
    ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(1));
    // 打印消费到的数据
    for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
        System.out.println(consumerRecord);
    }
    kafkaConsumer.commitSync();
    // kafkaConsumer.commitAsync();
}
```
##### 消费者初始化
构造方法重载
```java
public KafkaConsumer(Properties properties) {
    this(properties, null, null);
}
public KafkaConsumer(Properties properties,
                     Deserializer<K> keyDeserializer,
                     Deserializer<V> valueDeserializer) {
    this(Utils.propsToMap(properties), keyDeserializer, valueDeserializer);
}
public KafkaConsumer(Map<String, Object> configs,
                     Deserializer<K> keyDeserializer,
                     Deserializer<V> valueDeserializer) {
    this(new ConsumerConfig(ConsumerConfig.appendDeserializerToConfig(configs, keyDeserializer, valueDeserializer)),
            keyDeserializer, valueDeserializer);
}
```
```java
KafkaConsumer(ConsumerConfig config, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
    try {
        GroupRebalanceConfig groupRebalanceConfig = new GroupRebalanceConfig(config,
                GroupRebalanceConfig.ProtocolType.CONSUMER);

        this.groupId = Optional.ofNullable(groupRebalanceConfig.groupId);
        this.clientId = config.getString(CommonClientConfigs.CLIENT_ID_CONFIG);

        LogContext logContext;

        // If group.instance.id is set, we will append it to the log context.
        if (groupRebalanceConfig.groupInstanceId.isPresent()) {
            logContext = new LogContext("[Consumer instanceId=" + groupRebalanceConfig.groupInstanceId.get() +
                    ", clientId=" + clientId + ", groupId=" + groupId.orElse("null") + "] ");
        } else {
            logContext = new LogContext("[Consumer clientId=" + clientId + ", groupId=" + groupId.orElse("null") + "] ");
        }

        this.log = logContext.logger(getClass());
        boolean enableAutoCommit = config.maybeOverrideEnableAutoCommit();
        groupId.ifPresent(groupIdStr -> {
            if (groupIdStr.isEmpty()) {
                log.warn("Support for using the empty group id by consumers is deprecated and will be removed in the next major release.");
            }
        });

        log.debug("Initializing the Kafka consumer");
        this.requestTimeoutMs = config.getInt(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG);
        this.defaultApiTimeoutMs = config.getInt(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG);
        this.time = Time.SYSTEM;
        this.metrics = buildMetrics(config, time, clientId);
        this.retryBackoffMs = config.getLong(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG);

        List<ConsumerInterceptor<K, V>> interceptorList = (List) config.getConfiguredInstances(
                ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
                ConsumerInterceptor.class,
                Collections.singletonMap(ConsumerConfig.CLIENT_ID_CONFIG, clientId));
        this.interceptors = new ConsumerInterceptors<>(interceptorList);
        if (keyDeserializer == null) {
            this.keyDeserializer = config.getConfiguredInstance(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Deserializer.class);
            this.keyDeserializer.configure(config.originals(Collections.singletonMap(ConsumerConfig.CLIENT_ID_CONFIG, clientId)), true);
        } else {
            config.ignore(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
            this.keyDeserializer = keyDeserializer;
        }
        if (valueDeserializer == null) {
            this.valueDeserializer = config.getConfiguredInstance(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Deserializer.class);
            this.valueDeserializer.configure(config.originals(Collections.singletonMap(ConsumerConfig.CLIENT_ID_CONFIG, clientId)), false);
        } else {
            config.ignore(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
            this.valueDeserializer = valueDeserializer;
        }
        OffsetResetStrategy offsetResetStrategy = OffsetResetStrategy.valueOf(config.getString(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG).toUpperCase(Locale.ROOT));
        this.subscriptions = new SubscriptionState(logContext, offsetResetStrategy);
        ClusterResourceListeners clusterResourceListeners = configureClusterResourceListeners(keyDeserializer,
                valueDeserializer, metrics.reporters(), interceptorList);
        this.metadata = new ConsumerMetadata(retryBackoffMs,
                config.getLong(ConsumerConfig.METADATA_MAX_AGE_CONFIG),
                !config.getBoolean(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG),
                config.getBoolean(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG),
                subscriptions, logContext, clusterResourceListeners);
        List<InetSocketAddress> addresses = ClientUtils.parseAndValidateAddresses(
                config.getList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), config.getString(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG));
        this.metadata.bootstrap(addresses);
        String metricGrpPrefix = "consumer";

        FetcherMetricsRegistry metricsRegistry = new FetcherMetricsRegistry(Collections.singleton(CLIENT_ID_METRIC_TAG), metricGrpPrefix);
        ChannelBuilder channelBuilder = ClientUtils.createChannelBuilder(config, time, logContext);
        this.isolationLevel = IsolationLevel.valueOf(
                config.getString(ConsumerConfig.ISOLATION_LEVEL_CONFIG).toUpperCase(Locale.ROOT));
        Sensor throttleTimeSensor = Fetcher.throttleTimeSensor(metrics, metricsRegistry);
        int heartbeatIntervalMs = config.getInt(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG);

        ApiVersions apiVersions = new ApiVersions();
        NetworkClient netClient = new NetworkClient(
                new Selector(config.getLong(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG), metrics, time, metricGrpPrefix, channelBuilder, logContext),
                this.metadata,
                clientId,
                100, // a fixed large enough value will suffice for max in-flight requests
                config.getLong(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG),
                config.getLong(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG),
                config.getInt(ConsumerConfig.SEND_BUFFER_CONFIG),
                config.getInt(ConsumerConfig.RECEIVE_BUFFER_CONFIG),
                config.getInt(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG),
                config.getLong(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG),
                config.getLong(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG),
                time,
                true,
                apiVersions,
                throttleTimeSensor,
                logContext);
        this.client = new ConsumerNetworkClient(
                logContext,
                netClient,
                metadata,
                time,
                retryBackoffMs,
                config.getInt(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG),
                heartbeatIntervalMs); //Will avoid blocking an extended period of time to prevent heartbeat thread starvation

        this.assignors = ConsumerPartitionAssignor.getAssignorInstances(
                config.getList(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG),
                config.originals(Collections.singletonMap(ConsumerConfig.CLIENT_ID_CONFIG, clientId))
        );

        // no coordinator will be constructed for the default (null) group id
        this.coordinator = !groupId.isPresent() ? null :
            new ConsumerCoordinator(groupRebalanceConfig,
                    logContext,
                    this.client,
                    assignors,
                    this.metadata,
                    this.subscriptions,
                    metrics,
                    metricGrpPrefix,
                    this.time,
                    enableAutoCommit,
                    config.getInt(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG),
                    this.interceptors,
                    config.getBoolean(ConsumerConfig.THROW_ON_FETCH_STABLE_OFFSET_UNSUPPORTED));
        this.fetcher = new Fetcher<>(
                logContext,
                this.client,
                config.getInt(ConsumerConfig.FETCH_MIN_BYTES_CONFIG),
                config.getInt(ConsumerConfig.FETCH_MAX_BYTES_CONFIG),
                config.getInt(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG),
                config.getInt(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG),
                config.getInt(ConsumerConfig.MAX_POLL_RECORDS_CONFIG),
                config.getBoolean(ConsumerConfig.CHECK_CRCS_CONFIG),
                config.getString(ConsumerConfig.CLIENT_RACK_CONFIG),
                this.keyDeserializer,
                this.valueDeserializer,
                this.metadata,
                this.subscriptions,
                metrics,
                metricsRegistry,
                this.time,
                this.retryBackoffMs,
                this.requestTimeoutMs,
                isolationLevel,
                apiVersions);

        this.kafkaConsumerMetrics = new KafkaConsumerMetrics(metrics, metricGrpPrefix);

        config.logUnused();
        AppInfoParser.registerAppInfo(JMX_PREFIX, clientId, metrics, time.milliseconds());
        log.debug("Kafka consumer initialized");
    } catch (Throwable t) {
        // call close methods if internal objects are already constructed; this is to prevent resource leak. see KAFKA-2121
        // we do not need to call `close` at all when `log` is null, which means no internal objects were initialized.
        if (this.log != null) {
            close(0, true);
        }
        // now propagate the exception
        throw new KafkaException("Failed to construct kafka consumer", t);
    }
}
```
#### 订阅主题
#### 拉取和处理数据
##### 消费者/消费者组初始化
##### 拉取数据 
##### 拦截器处理数据
#### offset提交
##### offset手动同步提交
##### offset手动异步提交
### 服务器源码
## 附录
### Kafka 命令
```txt
# ----------------------kafka-console-producer.sh----------------------------
# 连接 Broker，并指定要往哪个主题写数据
[root@server7 bin]# ./kafka-console-producer.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo

# ----------------------kafka-topics.sh----------------------------
# 查看所有主题
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --list

# 创建一个名为 sanguo 的主题，1 个分区，3 个副本
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --create --partitions 1 --replication-factor 3

# 查看名为 sanguo 主题的详细信息
[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --describe
Topic: sanguo	TopicId: EJah9n4zQFSGZq4xg7HW1g	PartitionCount: 1	ReplicationFactor: 3	Configs: segment.bytes=1073741824
	Topic: sanguo	Partition: 0	Leader: 1	Replicas: 1,0,2	Isr: 1,0,2
# Partition：1 个分区，从0开始。broker.id=1 的是 Leader。Replicas：副本分散在三台服务器上

[root@192 bin]# ./kafka-topics.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --alter --partitions 3

# ----------------------kafka-console-consumer.sh----------------------------
# 连接 Broker，并指定要往哪个主题读数据，`--from-beginning`参数可以读取历史数据
[root@192 bin]# ./kafka-console-consumer.sh --bootstrap-server 192.168.44.132:9092 --topic sanguo --from-beginning



# Kafka 服务端启动
/mydata/kafka/bin/kafka-server-start.sh -daemon /mydata/kafka/config/server.properties

```
#### kafka-topics.sh
| 参数                                               | 描述                              |
| -------------------------------------------------- | --------------------------------- |
| --bootstrap-server <String: server to connect to>  | 连接 Kafka Broker 的 ip:prot 地址 |
| --topic <String: topic>                            | 指定 需要操作的 topic 名称        |
| --create                                           | 创建主题                          |
| --delete                                           | 删除主题                          |
| --alter                                            | 修改主题                          |
| --list                                             | 查看所有主题                      |
| --describe                                         | 查看 主题消息描述                 |
| --partitions <Integer: # of partitions>            | 设置分区数                        |
| --replication-factor <Integer: replication factor> | 设置分区副本                      |
| --config <String: name=value>                      | 更新系统默认的配置                |



### 启动脚本

```shell
#!/bin/bash

kafkaServers='192.168.44.129 192.168.44.131 192.168.44.132'
case $1 in 
"start")
	for k in $kafkaServers
	do
			echo ---------- Kafka $k 启动 ----------
			ssh $k "/mydata/kafka/bin/kafka-server-start.sh -daemon /mydata/kafka/config/server.properties"
	done
;;
"stop")
	for k in $kafkaServers
	do
			echo ---------- Kafka $k 停止 ----------
			ssh $k "/mydata/kafka/bin/kafka-server-stop.sh "
	done
;;
esac
```

### Sender#run()
```java
/**
 * The main run loop for the sender thread
 */
@Override
public void run() {
    log.debug("Starting Kafka producer I/O thread.");

    // main loop, runs until close is called
    while (running) {
        try {
            // sender 线程从缓冲区准备拉取数据，刚启动拉不到数据
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }

    log.debug("Beginning shutdown of Kafka producer I/O thread, sending remaining records.");

    // okay we stopped accepting requests but there may still be
    // requests in the transaction manager, accumulator or waiting for acknowledgment,
    // wait until these are completed.
    while (!forceClose && ((this.accumulator.hasUndrained() || this.client.inFlightRequestCount() > 0) || hasPendingTransactionalRequests())) {
        try {
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }

    // Abort the transaction if any commit or abort didn't go through the transaction manager's queue
    while (!forceClose && transactionManager != null && transactionManager.hasOngoingTransaction()) {
        if (!transactionManager.isCompleting()) {
            log.info("Aborting incomplete transaction due to shutdown");
            transactionManager.beginAbort();
        }
        try {
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }

    if (forceClose) {
        // We need to fail all the incomplete transactional requests and batches and wake up the threads waiting on
        // the futures.
        if (transactionManager != null) {
            log.debug("Aborting incomplete transactional requests due to forced shutdown");
            transactionManager.close();
        }
        log.debug("Aborting incomplete batches due to forced shutdown");
        this.accumulator.abortIncompleteBatches();
    }
    try {
        this.client.close();
    } catch (Exception e) {
        log.error("Failed to close network client", e);
    }

    log.debug("Shutdown of Kafka producer I/O thread has completed.");
}
```
### 拦截器的执行
```java
public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
    ProducerRecord<K, V> interceptRecord = record;
    
    // 遍历拦截器
    for (ProducerInterceptor<K, V> interceptor : this.interceptors) {
        try {
            // 拦截器处理
            interceptRecord = interceptor.onSend(interceptRecord);
        } catch (Exception e) {
            // do not propagate interceptor exception, log and continue calling other interceptors
            // be careful not to throw exception from here
            if (record != null)
                log.warn("Error executing interceptor onSend callback for topic: {}, partition: {}", record.topic(), record.partition(), e);
            else
                log.warn("Error executing interceptor onSend callback", e);
        }
    }
    return interceptRecord;
}
```
### 分区选择
```java
private int partition(ProducerRecord<K, V> record, byte[] serializedKey, byte[] serializedValue, Cluster cluster) {
    Integer partition = record.partition();
    return partition != null ?
            partition :
            partitioner.partition(
                    record.topic(), record.key(), serializedKey, record.value(), serializedValue, cluster);
}
```
默认分区是 DefaultPartitioner。
```java
public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
    return partition(topic, key, keyBytes, value, valueBytes, cluster, cluster.partitionsForTopic(topic).size());
}

public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster,
                     int numPartitions) {
    // 没有指定key，按粘性分区处理
    if (keyBytes == null) {
        return stickyPartitionCache.partition(topic, cluster);
    }
    // hash the keyBytes to choose a partition
    
    // 执行key，按key的hashcode对分区数取余
    return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
}
```
### 校验发送消息大小
```java
private void ensureValidRecordSize(int size) {
    // 一次请求获取消息的最大值，默认是 1m
    if (size > maxRequestSize)
        throw new RecordTooLargeException("The message is " + size +
                " bytes when serialized which is larger than " + maxRequestSize + ", which is the value of the " +
                ProducerConfig.MAX_REQUEST_SIZE_CONFIG + " configuration.");
                
    // 缓冲区内存总大小，默认 32m
    if (size > totalMemorySize)
        throw new RecordTooLargeException("The message is " + size +
                " bytes when serialized which is larger than the total memory buffer you have configured with the " +
                ProducerConfig.BUFFER_MEMORY_CONFIG +
                " configuration.");
}
```
### 缓冲区
向缓冲区发送数据，双端队列，内存默认 32m，默认 16k 一个批次
```java
public RecordAppendResult append(TopicPartition tp,
                                     long timestamp,
                                     byte[] key,
                                     byte[] value,
                                     Header[] headers,
                                     Callback callback,
                                     long maxTimeToBlock,
                                     boolean abortOnNewBatch,
                                     long nowMs) throws InterruptedException {
    // We keep track of the number of appending thread to make sure we do not miss batches in
    // abortIncompleteBatches().
    appendsInProgress.incrementAndGet();
    ByteBuffer buffer = null;
    if (headers == null) headers = Record.EMPTY_HEADERS;
    
    // 生产者是多线程的，所以下面采用加锁synchronized和双重追加数据检查机制tryAppend处理Deque
    try {
        // check if we have an in-progress batch
        
        // 为每个分区创建或者获取一个队列
        Deque<ProducerBatch> dq = getOrCreateDeque(tp);
        synchronized (dq) {
            if (closed)
                throw new KafkaException("Producer closed while send in progress");
            
            // 第一次尝试：尝试向队列中添加数据，（没有分配内存、批次对象，所以添加失败）
            RecordAppendResult appendResult = tryAppend(timestamp, key, value, headers, callback, dq, nowMs);
            if (appendResult != null)
                return appendResult;
        }

        // we don't have an in-progress record batch try to allocate a new batch
        if (abortOnNewBatch) {
            // Return a result that will cause another call to append.
            return new RecordAppendResult(null, false, false, true);
        }

        byte maxUsableMagic = apiVersions.maxUsableProduceMagic();
        
        // batchSize和一次请求的大小比较，返回较大的。（可能一条请求的大小大于批次大小。）
        int size = Math.max(this.batchSize, AbstractRecords.estimateSizeInBytesUpperBound(maxUsableMagic, compression, key, value, headers));
        log.trace("Allocating a new {} byte message buffer for topic {} partition {} with remaining timeout {}ms", size, tp.topic(), tp.partition(), maxTimeToBlock);
        
        // 分配内存
        buffer = free.allocate(size, maxTimeToBlock);

        // Update the current time in case the buffer allocation blocked above.
        nowMs = time.milliseconds();
        synchronized (dq) {
            // Need to check if producer is closed again after grabbing the dequeue lock.
            if (closed)
                throw new KafkaException("Producer closed while send in progress");

            // 第二次尝试：尝试向队列中添加数据，已分配内存，但是没有批次对象
            RecordAppendResult appendResult = tryAppend(timestamp, key, value, headers, callback, dq, nowMs);
            if (appendResult != null) {
                // Somebody else found us a batch, return the one we waited for! Hopefully this doesn't happen often...
                return appendResult;
            }

            // 根据内存大小封装批次（有内存，有批次对象）
            MemoryRecordsBuilder recordsBuilder = recordsBuilder(buffer, maxUsableMagic);
            
            // 尝试向队列中添加数据
            ProducerBatch batch = new ProducerBatch(tp, recordsBuilder, nowMs);
            FutureRecordMetadata future = Objects.requireNonNull(batch.tryAppend(timestamp, key, value, headers,
                    callback, nowMs));

            // 将新创建的批次放入队列末尾
            dq.addLast(batch);
            incomplete.add(batch);

            // Don't deallocate this buffer in the finally block as it's being used in the record batch
            buffer = null;
            
            // 更新是否批的状态，检查是否需要发送
            return new RecordAppendResult(future, dq.size() > 1 || batch.isFull(), true, false);
        }
    } finally {
        // 使用完毕释放掉buffer，进入bufferpool循环使用
        if (buffer != null)
            free.deallocate(buffer);
        appendsInProgress.decrementAndGet();
    }
}
```

tryAppend()

```java
public FutureRecordMetadata tryAppend(long timestamp, byte[] key, byte[] value, Header[] headers, Callback callback, long now) {
    
    if (!recordsBuilder.hasRoomFor(timestamp, key, value, headers)) {
        return null;
    } else {
        this.recordsBuilder.append(timestamp, key, value, headers);
        this.maxRecordSize = Math.max(this.maxRecordSize, AbstractRecords.estimateSizeInBytesUpperBound(magic(),
                                                                                                        recordsBuilder.compressionType(), key, value, headers));
        this.lastAppendTime = now;
        FutureRecordMetadata future = new FutureRecordMetadata(this.produceFuture, this.recordCount,
                                                               timestamp,
                                                               key == null ? -1 : key.length,
                                                               value == null ? -1 : value.length,
                                                               Time.SYSTEM);
        // we have to keep every future returned to the users in case the batch needs to be
        // split to several new batches and resent.
        thunks.add(new Thunk(callback, future));
        this.recordCount++;
        return future;
    }
}
```

