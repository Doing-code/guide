# Python3小时快速入门

### 控制台输出

```python
print("Hello World!!!")

# \n 换行符
print("Hello\nWorld")

print('''Hello
"World"
'!!!'
''')

print("""Hello
"World"
'!!!'
""")
```

### 变量的定义

变量命名规则：

- 不能有除了下划线之外的符号。

- 不能有空格。

- 不能数字开头。

变量名应该尽量取得易于理解和记忆。

而Python的英文变量命名约定俗成是使用下划线命名法。字母全部小写，不同单词用下划线分隔开。（例如user_age、user_gender），也可以使用驼峰命名规则。

在Python中变量名是大小写敏感的，即 user_age 和 user_Age 会被看作是两个不同的变量。

```python
old_tel = "19900000000"
print("old tel is " + old_tel)

new_tel = "13399999999"
print("new tel is " + new_tel)
```

### 注释

```python
# 单行注释 

'''
多行注释
'''
```

### 数据类型

字符串（str）、整型（int）、浮点型（float）、布尔类型（bool）、空值类型（NoneType）、列表、字典等。属于不可变数据类型。

```python
print("Hello")
print('World')
print("Hello"[1])   # 通过索引获取单个字符

print(256)
print(256.01)

print(True)
print(False)

print(None)
```

使用`type()`函数能够返回该对象的类型

```python
print(type("Hello"))    # <class 'str'>

print(type(256))        # <class 'int'>
print(type(256.01))     # <class 'float'>

print(type(True))       # <class 'bool'>

print(type(None))       # <class 'NoneType'>
```

### 标准输入input

```python
length = int(input("请输入长方形的长度: "))

width = int(input("请输入长方形的宽度: "))

print("长方形的面积为: ", length * width)
```

### 条件语句

语法：

```python
if [条件]:
    [执行语句]
    [执行语句]
else:
    [执行语句]
    [执行语句]
```

示例：

```python
score = int(input("请输入考试成绩: "))

if score >= 60:
    print("及格")
else:
    print("不及格")
```

嵌套if：

```python
score = int(input("请输入考试成绩: "))

if score >= 60:
    if score > 80:
        print("优秀")
    else:
        print("及格")
else:
    print("不及格")
```

### 逻辑运算

and（`&&`）、or（`||`）、not（`!`）

优先级：not > and > or

### 列表

列表属于可变数据类型。

```python
shopping_car = ["Mac", "Windows"]

shopping_car.append("Linux")
shopping_car.append("123")

print(shopping_car)

shopping_car.remove("Mac")

print(shopping_car)
```

### 字典

```python
contacts = {"key":"value", "...":"..."}
contacts.keys() # 获取所有键
contacts.values() # 获取所有值
contacts.items() # 获取所有兼职对
```

key的类型必须是不可变的，列表无法作为key，但可以使用元组作为key。也可以使用基本数据类型作为key。

元组和列表的区别是括号，元组是`()`，而列表是`[]`。且元组的元素无法使用`append()`、`remove()`等方法。

示例：

```python
name_age = {}

name_age["小明"] = 18
name_age["小芳"] = 20
name_age["小方"] = 22

print(name_age) # {'小明': 18, '小芳': 20, '小方': 22}

# 判断key 是否在字典中存在
print("小明" in name_age) # True

del name_age["小明"]

print(name_age) # {'小芳': 20, '小方': 22}

print("元素个数为: ", len(name_age))

name_age[("小明", 15)] = 15
name_age[("小明", 16)] = 16

print(name_age) # {'小芳': 20, '小方': 22, ('小明', 15): 15, ('小明', 16): 16}
print(name_age["小芳"])   # 20
print(name_age[("小明", 16)]) # 16
```

### 元组

元组使用`()`，且使用逗号隔开各个数据，数据可以是不同的数据类型。

```python
my_tuple1 = ("a", 1, True)
my_tuple2 = ("b", 2, False)

my_tuple2 = ("b",)
```

若元组只有一个数据时，这个数据后面需要添加逗号，负责不是元组类型。

### 集合

语法：

```python
my_set = {1, 2, 3, 4, 5, 6}

# 添加元素
my_set.add(7)

# 删除元素
my_set.remove(7)

# 随机取
my_set.pop()

...
```

### 循环

#### for循环

语法：

```python
for 变量名 in 可迭代对象:
    # 循环体
```

遍历字典：

```python
score_list = {"小明": 61, "小芳": 89, "小方": 91}

for name, score in score_list.items():
    print(name, ",成绩:", score)
```

示例：

```python
total = 0
for i in range(1, 101):
    total = total + i
print(total)
```

`range()`函数表示一个正数序列，第一个参数为起始值，第二个参数为结束值，包含起始值，但不包含结束值。还可以有第三个参数，表示步长，每次跨几个数字，默认步长为1。

#### while循环

语法：

```python
while 条件:
    循环体
```

跳出循环则可以使用`continue`或`break`关键字。也能够使用`return`。

### 格式化字符串

- 方式一

```python
name = "Jack"
message = '我是 {0}, 今年 {1} 岁'.format(name, 18)
print(message)

name = "Tom"
age = 21
# format(name=name, age=age -> 等号前面的是关键字，等号后面的是参数值
message = '我是 {name}, 今年 {age} 岁'.format(name=name, age=age)
print(message)
```

- 方式二

```python
name = "Tom"
age = 21

message = f'我是 {name}, 今年 {age} 岁'
print(message)
```

如果是浮点数格式化，可使用`{score:.2f}`保留两位小数。

### 函数

```python
def rectangular_area(length, width):
    print("长", length)
    print("宽", width)
    result = length * width
    print("面积", length * width)
    return result

s = rectangular_area(2, 3)

print(s)
```

### 引入模块

示例：引入官方库

```python
# 方式一：
import statistics

print(statistics.median([19, -5, 36]))
print(statistics.mean([19, -5, 36]))

# 方式二：
from statistics import median, mean

print(median([19, -5, 36]))
print(mean([19, -5, 36]))

# 方式三：
from statistics import *

print(median([19, -5, 36]))
print(mean([19, -5, 36]))
```

示例：引入第三方库

引入语法和引入官方库一样，但是在引入之前需要将第三方库下载本地。

```shell script
pip install 模块名
```

### 面向对象

什么是面向对象？面向过程就像编年体（以时间为中心），而面向对象则是纪传体（以人物传记为中心）。

面向对象三大特性：封装、继承、多态。

#### 类的定义

```python
# 定义类
class Person:

    # self：表示自己，可理解为 this，不需要开发人员传入
    def __init__(self, name, age, gender):
        self.name = name
        self.age = age
        self.gender = gender

    def eat(self, food):
        print(self.name, f"正在吃{food}")

# 创建对象
person1 = Person("Jack", 18,  "男")

print(person1.name, person1.age, person1.gender)

# 方法调用
person1.eat("面")
```

#### 继承

```python
class Person:

    def __init__(self, name, age, gender):
        self.name = name
        self.age = age
        self.gender = gender

    def eat(self, food):
        pass

    def run(self, food):
        pass

# 继承定义语法 class 子类(父类)
class Man(Person):
    def __init__(self, name, age, gender):
        super().__init__(name, age, gender)

    def eat(self, food):
        print(f"吃{food}")

class Woman(Person):
    def __init__(self, name, age, gender):
        super().__init__(name, age, gender)

    def eat(self, food):
        print(f"吃{food}")

man = Man("Tom", 18, "男")
man.eat("饭")
```

父类定义共性属性和行为，子类可以定义特有的属性和行为。

### 文件读写

#### 文件读

- read()：返回全部文件内容的字符串。

- readline()：返回一行文件内容的字符串。

- readlines()：返回全部文件内容组成的列表。

示例：

```python
with open("./data.txt", "r", encoding="utf-8") as f:
    content = f.read()
    print(content)
```

`r`表示读取模式，只读。

#### 文件写

如果文件名不存在，则会自动创建文件。`w`模式会覆盖文件已有内容。`a`模式表示附加内容，不会覆盖已有内容，只会在已有内容后追加。

```python
# with open("./data.txt", "a", encoding="utf-8") as f: 文件内容追加至末尾

# 可读可写
with open("./data.txt", "r+", encoding="utf-8") as f:
    content = f.read()
    f.write("Hello World!!!\n")
    print(content)
```

### 异常处理

```python
try:
    result = 0 / 0
    ...
except 异常类型1:
    # 处理异常
except ZeroDivisionError:
    # 处理异常
except 异常类型3:
    # 处理异常
else:
    # 没有发送错误时会运行
finally:
    ...
```

### 单元测试-unittest








