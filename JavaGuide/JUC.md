# JUC

## 进程与线程

### 进程与线程

#### 进程

* 程序是由指令和数据组成的，但这些指令要运行起来，数据要读写，就必须将指令加载至 CPU，数据加载至内存。再指令运行过程中还需要用到磁盘、网络等设备。而进程就是用来加载指令、管理内存、管理IO的。

* 当一个程序被运行，从磁盘加载这个程序的代码到内存，这时就开启了一个进程。

* 进程可以视为程序的一个实例。有些程序可以同时运行多个实例进程（记事本、画图、浏览器等），有的程序只能启动一盒实例进程（360、网易云等）

#### 线程

* 一个进程中可以分为一到多个线程。

* 一个线程就是一个指令流，将指令流中的一条条指令以一定的顺序交给CPU执行。

* 在 Java 中，线程作为最小调度单位，进程作为资源分配的最小单位。在 Windows 中进程是不活动的，只是作为线程的容器。

#### 线程和进程的区别

* 进程基本上相互独立，而线程存在于进程内，是进程的子集。

* 进程拥有共享的资源 ，如内存空间等，供其内部的线程共享。

* 进程间通信较为复杂：
  
  * 同一台计算机的进程通信称为 IPC（Inter process communication）
  
  * 不同计算机之间的进程通信，需要通过网络，并遵守协议，如 HTTP等。

* 线程通信相对简单，因为它们共享进程内的内存（比如多个线程可以访问到同一个共享变量）

* 线程更轻量，线程上下文（CPU时间片）切换成本一般要比进程上下切换低。

#### 并行与并发

并行：同一时刻执行。

并发：同一时间段执行。

* 涉及到线程上下文切换，将 cpu时间片 分给不同的线程使用。（Windows 时间片最小约为15毫秒）

* 假设8核cpu开启了16个线程，但不足以同时满足1000人秒杀，这种情况操作系统的任务调度器就会让线程轮流使用cpu。

#### 同步和异步

从方法调用的角度看：

* 方法的调用者需要等待结果返回，才能继续运行即是同步。

* 方法的调用者不需要等待结果返回，发起方法调用后，就可以返回，继续运行即是异步。

## Java 线程

### 创建和运行线程

#### Thread

```java
Thread t = new Thread("t1") {
    @Override
    public void run() {
        log.debug("new Thread");
    }
};
// 启动线程
t.start();
log.debug("main...");
```

#### Runnable

```java
new Thread(() ->{
    log.debug("Runnable...");
}, "t1").start();

log.debug("main...");
```

#### FutureTask

```java
FutureTask<Integer> future = new FutureTask<>(() -> {
    log.debug("FutureTask...");
    return 100;
});
new Thread(future, "t1").start();

// get() 阻塞同步等待 task 执行的结果
log.debug("result: {}", future.get());
```

FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况。

### 查看进程线程的命令

#### Windows

* 任务管理器可以查看进程和线程数，也可以 kill 进程。

* `tasklist` 查看进程。

* `taskkill /F /PID <PID>` kill 线程。

#### Linux

* `ps -fe` 查看 Java 进程。

* `ps -fT -p <PID>` 查看指定进程。

* `kill -9 <PID>` kill 进程。

* `top` 查看进程，按 H 切换显示线程。

* `top -H -p <PID>` 查看指定进程的所以线程。

#### Java进程监控

* `jps` 查看所以 Java 进程。

* `jstack <PID>`查看指定 Java 进程的所以线程状态。

* `jconsole`查看指定 Java 进程中线程的运行情况（可视化界面）。

jconsole 远程监控配置（Linux）：

* 需要以如下配置运行 Java 类或 jar（java -jar）。

```txt
java -Djava.rmi.server.hostname=ip地址 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=连接端口 -Dcom.sun.management.jmxremote.ssl=是否安全连接 -Dcom.sun.management.jmxremote.authenticate=是否认证 Java类或jar
```

* 修改 /etc/hosts 文件将 127.0.0.1 映射至主机名

* 如果需要认证访问，还需要几个步骤（具体Google，就不再赘述）

### 线程 API

| API               | 描述                                                                                                                               |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `start()`         | 启动一个新线程，在新的线程中运行 run 方法，但是 start 只是让线程进入就绪状态，并不是立刻执行，CPU的时间片还没分给它。每个线程对象的 start 方法只能调用一次，多次调用会出现 IllegalThreadStateException。    |
| `run()`           | 新线程启动后会调用的方法。如果在Thread对象中传递了 Runnable 参数，那么线程启动后会调用 Runnable 中的 run 方法。否则默认不执行任何操作。但可以创建 Thread 的子类，来覆盖默认行为。                     |
| `join()`          | 等待线程运行结束。                                                                                                                        |
| `join(long n)`    | 等待线程运行结束，最多等待 n 毫秒。                                                                                                              |
| `getId()`         | 获取线程的长整型id，id唯一。                                                                                                                 |
| `getPriority()`   | 获取线程优先级。                                                                                                                         |
| `setPriority()`   | 设置线程优先级。Java中线程优先级是1~10的整数，默认优先级5，较大的优先级能提高该线程被CPU调度的几率。                                                                         |
| `getState()`      | 获取线程状态。Java中线程状态使用枚举类表示的。分别为 NEW、RUNNABLE、BLOCKED、WAITING、TIMED_WAITING、TERMINATED。                                              |
| `isInterrupted()` | 判断是否被中断，不会清除中断标记。                                                                                                                |
| `isAlive()`       | 线程是否存活，为true表示还没有执行完任务。                                                                                                          |
| `interrupt()`     | 中断线程，如果被中断的线程正在 sleep、wait、join 会导致被中断的线程抛出 InterruptedException，并清除中断标记；如果中断正在运行的线程，则会设置中断标记；park（LockSupport） 的线程被中断，也会设置中断标记。 |
| `sleep(long n)`   | 让当前指执行的线程休眠 n 毫秒，休眠期间会让出 cpu 的时间片（休眠期间不会占用cpu）。                                                                                  |

### sleep & yield

#### sleep

1. 调用 sleep 会让当前线程从 Running 进入 Timed Waiting 状态（阻塞）。

2. 其它线程可以使用 interrupt() 方法中断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException。

3. 所谓的中断，就是把正在睡眠的线程"叫醒"。

4. 睡眠结束后的线程未必会立即得到执行（等待分配CPU时间片）。

5. 建议使用 TimeUnit 的 sleep 替代 Thread 的 sleep 来获得更好的可读性。

#### yield

1. `yield()`：通知线程调度器让出当前线程对 cpu 的使用。

2. 调用 `yield()` 会让当前线程从 Running 进入 Runnable 状态（就绪）。然后调度器执行其它线程。（本身可能也会继续得到）

3. 具体的实现依赖操作系统的任务调度器。

### join

`join`：等待线程运行结束，方法是同步的。

`join(long n)`：等待线程运行结束，最多等待 n 毫秒。限时同步。但是如果线程提前执行完了，会导致 join 结束。（如果 join 等待3秒，但是线程两秒就执行完业务了，那么 join 会提前结束，这时等价于 无参 join）

### interrupt

中断正在睡眠（sleep、wait、join）的线程，会清空中断标记（false）。

如果中断正常执行的线程，则不会InterruptException，会将中断标记置为true。被中断的线程可以用`Thread.currentThread().isInterrupted()`判断是否被中断，然后执行对应的逻辑。可以用来停止线程。

#### 两阶段终止

```mermaid
graph TD
    A("while(true)") --> B
    B("有没有被打断?") --是--> C[收尾工作]
    C --> G((结束循环))
    B --否--> D(睡眠指定时间)
    D --无异常--> E(执行监控记录)
    D --有异常--> F(设置打断标记)
    E --> A
    F --> A
```

#### 终止线程的错误思路

1. 使用线程对象的 `stop()` 方法停止线程
   
   * stop 方法会 kill 线程，如果这时线程锁住了共享资源，那么当它被 kill 后就没有机会释放锁了，其它线程也将无法获取锁。

2. 使用 `System.exit(int n)` 方法停止线程
   
   * 目的是仅停止一个线程。但这种做法会让整个程序都停止。

### 主线程和守护线程

默认情况下，Java 进程需要等待所以线程都运行结束，才会结束。

守护（Daemon）线程：只要其它非守护线程运行结束了，即时守护线程的代码没有执行完，也会被强制结束。

* 垃圾回收器线程就是一个守护线程。

* Tomcat 中的 Acceptor 和 Poller 线程都是守护线程，所以当 Tomcat 接收到 shutdown 命令后，不会等待 Acceptor 和 Poller 处理完当前请求。

### 线程状态

#### 五种状态

![](../image/juc_线程状态_5种.png)

这是从操作系统层面来描述的：

* 初始状态：仅仅创建了线程对象，还没有和操作系统线程关联。

* 可运行状态：（就绪状态，yield）与操作系统线程关联，可以被 CPU 调度执行。

* 运行状态：获得了 CPU 时间片。当CPU时间片用完，会从运行状态转换中可运行状态，会导致线程的上下文切换。

* 阻塞状态：调用了阻塞 API，如 BIO 读写文件，这时该线程实际不会用到CPU，会导致线程上下文切换，进入阻塞状态。当BIO操作完成，会由操作系统唤醒阻塞的线程，转换至可运行状态。

* 终止状态：生命周期结束，线程任务执行完了。

#### 六种状态

Java API 层面描述：

* `NEW`：创建线程对象。

* `RUNNABLE`：调用了 start() 方法。RUNNABLE状态涵盖了操作系统层面的可运行状态、运行状态以及阻塞状态（BIO操作、阻塞API）。

* `BLOCKED`：synchronized。

* `WAITING`：join()、wait()、LockSupport.park()。

* `TIMED_WAITING`：sleep(long)、wait(long)、join(long)、LockSupport。

* `TERMINATED`：生命周期结束，线程任务执行完了。

### 线程运行基础

虚拟机栈、栈帧、程序计数器、局部变量表、返回地址 ...

## 共享模型之管程

管程：Monitor

### 共享问题

`i++`不是原子操作，在字节码上对应四条指令：

```txt
getstatic   i   // 获取静态变量 i 的值
iconst_1        // 把数字1放到操作数栈中
iadd            // 自增
putstatic   i   // 将修改后的值存入静态变量 i
```

如果此时有A、B两个线程在多线程下修改静态变量 i 的值，因为两个线程在字节码种都会有`putstatic`（将值写入内存（方法区，JDK8是元空间，在直接内存中存储）），此时 i 的值就会有两种可能。

#### 错乱情况

负数情况：

```mermaid
sequenceDiagram
participant t1 as 线程A
participant t2 as 线程B
participant i as static i
i ->> t2 : getstatic i 读取 0
t2 ->> t2 : iconst_1 准备常数 1
t2 ->> t2 : isub, 线程内 i = -1
t2 ->> t1 : 上下文切换（时间片用完）

i ->> t1 : getstatic i 读取 0
t1 ->> t1 : iconst_1 准备常数 1
t1 ->> t1 : isub, 线程内 i = 1
t1 ->> i : putstatic i 写入 1

t1 ->> t2 : 上下文切换（时间片用完）
t2 ->> i : putstatic i 写入 -1
```

如果是正数的情况，则线程A、B执行顺序调换即可。这是由于指令交错执行造成的线程不安全。

#### 临界区（Critical Section）

在多线程对共享资源读写操作时会发生指令交错，这时就会出现问题（并发问题）。

一段代码块内如果存在对共享资源的多线程读写操作，则称这段代码块为临界区。

#### 竞态条件（Race Condition）

多个线程在临界区内执行，由于代码的执行序列不同而导致结果无法预测，则称之为发生了竞态条件。

### synchronized

避免临界区的竞态条件发生：

* 阻塞式：synchronized、Lock

* 非阻塞式：原子变量

synchronized，俗称对象锁，采用互斥的方式让同一时刻只能有一个线程可以持有对象锁。即使被 synchronized 锁住，线程也不可能一直执行，如果CPU时间片用完了，再去等待获取CPU时间片，期间是不会释放锁的。

synchronized 用对象锁保证了临界区内代码的原子性，临界区内的代码不会被线程切换打断。

#### 方法上的 synchronized

```java
// 成员方法
class Test {
    public synchronized void test() {}
}
// 等价于
class Test {
    public void test() {
        synchronized(this) {}
    }
}

// 静态方法
class Test {
    public synchronized static void test() {}
}
// 等价于
class Test {
    public static void test() {
        synchronized(Test.class) {}
    }
}
```

### Monitor

#### 对象头

以32位虚拟机为例，32位虚拟机空对象头占8字节，64位占16字节。

##### 普通对象

```ruby
|----------------------------------------------------------------|
|                 Object Header (64 bits)                        |
|-------------------------------|--------------------------------|
|      Mark Word (32 bits)      |      Klass Word (32 bits)      |
|-------------------------------|--------------------------------|
```

##### 数组对象

```ruby
|---------------------------------------------------------------------------------------------------|
|                               Object Header (96 bits)                                             |
|-------------------------------|--------------------------------|----------------------------------|
|      Mark Word (32 bits)      |      Klass Word (32 bits)      |      array length (32 bits)      |   
|-------------------------------|--------------------------------|----------------------------------|
```

##### 32位虚拟机Mark Word

```ruby
|----------------------------------------------------------------------|------------------------|
|           Mark Word (32 bits)                                        |   State                |
|----------------------------------------------------------------------|------------------------|
|   hashcode:25               |   age:4   |   biased_lock:0   |   01   |   Normal               |
|----------------------------------------------------------------------|------------------------|
|   thread:23   |   epoch:2   |   age:4   |   biased_lock:1   |   01   |   Biased               |
|----------------------------------------------------------------------|------------------------|
|              ptr_to_lock_record:30                          |   00   |   Lightweight Locked   |
|----------------------------------------------------------------------|------------------------|
|              ptr_to_heavyweight_monitor:30                  |   10   |   Heavyweight Locked   |
|----------------------------------------------------------------------|------------------------|
|                                                             |   11   |   Marked for GC        |
|----------------------------------------------------------------------|------------------------|
```

`biased_lock:0`：表示是否是偏向锁，紧接着最后2位`01`表示加锁状态。

##### 64位虚拟机Mark Word

```ruby
|--------------------------------------------------------------------------------------------------------------|
|                 Mark Word (64 bits)                                                 |   State                |
|-------------------------------------------------------------------------------------|------------------------|
|   unused:25   |hashcode:25  |   unused:1   |   age:4   |   biased_lock:0   |   01   |   Normal               |
|-------------------------------------------------------------------------------------|------------------------|
|   thread:54   |   epoch:2   |   unused:1   |   age:4   |   biased_lock:1   |   01   |   Biased               |
|-------------------------------------------------------------------------------------|------------------------|
|              ptr_to_lock_record:62                                         |   00   |   Lightweight Locked   |
|-------------------------------------------------------------------------------------|------------------------|
|              ptr_to_heavyweight_monitor:62                                 |   10   |   Heavyweight Locked   |
|-------------------------------------------------------------------------------------|------------------------|
|                                                                            |   11   |   Marked for GC        |
|-------------------------------------------------------------------------------------|------------------------|
```

#### Monitor

Monitor：监视器。

每个 Java 对象都可以关联一个 Monitor（操作系统提供的对象） 对象，如果使用 synchronized 给对象加锁（重量级）之后，该对象头的 Mark Word 中就会被设置指向 Monitor 对象的指针。

Monitor 结构如下：

![](../image/juc_Monitor结构.png)

* 刚开始 Monitor 中 Owner 为 null。

* 当 Thread-2 执行 synchronized(obj) 就会将 Monitor 的所有者 Owner 设置为 Thread-2，Monitor 中只能同时存在一个 Owner。

* 在 Thread-2 加锁的过程中，如果 Thread-3、Thread-4、Thread-5 也执行到 synchronized(obj)，就会进入 EntryList BLOCKED。

* 等待 Thread-2 执行完同步代码块中的逻辑后，然后唤醒 EntryList 中等待的线程来竞争锁，竞争锁时是非公平的。

* WaitSet 中的 Thread-0、Thread-1 是之前获得过锁，但条件不满足进入 WAITING 状态的线程。

synchronized 必须是获取到同一个对象的 monitor 才有上述的效果。

##### 字节码角度

```java
public class App {

    static final Object lock = new Object();
    static int counter = 0;

    public static void main(String[] args) {
        synchronized (lock) {
            counter++;
        }
    }
}
```

字节码：

```txt
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: getstatic     #2                  // 加载静态变量 lock引用 到操作数栈上， （synchronized 开始）
         3: dup                               // 复制一份 lock引用，解锁用
         4: astore_1                          // 将 lock引用 从操作数栈弹出，存储局部变量表 slot 1
         5: monitorenter                      // （加锁）将 lock对象 MarkWord 设置为 Monitor 指针
         6: getstatic     #3                  // 加载静态变量 counter 到操作数栈上
         9: iconst_1                          // 准备常数 1，弹入到操作数栈上
        10: iadd                              // +1 
        11: putstatic     #3                  // 将修改后 counter 的值写回静态变量
        14: aload_1                           // 将局部变量表 slot 1 加载到操作数栈 （lock引用）
        15: monitorexit                       // （解锁）将 lock 对象 MarkWord 重置，唤醒 EntryList
        16: goto          24
        19: astore_2                          // 出现异常了，将异常信息从操作数栈弹出，存入局部变量表 slot 2
        20: aload_1                           // 从局部变量表 slot 1 加载 lock引用 到操作数栈上
        21: monitorexit                       // （解锁）将 lock 对象 MarkWord 重置，唤醒 EntryList
        22: aload_2                           // 将局部变量表 slot 2 异常信息加载到操作数栈上
        23: athrow                            // throw e
        24: return

      // 会监测同步代码块中的异常，即时出现异常，也能正常解锁
      Exception table:
         from    to  target type
             6    16    19   any
            19    22    19   any
      LineNumberTable: ...
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      25     0  args   [Ljava/lang/String;
```

#### synchronized 优化进阶

##### 轻量级锁

- 轻量级锁（Lightweight Lock），也称为偏向锁（Biased Locking），是Java中用于提高多线程同步性能的一种机制。它是一种乐观锁的实现方式，用于解决竞争不激烈的情况下对同步操作的性能影响。

- 轻量级锁的基本思想是在对象头中添加一些标志位来表示锁的状态，通过CAS（Compare and Swap）操作来实现锁的获取和释放。当一个线程尝试获取锁时，首先会尝试使用CAS将对象头中的锁标志位更新为自己的线程ID，如果成功获取锁，则表示该线程获取到了轻量级锁。如果CAS操作失败，说明有竞争发生，此时轻量级锁会膨胀为重量级锁，进而采用传统的互斥量方式进行同步。

- 轻量级锁的优势在于避免了传统的互斥量同步带来的性能开销，适用于线程间竞争不激烈的场景。它的主要特点包括：
  
  - 使用CAS操作实现锁的获取和释放，避免了线程阻塞和唤醒带来的开销；
  
  - 锁的状态保存在对象头中，不需要额外的内存空间；
  
  - 对于没有竞争的情况下，几乎没有额外的性能开销。

- 需要注意的是，轻量级锁并不适用于存在大量线程间竞争的情况，因为在竞争激烈的场景下，轻量级锁会频繁膨胀为重量级锁，这样反而会增加锁操作的开销。因此，在设计并发应用时，需要根据实际情况选择合适的锁机制。

直白讲就是：

- 虚拟机首先将在当前线程的栈帧创建一块空间用于存储锁记录（lock record），把对象头中的Mark Word复制到锁记录中，拷贝成功后，虚拟机将使用CAS操作将对象Mark Word更新为指向Lock Record的指针。（并将Lock Record里的owner指针指向对象的Mark Word）。

- 若操作成功，那就完成了轻量锁操作，把对象头里的tag改成00，表示此对象处于轻量级锁定状态。

- 如果失败，虚拟机首先会检查对象的Mark Word是否指向当前线程的栈帧，如果是就说明当前线程已经拥有了这个对象的锁，那就可以直接进入同步块继续执行；

- 否则说明多个线程在竞争锁：若当前只有一个等待线程，尝试自旋获取锁，自旋超过一定的次数，或者多个线程竞争，则需要在当前对象上生成重量锁。

##### 偏向锁

偏向锁使用场景：冲突很少，基本上就一个线程使用。

轻量级锁在没有竞争时，每次锁重入仍然需要执行 CAS 操作。在 Java6 引入了偏向锁来做进一步优化，只有第一次使用 CAS 时才将线程ID设置到对象的 Mark Word 中，之后锁重入发现这个线程ID是自己的，就表示没有竞争，不用再 CAS。以后只要不发生锁竞争，这个锁对象就归该线程所有。

###### 偏向状态

```ruby
|--------------------------------------------------------------------------------------------------------------|
|                 Mark Word (64 bits)                                                 |   State                |
|-------------------------------------------------------------------------------------|------------------------|
|   unused:25   |hashcode:25  |   unused:1   |   age:4   |   biased_lock:0   |   01   |   Normal               |
|-------------------------------------------------------------------------------------|------------------------|
|   thread:54   |   epoch:2   |   unused:1   |   age:4   |   biased_lock:1   |   01   |   Biased               |
|-------------------------------------------------------------------------------------|------------------------|
|              ptr_to_lock_record:62                                         |   00   |   Lightweight Locked   |
|-------------------------------------------------------------------------------------|------------------------|
|              ptr_to_heavyweight_monitor:62                                 |   10   |   Heavyweight Locked   |
|-------------------------------------------------------------------------------------|------------------------|
|                                                                            |   11   |   Marked for GC        |
|-------------------------------------------------------------------------------------|------------------------|
```

- 如果开启了偏向锁（默认开启），那么对象创建后，Mark Word值为 0x05，即最后3位为 101，这时它的 thread、epoch、age都为0。

- 偏向锁默认是延迟的，不会在程序启动时立即生效，如果想避免延迟，可以使用 VM 参数`-XX:BiasedLockingStartupDelay=0`来禁用延迟。

- 如果没有开启偏向锁（`-XX:UseBiasedLocking=false`），当对象创建后，Mark Word 值为 0x01，即最后3位为 001，这时它的 hashcode、age都为0，第一次用到 hashcode 时才会赋值。

- 只要没有发生锁竞争，对象头 Mark Word 就会一直存储着上一次的线程ID。

###### 撤销

调用对象 hashCode、其它线程使用锁对象、调用 wait/notify 都会导致偏向锁撤销。wait/notify 只有重量级锁才有。

由可偏向锁撤销到不可偏向锁。

偏向锁只有遇到其他线程尝试竞争偏向锁时，持有偏向锁的线程才会释放锁，线程不会主动释放偏向锁。

偏向锁的撤销，需要等待全局安全点（在这个时间点上没有字节码正在执行），它会首先暂停拥有偏向锁的线程，检查持有偏向锁的线程是否处于活动状态（同步代码块是否执行完），不处于活动状态则将对象头设置为无锁状态，线程可以使用CAS竞争锁。处于活动状态，则升级到轻量级锁，唤醒暂停线程继续执行。

###### 批量重偏向

在Java中，批量重偏向是一种优化技术，用于解决偏向锁的不适用场景。当一个线程获取对象的偏向锁后，如果其他线程也想获取该对象的锁，那么需要撤销原有的偏向锁，并将锁状态进行升级，这个过程称为批量重偏向。

批量重偏向的目的是为了避免偏向锁在多线程竞争下的频繁撤销和重偏向操作，从而提高并发性能。在某些场景下，当对象的偏向锁被多个线程竞争时，频繁的偏向锁撤销和重偏向操作会导致性能下降。为了避免这种情况，JVM引入了批量重偏向机制。

具体来说，当一个偏向锁的对象被多个线程竞争时，JVM会在达到一定条件时触发批量重偏向操作。这个条件通常是当对象的偏向锁数量超过一定阈值（超过20次）时，或者当系统经过一段时间后，判断当前偏向锁的竞争情况不再适合偏向锁优化时。

当撤销偏向锁超过阈值（20次）后，会给这些对象加锁时重新偏向至加锁线程。

###### 批量撤销

当撤销偏向锁超过阈值（40次）后，加锁对象会变为不可偏向（无锁状态），新建的对象也是不可偏向的。

即类的实例会变成不可偏向。原因是偏向是指类偏性，而不是对象实例偏向；一个类只能有一个偏向。

##### 锁膨胀

Java锁膨胀是指在多线程环境下，当某个线程获取锁之后，如果其他线程也需要获取相同锁，并且无法立即获取到，就会触发锁膨胀的过程。

Java中锁膨胀主要涉及到两种类型的锁（偏向锁和轻量级锁）：

1. 偏向锁膨胀：偏向锁是为了解决无竞争的情况下的性能问题。当一个线程获取了偏向锁后，如果其他线程也需要获取该锁，就会触发偏向锁膨胀，偏向锁会升级为轻量级锁。

2. 轻量级锁膨胀：当一个线程获取锁时，如果该锁的标记位为轻量级锁，并且有其他线程尝试获取该锁但失败了，那么该锁会膨胀为重量级锁，即锁的实现由CAS操作变为互斥量操作。

锁膨胀的过程涉及到锁的升级，从偏向锁到轻量级锁，再到重量级锁，每一次升级都会增加锁操作的开销，因此在设计并发程序时，应该尽量避免不必要的锁膨胀，以提高程序的性能和并发能力。

锁升级后，解锁操作需按照升级后锁的解锁方式进行解锁。

##### 自旋优化

重量级锁竞争的时候，还可以使用自旋来进行优化。在 Java6 之后自旋锁是自适应的，自旋成功的可能性较高，就会多自旋几次，反之，就会减少自旋次数或者不自旋。Java7之后不能控制是否开启自旋功能。

##### 锁消除

即锁对象没有逃离方法作用域，JVM 会认为可以不用加锁操作。

`-XX:-EliminateLocks` 关闭锁消除优化。

### wait/notify

![](../image/juc_Monitor结构.png)

BLOCKED 是等待锁，而 WAITING 是已经获得锁，但是又放弃了锁。BLOCKED 和 WAITING都处于阻塞状态，不占用CPU时间片。

BLOCKED 线程会在 Owner 线程释放锁时被唤醒。WAITING 线程会在 Owner 线程调用 notify/notifyAll 时被唤醒。WAITING 线程被唤醒后不会立即获得锁，仍需进入 EntryList 重新竞争。

#### API

| API               | 描述                                                               |
| ----------------- | ---------------------------------------------------------------- |
| `obj.wait()`      | 让进入 obj 监视器的线程到 WaitSet 等待。wait() 方法会释放锁。有参 wait(long n) 会被提前唤醒。 |
| `obj.notify()`    | 随机唤醒一个在 obj 上正在 WaitSet 等待的线程。                                   |
| `obj.notifyAll()` | 唤醒全部在 obj 上正在 WaitSet 等待的线程。                                     |

它们都是线程之间进行协作的手段，属于 Object 对象的方法，必须获得此对象，才能调用方法。

#### wait/sleep 区别

1. sleep 是 Thread  方法，wait 是 Object 的方法。

2. sleep 可以不用配合 synchronized 使用，wait 需要和 synchronized 配合使用。

3. sleep 不会释放锁，wait 会释放锁。

wait 无参即无限等待，有参就是有限等待。

sleep(long n) 和 wait(long n) 都是 TIMED_WAITING 状态。

### 同步模式之保护性暂停

Guarded Suspension，用在一个线程等待另一个线程的执行结果：

- 当有一个结果需要从一个线程传递到另一个线程，让这两个线程关联同一个 GuardedObject。

- join、Future 的实现，采用的就是 Guarded Suspension 方式。

- 但是当一个线程传递多个结果到另一个线程时，那么可以使用消息队列（生产者/消费者）

![](../image/juc_保护性暂停.png)

### LockSupport

FutureTask 内部实现采用下面两个方法进行同步的。

```java
// 暂停当前线程
LockSupport.park();

// 恢复某个线程的运行
LockSupport.unpark(暂停线程对象)
```

每个线程都有一个 Parker 对象，由三部分组成`_counter`、`_cond`、`_mutex`。调用 park() 时，`_counter` 减1，调用 unpark() 加1。如果先调用 unpark()，`_counter=1`，那么后调用 park() 时直接减1即可，无需等待阻塞。（0为加锁，1为解锁）

如果调用unpark()时线程在阻塞状态，则唤醒该线程。

调用多次 unpark() 的效果和调用一次 unpark() 一样。

### 线程状态转换

![](../image/juc_线程状态转换.png)

### Lock

#### 活跃性

##### 定位死锁

检测死锁可以使用 jconsole 工具，或者使用 jps 定位进程id，再用 jstack 定位死锁。

##### 死锁

两个线程互相持有对方想要的锁。

##### 活锁

两个线程改变对方的结束条件。

##### 饥饿

生产者生产速度跟不上消费者消费速度。

#### ReentrantLock

ReentrantLock特点：

* 可中断。

* 可以设置超时时间。

* 可以设置公平锁。

* 支持多个条件变量。

与 synchronized 一样，都支持可重入。

基本语法：

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // 临界区
} finally {
    lock.unlock();
}
```

##### 可重入

可重入是指同一个线程如果首次获得了这把锁，可以有权利再次获取这把锁。如果是不可重入锁，那么第二次获得锁时，还是会被锁挡住。

```java
public class App {

    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        m1();
    }

    public static void  m1() {
        lock.lock();
        try {
            m2();
        } finally {
            lock.unlock();
        }
    }
    public static void m2() {
        lock.lock();
        try {
            m3();
        } finally {
            lock.unlock();
        }
    }
    public static void m3() {
        lock.lock();
        try {
            System.out.println("m3");
        } finally {
            lock.unlock();
        }
    }
}
```

##### 可中断

中断处于阻塞等待的线程，防止无限制的阻塞。加锁使用`ReentrantLock.lockInterruptibly()`，别忘了解锁。

##### 锁超时

- `tryLock()`：获取不到锁立即返回false。

- `tryLock(long timeout, TimeUnit unit)`：获取不到锁时，等待指定等待时间，等待时间超时还没获取到锁，则返回false。

##### 公平锁

ReentrantLock 默认是不公平锁。

```java
public ReentrantLock() {
    sync = new NonfairSync();
}

public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

##### 条件变量

wait()和notify()这两个方法主要用于多线程间的协同处理，即控制线程之间的等待、通知、切换及唤醒。而ReentrantLock也支持这样条件变量的能力。

条件变量维护的是一个单向链表的队列，调用await()在队列等待和当条件满足时signal()唤醒。

```java
static ReentrantLock lock = new ReentrantLock();
static Condition condition1 = lock.newCondition();
static Condition condition2 = lock.newCondition();

public static void main(String[] args) {
    new Thread(() -> {
        lock.lock();
        try {
            condition1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }).start();

    new Thread(() -> {
        lock.lock();
        try {
            condition1.signal();
        } finally {
            lock.unlock();
        }
    }).start();
}
```

可以根据业务场景定义多个 Condition，用于存放不同等待事件的线程。

使用流程：

- await 前需要获得锁。

- await 执行后，会释放锁，然后进入 conditionObject 等待。

- await 的线程被唤醒（中断、超时）后，可以重新竞争锁。

- 竞争锁成功后，从 await 后执行。

API：

```java
// 等价 wait、wait(long n)
void await() throws InterruptedException;

boolean await(long time, TimeUnit unit) throws InterruptedException;

boolean awaitUntil(Date deadline) throws InterruptedException;

void awaitUninterruptibly();

// 唤醒线程，等价于 notify、notifyAll
void signal();

void signalAll();
```

## 共享模型之内存

JMM Java Memory Model，它定义了主存（线程共享内存）、工作内存（线程私有内存）的抽象概念。

JMM 体现在以下几个方面：

1. 原子性：保证指令不会受到线程上下文切换的影响。

2. 可见性：保证指令不会受到 CPU 缓存的影响。

3. 有序性：保证指令不会受到 CPU 指令并行优化的影响。

### 原子性

synchronized、Atomic工具类、Lock...

### 可见性

修饰成员变量和静态变量。

一个线程对主存的数据进行修改，但对于另一个线程是不可见的（获取的是缓存中的值）。可以使用`volatile`关键字解决不可见。每次必须到主内存中读取。

synchronized 也能保证数据的可见性、原子性，但 synchronized 属于重量级操作，性能相对较低。被 synchronized 修饰的代码，在开始执行时会加锁，执行完成后会进行解锁，但在一个变量解锁之前，必须先把此变量同步回主存中。synchronized 会强制当前线程读取主内存中的值。

`volatile`不能保证原子性，仅用在一写多读的情况。只能保证看到最新值，无法解决指令交错。

### 有序性

同一个线程内，JVM 在不会改变程序结果的前提下，可以调整语句的执行顺序。这些指令的各个阶段可以通过重排序和组合来实现指令级并行。

比如 double-check-lock 单例模式，JVM 可能会优化为：先将引用地址赋值给 INSTANCE 变量后，再执行构造方法，那么另一个线程进来一看 INSTANCE 不为空了，可能拿到的是一个未初始化完成的单例对象。

在 JDK5 以上的版本 volatile 才会真正生效。`volatile`禁止指令重排序。防止之前的指令重排序，利用了内存屏障（写之后，读之前设置内存屏障）。

synchronized 不保证有序性。

```mermaid
sequenceDiagram
participant t1 as t1 线程
participant num as num=0
participant ready as  volatile ready=false
participant t2 as t2 线程
t1 -->> t1 : num=2
t1 ->> ready : ready=true
Note over t1,ready: 写屏障
Note over num,t2: 读屏障
t2 ->> ready : 读取ready=true
t2 ->> num : 读取num=2
```

### happens-before

happens-before 规定了哪些写操作对其它线程的读操作可见，是一套可见性与有序性的规则总结。

以下方式都可以对共享变量读可见。

1. synchronized。

2. volatile。

3. start() 前对共享变量的修改。

4. 线程结束前的写，对其它线程得知它结束后的读可见。

5. 线程中断前的写。

6. 对成员变量或静态变量的默认值（0、false、null）的写，对其它线程对该变量的读可见。

具有传递性，如果 x (happens-before) > y 并且 y (happens-before) > x，那么有 x (happens-before) > z

## 共享模型之无锁

### CAS

```mermaid
sequenceDiagram
participant t1 as 线程A
participant A as AtomicInteger对象
participant t2 as 线程B
    t1 ->> A : 获取变量 0
    t1 ->> t1 :自增1
    t2 -->> A : 已经修改为 1 了
    t1 ->> A : cas(0, 1) 交换失败

    t1 ->> A : 获取变量 1
    t1 ->> t1 :自增1
    t2 -->> A : 已经修改为 2 了
    t1 ->> A : cas(1, 2) 交换失败

    t1 ->> A : 获取变量 2
    t1 ->> t1 :自增1
    t1 ->> A : cas(1, 2) 交换成功
```

在对数据操作之前，首先会比较当前值跟传入值是否一样，如果一样就更新，否则不执行更新操作直接返回失败状态。

CAS 底层使用的 lock cpmxchg指令（X86架构），在单核或多核CPU下都能够保证 compareAndSwap 的原子性。

在多核状态下，当某个核执行到带有 lock 的指令时，CPU 会让总线锁住，当这个核讲此指令执行完，再开启总线。这个过程中不会被线程的调度机制中断，保证了多个线程对内存操作的准确性，是原子操作。

```java
// compareAndSet

/*
    expect：获取的当前最新值
    update：要修改的值
    工作内存 expect 和 主内存 expect 比较，如果相等则将主内存 expect 替换为 update
*/
public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}

public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);


// 自增
public final int incrementAndGet() {
    return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
}

public final int getAndAddInt(Object var1, long var2, int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

    return var5;
}
/*
    var1：是当前 AtomicInteger 对象
    var2：当前值（value）。（比如现在要实现 2+1）那么 var2 就是2，var4 就是 1
    var5：获取底层当前的值（value），获取最新值

    假设此时只有一个线程操作 AtomicInteger 对象，则 this.compareAndSwapInt(var1, var2, var5, var5 + var4) 的参数如下：
        this.compareAndSwapInt(AtomicInteger, 2, 2, 2+1)
        如果新值（主内存共享）和旧值（工作内存私有）相等，就将2+1写回主内存。在底层达到数据可见性。
    不相等则一直循环，直到相等为止。

*/
```

CAS 操作借助了 volatile 来达到交换并替换的效果。

- CAS 是乐观锁。synchronized 是悲观锁。

- CAS 体现的是无锁并发、无阻塞并发。但如果线程竞争激烈，必然会频繁重试，效率上也会受影响。线程数不超过CPU核心数时才能发挥 CAS 的性能。

CAS 底层是依赖于操作系统层的 CAS 指令。

### 原子整数

#### API

以 AtomicInteger 为例：

```java
AtomicInteger i = new AtomicInteger(0);
// i++
i.getAndIncrement();
// ++i
i.incrementAndGet();
// i--
i.getAndDecrement();
// --i
i.decrementAndGet();
// 等价于 i++，只不过不是加1，而是加指定步长
i.getAndAdd(5);
// 等价于 ++i，只不过不是加1，而是加指定步长
i.addAndGet(5);


// 使用函数式接口返回的结果更新当前值（value），返回更新后的值
i.updateAndGet(item -> {
    return 5;
});

// 使用函数式接口返回的结果更新当前值（value），返回更新前的值
i.getAndUpdate(item -> {
    return 5;
});
```

### 原子引用

- AtomicReference

- AtomicStampedReference：能够感知其它线程修改了共享变量对象，如果修改了，则自己的 cas 操作就算失败，然后再重试，不过 AtomicStampedReference cas 操作时会带一个版本号。

- AtomicMarkableReference：简化版 AtomicStampedReference，使用布尔值标识共享变量是否被修改。

```java
// AtomicStampedReference 基本用法
String obj = "A";
AtomicStampedReference<String> reference = new AtomicStampedReference<String>(obj, 0);

String oldReference = reference.getReference();
String newReference = "C";

int stamp = reference.getStamp();

reference.compareAndSet(oldReference, newReference, stamp, stamp+1);

// -------------------------------------------------------------------

// AtomicMarkableReference 基本用法
AtomicMarkableReference<String> markableReference = new AtomicMarkableReference<>(obj, true);
String oldReference = markableReference.getReference();
String newReference = "C";
markableReference.compareAndSet(oldReference, newReference, true, false);
```

### 原子数组

保护数组的元素变更时不受多线程影响（线程安全）。

原子更新数组的元素 Integer、Long、引用对象。AtomicReferenceArray类也支持比较并交换功能。即多线程下修改数组的元素 Integer、Long、引用对象 也是线程安全的。

- AtomicIntegerArray

- AtomicLongArray

- AtomicReferenceArray

### 字段更新器

利用字段更新器，可以针对对象的某个域（Field，字段）进行原子操作。只能配合 volatile 修饰的字段使用，否则抛异常。`java.lang.IllegalArgumentException: Must be volatile type`

- AtomicReferenceFieldUpdater

- AtomicIntegerFieldUpdater

- AtomicLongFieldUpdater

### 原子累加器

- LongAdder

#### LongAdder 原理

cas锁，伪共享。

##### 域

```java
/**
 * Table of cells. When non-null, size is a power of 2.

    累加单元数组，懒惰初始化，容量是2次幂
 */
transient volatile Cell[] cells;

/**
 * Base value, used mainly when there is no contention, but also as
 * a fallback during table initialization races. Updated via CAS.

    基础值，如果没有竞争，则用 cas 累加
 */
transient volatile long base;

/**
 * Spinlock (locked via CAS) used when resizing and/or creating Cells.

    在 cells 创建或扩容时，置为 1，表示加锁（CAS实现加锁）
 */
transient volatile int cellsBusy;
```

##### 伪共享

伪共享：即一个缓存行加入了多个Cell对象。

`@sun.misc.Contended`作用是防止缓存行伪共享，即一个 Cell 对应一个 缓存行。

```java
@sun.misc.Contended static final class Cell {
    volatile long value;
    Cell(long x) { value = x; }

    // 最重要的方法。用 cas 方式进行累加，cmp表示旧值，val表示新值
    final boolean cas(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
    }
    // ...
}
```

![img.png](../image/juc_longadder_缓存&内存.png)

| 从CPU到 | 大约需要的时钟周期                  |
| ----- | -------------------------- |
| 寄存器   | 1 cycle，（4GHz的PCU约为0.25ns） |
| L1    | 3~4cycle                   |
| L2    | 10~20cycle                 |
| L3    | 40~45cycle                 |
| 内存    | 120~240cycle               |

因为CP与内存的速度差异很大，因此需要靠预读数据至缓存来提升效率。

而缓存以缓存行为单位，每隔缓存行对应一块内存，一般是64byte（8个long）

但是缓存的加入会造成数据副本的产生，即同一份数据会缓存在不同核心的缓存行中。

CPU要保证数据的一致性，如果某个CPU核心更改了数据，那么其它CPU核心对应的整个缓存行必须失效。

![](../image/juc_longadder_缓存行_01.png)

如上图，因为Cell是数组形式，在内存中是连续存储的，一个Cell为24字节（16字节的对象头和8字节的value），因此缓存行可以存下2个Cell对象。那么问题来了：

- Core-0要修改Cell[0]。

- Core-1要修改Cell[1]。

无论谁修改成功，都会导致对象 Core 的缓存行失效。

`@sun.misc.Contended`注解就是用来解决这个问题的。它的原理是在使用此注解的对象或字段的前后各增加128字节大小的padding，从而让CPU将对象预读至缓存时占用不同的缓存行。这样就不会造成对象缓存行的失效。如下图：

![](../image/juc_longadder_缓存行_02.png)

#### LongAdder 源码

```java
LongAdder adder = new subgraph Connector `NIO EndPoint`();
adder.increment();

public void increment() {
    add(1L);
}

public void add(long x) {
    Cell[] as; long b, v; int m; Cell a;
    if ((as = cells) != null || !casBase(b = base, b + x)) {
        boolean uncontended = true;
        if (as == null || (m = as.length - 1) < 0 ||
            (a = as[getProbe() & m]) == null ||
            !(uncontended = a.cas(v = a.value, v + x)))
            longAccumulate(x, null, uncontended);
    }
}
```

```mermaid
graph LR
subgraph 分析 LongAdder.add 方法
  A(当前线程)-->B(cells)
  B --为空--> C(cas base 累加)
  C --成功--> D(return)
  C --失败--> E(longAccumulate)
  B --不为空--> F(当前线程 cell 是否创建)
  F --已创建--> G(cas cell 累加)
  G --成功--> D
  G --失败--> E
  F --未创建--> E
end
```

##### cells 未创建

```mermaid
graph LR
subgraph cells 创建
  E --失败--> A
  A(循环入口)-->B(cells不存在&加锁&未新建)
  B --> C(加锁)
  C --成功--> D(创建 cells 并初始化一个 cell)
  C --失败--> E(cas base 累加)
  D --> R(return)
  E --成功--> R
end
```

```java
final void longAccumulate(long x, LongBinaryOperator fn,
                          boolean wasUncontended) {
    int h;
    if ((h = getProbe()) == 0) {
        ThreadLocalRandom.current(); // force initialization
        h = getProbe();
        wasUncontended = true;
    }
    boolean collide = false;                // True if last slot nonempty
    for (;;) {
        Cell[] as; Cell a; int n; long v;
        if ((as = cells) != null && (n = as.length) > 0) {
            if ((a = as[(n - 1) & h]) == null) {
                if (cellsBusy == 0) {       // Try to attach new Cell
                    Cell r = new Cell(x);   // Optimistically create
                    if (cellsBusy == 0 && casCellsBusy()) {
                        boolean created = false;
                        try {               // Recheck under lock
                            Cell[] rs; int m, j;
                            if ((rs = cells) != null &&
                                (m = rs.length) > 0 &&
                                rs[j = (m - 1) & h] == null) {
                                rs[j] = r;
                                created = true;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                collide = false;
            }
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash
            else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                         fn.applyAsLong(v, x))))
                break;
            else if (n >= NCPU || cells != as)
                collide = false;            // At max size or stale
            else if (!collide)
                collide = true;
            else if (cellsBusy == 0 && casCellsBusy()) {
                try {
                    if (cells == as) {      // Expand table unless stale
                        Cell[] rs = new Cell[n << 1];
                        for (int i = 0; i < n; ++i)
                            rs[i] = as[i];
                        cells = rs;
                    }
                } finally {
                    cellsBusy = 0;
                }
                collide = false;
                continue;                   // Retry with expanded table
            }
            h = advanceProbe(h);
        }

        /*
            cells 未创建：
                cellsBusy == 0：还没有其它线程对其加锁
                cells == as：未新建
                casCellsBusy()：加锁
        */
        else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
            boolean init = false;
            try {                           // Initialize table
                if (cells == as) {
                    // 初始化累加单元数组长度为2
                    Cell[] rs = new Cell[2];

                    // x为要累加的值
                    rs[h & 1] = new Cell(x);

                    // 将初始化的 rs 赋值给成员变量 cells
                    cells = rs;
                    init = true;
                }
            } finally {

                // 解锁
                cellsBusy = 0;
            }
            if (init)
                break;
        }

        /*
            casBase()：cas base 累加
        */
        else if (casBase(v = base, ((fn == null) ? v + x :
                                    fn.applyAsLong(v, x))))
            break;                          // Fall back on using base
    }
}
```

##### cell 未创建

```mermaid
graph LR
subgraph cell 创建
  S --失败--> A
  A(循环入口)-->B(cells存在&cell未创建)
  B --创建cell--> C(加锁)
  C --失败--> A
  C --成功--> S(数组槽位为空)
  S --成功--> R(return)
end
```

```java
final void longAccumulate(long x, LongBinaryOperator fn,
                          boolean wasUncontended) {
    int h;
    if ((h = getProbe()) == 0) {
        ThreadLocalRandom.current(); // force initialization
        h = getProbe();
        wasUncontended = true;
    }
    boolean collide = false;                // True if last slot nonempty
    for (;;) {
        Cell[] as; Cell a; int n; long v;

        /*
            cell未创建
                cells存在但cell未创建（另一个线程）
        */
        if ((as = cells) != null && (n = as.length) > 0) {

            // 如果为null，说明当前线程还没有创建累加单元 cell
            if ((a = as[(n - 1) & h]) == null) {
                if (cellsBusy == 0) {       // Try to attach new Cell
                    // 创建 cell
                    Cell r = new Cell(x);   // Optimistically create

                    // 无锁的情况才尝试加锁
                    if (cellsBusy == 0 && casCellsBusy()) {
                        boolean created = false;
                        try {               // Recheck under lock
                            Cell[] rs; int m, j;

                            /*
                                rs[j = (m - 1) & h] == null：如果返回false，说明数组索引处不为空，有其它线程已经抢先一步占有了，继续循环。
                            */
                            if ((rs = cells) != null &&
                                (m = rs.length) > 0 &&
                                rs[j = (m - 1) & h] == null) {

                                // 将创建的Cell赋值给数组对应索引处
                                rs[j] = r;
                                created = true;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                collide = false;
            }
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash
            else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                         fn.applyAsLong(v, x))))
                break;
            else if (n >= NCPU || cells != as)
                collide = false;            // At max size or stale
            else if (!collide)
                collide = true;
            else if (cellsBusy == 0 && casCellsBusy()) {
                try {
                    if (cells == as) {      // Expand table unless stale
                        Cell[] rs = new Cell[n << 1];
                        for (int i = 0; i < n; ++i)
                            rs[i] = as[i];
                        cells = rs;
                    }
                } finally {
                    cellsBusy = 0;
                }
                collide = false;
                continue;                   // Retry with expanded table
            }
            h = advanceProbe(h);
        }

        /*
            cells 未创建：
                cellsBusy == 0：还没有其它线程对其加锁
                cells == as：未新建
                casCellsBusy()：加锁
        */
        else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
            boolean init = false;
            try {                           // Initialize table
                if (cells == as) {
                    // 初始化累加单元数组长度为2
                    Cell[] rs = new Cell[2];

                    // x为要累加的值
                    rs[h & 1] = new Cell(x);

                    // 将初始化的 rs 赋值给成员变量 cells
                    cells = rs;
                    init = true;
                }
            } finally {

                // 解锁
                cellsBusy = 0;
            }
            if (init)
                break;
        }

        /*
            casBase()：cas base 累加
        */
        else if (casBase(v = base, ((fn == null) ? v + x :
                                    fn.applyAsLong(v, x))))
            break;                          // Fall back on using base
    }
}
```

##### cell 已创建

```mermaid
graph LR
subgraph cell 创建
  G --> A
  A(循环入口)-- cells存在&cell已创建 --> B(cas cell 累加)
  B --成功--> R(return)
  B --失败--> D(数组长度是否超过CPU上限)
  D --是--> E(改变线程对应的cell)
  E --> A
  D --否--> F(加锁)
  F --失败--> E
  F --成功--> G(扩容)
end
```

```java
final void longAccumulate(long x, LongBinaryOperator fn,
                          boolean wasUncontended) {
    int h;
    if ((h = getProbe()) == 0) {
        ThreadLocalRandom.current(); // force initialization
        h = getProbe();
        wasUncontended = true;
    }
    boolean collide = false;                // True if last slot nonempty
    for (;;) {
        Cell[] as; Cell a; int n; long v;

        /*
            cell未创建
                cells存在但cell未创建（另一个线程）
        */
        if ((as = cells) != null && (n = as.length) > 0) {

            // 如果为null，说明当前线程还没有创建累加单元 cell
            if ((a = as[(n - 1) & h]) == null) {
                if (cellsBusy == 0) {       // Try to attach new Cell
                    // 创建 cell
                    Cell r = new Cell(x);   // Optimistically create

                    // 无锁的情况才尝试加锁
                    if (cellsBusy == 0 && casCellsBusy()) {
                        boolean created = false;
                        try {               // Recheck under lock
                            Cell[] rs; int m, j;

                            /*
                                rs[j = (m - 1) & h] == null：如果返回false，说明数组索引处不为空，有其它线程已经抢先一步占有了，继续循环。
                            */
                            if ((rs = cells) != null &&
                                (m = rs.length) > 0 &&
                                rs[j = (m - 1) & h] == null) {

                                // 将创建的Cell赋值给数组对应索引处
                                rs[j] = r;
                                created = true;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                collide = false;
            }
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash

            /*
                cas cell 累加
            */
            else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                         fn.applyAsLong(v, x))))

                // 累加成功
                break;

            /*
                判断数组长度是否在于CPU数
            */    
            else if (n >= NCPU || cells != as)
                collide = false;            // At max size or stale

            /*
                n >= NCPU：如果为true，collide = false
                会进入这个 else if；就不会进入到扩容的else if中了
            */
            else if (!collide)
                collide = true;

            /*
                扩容
            */
            else if (cellsBusy == 0 && casCellsBusy()) {
                try {
                    if (cells == as) {      // Expand table unless stale

                        // 扩容为原来的两倍，默认Cell[]为2
                        Cell[] rs = new Cell[n << 1];
                        for (int i = 0; i < n; ++i)
                            // 旧数组对应下标内容拷贝到新数组中
                            rs[i] = as[i];
                        cells = rs;
                    }
                } finally {
                    // 解锁
                    cellsBusy = 0;
                }
                collide = false;
                continue;                   // Retry with expanded table
            }

            /*
                改变线程对应的cell，即换一个cell累加单元试试
            */
            h = advanceProbe(h);
        }

        /*
            cells 未创建：
                cellsBusy == 0：还没有其它线程对其加锁
                cells == as：未新建
                casCellsBusy()：加锁
        */
        else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
            boolean init = false;
            try {                           // Initialize table
                if (cells == as) {
                    // 初始化累加单元数组长度为2
                    Cell[] rs = new Cell[2];

                    // x为要累加的值
                    rs[h & 1] = new Cell(x);

                    // 将初始化的 rs 赋值给成员变量 cells
                    cells = rs;
                    init = true;
                }
            } finally {

                // 解锁
                cellsBusy = 0;
            }
            if (init)
                break;
        }

        /*
            casBase()：cas base 累加
        */
        else if (casBase(v = base, ((fn == null) ? v + x :
                                    fn.applyAsLong(v, x))))
            break;                          // Fall back on using base
    }
}
```

##### sum

获取最终结果通过sum方法：

```java
public long sum() {
    Cell[] as = cells; Cell a;
    long sum = base;
    if (as != null) {
        for (int i = 0; i < as.length; ++i) {
            if ((a = as[i]) != null)
                sum += a.value;
        }
    }
    return sum;
}
```

简单来说即：原子累加器会在有竞争的情况下创建数组，数组元素对应不同的线程，不同线程的索引都是懒加载的对象。没有竞争情况下，就是单线程只会使用一个long类型。

读源码最难的还是理清作者的逻辑和思路，框架逻辑不见得有这个复杂。

### Unsafe

#### 概述

Unsafe对象提供了非常底层的操作内存、线程发方法。Unsafe对象不饿能直接调用，需要通过反射获取。

```java
try {
    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
    theUnsafe.setAccessible(true);
    Unsafe unsafe = (Unsafe) theUnsafe.get(null);
    System.out.println(unsafe);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### Unsafe CAS操作

## 共享模型之不可变

- 属性用 final 修饰保证了该属性是只读的，不能修改。但如果是引用类型，虽然对象不可变，但其内部的属性是可变的。

- 类用 final 修饰保证了该类中的方法不能被覆盖，该类也无法被继承，防止子类无意间破坏不可变性。

单个方法是线程安全的，但如个其多个方法的组合不一定是线程安全的。

final 也用到了写屏障，final 之前的指令不会跑到 final 之后执行。

### 享元模式

场景：当需要复用数量有限的同一类对象时，如字符串常量池、线程池、包装类的缓存等。

## 并发工具

### 线程池

#### ThreadPoolExecutor

![](../image/juc_线程池继承关系.png)

##### 线程池状态

ThreadPoolExecutor 使用 int 的高3位来表示线程池状态，低29位表示线程数量。这些信息存储在一个原子变量 ctl 中，目的是可以用一次 cas 原子操作进行赋值。

| 状态         | 高3位 | 接收新任务 | 处理阻塞任务 | 描述                     |
| ---------- | --- | ----- | ------ | ---------------------- |
| RUNNING    | 111 | Y     | Y      |                        |
| SHUTDOWN   | 000 | N     | Y      | 不会接收新任务，但会处理阻塞队列剩余任务   |
| STOP       | 001 | N     | N      | 会中断正在执行的任务，并抛弃阻塞队列任务   |
| TIDYING    | 010 | -     | -      | 任务全部执行完毕，活动线程为0，即将进入终结 |
| TERMINATED | 011 | -     | -      | 终结                     |

##### 构造方法

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {}
```

- `corePoolSize`：核心线程数（最多保留的线程数）。

- `maximumPoolSize`：最大线程数（减去核心线程数之后还能创建多少个临时线程）。

- `keepAliveTime`：存活时间，即超过 corePoolSize 的线程多久被回收。

- `unit`：存活时间单位。

- `workQueue`：阻塞队列。

- `threadFactory`：线程工厂，用来创建线程。

- `handler`：拒绝策略。

阻塞队列满了，新添加的任务才会去创建临时线程。最大线程数满了才会执行拒绝策略。

执行流程：

```txt
1. 线程池刚开始没有线程，当一个任务提交给线程池后，线程池会创建一个核心线程来执行该任务。
2. 当线程数达到 corePoolSize 后，这时新添加的任务会被加入到 workQueue 阻塞队列中，等待被线程池执行。
3. 如果 workQueue 是有界队列，那么当阻塞队列中任务数量超过了队列大小时，会创建 maximumPoolSize - corePoolSize 数目的临时线程来执行新添加的任务。
4. 如果线程数达到 maximumPoolSize，且还有新任务添加，这时会触发拒绝策略。JDK 提供了四种拒绝策略的实现：
    AbortPolicy：默认策略。抛 RejectedExecutionException 异常。
    CallerRunsPolicy：调用者线程执行该任务（即调用execute这个方法的线程）
    DiscardPolicy：放弃本次任务
    DiscardOldestPolicy：放弃队列中最早的任务，该任务取而代之。
5. 当高峰过去后，超过 corePoolSize 的临时线程如果在一段时间没有执行任务，则需要结束线程资源。这个时间由 keepAliveTime和unit 控制。
```

##### Executors.newFixedThreadPool()

固定大小线程池：

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}

public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>(),
                                  threadFactory);
}
```

特点：

- 核心线程数 == 最大线程数，没有临时线程。

- 阻塞队列是无界的，可以添加任意数量的任务。

适用于任务量已知，相对耗时的任务。

##### Executors.newCachedThreadPool()

带缓冲线程池：

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}

public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>(),
                                  threadFactory);
}
```

特点：

- 核心线程数是 0，最大线程数是 Integer.MAX_VALUE，临时线程的空闲时间是 60s。临时线程可以无限创建，都是临时线程，可被回收。

- 采用 SynchronousQueue 队列，该队列没有容量。将任务放到该队列中，不能马上返回（阻塞），需要等待另一个线程取出这个任务了，才能返回。最多只能放一个。多用于消息队列。

适用于任务数比较密集，但每个任务执行时间较短的情况。

##### Executors.newSingleThreadExecutor()

单线程的线程池：

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}

public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>(),
                                threadFactory));
}
```

使用场景：希望多个任务串行执行。线程数为 1，任务数超过 1时，会放入无界队列等待。

如果执行过程中发生异常，该线程池还会再创建一个线程，保证线程池的正常工作。

##### 提交任务

```java
// 执行任务
void execute(Runnable command);

// 提交任务 task，可以获取执行的返回值
<T> Future<T> submit(Callable<T> task);

// 提交 tasks 中的任务
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

// 提交 tasks 中的任务，带超时时间
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                          long timeout, TimeUnit unit)
        throws InterruptedException;

// 提交 tasks 中的任务，哪个任务先执行成功，就返回哪个任务的执行结果，其它任务取消
<T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;

// 提交 tasks 中的任务，哪个任务先执行成功，就返回哪个任务的执行结果，其它任务取消，带超时时间
<T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
```

##### 关闭线程池

###### shutdown

- 线程池状态变为 SHUTDOWN。

- 不会接收新任务。

- 已提交的任务会继续执行。

- 不会阻塞调用线程的执行。

```java
public void shutdown() {
  final ReentrantLock mainLock = this.mainLock;
  mainLock.lock();
  try {
      checkShutdownAccess();
      // 线程池状态变为 SHUTDOWN
      advanceRunState(SHUTDOWN);
      // 中断空闲线程
      interruptIdleWorkers();
      onShutdown(); // hook for ScheduledThreadPoolExecutor
  } finally {
      mainLock.unlock();
  }
  // 尝试终结
  tryTerminate();
}
```

###### shutdownNow

- 线程池状态变为 STOP。

- 不会接收新任务。

- 丢弃阻塞队列中的任务。

- 中断正在执行的任务。

```java
public List<Runnable> shutdownNow() {
  List<Runnable> tasks;
  final ReentrantLock mainLock = this.mainLock;
  mainLock.lock();
  try {
      checkShutdownAccess();
      advanceRunState(STOP);
      interruptWorkers();
      tasks = drainQueue();
  } finally {
      mainLock.unlock();
  }
  tryTerminate();
  return tasks;
}
```

##### Worker Thread

创建多少线程合适？：

1. CPU 密集型运算：通常采用 `cpu 核数 + 1` 能够实现最优的 CPU 利用率，+1 是保证当线程由于页缺失故障（操作系统）或其它原因导致暂停时，额外的这个线程就能顶替，保证 CPU 的时钟周期不被浪费。

2. IO 密集型运算：经验公式：`线程数 = 核数 * 期望 CPU 利用率 * 总时间(CPU计算时间+等待时间) / CPU 计算时间`

##### 任务调度线程池

- `ScheduledThreadPoolExecutor.schedule()`：延时执行。

- `ScheduledThreadPoolExecutor.scheduleAtFixedRate()`：定时执行。固定计时。

- `ScheduledThreadPoolExecutor.scheduleWithFixedDelay()`：定时执行，以任务执行完成后开始计时。

线程池不会主动抛异常，需要手动捕获。或者使用 Callable，接收异常（会将异常封装到返回结果中）。

```java
ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

// 延时执行
executor.schedule(() -> {
    System.out.println("ok");
}, 1, TimeUnit.SECONDS);

/*
    第二个参数：（第一次执行）延时多长时间执行；
    第三个参数：（非第一次执行）多久执行一次（频率）；
    第四个参数：时间单位。
*/
executor.scheduleAtFixedRate(() -> {
    log.debug("ok");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}, 2, 1, TimeUnit.SECONDS);

/*
    第二个参数：（第一次执行）延时多长时间执行；
    第三个参数：（非第一次执行）多久执行一次（频率），注意：需等待上一个任务执行完后，才开始计时。
    第四个参数：时间单位。
*/
executor.scheduleWithFixedDelay(() -> {
    log.debug("ok");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}, 2, 1, TimeUnit.SECONDS);
```

###### 定时任务

##### Tomcat 线程池

```mermaid
graph LR
subgraph Connector `NIO EndPoint`
    L(LimitLatch) --> A(Acceptor)
    A --> S1(SocketChannel1)
    A --> S2(SocketChannel1)
    S1 --读事件--> P(Poller)
    S2 --读事件--> P
    P --socketProcessor--> W1(worker1)
    P --socketProcessor--> W2(worker2)
    subgraph Executor
       W1
       W2
    end
end    
```

- LimitLatch 用来限流，可以控制最大连接数，类似 JUC Semaphore。

- Acceptor 负责接收新的 socket 连接。

- Poller 负责监听 socket channel 是否有可读的 IO 事件。

- 一旦有可读事件，封装一个任务对象（socketProcessor），提交给 Executor 线程池处理。

- Executor 线程池中的工作线程负责处理请求。

Tomcat 的线程数如果达到了 maximumPoolSize，并不会立即抛 RejectedExecutionException。而是会再次尝试将任务放入队列中，如果还是失败，才抛 RejectedExecutionException。

Connector 配置

| 配置项                 | 默认值 | 描述                          |
| ------------------- | --- | --------------------------- |
| acceptorThreadCount | 1   | acceptor 线程数                |
| pollerThreadCount   | 1   | poller 线程数                  |
| minSpareThreads     | 10  | 核心线程数，corePoolSize          |
| maxThreads          | 200 | 最大线程数，maximumPoolSize       |
| executor            | -   | Executor 名称，用来自定义线程池，覆盖默认配置 |

Executor 线程配置

| 配置项                     | 默认值               | 描述                    |
|:----------------------- | ----------------- | --------------------- |
| threadPriority          | 5                 | 线程优先级                 |
| daemon                  | true              | 是否守护线程                |
| minSpareThreads         | 25                | 核心线程数，corePoolSize    |
| maxThreads              | 200               | 最大线程数，maximumPoolSize |
| maxIdleTime             | 60000             | 线程存活时间，单位毫秒 ，默认1分钟    |
| maxQueueSize            | Integer.MAX_VALUE | 队列长度                  |
| prestartminSpareThreads | false             | 核心线程是否再服务器启动时启动       |

Tomcat 当提交的任务数超过核心线程数，但没超过最大线程数，Tomcat 就会创建临时线程来处理任务。当提交的任务数超过最大线程数，才会将新任务放入队列。

#### Fork/Join

JDK7 加入的新的线程池实现。体现了分治思想。适用于能够进行任务拆分的 CPU 密集型运算。所谓的任务拆分，是将一个大任务拆分为算法上相同的小任务，直到不能拆分可以直接求解。

Fork/Join 可以把每个任务的分解和合并交给不同的线程来完成。提升运算效率。

Fork/Join 默认会创建与 CPU 核心数相同的线程池。

需要返回值就继承`RecursiveTask`，不需要返回值就继承`RecusiveAction`。`fork()`方法用于拆分任务，`join()`方法用于合并结果。

定义了一个对1~n之间的整数求和的任务：

```java
public class Demo5 {
    public static void main(String[] args) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        forkJoinPool.invoke(new AddTask(1, 10));
    }

    static class AddTask extends RecursiveTask<Integer> {
        int head;
        int last;

        AddTask(int head, int last) {
            this.head = head;
            this.last = last;
        }

        @Override
        protected Integer compute() {

            if (head == last) {
                return head;
            }

            if (head == last - 1) {
                System.out.println(head + "-" + last + "相加为" + (head + last));
                return head + last;
            }

            int mid = (head + last) / 2;

            AddTask addTask = new AddTask(head, mid);

            /*
                分解任务：
                    fork内部最终会执行 java.util.concurrent.RecursiveTask#exec()
                    其exec()方法内部又会调用compute()方法，相当于是递归调用了。直到遇到return为止
            */
            addTask.fork();

            AddTask addTask1 = new AddTask(mid + 1, last);

            // 分解
            addTask1.fork();

            // 合并
            int result = addTask.join() + addTask1.join();
            System.out.println(head + "-" + last + "相加为" + result);
            return result;
        }
    }
}
```

类似二分查找法。深究的话，可以看作是递归调用，递归调用compute()，只不过是多线程下的递归调用。

### JUC

#### AQS

AbstractQueuedSynchronizer，是阻塞式锁和相关同步器工具的框架。

特点：

- 用 state 属性来表示资源的状态（独占模式和共享模式），子类需要定义如何维护这个状态，控制如何获取锁和释放锁。
  
  - `getState()` 获取 state 状态。
  
  - `setState()` 设置  state 状态。
  
  - `compareAndSetState()` cas 机制设置 state 状态。
  
  - 独占模式下（锁）只能有一个线程能够访问资源，而共享模式下可以允许多个线程访问资源。

- 提供基于 FIFO 的等待队列，类似于 Monitor EntryList。

- 支持条件变量来实现等待、唤醒机制，支持多个条件变量，类似于 Monitor WaitSet。

子类主要实现下面几个方法。（调用父类的会抛异常 `UnsupportedOperationException`）

1. `tryAcquire()`

2. `tryRelease()`

3. `tryAcquireShared()`

4. `tryReleaseShared()`

5. `isHeldExclusively()`

示例：

```java
// 如果获取锁失败
if (!tryAcquire(arg)) {
    // 可以选择阻塞当前线程。加入阻塞队列，park()
}

// 如果释放锁成功
if (tryRelease(arg)) {
    // 让阻塞线程恢复运行，unpark()
}
```

##### 自定义锁

```java
@Slf4j
public class App {

    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(() -> {
            lock.lock();
            try {
                log.debug("加锁成功");
            } finally {
                lock.unlock();
            }
        }, "t1").start();

        new Thread(() -> {
            lock.lock();
            try {
                log.debug("加锁成功");
            } finally {
                lock.unlock();
            }
        }, "t2").start();
    }

    /**
     * 自定义锁，不可重入锁
     */
    static class MyLock implements Lock {
        /**
         * 独占锁，同步器类
         */
        @Slf4j
        static class MySync extends AbstractQueuedSynchronizer {
            @Override
            protected boolean tryAcquire(int arg) {
                boolean flag;
                if ((flag = compareAndSetState(0, 1))) {
                    // 加锁成功，并设置 owner  为当前线程
                    setExclusiveOwnerThread(Thread.currentThread());
                }
                log.debug("{}", flag);
                return flag;
            }

            @Override
            protected boolean tryRelease(int arg) {
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }

            /**
             * 是否持有独占锁
             *
             * @return
             */
            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1;
            }

            public Condition newCondition() {
                return new ConditionObject();
            }
        }

        private MySync sync = new MySync();

        @Override
        public void lock() {
            sync.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(time));
        }

        @Override
        public void unlock() {
            sync.release(1);
        }

        @Override
        public Condition newCondition() {
            return sync.newCondition();
        }
    }
}
```

#### ReentrantLock原理

##### 加锁成功

没有发生竞争时：

![](../image/juc_reentrantLock_没有竞争.png)

源码：

```java
private final Sync sync;

// 默认构造
public ReentrantLock() {
    sync = new NonfairSync();
}

public void lock() {
    // lock() 是抽象方法，由子类 NonfairSync、FairSync 实现 
    sync.lock();
}

static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * Performs lock.  Try immediate barge, backing up to normal
     * acquire on failure.
     */
    final void lock() {
        // 是不是很熟悉，和上面自定义锁使用方式一样
        // 将state从0改为1，0是未加锁，1是加锁
        if (compareAndSetState(0, 1))
            // 将owner线程改为当前线程
            setExclusiveOwnerThread(Thread.currentThread());
        else
            acquire(1);
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}
```

##### 加锁失败

![](../image/juc_reentrantLock_有竞争.png)

源码：

```java
private final Sync sync;

// 默认构造
public ReentrantLock() {
    sync = new NonfairSync();
}

public void lock() {
    // lock() 是抽象方法，由子类 NonfairSync、FairSync 实现 
    sync.lock();
}

static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * Performs lock.  Try immediate barge, backing up to normal
     * acquire on failure.
     */
    final void lock() {
        // 是不是很熟悉，和上面自定义锁使用方式一样
        // 将state从0改为1，0是未加锁，1是加锁
        if (compareAndSetState(0, 1))
            // 将owner线程改为当前线程
            setExclusiveOwnerThread(Thread.currentThread());
        else

            // 加锁失败
            acquire(1);
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}
```

`java.util.concurrent.locks.AbstractQueuedSynchronizer#acquire()`：

```java
public final void acquire(int arg) {

    /* 
        尝试加锁，tryAcquire(arg)加锁失败返回法拉瑟，取反未true
        进入acquireQueued()逻辑，加入到阻塞队列
    */
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }

            // parkAndCheckInterrupt() 阻塞线程，等待被unpark()唤醒
            if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            // 调整链表
            cancelAcquire(node);
    }
}

private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```

加锁失败的流程：

1. CAS尝试将state由0改为1，结果失败。

2. 进入tryAcquire()逻辑，这时state已经是1，结果仍然失败。

3. 接下里进入到addWaiter()逻辑，构造Node队列：
   
   - 下图中黄色三角形表示该Node的waitStatus状态，0为默认状态。
   
   - Node的创建是懒惰的。（第一次会创建两个Node）
   
   - 第一个Node称为Dummy（哑元）或哨兵，用来占位，不关联线程。
   
   ![](../image/juc_reentrantLock_有竞争_addWaiter.png)

4. 当前线程再进入到acquireQueued()逻辑：
   
   - 1、acquireQueued()会在自旋中不断尝试获得锁，失败后进入park阻塞：
   
   - 2、如果当前线程紧挨着head（排第二个，即紧跟在Dummy节点后），那么再次tryAcquire()尝试获取锁，当然这时state仍然为1，获取锁失败。
   
   - 3、进入shouldParkAfterFailedAcquire()逻辑，将前驱node（即head）的waitStatus改为-1，返回false。（-1 表示Dummy节点需要唤醒其后继节点）
   
   ![](../image/juc_reentrantLock_有竞争_acquireQueued.png)

5. shouldParkAfterFailedAcquire()执行完后，会再次tryAcquire()尝试获取锁，当然这时state仍为1，获取锁失败。

6. 当再次执行shouldParkAfterFailedAcquire()时，这时因为其前驱node（Dummy节点）的waitStatus为-1，返回true。

7. 进入parkAndCheckInterrupt()逻辑，将`Thread-1`阻塞。（大概4次尝试获取锁时失败才会进入park阻塞状态）

![](../image/juc_reentrantLock_有竞争_park.png)

假设此时有多个线程都经历上述过程且竞争失败，此时状态如下图所示（-1 表示需要唤醒后继节点）：

![](../image/juc_reentrantLock_有竞争_park_n.png)

##### 解锁竞争成功

释放锁源码：

```java
public void unlock() {
    sync.release(1);
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}

private void unparkSuccessor(Node node) {

    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

Thread-0 释放锁，进入tryRelease()流程，如果成功：

- 设置exclusiveOwnerThread为null。

- state = 0。

![](../image/juc_reentrantLock_释放锁_tryRelease.png)

如果释放锁成功，如果当前队列（park阻塞线程的队列）不为null，且head的waitStatus=-1，则会进入unparkSuccessor()逻辑。

找到队列中离head最近的一个Node（没取消的节点），调用unpark()恢复其运行。

Thread-1被唤醒，执行获取锁流程，acquireQueued流程：

![](../image/juc_reentrantLock_释放锁_重新抢占锁.png)

如果加锁成功（没有竞争），则会设置：

- exclusiveOwnerThread设置为Thread-1，state = 1。

- head指向Thread-1所在的Node，该Node清空Thread。

- 将head从链表断开，可被垃圾回收。

##### 解锁竞争失败

如果这时候有其它线程来竞争（非公平锁的体现），假如此时Thread-4抢占锁了：

![](../image/juc_reentrantLock_释放锁_有竞争.png)

1. Thread-4被设置为exclusiveOwnerThread，state = 1。

2. Thread-1再次进入acquireQueued()流程，获取锁失败，重新进入park阻塞。

##### 锁重入

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
    // 锁重入
    final boolean nonfairTryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        // 如果已经获得锁，且owner设置的是当前线程，表示发生锁重入
        else if (current == getExclusiveOwnerThread()) {
            // state++
            int nextc = c + acquires;
            if (nextc < 0) // overflow
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }

    // 继承自父类方法
    // 解锁
    protected final boolean tryRelease(int releases) {
        // state--
        int c = getState() - releases;
        if (Thread.currentThread() != getExclusiveOwnerThread())
            throw new IllegalMonitorStateException();
        boolean free = false;
        // 支持锁重入，当state减为0时，锁才释放成功
        if (c == 0) {
            free = true;
            setExclusiveOwnerThread(null);
        }
        setState(c);
        return free;
    }
}
```

##### 可打断

- 不可打断模式：
  
  - 即使被打断，仍会驻留在AQS队列中，等待获得锁后才能继续运行（只是打断标记设置为true）

```java
public void lock() {
    sync.lock();
}

final void lock() {
    if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread());
    else
        acquire(1);
}

public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))

        // acquireQueued() 返回true说明线程park被打断且获取到锁
        // 重新产生一次中断
        selfInterrupt();
}

private final boolean parkAndCheckInterrupt() {
    // 如果打断标记已经是true，则park会失效
    LockSupport.park(this);
    // Thread.interrupted() 会清除打断标记，下次park还是会阻塞
    return Thread.interrupted();
}

final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;

                // 需要获得锁后，才能返回打断状态
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())

                // 如果 interrupted 被唤醒，打断状态设置为 true
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

- 可打断模式：

```java
public void lockInterruptibly() throws InterruptedException {
    sync.acquireInterruptibly(1);
}

public final void acquireInterruptibly(int arg)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    if (!tryAcquire(arg))
        doAcquireInterruptibly(arg);
}

// 可打断锁的获取锁流程
private void doAcquireInterruptibly(int arg)
    throws InterruptedException {
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())

                // 在 park 过程中如果被 interrupt，会进入if内
                // 抛 InterruptedException 有异常x
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

##### 公平锁

回顾一下非公平锁的实现：

```java
protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
}

final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();

    // 没有其他线程抢占锁
    if (c == 0) {
        // 尝试用cas获取锁，不检查AQS队列，体现了非公平性
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

公平锁实现：

- 与非公平锁的主要区别在于tryAcquire()方法的实现。

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        // 先检查AQS队列中是否有前驱节点，没有才竞争锁
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}

public final boolean hasQueuedPredecessors() {

    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    /*
        h != t 表示队列中有Node
        (s = h.next) == null 表示此时只有一个Dummy节点
        s.thread != Thread.currentThread() Dummy节点的后继节点不是当前线程
    */
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```

##### 条件变量

每个条件变量其实就对应着一个等待队列，其实现类是ConditionObject。

```java
public class ConditionObject implements Condition, java.io.Serializable {

    public final void await() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        // 1. 加入到阻塞队列
        Node node = addConditionWaiter();

        // 2. 释放锁
        int savedState = fullyRelease(node);
        int interruptMode = 0;
        while (!isOnSyncQueue(node)) {
            // 3. park() 阻塞当前线程
            LockSupport.park(this);
            if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                break;
        }
        if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
            interruptMode = REINTERRUPT;
        if (node.nextWaiter != null) // clean up if cancelled
            unlinkCancelledWaiters();
        if (interruptMode != 0)
            reportInterruptAfterWait(interruptMode);
    }

    private Node addConditionWaiter() {
        Node t = lastWaiter;
        // If lastWaiter is cancelled, clean out.
        if (t != null && t.waitStatus != Node.CONDITION) {
            unlinkCancelledWaiters();
            t = lastWaiter;
        }
        Node node = new Node(Thread.currentThread(), Node.CONDITION);
        if (t == null)
            firstWaiter = node;
        else
            t.nextWaiter = node;
        lastWaiter = node;
        return node;
    }

    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)

                // 唤醒后继节点
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    public final void signal() {
        // ownerThread 才有资格唤醒，否则抛异常
        if (!isHeldExclusively())
            throw new IllegalMonitorStateException();
        Node first = firstWaiter;
        if (first != null)
            doSignal(first);
    }

    private void doSignal(Node first) {
        do {
            if ( (firstWaiter = first.nextWaiter) == null)
                lastWaiter = null;
            first.nextWaiter = null;

            // transferForSignal() 转移成功跳出循环
        } while (!transferForSignal(first) &&
                 (first = firstWaiter) != null);
    }

    final boolean transferForSignal(Node node) {

        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        // 加入到AQS队列中，如果成功返回前驱节点，当前例子中则返回Thread-3
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }
}
```

- await流程：
  
  1. Thread-0持有锁，调用await()，进入ConditionObject的addConditionWaiter流程：
     
     - 创建新的Node状态为-2（Node.CONDITION），关联Thread-0，加入到等待队列尾部。
     
     ![](../image/juc_reentrantLock_条件变量_await_addConditionWaiter.png)
  
  2. 接下来进入AQS的fullyRelease()流程，释放同步器上的锁。
  
  ![](../image/juc_reentrantLock_条件变量_await_fullyRelease.png)
  
  - unpark() AQS队列中的下一个节点去竞争锁。假如没有其它线程线程竞争，则Thread-1竞争成功。
    
    ![](../image/juc_reentrantLock_条件变量_await_fullyRelease_unpark.png)
  3. park()阻塞Thread-0。
  
  ![](../image/juc_reentrantLock_条件变量_await_park.png)

- signal流程：

由Thread-1唤醒Thread-0。

进入 ConditionObject 的doSignal()流程，获取等待队列中第一个Node，即Thread-0所在的Node。执行transferForSignal()流程，将该Node假如到AQS队列尾部，将Thread-0的waitStatus设置为0，Thread-3的waitStatus设置为-1。

![](../image/juc_reentrantLock_条件变量_signal_transferForSignal.png)

#### 读写锁原理

##### ReentrantReadWriteLock

> 读读并发，读写互斥，写写互斥

当读操作远高于写操作时，可以使用读写锁让读操作可以并发，提高性能。类似于MySQL中的`select ... from ... lock in share mode`。

```java
ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
ReentrantReadWriteLock.WriteLock rw = rwl.writeLock();
ReentrantReadWriteLock.ReadLock rr = rwl.readLock();
re.lock();
rr.lock();
```

- 注意事项：
  
  - 读锁不支持条件变量。
  
  - 不支持重入时升级：即在持有读锁的情况下去获取写锁，会导致写锁永久等待。
    
    ```java
    r.lock();
    try {
      w.lock();
        try {
    
        } finally {
          w.unlock();
        }
    } finally {
      r.unlock();
    }
    ```
  
  - 支持重入时降级：即在持有写锁的情况下去获取读锁（同一线程）。

读写锁使用的是同一个Sync同步器。因此等待队列、state也是同一个。

源码部分：

```java
// w.lock
protected final boolean tryAcquire(int acquires) {
    Thread current = Thread.currentThread();
    int c = getState();
    int w = exclusiveCount(c);

    // 1. c != 0：写锁
    if (c != 0) {
        // (Note: if c != 0 and w == 0 then shared count != 0)
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
        // Reentrant acquire
        setState(c + acquires);
        return true;
    }
    if (writerShouldBlock() ||
        !compareAndSetState(c, c + acquires))
        return false;
    setExclusiveOwnerThread(current);
    return true;
}

// r.lock()
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    int r = sharedCount(c);
    if (!readerShouldBlock() &&
        r < MAX_COUNT &&
        compareAndSetState(c, c + SHARED_UNIT)) {
        if (r == 0) {
            firstReader = current;
            firstReaderHoldCount = 1;
        } else if (firstReader == current) {
            firstReaderHoldCount++;
        } else {
            HoldCounter rh = cachedHoldCounter;
            if (rh == null || rh.tid != getThreadId(current))
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0)
                readHolds.set(rh);
            rh.count++;
        }
        return 1;
    }
    return fullTryAcquireShared(current);
}

private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            // 获取node的前一个节点
            final Node p = node.predecessor();
            // 判断p是否是Dummy节点
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }

            // shouldParkAfterFailedAcquire(p, node)：把node节点的前驱节点的waitStatus设置为-1
            // 循环三次获取锁还是失败，shouldParkAfterFailedAcquire(p, node)返回true
            // parkAndCheckInterrupt() park 阻塞    
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

- t1 w.lock()：
  
  1. t1成功获取锁，获取锁流程与ReentrantLock相比没有特殊之处，不同的是写锁状态占了state的低16位，而读锁使用的是state的高16位。
  
  ![](../image/juc_reentrantReadWriteLock_t1_w.lock.png)

- t2 r.lock()：
  
  2. t2执行r.lock()，这时进入读锁的acquireShared(1)流程。首先会进入tryAcquireShared()流程。如果有写锁占据，那么tryAcquireShared()返回-1，表示获取锁失败。
     
     - tryAcquireShared()返回值说明：
       
       - -1表示失败。 
       
       - 0表示成功。不会继续唤醒后继节点。
       
       - 大于0表示成功且还有几个后继节点需要唤醒，读写锁返回1。
     
     ![](../image/juc_reentrantReadWriteLock_t2_r.lock.png)
  
  3. 这时会进入doAcquireShared()流程。首先是调用addWaiter()添加节点，不同之处在于节点被设置为Node.SHARED而非Node.EXCLUSIVE。此时t2仍处于活跃状态。
  
  ![](../image/juc_reentrantReadWriteLock_t2_doAcquireShared.png)
  
  4. t2会检查自己的节点是不是Dummy节点的后一个节点。如果是，还会再次调用tryAcquireShared(1)尝试获取锁。
  
  5. 如果还是获取锁失败，则把前驱节点的waitStatus设置为-1。再循环尝试tryAcquireShared(1)获取锁，（总共循环三次）如果还是失败，则会在parkAndCheckInterrupt() park阻塞。
  
  ![](../image/juc_reentrantReadWriteLock_t2_r.lock_park.png)

- t3 r.lock()&t4 w.lock()：
  
  - 假设又有t3加读锁，t4加写锁，这期间t1仍然持有锁，则阻塞队列如下所示：
  
  ![](../image/juc_reentrantReadWriteLock_t3_t4.png)
  
  - 读锁是Node.SHARED，写锁是Node.EXCLUSIVE

t1.unlock()源码部分：

```java
// w.unlock()
public void unlock() {
    sync.release(1);
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

protected final boolean tryRelease(int releases) {
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();

    // 计数-1    
    int nextc = getState() - releases;

    boolean free = exclusiveCount(nextc) == 0;
    if (free)
        setExclusiveOwnerThread(null);
    setState(nextc);
    return free;
}

private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}

// t2唤醒执行
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;

    // 读锁，高16位    
    int r = sharedCount(c);

    // 读锁不应该被阻塞
    if (!readerShouldBlock() &&
        r < MAX_COUNT &&
        compareAndSetState(c, c + SHARED_UNIT)) {
        if (r == 0) {
            firstReader = current;
            firstReaderHoldCount = 1;
        } else if (firstReader == current) {
            firstReaderHoldCount++;
        } else {
            HoldCounter rh = cachedHoldCounter;
            if (rh == null || rh.tid != getThreadId(current))
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0)
                readHolds.set(rh);
            rh.count++;
        }
        return 1;
    }
    return fullTryAcquireShared(current);
}

private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node);
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}

private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break;
    }
}
```

- t1.unlock()：
  
  - 写锁释放会调用sync.release(1)，其内部如果调用tryRelease(1)成功，则同步器状态如下：
  
  ![](../image/juc_reentrantReadWriteLock_t1_w.unlock.png)
  
  - 接下来执行唤醒流程unparkSuccessor()，即让Dummy节点的后继节点恢复运行（唤醒t2）。这时t2在parkAndCheckInterrupt()中恢复运行。
  
  - t2恢复运行后，再执行一次tryAcquireShared(1)，如果成功则让读锁计数+1。
  
  ![](../image/juc_reentrantReadWriteLock_state_1.png)
  
  - 这时t2已经恢复运行了，接下来t2调用setHeadAndPropagate(node, r)，将原head节点移除，将t2所在的节点置为head节点。
  
  ![](../image/juc_reentrantReadWriteLock_setHeadAndPropagate.png)
  
  - 在setHeadAndPropagate方法内部还会检查新head节点的后继节点是不是shared，如果是则调用doReleaseShared()将head的状态从-1置为0，并唤醒其后继节点。此时t3在parkAndCheckInterrupt()中恢复运行。
  
  ![](../image/juc_reentrantReadWriteLock_t3_interrupt.png)
  
  - t3被唤醒后，也会再执行tryAcquireShared(1)，如果成功则让读锁计数+1。重复t2被唤醒时执行的步骤。
  
  ![](../image/juc_reentrantReadWriteLock_t3_setHeadAndPropagate.png)    
  
  - 此时新head节点的后继节点不是shared，因此不会继续唤醒t4所在节点。

t2.unlock()源码部分：

```java
// r.unlock()

public void unlock() {
    sync.releaseShared(1);
}

public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}

protected final boolean tryReleaseShared(int unused) {
    Thread current = Thread.currentThread();
    if (firstReader == current) {
        // assert firstReaderHoldCount > 0;
        if (firstReaderHoldCount == 1)
            firstReader = null;
        else
            firstReaderHoldCount--;
    } else {
        HoldCounter rh = cachedHoldCounter;
        if (rh == null || rh.tid != getThreadId(current))
            rh = readHolds.get();
        int count = rh.count;
        if (count <= 1) {
            readHolds.remove();
            if (count <= 0)
                throw unmatchedUnlockException();
        }
        --rh.count;
    }
    for (;;) {
        int c = getState();
        int nextc = c - SHARED_UNIT;
        if (compareAndSetState(c, nextc))
            // Releasing the read lock has no effect on readers,
            // but it may allow waiting writers to proceed if
            // both read and write locks are now free.
            return nextc == 0;
    }
}

private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break;
    }
}
```

- t2.unlock()、t3.unlock()：
  
  - t2调用unlock()，进入releaseShared(1)，其内部调用tryReleaseShared(1)，但由于计数还不为零。tryReleaseShared()返回false
  
  ![](../image/juc_reentrantReadWriteLock_t2_unlock.png)
  
  - t3调用unlock()，进入releaseShared(1)，其内部调用tryReleaseShared(1)，此时计数为零，进入doReleaseShared()将head节点从-1置为0，并唤醒head后继节点。
  
  ![](../image/juc_reentrantReadWriteLock_t3_unlock.png)
  
  - t4在parkAndCheckInterrupt()中恢复运行，判断t4前驱节点是不是head，如果是，再次执行tryAcquire(1)。此时没有其它线程竞争，获取写锁成功，修改head节点（t4所在节点置为head，原head移除），流程结束。
  
  ![](../image/juc_reentrantReadWriteLock_t4_lock.png)

##### StampedLock

该类是在JDK8引入的，是为了进一步优化读性能。

#### Semaphore

Semaphore（信号量），用来限制同时访问共享资源的线程上限。可以理解为限流。

应用：使用Semaphore限流，在访问高峰期时，让请求线程阻塞，等待高峰期过去再释放许可。只适合限制单机线程数量，并且仅是限制线程数，而不是限制资源数（如连接数，参考Tomcat LimitLatch的实现）

演示：

```java
Semaphore semaphore = new Semaphore(3);
for (int i = 0; i < 10; i++) {
    new Thread(() -> {
        try {
            // 获取信号量
            semaphore.acquire();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放信号量
            semaphore.release();
        }
    }).start();
}
```

##### acquire&release

加锁源码：

```java
public Semaphore(int permits) {
    sync = new NonfairSync(permits);
}

public void acquire() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
    
public final void acquireSharedInterruptibly(int arg)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    if (tryAcquireShared(arg) < 0)
        doAcquireSharedInterruptibly(arg);
}
    
protected int tryAcquireShared(int acquires) {
    return nonfairTryAcquireShared(acquires);
}
    
final int nonfairTryAcquireShared(int acquires) {
    for (;;) {
        int available = getState();
        int remaining = available - acquires;
        if (remaining < 0 ||
            compareAndSetState(available, remaining))
            return remaining;
    }
}
    
private void doAcquireSharedInterruptibly(int arg)
    throws InterruptedException {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}

public void release() {
    sync.releaseShared(1);
}

public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
    
        // AQS中的方法。与ReentrantLock#doReleaseShared()逻辑一致
        doReleaseShared();
        return true;
    }
    return false;
}

protected final boolean tryReleaseShared(int releases) {
    for (;;) {
        int current = getState();
        int next = current + releases;
        if (next < current) // overflow
            throw new Error("Maximum permit count exceeded");
        if (compareAndSetState(current, next))
            return true;
    }
}
```

- 加锁流程：

  - 初始化时，permits（state）为3，但有5个线程来获取资源。
    
  ![](../image/juc_semaphore_acquire_01.png)

  - 假设其中Thread-1、2、4竞争锁成功。而Thread-0、3竞争失败，调用doAcquireSharedInterruptibly()，进入AQS队列park阻塞。
    
  ![](../image/juc_semaphore_acquire_02.png)

  - doAcquireSharedInterruptibly()流程与ReentrantLock中的类似，就不再详述了。
    
  - 此时Thread-4释放permits，其Sync状态如下：
    
  ![](../image/juc_semaphore_acquire_03.png)

  - 接着Thread-0竞争成功，permits再次设置为0，设置Thread-0所在节点为head节点，移除原head节点。unpark()唤醒后继节点Thread-3节点，但permits为0，因此Thread-3在尝试获取锁失败后再次进入park()状态。
    
  ![](../image/juc_semaphore_acquire_04.png)
    
#### CountdownLatch

#### CyclicBarrier

#### ConcurrentHashMap

#### ConcurrentLinkedQueue

#### BlockingQueue

### disruptor

### guava

#### RateLimiter