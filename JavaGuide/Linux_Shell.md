# Linux_Shell

> Shell 是一个命令行解释器，它接收应用程序/用户的命令，然后调用操作系统内核。此外，Shell也是一个编程语言，也会有诸如流程控制、函数等概念。

### Shell脚本入门

1. 脚本格式

    ```shell script
    #!/bin/bash
    ...
    ...
    ```

    脚本以`#!/bin/bash`开头，指定解析器。

2. 编写`Hello World`，`vim helloworld.sh`

    ```shell script
    #!/bin/bash
    echo "helloworld"
    ```

3. 执行脚本

    - 1、采用 `bash` 或 `sh` 后跟脚本的相对路径或绝对路径（不用赋予脚本+x 权限）
    
    ```shell script
    # 相对路径sh、bash
    [root@localhost shells]$ sh ./helloworld.sh
    helloworld
    
    [root@localhost shells]$ bash ./helloworld.sh
    helloworld
    
    # 绝对路径 sh、bash
    [root@localhost shells]$ sh /mydata/helloworld.sh
    helloworld
    
    [root@localhost shells]$ bash /mydata/helloworld.sh
    helloworld
    ```
   
   这种执行方式，本质是由`bash`解析器执行脚本，所以脚本本身不需要执行权限。
    
    - 2、采用脚本的绝对路径或相对路径执行脚本（必须具有可执行权限+x）
    
    ```shell script
    [root@localhost shells]$ chmod +x helloworld.sh
    
    # 相对路径
    [root@localhost shells]$ ./helloworld.sh
    helloworld
    
    # 绝对路径
    [root@localhost shells]$ /mydata/helloworld.sh
    helloworld
    ```
   
   这种执行方式，本质是脚本需要自己执行（“亲历亲为”），因此需要执行器权限。
    
    - 3、在脚本的路径前加上 `.` 或者 source
    
    ```shell script
    [root@localhost shells]$ . helloworld.sh
    helloworld
    ```
   
- 前两种方式都是在当前 Shell（Shell 进程）中 fork 一个子 Shell 来执行脚本内容，当脚本执行结束，子 Shell 关闭，回到父 Shell 中。

- 而第三种方式，可以让脚本直接在当前 Shell 执行，无需 fork 子 Shell。典型例子是修改完`/etc/profile`文件后，需要`source`刷新一下环境配置文件。

- 它们的区别在于，环境变量的继承关系。如果在子 Shell 中设置的变量，父 Shell 是不可见的。

### 变量

#### 系统预定义变量

常见系统变量有`$HOME`、`$PWD`、`$SHELL`、`$USER`等。

查看系统变量的值

```shell script
[root@localhost shells]$ echo $HOME
```

列出当前 Shell 中所有变量

```shell script
[root@localhost shells]$ set
```

#### 用户自定义变量

语法：`变量名=变量值`，如`age=18`，`=`前后不能有空格。

- 定义变量时，变量名不需要加`$`，只有在使用时才会在变量名前加`$`。

- 变量名和等号之间不能有空格。

- 变量命名规范

    - 命名只能使用英文字母，数字和下划线，首个字符不能以数字开头。
    
    - 中间不能有空格，但可以使用下划线`_`连接。
    
    - 不能使用标点符号。
    
    - 不能使用bash里的关键字（可用`help`命令查看保留关键字）。

- 变量名外面的花括号是可选的，加不加都行，加花括号是为了帮助解释器识别变量的边界（推荐给所有变量加上花括号）。

- 已定义的变量，可以被重新定义。

- 变量的值如果有空格，需要使用引号将其括起来。

- 在 bash 中，变量默认类型都是字符串类型，无法直接进行数值运算。

运行shell时，会同时存在三种变量：

- 局部变量： 局部变量在脚本或命令中定义，仅在当前shell实例中有效，其他shell启动的程序不能访问局部变量。

- 环境变量： 所有的程序，包括shell启动的程序，都能访问环境变量，有些程序需要环境变量来保证其正常运行。必要的时候shell脚本也可以定义环境变量。

- shell变量： shell变量是由shell程序设置的特殊变量。shell变量中有一部分是环境变量，有一部分是局部变量，这些变量保证了shell的正常运行。

#### 全局变量&只读变量&撤销变量

全局变量和系统变量通常划等号。

```shell script
[root@localhost shells]$ B=9

# 将变量提升为全局变量，可以被其它 shell 程序访问
[root@localhost shells]$ export B

# 只读变量（静态变量）
[root@localhost shells]$ readonly name='Tommy'
[root@localhost shells]$ name='Jack'
-bash: name: readonly variable

# 定义变量
[root@localhost shells]$ age=18
[root@localhost shells]$ echo $age
18

# 撤销变量
[root@localhost shells]$ unset age
[root@localhost shells]$ echo $age
```

####特殊变量

- trap

- dirname

- basename

- envsubst

- random/urandom

- mknod/mkfifo

- exec

- pushd/popd

### 注释

```shell script
# 单行注释

<<EOF
多行注释
...
EOF

# EOF 也可以使用其他符号
<<!
多行注释
...
!
```

### 字符串

字符串可以用单引号，也可以用双引号，也可以不用引号。

- 单引号

    - 单引号里的任何字符都会原样输出，单引号字符串中的变量是无效的。
    
    - 单引号字串中不能出现单独一个的单引号（对单引号使用转义符后也不行），但可成对出现，作为字符串拼接使用。
    
- 双引号

    - 双引号里可以有变量。
    
    - 双引号里可以出现转义字符。
    
#### 拼接字符串

```shell script
your_name="runoob"

# 使用双引号拼接,输出结果为：hello, runoob ! hello, runoob !
greeting="hello, "$your_name" !"
greeting_1="hello, ${your_name} !"
echo $greeting  $greeting_1

# 使用单引号拼接,输出结果为：hello, runoob ! hello, ${your_name} !
greeting_2='hello, '$your_name' !'
greeting_3='hello, ${your_name} !'
echo $greeting_2  $greeting_3
```

#### 获取字符串长度

```shell script
string="abcd"
echo ${#string}   # 输出 4

# 变量为数组时，${#array} 等价于 ${#array[0]}:
array=(1 2 3 4)
echo ${#array[0]}   # 输出 1

# 计算字符长度也可使用 length。
string="hello,everyone my name is mingming"  # string字符串里边有空格,所以需要添加双引号
expr length "$string"   # 输出 34

# 使用 expr 命令时，表达式中的运算符左右必须包含空格，如果不包含空格，将会输出表达式本身
expr 5+6     # 直接输出 5+6
expr 5 + 6   # 输出 11

# 对于某些运算符，还需要我们使用符号"\"进行转义，否则就会提示语法错误
expr 5 * 6       # 输出错误
expr 5 \* 6      # 输出30
```

#### 提取子字符串

```shell script
# 以下实例从字符串第 2 个字符开始截取 4 个字符
string="runoob is a great site"
echo ${string:1:4}   # 输出 unoo
```

#### 查找子字符串

```shell script
# 查找字符 i 或 o 的位置（哪个字母先出现就计算哪个）
string="runoob is a great site"
echo `expr index "$string" io`  # 输出 4
```

#### 字符串截取

```shell script
url=https://www.bilibili.com/123.html

# url 是变量名，# 是运算符，*// 表示从左边开始删除第一个 // 符号及左边的所有字符，即删除 https://
echo ${url#*//} # 结果是 ：www.bilibili.com/123.html

# ##*/ 表示从左边开始删除最后一个 / 符号及左边的所有字符，即删除 https://www.bilibili.com/
echo ${url##*/} # 结果是 123.htm

# %/* 表示从右边开始，删除第一个 / 符号及右边的字符
echo ${url%/*} # 结果是：https://www.bilibili.com

# %%/* 表示从右边开始，删除最后一个 / 符号及右边的字符
echo ${var%%/*} # 结果是：https:

# 0 表示从左边第一个字符开始，5 表示字符的总个数
echo ${var:0:5} # 结果是：https:

# 从左边第几个字符开始（从指定第几个字符 +1 开始），一直到结束
echo ${var:7} # 结果是 ：www.bilibili.com/123.html

# 0-7 表示从右边算起第七个字符结束，3 表示字符个数
echo ${var:0-7:3} # 结果是：123

# 表示从右边第七个字符开始，一直到结束。（字符截取顺序从左到右）
echo ${var:0-7} # 结果是：123.htm
```

- `#`、`##`表示从左边开始删除，一个 `#` 表示从左边删除到第一个指定的字符；两个 `#` 表示从左边删除到最后一个指定的字符。

- `%`、`%%` 表示从右边开始删除。一个 `%` 表示从右边删除到第一个指定的字符；两个 `%` 表示从右边删除到最后一个指定的字符。

- 删除包括了指定的字符本身。

#### 字符串替换

```shell script
# 使用 string/pattern/string（source/pattern/target） 进行首个 pattern 的替换
string="text, dummy, text, dummy"
echo ${string/text/TEXT}   # TEXT, dummy, text, dummy

# 使用 string//pattern/string 进行全部 pattern 的替换
string="text, dummy, text, dummy"
echo ${string//text/TEXT}  # TEXT, dummy, TEXT, dummy
```

#### 标准输入

> 获取键盘输入信息

语法：`read [-options] [variable...]`

```shell script
# -p 参数由于设置提示信息
read -p "input a val:" a    #获取键盘输入的 a 变量数字
read -p "input b val:" b    #获取键盘输入的 b 变量数字
r=$[a+b]                    #计算a+b的结果 赋值给r  不能有空格
echo "result = ${r}"        #输出显示结果 r
```

### 参数传递

向脚本传递参数，脚本内获取参数的格式为：$n。n 代表一个数字，其中 $0 为执行的文件名（包含文件路径），1 为执行脚本的第一个参数，2 为执行脚本的第二个参数，以此类推......

```shell script
[root@localhost shells]$ vim test.sh
echo "Shell 传递参数实例!";
echo "执行的文件名: $0";
echo "第一个参数为: $1";
echo "第二个参数为: $2";
echo "第三个参数为: $3";

[root@localhost shells]$ chmod +x test.sh 
[root@localhost shells]$ ./test.sh 1 2 3
Shell 传递参数实例!
执行的文件名: ./test.sh
第一个参数为: 1
第二个参数为: 2
第三个参数为: 3
```

| 参数表现形式 | 说明 |
|----|----|
| `$#` | 传递到脚本的参数个数 |
| `$*` | 以一个单字符串显示所有向脚本传递的参数，如果`$*`存在于双引号内部，则其表现形式为`"$1 $2 $3 ... $n"` |
| `$$` | 脚本运行的当前进程ID号 |
| `$!` | 后台运行的最后一个进程的ID号 |
| `$@` | 与`$*`相同，但如果`$@`存在于双引号内部，则其表现形式为`"$1" "$2" ... "$n"`，数组形式 |
| `$-` | 显示Shell使用的当前选项，与set命令功能相同 |
| `$?` | 显示最后命令的退出状态。0表示没有错误，其他任何值表明有错误 |

演示

```shell script
echo "Shell 传递参数实例!";
echo "第一个参数为：$1";
echo "参数个数为：$#";
echo "传递的参数作为一个字符串显示：$*";

$ chmod +x test.sh 
$ ./test.sh 1 2 3
Shell 传递参数实例!
第一个参数为：1
参数个数为：3
传递的参数作为一个字符串显示：1 2 3
```

### 数组

bash 仅支持一维数组，且没有限定数组的容量。

数组元素的下标由 0 开始编号。获取数组中的元素要利用下标，下标可以是整数或算术表达式，其值应大于或等于 0。

在 Shell 中，用括号来表示数组，数组元素用"空格"符号分割开。定义数组的一般形式为： `数组名=(值1 值2 ... 值n)`

```shell script
array_name=(value0 value1 value2 value3)

# 或者
array_name=(
value0
value1
value2
value3
)

# 还可以单独定义数组的各个分量。可以不使用连续的下标，而且下标的范围没有限制。
array_name[0]=value0
array_name[1]=value1
array_name[n]=valuen
```

#### 获取数组元素

```shell script
# ${数组名[索引]}
value=${array_name[n]}

# 使用 @ 符号可以获取数组中的所有元素
echo ${array_name[@]}
```

#### 获取数组长度

```shell script
# 获取数组长度的方法与获取字符串长度的方法相同
# 取得数组元素的个数
length=${#array_name[@]}
# 或者
length=${#array_name[*]}

# 取得数组单个元素的长度
length=${#array_name[n]}
```

#### 关联数组

> 其表现形式类似 map

语法：`declare -A array_name`。`-A` 选项就是用于声明一个关联数组。关联数组的键是唯一的。

```shell script
# 创建一个关联数组 site，并创建不同的键值
declare -A site=(["A"]="1" ["B"]="2" ["C"]="3")

# 也可以先声明一个关联数组，然后再设置键和值
declare -A site
site["A"]="1"
site["B"]="2"
site["C"]="3"

# 访问关联数组元素可以使用指定的键
echo ${site["B"]}  # 执行脚本，输出结果：2

# 在数组前加一个感叹号!可以获取数组的所有键
echo "数组的键为: ${!site[*]}" # A B C
echo "数组的键为: ${!site[@]}" # A B C
```

#### 字符串转数组

```shell script
words="aaa bbb ccc"

# 字符串转数组，空格是分隔符
array=(${words// / })  
# 打印数组最后一个成员
echo ${array[${#array[*]}-1]}  # ccc
# 打印数组长度
echo ${#array[*]}  # 3

# 字符串不转换为数组，在循环实现以空格为分隔符打印每个成员
for word in ${words}; do
    echo ${word}       # aaa \n bbb \n ccc
done
```

### 运算符

原生bash不支持简单的数学运算，所以需要使用第三方的命令来实现，例如 awk 和 expr。

`expr`是一个表达式计算工具，`awk`是文本处理工具。

```shell script
val=`expr 2 + 2`
echo "两数之和为 : $val"   # 两数之和为 : 4
```

表达式和运算符之间要有空格，如`2 + 2`。

| 运算符 | 描述 | eg |
|----|----|----|
| 算数运算符 | | 假定变量 a 为 10，变量 b 为 20 |
| + | | `expr $a + $b` 结果为 30 |
| - | | `expr $a - $b` 结果为 -10 |
| * | | `expr $a * $b` 结果为 200 |
| / | | `expr $a / $b` 结果为 0.5 |
| % | | `expr $a % $b` 结果为 10 |
| = | | a=$b 把变量 b 的值赋值给 a |
| == | 用于比较两个数字，相同则返回 true | `expr $a == $b` 结果为 false |
| != | 用于比较两个数字，不相同则返回 true | `expr $a != $b` 结果为 true |
| 关系运算符 | |  |
| -eq | == | `[ $a -eq $b ]` 返回 false |
| -ne | != | `[ $a -ne $b ]` 返回 true |
| -gt | > | `[ $a -gr $b ]` 返回 false |
| -lt | < | `[ $a -lt $b ]` 返回 true |
| -ge | >= | `[ $a -ge $b ]` 返回 false |
| -le | <= | `[ $a -le $b ]` 返回 true |
| 布尔运算符 | |  |
| ! | 非运算 | `[ !false ]` 返回true |
| -o | 或运算 | `[ $a -lt 20 -o $b -gt 100 ]` 返回 true |
| -a | 与运算 | `[ $a -lt 20 -a $b -gt 100 ]` 返回 false |
| 逻辑运算符 | |  |
| && | 逻辑 AND | `[[ $a -lt 100 && $b -gt 100 ]]` 返回 false |
|  | 逻辑 OR |  |
| 字符串运算符 | | 假定变量 a 为 "abc"，变量 b 为 "efg" |
| = | 比较两个字符串是否相等，相等返回 true | `[ $a = $b ]` 返回 false |
| != | 比较两个字符串是否不相等，不相等返回 true | `[ $a != $b ]` 返回 true |
| -z | 字符串长度是否为0，为0返回 true | `[ -z $a ]` 返回 false |
| -n | 字符串长度是否不为 0，不为 0 返回 true | `[ -n "$a" ]` 返回 true |
| $ | 字符串是否不为空，不为空返回 true | `[ $a ]` 返回 true |
| 文件运算符 | |  |
| `-b file` | 检测文件是否是块设备文件，如果是，则返回 true | |
| `-c file` | 检测文件是否是字符设备文件，如果是，则返回 true | |
| `-d file` | 检测文件是否是目录，如果是，则返回 true | |
| `-f file` | 检测文件是否是普通文件（既不是目录，也不是设备文件），如果是，则返回 true | |
| `-g file` | 检测文件是否设置了 SGID 位，如果是，则返回 true | |
| `-k file` | 检测文件是否设置了粘着位(Sticky Bit)，如果是，则返回 true | |
| `-p file` | 检测文件是否是有名管道，如果是，则返回 true | |
| `-u file` | 检测文件是否设置了 SUID 位，如果是，则返回 true | |
| `-r file` | 检测文件是否可读，如果是，则返回 true | |
| `-w file` | 检测文件是否可写，如果是，则返回 true | |
| `-x file` | 检测文件是否可执行，如果是，则返回 true | |
| `-s file` | 检测文件是否为空（文件大小是否大于0），不为空返回 true | |
| `-e file` | 检测文件（包括目录）是否存在，如果是，则返回 true | |

条件表达式要放在方括号之间，并且要有空格。如`[ $a == $b ]`。

> 逻辑运算符：||（逻辑OR），`[[ $a -lt 100 || $b -gt 100 ]]` 返回 true

### 条件判断

```shell script
# if
if condition
then
    command1 
    command2
    ...
    commandN 
fi

# if else
if condition
then
    command1 
    command2
    ...
    commandN
else
    command
fi

# if else-if else
if condition1
then
    command1
elif condition2 
then 
    command2
else
    commandN
fi
```

if else 的 `[...]` 判断语句中大于使用 -gt，小于使用 -lt。如果使用`((...))`将其括起来，大于和小于可以直接使用 > 和 <。

```shell script
a=10
b=20
if [ $a == $b ]
then
   echo "a 等于 b"
elif (($a > $b))
then
   echo "a 大于 b"
elif [ $a -lt $b ]
then
   echo "a 小于 b"
else
   echo "没有符合的条件"
fi
```

### 流程控制

#### for循环

```shell script
for 变量名 in item1 item2 ... itemN
do
    command1
    command2
    ...
    commandN
done

# 或者

for 变量名 in 数组名
do
    command1
    command2
    ...
    commandN
done
```

示例

```shell script
for loop in 1 2 3 4 5
do
    echo "The value is: $loop"
done

# 输出结果：
The value is: 1
The value is: 2
The value is: 3
The value is: 4
The value is: 5


for str in This is a string
do
    echo $str
done

# 输出结果：
This
is
a
string
```

#### while循环

```shell script
while condition
do
    command
done
```

示例

```shell script
a=1
while(( $a<=5 ))
do
    echo $a
    let "a++"
done

# 输出结果
1
2
3
4
5
```

#### until循环

until循环的循环条件与while循环恰好相反，until循环的循环条件为 true 时停止循环，而while循环则是为 false 时停止循环。

一般 while 循环优于 until 循环，但在某些时候—也只是极少数情况下，until 循环更加有用。

```shell script
# condition 一般为条件表达式，如果返回值为 false，则继续执行循环体内的语句，否则跳出循环。
until condition  
do
    command
done
```

示例

```shell script
a=0

# a < 10
until [ ! $a -lt 10 ]
do
   echo $a
   a=`expr $a + 1`
done

# 输出结果为：
0
1
2
3
4
5
6
7
8
9
```

#### case分支

类似`switch case`分支选择结构。

```shell script
case 变量 in
模式1)
    command1
    command2
    ...
    commandN
    ;;
模式2)
    command1
    command2
    ...
    commandN
    ;;
esac
```

示例

```shell script
echo '输入 1 到 4 之间的数字:'
echo '你输入的数字为:'
read aNum
case $aNum in
    1)  echo '你选择了 1'
    ;;
    2)  echo '你选择了 2'
    ;;
    3)  echo '你选择了 3'
    ;;
    4)  echo '你选择了 4'
    ;;
    *)  echo '你没有输入 1 到 4 之间的数字'
    ;;
esac
```

如果没有匹配到分支，可以用`*`来执行默认分支逻辑，类似`default`。

break 命令允许跳出所有循环（终止执行后面的所有循环）。

continue 命令与 break 命令类似，它不会跳出所有循环，仅仅跳出当前循环。进入下一个循环。

```shell script
#!/bin/bash
while :
do
    echo -n "输入 1 到 5 之间的数字:"
    read aNum
    case $aNum in
        1|2|3) echo "你输入的数字为 $aNum!"
            continue
        ;;

        4|5) echo "你输入的数字为 $aNum!"
        ;;
        *) echo "你输入的数字不是 4 到 5 之间的!"
            break
        ;;
    esac
done
```

### 函数

定义格式

```shell script
[ function ] funname [()]
{
    action;
    [return int;]
}

```

可以带function fun() 定义，也可以直接fun() 定义。默认返回值将以最后一条命令运行结果，作为返回值。 return后跟数值n(0-255）

```shell script
funWithParam(){
    echo "第一个参数为 $1 !"
    echo "第二个参数为 $2 !"
    echo "第十个参数为 $10 !"
    echo "第十个参数为 ${10} !"
    echo "第十一个参数为 ${11} !"
    echo "参数总数有 $# 个!"
    echo "作为一个字符串输出所有参数 $* !"
}
funWithParam 1 2 3 4 5 6 7 8 9 34 73

# 输出结果：
第一个参数为 1 !
第二个参数为 2 !
第十个参数为 10 !
第十个参数为 34 !
第十一个参数为 73 !
参数总数有 11 个!
作为一个字符串输出所有参数 1 2 3 4 5 6 7 8 9 34 73 !
```

`$10` 不能获取第十个参数，获取第十个参数需要`${10}`。当`n>=10`时，需要使用`${n}`来获取参数。

### 正则

#### 匹配开头

```shell script
[root@localhost shells]$ cat /etc/passwd | grep ^a 
# 会匹配出所有以 a 开头的行
```

#### 匹配结尾

```shell script
[root@localhost shells]$ cat /etc/passwd | grep t$
# 会匹配出所有以 t 结尾的行
```

#### 匹配任意字符

```shell script
[root@localhost shells]$ cat /etc/passwd | grep r..t
# 会匹配包含 rabt,rbbt,rxdt,root 等的所有行
```

#### 匹配一个字符若干次

> 配合左边相邻的字符连用，表示匹配左边相邻的字符0次或多次。

```shell script
[root@localhost shells]$ cat /etc/passwd | grep ro*t
# 会匹配 rt, rot, root, rooot, roooot 等所有行
```

#### 字符区间

- `[ ]` 表示匹配某个范围内的一个字符
- `[6,8]` ------ 匹配 6 或者 8
- `[0-9]` ------ 匹配一个 0-9 的数字
- `[0-9]*` ------ 匹配任意长度的数字字符串
- `[a-z]` ------匹配一个 a-z 之间的字符
- `[a-z]*` ------ 匹配任意长度的字母字符串
- `[a-c, e-f]` ----- 匹配 a-c 或者 e-f 之间的任意字符

### 文本处理工具

#### cut

> cut 命令从文件的每一行剪切字节、字符和字段并将这些字节、字符和字段写至标准输出。

语法：`cut [参数] [file]`。

如果不指定 File 参数，cut 命令将读取标准输入。必须指定 -b、-c 或 -f。

|参数 |说明|
|----|----|
| -b | 以字节为单位进行分割。这些字节位置将忽略多字节字符边界，除非也指定了 -n 标志。|
| -c | 以字符为单位进行分割。|
| -d | 自定义分隔符，默认为制表符。|
| -f | -d一起使用，指定显示哪个区域。|
| -n | 取消分割多字节字符。仅和 -b 标志一起使用。如果字符的最后一个字节落在由 -b 标志的 List 参数指示的范围之内，该字符将被写出；否则，该字符将被排除|

示例

```shell script
# 当执行who命令时，会输出类似如下的内容：
$ who
rocrocket :0           2009-01-08 11:07
rocrocket pts/0        2009-01-08 11:23 (:0.0)
rocrocket pts/1        2009-01-08 14:15 (:0.0)

# 如果我们想提取每一行的第3个字节，如下：
$ who|cut -b 3
c
c
```

#### sed

sed 主要用来批量新增、替换、删除多个文件中指定的字符。

#### awk

awk 是一种处理文本文件的语言，是一个强大的文本分析工具，把文件逐行的读入，以空格为默认分隔符将每行切片，切开的部分再进行分析处理。