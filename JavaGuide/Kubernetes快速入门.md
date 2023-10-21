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

### 容器配置

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

`pod.spec.containers.command`属性用于在pod中的**容器初始化完成后**执行一个命令。

创建`pod-command.yaml`文件：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-command
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    command: ["/bin/sh","-c","touch /tmp/hello.txt;while true;do /bin/echo $(date +%T) >> /tmp/hello.txt; sleep 3; done;"]
```

- `"/bin/sh","-c"`表示使用sh执行命令。

- `touch /tmp/hello.txt;`命令表示创建一个/tmp/hello.txt 文件。

- `while true;do /bin/echo $(date +%T) >> /tmp/hello.txt; sleep 3; done;`脚本内容表示每隔3秒向文件中写入当前时间。

创建pod：

```shell script
[root@k8s-master01 pod]# kubectl create  -f pod-command.yaml
pod/pod-command created

# 查看Pod状态
[root@k8s-master01 pod]# kubectl get pods pod-command -n dev
NAME          READY   STATUS   RESTARTS   AGE
pod-command   1/1     Runing   0          2s

# 进入pod中的nginx容器，查看文件内容
# 进入容器命令：kubectl exec  pod名称 -n 命名空间 -it -c 容器名称 /bin/sh
[root@k8s-master01 pod]# kubectl exec pod-command -n dev -it -c nginx /bin/sh
/ # tail -f /tmp/hello.txt
14:44:19
14:44:22
14:44:25
```

需要注意的是，如果在配置文件中设置了容器启动时要执行的命令及其参数，那么容器镜像中自带的命令与参数将会被覆盖而不再执行。如果配置文件中只是设置了参数，却没有设置其对应的命令，那么容器镜像中自带的命令会使用该新参数作为其执行时的参数。

#### 环境变量

> 用于在pod中的容器设置环境变量

创建`pod-env.yaml`文件：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-env
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    command: ["/bin/sh","-c","while true;do /bin/echo $(date +%T);sleep 60; done;"]
    env: # 设置环境变量列表
    - name: "username"
      value: "admin"
    - name: "password"
      value: "123456"
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-env.yaml
pod/pod-env created

# 进入容器，输出环境变量
[root@k8s-master01 ~]# kubectl exec pod-env -n dev -c nginx -it /bin/sh
/ # echo $username
admin
/ # echo $password
123456
```

这种方式不是很推荐，推荐将这些配置单独存储在配置文件中。

#### 端口设置

> 就是用来访问容器中的程序所使用的端口（容器内端口）

`pod.spec.containers.ports`支持的子选项：

```shell script
[root@k8s-master01 ~]# kubectl explain pod.spec.containers.ports
KIND:     Pod
VERSION:  v1
RESOURCE: ports <[]Object>
FIELDS:
   name         <string>  # 端口名称，如果指定，必须保证name在pod中是唯一的		
   containerPort<integer> # 容器要监听的端口(0<x<65536)
   hostPort     <integer> # 容器要在主机上公开的端口，如果设置，主机上只能运行容器的一个副本(一般省略) 
   hostIP       <string>  # 要将外部端口绑定到的主机IP(一般省略)
   protocol     <string>  # 端口协议。必须是UDP、TCP或SCTP。默认为“TCP”。
```

创建`pod-ports.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-ports
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    ports: # 设置容器暴露的端口列表
    - name: nginx-port
      containerPort: 80
      protocol: TCP
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-ports.yaml
pod/pod-ports created

# 查看pod
[root@k8s-master01 ~]# kubectl get pod pod-ports -n dev -o yaml
......
spec:
  containers:
  - image: nginx:1.17.1
    imagePullPolicy: IfNotPresent
    name: nginx
    ports:
    - containerPort: 80
      name: nginx-port
      protocol: TCP
......
```

#### 资源配额

> 限制容器的内存和CPU的资源配额

创建`pod-resources.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-resources
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    resources:         # 资源配额
      limits:          # 限制资源（上限）
        cpu: "2"       # CPU限制，单位是core数，可以为整数或小数
        memory: "10Gi" # 内存限制，可以使用Gi、Mi、G、M等形式（Mi：1024*1024B；M：1000*1000B；Gi/G类同）
      requests:        # 请求资源（下限）
        cpu: "1"       # CPU限制
        memory: "10Mi" # 内存限制
```

- limits：用于限制运行时容器的最大占用资源，当容器占用资源超过limits设置时会被终止，并进行重启。

- requests ：用于设置容器需要的最小资源，如果环境资源达不到requests设置，容器将无法启动。

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create  -f pod-resources.yaml
pod/pod-resources created

# 查看发现pod运行正常
[root@k8s-master01 ~]# kubectl get pod pod-resources -n dev
NAME            READY   STATUS    RESTARTS   AGE  
pod-resources   1/1     Running   0          39s   

# 删除pod
[root@k8s-master01 ~]# kubectl delete  -f pod-resources.yaml
pod "pod-resources" deleted

# 编辑pod，修改resources.requests.memory的值为10Gi
[root@k8s-master01 ~]# vim pod-resources.yaml

[root@k8s-master01 ~]# kubectl create  -f pod-resources.yaml
pod/pod-resources created

# 查看Pod状态，发现Pod启动失败
[root@k8s-master01 ~]# kubectl get pod pod-resources -n dev -o wide
NAME            READY   STATUS    RESTARTS   AGE          
pod-resources   0/1     Pending   0          20s 

# 查看pod详情会发现，如下提示
[root@k8s-master01 ~]# kubectl describe pod pod-resources -n dev
......
Warning  FailedScheduling  35s   default-scheduler  0/3 nodes are available: 1 node(s) had taint {node-role.kubernetes.io/master: }, that the pod didn't tolerate, 2 Insufficient memory.

# Insufficient memory (内存不足)
```

### pod生命周期

pod对象从创建到能使用的这段时间范围称为pod的生命周期，它主要包含下面的过程：

- pod的创建过程

- 运行初始化容器（init container）

- 运行主容器（main container）

    - 容器启动后钩子（post start）、容器终止前钩子（pre stop）
    
    - 容器的存活性探测（liveness probe）、容器的就绪性探测（readiness probe）

- pod的终止过程

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200412111402706-1626782188724.png)

而在pod的生命周期中，会出现5种状态（相位），分别如下所示：

- Pending（挂起）：apiServer已经创建了pod资源对象 ，但它**尚未被调度完成**或仍**处于拉取镜像的过程**中。

- Running（运行）：pod已经被调度至某个节点，并且所有容器都已经被 kubelet 创建完成。

- Succeeded（成功）：pod中的所有容器都已经成功终止并且不会被重启。

- Failed（失败）：所有容器都已经终止，但至少有一个容器终止失败，即容器返回了非0值的退出状态。

- Unknown（未知）：apiServer无法正常获取到pod对象的状态信息，通常由网络通信失败所导致。

#### pod的创建过程

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200406184656917-1626782168787.png)

1. 用户通过kubectl或其他api客户端提交需要创建的pod信息给apiServer。

2. apiServer开始生成pod对象的信息，并将信息存入etcd，然后返回确认信息至客户端。

3. apiServer开始反映etcd中的pod对象的变化，其它组件使用watch机制来跟踪检查apiServer上的变动。

4. scheduler发现有新的pod对象要创建，开始为Pod分配主机并将结果信息更新至apiServer。

5. node节点上的kubelet发现有pod调度过来，尝试调用docker启动容器，并将结果回送至apiServer。

6. apiServer将接收到的pod状态信息存入etcd中。

#### 初始化容器

> 简单理解为Spring Bean的初始化操作

初始化容器是在pod的主容器启动之前要运行的容器，主要是做一些主容器的前置工作，它具有两大特征：

1. 初始化容器必须运行完成直至结束，若某初始化容器运行失败，那么kubernetes需要重启它直到成功完成。

2. 初始化容器必须按照定义的顺序执行，当且仅当前一个成功之后，后面的一个才能运行。

在实际场景中，会有nginx在运行前需要先连接mysql和redis的场景（为简化测试，只做一个ping操作）。

创建`pod-initcontainer.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-initcontainer
  namespace: dev
spec:
  containers:
  - name: main-container
    image: nginx:1.17.1
    ports: 
    - name: nginx-port
      containerPort: 80
  initContainers:
  - name: test-mysql
    image: mysql:8.0.21
    command: ['sh', '-c', 'until ping 192.168.90.14 -c 1 ; do echo waiting for mysql...; sleep 2; done;']
  - name: test-redis
    image: redis:7.2
    command: ['sh', '-c', 'until ping 192.168.90.15 -c 1 ; do echo waiting for reids...; sleep 2; done;']
```

创建pod：

```shell script
# 为当前服务器新增两个ip，模拟redis、mysql
[root@k8s-master01 ~]# ifconfig ens33:1 192.168.90.14 netmask 255.255.255.0 up
[root@k8s-master01 ~]# ifconfig ens33:2 192.168.90.15 netmask 255.255.255.0 up

[root@k8s-master01 ~]# kubectl create -f pod-initcontainer.yaml
pod/pod-initcontainer created

# 查看pod状态
[root@k8s-master01 ~]# kubectl describe pod  pod-initcontainer -n dev
......

# 动态查看pod
[root@k8s-master01 ~]# kubectl get pods pod-initcontainer -n dev -w
NAME                             READY   STATUS     RESTARTS   AGE
......
```

#### 钩子函数

> 可以理解为在执行到pod的生命周期的指定阶段时的**回调**

post start：容器创建之后执行，如果失败了会重启容器。

pre stop ：容器终止之前执行，执行完成之后容器将成功终止，在其完成之前会阻塞删除容器的操作。

钩子处理器支持使用下面三种方式定义动作：

```yaml
# Exec命令：在容器内执行一次命令
......
    lifecycle:
        postStart: 
          exec:
            command:
            - cat
            - /tmp/healthy

# TCPSocket：在当前容器尝试访问指定的socket
    lifecycle:
        postStart:
          tcpSocket:
            port: 8080

# HTTPGet：在当前容器中向某url发起http请求
    lifecycle:
        postStart:
          httpGet:
            path: / #URI地址
            port: 80 #端口号
            host: 192.168.5.3 #主机地址
            scheme: HTTP #支持的协议：http或者https
```

以exec方式为例，演示下钩子函数的使用，创建`pod-hook-exec.yaml`文件：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-hook-exec
  namespace: dev
spec:
  containers:
  - name: main-container
    image: nginx:1.17.1
    ports:
    - name: nginx-port
      containerPort: 80
    lifecycle:
      postStart: 
        exec: # 在容器启动的时候执行一个命令，修改掉nginx的默认首页内容
          command: ["/bin/sh", "-c", "echo postStart... > /usr/share/nginx/html/index.html"]
      preStop:
        exec: # 在容器停止之前停止nginx服务
          command: ["/usr/sbin/nginx","-s","quit"]
```

创建pod

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-hook-exec.yaml
pod/pod-hook-exec created

# 查看pod
[root@k8s-master01 ~]# kubectl get pods  pod-hook-exec -n dev -o wide
NAME           READY   STATUS     RESTARTS   AGE    IP            NODE    
pod-hook-exec  1/1     Running    0          29s    10.244.2.48   node2

# 访问pod
[root@k8s-master01 ~]# curl 10.244.2.48
postStart...
```

#### 容器探测

容器探测用于检测容器中的应用实例是否正常工作，如果实例的状态不符合预期，则按照预定规则对其进行处理。

kubernetes提供了两种探针来实现容器探测：livenessProbe 决定是否重启容器，readinessProbe 决定是否将请求转发给容器。

上面两种探针目前均支持三种探测方式：

```yaml
# Exec命令：在容器内执行一次命令，如果命令执行的退出码为0，则认为程序正常，否则不正常
    livenessProbe:
        exec:
          command:
          - cat
          - /tmp/healthy

# TCPSocket：将会尝试访问一个用户容器的端口，如果能够建立这条连接，则认为程序正常，否则不正常
    livenessProbe:
        tcpSocket:
          port: 8080

# HTTPGet：调用容器内Web应用的URL，如果返回的状态码在200和399之间，则认为程序正常，否则不正常
    livenessProbe:
        httpGet:
          path: / #URI地址
          port: 80 #端口号
          host: 127.0.0.1 #主机地址
          scheme: HTTP #支持的协议：http或者https
```

`pod.spec.containers.livenessProbe`子配置项：

```shell script
[root@k8s-master01 ~]# kubectl explain pod.spec.containers.livenessProbe
FIELDS:
   exec <Object>  
   tcpSocket    <Object>
   httpGet      <Object>
   initialDelaySeconds  <integer>  # 容器启动后等待多少秒执行第一次探测
   timeoutSeconds       <integer>  # 探测超时时间。默认1秒，最小1秒
   periodSeconds        <integer>  # 执行探测的频率。默认是10秒，最小1秒
   failureThreshold     <integer>  # 连续探测失败多少次才被认定为失败。默认是3。最小值是1
   successThreshold     <integer>  # 连续探测成功多少次才被认定为成功。默认是1
```

以liveness probes为例演示。

##### exec

创建`pod-liveness-exec.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-liveness-exec
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    ports: 
    - name: nginx-port
      containerPort: 80
    livenessProbe:
      exec:
        command: ["/bin/cat","/tmp/hello.txt"] # 执行一个查看文件的命令
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-liveness-exec.yaml
pod/pod-liveness-exec created

# pod详情
[root@k8s-master01 ~]# kubectl describe pods pod-liveness-exec -n dev
......
  Normal   Created    20s (x2 over 50s)  kubelet, node1     Created container nginx
  Normal   Started    20s (x2 over 50s)  kubelet, node1     Started container nginx
  Normal   Killing    20s                kubelet, node1     Container nginx failed liveness probe, will be restarted
  Warning  Unhealthy  0s (x5 over 40s)   kubelet, node1     Liveness probe failed: cat: can't open '/tmp/hello11.txt': No such file or directory


[root@k8s-master01 ~]# kubectl get pods pod-liveness-exec -n dev
NAME                READY   STATUS             RESTARTS   AGE
pod-liveness-exec   0/1     CrashLoopBackOff   2          3m19s
```

因为文件不存在，导致容器正常启动失败，从而被容器探测发现，然后尝试重启。并且发现`RESTARTS`的值是增长的。解决方法是创建该文件，重新创建pod即可。

##### TCPSocket

创建`pod-liveness-tcpsocket.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-liveness-tcpsocket
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    ports: 
    - name: nginx-port
      containerPort: 80
    livenessProbe:
      tcpSocket:
        port: 8080 # 尝试访问8080端口
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-liveness-tcpsocket.yaml
pod/pod-liveness-tcpsocket created

# 查看Pod详情
[root@k8s-master01 ~]# kubectl describe pods pod-liveness-tcpsocket -n dev
......
  Normal   Scheduled  31s                            default-scheduler  Successfully assigned dev/pod-liveness-tcpsocket to node2
  Normal   Pulled     <invalid>                      kubelet, node2     Container image "nginx:1.17.1" already present on machine
  Normal   Created    <invalid>                      kubelet, node2     Created container nginx
  Normal   Started    <invalid>                      kubelet, node2     Started container nginx
  Warning  Unhealthy  <invalid> (x2 over <invalid>)  kubelet, node2     Liveness probe failed: dial tcp 10.244.2.44:8080: connect: connection refused
  
[root@k8s-master01 ~]# kubectl get pods pod-liveness-tcpsocket  -n dev
NAME                     READY   STATUS             RESTARTS   AGE
pod-liveness-tcpsocket   0/1     CrashLoopBackOff   2          3m19s
```

尝试访问8080端口，但是失败了。解决方案是修改成一个可以访问的端口，比如80，再试，结果就正常了。

##### HTTPGet

创建`pod-liveness-httpget.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-liveness-httpget
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    ports:
    - name: nginx-port
      containerPort: 80
    livenessProbe:
      httpGet:  # 其实就是访问 http://127.0.0.1:80/hello  
        scheme: HTTP #支持的协议，http或者https
        port: 80 #端口号
        path: /hello #URI地址
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-liveness-httpget.yaml
pod/pod-liveness-httpget created

# 查看Pod详情
[root@k8s-master01 ~]# kubectl describe pod pod-liveness-httpget -n dev
.......
  Normal   Pulled     6s (x3 over 64s)  kubelet, node1     Container image "nginx:1.17.1" already present on machine
  Normal   Created    6s (x3 over 64s)  kubelet, node1     Created container nginx
  Normal   Started    6s (x3 over 63s)  kubelet, node1     Started container nginx
  Warning  Unhealthy  6s (x6 over 56s)  kubelet, node1     Liveness probe failed: HTTP probe failed with statuscode: 404
  Normal   Killing    6s (x2 over 36s)  kubelet, node1     Container nginx failed liveness probe, will be restarted
  
[root@k8s-master01 ~]# kubectl get pod pod-liveness-httpget -n dev
NAME                   READY   STATUS    RESTARTS   AGE
pod-liveness-httpget   1/1     Running   5          3m17s
```

解决方案是修改成一个可以访问的路径path，比如/，再试，结果就正常了。

#### 重启策略

当pod启动失败，被探测到时就会触发pod重启策略，pod的重启策略有3种，分别如下：

- Always：容器失效时，自动重启该容器，这也是默认值。

- OnFailure：容器终止运行且退出码不为0时重启。

- Never：不论状态为何，都不重启该容器。

重启策略适用于pod对象中的所有容器，首次需要重启的容器，将在其需要时立即进行重启，随后再次需要重启的操作将由kubelet延迟一段时间后进行，且反复的重启操作的延迟时长为10s、20s、40s、80s、160s和300s，300s是最大延迟时长。

创建`pod-restartpolicy.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-restartpolicy
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
    ports:
    - name: nginx-port
      containerPort: 80
    livenessProbe:
      httpGet:
        scheme: HTTP
        port: 80
        path: /hello
  restartPolicy: Never # 设置重启策略为Never
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-restartpolicy.yaml
pod/pod-restartpolicy created

# 查看Pod详情，发现nginx容器失败
[root@k8s-master01 ~]# kubectl  describe pods pod-restartpolicy  -n dev
......
  Warning  Unhealthy  15s (x3 over 35s)  kubelet, node1     Liveness probe failed: HTTP probe failed with statuscode: 404
  Normal   Killing    15s                kubelet, node1     Container nginx failed liveness probe
  
# 多等一会，再观察pod的重启次数，发现一直是0，并未重启   
[root@k8s-master01 ~]# kubectl  get pods pod-restartpolicy -n dev
NAME                   READY   STATUS    RESTARTS   AGE
pod-restartpolicy      0/1     Running   0          5min42s
```

#### pod的终止过程

1. 用户向apiServer发送删除pod对象的命令。

2. apiServer中的pod对象信息会随着时间的推移而更新，在宽限期内（默认30s），pod被视为dead。

3. 将pod标记为terminating状态。

4. kubelet在监控到pod对象转为terminating状态的同时启动pod关闭过程。

5. 端点控制器监控到pod对象的关闭行为时，将其从所有匹配到此端点的service资源的端点列表中移除。

6. 如果当前pod对象定义了preStop钩子处理器，则在其标记为terminating后即会以同步的方式启动执行。

7. pod对象中的容器进程收到停止信号。

8. 宽限期结束后，若pod中还存在仍在运行的进程，那么pod对象会收到立即终止的信号。

9. kubelet请求apiServer将此pod资源的宽限期设置为0从而完成删除操作，此时pod对于用户已不可见。

### pod调度

用于控制pod在哪个node节点上运行。

Kubernetes提供四种调度方式：

- 自动调度：运行在哪个节点上完全由Scheduler经过一系列的算法计算得出。

- 定向调度：NodeName、NodeSelector。

- 亲和性调度：NodeAffinity、PodAffinity、PodAntiAffinity。

- 污点（容忍）调度：Taints、Toleration。

#### 定向调度

用于指定pod在哪个node节点或标签上运行。

##### NodeName

即使要调度的node不存在，也会进行调度。没有经过Scheduler的调度逻辑。

创建一个`pod-nodename.yaml`文件：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-nodename
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  nodeName: node1 # 指定调度到node1节点上
```

创建Pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-nodename.yaml
pod/pod-nodename created

# 查看Pod调度到NODE属性，确实是调度到了node1节点上
[root@k8s-master01 ~]# kubectl get pods pod-nodename -n dev -o wide
NAME           READY   STATUS    RESTARTS   AGE   IP            NODE      ......
pod-nodename   1/1     Running   0          56s   10.244.1.87   node1     ...... 

# 接下来，删除pod，修改nodeName的值为node3（并没有node3节点）
[root@k8s-master01 ~]# kubectl delete -f pod-nodename.yaml
pod "pod-nodename" deleted
[root@k8s-master01 ~]# vim pod-nodename.yaml
[root@k8s-master01 ~]# kubectl create -f pod-nodename.yaml
pod/pod-nodename created

# 再次查看，发现已经向Node3节点调度，但是由于不存在node3节点，所以pod无法正常运行
[root@k8s-master01 ~]# kubectl get pods pod-nodename -n dev -o wide
NAME           READY   STATUS    RESTARTS   AGE   IP       NODE    ......
pod-nodename   0/1     Pending   0          6s    <none>   node3   ......     
```

##### NodeSelector

用于将pod调度到指定标签的node节点上。基于kubernetes的label-selector机制实现的。由scheduler使用MatchNodeSelector调度策略处理。

创建一个`pod-nodeselector.yaml`文件：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-nodeselector
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  nodeSelector: 
    nodeenv: pro # 指定调度到具有nodeenv=pro标签的节点上
```

创建pod：

```shell script
# 给node节点添加标签
[root@k8s-master01 ~]# kubectl label nodes node1 nodeenv=pro
node/node2 labeled
[root@k8s-master01 ~]# kubectl label nodes node2 nodeenv=test
node/node2 labeled

[root@k8s-master01 ~]# kubectl create -f pod-nodeselector.yaml
pod/pod-nodeselector created

# 查看Pod调度到NODE属性，确实是调度到了node1节点上
[root@k8s-master01 ~]# kubectl get pods pod-nodeselector -n dev -o wide
NAME               READY   STATUS    RESTARTS   AGE     IP          NODE    ......
pod-nodeselector   1/1     Running   0          47s   10.244.1.87   node1   ......

# 接下来，删除pod，修改nodeSelector的值为nodeenv: xxxx（不存在此标签的节点）
[root@k8s-master01 ~]# kubectl delete -f pod-nodeselector.yaml
pod "pod-nodeselector" deleted
[root@k8s-master01 ~]# vim pod-nodeselector.yaml
[root@k8s-master01 ~]# kubectl create -f pod-nodeselector.yaml
pod/pod-nodeselector created

# 再次查看，发现pod无法正常运行,Node的值为none
[root@k8s-master01 ~]# kubectl get pods -n dev -o wide
NAME               READY   STATUS    RESTARTS   AGE     IP       NODE    
pod-nodeselector   0/1     Pending   0          2m20s   <none>   <none>

# 查看详情,发现node selector匹配失败的提示
[root@k8s-master01 ~]# kubectl describe pods pod-nodeselector -n dev
.......
Events:
  Type     Reason            Age        From               Message
  ----     ------            ----       ----               -------
  Warning  FailedScheduling  <unknown>  default-scheduler  0/3 nodes are available: 3 node(s) didn't match node selector.
```

#### 亲和性调度

比定向调度更灵活，在NodeSelector的基础之上的进行了扩展，如果调度不满足条件，可以通过配置提供默认调度。

亲和性调度（Affinity）主要分为三类：

- nodeAffinity（node亲和性）: 以node为目标，解决pod可以调度到哪些node的问题。

- podAffinity（pod亲和性） : 以pod为目标，解决pod可以和哪些已存在的pod部署在同一个拓扑域中的问题。

- podAntiAffinity（pod反亲和性） : 以pod为目标，解决pod不能和哪些已存在pod部署在同一个拓扑域中的问题。

> 亲和性：如果两个应用频繁交互，那就有必要利用亲和性让两个应用的尽可能的靠近，这样可以减少因网络通信而带来的性能损耗。
>
> 反亲和性：当应用的采用多副本部署时，有必要采用反亲和性让各个应用实例打散分布在各个node上，这样可以提高服务的高可用性。

##### nodeAffinity

可配置项：

```yaml
pod.spec.affinity.nodeAffinity:
  requiredDuringSchedulingIgnoredDuringExecution:  # Node节点必须满足指定的所有规则才可以，相当于硬限制
    nodeSelectorTerms:  # 节点选择列表
      matchFields:      # 按节点字段列出的节点选择器要求列表
      matchExpressions: # 按节点标签列出的节点选择器要求列表(推荐)
        key:      # 键
        values:   # 值
        operator: # 关系符 支持Exists, DoesNotExist, In, NotIn, Gt, Lt
  preferredDuringSchedulingIgnoredDuringExecution: # 优先调度到满足指定的规则的Node，相当于软限制 (倾向)
    preference:    # 一个节点选择器项，与相应的权重相关联
      matchFields: # 按节点字段列出的节点选择器要求列表
      matchExpressions:   # 按节点标签列出的节点选择器要求列表(推荐)
        key:      # 键
        values:   # 值
        operator: # 关系符 支持In, NotIn, Exists, DoesNotExist, Gt, Lt
	weight:       # 倾向权重，在范围1-100。
```

matchExpressions可配置项：

```yaml
- matchExpressions:
  - key: nodeenv              # 匹配存在标签的key为nodeenv的节点
    operator: Exists
  - key: nodeenv              # 匹配标签的key为nodeenv,且value是"xxx"或"yyy"的节点
    operator: In
    values: ["xxx","yyy"]
  - key: nodeenv              # 匹配标签的key为nodeenv,且value大于"xxx"的节点
    operator: Gt
    values: "xxx"
```

创建`pod-nodeaffinity-required.yaml`，演示`requiredDuringSchedulingIgnoredDuringExecution`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-nodeaffinity-required
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  affinity:  #亲和性设置
    nodeAffinity: #设置node亲和性
      requiredDuringSchedulingIgnoredDuringExecution: # 硬限制
        nodeSelectorTerms:
        - matchExpressions: # 匹配env的值在["xxx","yyy"]中的标签
          - key: nodeenv
            operator: In
            values: ["xxx","yyy"]
```

创建pod：

```shell script
# 创建pod
[root@k8s-master01 ~]# kubectl create -f pod-nodeaffinity-required.yaml
pod/pod-nodeaffinity-required created

# 查看pod状态 （运行失败）
[root@k8s-master01 ~]# kubectl get pods pod-nodeaffinity-required -n dev -o wide
NAME                        READY   STATUS    RESTARTS   AGE   IP       NODE    ...... 
pod-nodeaffinity-required   0/1     Pending   0          16s   <none>   <none>  ......

# 查看Pod的详情
# 发现调度失败，提示node选择失败
[root@k8s-master01 ~]# kubectl describe pod pod-nodeaffinity-required -n dev
......
  Warning  FailedScheduling  <unknown>  default-scheduler  0/3 nodes are available: 3 node(s) didn't match node selector.
  Warning  FailedScheduling  <unknown>  default-scheduler  0/3 nodes are available: 3 node(s) didn't match node selector.

#接下来，停止pod
[root@k8s-master01 ~]# kubectl delete -f pod-nodeaffinity-required.yaml
pod "pod-nodeaffinity-required" deleted

# 修改文件，将values: ["xxx","yyy"]------> ["pro","yyy"]
[root@k8s-master01 ~]# vim pod-nodeaffinity-required.yaml

# 再次启动
[root@k8s-master01 ~]# kubectl create -f pod-nodeaffinity-required.yaml
pod/pod-nodeaffinity-required created

# 此时查看，发现调度成功，已经将pod调度到了node1上
[root@k8s-master01 ~]# kubectl get pods pod-nodeaffinity-required -n dev -o wide
NAME                        READY   STATUS    RESTARTS   AGE   IP            NODE  ...... 
pod-nodeaffinity-required   1/1     Running   0          11s   10.244.1.89   node1 ......
```

创建`pod-nodeaffinity-preferred.yaml`，演示`preferredDuringSchedulingIgnoredDuringExecution`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-nodeaffinity-preferred
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  affinity:  # 亲和性设置
    nodeAffinity: # 设置node亲和性
      preferredDuringSchedulingIgnoredDuringExecution: # 软限制
      - weight: 1
        preference:
          matchExpressions: # 匹配env的值在["xxx","yyy"]中的标签(当前环境没有)
          - key: nodeenv
            operator: In
            values: ["xxx","yyy"]
```

创建pod：

```shell script
# 创建pod
[root@k8s-master01 ~]# kubectl create -f pod-nodeaffinity-preferred.yaml
pod/pod-nodeaffinity-preferred created

# 查看pod状态 （运行成功）
[root@k8s-master01 ~]# kubectl get pod pod-nodeaffinity-preferred -n dev
NAME                         READY   STATUS    RESTARTS   AGE
pod-nodeaffinity-preferred   1/1     Running   0          40s
```

`preferredDuringSchedulingIgnoredDuringExecution`如果找不到匹配的节点，调度器仍然会调度该 Pod。

##### podAffinity

> 以运行的Pod为参照，实现让新创建的Pod跟参照pod在一个区域的功能。

可配置项：

```yaml
pod.spec.affinity.podAffinity:
  requiredDuringSchedulingIgnoredDuringExecution:  # 硬限制
    namespaces:          # 指定参照pod的namespace
    topologyKey:         # 指定调度作用域
    labelSelector:       # 标签选择器
      matchExpressions:  # 按节点标签列出的节点选择器要求列表(推荐)
        key:      # 键
        values:   # 值
        operator: # 关系符 支持In, NotIn, Exists, DoesNotExist.
      matchLabels:    # 指多个matchExpressions映射的内容
  preferredDuringSchedulingIgnoredDuringExecution: # 软限制
    podAffinityTerm:  # 选项
      namespaces:      
      topologyKey:
      labelSelector:
        matchExpressions:  
          key:    # 键
          values: # 值
          operator:
        matchLabels: 
    weight: # 倾向权重，在范围1-100

# 其中 topologyKey 用于指定调度时作用域,例如:
#   如果指定为kubernetes.io/hostname，那就是以Node节点为区分范围
#   如果指定为beta.kubernetes.io/os,则以Node节点的操作系统类型来区分
```

创建`pod-podaffinity-required.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-podaffinity-required
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  affinity:  # 亲和性设置
    podAffinity: # 设置pod亲和性
      requiredDuringSchedulingIgnoredDuringExecution: # 硬限制
      - labelSelector:
          matchExpressions: # 匹配env的值在["xxx","yyy"]中的标签
          - key: podenv
            operator: In
            values: ["pro","yyy"]
        topologyKey: kubernetes.io/hostname
```

该配置的意思是，新的pod必须要与拥有标签`nodeenv=pro`或者`nodeenv=yyy`的pod在同一Node上。

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-podaffinity-required.yaml
pod/pod-podaffinity-required created

[root@k8s-master01 ~]# kubectl get pods pod-podaffinity-required -n dev
NAME                       READY   STATUS    RESTARTS   AGE   LABELS
pod-podaffinity-required   1/1     Running   0          6s    <none>
```

##### podAntiAffinity

podAntiAffinity的配置项与podAffinity是一样的。其实现的是让新创建的Pod跟参照pod不在一个区域中的功能。

创建`pod-podantiaffinity-required.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-podantiaffinity-required
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  affinity:  # 亲和性设置
    podAntiAffinity: # 设置pod亲和性
      requiredDuringSchedulingIgnoredDuringExecution: # 硬限制
      - labelSelector:
          matchExpressions: # 匹配podenv的值在["pro"]中的标签
          - key: podenv
            operator: In
            values: ["pro"]
        topologyKey: kubernetes.io/hostname
```

该配置的意思是，新Pod必须要与拥有标签`nodeenv=pro`的pod**不在同一Node上**。

创建pod：

```shell script
# 创建pod
[root@k8s-master01 ~]# kubectl create -f pod-podantiaffinity-required.yaml
pod/pod-podantiaffinity-required created

# 查看pod
# 发现调度到了node2上
[root@k8s-master01 ~]# kubectl get pods pod-podantiaffinity-required -n dev -o wide
NAME                           READY   STATUS    RESTARTS   AGE   IP            NODE   .. 
pod-podantiaffinity-required   1/1     Running   0          30s   10.244.1.96   node2  ..
```

#### 污点和容忍

##### 污点

> 使用kubeadm搭建的集群，默认就会给master节点添加一个污点标记,所以pod就不会调度到master节点上.

用于决定是否允许pod调度到该节点上。也可以将已存在的pod驱逐。

污点属性定义的格式为：`key=value:effect`，key和value是污点的标签，effect描述污点的作用，支持如下三个选项：

- PreferNoSchedule：kubernetes将尽量避免把Pod调度到具有该污点的Node上，除非没有其他节点可调度。

- NoSchedule：kubernetes将不会把Pod调度到具有该污点的Node上，但不会影响当前Node上已存在的Pod。

- NoExecute：kubernetes将不会把Pod调度到具有该污点的Node上，同时也会将Node上已存在的Pod驱离。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200605021831545.png)

定义和去除污点的命令：

```shell script
# 设置污点
kubectl taint nodes node1 key=value:effect

# 去除污点
kubectl taint nodes node1 key:effect-

# 去除所有污点
kubectl taint nodes node1 key-
```

演示：

```shell script
# 为 node1 节点设置一个污点: tag=heima:PreferNoSchedule；然后创建pod1( pod1 可以 )
# 修改 node1 节点设置一个污点: tag=heima:NoSchedule；然后创建pod2( pod1 正常 pod2 失败 )
# 修改 node1 节点设置一个污点: tag=heima:NoExecute；然后创建pod3 ( 3个pod都失败 )

# 1、为node1设置污点(PreferNoSchedule)
[root@k8s-master01 ~]# kubectl taint nodes node1 tag=heima:PreferNoSchedule

# 创建pod1
[root@k8s-master01 ~]# kubectl run taint1 --image=nginx:1.17.1 -n dev
[root@k8s-master01 ~]# kubectl get pods -n dev -o wide
NAME                      READY   STATUS    RESTARTS   AGE     IP           NODE   
taint1-7665f7fd85-574h4   1/1     Running   0          2m24s   10.244.1.59   node1

# 2、为node1设置污点(取消PreferNoSchedule，设置NoSchedule)
[root@k8s-master01 ~]# kubectl taint nodes node1 tag:PreferNoSchedule-
[root@k8s-master01 ~]# kubectl taint nodes node1 tag=heima:NoSchedule

# 创建pod2
[root@k8s-master01 ~]# kubectl run taint2 --image=nginx:1.17.1 -n dev
[root@k8s-master01 ~]# kubectl get pods taint2 -n dev -o wide
NAME                      READY   STATUS    RESTARTS   AGE     IP            NODE
taint1-7665f7fd85-574h4   1/1     Running   0          2m24s   10.244.1.59   node1 
taint2-544694789-6zmlf    0/1     Pending   0          21s     <none>        <none>

# 3、为node1设置污点(取消NoSchedule，设置NoExecute)
[root@k8s-master01 ~]# kubectl taint nodes node1 tag:NoSchedule-
[root@k8s-master01 ~]# kubectl taint nodes node1 tag=heima:NoExecute

# 创建pod3
[root@k8s-master01 ~]# kubectl run taint3 --image=nginx:1.17.1 -n dev
[root@k8s-master01 ~]# kubectl get pods -n dev -o wide
NAME                      READY   STATUS    RESTARTS   AGE   IP       NODE     NOMINATED 
taint1-7665f7fd85-htkmp   0/1     Pending   0          35s   <none>   <none>   <none>    
taint2-544694789-bn7wb    0/1     Pending   0          35s   <none>   <none>   <none>     
taint3-6d78dbd749-tktkq   0/1     Pending   0          6s    <none>   <none>   <none>
```

##### 容忍

> 用于将pod调度到一个有污点的node节点上。
>
> 污点就是拒绝，容忍就是忽略，Node通过污点拒绝pod调度上去，Pod通过容忍忽略拒绝

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200514095913741.png)

创建`pod-toleration.yaml`：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-toleration
  namespace: dev
spec:
  containers:
  - name: nginx
    image: nginx:1.17.1
  tolerations:          # 添加容忍
  - key: "tag"          # 要容忍的污点的key
    operator: "Equal"   # 操作符
    value: "heima"      # 容忍的污点的value
    effect: "NoExecute" # 添加容忍的规则，这里必须和标记的污点规则相同
```

创建pod：

```shell script
[root@k8s-master01 ~]# kubectl create -f pod-toleration.yaml
pod/pod-toleration created

[root@k8s-master01 ~]# kubectl get pods -n dev -o wide
NAME             READY   STATUS    RESTARTS   AGE   IP            NODE    NOMINATED
pod-toleration   1/1     Running   0          3s    10.244.1.62   node1   <none>

# pod.spec.tolerations 容忍可配置项
[root@k8s-master01 ~]# kubectl explain pod.spec.tolerations
......
FIELDS:
   key       # 对应着要容忍的污点的键，空意味着匹配所有的键
   value     # 对应着要容忍的污点的值
   operator  # key-value的运算符，支持Equal和Exists（默认）
   effect    # 对应污点的effect，空意味着匹配所有影响
   tolerationSeconds   # 容忍时间, 当effect为NoExecute时生效，表示pod在Node上的停留时间
```

## Pod控制器

所谓pod控制器，就是管理pd的中间层，只需要告诉Pod控制器需要几个什么样的pod，它会创建满足用户预期的pod。如果Pod资源在运行中出现故障，它会基于指定策略重新编排Pod（重启或者再创建一个容器填补）。

Pod是kubernetes的最小管理单元，在kubernetes中，按照pod的创建方式可以将其分为两类：

- 自主式pod：kubernetes直接创建出来的Pod，这种pod删除后就没有了，也不会重建。

- 控制器创建的pod：kubernetes通过控制器创建的pod，这种pod删除了之后还会自动重建。

kubernetes有很多类型的pod控制器，每种都有自己的适合的场景，常见的有下面这些：

- ~~ReplicationController：比较原始的pod控制器，已经被废弃，由ReplicaSet替代~~。

- ReplicaSet：保证副本数量一直维持在期望值，并支持pod数量扩缩容，镜像版本升级。

- Deployment：通过控制ReplicaSet来控制Pod，并支持滚动升级、回退版本。

- Horizontal Pod Autoscaler：可以根据集群负载自动水平调整Pod的数量，实现削峰填谷。

- DaemonSet：在集群中的指定Node上运行且仅运行一个副本，一般用于守护进程类的任务。

- Job：它创建出来的pod只要完成任务就立即退出，不需要重启或重建，用于执行一次性任务。

- Cronjob：它创建的Pod负责周期性任务控制，不需要持续后台运行。

- StatefulSet：管理有状态应用。

### ReplicaSet

ReplicaSet用于保证指定数量的od正常运行。一旦Pod发生故障，就会重启或重建。同时它还支持对pod数量的扩缩容和镜像版本的升降级。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200612005334159.png)

ReplicaSet的资源配置示例：

```yaml
apiVersion: apps/v1 # 版本号
kind: ReplicaSet    # 类型       
metadata:           # 元数据
  name:             # rs名称 
  namespace:        # 所属命名空间 
  labels: # 标签
    controller: rs
spec:         # 详情描述
  replicas: 3 # 副本数量，当前rs创建出来的pod的数量，默认为1
  selector:   # 选择器，通过它指定该控制器管理哪些pod，采用Label Selector机制
    matchLabels:      # Labels匹配规则
      app: nginx-pod
    matchExpressions: # Expressions匹配规则
      - {key: app, operator: In, values: [nginx-pod]}
  template: # 模板，当副本数量不足时，会根据下面的模板创建pod副本，子配置项就是定义pod的配置项
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
        ports:
        - containerPort: 80
```

#### 创建

创建`pc-replicaset.yaml`文件：

```yaml
apiVersion: apps/v1
kind: ReplicaSet   
metadata:
  name: pc-replicaset
  namespace: dev
spec:
  replicas: 3
  selector: 
    matchLabels:
      app: nginx-pod
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
```

创建pod控制器：

```shell script
[root@k8s-master01 ~]# kubectl create -f pc-replicaset.yaml
replicaset.apps/pc-replicaset created

# 查看rs
# DESIRED:期望副本数量  CURRENT:当前副本数量  READY:已经准备好提供服务的副本数量
[root@k8s-master01 ~]# kubectl get rs pc-replicaset -n dev -o wide
NAME          DESIRED   CURRENT READY AGE   CONTAINERS   IMAGES             SELECTOR
pc-replicaset 3         3       3     22s   nginx        nginx:1.17.1       app=nginx-pod

# 查看当前控制器创建出来的pod
[root@k8s-master01 ~]# kubectl get pod -n dev
NAME                          READY   STATUS    RESTARTS   AGE
pc-replicaset-6vmvt   1/1     Running   0          54s
pc-replicaset-fmb8f   1/1     Running   0          54s
pc-replicaset-snrk2   1/1     Running   0          54s
```

#### 扩缩容

```shell script
# 编辑rs的副本数量，修改spec:replicas: 6 即可
[root@k8s-master01 ~]# kubectl edit rs pc-replicaset -n dev
replicaset.apps/pc-replicaset edited

# 查看pod
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                          READY   STATUS    RESTARTS   AGE
pc-replicaset-6vmvt   1/1     Running   0          114m
pc-replicaset-cftnp   1/1     Running   0          10s
pc-replicaset-fjlm6   1/1     Running   0          10s
pc-replicaset-fmb8f   1/1     Running   0          114m
pc-replicaset-s2whj   1/1     Running   0          10s
pc-replicaset-snrk2   1/1     Running   0          114m

# 或者使用命令实现扩缩容，使用scale命令实现扩缩容，--replicas=n 直接指定目标数量即可
[root@k8s-master01 ~]# kubectl scale rs pc-replicaset --replicas=2 -n dev
replicaset.apps/pc-replicaset scaled

# 命令运行完毕，立即查看，发现已经有4个开始准备退出了
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                       READY   STATUS        RESTARTS   AGE
pc-replicaset-6vmvt   0/1     Terminating   0          118m
pc-replicaset-cftnp   0/1     Terminating   0          4m17s
pc-replicaset-fjlm6   0/1     Terminating   0          4m17s
pc-replicaset-fmb8f   1/1     Running       0          118m
pc-replicaset-s2whj   0/1     Terminating   0          4m17s
pc-replicaset-snrk2   1/1     Running       0          118m

# 稍等片刻，就只剩下2个了
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                       READY   STATUS    RESTARTS   AGE
pc-replicaset-fmb8f   1/1     Running   0          119m
pc-replicaset-snrk2   1/1     Running   0          119m
```

#### 镜像升级

```shell script
# 编辑rs的容器镜像 - image: nginx:1.17.2
[root@k8s-master01 ~]# kubectl edit rs pc-replicaset -n dev
replicaset.apps/pc-replicaset edited

# 再次查看，发现镜像版本已经变更了
[root@k8s-master01 ~]# kubectl get rs -n dev -o wide
NAME                DESIRED  CURRENT   READY   AGE    CONTAINERS   IMAGES        ...
pc-replicaset       2        2         2       140m   nginx         nginx:1.17.2  ...

# 同样的道理，也可以使用命令完成这个工作
# kubectl set image rs rs名称 容器=镜像版本 -n namespace
[root@k8s-master01 ~]# kubectl set image rs pc-replicaset nginx=nginx:1.17.1  -n dev
replicaset.apps/pc-replicaset image updated

# 再次查看，发现镜像版本已经变更了
[root@k8s-master01 ~]# kubectl get rs -n dev -o wide
NAME                 DESIRED  CURRENT   READY   AGE    CONTAINERS   IMAGES            ...
pc-replicaset        2        2         2       145m   nginx        nginx:1.17.1 ... 
```

#### 删除

```shell script
# 默认会删除rs和其管理的pod，在kubernetes删除RS前，会将RS的replicasclear调整为0，等待所有的Pod被删除后，在执行RS对象的删除
[root@k8s-master01 ~]# kubectl delete rs pc-replicaset -n dev
replicaset.apps "pc-replicaset" deleted

[root@k8s-master01 ~]# kubectl get pod -n dev -o wide
No resources found in dev namespace.

# 如果希望仅仅删除RS对象（保留Pod），可以使用 kubectl delete 命令时添加 --cascade=false 选项（不推荐）。
[root@k8s-master01 ~]# kubectl delete rs pc-replicaset -n dev --cascade=false
replicaset.apps "pc-replicaset" deleted

[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                  READY   STATUS    RESTARTS   AGE
pc-replicaset-cl82j   1/1     Running   0          75s
pc-replicaset-dslhb   1/1     Running   0          75s

# 也可以使用yaml直接删除(推荐)
[root@k8s-master01 ~]# kubectl delete -f pc-replicaset.yaml
replicaset.apps "pc-replicaset" deleted
```

### Deployment

> Kubernetes 1.2v 引入了Deployment控制器。Deployment管理ReplicaSet，ReplicaSet管理Pod；相对的，提供的功能也比ReplicaSet丰富。（Deployment通过管理ReplicaSet来间接管理Pod）

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200612005524778.png)

Deployment主要功能有下面几个：

- 支持ReplicaSet的所有功能。

- 支持发布的停止、继续。

- 支持滚动升级和回滚版本。

Deployment的资源配置示例：

```yaml
apiVersion: apps/v1 # 版本号
kind: Deployment    # 类型       
metadata:           # 元数据
  name:             # rs名称 
  namespace:  # 所属命名空间 
  labels:     # 标签
    controller: deploy
spec:         # 详情描述
  replicas: 3 # 副本数量
  revisionHistoryLimit: 3 # 保留历史版本
  paused: false # 暂停部署，默认是false
  progressDeadlineSeconds: 600 # 部署超时时间（s），默认是600
  strategy: # 策略
    type: RollingUpdate # 滚动更新策略
    rollingUpdate:      # 滚动更新
      maxSurge: 30%        # 最大额外可以存在的副本数，可以为百分比，也可以为整数
      maxUnavailable: 30%  # 最大不可用状态的 Pod 的最大值，可以为百分比，也可以为整数
  selector:       # 选择器，通过它指定该控制器管理哪些pod
    matchLabels:  # Labels匹配规则
      app: nginx-pod
    matchExpressions: # Expressions匹配规则
      - {key: app, operator: In, values: [nginx-pod]}
  template: # 模板，当副本数量不足时，会根据下面的模板创建pod副本
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
        ports:
        - containerPort: 80
```

#### 创建

创建`pc-deployment.yaml`：

```yaml
apiVersion: apps/v1
kind: Deployment      
metadata:
  name: pc-deployment
  namespace: dev
spec: 
  replicas: 3
  selector:
    matchLabels:
      app: nginx-pod
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
```

创建pod控制器：

```shell script
# --record=true 表示记录整个deployment更新过程
[root@k8s-master01 ~]# kubectl create -f pc-deployment.yaml --record=true
deployment.apps/pc-deployment created

# 查看 deployment  UP-TO-DATE 最新版本的pod的数量  AVAILABLE  当前可用的pod的数量
[root@k8s-master01 ~]# kubectl get deploy pc-deployment -n dev
NAME            READY   UP-TO-DATE   AVAILABLE   AGE
pc-deployment   3/3     3            3           15s

# 查看rs
[root@k8s-master01 ~]# kubectl get rs -n dev
NAME                       DESIRED   CURRENT   READY   AGE
pc-deployment-6696798b78   3         3         3       23s

# 查看pod
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-6696798b78-d2c8n   1/1     Running   0          107s
pc-deployment-6696798b78-smpvp   1/1     Running   0          107s
pc-deployment-6696798b78-wvjd8   1/1     Running   0          107s
```

#### 扩缩容

```shell script
# 变更副本数量为5个
[root@k8s-master01 ~]# kubectl scale deploy pc-deployment --replicas=5  -n dev
deployment.apps/pc-deployment scaled

# 查看deployment
[root@k8s-master01 ~]# kubectl get deploy pc-deployment -n dev
NAME            READY   UP-TO-DATE   AVAILABLE   AGE
pc-deployment   5/5     5            5           2m

# 查看pod
[root@k8s-master01 ~]#  kubectl get pods -n dev
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-6696798b78-d2c8n   1/1     Running   0          4m19s
pc-deployment-6696798b78-jxmdq   1/1     Running   0          94s
pc-deployment-6696798b78-mktqv   1/1     Running   0          93s
pc-deployment-6696798b78-smpvp   1/1     Running   0          4m19s
pc-deployment-6696798b78-wvjd8   1/1     Running   0          4m19s

# 编辑deployment的副本数量，修改 spec:replicas: 4 即可
[root@k8s-master01 ~]# kubectl edit deploy pc-deployment -n dev
deployment.apps/pc-deployment edited

# 查看pod
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-6696798b78-d2c8n   1/1     Running   0          5m23s
pc-deployment-6696798b78-jxmdq   1/1     Running   0          2m38s
pc-deployment-6696798b78-smpvp   1/1     Running   0          5m23s
pc-deployment-6696798b78-wvjd8   1/1     Running   0          5m23s
```

#### 镜像更新

deployment支持两种更新策略：重建更新和滚动更新。

```yaml
    strategy: # 指定新的Pod替换旧的Pod的策略， 支持两个属性：
      type:   # 指定策略类型，支持两种策略
        Recreate:       # 在创建出新的Pod之前会先杀掉所有已存在的Pod
        RollingUpdate:  # 滚动更新，就是杀死一部分，就启动一部分，在更新过程中，存在两个版本Pod
      rollingUpdate:    # 当type为RollingUpdate时生效，用于为RollingUpdate设置参数，支持两个属性：
        maxUnavailable: # 用来指定在升级过程中不可用Pod的最大数量，默认为25%。
        maxSurge:       # 用来指定在升级过程中可以超过期望的Pod的最大数量，默认为25%。
```

##### 重建更新

创建`pc-deployment.yaml`：

```yaml
apiVersion: apps/v1 # 版本号
kind: Deployment    # 类型
metadata:           # 元数据
  name: pc-deployment # deployment的名称
  namespace: dev      # 命名类型
spec:         # 详细描述
  replicas: 3 # 副本数量
  strategy:   # 镜像更新策略
    type: Recreate # Recreate：在创建出新的Pod之前会先杀掉所有已经存在的Pod
  selector:        # 选择器，通过它指定该控制器可以管理哪些Pod
    matchLabels:   # Labels匹配规则
      app: nginx-pod
  template: # 模块 当副本数据不足的时候，会根据下面的模板创建Pod副本
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
        - name: nginx # 容器名称
          image: nginx:1.17.1 # 容器需要的镜像地址
          ports:
            - containerPort: 80 # 容器所监听的端口
```

演示

```shell script
# 前提是 deployment 已经运行
# 变更镜像
[root@k8s-master01 ~]# kubectl set image deployment pc-deployment nginx=nginx:1.17.2 -n dev
deployment.apps/pc-deployment image updated

# 观察升级过程
[root@k8s-master01 ~]#  kubectl get pods -n dev -w
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-5d89bdfbf9-65qcw   1/1     Running   0          31s
pc-deployment-5d89bdfbf9-w5nzv   1/1     Running   0          31s
pc-deployment-5d89bdfbf9-xpt7w   1/1     Running   0          31s

pc-deployment-5d89bdfbf9-xpt7w   1/1     Terminating   0          41s
pc-deployment-5d89bdfbf9-65qcw   1/1     Terminating   0          41s
pc-deployment-5d89bdfbf9-w5nzv   1/1     Terminating   0          41s

pc-deployment-675d469f8b-grn8z   0/1     Pending       0          0s
pc-deployment-675d469f8b-hbl4v   0/1     Pending       0          0s
pc-deployment-675d469f8b-67nz2   0/1     Pending       0          0s

pc-deployment-675d469f8b-grn8z   0/1     ContainerCreating   0          0s
pc-deployment-675d469f8b-hbl4v   0/1     ContainerCreating   0          0s
pc-deployment-675d469f8b-67nz2   0/1     ContainerCreating   0          0s

pc-deployment-675d469f8b-grn8z   1/1     Running             0          1s
pc-deployment-675d469f8b-67nz2   1/1     Running             0          1s
pc-deployment-675d469f8b-hbl4v   1/1     Running             0          2s
```

##### 滚动更新

创建`pc-deployment.yaml`：

```yaml
apiVersion: apps/v1 # 版本号
kind: Deployment    # 类型
metadata: # 元数据
  name: pc-deployment # deployment的名称
  namespace: dev # 命名类型
spec:            # 详细描述
  replicas: 4    # 副本数量
  strategy:      # 镜像更新策略
    type: RollingUpdate # RollingUpdate：滚动更新，就是杀死一部分，就启动一部分，在更新过程中，存在两个版本的Pod
    rollingUpdate:
      maxUnavailable: 25%
      maxSurge: 25%
  selector:      # 选择器，通过它指定该控制器可以管理哪些Pod
    matchLabels: # Labels匹配规则
      app: nginx-pod
  template:      # 模块 当副本数据不足的时候，会根据下面的模板创建Pod副本
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
        - name: nginx           # 容器名称
          image: nginx:1.17.1   # 容器需要的镜像地址
          ports:
            - containerPort: 80 # 容器所监听的端口
```

演示

```shell script
# 前提是 deployment 已经运行
# 变更镜像
[root@k8s-master01 ~]# kubectl set image deployment pc-deployment nginx=nginx:1.17.3 -n dev 
deployment.apps/pc-deployment image updated

# 观察升级过程
[root@k8s-master01 ~]# kubectl get pods -n dev -w
NAME                           READY   STATUS    RESTARTS   AGE
pc-deployment-c848d767-8rbzt   1/1     Running   0          31m
pc-deployment-c848d767-h4p68   1/1     Running   0          31m
pc-deployment-c848d767-hlmz4   1/1     Running   0          31m
pc-deployment-c848d767-rrqcn   1/1     Running   0          31m

pc-deployment-966bf7f44-226rx   0/1     Pending             0          0s
pc-deployment-966bf7f44-226rx   0/1     ContainerCreating   0          0s
pc-deployment-966bf7f44-226rx   1/1     Running             0          1s
pc-deployment-c848d767-h4p68    0/1     Terminating         0          34m

pc-deployment-966bf7f44-cnd44   0/1     Pending             0          0s
pc-deployment-966bf7f44-cnd44   0/1     ContainerCreating   0          0s
pc-deployment-966bf7f44-cnd44   1/1     Running             0          2s
pc-deployment-c848d767-hlmz4    0/1     Terminating         0          34m

pc-deployment-966bf7f44-px48p   0/1     Pending             0          0s
pc-deployment-966bf7f44-px48p   0/1     ContainerCreating   0          0s
pc-deployment-966bf7f44-px48p   1/1     Running             0          0s
pc-deployment-c848d767-8rbzt    0/1     Terminating         0          34m

pc-deployment-966bf7f44-dkmqp   0/1     Pending             0          0s
pc-deployment-966bf7f44-dkmqp   0/1     ContainerCreating   0          0s
pc-deployment-966bf7f44-dkmqp   1/1     Running             0          2s
pc-deployment-c848d767-rrqcn    0/1     Terminating         0          34m
```

滚动更新的过程（从左至右）：

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200416140251491.png)

镜像更新中rs的变化：

```shell script
[root@k8s-master01 ~]# kubectl get rs -n dev
NAME                       DESIRED   CURRENT   READY   AGE
pc-deployment-6696798b11   0         0         0       5m37s
pc-deployment-c848d76789   4         4         4       72s
```

查看rs，发现原来的rs的依旧存在，只是pod数量变为了0，而后又新产生了一个rs，pod数量为4。用于deployment能够进行版本回退。

#### 版本回退

deployment支持版本升级过程中的暂停、继续功能以及版本回退等功能。

使用`kubectl rollout`命令执行版本回退的相关指令。支持下面的选项：

- status：显示当前升级状态。

- history：显示 升级历史记录。

- pause：暂停版本升级过程。

- resume：继续已经暂停的版本升级过程。

- restart：重启版本升级过程。

- undo：回滚到上一级版本（可以使用--to-revision回滚到指定版本）。

```shell script
# 查看当前升级版本的状态
[root@k8s-master01 ~]# kubectl rollout status deploy pc-deployment -n dev
deployment "pc-deployment" successfully rolled out

# 查看升级历史记录，可以发现有三次版本记录，说明完成过两次升级
[root@k8s-master01 ~]# kubectl rollout history deploy pc-deployment -n dev
deployment.apps/pc-deployment
REVISION  CHANGE-CAUSE
1         kubectl create --filename=pc-deployment.yaml --record=true
2         kubectl create --filename=pc-deployment.yaml --record=true
3         kubectl create --filename=pc-deployment.yaml --record=true

# 版本回滚，使用 --to-revision=1 回滚到了1版本，如果省略这个选项，就是回退到上个版本，就是2版本
[root@k8s-master01 ~]# kubectl rollout undo deployment pc-deployment --to-revision=1 -n dev
deployment.apps/pc-deployment rolled back

# 查看发现，通过nginx镜像版本可以发现到了第一版
[root@k8s-master01 ~]# kubectl get deploy -n dev -o wide
NAME            READY   UP-TO-DATE   AVAILABLE   AGE   CONTAINERS   IMAGES         
pc-deployment   4/4     4            4           74m   nginx        nginx:1.17.1

# 查看 rs
[root@k8s-master01 ~]# kubectl get rs -n dev
NAME                       DESIRED   CURRENT   READY   AGE
pc-deployment-6696798b78   4         4         4       78m
pc-deployment-966bf7f44    0         0         0       37m
pc-deployment-c848d767     0         0         0       71m
```

deployment之所以能够实现版本的回退，就是通过记录历史的ReplicaSet来实现的，一旦想回滚到那个版本，只需要将当前版本的Pod数量降为0，然后将回退版本的Pod提升为目标数量即可。

#### 金丝雀发布

> 颗粒理解为灰度发布，即按照一定的策略上线部分新版本，同时保留老版本，然后让部分用户体验新版本，通过一段时间新版本的反馈收集，然后再决定是否逐步升级直至全量升级或全部回滚到老版本。

```shell script
# 更新deployment的版本，并配置暂停deployment
[root@k8s-master01 ~]#  kubectl set image deploy pc-deployment nginx=nginx:1.17.4 -n dev && kubectl rollout pause deployment pc-deployment  -n dev
deployment.apps/pc-deployment image updated
deployment.apps/pc-deployment paused

#观察更新状态
[root@k8s-master01 ~]# kubectl rollout status deploy pc-deployment -n dev　
Waiting for deployment "pc-deployment" rollout to finish: 2 out of 4 new replicas have been updated...

# 监控更新的过程，可以看到已经新增了一个资源，但是并未按照预期的状态去删除一个旧的资源，就是因为使用了pause暂停命令
[root@k8s-master01 ~]# kubectl get rs -n dev -o wide
NAME                       DESIRED   CURRENT   READY   AGE     CONTAINERS   IMAGES         
pc-deployment-5d89bdfbf9   3         3         3       19m     nginx        nginx:1.17.1   
pc-deployment-675d469f8b   0         0         0       14m     nginx        nginx:1.17.2   
pc-deployment-6c9f56fcfb   2         2         2       3m16s   nginx        nginx:1.17.4  
 
[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-5d89bdfbf9-rj8sq   1/1     Running   0          7m33s
pc-deployment-5d89bdfbf9-ttwgg   1/1     Running   0          7m35s
pc-deployment-5d89bdfbf9-v4wvc   1/1     Running   0          7m34s
pc-deployment-6c9f56fcfb-996rt   1/1     Running   0          3m31s
pc-deployment-6c9f56fcfb-j2gtj   1/1     Running   0          3m31s

# 确保更新的pod没问题了，继续更新
[root@k8s-master01 ~]# kubectl rollout resume deploy pc-deployment -n dev
deployment.apps/pc-deployment resumed

# 查看最后的更新情况
[root@k8s-master01 ~]# kubectl get rs -n dev -o wide
NAME                       DESIRED   CURRENT   READY   AGE     CONTAINERS   IMAGES         
pc-deployment-5d89bdfbf9   0         0         0       21m     nginx        nginx:1.17.1   
pc-deployment-675d469f8b   0         0         0       16m     nginx        nginx:1.17.2   
pc-deployment-6c9f56fcfb   4         4         4       5m11s   nginx        nginx:1.17.4

[root@k8s-master01 ~]# kubectl get pods -n dev
NAME                             READY   STATUS    RESTARTS   AGE
pc-deployment-6c9f56fcfb-7bfwh   1/1     Running   0          37s
pc-deployment-6c9f56fcfb-996rt   1/1     Running   0          5m27s
pc-deployment-6c9f56fcfb-j2gtj   1/1     Running   0          5m27s
pc-deployment-6c9f56fcfb-rf84v   1/1     Running   0          37s
```

#### 删除

```shell script
# 删除deployment，其下的rs和pod也将被删除
[root@k8s-master01 ~]# kubectl delete -f pc-deployment.yaml
deployment.apps "pc-deployment" deleted
```

### Horizontal Pod Autoscaler

Kubernetes的定位目标是自动化、智能化。即期望通过检测pod，实现pod的数量的自动调整。于是就产生了 Horizontal Pod Autoscaler（HPA） 控制器。

HPA可以获取每个Pod利用率，然后和HPA中定义的指标进行对比，同时计算出需要伸缩的具体值，最后实现Pod的数量的调整。它通过追踪分析RC（ReplicaSet）控制的所有目标Pod的负载变化情况，来确定是否需要针对性地调整目标Pod的副本数，这是HPA的实现原理。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200608155858271.png)

#### 安装metrics-server

metrics-server是开源组件，用于收集Kubernetes集群中的资源使用情况。

```shell script
[root@k8s-master01 ~]# yum install git -y
[root@k8s-master01 ~]# git clone -b v0.3.6 https://github.com/kubernetes-incubator/metrics-server

# 修改deployment, 注意修改的是镜像和初始化参数
[root@k8s-master01 ~]# cd /root/metrics-server/deploy/1.8+/

# 找到下列三个配置项，并修改为如下对应的配置
[root@k8s-master01 1.8+]# vim metrics-server-deployment.yaml
......
    hostNetwork: true
    image: registry.cn-hangzhou.aliyuncs.com/google_containers/metrics-server-amd64:v0.3.6
    args:
    - --kubelet-insecure-tls
    - --kubelet-preferred-address-types=InternalIP,Hostname,InternalDNS,ExternalDNS,ExternalIP
......
```

安装：

```shell script
# 安装metrics-server
[root@k8s-master01 1.8+]# kubectl apply -f ./

# 查看pod运行情况
[root@k8s-master01 1.8+]# kubectl get pod -n kube-system
metrics-server-6b976979db-2xwbj   1/1     Running   0          90s

# 使用kubectl top node 查看资源使用情况
[root@k8s-master01 1.8+]# kubectl top node
NAME           CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%
k8s-master01   289m         14%    1582Mi          54%       
k8s-node01     81m          4%     1195Mi          40%       
k8s-node02     72m          3%     1211Mi          41%

[root@k8s-master01 1.8+]# kubectl top pod -n kube-system
NAME                              CPU(cores)   MEMORY(bytes)
coredns-6955765f44-7ptsb          3m           9Mi
coredns-6955765f44-vcwr5          3m           8Mi
etcd-master                       14m          145Mi
......

# 至此, metrics-server安装完成
```

#### 准备deployment和service

创建`pc-hpa-pod.yaml`文件：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
  namespace: dev
spec:
  strategy:             # 策略
    type: RollingUpdate # 滚动更新策略
  replicas: 1
  selector:
    matchLabels:
      app: nginx-pod
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
        resources:       # 资源配额
          limits:        # 限制资源（上限）
            cpu: "1"     # CPU限制，单位是core数
          requests:      # 请求资源（下限）
            cpu: "100m"  # CPU限制，单位是core数
```

创建pod控制器、创建集群外部也可访问的Service：

```shell script
# 创建deployment
[root@k8s-master01 1.8+]# kubectl create -f pc-hpa-pod.yaml
replicaset.apps/nginx created

# 创建service
[root@k8s-master01 1.8+]# kubectl expose deployment nginx --type=NodePort --port=80 -n dev

# 查看
[root@k8s-master01 1.8+]# kubectl get deployment,pod,svc -n dev
NAME                    READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/nginx   1/1     1            1           47s

NAME                         READY   STATUS    RESTARTS   AGE
pod/nginx-7df9756ccc-bh8dr   1/1     Running   0          47s

NAME            TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
service/nginx   NodePort   10.101.18.29   <none>        80:31830/TCP   35s
```

#### 部署Horizontal Pod Autoscaler

创建`pc-hpa.yaml`文件：

```yaml
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: pc-hpa
  namespace: dev
spec:
  minReplicas: 1  #最小pod数量
  maxReplicas: 10 #最大pod数量
  targetCPUUtilizationPercentage: 3 # CPU使用率指标
  scaleTargetRef:   # 指定要控制的nginx信息
    apiVersion:  apps/v1
    kind: Deployment
    name: nginx
```

创建pod控制器：

```shell script
# 创建hpa
[root@k8s-master01 1.8+]# kubectl create -f pc-hpa.yaml
horizontalpodautoscaler.autoscaling/pc-hpa created

# 查看hpa
[root@k8s-master01 1.8+]# kubectl get hpa -n dev
NAME     REFERENCE          TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
pc-hpa   Deployment/nginx   0%/3%     1         10        1          62s
```

然后可以采用压测工具对service地址进行压测，通过控制台查看pod控制器和pod的变化。

```shell script
# hpa变化
[root@k8s-master01 ~]# kubectl get hpa -n dev -w

# deployment变化
[root@k8s-master01 ~]# kubectl get deployment -n dev -w

# pod变化
[root@k8s-master01 ~]# kubectl get pods -n dev -w
```

### DaemonSet

DaemonSet类型的控制器可以保证在集群中的每一台（或指定）节点上都运行一个副本。一般适用于日志收集、节点监控等场景。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200612010223537.png)

DaemonSet控制器的特点：

- 每当向集群中添加一个节点时，指定的 Pod 副本也将添加到该节点上。

- 当节点从集群中移除时，Pod 也就被垃圾回收了。

DaemonSet的资源配置示例：

```yaml
apiVersion: apps/v1 # 版本号
kind: DaemonSet     # 类型       
metadata:           # 元数据
  name:             # rs名称 
  namespace:        # 所属命名空间 
  labels:           #标签
    controller: daemonset
spec: # 详情描述
  revisionHistoryLimit: 3 # 保留历史版本
  updateStrategy:         # 更新策略
    type: RollingUpdate   # 滚动更新策略
    rollingUpdate:        # 滚动更新
      maxUnavailable: 1   # 最大不可用状态的 Pod 的最大值，可以为百分比，也可以为整数
  selector:       # 选择器，通过它指定该控制器管理哪些pod
    matchLabels:  # Labels匹配规则
      app: nginx-pod
    matchExpressions: # Expressions匹配规则
      - {key: app, operator: In, values: [nginx-pod]}
  template: # 模板，当副本数量不足时，会根据下面的模板创建pod副本
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
        ports:
        - containerPort: 80
```

创建`pc-daemonset.yaml`：

```yaml
apiVersion: apps/v1
kind: DaemonSet      
metadata:
  name: pc-daemonset
  namespace: dev
spec: 
  selector:
    matchLabels:
      app: nginx-pod
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
      - name: nginx
        image: nginx:1.17.1
```

创建pod控制器

```shell script
[root@k8s-master01 ~]# kubectl create -f  pc-daemonset.yaml
daemonset.apps/pc-daemonset created

# 查看daemonset
[root@k8s-master01 ~]#  kubectl get ds -n dev -o wide
NAME        DESIRED  CURRENT  READY  UP-TO-DATE  AVAILABLE   AGE   CONTAINERS   IMAGES         
pc-daemonset   2        2        2      2           2        24s   nginx        nginx:1.17.1

# 查看pod,发现在每个Node上都运行一个pod
[root@k8s-master01 ~]#  kubectl get pods -n dev -o wide
NAME                 READY   STATUS    RESTARTS   AGE   IP            NODE    
pc-daemonset-9bck8   1/1     Running   0          37s   10.244.1.43   node1     
pc-daemonset-k224w   1/1     Running   0          37s   10.244.2.74   node2      

# 删除daemonset
[root@k8s-master01 ~]# kubectl delete -f pc-daemonset.yaml
daemonset.apps "pc-daemonset" deleted
```

### Job

用于负责批量处理短暂的一次性任务，其特点如下：

- 当Job创建的pod执行成功结束时，Job将记录成功结束的pod数量。

- 当成功结束的pod达到指定的数量时，Job将完成执行。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200618213054113.png)

job的资源配置示例：

```yaml
apiVersion: batch/v1  # 版本号
kind: Job             # 类型       
metadata:             # 元数据
  name:               # rs名称 
  namespace:          # 所属命名空间 
  labels:             #标签
    controller: job
spec:            # 详情描述
  completions: 1 # 指定job需要成功运行Pods的次数。默认值: 1
  parallelism: 1 # 指定job在任一时刻应该并发运行Pods的数量。默认值: 1
  activeDeadlineSeconds: 30 # 指定job可运行的时间期限，超过时间还未结束，系统将会尝试进行终止。
  backoffLimit: 6           # 指定job失败后进行重试的次数。默认是6
  manualSelector: true      # 是否可以使用selector选择器选择pod，默认是false
  selector:       # 选择器，通过它指定该控制器管理哪些pod
    matchLabels:  # Labels匹配规则
      app: counter-pod
    matchExpressions: # Expressions匹配规则
      - {key: app, operator: In, values: [counter-pod]}
  template: # 模板，当副本数量不足时，会根据下面的模板创建pod副本
    metadata:
      labels:
        app: counter-pod
    spec:
      restartPolicy: Never # 重启策略只能设置为Never或者OnFailure
      containers:
      - name: counter
        image: busybox:1.30
        command: ["bin/sh","-c","for i in 9 8 7 6 5 4 3 2 1; do echo $i;sleep 2;done"]
```

创建`pc-job.yaml`：

```yaml
apiVersion: batch/v1
kind: Job      
metadata:
  name: pc-job
  namespace: dev
spec:
  manualSelector: true
  selector:
    matchLabels:
      app: counter-pod
  template:
    metadata:
      labels:
        app: counter-pod
    spec:
      restartPolicy: Never
      containers:
      - name: counter
        image: busybox:1.30
        command: ["bin/sh","-c","for i in 9 8 7 6 5 4 3 2 1; do echo $i;sleep 3;done"]
```

创建pod控制器

```shell script
[root@k8s-master01 ~]# kubectl create -f pc-job.yaml
job.batch/pc-job created

# 查看job
[root@k8s-master01 ~]# kubectl get job -n dev -o wide  -w
NAME     COMPLETIONS   DURATION   AGE   CONTAINERS   IMAGES         SELECTOR
pc-job   0/1           21s        21s   counter      busybox:1.30   app=counter-pod
pc-job   1/1           31s        79s   counter      busybox:1.30   app=counter-pod

# 通过观察pod状态可以看到，pod在运行完毕任务后，就会变成Completed状态
[root@k8s-master01 ~]# kubectl get pods -n dev -w
NAME           READY   STATUS     RESTARTS      AGE
pc-job-rxg96   1/1     Running     0            29s
pc-job-rxg96   0/1     Completed   0            33s

[root@k8s-master01 ~]# kubectl get pods -n dev -w

# 删除job
[root@k8s-master01 ~]# kubectl delete -f pc-job.yaml
job.batch "pc-job" deleted
```

### Cronjob

以定时任务的形式执行job。

![](https://gitee.com/yooome/golang/raw/main/21-k8s%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/Kubenetes.assets/image-20200618213149531.png)

CronJob的资源配置示例：

```yaml
apiVersion: batch/v1beta1 # 版本号
kind: CronJob             # 类型       
metadata:                 # 元数据
  name:                   # rs名称 
  namespace:              # 所属命名空间 
  labels: #标签
    controller: cronjob
spec:       # 详情描述
  schedule: # cron格式的作业调度运行时间点,用于控制任务在什么时间执行
  concurrencyPolicy:          # 并发执行策略，用于定义前一次作业运行尚未完成时是否以及如何运行后一次的作业
  failedJobHistoryLimit:      # 为失败的任务执行保留的历史记录数，默认为1
  successfulJobHistoryLimit:  # 为成功的任务执行保留的历史记录数，默认为3
  startingDeadlineSeconds:    # 启动作业错误的超时时长
  jobTemplate:  # job控制器模板，用于为cronjob控制器生成job对象;下面其实就是job的定义
    metadata:
    spec:
      completions: 1
      parallelism: 1
      activeDeadlineSeconds: 30
      backoffLimit: 6
      manualSelector: true
      selector:
        matchLabels:
          app: counter-pod
        matchExpressions: # 规则
          - {key: app, operator: In, values: [counter-pod]}
      template:
        metadata:
          labels:
            app: counter-pod
        spec:
          restartPolicy: Never 
          containers:
          - name: counter
            image: busybox:1.30
            command: ["bin/sh","-c","for i in 9 8 7 6 5 4 3 2 1; do echo $i;sleep 20;done"]
```

需要解释的部分配置项：

```text
schedule: "*/1 * * * *"
    */1    *      *    *      *
    <分钟>  <小时> <日>  <月份> <星期>
    
    分钟 值从 0 到 59.
    小时 值从 0 到 23.
    日 值从 1 到 31.
    月 值从 1 到 12.
    星期 值从 0 到 6, 0 代表星期日
    多个时间可以用逗号隔开； 范围可以用连字符表示；*可以作为通配符； /表示每...

concurrencyPolicy:
    Allow:   允许Jobs并发运行(默认)
    Forbid:  禁止并发运行，如果上一次运行尚未完成，则跳过下一次运行
    Replace: 替换，取消当前正在运行的作业并用新作业替换它
```

创建`pc-cronjob.yaml`：

```yaml
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: pc-cronjob
  namespace: dev
  labels:
    controller: cronjob
spec:
  schedule: "*/1 * * * *" # 每分钟执行
  jobTemplate:
    metadata:
    spec:
      template:
        spec:
          restartPolicy: Never
          containers:
          - name: counter
            image: busybox:1.30
            command: ["bin/sh","-c","for i in 9 8 7 6 5 4 3 2 1; do echo $i;sleep 3;done"]
```

创建pod控制器

```shell script
[root@k8s-master01 ~]# kubectl create -f pc-cronjob.yaml
cronjob.batch/pc-cronjob created

# 查看cronjob
[root@k8s-master01 ~]# kubectl get cronjobs -n dev
NAME         SCHEDULE      SUSPEND   ACTIVE   LAST SCHEDULE   AGE
pc-cronjob   */1 * * * *   False     0        <none>          6s

# 查看job
[root@k8s-master01 ~]# kubectl get jobs -n dev
NAME                    COMPLETIONS   DURATION   AGE
pc-cronjob-1592587800   1/1           28s        3m26s
pc-cronjob-1592587860   1/1           28s        2m26s
pc-cronjob-1592587920   1/1           28s        86s

# 查看pod
[root@k8s-master01 ~]# kubectl get pods -n dev
pc-cronjob-1592587800-x4tsm   0/1     Completed   0          2m24s
pc-cronjob-1592587860-r5gv4   0/1     Completed   0          84s
pc-cronjob-1592587920-9dxxq   1/1     Running     0          24s

# 删除cronjob
[root@k8s-master01 ~]# kubectl  delete -f pc-cronjob.yaml
cronjob.batch "pc-cronjob" deleted
```

## Service

## 数据存储

## 安全认证

## 可视化界面