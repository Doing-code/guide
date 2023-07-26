# Dubbo

> 官方文档：https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/

## Dubbo 服务导出流程图

![](../image/dubbo_服务导出流程.png)

## Dubbo 服务导出时序图

![](../image/dubbo_服务导出时序图.png)

## Dubbo 服务引用流程图

![](../image/dubbo_服务引用流程图.png)

## Dubbo 服务调用流程图

![](../image/dubbo-extension.jpg)

## 基础知识

RPC（Remote Procedure Call 远程过程调用）

决定RPC框架的模块：通信、序列化与反序列化。

常见的 RPC 框架：Dubbo、gRPC、Thrift、HSF（High Speed Service Framework）。

Dubbo 提供了三个核心能力：面向接口的远程方法调用、智能容错和负载均衡、服务自动注册和发现。

### 基本原理

#### RPC 简介

![](../image/RPC_基本原理.png)

示例：

```java
// A 服务
hello() {
    String msg = B.hi(new User("刘亦菲"));
    System.out.println(msg);
}

// B 服务
String hi(User user) {
    return "你好" + user.getName();
}
```

![](../image/RPC_eg.png)

Client Stub、Server Stub 可以理解为是处理器，处理客户端调用请求，封装信息，返回结果，建立连接等。

#### Dubbo 设计架构

![](../image/Dubbo_Architecture.png)

#### Dubbo 初体验

- 公共服务-rpc接口
  
  - 新建一个 UserService 接口
    
    ```java
    public interface UserService {
        String sayHi(String msg);
    }
    ```

- A服务-服务提供者
  
  - 引入 公共服务-rpc接口 依赖
  
  - 新建接口实现类
    
    ```java
    import cn.forbearance.rpc.UserService;
    import org.apache.dubbo.config.annotation.Service;
    
    /**
     * @author cristina
     */
    @Service // 暴露服务
    public class UserServiceImpl implements UserService {
    
        @Override
        public String sayHi(String msg) {
            return "hi, " + msg;
        }
    }
    ```
  
  - yml 配置
    
    ```yaml
    server:
      port: 8082
    
    # Dubbo 广播方式配置
    dubbo:
      application:
        name: produces
        version: 1.0.0
      registry:
    #    address: N/A #multicast://224.5.6.7:1234
        address: zookeeper://127.0.0.1:2181
      protocol:
        name: dubbo
        port: 20880
    
      scan:
        base-packages: cn.forbearance.dubbo
    ```
  
  - 启动类添加注解 `@EnableDubbo`并运行

- B服务-服务消费者
  
  - 引入 公共服务-rpc接口 依赖
  
  - yml 配置
    
    ```yaml
    server:
      port: 8081
    
    # Dubbo 广播方式配置
    dubbo:
      application:
        name: consumer
        version: 1.0.0
      registry:
        #    address: multicast://224.5.6.7:1234
        address: zookeeper://127.0.0.1:2181
      protocol:
        name: dubbo
        port: 20880
    ```
  
  - 新建测试类
    
    ```java
    // DubboConsumeTest.class
    @RunWith(SpringRunner.class)
    @SpringBootTest
    public class DubboConsumeTest {
    
        private Logger logger = LoggerFactory.getLogger(DubboConsumeTest.class);
    
        @Reference(interfaceClass = UserService.class, url = "dubbo://127.0.0.1:20880") // 引用服务
        private UserService userService;
    
        @Test
        public void test_rpc() {
            String info = userService.sayHi("hello world");
            logger.info("测试结果：{}", info);
        }
    }
    ```
  
  - 运行测试
    
    ```txt
    2023-06-11 15:07:27.604  INFO 248 --- [           main] c.f.dubbo.consume.test.DubboConsumeTest  : 测试结果：hi, hello world
    ```

## Dubbo 配置

> https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/advanced-features-and-usage/service/
> 
> https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/config/properties/

1. 启动时检查 

2. 超时&配置覆盖关系

3. 重试次数

4. 多版本

5. 本地存根

## 高可用

高可用：通过设计，减少系统不能提供服务的时间。

### Zookeeper宕机与Dubbo直连

- 现象：zookeeper 注册中心宕机后，还可以消费 dubbo 暴露的服务。

- 原因：
  
  - 1、注册中心宕机后，服务提供者和服务消费者仍能通过本地缓存通讯。
  
  - 2、监控中心宕机不影响使用，只是丢失部分采样数据。
  
  - 3、数据库宕机后，注册中心仍能通过缓存提供服务列表查询，但不能注册新服务。
  
  - 4、注册中心对等集群，任意一台宕机后，将自动切换到另一台。
  
  - 5、服务提供者无状态，其中一台宕机后，不影响使用。
  
  - 6、服务提供者全部宕机后，服务消费者将无法使用，并无线次重连等待服务提供者恢复。

### 负载均衡机制

默认为 Random

#### Random LoadBalance

随机，按权重设计随机概率。在一个截面上碰撞的概率高，但调用量越大分布越均匀。而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。

#### RoundRobin LoadBalance

轮询，按公约后的权重设置轮询比率。但存在慢的提供者累计请求的问题。

比如：第二台机器很慢，但没挂，当请求调到第二台机器时就卡在那，久而久之，所有请求都卡在调到第二台上。

#### LeastActive LoadBalance

最少活跃调用树，相同活跃数的随机，活跃数指调用前后计数差。使得慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。

#### ConsistentHash LoadBalance

一致性 Hash，相同参数的请求总是发到统一提供者。

当某一台提供者宕机时，原本发往该提供者的请求，基于虚拟节点，会平坦到其他提供者，不会引起剧烈变动。

默认只对第一个参数 Hash，默认用 160 份虚拟节点：

```xml
<!--通过如下配置进行修改默认配置-->
<dubbo:parameter key="hash.arguments" value="0,1"/>
<dubbo:parameter key="hash.nodes" value="320"/>
```

配置负载均衡：

```xml
<!-- 使用 xml 配置负载均衡 -->
<dubbo:service interface="..." loadbalance="roundrobin" />
```

```java
// 基于注解
@Service(version = "${product.service.version}",loadbalance="roundrobin")
```

```yaml
# 修改默认配置
dubbo:
  provider:
    loadbalance: roundrobin
```

### 服务降级

当服务器压力剧增的情况下，根据时实际业务情况及流量，对一些服务和页面有策略的不处理或简单方式的处理，从而释放服务器资源以保证核心业务正常运作或高效运作。

可以通过服务降级功能临时屏蔽某个出错的非关键服务，并定义降级后的返回策略：

- mock=force:return+null（屏蔽）
  
  - 不发起远程调用，消费方对该服务方法的调用都直接返回 null。用来屏蔽不重要服务不可用时对调用方的影响。

- mock=fail:return+null（容错）
  
  - 当消费方对该服务方法的调用失败后，再返回 null，不抛异常。用来容忍不重要服务不稳定时对调用方的影响。

服务降级配置：

```xml
<!--
    实现接口（在业务接口所在的包中定义一个类）
    该类的命名需要满足以下规则：
        实现mock接口
        类名命名规范：接口名+Mock后缀。（public class FooServiceMock implements FooService）
    此时如果调用失败会调用Mock实现，需要有无参构造
-->
<dubbo:reference id="fooService" interface="com.test.service.FooService" timeout="10000" check="false" mock="true" />
```

### 服务容错&Hystrix

在集群调用失败时，Dubbo 提供了多种容错方案，默认为 failover 重试。

```xml
<dubbo:reference cluster="failfast" />
```

## Dubbo 原理

> Dubbo Version 2.7.5

Dubbo URL说明（`protocol://username:password@host:port/path?key=value&key=value`）：

- protocol：指的是 dubbo 中的各种协议，如：dubbo thrift http

- username/password：用户名/密码

- host/port：主机/端口

- path：接口的名称

- parameters：参数键值对

### RPC 原理

![](../image/dubbo_一次完整的RPC调用.png)

一次完整的 RPC 调用流程如下（同步调用）：

1. 服务消费方（Client）以本地调用方式调用服务。

2. Client Stub 接收到调用请求后负责将方法、参数等封装成能够进行网络传输的消息体。

3. Client Stub 获取到服务地址，并将消息体发送到服务端。

4. Server Stub 接收到消息体后进行解码。

5. Server Stub 根据解码结果调用本地服务。

6. 本地服务执行并将结果返回给 Server Stub。

7. Server Stub 将返回结果封装为消息体并发送给消费方。

8. Client Stub 接收到消息并进行解码。

9. 服务消费方得到远程调用结果。

而 RPC 框架的目标就是要将 2~8 这些步骤封装起来，这些细节对于用户来说，是不可见的。

### Dubbo 框架设计

> 官网 https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/architecture/code-architecture/

![](../image/dubbo-framework.jpg)

只有 Service 和 Config 层为 API，其他各层均为 SPI：

1. Config 配置层：对外配置接口，以 `ServiceConfig`, `ReferenceConfig` 为中心，可以直接初始化配置类，也可以通过 Spring 解析配置生成配置类。

2. Proxy 服务代理层：服务接口透明代理，生成服务的客户端 Stub 和服务器端 Skeleton，以 `ServiceProxy` 为中心，扩展接口为 `ProxyFactory`。

3. Registry 注册中心层：封装服务地址的注册与发现，以服务 URL 为中心，扩展接口为 `RegistryFactory`、`Registry`、`RegistryService`。

4. Cluster 路由层：封装多个服务提供者的路由及负载均衡，并桥接注册中心，以 `Invoker` 为中心，扩展接口为 `Cluster`、`Directory`、`Router`、`LoadBalance`。

5. Monitor 监控层：RPC 调用次数和调用时间监控，以 `Statistics` 为中心，扩展接口为 `MonitorFactory`, `Monitor`, `MonitorService`。

6. Protocol 远程调用层：封装 RPC 调用，以 `Invocation`、`Result` 为中心，扩展接口为 `Protocol`、`Invoker`、`Exporter`。

7. Exchange 信息交换层：封装请求响应模式，同步转异步，以 `Request`、`Response` 为中心，扩展接口为 `Exchanger`、`ExchangeChannel`、`ExchangeClient`、`ExchangeServer`。

8. Transport 网络传输层：抽象 mina 和 netty 为统一接口，以 `Message` 为中心，扩展接口为 `Channel`、`Transporter`、`Client`、`Server`、`Codec`。

9. Serialize 数据序列化层：可复用的一些工具，扩展接口为 `Serialization`, `ObjectInput`, `ObjectOutput`, `ThreadPool`

`以xxx为中心`可以理解为是抽象接口或核心概念。

只要有 Protocol + Invoker + Exporter 就可以完成非透明的 RPC 调用。

### 启动解析、加载配置信息

解析配置的入口 `org.apache.dubbo.config.spring.schema.DubboNamespaceHandler#parse`，由上层`org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseCustomElement()`调用。

```java
public class DubboNamespaceHandler extends NamespaceHandlerSupport implements ConfigurableSourceBeanMetadataElement {

  // 提前将配置类解析初始化，将xml标签解析成对应的 xxxConfig 配置类。
  @Override
  public void init() {
      registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
      registerBeanDefinitionParser("module", new DubboBeanDefinitionParser(ModuleConfig.class, true));
      registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));
      registerBeanDefinitionParser("config-center", new DubboBeanDefinitionParser(ConfigCenterBean.class, true));
      registerBeanDefinitionParser("metadata-report", new DubboBeanDefinitionParser(MetadataReportConfig.class, true));
      registerBeanDefinitionParser("monitor", new DubboBeanDefinitionParser(MonitorConfig.class, true));
      registerBeanDefinitionParser("metrics", new DubboBeanDefinitionParser(MetricsConfig.class, true));
      registerBeanDefinitionParser("ssl", new DubboBeanDefinitionParser(SslConfig.class, true));
      registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
      registerBeanDefinitionParser("consumer", new DubboBeanDefinitionParser(ConsumerConfig.class, true));
      registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));
      registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));
      registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));
      registerBeanDefinitionParser("annotation", new AnnotationBeanDefinitionParser());
  }

  /**
   * Override {@link NamespaceHandlerSupport#parse(Element, ParserContext)} method
   *
   * @param element       {@link Element}
   * @param parserContext {@link ParserContext}
   * @return
   * @since 2.7.5
   */
  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
      BeanDefinitionRegistry registry = parserContext.getRegistry();
      registerAnnotationConfigProcessors(registry);
      registerApplicationListeners(registry);
      BeanDefinition beanDefinition = super.parse(element, parserContext);
      setSource(beanDefinition);
      return beanDefinition;
  }
}
```

会根据标签找到对应的解析器`findParserForElement()`，然后调用对应`BeanDefinitionParser#parse`。

省略中间调用部分，最终会调用`org.apache.dubbo.config.spring.schema.DubboBeanDefinitionParser#parse`。

`service`标签会解析成 `ServiceBean`，`reference` 标签会解析成 `ReferenceBean`。

```java
private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
    RootBeanDefinition beanDefinition = new RootBeanDefinition();
    beanDefinition.setBeanClass(beanClass);
    beanDefinition.setLazyInit(false);
    String id = element.getAttribute("id");
    if (StringUtils.isEmpty(id) && required) {
        String generatedBeanName = element.getAttribute("name");
        if (StringUtils.isEmpty(generatedBeanName)) {
            if (ProtocolConfig.class.equals(beanClass)) {
                generatedBeanName = "dubbo";
            } else {
                generatedBeanName = element.getAttribute("interface");
            }
        }
        if (StringUtils.isEmpty(generatedBeanName)) {
            generatedBeanName = beanClass.getName();
        }
        id = generatedBeanName;
        int counter = 2;
        while (parserContext.getRegistry().containsBeanDefinition(id)) {
            id = generatedBeanName + (counter++);
        }
    }
    if (StringUtils.isNotEmpty(id)) {
        if (parserContext.getRegistry().containsBeanDefinition(id)) {
            throw new IllegalStateException("Duplicate spring bean id " + id);
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        beanDefinition.getPropertyValues().addPropertyValue("id", id);
    }
    if (ProtocolConfig.class.equals(beanClass)) {
        for (String name : parserContext.getRegistry().getBeanDefinitionNames()) {
            BeanDefinition definition = parserContext.getRegistry().getBeanDefinition(name);
            PropertyValue property = definition.getPropertyValues().getPropertyValue("protocol");
            if (property != null) {
                Object value = property.getValue();
                if (value instanceof ProtocolConfig && id.equals(((ProtocolConfig) value).getName())) {
                    definition.getPropertyValues().addPropertyValue("protocol", new RuntimeBeanReference(id));
                }
            }
        }
    } else if (ServiceBean.class.equals(beanClass)) {
        String className = element.getAttribute("class");
        if (StringUtils.isNotEmpty(className)) {
            RootBeanDefinition classDefinition = new RootBeanDefinition();
            classDefinition.setBeanClass(ReflectUtils.forName(className));
            classDefinition.setLazyInit(false);
            parseProperties(element.getChildNodes(), classDefinition);
            beanDefinition.getPropertyValues().addPropertyValue("ref", new BeanDefinitionHolder(classDefinition, id + "Impl"));
        }
    } else if (ProviderConfig.class.equals(beanClass)) {
        parseNested(element, parserContext, ServiceBean.class, true, "service", "provider", id, beanDefinition);
    } else if (ConsumerConfig.class.equals(beanClass)) {
        parseNested(element, parserContext, ReferenceBean.class, false, "reference", "consumer", id, beanDefinition);
    }
    Set<String> props = new HashSet<>();
    ManagedMap parameters = null;
    for (Method setter : beanClass.getMethods()) {
        String name = setter.getName();
        if (name.length() > 3 && name.startsWith("set")
                && Modifier.isPublic(setter.getModifiers())
                && setter.getParameterTypes().length == 1) {
            Class<?> type = setter.getParameterTypes()[0];
            String beanProperty = name.substring(3, 4).toLowerCase() + name.substring(4);
            String property = StringUtils.camelToSplitName(beanProperty, "-");
            props.add(property);
            // check the setter/getter whether match
            Method getter = null;
            try {
                getter = beanClass.getMethod("get" + name.substring(3), new Class<?>[0]);
            } catch (NoSuchMethodException e) {
                try {
                    getter = beanClass.getMethod("is" + name.substring(3), new Class<?>[0]);
                } catch (NoSuchMethodException e2) {
                    // ignore, there is no need any log here since some class implement the interface: EnvironmentAware,
                    // ApplicationAware, etc. They only have setter method, otherwise will cause the error log during application start up.
                }
            }
            if (getter == null
                    || !Modifier.isPublic(getter.getModifiers())
                    || !type.equals(getter.getReturnType())) {
                continue;
            }
            if ("parameters".equals(property)) {
                parameters = parseParameters(element.getChildNodes(), beanDefinition);
            } else if ("methods".equals(property)) {
                parseMethods(id, element.getChildNodes(), beanDefinition, parserContext);
            } else if ("arguments".equals(property)) {
                parseArguments(id, element.getChildNodes(), beanDefinition, parserContext);
            } else {
                String value = element.getAttribute(property);
                if (value != null) {
                    value = value.trim();
                    if (value.length() > 0) {
                        if ("registry".equals(property) && RegistryConfig.NO_AVAILABLE.equalsIgnoreCase(value)) {
                            RegistryConfig registryConfig = new RegistryConfig();
                            registryConfig.setAddress(RegistryConfig.NO_AVAILABLE);
                            beanDefinition.getPropertyValues().addPropertyValue(beanProperty, registryConfig);
                        } else if ("provider".equals(property) || "registry".equals(property) || ("protocol".equals(property) && AbstractServiceConfig.class.isAssignableFrom(beanClass))) {
                            /**
                             * For 'provider' 'protocol' 'registry', keep literal value (should be id/name) and set the value to 'registryIds' 'providerIds' protocolIds'
                             * The following process should make sure each id refers to the corresponding instance, here's how to find the instance for different use cases:
                             * 1. Spring, check existing bean by id, see{@link ServiceBean#afterPropertiesSet()}; then try to use id to find configs defined in remote Config Center
                             * 2. API, directly use id to find configs defined in remote Config Center; if all config instances are defined locally, please use {@link ServiceConfig#setRegistries(List)}
                             */
                            beanDefinition.getPropertyValues().addPropertyValue(beanProperty + "Ids", value);
                        } else {
                            Object reference;
                            if (isPrimitive(type)) {
                                if ("async".equals(property) && "false".equals(value)
                                        || "timeout".equals(property) && "0".equals(value)
                                        || "delay".equals(property) && "0".equals(value)
                                        || "version".equals(property) && "0.0.0".equals(value)
                                        || "stat".equals(property) && "-1".equals(value)
                                        || "reliable".equals(property) && "false".equals(value)) {
                                    // backward compatibility for the default value in old version's xsd
                                    value = null;
                                }
                                reference = value;
                            } else if (ONRETURN.equals(property) || ONTHROW.equals(property) || ONINVOKE.equals(property)) {
                                int index = value.lastIndexOf(".");
                                String ref = value.substring(0, index);
                                String method = value.substring(index + 1);
                                reference = new RuntimeBeanReference(ref);
                                beanDefinition.getPropertyValues().addPropertyValue(property + METHOD, method);
                            } else {
                                if ("ref".equals(property) && parserContext.getRegistry().containsBeanDefinition(value)) {
                                    BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(value);
                                    if (!refBean.isSingleton()) {
                                        throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value + " bean scope to singleton, eg: <bean id=\"" + value + "\" scope=\"singleton\" ...>");
                                    }
                                }
                                reference = new RuntimeBeanReference(value);
                            }
                            beanDefinition.getPropertyValues().addPropertyValue(beanProperty, reference);
                        }
                    }
                }
            }
        }
    }
    NamedNodeMap attributes = element.getAttributes();
    int len = attributes.getLength();
    for (int i = 0; i < len; i++) {
        Node node = attributes.item(i);
        String name = node.getLocalName();
        if (!props.contains(name)) {
            if (parameters == null) {
                parameters = new ManagedMap();
            }
            String value = node.getNodeValue();
            parameters.put(name, new TypedStringValue(value, String.class));
        }
    }
    if (parameters != null) {
        beanDefinition.getPropertyValues().addPropertyValue("parameters", parameters);
    }
    return beanDefinition;
}
```

### 服务暴露

服务暴露/导出是在 SpringIOC 容器完成刷新后开始暴露的。

利用 Spring 自定义标签解析机制生成对应的 Bean，服务暴露/导出使用到的` Provider Sefvice` 使用的是 `ServiceBean`。

服务导出思路：

- 确定服务的参数。

- 确定服务支持的协议。

- 构建服务最终的 URL。

- 将服务 URL 注册到注册中心。

- 根据服务支持的不同协议，启动不同 Server，用来接受和处理请求。

- Dubbo 支持动态配置服务参数，所以服务导出时还需要绑定一个监听器来监听服务的参数是否有修改，如果发现有修改，则需要重新进行导出。

Dubbo 会在容器中注册两个监听器：DubboLifecycleComponentApplicationListener、DubboBootstrapApplicationListener。前者主要负责从容器中调用`org.apache.dubbo.common.context.Lifecycle#start`，后者负责启动 `DubboBootstrap`。

#### 服务暴露的入口

```java
// 监听容器刷新完事件：ContextRefreshedEvent

org.apache.dubbo.config.spring.context.DubboBootstrapApplicationListener#onApplicationContextEvent
    org.apache.dubbo.config.spring.context.DubboBootstrapApplicationListener#onContextRefreshedEvent
        org.apache.dubbo.config.bootstrap.DubboBootstrap#start
            org.apache.dubbo.config.bootstrap.DubboBootstrap#exportServices 【获取到所有的ServiceBean对象并依次export()】
                org.apache.dubbo.config.ServiceConfig#export 【服务暴露/导出 入口】
```

1. `org.apache.dubbo.config.bootstrap.DubboBootstrap#exportServices`：

```java
private void exportServices() {
    configManager.getServices().forEach(sc -> {
        // TODO, compatible with ServiceConfig.export()
        ServiceConfig serviceConfig = (ServiceConfig) sc;
        serviceConfig.setBootstrap(this);

        if (exportAsync) {
            ExecutorService executor = executorRepository.getServiceExporterExecutor();
            Future<?> future = executor.submit(() -> {
                // 服务开始导出/暴露
                sc.export();
            });
            asyncExportingFutures.add(future);
        } else {
            sc.export();
            exportedServices.add(sc);
        }
    });
}
```

2. `org.apache.dubbo.config.ServiceConfig#export`：

这个方法从整体上来看就做了三件事。

- 1、实例并初始化 DubboBootstrap。

- 2、对当前对象做属性的检查和赋值。

- 3、调用 doExport()。

```java
public synchronized void export() {
    if (!shouldExport()) {
        return;
    }

    if (bootstrap == null) {
        bootstrap = DubboBootstrap.getInstance();
        bootstrap.init();
    }

    // 检查并更新子配置信息
    checkAndUpdateSubConfigs();

    //init serviceMetadata
    serviceMetadata.setVersion(version);
    serviceMetadata.setGroup(group);
    serviceMetadata.setDefaultGroup(group);
    serviceMetadata.setServiceType(getInterfaceClass());
    serviceMetadata.setServiceInterfaceName(getInterface());
    serviceMetadata.setTarget(getRef());

    if (shouldDelay()) {
        DELAY_EXPORT_EXECUTOR.schedule(this::doExport, getDelay(), TimeUnit.MILLISECONDS);
    } else {
        doExport();
    }
}
```

3. `org.apache.dubbo.config.ServiceConfig#doExport`：

```java
protected synchronized void doExport() {
    if (unexported) {
        throw new IllegalStateException("The service " + interfaceClass.getName() + " has already unexported!");
    }
    if (exported) {
        return;
    }
    exported = true;

    if (StringUtils.isEmpty(path)) {
        path = interfaceName;
    }
    doExportUrls();

    // dispatch a ServiceConfigExportedEvent since 2.7.4
    dispatch(new ServiceConfigExportedEvent(this));
}
```

4. `org.apache.dubbo.config.ServiceConfig#doExportUrls`：
- 多协议多注册中心导出服务

```java
private void doExportUrls() {

    // 首先将服务注册到本地
    ServiceRepository repository = ApplicationModel.getServiceRepository();
    ServiceDescriptor serviceDescriptor = repository.registerService(getInterfaceClass());
    repository.registerProvider(
            getUniqueServiceName(),
            ref,
            serviceDescriptor,
            this,
            serviceMetadata
    );
    // 加载注册中心信息（获取 URL 列表）
    List<URL> registryURLs = ConfigValidationUtils.loadRegistries(this, true);

    // 遍历 protocols，并在每个协议下导出服务
    for (ProtocolConfig protocolConfig : protocols) {
        String pathKey = URL.buildKey(getContextPath(protocolConfig)
                .map(p -> p + "/" + path)
                .orElse(path), group, version);
        // In case user specified path, register service one more time to map it to path.

        // 根据协议名注册不同的服务
        repository.registerService(pathKey, interfaceClass);
        // TODO, uncomment this line once service key is unified
        serviceMetadata.setServiceKey(pathKey);
        doExportUrlsFor1Protocol(protocolConfig, registryURLs);
    }
}
```

5. `org.apache.dubbo.config.ServiceConfig#doExportUrlsFor1Protocol`：

配置检查完毕后，紧接着要做的事情是根据配置以及其他一些信息组装 URL。URL 是 Dubbo 配置的载体，通过 URL 可让 Dubbo 的各种配置在各个模块之间传递。

该方法主要逻辑：根据 host、post等信息构建服务对外提供的 URL。还提供服务暴露功能，本地服务和远程服务。

```java
private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
    String name = protocolConfig.getName();
    // 如果协议名为空或空串，则将协议名设置为默认协议名 dubbo
    if (StringUtils.isEmpty(name)) {
        name = DUBBO;
    }

    Map<String, String> map = new HashMap<String, String>();
    map.put(SIDE_KEY, PROVIDER_SIDE);

    // 通过反射将对象的字段信息添加到 map 中
    ServiceConfig.appendRuntimeParameters(map);
    AbstractConfig.appendParameters(map, getMetrics());
    AbstractConfig.appendParameters(map, getApplication());
    AbstractConfig.appendParameters(map, getModule());
    // remove 'default.' prefix for configs from ProviderConfig
    // appendParameters(map, provider, Constants.DEFAULT_KEY);
    AbstractConfig.appendParameters(map, provider);
    AbstractConfig.appendParameters(map, protocolConfig);
    AbstractConfig.appendParameters(map, this);

    // methods 是 MethodConfig 集合，MethodConfig 中存储的是 <dubbo:method> 标签的配置信息
    if (CollectionUtils.isNotEmpty(getMethods())) {
        // ... 省略
    }

    if (ProtocolUtils.isGeneric(generic)) {
        map.put(GENERIC_KEY, generic);
        map.put(METHODS_KEY, ANY_VALUE);
    } else {
        String revision = Version.getVersion(interfaceClass, version);
        if (revision != null && revision.length() > 0) {
            map.put(REVISION_KEY, revision);
        }

        // 为接口生成包装类 Wrapper，Wrapper 中包含了接口的详细信息。接口方法名数组、字段信息等。
        String[] methods = Wrapper.getWrapper(interfaceClass).getMethodNames();
        if (methods.length == 0) {
            logger.warn("No method found in service interface " + interfaceClass.getName());
            map.put(METHODS_KEY, ANY_VALUE);
        } else {
            map.put(METHODS_KEY, StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
        }
    }
    if (!ConfigUtils.isEmpty(token)) {
        if (ConfigUtils.isDefault(token)) {
            map.put(TOKEN_KEY, UUID.randomUUID().toString());
        } else {
            map.put(TOKEN_KEY, token);
        }
    }
    //init serviceMetadata attachments
    serviceMetadata.getAttachments().putAll(map);

    // export service
    // 获取 host 和 port
    String host = findConfigedHosts(protocolConfig, registryURLs, map);
    Integer port = findConfigedPorts(protocolConfig, name, map);
    // 构建 URL
    URL url = new URL(name, host, port, getContextPath(protocolConfig).map(p -> p + "/" + path).orElse(path), map);

    // You can customize Configurator to append extra parameters
    if (ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
            .hasExtension(url.getProtocol())) {
        // 加载 ConfiguratorFactory，并生成 Configurator 实例并通过其配置 url
        url = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                .getExtension(url.getProtocol()).getConfigurator(url).configure(url);
    }

    String scope = url.getParameter(SCOPE_KEY);
    // don't export when none is configured
    if (!SCOPE_NONE.equalsIgnoreCase(scope)) {

        // export to local if the config is not remote (export to remote only when config is remote)
        // 作用域不是 remote 则导出到本地
        if (!SCOPE_REMOTE.equalsIgnoreCase(scope)) {
            exportLocal(url);
        }
        // export to remote if the config is not local (export to local only when config is local)
        // 作用域不是 local 则导出到远程
        if (!SCOPE_LOCAL.equalsIgnoreCase(scope)) {
            if (CollectionUtils.isNotEmpty(registryURLs)) {
                for (URL registryURL : registryURLs) {
                    //if protocol is only injvm ,not register
                    if (LOCAL_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
                        continue;
                    }
                    url = url.addParameterIfAbsent(DYNAMIC_KEY, registryURL.getParameter(DYNAMIC_KEY));
                    // 加载监视器链接
                    URL monitorUrl = ConfigValidationUtils.loadMonitor(this, registryURL);
                    if (monitorUrl != null) {
                        // 将监视器作为参数添加到 url 中
                        url = url.addParameterAndEncoded(MONITOR_KEY, monitorUrl.toFullString());
                    }
                    if (logger.isInfoEnabled()) {
                        if (url.getParameter(REGISTER_KEY, true)) {
                            logger.info("Register dubbo service " + interfaceClass.getName() + " url " + url + " to registry " + registryURL);
                        } else {
                            logger.info("Export dubbo service " + interfaceClass.getName() + " to url " + url);
                        }
                    }

                    // For providers, this is used to enable custom proxy to generate invoker
                    String proxy = url.getParameter(PROXY_KEY);
                    if (StringUtils.isNotEmpty(proxy)) {
                        registryURL = registryURL.addParameter(PROXY_KEY, proxy);
                    }

                    // 为服务提供者（ref）生成 Invoker
                    Invoker<?> invoker = PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(EXPORT_KEY, url.toFullString()));
                    // DelegateProviderMetaDataInvoker 用于持有 Invoke人 和 ServiceConfig
                    DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);

                    // 导出服务，并生成 Exporter
                    Exporter<?> exporter = protocol.export(wrapperInvoker);
                    exporters.add(exporter);
                }
            } else {
                // 不存在注册中心，仅导出服务

                if (logger.isInfoEnabled()) {
                    logger.info("Export dubbo service " + interfaceClass.getName() + " to url " + url);
                }
                Invoker<?> invoker = PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, url);
                DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);

                Exporter<?> exporter = protocol.export(wrapperInvoker);
                exporters.add(exporter);
            }
            /**
             * @since 2.7.0
             * ServiceData Store
             */
            WritableMetadataService metadataService = WritableMetadataService.getExtension(url.getParameter(METADATA_KEY, DEFAULT_METADATA_STORAGE_TYPE));
            if (metadataService != null) {
                metadataService.publishServiceDefinition(url);
            }
        }
    }
    this.urls.add(url);
}
```

根据 url 中的 scope 参数决定服务导出方式：

- scope == none：不导出服务

- scope != remote：导出到本地

- scope != local：导出到远程

不管是导出到本地，还是远程。进行服务导出之前，都需要先创建 Invoker。

`Invoker`解释：Invoker是Dubbo中的实体域，也就是真实存在的。其他模型都向它靠拢或转换成它，它也就代表一个可执行体，可向它发起invoke调用。在服务提供方，Invoker用于调用服务提供类。在服务消费方，Invoker用于执行远程调用。

在服务提供方中的`Invoker`是由ProxyFactory创建而来的，Dubbo默认的ProxyFactory实现类为JavassistProxyFactory。

6. `org.apache.dubbo.registry.integration.RegistryProtocol#export`：

```java
@Override
public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
    // 尝试将 url 中的协议替换成映射 registry 的值，并删除 registry
    URL registryUrl = getRegistryUrl(originInvoker);
    // url to export locally
    // 获取映射 export 的值并解析成 url
    URL providerUrl = getProviderUrl(originInvoker);

    // Subscribe the override data
    // FIXME When the provider subscribes, it will affect the scene : a certain JVM exposes the service and call
    //  the same service. Because the subscribed is cached key with the name of the service, it causes the
    //  subscription information to cover.
    // 将 providerUrl 的协议修改为 provider 并添加 category 和 check 两个属性
    final URL overrideSubscribeUrl = getSubscribedOverrideUrl(providerUrl);
    // 创建监听器
    final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl, originInvoker);
    overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);

    providerUrl = overrideUrlWithConfig(providerUrl, overrideSubscribeListener);

    // 1、export invoker：导出服务
    final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker, providerUrl);

    // url to registry
    // 根据 URL 加载 Registry 实现类，比如 ZookeeperRegistry
    final Registry registry = getRegistry(originInvoker);

    // 获取已注册的服务提供 URL
    final URL registeredProviderUrl = getUrlToRegistry(providerUrl, registryUrl);

    // decide if we need to delay publish
    // 获取 register 参数
    boolean register = providerUrl.getParameter(REGISTER_KEY, true);

    // 根据 register 的值决定是否注册服务
    if (register) {
        // 2、注册服务
        register(registryUrl, registeredProviderUrl);
    }

    // Deprecated! Subscribe to override rules in 2.6.x or before.
    // 3、注册override协议， 用于适配2.6.x 及之前的版本
    registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);

    exporter.setRegisterUrl(registeredProviderUrl);
    exporter.setSubscribeUrl(overrideSubscribeUrl);

    // 4、创建并返回 DestroyableExporter
    return new DestroyableExporter<>(exporter);
}
```

`export()`方法主要做如下一些操作：

- 1、调用 `doLocalExport` 导出服务。

- 2、向注册中心注册服务。

- 3、向注册中心订阅 override，注册override协议。

- 4、创建并返回 DestroyableExporter
7. `org.apache.dubbo.registry.integration.RegistryProtocol#doLocalExport`：

```java
private <T> ExporterChangeableWrapper<T> doLocalExport(final Invoker<T> originInvoker, URL providerUrl) {
    String key = getCacheKey(originInvoker);

    // 若缓存中没有与 key 映射的 ExporterChangeableWrapper，则执行lambda表达式并将 key 与lambda表达式返回值做映射
    return (ExporterChangeableWrapper<T>) bounds.computeIfAbsent(key, s -> {
        // 创建 Invoker 委托类对象
        Invoker<?> invokerDelegate = new InvokerDelegate<>(originInvoker, providerUrl);
        // 调用 protocol.export 导出服务
        return new ExporterChangeableWrapper<>((Exporter<T>) protocol.export(invokerDelegate), originInvoker);
    });
}
```

会执行构建protocol的Filter链和listener链的对应的 export 方法。

8. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#export`：

```java
@Override
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
    URL url = invoker.getUrl();

    // export service.
    // 获取服务标识（服务坐标）：
    //     由服务组名 + 服务名 + 服务版本号 + 端口组成
    //     比如：demoGroup/com.alibaba.dubbo.demo.DemoService:1.0.1:20880
    String key = serviceKey(url);
    // 创建 DubboExporter
    DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
    exporterMap.put(key, exporter);

    //export an stub service for dispatching event
    // 本地存根相关代码
    Boolean isStubSupportEvent = url.getParameter(STUB_EVENT_KEY, DEFAULT_STUB_EVENT);
    Boolean isCallbackservice = url.getParameter(IS_CALLBACK_SERVICE, false);
    if (isStubSupportEvent && !isCallbackservice) {
        String stubServiceMethods = url.getParameter(STUB_EVENT_METHODS_KEY);
        if (stubServiceMethods == null || stubServiceMethods.length() == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn(new IllegalStateException("consumer [" + url.getParameter(INTERFACE_KEY) +
                        "], has set stubproxy support event ,but no stub methods founded."));
            }

        } else {
            stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
        }
    }

    // 开启服务
    openServer(url);

    // 优化序列化
    optimizeSerialization(url);

    return exporter;
}
```

9. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#openServer`：

```java
private void openServer(URL url) {
    // find server.
    // 获取 host:port，并将其作为服务器实例的 key，用于标识当前的服务器实例
    String key = url.getAddress();
    //client can export a service which's only for server to invoke
    boolean isServer = url.getParameter(IS_SERVER_KEY, true);
    if (isServer) {
        // 先从缓存中获取
        ProtocolServer server = serverMap.get(key);
        if (server == null) {
            synchronized (this) {
                server = serverMap.get(key);
                if (server == null) {
                    // 创建服务器实例
                    serverMap.put(key, createServer(url));
                }
            }
        } else {
            // server supports reset, use together with override
            // 如果服务器已经创建了，则根据 url 中的配置重置服务器
            server.reset(url);
        }
    }
}
```

10. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#createServer`：

```java
private ProtocolServer createServer(URL url) {
  url = URLBuilder.from(url)
          // send readonly event when server closes, it's enabled by default
          .addParameterIfAbsent(CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString())
          // enable heartbeat by default
          // 添加心跳检测配置到 url 中
          .addParameterIfAbsent(HEARTBEAT_KEY, String.valueOf(DEFAULT_HEARTBEAT))
          // 添加编解码器参数
          .addParameter(CODEC_KEY, DubboCodec.NAME)
          .build();
  String str = url.getParameter(SERVER_KEY, DEFAULT_REMOTING_SERVER);

  // 1、 通过 SPI 检测是否存在 server 参数对应的 Transporter 扩展，不存在则抛异常
  if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
      throw new RpcException("Unsupported server type: " + str + ", url: " + url);
  }

  ExchangeServer server;
  try {
      // 2、创建 ExchangeServer
      server = Exchangers.bind(url, requestHandler);
  } catch (RemotingException e) {
      throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
  }

  // 获取 client 参数，可指定 netty、mina、grizzly
  // 3、通过 SPI 检测是否存在 client 参数对应的 Transporter 扩展，不存在则抛异常
  str = url.getParameter(CLIENT_KEY);
  if (str != null && str.length() > 0) {
      Set<String> supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions();
      if (!supportedTypes.contains(str)) {
          throw new RpcException("Unsupported client type: " + str);
      }
  }

  return new DubboProtocolServer(server);
}
```

`createServer()`方法包含三个核心的逻辑：

- 1、检测是否存在 server 参数对应的 Transporter 扩展，不存在则抛异常。

- 2、创建服务器实例。

- 3、检测是否存在 client 参数对应的 Transporter 扩展，不存在则抛异常。
11. 创建 NettyServer：

```java
// 1. org.apache.dubbo.remoting.exchange.Exchangers#bind(URL, ExchangeHandler)
public static ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    if (handler == null) {
        throw new IllegalArgumentException("handler == null");
    }
    url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
    // 获取 Exchanger，默认为 HeaderExchange。并调用其 bind() 创建 ExchangeServer 实例
    return getExchanger(url).bind(url, handler);
}

// 2. org.apache.dubbo.remoting.exchange.support.header.HeaderExchanger#bind
@Override
public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
    return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
}

// 3. org.apache.dubbo.remoting.Transporters#bind(URL, ChannelHandler...)
public static RemotingServer bind(URL url, ChannelHandler... handlers) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    if (handlers == null || handlers.length == 0) {
        throw new IllegalArgumentException("handlers == null");
    }
    ChannelHandler handler;
    if (handlers.length == 1) {
        handler = handlers[0];
    } else {
        // 如果 handlers 元素数量大于 1，则创建 ChannelHandler 分发器
        handler = new ChannelHandlerDispatcher(handlers);
    }
    // 获取自适应 Transporter 实例，并调用实例返回 bind.
    return getTransporter().bind(url, handler);
}

// 4. org.apache.dubbo.remoting.transport.netty4.NettyTransporter#bind
@Override
public RemotingServer bind(URL url, ChannelHandler listener) throws RemotingException {
    return new NettyServer(url, listener);
}

/*
  1. 创建一个 NettyServer，会调用 NettyServer 的父类构造 AbstractServer 进行配置获取。如 bindIp、bindPort，构造 localAddress，最大可接受连接数等
    1.1 调用模板方法 doOpen(); 启动服务
    1.2 在 NettyServer 类中得到实现，如下所示
*/

/**
 * Init and start netty server
 *
 * @throws Throwable
 */
@Override
protected void doOpen() throws Throwable {
    bootstrap = new ServerBootstrap();

    bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
    workerGroup = new NioEventLoopGroup(getUrl().getPositiveParameter(IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS),
            new DefaultThreadFactory("NettyServerWorker", true));

    final NettyServerHandler nettyServerHandler = new NettyServerHandler(getUrl(), this);
    channels = nettyServerHandler.getChannels();

    bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
            .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // FIXME: should we use getTimeout()?
                    int idleTimeout = UrlUtils.getIdleTimeout(getUrl());
                    NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
                    if (getUrl().getParameter(SSL_ENABLED_KEY, false)) {
                        ch.pipeline().addLast("negotiation",
                                SslHandlerInitializer.sslServerHandler(getUrl(), nettyServerHandler));
                    }
                    ch.pipeline()
                            .addLast("decoder", adapter.getDecoder())
                            .addLast("encoder", adapter.getEncoder())
                            .addLast("server-idle-handler", new IdleStateHandler(0, 0, idleTimeout, MILLISECONDS))
                            .addLast("handler", nettyServerHandler);
                }
            });
    // bind
    ChannelFuture channelFuture = bootstrap.bind(getBindAddress());
    channelFuture.syncUninterruptibly();
    channel = channelFuture.channel();

}
```

- `getTransporter()`获取的 Transporter 是在运行时动态创建的，类名为 TransporterAdaptive，也就是自适应扩展类。TransporterAdaptive 运行时根据传入的 URL 参数决定加载什么类型的 Transporter，默认是 NettyTransporter。

至此，Dubbo 的服务暴露流程就走完了。

#### 总结

1. 服务配置解析：
   
   - Dubbo会解析服务提供者在XML配置文件或注解中定义的服务配置，包括服务接口、服务实现类、协议、端口等信息。

2. 服务实例化：
   
   - Dubbo会根据服务实现类的配置信息，实例化服务对象。

3. 服务包装：
   
   - Dubbo会对服务对象进行包装，以提供一些额外的功能，如事务管理、远程调用的封装等。这些包装类会按照一定的顺序进行包装，形成一个代理链。

4. 服务注册：
   
   - Dubbo将服务的相关信息注册到注册中心，包括服务接口、服务地址、服务版本等。注册中心可以是ZooKeeper、Consul等。

5. 协议导出：
   
   - Dubbo会根据配置的协议类型，将服务暴露到指定的协议上，比如Dubbo协议、HTTP协议等。在导出过程中，Dubbo会创建对应的Server对象，并将服务对象和协议绑定起来。

6. 服务暴露：
   
   - 服务暴露是指将服务发布到网络上（Netty），以供消费者调用。在此阶段，Dubbo会启动监听器，监听指定协议的端口，等待消费者的调用。

### 服务引用

服务引用分为本地引入、直接引入、注册中心引入。`Consumer Reference` 使用的是 `ReferenceBean`。服务引用分为饿汉式和懒汉式。

饿汉式通过实现 `InitializingBean` 接口的 `afterPropertiesSet()` 方法实现，容器通过调用 `ReferenceBean` 的 `afterPropertiesSet()` 方法时引入服务。

Dubbo 默认使用懒汉式引入策略，`ReferenceBean` 还实现了 `FactoryBean`，通过 `FactoryBean` 来实现懒汉式引用服务。

如果需要饿汉式则配置为`<dubbo:reference init="true"/>`。

URL 参数：

```txt
dubbo://192.168.44.1:20880/cn.forbearance.rpc.UserService
  ?anyhost=true
  &application=Lottery
  &application.version=1.0.0
  &check=false
  &deprecated=false
  &dubbo=2.0.2
  &dynamic=true
  &generic=false
  &init=false
  &interface=cn.forbearance.rpc.UserService
  &methods=sayHi
  &pid=11920
  &qos.enable=false
  &register.ip=192.168.44.1
  &release=2.7.5
  &remote.application=Lottery
  &side=consumer
  &sticky=false
  &timestamp=1686627527865
```

#### 服务引用入口

1. 服务引用入口 `ReferenceBean#getObject()`：

```java
public class ReferenceBean<T> extends ReferenceConfig<T> implements FactoryBean,
        ApplicationContextAware, InitializingBean, DisposableBean {
    @Override
    public Object getObject() {
        return get();
    }
}
```

2. `org.apache.dubbo.config.ReferenceConfig#get`：

```java
public synchronized T get() {
    if (destroyed) {
        throw new IllegalStateException("The invoker of ReferenceConfig(" + url + ") has already destroyed!");
    }
    if (ref == null) {
        init();
    }
    return ref;
}
```

3. `org.apache.dubbo.config.ReferenceConfig#init`：

```java
public synchronized void init() {
    // 避免重复初始化
    if (initialized) {
        return;
    }

    /*
      省略部分代码，配置检查、赋值等逻辑
      checkAndUpdateSubConfigs()、init serivceMetadata、获取接口方法 methods等
    */

    // 创建代理
    ref = createProxy(map);

    serviceMetadata.setTarget(ref);
    serviceMetadata.addAttribute(PROXY_CLASS_REF, ref);
    repository.lookupReferredService(serviceMetadata.getServiceKey()).setProxyObject(ref);

    initialized = true;

    // dispatch a ReferenceConfigInitializedEvent since 2.7.4
    dispatch(new ReferenceConfigInitializedEvent(this, invoker));
}
```

上述逻辑中，`createProxy()`方法是重要逻辑，它负责创建 Invoker 实例和创建代理对象。

4. `org.apache.dubbo.config.ReferenceConfig#createProxy`：

```java
private T createProxy(Map<String, String> map) {
    // 判断是否为本地引用
    if (shouldJvmRefer(map)) {
        URL url = new URL(LOCAL_PROTOCOL, LOCALHOST_VALUE, 0, interfaceClass.getName()).addParameters(map);
        invoker = REF_PROTOCOL.refer(interfaceClass, url);
        if (logger.isInfoEnabled()) {
            logger.info("Using injvm service " + interfaceClass.getName());
        }

    // 远程引用
    } else {
        urls.clear();

        // url 不为空则代表是服务直连
        if (url != null && url.length() > 0) { // user specified URL, could be peer-to-peer address, or register center's address.
            String[] us = SEMICOLON_SPLIT_PATTERN.split(url);
            if (us != null && us.length > 0) {
                for (String u : us) {
                    URL url = URL.valueOf(u);
                    if (StringUtils.isEmpty(url.getPath())) {
                        url = url.setPath(interfaceName);
                    }
                    if (UrlUtils.isRegistry(url)) {
                        urls.add(url.addParameterAndEncoded(REFER_KEY, StringUtils.toQueryString(map)));
                    } else {
                        urls.add(ClusterUtils.mergeUrl(url, map));
                    }
                }
            }

        // 加载注册中心的 url
        } else { // assemble URL from register center's configuration
            // if protocols not injvm checkRegistry
            if (!LOCAL_PROTOCOL.equalsIgnoreCase(getProtocol())) {
                checkRegistry();
                List<URL> us = ConfigValidationUtils.loadRegistries(this, false);
                if (CollectionUtils.isNotEmpty(us)) {
                    for (URL u : us) {
                        URL monitorUrl = ConfigValidationUtils.loadMonitor(this, u);
                        if (monitorUrl != null) {
                            map.put(MONITOR_KEY, URL.encode(monitorUrl.toFullString()));
                        }
                        urls.add(u.addParameterAndEncoded(REFER_KEY, StringUtils.toQueryString(map)));
                    }
                }
                if (urls.isEmpty()) {
                    throw new IllegalStateException("No such any registry to reference " + interfaceName + " on the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", please config <dubbo:registry address=\"...\" /> to your spring config.");
                }
            }
        }

        // 单个注册中心
        if (urls.size() == 1) {
            invoker = REF_PROTOCOL.refer(interfaceClass, urls.get(0));
        } else {
            // 多个注册中心

            List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
            URL registryURL = null;
            for (URL url : urls) {
                invokers.add(REF_PROTOCOL.refer(interfaceClass, url));
                if (UrlUtils.isRegistry(url)) {
                    registryURL = url; // use last registry url
                }
            }
            if (registryURL != null) { // registry url is available
                // for multi-subscription scenario, use 'zone-aware' policy by default
                URL u = registryURL.addParameterIfAbsent(CLUSTER_KEY, ZoneAwareCluster.NAME);
                // The invoker wrap relation would be like: ZoneAwareClusterInvoker(StaticDirectory) -> FailoverClusterInvoker(RegistryDirectory, routing happens here) -> Invoker
                invoker = CLUSTER.join(new StaticDirectory(u, invokers));
            } else { // not a registry url, must be direct invoke.
                invoker = CLUSTER.join(new StaticDirectory(invokers));
            }
        }
    }

    // 省略检查逻辑...

    // create service proxy
    return (T) PROXY_FACTORY.getProxy(invoker);
}
```

在服务暴露的时候，会分为本地暴露和远程暴露。服务引用也是如此，Dubbo 首先判断服务引用是本地引用还是远程引用，默认是远程引用，然后判断是否为直连服务，根据协议调用 `refer()` 方法创建 `Invoker` 对象，最后创建代理对象并返回。

逻辑流程大致分为两部分：创建 Invoker、创建代理。

若使用的是单机版，则会走`invoker = REF_PROTOCOL.refer(interfaceClass, urls.get(0));`这行逻辑。

`urls.get(0)` 获取到的是注册中心的地址，内容如下：

```txt
registry://127.0.0.1:2181/org.apache.dubbo.registry.RegistryService
  ?application=Lottery
  &application.version=1.0.0
  &dubbo=2.0.2
  &pid=14236
  &qos.enable=false
  &refer=application%3DLottery%26application.version%3D1.0.0%26dubbo%3D2.0.2%26init%3Dfalse%26interface%3Dcn.forbearance.rpc.UserService%26methods%3DsayHi%26pid%3D14236%26qos.enable%3Dfalse%26register.ip%3D192.168.44.1%26release%3D2.7.5%26side%3Dconsumer%26sticky%3Dfalse%26timestamp%3D1686632744577
  &registry=zookeeper
  &release=2.7.5
  &timestamp=1686632744603
```

5. `org.apache.dubbo.registry.integration.RegistryProtocol#refer`：

获取注册中心并执行服务引用。

```java
public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
    // 转换协议
    url = getRegistryUrl(url);

    // 通过 Zookeeper 链接到注册中心 【ZookeeperRegistryFactory】
    Registry registry = registryFactory.getRegistry(url);
    if (RegistryService.class.equals(type)) {
        return proxyFactory.getInvoker((T) registry, type, url);
    }

    // group="a,b" or group="*"
    Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(REFER_KEY));
    String group = qs.get(GROUP_KEY);
    if (group != null && group.length() > 0) {
        if ((COMMA_SPLIT_PATTERN.split(group)).length > 1 || "*".equals(group)) {
            return doRefer(getMergeableCluster(), registry, type, url);
        }
    }
    return doRefer(cluster, registry, type, url);
}
```

6. `org.apache.dubbo.registry.integration.RegistryProtocol#doRefer`：

```java
private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
    // 创建 RegistryDirectory 对象，并设置注册中心
    RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
    directory.setRegistry(registry);
    directory.setProtocol(protocol);
    // all attributes of REFER_KEY
    Map<String, String> parameters = new HashMap<String, String>(directory.getUrl().getParameters());

    // 创建订阅 URL
    URL subscribeUrl = new URL(CONSUMER_PROTOCOL, parameters.remove(REGISTER_IP_KEY), 0, type.getName(), parameters);
    if (!ANY_VALUE.equals(url.getServiceInterface()) && url.getParameter(REGISTER_KEY, true)) {
        directory.setRegisteredConsumerUrl(getRegisteredConsumerUrl(subscribeUrl, url));

        // 向注册中心注册服务消费者
        registry.register(directory.getRegisteredConsumerUrl());
    }
    directory.buildRouterChain(subscribeUrl);

    // 向注册中心订阅服务提供者
    directory.subscribe(subscribeUrl.addParameter(CATEGORY_KEY,
            PROVIDERS_CATEGORY + "," + CONFIGURATORS_CATEGORY + "," + ROUTERS_CATEGORY));

    // 创建 Invoker 对象
    Invoker invoker = cluster.join(directory);
    return invoker;
}
```

上述代码大致做了三件事：

- 1、向注册中心注册服务消费者（自身）。

- 2、向注册中心订阅服务提供者。

- 3、通过 Cluster 创建 Invoker 对象。

重点在于`directory.subscribe()`，它通过订阅注册中心的服务提供者，调用相关协议的引用方法，最终会调用到`DubboProtocol#refer`。

7. 方法调用链

```java
// 方法调用，最终会调用到 DubboProtocol#refer，但是 DubboProtocol 没有重写 refer 方法，所以执行父类方法。
org.apache.dubbo.registry.integration.RegistryDirectory#subscribe
  ➡ org.apache.dubbo.registry.ListenerRegistryWrapper#subscribe
    ➡ org.apache.dubbo.registry.support.FailbackRegistry#subscribe
      ➡ org.apache.dubbo.registry.zookeeper.ZookeeperRegistry#doSubscribe
        ➡ org.apache.dubbo.registry.support.FailbackRegistry#notify
          ➡ org.apache.dubbo.registry.support.FailbackRegistry#doNotify
            ➡ org.apache.dubbo.registry.support.AbstractRegistry#notify(URL, NotifyListener, List<URL>)
              ➡ org.apache.dubbo.registry.integration.RegistryDirectory#notify
                ➡ org.apache.dubbo.registry.integration.RegistryDirectory#refreshOverrideAndInvoker
                  ➡ org.apache.dubbo.registry.integration.RegistryDirectory#refreshInvoker
                    ➡ org.apache.dubbo.registry.integration.RegistryDirectory#toInvokers
                     ➡ org.apache.dubbo.rpc.protocol.ProtocolListenerWrapper#refer
                      ➡ org.apache.dubbo.qos.protocol.QosProtocolWrapper#refer
                        ➡ org.apache.dubbo.rpc.protocol.ProtocolFilterWrapper#refer
                          ➡ org.apache.dubbo.rpc.protocol.AbstractProtocol#refer
```

8. `org.apache.dubbo.rpc.protocol.AbstractProtocol#refer`：

```java
public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
    return new AsyncToSyncInvoker<>(protocolBindingRefer(type, url));
}
```

9. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#protocolBindingRefer`：

```java
public <T> Invoker<T> protocolBindingRefer(Class<T> serviceType, URL url) throws RpcException {
    optimizeSerialization(url);

    // create rpc invoker.
    DubboInvoker<T> invoker = new DubboInvoker<T>(serviceType, url, getClients(url), invokers);
    invokers.add(invoker);

    return invoker;
}
```

10. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#getClients`：

创建通信客户端，默认创建共享连接客户端。

```java
private ExchangeClient[] getClients(URL url) {
    // whether to share connection

    // 共享连接标识符
    boolean useShareConnect = false;

    // 获取连接数，默认为 0 表示未配置
    int connections = url.getParameter(Constants.CONNECTIONS_KEY, 0);
    List<ReferenceCountExchangeClient> shareClients = null;
    // if not configured, connection is shared, otherwise, one connection for one service

    // 如果未配置 connections 则共享连接.
    if (connections == 0) {
        useShareConnect = true;

        /**
         * The xml configuration should have a higher priority than properties.
         */
        String shareConnectionsStr = url.getParameter(Constants.SHARE_CONNECTIONS_KEY, (String) null);
        connections = Integer.parseInt(StringUtils.isBlank(shareConnectionsStr) ? ConfigUtils.getProperty(Constants.SHARE_CONNECTIONS_KEY,
                Constants.DEFAULT_SHARE_CONNECTIONS) : shareConnectionsStr);
        shareClients = getSharedClient(url, connections);
    }

    ExchangeClient[] clients = new ExchangeClient[connections];
    for (int i = 0; i < clients.length; i++) {
        if (useShareConnect) {
            // 获取共享客户端
            clients[i] = shareClients.get(i);

        } else {

            // 初始化新的客户端
            clients[i] = initClient(url);
        }
    }

    return clients;
}
```

11. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#getSharedClient`：

```java
private List<ReferenceCountExchangeClient> getSharedClient(URL url, int connectNum) {
    // 服务地址    
    String key = url.getAddress();

    // 查询缓存是不是已经创建了客户端
    List<ReferenceCountExchangeClient> clients = referenceClientMap.get(key);

    if (checkClientCanUse(clients)) {
        batchClientRefIncr(clients);
        return clients;
    }

    locks.putIfAbsent(key, new Object());
    synchronized (locks.get(key)) {
        clients = referenceClientMap.get(key);
        // dubbo check
        if (checkClientCanUse(clients)) {
            batchClientRefIncr(clients);
            return clients;
        }

        // connectNum must be greater than or equal to 1
        connectNum = Math.max(connectNum, 1);

        // If the clients is empty, then the first initialization is
        if (CollectionUtils.isEmpty(clients)) {
            /*
                1、初始化客户端；
                2、封装成 ReferenceCountExchangeClient 对象，用于引用计数。
            */
            clients = buildReferenceCountExchangeClientList(url, connectNum);

            // 放入缓存
            referenceClientMap.put(key, clients);

        } else {
            for (int i = 0; i < clients.size(); i++) {
                ReferenceCountExchangeClient referenceCountExchangeClient = clients.get(i);
                // If there is a client in the list that is no longer available, create a new one to replace him.
                if (referenceCountExchangeClient == null || referenceCountExchangeClient.isClosed()) {
                    clients.set(i, buildReferenceCountExchangeClient(url));
                    continue;
                }

                referenceCountExchangeClient.incrementAndGetCount();
            }
        }

        /**
         * I understand that the purpose of the remove operation here is to avoid the expired url key
         * always occupying this memory space.
         */
        locks.remove(key);

        return clients;
    }
}
```

12. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#buildReferenceCountExchangeClientList`：

```java
private List<ReferenceCountExchangeClient> buildReferenceCountExchangeClientList(URL url, int connectNum) {
    List<ReferenceCountExchangeClient> clients = new CopyOnWriteArrayList<>();

    for (int i = 0; i < connectNum; i++) {
        clients.add(buildReferenceCountExchangeClient(url));
    }

    return clients;
}
```

13. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#buildReferenceCountExchangeClient`：

```java
private ReferenceCountExchangeClient buildReferenceCountExchangeClient(URL url) {
    ExchangeClient exchangeClient = initClient(url);

    return new ReferenceCountExchangeClient(exchangeClient);
}
```

14. `org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#initClient`：

```java
/**
 * Create new connection
 *
 * @param url
 */
private ExchangeClient initClient(URL url) {

    // client type setting.
    // 客户端类型，默认为 netty
    String str = url.getParameter(Constants.CLIENT_KEY, url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_CLIENT));

    // 编解码
    url = url.addParameter(Constants.CODEC_KEY, DubboCodec.NAME);
    // enable heartbeat by default

    // 心跳机制
    url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));

    // BIO is not allowed since it has severe performance issue.
    if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
        throw new RpcException("Unsupported client type: " + str + "," +
                " supported client type is " + StringUtils.join(ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions(), " "));
    }

    ExchangeClient client;
    try {
        // connection should be lazy
        if (url.getParameter(Constants.LAZY_CONNECT_KEY, false)) {
            client = new LazyConnectExchangeClient(url, requestHandler);

        } else {
            // 创建连接客户端
            client = Exchangers.connect(url, requestHandler);
        }

    } catch (RemotingException e) {
        throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), e);
    }

    return client;
}
```

15. `org.apache.dubbo.remoting.exchange.Exchangers#connect(URL, ExchangeHandler)`：

```java
public static ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    if (handler == null) {
        throw new IllegalArgumentException("handler == null");
    }
    url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
    return getExchanger(url).connect(url, handler);
}
```

16. `org.apache.dubbo.remoting.exchange.support.header.HeaderExchanger#connect`：

```java
@Override
public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
    return new HeaderExchangeClient(Transporters.connect(url, new DecodeHandler(new HeaderExchangeHandler(handler))), true);
}
```

17. `org.apache.dubbo.remoting.Transporters#connect(URL, ChannelHandler...)`：

```java
public static Client connect(URL url, ChannelHandler... handlers) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    ChannelHandler handler;
    if (handlers == null || handlers.length == 0) {
        handler = new ChannelHandlerAdapter();
    } else if (handlers.length == 1) {
        handler = handlers[0];
    } else {
        handler = new ChannelHandlerDispatcher(handlers);
    }
    return getTransporter().connect(url, handler);
}
```

18. `org.apache.dubbo.remoting.transport.netty4.NettyTransporter#connect`：

```java
public Client connect(URL url, ChannelHandler listener) throws RemotingException {
    return new NettyClient(url, listener);
}
```

至此，dubbo 服务引用的大致流程分析完。而Dubbo默认使用 Javassist 创建代理。过程比较简单，就不再叙述。

#### 总结

1. 通过 FactoryBean 或者 afterPropertiesSet() 方法触发服务引用。

2. 连接注册中心，获取服务提供者列表，创建 Invoker。

3. 订阅服务提供者节点，数据变更时监听通知。

4. 连接通信服务器，获得客户端。

5. 创建代理并返回。

6. 此时 <dubbo:reference\> 声明的对象就是这个代理对象。

### 服务调用

> Dubbo 默认使用 Javassist 框架为服务接口生成动态代理类

#### 服务调用入口

dubbo 服务调用入口`String info = userService.sayHi("hello world");`

1. `org.apache.dubbo.rpc.proxy.InvokerInvocationHandler#invoke`：

```java
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
        return method.invoke(invoker, args);
    }
    String methodName = method.getName();
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length == 0) {
        if ("toString".equals(methodName)) {
            return invoker.toString();
        } else if ("$destroy".equals(methodName)) {
            invoker.destroy();
            return null;
        } else if ("hashCode".equals(methodName)) {
            return invoker.hashCode();
        }
    } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
        return invoker.equals(args[0]);
    }
    RpcInvocation rpcInvocation = new RpcInvocation(method, invoker.getInterface().getName(), args);
    rpcInvocation.setTargetServiceUniqueName(invoker.getUrl().getServiceKey());

    // 将 method、args 封装成 RpcInvocation 并执行后续调用
    return invoker.invoke(rpcInvocation).recreate();
}
```

InvokerInvocationHandler 中的 invoker 成员变量类型是 MockClusterInvoker，其内部封装服务降级的逻辑。

2. `org.apache.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker#invoke`：

```java
public Result invoke(Invocation invocation) throws RpcException {
    Result result = null;

    // 获取 mock 配置值
    String value = directory.getUrl().getMethodParameter(invocation.getMethodName(), MOCK_KEY, Boolean.FALSE.toString()).trim();

    // no mock，直接调用其他 Invoker 对象的 invoke 方法，默认是 FailoverClusterInvoker
    if (value.length() == 0 || "false".equalsIgnoreCase(value)) {
        //no mock
        result = this.invoker.invoke(invocation);
    } else if (value.startsWith("force")) {

        // force:xxx 不发起远程调用，直接执行 mock 逻辑
        if (logger.isWarnEnabled()) {
            logger.warn("force-mock: " + invocation.getMethodName() + " force-mock enabled , url : " + directory.getUrl());
        }
        //force:direct mock
        result = doMockInvoke(invocation, null);
    } else {

        // fail:xxx 表示消费方对调用服务失败后，再执行 mock 逻辑，不抛出异常
        //fail-mock
        try {
            result = this.invoker.invoke(invocation);

            //fix:#4585
            if(result.getException() != null && result.getException() instanceof RpcException){
                RpcException rpcException= (RpcException)result.getException();
                if(rpcException.isBiz()){
                    throw  rpcException;
                }else {

                    // 用失败，执行 mock 逻辑
                    result = doMockInvoke(invocation, rpcException);
                }
            }

        } catch (RpcException e) {
            if (e.isBiz()) {
                throw e;
            }

            if (logger.isWarnEnabled()) {
                logger.warn("fail-mock: " + invocation.getMethodName() + " fail-mock enabled , url : " + directory.getUrl(), e);
            }
            result = doMockInvoke(invocation, e);
        }
    }
    return result;
}
```

3. 方法调用链

```java
org.apache.dubbo.rpc.cluster.support.wrapper.AbstractCluster.InterceptorInvokerNode#invoke
  ➡ org.apache.dubbo.rpc.cluster.interceptor.ClusterInterceptor#intercept
    ➡ org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker#invoke
      ➡ org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker#doInvoke
        ➡ org.apache.dubbo.rpc.protocol.InvokerWrapper#invoke
          ➡ org.apache.dubbo.rpc.Invoker#invoke [org.apache.dubbo.rpc.protocol.ProtocolFilterWrapper#buildInvokerChain() 匿名内部类]
            ➡ org.apache.dubbo.rpc.filter.ConsumerContextFilter#invoke
              ➡ org.apache.dubbo.rpc.Invoker#invoke [org.apache.dubbo.rpc.protocol.ProtocolFilterWrapper#buildInvokerChain() 匿名内部类]
                ➡ org.apache.dubbo.rpc.protocol.dubbo.filter.FutureFilter#invoke
                  ➡ org.apache.dubbo.rpc.Invoker#invoke [org.apache.dubbo.rpc.protocol.ProtocolFilterWrapper#buildInvokerChain() 匿名内部类]
                    ➡ org.apache.dubbo.monitor.support.MonitorFilter#invoke
                      ➡ org.apache.dubbo.rpc.listener.ListenerInvokerWrapper#invoke
                        ➡ org.apache.dubbo.rpc.protocol.AsyncToSyncInvoker#invoke
                          ➡ org.apache.dubbo.rpc.protocol.AbstractInvoker#invoke
```

4. `org.apache.dubbo.rpc.protocol.AbstractInvoker#invoke`：

```java
public Result invoke(Invocation inv) throws RpcException {
    // if invoker is destroyed due to address refresh from registry, let's allow the current invoke to proceed
    if (destroyed.get()) {
        logger.warn("Invoker for service " + this + " on consumer " + NetUtils.getLocalHost() + " is destroyed, "
                + ", dubbo version is " + Version.getVersion() + ", this invoker should not be used any longer");
    }
    RpcInvocation invocation = (RpcInvocation) inv;

    // 设置 Invoker
    invocation.setInvoker(this);
    if (CollectionUtils.isNotEmptyMap(attachment)) {

        // 设置 attachment
        invocation.addAttachmentsIfAbsent(attachment);
    }
    Map<String, String> contextAttachments = RpcContext.getContext().getAttachments();
    if (CollectionUtils.isNotEmptyMap(contextAttachments)) {

        // 添加 contextAttachments 到 RpcInvocation#attachment 变量中
        invocation.addAttachments(contextAttachments);
    }

    // 置异步信息到 RpcInvocation#attachment 中
    invocation.setInvokeMode(RpcUtils.getInvokeMode(url, invocation));
    RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);

    AsyncRpcResult asyncResult;
    try {

        // 模板方法，由子类实现
        asyncResult = (AsyncRpcResult) doInvoke(invocation);
    } catch (InvocationTargetException e) { // biz exception
        // ... 省略
    }
    RpcContext.getContext().setFuture(new FutureAdapter(asyncResult.getResponseFuture()));
    return asyncResult;
}
```

5. `org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker#doInvoke`：

```java
protected Result doInvoke(final Invocation invocation) throws Throwable {
    RpcInvocation inv = (RpcInvocation) invocation;
    final String methodName = RpcUtils.getMethodName(invocation);

    // 设置 path 和 version 到 attachment 中
    inv.setAttachment(PATH_KEY, getUrl().getPath());
    inv.setAttachment(VERSION_KEY, version);

    ExchangeClient currentClient;

    // 获取客户端，服务引用时将 clients 传入DubboInvoker构造函数的.
    if (clients.length == 1) {
        currentClient = clients[0];
    } else {
        currentClient = clients[index.getAndIncrement() % clients.length];
    }
    try {

        // isOneway 为 true，表示“单向”通信
        boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
        int timeout = getUrl().getMethodPositiveParameter(methodName, TIMEOUT_KEY, DEFAULT_TIMEOUT);
        if (isOneway) {
            boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
            currentClient.send(inv, isSent);
            return AsyncRpcResult.newDefaultAsyncResult(invocation);
        } else {
            ExecutorService executor = getCallbackExecutor(getUrl(), inv);
            CompletableFuture<AppResponse> appResponseFuture =
                    currentClient.request(inv, timeout, executor).thenApply(obj -> (AppResponse) obj);
            // save for 2.6.x compatibility, for example, TraceFilter in Zipkin uses com.alibaba.xxx.FutureAdapter
            FutureContext.getContext().setCompatibleFuture(appResponseFuture);
            AsyncRpcResult result = new AsyncRpcResult(appResponseFuture, inv);
            result.setExecutor(executor);
            return result;
        }
    } catch (TimeoutException e) {
        throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
    } catch (RemotingException e) {
        throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
    }
}
```

6. `org.apache.dubbo.rpc.protocol.dubbo.ReferenceCountExchangeClient#request()`：

```java
public CompletableFuture<Object> request(request, timeout, executor) throws RemotingException {
    return client.request(request, timeout, executor);
}
```

5. `org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeClient#request()`：

```java
public CompletableFuture<Object> request(Object request, int timeout, ExecutorService executor) throws RemotingException {
    return channel.request(request, timeout, executor);
}
```

6. `org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeChannel#request()`：

```java
public CompletableFuture<Object> request(Object request, int timeout, ExecutorService executor) throws RemotingException {
    if (closed) {
        throw new RemotingException(this.getLocalAddress(), null, "Failed to send request " + request + ", cause: The channel " + this + " is closed!");
    }
    // create request.
    Request req = new Request();
    req.setVersion(Version.getProtocolVersion());

    // 设置双向通信标志为 true
    req.setTwoWay(true);

    // 这里的 request 变量类型为 RpcInvocation
    req.setData(request);
    DefaultFuture future = DefaultFuture.newFuture(channel, req, timeout, executor);
    try {
        // 调用 NettyClient#send 方法发送请求
        channel.send(req);
    } catch (RemotingException e) {
        future.cancel();
        throw e;
    }
    return future;
}
```

7. `org.apache.dubbo.remoting.transport.AbstractPeer#send`：

```java
public void send(Object message) throws RemotingException {
    // 该方法由  AbstractClient 实现
    send(message, url.getParameter(Constants.SENT_KEY, false));
}
```

8. `org.apache.dubbo.remoting.transport.AbstractClient#send`：

```java
public void send(Object message, boolean sent) throws RemotingException {
    if (needReconnect && !isConnected()) {
        connect();
    }

    // 获取 Channel，是一个模板方法，由具体子类实现
    Channel channel = getChannel();
    //TODO Can the value returned by getChannel() be null? need improvement.
    if (channel == null || !channel.isConnected()) {
        throw new RemotingException(this, "message can not send, because channel is closed . url:" + getUrl());
    }
    channel.send(message, sent);
}
```

9. `org.apache.dubbo.remoting.transport.netty4.NettyChannel#send`：

```java
public void send(Object message, boolean sent) throws RemotingException {
    // whether the channel is closed
    super.send(message, sent);

    boolean success = true;
    int timeout = 0;
    try {

        // 发送消息（包含请求和相应消息）
        ChannelFuture future = channel.writeAndFlush(message);

        /* sent 的值源于 <dubbo:method sent="true/false" /> 中 sent 的配置值，有两种配置值：
             1. true: 等待消息发出，消息发送失败将抛出异常
             2. false: 不等待消息发出，将消息放入 IO 队列，即刻返回
           默认情况下 sent = false；
        */ 
        if (sent) {
            // wait timeout ms
            timeout = getUrl().getPositiveParameter(TIMEOUT_KEY, DEFAULT_TIMEOUT);
            success = future.await(timeout);
        }
        Throwable cause = future.cause();
        if (cause != null) {
            throw cause;
        }
    } catch (Throwable e) {
        removeChannelIfDisconnected(channel);
        throw new RemotingException(this, "Failed to send message " + PayloadDropper.getRequestWithoutData(message) + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
    }
    if (!success) {
        throw new RemotingException(this, "Failed to send message " + PayloadDropper.getRequestWithoutData(message) + " to " + getRemoteAddress()
                + "in timeout(" + timeout + "ms) limit");
    }
}
```

至此，请求数据的发送过程就结束了，后续就是提供方接受请求并处理，提供方相应请求，消费方接受调用结果。

如下是整个调用逻辑链：

```txt
proxy0#sayHi(String)
  —> InvokerInvocationHandler#invoke(Object, Method, Object[])
    —> MockClusterInvoker#invoke(Invocation)
      —> AbstractClusterInvoker#invoke(Invocation)
        —> FailoverClusterInvoker#doInvoke(Invocation, List<Invoker<T>>, LoadBalance)
          —> Filter#invoke(Invoker, Invocation)  // 包含多个 Filter 调用
            —> ListenerInvokerWrapper#invoke(Invocation) 
              —> AbstractInvoker#invoke(Invocation) 
                —> DubboInvoker#doInvoke(Invocation)
                  —> ReferenceCountExchangeClient#request(Object, int)
                    —> HeaderExchangeClient#request(Object, int)
                      —> HeaderExchangeChannel#request(Object, int)
                        —> AbstractPeer#send(Object)
                          —> AbstractClient#send(Object, boolean)
                            —> NettyChannel#send(Object, boolean)
                              —> NioClientSocketChannel#write(Object)
```

#### 总结

1. Dubbo框架根据服务接口生成一个服务引用对象，包含了服务接口、协议等信息。（代理对象）

2. Dubbo框架根据服务引用对象中的配置，选择合适的负载均衡策略和集群容错机制。

3. 服务消费者发起远程调用请求，Dubbo框架根据服务引用对象的信息构建调用请求。

4. Dubbo框架选择一个可用的服务提供者，通过网络通信组件向其发送调用请求。

5. 服务提供者接收到调用请求，Dubbo框架根据请求的接口名、方法名找到对应的服务实例，并调用相应的方法。

6. 服务实例执行方法逻辑，并返回执行结果。

7. Dubbo框架将执行结果封装成响应对象，并通过网络通信组件发送给服务消费者。

8. 服务消费者接收响应对象，解析并处理响应结果。

### SPI

> https://juejin.cn/post/6944733434593935391#heading-14

## 附录

#### checkAndUpdateSubConfigs()

```java
// org.apache.dubbo.config.ServiceConfig#checkAndUpdateSubConfigs

private void checkAndUpdateSubConfigs() {
    // Use default configs defined explicitly with global scope
    /*
      检测 provider、application 等核心配置类对象是否为空
      若为空则尝试从其他配置类中获取相应的实例。
    */
    completeCompoundConfigs();

    // 检测 provider 属性是否为空，若为空则创建一个 ProviderConfig 并调用 refresh().
    checkDefault();

    // 检测 protocol 属性
    checkProtocol();
    // if protocol is not injvm checkRegistry

    //检测 protocol 数量是否为 1，并且名字是 injvm
    if (!isOnlyInJvm()) {
        checkRegistry();
    }
    this.refresh();

    if (StringUtils.isEmpty(interfaceName)) {
        throw new IllegalStateException("<dubbo:service interface=\"\" /> interface not allow null!");
    }

    // 检测 ref 是否为泛化服务类型
    if (ref instanceof GenericService) {
        // 设置 interfaceClass 为 GenericService.class
        interfaceClass = GenericService.class;
        if (StringUtils.isEmpty(generic)) {
            设置 generic = “true”
            generic = Boolean.TRUE.toString();
        }
    } else {

        // ref 非泛化服务类型
        try {
            // 对 interfaceClass 以及 <dubbo:method> 标签中的必要字段进行检查
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        // 对 ref 合法性进行检测
        checkInterfaceAndMethods(interfaceClass, getMethods());
        checkRef();
        generic = Boolean.FALSE.toString();
    }

    // local 和 stub 在功能上应该是一致的，用于配置本地存根
    if (local != null) {
        if ("true".equals(local)) {
            local = interfaceName + "Local";
        }
        Class<?> localClass;
        try {
            localClass = ClassUtils.forNameWithThreadContextClassLoader(local);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (!interfaceClass.isAssignableFrom(localClass)) {
            throw new IllegalStateException("The local implementation class " + localClass.getName() + " not implement interface " + interfaceName);
        }
    }
    if (stub != null) {
        if ("true".equals(stub)) {
            stub = interfaceName + "Stub";
        }
        Class<?> stubClass;
        try {
            stubClass = ClassUtils.forNameWithThreadContextClassLoader(stub);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (!interfaceClass.isAssignableFrom(stubClass)) {
            throw new IllegalStateException("The stub implementation class " + stubClass.getName() + " not implement interface " + interfaceName);
        }
    }
    checkStubAndLocal(interfaceClass);

    // 检测各种对象是否为空，为空则新建，或抛异常
    ConfigValidationUtils.checkMock(interfaceClass, this);
    ConfigValidationUtils.validateServiceConfig(this);

    // 动态添加参数（通过SPI机制调用 org.apache.dubbo.config.AppendParametersComponent 实现类并调用 appendExportParameters 方法将 ServiceConfig 对象传入）
    appendParameters();
}
```

配置检查的逻辑：

1. 检测 `<dubbo:services\>` 标签的 interface 属性合法性，不合法则抛出异常。

2. 检测 ProviderConfig、ApplicationConfig 等核心配置类对象是否为空，若为空，则尝试从其他配置类对象中获取相应的实例。

3. 检测并处理泛化服务和普通服务类。

4. 检测本地存根配置，并进行相应的处理。

5. 对 ApplicationConfig、RegistryConfig等配置类进行检测，为空则尝试创建，若无法创建则抛出异常。

#### loadRegistries()

```java
// org.apache.dubbo.config.utils.ConfigValidationUtils#loadRegistries

public static List<URL> loadRegistries(AbstractInterfaceConfig interfaceConfig, boolean provider) {
    // check && override if necessary
    List<URL> registryList = new ArrayList<URL>();

    // 获取全局配置
    ApplicationConfig application = interfaceConfig.getApplication();

    // 获取注册中心配置集合
    List<RegistryConfig> registries = interfaceConfig.getRegistries();
    if (CollectionUtils.isNotEmpty(registries)) {
        for (RegistryConfig config : registries) {
            String address = config.getAddress();
            if (StringUtils.isEmpty(address)) {
                address = ANYHOST_VALUE;
            }
            if (!RegistryConfig.NO_AVAILABLE.equalsIgnoreCase(address)) {
                Map<String, String> map = new HashMap<String, String>();

                // 添加 ApplicationCOnfig、RegistryConfig 字段信息以及 path、pid、protocol 等信息到 map 中
                AbstractConfig.appendParameters(map, application);
                AbstractConfig.appendParameters(map, config);
                map.put(PATH_KEY, RegistryService.class.getName());
                AbstractInterfaceConfig.appendRuntimeParameters(map);
                if (!map.containsKey(PROTOCOL_KEY)) {
                    map.put(PROTOCOL_KEY, DUBBO_PROTOCOL);
                }

                // 解析得到 URL 列表，address 可能包含多个注册中心 ip
                List<URL> urls = UrlUtils.parseURLs(address, map);

                for (URL url : urls) {

                    url = URLBuilder.from(url)
                            .addParameter(REGISTRY_KEY, url.getProtocol())
                            .setProtocol(extractRegistryType(url))
                            .build();
                    if ((provider && url.getParameter(REGISTER_KEY, true))
                            || (!provider && url.getParameter(SUBSCRIBE_KEY, true))) {
                        registryList.add(url);
                    }
                }
            }
        }
    }
    return registryList;
}
```

`loadRegistries()`主要包含的逻辑：

1. 构建参数映射 map。

2. 构建注册中心链接列表。

3. 遍历链接列表，并按照条件将其添加到 registryList 中。

#### exportLocal()暴露服务到本地

可能存在同一个 JVM 内部引用自身服务的情况，暴露的本地服务在内部调用的时候可以直接消费同一个 JVM 的服务避免了网络间的通信。

最终会生成一个InjvmExporter对象，作为服务暴露对象：

```java
// org.apache.dubbo.config.ServiceConfig#exportLocal

/**
 * always export injvm
 */
private void exportLocal(URL url) {
    // 构建 injvm: 127.0.0.1，并在本地导出服务
    URL local = URLBuilder.from(url)
            .setProtocol(LOCAL_PROTOCOL)
            .setHost(LOCALHOST_VALUE)
            .setPort(0)
            .build();

    // 根据 SPI 机制，调用 InjvmProtocol.export()
    Exporter<?> exporter = protocol.export(
            PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, local));
    exporters.add(exporter);
    logger.info("Export dubbo service " + interfaceClass.getName() + " to local registry url : " + local);
}
```

#### getInvoker()

Invoker 是实体域，它是 Dubbo 的核心模型，其它模型都向它靠扰，或转换成它，它代表一个可执行体，可向它发起 invoke 调用，它有可能是一个本地的实现，也可能是一个远程的实现，也可能一个集群实现。

Dubbo 默认的 ProxyFactory 实现类是 JavassistProxyFactory。

```java
// org.apache.dubbo.rpc.proxy.javassist.JavassistProxyFactory#getInvoker

@Override
public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
    // TODO Wrapper cannot handle this scenario correctly: the classname contains '$'

    // 为目标类创建 Wrapper
    final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);

    // 创建匿名 Invoker 对象，并实现 doInvoke 方法
    return new AbstractProxyInvoker<T>(proxy, type, url) {
        @Override
        protected Object doInvoke(T proxy, String methodName,
                                  Class<?>[] parameterTypes,
                                  Object[] arguments) throws Throwable {
            return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
        }
    };
}
```

#### register()

```java
// org.apache.dubbo.registry.integration.RegistryProtocol#register

public void register(URL registryUrl, URL registeredProviderUrl) {
    Registry registry = registryFactory.getRegistry(registryUrl);
    registry.register(registeredProviderUrl);

    ProviderModel model = ApplicationModel.getProviderModel(registeredProviderUrl.getServiceKey());
    model.addStatedUrl(new ProviderModel.RegisterStatedURL(
            registeredProviderUrl,
            registryUrl,
            true
    ));
}

// org.apache.dubbo.registry.support.FailbackRegistry#register

@Override
public void register(URL url) {
    if (!acceptable(url)) {
        logger.info("URL " + url + " will not be registered to Registry. Registry " + url + " does not accept service of this protocol type.");
        return;
    }
    super.register(url);
    removeFailedRegistered(url);
    removeFailedUnregistered(url);
    try {
        // Sending a registration request to the server side
        doRegister(url);
    } catch (Exception e) {
        Throwable t = e;

        // If the startup detection is opened, the Exception is thrown directly.
        boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                && url.getParameter(Constants.CHECK_KEY, true)
                && !CONSUMER_PROTOCOL.equals(url.getProtocol());
        boolean skipFailback = t instanceof SkipFailbackWrapperException;
        if (check || skipFailback) {
            if (skipFailback) {
                t = t.getCause();
            }
            throw new IllegalStateException("Failed to register " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
        } else {
            logger.error("Failed to register " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }

        // Record a failed registration request to a failed list, retry regularly
        addFailedRegistered(url);
    }
}

// org.apache.dubbo.registry.zookeeper.ZookeeperRegistry#doRegister
@Override
public void doRegister(URL url) {
    try {
        zkClient.create(toUrlPath(url), url.getParameter(DYNAMIC_KEY, true));
    } catch (Throwable e) {
        throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
    }
}
```

当前程序使用的是 Zookeeper 作为注册中心，所以使用的是`ZookeeperRegistry`，但是`ZookeeperRegistry`中没有重写`register()`方法，所以调用的是父类中的方法（`org.apache.dubbo.registry.support.FailbackRegistry#register`）。

在`FailbackRegistry#register`方法中又会调用`doRegister()`模板方法，实际调用的是`ZookeeperRegistry`中的实现。

#### shouldJvmRefer()

```java
// org.apache.dubbo.config.ReferenceConfig#shouldJvmRefer

/**
 * Figure out should refer the service in the same JVM from configurations. The default behavior is true
 * 1. if injvm is specified, then use it
 * 2. then if a url is specified, then assume it's a remote call
 * 3. otherwise, check scope parameter
 * 4. if scope is not specified but the target service is provided in the same JVM, then prefer to make the local
 * call, which is the default behavior
 */
protected boolean shouldJvmRefer(Map<String, String> map) {
    URL tmpUrl = new URL("temp", "localhost", 0, map);
    boolean isJvmRefer;
    if (isInjvm() == null) {
        // if a url is specified, don't do local reference
        if (url != null && url.length() > 0) {
            isJvmRefer = false;
        } else {
            // by default, reference local service if there is
            isJvmRefer = InjvmProtocol.getInjvmProtocol().isInjvmRefer(tmpUrl);
        }
    } else {
        isJvmRefer = isInjvm();
    }
    return isJvmRefer;
}
```

#### doSubscribe()

```java
// org.apache.dubbo.registry.zookeeper.ZookeeperRegistry#doSubscribe

public void doSubscribe(final URL url, final NotifyListener listener) {
    try {
        if (ANY_VALUE.equals(url.getServiceInterface())) {
            String root = toRootPath();
            ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
            if (listeners == null) {
                zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
                listeners = zkListeners.get(url);
            }
            ChildListener zkListener = listeners.get(listener);
            if (zkListener == null) {
                listeners.putIfAbsent(listener, (parentPath, currentChilds) -> {
                    for (String child : currentChilds) {
                        child = URL.decode(child);
                        if (!anyServices.contains(child)) {
                            anyServices.add(child);
                            subscribe(url.setPath(child).addParameters(INTERFACE_KEY, child,
                                    Constants.CHECK_KEY, String.valueOf(false)), listener);
                        }
                    }
                });
                zkListener = listeners.get(listener);
            }
            zkClient.create(root, false);
            List<String> services = zkClient.addChildListener(root, zkListener);
            if (CollectionUtils.isNotEmpty(services)) {
                for (String service : services) {
                    service = URL.decode(service);
                    anyServices.add(service);
                    subscribe(url.setPath(service).addParameters(INTERFACE_KEY, service,
                            Constants.CHECK_KEY, String.valueOf(false)), listener);
                }
            }
        } else {
            List<URL> urls = new ArrayList<>();

            /*
              toCategoriesPath(url) 获取注册中心除了消费者信息之外的所有节点
              eg: configurators, routers, providers
            */
            for (String path : toCategoriesPath(url)) {

                // 获取节点监听器管理容器
                ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                if (listeners == null) {
                    zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
                    listeners = zkListeners.get(url);
                }

                // 获取节点监听器
                ChildListener zkListener = listeners.get(listener);

                // 如果为空则创建一个监听器
                if (zkListener == null) {
                    listeners.putIfAbsent(listener, (parentPath, currentChilds) -> ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds)));
                    zkListener = listeners.get(listener);
                }

                // 创建节点
                zkClient.create(path, false);

                // 订阅节点 path，如果数据变化，通知到 zkListener。即  ZookeeperRegistry.this.notify
                List<String> children = zkClient.addChildListener(path, zkListener);
                if (children != null) {
                    urls.addAll(toUrlsWithEmpty(url, path, children));
                }
            }
            // 通知
            notify(url, listener, urls);
        }
    } catch (Throwable e) {
        throw new RpcException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
    }
}
```

上述逻辑重点就是通过注册中心获取到服务提供者的 url，订阅服务提供者的节点数据变化，如果有变化则调用 notify() 方法，在 notify() 方法中，最终会根据 url 调用服务引用方法 refer，再封装成 InvokerDelegate 对象。

```java
// org.apache.dubbo.registry.integration.RegistryDirectory#toInvokers

invoker = new InvokerDelegate<>(protocol.refer(serviceType, url), url, providerUrl);
```

这样不仅完成了本次的服务引用，而且当服务提供者上下线的时候，消费者也可以动态感知。

#### Listener & Filter 扩展点

ExporterListener(服务暴露监听) 与 InvokerListener(服务调用监听)

通过 ProtocolFilterWrapper#export 与 ProtocolFilterWrapper#refer 构建 Filter 对服务暴露方与服务引用方的 Invoke 进行拦截。

#### getChannel()

```java
// org.apache.dubbo.remoting.transport.netty4.NettyClient#getChannel

protected org.apache.dubbo.remoting.Channel getChannel() {
    Channel c = channel;
    if (c == null) {
        return null;
    }

    // 获取一个 NettyChannel 对象
    return NettyChannel.getOrAddChannel(c, getUrl(), this);
}
```

#### getOrAddChannel()

```java
// org.apache.dubbo.remoting.transport.netty4.NettyChannel#getOrAddChannel

static NettyChannel getOrAddChannel(Channel ch, URL url, ChannelHandler handler) {
    if (ch == null) {
        return null;
    }

    // 尝试从缓存中获取 NettyChannel 实例
    NettyChannel ret = CHANNEL_MAP.get(ch);
    if (ret == null) {

        // 如果没找到则创建一个 NettyChannel 实例
        NettyChannel nettyChannel = new NettyChannel(ch, url, handler);
        if (ch.isActive()) {
            nettyChannel.markActive(true);

            // 缓存
            ret = CHANNEL_MAP.putIfAbsent(ch, nettyChannel);
        }
        if (ret == null) {
            ret = nettyChannel;
        }
    }
    return ret;
}
```

#### 消费方发送请求

https://blog.csdn.net/wuyangyang555/article/details/122566485

#### cluster.join(directory)