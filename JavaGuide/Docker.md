# Docker

## 快速入门

### 命令解读

```bash
docker run -d \
   --name mysql \
   -p 3306:3306 \
   -e TZ=Asia/Shanghai \
   -e MYSQL_ROOT_PASSWORD=123 \
   mysql
```

> `\`表示换行。

- `docker run`：创建并运行一个容器。（也有创建不运行的命令）

- `-d`：让容器在后台运行。

- `--name mysql`：给容器起个名字，必须唯一。（比如此时的容器名称为mysql）

- `-p 3306:3306`：设置端口映射。（前者是宿主机端口，后者是容器内端口，宿主机端口映射到容器内端口。原因是容器内部是有网络隔离的）

- `-e key=value`：设置环境变量。

- `mysql`：指定运行的镜像的名称。（一般由`repository:tag`组成，前者是镜像名，后者是镜像的版本）

## Docker基础

### 常见命令

> Docker常见命令就是操作镜像、容器。 
>
> 详见官方文档：https://docs.docker.com/

命令 | 说明 
-- | -- 
docker pull | 拉取镜像 
docker push | 推送镜像到DockerRegistry 
docker images | 查看本地镜像 
docker rmi | 删除本地镜像 | docker rmi
docker run | 创建并运行容器（不能重复创建） 
docker stop | 停止指定容器 
docker start | 启动指定容器 
docker restart | 重新启动容器 
docker rm | 删除指定容器 
docker ps | 查看容器 
docker logs | 查看容器运行日志 
docker exec | 进入容器 
docker save | 保存镜像到本地压缩文件 
docker load | 加载本地压缩文件到镜像 
docker inspect | 查看容器详细信息 

用一副图来表示这些命令的关系：

![image](https://github.com/Doing-code/guide/assets/53521537/46160cb2-48eb-414a-ae23-82ac7da28a6f)

#### 演示

```sheel
# 第1步，去DockerHub查看nginx镜像仓库及相关信息

# 第2步，拉取Nginx镜像，或者 docker pull nginx:1.4.2
docker pull nginx

# 第3步，查看镜像
docker images
# 结果如下：
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
nginx        latest    605c77e624dd   16 months ago   141MB
mysql        latest    3218b38490ce   17 months ago   516MB

# 第4步，创建并允许Nginx容器
docker run -d --name nginx -p 80:80 nginx

# 第5步，查看运行中容器
docker ps
# 也可以加格式化方式访问，格式会更加清爽
docker ps --format "table {{.ID}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}\t{{.Names}}"

# 第6步，访问网页，地址：http://虚拟机地址

# 第7步，停止容器
docker stop nginx

# 第8步，查看所有容器
docker ps -a --format "table {{.ID}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}\t{{.Names}}"

# 第9步，再次启动nginx容器
docker start nginx

# 第10步，再次查看容器
docker ps --format "table {{.ID}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}\t{{.Names}}"

# 第11步，查看容器详细信息
docker inspect nginx

# 第12步，进入容器,查看容器内目录，-it：可交互的终端，bash：命令行交互
docker exec -it nginx bash
# 或者，可以进入MySQL
docker exec -it mysql mysql -uroot -p

# 第13步，删除容器
docker rm nginx
# 发现无法删除，因为容器运行中，强制删除容器
docker rm -f nginx
```

#### 命令别名

Linux的使用技巧。给经常使用且较长的命令起一个简短的别名，如`docker ps --format`、`docker images`等。

```bash
# 修改/root/.bashrc文件
vi /root/.bashrc
内容如下：
# .bashrc

# User specific aliases and functions

alias rm='rm -i'
alias cp='cp -i'
alias mv='mv -i'

#------------
alias dps='docker ps --format "table {{.ID}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}\t{{.Names}}"'
alias dis='docker images'
#------------

# Source global definitions
if [ -f /etc/bashrc ]; then
        . /etc/bashrc
fi
```

让配置生效：`source /root/.bashrc`。

### 数据卷挂载

数据卷（volume）是一个虚拟目录，是容器内目录与宿主机目录之间映射的桥梁。

容器提供程序的运行环境，但是程序运行产生的数据、程序运行依赖的配置都应该与容器解耦。

容器运行所有的文件都在容器内部，所以需要利用数据卷将两个目录与宿主机目录关联，方便操作。

![image](https://github.com/Doing-code/guide/assets/53521537/3e802ff5-64a1-4e62-bbe3-d4adc47ac634)

容器内的conf和html目录就 与宿主机的conf和html目录关联起来，称为挂载。`/var/lib/docker/volumes`这个目录就是默认的存放所有容器数据卷的目录

#### 数据卷命令

命令 | 说明
-- | --
docker volume create | 创建数据卷
docker volume ls | 查看所有数据卷
docker volume rm | 删除指定数据卷
docker volume inspect | 查看某个数据卷的详情
docker volume prune | 清除数据卷

容器与数据卷的挂载需要在创建容器时配置，对于已创建的容器，是不能设置数据卷的。默认情况下创建容器的过程中，数据卷会自动创建。

#### 演示

`-v 数据卷:容器内目录`。而匿名数据卷可以理解为是缺省数据卷（默认数据卷）。

```bash
# 1.首先创建容器并指定数据卷，注意通过 -v 参数来指定数据卷
docker run -d --name nginx -p 80:80 -v html:/usr/share/nginx/html nginx

# 2.然后查看数据卷
docker volume ls
# 结果
DRIVER    VOLUME NAME
local     29524ff09715d3688eae3f99803a2796558dbd00ca584a25a4bbc193ca82459f
local     html

# 3.查看数据卷详情
docker volume inspect html
# 结果
[
    {
        "CreatedAt": "2024-05-17T19:57:08+08:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/html/_data",
        "Name": "html",
        "Options": null,
        "Scope": "local"
    }
]

# 4.查看/var/lib/docker/volumes/html/_data目录
ll /var/lib/docker/volumes/html/_data
# 可以看到与nginx的html目录内容一样，结果如下：
总用量 8
-rw-r--r--. 1 root root 497 12月 28 2021 50x.html
-rw-r--r--. 1 root root 615 12月 28 2021 index.html

# 5.进入该目录，并随意修改index.html内容
cd /var/lib/docker/volumes/html/_data
vi index.html

# 6.打开页面，查看效果

# 7.进入容器内部，查看/usr/share/nginx/html目录内的文件是否变化
docker exec -it nginx bash
```

#### 本地目录挂载

```bash
# 挂载本地目录
-v 本地目录:容器内目录
# 挂载本地文件
-v 本地文件:容器内文件
```

本地目录或文件必须以`/`或`./`开头，如果以名称开头，会被识别为数据卷名，而非本地目录名。

```bash
# 会被识别为一个数据卷叫mysql，运行时会自动创建这个数据卷
-v mysql:/var/lib/mysql 

# 会被识别为当前目录下的mysql目录，运行时如果不存在会创建目录
-v ./mysql:/var/lib/mysql 
```

##### 演示

```bash
docker run -d \
   --name mysql \
   -p 3306:3306 \
   -e TZ=Asia/Shanghai \
   -e MYSQL_ROOT_PASSWORD=123 \
   -v /root/mysql/data:/var/lib/mysql \
   -v /root/mysql/init:/docker-entrypoint-initdb.d \  # 初始化的SQL脚本目录
   -v /root/mysql/conf:/etc/mysql/conf.d \  # MySQL配置文件目录
   mysql 

# 还需要配置MySQL的字符编码
# 通过 show variables like "%char%"; 查看字符编码配置
```

### Dockerfile语法

### 镜像

**镜像**是一个特殊的文件系统，除了提供容器运行时所需的程序、库、资源、配置等文件外，还包含了一些为运行时准备的一些配置参数（如匿名卷、环境变量、用户等）。镜像 不包含 任何动态数据，其内容在构建之后也不会被改变。

而自定义镜像本质就是依次准备好程序运行的基础环境、依赖、应用本身、运行配置等文件，并且打包而成。

一个完整的Java项目的镜像结构如图所示：

![image](https://github.com/Doing-code/guide/assets/53521537/c10040d5-c42d-485e-8dc8-ce5c6ff80884)

对于已存在的镜像，不需要重复制作，可以直接拷贝这些layer。

#### Dockerfile

Docker提供自动打包镜像的功能，只需要阿静打包的过程，每一层要做的事情用固定的语法描述，交给Docker执行即可。而这种描述镜像结构的文件称为Dockerfile。

常用的命令有：

指令 | 说明
-- | --
FROM | 指定基础镜像
ENV | 设置环境变量，可在后面指令使用
COPY | 拷贝本地文件到镜像的指定目录
RUN | 执行Linux的shell命令，一般是安装过程的命令
EXPOSE | 指定容器运行时监听的端口，是给镜像使用者看的
ENTRYPOINT | 镜像中应用的启动命令，容器运行时调用

基于Ubuntu镜像构建Java应用，其Dockerfile内容如下：

```bash
# 指定基础镜像
FROM ubuntu:16.04
# 配置环境变量，JDK的安装目录、容器内时区
ENV JAVA_DIR=/usr/local
ENV TZ=Asia/Shanghai
# 拷贝jdk和java项目的包
COPY ./jdk8.tar.gz $JAVA_DIR/
COPY ./docker-demo.jar /tmp/app.jar
# 设定时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 安装JDK
RUN cd $JAVA_DIR \
 && tar -xf ./jdk8.tar.gz \
 && mv ./jdk1.8.0_144 ./java8
# 配置环境变量
ENV JAVA_HOME=$JAVA_DIR/java8
ENV PATH=$PATH:$JAVA_HOME/bin
# 指定项目监听的端口
EXPOSE 8080
# 入口，java项目的启动命令
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

基于已有的基础镜像构建Java应用：

```bash
# 基础镜像
FROM openjdk:11.0-jre-buster
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 拷贝jar包
COPY docker-demo.jar /app.jar
# 入口
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### 构建镜像

在Dockerfile所在目录下执行`docker build -t docker-demo:1.0 .`：

- `docker build`：用于构建docker镜像的命令。

- `-t docker-demo:1.0`：`-t`参数指定镜像的名称（repository:tag）

- `.`：构建时Dockerfile所在目录，`.`代表当前目录。也可以指定Dockerfile目录全路径。

查看镜像列表：

```bash
# 查看镜像列表：
docker images
# 结果
REPOSITORY    TAG       IMAGE ID       CREATED          SIZE
docker-demo   1.0       d6ab0b9e64b9   27 minutes ago   327MB
nginx         latest    605c77e624dd   16 months ago    141MB
mysql         latest    3218b38490ce   17 months ago    516MB
```

### 容器网络互连

容器的网络IP是一个虚拟的IP，其值并不固定与某个容器绑定，如果在开发时固定某个IP时，在部署时很可能容器的IP会发生变化，导致通信失败。

而Docker也提供了该问题的解决方案。

常见的命令有：

命令 | 说明
-- | --
docker network create | 创建一个网络
docker network ls | 查看所有网络
docker network rm | 删除指定网络
docker network prune | 清除未使用的网络
docker network connect | 使指定容器连接加入某网络
docker network disconnect | 使指定容器连接离开某网络
docker network inspect | 查看网络详细信息

#### 演示

```bash
# 1.首先通过命令创建一个网络
docker network create hmall

# 2.然后查看网络
docker network ls
# 结果：
NETWORK ID     NAME      DRIVER    SCOPE
639bc44d0a87   bridge    bridge    local
403f16ec62a2   hmall     bridge    local
0dc0f72a0fbb   host      host      local
cd8d3e8df47b   none      null      local
# 其中，除了hmall以外，其它都是默认的网络

# 3.让dd和mysql都加入该网络，注意，在加入网络时可以通过--alias给容器起别名
# 这样该网络内的其它容器可以用别名互相访问！
# 3.1.mysql容器，指定别名为db，另外每一个容器都有一个别名是容器名
docker network connect hmall mysql --alias db
# 3.2.db容器，也就是我们的java项目
docker network connect hmall dd

# 4.进入dd容器，尝试利用别名访问db
# 4.1.进入容器
docker exec -it dd bash
# 4.2.用db别名访问
ping db
# 结果
PING db (172.18.0.2) 56(84) bytes of data.
64 bytes from mysql.hmall (172.18.0.2): icmp_seq=1 ttl=64 time=0.070 ms
64 bytes from mysql.hmall (172.18.0.2): icmp_seq=2 ttl=64 time=0.056 ms
# 4.3.用容器名访问
ping mysql
# 结果：
PING mysql (172.18.0.2) 56(84) bytes of data.
64 bytes from mysql.hmall (172.18.0.2): icmp_seq=1 ttl=64 time=0.044 ms
64 bytes from mysql.hmall (172.18.0.2): icmp_seq=2 ttl=64 time=0.054 ms
```

此时，无需固定IP地址也可以实现容器互联。在同一个自定义网络中的容器，可以通过别名互相访问。

可以在`docker run`时通过`--network`参数指定网络，如下所示：

```bash
docker run -d --name hmall --network hmall -p 8080:8080 hmall
```

## 项目部署

一个简单的Java项目，一般包括3个容器：MySQL、Ngix、Java应用。而复杂应用还会有中间件等需要部署，全都手动部署效率低、麻烦。

而DockerCompose可以实现多个相互关联的Docker容器快速部署。允许通过`docker-compose.yml`模板文件来定义一组相互关联的应用容器。

### DockerCompose

> 官方文档：https://docs.docker.com/compose/compose-file/compose-file-v3/

docker-compose文件中可以定义多个相互关联的应用容器，**每一个应用容器被称为一个服务（service）**。由于service就是在定义某个应用的运行时参数，因此与docker run参数非常相似。

举例来说，用docker run部署MySQL的命令如下：

```bash
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e TZ=Asia/Shanghai \
  -e MYSQL_ROOT_PASSWORD=123 \
  -v ./mysql/data:/var/lib/mysql \
  -v ./mysql/conf:/etc/mysql/conf.d \
  -v ./mysql/init:/docker-entrypoint-initdb.d \
  --network hmall
  mysql
```

如果用docker-compose.yml文件来定义，就是这样：

```bash
version: "3.8"

services:
  mysql:
    image: mysql
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      TZ: Asia/Shanghai
      MYSQL_ROOT_PASSWORD: 123
    volumes:
      - "./mysql/conf:/etc/mysql/conf.d"
      - "./mysql/data:/var/lib/mysql"
    networks:
      - new
networks:
  new:
    name: hmall
```

两者对比如下：

docker run 参数 | docker compose 指令 | 说明
-- | -- | --
--name | container_name | 容器名称
-p | ports | 端口映射
-e | environment | 环境变量
-v | volumes | 数据卷配置
--network | networks | 网络

此时使用DockerCompose模板文件配置简单的Java应用：

```bash
version: "3.8"

services:
  mysql:
    image: mysql
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      TZ: Asia/Shanghai
      MYSQL_ROOT_PASSWORD: 123
    volumes:
      - "./mysql/conf:/etc/mysql/conf.d"
      - "./mysql/data:/var/lib/mysql"
      - "./mysql/init:/docker-entrypoint-initdb.d"
    networks:
      - hm-net
  hmall:
    build: 
      context: .
      dockerfile: Dockerfile
    container_name: hmall
    ports:
      - "8080:8080"
    networks:
      - hm-net
    depends_on:
      - mysql
  nginx:
    image: nginx
    container_name: nginx
    ports:
      - "18080:18080"
      - "18081:18081"
    volumes:
      - "./nginx/nginx.conf:/etc/nginx/nginx.conf"
      - "./nginx/html:/usr/share/nginx/html"
    depends_on:
      - hmall
    networks:
      - hm-net
networks:
  hm-net:
    name: hmall
```

`depends_on`参数表示：以如下配置为例，db和redis在web之前启动。且仅仅等到db和redis开始，web就会开始自己的逻辑。

```bash
version: "3.8"
services:
  web:
    build: .
    depends_on:
      - db
      - redis
  redis:
    image: redis
  db:
    image: postgres
```

编写好docker-compose.yml文件后，就可以部署项目了。

> 官方文档：https://docs.docker.com/compose/reference/

基本语法 ：`docker compose [OPTIONS] [COMMAND]`。

类型 | 参数或指令 | 说明
-- | -- | --
Options | -f | 指定compose文件的路径和名称
-p | 指定project名称。project就是当前compose文件中设置的多个service的集合，是逻辑概念
Commands | up | 创建并启动所有service容器
down | 停止并移除所有容器、网络
ps | 列出所有启动的容器
logs | 查看指定容器的日志
stop | 停止容器
start | 启动容器
restart | 重启容器
top | 查看运行的进程
exec | 在指定的运行中容器中执行命令

#### 演示

```bash
# 1.进入root目录
cd /root

# 2.删除旧容器
docker rm -f $(docker ps -qa)

# 3.删除hmall镜像
docker rmi hmall

# 4.清空MySQL数据
rm -rf mysql/data

# 5.启动所有, -d 参数是后台启动
docker compose up -d
# 结果：
[+] Building 15.5s (8/8) FINISHED
 => [internal] load build definition from Dockerfile                                    0.0s
 => => transferring dockerfile: 358B                                                    0.0s
 => [internal] load .dockerignore                                                       0.0s
 => => transferring context: 2B                                                         0.0s
 => [internal] load metadata for docker.io/library/openjdk:11.0-jre-buster             15.4s
 => [1/3] FROM docker.io/library/openjdk:11.0-jre-buster@sha256:3546a17e6fb4ff4fa681c3  0.0s
 => [internal] load build context                                                       0.0s
 => => transferring context: 98B                                                        0.0s
 => CACHED [2/3] RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo   0.0s
 => CACHED [3/3] COPY hm-service.jar /app.jar                                           0.0s
 => exporting to image                                                                  0.0s
 => => exporting layers                                                                 0.0s
 => => writing image sha256:32eebee16acde22550232f2eb80c69d2ce813ed099640e4cfed2193f71  0.0s
 => => naming to docker.io/library/root-hmall                                           0.0s
[+] Running 4/4
 ✔ Network hmall    Created                                                             0.2s
 ✔ Container mysql  Started                                                             0.5s
 ✔ Container hmall  Started                                                             0.9s
 ✔ Container nginx  Started                                                             1.5s

# 6.查看镜像
docker compose images
# 结果
CONTAINER           REPOSITORY          TAG                 IMAGE ID            SIZE
hmall               root-hmall          latest              32eebee16acd        362MB
mysql               mysql               latest              3218b38490ce        516MB
nginx               nginx               latest              605c77e624dd        141MB

# 7.查看容器
docker compose ps
# 结果
NAME                IMAGE               COMMAND                  SERVICE             CREATED             STATUS              PORTS
hmall               root-hmall          "java -jar /app.jar"     hmall               54 seconds ago      Up 52 seconds       0.0.0.0:8080->8080/tcp, :::8080->8080/tcp
mysql               mysql               "docker-entrypoint.s…"   mysql               54 seconds ago      Up 53 seconds       0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp
nginx               nginx               "/docker-entrypoint.…"   nginx               54 seconds ago      Up 52 seconds       80/tcp, 0.0.0.0:18080-18081->18080-18081/tcp, :::18080-18081->18080-18081/tcp
```

## 附录

### 开机自启

```bash
# Docker开机自启
systemctl enable docker

# Docker容器开机自启
docker update --restart=always [容器名/容器id]
```