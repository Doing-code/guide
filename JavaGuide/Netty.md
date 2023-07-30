# Netty

## 源码

### 程序入口

如下所示是JavaNIO的步骤，那么Netty在底层是怎么封装这些步骤的呢？

```java
// 1、创建Selector
Selector selector = Selector.open();

// NioServerSocketChannel attachment = new NioServerSocketChannel();

// 2、创建ServerSocketChannel，处理连接请求
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
// 设置是否为非阻塞
serverSocketChannel.configureBlocking(false);

// SelectionKey selectionKey = serverSocketChannel.register(selector, 0, attachment);

// 3、将 serverSocketChannel 注册到 selector 中
SelectionKey selectionKey = serverSocketChannel.register(selector, 0);

// 4、该SelectionKey上注册的关注事件集合
selectionKey.interestOps(SelectionKey.OP_ACCEPT);

// 5、绑定端口号
serverSocketChannel.bind(new InetSocketAddress(8080));
```

Netty的启动程序如下所示：

```java
new ServerBootstrap()
    // NioEventLoopGroup 内部包含 nio 线程
    .group(new NioEventLoopGroup())
    .channel(NioServerSocketChannel.class)
    .childHandler(new ChannelInitializer<NioServerSocketChannel>() {
        @Override
        protected void initChannel(NioServerSocketChannel ch) throws Exception {
            ch.pipeline().addLast(new SimpleChannelInboundHandler() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    System.out.println("第一个Netty程序!");
                }
            });
        }
    }).bind(8080);
```

在Netty中，除了前面JavaNIO第一个的步骤是在NioEventLoopGroup完成的，剩余四个步骤都是在bind方法进行的。

### 启动流程

#### init

```java
public ChannelFuture bind(int inetPort) {
    return bind(new InetSocketAddress(inetPort));
}

public ChannelFuture bind(SocketAddress localAddress) {
    validate();
    return doBind(ObjectUtil.checkNotNull(localAddress, "localAddress"));
}

private ChannelFuture doBind(final SocketAddress localAddress) {

    // 1、initAndRegister 完成初始化和注册
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    if (regFuture.isDone()) {
        // At this point we know that the registration was complete and successful.
        ChannelPromise promise = channel.newPromise();

        // 2、在初始化和注册完成后，真正执行原生 ServerSocketChannel bind
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else {
        // Registration future is almost always fulfilled already, but just in case it's not.
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);

        /* 
            添加监听器（回调）
            当initAndRegister()执行完成后，由Nio Thread回调执行
        */
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    // Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
                    // IllegalStateException once we try to access the EventLoop of the Channel.
                    promise.setFailure(cause);
                } else {
                    // Registration was successful, so set the correct executor to use.
                    // See https://github.com/netty/netty/issues/2586
                    promise.registered();

                    // 2、在初始化和注册完成后，真正执行原生 ServerSocketChannel bind
                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```

initAndRegister()从方法名就能看出来，其对应的是两个步骤：

1. `ServerSocketChannel.open();`

2. `serverSocketChannel.register(selector, 0);`

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        /*
        1、ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
            创建 NioServerSocketChannel 实例，反射调用无参构造
            在其构造中执行 ServerSocketChannel.open()
        */
        channel = channelFactory.newChannel();
        // 初始化 NioServerSocketChannel
        init(channel);
    } catch (Throwable t) {
        if (channel != null) {
            // channel can be null if newChannel crashed (eg SocketException("too many open files"))
            channel.unsafe().closeForcibly();
            // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
            return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
        }
        // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
        return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
    }

    /*
    2、serverSocketChannel.register(selector, 0, attr)
        将 ServerSocketChannel 注册到 Selector
    */
    ChannelFuture regFuture = config().group().register(channel);
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }

    return regFuture;
}
```

而doBind0()方法自然就是绑定端口逻辑了：

- `serverSocketChannel.bind(new InetSocketAddress(8080));`

#### register

```java
// 入口：ChannelFuture regFuture = config().group().register(channel);

// 调用：io.netty.channel.MultithreadEventLoopGroup#register
public ChannelFuture register(Channel channel) {
    return next().register(channel);
}

// 调用：io.netty.channel.SingleThreadEventLoop#register
public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, this));
}
public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    promise.channel().unsafe().register(this, promise);
    return promise;
}

// 调用：io.netty.channel.AbstractChannel.AbstractUnsafe#register
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    ObjectUtil.checkNotNull(eventLoop, "eventLoop");
    if (isRegistered()) {
        promise.setFailure(new IllegalStateException("registered to an event loop already"));
        return;
    }
    if (!isCompatible(eventLoop)) {
        promise.setFailure(
                new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
        return;
    }

    AbstractChannel.this.eventLoop = eventLoop;

    // 判断当前线程是不是 nio thread，启动时很显然不是，则执行 else 逻辑
    if (eventLoop.inEventLoop()) {
        register0(promise);
    } else {
        try {
            /*
                将真正注册逻辑交给 eventLoop 执行 （nio thread 执行）
                懒加载，第一次调用 execute() 才会将 eventLoop 关联的 nio thread 创建
                （启动 nio boss 线程）
            */
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    // 原生 ServerSocketChannel 注册到 Selector，但没有关注事件
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            logger.warn(
                    "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                    AbstractChannel.this, t);
            closeForcibly();
            closeFuture.setClosed();
            safeSetFailure(promise, t);
        }
    }
}

// 调用：io.netty.channel.AbstractChannel.AbstractUnsafe#register0
private void register0(ChannelPromise promise) {
    try {
        // check if the channel is still open as it could be closed in the mean time when the register
        // call was outside of the eventLoop
        if (!promise.setUncancellable() || !ensureOpen(promise)) {
            return;
        }
        boolean firstRegistration = neverRegistered;

        /*
        模板方法，由子类实现，(AbstractNioChannel)
            一般do开头的方法就是干活的方法
            原生 ServerSocketChannel 注册到 Selector，但没有关注事件
            底层等同于serverSocketChannel.register(selector, 0)
        */
        doRegister();
        neverRegistered = false;
        registered = true;

        // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
        // user may already fire events through the pipeline in the ChannelFutureListener.

        /*
        具体执行到附录中init(channel)中的回调方法
            执行 NioServerSockerChannel 初始化 handler （ChannelInitializer）
        */
        pipeline.invokeHandlerAddedIfNeeded();

        safeSetSuccess(promise);
        pipeline.fireChannelRegistered();
        // Only fire a channelActive if the channel has never been registered. This prevents firing
        // multiple channel actives if the channel is deregistered and re-registered.
        if (isActive()) {
            if (firstRegistration) {
                pipeline.fireChannelActive();
            } else if (config().isAutoRead()) {
                // This channel was registered before and autoRead() is set. This means we need to begin read
                // again so that we process inbound data.
                //
                // See https://github.com/netty/netty/issues/4805
                beginRead();
            }
        }
    } catch (Throwable t) {
        // Close the channel directly to avoid FD leak.
        closeForcibly();
        closeFuture.setClosed();
        safeSetFailure(promise, t);
    }
}
```

在AbstractChannel的子类`io.netty.channel.nio.AbstractNioChannel#doRegister`中执行了Java原生nio的注册逻辑：

```java
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            /*
            调用 Java nio 的 serverSocketChannel.register(selector, 0, attr);
                1、Selector被eventLoop管理，从eventLoop().unwrappedSelector()获取Selector
                2、未关注事件
                3、Netty传入的附件为 this，类型为 NioServerSocketChannel，将来selector发生事件由 NioServerSocketChannel 处理
            */
            selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
            return;
        } catch (CancelledKeyException e) {
            ......
        }
    }
}

protected SelectableChannel javaChannel() {
    return ch;
}
```

回顾一下Java nio 的注册逻辑：

```java
/*
    public abstract class ServerSocketChannel extends AbstractSelectableChannel implements NetworkChannel
    public abstract class AbstractSelectableChannel extends SelectableChannel
*/
// 入口：SelectionKey selectionKey = serverSocketChannel.register(selector, 0);

// 调用：java.nio.channels.SelectableChannel#register
public final SelectionKey register(Selector sel, int ops)
    throws ClosedChannelException
{ 
    return register(sel, ops, null);
}

// 调用：java.nio.channels.spi.AbstractSelectableChannel#register
public final SelectionKey register(Selector sel, int ops,
                                   Object att)
    throws ClosedChannelException
{
    synchronized (regLock) {
        if (!isOpen())
            throw new ClosedChannelException();
        if ((ops & ~validOps()) != 0)
            throw new IllegalArgumentException();
        if (blocking)
            throw new IllegalBlockingModeException();
        SelectionKey k = findKey(sel);
        if (k != null) {
            k.interestOps(ops);
            k.attach(att);
        }
        if (k == null) {
            // New registration
            synchronized (keyLock) {
                if (!isOpen())
                    throw new ClosedChannelException();
                k = ((AbstractSelector)sel).register(this, ops, att);
                addKey(k);
            }
        }
        return k;
    }
}
```

此时Java Nio 和 Netty 的注册ServerSocketChannel到Selector就对上了。

#### doBind0

当initAndRegister()执行完后，会触发regFuture的回调执行doBind0()，在前面逻辑中`regFuture.addListener(new ChannelFutureListener() {...doBind0()...})`添加的回调函数，此时会得到执行。并执行原生 ServerSocketChannel 绑定端口。

```java
// 调用：io.netty.bootstrap.AbstractBootstrap#doBind0
private static void doBind0(
        final ChannelFuture regFuture, final Channel channel,
        final SocketAddress localAddress, final ChannelPromise promise) {

    // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
    // the pipeline in its channelRegistered() implementation.

    // 还是由 eventLoop nio 线程执行
    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            if (regFuture.isSuccess()) {
                // 
                channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                promise.setFailure(regFuture.cause());
            }
        }
    });
}

// 调用：io.netty.channel.AbstractChannel#bind
public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return pipeline.bind(localAddress, promise);
}

// 调用：io.netty.channel.DefaultChannelPipeline#bind
public final ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return tail.bind(localAddress, promise);
}

// 调用：io.netty.channel.AbstractChannelHandlerContext#bind
public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
    ObjectUtil.checkNotNull(localAddress, "localAddress");
    if (isNotValidPromise(promise, false)) {
        // cancelled
        return promise;
    }

    final AbstractChannelHandlerContext next = findContextOutbound(MASK_BIND);
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        // 
        next.invokeBind(localAddress, promise);
    } else {
        safeExecute(executor, new Runnable() {
            @Override
            public void run() {
                next.invokeBind(localAddress, promise);
            }
        }, promise, null, false);
    }
    return promise;
}
private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
    if (invokeHandler()) {
        try {
            // 反射调用bind()，其调用的是 DefaultChannelPipeline.HeadContext#bind
            ((ChannelOutboundHandler) handler()).bind(this, localAddress, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    } else {
        bind(localAddress, promise);
    }
}

// 调用：io.netty.channel.DefaultChannelPipeline.HeadContext#bind
public void bind(
        ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
    unsafe.bind(localAddress, promise);
}

// 调用：io.netty.channel.AbstractChannel.AbstractUnsafe#bind
public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
    assertEventLoop();

    if (!promise.setUncancellable() || !ensureOpen(promise)) {
        return;
    }

    // See: https://github.com/netty/netty/issues/576
    if (Boolean.TRUE.equals(config().getOption(ChannelOption.SO_BROADCAST)) &&
        localAddress instanceof InetSocketAddress &&
        !((InetSocketAddress) localAddress).getAddress().isAnyLocalAddress() &&
        !PlatformDependent.isWindows() && !PlatformDependent.maybeSuperUser()) {
        // Warn a user about the fact that a non-root user can't receive a
        // broadcast packet on *nix if the socket is bound on non-wildcard address.
        logger.warn(
                "A non-root user can't receive a broadcast packet if the socket " +
                "is not bound to a wildcard address; binding to a non-wildcard " +
                "address (" + localAddress + ") anyway as requested.");
    }

    boolean wasActive = isActive();
    try {
        // 原生 ServerSocketChannel 绑定端口
        doBind(localAddress);
    } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
    }

    // 判断ServerSocketChannel是否可用，可用则处于active状态
    if (!wasActive && isActive()) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                pipeline.fireChannelActive();
            }
        });
    }

    safeSetSuccess(promise);
}
```

最终经过漫长的方法调用，又回到了NioServerSocketChannel调用bind()，其内部就调用的就是Java nio 的bind逻辑。

```java
// 调用：io.netty.channel.socket.nio.NioServerSocketChannel#doBind
protected void doBind(SocketAddress localAddress) throws Exception {
    // 当前使用的JDK是1.8
    if (PlatformDependent.javaVersion() >= 7) {
        // serverSocketChannel.bind(new InetSocketAddress(8080));
        javaChannel().bind(localAddress, config.getBacklog());
    } else {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }
}
// javaChannel()返回的是sun.nio.ch.ServerSocketChannelImpl。
```

此时Java Nio 和 Netty 的bind()就对上了。

#### 关注accept事件

```java
/*
判断ServerSocketChannel是否可用，可用则处于active状态

channel并不具备业务功能，真正干活的是pipeline中 handler，即channel通知handler开始干活
    此时pipeline中有3个handler：head -> ServerBootstrapAcceptor -> tail
    head、tail是特殊的handler，每个Channel pipeline 都会自带，具体看附录中NioServerSocketChannel构造
    而ServerBootstrapAcceptor由Neety在回调方法中添加的
*/
if (!wasActive && isActive()) {
    invokeLater(new Runnable() {
        @Override
        public void run() {
            // 执行pipeline中所有handler的 channelActive() 方法
            // 主要逻辑集中在 head（HeadContext）
            // 触发 NioServerSocketChannel channelActive 事件
            pipeline.fireChannelActive();
        }
    });
}
```

上述逻辑作用是在ServerSocketChannel可用之后执行`selectionKey.interestOps(SelectionKey.OP_ACCEPT)`，在pipeline handler中得到执行。

```java
// 调用：io.netty.channel.DefaultChannelPipeline#fireChannelActive
public final ChannelPipeline fireChannelActive() {
    AbstractChannelHandlerContext.invokeChannelActive(head);
    return this;
}

// 调用：io.netty.channel.AbstractChannelHandlerContext#invokeChannelActive
static void invokeChannelActive(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    // 是 nio 线程，会进入 if 逻辑
    if (executor.inEventLoop()) {
        next.invokeChannelActive();
    } else {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                next.invokeChannelActive();
            }
        });
    }
}
private void invokeChannelActive() {
    if (invokeHandler()) {
        try {
            // 调用实现类的 channelActive() 方法
            ((ChannelInboundHandler) handler()).channelActive(this);
        } catch (Throwable t) {
            invokeExceptionCaught(t);
        }
    } else {
        fireChannelActive();
    }
}
```

默认先调用pipeline调用链的head节点，即`DefaultChannelPipeline.HeadContext`：

```java
public class DefaultChannelPipeline implements ChannelPipeline {
    final class HeadContext extends AbstractChannelHandlerContext
                implements ChannelOutboundHandler, ChannelInboundHandler {
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.fireChannelActive();

            // selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            readIfIsAutoRead();
        }

        private void readIfIsAutoRead() {
            if (channel.config().isAutoRead()) {
                channel.read();
            }
        }
    }
}

// 调用：io.netty.channel.AbstractChannel#read
public Channel read() {
    pipeline.read();
    return this;
}

// 调用：io.netty.channel.DefaultChannelPipeline#read
public final ChannelPipeline read() {
    tail.read();
    return this;
}

// 调用：io.netty.channel.AbstractChannelHandlerContext#read
public ChannelHandlerContext read() {
    final AbstractChannelHandlerContext next = findContextOutbound(MASK_READ);
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
        next.invokeRead();
    } else {
        Tasks tasks = next.invokeTasks;
        if (tasks == null) {
            next.invokeTasks = tasks = new Tasks(next);
        }
        executor.execute(tasks.invokeReadTask);
    }

    return this;
}
private void invokeRead() {
    if (invokeHandler()) {
        try {
            ((ChannelOutboundHandler) handler()).read(this);
        } catch (Throwable t) {
            invokeExceptionCaught(t);
        }
    } else {
        read();
    }
}

// 调用：io.netty.channel.DefaultChannelPipeline.HeadContext#read
public void read(ChannelHandlerContext ctx) {
    unsafe.beginRead();
}

// 调用：io.netty.channel.AbstractChannel.AbstractUnsafe#beginRead
public final void beginRead() {
    assertEventLoop();

    try {
        doBeginRead();
    } catch (final Exception e) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                pipeline.fireExceptionCaught(e);
            }
        });
        close(voidPromise());
    }
}

// 调用：io.netty.channel.nio.AbstractNioMessageChannel#doBeginRead
protected void doBeginRead() throws Exception {
    if (inputShutdown) {
        return;
    }
    super.doBeginRead();
}
```

最终在AbstractNioChannel类中执行doBeginRead()，绑定`SelectionKey.OP_ACCEPT`事件到selectionKey：

```java
// 调用：io.netty.channel.nio.AbstractNioChannel#doBeginRead
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }

    readPending = true;

    /*
        如果没有关注 SelectionKey.OP_ACCEPT，则关注 SelectionKey.OP_ACCEPT 事件
        readInterestOp 是在 NioServerSocketChannel 构造方法中得到赋值的
    */
    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        // selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        selectionKey.interestOps(interestOps | readInterestOp);
    }
}
```

此时Java Nio 和 Netty 的绑定accept事件就对上了。

到现在为止，Java nio 的五个步骤都在Netty中对应上了。至此服务器端启动好了。

### EventLoop

- NioEventLoop的重要组成：Selector、线程、任务队列。

- NioEventLoop 既处理io事件，也会处理普通任务和定时任务。

#### Selector何时创建

#### nio thread何时启动

#### 提交普通任务会不会结束select阻塞

#### 什么时候会进入SelectStrategy.SELECT分支

#### nio空轮询bug在哪里体现，如何解决？

#### ioRatio控制什么？设置为100有何作用？

#### selectedKyes优化

#### 在何处区分不同事件类型

### accept流程

### read流程

## 附录

### channelFactory.newChannel()

这个`constructor`就是引导类启动时调用`bootstrap.channel(NioServerSocketChannel.class)`传递来的。初始化 NioServerSocketChannel。

```java
public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {

    public ReflectiveChannelFactory(Class<? extends T> clazz) {
        ObjectUtil.checkNotNull(clazz, "clazz");
        try {
            this.constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) +
                    " does not have a public non-arg constructor", e);
        }
    }

    public T newChannel() {
        try {
            // 反射调用无参构造
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new ChannelException("Unable to create Channel from class " + constructor.getDeclaringClass(), t);
        }
    }
}
```

#### NioServerSocketChannel构造

```java
public class NioServerSocketChannel extends AbstractNioMessageChannel implements io.netty.channel.socket.ServerSocketChannel {

    private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();

    private static final Method OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY = SelectorProviderUtil.findOpenMethod("openServerSocketChannel");

    public NioServerSocketChannel() {
        this(DEFAULT_SELECTOR_PROVIDER);
    }
    public NioServerSocketChannel(SelectorProvider provider) {
        this(provider, null);
    }
    public NioServerSocketChannel(SelectorProvider provider, InternetProtocolFamily family) {
        this(newChannel(provider, family));
    }

    private static ServerSocketChannel newChannel(SelectorProvider provider, InternetProtocolFamily family) {
        try {
            ServerSocketChannel channel = SelectorProviderUtil.newChannel(OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY, provider, family);

            // ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            return channel == null ? provider.openServerSocketChannel() : channel;
        } catch (IOException e) {
            throw new ChannelException("Failed to open a socket.", e);
        }
    }
}
```

来回顾一下Java Nio 的`ServerSocketChannel.open()`：

```java
// ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

public static ServerSocketChannel open() throws IOException {
    return SelectorProvider.provider().openServerSocketChannel();
}

// sun.nio.ch.SelectorProviderImpl#openServerSocketChannel
public ServerSocketChannel openServerSocketChannel() throws IOException {
    return new ServerSocketChannelImpl(this);
}
```

此时Java Nio 和 Netty 的创建ServerSocketChannel就对上了。

而在构造NioServerSocketChannel时，在其父类构造中初始化pipeline（类型是DefaultChannelPipeline），增加了首尾结点。

```java
// ......

public NioServerSocketChannel(SelectorProvider provider, InternetProtocolFamily family) {
    this(newChannel(provider, family));
}
public NioServerSocketChannel(ServerSocketChannel channel) {
    // 调用父类构造，并配置监听 SelectionKey.OP_ACCEPT，即监听客户端连接请求
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}

protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    // 调用父类构造
    super(parent, ch, readInterestOp);
}

protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    // 调用父类构造
    super(parent);

    this.ch = ch;
    this.readInterestOp = readInterestOp;
    try {
        // 设置非堵塞
        ch.configureBlocking(false);
    } catch (IOException e) {
        try {
            ch.close();
        } catch (IOException e2) {
            logger.warn(
                        "Failed to close a partially initialized socket.", e2);
        }

        throw new ChannelException("Failed to enter non-blocking mode.", e);
    }
}

protected AbstractChannel(Channel parent) {
    this.parent = parent;
    id = newId();
    unsafe = newUnsafe();
    pipeline = newChannelPipeline();
}
protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
}

// 在构造NioServerSocketChannel时，初始化pipeline，类型为DefaultChannelPipeline，增加了首尾结点。
protected DefaultChannelPipeline(Channel channel) {
    this.channel = ObjectUtil.checkNotNull(channel, "channel");
    succeededFuture = new SucceededChannelFuture(channel, null);
    voidPromise =  new VoidChannelPromise(channel, true);

    tail = new TailContext(this);
    head = new HeadContext(this);

    head.next = tail;
    tail.prev = head;
}
```

### init(channel)

```java
public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {

    void init(Channel channel) {
        setChannelOptions(channel, newOptionsArray(), logger);
        setAttributes(channel, newAttributesArray());

        // NioServerSocketChannel 本质也是一个 Channel，也有自己的pipeline（流水线）
        ChannelPipeline p = channel.pipeline();

        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Entry<ChannelOption<?>, Object>[] currentChildOptions = newOptionsArray(childOptions);
        final Entry<AttributeKey<?>, Object>[] currentChildAttrs = newAttributesArray(childAttrs);

        /*
        ChannelInitializer 只会被执行一次
            添加 NioServerSocketChannel 初始化 handler （回调）
            等到 ssc.register() 注册完成后，该回调得到执行
        */
        p.addLast(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(final Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                ChannelHandler handler = config.handler();
                if (handler != null) {
                    pipeline.addLast(handler);
                }

                ch.eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.addLast(new ServerBootstrapAcceptor(
                                ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                    }
                });
            }
        });
    }
}
```

该方法在NioServerSocketChannel的pipeline中添加了一个初始化handler。而初始化handler内部又向NioServerSocketChannel的pipeline中添加了一个Acceptor handler，其作用是在 Acceptor 事件发生后建立连接。