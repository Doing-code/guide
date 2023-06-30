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
* 进程间通信较为复杂
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
#### Java
* `jps` 查看所以 Java 进程。
* `jstack <PID>`查看指定 Java 进程的所以线程状态
* `jconsole`查看指定 Java 进程中线程的运行情况（可视化界面）

jconsole 远程监控配置：
* 需要以如下配置运行 Java 类或 jar（java -jar）。
```txt
java -Djava.rmi.server.hostname=ip地址 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=连接端口 -Dcom.sun.management.jmxremote.ssl=是否安全连接 -Dcom.sun.management.jmxremote.authenticate=是否认证 Java类或jar
```
* 修改 /etc/hosts 文件将 127.0.0.1 映射至主机名
* 如果需要认证访问，还需要几个步骤（具体Google，就不再赘述）
### 线程 API
1. start()：启动一个新线程，在新的线程中运行 run 方法，但是 start 只是让线程进入就绪状态，并不是立刻执行，CPU的时间片还没分给它。每个线程对象的 start 方法只能调用一次，多次调用会出现 IllegalThreadStateException。
2. run()：新线程启动后会调用的方法。如果在Thread对象中传递了 Runnable 参数，那么线程启动后会调用 Runnable 中的 run 方法。否则默认不执行任何操作。但可以创建 Thread 的子类，来覆盖默认行为。
3. join()：等待线程运行结束。
4. join(long n)：等待线程运行结束，最多等待 n 毫秒。
5. getId()：获取线程的长整型id，id唯一。
6. getPriority()：获取线程优先级。
7. setPriority()：设置线程优先级。Java中线程优先级是1~10的整数，默认优先级5，较大的优先级能提高该线程被CPU调度的几率。
8. getState()：获取线程状态。Java中线程状态使用枚举类表示的。分别为 NEW、RUNNABLE、BLOCKED、WAITING、TIMED_WAITING、TERMINATED。
9. isInterrupted()：判断是否被中断，不会清除中断标记。
10. isAlive()：线程是否存活，表示还没有执行完任务。
11. interrupt()：中断线程，如果被中断的线程正在 sleep、wait、join 会导致被中断的线程抛出 InterruptedException，并清除中断标记；如果中断正在运行的线程，则会设置中断标记；park 的线程被中断，也会设置中断标记。
12. interrupted()：判断当前线程是否被中断，会清除中断标记。
13. sleep(long n)：让当前指执行的线程休眠 n 毫秒，休眠期间会让出 cpu 的时间片（休眠期间不会占用cpu）。
14. yield()：通知线程调度器让出当前线程对 cpu 的使用。
### sleep & yield
#### sleep
1. 调用 sleep 会让当前线程从 Running 进入 Timed Waiting 状态（阻塞）。
2. 其它线程可以使用 interrupt() 方法中断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException。
3. 所谓的中断，就是把正在睡眠的线程"叫醒"。
4. 睡眠结束后的线程未必会立即得到执行（等待分配CPU时间片）。
5. 建议使用 TimeUnit 的 sleep 替代 Thread 的 sleep 来获得更好的可读性。
#### yield
1. 调用 yield 会让当前线程从 Running 进入 Runnable 状态（就绪）。然后调度器执行其它线程。（本身可能也会继续得到）
2. 具体的实现依赖操作系统的任务调度器。
### 线程状态
### 线程运行基础









