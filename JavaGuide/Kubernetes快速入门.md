# Kubernetes

## Kubernetes介绍

### 应用部署方式的演变

1. 传统部署：互联网早期，会直接将应用部署在物理机上。

2. 虚拟化部署：可以在一台物理机上运行多个虚拟机，每个虚拟机都是独立的一个环境。

3. 容器化部署：和虚拟化类似，但是共享操作系统。

容器化部署如果容器宕机，怎么让另一个容器立即去替补停机的机器；当并发访问量变大的时候，怎么做到横向扩展容器数量。

而这些容器管理的问题统称为”容器编排问题“，要解决这些容器编排问题，那么就会产生一些容器编排的软件。

- Swarm：Docker 自己的容器编排工具。

- Mesos：Apache 的一个资源统一管控的工具，需要和 Marathon 结合。

- Kubernetes：Google 开源的容器编排工具。

### Kubernetes是什么

Kubernetes 是一个基于容器技术的分布式架构领先方案。是 Google Borg 系统的一个开源版本，于 2014 年 9 月发布第一个版本，2015 年 7 月发布第一个正式版本。

Kubernetes 的本质是一组服务集群，它可以在集群的每个节点上运行特定的程序，使其对节点中的容器进行管理。它的目的就是实现资源管理的自动化。

其主要提供了如下功能：

- 自我修复：一旦某一个容器崩溃，能够在短时间内（1秒左右）迅速启动新的容器。

- 弹性伸缩：可以根据需要，自动对集群中正在运行的容器数量进行调整。

- 服务发现：服务可以通过自动发现的形式找到它所依赖的服务。

- 负载均衡：如果一个服务启动了多个容器，能够自动实现请求的负载均衡。

- 版本回退：如果发现新发布的程序版本有问题，可以立即回退到原来的版本。

- 存储编排：可以根据容器自身的需求自动创建存储卷。

- ......

### Kubernetes组件

一个 Kubernetes 集群主要由控制节点（master）和工作节点（node）构成，每个节点上都会安装不同的组件。

- 控制节点（master）：负责集群的决策。

    - apiServer：集群的唯一入口，接收用户输入的命令，提供认证、授权、api注册和发现等机制。
    
    - Scheduler：负责集群资源调度，按照既定的调度策略将 Pod 调度到相应的 node 节点上。
    
    - ControllerManager：负责维护集群的状态。比如程序部署安排、故障检测、自动扩展和滚动更新等。
    
    - etcd：负责存储集群中各种资源对象的信息。

- 工作节点（node）：负责为容器提提供运行环境。

    - Kubelet：负责维护容器的生命周期，即通过控制 Docker，创建、更新和销毁容器。
    
    - KubeProxy：负责提供集群内部的服务发现和负载均衡。
    
    - Docker：负责节点上容器的各种操作。
    

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/images/image-20200406184656917.png)

eg：以部署一个 Nginx 服务来说明 Kubernetes 系统中各个组件调用关系。

首先要明确，一旦 Kubernetes 环境启动后，master和node都会将自身的信息存储到etcd数据库中。

1. 一个 Nginx 服务的安装请求会首先被发送到master节点 apiServer 组件。

2. apiServer组件会调用 Scheduler 组件来决定应该把这个服务安装到哪个 node 节点上。

    - Scheduler会从etcd中读取所有node节点的信息，然后按照一定的算法进行选择，并将结果告知给 apiServer。
    
3. apiServer调用 Controller-Manager 去调度node节点安装 Nginx 服务。

4. kubelet 接收到指令后，会去通知Docker，然后由Docker来启动一个 Nginx 的 pod。

    - pod 是 Kubernetes 的最小操作单元，容器必须运行在 pod 中（pod可以认为是对容器的封装）。

5. 至此，一个 Nginx 服务就运行了，如果需要访问nginx，就需要通过kube-proxy来对pod产生访问的代理。

### Kubernetes概念

- Master：集群控制节点，每个集群至少需要一个master节点负责集群的管理。

- Node：工作节点，由master分配容器到这些工作节点上，然后node节点中的Docker负责容器的运行。

- Pod：Kubernetes 的最小控制单元，容器都是运行在 pod 中的，一个 pod 中可以有1个或多个容器。

- Controller：控制器，通过它来实现对pod的管理，比如启动pod、停止pod、伸缩pod的数量等。

- Service：pod对外服务的统一入口，一个Service可以维护同一类的多个pod。

- Label：用于对pod进行分类，同一类pod会拥有相同的标签。

- Namespace：命名空间，用来隔离pod的运行环境。

## Kubernetes环境搭建

目前生产部署Kubernetes集群主要有两种方式，kubeadm（K8s 部署工具）、手动部署每个组件，组成Kubernetes集群。

Kubernetes集群大致分为两类：一主多从和多主多从。

- 一主多从：一个Master节点和多台Node节点，搭建简单，但是有单机故障风险，适合用于测试环境。

- 多主多从：多台Master和多台Node节点，搭建麻烦，安全性高，适合用于生产环境。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/images/image-20200404094800622.png)

> 环境搭建部分省略, 没那个服务器条件. 并且Google上有教程. 

## 资源管理

在 Kubernetes 中，所有的内容都抽象为资源，用户需要通过操作资源来管理 kubernetes。

> Kubernetes的本质是一个集群系统，用户可以在集群中部署各种服务。所有部署服务，其实就是在kubernetes集群中运行一个个的容器，并将指定的程序跑在容器中。
>
> kubernetes的最小管理单元是pod而不是容器，所以只能将容器放在Pod中。而Kubernetes一般也不会直接管理pod，而是通过`pod控制器`来管理pod。
> 
> 当pod可以提供服务之后，就要考虑如何访问pod中的服务了。Kubernetes提供了`Service`资源实现这个功能。
>
> 如果pod程序的数据需要持久化，Kubernetes还提供了数据存储功能。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200406225334627.png)

学习Kubernetes的核心，就是核心如何对集群上的pod、pod控制器、Service、存储等各种资源进行操作（配置）。

### YAML介绍

```yaml
# 字面量, 就是指的一个简单的值，字符串、布尔值、整数、浮点数、Null、时间、日期
# 1 布尔类型 (或者True)
c1: true 
# 2 整型
c2: 234
# 3 浮点型
c3: 3.14
# 4 null类型 
c4: ~  # 使用~表示null
# 5 日期类型
c5: 2018-02-17    # 日期必须使用ISO 8601格式，即yyyy-MM-dd
# 6 时间类型
c6: 2018-02-17T15:02:31+08:00  # 时间使用ISO 8601格式，时间和日期之间使用T连接，最后使用+代表时区
# 7 字符串类型
c7: aaa     # 简单写法，直接写值 , 如果字符串中间有特殊字符，必须使用双引号或者单引号包裹 
c8: bbb
    ccc     # 字符串过多的情况可以拆成多行，每一行会被转化成一个空格

# 对象
# 形式一(推荐):
aaa:
  age: 15
  address: Beijing
# 形式二(了解):
bbb: {age: 15,address: Beijing}

# 数组
# 形式一(推荐):
ccc:
  - a
  - b  
# 形式二(了解):
ddd: [a,b]
```

如果需要将多段yaml配置放在一个文件中，中间要使用`---`分隔

### 资源管理方式

- 命令式对象管理：直接使用命令去操作kubernetes资源。

    ```shell script
    kubectl run nginx-pod --image=nginx:1.17.1 --port=80
    ```

- 命令式对象配置：通过命令配置和配置文件去操作kubernetes资源。

    ```shell script
    kubectl create/patch -f nginx-pod.yaml
    ```

- 声明式对象配置：通过apply命令和配置文件去操作kubernetes资源

    ```shell script
    kubectl apply -f nginx-pod.yaml
    ```

> -f 表示指定配置文件路径

| 类型 | 操作对象 | 适用环境 | 优点 | 缺点 |
|----|----|----|----|----|
| 命令式对象管理 | 对象 | 测试 | 简单 | 只能操作活动对象，无法审计、跟踪 |
| 命令式对象配置 | 文件 | 开发 | 可以审计、跟踪 | 项目大时，配置文件多，操作麻烦 |
| 声明式对象配置 | 目录 | 开发 | 支持目录操作 | 意外情况下难以调试 |

#### 命令式对象管理

##### kubectl命令

kubectl是kubernetes集群的命令行工具，通过它能够对集群本身进行管理，并能够在集群上进行容器化应用的安装部署。

kubectl命令的语法：`kubectl [command] [type] [name] [flags]`

- command：指定要对资源执行的操作，例如create、get、delete。

- type：指定资源类型，比如deployment、pod、service。

- name：指定资源的名称，名称大小写敏感。

- flags：指定额外的可选参数。

示例：

```shell script
# 查看所有pod
kubectl get pod 

# 查看某个pod
kubectl get pod pod_name

# 查看某个pod,以yaml格式展示结果
kubectl get pod pod_name -o yaml
```

##### 资源类型

kubernetes中所有的内容都抽象为资源，可以通过下面的命令进行查看：

```shell script
kubectl api-resources
```

常用的资源如下所示：

| 资源分类 | 资源名称 | 简写 | 资源描述 |
|----|----|----|----|
| 集群级别资源 | nodes | no | 集群组成部分 |
| | namespaces | ns | 隔离Pod |
| pod资源 | pods | po | 装载容器 |
| pod资源控制器 |	replicationcontrollers | rc | 控制pod资源 |
| | replicasets	| rs | 控制pod资源 |
| | deployments	| deploy | 控制pod资源 |
| | daemonsets	| ds | 控制pod资源 |
| | jobs | | 控制pod资源 |
| | cronjobs | cj | 控制pod资源 |
| | horizontalpodautoscalers | hpa | 控制pod资源 |
| | statefulsets | sts | 控制pod资源 |
| 服务发现资源 | services |　svc | 统一pod对外接口|
| | ingress | ing | 统一pod对外接口 |
| 存储资源 | volumeattachments | | 存储 |
| | persistentvolumes | pv | 存储 |
| | persistentvolumeclaims | pvc | 存储 |
| 配置资源 | configmaps | cm | 配置 |
| | secrets | | 配置 |

##### command

kubernetes允许对资源进行多种操作，可以通过--help查看详细的操作命令。`kubectl --help`。

常用的命令操作如下：

| 命令分类	| 命令	| 描述	| 命令作用 |
|----|----|----|----|
| 基本命令	| create	|创建	|创建一个资源|
| |edit	| 编辑	|编辑一个资源|
| |get	| 获取	|获取一个资源|
| |patch	| 更新	|更新一个资源|
| |delete	| 删除	|删除一个资源|
| |explain	| 解释	|展示资源文档|
| 运行和调试	| run	|运行	|在集群中运行一个指定的镜像|
| | expose	| 暴露	|暴露资源为Service|
| | describe	| 描述	|显示资源内部信息|
| | logs	| 日志输出容器在 pod 中的日志	|输出容器在 pod 中的日志|
| | attach	| 缠绕进入运行中的容器	|进入运行中的容器|
| | exec	| 执行容器中的一个命令	|执行容器中的一个命令|
| | cp	| 复制	|在Pod内外复制文件|
| | rollout	| 首次展示	|管理资源的发布|
| | scale	| 规模	|扩(缩)容Pod的数量|
| | autoscale	| 自动调整	|自动调整Pod的数量|
| 高级命令	| apply|		|通过文件对资源进行配置|
| | label	| 标签	|更新资源上的标签|
| 其他命令	| cluster-info	|集群信息	|显示集群信息|
| | version	| 版本	|显示当前Server和Client的版本|

示例

```shell script
# 创建一个 namespace
[root@master ~]# kubectl create namespace dev
namespace/dev created

# 获取 namespace
[root@master ~]# kubectl get ns
NAME              STATUS   AGE
default           Active   21h
dev               Active   21s
kube-node-lease   Active   21h
kube-public       Active   21h
kube-system       Active   21h

# 在此 namespace 下创建并运行一个 nginx 的 Pod
[root@master ~]# kubectl run pod --image=nginx:latest -n dev
kubectl run --generator=deployment/apps.v1 is DEPRECATED and will be removed in a future version. Use kubectl run --generator=run-pod/v1 or kubectl create instead.
deployment.apps/pod created

# 查看新创建的 pod
[root@master ~]# kubectl get pod -n dev
NAME                   READY   STATUS    RESTARTS   AGE
pod-864f9875b9-pcw7x   1/1     Running   0          21s

# 删除指定的 pod
[root@master ~]# kubectl delete pod pod-864f9875b9-pcw7x
pod "pod" deleted
```

#### 命令式对象配置

命令式对象配置就是使用命令配合配置文件一起来操作kubernetes资源。

1. 创建一个nginx.yaml

    ```yaml
    # 创建该对象所使用的 Kubernetes API 的版本
    apiVersion: v1
    # 想要创建的对象的类别
    kind: Namespace
    # 唯一标识对象的一些数据
    metadata:
      name: dev
    
    ---
    
    apiVersion: v1
    kind: Pod
    metadata:
      name: nginxpod
      namespace: dev
    # 对象的状态
    spec:
      # 要在 Pod 中运行的单个应用容器
      containers:
        # 容器名称
      - name: nginx-containers
        # 容器镜像名称，此处为最新版本
        image: nginx:latest
    ```

2. 执行create命令，创建资源

    ```shell script
    [root@master ~]# kubectl create -f nginxpod.yaml
    namespace/dev created
    pod/nginxpod created
    ```
   
   此时发现创建了两个资源对象，分别是namespace和pod。
   
3. 执行get命令，查看资源

    ```shell script
    [root@master ~]#  kubectl get -f nginxpod.yaml
    NAME            STATUS   AGE
    namespace/dev   Active   18s
    
    NAME            READY   STATUS    RESTARTS   AGE
    pod/nginxpod    1/1     Running   0          17s
    ```
   
4. 执行delete命令，删除资源

    ```shell script
    [root@master ~]# kubectl delete -f nginxpod.yaml
    namespace "dev" deleted
    pod "nginxpod" deleted
    ```

命令式对象配置的方式操作资源，可以简单的认为：命令 + yaml配置文件（里面是命令需要的各种参数）。

#### 声明式对象配置

声明式对象配置跟命令式对象配置很相似，但是它只有一个命令apply。

```shell script
# 首先执行一次kubectl apply -f yaml文件，发现创建了资源
[root@master ~]#  kubectl apply -f nginxpod.yaml
namespace/dev created
pod/nginxpod created

# 再次执行一次kubectl apply -f yaml文件，发现说资源没有变动
[root@master ~]#  kubectl apply -f nginxpod.yaml
namespace/dev unchanged
pod/nginxpod unchanged
```

其实声明式对象配置就是使用apply描述一个资源最终的状态：

- 使用apply操作资源，如果资源不存在，就创建，相当于 `kubectl create`。

- 使用apply操作资源，如果资源已存在，就更新，相当于 `kubectl patch`。

既然有三种资源管理方式，那么应该使用哪一种呢？这三种方式应该是以组合形式使用。

- 创建/更新资源 使用声明式对象配置 `kubectl apply -f XXX.yaml`。

- 删除资源 使用命令式对象配置 `kubectl delete -f XXX.yaml`。

- 查询资源 使用命令式对象管理 `kubectl get/describe 资源名称`。

## 案例

介绍如何在Kubernetes集群中部署一个nginx服务 ，并且能够对外提供服务。

### namespace

默认情况下，kubernetes集群中的所有的Pod都是可以相互访问的。但是在实际中，可能不想让两个Pod之间进行互相的访问，那此时就可以将两个Pod划分到不同的namespace下。kubernetes通过将集群内部的资源分配到不同的Namespace中，可以形成逻辑上的"Group"，以方便不同的组的资源进行隔离使用和管理。

通过kubernetes的授权机制，将不同的namespace交给不同租户进行管理，这样就实现了多租户的资源隔离。此时还能结合kubernetes的资源配额机制，限定不同租户能占用的资源，例如CPU使用量、内存使用量等等，来实现租户可用资源的管理。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200407100850484.png)

Kubernetes集群环境运行之后，会创建几个默认的namespace。

```shell script
[root@master ~]# kubectl  get namespace
NAME              STATUS   AGE
default           Active   45h     #  所有未指定Namespace的对象都会被分配在default命名空间
kube-node-lease   Active   45h     #  集群节点之间的心跳维护，v1.13开始引入
kube-public       Active   45h     #  此命名空间下的资源可以被所有人访问（包括未认证用户）
kube-system       Active   45h     #  所有由Kubernetes系统创建的资源都处于这个命名空间
```

#### 查看

```shell script
# 1 查看所有的 ns  命令：kubectl get ns
[root@master ~]# kubectl get ns
NAME              STATUS   AGE
default           Active   45h
kube-node-lease   Active   45h
kube-public       Active   45h     
kube-system       Active   45h 

# 2 查看指定的 ns   命令：kubectl get ns ns名称
[root@master ~]# kubectl get ns default
NAME      STATUS   AGE
default   Active   45h

# 3 指定输出格式  命令：kubectl get ns ns名称  -o 格式参数（kubernetes支持的格式有很多，比较常见的是wide、json、yaml）
[root@master ~]# kubectl get ns default -o yaml
apiVersion: v1
kind: Namespace
metadata:
  creationTimestamp: "2021-05-08T04:44:16Z"
  name: default
  resourceVersion: "151"
  selfLink: /api/v1/namespaces/default
  uid: 7405f73a-e486-43d4-9db6-145f1409f090
spec:
  finalizers:
  - kubernetes
status:
  phase: Active

# 4 查看ns详情  命令：kubectl describe ns ns名称
[root@master ~]# kubectl describe ns default
Name:         default
Labels:       <none>
Annotations:  <none>
Status:       Active  # Active 命名空间正在使用中  Terminating 正在删除命名空间

# ResourceQuota 针对namespace做的资源限制
# LimitRange针对namespace中的每个组件做的资源限制
No resource quota.
No LimitRange resource.
```

#### 创建

```shell script
# 创建namespace
[root@master ~]# kubectl create ns dev
namespace/dev created
```

#### 删除

```shell script
# 删除namespace
[root@master ~]# kubectl delete ns dev
namespace "dev" deleted
```

#### 配置方式

准备一个ns-dev.yaml文件

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dev
```

- 创建：`kubectl create -f ns-dev.yaml`

- 删除：`kubectl delete -f ns-dev.yaml`

### pod

pod是Kubernetes集群进行管理的最小单元，应用程序要运行就需要部署在容器中，而容器存在于pod中。

pod可以认为是对容器的封装，一个pod中可以存在一个或多个容器。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200407121501907.png)

Kubernetes集群环境运行后，集群中的默认组件也都是以pod方式运行的。

```shell script
[root@master ~]# kubectl get pod -n kube-system
NAMESPACE     NAME                             READY   STATUS    RESTARTS   AGE
kube-system   coredns-6955765f44-68g6v         1/1     Running   0          2d1h
kube-system   coredns-6955765f44-cs5r8         1/1     Running   0          2d1h
kube-system   etcd-master                      1/1     Running   0          2d1h
kube-system   kube-apiserver-master            1/1     Running   0          2d1h
kube-system   kube-controller-manager-master   1/1     Running   0          2d1h
kube-system   kube-flannel-ds-amd64-47r25      1/1     Running   0          2d1h
kube-system   kube-flannel-ds-amd64-ls5lh      1/1     Running   0          2d1h
kube-system   kube-proxy-685tk                 1/1     Running   0          2d1h
kube-system   kube-proxy-87spt                 1/1     Running   0          2d1h
kube-system   kube-scheduler-master            1/1     Running   0          2d1h
```

#### 创建并运行

```shell script
[root@master ~]# kubectl run nginx --image=nginx:latest --port=80 --namespace dev
deployment.apps/nginx created
```

命令格式：`kubectl run pod控制器名称 [参数]`，`--image`指定Pod的镜像，`--port`指定端口，`--namespace`指定namespace。

#### 查看pod

```shell script
# 查看Pod基本信息
[root@master ~]# kubectl get pods -n dev
NAME    READY   STATUS    RESTARTS   AGE
nginx   1/1     Running   0          43s

# 查看Pod的详细信息
[root@master ~]# kubectl describe pod nginx -n dev
Name:         nginx
Namespace:    dev
Priority:     0
Node:         node1/192.168.5.4
Start Time:   Wed, 08 May 2021 09:29:24 +0800
Labels:       pod-template-hash=5ff7956ff6
              run=nginx
Annotations:  <none>
Status:       Running
IP:           10.244.1.23
IPs:
  IP:           10.244.1.23
Controlled By:  ReplicaSet/nginx
Containers:
  nginx:
    Container ID:   docker://4c62b8c0648d2512380f4ffa5da2c99d16e05634979973449c98e9b829f6253c
    Image:          nginx:latest
    Image ID:       docker-pullable://nginx@sha256:485b610fefec7ff6c463ced9623314a04ed67e3945b9c08d7e53a47f6d108dc7
    Port:           80/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Wed, 08 May 2021 09:30:01 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-hwvvw (ro)
Conditions:
  Type              Status
  Initialized       True
  Ready             True
  ContainersReady   True
  PodScheduled      True
Volumes:
  default-token-hwvvw:
    Type:        Secret (a volume populated by a Secret)
    SecretName:  default-token-hwvvw
    Optional:    false
QoS Class:       BestEffort
Node-Selectors:  <none>
Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
                 node.kubernetes.io/unreachable:NoExecute for 300s
Events:
  Type    Reason     Age        From               Message
  ----    ------     ----       ----               -------
  Normal  Scheduled  <unknown>  default-scheduler  Successfully assigned dev/nginx-5ff7956ff6-fg2db to node1
  Normal  Pulling    4m11s      kubelet, node1     Pulling image "nginx:latest"
  Normal  Pulled     3m36s      kubelet, node1     Successfully pulled image "nginx:latest"
  Normal  Created    3m36s      kubelet, node1     Created container nginx
  Normal  Started    3m36s      kubelet, node1     Started container nginx
```

#### 访问pod

```shell script
# 获取podIP
[root@master ~]# kubectl get pods -n dev -o wide
NAME    READY   STATUS    RESTARTS   AGE    IP             NODE    ... 
nginx   1/1     Running   0          190s   10.244.1.23   node1   ...

#访问POD
[root@master ~]# curl http://10.244.1.23:80
<!DOCTYPE html>
<html>
<head>
	<title>Welcome to nginx!</title>
</head>
<body>
	<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

#### 删除pod

```shell script
# 删除Pod
[root@master ~]# kubectl delete pod nginx -n dev
pod "nginx" deleted

# 显示删除Pod成功，但是再查询，发现又新产生了一个 
[root@master ~]# kubectl get pods -n dev
NAME    READY   STATUS    RESTARTS   AGE
nginx   1/1     Running   0          21s

# 这是因为当前Pod是由Pod控制器创建的，控制器会监控Pod状况，一旦发现Pod死亡，会立即重建
# 所以要想删除Pod，必须删除Pod控制器
# 先来查询一下当前namespace下的Pod控制器
[root@master ~]# kubectl get deploy -n  dev
NAME    READY   UP-TO-DATE   AVAILABLE   AGE
nginx   1/1     1            1           9m7s

# 接下来，删除该Pod 所属的 Pod控制器
[root@master ~]# kubectl delete deploy nginx -n dev
deployment.apps "nginx" deleted

# 稍等片刻，再查询Pod，发现Pod被删除了
[root@master ~]# kubectl get pods -n dev
No resources found in dev namespace.
```

#### 配置方式

创建一个pod-nginx.yaml文件.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
  namespace: dev
# 对 Pod 预期行为的规约
spec:
  containers:
  - image: nginx:latest
    name: pod
    ports:
    - name: nginx-port
      containerPort: 80
      protocol: TCP
```

- 创建：`kubectl create -f pod-nginx.yaml`。

- 删除：`kubectl delete -f pod-nginx.yaml`。

### label

label的作用是在资源上添加标识（打标签），用来对资源进行区分和选择。

Label的特点：

- 一个Label会以`key:value`键值对的形式附加到各种对象上，如Node、Pod、Service等。

- 一个资源对象可以定义任意数量的Label ，同一个Label也可以被添加到任意数量的资源对象上去。

- Label通常在资源对象定义时确定，当然也可以在对象创建后动态添加或者删除。

可以通过Label实现资源的多维度分组，以便灵活、方便地进行资源分配、调度、配置、部署等管理工作。

一些常用的Label 示例如下：

- 版本标签：`"version":"release", "version":"stable"`

- 环境标签：`"environment":"dev", "environment":"test", "environment":"pro"`

- 架构标签：`"tier":"frontend", "tier":"backend"`

当标签定义完毕之后，还要考虑到标签的选择，这就要使用到Label Selector。Label用于给某个资源对象定义标识，Label Selector用于查询和筛选拥有某些标签的资源对象。

当前有两种Label Selector：

- 基于等式的Label Selector

    - name = slave: 选择所有包含Label中 key="name" 且 value="slave" 的对象。
    
    - env != production: 选择所有包括Label中的 key="env" 且 value!="production" 的对象。

- 基于集合的Label Selector

    - name in (master, slave): 选择所有包含Label中的 key="name" 且 value="master"或"slave" 的对象。
    
    - name not in (frontend): 选择所有包含Label中的 key="name" 且 value不等于"frontend" 的对象。

标签的选择条件可以使用多个，此时将多个Label Selector进行组合，使用逗号","进行分隔即可。例如：`name=slave, env!=production`，`name not in (frontend), env!=production`。

#### 命令方式

```shell script
# 为pod资源打标签
[root@master ~]# kubectl label pod nginx-pod version=1.0 -n dev
pod/nginx-pod labeled

# 为pod资源更新标签
[root@master ~]# kubectl label pod nginx-pod version=2.0 -n dev --overwrite
pod/nginx-pod labeled

# 查看标签
[root@master ~]# kubectl get pod nginx-pod  -n dev --show-labels
NAME        READY   STATUS    RESTARTS   AGE   LABELS
nginx-pod   1/1     Running   0          10m   version=2.0

# 筛选标签
[root@master ~]# kubectl get pod -n dev -l version=2.0  --show-labels
NAME        READY   STATUS    RESTARTS   AGE   LABELS
nginx-pod   1/1     Running   0          17m   version=2.0

[root@master ~]# kubectl get pod -n dev -l version!=2.0 --show-labels
No resources found in dev namespace.

#删除标签
[root@master ~]# kubectl label pod nginx-pod -n dev tier-
pod/nginx unlabeled
```

在标签键的末尾添加减号（-）表示删除该标签。

#### 配置方式

创建一个`pod-nginx.yaml`文件。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
  namespace: dev
  labels:
    version: "3.0" 
    env: "test"
spec:
  containers:
  - image: nginx:latest
    name: pod
    ports:
    - name: nginx-port
      containerPort: 80
      protocol: TCP
```

执行命令`kubectl apply -f pod-nginx.yaml`。

### pod控制器

在kubernetes中，Pod是最小的控制单元，但是kubernetes很少直接控制Pod，一般都是通过Pod控制器来完成的。Pod控制器用于pod的管理，确保pod资源符合预期的状态，当pod的资源出现故障时，会尝试进行重启或重建pod。

在kubernetes中Pod控制器的种类有很多，这里仅介绍 Deployment pod控制器。

Deployment适用场景：

- Deployment：通过控制ReplicaSet来控制Pod，并支持滚动升级、版本回退。

- ReplicaSet：保证指定数量的Pod运行，并支持Pod数量变更，镜像版本变更。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200408193950807.png)

#### 命令方式

```shell script
# 命令格式: kubectl create pod控制器名称  [参数] 
# --image      指定pod的镜像
# --port       指定端口
# --replicas   指定创建pod数量
# --namespace  指定namespace
[root@master ~]# kubectl run nginx --image=nginx:latest --port=80 --replicas=3 -n dev
deployment.apps/nginx created

# 查看创建的Pod
[root@master ~]# kubectl get pods -n dev
NAME                     READY   STATUS    RESTARTS   AGE
nginx-5ff7956ff6-6k8cb   1/1     Running   0          19s
nginx-5ff7956ff6-jxfjt   1/1     Running   0          19s
nginx-5ff7956ff6-v6jqw   1/1     Running   0          19s

# 查看 deployment 的信息
[root@master ~]# kubectl get deploy -n dev
NAME    READY   UP-TO-DATE   AVAILABLE   AGE
nginx   3/3     3            3           2m42s

# 查看 deployment 简要信息   UP-TO-DATE：成功升级的副本数量   AVAILABLE：可用副本的数量
[root@master ~]# kubectl get deploy -n dev -o wide
NAME    READY UP-TO-DATE  AVAILABLE   AGE     CONTAINERS   IMAGES              SELECTOR
nginx   3/3     3         3           2m51s   nginx        nginx:latest        run=nginx

# 查看deployment的详细信息
[root@master ~]# kubectl describe deploy nginx -n dev
Name:                   nginx
Namespace:              dev
CreationTimestamp:      Wed, 08 May 2021 11:14:14 +0800
Labels:                 run=nginx
Annotations:            deployment.kubernetes.io/revision: 1
Selector:               run=nginx
Replicas:               3 desired | 3 updated | 3 total | 3 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  25% max unavailable, 25% max surge
Pod Template:
  Labels:  run=nginx
  Containers:
   nginx:
    Image:        nginx:latest
    Port:         80/TCP
    Host Port:    0/TCP
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Available      True    MinimumReplicasAvailable
  Progressing    True    NewReplicaSetAvailable
OldReplicaSets:  <none>
NewReplicaSet:   nginx-5ff7956ff6 (3/3 replicas created)
Events:
  Type    Reason             Age    From                   Message
  ----    ------             ----   ----                   -------
  Normal  ScalingReplicaSet  5m43s  deployment-controller  Scaled up replicaset nginx-5ff7956ff6 to 3

# 删除 pod控制器
[root@master ~]# kubectl delete deploy nginx -n dev
deployment.apps "nginx" deleted
```

#### 配置方式

创建一个`deploy-nginx.yaml`文件。

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
  namespace: dev
spec:
  replicas: 3
  selector:
    matchLabels:
      run: nginx
  template:
    metadata:
      labels:
        run: nginx
    spec:
      containers:
      - image: nginx:latest
        name: nginx
        ports:
        - containerPort: 80
          protocol: TCP
```

### service

Service可以看作是一组同类 pod 对外访问的接口，借助Service，应用可以方便地实现服务发现和负载均衡。这样当一个访问容器服务请求时，首先会经过Service。统一管理请求。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200408194716912.png)

#### 创建集群内部可访问的Service

```shell script
# 暴露Service
[root@master ~]# kubectl expose deploy nginx --name=svc-nginx1 --type=ClusterIP --port=80 --target-port=80 -n dev
service/svc-nginx1 exposed

# 查看service
# 命令：kubectl get services svc-nginx1 -n dev -o wide 其中 services 可以简写为 svc
[root@master ~]# kubectl get svc svc-nginx1 -n dev -o wide
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)   AGE     SELECTOR
svc-nginx1   ClusterIP   10.109.179.231   <none>        80/TCP    3m51s   run=nginx

# 这里产生了一个CLUSTER-IP，这就是service的IP，在Service的生命周期中，这个地址是不会变动的
# 可以通过这个IP访问当前service对应的 pod

[root@master ~]# curl 10.109.179.231:80
<!DOCTYPE html>
<html>
<head>
	<title>Welcome to nginx!</title>
</head>
<body>
	<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

#### 创建集群外部也能访问的Service

```shell script
# 将 --type=ClusterIP 修改为 --type=NodePort 即可对外访问
[root@master ~]# kubectl expose deploy nginx --name=svc-nginx2 --type=NodePort --port=80 --target-port=80 -n dev
service/svc-nginx2 exposed

[root@master ~]# kubectl get svc  svc-nginx2  -n dev -o wide
NAME          TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE    SELECTOR
svc-nginx2    NodePort    10.100.94.0      <none>        80:31928/TCP   9s     run=nginx
```

选择就可以集群外部访问`http://Kubernetes节点IP:31928`提供的服务。

#### 删除Service

```shell script
[root@master ~]# kubectl delete svc svc-nginx-1 -n dev
service "svc-nginx-1" deleted
```

#### 配置方式

创建一个`svc-nginx.yaml`文件。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: svc-nginx
  namespace: dev
spec:
  clusterIP: 10.109.179.231 # 固定svc的内网ip
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:
    run: nginx
  type: ClusterIP
```

然后就可以执行对应的创建和删除命令了：

- 创建：`kubectl create -f svc-nginx.yaml`

- 删除：`kubectl delete -f svc-nginx.yaml`

至此，在kubernetes集群中可以实现一个服务的简单部署和访问了。但是如果想要更好的使用kubernetes，就需要深入学习这几种资源的细节和原理。

## Pod

### pod是什么

#### pod结构

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200407121501907-1626781151898.png)

每个pod中可以包含一个或多个容器，而这些容器可以分为两类：

- 用户程序所在的容器，数量可多可少。

- Pause容器，这是每个Pod都会有的一个根容器，它的作用有两个：

    - 可以以它为依据，评估整个Pod的健康状态。
    - 可以在根容器上设置IP地址，其它容器都共享此IP（Pod的IP），以实现Pod内部的网络通信。（Pod内部的通讯采用虚拟二层网络技术来实现，我们当前环境使用的是Flannel）

#### pod配置项

pod常见的配置项。

```yaml
apiVersion: v1        # 🎉 必选，版本号，例如v1
kind: Pod             # 🎉 必选，资源类型，例如 Pod
metadata:             # 🎉 必选，元数据
  name: string        # 🎉 必选，Pod名称
  namespace: string   # Pod所属的命名空间,默认为"default"
  labels:             # 自定义标签列表
    - name: string      　          
spec:               # 🎉 必选，Pod中容器的详细定义
  containers:       # 🎉 必选，Pod中容器列表
  - name: string    # 🎉 必选，容器名称
    image: string   # 🎉 必选，容器的镜像名称
    imagePullPolicy: [ Always|Never|IfNotPresent ]  # 获取镜像的策略 
    command: [string]   # 容器的启动命令列表，如不指定，使用打包时使用的启动命令
    args: [string]      # 容器的启动命令参数列表
    workingDir: string  # 容器的工作目录
    volumeMounts:       # 挂载到容器内部的存储卷配置
    - name: string      # 引用pod定义的共享存储卷的名称，需用volumes[]部分定义的的卷名
      mountPath: string # 存储卷在容器内mount的绝对路径，应少于512字符
      readOnly: boolean # 是否为只读模式
    ports:              # 需要暴露的端口库号列表
    - name: string        # 端口的名称
      containerPort: int  # 容器需要监听的端口号
      hostPort: int       # 容器所在主机需要监听的端口号，默认与Container相同
      protocol: string    # 端口协议，支持TCP和UDP，默认TCP
    env:            # 容器运行前需设置的环境变量列表
    - name: string  # 环境变量名称
      value: string # 环境变量的值
    resources: # 资源限制和请求的设置
      limits:  # 资源限制的设置
        cpu: string     # Cpu的限制，单位为core数，将用于docker run --cpu-shares参数
        memory: string  # 内存限制，单位可以为Mib/Gib，将用于docker run --memory参数
      requests:         # 资源请求的设置
        cpu: string     # Cpu请求，容器启动的初始可用数量
        memory: string  # 内存请求,容器启动的初始可用数量
    lifecycle:      # 生命周期钩子
        postStart:  # 容器启动后立即执行此钩子,如果执行失败,会根据重启策略进行重启
        preStop:    # 容器终止前执行此钩子,无论结果如何,容器都会终止
    livenessProbe:  # 对Pod内各容器健康检查的设置，当探测无响应几次后将自动重启该容器
      exec:         # 对Pod容器内检查方式设置为exec方式
        command: [string]  # exec方式需要制定的命令或脚本
      httpGet:             # 对Pod内个容器健康检查方法设置为HttpGet，需要制定Path、port
        path: string
        port: number
        host: string
        scheme: string
        HttpHeaders:
        - name: string
          value: string
      tcpSocket:     # 对Pod内个容器健康检查方式设置为tcpSocket方式
         port: number
       initialDelaySeconds: 0     # 容器启动完成后首次探测的时间，单位为秒
       timeoutSeconds: 0          # 对容器健康检查探测等待响应的超时时间，单位秒，默认1秒
       periodSeconds: 0           # 对容器监控检查的定期探测时间设置，单位秒，默认10秒一次
       successThreshold: 0
       failureThreshold: 0
       securityContext:
         privileged: false
  restartPolicy: [Always | Never | OnFailure]  # Pod的重启策略
  nodeName: <string>    # 设置NodeName表示将该Pod调度到指定到名称的node节点上
  nodeSelector: obeject # 设置NodeSelector表示将该Pod调度到包含这个label的node上
  imagePullSecrets:     # Pull镜像时使用的secret名称，以key：secretkey格式指定
  - name: string
  hostNetwork: false   # 是否使用主机网络模式，默认为false，如果设置为true，表示使用宿主机网络
  volumes:             # 在该pod上定义共享存储卷列表
  - name: string       # 共享存储卷名称 （volumes类型有很多种）
    emptyDir: {}       # 类型为emtyDir的存储卷，与Pod同生命周期的一个临时目录。为空值
    hostPath: string   # 类型为hostPath的存储卷，表示挂载Pod所在宿主机的目录
      path: string     # Pod所在宿主机的目录，将被用于同期中mount的目录
    secret:       　　　# 类型为secret的存储卷，挂载集群与定义的secret对象到容器内部
      scretname: string  
      items:     
      - key: string
        path: string
    configMap:         # 类型为configMap的存储卷，挂载预定义的configMap对象到容器内部
      name: string
      items:
      - key: string
        path: string
```

对于这些命令不需要背，在使用时查文档即可。可通过`kubectl explain pod`命令来查看每种资源的可配置项：

- kubectl explain 资源类型（查看某种资源可以配置的一级属性）

```shell script
[root@k8s-master01 ~]# kubectl explain pod
KIND:     Pod
VERSION:  v1
FIELDS:
   apiVersion   <string>
   kind <string>
   metadata     <Object>
   spec <Object>
   status       <Object>
```

- kubectl explain 资源类型.属性（查看一级属性的子属性）

```shell script
[root@k8s-master01 ~]# kubectl explain pod.metadata
KIND:     Pod
VERSION:  v1
RESOURCE: metadata <Object>
FIELDS:
   annotations  <map[string]string>
   clusterName  <string>
   creationTimestamp    <string>
   deletionGracePeriodSeconds   <integer>
   deletionTimestamp    <string>
   finalizers   <[]string>
   generateName <string>
   generation   <integer>
   labels       <map[string]string>
   managedFields        <[]Object>
   name <string>
   namespace    <string>
   ownerReferences      <[]Object>
   resourceVersion      <string>
   selfLink     <string>
   uid  <string>
```

在kubernetes中大部分资源类型的一级属性都是一样的，主要包含5部分：

- apiVersion 版本，由kubernetes内部定义，版本号必须可以用 kubectl api-versions 查询到。

- kind 类型，由kubernetes内部定义，版本号必须可以用 kubectl api-resources 查询到。

- metadata 元数据，主要是资源标识和说明，常用的有name、namespace、labels等。

- spec 描述，这是配置中最重要的一部分，里面是对各种资源配置的详细描述。

- status 状态信息，里面的内容不需要定义，由kubernetes自动生成。

### `pod.spec.containers`配置

`pod.spec.containers`是pod配置中最为关键的一项配置。

```shell script
[root@k8s-master01 ~]# kubectl explain pod.spec.containers
KIND:     Pod
VERSION:  v1
RESOURCE: containers <[]Object>   # 数组，代表可以有多个容器
FIELDS:
   name  <string>            # 容器名称
   image <string>            # 容器需要的镜像地址
   imagePullPolicy  <string> # 镜像拉取策略 
   command  <[]string>       # 容器的启动命令列表，如不指定，使用打包时使用的启动命令
   args     <[]string>       # 容器的启动命令需要的参数列表
   env      <[]Object>       # 容器环境变量的配置
   ports    <[]Object>       # 容器需要暴露的端口号列表
   resources <Object>        # 资源限制和资源请求的设置
```

#### 基本配置

创建`pod-base.yaml`文件。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-base
  namespace: dev
  labels:
    user: heima
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
```

创建 pod：

```shell script
# 创建Pod
[root@k8s-master01 pod]# kubectl apply -f pod-base.yaml
pod/pod-base created

# 查看Pod状况
[root@master ~]# kubectl get pod -n dev
NAME       READY   STATUS    RESTARTS   AGE
pod-base   1/1     Running   0          21s

# 可以通过describe查看内部的详情
[root@k8s-master01 pod]# kubectl describe pod pod-base -n dev
```

#### 镜像拉取

创建`pod-imagepullpolicy.yaml`文件。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-imagepullpolicy
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    imagePullPolicy: Never # 用于设置镜像拉取策略
```

创建 pod：

```shell script
# 创建Pod
[root@k8s-master01 pod]# kubectl create -f pod-imagepullpolicy.yaml
pod/pod-imagepullpolicy created

# 查看Pod详情
[root@k8s-master01 pod]# kubectl describe pod pod-imagepullpolicy -n dev
......
Events:
  Type     Reason     Age               From               Message
  ----     ------     ----              ----               -------
  Normal   Scheduled  <unknown>         default-scheduler  Successfully assigned dev/pod-imagePullPolicy to node1
  Normal   Pulling    32s               kubelet, node1     Pulling image "nginx:1.17.1"
  Normal   Pulled     26s               kubelet, node1     Successfully pulled image "nginx:1.17.1"
  Normal   Created    26s               kubelet, node1     Created container nginx
  Normal   Started    25s               kubelet, node1     Started container nginx
```

imagePullPolicy，用于设置镜像拉取策略，kubernetes支持配置三种拉取策略：

- Always：总是从远程仓库拉取镜像。

- IfNotPresent：本地有则使用本地镜像，本地没有则从远程仓库拉取镜像。

- Never：只使用本地镜像，从不去远程仓库拉取，本地没有就报错。

如果镜像tag为具体版本号， 默认策略是：IfNotPresent。

如果镜像tag为 latest（最新版本） ，默认策略是always。

#### 启动命令

#### 环境变量

#### 端口设置

#### 资源配额

### pod生命周期

### pod调度

## Pod控制器

## Service

## 数据存储

## 安全认证

## 可视化界面





















