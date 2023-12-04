# C++ 快速入门

## C++ 基础

### 变量

```c++
// 数据类型 变量名 = 初始值;
int age = 18;
```

### 常量

常量是不可以修改的。

```c++
// 常量定义方式一
// #define 常量名 常量值
#define day 7

// 常量定义方式二
// const 数据类型 常量名 = 常量值
const int month = 12;
```

### 关键字

|            |              |                  |             |          |
|------------|--------------|------------------|-------------|----------|
| asm        | do           | if               | return      | typedef  |
| auto       | double       | inline           | short       | typeid   |
| bool       | dynamic_cast | int              | signed      | typename |
| break      | else         | long             | sizeof      | union    |
| case       | enum         | mutable          | static      | unsigned |
| catch      | explicit     | namespace        | static_cast | using    |
| char       | export       | new              | struct      | virtual  |
| class      | extern       | operator         | switch      | void     |
| const      | false        | private          | template    | volatile |
| const_cast | float        | protected        | this        | wchar_t  |
| continue   | for          | public           | throw       | while    |
| default    | friend       | register         | true        |          |
| delete     | goto         | reinterpret_cast | try         |          |

### 标识符命名规则

给标识符命名时，争取做到见名知意的效果，方便自己和他人的阅读。

- 标识符不能是关键字。

- 标识符只能由字母、数字、下划线组成。

- 第一个字符必须为字母或下划线。

- 标识符中字母区分大小写。

### 基本数据类型

| **数据类型**        | **占用空间**                            | 取值范围                         |
|-----------------|-------------------------------------|------------------------------|
| short(短整型)      | 2字节                                 | (-2^15 ~ 2^15-1)             |
| int(整型)         | 4字节                                 | (-2^31 ~ 2^31-1)             |
| long(长整形)       | Windows为4字节，Linux为4字节(32位)，8字节(64位) | (-2^31 ~ 2^31-1)             |
| long long(长长整形) | 8字节                                 | (-2^63 ~ 2^63-1)             |
|                 |                                     |                              |
| float           | 4字节                                 | 7位有效数字                       |
| double          | 8字节                                 | 15～16位有效数字                   |
|                 |                                     |                              |
| char            | 1字节                                 |                              |
|                 |                                     |                              |
| bool            | 字节                                  | true- 真（本质是1），false- 假（本质是0） |

### 转义字符

| **转义字符** | **含义**                     | **ASCII**码值（十进制） |
|----------|----------------------------|------------------|
| \a       | 警报                         | 007              |
| \b       | 退格(BS) ，将当前位置移到前一列         | 008              |
| \f       | 换页(FF)，将当前位置移到下页开头         | 012              |
| **\n**   | **换行(LF) ，将当前位置移到下一行开头**   | **010**          |
| \r       | 回车(CR) ，将当前位置移到本行开头        | 013              |
| **\t**   | **水平制表(HT)  （跳到下一个TAB位置）** | **009**          |
| \v       | 垂直制表(VT)                   | 011              |
| **\\\\** | **代表一个反斜线字符"\"**           | **092**          |
| \'       | 代表一个单引号（撇号）字符              | 039              |
| \"       | 代表一个双引号字符                  | 034              |
| \?       | 代表一个问号                     | 063              |
| \0       | 数字0                        | 000              |
| \ddd     | 8进制转义字符，d范围0~7             | 3位8进制            |
| \xhh     | 16进制转义字符，h范围0~9，a~f，A~F    | 3位16进制           |

### 字符串型

```c++
// C风格字符串
// char 变量名[] = "字符串值"
char str1[] = "hello world";

// C++风格字符串
// string  变量名 = "字符串值"
string str = "hello world";
```

### 标准输入

用于从键盘获取数据。

语法：`cin >> 变量`。

```c++
// 整型输入
int a = 0;
cout << "请输入整型变量：" << endl;
cin >> a;
cout << a << endl;
```

### 运算符

| **运算符** | **术语** | **示例**      | **结果**                        |
|---------|--------|-------------|-------------------------------|
| +       | 正号     | +3          | 3                             |
| -       | 负号     | -3          | -3                            |
| +       | 加      | 10 + 5      | 15                            |
| -       | 减      | 10 - 5      | 5                             |
| *       | 乘      | 10 * 5      | 50                            |
| /       | 除      | 10 / 5      | 2                             |
| %       | 取模(取余) | 10 % 3      | 1                             |
| ++      | 前置递增   | a=2; b=++a; | a=3; b=3;                     |
| ++      | 后置递增   | a=2; b=a++; | a=3; b=2;                     |
| --      | 前置递减   | a=2; b=--a; | a=1; b=1;                     |
| --      | 后置递减   | a=2; b=a--; | a=1; b=2;                     |
|         |        |             |                               |
| =       | 赋值     | a=2; b=3;   | a=2; b=3;                     |
| +=      | 加等于    | a=0; a+=2;  | a=2;                          |
| -=      | 减等于    | a=5; a-=3;  | a=2;                          |
| *=      | 乘等于    | a=2; a*=2;  | a=4;                          |
| /=      | 除等于    | a=4; a/=2;  | a=2;                          |
| %=      | 模等于    | a=3; a%2;   | a=1;                          |
|         |        |             |                               |
| ==      | 相等于    | 4 == 3      | 0                             |
| !=      | 不等于    | 4 != 3      | 1                             |
| <       | 小于     | 4 < 3       | 0                             |
| \>      | 大于     | 4 > 3       | 1                             |
| <=      | 小于等于   | 4 <= 3      | 0                             |
| \>=     | 大于等于   | 4 >= 1      | 1                             |
|         |        |             |                               |
| !       | 非      | !a          | 如果a为假，则!a为真；  如果a为真，则!a为假。    |
| &&      | 与      | a && b      | 如果a和b都为真，则结果为真，否则为假。          |
| \|\|    | 或      | a \|\| b    | 如果a和b有一个为真，则结果为真，二者都为假时，结果为假。 |

### 流程控制

#### 选择

```c++
int main() {
    int age = 10;
    if (age > 10) {
        
    } else if (age > 20) {
        
    } else {
        
    }
    
    int b = 20;
    int c = 0;
    
    // 三目运算
    c = a > b ? a : b;
    cout << "c = " << c << endl;
    
    switch(age) {
        case age == 10:
            break;
 
        case age == 20:
            break;
        
        default:
            break;
    }
}
```

#### 循环

```c++
int main() {
    int num = 0;
    while (num < 10) {
        cout << "num = " << num << endl;
        num++;
    }
    
    int size = 0;
    do {
        cout << size << endl;
        size++;
    } while (size < 10);
    
    for (int i = 0; i < 10; i++) {
        if (i == 5) {
            continue;
        }
        if (i == 8) {
            break;
        }
        cout << i << endl;
    }
}
```

continue 并没有使整个循环终止，而 break 会跳出循环。

#### goto

可以无条件跳转语句。

语法：`goto 标记`。

如果标记的名称存在，执行到goto语句时，会跳转到标记的位置。

```c++
int main() {

    cout << "1" << endl;

    goto FLAG;
    cout << "2" << endl;
    cout << "3" << endl;
    cout << "4" << endl;
    FLAG:

    cout << "5" << endl;

    return 0;
}
```

上述逻辑会跳过`2、3、4`的打印输出。

在程序中不建议使用goto语句，以免造成程序流程混乱。

### 数组

#### 二维数组定义方式

```c++
1. 数组类型 数组名 [行树][列数];
2. 数组类型 数组名 [行数][列数] = {{n, n1}, {m, m1}};
3. 数组类型 数组名 [行数][列数] = {n, n1, m, m1};
4. 数组类型 数组名 [][列数] = {n, n1, m, m1};
```

#### 二维数组的变量名

- 能够通过数组名查看数组所占用内存，`typeof(arr)`。

- 能够通过数组名获取二维数组首地址，`&arr[0][0]`。

#### 代码实现

```c++
void testArr01();
void testArr02();
void testArr03();
void testArr04();

/**
 * 1. 数组类型 数组名 [行数][列数];
 */
void testArr01() {
    int arr[2][3];
    arr[0][0] = 1;
    arr[0][2] = 1;
    arr[1][0] = 1;
}

/**
 * 2. 数组类型 数组名 [行数][列数] = {{n, n1}, {m, m1}};
 */
void testArr02() {
    int arr[2][3] = {{1, 2, 3}, 
                     {4, 5, 6}};
}

/**
 * C++ 会推断数组行列
 * 3. 数组类型 数组名 [行数][列数] = {n, n1, m, m1};
 */
void testArr03() {
    int arr[2][3] = {1, 2, 3, 4, 5, 6};
}

/**
 * C++ 推断行数
 * 4. 数组类型 数组名 [][列数] = {n, n1, m, m1};
 */
void testArr04() {
    int arr[][3] = {{1, 2, 3}, 
                    {4, 5, 6}, 
                    {4, 5, 6}, 
                    {4, 5, 6}};
}
```

### 函数

> 默认情况下，函数是值传递，如果需要地址传递，则在参数数据类型后跟一个`&`，比如`(int& refVar)`。

#### 函数的定义

```c++
返回值类型 函数名 (参数列表) {
    // 函数体
    
    // return 表达式;
}
```

#### 代码实现

> 在C++中，程序是从上往下编译的，如果函数的调用放在了这个函数定义之前(未经声明)，在编译时就会报错。所以需要将函数先声明。

```c++
// 函数声明
int add(int x, int y);

int main() {
    // 函数调用
    int num = add(15, 16);
    cout << num << endl;
    return 0;
}

// 函数定义
int add(int x, int y) {
    return x + y;
}
```

#### 函数声明

函数的声明可以有多次，但是函数的定义只能有一次。即同名方法只能存在一个。

```c++
// 函数声明
int add(int x, int y);
int add(int m, int n);

int main() {
    // 函数调用
    int num = add(15, 16);
    cout << num << endl;
    return 0;
}

// 函数定义
int add(int x, int y) {
    return x + y;
}
```

#### 函数的分文件编写

>  `.h`中写类或者函数的声明，`.cpp`中写相应的实现。`.h` 文件叫头文件，它是不能被编译的，`#include` 叫做预编译指令，相当于将`.h`文件中的内容添加到了`.cpp`的头部。

步骤：

1. 创建`.h`头文件。

2. 创建`.cpp`源文件。

3. 在头文件中写函数的声明。

4. 在源文件中写函数的定义。

- `swap.h`

```c++
#include <iostream>
using namespace std;

void swap(int a, int b);
```

- `swap.cpp`

```c++
#include "swap.h"

void swap(int a, int b) {
    int temp = a;
    a = b;
    b = temp;
}
```

- 使用

```c++
#include <iostream>
#include "swap.h"
using namespace std;

int main() {
    swap(10, 20);
    return 0;
}
```

### 指针

#### 定义和使用

- 指针定义的语法：数据类型* 指针变量名。

```c++
#include <iostream>
using namespace std;

int main() {
    int a = 10;
    // 让指针记录变量a的地址
    int* p = &a;
    
    cout << "a address: " << &a << endl;
    cout << "p value: " << p << endl;
    
    // *p 表示解引用，找到指针指向的内存中的数据
    *p = 20;
    
    cout << a << endl;
    cout << *p << endl;
    return 0;
}
```

#### 指针的占用内存

- 在32位操作系统下，指针占 4 个字节，在64位操作系统下，指针占 8 个字节。

#### 空指针&野指针

- 空指针：指针变量指向内存中编号为 0 的空间。

- 用于初始化指针变量。

- 空指针指向的内存是不可以访问的。访问空指针会报错。

- 内存编号`0~255`为系统占用内存，不允许用户访问。

```c++
int main() {

    int* p = NULL;

    // 访问空指针报错
    cout << *p << endl;

    return 0;
}
```

野指针：指针变量指向非法的内存空间。

```c++
int main() {

    int* p = (int*) 0x1100;

    // 访问野指针报错
    cout << *p << endl;

    return 0;
}
```

#### const修饰指针

1. const修饰指针（常量指针）。`指针指向的值不可以改，指针的指向可以改`

2. const修饰常量（指针常量）。`指针的指向不可以改，指针指向的值可以改`

3. const同时修饰指针、常量。`都不可以改`

```c++
int main() {
    int a = 100;
    int b = 200;

    // 1. const修饰指针（常量指针）。`指针指向的值不可以改，指针的指向可以改`
    const int* a1 = &a;
    a1 = &b;

    // *a1 = 50;    // Read-only variable is not assignable

    // 2. const修饰常量（指针常量）。`指针的指向不可以改，指针指向的值可以改`
    int* const a2 = &a;
    *a2 = 50;

    // a2 = &a;     // Cannot assign to variable 'a2' with const-qualified type 'int *const'
    
    // 3. const同时修饰指针、常量。`都不可以改`
    const int* const a3 = &b;
    
    // *a3 = 50;   // Read-only variable is not assignable
    // a3 = &b;    // Cannot assign to variable 'a3' with const-qualified type 'const int *const'

    return 0;
}
```

#### 指针和数组

```c++
int main() {
    int arr[10] = {1, 2, 3, 4, 5};

    // arr 就是数组首地址
    int* p = arr;

    cout << "first element: " << p[0] <<endl;

    // *p 表示解引用，找到指针指向的内存中的数据
    cout << "first element: " << *p <<endl;

    // 让指针向后偏移4个字节/8个字节，具体看多少位操作系统
    p++;

    cout << "second element: " << *p <<endl;

    return 0;
}
```

#### 指针和函数

将指针作为函数参数进行传递，可以修改实参的值。

```c++
/**
 * 值传递
 */
void swap01(int a, int b) {
    int temp = a;
    a = b;
    b = temp;
}

/**
 * 地址传递
 * *a 表示解引用，找到指针指向的内存中的数据
 */
void swap02(int* a, int* b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

int main() {

    int a = 10;
    int b = 5;
    
    int c = 20;
    int d = 50;
    
    swap01(a, b);
    swap02(&c, &d);
    
    cout << "a = " << a << endl;
    cout << "b = " << b << endl;
    cout << "c = " << c << endl;
    cout << "d = " << d << endl;
    
    return 0;
}
```

### 结构体 struct

结构体属于开发者自定义的数组类型，允许开发者存储不同的数据类型。

#### 结构体的定义和使用

- 语法：`struct 结构体名 { 结构体成员属性 };`

```c++
struct Student {
    string name;
    int age;
    float score;
} s3;

int main() {
    // 在使用结构体时，struct 关键字可以省略.

    // 使用方式一
    Student s1;
    s1.name = "Tommy";
    s1.age = 18;
    s1.score = 98.6f;

    // 使用方式二
    struct Student s2 = {"Jack", 24, 88.8f};

    // 使用方式三，在定义结构体时，设置结构体变量名
    s3.name = "Mark";
    s3.age = 25;
    s3.score = 76.5f;

    return 0;
}
```

#### 结构体数组

```c++
struct Student {
    string name;
    int age;
    float score;
};

int main() {
    struct Student s2[2] = {
            {"Tommy", 18, 98.8f},
            {"Jack", 24, 88.8f}};
    
    s2[1].age = 26;
    
    return 0;
}
```

#### 结构体指针

> 结构体如果作为参数传递时，默认是值传递。如果需要修改结构体的属性值，需要传递结构体指针。

- 通过指针访问结构体中的成员属性。

- 通过操作符`->`可以通过结构体指针访问结构体属性。

```c++
struct Student {
    string name;
    int age;
    float score;
};

int main() {
    struct Student s2 = {"Tommy", 18, 98.8f};

    Student* p = &s2;
    
    p -> name = "Jack";
    p -> age = 28;
    p -> score = 60.0f;

    return 0;
}
```

#### 结构体嵌套

在结构体中可以定义另一个结构体作为成员。

```c++
struct Course {
    string courseName;
    int classHour;
};

struct Student {
    string name;
    int age;
    float score;
    
    // 结构体嵌套
    struct Course course;
};
```

#### const 在结构体中的使用场景

作用与const修饰指针的用法一致。用以限定只读状态。

```c++
struct Student {
    string name;
    int age;
    float score;
};

// 可以修改指针的指向，但不可以修改指针指向的值
void update01(const Student* stu) {
}

// 可以修改指针指向的值，但不可以修改指针的指向
void update02(Student* const stu) {
    stu->name = "Jack";
}

// 都不能修改
void update03(const Student* const stu) {
}
```

## C++ 核心编程

### 内存分区模型

C++ 程序在执行时，会将内存大致分为4个区域。

- 代码区：存放函数体的二进制代码，由操作系统进行管理。

- 全局区：存放全局变量、静态变量以及常量。

- 栈：存放函数的参数值、局部变量等，由编译器自动分配和释放。

- 堆：由开发者自行分配和释放，若不手动释放，则程序结束时由操作系统回收。

#### 程序运行前

在程序编译后，生成 exe 可执行文件，执行改程序前分为两个区域。

- 代码区：

  - 存放 CPU 执行的机器指令。

  - 代码区是共享的，其目的是对于频繁被执行的程序，只需要在内存中有一份代码即可。

  - 代码区是只读的，其目的是防止程序意外地修改了指令。

- 全局区：

  - 存放全局变量、静态变量。

  - 全局区还包含常量区，字符串常量和其它常量（全局 const）也存放在全局区。

  - 该区域的数据在程序结束后由操作系统释放。

#### 程序运行后

栈区：

  - 由编译器自动分配和释放内存，存放函数的参数值、局部变量等。

  - 由于栈区开辟的内存会由编译器自动释放，所以不要返回局部变量的地址，获取到的局部变量地址可能是野指针。

```c++
int* func() {
    int a = 10;
    return &a;
}

int main() {
    int* p = func();

    cout << *p << endl;
    cout << *p << endl;

    return 0;
}
```

堆区：

- 在C++中主要利用`new`关键字在堆区开辟内存空间。

- 由开发者自行分配和释放，若不手动释放，则程序结束时由操作系统回收。

```c++
int* func() {
    int* a = new int (100);
    return a;
}

int main() {
    int* p = func();

    cout << *p << endl;

    return 0;
}
```

#### new操作符

> C++ 中 new 出来的变量都是通过指针创建的，即 new 返回的是指针。

语法：`new 数据类型`。

利用 new 创建的数据，会返回该数据对应的类型的指针，释放内存用关键字`delete`实现。

```c++
int* func() {
    int* a = new int (100);
    return a;
}

int main() {
    int* p = func();

    cout << *p << endl;
    
    delete p;
    
    // 在堆区创建数组
    int* arr = new int[10];
    
    // 释放数组
    delete[] arr;

    return 0;
}
```

释放的内存空间不可访问，那是一个野指针。

### 引用

#### 基本使用

引用访问一个变量是直接访问，引用是一个变量的别名，本身不单独分配自己的内存空间。

语法：`数据类型 &别名 = 原名`。

```c++
int main() {
    int a = 10;
    int &b = a;

    cout << "a= " << a << endl;
    cout << "b= " << b << endl;

    b = 100;

    cout << "a= " << a << endl;
    cout << "b= " << b << endl;

    return 0;
}
```

#### 注意事项

- 引用必须初始化。

- 引用在初始化后，不可以变更指向。

```c++
int main() {
    int a = 10;
    int b = 20;

    int &c = a;

    c = b;  // 赋值操作，不是变更引用

    cout << c << endl;

    return 0;
}
```

#### 引用做函数参数

```c++
void swap(int &m, int &n) {
    int temp = m;
    m = n;
    n = temp;
}

int main() {
    int a = 10;
    int b = 20;

    swap(a, b);

    cout << a << endl;
    cout << b << endl;

    return 0;
}
```

通过引用传参产生的效果和按指针传递是一样的。

#### 引用做函数返回值

```c++
int& func() {
    static int a = 10;
    return a;
}
```

#### 引用的本质

引用的本质在 C++ 内部实现是一个指针常量（指针的指向不可以改，指针指向的值可以改）。

```c++
// 参数类型是引用，转换为 int* const ref = &a
void func(int& ref) {
    // ref是引用，转换为 *ref = 100
    ref = 100;
}

int main() {
    int a = 10;

    // 自动转换为 int* const ref = &a
    int& ref = a;
    // C++ 编译器检测到 ref 是一个引用，则自动转换为 *ref = 20
    ref = 20;

    func(a);

    return 0;
}
```

引用可以看作是语法糖，语法方便，编译器在底层进行指针处理。

#### 常量引用

常量引用主要用来修饰形参，防止误操作。

```c++
/**
 * int& ref = 10; 这段代码编译不通过，原因是引用必须是引用一块合法的内存空间
 * const int& ref = 10; 这段代码编译通过，原因是编译器底层做了两件事：
 *      1. int temp = 10;
 *      2. const int& ref = temp;
 *      编译器会创建一个临时变量，而 ref 则是临时变量的一个别名，即引用。操作的也是引用
 * */
void func(const int& ref) {
    // ref = 100;  // Cannot assign to variable 'ref' with const-qualified type 'const int &'
    cout << ref << endl;
}
```

### 函数高级用法

#### 函数默认参数

1. 如果某个位置参数有默认值，那么从这个位置起始，从左往右都必须要有默认值。

2. 如果函数声明有默认值，则函数的实现就不能有默认参数。反之亦然。

```c++
int func(int a, int b = 2, int c = 3) {
    return a + b + c;
}

// Missing default argument on parameter 'c'
// int func03(int a, int b = 2, int c) {}

int func01(int a = 10, int b = 20);

int func01(int a, int b) {
    return a + b;
}

// error: Redefinition of default argument
// int func01(int a = 10, int b = 20) {}

int main() {

    func(10, 50, 30);
    
    func(10);

    return 0;
}
```

#### 函数占位参数

- 如果函数形参中有占位参数，调用函数时必须填补该位置。

- 占位参数也可以有默认值。

```c++
void func01(int a, int) {
    
}

void func02(int a, int = 10) {

}

int main() {

    func01(10, 20);

    func02(10);

    return 0;
}
```

#### 函数重载

函数重载满足条件：

1. 同一个作用域。

2. 函数名相同。

3. 函数参数类型不同、参数个数不同、参数顺序不同。

**函数的返回类型不作为函数重载的条件**。

```c++
void func01(int a) {}
void func01(int a, int b) {}
void func01(int a, int b, int c) {}
void func01(int a, int b, int c, int d) {}

int main() {

    func01(10);
    func01(10, 20);
    func01(10, 20, 30);
    func01(10, 20, 30, 40);

    return 0;
}
```

函数重载注意事项：

- 引用作为重载条件。

- 函数重载遇到函数默认参数。

```c++
void func01(int &a) {}
void func01(const int &a) {}
void func01(int a, int b = 10) {}

int main() {

    int a = 10;
    int &b = a;
    // func01(b);  // Call to 'func01' is ambiguous
    // func01(10); // Call to 'func01' is ambiguous
    func01(10, 20);

    return 0;
}
```

引用可以作为函数重载条件。但是函数重载时，不建议使用默认参数，因为默认参数可以不传入，可能会引起二义性。

### 类和对象

C++面向对象的三大特性：封装、继承、多态。

C++中一切皆对象 ，对象上有其属性和行为。

#### 封装

通过`class`关键字定义一个类。

```c++
class Student {
private:
    string name;
    int age;

public:
    void setName(string val) {
        this->name = val;
    }

    void setAge(int val) {
        this->age = val;
    }

    void tString() {
        cout << this->name << " " << this->age << endl;
    }
};

int main() {

    Student stu;
    stu.setAge(18);
    stu.setName("Tommy");
    stu.tString();  // Tommy 18

    return 0;
}
```

- 访问权限：

  - public：本类可以访问，类外可以访问。

  - protected：本类可以访问，子类访问。

  - private：仅本类可以访问。

#### struct和class区别

struct和class都能表示一个类。

在C++中 struct 和 class 唯一的区别在于**默认的访问权限不同**。

- struct 默认是公共权限。

- class 默认是私有权限。

#### 对象的初始化的销毁

C++中的对象会有初始化操作以及对象销毁前的处理操作。

##### 构造函数和析构函数

- 构造函数：主要用于在创建对象时，为对象的成员属性赋值，构造函数由编译器自动调用。无须手动调用。

- 析构函数：主要用于对象销毁前的清理工作，对象销毁前由系统自动调用。

C++利用**构造函数**和**析构函数**解决了对象的初始化和销毁，这两个函数会被编译器自动调用。如果不提供构造和析构，编译器默认提供的是空实现。

构造函数语法：`类名() {}`。

- 可以重载。

析构函数语法：`~类名() {}`。

- 不可以有参数，不可以重载。

```c++
class Student {
private:
    string name;
    int age;

public:
    void setName(string val) {
        this->name = val;
    }

    void setAge(int val) {
        this->age = val;
    }

    void tString() {
        cout << this->name << " " << this->age << endl;
    }

    Student() {
        cout << "Student" << endl;
    }

    ~Student() {
        cout << "~Student" << endl;
    }
};

/*
 * 打印结果：
     Student 
     Tommy 18
     ~Student
 * */
int main() {
    Student stu;
    stu.setAge(18);
    stu.setName("Tommy");
    stu.tString();  // Tommy 18

    return 0;
}
```

##### 构造函数的分类及调用

分类方式：

1. 按参数分：有参构造和无参构造。

2. 按类型分：普通构造和拷贝构造。

调用方式：

1. 括号调用。

2. 显式调用。

3. 隐式转换调用。

```c++
class Student {
public:

    // 无参构造
    Student() {
        cout << "Student()" << endl;
    }

    // 有参构造
    Student(int p) {
        cout << "Student(int p)" << endl;
    }

    // 拷贝构造
    Student(const Student &p) {
        cout << "Student(const Student &p)" << endl;
    }

    // 析构函数
    ~Student() {
        cout << "~~Student()" << endl;
    }
};

int main() {

    // 括号调用
    Student s1;
    Student s2(10);
    Student s3(s2);

    // 显式调用
    Student m1;
    Student m2 = Student(10);
    Student m3 = Student(m2);

    Student(10); // 匿名对象，当前行执行结束后，系统会自动回收匿名对象.
    // Student(m3);

    // 隐式转换调用. 等同于 Student n1 = Student(10);
    Student n1 = 10;

    return 0;
}
```

注意事项：

1. 括号调用时，无参构造不需要加`()`，编译器会认为是函数声明。

2. 不要用拷贝对象初始化匿名对象，编译器会认为`Student(m3) == Student m3`是对象声明。

##### 拷贝构造函数调用时机

1. 使用一个已经创建完成的对象来初始化一个新对象。

2. 值传递的方式给函数参数传参。

3. 以值方式返回局部对象（这种拷贝构造方式已经在 C++11 中被优化了）。

```c++
class Student {
public:

    // 无参构造
    Student() {
        cout << "Student()" << endl;
    }

    // 有参构造
    Student(int p) {
        cout << "Student(int p)" << endl;
    }

    // 拷贝构造
    Student(const Student &p) {
        cout << "Student(const Student &p)" << endl;
    }

    // 析构函数
    ~Student() {
        cout << "~~Student()" << endl;
    }
};

void func(Student stu) {}

Student func1() {
    Student s;
    cout << (int*)&s << endl;
    return s;
}

void func2() {
    Student s = func1();
    cout << (int*)&s << endl;
}

int main() {

    // 1. 使用一个已经创建完成的对象来初始化一个新对象。
    Student s1(10);
    Student s2(s1);

    // 2. 值传递的方式给函数参数传参。
    Student s3;
    func(s3);
    
    // 3. 以值方式返回局部对象
    func2();

    return 0;
}
```

##### 构造函数调用规则

默认情况下，C++编译器至少给一个类添加3个函数。

1. 默认构造函数（无参构造）

2. 默认析构函数（无参构造）

3. 默认拷贝构造函数，对属性进行值拷贝

开发者自定义的情况。

- 如果开发者定义有参构造，则C++不再提供默认无参构造，但是会提供默认拷贝构造。

- 如果开发者定义拷贝构造函数，则C++不再提供其它构造函数。

##### 深拷贝&浅拷贝

> 用Java中的思想理解即：
> 
> - 浅拷贝：基本数据类型会重新赋值，但引用类型（指针）会共用。
> 
> - 深拷贝：基本数据类型会重新赋值，引用类型（指针）会重新在堆区开辟一块新的内存空间。

- 浅拷贝：值赋值拷贝操作。

- 深拷贝：在堆区重新申请一块内存空间，进行拷贝操作。

浅拷贝如果遇到指针类型拷贝，会出现交叉释放内存的情况。可以自定义拷贝构造函数来避免这种情况。

##### 初始化列表

C++提供了初始化列表语法，用来初始化属性。

语法：`类名(): 属性1(值1), 属性2(值2)... {}`

```c++
class Student {
public:
    string name;
    int age;

    // 传统初始化方式
    /*Student(string nameVal, int ageVal) {
        name = nameVal;
        age = ageVal;
    }*/

    // 初始化列表方式
    Student(string nameVal, int ageVal): name(nameVal), age(ageVal) {

    };
};

int main() {

    Student s1("Tommy", 18);
    cout << s1.name << " " << s1.age << endl;  // Tommy 18

    return 0;
}
```

##### 类对象作为类成员

即一个类作为另一个类中的成员属性。

```c++
class A {
public:
    A() {
        cout << "A constructor" << endl;
    }

    ~A() {
        cout << "A destroy" << endl;
    }
};

class B {
private:
    A a;
public:
    B() {
        cout << "B constructor" << endl;
    }

    ~B() {
        cout << "B destroy" << endl;
    }
};

int main() {
    B();
    return 0;
}
/**
 * 打印结果
    A constructor
    B constructor
    B destroy    
    A destroy
*/
```

当 B 类中有对象 A 作为成员时，那么创建 B 对象时，A 和 B 的构造和析构的顺序是谁先谁后？

- 从打印结果来看，A 的构造先于 B 执行，而 B 的析构先于 A 执行。

- 当其他类对象作为本类成员时，构造时先构造类成员属性，再构造本类。析构时先析构本类，再析构类成员。

##### 静态成员

静态成员包括静态变量和静态函数（方法），用`static`关键字修饰。

- 静态成员变量

  - 所有对象共享同一份静态成员变量。

  - 在编译阶段分配内存。

  - 在本类中声明，类外初始化。

- 静态成员函数

  - 所有对象共享同一个函数。

  - 静态函数只能访问静态变量。

```c++
class Student {
public:
    // 声明
    static int age;

    static void func() {
        cout << age << endl;
    }
};

// 初始化
int Student::age = 18;

int main() {
    // 通过类名进行访问静态变量
    Student::age = 20;
    
    Student::func();

    return 0;
}
```

`Student::func()`等同于 Java 中`Student.func()`，`类名.静态方法`。

#### 对象模型和this指针

##### 成员变量和成员函数分开存储

在C++中，类的成员变量和成员函数分开存储，即只有非静态成员变量才属于类的对象。而所有非静态函数共享一个函数实例。

- 非静态成员变量占对象空间。

- 静态成员变量不占对象空间。

- 函数不占对象空间，所有函数共享一个函数实例。

```c++
class A {};

class Student {
public:
    int num;
    void func01() {}

    static int age;
    static void func02() {}
};

int main() {
    A a;
    cout << sizeof(a) << endl;  // 1，空对象占用 1 个字节

    Student s1;
    // 4，如果有属性，则占用实际字节数，但静态变量、函数不占用对象内存
    cout << sizeof(s1) << endl;

    return 0;
}
```

##### this指针

> this 指针的本质是**指针常量**，指针的指向是不可以修改的。

**`this`指针指向被调用的成员函数所属的对象**。（当前对象）

- 当形参和成员变量同名时，可以用`this`指针加以区分。

- 在类的非静态成员函数中返回对象本身时，可以使用`return *this`。

```c++
class Student {
private:
    int age;
public:

    void setAge(int age) {
        this->age = age;
    }

    int getAge() {
        return this->age;
    }

    // 返回 "this"，返回值类型是 引用
    Student& builder() {
        // 返回对象本身
        return *this;
    }
};

int main() {
    Student s1;
    s1.setAge(18);
    cout << s1.getAge() << endl;

    return 0;
}
```

##### 空指针访问成员函数

C++中空指针也可以调用成员函数，但如果空指针调用成员函数，肯定是会出错的。

如果用到 this 指针，需要加以判断保证代码的健壮性。

```c++
class Student {
private:
    int age;
public:

    void setAge(int age) {
        if (this == NULL) {
            return;
        }
        this->age = age;
    }

    int getAge() {
        if (this == NULL) {
            return 0;
        }
        return this->age;
    }
};

int main() {
    Student s1;
    s1.setAge(18);
    cout << s1.getAge() << endl;

    return 0;
}
```

##### const修饰成员函数

const 修饰成员函数称这个函数为**常函数**。

- 常函数内不可以修改成员属性。

- 成员属性声明时用`mutable`关键字修饰后，在常函数中仍然可以被修改。

const 修饰声明对象称为常对象。

- 常对象只能调用常函数。

```c++
class Student {
private:
    int age;
    mutable string name;
public:
    Student() {}

    int setAge(int age) const {
        // Cannot assign to non-static data member within const member function 'setAge'
        // this->age = age;
    }

    int setName(string name) const {
         this->name = name;
    }

    int func() {}
};

int main() {
    const Student s1;
    s1.setName("Tommy");

    // this' argument to member function 'func' has type 'const Student', but function is not marked const 'func' declared here
    // s1.func();

    return 0;
}
```

#### 友元

友元的关键字是`friend`，其目的是让一个函数或者类，访问另一个类中私有成员。

友元的三种实现：

1. 全局函数做友元。

2. 类做友元。

3. 成员函数做友元。

```c++
// 声明类
class Student;

class A {
    int age;
    Student *stu;
public:
    A();

    void setAge(int age) {
        this->age = age;
    }

    int getAge() {
        return this->age;
    }

    int getStuAge();

    void func() {}
};

class Student {
    // 全局函数做友元
    friend void func(Student *stu, A *a);

    // 类做友元
    friend class A;

    // 成员函数做友元
    friend void A::func();

private:
    int age;
public:
    Student() {
        age = 26;
    }
};

void func(Student *stu, A *a) {
    stu->age = 20;
    a->setAge(stu->age);
}

// 在类外初始化构造函数
A::A() {
    stu = new Student;
}

// 在类外初始化函数
int A::getStuAge() {
    return stu->age;
}

int main() {

    A a;
    Student s;
    func(&s, &a);
    cout << a.getAge() << endl;  // 18

    A a1;
    cout << a1.getStuAge() << endl; // 26

    return 0;
}
```

`::`有作用域的含义。

#### 运算符重载

##### 加号重载

```c++
class Student {
public:
    int age;
    int height;

    // 成员函数重载 + 号
    Student operator+(Student &stu) {
        Student temp;
        temp.age = this->age + stu.age;
        temp.height = this->height + stu.height;
        return temp;
    }
    void print() {
        cout << this->age << endl;
        cout << this->height << endl;
    }
};

// 全局函数重载 + 号
/*Student operator+(Student &stu1, Student &stu2) {
    Student temp;
    temp.age = stu1.age + stu2.age;
    temp.height = stu1.height + stu2.height;
    return temp;
}*/

int main() {

    Student s1;
    s1.age = 18;
    s1.height = 160;
    Student s2;
    s2.age = 28;
    s2.height = 170;

    Student s3 = s1 + s2;
    s3.print();

    return 0;
}
```

- 成员函数重载本质调用等同于`Student s3 = s1.operator+(s2)`。

- 全局函数重载本质调用等同于`Student s3 = operator+(s1, s2)`。

本质上都是方法调用，只不过C++编译器将这些操作做了优化。

运算符重载支持函数重载。

##### 左移`<<`重载

可用于打印对象的属性，类似于`Java#toString()`。

```c++
class Student {
    friend void operator<<(ostream &out, Student &stu);
private:
    int age;
    int height;
public:

    void print() {
        cout << this->age << endl;
        cout << this->height << endl;
    }
};

// 只能利用全局函数重载左移运算符
void operator<<(ostream &out, Student &stu) {
    out << stu.age << " " << stu.height << endl;
}

int main() {

    Student s1;
    cout << s1;

    return 0;
}
```

##### 递增运算符重载

```c++
class Student {
private:
    int height;
    int age = 18;
public:
    int operator++() {
        return ++age;
    }

    int operator++(int) {
        return age++;
    }
};
```

##### 赋值运算符重载

用于在对象拷贝赋值时，对于指针在堆区重新创建对象。避免交叉释放问题。

```c++
class Student {
private:
    int *age;
    int height;
public:
    void operator=(Student &stu) {
        // 编译器默认是提供浅拷贝

        age = new int(*stu.age);
    }
};
```

##### 关系运算符重载

用于两个对象之间的属性比较。类似`Java#equals`。

```c++
class Student {
private:
    int age;
    int height;
public:
    bool operator==(Student &stu) {
        if (this->age == stu.age && this->height == stu.height) {
            return true;
        }
        return false;
    }
};
```

##### 函数调用运算符重载

```c++
class Student {
private:
    int height;
    int age = 18;
public:
    void operator()(string str) {
        cout << str << endl;
    }

    void operator()(string str, int num) {
        cout << str << " " << num << endl;
    }
};

int main() {

    Student s1;
    s1("Tommy");
    s1("Tommy", 18);
    
    return 0;
}
```

#### 继承

```c++
class Animal {
public:
    virtual void eat() {
        cout << "动物吃饭" << endl;
    }
};

// class 子类 : public 父类，: public 表示公共继承。比如 Java 中的继承关键字是 extends
class Dog : public Animal {
public:
    void eat() override {
        cout << "狗子吃骨头" << endl;
    }
};

int main() {

    return 0;
}
```

- `: public`：子类访问父类的属性时，访问权限不受影响。

- `: protected`：子类访问父类的属性时，在子类中访问权限变更为`protected`。

- `: private`：子类访问父类的属性时，在子类中访问权限变更为`private`。

> 如果父类中成员的访问修饰是 private，则子类无法访问，只是拥有。

##### 继承中构造和析构顺序

```c++
class Animal {
public:
    Animal() {
        cout << "Animal constructor" << endl;
    }
    void eat() {
        cout << "动物吃饭" << endl;
    }
    ~Animal() {
        cout << "Animal destroy" << endl;
    }
};

class Dog : protected Animal {
public:
    Dog() {
        cout << "Dog constructor" << endl;
    }
    void eat() {
        cout << "狗子吃骨头" << endl;
    }
    ~Dog() {
        cout << "Dog destroy" << endl;
    }
};

/**
 * 打印结果
    Animal constructor
    Dog constructor
    Dog destroy
    Animal destroy
    
    子类初始化时：
      构造：父类构造 -> 子类构造
      析构：子类析构 -> 父类析构
 */
int main() {
    Dog dog;
    return 0;
}
```

##### 继承同名成员处理方式

当子类与父类出现同名的成员时：

- 若访问子类同名成员，直接访问即可。

- 若访问父类同名成员，需要添加作用域。

```c++
class Animal {
protected:
    static string name;
    int age = 0;
public:
    Animal() {
    }
    void eat() {
        cout << "Animal eat" << endl;
    }
    static void test() {
        cout << "Animal static test()" << endl;
    }
};

class Dog : protected Animal {
    int age;
    static string name;
public:
    Dog() {
        age = 2;
    }
    void eat() {
        cout << "dog eat bone" << endl;
    }

    static void test() {
        cout << "Dog static test()" << endl;
    }

    /**
     * 打印结果
            dog age = 2                
            Animal age = 0             
                                       
            dog eat bone               
            Animal eat                 
                                       
            dog static name = Dog      
            Animal static name = Animal
                                       
            Dog static test()          
            Animal static test()
     */
    void func() {
        // 访问非静态成员
        cout << "dog age = " << age << endl;
        cout << "Animal age = " << this->Animal::age << endl;
        cout << endl;

        eat();
        this->Animal::eat();
        cout << endl;

        // 访问静态成员
        cout << "dog static name = " << name << endl;
        cout << "Animal static name = " << Dog::Animal::name << endl;
        cout << endl;

        test();
        Dog::Animal::test();
    }
};

string Animal::name = "Animal";
string Dog::name = "Dog";

int main() {
    Dog dog;
    dog.func();
    return 0;
}
```

如果子类中出现和父类同名的成员函数，子类会隐藏父类中所有同名成员函数，包括重载函数。如果需要访问父类中的同名函数，则需要添加作用域。

`Dog::Animal::name`第一个`::`代表通过类名方式访问，第二个`::`代表访问父类作用域下的成员。

##### 多继承

C++允许多继承。

语法：`class 子类 : 继承方式 父类1,  继承方式 父类2 ...`。

多继承可能会引发父类中有同名成员出现，需要加作用域区分。但是实际开发中不建议使用多继承。

```c++
class Base1 {};
class Base2 {};
class Base3 {};

class Animal {
public:
    Animal() {
    }
};

class Dog : public Animal, public Base1, public Base2, public Base3 {
public:
    Dog() {
    }
};

int main() {

    return 0;
}
```

##### 菱形继承

如果发生菱形继承，多个父类中拥有相同的数据，且都会携带给子类，但有些数据只需要一份即可，比如`age`。而菱形继承导致数据有多份，造成资源浪费。

```text
  Animal
  /    \
 Dog   Cat
  \    / 
 ClassTest
```

利用虚继承可以解决菱形继承的问题，在继承之前，加上关键字`virtual`变为虚继承，Animal 类称为虚基类。

```c++
class Animal {
protected:
    int age;
};

class Dog : virtual public Animal {};

class Cat : virtual public Animal {};

class ClassTest : public Dog, public Cat {
public:
    void func() {
        cout << age << endl;
    }
};
```

#### 多态

##### 基本概念

> 父类引用指向子类实例

多态分为两类：

- 静态多态：函数重载和运算符重载属于静态多态，复用函数名。

- 动态重载：派生类和虚函数实现运行时多态。

静态多态和动态多态的区别：

- 静态多态的函数地址在**编译阶段**确定。

- 动态多态的函数地址在**运行阶段**确定。

```c++
class Animal {
public:
    virtual void eat(){
        cout << "Animal eat" << endl;
    }
};

class Dog : public Animal {
public:
    void eat() override {
        cout << "Dog eat" << endl;
    }
};

void doEat(Animal & animal) {
    animal.eat();
}

int main() {
    Dog dog;
    doEat(dog);  // Dog eat
    return 0;
}
```

##### 纯虚函数和抽象类

当类中有了纯虚函数，则这个类称为抽象类。

语法：`virtual 返回类型 <funcName> ([参数列表]) = 0;`。

抽象类无法实例化对象，且子类必须重写父类中纯虚函数，否则也属于抽象类。

```c++
class Animal {
public:
    // 纯虚函数
    virtual void eat() = 0;
};

class Dog : public Animal {
public:
    void eat() override {
        cout << "Dog eat" << endl;
    }
};

void doEat(Animal & animal) {
    animal.eat();
}

int main() {
    Dog dog;
    doEat(dog);  // Dog eat
    return 0;
}
```

##### 虚析构和纯虚析构

多态使用时，如果子类中有属性开辟到堆区，那么父类指针在释放时无法调用到子类的析构函数。

可以将父类中的析构函数变更为**虚析构**或**纯虚析构**来解决这个问题。

虚析构和纯虚析构共性：

- 可以解决父类指针释放子类对象问题。

- 都需要有具体的函数实现。

虚析构和纯虚析构区别：

- 如果是纯虚析构，则该类属于抽象类，无法实例化对象。

虚析构语法：`virtual ~类名() {}`。

纯虚析构语法：`virtual ~类名() = 0`。

```c++
class Animal {
public:
    // 纯虚函数
    virtual void eat() = 0;

    // 纯虚析构
    virtual ~Animal() = 0;

    Animal() {
        cout << "Animal constructor" << endl;
    }

    // 虚析构
    /*virtual ~Animal() {
        cout << "Animal destroy" << endl;
    }*/
};

// 纯虚析构实现
Animal::~Animal() {
    cout << "Animal destroy" << endl;
}

class Dog : public Animal {
public:
    void eat() override {
        cout << "Dog eat" << endl;
    }
    Dog() {
        cout << "Dog constructor" << endl;
    }
    virtual ~Dog() {
        cout << "Dog destroy" << endl;
    }
};

void doEat(Animal & animal) {
    animal.eat();
}

/**
 * 打印结果
        Animal constructor
        Dog constructor   
        Dog eat           
        Dog destroy       
        Animal destroy
 */
int main() {
    Dog dog;
    doEat(dog);  // Dog eat
    return 0;
}
```

### 文件操作

文件类型分为两种：

1. 文本文件：文件以文本的ASCII码形式存储。

2. 二进制文件：文件以文本的二进制形式存储。

操作文件的三大类：

1. ofstream：写

2. ifstream：读

3. fstream：读写

C++中对文件的操作需要包含头文件`<fstream>`。

#### 文本文件

##### 写

```c++
#include <iostream>
#include <fstream>

using namespace std;

int main() {
    ofstream out;
    // out.open("文件路径", 打开方式);
    out.open("test.txt", ios::out);
    out << "hello world" << endl;
    out << "Tommy";
    out.close();
    return 0;
}
```

| 打开方式        | 说明            |
|-------------|---------------|
| ios::in     | 读文件           |
| ios::out    | 写文件           |
| ios::ate    | 初始位置：文件尾部     |
| ios::app    | 追加方式写文件       |
| ios::trunc  | 如果文件存在则先删除再创建 |
| ios::binary | 二进制方式         |

文件打开方式可以配合使用，使用`|`操作符分隔。

`ios::binary | ios::out`用二进制方式写入文件。

##### 读

```c++
#include <iostream>
#include <fstream>

using namespace std;

int main() {
    ifstream in;
    in.open("test.txt", ios::in);
    if (!in.is_open()) {
        cout << "文件打开失败" << endl;
        return -1;
    }

    // 读文件方式一
    char buf[1024];
    while (in >> buf) {
        cout << buf << endl;
    }

    // 读文件方式二
    /*while (in.getline(buf, sizeof(buf))) {
        cout << buf << endl;
    }*/

    // 读文件方式三
    /*string strBuf;
    while (getline(in, strBuf)) {
        cout << strBuf << endl;
    }*/

    // 读文件方式四
    /*char c;
    while ((c = in.get()) != EOF) {
        cout << c;
    }*/

    in.close();
    return 0;
}
```

#### 二进制文件

##### 写

```c++
#include <iostream>
#include <fstream>

using namespace std;

int main() {
    ofstream out;
    out.open("test.txt", ios::binary | ios::out);
    int a = 97;
    
    // out.write(字符指针, 读写的字节数);
    out.write((const char *) &a, sizeof(a));

    out.close();
    return 0;
}
```

##### 读

```c++
#include <iostream>
#include <fstream>

using namespace std;

int main() {
    ifstream in;
    in.open("test.txt", ios::binary | ios::in);

    int a;
    in.read((char *)&a, sizeof(a));

    cout << a << endl;
    in.close();
    return 0;
}
```

## C++高级编程

这部分主要针对C++**泛型编程**和**STL**技术，探讨C++更深层次的使用。

模板不可以直接使用，它只是一个框架。且模板的通用不是万能的。

C++中泛型的实现就是基于模板，C++提供两种模板机制，**函数模板**和**类模板**。

### 模板

#### 函数模板

语法：

```c++
/*
 * template 声明定义模板的关键字
 * typename 定义泛型的类型，可以用 class 代替
 * T 泛型类型
 * */
template<typename T>
函数声明或定义
```

示例：

```c++
template<typename T>
// 交换两个变量的值
void swapNum(T &val1, T &val2) {
    T temp = val1;
    val1 = val2;
    val2 = temp;
}

int main() {
    int a = 10;
    int b = 20;
    
    // 1、自动类型推导
    swapNum(a, b);

    // 2、显式指定类型
    swapNum<int>(a, b);

    cout << a << endl;
    cout << b << endl;

    return 0;
}
```

模板必须要确定`T`的数据类型，才可以使用，推荐显式指定类型使用。

##### 普通函数和函数模板的区别

> 有点类似类型向上转型

普通函数和函数模板的区别：

- 普通函数在调用时是自动类型转换（隐式类型转换）。

- 函数模板在调用时，如果利用自动类型推导，则不会发生隐式类型转换。

- 如果利用显式指定类型的方式，则隐式类型转换。

```c++
int add01(int val1, int val2) {
    return val1 + val2;
}

template<typename T>
T add(T val1, T val2) {
    return val1 + val2;
}

int main() {
    int a = 10;
    int b = 20;
    char c = 'c';

    cout << add01(a, c) << endl;

    // No matching function for call to 'add'
    // add(a, c);

    cout << add<int>(a, c) << endl;

    return 0;
}
```

将 char 类型强转为 int 类型后，进行运算。

##### 普通函数与模板函数的调用规则

1. 如果函数模板和普通函数有同名函数，优先调用普通函数。

2. 可以通过空模板参数列表来强制调用函数模板。

3. 函数模板可以发生重载。

4. 如果函数模板匹配度更高，则优先调用函数模板。

```c++
int add(int val1, int val2) {
    return val1 + val2 + 1000;
}

template<typename T>
T add(T val1, T val2) {
    return val1 + val2;
}

template<typename T>
T add(T val1, T val2, T val3) {
    return val1 + val2 + val3;
}

int main() {
    int a = 10;
    int b = 20;

    // 1. 如果函数模板和普通函数有同名函数，优先调用普通函数
    cout << add(a, b) << endl;  // 1030

    // 2. 可以通过空模板参数列表来强制调用函数模板
    cout << add<>(a, b) << endl; // 30

    // 3. 函数模板可以发生重载
    cout << add<>(a, b, 30) << endl;  // 60

    char c = '9';
    char d = '7';

    // 4. 如果函数模板匹配度更高，则优先调用函数模板，（不用发生类型强转）
    cout << add(c, d) << endl;  // p

    return 0;
}
```

如果提供了函数模板，建议不要再提供普通函数，避免二义性。

##### 模板的局限性

```c++
template<typename T>
void func01(T a, T b) {
    if (a > b) { ... }
}

template<typename T>
void func02(T a, T b) {
    a = b;
}
```

上述两个函数模板中，如果传入的类型的数组，或者自定义数据类型，那么函数模板就无法正常运行。

C++提供了模板的重载，给这些特定的类型提供具体化的模板。

```c++
class Student {
public:
    int age;
    Student(int age) {
        this->age = age;
    }
};

template<typename T>
bool compareTo(T a, T b) {
    if (a > b) {
        return true;
    }
    return false;
}

// 具体化模板
template<> bool compareTo(Student a, Student b) {
    if (a.age > b.age) {
        return true;
    }
    return false;
}

int main() {
    Student s1(18);
    Student s2(28);

    cout << compareTo(s1, s2) << endl;

    return 0;
}
```

#### 类模板

```c++
template<class NameType, class AgeType>
class Student {
public:
    NameType name;
    AgeType age;
    Student(NameType name, AgeType age) {
        this->name = name;
        this->age = age;
    }
    void print() {
        cout << this->name << " " << this->age << endl;
    }
};

int main() {
    Student<string, int> s1("Tommy", 18);
    s1.print();

    return 0;
}
```

##### 类模板与函数模板区别

类模板与函数模板区别：

1. 类模板没有自动类型推导的使用方式。

2. 类模板在模板参数列表中可以有默认参数。

```c++
// age 定义默认类型，没传用默认，传了则用自定义的
template<class NameType, class AgeType = int >
class Student {
public:
    NameType name;
    AgeType age;
    Student(NameType name, AgeType age) {
        this->name = name;
        this->age = age;
    }
    void print() {
        cout << this->name << " " << this->age << endl;
    }
};

int main() {
    // 类模板没有自动类型推导的使用方式
    // Too few template arguments for class template 'Student'
    // Student<> s1("Tommy", 18);

    Student<string, int> s1("Tommy", 18);

    // 类模板在模板参数列表中可以有默认参数
    Student<string> s2("Tommy", 18);
    s1.print();

    return 0;
}
```

##### 类模板中成员函数创建时机

- 普通类中的成员函数在定义时就可以创建。

- 类模板中的成员函数在调用时才会创建。（编译器）

```c++
class Student1 {
public:
    void func01() {}
};

class Student2 {
public:
    void func02() {}
};

// age 定义默认类型，没传用默认，传了则用自定义的
template<class T>
class Student {
public:
    T stu;

    void func01() {
        stu.func01();
    }

    void func02() {
        stu.func02();
    }
};

int main() {
    // Student<Student1> stu;
    
    Student<Student2> stu;
    
    // No member named 'func01' in 'Student2'
    // stu.func01();
    
    // No member named 'func02' in 'Student1'
    stu.func02();

    return 0;
}
```

类模板中的成员函数在调用时（编译期）才会确定创建。

##### 类模板对象做函数参数

1. 指定传入的类型，显式对象的数据类型。

2. 参数模板化，将对象中的参数变为模板进行传递。

3. 类模板化，将这个对象类型模板化进行传递。

```c++
template<class T>
class Student {
public:
    T name;
    Student(T name) {
        this->name = name;
    }

    void func() {
        cout << this->name << endl;
    }
};

// 1、指定传入的类型，显式对象的数据类型
void print01(Student<string> &stu) {
    stu.func();
}

// 2、参数模板化
template<typename T>
void print02(Student<T> &stu) {
    stu.func();
}

// 3、类模板化
template<typename T>
void print03(T &stu) {
    stu.func();
}

int main() {
    Student<string> s1("Tommy");
    print01(s1);

    Student<string> s2("Jack");
    print02(s2);

    Student<string> s3("Mark");
    print02(s3);
    return 0;
}
```

可以通过`typeid(<泛型标识>).name()`查看类型名称。

实际开发中，第一种传递方式比较常用。

##### 类模板与继承

```c++
template<class T>
class Student {
public:
    T name;
};

// 当子类继承的是类模板时，子类在声明时，需要指定父类泛型
class Tommy : public Student<string> {

};

// 如果需要灵活指定父类中泛型，则子类也需要变为类模板
template<class T, class T1>
class Jack : public Student<T> {
public:
    T1 age;
};


int main() {
    Tommy tommy;

    Jack<string, int> jack;
    
    return 0;
}
```

##### 类模板成员函数类外实现

```c++
template<class T>
class Student {
public:
    T name;

    Student();

    void print();
};

// 构造函数 类外实现
template<typename T>
Student<T>::Student() {
    this->name = "Tommy";
}

// 成员函数 类外实现
template<typename T>
void Student<T>::print() {
    cout << this->name << endl;
}

int main() {

    Student<string> stu;
    stu.print();

    return 0;
}
```

##### 类模板分文件编写

- 解决方式1：直接引入`.cpp`源文件。

- 解决方式2：将类模板声明和实现写在`.hpp`文件中，`.hpp`是约定的名称，并不是强制。

student.hpp：

```c++
#pragma once
#include "iostream"
#include "string"
using namespace std;

template<class T>
class Student {
public:
    T name;

    Student();

    void print();
};

template<typename T>
Student<T>::Student() {
    this->name = "Tommy";
}

template<typename T>
void Student<T>::print() {
    std::cout << this->name << endl;
}
```

main.cpp：

```c++
#include <iostream>
#include "string"
#include "student.hpp"

using namespace std;

int main() {

    Student<string> stu;
    stu.print();

    return 0;
}
```

通过这种方式能够有效地解耦，不至于所有代码都写在`main.cpp`内。

##### 类模板与友元

- 全局函数在本类中实现，直接在本类中声明友元即可。

- 全局函数在类外实现，需要提前让编译器知道全局函数的存在。

```c++
// 声明类模板
template<class T> class Student;

// 声明函数模板
template<typename T>
void printMsg2(Student<T> &stu) {
    cout << stu.name << endl;
}

template<class T>
class Student {
    // 全局函数配合友元，本类实现
    friend void printMsg(Student<T> &stu) {
        cout << stu.name << endl;
    }

    // 全局函数配合友元，类外实现
    friend void printMsg2<>(Student<T> &stu);

private:
    T name;
public:
    Student() {
        this->name = "Tommy";
    }
};

int main() {
    Student<string> stu1;
    
    printMsg(stu1);

    printMsg2(stu1);

    return 0;
}
```

全局函数配合友元，类外实现时，需要提前声明类、函数，提前告知编译器在哪里能找到。

但是建议全局函数做本类实现，用法简单，且编译器可以直接识别。

### STL

STL：Standard Template Library，**标准模板库**。

STL 从广义上分为：容器（container）、算法（algorithm）、迭代器（iterator）。

**容器**和**算法**之间通过**迭代器**进行无缝衔接。

STL 几乎所有的代码都采用了模板类或者模板函数。其目的就是代码的复用性。

STL 大致分为六大组件，容器、算法、迭代器、仿函数、适配器（配接器）、空间配置器。

1. 容器：数据结构，如 vector、list、deque、set、map 等，用来存放数据。

2. 算法：如 sort、find、copy、for_each 等。

3. 迭代器：扮演容器与算法之间的胶合剂。（可以简单理解为 Java 中的迭代器）

4. 仿函数：行为类似函数，可作为函数的某种策略。

5. 适配器：用来修饰容器、仿函数或者迭代器接口。

6. 空间配置器：负责内存的配置与管理。

迭代器提供一种方法，使之能够依序寻访某个容器包含的各个元素，而又无需暴露该容器的内部标识方式，每个容器都有自己的迭代器。迭代器使用非常类似于指针。

迭代器种类：

| 迭代器     | 描述                     | 支持哪些运算符                       |
|---------|------------------------|-------------------------------|
| 输入迭代器   | 对数据只读访问                | 只读，支持 ++、==、!=                |
| 输出迭代器   | 对数据只写访问                | 只写，支持 ++                      |
| 前向迭代器   | 读写操作，并能向前推进迭代器         | 读写， 支持 ++、==、!=               |
| 双向迭代器   | 读写操作，并能向前和向后操作         | 读写，支持 ++、--                   |
| 随机访问迭代器 | 读写操作，随机访问，（可以理解为数组的方式） | 读写，支持 ++、--、[n]、-n、< 、<=、>、>= |

常用容器的迭代器，双向迭代器、随机访问迭代器。

#### 示例（放置内置数据类型）

```c++
#include <iostream>
#include "string"
#include "vector"
#include "algorithm"

using namespace std;

void print(int val) {
    cout << val << endl;
}

int main() {

    vector<int> vect;

    vect.push_back(10);
    vect.push_back(2);
    vect.push_back(88);

    // 第一种遍历方式
    /* 
    // 起始迭代器，指向容器中第一个元素
    vector<int>::iterator vectIterBegin = vect.begin();
    // 结束迭代器，指向容器中最后一个元素的下一个位置
    vector<int>::iterator vectIterEnd = vect.end();

    while (vectIterBegin != vectIterEnd) {
        cout << *vectIterBegin << endl;
        vectIterBegin++;
    } */

    // 第二种遍历方式
    /*for (vector<int>::iterator iter = vect.begin(); iter != vect.end(); iter++) {
        cout << *iter << endl;
    }
    */

    // 第二种遍历方式的简写版本
    /*for (int & iter : vect) {
        cout << iter << endl;
    }*/

    // 第三种遍历方式，底层相当于执行回调 print
    for_each(vect.begin(), vect.end(), print);

    return 0;
}
```

#### 示例（放置自定义类型）

```c++
#include <iostream>
#include "string"
#include "vector"
#include "algorithm"

using namespace std;

class Student {
    string name;
    int age;
public:
    Student(string name, int age) {
        this->name = name;
        this->age = age;
    }

    void print() {
        cout << "name: " << this->name << " age: " << this->age << endl;
    }
};

int main() {

    vector<Student> vect;
    /*vect.push_back(Student("Tommy", 18));
    vect.push_back(Student("Jack", 28));
    vect.push_back(Student("Mark", 25));*/

    // 简写版本
    vect.emplace_back("Tommy", 18);
    vect.emplace_back("Jack", 28);
    vect.emplace_back("Mark", 25);

    for (Student & iter : vect) {
        iter.print();
    }

    return 0;
}
```

#### 示例（容器嵌套）

```c++
#include <iostream>
#include "string"
#include "vector"
#include "algorithm"

using namespace std;

int main() {

    vector<vector<int>> vec;

    vector<int> v1;
    vector<int> v2;
    vector<int> v3;

    for (int i = 0; i < 4; i++) {
        v1.push_back(i + 10);
        v2.push_back(i + 20);
        v3.push_back(i + 30);
    }

    vec.push_back(v1);
    vec.push_back(v2);
    vec.push_back(v3);

    for (vector<int> &item: vec) {
        for (int &val: item) {
            cout << val << " ";
        }
        cout << endl;
    }

    return 0;
}
```

### 常用容器

#### string

string 内部封装了`char *`，`char *`是一个指针，维护的是一个`char *`型的容器。

##### 构造函数

```c++
string();                   // 初始化空的字符串
string(const char* s);      // 使用字符串 s 初始化
string(const string* str);  // 使用一个 string 对象初始化另一个 string 对象
string(int n, char c);      // 使用 n 个字符 c 初始化。
```

##### 赋值操作

在 string 初始化之后，重新赋值。

```c++
string& operator=(const char* s);
string& operator=(const string &s);
string& operator=(char c);
string& assign(const char* s);         // 把字符串 s 赋值给当前字符串
string& assign(const char* s, int n);  // 把字符串 s 前 n 个字符赋值给当前的字符串
string& assign(const string &s);       // 把字符串 s 赋值给当前字符串
string& assign(int n, char c);         // 用 n 个字符 c 赋值给当前字符串
```

##### 字符串拼接

在字符串末尾拼接字符串。

```c++
string& operator+=(const char* str);
string& operator+=(const char c);
string& operator+=(const string& str);
string& append(const char* s);        // 将字符串 s 拼接到当前字符串尾部
string& append(const char* s, int n); // 将字符串 s 前 n 个字符拼接到当前字符串尾部
string& append(const string &s);      // 等同于 operator+=(const string& str)
// 将字符串 s 从 pos 开始的 n 个字符串拼接到当前字符串尾部
string& append(const string &s, int pos, int n);
```

##### 查找&替换

- 查找：查找指定字符串是否存在。

- 替换：在指定的位置替换字符串。

```c++
// 查找str第一次出现位置,从pos开始查找
int find(const string& str, int pos = 0) const;

// 查找s第一次出现位置,从pos开始查找
int find(const char* s, int pos = 0) const;

// 从pos位置查找s的前n个字符第一次位置
int find(const char* s, int pos, int n) const;

// 查找字符c第一次出现位置
int find(const char c, int pos = 0) const;

// 查找str最后一次位置,从pos开始查找
int rfind(const string& str, int pos = npos) const;

// 查找s最后一次出现位置,从pos开始查找
int rfind(const char* s, int pos = npos) const;

// 从pos查找s的前n个字符最后一次位置
int rfind(const char* s, int pos, int n) const;

// 查找字符c最后一次出现位置
int rfind(const char c, int pos = 0) const;

// 替换从pos开始n个字符为字符串str
string& replace(int pos, int n, const string& str);

// 替换从pos开始的n个字符为字符串s
string& replace(int pos, int n,const char* s);      
```

- `find`查找是从左往右，而`rfind`是从右往左。

- `find`找到字符串后返回查找的第一个字符位置，找不到返回 -1。

- `replace`在替换时，要指定从哪个位置起，多少个字符，替换成什么样的字符串。

```c++
int main() {
    string str1 = "abcdefgde";
    str1.replace(1, 3, "1111");

    // 打印结果 str1 = a1111efgde
    cout << "str1 = " << str1 << endl;
    return 0;
}
```

##### 字符串比较

字符串之间的比较是按字符的 ASCII 码进行对比。`=`返回0，`>`返回1，`<`返回-1。

```c++
int compare(const string &s) const; // 与字符串s比较
int compare(const char *s) const;   // 与字符串s比较
```

##### 字符存取

单个字符的存取。

```c++
char& operator[](int n); // 通过[]方式取字符
char& at(int n);         // 通过at方法获取字符
```

**示例**：

```c++
int main() {

    string str = "hello world";

    // h e l l o   w o r l d
    for (int i = 0; i < str.size(); i++) {
        cout << str[i] << " ";
    }
    cout << endl;

    // h e l l o   w o r l d
    for (int i = 0; i < str.size(); i++) {
        cout << str.at(i) << " ";
    }
    cout << endl;


    // 字符修改
    str[0] = 'x';
    str.at(1) = 'x';
    
    // xxllo world
    cout << str << endl;

    return 0;
}
```

##### 插入&删除

对字符串进行插入和删除字符操作。

```c++
*string& insert(int pos, const char* s);     // 插入字符串
*string& insert(int pos, const string& str); // 插入字符串
*string& insert(int pos, int n, char c);     // 在指定位置插入n个字符c
*string& erase(int pos, int n = npos);       // 删除从Pos开始的n个字符 
```

插入和删除的起始下标都是从0开始。

##### 字符串截取

从字符串中获取字串。

```c++
string substr(int pos = 0, int n = npos) const; // 返回从pos开始的n个字符组成的字符串
```

示例：

```c++
int main() {

    string str = "abcdefg";
    string subStr = str.substr(1, 3);
    
    // subStr = bcd
    cout << "subStr = " << subStr << endl;

    return 0;
}
```

包含开始和结束区间。

#### vector

vector 数据结构和数组类型，也属于单端数组。不同之处在于数据是静态空间，而 vector 可以动态扩展。

vector 容器的迭代器是支持随机访问的迭代器。

##### 构造函数

```c++
vector<T> v;                 // 采用模板实现类实现，默认构造函数
vector(v.begin(), v.end());  // 将v[begin(), end())区间中的元素拷贝给本身。
vector(n, elem);             // 构造函数将 n 个 elem 拷贝给本身。
vector(const vector &vec);   // 拷贝构造函数。
```

##### 赋值操作

```c++
vector& operator=(const vector &vec); // 重载等号操作符
assign(beg, end);  // 将[beg, end)区间中的数据拷贝赋值给本身。
assign(n, elem);   // 将 n 个 elem 拷贝赋值给本身。
```

##### 容量

```c++
empty(); // 判断容器是否为空
 
capacity(); // 获取容器的容量
 
size(); // 获取容器中元素的个数
 
resize(int num); // 重新指定容器的长度为 num，若容器变长，则以默认值填充新位置。
// 如果容器变短，则末尾超出容器长度的元素被删除。
 
resize(int num, elem); // 重新指定容器的长度为 num，若容器变长，则以 elem 值填充新位置。
// 如果容器变短，则末尾超出容器长度的元素被删除
```

##### 插入&删除

```c++
push_back(ele);  // 尾部插入元素 ele
pop_back();  // 尾部删除最后一个元素
insert(const_iterator pos, ele);   // 迭代器指向位置 pos 插入元素 ele
insert(const_iterator pos, int count,ele);  // 迭代器指向位置 pos 插入 count 个元素 ele
erase(const_iterator pos);  // 删除迭代器指向的元素
erase(const_iterator start, const_iterator end);  // 删除迭代器从 start 到 end 之间的元素
clear();  // 删除容器中所有元素
```

##### 数据存取

```c++
at(int idx); // 返回索引 idx 处的数据
operator[int idx];  // 返回索引 idx 处的数据
front();     // 返回容器中第一个数据元素
back();      // 返回容器中最后一个数据元素
```

除了使用迭代器获取 vector 容器中元素，还可以使用索引下标和`at()`方法。

##### 互换容器元素

实现两个容器内元素进行互换。

```c++
swap(vec); // 将 vec 与本身的元素互换
```

示例：

```c++
#include <iostream>
#include "string"
#include "vector"
#include "algorithm"

using namespace std;

int main() {

    vector<int>v1; // 1 2 3
    vector<int>v2; // 4 5 6

    for (int i = 1; i < 4; i++) {
        v1.push_back(i);
        v2.push_back(i + 3);
    }

    v1.swap(v2);

    // v1: 4 5 6
    for (int & it : v1) {
        cout << it << " ";
    }

    cout << endl;

    // v2: 1 2 3
    for (int & it : v2) {
        cout << it << " ";
    }

    return 0;
}
```

##### 预留空间

减少 vector 在动态扩展容量时的扩展次数。

```c++
// 容器预留 len 个元素长度，预留位置不初始化，元素不可访问。
reserve(int len);
```

#### deque

双端数组，可以从头部进行插入删除操作。

deque 与 vector 区别：

1. vector 对于头部的插入删除效率低，数据量越大，效率越低。

2. deque 相对而言，对头部的插入删除速度会比 vector 快。

3. 但 vector 访问元素时的速度会比 deque 快。

deque内部工作原理:

- deque内部有个**中控器**，维护每段缓冲区中的内容，缓冲区中存放真实数据，中控器维护的是每个缓冲区的地址，使得使用 deque 时像一片连续的内存空间。

deque 容器的迭代器也是支持随机访问的。

##### 构造函数

```c++
deque<T> deqT;  // 默认构造形式
deque(beg, end);  // 构造函数将 [beg, end) 区间中的元素拷贝给本身。
deque(n, elem);  // 构造函数将 n 个 elem 拷贝给本身。
deque(const deque &deq);  // 拷贝构造函数
```

##### 赋值操作

```c++
deque& operator=(const deque &deq);  // 重载等号操作符
assign(beg, end);  // 将[beg, end)区间中的数据拷贝赋值给本身。
assign(n, elem);  // 将 n 个 elem 拷贝赋值给本身。
```

##### size

deque 没有容量的概念。

```c++
deque.empty();  // 判断容器是否为空
 
deque.size();  // 返回容器中元素的个数
 
deque.resize(num);  // 重新指定容器的长度为 num,若容器变长，则以默认值填充新位置。
// 如果容器变短，则末尾超出容器长度的元素被删除。
 
deque.resize(num, elem);  // 重新指定容器的长度为 num,若容器变长，则以elem值填充新位置。
// 如果容器变短，则末尾超出容器长度的元素被删除。
```

##### 插入&删除

```c++
// 两端插入操作：
 
push_back(elem); // 在容器尾部添加一个数据
push_front(elem); // 在容器头部插入一个数据
pop_back(); // 删除容器最后一个数据
pop_front(); // 删除容器第一个数据
 
// 指定位置操作：
 
insert(pos,elem); // 在pos位置插入一个elem元素的拷贝，返回新数据的位置。
insert(pos,n,elem); // 在pos位置插入n个elem数据，无返回值。
insert(pos,beg,end); // 在pos位置插入[beg,end)区间的数据，无返回值。
clear(); // 清空容器的所有数据
erase(beg,end); // 删除[beg,end)区间的数据，返回下一个数据的位置。
erase(pos); // 删除pos位置的数据，返回下一个数据的位置。
```

插入和删除提供的位置是迭代器。

##### 数据存取

```c++
at(int idx);// 返回索引 idx 处的数据
operator[idx]; // 返回索引 idx 处的数据
front();    // 返回容器中第一个数据元素
back();     // 返回容器中最后一个数据元素
```

##### 排序

利用算法实现对 deque 容器进行排序。

```c++
sort(iterator beg, iterator end); // 对 beg 和 end 区间内元素进行排序
```

sort 算法非常实用，使用时包含头文件 algorithm 即可。默认按照升序排序。

#### stack

先进后出（First In Last Out, FILO）。

栈中只有栈顶的元素才可以被访问，因此栈不允许有遍历行为。入站`push`，出站`pop`。

##### 常用接口

- 构造函数

```c++
stack<T> stk;  // stack 采用模板类实现，默认构造
stack(const stack &stk);  // 拷贝构造函数
```

- 赋值操作

```c++
stack& operator=(const stack &stk);  // 重载等号操作符
```

- 数据存取

```c++
push(elem);  // 向栈顶添加元素
pop();  // 从栈顶移除第一个元素
top();  // 返回栈顶元素
```

- size

```c++
empty(); // 判断堆栈是否为空
size();  // 返回栈的大小
```

#### queue

Queue是一种**先进先出**（First In First Out, FIFO）的数据结构。

队列容器允许从一端新增元素，从另一端移除元素。但队列中只有队头和队尾才可以被访问，因此队列不允许有遍历行为。入站`push`，出站`pop`。

##### 常用接口

- 构造函数

```c++
queue<T> que;  // queue采用模板类实现，默认构造
queue(const queue &que);  // 拷贝构造函数
```

- 赋值操作

```c++
queue& operator=(const queue &que); // 重载等号操作符
```

- 数据存取

```c++
push(elem); // 往队尾添加元素
pop();      // 从队头移除第一个元素
back();     // 返回最后一个元素
front();    // 返回第一个元素
```

- size

```c++
empty(); // 判断堆栈是否为空
size();  // 返回栈的大小
```

#### list

list 是一个双向循环链表。由于链表的存储方式并不是连续的内存空间，因此链表 list 中的迭代器只支持前移和后移，属于**双向迭代器**

list 的优点：

* 采用动态存储分配，不会造成内存浪费和溢出。
* 
* 链表执行插入和删除操作十分方便，修改指针即可，不需要移动大量元素。

list 的缺点：

* 链表灵活，但是空间(指针域) 和 时间（遍历）额外耗费较大。

List 有一个重要的性质，插入操作和删除操作都不会造成原有 list 迭代器的失效，这在 vector 是不成立的。

##### 构造函数

```c++
list<T> lst;  // list采用采用模板类实现,默认构造。
list(beg,end);  // 构造函数将[beg, end)区间中的元素拷贝给本身。
list(n,elem);  // 构造函数将 n 个 elem 拷贝给本身。
list(const list &lst);  // 拷贝构造函数。
```

##### 赋值&交换

```c++
assign(beg, end);  // 将[beg, end)区间中的数据拷贝赋值给本身。
assign(n, elem);  // 将 n 个 elem 拷贝赋值给本身。
list& operator=(const list &lst);  // 重载等号操作符
swap(lst);  // 将 lst 与本身的元素互换。
```

##### size

```c++
size();  // 返回容器中元素的个数
 
empty();  // 判断容器是否为空
 
resize(num);  // 重新指定容器的长度为 num，若容器变长，则以默认值填充新位置。
//如果容器变短，则末尾超出容器长度的元素被删除。
 
resize(num, elem);  // 重新指定容器的长度为 num，若容器变长，则以 elem 值填充新位置。
//如果容器变短，则末尾超出容器长度的元素被删除。
```

##### 插入&删除

```c++
push_back(elem); // 在容器尾部加入一个元素
pop_back();      // 删除容器中最后一个元素
push_front(elem);// 在容器开头插入一个元素
pop_front();     // 从容器开头移除第一个元素
insert(pos,elem);// 在 pos 位置插 elem 元素的拷贝，返回新数据的位置。
insert(pos,n,elem); // 在 pos 位置插入 n 个 elem 数据，无返回值。
insert(pos,beg,end);// 在 pos 位置插入[beg,end)区间的数据，无返回值。
clear();       // 移除容器的所有数据
erase(beg,end);// 删除[beg,end)区间的数据，返回下一个数据的位置。
erase(pos);    // 删除 pos 位置的数据，返回下一个数据的位置。
remove(elem);  // 删除容器中所有与 elem 值匹配的元素。
```

##### 数据存取

```c++
front(); // 返回第一个元素。
back();  // 返回最后一个元素。
```

list 容器中不可以通过索引或者`at()`方式访问数据。

##### 反转和排序

```c++
reverse(); // 反转链表
sort();    // 链表排序
```

对于自定义数据类型，必须要指定排序规则，否则编译器不知道如何进行排序。

#### set/multiset

`set/multiset`所有元素都会在插入时自动被排序。底层结构是用**二叉树**实现。

**set 和 multiset 区别**：

* set 不允许容器中有重复的元素。

* multiset 允许容器中有重复的元素。

##### 构造函数&赋值

```c++
// 构造：
set<T> st;  // 默认构造函数
set(const set &st); // 拷贝构造函数
 
// 赋值：
set& operator=(const set &st);  // 重载等号操作符
```

##### size&交换

```c++
size();   // 返回容器中元素的数目
empty();  // 判断容器是否为空
swap(st); // 交换两个集合容器元素
```

##### 插入&删除

```c++
insert(elem);// 在容器中插入元素。
clear();     // 清除所有元素
erase(pos);  // 删除 pos 迭代器所指的元素，返回下一个元素的迭代器。
erase(beg, end); // 删除区间[beg,end)的所有元素 ，返回下一个元素的迭代器。
erase(elem);     // 删除容器中值为 elem 的元素。
```

对于自定义数据类型，set 必须在构造函数时指定排序规则才可以插入数据。

##### 查找&统计

```c++
find(key);  //查找 key 是否存在,若存在，返回该键的元素的迭代器；若不存在，返回set.end();
count(key); // 统计 key 的元素个数，对于 set，结果为 0 或者 1
```

##### set&multiset区别

* set 不可以插入重复数据，而 multiset 可以。

* set 插入数据的同时会返回插入结果，表示插入是否成功。

* multiset 不会检测数据，因此可以插入重复数据。

如果不允许插入重复数据可以利用 set，如果需要插入重复数据利用 multiset。

##### pair对组创建

成对出现的数据，利用对组可以返回两个数据。

**两种创建方式：**

* `pair<type, type> p ( value1, value2 );`

* `pair<type, type> p = make_pair( value1, value2 );`

示例：

```c++
#include <iostream>
#include <deque>
#include "string"
#include "vector"
#include "algorithm"

using namespace std;

int main() {

    pair<string, int> p(string("Tommy"), 20);
    // name: Tommy age: 20
    cout << "name: " <<  p.first << " age: " << p.second << endl;

    pair<string, int> p2 = make_pair("Jack", 10);
    // name: Jack age: 10
    cout << "name: " << p2.first << " age: " << p2.second << endl;
    return 0;
}
```

##### 排序

利用仿函数，可以改变排序规则。

示例（自定义类型）：

```c++
#include <iostream>
#include "string"
#include "set"

using namespace std;

class Person {
public:
    Person(string name, int age) {
        this->m_Name = name;
        this->m_Age = age;
    }
    string m_Name;
    int m_Age;
};

class comparePerson {
public:
    bool operator()(const Person &p1, const Person &p2) const {
        // 按照年龄进行排序  降序
        return p1.m_Age > p2.m_Age;
    }
};

int main() {
    set<Person, comparePerson> s;

    Person p1("Tommy", 23);
    Person p2("Jack", 27);
    Person p3("Mark", 25);
    Person p4("Lucie", 21);

    s.insert(p1);
    s.insert(p2);
    s.insert(p3);
    s.insert(p4);

    for (const auto & it : s) {
        cout << "name: " << it.m_Name << " age: " << it.m_Age << endl;
    }

    return 0;
}
```

#### map/multimap

* map中所有元素都是pair。

* pair中第一个元素为key（键），起到索引作用，第二个元素为value（值）。

* 所有元素都会根据元素的键值自动排序。

底层结构是用二叉树实现。

map和multimap**区别**：

- map 不允许容器中有重复 key 值元素。

- multimap 允许容器中有重复 key 值元素。

##### 构造函数&赋值

```c++
map<T1, T2> mp;  // map 默认构造函数:
map(const map &mp); // 拷贝构造函数
map& operator=(const map &mp); // 重载等号操作符
```

##### size&交换

```c++
size();   // 返回容器中元素的数目
empty();  // 判断容器是否为空
swap(st); // 交换两个集合容器
```

##### 插入&删除

```c++
insert(elem);  // 在容器中插入元素。
clear();  // 清除所有元素
erase(pos);  // 删除 pos 迭代器所指的元素，返回下一个元素的迭代器。
erase(beg, end);  // 删除区间[beg,end)的所有元素 ，返回下一个元素的迭代器。
erase(key);  // 删除容器中值为 key 的元素。
```

示例：

```c++
#include <iostream>
#include "string"
#include "map"

using namespace std;

int main() {

    //插入
    map<int, int> m;

    //第一种插入方式
    m.insert(pair<int, int>(1, 10));

    //第二种插入方式
    m.insert(make_pair(2, 20));

    //第三种插入方式
    m.insert(map<int, int>::value_type(3, 30));

    //第四种插入方式
    m[4] = 40;

    //删除
    m.erase(m.begin());

    m.erase(3);

    //清空
    m.erase(m.begin(),m.end());
    m.clear();

    return 0;
}
```

##### 查找&统计

```c++
find(key);  // 查找 key 是否存在,若存在，返回该键的元素的迭代器；若不存在，返回 map.end();
count(key); // 统计 key 的元素个数，对于map，结果为0或者1
```

##### 排序

map 容器默认排序规则为按照 key 值进行从小到大排序。

对于自定义数据类型，map 必须要指定排序规则，同 set 容器。

示例：

```c++
#include <iostream>
#include "map"

using namespace std;

class MyCompare {
public:
    bool operator()(int v1, int v2) const {
        return v1 > v2;
    }
};

int main() {

    // 默认从小到大排序
    // 利用仿函数实现从大到小排序
    map<int, int, MyCompare> m;

    m.insert(make_pair(1, 10));
    m.insert(make_pair(2, 20));
    m.insert(make_pair(3, 30));
    m.insert(make_pair(4, 40));
    m.insert(make_pair(5, 50));

    for (auto & it : m) {
        cout << "key:" << it.first << " value:" << it.second << endl;
    }

    return 0;
}
```

### 函数对象

#### 函数对象

* 重载**函数调用操作符**的类，其对象常称为**函数对象**。

* **函数对象**使用重载的`()`时，行为类似函数调用，也叫**仿函数**。

函数对象(仿函数)是一个**类**，不是一个函数。

##### 函数对象使用

* 函数对象在使用时，可以像普通函数那样调用, 可以有参数，可以有返回值。

* 函数对象超出普通函数的概念，函数对象可以有自己的状态。

* 函数对象可以作为参数传递。

示例：

```c++
#include <iostream>

using namespace std;

// 1、函数对象在使用时，可以像普通函数那样调用, 可以有参数，可以有返回值
class MyAdd {
public :
    int operator()(int v1, int v2) {
        return v1 + v2;
    }
};

// 2、函数对象可以有自己的状态
class MyPrint {
public:
    MyPrint() {
        count = 0;
    }

    void operator()(string test) {
        cout << test << endl;
        count++; // 统计使用次数
    }

    int count; // 内部自己的状态
};

// 3、函数对象可以作为参数传递
void doPrint(MyPrint &mp, string test) {
    mp(test);
}

int main() {
    // 1、函数对象在使用时，可以像普通函数那样调用, 可以有参数，可以有返回值
    MyAdd myAdd;
    cout << myAdd(10, 10) << endl;

    // 2、函数对象可以有自己的状态
    MyPrint myPrint1;
    myPrint1("hello world");
    myPrint1("hello world");
    myPrint1("hello world");
    cout << "myPrint count: " << myPrint1.count << endl;

    // 3、函数对象可以作为参数传递
    MyPrint myPrint3;
    doPrint(myPrint3, "Hello C++");

    return 0;
}
```

#### 谓词

* 返回`bool`类型的仿函数称为**谓词**。

* 如果`operator()`接受一个参数，那么叫做一元谓词。

* 如果`operator()`接受两个参数，那么叫做二元谓词。

#### 内置函数对象

STL内置了一些函数对象，这些仿函数所产生的对象，用法和一般函数完全相同。

使用内建函数对象，需要引入头文件 `#include<functional>`。

```c++
// 算术仿函数
template<class T> T plus<T>       // 加法仿函数
template<class T> T minus<T>      // 减法仿函数
template<class T> T multiplies<T> // 乘法仿函数
template<class T> T divides<T>    // 除法仿函数
template<class T> T modulus<T>    // 取模仿函数
template<class T> T negate<T>     // 取反仿函数
        
// 关系仿函数
template<class T> bool equal_to<T>      // ==
template<class T> bool not_equal_to<T>  // !=
template<class T> bool greater<T>       // >
template<class T> bool greater_equal<T> // >=
template<class T> bool less<T>          // <
template<class T> bool less_equal<T>    // <=

// 逻辑仿函数
template<class T> bool logical_and<T> // &&
template<class T> bool logical_or<T>  // ||
template<class T> bool logical_not<T> // !
```

示例：

```c++
#include <iostream>
#include <functional>
using namespace std;

int main() {
    negate<int> n;
    cout << n(50) << endl; // -50
    
    plus<int> p;
    cout << p(10, 20) << endl; // 30
    return 0;
}
```

只演示取反和加法，剩下的在使用时参考头文件定义即可。

### 常用算法

算法主要是由头文件`<algorithm>` `<functional>` `<numeric>`组成。

- `<algorithm>`是所有 STL 头文件中最大的一个，范围涉及到比较、 交换、查找、遍历操作、复制、修改等等。

- `<numeric>`体积很小，只包括几个在序列上面进行简单数学运算的模板函数。

- `<functional>`定义了一些模板类,用以声明函数对象。

#### 常用遍历算法

```c++
/**
 * 遍历容器
 * beg   开始迭代器
 * end   结束迭代器
 * _func 函数或者函数对象
 */
for_each(iterator beg, iterator end, _func);

/**
 * 搬运容器到另一个容器中
 * beg1  源容器开始迭代器
 * end1  源容器结束迭代器
 * beg2  目标容器开始迭代器
 * _func 函数或者函数对象
 */
transform(iterator beg1, iterator end1, iterator beg2, _func);
```

示例：

```c++
#include <iostream>
#include <functional>
#include <algorithm>
#include <vector>

using namespace std;

// 普通函数
void print01(int val) {
    cout << val << " ";
}

// 函数对象
class print02 {
public:
    void operator()(int val) {
        cout << val << " ";
    }
};

class TransForm {
public:
    int operator()(int val) {
        return val;
    }
};

int main() {
    vector<int> v;
    for (int i = 0; i < 10; i++) {
        v.push_back(i);
    }

    // 遍历
    for_each(v.begin(), v.end(), print01);
    cout << endl;
    for_each(v.begin(), v.end(), print02());
    cout << endl;

    // 容器元素转移
    // 目标容器
    vector<int> vTarget;
    // 目标容器需要提前开辟空间
    vTarget.resize(v.size());

    transform(v.begin(), v.end(), vTarget.begin(), TransForm());

    for_each(vTarget.begin(), vTarget.end(), print02());

    return 0;
}
```

搬运的目标容器必须要提前开辟空间，否则无法正常搬运。

#### 常用查找算法

```c++
/**
 * 查找元素，按值查找元素，找到返回指定位置迭代器，找不到返回结束迭代器位置
 * beg   开始迭代器
 * end   结束迭代器
 * value 查找的元素
 */
find(iterator beg, iterator end, value);

/**
 * 按条件查找元素，按值查找元素，找到返回指定位置迭代器，找不到返回结束迭代器位置
 * beg   开始迭代器
 * end   结束迭代器
 * _Pred 函数或者谓词（返回bool类型的仿函数）
 */
find_if(iterator beg, iterator end, _Pred);

/**
 * 查找相邻重复元素，返回相邻元素的第一个位置的迭代器
 * beg 开始迭代器
 * end 结束迭代器
 */
adjacent_find(iterator beg, iterator end);

/**
 * 查找指定的元素，查到返回true，否则false
 * beg   开始迭代器
 * end   结束迭代器
 * value 查找的元素
 */
binary_search(iterator beg, iterator end, value);

/**
 * 统计元素出现次数
 * beg   开始迭代器
 * end   结束迭代器
 * value 统计的元素
 * 
 * 统计自定义数据类型时候，需要配合重载 `operator==`
 */
count(iterator beg, iterator end, value);

/**
 * 按条件统计元素出现次数
 * beg   开始迭代器
 * end   结束迭代器
 * _Pred 谓词
 */
count_if(iterator beg, iterator end, _Pred);
```

二分查找法查找效率很高，值得注意的是查找的容器中元素必须的有序序列。

示例：

```c++
#include <iostream>
#include <functional>
#include <algorithm>
#include <vector>

using namespace std;

class Student : public error_code {
public:
    Student(int age) {
        this->m_Age = age;
    }

    bool operator==(const Student &p) const {
        if (this->m_Age == p.m_Age) {
            return true;
        }
        return false;
    }

    int m_Age;
};

class CustomType1 {
public:
    bool operator()(Student &p)
    {
        return p.m_Age > 20;
    }
};

int main() {
    vector<Student> v;

    //创建数据
    Student p1(10);
    Student p2(20);
    Student p3(30);
    Student p4(40);

    v.push_back(p1);
    v.push_back(p2);
    v.push_back(p3);
    v.push_back(p4);

    // 1、查找元素
    vector<Student>::iterator it1 = find(v.begin(), v.end(), p2);
    if (it1 == v.end()) {
        cout << "No" << endl;
    } else {
        cout << "Yes" << endl;
    }

    // 2、按条件查找元素
    auto it2 = find_if(v.begin(), v.end(), CustomType1());
    if (it2 == v.end()) {
        cout << "No" << endl;
    } else {
        cout << "Yes" << endl;
    }

    // 3、查找相邻重复元素
    auto it3 = adjacent_find(v.begin(), v.end());
    if (it3 == v.end()) {
        cout << "No" << endl;
    } else {
        cout << "Yes" << endl;
    }

    // 4、查找指定的元素
    bool ret = binary_search(v.begin(), v.end(),p2);
    if (ret) {
        cout << "Yes" << endl;
    } else {
        cout << "No" << endl;
    }

    // 5、统计元素出现次数
    int num1 = count(v.begin(), v.end(), p2);
    cout << "count: " << num1 << endl;

    // 6、按条件统计元素出现次数
    int num2 = count_if(v.begin(), v.end(), CustomType1());
    cout << "count: " << num2 << endl;

    return 0;
}
```

#### 常用排序算法

```c++
/**
 * 对容器内元素进行排序
 * beg    开始迭代器
 * end    结束迭代器
 * _Pred  谓词
 * 
 * 默认从小到大排序
 */
sort(iterator beg, iterator end, _Pred);

/**
 * 指定范围内的元素随机调整次序
 * beg 开始迭代器
 * end 结束迭代器
 */
random_shuffle(iterator beg, iterator end);

/**
 * 容器元素合并，并存储到另一容器中
 * beg1  容器1开始迭代器
 * end1  容器1结束迭代器
 * beg2  容器2开始迭代器
 * end2  容器2结束迭代器
 * dest  目标容器开始迭代器
 * 
 * merge合并的两个容器必须的有序序列
 */
merge(iterator beg1, iterator end1, iterator beg2, iterator end2, iterator dest);

/**
 * 反转指定范围的元素
 * beg 开始迭代器
 * end 结束迭代器
 */
reverse(iterator beg, iterator end);
```

使用时无非就是配合重载`operator==`、`operator()`使用。就不再给出示例了，等到用到时再更行。

#### 常用拷贝和替换算法

```c++
/**
 * 将容器内指定范围的元素拷贝到另一个容器中
 * beg  开始迭代器
 * end  结束迭代器
 * dest 目标起始迭代器
 */
copy(iterator beg, iterator end, iterator dest);

/**
 * 将容器内指定范围的旧元素修改为新元素
 * beg      开始迭代器
 * end      结束迭代器
 * oldvalue 旧元素
 * newvalue 新元素
 */
replace(iterator beg, iterator end, oldvalue, newvalue);

/**
 * 容器内指定范围满足条件的元素替换为新元素
 * beg      开始迭代器
 * end      结束迭代器
 * _pred    谓词
 * newvalue 要替换的新元素
 */
replace_if(iterator beg, iterator end, _pred, newvalue);

/**
 * 互换两个容器的元素
 * c1 容器1
 * c2 容器2
 * 
 * swap交换容器时，注意交换的容器要同种类型
 */
swap(container c1, container c2);
```

#### 常用算术生成算法

```c++
/**
 * 计算容器元素累计总和
 * beg   开始迭代器
 * end   结束迭代器
 * value 起始值
 */
accumulate(iterator beg, iterator end, value);

/**
 * 向容器中填充元素
 * beg   开始迭代器
 * end   结束迭代器
 * value 填充的值
 * 
 * 利用fill可以将容器区间内元素填充为 指定的值
 */
fill(iterator beg, iterator end, value);
```

#### 常用集合算法

```c++
/**
 * 求两个容器的交集
 * beg1 容器1开始迭代器
 * end1 容器1结束迭代器
 * beg2 容器2开始迭代器
 * end2 容器2结束迭代器
 * dest 目标容器开始迭代器
 * 
 * 两个集合必须是有序序列，目标容器开辟空间需要从两个容器中取小值
 * set_intersection 返回值是交集中最后一个元素的位置
 */
set_intersection(iterator beg1, iterator end1, iterator beg2, iterator end2, iterator dest);

/**
 * 求两个容器的并集
 * beg1 容器1开始迭代器
 * end1 容器1结束迭代器
 * beg2 容器2开始迭代器
 * end2 容器2结束迭代器
 * dest 目标容器开始迭代器
 * 
 * 两个集合必须是有序序列，目标容器开辟空间需要两个容器相加
 * set_union 返回值是交集中最后一个元素的位置
 */
set_union(iterator beg1, iterator end1, iterator beg2, iterator end2, iterator dest);

/**
 * 求两个容器的差集
 * beg1 容器1开始迭代器
 * end1 容器1结束迭代器
 * beg2 容器2开始迭代器
 * end2 容器2结束迭代器
 * dest 目标容器开始迭代器
 * 
 * 目标容器开辟空间需要从两个容器取较大值
 * set_difference 返回值是差集中最后一个元素的位置
 */
set_difference(iterator beg1, iterator end1, iterator beg2, iterator end2, iterator dest);
```

对于工作中模块开发，以上内容够用了。后续有时间会持续更新，异常、多线程、网路编程等。