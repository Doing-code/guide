# Golang8小时转职

> Go 官网文档（语法）：https://go.dev/ref/spec#For_statements
>
> 在开发时进行查阅

### Hello World

```go
package main // 程序的包名，main函数的文件包一定是属于 main 包的

// 导包
import "fmt"

// main 函数
func main() {
	fmt.Println("Hello World!")
}
```

### 变量的定义

- 声明单个变量

```go
package main // 程序的包名
import "fmt"

// main 函数
func main() {
	// 方式一   var 变量名 变量类型
	var a int
	fmt.Println(a)	// 0

	// 方式二
	var b int = 100
	fmt.Println(b)	// 100

	// 方式三，省去数据类型，通过值推断变量的数据类型
	var c = 100
	fmt.Println(c)	// 100

	// 方式四，常用，自动匹配数据类型
	d := 100
	fmt.Println(d)	// 100
}
```

方式四的变量定义只能定义在函数体内，无法定义为全局变量，而方式一、二、三可以定义全局变量（即定义在函数体外部的变量称为全局变量）。

- 声明多个变量

```go
package main // 程序的包名
import "fmt"

// main 函数
func main() {

    // 方式一
	var a, b = 100, 200
	fmt.Println(a, b)

    // 方式二
	var c, d = 100, "Hello"
	fmt.Println(c, d)
	
    // 方式三
	var(
		name string = "Jack"
		age int = 18
	)
	fmt.Println(name, age)
}
```

### 常量

常量的声明使用`const`关键字。`const 常量名 数据类型 = 常量值`。

`const`可以用来定义枚举类型。

```go
package main // 程序的包名
import "fmt"

// 定义枚举类型
const(
    BEIJING = 0
    SHANGHAI = 1
    SHENZHEN = 2
)

const(
	a, b = iota + 1, iota + 2	// iota = 0		a = 1	b = 2
	c, d	// iota = 1		c = 2	d = 3
	e, f	// iota = 2		e = 3	f = 4

	g, h = iota * 2, iota * 3	// iota = 3		g = 6	h = 9
	i, k	// iota = 4		i = 8	k = 12
)

const(
	m = iota * 10	// iota = 0		m = 0
	n	// iota = 1		n = 10
	z	// iota = 2		z = 20
)

const o int = 101

// main 函数
func main() {
	const s int = 100
	fmt.Println("s = ", s)

	fmt.Println("o = ", o)

	fmt.Println("a = ", a, ",b = ", b)
	fmt.Println("c = ", c, ",d = ", d)
	fmt.Println("e = ", e, ",f = ", f)
	fmt.Println("g = ", g, ",h = ", h)
	fmt.Println("i = ", i, ",k = ", k)

	fmt.Println("m = ", m)
	fmt.Println("n = ", n)
	fmt.Println("z = ", z)
}
```

常量具有只读属性，且关键字`iota`只能配合`const`使用。`iota`只有在`const`中进行累加效果。

### 函数

```go
package main // 程序的包名
import "fmt"

/*
		 函数名			 形参   参数类型         返回值
	func rectangularArea(length int, width int) int {}
*/
func func1(length int, width int) int {
	result := length * width
	return result
}

/*
	返回多个返回值，匿名
*/
func func2(length int, width int) (int, int) {
	return length, width
}

/*
	返回多个返回值，有名称的返回值，r1、r2属于形参
*/
func func3(length int, width int) (r1 int, r2 int) {
	r1 = length
	r2 = width
	return
}

/*
	返回多个返回值，如果多返回值类型相同，可以定义成如下形式
*/
func func4(length int, width int) (r1 , r2 int) {
	r1 = length
	r2 = width
	return
}

// main 函数
func main() {
	s1 := func1(3, 6)
	fmt.Println(s1) // 18

	r1, r2 := func2(2, 2)
	fmt.Println(r1, r2) // 2 2

	fmt.Println(func3(3, 3))    // 3 3

	fmt.Println(func4(4, 4))    // 4 4
}
```

形参的类型如果都相同，则可以有`func foo(length , width int) (r1 , r2 int) { }`。

### init函数和import

当一个包被导入时，如果该包还导入了其它的包，那么会先将其它包导入进来，然后再对这些包中的包级常量和变量进行初始化，接着执行init函数（如果有的话），依次类推。

等所有被导入的包都加载完毕了，就会开始对main包中的包级常量和变量进行初始化，然后执行main包中的init函数（如果存在的话），最后执行main函数。下图详细地解释了整个执行过程：

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650528765014-63d3d631-428e-4468-bc95-40206d8cd252.png)

如果一个包会被多个包同时导入，那么它只会被导入一次。

#### import匿名及别名导包方式

```go
package main // 程序的包名
import (
	"fmt"

    // 方式一
	_ "mylib1"
    // 方式二
	mylib2 "mylib2"
    // 方式三
	. "mylib3"
)

// main 函数
func main() {
	fmt.Println("Hello World")
}
```

方式一属于匿名导包，有些场景下需要执行某个包的 init() 函数，但是又不希望使用到该包。而Golang对于导包时，导入包但是不使用是编译不通过的，可以使用匿名导包来解决这种问题。让某个包的 init() 得到执行。

方式二属于别名方式导包。

方式三属于将该包的所有函数都导入到当前包，但不建议使用，会导致有同名函数产生歧义。

### defer

defer语句被用于延迟对一个函数的调用。可以把这类被defer语句调用的函数称为延迟函数。

defer作用：释放占用的资源、捕捉处理异常、输出日志等。

如果一个函数中有多个defer语句，它们会以LIFO（后进先出）的顺序执行。

示例-defer和return谁先执行：

```go
package main

import "fmt"

func main() {
	fmt.Println(f3())
}

func f1() int {
	fmt.Println("defer ... called")
	return 1
}

func f2() int {
	fmt.Println("return ... called")
	return 2
}

func f3() int {
	defer f1()
	return f2()
}
```

执行结果

```text
return ... called
defer ... called
2
```

示例-recover异常拦截：

```go
package main

import "fmt"

func Demo(i int) {
	//定义10个元素的数组
	var arr [10]int
	//错误拦截要在产生错误前设置
	defer func() {
		//设置recover拦截错误信息
		err := recover()
		//产生panic异常  打印错误信息
		if err != nil {
			fmt.Println(err)
		}
	}()
	//根据函数参数为数组元素赋值
	//如果i的值超过数组下标 会报错误：数组下标越界
	arr[i] = 10
}

func main() {
	Demo(10)
	//产生错误后 程序继续
	fmt.Println("程序继续执行...")
}
```

执行结果

```text
runtime error: index out of range [10] with length 10
程序继续执行...
```

### 指针

```go
package main // 程序的包名

import "fmt"

/*
    p 是一个指针类型
*/
func changeValue(p *int) {
	*p = 10
}

// main 函数
func main() {
    // 基本数据类型，变量村的就是值，即值类型
	var a int = 1

    /*
        这行代码可以看作是 p = &a，变量 p 存的是一个地址，这个地址指向的内存地址才是值
        &a 表示获取 a 的内存地址 （值类型 -> 指针类型）
        *p = 10 表示获取指针类型所指向的值。即将字面量 10 赋值给指针 p 指向的值 （指针类型 -> 值类型）
    */
	changeValue(&a)
	fmt.Println("a = ", a)  // a = 10
}
```

交换值的案例：

```go
package main

import "fmt"

func main() {
	/* 定义局部变量 */
	var a int = 100
	var b int = 200

	fmt.Printf("交换前，a 的值 : %d\n", a)
	fmt.Printf("交换前，b 的值 : %d\n", b)

	/* 调用 swap() 函数
	 * &a 指向 a 指针，a 变量的地址
	 * &b 指向 b 指针，b 变量的地址
	 */
	swap(&a, &b)

	fmt.Printf("交换后，a 的值 : %d\n", a)
	fmt.Printf("交换后，b 的值 : %d\n", b)
}

func swap(x *int, y *int) {
	var temp int
	temp = *x /* 保存 x 地址上的值 */
	*x = *y   /* 将 y 值赋给 x */
	*y = temp /* 将 temp 值赋给 y */
}
```

执行结果

```text
交换前，a 的值 : 100
交换前，b 的值 : 200
交换后，a 的值 : 200
交换后，b 的值 : 100
```

如`a := 5; var p *int = &a`，这里p是指针类型，存储的是 a 的地址值如`0x61fe14`。而`*p`可以用于寻址，拿着p存储的地址值去`解引用`，如果是一级指针，`*p`可以获取到值类型（即 5）。

而二级指针存储的应该是`一级指针的地址`，如`var pp **int = &p`，这里赋值的是一级指针的地址。`*pp`解一个引用得到一级指针地址，`**pp`解两个引用得到5。因为一级指针解引用就获取到值类型了。

### 切片 slice

在 Go 语言中切片是对数组的抽象。

Go 数组的长度不可改变，与数组相比切片的长度是不固定的，可以追加元素，在追加时可能使切片的容量增大。

并且默认情况下 Go 数组是值传递（副本），而切片是引用传递。

#### 定义切片

```go
// 方式一：声明一个未指定大小的数组来定义切片
var my_slice []int

// 方式二：使用make()函数来创建切片，第二个参数是 slice 的初始长度
var my_slice2 []int = make([]int, 5)

// 方式二也可以简写为：
my_slice2 := make([]int, 5)

/*
    capacity为可选参数，表示数组最大容量，
*/
make([]T, length, capacity)
```

len 是数组的长度并且也是切片的初始长度。

一个切片在未初始化之前默认为 `nil`（表现形式为`var my_slice []int`），长度为 0。

#### 切片初始化

- 方式一

```go
s := []int {1,2,3}
```

初始化切片，当前切片中 cap=len=3。

- 方式二

语法：`s1 := s[startIndex:endIndex]`。

```go
s := []int {1,2,3}

// 截取 numbers 切片的所有元素
number2 := numbers[:]

// 将s中从下标 startIndex 到 endIndex-1 的元素创建为一个新的切片
s1 := s[0:2]    // [1 2]
```

**s1是数组s的引用。即修改新切片的元素也会导致原始切片的元素被修改**。

```go
s1 := s[:endIndex]

s1 := s[startIndex:]
```

缺省endIndex时将表示一直到s的最后一个元素；缺省startIndex时将表示从s的第一个元素开始。

- 方式三

```go
s := make([]int, 5)
```

#### len() 和 cap() 函数

切片是可索引的，并且可以由 `len()` 方法获取长度。

切片提供了计算容量的方法 `cap()` 可以测量切片的最大容量。

```go
package main

import "fmt"

func main() {
	var numbers = make([]int,3,5)
	printSlice(numbers)
}

func printSlice(x []int){
	fmt.Printf("len=%d cap=%d slice=%v\n",len(x),cap(x),x)  // len=3 cap=5 slice=[0 0 0]
}
```

#### 切片截取

可以通过设置下限（默认下限为0）及上限（默认上限为len([]int)）来设置截取切片。

```go
package main

import "fmt"

func main() {
	// 创建切片
	numbers := []int{0, 1, 2, 3, 4, 5, 6, 7, 8}

	// 打印原始切片
	fmt.Println("numbers ==", numbers)

	// numbers[1:4] -> 截取从 1 到 4-1 范围内的元素形成新的切片
	fmt.Println("numbers[1:4] ==", numbers[1:4])

	// numbers[:3] -> 截取从 0 到 3-1 范围内的元素形成新的切片
	fmt.Println("numbers[:3] ==", numbers[:3])

	// numbers[4:] -> 截取从 4 到 元素个数-1 范围内的元素形成新的切片
	fmt.Println("numbers[4:] ==", numbers[4:])

	numbers1 := make([]int,0,5)
	printSlice(numbers1)

	// numbers[:2] -> 截取从 0 到 2-1 范围内的元素形成新的切片
	number2 := numbers[:2]
	printSlice(number2)

	// numbers[2:5] -> 截取从 2 到 5-1 范围内的元素形成新的切片
	number3 := numbers[2:5]
	printSlice(number3)

    // 遍历切片，idx 表示索引、rec 表示遍历的值，如果不需要 idx 可以使用 _ 进行匿名
	slice := []string{"a", "b"}
	for idx, rec := range slice {
		fmt.Println(idx, rec)
	}
}

func printSlice(x []int) {
	fmt.Printf("len=%d cap=%d slice=%v\n", len(x), cap(x), x)
}
```

执行结果

```text
numbers == [0 1 2 3 4 5 6 7 8]
numbers[1:4] == [1 2 3]
numbers[:3] == [0 1 2]
numbers[4:] == [4 5 6 7 8]
len=0 cap=5 slice=[]
len=2 cap=9 slice=[0 1]
len=3 cap=7 slice=[2 3 4]
0 a
1 b
```

#### append() 和 copy() 函数

如果想增加切片的容量，我们必须创建一个新的更大的切片并把原分片的内容都拷贝过来。默认情况下如果使用`append()`函数添加元素，当容量达到上限时会自动增加容量，默认是原容量的两倍。

切片可以理解为浅拷贝，共用底层数组的slice。而`copy()`函数可以理解为深拷贝，可以将底层数组的slice一起进行拷贝。

```go
package main

import "fmt"

func main() {
	var numbers []int
	printSlice(numbers)

	numbers = append(numbers, 0)
	printSlice(numbers)

	numbers = append(numbers, 2)
	printSlice(numbers)

	numbers = append(numbers, 2, 3, 4)
	printSlice(numbers)

	// 创建切片 numbers1，容量是原容量的两倍
	numbers1 := make([]int, len(numbers), cap(numbers) * 2)

	// 拷贝 numbers 的内容到 numbers1
	copy(numbers1, numbers)
	printSlice(numbers1)
}

func printSlice(x []int) {
	fmt.Printf("len=%d cap=%d slice=%v\n", len(x), cap(x), x)
}
```

执行结果

```text
len=0 cap=0 slice=[]
len=1 cap=1 slice=[0]
len=2 cap=2 slice=[0 2]
len=5 cap=6 slice=[0 2 2 3 4]
len=5 cap=12 slice=[0 2 2 3 4]
```

### map

> 引用传递

map的声明方式

```go
package main

import "fmt"

func main() {
	// 第一种声明		map[string]string -> map[key类型]value类型
	var map1 map[string]string

    // 在使用 map 前，需要先用 make() 给 map 分配内存地址
	map1 = make(map[string]string, 10)
	map1["Tom"] = "Beijing"
	map1["Jack"] = "Shanghai"
	map1["Tommy"] = "Shenzhen"
	fmt.Println(map1)	// map[Jack:Shanghai Tom:Beijing Tommy:Shenzhen]

	// 第二种声明
	map2 := make(map[string]string)
	map2["Tom"] = "Beijing"
	map2["Jack"] = "Shanghai"
	map2["Tommy"] = "Shenzhen"
	fmt.Println(map2)	// map[Jack:Shanghai Tom:Beijing Tommy:Shenzhen]

	// 第三种声明
	map3 := map[string]string {
		"Tom": "Beijing",
		"Jack": "Shanghai",
		"Tommy": "Shenzhen",
	}
	fmt.Println(map3)	// map[Jack:Shanghai Tom:Beijing Tommy:Shenzhen]
}
```

示例

```go
package main

import "fmt"

func main() {
	/*
		等同于 Java：Map<String, Map<String, String>>
	*/
	map1 := make(map[string]map[string]string)
	map1["Tom"] = make(map[string]string, 2)
	map1["Tom"]["id"] = "10001"
	map1["Tom"]["age"] = "18"

	map1["Jack"] = make(map[string]string, 2)
	map1["Jack"]["id"] = "10002"
	map1["Jack"]["age"] = "22"

	fmt.Println(map1)	// map[Jack:map[age:22 id:10002] Tom:map[age:18 id:10001]]

	// 查看指定元素是否在 map 中存在
	val, key := map1["Tom"]
	if key {
		fmt.Println(val)	// map[age:18 id:10001]
	}

    // 遍历
    for key, val := range map1 {
        fmt.Print(key)
        fmt.Print(" : ")
        fmt.Println(val)
    }

	// 修改元素
	map1["Tom"]["id"] = "10003"
	// 新增元素
	map1["Tom"]["username"] = "放羊的星星"
	// 删除元素
	delete(map1, "Jack")
	fmt.Println(map1)	// map[Tom:map[age:18 id:10003 username:放羊的星星]]
}
```

执行结果

```go
map[Jack:map[age:22 id:10002] Tom:map[age:18 id:10001]]
map[age:18 id:10001]
Tom : map[age:18 id:10001]
Jack : map[age:22 id:10002]
map[Tom:map[age:18 id:10003 username:放羊的星星]]
```

### interface

> 多态的表现形式

#### 示例

```go
package main

import (
	"fmt"
)

/*
	Animal 多态的表现形式，本质是一个指针

	只要实现类实现了父接口的所有方法，即建立了多态的关系
*/
type Animal interface {
	Sleep()
	GetType() string
}

type Cat struct {
	color string
}

func (cat *Cat) Sleep() {
	fmt.Println("猫睡觉")
}

func (cat *Cat) GetType() string {
	return  "Cat"
}

type Dog struct {
	color string
}

func (dog *Dog) Sleep() {
	fmt.Println("狗睡觉")
}

func (dog *Dog) GetType() string {
	return "Dog"
}

func show(animal Animal) {
	animal.Sleep()
	fmt.Println(animal.GetType())
}

func main() {
	show(&Cat{"Green"})
	show(&Dog{"Yellow"})
}
```

#### 万能类型&类型断言

Golang的语言中提供了断言的功能。golang中的所有程序都实现了`interface{}`的接口，这意味着，所有的类型如string,int,int64甚至是自定义的struct类型都就此拥有了`interface{}`的接口，**这种做法和java中的Object类型比较类似**。

那么在一个数据通过`func funcName(interface{})`的方式传进来的时候，也就意味着这个参数被自动的转为`interface{}`的类型。

如果断言失败，那么ok的值将会是false,但是如果断言成功ok的值将会是true,同时value将会得到所期待的正确的值。

```go
package main

import (
	"fmt"
)

func main() {
	funcName("abc")
	fmt.Println("===============")
	funcName(123)
	fmt.Println("===============")
	person := Person{"Jack"}
	funcName(person)
}

type Person struct {
	name string
}

func funcName(a interface{}) string {
	// 断言，a 传进来的类型是不是 string
	value, ok := a.(string)
	fmt.Println(ok)
	if !ok {
		fmt.Println("It is not ok for type string")
		return ""
	}
	fmt.Println("The value is", value)
	fmt.Printf("The value type is %T", value)
	fmt.Println()
	return value
}
```

搭配 switch 使用

```go
var t interface{}
t = functionOfSomeType()
switch t := t.(type) {
    default:
        fmt.Printf("unexpected type %T", t)       // %T prints whatever type t has
    case bool:
        fmt.Printf("boolean %t\n", t)             // t has type bool
    case int:
        fmt.Printf("integer %d\n", t)             // t has type int
    case *bool:
        fmt.Printf("pointer to boolean %t\n", *t) // t has type *bool
    case *int:
        fmt.Printf("pointer to integer %d\n", *t) // t has type *int
}
```

### 结构体

`type myint int`表示为定义一种新的的数据类型 myint，是 int 的一个别名。

```go
package main

import "fmt"

func main() {
	type myint int
	var a myint = 10
	fmt.Println(a)          // 10
	fmt.Printf("%T", a)     // main.myint
}
```

示例

```go
package main

import "fmt"

/*
	Person 定义一个结构体，类型为 Person
*/
type Person struct {
	name string
	age int
	gender string
}

/*
	结构体默认是值传递
*/
func changePerson1(person Person) {
	person.name = "Tom"
}

/*
	引用传递
*/
func changePerson2(person *Person) {
	person.name = "Jack"
}

func main() {
	var person Person
	person.name	= "Mark"
	person.age	= 18
	person.gender = "男"

	changePerson1(person)
	fmt.Println(person)

	changePerson2(&person)
	fmt.Println(person)
}
```

#### 结构体标签在json中的应用

> json、orm映射关系

```go
package main

import (
	"encoding/json"
	"fmt"
)

type User struct {
	Id   int    `json:"id"`
	Name string
	Age  int    `json:"age"`
}

func main() {
	user := User{10001, "Jack", 18}

    // 结构体 -> json
	jsonStr, err := json.Marshal(user)
	fmt.Printf("%s", jsonStr)
	if err != nil {
		fmt.Println(err)
	}

    fmt.Println()

    // json -> 结构体
	myUser := User{}
	err = json.Unmarshal(jsonStr, &myUser)
	if err != nil {
		return
	}
	fmt.Println(myUser)
}
```

执行结果

```text
{"id":10001,"Name":"Jack","age":18}
{10001 Jack 18}
```

### 反射

先来看看Golang关于类型设计的一些原则，变量包括（type, value）两部分，type 包括 `static type`和`concrete type`。

简单来说 `static type`是你在编码是看见的类型(如int、string)，`concrete type`是`runtime`系统看见的类型。

类型断言能否成功，取决于变量的`concrete type`，而不是`static type`. 因此，一个 reader 变量如果它的`concrete type`也实现了write方法的话，它也可以被类型断言为writer.

而反射就是建立在类型之上的，Golang的指定类型的变量的类型是静态的（也就是指定int、string这些的变量，它的type是static type），在创建变量的时候就已经确定。反射主要与Golang的interface类型相关（它的type是concrete type），只有interface类型才有反射一说。

在Golang的实现中，每个interface变量都有一个对应pair，pair中记录了实际变量的值和类型 `(value, type)`

**value是实际变量值**，**type是实际变量的类型**。一个interface{}类型的变量包含了2个指针，一个指针指向值的类型【对应concrete type】，另外一个指针指向实际的值【对应value】。

interface及其pair的存在，是Golang中实现反射的前提，理解了pair，就更容易理解反射。反射就是用来检测存储在接口变量内部(值value；类型concrete type) pair对的一种机制。

```go
package main

import (
	"fmt"
	"reflect"
)

func main() {
	var num float64 = 1.2345

	/*
		reflect.TypeOf() 是获取 pair 中的 type
		reflect.ValueOf() 是获取 pair 中的 value
	*/
	// TypeOf 用来动态获取输入参数接口中的值的类型，如果接口为空则返回nil
	fmt.Println("type is", reflect.TypeOf(num))	// type is float64

	// ValueOf 用来获取输入参数接口中的数据的值，如果接口为空则返回0
	// 类型为”relfect.Value”变量
	fmt.Println("value is", reflect.ValueOf(num))	// value is 1.2345

	fmt.Println("====================")

	pointer := reflect.ValueOf(&num)
	value := reflect.ValueOf(num)

	convertPointer := pointer.Interface().(*float64)
	convertValue := value.Interface().(float64)

	fmt.Println(convertPointer)	// 0xc00000a098
	fmt.Println(convertValue)	// 1.2345
}
```

**反射可以将“接口类型变量”转换为“反射类型对象”，反射类型指的是reflect.Type和reflect.Value这两种**

很多情况下，我们可能并不知道其具体类型，需要我们进行遍历探测其Filed来得知。

```go
package main

import (
	"fmt"
	"reflect"
)

type User struct {
	Id   int
	Name string
	Age  int
}

func (u User) ReflectCallFunc() {
	fmt.Println("Allen.Wu ReflectCallFunc")
}

func main() {
	user := User{1, "Allen.Wu", 25}
	DoFiledAndMethod(user)
}

/*
	通过接口来获取任意参数，然后一一揭晓
*/
func DoFiledAndMethod(input interface{}) {

	getType := reflect.TypeOf(input)
	fmt.Println("get Type is :", getType.Name())

	getValue := reflect.ValueOf(input)
	fmt.Println("get all Fields is:", getValue)

	// 获取方法字段
	// 1. 先获取interface的reflect.Type，然后通过NumField进行遍历
	// 2. 再通过reflect.Type的Field获取其Field
	// 3. 最后通过Field的Interface()得到对应的value
	for i := 0; i < getType.NumField(); i++ {
		field := getType.Field(i)
		value := getValue.Field(i).Interface()
		fmt.Printf("%s: %v = %v\n", field.Name, field.Type, value)
	}

	// 获取方法
	// 1. 先获取interface的reflect.Type，然后通过.NumMethod进行遍历
	for i := 0; i < getType.NumMethod(); i++ {
		m := getType.Method(i)
		fmt.Printf("%s: %v\n", m.Name, m.Type)
	}
}
```

执行结果

```text
get Type is : User
get all Fields is: {1 Allen.Wu 25}
Id: int = 1
Name: string = Allen.Wu
Age: int = 25
ReflectCallFunc: func(main.User)
```

#### 通过reflect.Value设置实际变量的值

reflect.Value是通过reflect.ValueOf(X)获得的，但只有当X是指针的时候，才可以通过reflec.Value修改实际变量X的值

```go
package main

import (
	"fmt"
	"reflect"
)

func main() {
	var num float64 = 1.2345
	fmt.Println("old value of pointer:", num)

	// 通过reflect.ValueOf获取num中的reflect.Value，注意，参数必须是指针才能修改其值
	pointer := reflect.ValueOf(&num)
    // 通过Elem获取原始值对应的对象
	newValue := pointer.Elem()

	fmt.Println("type of pointer:", newValue.Type())
    // 通过CanSet方法查询是否可以设置值
	fmt.Println("settability of pointer:", newValue.CanSet())

	// 重新赋值
	newValue.SetFloat(77)
	fmt.Println("new value of pointer:", num)

	// 如果reflect.ValueOf的参数不是指针，会如何？
	pointer = reflect.ValueOf(num)
	//newValue = pointer.Elem() // 如果非指针，这里直接panic，“panic: reflect: call of reflect.Value.Elem on float64 Value”
}
```

执行结果

```text
old value of pointer: 1.2345
type of pointer: float64
settability of pointer: true
new value of pointer: 77
```

#### 通过reflect.ValueOf来进行方法的调用

```go
package main

import (
	"fmt"
	"reflect"
)

type User struct {
	Id   int
	Name string
	Age  int
}

func (u User) ReflectCallFuncHasArgs(name string, age int) {
	fmt.Println("ReflectCallFuncHasArgs name: ", name, ", age:", age, "and origal User.Name:", u.Name)
}

func (u User) ReflectCallFuncNoArgs() {
	fmt.Println("ReflectCallFuncNoArgs")
}

// 如何通过反射来进行方法的调用？
// 本来可以用 u.ReflectCallFuncXXX 直接调用的，但是如果要通过反射，那么首先要将方法注册，也就是MethodByName，然后通过反射调动 mv.Call
func main() {
	user := User{10001, "Allen", 25}

	// 1. 要通过反射来调用起对应的方法，必须要先通过reflect.ValueOf(interface)来获取到reflect.Value，得到“反射类型对象”后才能做下一步处理
	getValue := reflect.ValueOf(user)

	// 一定要指定参数为正确的方法名
	// 2. 先看看带有参数的调用方法
	methodValue := getValue.MethodByName("ReflectCallFuncHasArgs")
	args := []reflect.Value{reflect.ValueOf("Jack"), reflect.ValueOf(30)}
	methodValue.Call(args)

	// 一定要指定参数为正确的方法名
	// 3. 再看看无参数的调用方法
	methodValue = getValue.MethodByName("ReflectCallFuncNoArgs")
	args = make([]reflect.Value, 0)
	methodValue.Call(args)
}
```

执行结果

```text
ReflectCallFuncHasArgs name:  Jack , age: 30 and origal User.Name: Allen
ReflectCallFuncNoArgs
```

#### 反射解析结构体Tag

```go
package main

import (
	"fmt"
	"reflect"
)

type resume struct {
	Name string `json:"name" doc:"我的名字"`
}

func findDoc(stru interface{}) map[string]string {
	doc := make(map[string]string)
    
    // 步骤一
	t := reflect.TypeOf(stru).Elem()

	for i := 0; i < t.NumField(); i++ {
        // 步骤二
        tagInfo := t.Field(i).Tag.Get("json")
        tagDoc := t.Field(i).Tag.Get("doc")
		doc[t.Field(i).Tag.Get("json")] = t.Field(i).Tag.Get("doc")
	}
	return doc
}

func main() {
	var stru resume
	doc := findDoc(&stru)
	fmt.Printf("name字段为：%s\n", doc["name"]) // name字段为：我的名字
}
```

#### 总结

Golang本身不支持模板，因此在以往需要使用模板的场景下往往就需要使用反射(reflect)来实现。

反射的基本原理: 

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650529719193-8d81ac41-106c-40a5-b6a1-e54bbc9748eb.png?x-oss-process=image%2Fresize%2Cw_750%2Climit_0)

### 面向对象

#### 封装

结构体名称、属性名、方法名若首字母大写则可以理解为`public`（对外访问），若首字母小写则可以立即为`private`（本包访问）。

```go
package main

import "fmt"

/*
	Person 定义一个结构体，类型为 Person
*/
type Person struct {
	name   string
	age    int
	gender string
}

/*
	GetName : 这里的 p 可以理解为 this 或 self，指代 Person 结构体本身；函数绑定结构体
	但和 this 指针还是有区别的，p 是调用该方法对象的一个副本（值拷贝）
*/
func (p Person) GetName() string {
	return p.name
}

/*
	SetName : 这里的 p 可以理解为 this 或 self，指代 Person 结构体本身；函数绑定结构体
*/
func (p *Person) SetName(name string) {
	p.name = name
}

func main() {
	// 创建对象
	person := Person{name: "Tommy", age: 18, gender: "男"}
	person.SetName("Jack")
	fmt.Println("GetName >> ", person.GetName())    // GetName >>  Jack
	fmt.Println(person)     // {Jack 18 男}
}
```

#### 继承

```go
package main

import "fmt"

/*
	Animal 父类
*/
type Animal struct {
	animalName string
	age        int
}

func (animal Animal) eat(food string) {
	fmt.Println("父类 ...")
}

/*
	Bird 子类
*/
type Bird struct {
	// 继承的表现形式
	Animal
	wing string
}

func (bird Bird) eat(food string) {
	fmt.Println("子类 ...")
}

func main() {
	animal := Animal{"父类", 0}
	animal.eat("")
	fmt.Println(animal)

	fmt.Println("============")

	// 创建对象
	bird := Bird{Animal{"鸟", 2}, "翅膀"}
	bird.eat("insect")
	fmt.Println(bird)
}
```

#### 多态

> 表现形式是 interface

### Go高级

#### goroutine

> GPM 模型

goroutine：协程，也被称为轻量级线程。

与传统的系统级线程和进程相比，协程最大的优势在于“轻量级”。可以轻松创建上万个而不会导致系统资源衰竭。而线程和进程通常很难超过1万个。这也是协程别称“轻量级线程”的原因。

一个线程中可以有任意多个协程，但某一时刻只能有一个协程在运行，多个协程分享该线程分配到的计算机资源。

在协程中，调用一个任务就像调用一个函数一样，消耗的系统资源最少！但能达到进程、线程并发相同的效果。

而 Go 在语言级别支持协程，叫goroutine。Go 语言标准库提供的所有系统调用操作（包括所有同步IO操作），都会出让CPU给其他goroutine。这让轻量级线程的切换管理不依赖于系统的线程和进程，也不需要依赖于CPU的核心数量。

> 异步系统调用 G 会和MP分离（G挂到NetPoller（网络轮询器））
>
> 同步系统调用 MG 会和P分离（P另寻M），当M从系统调用返回时，不会继续执行，而是将G放到run queue。

Go语言为并发编程而内置的上层API基于顺序通信进程模型CSP(communicating sequential processes)。这就意味着显式锁都是可以避免的，因为Go通过相对安全的通道发送和接受数据以实现同步，这大大地简化了并发程序的编写。

goroutine是Go语言并行设计的核心。Goroutine从量级上看很像协程，它比线程更小，十几个goroutine可能体现在底层就是五六个线程，Go语言内部帮你实现了这些goroutine之间的内存共享。执行goroutine只需极少的栈内存(大概是4~5KB)，当然会根据相应的数据伸缩。也正因为如此，可同时运行成千上万个并发任务。goroutine比thread更易用、更高效、更轻便。

一般情况下，一个普通计算机跑几十个线程就有点负载过大了，但是同样的机器却可以轻松地让成百上千个goroutine进行资源竞争。

##### goroutine的使用

只需在函数调⽤语句前添加 go 关键字，就可创建并发执⾏单元。

```go
package main

import (
	"fmt"
	"time"
)

func main() {
    // 创建一个 goroutine，启动一个任务
	go newTask()
	i := 0
	for {
		i++
		fmt.Printf("main goroutine: i = %d\n", i)
		time.Sleep(1 * time.Second) //延时1s
	}
}

func newTask() {
	i := 0
	for {
		i++
		fmt.Printf("new goroutine: i = %d\n", i)
		time.Sleep(1 * time.Second) //延时1s
	}
}
```

执行结果

```text
main goroutine: i = 1
new goroutine: i = 1
main goroutine: i = 2
new goroutine: i = 2
new goroutine: i = 3
main goroutine: i = 3
main goroutine: i = 4
new goroutine: i = 4
......
```

当 main() 函数执行完并退出后，已启动的 goroutine 也会自动退出

```go
package main

import (
	"fmt"
	"time"
)

func main() {
	go newTask()

	time.Sleep(3 * time.Second) //延时3s
	fmt.Println("main goroutine exit")
}

func newTask() {
	i := 0
	for {
		i++
		fmt.Printf("new goroutine: i = %d\n", i)
		time.Sleep(1 * time.Second) //延时1s
	}
}
```

执行结果

```text
new goroutine: i = 1
new goroutine: i = 2
new goroutine: i = 3
main goroutine exit
```

当调用`runtime.Goexit()`会立即终止当前 goroutine 执行，Go调度器会确保所有已注册 defer 延迟调用被执行。

```go
package main

import (
	"fmt"
	"runtime"
)

func main() {
	go func() {
		defer fmt.Println("A.defer")

		func() {
			defer fmt.Println("B.defer")
			runtime.Goexit() // 终止当前 goroutine, import "runtime"
			fmt.Println("B") // 不会执行
		}()

		fmt.Println("A") // 不会执行
	}()       //不要忘记()

	//死循环，目的不让主goroutine结束
	for {
	}
}
```

执行结果

```text
B.defer
A.defer
```

#### channel

channel 是Go语言中的一个核心类型，可以把它看成管道。goroutine通过它就可以发送或者接收数据，进行通信。

channel是一个数据类型，主要用来解决 goroutine 的同步问题以及 goroutine 之间数据共享（数据传递）的问题。

引⽤类型 channel可用于多个 goroutine 通讯。其内部实现了同步，确保并发安全。

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650606672440-8838ebc8-89bd-48af-9481-3e402623a5da.png?x-oss-process=image%2Fresize%2Cw_662%2Climit_0)

**chan**是创建channel所需使用的关键字。Type 代表指定channel收发数据的类型。

当我们复制一个channel或用于函数参数传递时，我们只是拷贝了一个channel引用，因此调用者和被调用者将引用同一个channel对象。和其它的引用类型一样，channel的零值也是nil。

```go
make(chan Type)  //等价于make(chan Type, 0)
make(chan Type, capacity)
```

当 参数capacity= 0 时，channel 是无缓冲阻塞读写的；当capacity > 0 时，channel 有缓冲、是非阻塞的，直到写满 capacity个元素才阻塞写入。

```go
package main

import "fmt"

func main() {
	// 无缓冲阻塞读写 channel，等价于 make(chan int, 0)
	w := make(chan int)
	// channel 有缓冲、是非阻塞的，直到写满 capacity个元素才阻塞写入。
	//w := make(chan int, 10)

	go func() {
		defer fmt.Println("子 goroutine end ...")
		fmt.Println("子 goroutine runtime ...")

		// channel <- value   发送value到channel
		// <-channel          接收并将其丢弃
		w <- 1024
	}()

	// r := <- channel        从channel中接收数据，并赋值给r
	// r, ok := <-channel     功能同上，同时检查通道是否已关闭或者是否为空
	r := <- w
	fmt.Println(r)
	fmt.Println("main goroutine end ...")
}
```

执行结果

```text
子 goroutine runtime ...
子 goroutine end ...
1024
main goroutine end ...
```

##### 无缓冲的channel

无缓冲的通道（unbuffered channel）是指在**接收前**没有能力保存任何数据值的通道。这种类型的通道要求发送goroutine和接收goroutine同时准备好，才能完成发送和接收操作。否则，通道会导致先执行发送或接收操作的 goroutine 阻塞等待。这种对通道进行发送和接收的交互行为本身就是同步的。

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650606704122-b43e4b58-5688-4c53-a82c-fcd8073ab6cf.png?x-oss-process=image%2Fresize%2Cw_750%2Climit_0)

```go
package main

import (
	"fmt"
	"time"
)

func main() {
	c := make(chan int, 0) //创建无缓冲的通道 c

	//内置函数 len 返回未被读取的缓冲元素数量，cap 返回缓冲区大小
	fmt.Printf("len(c)=%d, cap(c)=%d\n", len(c), cap(c))

	go func() {
		defer fmt.Println("子go程结束")

		for i := 0; i < 3; i++ {
			c <- i
			fmt.Printf("子go程正在运行[%d]: len(c)=%d, cap(c)=%d\n", i, len(c), cap(c))
		}
	}()

	time.Sleep(2 * time.Second) //延时2s

	for i := 0; i < 3; i++ {
		num := <-c //从c中接收数据，并赋值给num
		fmt.Println("num = ", num)
	}

	fmt.Println("main进程结束")
}
```

执行结果

```text
len(c)=0, cap(c)=0
num =  0
子go程正在运行[0]: len(c)=0, cap(c)=0
子go程正在运行[1]: len(c)=0, cap(c)=0
num =  1
num =  2
main进程结束
```

##### 有缓冲的channel

有缓冲的通道（buffered channel）是一种在被接收前能存储一个或者多个数据值的通道。**只有通道中没有要接收的值时，接收动作才会阻塞**。**只有通道没有可用缓冲区容纳被发送的值时，发送动作才会阻塞**。

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650606751449-c12b6106-ffbb-4f15-a83a-ff09d04754ee.png?x-oss-process=image%2Fresize%2Cw_750%2Climit_0)

```go
package main

import (
	"fmt"
	"time"
)

func main() {
	c := make(chan int, 3) //带缓冲的通道

	//内置函数 len 返回未被读取的缓冲元素数量， cap 返回缓冲区大小
	fmt.Printf("len(c)=%d, cap(c)=%d\n", len(c), cap(c))

	go func() {
		defer fmt.Println("子go程结束")

		for i := 0; i < 3; i++ {
			c <- i
			fmt.Printf("子go程正在运行[%d]: len(c)=%d, cap(c)=%d\n", i, len(c), cap(c))
		}
	}()

	time.Sleep(2 * time.Second) //延时2s
	for i := 0; i < 3; i++ {
		num := <-c //从c中接收数据，并赋值给num
		fmt.Println("num = ", num)
	}
	fmt.Println("main进程结束")
}
```

执行结果

```text
len(c)=0, cap(c)=3
子go程正在运行[0]: len(c)=1, cap(c)=3
子go程正在运行[1]: len(c)=2, cap(c)=3
子go程正在运行[2]: len(c)=3, cap(c)=3
子go程结束
num =  0
num =  1
num =  2
main进程结束
```

如果给定了一个缓冲区容量，通道就是异步的。只要缓冲区有未使用空间用于发送数据，或还包含可以接收的数据，那么其通信就会无阻塞地进行。

##### 关闭channel

channel不像文件一样需要经常去关闭，只有当你确实没有任何发送数据了，或者你想显式的结束range循环之类的，才去关闭channel；

**关闭channel后，无法向channel再发送数据**（引发 panic 错误后导致接收立即返回零值）；**但可以继续从channel接收数据**；

对于nil channel，无论收发都会被阻塞。

```go
package main

import (
	"fmt"
)

func main() {
	c := make(chan int)

	go func() {
		for i := 0; i < 5; i++ {
			c <- i
		}
		close(c)
	}()

	for data := range c {
		fmt.Println(data)
	}
	fmt.Println("Finished")
}
```

执行结果

```text
0
1
2
3
4
Finished
```

##### 单向&双向channel

默认情况下，通道 channel 是双向的，即既可以往channel发送数据，也可以从channel接收数据。

但是，我们经常见一个通道作为参数进行传递而只希望对方是单向使用的，要么只让它发送数据，要么只让它接收数据，这时候我们可以指定通道的方向。

![](https://cdn.nlark.com/yuque/0/2022/png/26269664/1650606808398-a8f28336-26c0-446f-b507-37805127b795.png)

单向channel的声明如下：

```go
var ch1 chan int       // ch1是一个正常的channel，是双向的

var ch2 chan<- float64 // ch2是单向channel，只用于写float64数据
var ch3 <-chan int     // ch3是单向channel，只用于读int数据
```

`chan <-` 表示数据进入管道，要把数据写进管道，对于调用者就是输出。`<- chan` 表示数据从管道出来，对于调用者就是得到管道的数据，当然就是输入。

可以将 channel 隐式转换为单向队列，只收或只发，但不能将单向 channel 转换为普通 channel。

```go
c := make(chan int, 3)
var send chan<- int = c // send-only
var recv <-chan int = c // receive-only
send <- 1
// <- send //invalid operation: <- send (receive from send-only type chan<- int)

<-recv
// recv <- 2 //invalid operation: recv <- 2 (send to receive-only type <-chan int)

// 不能将单向 channel 转换为普通 channel
d1 := (chan int)(send) // cannot convert send (type chan<- int) to type chan int
d2 := (chan int)(recv) // cannot convert recv (type <-chan int) to type chan int
```

常见的是生产者消费者模型

```go
package main

import (
	"fmt"
)

//   chan <- 只写
func counter(out chan<- int) {
	defer close(out)
	for i := 0; i < 5; i++ {
		out <- i // 如果对方不读 会阻塞
	}
}

//   <- chan  只读
func printer(in <-chan int) {
	for num := range in {
		fmt.Println(num)
	}
}

func main() {
	c := make(chan int) //   chan  读写

	go counter(c) // 生产者
	printer(c)    // 消费者

	fmt.Println("done")
}
```

执行结果

```text
0
1
2
3
4
done
```

#### Select

Go里面提供了一个关键字select，通过select可以监听channel上的数据流动。

有时候我们希望能够借助channel发送或接收数据，并避免因为发送或者接收导致的阻塞，尤其是当channel没有准备好写或者读时。select语句就可以实现这样的功能。

select的用法与switch语言非常类似，由select开始一个新的选择块，每个选择条件由case语句来描述。但与switch语句相比，select有比较多的限制，其中最大的一条限制就是每个case语句里必须是一个IO操作，大致的结构如下：

```go
select {
case <- chan1:
    // 如果chan1成功读到数据，则进行该case处理语句
case chan2 <- 1:
    // 如果成功向chan2写入数据，则进行该case处理语句
default:
    // 如果上面都没有成功，则进入default处理流程
}
```

如果没有default语句，那么select语句将被阻塞，直到至少有一个通信可以进行下去。

示例

```go
package main

import (
	"fmt"
)

func test(c, quit chan int) {
	x, y := 1, 1
	for {
		select {
		case c <- x:
			x, y = y, x+y
		case <-quit:
			fmt.Println("quit")
			return
		}
	}
}

func main() {
	c := make(chan int)
	quit := make(chan int)

	go func() {
		for i := 0; i < 6; i++ {
			fmt.Println(<-c)
		}
		quit <- 0
	}()

	test(c, quit)
}
```

执行结果

```text
1
1
2
3
5
8
quit
```

### Go模块管理

安装go后，需要配置GOROOT（go 的安装目录），还需要配置GOPATH（依赖包的目录，相当于 Maven 中的 Repository（MAVEN_HOME））

这个 path 的配置是一个全局的配置，配置之后依赖放到 pkg 文件中。

但也有缺点：

- 无法处理不同项目的版本依赖问题，因为共享的依赖和独立的依赖都放在同一个目录下。

- 使用的是`go get`获取依赖，无法区分版本。

而`go mod`类似于 Maven，其中`go.mod`文件类似于Maven中的`pom.xml`，但是依赖还是会下载到`gopath`中（Maven中的依赖也是如此，下载到MAVEN_HOME），可以理解为repository（仓库）管理jar包一样，可以管理不同版本的依赖。

### 总结

1. Go 适合的是网络 IO 密集型的场景，而非磁盘 IO 密集型。Go 对于磁盘 IO 密集型并不友好，根本原因在于：网络 socket 句柄和文件句柄的不同。网络 IO 能够用异步化的事件驱动的方式来管理，磁盘 IO 则不行。

> 文件 IO 的 read/write 都是同步的 IO（比如read 一下，没数据直接就卡住了）




























