# SpringMVC

> 解析 @RequestMapping https://blog.csdn.net/qq_33012981/article/details/115081609

```txt
工作流程是：请求通过前端控制器将请求URL交给处理器映射器处理获取Handler，并将获取的handler交给处理器适配器执行，执行后返回ModelAndView对象，根据对象值进行页面渲染。

具体做了什么？
1、处理请求（请求参数）
2、处理器映射器、处理器适配器的工作就没有那么明显了
3、视图解析器

```
## @RequestMapping 原理
1. `org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#afterPropertiesSet`
```java
public void afterPropertiesSet() {
    
    // 创建请求映射的配置容器
    this.config = new RequestMappingInfo.BuilderConfiguration();
    
    // 将处理器映射器持有的 UrlPathHelper 对象赋值给配置容器
    this.config.setUrlPathHelper(getUrlPathHelper());
    
    // 将处理器映射器持有的PathMatcher对象赋值给配置容器
    this.config.setPathMatcher(getPathMatcher());
    
    // 是否使用注册的后缀路径进行匹配，默认为true，已过时，直接忽略
    this.config.setSuffixPatternMatch(this.useSuffixPatternMatch);
    
    // 是否与URL匹配，无论是否存在斜杠，默认为true
    this.config.setTrailingSlashMatch(this.useTrailingSlashMatch);
    
    // 是否使用注册的后缀路径进行匹配，默认为true，已过时，直接忽略
    this.config.setRegisteredSuffixPatternMatch(this.useRegisteredSuffixPatternMatch);
    
    // 将处理器映射器持有的内容协商管理器对象赋值给配置容器
    this.config.setContentNegotiationManager(getContentNegotiationManager());

    // 调用父类的 afterPropertiesSet。（AbstractHandlerMethodMapping（爷爷类））
    super.afterPropertiesSet();
}
```
保存所有请求映射信息对象的公有配置。

2. `org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#afterPropertiesSet`
```java
public void afterPropertiesSet() {
    initHandlerMethods();
}

protected void initHandlerMethods() {
    if (logger.isDebugEnabled()) {
        logger.debug("Looking for request mappings in application context: " + getApplicationContext());
    }
    
    // 获取ioc容器中所有的 beanName
    String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
            obtainApplicationContext().getBeanNamesForType(Object.class));

    // 遍历 beanName
    for (String beanName : beanNames) {
    
        // 处理Bean名称不是`scopedTarget.`开头
        if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
            Class<?> beanType = null;
            try {
                beanType = obtainApplicationContext().getType(beanName);
            }
            catch (Throwable ex) {
                // An unresolvable bean type, probably from a lazy bean - let's ignore it.
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
                }
            }
            
            // isHandler 这个方法是判断是否被 @Controller 或 @RequestMapping 标记
            if (beanType != null && isHandler(beanType)) {
                
                // 处理被 @Controller 或 @RequestMapping 标记的 bean
                detectHandlerMethods(beanName);
            }
        }
    }
    handlerMethodsInitialized(getHandlerMethods());
}
```
3. `org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#detectHandlerMethods`
```java
protected void detectHandlerMethods(final Object handler) {

    // 获取处理器类的 Class 对象
    Class<?> handlerType = (handler instanceof String ?
            obtainApplicationContext().getType((String) handler) : handler.getClass());

    if (handlerType != null) {
    
        // 获取目标类（可能存在代理类）
        final Class<?> userType = ClassUtils.getUserClass(handlerType);
        
        /*
            反射遍历userType类及其父接口的所有的方法，得到符合条件的方法信息
            此处会收集到所有 标注了@RequestMapping注解的方法 -> 对应的请求映射信息对象
        */
        Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<T>) method -> {
                    try {
                    
                        // 解析方法上 @RequestMapping 注解，并生成对应的请求映射信息对象
                        return getMappingForMethod(method, userType);
                    }
                    catch (Throwable ex) {
                        throw new IllegalStateException("Invalid mapping on handler class [" +
                                userType.getName() + "]: " + method, ex);
                    }
                });
        if (logger.isDebugEnabled()) {
            logger.debug(methods.size() + " request handler methods found on " + userType + ": " + methods);
        }
        methods.forEach((method, mapping) -> {
            
            /**
             * 对方法进行验证
             * 如果方法是私有的、且非静态的、且方法所在类实现了SpringProxy接口的
             * 就抛出异常，表明该方法不符合条件
             */
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            
            // 将处理器方法和RequestMappingInfo之间的唯一映射注册到MappingRegistry对象中
            registerHandlerMethod(handler, invocableMethod, mapping);
        });
    }
}
```
