# Nginx

## 基础篇

### Nginx服务器启停命令

#### Nginx服务的信号控制

1. Nginx中的master和worker进程？

worker负责处理连接，master负责管理worker。

2. Nginx的工作方式？

多进程架构，一个master进程和多个worker进程，worker负责处理连接，master负责管理worker。

3. 如何获取进程的PID？

`ps -ef | grep nginx`

4. 信号有哪些？

| 信号       | 描述                                  |
| -------- | ----------------------------------- |
| TERM/INT | 立即关闭Nginx服务                         |
| QUIT     | 优雅地关闭Nginx服务                        |
| HUP      | 重新读取配置文件，使服务对新配置项生效                 |
| USR1     | 重新打开日志文件，用于日志切割                     |
| USR2     | 平滑升级到最新版本的Nginx                     |
| WINCH    | 所有子进程不再接收处理新连接，等同于给worker进程发送QUIT指令 |

5. 如何通过信号控制Nginx的启停等操作？

`kill -signal PID`：signal即为信号，如TERM、QUIT、WINCH等。PID即为master进程ID。

```shell
kill -TERM PID
kill -QUIT PID
```

#### Nginx的命令行控制

```shell
./nginx
./nginx -s stop
./nginx -s reload
./nginx -t
```

### Nginx服务器版本升级

> https://www.bilibili.com/video/BV1ov41187bq?p=20

版本升级就是将新版本的nginx可执行文件，拷贝旧版本中，然后使用命令在启动一个nginx进程，保留原进程，两者都能对外提供服务，最后将旧nginx服务关闭即可。即可完成版本升级

#### Nginx核心配置文件结构

- 全局块：events和http之外的配置。主要设置Nginx服务器运行的配置指令。

- events：nginx服务与用户的网络连接的配置。

- http：代理、缓存、日志、第三方模块配置...

```conf
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   html;
            index  index.html index.htm;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

更多指令可以参考`https://www.bilibili.com/video/BV1ov41187bq?p=22`

## 进阶篇

当server_name被匹配多次时的执行顺序：

1. 精确匹配server_name。

2. 通配符在开始时的匹配server_name。

3. 通配符在结束时的匹配server_name。

4. 正则匹配server_name。

5. default_server，若没有指定default_server则默认找第一个server。

### 静态资源优化配置

#### sendfile

可以理解为零拷贝。

- 语法：sendfile on | off

- 默认值：sendfile off

- 可配置的位置：http、server、location

一般配置到http块即可。

#### tcp_nopush

- 语法：tcp_nopush on | off

- 默认值：tcp_nopush off

- 可配置的位置：http、server、location

提升网络包的传输效率。该值需要在sendfile开启的状态才会生效。

#### tcp_nodeplay

- 语法：tcp_nodelay on | off

- 默认值：tcp_nodelay on

- 可配置的位置：http、server、location

提升网络包传输的实时性。该值需要在keep-alive开启的情况下才会生效。

### 静态资源压缩

> 具体参数配置参考：https://www.bilibili.com/video/BV1ov41187bq?p=51

语法：gzip on | off

根据响应页类型开启gzip压缩功能：`gzip_types application/javascript text/html`

一般`gzip_comp_level 6;`压缩级别设置为6即可。 

Gzip压缩功能配置模板

```conf
gzip on;                          # 开启 Gzip 功能
gzip_types *;                     # 压缩源文件类型,根据具体的访问资源类型设定
gzip_comp_level 6;                # Gzip 压缩级别
gzip_min_length 1k;          # 进行压缩响应页面的最小长度，content-length
gzip_buffers 4 16K;             # 缓存空间大小
gzip_http_version 1.1;       # 指定压缩响应所需要的最低 HTTP 请求版本
gzip_vary  on;                 # 往头信息中添加压缩标识
gzip_disable "MSIE [1-6]\."; # 对 IE6 以下的版本都不进行压缩
gzip_proxied  off;           # Nginx 作为反向代理压缩服务端返回数据的条件
```

Gzip和sendfile共存问题：

sendfile不需要经过用户缓冲区，而开启Gzip后，需要在用户缓冲区中进行压缩，那么两者就互斥了。

解决方案：提前将资源压缩好，等待获取静态资源时将压缩文件发送即可。关闭gzip，手动压缩。

指令：gzip_static on | off | always，默认off。

gzip_static在 ngx_http_gzip_static_module 模块下。需要添加模块。

### 静态资源缓存配置

Expires    缓存过期的日期和时间

```conf
location ~ .*\.(html|js|css|png|jpg|jpeg|gif)$ {
    # ...
    expires max
    # ...
}

#expires 30s;  # 表示把数据缓存 30 秒
#expires 30m;  # 表示把数据缓存 30 分
#expires 10h;  # 表示把数据缓存 10 小时
#expires 1d;   # 表示把数据缓存 1 天
```

Cache-Control 设置和缓存相关的配置信息

```conf
add_header Cache-control must-revalidate;   # 可缓存但必须再向源服务器进行确认
add_header Cache-control no-cache;  # 数据内容不能被缓存，每次请求都重新访问服务器，若有 max-age，则缓存期间不访问服务器
add_header Cache-control no-store;  # 不缓存请求或响应的任何内容，暂存也不可以(临时文件夹中不能暂存该资源)
add_header Cache-control no-transform;  # 代理不可更改媒体类型
add_header Cache-control public;    # 可以被任何缓存区缓存，如: 浏览器、服务器、代理服务器等
add_header Cache-control private;   # 只能在浏览器中缓存，只有在第一次请求的时候才访问服务器，若有 max-age，则缓存期间不访问服务器
add_header Cache-control proxy-revalidate;  # 要求中间缓存服务器对缓存的响应有效性再进行确认
add_header Cache-Control max-age=<seconds>;  # 秒, 过期时间，即以秒为单位的缓存时间
add_header Cache-control s-maxage=<seconds>; # 秒, 公共缓存服务器响应的最大 Age 值
```

### Nginx跨域

```conf
location /getUser {
    # Access-Control-Allow-Origin：允许跨域访问的源地址信息，可以配置多个(多个用逗号分隔)，也可以使用 * 代表所有源。
    add_header Access-Control-Allow-Origin *;
    # Access-Control-Allow-Methods：允许跨域访问的请求方式，值可以为 GET、POST、PUT、DELETE ......，可以全部设置，也可以根据需要设置，多个用逗号分隔。
    add_header Access-Control-Allow-Methods GET,POST,PUT,DELETE;
    default_type application/json;   # return 的格式是 json
    return 200 '{"id":1,"name":"TOM","age":18}';
}
```

### Nginx防盗链

当浏览器向 Web 服务器发送请求的时候，一般都会带上 Referer，来告诉浏览器该网页是从哪个页面链接过来的。后台服务器可以根据获取到的这个 Referer 信息来判断是否为自己信任的网站地址，如果是则放行继续访问，如果不是则可以返回 403（服务端拒绝访问）的状态信息。

```conf
location ~ *\.(png|jpg|gif){
    valid_referers none blocked www.baidu.com 192.168.91.200;

    # valid_referers none blocked *.example.com example.*  www.example.org  ~\.google\.;

    if ($invalid_referer){
        return 403;
    }
    root /usr/local/nginx/html;
}

# 针对目录防盗链
location /images {
    valid_referers none blocked www.baidu.com 192.168.199.27;

    # valid_referers none blocked *.example.com example.*  www.example.org  ~\.google\.;

    if ($invalid_referer){
        return 403;
    }
    root /usr/local/nginx/html;
}
```

上方代码如果没有匹配上 www.baidu.com 和 192.168.91.200，则 $invalid_referer 为 1（true），返回 403，代表不允许获取资源。

但Referer 的限制比较粗，比如浏览器发送请求时恶意加一个 Referer，上面的方式是无法进行限制的。

### Rewrite

> 更多配置可参考：https://www.bilibili.com/video/BV1ov41187bq?p=70

Rewrite相关命令：

- set：该指令用来设置一个新的变量。
  
  - 语法：`set <$key> <value>;`
  
  ```conf
  server {
      listen 8081;
      server_name localhost;
      location /server {
          set $name TOM;
          set $age 18;
          default_type text/plain;
          return 200 $name=$age;
      }
  }
  ```

- if:该指令用来支持条件判断，并根据条件判断结果选择不同的 Nginx 配置。
  
  ```conf
  if ($request_method = POST){
      return 405;
  }
  ```

- break：该指令用于中断当前相同作用域中的其他 Nginx 配置。终止当前的匹配并把当前的URI在本location进行重定向访问处理。

- return：该指令用于完成对请求的处理，直接向客户端返回响应状态代码。在 return 后的所有 Nginx 配置都是无效的。
  
  - 语法：
  
  ```conf
  return <code> [text];
  return <code> <URL>;
  return <URL>;
  
  code：返回给客户端的 HTTP 状态代理。可以返回的状态代码为 0 ~ 999 的任意 HTTP 状态代理
  text：返回给客户端的响应体内容，支持变量的使用和 JSON 字符串
  URL：跳转给客户端的 URL 地址。
  ```

- rewrite：该指令通过正则表达式的使用来改变 URI。可以同时存在一个或者多个指令，按照顺序依次对 URL 进行匹配和处理。
  
  - 语法：`rewrite regex replacement [flag];`
    
    - regex：用来匹配 URI 的正则表达式。
    
    - replacement：匹配成功后，用于替换 URI 中被截取内容的字符串。如果该字符串是以 『 http:// 』或者『 https:// 』开头的，则不会继续向下对URI 进行其他处理，而是直接返回重写后的 URI 给客户端。
    
    - flag：用来设置 Rewrite 对 URI 的处理行为。
  
  ```conf
  # ......
  listen 8081;
  location /rewrite {
      rewrite ^/rewrite/url\w*$ https://www.baidu.com;
  
      # （括号的值会作为 $1 的值） ^代表匹配输入字符串的起始位置
      rewrite ^/rewrite/(test)\w*$ /$1;   # 如果是 /rewrite/testxxx，则重写 url 为 test
      rewrite ^/rewrite/(demo)\w*$ /$1;    # 如果是 /rewrite/demoxxx，则重写 url 为 demo
  }
  location /test {   # 重写后的 url 如果为 test，触发 location
      default_type text/plain;
      return 200 test_sucess;
  }
  location /demo {   # 重写后的 url 如果为 demo，触发 location
      default_type text/plain;
      return 200 demo_sucess;
  }
  ```

- rewrite_log：该指令配置是否开启 URL 重写日志的输出功能，默认关闭。开启后，URL 重写的相关日志将以 notice 级别输出到 error_log 指令配置的日志文件汇总。
  
  ```conf
  location /rewrite_log {
      rewrite_log on;    # 开启重写日志
      error_log logs /error.log notice;   # 切换为 notice 模式，因为只支持这个模式
      return 200 '开启了重写日志';
  }
  ```

Rewrite的应用场景：

- 域名跳转

- 域名镜像

- 独立域名

- 目录自动添加`/`
  
  ```conf
  server {
      listen    80;
      server_name localhost;
      server_name_in_redirect on;
      location /frx {
          if (-d $request_filename){   # 如果请求的资源目录存在
              rewrite ^/(.*)([^/])$ http://$host/$1$2/ permanent; # $2 获取第二个括号的值：/
          }
      }
  }
  ```

- 合并目录

- 防盗链
  
  ```conf
  # 基于文件类型实现防盗链配置
  server{
      listen 80;
      server_name www.web.com;
      locatin ~* ^.+\.(gif|jpg|png|swf|flv|rar|zip)$ {
          valid_referers none blocked server_names *.web.com; # server_names 后指定具体的域名或者 IP
          if ($invalid_referer){
              rewrite ^/ http://www.web.com/images/forbidden.png;  # 跳转到默认地址
          }
      }
  }
  
  # 基于目录实现防盗链配置
  server{
      listen 80;
      server_name www.web.com;
      location /file {
          root /server/file;  # 资源在 server 目录下的 file 目录里
          valid_referers none blocked server_names *.web.com; # server_names 后指定具体的域名或者 IP
          if ($invalid_referer){
              rewrite ^/ http://www.web.com/images/forbidden.png;  # 跳转到 file 目录下的图片
          }
      }
  }
  ```

- 多级域名 

重写和转发的区别:

- 地址重写浏览器地址会发生变化而地址转发则不变

- 一次地址重写会产生两次请求而一次地址转发只会产生一次请求

- 地址重写到的页面必须是一个完整的路径而地址转发则不需要

- 地址重写因为是两次请求，所以 request 范围内属性不能传递给新页面，而地址转发因为是一次请求所以可以传递值

- 地址转发速度快于地址重写

#### Rewrite常用全局变量

| 变量                 | 说明                                                                                                                               |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------- |
| $args              | 变量中存放了请求 URL 中的请求指令。比如 `http://192.168.200.133:8080?arg1=value1&args2=value2` 中的『 arg1=value1&arg2=value2 』，功能和 $query_string 一样 |
| $http_user_agent   | 变量存储的是用户访问服务的代理信息（如果通过浏览器访问，记录的是浏览器的相关版本信息）                                                                                      |
| $host              | 变量存储的是访问服务器的 server_name 值                                                                                                       |
| $document_uri      | 变量存储的是当前访问地址的URI。比如 `http://192.168.200.133/server?id=10&name=zhangsan`中的『 /server 』，功能和 $uri 一样                                 |
| $document_root     | 变量存储的是当前请求对应 location 的 root 值，如果未设置，默认指向 Nginx 自带 html 目录所在位置                                                                   |
| $content_length    | 变量存储的是请求头中的 Content-Length 的值                                                                                                    |
| $content_type      | 变量存储的是请求头中的 Content-Type 的值                                                                                                      |
| $http_cookie       | 变量存储的是客户端的 cookie 信息，可以通过 add_header Set-Cookie 'cookieName=cookieValue' 来添加 cookie 数据                                           |
| $limit_rate        | 变量中存储的是 Nginx 服务器对网络连接速率的限制，也就是 Nginx 配置中对 limit_rate 指令设置的值，默认是 0，不限制。                                                          |
| $remote_addr       | 变量中存储的是客户端的 IP 地址                                                                                                                |
| $remote_port       | 变量中存储了客户端与服务端建立连接的端口号                                                                                                            |
| $remote_user       | 变量中存储了客户端的用户名，需要有认证模块才能获取                                                                                                        |
| $scheme            | 变量中存储了访问协议                                                                                                                       |
| $server_addr       | 变量中存储了服务端的地址                                                                                                                     |
| $server_name       | 变量中存储了客户端请求到达的服务器的名称                                                                                                             |
| $server_port       | 变量中存储了客户端请求到达服务器的端口号                                                                                                             |
| $server_protocol   | 变量中存储了客户端请求协议的版本，比如 『 HTTP/1.1 』                                                                                                 |
| $request_body_file | 变量中存储了发给后端服务器的本地文件资源的名称                                                                                                          |
| $request_method    | 变量中存储了客户端的请求方式，比如『 GET 』,『 POST 』等                                                                                               |
| $request_filename  | 变量中存储了当前请求的资源文件的路径名                                                                                                              |
| $request_uri       | 变量中存储了当前请求的 URI，并且携带请求参数，比如 `http://192.168.200.133/server?id=10&name=zhangsan` 中的 『 /server?id=10&name=zhangsan 』               |

### 访问限流

```conf
# 限制用户连接数来预防 DOS 攻击
limit_conn_zone $binary_remote_addr zone=perip:10m;
limit_conn_zone $server_name zone=perserver:10m;
# 限制同一客户端 ip 最大并发连接数
limit_conn perip 2;
# 限制同一server最大并发连接数
limit_conn perserver 20;
# 限制下载速度，根据自身服务器带宽配置
limit_rate 300k; 
```

### 链接超时

```conf
# 客户端、服务端设置
server_names_hash_bucket_size 128;
server_names_hash_max_size 512;
# 长连接超时配置
keepalive_timeout  65;
client_header_timeout 15s;
client_body_timeout 15s;
send_timeout 60s;

# 代理设置
# 与后端服务器建立连接的超时时间。注意这个一般不能大于 75 秒
proxy_connect_timeout 30s;
proxy_send_timeout 120s;
# 从后端服务器读取响应的超时
proxy_read_timeout 120s;
```

### 反向代理

- 正向代理：代理的是客户端。

- 反向代理：代理的是服务端。

反向代理中常用的指令：

- proxy_pass：配置代理的服务器地址。
  
  - 语法：`proxy_pass <URL>;`
  
  ```conf
  # 例子
  location /server {
      # 结尾不加斜杠
      proxy_pass http://192.168.200.146;
      # 访问的是：http://192.168.200.146/server/index.html
  
      # 结尾加斜杠
      proxy_pass http://192.168.200.146/;
      # 访问的是：http://192.168.200.146/index.html
  }
  ```

- proxy_set_header：转发给被代理服务器时，设置一些请求头信息。
  
  ```conf
  # 目标服务器
  server {
      listen  8080;
      server_name localhost;
      default_type text/plain;
      return 200 $http_username;    # 获取代理服务器发送过来的 http 请求头的 username 值
  }
  
  # 代理服务器
  server {
      listen  8080;
      server_name localhost;
      location /server {           # 访问 /server 触发代理
          proxy_pass http://192.168.200.146:8080/;  # 配置服务器 B 的地址
          proxy_set_header username TOM;  # 发送 key 为 username，value 为 TOM 的请求头给服务器 B
      }
  }
  ```

- proxy_redirect：防止客户端可以看到被代理服务器的地址。
  
  - 语法：
  
  ```conf
  # redirect：被代理服务器返回的 Location 值
  # replacement：要替换 Location 的值
  proxy_redirect redirect replacement;
  proxy_redirect default;
  proxy_redirect off;
  ```
  
  - 为什么要使用`proxy_redirect`指令？
    
    - 避免将真实的目标地址暴露给客户端。该指令是用来重置头信息中的『 Location 』和『 Refresh 』的值，防止客户端可以看到被代理服务器的地址。
  
  ```conf
  # 目标服务器
  server {
      listen  8081;
      server_name localhost;
      if (!-f $request_filename){
          return 302 http://192.168.200.146;   #  2.如果请求的资源不存在，则重定向到服务器 B
      }
  }
  
  # 代理服务器
  server {
      listen  8081;
      server_name localhost;
      location / {
          proxy_pass http://192.168.200.146:8081/;  # 1.转发给服务器 B
          proxy_redirect http://192.168.200.146 http://192.168.200.133; # 3.修改服务器 B 的地址
      }
  }
  # 该 server 去请求服务器 B 的欢迎页面
  server {
      listen  80;
      server_name 192.168.200.133;
      location / {
          proxy_pass http://192.168.200.146;  # 4.重新发送请求给服务器 B，获取欢迎页面
      }
  }
  ```

思考：在编写 proxy_pass 的时候，后面的值要不要加`/`。

```conf
server {
    listen 80;
    server_name localhost;
    location / {
        # 下面两个地址加不加斜杠，效果都一样，因为 location 后的 / 会添加在代理地址后面
        proxy_pass http://192.168.200.146;
        proxy_pass http://192.168.200.146/;
    }
}

server{
    listen 80;
    server_name localhost;
    location /server {
        # 下面两个地址必须加斜杠，因为 location 后的 /server 会添加在代理地址后面，第一个将没有 / 结尾
        #proxy_pass http://192.168.200.146;
        proxy_pass http://192.168.200.146/;
    }
}
# 上面的 location /server：当客户端访问 http://localhost/server/index.html
# 第一个 proxy_pass 就变成了 http://localhost/server/index.html
# 第二个 proxy_pass 就变成了 http://localhost/index.html 效果就不一样了。
```

如果不以 / 结尾，则 location 后的 /server 会添加在地址后面，所以第一个 proxy_pass 因为没有 / 结尾而被加上 /server，而第二个自带了 / ，所以不会添加 /server。

一般来说：访问任意请求如 /server 时，想要代理到其他服务器的首页，则加 /，否则你如果真的想访问 /server 下的资源，那么不要加 /。所以加了 / 后，请求的是服务器根目录下的资源。

> 斜杠总结参考：https://frxcat.fun/middleware/Nginx/Nginx_Reverse_proxy/#%E6%96%9C%E6%9D%A0%E6%80%BB%E7%BB%93

#### Nginx添加SSL的支持

SSL的支持需要添加`--with-http_ssl_module`模块。ssl指令用来在指定的服务器开启HTTPS，默认关闭。如需开始则使用配置`listen 443 ssl; 或者 ssl on | off;`。

HTTPS 是一种通过计算机网络进行安全通信的传输协议。它经由 HTTP 进行通信，利用 SSL/TLS 建立全通信，加密数据包，确保数据的安全性。

##### SSL相关指令

- `ssl_certificate`：指令是为当前这个虚拟主机指定一个带有 PEM 格式证书的证书。
  
  - 语法：`ssl_certificate <file>;`

- `ssl_certificate_key`：指令用来指定 PEM secret key 文件的路径。
  
  - 语法：`ssl_ceritificate_key <file>;`

- `ssl_session_cache`：指令用来配置用于 SSL 会话的缓存。
  
  - 语法：`ssl_sesion_cache <off | none | [builtin[:size]] [shared:name:size]>`
    
    - off：严格禁止使用会话缓存：Nginx 明确告诉客户端会话不能被重用。
    
    - none：禁止使用会话缓存，Nginx 告诉客户端会话可以被重用，但实际上并不在缓存中存储会话参数。默认值。
    
    - builtin：内置 OpenSSL 缓存，仅在一个工作进程中使用。缓存大小在会话中指定。如果未给出大小，则等于 20480 个会话。使用内置缓存可能会导致内存碎片。
    
    - shared：所有工作进程之间共享缓存，缓存的相关信息用 name 和 size 来指定，同 name 的缓存可用于多个虚拟服务器。name 是允许缓存的数据名，size 是允许缓存的数据大小，以字节为单位。
    
    ```conf
    ssl_session_cache builtin:1000 shared:SSL:10m;
    ```

- `ssl_session_timeout`：指令用于开启 SSL 会话功能后，设置客户端能够反复使用储存在缓存中的会话参数时间，默认值超时时间是 5 秒。
  
  - 语法：`ssl_session_timeout <time>;`

- `ssl_ciphers`：指令指出允许的密码，密码指定为 OpenSSL 支持的格式。
  
  - 语法：`ssl_ciphers <ciphers>;`
  
  使用`openssl ciphers`查看OpenSSl 支持的格式。

- `ssl_prefer_server_ciphers`：指令指定是否服务器密码优先客户端密码，默认关闭，建议开启。
  
  - 语法：`ssl_perfer_server_ciphers <on | off>;`

##### SSL通用配置

```conf
server {
    listen       80;
    # ......
}
server {
    listen       443 ssl;        # 开启 SSL 功能
    server_name  localhost;     # 如果是购买的域名，这里加上该域名

    ssl_certificate      /root/cert/server.cert; # 生成的 cert 或者 pem 证书路径，根据需求修改
    ssl_certificate_key  /root/cert/server.key; # 生成的 key 证书路径，根据需求修改
    ssl_session_timeout 5m; 
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4; # 表示使用的加密套件的类型
    ssl_protocols TLSv1.1 TLSv1.2 TLSv1.3;  # 表示使用的TLS协议的类型
    ssl_prefer_server_ciphers on;

    location / {
        root   html;
        index  index.html index.htm;
    }
}
```

解决默认`http://`问题：

```conf
server {
    listen 443 ssl;
    server_name www.test.com;   # 如果是 www.test.com 发送请求

    location / {
        # ......
        rewrite ^(.*)$ https://www.test.com$1;  # 则改为 https 方式
        # ......
    }
    # ......
}
```

#### 反向代理系统缓冲区调优

- `proxy_buffering`：指令用来开启或者关闭代理服务器的缓冲区，默认开启。
  
  - 语法：`proxy_buffering <on | off>;`

- `proxy_buffers`：指令用来指定单个连接从代理服务器读取响应的缓存区的个数和大小。
  
  - 语法：`proxy_buffers <number> <size>;`。
  
  - number：缓冲区的个数，size：每个缓冲区的大小。缓冲区的总大小就是 number * size。
  
  - 默认`proxy_buffers 8 4k | 8k`

- `proxy_buffer_size`：指令用来设置从被代理服务器获取的第一部分响应数据的大小。保持与 proxy_buffers 中的 size 一致即可，当然也可以更小。
  
  - 语法：`proxy_buffer_size <size>;`。默认`proxy_buffers 8 4k | 8k`。

- `proxy_busy_buffers_size`：指令用来限制同时处于 BUSY 状态的缓冲总大小。
  
  - 语法：`proxy_busy_buffers_size <size>;`。默认`proxy_busy_buffers_size 8k | 16K;`。

- `proxy_temp_path`：指令用于当缓冲区存满后，仍未被 Nginx 服务器完全接受，响应数据就会被临时存放在磁盘文件上的该指令设置的文件路径下。
  
  - 语法：`proxy_temp_path <path>;`。默认`proxy_temp_path proxy_temp;`。但需要保证path最多设置三层路径。

- `proxy_temp_file_write_size`：指令用来设置磁盘上缓冲文件的大小。
  
  - 语法：`proxy_temp_file_write_size <size>;`。默认`proxy_temp_file_write_size 8K | 16K;`。

调优通用配置：

```conf
proxy_buffering on;
proxy_buffers 4 64k;
proxy_buffer_size 64k;
proxy_busy_buffers_size 128k;
proxy_temp_file_write_size 128k;
```

#### 代理服务器获取目标服务器的静态文件

```conf
server {
    listen  80;
    server_name localhost;

    location ~ .* {                            # 如果不是代理网站的根路径，请自行修改
        proxy_pass http://127.0.0.1:8081;   # 代理的网站地址
        # 将网站的静态文件也代理过来
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 负载均衡

- 四层负载均衡指的是在OSI模型中的传输层，主要是基于ip+port的负载均衡。

- 七层负载均衡指的是在应用层，主要是基于虚拟的URL或主机ip的负载均衡。

- 两者的区别：
  
  - 四层负载均衡数据包是在底层就进行了分发，而七层负载均衡数据包则在最顶端进行分发，所以四层负载均衡的效率比七层负载均衡的要高（四层比七层少，速度块，效率高，但是可能请求丢失等）
  
  - 四层负载均衡不识别域名，而七层负载均衡识别域名。

实际环境采用的是：四层负载(LVS) + 七层负载(Nginx)

#### 七层负载均衡指令

Nginx 要实现七层负载均衡需要用到 proxy_pass 代理模块配置。Nginx默认安装支持这个模块。

Nginx 的负载均衡是在 Nginx 反向代理的基础上把用户的请求根据指定的算法分发到一组「upstream 虚拟服务池」。

- upstream：该指令是用来定义一组服务器，服务器可以指定不同的权重，默认为 1。它们可以是监听不同端口的服务器，也可以是同时监听 TCP 和 Unix socket 的服务器。
  
  - 语法：`upstream <name> {ip+port;ip+port ...}`

- server：该指令用来指定后端服务器的名称和一些参数，可以使用域名、IP、端口或者 Unix socket。
  
  - 语法：`server <name> [paramerters]`
  
  - server 后的 name 就是 upstream 后的 name，两者保持一致。

- 案例

```conf
# 服务器 1
server {
    listen   9001;
    server_name localhost;
    default_type text/html;
    location /{
        return 200 '<h1>192.168.200.146:9001</h1>';
    }
}

# 服务器 2
server {
    listen   9002;
    server_name localhost;
    default_type text/html;
    location /{
        return 200 '<h1>192.168.200.146:9002</h1>';
    }
}

# 服务器 3
server {
    listen   9003;
    server_name localhost;
    default_type text/html;
    location / {
        return 200 '<h1>192.168.200.146:9003</h1>';
    }
}

# 代理服务器
upstream backend{
    server 192.168.200.146:9091;
    server 192.168.200.146:9092;
    server 192.168.200.146:9093;
}
server {
    listen 8083;
    server_name localhost;
    location / {
        proxy_pass http://backend;   # backend 要对应上 upstream 后的值，根据需求修改
    }
}
```

此时访问代理服务器的地址`http://192.168.200.133:8083`，它会找到proxy_pass的值，即会根据backend找到对应的upstream中的地址，替换掉backend。如下所示：

```text
proxy_pass http://192.168.200.146:9091
proxy_pass http://192.168.200.146:9092
proxy_pass http://192.168.200.146:9093
```

但是它不会全部访问三个服务器地址，而是根据自己的算法（默认轮询）选择其中一个服务器地址。

#### 七层负载均衡状态

- down：当前的 server 暂时不参与负载均衡。该状态一般会对需要停机维护的服务器进行设置。

- backup：将该服务器标记为备份服务器，当主服务器不可用时，才用备份服务器来传递请求。
  
  - 它不同于 down 指令，down 指令将服务器永久禁止，而 backup 指令仅仅临时禁止，当主服务器不可用后，临时禁止的服务器就会站出来。

- max_fails：设置允许请求代理服务器失败的次数，默认为 1。

- fail_timeout：设置经过 max_fails 失败后，服务暂停的时间，默认是 10 秒。

- max_cons：用来限制同时连接到 upstream 负载上的单个服务器的最大连接数。默认为 0，表示不限制，使用该配置可以根据后端服务器处理请求的并发量来进行设置，防止后端服务器被压垮。

七层负载均衡状态的用法：

```conf
upstream backend{
    # 暂时不参与负载均衡
    server 192.168.200.146:9001 down;

    # 标记为备份服务器
    server 192.168.200.146:9002 backup;

    # 该服务器最大能被 2 个客户端请求。
    server 192.168.200.146:9003 max_conns=2;

    # 允许请求代理服务器失败三次，并且请求失败后，服务暂停15秒
    server 192.168.200.146:9003 max_fails=3 fail_timeout=15;
}

server {
    listen 8083;
    server_name localhost;
    location / {
        proxy_pass http://backend;
    }
}
```

#### 七层负载均衡策略

- 轮询（默认）：每个请求会按时间顺序逐个分配到不同的后端服务器。轮询不需要额外的配置。

- weight：权重方式。用来设置服务器的权重，默认为 1，权重数据越大，被分配到请求的几率越大；该权重值，主要是针对实际工作环境中不同的后端服务器硬件配置进行调整的，所有此策略比较适合服务器的硬件配置差别比较大的情况。

- ip_hash：依据IP分配方式。使用 ip_hash 指令无法保证后端服务器的负载均衡，可能导致有些后端服务器接收到的请求多，有些后端服务器接收的请求少，而且设置后端服务器权重等方法将不起作用。

- least_conn：依据最少连接方式。此负载均衡策略适合请求处理时间长短不一造成服务器过载的情况。

- url_hash：依据URL分配方式。

- fair：依据响应时间方式。根据页面大小、加载时间长短智能的进行负载均衡。使用fair需要添加`nginx-upstream-fair`模块。

七层负载均衡策略的用法：

```conf
upstream backend{
    # server 192.168.200.146:9001 weight=10;
    # ip_hash;
    # least_conn;
    # hash &request_uri;
    # fair;
    server 192.168.200.146:9002;
    server 192.168.200.146:9003;
}

server {
    listen 8083;
    server_name localhost;
    location /{
        proxy_pass http://backend;
    }
}
```

#### 四层负载均衡指令

Nginx在1.9之后，增加了一个stream模块，用来实现四层协议的转发、代理、负载均衡等。用法与http模块类似。

四层协议负载均衡的实现，一般采用LVS、HAProxy、F5等。但要么成本高或者配置麻烦。使用Nginx相对来说跟简单。

Nginx使用四层负载均衡需要用到stream模块，需要添加模块`--with-stream`。

七层负载均衡是在http块中实现的，而四层负载均衡也可以在stream块中实现。stream提供在其中指定流服务器指令的配置文件上下文。和 http 模块同级。

upstream用法与七层负载均衡类似。

用法如下：

```conf
http {
    server {
        listen 80;
        # ......
    }
}
stream {
    upstream backend{
        server 192.168.200.146:6379;
        server 192.168.200.146:6378;
    }
    server {
        listen 81;
        proxy_pass backend;
    }
}
```

### Nginx缓存

Nginx 的 Web 缓存服务主要是使用 `ngx_http_proxy_module` 模块相关指令集来完成。

#### 缓存指令

- `proxy_cache_path`：设置缓存文件的存放路径。
  
  - 语法：`proxy_cache_path <path> [levels=number] <keys_zone=zone_name:zone_size> [inactive=time][max_size=size];`
    
    - levels：指定该缓存空间 path 基础上新建的目录（存储路径在 path 目录基础上再创建新的目录），最多可以设置 3 层，每层取 1 到 2 个字母作为目录名。字母名从 MD5 加密的值后面往前截取。
    
    - keys_zone：用来为这个缓存区设置名称和指定大小。
    
    - inactive：指定的时间内未访问的缓存数据会从缓存中删除，默认情况下，inactive 设置为 10 分钟。
    
    - max_size：设置最大缓存空间，如果缓存空间存满，默认会覆盖缓存时间最长的资源，默认单位为兆。
  
  ```conf
  # 假设 proxy_cache_key 为 kele，通过 MD5 加密以后的值为 27ce47ea65c1381dbe5175f7c77d8a3a
  # levels=1:2    # 最终的存储路径为 /usr/local/proxy_cache/a/a3，每层截取个数根据 1:2
  # levels=2:1:2  # 最终的存储路径为 /usr/local/proxy_cache/3a/a/d8，每层截取个数根据 2:1:2
  # levels=2:2:2  # 最终的存储路径为 /usr/local/proxy_cache/3a/8a/7d，每层截取个数根据 2:2:2
  # keys_zone=kele:200m  # 缓存区的名称是 kele，大小为 200M，1M 大概能存储 8000 个 keys
  # inactive=1d   # 缓存数据在 1 天内没有被访问就会被删除
  # max_size=20g    # 最大缓存空间为 20G
  
  http{
      proxy_cache_path /usr/local/proxy_cache keys_zone=kele:200m levels=1:2:1 inactive=1d max_size=20g;
  }
  ```

- `proxy_cache`：开启或关闭代理缓存，如果是开启则自定义使用哪个缓存区来进行缓存。默认关闭。
  
  - 语法：`proxy_cache <zone_name | off>;`
  
  - 缓存区的名称`zone_name`必须是 proxy_cache_path 里的 keys_zone 生成的缓存名。

- `proxy_cache_key`：设置 Web 缓存的 key 值，Nginx 会根据 key 值利用 MD5 计算处哈希值并缓存起来，作为缓存目录名的参考。
  
  - 语法：`proxy_cache_key <key>;`。

- `proxy_cache_valid`：对不同返回状态码的 URL 设置不同的缓存时间。
  
  - 语法：`proxy_cache_valid [code ...... ] <time>;`。
  
  ```conf
  proxy_cache_valid 200 302 10m; # 为 200 和 302 的响应 URL 设置 10 分钟缓存时间
  proxy_cache_valid 404 1m;      # 为 404 的响应 URL 设置 1 分钟缓存时间
  proxy_cache_valid any 1m;      # 对所有响应状态码的URL都设置 1 分钟缓存时间
  ```

- `proxy_cache_min_uses`：设置资源被访问多少次后才会被缓存。默认是 1 次。
  
  - 语法：`proxy_cache_min_uses <number>;`

- `proxy_cache_methods`：设置缓存哪些 HTTP 方法的请求资源。默认缓存 HTTP 的 GET 和 HEAD 方法的请求资源，不缓存 POST 方法的请求资源。
  
  - 语法：`proxy_cache_methods <GET | HEAD | POST>;`

#### 缓存用法

```conf
# 代理服务器
http{
    proxy_cache_path /usr/local/proxy_cache levels=2:1 keys_zone=bing:200m inactive=1d max_size=20g;

    upstream backend{
        server 192.168.200.146:8080;   # 服务器 A 的地址
    }
    server {
        listen       8080;                 # 监听 8080 端口
        server_name  localhost;         # 监听 localhost 的IP
        location / {                    # 监听包含 / 的请求
            proxy_cache bing;            # 开启 bing 缓存区，和第 2 行的 keys_zone 对应
            proxy_cache_key kele;          # 缓存的 key 值，会被 MD5 解析成字符串用于生成缓存的目录
            proxy_cache_min_uses 5;     # 资源被访问 5 次后才会被缓存
            proxy_cache_valid 200 5d;    # 为 200 响应 URL 设置 5 天缓存时间
            proxy_cache_valid 404 30s;  # 为 404 的响应 URL 设置 30 秒缓存时间
            proxy_cache_valid any 1m;    # 为除了上方的任意响应 URL 设置 1 分钟缓存时间
            add_header nginx-cache "$upstream_cache_status";  # 将缓存的状态放到请求头里
            proxy_pass http://backend/js/;  # 代理 backend，将 /js/ 追加到 backend 模块里的地址后面
        }
    }
}
```

#### 缓存删除

- 删除缓存目录
  
  ```shell
  # 假设缓存目录是 /usr/local/proxy_cache/
  rm -rf /usr/local/proxy_cache/
  ```

- 使用第三方扩展模块 `ngx_cache_purge` 进行删除缓存。使用前需要添加模块。
  
  语法：`proxy_cache_purge <cache> <key>`。cache 是 proxy_cache，key 是 proxy_cache_key。
  
  ```conf
  server{
      location ~/purge(/.*) {
          proxy_cache_purge bing kele;
      }
  }
  ```

#### 不缓存数据

不是所有的数据都适合进行缓存。

- `proxy_no_cache`：定义不将数据进行缓存的条件，也就是不缓存指定的数据。
  
  - 语法：`proxy_no_cache <string> ...... ;`。可设置多个 string。
  
  - `proxy_no_cache $cookie_nocache $arg_nocache $arg_comment;`

- `proxy_cache_bypass`：设置不从缓存中获取数据的条件，也就是虽然缓存了指定的资源，但请求过来也不会去获取它，而是去服务器里获取资源。
  
  - 语法：`proxy_cache_bypass <string> ...... ;`
  
  - `proxy_cache_bypass $cookie_nocache $arg_nocache $arg_comment;`

上述两个指令都有一个指定的条件，这个条件可以是多个，并且多个条件中至少有一个不为空且不等于「0」，则条件满足成立。

资源不缓存配置：

```conf
server {
    listen    8080;
    server_name localhost;
    location / {
        if ($request_uri ~ /.*\.js$){
            set $nocache 1;
        }
        proxy_no_cache $nocache $cookie_nocache $arg_nocache $arg_comment;
        proxy_cache_bypass $nocache $cookie_nocache $arg_nocache $arg_comment;
    }
}
```

如果 `$nocache $cookie_nocache $arg_nocache $arg_comment` 任意不为空或 0，则访问的资源不进行缓存。

### Nginx集群搭建

如果使用单机Nginx，当Nginx宕机后，那么整个系统都会处于瘫痪状态。如何解决？答案就是Nginx集群。

至少需要两台以上的 Nginx 服务器对外提供服务，这样的话就可以解决其中一台宕机了，另外一台还能对外提供服务，但是如果是两台 Nginx 服务器的话，会有两个 IP 地址，用户该访问哪台服务器，用户怎么知道哪台是好的，哪台是宕机了的？

如上问题使用 Keepalived 解决，Keepalived主要是通过VRRP协议实现高可用功能。

基本思路就是：使用Keepalived后，会暴露一个虚拟ip用来和客户端交互，而一旦客户端请求到虚拟ip，虚拟ip就会发送给master的Nginx。如果master的Nginx宕机了，才会发送给Backup的Nginx进行路由。

> 具体搭建参考：https://www.bilibili.com/video/BV1ov41187bq?p=128

### Nginx站点下载&认证

配置模板：

```conf
location /download {
    root /opt;                # 下载目录所在的路径，location 后面的 /download 拼接到 /opt 后面
    # 以这些后缀的文件点击后为下载，注释掉则 txt 等文件是在网页打开并查看内容
    if ($request_filename ~* ^.*?\.(txt|doc|pdf|rar|gz|zip|docx|exe|xlsx|ppt|pptx|conf)$){
			  add_header Content-Disposition 'attachment;';
		  }
    autoindex on;			  # 启用目录列表的输出
    autoindex_exact_size on;  # 在目录列表展示文件的详细大小
    autoindex_format html;	  # 设置目录列表的格式为 html
    autoindex_localtime on;   # 目录列表上显示系统时间
}
```

root 指令后面必须是下载路径，因为我的下载路径是 /opt/download，所以这里填写 /opt，而 location 的 /download 会自动拼接到后面，形成完整的下载路径。

> 认证参考：https://www.bilibili.com/video/BV1ov41187bq?p=140

### lua

> lua脚本编写参考：http://www.lua.org/manual/5.4/

## 附录

### nginx中配置root和alias的区别

首先root和alias都可以代理静态资源。

- 当使用root时，访问的路径是：root指定的值 + location指定的值。

- 当使用alias时，无论location配置的值是什么，nginx都会转发到alias配置的路径中，与location无关。

### nginx核心功能

静态资源配置、反向代理、负载均衡