# Tomcat

## Tomcat

### 常见的web服务器

```textile
1). webLogic：oracle公司，大型的JavaEE服务器，支持所有的JavaEE规范，收费的。
2). webSphere：IBM公司，大型的JavaEE服务器，支持所有的JavaEE规范，收费的。
3). JBOSS：JBOSS公司的，大型的JavaEE服务器，支持所有的JavaEE规范，收费的。
4). Tomcat：Apache基金组织，中小型的JavaEE服务器，仅仅支持少量的JavaEE规范servlet/jsp。开源的，免费的。
```

### Tomcat架构

#### HTTP工作原理

HTTP协议是浏览器与服务器之间的数据传送协议。作为应用层协议，HTTP是基于TCP/IP协议来传递数据的（HTML文件、图片、查询结果等），HTTP协议不涉及数据包（Packet）传输，主要规定了客户端和服务器之间的通信格式。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_http工作原理.png)

1. 用户通过浏览器进行了一个操作，比如输入网址并回车，或者是点击链接，接着浏览器获取了这个事件。

2. 浏览器向服务端发出TCP连接请求。

3. 服务程序接受浏览器的连接请求，并经过TCP三次握手建立连接。

4. 浏览器将请求数据打包成一个HTTP协议格式的数据包。

5. 浏览器将该数据包推入网络，数据包经过网络传输，最终达到端服务程序。

6. 服务端程序拿到这个数据包后，同样以HTTP协议格式解包，获取到客户端的意图。

7. 得知客户端意图后进行处理，比如提供静态文件或者调用服务端程序获得动态结果。

8. 服务器将响应结果（可能是HTML或者图片等）按照HTTP协议格式打包。

9. 服务器将响应数据包推入网络，数据包经过网络传输最终达到到浏览器。

10. 浏览器拿到数据包后，以HTTP协议的格式解包，然后解析数据，假设这里的数据是HTML。

11. 浏览器将HTML文件展示在页面上。

而Tomcat作为一个HTTP服务器，在这个过程中都做了些什么事情呢？

- 主要是接受连接、解析请求数据、处理请求和发送响应这几个步骤。

#### Tomcat架构

##### Servlet容器工作流程

为了解耦，HTTP服务器不直接调用Servlet，而是把请求交给Servlet容器来处理，那Servlet容器又是怎么工作的呢？

当客户请求某个资源时，HTTP服务器会用一个ServletRequest对象把客户的请求信息封装起来，然后调用Servlet容器的service方法，Servlet容器拿到请求后，根据请求的URL和Servlet的映射关系，找到相应的Servlet。

如果Servlet还没有被加载，就用反射机制创建这个Servlet，并调用Servlet的init方法来完成初始化，接着调用Servlet的service方法来处理请求，把ServletResponse对象返回给HTTP服务器，HTTP服务器会把响应发送给客户端。

> SpringMVC中的DispatcherServlet就是实现了Servlet接口的Servlet。用于请求分发、响应。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Servlet容器工作流程.png)

##### Tomcat整体架构

Tomcat要实现两个核心功能：

1. 处理Socket连接，负责网络字节流与Request和Response对象的转化。

2. 加载和管理Servlet，以及具体处理Request请求。

因此Tomcat设计了两个核心组件连接器（Connector）和容器（Container）来分别做这两件事情。连接器负责对外交流，容器负责内部处理。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_整体架构.png)

#### 连接器-Coyote

##### 架构介绍

Coyote 是Tomcat的连接器框架的名称，是Tomcat服务器提供的供客户端访问的外部接口。客户端通过Coyote与服务器建立连接、发送请求并接受响应 。

Coyote 封装了底层的网络通信（Socket 请求及响应处理），为Catalina 容器提供了统一的接口，使Catalina 容器与具体的请求协议及IO操作方式完全解耦。

Coyote 将Socket 输入转换封装为 Request 对象，交由Catalina 容器进行处理，处理请求完成后, Catalina 通过Coyote 提供的Response 对象将结果写入输出流 。

Coyote 作为独立的模块，只负责具体协议和IO的相关操作，与Servlet 规范实现没有直接关系，因此即便是 Request 和 Response 对象也并未实现Servlet规范对应的接口，而是在Catalina 中将他们进一步封装为ServletRequest 和 ServletResponse 。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Coyote与Catalina的交互过程.png)

##### IO模型与协议

在Coyote中，Tomcat支持的多种I/O模型和应用层协议。Tomcat 支持的IO模型（自8.5/9.0 版本起，Tomcat 移除了 对 BIO 的支持）：

| IO模型 | 描述| 
| ----- | ----| 
| NIO | 非阻塞I/O，采用Java NIO类库实现。| 
| NIO2 | 异步I/O，采用JDK 7最新的NIO2类库实现。| 
| APR | 采用Apache可移植运行库实现，是C/C++编写的本地库。如果选择该方案，需要单独安装APR库。| 

Tomcat 支持的应用层协议 ：

|应用层协议 |描述|
| ----- | ----| 
|HTTP/1.1 |这是大部分Web应用采用的访问协议。|
|AJP |用于和Web服务器集成（如Apache），以实现对静态资源的优化以及集群部署，当前支持AJP/1.3。|
|HTTP/2 |HTTP 2.0大幅度的提升了Web性能。下一代HTTP协议，自8.5以及9.0版本之后支持。|

协议分层 ：

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_协议分层.png)

在 8.0 之前，Tomcat 默认采用的I/O方式为 BIO，之后改为 NIO。无论 NIO、NIO2还是 APR，在性能方面均优于以往的BIO。

而Tomcat为了实现支持多种I/O模型和应用层协议，一个容器可能对接多个连接器，就好比一个房间有多个门。但是单独的连接器或者容器都不能对外提供服务，需要把它们组装起来才能工作，组装后这个整体叫作Service组件。

这里请注意，Service本身没有做什么重要的事情，只是在连接器和容器外面多包了一层，把它们组装在一起。Tomcat内可能有多个Service，这样的设计也是出于灵活性的考虑。

通过在Tomcat中配置多个Service，可以实现通过不同的端口号来访问同一台机器上部署的不同应用。

##### 连接器组件

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_连接器组件.png)

连接器中的各个组件的作用如下：

- EndPoint：

  - Coyote 通信端点，即通信监听的接口，是具体Socket接收和发送处理器，是对传输层的抽象，因此EndPoint用来实现TCP/IP协议的。
  
  - Tomcat 并没有EndPoint 接口，而是提供了一个抽象类AbstractEndpoint，里面定义了两个内部类：Acceptor和SocketProcessor。Acceptor用于监听Socket连接请求。SocketProcessor用于处理接收到的Socket请求。
  
    - 它实现Runnable接口，在Run方法里调用协议处理组件Processor进行处理。为了提高处理能力，SocketProcessor被提交到线程池来执行。而这个线程池叫作执行器（Executor)。

- Processor：

  - Coyote 协议处理接口，如果说EndPoint是用来实现TCP/IP协议的，那么Processor用来实现HTTP协议，Processor接收来自EndPoint的Socket，读取字节流解析成Tomcat Request和Response对象，并通过Adapter将其提交到容器处理，Processor是对应用层协议的抽象。

- ProtocolHandler：Coyote 协议接口，通过Endpoint 和 Processor，实现针对具体协议的处理能力。

  Tomcat 按照协议和I/O 提供了6个实现类：AjpNioProtocol、AjpAprProtocol、AjpNio2Protocol、Http11NioProtocol、Http11Nio2Protocol、Http11AprProtocol。

  在配置`tomcat/conf/server.xml`时，至少要指定具体的ProtocolHandler，当然也可以指定协议名称，如：HTTP/1.1，如果安装了APR，那么将使用Http11AprProtocol，否则使用 Http11NioProtocol。

- Adapter：

  由于协议不同，客户端发过来的请求信息也不尽相同，Tomcat定义了自己的Request类来“存放”这些请求信息。ProtocolHandler接口负责解析请求并生成Tomcat Request类。
  
  但是这个Request对象不是标准的ServletRequest，也就意味着，不能用TomcatRequest作为参数来调用容器。Tomcat设计者的解决方案是引入CoyoteAdapter，这是适配器模式的经典运用。
  
  连接器调用CoyoteAdapter的service方法，传入的是TomcatRequest对象，CoyoteAdapter负责将Tomcat Request转成ServletRequest，再调用容器的Service方法。

#### 容器-Catalina

Tomcat是一个由一系列可配置的组件构成的Web容器，而Catalina是Tomcat的servlet容器。

Catalina 是Servlet 容器实现，包含了之前讲到的所有的容器组件，以及安全、会话、集群、管理等Servlet 容器架构的各个方面。它通过松耦合的方式集成Coyote，以完成按照请求协议进行数据读写。同时，它还包括我们的启动入口、Shell程序等。

> 比如SpringMVC DispatcherServlet也是Servlet的实现，也是一个Servlet容器

##### Tomcat 的模块分层结构

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_模块分层.png)

Tomcat 本质上就是一款 Servlet 容器，因此Catalina 才是 Tomcat 的核心，其他模块都是为Catalina 提供支撑的。比如：通过Coyote 模块提供链接通信，Jasper 模块提供JSP引擎，Naming 提供JNDI 服务，Juli 提供日志服务。

> catalina不是代表服务器，而是作为服务器的一部分，同时又管理着整个服务器。
>
> `Socket -> Coyote -> Catalina -> Server -> Service -> connector -> container -> Servlet -> doGet,doPost`。

- Service包含连接器和容器；连接器处理对外负责连接Socket，容器加载管理Servlet，对内负责，Catalina是Servlet容器。

- Coyote是Tomcat连接器框架名称，封装了Socket为Request交给Catalina容器，Catalina容器处理请求后返回Respose对象。

- Catalina解析Tomcat配置文件来创建Server组件，根据命令对其管理。Server代表服务器，可以有多个。Service是Server的内部组件，两者是一般是多对一的关系。

- Service将多个Connector组件绑定到一个Container(Engine)上。Service包含Connector，Container。

- Connector接口组件和它包含的组件是父子（实现）关系，最底层组件是Wrapper组件，Wrapper包装Servlet

##### Catalina结构

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Catalina结构.png)

如上图所示，Catalina负责管理Server，而Server表示着整个服务器。Server下面有多个服务Service，每个服务都包含着多个连接器组件Connector（Coyote 实现）和一个容器组件Container。在Tomcat 启动的时候，会初始化一个Catalina的实例。

Catalina 各个组件的职责：

| 组件 | 职责|
| ---- | ----|
| Catalina | 负责解析Tomcat的配置文件，以此来创建服务器Server组件，并根据命令来对其进行管理|
| Server | 服务器表示整个Catalina Servlet容器以及其它组件，负责组装并启动Servlet引擎、Tomcat连接器。Server通过实现Lifecycle接口，提供了一种优雅的启动和关闭整个系统的方式|
| Service | 服务是Server内部的组件，一个Server包含多个Service。它将若干个Connector组件绑定到一个Container（Engine）上|
| Connector | 连接器，处理与客户端的通信，它负责接收客户请求，然后转给相关的容器处理，最后向客户返回响应结果|
| Container | 容器，**负责处理用户的servlet请求，并返回对象给web用户的模块**|

##### Container结构

Tomcat设计了4种容器，分别是Engine、Host、Context和Wrapper。这4种容器不是平行关系，而是父子关系。Tomcat通过一种分层的架构，使得Servlet容器具有很好的灵活性。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Container分层结构.png)

各个组件的含义 ：

| 容器 | 描述|
| ---- | ----|
| Engine | 表示整个Catalina的Servlet引擎，用来管理多个虚拟站点，一个Service最多只能有一个Engine，但是一个引擎可包含多个Host|
| Host | 代表一个虚拟主机，或者说一个站点，可以给Tomcat配置多个虚拟主机地址，而一个虚拟主机下可包含多个Context|
| Context | 表示一个Web应用程序，一个Web应用可包含多个Wrapper|
| Wrapper | **表示一个Servlet**，Wrapper 作为容器中的最底层，不能包含子容器|

Tomcat的server.xml配置文件格式，Tomcat采用了组件化的设计，它的构成组件都是可配置的，其中最外层的是Server，其他组件按照一定的格式要求配置在这个顶层容器中。

```xml
<Server>
    <Service>
        <Connector>
        </Connector>
        <Engine>
            <Host>
                <Context></Context>
            </Host>
        </Engine>
    </Service>
</Server>
```

那么，Tomcat是怎么管理这些容器的呢？这些容器具有父子关系，形成一个树形结构。Tomcat采用组合模式来管理这些容器的。

具体实现方法是，**所有容器组件都实现了Container接口**，因此组合模式可以使得用户对**单容器对象**和**组合容器对象**的使用具有一致性。

这里**单容器对象**指的是最底层的Wrapper，**组合容器对象**指的是上面的Context、Host或者Engine。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Container容器组件之间的关系.png)

Container接口扩展了LifeCycle接口，LifeCycle接口用来统一管理各组件的生命周期。

#### Tomcat启动流程

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_启动流程.png)

启动流程步骤：

1. 启动tomcat ， 需要调用 bin/startup.bat (在linux 目录下 , 需要调用 bin/startup.sh)，在startup.bat 脚本中, 调用了catalina.bat。

2. 在catalina.bat 脚本文件中，调用了BootStrap 中的main方法。

3. 在BootStrap 的main 方法中调用了 init 方法 ， 来创建Catalina 及 初始化类加载器。

4. 在BootStrap 的main 方法中调用了 load 方法 ， 在其中又调用了Catalina的load方法。

5. 在Catalina 的load 方法中 , 需要进行一些初始化的工作, 并需要构造Digester 对象, 用于解析 XML。

6. 然后在调用后续组件的初始化操作 。。。

加载Tomcat的配置文件，初始化容器组件 ，监听对应的端口号，准备接受客户端请求。

##### Lifecycle

由于所有的组件均存在初始化、启动、停止等生命周期方法，拥有生命周期管理的特性，所以Tomcat在设计的时候，基于生命周期管理抽象成了一个接口 Lifecycle，而组件 Server、Service、Container、Executor、Connector 组件，都实现了一个生命周期的接口，从而具有了生命周期中的核心方法：

1. init()：初始化组件

2. start()：启动组件

3. stop()：停止组件

4. destroy()：销毁组件

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Lifecycle及组件依赖关系.png)

##### 各组件的默认实现

Server、Service、Engine、Host、Context都是接口，下图中罗列了这些接口的默认实现类。当前对于 Endpoint组件来说，在Tomcat中没有对应的Endpoint接口，但是有一个抽象类 AbstractEndpoint，其下有三个实现类：NioEndpoint、Nio2Endpoint、AprEndpoint，这三个实现类，分别对应于连接接器 Coyote支持的三种IO模型：NIO，NIO2，APR。Tomcat8.5版本中，默认采用的是 NioEndpoint。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_组件的默认实现.png)

ProtocolHandler：Coyote协议接口，通过封装Endpoint和Processor，实现针对具体协议的处理功能。Tomcat按照协议和IO提供了6个实现类。

AJP协议（Apache JServ Protocol）：

1. AjpNioProtocol ：采用NIO的IO模型。

2. AjpNio2Protocol：采用NIO2的IO模型。

3. AjpAprProtocol ：采用APR的IO模型，需要依赖于APR库。

HTTP协议：

1. Http11NioProtocol ：采用NIO的IO模型，默认使用的协议（如果服务器没有安装APR）。

2. Http11Nio2Protocol：采用NIO2的IO模型。

3. Http11AprProtocol ：采用APR的IO模型，需要依赖于APR库。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_protocol实现类.png)

##### 总结

Tomcat的启动过程非常标准化，统一按照生命周期管理接口Lifecycle的定义进行启动。首先调用init() 方法进行组件的逐级初始化操作，然后再调用start()方法进行启动。

每一级的组件除了完成自身的处理外，还要负责调用子组件响应的生命周期管理方法，组件与组件之间是松耦合的，因为我们可以很容易的通过配置文件进行修改和替换。

#### Tomcat请求处理流程

设计了这么多层次的容器，Tomcat是怎么确定每一个请求应该由哪个Wrapper容器里的Servlet来处理的呢？答案是，Tomcat是用**Mapper组件**来完成这个任务的。

Mapper组件的功能就是将用户请求的URL定位到一个Servlet，它的工作原理是：Mapper组件里保存了Web应用的配置信息，其实就是容器组件与访问路径的映射关系，比如Host容器里配置的域名、Context容器里的Web应用路径，以及Wrapper容器里Servlet映射的路径，你可以想象这些配置信息就是一个多层次的Map。

当一个请求到来时，Mapper组件通过解析请求URL里的域名和路径，再到自己保存的Map里去查找，就能定位到一个Servlet。但请注意，一个请求URL最后只会定位到一个Wrapper容器，也就是一个Servlet。

下面的示意图中 ， 就描述了 当用户请求链接 `http://localhost:8080/bbs/findAll` 之后, 是如何找到最终处理业务逻辑的servlet。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_请求流程_定位Wrapper示意图.png)

上图中只是描述了根据请求的URL如何查找到需要执行的Servlet，那么接下来再来解析一下，从Tomcat的设计架构层面来分析Tomcat的请求处理。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_Tomcat的设计架构层面来分析Tomcat的请求处理.png)

步骤如下：

1. Connector组件Endpoint中的Acceptor监听客户端套接字连接并接收Socket。

2. 将连接交给线程池Executor处理，开始执行请求响应任务。

3. Processor组件读取消息报文，解析请求行、请求体、请求头，封装成Request对象。

4. Mapper组件根据请求行的URL值和请求头的Host值匹配由哪个Host容器、Context容器、Wrapper容器处理请求。

5. CoyoteAdaptor组件负责将Connector组件和Engine容器关联起来，把生成的Request对象和响应对象Response传递到Engine容器中，调用 Pipeline。

6. Engine容器的管道开始处理，管道中包含若干个Valve、每个Valve负责部分处理逻辑。执行完Valve后会执行基础的 Valve--StandardEngineValve，负责调用Host容器的Pipeline。

7. Host容器的管道开始处理，流程类似，最后执行 Context容器的Pipeline。

8. Context容器的管道开始处理，流程类似，最后执行 Wrapper容器的Pipeline。

9. Wrapper容器的管道开始处理，流程类似，最后执行 Wrapper容器对应的Servlet对象的处理方法。

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_责任链请求处理.png)

Tomcat中的各个组件各司其职，组件之间松耦合，确保了整体架构的可伸缩性和可拓展性，那么在组件内部，如何增强组件的灵活性和拓展性呢？在Tomcat中，每个Container组件采用责任链模式来完成具体的请求处理。

在Tomcat中定义了Pipeline 和 Valve 两个接口，Pipeline 用于构建责任链，后者代表责任链上的每个处理器。Pipeline 中维护了一个基础的Valve，它始终位于Pipeline的末端（最后执行），封装了具体的请求处理和输出响应的过程。

当然，我们也可以调用addValve()方法，为Pipeline 添加其他的Valve，后添加的Valve 位于基础的Valve之前，并按照添加顺序执行。Pipeline通过获得首个Valve来启动整合链条的执行 。

### Tomcat服务器配置

Tomcat 服务器的配置主要集中于 tomcat/conf 下的 catalina.policy、catalina.properties、context.xml、server.xml、tomcat-users.xml、web.xml 文件。

server.xml 是tomcat 服务器的核心配置文件，包含了Tomcat的 Servlet 容器（Catalina）的所有配置。

#### Server

Server是server.xml的根元素，用于创建一个Server实例，默认使用的实现类是`org.apache.catalina.core.StandardServer`。

```xml
<Server port="8005" shutdown="SHUTDOWN">
  <!--  用于以日志形式输出服务器 、操作系统、JVM的版本信息-->
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />

  <!--  用于加载（服务器启动） 和 销毁 （服务器停止） APR。 如果找不到APR库， 则会输出日志， 并不影响Tomcat启动-->
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />

  <!--  用于避免JRE内存泄漏问题-->
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />

  <!--  用户加载（服务器启动） 和 销毁（服务器停止） 全局命名服务-->
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />

  <!--  用于在Context停止时重建Executor 池中的线程， 以避免ThreadLocal 相关的内存泄漏-->
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
</Server>
```

- port : Tomcat 监听的关闭服务器的端口。（该端口用于处理Tomcat Web服务器的SHUTDOWN命令。当Tomcat服务器需要关闭时，可以通过向该端口发送SHUTDOWN命令来关闭服务器。默认情况下，此端口只能在本地访问）

- shutdown： 关闭服务器的指令字符串。

- Server内嵌的子元素为 Listener、GlobalNamingResources、Service。

- 默认配置的5个Listener。

`GlobalNamingResources`标签中定义了全局命名服务：

```xml
<!-- Global JNDI resources
   Documentation at /docs/jndi-resources-howto.html
-->
<GlobalNamingResources>
<!-- Editable user database that can also be used by
     UserDatabaseRealm to authenticate users
-->
<Resource name="UserDatabase" auth="Container"
          type="org.apache.catalina.UserDatabase"
          description="User database that can be updated and saved"
          factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
          pathname="conf/tomcat-users.xml" />
</GlobalNamingResources>
```

#### Service

该元素用于创建 Service 实例，默认使用 `org.apache.catalina.core.StandardService`。默认情况下，Tomcat 仅指定了Service 的名称，值为 "Catalina"。Service 可以内嵌的元素为：Listener、Executor、Connector、Engine。

其中Listener 用于为Service添加生命周期监听器，Executor 用于配置Service 共享线程池，Connector 用于配置Service 包含的连接器，Engine 用于配置Service中链接器对应的Servlet 容器引擎。

```xml
<Service name="Catalina">
<!--The connectors can use a shared executor, you can define one or more named thread pools-->
<!--
<Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
    maxThreads="150" minSpareThreads="4"/>
-->


<!-- A "Connector" represents an endpoint by which requests are received
     and responses are returned. Documentation at :
     Java HTTP Connector: /docs/config/http.html
     Java AJP  Connector: /docs/config/ajp.html
     APR (HTTP/AJP) Connector: /docs/apr.html
     Define a non-SSL/TLS HTTP/1.1 Connector on port 8080
-->
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
<!-- A "Connector" using the shared thread pool-->
<!--
<Connector executor="tomcatThreadPool"
           port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
-->
<!-- Define an SSL/TLS HTTP/1.1 Connector on port 8443
     This connector uses the NIO implementation. The default
     SSLImplementation will depend on the presence of the APR/native
     library and the useOpenSSL attribute of the
     AprLifecycleListener.
     Either JSSE or OpenSSL style configuration may be used regardless of
     the SSLImplementation selected. JSSE style configuration is used below.
-->
<!--
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/localhost-rsa.jks"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
-->
<!-- Define an SSL/TLS HTTP/1.1 Connector on port 8443 with HTTP/2
     This connector uses the APR/native implementation which always uses
     OpenSSL for TLS.
     Either JSSE or OpenSSL style configuration may be used. OpenSSL style
     configuration is used below.
-->
<!--
<Connector port="8443" protocol="org.apache.coyote.http11.Http11AprProtocol"
           maxThreads="150" SSLEnabled="true" >
    <UpgradeProtocol className="org.apache.coyote.http2.Http2Protocol" />
    <SSLHostConfig>
        <Certificate certificateKeyFile="conf/localhost-rsa-key.pem"
                     certificateFile="conf/localhost-rsa-cert.pem"
                     certificateChainFile="conf/localhost-rsa-chain.pem"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
-->

<!-- Define an AJP 1.3 Connector on port 8009 -->
<!--
<Connector protocol="AJP/1.3"
           address="::1"
           port="8009"
           redirectPort="8443" />
-->

<!-- An Engine represents the entry point (within Catalina) that processes
     every request.  The Engine implementation for Tomcat stand alone
     analyzes the HTTP headers included with the request, and passes them
     on to the appropriate Host (virtual host).
     Documentation at /docs/config/engine.html -->

<!-- You should set jvmRoute to support load-balancing via AJP ie :
<Engine name="Catalina" defaultHost="localhost" jvmRoute="jvm1">
-->
<Engine name="Catalina" defaultHost="localhost">

  <!--For clustering, please take a look at documentation at:
      /docs/cluster-howto.html  (simple how to)
      /docs/config/cluster.html (reference documentation) -->
  <!--
  <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"/>
  -->

  <!-- Use the LockOutRealm to prevent attempts to guess user passwords
       via a brute-force attack -->
  <Realm className="org.apache.catalina.realm.LockOutRealm">
    <!-- This Realm uses the UserDatabase configured in the global JNDI
         resources under the key "UserDatabase".  Any edits
         that are performed against this UserDatabase are immediately
         available for use by the Realm.  -->
    <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
           resourceName="UserDatabase"/>
  </Realm>

  <Host name="localhost"  appBase="webapps"
        unpackWARs="true" autoDeploy="true">

    <!-- SingleSignOn valve, share authentication between web applications
         Documentation at: /docs/config/valve.html -->
    <!--
    <Valve className="org.apache.catalina.authenticator.SingleSignOn" />
    -->

    <!-- Access log processes all example.
         Documentation at: /docs/config/valve.html
         Note: The pattern used is equivalent to using pattern="common" -->
    <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
           prefix="localhost_access_log" suffix=".txt"
           pattern="%h %l %u %t &quot;%r&quot; %s %b" />

  </Host>
</Engine>
</Service>
```

#### Executor

默认情况下，在Catalina执行`Catalina#start`会启动StandardThreadExecutor。如果我们想添加一个线程池，可以在下添加如下配置：

```xml
<Executor name="tomcatThreadPool"
    namePrefix="catalina‐exec‐"
    maxThreads="200"
    minSpareThreads="100"
    maxIdleTime="60000"
    maxQueueSize="Integer.MAX_VALUE"
    prestartminSpareThreads="false"
    threadPriority="5"
    className="org.apache.catalina.core.StandardThreadExecutor"/>
```

| 属性 | 含义|
| ---- | ----|
| name | 线程池名称，用于 Connector中指定。|
| namePrefix | 所创建的每个线程的名称前缀，一个单独的线程名称为namePrefix+threadNumber。|
| maxThreads | 池中最大线程数。|
| minSpareThreads | 活跃线程数，也就是核心池线程数，这些线程不会被销毁，会一直存在。|
| maxIdleTime | 线程空闲时间，超过该时间后，空闲线程会被销毁，默认值为6000（1分钟），单位毫秒。|
| maxQueueSize | 在被执行前最大线程排队数目，默认为Int的最大值，也就是广义的无限。除非特殊情况，这个值不需要更改，否则会有请求不会被处理的情况发生。|
| prestartminSpareThreads | 启动线程池时是否启动 minSpareThreads部分线程。默认值为false，即不启动。|
| threadPriority | 线程池中线程优先级，默认值为5，值从1到10。|
| className | 线程池实现类，未指定情况下，默认实现类为`org.apache.catalina.core.StandardThreadExecutor`。如果想使用自定义线程池首先需要实现`org.apache.catalina.Executor`接口。|

如果不配置共享线程池，那么Catalina 各组件在用到线程池时会独立创建。

#### Connector

Connector 用于创建链接器实例。默认情况下，server.xml 配置了两个链接器，一个支持HTTP协议，一个支持AJP协议。因此大多数情况下，我们并不需要新增链接器配置，只是根据需要对已有链接器进行优化。

```xml
<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />

<Connector protocol="AJP/1.3"
           address="::1"
           port="8009"
           redirectPort="8443" />
```

属性说明：

- port：端口号，Connector 用于创建服务端Socket 并进行监听，以等待客户端请求链接。如果该属性设置为0，Tomcat将会随机选择一个可用的端口号给当前Connector使用。

- protocol：当前Connector 支持的访问协议。默认为 HTTP/1.1，并采用自动切换机制选择一个基于 JAVA NIO 的链接器或者基于本地APR的链接器（根据本地是否含有Tomcat的本地库判定）。

  如果不希望采用上述自动切换的机制，而是明确指定协议，可以使用以下值。
  
  Http协议：
  
  ```text
  org.apache.coyote.http11.Http11NioProtocol ， 非阻塞式 Java NIO 链接器
  org.apache.coyote.http11.Http11Nio2Protocol ， 非阻塞式 JAVA NIO2 链接器
  org.apache.coyote.http11.Http11AprProtocol ， APR 链接器
  ```
  
  AJP协议：
  
  ```text
  org.apache.coyote.ajp.AjpNioProtocol ， 非阻塞式 Java NIO 链接器
  org.apache.coyote.ajp.AjpNio2Protocol ，非阻塞式 JAVA NIO2 链接器
  org.apache.coyote.ajp.AjpAprProtocol ， APR 链接器
  ```

- connectionTimeOut：Connector 接收链接后的等待超时时间，单位为毫秒。-1表示不超时。

- SSLEnabled：开启对SSL的支持。

- redirectPort：若接收到了一个请求，并且也符合security-constraint 约束，需要SSL传输，Catalina自动将请求重定向到指定的端口。

- executor：指定共享线程池的名称，也可以通过maxThreads、minSpareThreads等属性配置内部线程池。

- URIEncoding：用于指定编码URI的字符编码，Tomcat8.x版本默认的编码为 UTF-8，Tomcat7.x版本默认为ISO-8859-1。

- ......

```xml
<Connector port="8080"
    protocol="HTTP/1.1"
    executor="tomcatThreadPool"
    maxThreads="1000"
    minSpareThreads="100"
    acceptCount="1000"
    maxConnections="1000"
    connectionTimeout="20000"
    compression="on"
    compressionMinSize="2048"
    disableUploadTimeout="true"
    redirectPort="8443"
    URIEncoding="UTF‐8" />
```

#### Engine

Engine 作为Servlet 的顶级元素，内部可以嵌入：Cluster、Listener、Realm、Valve和Host。

```xml
<Engine name="Catalina" defaultHost="localhost"></Engine>
```

属性说明：

1. name：用于指定Engine 的名称， 默认为Catalina。该名称会影响一部分Tomcat的存储路径（如临时文件）。

2. defaultHost：默认使用的虚拟主机名称，当客户端请求指向的主机无效时，将交由默认的虚拟主机处理，默认为localhost。

#### Host

Host 元素用于配置一个虚拟主机，它支持以下嵌入元素：Alias、Cluster、Listener、Valve、Realm、Context。如果在Engine下配置Realm，那么此配置将在当前Engine下的所有Host中共享。

同样，如果在Host中配置Realm，则在当前Host下的所有Context中共享。Context中的Realm优先级 > Host 的Realm优先级 > Engine中的Realm优先级。

```xml
<Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">
</Host>
```

属性说明：

1. name: 当前Host通用的网络名称，必须与DNS服务器上的注册信息一致。Engine中包含的Host必须存在一个名称与Engine的defaultHost设置一致。

2. appBase：当前Host的应用基础目录，当前Host上部署的Web应用均在该目录下（可以是绝对目录，相对路径）。默认为webapps。

3. unpackWARs：设置为true，Host在启动时会将appBase目录下war包解压为目录。设置为false，Host将直接从war文件启动。

4. autoDeploy：控制tomcat是否在运行时定期检测并自动部署新增或变更的web应用。

通过给Host添加别名，可以实现同一个Host拥有多个网络名称，配置如下：

```xml
<Host name="www.web1.com" appBase="webapps" unpackWARs="true"
    autoDeploy="true">
    <Alias>www.web2.com</Alias>
</Host>
```

这个时候，我们就可以通过两个域名访问当前Host下的应用（需要确保DNS或hosts中添加了域名的映射配置）。

#### Context

Context 用于配置一个Web应用，默认的配置如下：

```xml
<Context docBase="myApp" path="/myApp">
....
</Context>
```

属性说明：

1. docBase：Web应用目录或者War包的部署路径。可以是绝对路径，也可以是相对于Host appBase的相对路径。

2. path：Web应用的Context 路径。如果我们Host名为localhost，则该web应用访问的根路径为：`http://localhost:8080/myApp/` 。

它支持的内嵌元素为：CookieProcessor， Loader， Manager，Realm，Resources，WatchedResource，JarScanner，Valve。

```xml
<Host name="www.tomcat.com" appBase="webapps" unpackWARs="true" 
    autoDeploy="true">

    <Context docBase="D:\servlet_project03" path="/myApp"></Context>

    <Valve className="org.apache.catalina.valves.AccessLogValve"
        directory="logs"
        prefix="localhost_access_log" suffix=".txt"
        pattern="%h %l %u %t &quot;%r&quot; %s %b" />

</Host>
```

#### tomcat-users.xml

该配置文件中，主要配置的是Tomcat的用户，角色等信息，用来控制Tomcat中manager，host-manager的访问权限。

### Web应用配置

web.xml 是web应用的描述文件，它支持的元素及属性来自于Servlet 规范定义。在Tomcat 中，Web 应用的描述信息包括`tomcat/conf/web.xml`中默认配置以及Web应用`WEB-INF/web.xml`下的定制配置。

#### ServletContext 初始化参数

我们可以通过 添加ServletContext 初始化参数，它配置了一个键值对，这样我们可以在应用程序中使用 javax.servlet.ServletContext.getInitParameter()方法获取参数。

contextConfigLocation：名称是固定的，表示自定义的配置文件的路径

```xml
<context‐param>
    <param‐name>contextConfigLocation</param‐name>
    <param‐value>classpath:applicationContext‐*.xml</param‐value>
    <description>Spring Config File Location</description>
</context‐param>
```

#### 会话配置

session：浏览器与服务器建立的一次连接。

`<session‐config>`用于配置Web应用会话，包括超时时间、Cookie配置以及会话追踪模式。它将覆盖server.xml 和 context.xml 中的配置。

```xml
<session‐config>
    <session‐timeout>30</session‐timeout>
    <cookie‐config>
        <name>JESSIONID</name>
        <domain>www.test001.cn</domain>
        <path>/</path>
        <comment>Session Cookie</comment>
        <http‐only>true</http‐only>
        <secure>false</secure>
        <max‐age>3600</max‐age>
    </cookie‐config>
    <tracking‐mode>COOKIE</tracking‐mode>
</session‐config>
```

配置说明：

- session‐timeout：会话超时时间，单位分钟。

- cookie‐config：用于配置会话追踪Cookie

  - name：Cookie的名称
    
  - domain：Cookie的域名
    
  - path：Cookie的路径
    
  - comment：注释

  - http‐only：cookie只能通过HTTP方式进行访问，JS无法读取或修改，此项可以增加网站访问的安全性。
  
  - secure：此cookie只能通过HTTPS连接传递到服务器，而HTTP 连接则不会传递该信息。注意是从浏览器传递到服务器，服务器端的Cookie对象不受此项影响。
  
  - max‐age：以秒为单位表示cookie的生存期，默认为‐1表示是会话Cookie，浏览器关闭时就会消失。
  
- tracking‐mode：用于配置会话追踪模式，Servlet3.0版本中支持的追踪模式：COOKIE、URL、SSL。

  - COOKIE: 通过HTTP Cookie 追踪会话是最常用的会话追踪机制，而且Servlet规范也要求所有的Servlet规范都需要支持Cookie追踪。
  
  - URL: URL重写是最基本的会话追踪机制。当客户端不支持Cookie时，可以采用URL重写的方式。当采用URL追踪模式时，请求路径需要包含会话标识信息，Servlet容器会根据路径中的会话标识设置请求的会话信息。如：`http：//www.myserver.com/user/index.html;jessionid=1234567890` 。
  
  - SSL: 对于SSL请求，通过SSL会话标识确定请求会话标识。

#### Servlet配置

> 定义处理请求的处理类

Servlet 的配置主要是两部分，servlet 和 servlet-mapping：

```xml
<servlet>
    <servlet‐name>DispatcherServlet</servlet‐name>
    <servlet‐class>org.springframework.web.servlet.DispatcherServlet</servlet‐class>
    <init‐param>
        <param‐name>fileName</param‐name>
        <param‐value>init.conf</param‐value>
    </init‐param>
    <load‐on‐startup>1</load‐on‐startup>
    <enabled>true</enabled>
</servlet>

<servlet‐mapping>
    <servlet‐name>DispatcherServlet</servlet‐name>
    <url‐pattern>*.do</url‐pattern>
    <url‐pattern>/myservet/*</url‐pattern>
</servlet‐mapping>
```

配置说明：

- servlet‐name: 指定servlet的名称，该属性在web.xml中唯一。

- servlet‐class: 用于指定servlet类名。

- init‐param：用于指定servlet的初始化参数，在应用中可以通过HttpServlet.getInitParameter 获取。

- load‐on‐startup： 用于控制在Web应用启动时，Servlet的加载顺序。值小于0，web应用启动时，不加载该servlet, 第一次访问时加载。

- enabled：true | false。若为false，表示Servlet不处理任何请求。

- url‐pattern：用于指定URL表达式，一个 servlet‐mapping可以同时配置多个 url‐pattern。

例如Servlet 中文件上传配置（自定义Servlet需继承HttpServlet）：

```xml
<servlet>
    <servlet‐name>uploadServlet</servlet‐name>
    <servlet‐class>cn.test.web.UploadServlet</servlet‐class>
    <multipart‐config>
    <location>C://path</location>
        <max‐file‐size>10485760</max‐file‐size>
        <max‐request‐size>10485760</max‐request‐size>
        <file‐size‐threshold>0</file‐size‐threshold>
    </multipart‐config>
</servlet>
```

配置说明：

- location：存放生成的文件地址。

- max‐file‐size：允许上传的文件最大值。默认值为‐1，表示没有限制。

- max‐request‐size：针对该 multi/form‐data 请求的最大数量，默认值为‐1，表示无限制。

- file‐size‐threshold：当数量量大于该值时， 内容会被写入文件。

#### Listener配置

**Listener用于监听servlet中的事件**，例如context、request、session对象的创建、修改、删除，并触发响应事件。

Listener是观察者模式的实现，在servlet中主要用于对context、request、session对象的生命周期进行监控。在servlet2.5规范中共定义了8中Listener。

在启动时，ServletContextListener 的执行顺序与web.xml 中的配置顺序一致，停止时执行顺序相反。

```xml
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```

#### Filter配置

> 自定义过滤器需实现Filter接口。

filter 用于配置web应用过滤器，用来过滤资源请求及响应。经常用于认证、日志、加密、数据转换等操作，配置如下：

```xml
<filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
<!--    <async‐supported>true</async‐supported>-->
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

配置说明：

1. filter‐name：用于指定过滤器名称，在web.xml中，过滤器名称必须唯一。

2. filter‐class：过滤器的全限定类名，该类必须实现Filter接口。

3. async‐supported：该过滤器是否支持异步。

4. init‐param：用于配置Filter的初始化参数，可以配置多个，可以通过FilterConfig.getInitParameter获取。

5. url‐pattern：指定该过滤器需要拦截的URL。

#### 欢迎页面配置

welcome-file-list 用于指定web应用的欢迎文件列表。尝试请求的顺序，从上到下。

```xml
<welcome‐file‐list>
    <welcome‐file>index.html</welcome‐file>
    <welcome‐file>index.htm</welcome‐file>
    <welcome‐file>index.jsp</welcome‐file>
</welcome‐file‐list>
```

#### 错误页面配置

error-page 用于配置Web应用访问异常时定向到的页面，支持HTTP响应码和异常类两种形式。

```xml
<error‐page>
    <error‐code>404</error‐code>
    <location>/404.html</location>
</error‐page>
<error‐page>
    <error‐code>500</error‐code>
    <location>/500.html</location>
</error‐page>
<error‐page>
    <exception‐type>java.lang.Exception</exception‐type>
    <location>/error.jsp</location>
</error‐page>
```

### Tomcat管理配置

从早期的Tomcat版本开始，就提供了Web版的管理控制台，他们是两个独立的Web应用，位于webapps目录下。Tomcat 提供的管理应用于管理的Host的host-manager和用于管理Web应用的manager。

### JVM配置

一般不需要改动，具体情况根据业务场景而定。

最常见的JVM配置当属内存分配，因为在绝大多数情况下，JVM默认分配的内存可能不能够满足我们的需求，特别是在生产环境，此时需要手动修改Tomcat启动时的内存参数分配。

- windows 平台(catalina.bat)：

```text
set JAVA_OPTS=‐server ‐Xms2048m ‐Xmx2048m ‐XX:MetaspaceSize=256m ‐XX:MaxMetaspaceSize=256m ‐XX:SurvivorRatio=8
```

- linux 平台(catalina.sh)：

```text
JAVA_OPTS="‐server ‐Xms2048m ‐Xmx2048m ‐XX:MetaspaceSize=512m ‐XX:MaxMetaspaceSize=512m ‐XX:SurvivorRatio=8"
```

参数说明 ：

| 参数 | 含义|
| ---- | ----|
| -Xms | 堆内存的初始大小|
| -Xmx | 堆内存的最大大小|
| -Xmn | 新生代的内存大小，官方建议是整个堆的3/8。|
| -XX:MetaspaceSize | 元空间内存初始大小，在JDK1.8版本之前配置为 -XX:PermSize（永久代）|
| -XX:MaxMetaspaceSize | 元空间内存最大大小，在JDK1.8版本之前配置为 -XX:MaxPermSize（永久代）|
| -XX:InitialCodeCacheSize | -XX:ReservedCodeCacheSize 代码缓存区大小|
| -XX:NewRatio | 设置新生代和老年代的相对大小比例。这种方式的优点是新生代大小会随着整个堆大小动态扩展。如 -XX:NewRatio=3 指定老年代 /新生代为 3/1。老年代占堆大小的 3/4，新生代占 1/4 。|
| -XX:SurvivorRatio | 指定伊甸园区 (Eden) 与幸存区大小比例。如-XX:SurvivorRatio=8 表示伊甸园区 (Eden)是 幸存区 To 大小的 8 倍 (也是幸存区 From的 8 倍)。所以，伊甸园区 (Eden) 占新生代大小的 8/10， 幸存区 From 和幸存区 To 每个占新生代的 1/10 。注意，两个幸存区永远是一样大的。

### Tomcat集群

由于单台Tomcat的承载能力是有限的，当我们的业务系统用户量比较大，请求压力比较大时，单台Tomcat是扛不住的，这个时候，就需要搭建Tomcat的集群，而目前比较流行的做法就是通过Nginx来实现Tomcat集群的负载均衡。

```conf
upstream serverpool{
    server localhost:8888;
    server localhost:9999;
}
server {
    listen 99;
    server_name localhost;
    location / {
        proxy_pass http://serverpool/;
    }
}
```

#### Session共享方案

在Tomcat集群中，如果应用需要用户进行登录，那么这个时候，用于tomcat做了负载均衡，则用户登录并访问应用系统时，就会出现问题。

用户在A服务器上已经到登录过了，下一次请求nginx到了B服务器，又要重新登录一次，这是不允许的，对用户不友好。

解决上述问题， 有以下几种方案：

- ip_hash 策略：通过nginx的ip_hash策略实现。

- Session复制

  - 在Tomcat的conf/server.xml 配置如下：
  
  ```xml
  <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"/>
  ```
  
  - 在Tomcat部署的应用程序的web.xml 中加入如下配置：
  
    ```xml
    <distributable/>
    ```
    
  上述方案，适用于较小的集群环境（节点数不超过4个），如果集群的节点数比较多的话，通过这种广播的形式来完成Session的复制，会消耗大量的网络带宽，影响服务的性能。
  
- SSO-单点登录：单点登录（Single Sign On），简称为 SSO，将用户登录信息（如token）存放在Tomcat之外的地方（如Redis）。

### Tomcat性能调优

#### 性能测试

对于系统性能，用户最直观的感受就是系统的加载和操作时间，即用户执行某项操作的耗时。从更为专业的角度上讲，性能测试可以从以下两个指标量化。

1. 响应时间：如上所述，为执行某个操作的耗时。大多数情况下，我们需要针对同一个操作测试多次，以获取操作的平均响应时间。

2. 吞吐量：即在给定的时间内，系统支持的事务数量，计算单位为 TPS。

#### 性能调优

Tomcat是一款Java应用，那么JVM的配置便与其运行性能密切相关，而JVM优化的重点则集中在内存分配和GC策略的调整上，因为内存会直接影响服务的运行效率和吞吐量，JVM垃圾回收机制则会不同程度地导致程序运行中断。

可以根据应用程序的特点，选择不同的垃圾回收策略，调整JVM垃圾回收策略，可以极大减少垃圾回收次数，提升垃圾回收效率，改善程序运行性能。

JVM内存参数：

| 参数 | 参数作用 | 优化建议|
| ---- | ---- | ----|
| -server | 启动Server，以服务端模式运行 | 服务端模式建议开启|
| -Xms | 最小堆内存| 建议与-Xmx设置相同|
| -Xmx | 最大堆内存| 建议设置为可用内存的80%|
| -XX:MetaspaceSize | 元空间初始值| |
| -XX:MaxMetaspaceSize | 元空间最大内存 | 默认无限|
| -XX:MaxNewSize | 新生代最大内存 | 默认16M|
| -XX:NewRatio | 年轻代和老年代大小比值，取值为整数，默认为2 | 不建议修改|
| -XX:SurvivorRatio | Eden区与Survivor区大小的比值，取值为整数，默认为8 | 不建议修改|

```text
JAVA_OPTS="‐server ‐Xms2048m ‐Xmx2048m ‐XX:MetaspaceSize=512m ‐XX:MaxMetaspaceSize=512m ‐XX:SurvivorRatio=8"
```

> 更多JVM参数设置参考：https://www.bilibili.com/video/BV1dJ411N7Um?p=46 或参考Oracle文档

#### Tomcat连接器配置调优

调整tomcat/conf/server.xml中关于连接器的配置可以提升应用服务器的性能。

| 参数 | 说明 |
|----|----|
| maxConnections | 最大连接数，当达到该值后，服务器接收但不会处理更多的请求，额外的请求将会阻塞知道连接数低于该值。对于CPU要求不是特别高时，建议配置在2000左右，对CPU要求更高时不建议配置过大 |
| maxThreads | 最大连接数，根据服务器硬件配置进行合理设置 |
| acceptCount | 最大排队等待数，当服务器接收的请求数到达maxConnections，此时Tomcat会将额外的请求存放在任务队列中进行排序，acceptCount就是任务队列中排队等待的请求数。而一台Tomcat的最大请求处理数为maxConnections+acceptCount |

## 初始化-原理篇

Tomcat程序入口：`org.apache.catalina.startup.Bootstrap#main`。

```java
public final class Bootstrap {

    private static volatile Bootstrap daemon = null;

    private Object catalinaDaemon = null;

    public static void main(String args[]) {

        // 创建一个 Bootstrap 对象，调用它的 init 方法初始化
        synchronized (daemonLock) {
            if (daemon == null) {
                // Don't set daemon until init() has completed
                Bootstrap bootstrap = new Bootstrap();
                try {
                    /*
                        加载 Catalina 类，并赋值给 catalinaDaemon
                    */
                    bootstrap.init();
                } catch (Throwable t) {
                    handleThrowable(t);
                    t.printStackTrace();
                    return;
                }
                daemon = bootstrap;
            } else {
                // When running as a service the call to stop will be on a new
                // thread so make sure the correct class loader is used to
                // prevent a range of class not found exceptions.
                Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
            }
        }

        // 根据启动参数，分别调用 Bootstrap 对象的不同方法
        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("startd")) {
                args[args.length - 1] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[args.length - 1] = "stop";
                daemon.stop();

            // 启动 Tomcat
            } else if (command.equals("start")) {
                daemon.setAwait(true);

                // 第一步：加载（初始化Tomcat），加载完会绑定监听socket端口（Tomcat 8开始使用NIO）
                daemon.load(args);

                // 第二步：启动Tomcat，启动完才会去accept()处理请求
                daemon.start();
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            } else if (command.equals("configtest")) {
                daemon.load(args);
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
                System.exit(0);
            } else {
                log.warn("Bootstrap: command \"" + command + "\" does not exist.");
            }
        } catch (Throwable t) {
            ......
        }
    }
}
```

### init()-Catalina的初始化

```java
    // 初始化守护进程
    public void init() throws Exception {

        // 初始化classloader（包括catalinaLoader）
        initClassLoaders();

        // 设置当前的线程的contextClassLoader为catalinaLoader
        Thread.currentThread().setContextClassLoader(catalinaLoader);

        SecurityClassLoad.securityClassLoad(catalinaLoader);

        // Load our startup class and call its process() method
        if (log.isDebugEnabled()) {
            log.debug("Loading startup class");
        }
        
        // 通过catalinaLoader加载Catalina，并初始化startupInstance 对象
        Class<?> startupClass = catalinaLoader.loadClass("org.apache.catalina.startup.Catalina");
        Object startupInstance = startupClass.getConstructor().newInstance();

        // Set the shared extensions class loader
        if (log.isDebugEnabled()) {
            log.debug("Setting startup class properties");
        }
    
        // 反射调用setParentClassLoader 方法
        String methodName = "setParentClassLoader";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object paramValues[] = new Object[1];
        paramValues[0] = sharedLoader;
        Method method =
            startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);

        catalinaDaemon = startupInstance;
    }
```

### load()-Catalina的加载

> load加载过程本质上是初始化Server的实例

反射调用`Catalina`类的load()方法。

```java
private void load(String[] arguments) throws Exception {

    // Call the load() method
    String methodName = "load";
    Object param[];
    Class<?> paramTypes[];
    if (arguments==null || arguments.length==0) {
        paramTypes = null;
        param = null;
    } else {
        paramTypes = new Class[1];
        paramTypes[0] = arguments.getClass();
        param = new Object[1];
        param[0] = arguments;
    }

    // 反射调用 Catalina.load()
    Method method =
        catalinaDaemon.getClass().getMethod(methodName, paramTypes);
    if (log.isDebugEnabled()) {
        log.debug("Calling startup class " + method);
    }
    method.invoke(catalinaDaemon, param);
}
```

`org.apache.catalina.startup.Catalina#load(java.lang.String[])`：

```java
public void load(String args[]) {

    try {
        // 处理命令行的参数
        if (arguments(args)) {
            load();
        }
    } catch (Exception e) {
        e.printStackTrace(System.out);
    }
}

public void load() {

    // 避免重复加载
    if (loaded) {
        return;
    }
    loaded = true;

    long t1 = System.nanoTime();

    // 已经弃用了，Tomcat10会删除这个方法
    initDirs();

    // 设置额外的系统变量
    // Before digester - it may be needed
    initNaming();

    // Set configuration source
    ConfigFileLoader.setSource(new CatalinaBaseConfigurationSource(Bootstrap.getCatalinaBaseFile(), getConfigFile()));
    File file = configFile();

    // Create and execute our Digester
    Digester digester = createStartDigester();

    // 读取配置文件，解析 server.xml
    try (ConfigurationSource.Resource resource = ConfigFileLoader.getSource().getServerXml()) {
        InputStream inputStream = resource.getInputStream();
        InputSource inputSource = new InputSource(resource.getURI().toURL().toString());
        inputSource.setByteStream(inputStream);
        digester.push(this);

        // 由 com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl.JAXPSAXParser#parse 处理
        digester.parse(inputSource);
    } catch (Exception e) {
        log.warn(sm.getString("catalina.configFail", file.getAbsolutePath()), e);
        if (file.exists() && !file.canRead()) {
            log.warn(sm.getString("catalina.incorrectPermissions"));
        }
        return;
    }

    getServer().setCatalina(this);
    getServer().setCatalinaHome(Bootstrap.getCatalinaHomeFile());
    getServer().setCatalinaBase(Bootstrap.getCatalinaBaseFile());

    // Stream redirection
    initStreams();

    // Start the new server
    try {
        // 初始化Server对象（个人理解为Tomcat容器）
        getServer().init();
    } catch (LifecycleException e) {
        if (Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE")) {
            throw new java.lang.Error(e);
        } else {
            log.error(sm.getString("catalina.initError"), e);
        }
    }

    long t2 = System.nanoTime();
    if(log.isInfoEnabled()) {
        log.info(sm.getString("catalina.init", Long.valueOf((t2 - t1) / 1000000)));
    }
}
```

#### 初始化Server

每个组件初始化都会进入该方法，因为这些组件都实现了Lifecycle接口，Lifecycle负责管理其生命周期。

`org.apache.catalina.util.LifecycleBase#init`：

```java
public final synchronized void init() throws LifecycleException {
    // 非NEW状态，不允许调用init()方法
    if (!state.equals(LifecycleState.NEW)) {
        // 该方法直接抛出异常
        invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
    }

    try {
        // 初始化逻辑之前，先将状态变更为`INITIALIZING`
        // setStateInternal方法用于维护状态，同时在状态转换成功之后触发事件。
        setStateInternal(LifecycleState.INITIALIZING, null, false);

        // 该方法为一个abstract方法，真正执行初始化的方法，由子类StandardServer调用
        initInternal();
        // 初始化完成之后，状态变更为`INITIALIZED`
        setStateInternal(LifecycleState.INITIALIZED, null, false);
    } catch (Throwable t) {
        // 初始化的过程中，可能会有异常抛出，这时需要捕获异常，并将状态变更为`FAILED`
        handleSubClassException(t, "lifecycleBase.initFail", toString());
    }
}
```

`org.apache.catalina.core.StandardServer#initInternal` 

```java
protected void initInternal() throws LifecycleException {

    super.initInternal();

    // Initialize utility executor
    reconfigureUtilityExecutor(getUtilityThreadsInternal(utilityThreads));
    register(utilityExecutor, "type=UtilityExecutor");

    // Register global String cache
    // Note although the cache is global, if there are multiple Servers
    // present in the JVM (may happen when embedding) then the same cache
    // will be registered under multiple names
    onameStringCache = register(new StringCache(), "type=StringCache");

    // Register the MBeanFactory
    MBeanFactory factory = new MBeanFactory();
    factory.setContainer(this);
    onameMBeanFactory = register(factory, "type=MBeanFactory");

    // Register the naming resources
    globalNamingResources.init();

    // Populate the extension validator with JARs from common and shared
    // class loaders
    if (getCatalina() != null) {
        ClassLoader cl = getCatalina().getParentClassLoader();
        // Walk the class loader hierarchy. Stop at the system class loader.
        // This will add the shared (if present) and common class loaders
        while (cl != null && cl != ClassLoader.getSystemClassLoader()) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                for (URL url : urls) {
                    if (url.getProtocol().equals("file")) {
                        try {
                            File f = new File (url.toURI());
                            if (f.isFile() &&
                                    f.getName().endsWith(".jar")) {
                                ExtensionValidator.addSystemResource(f);
                            }
                        } catch (URISyntaxException e) {
                            // Ignore
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
            cl = cl.getParent();
        }
    }

    // Server管理自己的Service，最终也借用超类的init()方法对Service进行初始化
    // Initialize our defined Services
    for (Service service : services) {
        service.init();
    }
}
```

#### 初始化Service

`org.apache.catalina.util.LifecycleBase#init`

入口：`org.apache.catalina.core.StandardService#initInternal`

此时执行 `initInternal();` 的是LifecycleBase的子类StandardService。

```java
protected void initInternal() throws LifecycleException {

    super.initInternal();

    // 初始化Engine
    if (engine != null) {
        engine.init();
    }

    // 初始化执行器
    // Initialize any Executors
    for (Executor executor : findExecutors()) {
        if (executor instanceof JmxEnabled) {
            ((JmxEnabled) executor).setDomain(getDomain());
        }
        executor.init();
    }

    // 初始化监听器
    // Initialize mapper listener
    mapperListener.init();

    // 初始化 Connectors （可以有多个连接器）
    // Initialize our defined Connectors
    synchronized (connectorsLock) {
        for (Connector connector : connectors) {
            connector.init();
        }
    }
}
```

#### 初始化引擎、执行器、监听器、连接器

入口：org.apache.catalina.util.LifecycleBase#init

StandardEngine继承自LifecycleBase。

##### 初始化引擎

```java
protected void initInternal() throws LifecycleException {
    // Ensure that a Realm is present before any attempt is made to start
    // one. This will create the default NullRealm if necessary.
    getRealm();
    super.initInternal();
}

// 调用父类：org.apache.catalina.core.ContainerBase#initInternal
protected void initInternal() throws LifecycleException {
    reconfigureStartStopExecutor(getStartStopThreads());
    super.initInternal();
}
private void reconfigureStartStopExecutor(int threads) {
    if (threads == 1) {
        // 虚假的线程池
        // Use a fake executor
        if (!(startStopExecutor instanceof InlineExecutorService)) {
            startStopExecutor = new InlineExecutorService();
        }
    } else {
        // Delegate utility execution to the Service
        Server server = Container.getService(this).getServer();
        server.setUtilityThreads(threads);
        startStopExecutor = server.getUtilityExecutor();
    }
}
```

##### 初始化执行器

默认没有定义执行器。

##### 初始化监听器

默认执行：`org.apache.catalina.util.LifecycleMBeanBase#initInternal`

##### 初始化连接器

```java
protected void initInternal() throws LifecycleException {

    super.initInternal();

    if (protocolHandler == null) {
        throw new LifecycleException(
                sm.getString("coyoteConnector.protocolHandlerInstantiationFailed"));
    }

    // Initialize adapter

    // 初始化CoyoteAdapter，并和protocolHandler管理起来
    adapter = new CoyoteAdapter(this);
    protocolHandler.setAdapter(adapter);
    if (service != null) {
        protocolHandler.setUtilityExecutor(service.getServer().getUtilityExecutor());
    }

    // Make sure parseBodyMethodsSet has a default
    // 设置parseBody的方法，默认为POST
    if (null == parseBodyMethodsSet) {
        setParseBodyMethods(getParseBodyMethods());
    }

    // 校验
    if (protocolHandler.isAprRequired() && !AprLifecycleListener.isInstanceCreated()) {
        throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerNoAprListener",
                getProtocolHandlerClassName()));
    }
    if (protocolHandler.isAprRequired() && !AprLifecycleListener.isAprAvailable()) {
        throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerNoAprLibrary",
                getProtocolHandlerClassName()));
    }
    if (AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseOpenSSL() &&
            protocolHandler instanceof AbstractHttp11JsseProtocol) {
        AbstractHttp11JsseProtocol<?> jsseProtocolHandler =
                (AbstractHttp11JsseProtocol<?>) protocolHandler;
        if (jsseProtocolHandler.isSSLEnabled() &&
                jsseProtocolHandler.getSslImplementationName() == null) {
            // OpenSSL is compatible with the JSSE configuration, so use it if APR is available
            jsseProtocolHandler.setSslImplementationName(OpenSSLImplementation.class.getName());
        }
    }

    try {
        // 对协议处理器进行初始化，主要是其内部Endpoint的初始化
        protocolHandler.init();
    } catch (Exception e) {
        throw new LifecycleException(
                sm.getString("coyoteConnector.protocolHandlerInitializationFailed"), e);
    }
}
```

`org.apache.coyote.http11.AbstractHttp11Protocol#init`

```java
public void init() throws Exception {
    // Upgrade protocols have to be configured first since the endpoint
    // init (triggered via super.init() below) uses this list to configure
    // the list of ALPN protocols to advertise
    for (UpgradeProtocol upgradeProtocol : upgradeProtocols) {
        configureUpgradeProtocol(upgradeProtocol);
    }

    super.init();
}

// 调用父类：org.apache.coyote.AbstractProtocol#init
public void init() throws Exception {
    if (getLog().isInfoEnabled()) {
        getLog().info(sm.getString("abstractProtocolHandler.init", getName()));
        logPortOffset();
    }

    if (oname == null) {
        // Component not pre-registered so register it
        oname = createObjectName();
        if (oname != null) {
            Registry.getRegistry(null, null).registerComponent(this, oname, null);
        }
    }

    if (this.domain != null) {
        rgOname = new ObjectName(domain + ":type=GlobalRequestProcessor,name=" + getName());
        Registry.getRegistry(null, null).registerComponent(
                getHandler().getGlobal(), rgOname, null);
    }

    String endpointName = getName();
    endpoint.setName(endpointName.substring(1, endpointName.length()-1));
    endpoint.setDomain(domain);

    // 通信端点的初始化
    endpoint.init();
}
```

org.apache.tomcat.util.net.AbstractEndpoint#init

```java
public final void init() throws Exception {
    if (bindOnInit) {
        bindWithCleanup();
        bindState = BindState.BOUND_ON_INIT;
    }
    // 注册JMX
    if (this.domain != null) {
        // Register endpoint (as ThreadPool - historical name)
        oname = new ObjectName(domain + ":type=ThreadPool,name=\"" + getName() + "\"");
        Registry.getRegistry(null, null).registerComponent(this, oname, null);

        ObjectName socketPropertiesOname = new ObjectName(domain +
                ":type=SocketProperties,name=\"" + getName() + "\"");
        socketProperties.setObjectName(socketPropertiesOname);
        Registry.getRegistry(null, null).registerComponent(socketProperties, socketPropertiesOname, null);

        for (SSLHostConfig sslHostConfig : findSslHostConfigs()) {
            registerJmx(sslHostConfig);
        }
    }
}
private void bindWithCleanup() throws Exception {
    try {
        bind();
    } catch (Throwable t) {
        // Ensure open sockets etc. are cleaned up if something goes
        // wrong during bind
        ExceptionUtils.handleThrowable(t);
        unbind();
        throw t;
    }
}

// 调用：org.apache.tomcat.util.net.NioEndpoint#bind
public void bind() throws Exception {
    initServerSocket();

    setStopLatch(new CountDownLatch(1));

    // Initialize SSL if needed
    initialiseSsl();

    selectorPool.open(getName());
}

protected void initServerSocket() throws Exception {
    if (!getUseInheritedChannel()) {

        // 获取nio channel
        serverSock = ServerSocketChannel.open();
        socketProperties.setProperties(serverSock.socket());
        InetSocketAddress addr = new InetSocketAddress(getAddress(), getPortWithOffset());
        serverSock.socket().bind(addr,getAcceptCount());
    } else {
        // Retrieve the channel provided by the OS
        Channel ic = System.inheritedChannel();
        if (ic instanceof ServerSocketChannel) {
            serverSock = (ServerSocketChannel) ic;
        }
        if (serverSock == null) {
            throw new IllegalArgumentException(sm.getString("endpoint.init.bind.inherited"));
        }
    }
    serverSock.configureBlocking(true); //mimic APR behavior
}
```

Server -> Services -> (engine -> executor -> mapperListener -> connector)

### start()

本质是按照load()方法中组件初始化的顺序，再次顺序执行startInternal()方法。

> Server -> Service -> engine -> executors(StandardThreadExecutor...) -> mapperListener -> connector

```java
/**
 * Start a new server instance.
 */
public void start() {

    if (getServer() == null) {
        load();
    }

    if (getServer() == null) {
        log.fatal(sm.getString("catalina.noServer"));
        return;
    }

    long t1 = System.nanoTime();

    // Start the new server
    try {
        getServer().start();
    } catch (LifecycleException e) {
        log.fatal(sm.getString("catalina.serverStartFail"), e);
        try {
            getServer().destroy();
        } catch (LifecycleException e1) {
            log.debug("destroy() failed for failed Server ", e1);
        }
        return;
    }

    long t2 = System.nanoTime();
    if(log.isInfoEnabled()) {
        log.info(sm.getString("catalina.startup", Long.valueOf((t2 - t1) / 1000000)));
    }

    // Register shutdown hook
    if (useShutdownHook) {
        if (shutdownHook == null) {
            shutdownHook = new CatalinaShutdownHook();
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // If JULI is being used, disable JULI's shutdown hook since
        // shutdown hooks run in parallel and log messages may be lost
        // if JULI's hook completes before the CatalinaShutdownHook()
        LogManager logManager = LogManager.getLogManager();
        if (logManager instanceof ClassLoaderLogManager) {
            ((ClassLoaderLogManager) logManager).setUseShutdownHook(
                    false);
        }
    }

    if (await) {
        await();
        stop();
    }
}
```

## 请求处理-原理篇

![](https://github.com/Doing-code/guide/blob/main/image/tomcat_责任链请求处理.png)

Tomcat中的各个组件各司其职，组件之间松耦合，确保了整体架构的可伸缩性和可拓展性，那么在组件内部，如何增强组件的灵活性和拓展性呢？在Tomcat中，每个Container组件采用责任链模式来完成具体的请求处理。

在Tomcat中定义了Pipeline 和 Valve 两个接口，Pipeline 用于构建责任链，后者代表责任链上的每个处理器。Pipeline 中维护了一个基础的Valve，它始终位于Pipeline的末端（最后执行），封装了具体的请求处理和输出响应的过程。

当然，我们也可以调用addValve()方法，为Pipeline 添加其他的Valve，后添加的Valve 位于基础的Valve之前，并按照添加顺序执行。Pipeline通过获得首个Valve来启动整合链条的执行 。

当Servlet执行后，如果配置了Filter，则会执行自定义Filter。

## 附录

### Tomcat线程池的设计与实现

> 自定义线程池需实现org.apache.catalina.Executor接口

| 属性	| 描述|
| ----	| ----|
| threadPriority	| (int) 执行器中线程的线程优先级，默认为 5（常量的值Thread.NORM_PRIORITY）|
| daemon	| (boolean) 线程是否应该是守护线程，默认是true|
| namePrefix	| （String）执行器创建的每个线程的名称前缀。单个线程的线程名称将为namePrefix+threadNumber。默认值为 tomcat-exec-。|
| maxThreads	| (int) 该池中活动线程的最大数量，默认为**200**|
| minSpareThreads	| (int) 核心线程数，始终保持活动状态的最小线程数（空闲和活动），默认为**25**|
| maxIdleTime| 	(int) 空闲线程关闭之前的毫秒数，除非活动线程数小于或等于 minSpareThreads。默认值为60000（1 分钟）|
| maxQueueSize| 	(int) 在我们拒绝它们之前可以排队等待执行的可运行任务的最大数量。默认值为Integer.MAX_VALUE|
| threadRenewalDelay	| （long）如果配置了ThreadLocalLeakPreventionListener，它将通知此执行程序有关已停止的上下文。上下文停止后，池中的线程将被更新。为了避免同时更新所有线程，此选项设置任意 2 个线程更新之间的延迟。该值以毫秒为单位，默认值为1000毫秒。如果值为负，则不会更新线程。|

### LifeCycle组件生命周期管理

```java
public interface Lifecycle {
    /** 第1类：针对监听器 **/
    // 添加监听器
    public void addLifecycleListener(LifecycleListener listener);
    // 获取所以监听器
    public LifecycleListener[] findLifecycleListeners();
    // 移除某个监听器
    public void removeLifecycleListener(LifecycleListener listener);
    
    /** 第2类：针对控制流程 **/
    // 初始化方法
    public void init() throws LifecycleException;
    // 启动方法
    public void start() throws LifecycleException;
    // 停止方法，和start对应
    public void stop() throws LifecycleException;
    // 销毁方法，和init对应
    public void destroy() throws LifecycleException;
    
    /** 第3类：针对状态 **/
    // 获取生命周期状态
    public LifecycleState getState();
    // 获取字符串类型的生命周期状态
    public String getStateName();
}
```