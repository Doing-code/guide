# OpenFeign

## 前置

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <!--<version>3.0.6</version>-->
</dependency>
```

Starters可以理解为启动器，它包含了一系列可以集成到应用里面的依赖包。所以它大概率会和SpringBoot自动装配有联系，找一下`spring.factories`文件。

```factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.openfeign.hateoas.FeignHalAutoConfiguration,\
org.springframework.cloud.openfeign.FeignAutoConfiguration,\
org.springframework.cloud.openfeign.encoding.FeignAcceptGzipEncodingAutoConfiguration,\
org.springframework.cloud.openfeign.encoding.FeignContentGzipEncodingAutoConfiguration,\
org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration
```

再进行源码分析前，需要带着以下几个问题，有目的的源码学习：

1. 被`@FeignClient`注入的接口，如何被解析和注入的呢?

2. `@Autowired`可以针对`@FeignClient`注入实例对象，是如何注入的，注入的又是什么对象呢?

3. `FeignClient`声明的接口被解析后，以什么方式存储和调用的呢?

4. `OpenFeign`如何实现负载均衡的呢?

5. `OpenFeign`如何发现服务?

## OpenFeign源码解析

### FeignClient注入初始化

OpenFeign要生效需要使用`@EnableFeignClients`。

```java
package org.springframework.cloud.openfeign;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(FeignClientsRegistrar.class)
public @interface EnableFeignClients {
    // ...
}
```

可以看到`@EnableFeignClients`注解向容器种导入了一个类`FeignClientsRegistrar`，从类名上看也能知道其功能是 FeignClient 客户端类的注册。

```java
class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 获取 @EnableFeignClients 注解参数 defaultConfiguration，完成一些配置内容注册
		registerDefaultConfiguration(metadata, registry);
        // 重点，注册FeignClient客户端
		registerFeignClients(metadata, registry);
	}
}
```

关注点在`registerFeignClients()`方法

```java
class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    public void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

		LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();

        // 收集该注解的元数据信息：value ，basePackages ，basePackageClasses 等
		Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableFeignClients.class.getName());

        // 获取 @EnableFeignClients 注解中的 client 属性
		final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");

		// 如果没有配置client相关属性会进入到这里
		if (clients == null || clients.length == 0) {
			ClassPathScanningCandidateComponentProvider scanner = getScanner();
			scanner.setResourceLoader(this.resourceLoader);
            // 添加需要扫描的注解 @FeignClient
			scanner.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));
            // 根据 @EnableFeignClients 注解的属性信息去获取需要扫描的路径
			Set<String> basePackages = getBasePackages(metadata);
			for (String basePackage : basePackages) {
                // 找到候选的对象（标有 @FeignClient 注解的接口）封装成BeanDefinition对象
                // 然后将候选接口添加到 candidateComponents 集合中
				candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
			}
		}
		else {
            // 如果clients属性有值，则直接把指定的clients加入候选者
			for (Class<?> clazz : clients) {
				candidateComponents.add(new AnnotatedGenericBeanDefinition(clazz));
			}
		}

        // 遍历所有的接口，封装BeanDefinition，然后注册到Spring IOC容器.
		for (BeanDefinition candidateComponent : candidateComponents) {
			if (candidateComponent instanceof AnnotatedBeanDefinition) {
				// verify annotated class is an interface
				AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
				AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
				Assert.isTrue(annotationMetadata.isInterface(), "@FeignClient can only be specified on an interface");

                // 获取每个接口中定义的元数据信息，即 @FeignClient 注解中配置的属性值，例如：value,name,path,url等
				Map<String, Object> attributes = annotationMetadata
						.getAnnotationAttributes(FeignClient.class.getCanonicalName());

                // 获取name的属性值（服务名）
				String name = getClientName(attributes);

				// 注册该FeignClient的配置类，FeignClient注解上的configuration属性，其实是注册一个FeignClientSpecification
                // 该属性值将会作为构造器参数传入，每个FeignClient都有自己的spring上下文
				registerClientConfiguration(registry, name, attributes.get("configuration"));

                // 注册 FeignClient，其实就是注册一个FactoryBean
				registerFeignClient(registry, annotationMetadata, attributes);
			}
		}
	}

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        // @EnableFeignClients 元数据信息
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableFeignClients.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        // 遍历属性信息，拿到需要扫描的路径
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }
}
```

`registerFeignClients()`方法的主要步骤如下：

1. 查找FeignClient。

2. 得到一个 @FeignClient 的接口的集合。

3. 解析 @FeignClient 注解中的元数据信息。

4. 遍历FeignClient接口，注入一个动态Bean实例（通过动态代理的方式实现）

**registerFeignClient()**，其内部就是生成接口对应的代理类并诸如到容器中，是一个FeignClientFactoryBean。

```java
class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private void registerFeignClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata,
                Map<String, Object> attributes) {
        // 获取到标注了 @FeignClient 注解的接口全路径，eg: com.train.service.feign.BaseInfoManagementFeign
        String className = annotationMetadata.getClassName();
        Class clazz = ClassUtils.resolveClassName(className, null);
        ConfigurableBeanFactory beanFactory = registry instanceof ConfigurableBeanFactory
                ? (ConfigurableBeanFactory) registry : null;
        String contextId = getContextId(beanFactory, attributes);
        String name = getName(attributes);

        // 创建FeignClientFactoryBean
        FeignClientFactoryBean factoryBean = new FeignClientFactoryBean();
        factoryBean.setBeanFactory(beanFactory);
        // 将属性设置到 FeignClientFactoryBean 中，也就是在@FeignClient中配置的属性值
        factoryBean.setName(name);
        factoryBean.setContextId(contextId);
        factoryBean.setType(clazz);
        factoryBean.setRefreshableClient(isClientRefreshEnabled());

        /*
            设置 bean 的 instanceSupplier，指定spring bean实例化策略为 supplier 方式
            BeanDefinitionBuilder 用来构建一个 BeanDefinition
            它是通过 genericBeanDefinition 来构建的，并且传入了一个 FeignClientFactoryBean 的类
        */
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> {
            factoryBean.setUrl(getUrl(beanFactory, attributes));
            factoryBean.setPath(getPath(beanFactory, attributes));
            factoryBean.setDecode404(Boolean.parseBoolean(String.valueOf(attributes.get("decode404"))));
            Object fallback = attributes.get("fallback");
            if (fallback != null) {
                factoryBean.setFallback(fallback instanceof Class ? (Class<?>) fallback
                        : ClassUtils.resolveClassName(fallback.toString(), null));
            }
            Object fallbackFactory = attributes.get("fallbackFactory");
            if (fallbackFactory != null) {
                factoryBean.setFallbackFactory(fallbackFactory instanceof Class ? (Class<?>) fallbackFactory
                        : ClassUtils.resolveClassName(fallbackFactory.toString(), null));
            }
            return factoryBean.getObject();
        });
        // 指定按类型注入
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 懒加载
        definition.setLazyInit(true);
        // 属性值校验
        validate(attributes);
    
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, className);
        beanDefinition.setAttribute("feignClientsRegistrarFactoryBean", factoryBean);
    
        // has a default, won't be null
        boolean primary = (Boolean) attributes.get("primary");
        // 指定为首选 Bean
        beanDefinition.setPrimary(primary);
    
        String[] qualifiers = getQualifiers(attributes);
        if (ObjectUtils.isEmpty(qualifiers)) {
            qualifiers = new String[] { contextId + "FeignClient" };
        }
    
        // 将BeanDefinition包装成BeanDefinitionHolder，用于注册
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, qualifiers);
        // 注册 BeanDefinition
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        // 注册可选的Request.Options，可以动态刷新Request配置，但不太常用
        registerOptionsBeanDefinition(registry, contextId);
    }
}
```

**FeignClientFactoryBean的作用**，实现了FactoryBean接口，会调用getObject方法创建bean。

```java
public class FeignClientFactoryBean
		implements FactoryBean<Object>, InitializingBean, ApplicationContextAware, BeanFactoryAware {

	@Override
	public Object getObject() {
		return getTarget();
	}

    <T> T getTarget() {
        /*
            获取FeignContext，FeignContext继承了NamedContextFactory，它是用来统一维护feign中各个feign客户端相互隔离的上下文
            FeignContext注册到容器是在FeignAutoConfiguration上完成的
        */
        FeignContext context = beanFactory != null ? beanFactory.getBean(FeignContext.class)
                : applicationContext.getBean(FeignContext.class);

        // 构建feign.builder，在构建时会向FeignContext获取配置的Encoder，Decoder等各种信息
        // FeignContext会为每个Feign客户端分配一个容器，它们的父容器就是Spring容器。
        Feign.Builder builder = feign(context);

        // 如果url为空，则走负载均衡，生成有负载均衡功能的代理类
        if (!StringUtils.hasText(url)) {

            if (LOG.isInfoEnabled()) {
                LOG.info("For '" + name + "' URL not provided. Will try picking an instance via load-balancing.");
            }
            if (!name.startsWith("http")) {
                url = "http://" + name;
            }
            else {
                url = name;
            }
            url += cleanPath();
            // 创建 feign 客户端，@FeignClient 没有配置url属性，返回有负载均衡功能的代理对象
            return (T) loadBalance(builder, context, new HardCodedTarget<>(type, name, url));
        }
        // 如果指定了url，则生成默认的代理类
        if (StringUtils.hasText(url) && !url.startsWith("http")) {
            url = "http://" + url;
        }
        String url = this.url + cleanPath();
        Client client = getOptional(context, Client.class);
        if (client != null) {
            if (client instanceof FeignBlockingLoadBalancerClient) {
                // not load balancing because we have a url,
                // but Spring Cloud LoadBalancer is on the classpath, so unwrap
                client = ((FeignBlockingLoadBalancerClient) client).getDelegate();
            }
            if (client instanceof RetryableFeignBlockingLoadBalancerClient) {
                // not load balancing because we have a url,
                // but Spring Cloud LoadBalancer is on the classpath, so unwrap
                client = ((RetryableFeignBlockingLoadBalancerClient) client).getDelegate();
            }
            builder.client(client);
        }
        Targeter targeter = get(context, Targeter.class);
        return (T) targeter.target(this, builder, context, new HardCodedTarget<>(type, name, url));
    }

    protected <T> T loadBalance(Feign.Builder builder, FeignContext context, HardCodedTarget<T> target) {
        // 从上下文中获取 Client，默认是 FeignBlockingLoadBalancerClient
        // 它是在 FeignLoadBalancerAutoConfiguration 这个自动装配类中，通过Import实现的
        // Client是根据serviceId（服务名）进行隔离的
        Client client = getOptional(context, Client.class);
        if (client != null) {
            builder.client(client);
            Targeter targeter = get(context, Targeter.class);
            /*
                默认实现是 DefaultTargeter
                在OpenFeign低版本是HystrixTargeter，高版本移除了Hystrix，采用Spring Cloud Circuit Breaker 做限流熔断
            */
            return targeter.target(this, builder, context, target);
        }

        throw new IllegalStateException(
                "No Feign Client for loadBalancing defined. Did you forget to include spring-cloud-starter-loadbalancer?");
    }
}
```

**DefaultTargeter**

```java
class DefaultTargeter implements Targeter {

	@Override
	public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,
			Target.HardCodedTarget<T> target) {
		return feign.target(target);
	}
}
```

**Feign**

```java
public abstract class Feign {

    public static class Builder {

        public <T> T target(Target<T> target) {
          // 创建一个动态代理类，最终会调用 ReflectiveFeign.newInstance
          return build().newInstance(target);
        }

        public Feign build() {
          /*
            在FeignClientFactoryBean#configureFeign中会把容器中的Capability设置进来
            Capability对象可以增强Feign的各个组件，一般就是对原有的组件的包装
          */
          Client client = Capability.enrich(this.client, capabilities);
          Retryer retryer = Capability.enrich(this.retryer, capabilities);
          List<RequestInterceptor> requestInterceptors = this.requestInterceptors.stream()
              .map(ri -> Capability.enrich(ri, capabilities))
              .collect(Collectors.toList());
          Logger logger = Capability.enrich(this.logger, capabilities);
          Contract contract = Capability.enrich(this.contract, capabilities);
          Options options = Capability.enrich(this.options, capabilities);
          Encoder encoder = Capability.enrich(this.encoder, capabilities);
          Decoder decoder = Capability.enrich(this.decoder, capabilities);
          InvocationHandlerFactory invocationHandlerFactory =
              Capability.enrich(this.invocationHandlerFactory, capabilities);
          QueryMapEncoder queryMapEncoder = Capability.enrich(this.queryMapEncoder, capabilities);
    
          // 初始化SynchronousMethodHandler.Factory工厂，后续使用该工厂生成代理对象的方法
          SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
              new SynchronousMethodHandler.Factory(client, retryer, requestInterceptors, logger,
                  logLevel, decode404, closeAfterDecode, propagationPolicy, forceDecoding);

          // 用来创建方法名与对应MethodHandler的映射关系
          ParseHandlersByName handlersByName =
              new ParseHandlersByName(contract, options, encoder, decoder, queryMapEncoder,
                  errorDecoder, synchronousMethodHandlerFactory);
          // 创建一个动态代理类 ReflectiveFeign
          return new ReflectiveFeign(handlersByName, invocationHandlerFactory, queryMapEncoder);
        }
    }
}
```

**ReflectiveFeign.newInstance()**

```java
public class ReflectiveFeign extends Feign {

  @Override
  public <T> T newInstance(Target<T> target) {
    /*
        这里的 target 为 HardCodedTarget，包含了该feign客户端的接口类型，name，url等
        用 ParseHandlersByName 创建方法名和MethodHandler的映射map
    */
    Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
    // Method对象和MethodHandler的映射map
    Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<Method, MethodHandler>();
    List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();

    // 遍历接口中的所有方法
    for (Method method : target.type().getMethods()) {
      if (method.getDeclaringClass() == Object.class) {
        continue;

      // 判断是不是接口中的默认方法
      } else if (Util.isDefault(method)) {
        DefaultMethodHandler handler = new DefaultMethodHandler(method);
        defaultMethodHandlers.add(handler);
        methodToHandler.put(method, handler);
      } else {
        // 自定义的方法从 nameToHandler 里面获取对应的 SynchronousMethodHandler
        methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method)));
      }
    }
    // jdk动态代理 InvocationHandler，类型为 FeignCircuitBreakerInvocationHandler
    InvocationHandler handler = factory.create(target, methodToHandler);

    // 基于 JDK动态代理 为接口类创建动态实现，将所有的请求转换给 InvocationHandler 处理
    // 创建的是 ReflectiveFeign.FeignInvocationHandler 代理对象
    T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(),
        new Class<?>[] {target.type()}, handler);

    for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
      // 将 MethodHandle 绑定到代理对象
      defaultMethodHandler.bindTo(proxy);
    }
    return proxy;
  }
}
```

至此，FeignClient接口注入就分析完了，大致流程就是：

1. 包扫描，扫描带有`@FeignClient`注解标识的接口。

2. 根据接口信息封装为BeanDefinition。

3. 在使用时，调用`FactoryBean#getObject`返回代理对象并注入到IOC容器中。

### OpenFeign调用过程

前面创建的代理对象类型为 **ReflectiveFeign.FeignInvocationHandler**，所以当调用时，该代理对象的**invoke()**方法是调用入口。

```java
public class ReflectiveFeign extends Feign {

    static class FeignInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          if ("equals".equals(method.getName())) {
            try {
              Object otherHandler =
                  args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
              return equals(otherHandler);
            } catch (IllegalArgumentException e) {
              return false;
            }
          } else if ("hashCode".equals(method.getName())) {
            return hashCode();
          } else if ("toString".equals(method.getName())) {
            return toString();
          }
          // 这里的 dispatch 对象就是之前的 methodToHandler 方法与 SynchronousMethodHandler 的映射 map
          // 利用分发器找到处理目标方法的 handler，根据请求目标对应的url找到需要执行的方法进行调用
          // 在创建代理对象的时候，创建了一个动态代理的方法，其类型为 SynchronousMethodHandler
          return dispatch.get(method).invoke(args);
        }
    }
}
```

**SynchronousMethodHandler.invoke()**，该方法会根据参数生成完整的RequestTemplate对象，这个对象是HTTP请求的模板。

```java
final class SynchronousMethodHandler implements MethodHandler {

    @Override
    public Object invoke(Object[] argv) throws Throwable {
    // 获取 RequestTemplate 对象
    RequestTemplate template = buildTemplateFromArgs.create(argv);
    // 配置接口请求参数
    Options options = findOptions(argv);

    // 重试器
    Retryer retryer = this.retryer.clone();
    while (true) {
      try {
        // 请求的调用和处理响应
        return executeAndDecode(template, options);
      } catch (RetryableException e) {
        try {
          // 尝试重试和处理
          // 重试间隔按照1.5的倍数进行重试，如果超过重试设置的最大因子数则停止重试。
          retryer.continueOrPropagate(e);
        } catch (RetryableException th) {
          Throwable cause = th.getCause();
          if (propagationPolicy == UNWRAP && cause != null) {
            throw cause;
          } else {
            throw th;
          }
        }
        // 如果实现了日志类的打印，会打印日志信息
        if (logLevel != Logger.Level.NONE) {
          logger.logRetry(metadata.configKey(), logLevel);
        }
        continue;
      }
    }
    }
}
```

**executeAndDecode()**

```java
final class SynchronousMethodHandler implements MethodHandler {

    Object executeAndDecode(RequestTemplate template, Options options) throws Throwable {
        // 获取 Request 对象，可以实现RequestInterceptor接口，对Request进⾏增强
        Request request = targetRequest(template);
    
        if (logLevel != Logger.Level.NONE) {
          logger.logRequest(metadata.configKey(), logLevel, request);
        }
    
        Response response;
        long start = System.nanoTime();
        try {
          /*
            发起远程调用，这个 client 类型为是 FeignBlockingLoadBalancerClient
            FeignBlockingLoadBalancerClient 中的负载均衡客户端会改写url路径
          */
          response = client.execute(request, options);
          // ensure the request is set. TODO: remove in Feign 12
          response = response.toBuilder()
              .request(request)
              .requestTemplate(template)
              .build();
        } catch (IOException e) {
          if (logLevel != Logger.Level.NONE) {
            logger.logIOException(metadata.configKey(), logLevel, e, elapsedTime(start));
          }
          throw errorExecuting(request, e);
        }
        long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    
    
        if (decoder != null)
          return decoder.decode(response, metadata.returnType());
    
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        asyncResponseHandler.handleResponse(resultFuture, metadata.configKey(), response,
            metadata.returnType(),
            elapsedTime);
    
        try {
          if (!resultFuture.isDone())
            throw new IllegalStateException("Response handling not done");
    
          return resultFuture.join();
        } catch (CompletionException e) {
          Throwable cause = e.getCause();
          if (cause != null)
            throw cause;
          throw e;
        }
  }
}
```

**FeignBlockingLoadBalancerClient#execute()**

```java
public class FeignBlockingLoadBalancerClient implements Client {

    @Override
	public Response execute(Request request, Request.Options options) throws IOException {
        // 原始的url，一般为服务名 "http://orderService/user/1"
		final URI originalUri = URI.create(request.url());
        // 获取服务名 "orderService"
		String serviceId = originalUri.getHost();
		Assert.state(serviceId != null, "Request URI does not contain a valid hostname: " + originalUri);
		String hint = getHint(serviceId);
		DefaultRequest<RequestDataContext> lbRequest = new DefaultRequest<>(
				new RequestDataContext(buildRequestData(request), hint));
		Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
				.getSupportedLifecycleProcessors(
						loadBalancerClientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
						RequestDataContext.class, ResponseData.class, ServiceInstance.class);
		supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));

        // 使用负载均衡客户端根据服务名选择一个服务实例
		ServiceInstance instance = loadBalancerClient.choose(serviceId, lbRequest);
		org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse = new DefaultResponse(
				instance);
		if (instance == null) {
			String message = "Load balancer does not contain an instance for the service " + serviceId;
			if (LOG.isWarnEnabled()) {
				LOG.warn(message);
			}
			supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
					.onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
							CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
			return Response.builder().request(request).status(HttpStatus.SERVICE_UNAVAILABLE.value())
					.body(message, StandardCharsets.UTF_8).build();
		}
        // 改写url，负载均衡器通过注册中心拉取的服务地址改写url
		String reconstructedUrl = loadBalancerClient.reconstructURI(instance, originalUri).toString();
        // 使用改写过后的url构建一个新的request
		Request newRequest = buildRequest(request, reconstructedUrl);
		return executeWithLoadBalancerLifecycleProcessing(delegate, options, newRequest, lbRequest, lbResponse,
				supportedLifecycleProcessors);
	}
}
```

**LoadBalancerUtils#executeWithLoadBalancerLifecycleProcessing()**

```java
final class LoadBalancerUtils {

	static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
			Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
			org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
			Set<LoadBalancerLifecycle> supportedLifecycleProcessors) throws IOException {
		return executeWithLoadBalancerLifecycleProcessing(feignClient, options, feignRequest, lbRequest, lbResponse,
				supportedLifecycleProcessors, true);
	}

	static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
			Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
			org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
			Set<LoadBalancerLifecycle> supportedLifecycleProcessors, boolean loadBalanced) throws IOException {
		supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, lbResponse));
		try {
            // 使用feignClient执行请求，这里默认是Client.Default，可以换成 httpclient 或 okhttp 等
            // 如果是Client.Default，会使用java自带的HttpURLConnection发送请求
			Response response = feignClient.execute(feignRequest, options);
			if (loadBalanced) {
				supportedLifecycleProcessors.forEach(
						lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
								lbRequest, lbResponse, buildResponseData(response))));
			}
			return response;
		}
		catch (Exception exception) {
			if (loadBalanced) {
				supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onComplete(
						new CompletionContext<>(CompletionContext.Status.FAILED, exception, lbRequest, lbResponse)));
			}
			throw exception;
		}
	}
}
```

### 总结

OpenFeign的源码体量相对较小，但想要用一篇文章来深入也不太现实，都是挑执行链路上的核心点进行剖析。

OpenFeign的底层原理大致如下：

1. 通过`@EnableFeignClients`注解注入 OpenFeign 配置类。

2. FeignClientsRegistrar 扫描包下所有的 @FeignClient 接口类，并将其注册到 IOC 容器。

3. `@FeignClient` 接口类被注入时，通过`FactoryBean#getObject`返回动态代理类。

4. 接口被调用时被动态代理类逻辑拦截，将 `@FeignClient` 请求信息通过编码器生成 Request。

5. 由 Ribbon 进行负载均衡，挑选出一个健康的 Server 实例。

6. 通过 Client 携带 Request 调用远端服务，返回请求响应。

7. 通过解码器生成 Response 返回客户端，将信息流解析成为接口返回数据。

如果使用的注册中心是`Nacos`，则`Nacos`会适配 Ribbon，当 Ribbon 获取可用服务时，实际上调用的是`Nacos`的组件。