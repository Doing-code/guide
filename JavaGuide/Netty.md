# Netty

## nio

## 入门

## 进阶

## 优化

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
    2、serverSocketChannel.register(selector, 0, att)
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

        // 回调 channelRegistered()
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
            调用 Java nio 的 serverSocketChannel.register(selector, 0, att);
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
            // 其调用的是实现类 DefaultChannelPipeline.HeadContext#bind
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

其重要的成员变量：

```java
// 成员变量
public final class NioEventLoop extends SingleThreadEventLoop {

    private Selector selector;
    private Selector unwrappedSelector;

    /*
        任务队列，缓存任务，由 thread 依次取出执行
        普通任务，非IO事件
    */
    private final Queue<Runnable> taskQueue;

    /*
        在父类SingleThreadEventLoop的父类SingleThreadEventExecutor中
        处理IO事件的线程（单线程）
    */
    private volatile Thread thread;

    /*
        在父类SingleThreadEventLoop的父类SingleThreadEventExecutor中
        执行器，和 thread 用的是同一个线程
    */
    private final Executor executor;

    /*
        在父类SingleThreadEventLoop的父类SingleThreadEventExecutor的父类AbstractScheduledEventExecutor中
        处理定时任务的队列
    */
    PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue;
}
```

#### EventLoop何时创建

启动类程序入口：

```java
NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
NioEventLoopGroup workerGroup = new NioEventLoopGroup();

new ServerBootstrap().group(bossGroup, workerGroup);
```

bossGroup就是parentGroup，是负责处理TCP/IP连接的，而workerGroup就是childGroup，是负责处理Channel（通道）的I/O事件。

```java
// 调用NioEventLoopGroup构造重载：io.netty.channel.nio.NioEventLoopGroup#NioEventLoopGroup
public NioEventLoopGroup(int nThreads) {
    this(nThreads, (Executor) null);
}
public NioEventLoopGroup(int nThreads) {
    this(nThreads, (Executor) null);
}
public NioEventLoopGroup(int nThreads, Executor executor) {
    this(nThreads, executor, SelectorProvider.provider());
}
public NioEventLoopGroup(int nThreads, Executor executor, final SelectorProvider selectorProvider) {
    this(nThreads, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
}
public NioEventLoopGroup(int nThreads, Executor executor, final SelectorProvider selectorProvider,
                         final SelectStrategyFactory selectStrategyFactory) {
    super(nThreads, executor, selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject());
}

// 调用父类构造：io.netty.channel.MultithreadEventLoopGroup#MultithreadEventLoopGroup
protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
    // nThreads==0即无参构造，默认会被设置为 CPU 核心数*2
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
}

// 调用父类构造：io.netty.util.concurrent.MultithreadEventExecutorGroup#MultithreadEventExecutorGroup
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
    this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
}
```

经过漫长的构造函数重载调用，最终调用到MultithreadEventExecutorGroup的构造函数，真正干活的构造：

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
    checkPositive(nThreads, "nThreads");

    /*
    executor 如果是 null 则初始化一个，（线程池）
        ThreadPerTaskExecutor 的逻辑就是每来一个任务，新建一个线程
    */
    if (executor == null) {
        executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
    }

    children = new EventExecutor[nThreads];

    for (int i = 0; i < nThreads; i ++) {
        boolean success = false;
        try {
            // 实例化 NioEventLoop 并放入数组容器中
            children[i] = newChild(executor, args);
            success = true;
        } catch (Exception e) {
            // TODO: Think about if this is a good exception type
            throw new IllegalStateException("failed to create a child event loop", e);
        } finally {
            // 如果有一个 child 实例化失败，则把已经成功实例化的“线程” shutdown，shutdown 是异步操作
            if (!success) {
                for (int j = 0; j < i; j ++) {
                    children[j].shutdownGracefully();
                }

                for (int j = 0; j < i; j ++) {
                    EventExecutor e = children[j];
                    try {
                        while (!e.isTerminated()) {
                            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                        }
                    } catch (InterruptedException interrupted) {
                        // Let the caller handle the interruption.
                        // 设置中断状态，交给关心的线程来处理.
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    // 通过chooserFactory工厂来实例化 Chooser，把线程池数组传进去
    chooser = chooserFactory.newChooser(children);

    // 注册一个 Listener， 用来监听该线程池的 termination 事件
    final FutureListener<Object> terminationListener = new FutureListener<Object>() {
        @Override
        public void operationComplete(Future<Object> future) throws Exception {
            if (terminatedChildren.incrementAndGet() == children.length) {
                terminationFuture.setSuccess(null);
            }
        }
    };

    // 给池中每一个线程都设置 listener，当监听到所有线程都 terminate 以后，这个线程池就算真正的 terminate 了。
    for (EventExecutor e: children) {
        e.terminationFuture().addListener(terminationListener);
    }

    // 设置 readonlyChildren，它是只读集合
    Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
    Collections.addAll(childrenSet, children);
    readonlyChildren = Collections.unmodifiableSet(childrenSet);
}
```

上面的代码比较简单，没有什么需要特别说的，接下来，我们来看看 newChild() 这个方法，这个方法非常重要，它将创建线程池中的线程。事实上这里的线程，并不是真的Thread，而是指池中的个体，即NioEventLoop。而NioEventLoop内部都会有一个自己的Thread实例。

```java
// 调用：io.netty.channel.nio.NioEventLoopGroup#newChild
protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    SelectorProvider selectorProvider = (SelectorProvider) args[0];
    SelectStrategyFactory selectStrategyFactory = (SelectStrategyFactory) args[1];
    RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) args[2];
    EventLoopTaskQueueFactory taskQueueFactory = null;
    EventLoopTaskQueueFactory tailTaskQueueFactory = null;

    int argsLength = args.length;
    if (argsLength > 3) {
        taskQueueFactory = (EventLoopTaskQueueFactory) args[3];
    }
    if (argsLength > 4) {
        tailTaskQueueFactory = (EventLoopTaskQueueFactory) args[4];
    }
    return new NioEventLoop(this, executor, selectorProvider,
            selectStrategyFactory.newSelectStrategy(),
            rejectedExecutionHandler, taskQueueFactory, tailTaskQueueFactory);
}
```

真正创建NioEventLoop的地方：

```java
// io.netty.channel.nio.NioEventLoop#NioEventLoop
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
             SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
             EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
    super(parent, executor, false, newTaskQueue(taskQueueFactory), newTaskQueue(tailTaskQueueFactory),
            rejectedExecutionHandler);
    this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
    this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");

    // 获取NIO组件 Selector
    final SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
}
```

此时可以得出：

- 在 Netty 中，NioEventLoopGroup 代表线程池，NioEventLoop 就是其中的线程。

- 线程池 NioEventLoopGroup 是池中的线程 NioEventLoop 的 parent，从上面的代码中的取名可以看出。

- 每个 NioEventLoop 都有自己的 Selector，上面的代码也反映了这一点，这和 Tomcat 中的 NIO 模型有点区别。

- executor、selectStrategy 和 rejectedExecutionHandler 从 NioEventLoopGroup 中一路传递到了 NioEventLoop 中。

- NioEventLoop 是在NioEventLoopGroup实例化时被创建。

#### Selector何时创建

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
                 SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
                 EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
    super(parent, executor, false, newTaskQueue(taskQueueFactory), newTaskQueue(tailTaskQueueFactory),
            rejectedExecutionHandler);
    this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
    this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");

    final SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
}

private SelectorTuple openSelector() {
    final Selector unwrappedSelector;
    try {
        // 创建 Java nio 原生Selector
        unwrappedSelector = provider.openSelector();
    } catch (IOException e) {
        throw new ChannelException("failed to open a new selector", e);
    }
    ......
    return new SelectorTuple(unwrappedSelector, new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
}
```

Selector会在NioEventLoop构造方法调用时被创建，而NioEventLoop会在NioEventLoopGroup构造方法调用时被创建。

##### EventLoop为何有两个selector成员变量?

```java
// NioEventLoop.class
private Selector selector;
private Selector unwrappedSelector;
```

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
             SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
             EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
    super(parent, executor, false, newTaskQueue(taskQueueFactory), newTaskQueue(tailTaskQueueFactory),
            rejectedExecutionHandler);
    this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
    this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");
    final SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
}
```

```java
private SelectorTuple openSelector() {
    final Selector unwrappedSelector;
    try {
        // 创建一个原生的Selector
        unwrappedSelector = provider.openSelector();
    } catch (IOException e) {
        throw new ChannelException("failed to open a new selector", e);
    }

    // 是否禁止优化，默认false
    if (DISABLE_KEY_SET_OPTIMIZATION) {
        return new SelectorTuple(unwrappedSelector);
    }

    // 尝试获取sun.nio.ch.SelectorImpl的class对象，Netty重写了一个Selector
    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {
                return Class.forName(
                        "sun.nio.ch.SelectorImpl",
                        false,
                        PlatformDependent.getSystemClassLoader());
            } catch (Throwable cause) {
                return cause;
            }
        }
    });

    /*
        如果返回maybeSelectorImplClass不是一个class对象，
        或者maybeSelectorImplClass不是unwrappedSelector的子类
    */
    if (!(maybeSelectorImplClass instanceof Class) ||
        // ensure the current selector implementation is what we can instrument.
        !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
        if (maybeSelectorImplClass instanceof Throwable) {
            Throwable t = (Throwable) maybeSelectorImplClass;
            logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
        }
        return new SelectorTuple(unwrappedSelector);
    }

    final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;

    // 内部元素是数组结构实现的set接口
    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

    Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {
                Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

                if (PlatformDependent.javaVersion() >= 9 && PlatformDependent.hasUnsafe()) {
                    // Let us try to use sun.misc.Unsafe to replace the SelectionKeySet.
                    // This allows us to also do this in Java9+ without any extra flags.
                    long selectedKeysFieldOffset = PlatformDependent.objectFieldOffset(selectedKeysField);
                    long publicSelectedKeysFieldOffset =
                            PlatformDependent.objectFieldOffset(publicSelectedKeysField);

                    if (selectedKeysFieldOffset != -1 && publicSelectedKeysFieldOffset != -1) {
                        PlatformDependent.putObject(
                                unwrappedSelector, selectedKeysFieldOffset, selectedKeySet);
                        PlatformDependent.putObject(
                                unwrappedSelector, publicSelectedKeysFieldOffset, selectedKeySet);
                        return null;
                    }
                    // We could not retrieve the offset, lets try reflection as last-resort.
                }

                Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
                if (cause != null) {
                    return cause;
                }
                cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
                if (cause != null) {
                    return cause;
                }

                /*
                    把selectedKeys设置为SelectedSelectionKeySet（它是数组实现)，原来是HashSet，也就是用HashMap实现
                    把publicSelectedKeys设置为SelectedSelectionKeySet（它是数组实现)，原来是HashSet，也就是用HashMap实现
                */
                selectedKeysField.set(unwrappedSelector, selectedKeySet);
                publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
                return null;
            } catch (NoSuchFieldException e) {
                return e;
            } catch (IllegalAccessException e) {
                return e;
            }
        }
    });

    if (maybeException instanceof Exception) {
        selectedKeys = null;
        Exception e = (Exception) maybeException;
        logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
        return new SelectorTuple(unwrappedSelector);
    }
    selectedKeys = selectedKeySet;
    logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
    return new SelectorTuple(unwrappedSelector,
                             new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
}
```

openSelector() 方法是用于创建一个新的 Selector，并对其进行优化，以提高性能。在优化过程中，会尝试将 SelectedSelectionKeySet 对象设置为 Selector 的属性，以提供更高效的选择器操作。

- 为了在遍历 selectedKey 时提高性能。（使用数组遍历）。

- 只有在遍历时会使用包装过的Selector。

#### nio thread何时启动

测试代码：

```java
EventLoop eventLoop = new NioEventLoopGroup().next();
eventLoop.execute(() -> {
    System.out.println("hello");
});
```

开始分析nio thread何时启动：

```java
// 调用：io.netty.util.concurrent.SingleThreadEventExecutor#execute
public void execute(Runnable task) {
    execute0(task);
}
private void execute0(@Schedule Runnable task) {
    // 有效性检查，任务是否为空
    ObjectUtil.checkNotNull(task, "task");
    execute(task, !(task instanceof LazyRunnable) && wakesUpForTask(task));
}
private void execute(Runnable task, boolean immediate) {
    // 当前线程对比EventLoop线程是不是同一个线程
    boolean inEventLoop = inEventLoop();
    // 将任务添加到任务队列
    addTask(task);

    // inEventLoop第一次为false，取反为true，进入if逻辑
    if (!inEventLoop) {
        // 首次开启线程
        startThread();
        if (isShutdown()) {
            boolean reject = false;
            try {
                if (removeTask(task)) {
                    reject = true;
                }
            } catch (UnsupportedOperationException e) {
                // The task queue does not support removal so the best thing we can do is to just move on and
                // hope we will be able to pick-up the task before its completely terminated.
                // In worst case we will log on termination.
            }
            if (reject) {
                reject();
            }
        }
    }

    if (!addTaskWakesUp && immediate) {
        wakeup(inEventLoop);
    }
}
```

`startThread()`开启 nio thread：

```java
// NioEventLoop的父类
public abstract class SingleThreadEventExecutor extends AbstractScheduledEventExecutor implements OrderedEventExecutor {
    private static final int ST_NOT_STARTED = 1;
    private static final int ST_STARTED = 2;

    private void startThread() {
        // 第一次线程状态为未开启
        if (state == ST_NOT_STARTED) {
            // 使用 cas 修改线程状态为已开启，如果cas成功则去创建nio thread。（只有第一次调用execute()才会cas成功）
            if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
                boolean success = false;
                try {
                    // 真正干活的方法
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_NOT_STARTED);
                    }
                }
            }
        }
    }

    private void doStartThread() {
        assert thread == null;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                /*
                executor就是nio thread
                    将executor执行时的当前线程赋值给了 EventLoop 的成员变量 thread
                */
                thread = Thread.currentThread();
                if (interrupted) {
                    thread.interrupt();
                }

                boolean success = false;
                updateLastExecutionTime();
                try {
                    /*
                        执行 NioEventLoop run 方法（Run the tasks in the taskQueue）
                        查看有没有任务、定时任务、IO事件，如果有则执行任务
                    */
                    SingleThreadEventExecutor.this.run();
                    success = true;
                } catch (Throwable t) {
                    logger.warn("Unexpected exception from an event executor: ", t);
                } finally {
                    ... ...
                }
            }
        });
    }
}
```

结论 ：

- 当首次调用 EventLoop#execute() 方法时会创建 nio thread。

- 并且只会启动一次，通过state状态控制。

#### 提交普通任务会不会结束select阻塞

```java
// 调用：io.netty.channel.nio.NioEventLoop#run
protected void run() {
    int selectCnt = 0;
    for (;;) {
        try {
            int strategy;
            try {
                strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                switch (strategy) {
                case SelectStrategy.CONTINUE:
                    continue;

                case SelectStrategy.BUSY_WAIT:
                    // fall-through to SELECT since the busy-wait is not supported with NIO

                case SelectStrategy.SELECT:
                    long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                    if (curDeadlineNanos == -1L) {
                        curDeadlineNanos = NONE; // nothing on the calendar
                    }
                    nextWakeupNanos.set(curDeadlineNanos);
                    try {
                        if (!hasTasks()) {
                            /*
                                带有超时时间的 select()，超过指定时间则会被唤醒
                                或者被wakeup()，也会被唤醒，向下执行逻辑，以便处理普通任务
                            */
                            strategy = select(curDeadlineNanos);
                        }
                    } finally {
                        // This update is just to help block unnecessary selector wakeups
                        // so use of lazySet is ok (no race condition)
                        nextWakeupNanos.lazySet(AWAKE);
                    }
                    // fall through
                default:
                }
            } catch (IOException e) {
                ......
            }

            selectCnt++;
            cancelledKeys = 0;
            needsToSelectAgain = false;
            final int ioRatio = this.ioRatio;
            boolean ranTasks;
            ......
        } catch (CancelledKeyException e) {
            ......
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            handleLoopException(t);
        } finally {
            ......
        }
    }
}

private int select(long deadlineNanos) throws IOException {
    if (deadlineNanos == NONE) {
        return selector.select();
    }
    // Timeout will only be 0 if deadline is within 5 microsecs
    long timeoutMillis = deadlineToDelayNanos(deadlineNanos + 995000L) / 1000000L;
    return timeoutMillis <= 0 ? selector.selectNow() : selector.select(timeoutMillis);
}
```

提交普通任务（非第一次提交）：

```java
// io.netty.util.concurrent.SingleThreadEventExecutor#execute
private void execute(Runnable task, boolean immediate) {
    boolean inEventLoop = inEventLoop();
    addTask(task);
    if (!inEventLoop) {
        ......
    }

    if (!addTaskWakesUp && immediate) {
        // 唤醒 selector
        wakeup(inEventLoop);
    }
}

// 调用子类：io.netty.channel.nio.NioEventLoop#wakeup
protected void wakeup(boolean inEventLoop) {

    if (!inEventLoop && nextWakeupNanos.getAndSet(AWAKE) != AWAKE) {
        // selector被唤醒，即nio thread被唤醒
        selector.wakeup();
    }
}
```

结论：

- 当提交普通任务时会结束 select 阻塞。

##### wakeup()方法如何理解

```java
// 调用子类：io.netty.channel.nio.NioEventLoop#wakeup
protected void wakeup(boolean inEventLoop) {
    /*
    !inEventLoop：如果不是nio thread，即普通任务
    nextWakeupNanos.getAndSet(AWAKE) != AWAKE
        ：如果nextWakeupNanos不是AWAKE（-1），即eventLoop线程还堵塞在IO上，
            此时需要调用selector.wakeup()方法唤醒堵塞在IO上的线程，任务来了并且要求立即执行，赶紧去执行任务；
        ：如果是AWAKE，则表示EL线程已经是唤醒状态，不需要去重复唤醒。
    */
    if (!inEventLoop && nextWakeupNanos.getAndSet(AWAKE) != AWAKE) {
        // selector被唤醒，即nio thread被唤醒
        selector.wakeup();
    }
}
```

结论：

1. 只有当其它线程提交任务时，才会调用selector#wakeup()方法。

2. 如果有多个其它线程都来提交任务，会使用cas来避免 wakeup() 被频繁调用。

#### 什么时候会进入SelectStrategy.SELECT分支

```java
public interface SelectStrategy {
    /**
     * Indicates a blocking select should follow.
        表示应随后选择一个阻塞选择。
     */
    int SELECT = -1;
    /**
     * Indicates the IO loop should be retried, no blocking select to follow directly.
        表示应该重试IO循环，没有需要直接遵循的阻塞选择。
     */
    int CONTINUE = -2;
    /**
     * Indicates the IO loop to poll for new events without blocking.
        指示不轮询新事件的IO循环。
     */
    int BUSY_WAIT = -3;
}

public final class NioEventLoop extends SingleThreadEventLoop {

    /*
    hasTasks()如果返回true，则会调用selectNowSupplier.get()方法
        其内部调用的是 selector.selectNow()，该selectNow()方法不会阻塞
        会立即去Selector选择器上查看有没有事件，如果有则返回事件个数，如果没有也不会报错，返回0
    如果有普通任务时，会顺便获取到IO事件。
    */
    private final IntSupplier selectNowSupplier = new IntSupplier() {
        @Override
        public int get() throws Exception {
            return selectNow();
        }
    };

    int selectNow() throws IOException {
        return selector.selectNow();
    }

    protected void run() {
        int selectCnt = 0;
        for (;;) {
            try {
                int strategy;
                try {
                    // 分支由 selectStrategy.calculateStrategy() 控制
                    strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                    switch (strategy) {
                    case SelectStrategy.CONTINUE:
                        continue;

                    case SelectStrategy.BUSY_WAIT:
                        // fall-through to SELECT since the busy-wait is not supported with NIO

                    case SelectStrategy.SELECT:
                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                        if (curDeadlineNanos == -1L) {
                            curDeadlineNanos = NONE; // nothing on the calendar
                        }
                        nextWakeupNanos.set(curDeadlineNanos);
                        try {
                            if (!hasTasks()) {
                                strategy = select(curDeadlineNanos);
                            }
                        } finally {
                            // This update is just to help block unnecessary selector wakeups
                            // so use of lazySet is ok (no race condition)
                            nextWakeupNanos.lazySet(AWAKE);
                        }
                        // fall through
                    default:
                    }
                } catch (IOException e) {
                    ......
                }
                ......
            } catch (CancelledKeyException e) {
                ......
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                handleLoopException(t);
            } finally {
                ......
            }
        }
    }
}
```

NIOEventLoop执行策略：

```java
final class DefaultSelectStrategy implements SelectStrategy {
    static final SelectStrategy INSTANCE = new DefaultSelectStrategy();

    private DefaultSelectStrategy() { }

    @Override
    public int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception {
        return hasTasks ? selectSupplier.get() : SelectStrategy.SELECT;
    }
}
```

结论：

- 没有任务时，才会进入SelectStrategy.SELECT分支。

- 当有普通任务时，会调用selector.selectNow()顺被获取到IO事件。

##### 何时会select阻塞，阻塞多久?

```java
public final class NioEventLoop extends SingleThreadEventLoop {

    private static final long NONE = Long.MAX_VALUE;

    protected void run() {
        int selectCnt = 0;
        for (;;) {
            try {
                int strategy;
                try {
                    strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                    switch (strategy) {
                    ......
                    case SelectStrategy.SELECT:
                        // 获取下一个定时任务的执行时间，若没有定时任务则返回-1
                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();

                        // 如果没有下一个定时任务，将 curDeadlineNanos 设置为 NONE 表示没有任务在计划中
                        if (curDeadlineNanos == -1L) {
                            curDeadlineNanos = NONE; // nothing on the calendar
                        }

                        // 将 curDeadlineNanos 设置为 nextWakeupNanos，以表示下次唤醒的时间。
                        //  nextWakeupNanos 是一个 AtomicLong 类型的变量，用于记录下次唤醒的时间戳
                        nextWakeupNanos.set(curDeadlineNanos);
                        try {
                            if (!hasTasks()) {
                                // 如果没有定时任务，curDeadlineNanos值为long最大值，返回 selectedKeys 数量.
                                strategy = select(curDeadlineNanos);
                            }
                        } finally {
                            // This update is just to help block unnecessary selector wakeups
                            // so use of lazySet is ok (no race condition)

                            /*
                                将 nextWakeupNanos 设置为 AWAKE，表示当前选择器已被唤醒
                                用于帮助阻塞不必要的选择器唤醒。在这里使用 lazySet 是为了避免多线程竞争问题。
                            */
                            nextWakeupNanos.lazySet(AWAKE);
                        }
                        // fall through
                    default:
                    }
                } 
                ......
            } 
            ......
        }
    }

    private int select(long deadlineNanos) throws IOException {
        // 如果没有定时任务，则会一直阻塞
        if (deadlineNanos == NONE) {
            return selector.select();
        }
        // Timeout will only be 0 if deadline is within 5 microsecs

        /*
        假设下一次执行定时任务是1秒后，则deadlineNanos=1000000000（单位纳秒）
            timeoutMillis = (1000000000 + 995000) / 1000000 = 1000.995 毫秒
        select()阻塞1秒左右，比1秒稍长一点
        */
        long timeoutMillis = deadlineToDelayNanos(deadlineNanos + 995000L) / 1000000L;
        return timeoutMillis <= 0 ? selector.selectNow() : selector.select(timeoutMillis);
    }
}
```

结论：

- 当没有普通任务时，会select阻塞。

- 基于下一个定时执行的时间来设置阻塞时间。

- 默认会阻塞 long 的最大值。

#### nio空轮询bug在哪里体现，如何解决?

```java
protected void run() {
    // 用于循环计数
    int selectCnt = 0;
    for (;;) {
        try {
            int strategy;
            try {
                strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                switch (strategy) {
                ......
                case SelectStrategy.SELECT:
                    long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                    if (curDeadlineNanos == -1L) {
                        curDeadlineNanos = NONE; // nothing on the calendar
                    }
                    nextWakeupNanos.set(curDeadlineNanos);
                    try {
                        if (!hasTasks()) {
                            // 可能会出现 select 空轮询bug
                            strategy = select(curDeadlineNanos);
                        }
                    } 
                    ......
                default:
                }
            } catch (IOException e) {......}
            selectCnt++;
            ......
            if (ranTasks || strategy > 0) {
                if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                            selectCnt - 1, selector);
                }
                selectCnt = 0;

            // 如果select出现空轮询bug，默认循环512次会重新 rebuild Selector
            } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
                selectCnt = 0;
            }
        } 
        ......
    }
}

private boolean unexpectedSelectorWakeup(int selectCnt) {
    if (Thread.interrupted()) {
        // Thread was interrupted so reset selected keys and break so we not run into a busy loop.
        // As this is most likely a bug in the handler of the user or it's client library we will
        // also log it.
        //
        // See https://github.com/netty/netty/issues/2426
        if (logger.isDebugEnabled()) {
            logger.debug("Selector.select() returned prematurely because " +
                    "Thread.currentThread().interrupt() was called. Use " +
                    "NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
        }
        return true;
    }
    if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
            selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
        // The selector returned prematurely many times in a row.
        // Rebuild the selector to work around the problem.
        logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.",
                selectCnt, selector);

        // 重新创建一个 Selector，替换出现bug的Selector，旧的Selector selectedKeys 等信息拷贝到新 Selector
        rebuildSelector();
        return true;
    }
    return false;
}
```

结论：

- 解决1：Netty会重新创建一个Selector，替换出现bug的Selector。

- 解决2：重写Selector的实现。

- 这个空轮询bug是JDK在Linux下的selector才会出现的bug。

- 出现bug后，select()不会阻塞，还是会继续向下执行逻辑。

#### ioRatio控制什么？设置为100有何作用？

- 作用：ioRatio 控制处理 io 事件所占用的时间比例。默认50%，即50%时间用于处理IO时间，50%时间处理普通任务。

```java
protected void run() {
    int selectCnt = 0;
    for (;;) {
        try {
            int strategy;
            // ...... strategy = select(curDeadlineNanos);

            selectCnt++;
            cancelledKeys = 0;
            needsToSelectAgain = false;
            final int ioRatio = this.ioRatio;
            boolean ranTasks;

            // 设置为100，则处理普通任务没有超时时间
            if (ioRatio == 100) {
                try {
                    if (strategy > 0) {
                        processSelectedKeys();
                    }
                } finally {
                    // Ensure we always run tasks.
                    ranTasks = runAllTasks();
                }
            } else if (strategy > 0) {
                final long ioStartTime = System.nanoTime();
                try {
                    // 处理选择器上的可连接、可写、可读事件
                    processSelectedKeys();
                } finally {
                    // 在finally 执行普通任务
                    // Ensure we always run tasks.

                    /*
                         ioTime 表示执行io事件耗时时间
                         假设IO事件耗时8s，当然实际不可能这么耗时。则 ioTime=8，假设ioRatio=80
                         ioTime * (100 - ioRatio) / ioRatio = 8*20/80 = 2s
                         普通任务耗时2秒，如果2秒钟没有执行完，则退出执行任务的循环，等待下一次继续执行普通任务即可
                    */
                    final long ioTime = System.nanoTime() - ioStartTime;
                    ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                }
            } else {
                ranTasks = runAllTasks(0); // This will run the minimum number of tasks
            }

            if (ranTasks || strategy > 0) {
                if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                            selectCnt - 1, selector);
                }
                selectCnt = 0;
            } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
                selectCnt = 0;
            }
        } 
        ......
    }
}
```

结论：

- ioRatio 控制处理 io 事件所占用的时间比例。

- 若 ioRatio=100，则会影响到IO事件的处理。
  
  - 因为ioRatio=100，处理普通任务时没有设置超时时间，即将所有普通任务处理完成，才进入新一轮循环。

#### selectedKeys优化

这部分可参考前文`EventLoop为何有两个selector成员变量?`。简言之就是优化底层selectedKeys的数据结构。

在Netty种的体现：

```java
// 处理SelectedKeys可连接、可读、可写事件
// io.netty.channel.nio.NioEventLoop#processSelectedKeys
private void processSelectedKeys() {
    // selectedKeys != null 如果成立则表示Netty将selectedKeys的底层HashSet实现替换成了数组实现
    if (selectedKeys != null) {
        // 数组遍历
        processSelectedKeysOptimized();
    } else {
        // HashSet 遍历（迭代器遍历）
        processSelectedKeysPlain(selector.selectedKeys());
    }
}

private void processSelectedKeysOptimized() {
    for (int i = 0; i < selectedKeys.size; ++i) {
        final SelectionKey k = selectedKeys.keys[i];
        // null out entry in the array to allow to have it GC'ed once the Channel close
        // See https://github.com/netty/netty/issues/2363
        selectedKeys.keys[i] = null;

        /*
            获取附件，即NioServerSocketChannel，由NioServerSocketChannel处理selectedKeys发生的事件
                先获取到NioServerSocketChannel，由NioServerSocketChannel获取到pipeline
                    再由pipeline获取到handler，由handler处理selectedKeys发生的事件
        */
        final Object a = k.attachment();

        // AbstractNioChannel是所有NioChannel的父类，所以会进入if分支
        if (a instanceof AbstractNioChannel) {
            // 这个方法会区分不同事件类型
            processSelectedKey(k, (AbstractNioChannel) a);
        } else {
            @SuppressWarnings("unchecked")
            NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
            processSelectedKey(k, task);
        }

        if (needsToSelectAgain) {
            // null out entries in the array to allow to have it GC'ed once the Channel close
            // See https://github.com/netty/netty/issues/2363
            selectedKeys.reset(i + 1);

            selectAgain();
            i = -1;
        }
    }
}
```

#### 在何处区分不同事件类型

```java
// io.netty.channel.nio.NioEventLoop#processSelectedKey
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
        final EventLoop eventLoop;
        try {
            eventLoop = ch.eventLoop();
        } catch (Throwable ignored) {
            // If the channel implementation throws an exception because there is no event loop, we ignore this
            // because we are only trying to determine if ch is registered to this event loop and thus has authority
            // to close ch.
            return;
        }
        // Only close ch if ch is still registered to this EventLoop. ch could have deregistered from the event loop
        // and thus the SelectionKey could be cancelled as part of the deregistration process, but the channel is
        // still healthy and should not be closed.
        // See https://github.com/netty/netty/issues/5125
        if (eventLoop == this) {
            // close the channel if the key is not valid anymore
            unsafe.close(unsafe.voidPromise());
        }
        return;
    }

    try {
        int readyOps = k.readyOps();
        // We first need to call finishConnect() before try to trigger a read(...) or write(...) as otherwise
        // the NIO JDK channel implementation may throw a NotYetConnectedException.

        // 可连接事件
        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
            // remove OP_CONNECT as otherwise Selector.select(..) will always return without blocking
            // See https://github.com/netty/netty/issues/924
            int ops = k.interestOps();
            ops &= ~SelectionKey.OP_CONNECT;
            k.interestOps(ops);

            unsafe.finishConnect();
        }

        // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.

        // 可写事件
        if ((readyOps & SelectionKey.OP_WRITE) != 0) {
            // Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
            ch.unsafe().forceFlush();
        }

        // Also check for readOps of 0 to workaround possible JDK bug which may otherwise lead
        // to a spin loop

        // 可读事件，readyOps取值为1则是OP_READ，取值为16则是OP_ACCEPT
        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
            // accept流程
            unsafe.read();
        }
    } catch (CancelledKeyException ignored) {
        unsafe.close(unsafe.voidPromise());
    }
}
```

### accept流程

回忆一下nio中的accept流程：

1. selector.select()阻塞直到事件发生。

2. 遍历处理selectedKeys。

3. 判断事件类型是否为accept。

4. 创建SocketChannel，设置非阻塞。

5. 将SocketChannel注册至selector。

6. 关注selectionKey的read事件。

验证在Netty中什么地方对应这些步骤的，1~3的步骤在前面已经分析过了。现在开始分析4~6的步骤，监听客户端感兴趣事件：

```java
private final class NioMessageUnsafe extends AbstractNioUnsafe {

    private final List<Object> readBuf = new ArrayList<Object>();

    /*
        如果服务端发生accept或read事件，则会进入这里
        【假设此时发生的是客户端连接事件 accept】
    */
    @Override
    public void read() {
        assert eventLoop().inEventLoop();
        final ChannelConfig config = config();
        final ChannelPipeline pipeline = pipeline();
        final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        allocHandle.reset(config);

        boolean closed = false;
        Throwable exception = null;
        try {
            try {
                do {
                    /*
                        4、创建SocketChannel，设置非阻塞。
                        readBuf会放到NioServerSocketChannel的pipeline上进行处理
                    */
                    int localRead = doReadMessages(readBuf);
                    if (localRead == 0) {
                        break;
                    }
                    if (localRead < 0) {
                        closed = true;
                        break;
                    }

                    allocHandle.incMessagesRead(localRead);
                } while (continueReading(allocHandle));
            } catch (Throwable t) {
                exception = t;
            }

            int size = readBuf.size();
            for (int i = 0; i < size; i ++) {
                readPending = false;

                /*
                    NioServerSocketChannel上的pipeline上共有三个handler（会依次执行每个handler）：
                        head -> acceptor(ServerBootstrapAcceptor) -> tail
                        而此时的accept事件会在ServerBootstrapAcceptor handler中得到执行
                        触发 handler 的 channelRead() 事件
                */
                pipeline.fireChannelRead(readBuf.get(i));
            }
            readBuf.clear();
            allocHandle.readComplete();
            pipeline.fireChannelReadComplete();

            if (exception != null) {
                closed = closeOnReadError(exception);

                pipeline.fireExceptionCaught(exception);
            }

            if (closed) {
                inputShutdown = true;
                if (isOpen()) {
                    close(voidPromise());
                }
            }
        } finally {
            // Check if there is a readPending which was not processed yet.
            // This could be for two reasons:
            // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
            // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
            //
            // See https://github.com/netty/netty/issues/2254
            if (!readPending && !config.isAutoRead()) {
                removeReadOp();
            }
        }
    }
}
```

#### 创建SocketChannel，设置非阻塞

```java
// io.netty.channel.socket.nio.NioServerSocketChannel#doReadMessages
protected int doReadMessages(List<Object> buf) throws Exception {

    // 内部会调用serverSocketChannel.accept()建立连接，和原生nio对应上了
    SocketChannel ch = SocketUtils.accept(javaChannel());

    try {
        if (ch != null) {
            /*
            new NioSocketChannel(this, ch)：
                将Netty中NioSocketChannel和Java中SocketChannel关联起来，并设置SocketChannel为非阻塞
            将创建的NioSocketChannel当作消息添加到集合中，将来会由pipeline中的handler进行处理
            */
            buf.add(new NioSocketChannel(this, ch));
            return 1;
        }
    } catch (Throwable t) {
        logger.warn("Failed to create a new channel from an accepted socket.", t);

        try {
            ch.close();
        } catch (Throwable t2) {
            logger.warn("Failed to close a socket.", t2);
        }
    }

    return 0;
}
```

#### 将SocketChannel注册至selector

```java
public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {

    private static class ServerBootstrapAcceptor extends ChannelInboundHandlerAdapter {
        ......
        /*
            此时这个 msg 就是前面创建的NioSocketChannel
        */
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            final Channel child = (Channel) msg;

            child.pipeline().addLast(childHandler);

            setChannelOptions(child, childOptions, logger);
            setAttributes(child, childAttrs);

            try {
                /*
                这里的register()和前面注册NioServerSocketChannel时调用的方法是一样的
                在其内部注册完成后会调用自定义启动引导类时定义的
                    `.childHandler(new ChannelInitializer<NioServerSocketChannel>() {}`初始化逻辑
                    （即触发新的SocketChannel上的初始化事件）
                register()做了两件事：
                    步骤5. 将SocketChannel注册至selector。
                        sc.register(eventLoop的选择器, 0, NioSocketChannel)
                    步骤6. 关注selectionKey的read事件。
                此时的pipeline是：head -> 自定义的handler -> tail
                */
                childGroup.register(child).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            forceClose(child, future.cause());
                        }
                    }
                });
            } catch (Throwable t) {
                forceClose(child, t);
            }
        }
        ......
    }
}
```

### read流程

回忆一下nio中的read流程：

1. selector.select()阻塞直到事件发生。

2. 遍历处理selectedKeys。

3. 判断事件类型是否为read。

4. 读取操作。

验证在Netty中什么地方对应这些步骤的，1~3的步骤在前面已经分析过了。现在开始分析步骤4，读取操作：

```java
public abstract class AbstractNioByteChannel extends AbstractNioChannel {

    protected class NioByteUnsafe extends AbstractNioUnsafe {
        ......
        @Override
        public final void read() {
            final ChannelConfig config = config();
            if (shouldBreakReadReady(config)) {
                clearReadPending();
                return;
            }
            final ChannelPipeline pipeline = pipeline();
            final ByteBufAllocator allocator = config.getAllocator();
            final RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
            allocHandle.reset(config);

            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                do {
                    byteBuf = allocHandle.allocate(allocator);
                    allocHandle.lastBytesRead(doReadBytes(byteBuf));
                    if (allocHandle.lastBytesRead() <= 0) {
                        // nothing was read. release the buffer.
                        byteBuf.release();
                        byteBuf = null;
                        close = allocHandle.lastBytesRead() < 0;
                        if (close) {
                            // There is nothing left to read as we received an EOF.
                            readPending = false;
                        }
                        break;
                    }

                    allocHandle.incMessagesRead(1);
                    readPending = false;

                    /*
                        步骤4、读取操作。
                        处理读的流程，触发 channelRead()
                        依次调用pipeline上的handler：head -> 自定义handler -> tail
                    */
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                } while (allocHandle.continueReading());

                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (close) {
                    closeOnRead(pipeline);
                }
            } catch (Throwable t) {
                handleReadException(pipeline, byteBuf, t, close, allocHandle);
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }
    }
}
```

## 附录

### Java nio  事件

Java NIO事件针对于服务器端而言。

在服务器端的网络编程中，通常会使用 Java NIO 来实现异步非阻塞的网络通信。服务器端会创建一个 `ServerSocketChannel` 来监听客户端的连接请求，并使用一个 `Selector` 来注册感兴趣的事件。服务器端会监听三种事件：可接受连接事件（`OP_ACCEPT`）、可读事件（`OP_READ`）和可写事件（`OP_WRITE`）。

- 可连接事件（`OP_CONNECT`）：当一个客户端发起连接请求，但连接还未建立时（即有新的客户端连接请求到达服务端时），会触发可连接事件。通常用于客户端与服务器建立连接的过程。当该事件触发时，可以使用 `SelectionKey.isConnectable()` 方法来判断。

- 可读事件（`OP_READ`）：当一个通道有数据可读时，会触发可读事件。通常用于服务器端接收客户端发送的数据。当该事件触发时，可以使用 `SelectionKey.isReadable()` 方法来判断哪些连接有数据可读，并使用对应的 `SocketChannel` 读取数据。

- 可写事件（`OP_WRITE`）：当一个通道可写入数据时，会触发可写事件。通常用于服务器端向客户端发送数据。当该事件触发时，可以使用 `SelectionKey.isWritable()` 方法来判断哪些连接可以写入数据，并使用对应的 `SocketChannel` 向客户端发送数据。

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

### NioEventLoop调用父类构造

```java
// io.netty.channel.nio.NioEventLoop#NioEventLoop
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
             SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
             EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
    super(parent, executor, false, newTaskQueue(taskQueueFactory), newTaskQueue(tailTaskQueueFactory),
            rejectedExecutionHandler);
    this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
    this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");
    final SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
}

// 调用：io.netty.channel.SingleThreadEventLoop#SingleThreadEventLoop
protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                boolean addTaskWakesUp, Queue<Runnable> taskQueue, Queue<Runnable> tailTaskQueue,
                                RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, addTaskWakesUp, taskQueue, rejectedExecutionHandler);
    tailTasks = ObjectUtil.checkNotNull(tailTaskQueue, "tailTaskQueue");
} 

// 调用：io.netty.util.concurrent.SingleThreadEventExecutor#SingleThreadEventExecutor
protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                    boolean addTaskWakesUp, Queue<Runnable> taskQueue,
                                    RejectedExecutionHandler rejectedHandler) {
    super(parent);
    this.addTaskWakesUp = addTaskWakesUp;
    this.maxPendingTasks = DEFAULT_MAX_PENDING_EXECUTOR_TASKS;
    this.executor = ThreadExecutorMap.apply(executor, this);

    /*
        taskQueue：提交给 NioEventLoop 的任务都会进入到这个 taskQueue 中等待被执行
        默认容量是16
    */
    this.taskQueue = ObjectUtil.checkNotNull(taskQueue, "taskQueue");
    this.rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
}
```

NioEventLoop 的父类是 SingleThreadEventLoop，而 SingleThreadEventLoop 的父类是 SingleThreadEventExecutor，从其类名可以看出它是一个 Executor，是一个线程池，而且是 Single Thread 单线程的。

也就是说，线程池 NioEventLoopGroup 中的每一个线程 NioEventLoop 也可以当做一个线程池来用，只不过池中只有一个线程。

### inEventLoop()

- 作用：判断当前线程是不是 nio 线程。

```java
// 调用：io.netty.util.concurrent.AbstractEventExecutor#inEventLoop
public boolean inEventLoop() {
    return inEventLoop(Thread.currentThread());
}

// 一般是调用：io.netty.util.concurrent.SingleThreadEventExecutor#inEventLoop
public boolean inEventLoop(Thread thread) {
    // 第一次 this.thread 为 null
    return thread == this.thread;
}
```

### nextScheduledTaskDeadlineNanos()

- 作用：获取下一个定时任务的执行时间，若没有定时任务则返回-1。

```java
// io.netty.util.concurrent.AbstractScheduledEventExecutor#nextScheduledTaskDeadlineNanos
protected final long nextScheduledTaskDeadlineNanos() {
    ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
    return scheduledTask != null ? scheduledTask.deadlineNanos() : -1;
}

final ScheduledFutureTask<?> peekScheduledTask() {
    Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
    return scheduledTaskQueue != null ? scheduledTaskQueue.peek() : null;
}
```

### deadlineToDelayNanos()

- 作用：给定一个任意的截止日期，返回过期剩余时间。

```java
// io.netty.util.concurrent.AbstractScheduledEventExecutor#deadlineToDelayNanos
protected static long deadlineToDelayNanos(long deadlineNanos) {
    return ScheduledFutureTask.deadlineToDelayNanos(deadlineNanos);
}

// io.netty.util.concurrent.ScheduledFutureTask#deadlineToDelayNanos
static long deadlineToDelayNanos(long deadlineNanos) {
    return deadlineNanos == 0L ? 0L : Math.max(0L, deadlineNanos - nanoTime());
}
```

### NioEventLoop常量SELECTOR_AUTO_REBUILD_THRESHOLD

```java
private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;

static {
    if (PlatformDependent.javaVersion() < 7) {
        final String key = "sun.nio.ch.bugLevel";
        final String bugLevel = SystemPropertyUtil.get(key);
        if (bugLevel == null) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        System.setProperty(key, "");
                        return null;
                    }
                });
            } catch (final SecurityException e) {
                logger.debug("Unable to get/set System Property: " + key, e);
            }
        }
    }

    int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("io.netty.selectorAutoRebuildThreshold", 512);
    if (selectorAutoRebuildThreshold < MIN_PREMATURE_SELECTOR_RETURNS) {
        selectorAutoRebuildThreshold = 0;
    }

    SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;

    if (logger.isDebugEnabled()) {
        logger.debug("-Dio.netty.noKeySetOptimization: {}", DISABLE_KEY_SET_OPTIMIZATION);
        logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", SELECTOR_AUTO_REBUILD_THRESHOLD);
    }
}
```

### SocketUtils.accept(javaChannel())

```java
public static SocketChannel accept(final ServerSocketChannel serverSocketChannel) throws IOException {
    try {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<SocketChannel>() {
            @Override
            public SocketChannel run() throws IOException {
                return serverSocketChannel.accept();
            }
        });
    } catch (PrivilegedActionException e) {
        throw (IOException) e.getCause();
    }
}
```