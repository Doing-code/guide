# Linux

## 基础命令

### Linux目录结构

Linux的目录结构是一个树型结构，Linux没有盘符概念，只有一个根目录`/`，所以文件都挂载到根目录下，Linux中一切皆文件，一个设备、磁盘、网卡等都是一个文件。

![](https://github.com/Doing-code/guide/blob/main/image/linux_目录结构.png)

Windows路径描述是`\`，而Linux路径描述是`/`。表示层级关系。

### Linux基础命令

#### ls

- 作用：列出目录下的内容。

- 命令：`ls [option] [Linux路径]`
  
  - option，可选项参数：
    
    - `-a`：列出所以内容，包括隐藏文件。
    
    - `-l`：使用较长格式列出信息（文件的详细信息，如权限、创建时间等）。
    
    - -`h`：与`-l`一起使用，以易于阅读的格式输出文件大小，(例如 1K 234M 2G)
    
    - 其他指令：可通过命令`ls --help`进行查看。
  
  - 如果携带了Linux路径，则列出指定路径的内容信息。

```shell
[root@server7 test]# ls
Hello.java  test01

[root@server7 test]# ls -a
.  ..  Hello.java  test01

[root@server7 test]# ls -l
总用量 0
-rw-r--r--. 1 root root 0 7月  24 16:08 Hello.java
drwxr-xr-x. 2 root root 6 7月  24 16:08 test01

[root@server7 test]# ls -l -h
总用量 4.0K
drwxr-xr-x. 3 root root   38 7月  24 16:08 .
dr-xr-x---. 7 root root 4.0K 7月  24 16:08 ..
-rw-r--r--. 1 root root    0 7月  24 16:08 Hello.java
drwxr-xr-x. 2 root root    6 7月  24 16:08 test01

[root@server7 /]# ls -l -h ~/test/
总用量 0
-rw-r--r--. 1 root root 0 7月  24 16:08 Hello.java
drwxr-xr-x. 2 root root 6 7月  24 16:08 test01

[root@server7 /]# ls -l -h ~/test/Hello.java 
-rw-r--r--. 1 root root 0 7月  24 16:08 /root/test/Hello.java
```

### 目录切换相关命令(cd/pwd)

#### cd

cd是 change directory 的缩写。

- 作用：目录切换。

- 命令：`cd [路径]`。
  
  - 如果不携带路径，表示切换到当前用户的home目录。
  
  - 如果携带路径，表示切换到指定目录。

```shell
[root@server7 ~]# cd /
[root@server7 /]# cd ~
[root@server7 ~]#
```

#### pwd

pwd是Print Work Directory的缩写。

- 作用：查看当前所在目录。

- 命令：`pwd`。

```shell
[root@server7 /]# pwd
/
```

### 相对路径&绝对路径&特殊路径符

- 相对路径：以当前目录为起点，描述路径的一种写法。路径描述不需要以`/`开头。

- 绝对路径：以根目录为起点，描述路径的一种写法。路径描述需要以`/`开头。

- 特殊路径符：
  
  - `.`：表示当前目录。如`cd ./Desktop`表示切换到当前目录下的Desktop目录，和`cd Desktop`效果一样。
  
  - `..`：表示上一级目录。比如`cd ..`即可切换到上一级目录，`cd ../..`切换到上二级的目录。
  
  - `~`：表示home目录。比如`cd ~`即可切换到home目录。`cd ~/Desktop`表示切换到home内的Desktop目录。

### 创建目录命令(mkdir)

mkdir是Make Directory的缩写。

- 作用：创建文件夹。

- 命令：`mkdir [option] 目录`。
  
  - 目录：如果只写文件夹名，则使用相对路径，在当前目录下创建文件夹；如果写全路径，则在指定路径下创建文件夹。
  
  - option：
    
    - `-p`：如果父目录不存在则自动创建，适用于创建多层级的目录。
  
  - 其它命令：通过`mkdir --help`命令查看。

```shell
[root@server7 test]# mkdir test02
[root@server7 test]# mkdir ~/test/test02/test03

[root@server7 test]# ls
Hello.java  test01  test02

[root@server7 test]# ls test02
test03
```

### 文件操作命令(touch、cat、more)

#### touch

- 作用：创建文件。

- 命令：`touch 路径`。
  
  - 参数表示要创建的文件路径。相对、绝对、特殊路径均可使用。

```shell
[root@server7 test]# touch t1.conf
[root@server7 test]# touch test02/t2.conf

[root@server7 test]# ls 
Hello.java  t1.conf  test01  test02

[root@server7 test]# ls test02/
t2.conf  test03
```

#### cat

- 作用：查看文件内容，一次全部显示出来。

- 命令：`cat 路径`。
  
  - 参数表示被查看的文件路径，相对、绝对、特殊路径均可使用。

```shell
[root@server7 test]# cat t1.conf 
Hello World.
```

#### more

- 作用：查看文件内容，如果文件内容较多，支持分页显示。

- 命令：`more 路径`。
  
  - 参数表示被查看的文件路径，相对、绝对、特殊路径均可使用。

```shell
[root@server7 test]# more t1.conf 
Hello World.
```

通过空格进行翻页。

### 文件操作命令(cp、mv)

#### cp

cp是Copy的缩写。

- 作用：复制文件/文件夹。

- 命令：`cp [option] source target`
  
  - option：
    
    - `-r`：递归复制目录及其子目录内的所有内容。
  
  - source：被复制的文件或文件夹。
  
  - target：目标路径。
  
  - 其它命令：通过`cp --help`命令查看。

```shell
[root@server7 test]# cp t1.conf t1.conf.copy
[root@server7 test]# cp test02/t2.conf test02/t2.conf.copy

[root@server7 test]# ls
Hello.java  t1.conf  t1.conf.copy  test01  test02

[root@server7 test]# ls test02/
t2.conf  t2.conf.copy  test03

[root@server7 test]# cp test02/ test01/
cp: 略过目录"test02/"

[root@server7 test]# cp -r test02/ test01/
```

#### mv

mv是move的缩写。

- 作用：移动文件或文件夹。

- 命令：`mv source target`。
  
  - source：要移动的文件或文件夹。
  
  - target：目标路径。如果目标不存在，则进行改名确保目标存在。

```shell
[root@server7 test]# ls
Hello.java  t1.conf  t1.conf.copy  test01  test02

[root@server7 test]# mv t1.conf t2.conf

[root@server7 test]# ls
Hello.java  t1.conf.copy  t2.conf  test01  test02

[root@server7 test]# mv t2.conf test01/t1.conf

[root@server7 test]# ls test01/
t1.conf  test02

[root@server7 test]# mv test01/ test02/test10

[root@server7 test]# ls test02/
t2.conf  t2.conf.copy  test01  test03  test10
```

#### rm

rm是remove的缩写。

- 作用：删除文件或文件夹。

- 命令：`rm [option] file`。
  
  - option：
    
    - `-f`：force，强制删除（不会有提示确认信息）
    
    - `-t`：递归地删除目录及其内容。
  
  - file：可以是文件或目录。可以有一个file或多个file。多个文件用空格隔开。
  
  - file支持通配符：`*`即文件模糊匹配。以什么开头、以什么结尾、包含什么内容。如`test*`、`*test`、`*test*`。

```shell
[root@server7 test]# ls
Hello.java  t1.conf.copy test02 test03

[root@server7 test]# ls test02/
t2.conf  t2.conf.copy  test01  test03  test10

[root@server7 test]# rm -rf test02/

[root@server7 test]# ls
Hello.java  t1.conf.copy  test03
```

谨慎使用如下命令：

```shell
rm -rf /

rm -rf /*
```

效果等同于在Windows中执行C盘格式化。

### 查找命令(which、find)

#### which

Linux命令实际上本体就是一个个二进制的可执行程序。

- 作用：查看Linux命令的程序文件的存放路径。

- 命令：`which 要查找的命令`。

```shell
[root@server7 test]# which ll
alias ll='ls -l --color=auto'
    /usr/bin/ls
[root@server7 test]# which cd
/usr/bin/cd
[root@server7 test]# which pwd
/usr/bin/pwd
[root@server7 test]# which touch
/usr/bin/touch
[root@server7 test]# which which
alias which='alias | /usr/bin/which --tty-only --read-alias --show-dot --show-tilde'
    /usr/bin/alias
    /usr/bin/which
```

#### find

默认路径为当前目录。

- 作用：搜索指定的文件。

- 命令：`find 起始路径 -name "filename"`。
  
  - `-name`：表示按照文件名的模式进行搜索。文件名是`"filename"`。
  
  - 查找文件名支持通配符。模糊匹配。

- `find 起始路径 -type d -iname "test02"`搜索文件夹。

- 按照文件大小查找文件：`find 起始路径 -size [+|-]n[KMG]`
  
  - `+|-`：+表示大于、-表示小于。
  
  - `n`：表示文件大小。
  
  - `KMG`：表示文件大小单位。

```shell
[root@server7 test]# ls
Hello.java  t1.conf.copy  test03

[root@server7 test]# find ./ -name "Hello.java"
./Hello.java

[root@server7 test]# find / -name "Hello.java"
# ... ...

[root@server7 test]# find ./ -name "*.java"
./Hello.java

[root@server7 test]# find ./ -size -10K
[root@server7 test]# find ./ -size +10M
[root@server7 test]# find ./ -size +1G
```

### grep、wc和管道符

#### grep

- 作用：从文件中通过正则表达式过滤文件行。

- 命令：`grep [option]... pattern [file]...`。
  
  - option：
    
    - `-n`：表示在结果中显示匹配的行的行号。
  
  - pattern：在每个 FILE 或是标准输入中查找 PATTERN。过滤的条件。
  
  - file：文件路径，表示要过滤内容的文件路径。如果为空，则配合管道符使用，作为内容输入端口。

```shell
[root@server7 test]# grep -n static grep_test.txt 
1:public static void 
2:public static int
3:public static long
4:public static String

[root@server7 test]# grep static grep_test.txt 
public static void 
public static int
public static long
public static String

[root@server7 test]# grep -n void grep_test.txt 
1:public static void 

[root@server7 test]# grep -n long grep_test.txt 
3:public static long
```

#### wc

- 作用：统计文件的行数、单词数量等。

- 命令：`wc [option] 文件...`。
  
  - option：
    
    - `-c`：统计字节数。
    
    - `-m`：统计字符数。
    
    - `-l`：统计行数。
    
    - `-w`：统计单词数量。（按空格划分）
  
  - 参数：文件路径，被统计的文件。

```shell
# 不加option，默认是 行数、单词数、字节数
[root@server7 test]# wc grep_test.txt 
  5  13 103 grep_test.txt

[root@server7 test]# wc -c grep_test.txt 
103 grep_test.txt

[root@server7 test]# wc -m grep_test.txt 
103 grep_test.txt

[root@server7 test]# wc -l grep_test.txt 
5 grep_test.txt

[root@server7 test]# wc -w grep_test.txt 
13 grep_test.txt
```

#### 管道符

管道符：`|`。

- 作用：将管道符左边命令的结果，作为右边命令的输入。

```shell
[root@server7 test]# cat grep_test.txt | grep void
public static void
```

`cat grep_test.txt`的输出结果，作为右边grep命令的输入。

### echo&tail&重定向符

#### echo

- 作用：在命令行输出指定内容。

- 命令：`echo 输出内容`。

- 带有空格或`\`等特殊符号的内容，建议使用双引号包围。空格后的内容容易被识别为参数2。

- 被``包围的内容，会被作为命令执行。

```shell
[root@server7 test]# echo Hello World
Hello World

[root@server7 test]# echo "Hello World"
Hello World

[root@server7 test]# echo `pwd`
/root/test
```

#### tail

- 作用：查看文件尾部内容，并跟踪文件的最新更改。

- 命令：`tail [option]... [文件]...`。
  
  - option：
    
    - `-f`：表示持续跟踪。
    
    - `-num`：表示查看尾部多少（从尾部开始向上num行），不填默认10行。
  
  - 参数：linux文件路径，表示被跟踪的文件路径。

```shell
[root@server7 test]# tail -f -100 test.txt
# ... ...

[root@server7 test]# tail -f -2 test.txt 
Hello World
Hello World

[root@server7 test]# tail -f -5 test.txt 
Hello World
Hello World
Hello World
Hello World
Hello World
```

#### 重定向符

- `>`：将左侧命令的结果，覆盖写入到符号右侧指定的文件中。

- `>>`：将左侧命令的结果，追加写入到符号右侧指定的文件中。

```shell
[root@server7 test]# echo "Hello World" > test.txt 

[root@server7 test]# cat test.txt 
Hello World

[root@server7 test]# echo "Hello World, Hello Hello" > test.txt 

[root@server7 test]# cat test.txt 
Hello World, Hello Hello

[root@server7 test]# echo "Hello Linux" >> test.txt 

[root@server7 test]# cat test.txt 
Hello World, Hello Hello
Hello Linux

[root@server7 test]# ls > test.txt 

[root@server7 test]# cat test.txt 
grep_test.txt
Hello.java
t1.conf.copy
test03
test.txt
```

不仅仅是使用echo，只要是产生结果的都能往右边写入。

### vi编辑器

vi/vim（visual interface）文本编辑器。vim兼容vi。

- 命令：`vi file`、`vim file`。
  
  - 如果文件不存在，那么此命令用于编辑新文件。
  
  - 如果文件存在，则编辑已有文件。

- 命令模式（command mode）：不能自由地进行文本编辑。以命令驱动执行不同的功能。

- 输入模式（insert mode）：编辑模式。可对文件进行自由编辑。按键盘`i`进入输入模式。

- 底线命令模式（last line mode）：以`:`开始，通常用于文件的保存、退出。

- 在任何情况下输入`esc`都能回到命令模式

#### 命令模式快捷键

| 命令                 | 描述                    |
|:------------------:|:---------------------:|
| `i`                | 在当前光标位置进入输入模式         |
| `a`                | 在当前光标位置之后，进入输入模式      |
| `I`                | 在当前行的开头，进入输入模式        |
| `A`                | 在当前行的结尾，进入输入模式        |
| `o`                | 在当前光标的下一行进入输入模式       |
| `O`                | 在当前光标的上一行进入输入模式       |
| `键盘上`、`键盘k`        | 向上移动光标                |
| `键盘下`、`键盘k`        | 向下移动光标                |
| `键盘左`、`键盘h`        | 向左移动光标                |
| `键盘右`、`键盘l`        | 向右移动光标                |
| `0`                | 移动光标到当前行的开头（字母上方的数字0） |
| `$`                | 移动光标到当前行的结尾           |
| `pageup`（`PgUp`）   | 向上翻页                  |
| `pagedown`（`PgDn`） | 向下翻页                  |
| `/`                | 进入搜索模式                |
| `n`                | 向下继续搜索                |
| `dd`               | 删除光标所在行的内容            |
| `ndd`              | n是数字，表示删除当前光标向下n行     |
| `yy`               | 复制当前行                 |
| `nyy`              | n是数字，表示复制当前光标向下n行     |
| p                  | 粘贴复制的内容               |
| u                  | 撤销修改                  |
| `ctrl + r`         | 反向撤销修改                |
| `gg`               | 移动光标到首行               |
| `G`                | 移动光标到行尾               |
| `dG`               | 从当前行开始，向下全部删除         |
| `dgg`              | 从当前行开始，向上全部删除         |
| `d$`               | 从当前光标开始，删除到结尾，包含光标处   |
| `d0`               | 从当前光标开始，删除到开头，不包含光标处  |

#### 底线命令模式

| 命令           | 描述                              |
| ------------ | ------------------------------- |
| `:wq`        | 保存并退出                           |
| `:q`         | 仅退出                             |
| `:q!`        | 强制退出                            |
| `:w`         | 仅保存                             |
| `:set nu`    | 显示行号                            |
| `:set paste` | 设置粘贴模式，适用于从外部复制的内容，不会产生格式的错乱之类的 |

## Linux用户和权限

Linux采用多用户的管理模式进行权限管理。

在Linux系统中，拥有最大权限的账户名为：root（超级管理员）。

使用命令`sudo -i`即可切换至root用户。使用`exit`命令即可退回到上一个用户。

### 用户、用户组

- 可以配置多个用户。

- 可以配置多个用户组。

- 用户可以加入到多个用户组中。

Linux中关于权限控制有2个级别：

- 针对用户的权限控制。

- 针对用户组的权限控制。

针对某个文件，可以控制用户的权限，也可以控制用户组的权限。

#### 用户组

- 创建用户组：`groupadd 用户组名`。

- 删除用户组：`groupdel 用户组名`。

#### 用户管理

- 创建用户：`useradd [option] 用户名`。
  
  - option：
    
    - `-g`：指定用户所属组，若不指定`-g`，则创建同名组并自动加入；指定`-g`需要组已存在。如已存在同名组，则必须指定`-g`。
    
    - `-d`：指定用户home路径，若不指定，则home目录默认在`/home/用户名`。

- 删除用户：`userdel [option] 用户名`。
  
  - option：
    
    - `-r`：删除用户的home目录，若不加`-r`参数，则删除用户时，home目录保留。

- 查看用户所属组：`id [用户名]`。
  
  - 参数：用户名，查看指定用户，若不提供则查看自身。

- 修改用户所属组：`usermod -aG 用户组 用户名`。将指定用户加入到指定用户组。
  
    `-aG`：表示追加到GROUPS，并不从其它组中删除此用户。
  
  - `-g`：强制使用 GROUP 为新主组。

- 其它命令：通过命令`usermod --help`查看。

- `getent`：查看当前系统中有哪些用户、组。`getent passwd`查看所以用户、`getent group`查看所有组。

```shell
[root@server7 test]# groupadd ceshi

[root@server7 test]# useradd test01 -g ceshi -d /home/test01

[root@server7 test]# su - test01

[test01@server7 ~]$ pwd
/home/test01

[test01@server7 ~]$ exit
登出

[root@server7 home]# ls
test01  root

[root@server7 home]# id test01
uid=1001(test01) gid=1002(ceshi) 组=1002(ceshi)

[root@server7 home]# groupadd dev

[root@server7 home]# usermod -aG dev test01

[root@server7 home]# id test01
uid=1001(test01) gid=1002(ceshi) 组=1002(ceshi),1003(dev)

[root@server7 home]# usermod -g dev test01

[root@server7 home]# id test01
uid=1001(test01) gid=1003(dev) 组=1003(dev)

[root@server7 home]# getent passwd
# ... ...

[root@server7 home]# getent passwd | grep test*
test01:x:1001:1003::/home/test01:/bin/bash
# 输出信息说明：用户名:密码(x)用户id:组id:描述信息:home目录:执行终端(默认bash)

[root@server7 home]# getent group | grep dev
dev:x:1003:test01
# 组名称:组认证(显示为x):组i:用户名称

[root@server7 home]# getent group | grep ceshi
ceshi:x:1002:
```

### 查看权限控制

通过`ls -l`命令可以以列表形式查看文件，并显示权限等信息：

```shell
[root@server7 test]# ls -l
总用量 16
-rw-r--r--. 1 root root  41 7月  24 20:53 01.txt
-rw-r--r--. 1 root root 103 7月  24 18:48 grep_test.txt
-rw-r--r--. 1 root root   0 7月  24 16:08 Hello.java
-rw-r--r--. 1 root root  13 7月  24 17:39 t1.conf.copy
drwxr-xr-x. 2 root root   6 7月  24 17:55 test03
-rw-r--r--. 1 root root 712 7月  24 20:08 test.txt
```

信息说明：

- 第一列：表示文件、文件夹的权限控制信息。

- 第三列：表示文件、文件夹所属用户。

- 第四列：表示文件、文件夹所属用户组。

权限分为10个槽位：

![](https://github.com/Doing-code/guide/blob/main/image/linux_权限槽位描述.png)

举例：`drwxr-xr-x`

- 这是一个文件夹，首字母d表示。

- 所属用户的权限是：rwx，可读、可写、可执行。

- 所属用户组的权限是：r-x，可读、不可写、可执行。

- 其它用户的权限是：r-x，可读、不可写、可执行。（非该文件的所属用户或所属用户组）

rwx表示什么意思呢？

- r表示读权限。
  
  - 针对文件表示可以查看文件内容。针对文件夹表示可以查看文件夹内容。如`ls`命令。

- w表示写权限。
  
  - 针对文件表示可以修改此文件。
  
  - 针对文件夹表示可以在文件夹内进行创建、删除、重命名等操作。

- x表示执行权限。
  
  - 针对文件表示可以将文件作为程序执行。
  
  - 针对文件夹表示可以切换目录。如`cd`命令。

### 修改权限控制

#### chmod

只有文件、文件夹的所属用户或root用户可以修改。

- 作用：修改文件、文件夹的权限信息。

- 命令：`chmod [option] 权限 file`。 
  
  - option：
    
    - `-R`：递归地更改文件和目录。

- 实例：
  
  - `chmod u=rwx,g=rx,o=x hello.txt`：将文件权限修改为`rwxr-x--x`。
    
    - u表示user所属用户权限，g表示group组权限，o表示other其它用户权限。
  
  - `chmod -R u=rwx,g=rx,o=x hello`：递归地更改hello文件夹下的文件和目录权限设置为`rwxr-x--x`。

权限也可以用一个3位数字来表示，从左向右第一位数字表示用户权限，第二位表示用户组权限 ，第三位表示其它用户权限。

数字表示的含义：r记为4，w记为2，x记为1，则可以有：

| 权限数字 | 二进制 | 描述     | 字母描述 |
|:----:|:---:|:------:|:----:|
| 0    | 000 | 无任何权限  | ---  |
| 1    | 001 | 仅有x权限  | --x  |
| 2    | 010 | 仅有w权限  | -w-  |
| 3    | 011 | 有w和x权限 | -wx  |
| 4    | 100 | 仅有r权限  | r--  |
| 5    | 101 | 有r和x权限 | r-x  |
| 6    | 110 | 有r和w权限 | rw-  |
| 7    | 111 | 有全部权限  | rwx  |

> chmod -R 777 file
> 
> chmod -R 751 file：user=7，group=5，other=1

#### chown

普通用户无法修改。只适用于root用户执行。

- 作用：修改文件、文件夹的所属用户和用户组。

- 命令：`chown [option] [用户][:][用户组] file`。
  
  - option：
    
    - `-R`：递归地更改文件和目录。
  
  - 用户：修改后的所属用户。
  
  - `:`：用于分隔用户和用户组。
  
  - 用户组：修改后的所属用户组。

- 示例：
  
  1. `chown root hello.txt`：将hello.txt的所属用户修改为root用户。
  
  2. `chown :root hello.txt`：将hello.txt的所属用户组修改为root组。
  
  3. `chown root:dev hello.txt`：将hello.txt的所属用户修改为root用户，用户组修改为dev组。
  
  4. `chown -R root hello`：递归将hello文件夹及子目录的所属用户修改为root。

## Linux使用操作

### 快捷键

| 快捷命令        | 描述                                |
| ----------- | --------------------------------- |
| ctrl + c    | 强制停止                              |
| ctrl + d    | 退出、登出或退出特定程序的命令行（如redis），但不能用于vim |
| history     | 查看历史输入过的命令，可配合管道符使用               |
| !命令前缀       | 按照命令前缀自动匹配上一个命令                   |
| ctrl + r    | 搜索历史命令                            |
| ctrl + a    | 跳到命令开头                            |
| ctrl + e    | 跳到命令结尾                            |
| ctrl + 键盘左键 | 向左跳一个单词                           |
| ctrl + 键盘右键 | 向右跳一个单词                           |
| ctrl + l    | 终端清屏（也可以通过命令clear得到相同效果）          |

### 软件安装

#### yum

yum：RPM包软件软件器，用于自动化安装配置Linux软件，并可以自动决绝依赖问题。

yum命令需要root权限。且需要联网。

- 命令：`yum [option] [install|remove|search] 软件名称`。
  
  - option：
    
    - `-y`：回答全部问题为是。
  
  - install：安装。
  
  - remove：卸载。
  
  - search：搜索。

```shell
[root@server7 ~]# yum search redis
已加载插件：fastestmirror
Repodata is over 2 weeks old. Install yum-cron? Or run: yum makecache fast
Determining fastest mirrors
 * base: mirrors.ustc.edu.cn
 * extras: mirrors.bfsu.edu.cn
 * updates: mirrors.ustc.edu.cn
====================================================== N/S matched: redis ======================================================
pcp-pmda-redis.x86_64 : Performance Co-Pilot (PCP) metrics for Redis

  名称和简介匹配 only，使用“search all”试试。

[root@server7 ~]# yum install redis
```

#### apt

centos使用yum管理器，Ubuntu使用apt管理器。

- 命令：`apt [-y] [install|remove|search] 软件名称`。

用法与yum一致。

### systemctl

- 作用：控制软件（服务）的启动、停机以及开机自启等。

- 命令：`systemctl start|stop|status|enable|disable 服务名`。

> start|stop|status|enable|disable
> 
> 启动|关闭|状态|开机自启|关闭开机自启

- Linux系统内置有较多的服务，如：
  
  - firewalld：防火墙服务。
  
  - NetworkManager：主网络服务。
  
  - network：副网络服务。

```shell
[root@server7 ~]# systemctl status firewalld
● firewalld.service - firewalld - dynamic firewall daemon
   Loaded: loaded (/usr/lib/systemd/system/firewalld.service; disabled; vendor preset: enabled)
   Active: inactive (dead)
     Docs: man:firewalld(1)

[root@server7 ~]# systemctl start firewalld

[root@server7 ~]# systemctl status firewalld
● firewalld.service - firewalld - dynamic firewall daemon
   Loaded: loaded (/usr/lib/systemd/system/firewalld.service; disabled; vendor preset: enabled)
   Active: active (running) since 一 2022-07-24 23:21:57 CST; 1s ago
     Docs: man:firewalld(1)
 Main PID: 21929 (firewalld)
    Tasks: 1
   Memory: 24.5M
   CGroup: /system.slice/firewalld.service
           ├─21929 /usr/bin/python2 -Es /usr/sbin/firewalld --nofork --nopid
           └─21995 /usr/bin/python2 -Es /usr/sbin/firewalld --nofork --nopid

7月 24 23:21:56 server7 systemd[1]: Starting firewalld - dynamic firewall daemon...
7月 24 23:21:57 server7 systemd[1]: Started firewalld - dynamic firewall daemon.
7月 24 23:21:57 server7 firewalld[21929]: WARNING: AllowZoneDrifting is enabled. This is considered an insecure config...t now.
Hint: Some lines were ellipsized, use -l to show in full.

[root@server7 ~]# systemctl stop firewalld
```

#### 注册服务

如果是第三方软件，有些软件需要手动注册为服务。

### 软链接

可以理解为Windows系统中的快捷方式。通过软连接能够操作实际文件或文件夹。

- 作用：可以将文件、文件夹链接到其它位置。

- 命令 ：`ln [option] 参数1 参数2`
  
  - option：
    
    - `-s`：创建软链接。
  
  - 参数1：被链接的文件或文件夹。
  
  - 参数2：目标文件或文件夹。

示例：

```shell
[root@server7 test]# ln -s Hello.java link.java 

[root@server7 test]# ll
-rw-r--r--. 1 root root   0 7月  24 16:08 Hello.java
lrwxrwxrwx. 1 root root  10 7月  25 07:06 link.java -> Hello.java

[root@server7 test]# cat Hello.java 

[root@server7 test]# vim link.java 

[root@server7 test]# cat Hello.java 
test
```

### 日期&时区

#### 日期

- 作用：查看系统时间。

- 命令：`date [option] [+格式化字符串]`。
  
  - option：
    
    - -d：按照给定的字符串显示日期，可用于日期计算。`-d`支持的时间标记有：year、month、day、hour、minute、second。
  
  - 格式化字符串：通过特定的字符串标记，来控制输出的日期格式。
  
  ```text
    %%    一个文字的 %
    %a    当前locale 的星期名缩写(例如： 日，代表星期日)
    %A    当前locale 的星期名全称 (如：星期日)
    %b    当前locale 的月名缩写 (如：一，代表一月)
    %B    当前locale 的月名全称 (如：一月)
    %c    当前locale 的日期和时间 (如：2005年3月3日 星期四 23:05:25)
    %C    世纪；比如 %Y，通常为省略当前年份的后两位数字(例如：20)
    %d    按月计的日期(例如：01)
    %D    按月计的日期；等于%m/%d/%y
    %e    按月计的日期，添加空格，等于%_d
    %F    完整日期格式，等价于 %Y-%m-%d
    %g    ISO-8601 格式年份的最后两位 (参见%G)
    %G    ISO-8601 格式年份 (参见%V)，一般只和 %V 结合使用
    %h    等于%b
    %H    小时(00-23)
    %I    小时(00-12)
    %j    按年计的日期(001-366)
    %k   hour, space padded ( 0..23); same as %_H
    %l   hour, space padded ( 1..12); same as %_I
    %m   month (01..12)
    %M   minute (00..59)
    %n    换行
    %N    纳秒(000000000-999999999)
    %p    当前locale 下的"上午"或者"下午"，未知时输出为空
    %P    与%p 类似，但是输出小写字母
    %r    当前locale 下的 12 小时时钟时间 (如：11:11:04 下午)
    %R    24 小时时间的时和分，等价于 %H:%M
    %s    自UTC 时间 1970-01-01 00:00:00 以来所经过的秒数
    %S    秒(00-60)
    %t    输出制表符 Tab
    %T    时间，等于%H:%M:%S
    %u    星期，1 代表星期一
    %U    一年中的第几周，以周日为每星期第一天(00-53)
    %V    ISO-8601 格式规范下的一年中第几周，以周一为每星期第一天(01-53)
    %w    一星期中的第几日(0-6)，0 代表周一
    %W    一年中的第几周，以周一为每星期第一天(00-53)
    %x    当前locale 下的日期描述 (如：12/31/99)
    %X    当前locale 下的时间描述 (如：23:13:48)
    %y    年份最后两位数位 (00-99)
    %Y    年份
    %z +hhmm        数字时区(例如，-0400)
    %:z +hh:mm        数字时区(例如，-04:00)
    %::z +hh:mm:ss    数字时区(例如，-04:00:00)
    %:::z            数字时区带有必要的精度 (例如，-04，+05:30)
    %Z            按字母表排序的时区缩写 (例如，EDT)
  ```

示例：

```shell
[root@server7 test]# date +%Y-%m-%d %H:%M:%S
date: 额外的操作数 "%H:%M:%S"
Try 'date --help' for more information.

[root@server7 test]# date "+%Y-%m-%d %H:%M:%S"
2022-07-25 07:17:26

[root@server7 test]# date +%Y-%m-%d
2022-07-25

[root@server7 test]# date -d "2022-05-25" +%Y-%m-%d
2022-05-25

[root@server7 test]# date -d "+1 day" +%Y-%m-%d
[root@server7 test]# date -d "-1 day" +%Y-%m-%d
[root@server7 test]# date -d "-1 month" +%Y-%m-%d
[root@server7 test]# date -d "+1 month" +%Y-%m-%d
```

如果日期格式中间带有空格时，需要使用双引号包围格式化字符串。

#### 修改时区

```shell
# 删除默认时区
[root@server7 test]# rm -f /etc/localtime

# 修改时区为东八区，软链接方式
[root@server7 test]# ln -s /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
```

#### ntp时间校准

安装`yum -y install ntp`。

开机自启：

```shell
systemctl start ntpd

systemctl enable ntpd
```

默认自动联网校准系统时间。

也可以手动校准，但需要root权限。

```shell
ntpdate -u ntp.aliyun.com
```

### ip地址&主机名

#### ip地址

可通过命令`ifconfig`查看本机ip地址，若无法使用`ifconfig`命令，则需要安装：`yum -y install net-tools`。

- 127.0.0.1：表示回环地址，这个ip地址用于指代本机。

- 0.0.0.0：特殊ip地址。
  
  - 可用于指代本机。
  
  - 可在端口绑定中用来确定绑定关系。
  
  - 在一些ip地址限制中，表示所有ip，如放行规则设置为0.0.0.0，则表示允许任意ip访问。

#### 主机名

可以理解为设备名称。

```shell
[root@server7 test]# hostname
server7
```

修改主机名，需root权限：

```shell
[root@server7 test]# hostnamectl set-hostname 主机名
```

#### 域名解析

![](https://github.com/Doing-code/guide/blob/main/image/linux_域名解析流程.png)

#### 虚拟机配置固定IP

网卡配置文件：`/etc/sysconfig/network-scripts/ifcfg-ens33`。

修改完配置后执行：`systemctl restart network`重启网卡。

### 网络传输

#### 下载和网络请求

- wget：可以在命令行内下载网络文件。

- 命令：`wget [-b] url`。
  
  - `-b`：后台下载，会将紫日子写入当前工作目录的wget-log文件。
  
  - url：下载链接。

- curl：可发送http请求，可用于下载文件、获取信息等。

- 命令：`curl [-O] url`。
  
  - `-O`：用于下载文件，若url是下载链接时，可以保存文件。
  
  - url：要发起的网络地址。

#### 端口

- 使用nmap命令可以查看ip地址端口的占用情况。

安装nmap：`yum -y install nmap`。

```shell
nmap 127.0.0.1
```

- 使用netstat命令，查看指定端口的占用情况。

安装netstat：`yum -y install net-tools`。

```shell
netstat -anp | grep 6379
```

### 进程管理

- 作用：查看Linux系统中的进程信息。

- 命令：`ps [-e -f]`。
  
  - -e：显示全部进程。
  
  - -f：显示全部信息。

```shell
[root@server7 test]# ps -ef
UID         PID   PPID  C STIME TTY          TIME CMD

# 从左到右依次是：
#   UID：进程所属的用户ID
#   PID：进程id
#   PPID：进程的父id（启动此进程的其它进程）
#   C：此进程的cpu占用率（百分比）
#   STIME：此进程的启动时间
#   TTY：启动此进程的终端序号。如显示?，则表示非终端启动
#   TIME：进程占用cpu的时间
#   CMD：进程对应的名称或启动路径或启动命令
```

#### 关闭进程

```shell
kill -9 进程id
```

`-9`表示强制关闭进程。

### 主机状态

#### 系统资源占用

- 作用：查看cpu、内存使用情况。类似Windows的任务管理器。

- 命令：`top`，默认5秒刷新一次。

```shell
[root@server7 test]# top

top - 08:31:02 up  9:02,  3 users,  load average: 0.00, 0.01, 0.05
Tasks: 142 total,   1 running, 141 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.0 us,  0.0 sy,  0.0 ni,100.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
KiB Mem :  8154720 total,  6628496 free,   827456 used,   698768 buff/cache
KiB Swap:  2097148 total,  2097148 free,        0 used.  7051384 avail Mem 

   PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND

# 说明：
#   load average: 0.00, 0.01, 0.05：1分钟，5分钟，15分钟负载
#   %Cpu(s)：CPU
#   KiB：内存
#
#   PID：进程id
#   USER：进程所属用户
#   PR：进程优先级，越小越高
#   NI：负值表示高优先级，正表示低优先级
#   VIRT：进程使用虚拟内存，单位kb
#   RES：进程使用物理内存，单位kb
#   SHR：进程使用共享内存，单位kb
#   S：进程状态（S休眠、R运行、Z僵死状态、N负数优先级、I空闲状态）
#   %CPU：进程占用CPU率
#   %MEM：进程占用内存率
#   TIME+：进程使用CPU时间总计 ，单位毫秒
#   COMMAND：进程的命令或进程名称或程序文件路径
```

![](https://github.com/Doing-code/guide/blob/main/image/linux_top命令内容详解.png)

一般CPU只用关心 us、sy 即可。

top命令支持的选项：

![](https://github.com/Doing-code/guide/blob/main/image/linux_top_支持的选项_部分.png)

top交互式选项：

![](https://github.com/Doing-code/guide/blob/main/image/linux_top交互式选项.png)

补充：H键可以切换进程或线程。

#### 磁盘资源占用

- 命令：`df [-h]`，-h表示使用合适的单位显示。

- 可以使用iostat查看CPU、磁盘相关的详细信息。

- 命令：`iostat [-x] [num1] [num2]`。
  
  - -x：显示更多信息。
  
  - num1：刷新间隔。
  
  - num2：刷新次数。

如果提示未找到命令，则安装`yum install -y sysstat`。

补充：tps（Transaction Per Second），该设备每秒的传输次数。一次传输即`一次IO请求`。多个逻辑请求可能会被合并为`一次IO请求`，那么`一次传输请求`的大小是未知的。

`iostat -x`详细信息字段描述：

![](https://github.com/Doing-code/guide/blob/main/image/linux_top_详细信息字段描述.png)

#### 网络资源占用

查看网络的相关统计。

- 命令：`sar -n DEV num1 num2`。
  
  - -n：查看网络，DEV表示查看网络接口。
  
  - num1：刷新间隔。为空则查看一次结束。
  
  - num2：刷新次数。为空则无限次。

| 字段       | 描述              |
| -------- | --------------- |
| IFACE    | 本地网卡接口名称        |
| rxpck/s  | 每秒接收的数据包        |
| txpck/s  | 每秒发送的数据包        |
| rxKB/S   | 每秒接收的数据包大小，单位KB |
| txKB/S   | 每秒发送的数据包大小，单位KB |
| rxcmp/s  | 每秒接收的压缩数据包      |
| txcmp/s  | 每秒发送的压缩包        |
| rxmcst/s | 每秒接收的多播数据包      |

### 环境变量

使用`env`命令可查看当前系统的环境变量。

```shell
[root@server7 test]# env
```

查看环境变量：

```shell
[root@server7 test]# echo $PATH
```

临时设置环境变量，命令：`export 变量名=变量值`。

永久生效环境变量：

- 针对当前用户生效，配置在当前用户的：`~/bashrc`文件中。

- 针对所有用户生效，配置在系统的：`/etc/profile`文件中。

通过命令`source /etc/profile`、`source ~/bashrc`生效。

临时修改PATH的值：`export PATH=$PATH:/home/myenv`。将可执行文件所在的目录追加到PATH后。使用`:`分隔。

永久生效则将`export PATH=$PATH:/home/myenv`添加到环境变量文件`/etc/profile`即可。

### 上传下载

命令行方式上传下载。

先命令安装:`yum -y install lrzsz`。

上传：`rz`。

下载：`sz 要下载的文件`。默认下载到桌面的fsdownload文件夹中。

> 补充：rz、sz命令需要终端软件支持。

### 压缩解压

常见的压缩格式：

- zip：Linux、Windows、MacOS

- 7zip：Windows

- rar：Windows

- tar：Linux、MacOS

- gzip：Linux、MacOS

#### tar

.tar：称为tarball，归档文件。封装文件不压缩。

.gz：`.tar.gz`，gzip格式。使用gzip算法压缩文件。

tar命令可以对tar、gzip压缩格式进行压缩和解压缩的操作。

- 命令：`tar [option] 参数1 ... 参数N`。

  - option：
  
    - `-z`：gzip格式。不使用`-z`就是tar格式。该选项如果使用，一般处于选项为第一个。
  
    - `-x`：解压模式。
      
    - `-v`：显示压缩、解压过程，用于查看进度。
      
    - `-f`：要创建的文件，或要加压的文件，该选项必须在所有选项中处于最后一个。
  
    - `-c`：创建压缩文件，用于压缩模式。
  
    - `-C`：解压到指定目录，用于解压模式。单独使用，和其它解压选项分开。

  - 参数：压缩包或者需要压缩的文件，一个或多个。

示例：

```shell
# 将 1.txt 2.txt 3.txt 压缩到test.tar
[root@server7 test]# tar -cvf test.tar 1.txt 2.txt 3.txt

# 将 1.txt 2.txt 3.txt 压缩到test.tar.gz
[root@server7 test]# tar -zcvf test.tar.gz 1.txt 2.txt 3.txt

[root@server7 test]# tar -xvf test.tar -C /home/test

[root@server7 test]# tar -zxvf test.tar.gz -C /home/test
```

#### zip

压缩文件为zip格式的压缩包。

- 命令：`zip [-r] file1 ... fileN`

  - -r：递归压缩，适用于被压缩的文件中包含文件夹时。
  
示例：

```shell
[root@server7 test]# zip test.zip a.txt b.txt cc.txt

[root@server7 test]# zip -r test.zip test dev cc.txt
```

#### unzip

解压zip压缩包。

- 命令：`unzip [-d] file`。

  - -d：解压到指定目录。
  
  - file：zip压缩包文件。
  
示例：

```shell
[root@server7 test]# unzip test.zip

[root@server7 test]# unzip -d /home/test test.zip
```

## 附录

### 文件操作

#### rmdir

#### less

#### head

### 磁盘管理

#### 挂载&卸载

#### 磁盘分区