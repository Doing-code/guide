# 数据结构和算法

## 数据结构

## 算法

### 动态规划

从已知子问题的解，推导出当前问题的解，且推导过程可以表达为一个数学公式。

找出递推公式，将当前问题分解成子问题，分阶段进行求解。求解过程中缓存子问题的解，避免重复计算。

> 动态规划是一种数学规划的建模思想, 但本身却只蕴含了一个和暴力枚举差不多的基本算法.

#### 斐波那契

```java
/**
 * <pre>
 * 前两项的和等于第三项（利用额外的参数缓存前两项的和，避免了像递归重复计算）
 *    F0    F1    F2    F3    F4    F5    F6    F7    F8    F9    F10    F11    F12    F13
 *     0     1     1     2     3     5     8    13    21    34     55     89    144    233
 * @param n 项
 * @return
 */
public int fibonacci(int n) {
    if (n == 0) {
        return n;
    }

    if (n == 1) {
        return n;
    }

    int a = 0;
    int b = 1;
    for (int i = 2; i <= n; i++) {
        int c = a + b;
        a = b;
        b = c;
    }
    return b;
}
```

#### 最短路径

```java
/**
 * <pre>
 * 从起点到任一点的最短距离
 *
 * 初始化时：
 *    当 v == 起点时，f(v) = 0
 *    当 v != 起点时，f(v) = ∞
 *
 * 计算两两点之间最短距离的递归公式：
 *    新最短距离   旧最短距离  v到任一点的距离
 *    f(to) = min(f(to), f(from) + from.weight)
 *
 *    dp[e.to] = Integer.min(dp[e.to], dp[e.from] + e.weight)
 *
 * 假定有 v1、v2、v3、v4、v5、v6 6个节点，循环节点-1次即可
 * @return 起点到任一点的最短距离数组
 */
public int[] optimalPath() {
    List<Edge> edges = Arrays.asList(
            new Edge(6, 5, 9),
            new Edge(4, 5, 6),
            new Edge(1, 6, 14),
            new Edge(3, 6, 2),
            new Edge(3, 4, 11),
            new Edge(2, 4, 15),
            new Edge(1, 3, 9),
            new Edge(1, 2, 7)
    );
    int[] dp = new int[7];
    for (int i = 2; i < dp.length; i++) {
        dp[i] = Integer.MAX_VALUE;
    }
    for (int i = 0; i < 5; i++) {
        for (Edge e : edges) {
            if (dp[e.from] != Integer.MAX_VALUE) {
                dp[e.to] = Integer.min(dp[e.to], dp[e.from] + e.weight);
            }
        }
    }
    return dp;
}

static class Edge {
    public Edge(Integer from, Integer to, Integer weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    /**当前边的起点*/
    private final Integer from;
    /**当前边的终点*/
    private final Integer to;
    /**当前边的起点到该边终点的距离*/
    private final Integer weight;
}
```

#### 不同路径

```java
/**
 * <pre>
 * 假定3行7列：从 dp[0][0] 到 dp[2][6] 的路线共有多少种走法，只允许向右和向下。起点和终点用'▲'标识
 *
 * ▲   0   0   0   0   0   0
 * 0   0   0   0   0   0   0
 * 0   0   0   0   0   0   ▲
 *
 * 第一行和第一列的路径都是1
 * ▲   1   1   1   1   1   1
 * 1   0   0   0   0   0   0
 * 1   0   0   0   0   0   ▲
 *
 * 而规律就是路径之和 = 上边和左边的路径之和
 * ▲   1   1   1    1    1   1
 * 1   2   3   4    5    6   7
 * 1   3   6   10   15   21  ▲(28)
 *
 * @param line 行
 * @param column 列
 * @return
 */
public int uniquePaths(int line, int column) {
    int[] dp = new int[column];
    for (int i = 0; i < column; i++) {
        dp[i] = 1;
    }

    for (int i = 1; i < line; i++) {
        for (int j = 1; j < column; j++) {
            dp[j] = dp[j] + dp[j - 1];
        }
    }

    return dp[column - 1];
}
```

#### 01背包问题

```java
/**
 * <pre>
 1. 假定背包容量为10g，要求取走不超过背包容量的物品
 2. 每次可以不拿或全拿，但是每件物品只能拿一次，问最高价值是多少?

    编号   重量(g)   价值(元)
    1      4        1600     黄金(A)
    2      8        2400     红宝石(R)
    3      5        30       白银(S)
    4      1        10000    钻石(D)

    0   1   2   3   4   5   6   7   8   9   10
 1  0   0   0   0   A   A   A   A   A   A   A       黄金
 2  0   0   0   0   A   A   A   A   R   R   R       红宝石
 3  0   0   0   0   A   A   A   A   R   R   R       白银
 4  0   D   D   D   D   DA  DA  DA  DA  DR  DR      钻石

 其递推公式为：
 if (装不下) {
    dp[i][j] = dp[i-1][j]
 } else { 装得下
    // 当前背包最大价值 = max(上一次最大价值, 当前物品价值 + 放入当前物品之前的背包容量最大价值)
    dp[i][j] = max(dp[i-1][j], item.value + dp[i-1][j-item.weight])
 }
 *
 * @param items    物品数组
 * @param capacity 容量
 * @return 最大价值
 */
public int select(Item[] items, int capacity) {
    int [] dp = new int[capacity + 1];
    Item item0 = items[0];
    // 特殊处理第0行数据
    for (int j = 0; j < capacity + 1; j++) {
        // 背包容量装得下第0行物品
        if (j >= item0.weight) {
            dp[j] = item0.value;
        }
    }

    for (int i = 1; i < items.length; i++) {
        for (int j = capacity; j > 0; j--) {
            // 装得下
            if (j >= items[i].weight) {
                dp[j] = Integer.max(dp[j], items[i].value + dp[j - items[i].weight]);
            }
        }
    }
    return dp[capacity];
}

/**物品*/
static class Item {
    /**编号*/
    private int index;
    /**物品名称*/
    private String name;
    /**重量(g)*/
    private int weight;
    /**价值*/
    private int value;

    public Item(int index, String name, int weight, int value) {
        this.index = index;
        this.name = name;
        this.weight = weight;
        this.value = value;
    }
}
```

#### 完全背包问题

`完全背包问题` 和 `01背包问题` 两者大同小异，唯一的区别就是：

- `01背包问题` 每件物品只有一个，即只能取一次。

- 而 `完全背包问题` 每件物品可以重复取。

```java
/**
 * <pre>
 1. 假定背包容量为10g，要求取走不超过背包容量的物品
 2. 每次可以不拿或全拿，每件物品可以重复取，问最高价值是多少?

    编号   重量(g)   价值(元)
    1      2        3     青铜(c)
    2      3        4     白银(s)
    3      4        5     黄金(a)

    0   1   2   3   4    5    6
 1  0   0   c   c   cc   cc   ccc      青铜
 2  0   0   c   s   s    sc   ss       白银
 3  0   0   c   s   a    a    ac       黄金

 其递推公式为：
 if (装不下) {
    dp[i][j] = dp[i-1][j]
 } else { 装得下
    // 当前背包最大价值 = max(上一次最大价值, 当前物品价值 + 放入当前物品之前的背包容量最大价值)
    dp[i][j] = max(dp[i-1][j], item.value + dp[i-1][j-item.weight])
 }
 *
 * @param items    物品数组
 * @param capacity 容量
 * @return 最大价值
 */
public int select(Item[] items, int capacity) {
    int [] dp = new int[capacity + 1];
    Item item0 = items[0];
    // 特殊处理第0行数据
    for (int j = 0; j < capacity + 1; j++) {
        // 背包容量装得下第0行物品
        if (j >= item0.weight) {
            dp[j] = dp[j - item0.weight] + item0.value;
        }
    }

    for (int i = 1; i < items.length; i++) {
        for (int j = capacity; j > 0; j--) {
            // 装得下
            if (j >= items[i].weight) {
                dp[j] = Integer.max(dp[j], items[i].value + dp[j - items[i].weight]);
            }
        }
    }
    return dp[capacity];
}

static class Item {
    /**编号*/
    private int index;
    /**物品名称*/
    private String name;
    /**重量(g)*/
    private int weight;
    /**价值*/
    private int value;

    public Item(int index, String name, int weight, int value) {
        this.index = index;
        this.name = name;
        this.weight = weight;
        this.value = value;
    }
}
```

#### 零钱兑换问题--最少组成

```java
/**
<pre>
  面值   0   1   2   3     4     5
    1   0   1   11  111   1111  11111
    2   0   1   2   21    22    221
    5   0   1   2   21    22    1

 总金额    -   类比为背包容量
 硬币面值  -   类比为物品重量
 硬币个数  -   类比为物品价值，固定为1，（因为是求最少组成总金额的硬币数量）

 递推公式如下：
 if (装得下) {
     上次硬币组成个数,   剩余容量能装下的最小个数+1  （个数固定为1）
    dp[j] = Integer.min(dp[j], dp[j - coins[i]] + 1);
    // dp[i][j] = max(dp[i-1][j], dp[i-1][j-item.weight] + 1)
 } else {
    保留上次个数不变
    dp[i][j] = dp[i-1][j]
 }

 * @param coins  面值种类
 * @param amount 总金额
 * @return 最少组成总金额的硬币数量
 */
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    // 特殊处理第0行数据，可以用Arrays.fill进行优化，赋初始值
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;

    for (int coin : coins) {
        for (int j = coin; j < amount + 1; j++) {
            dp[j] = Integer.min(dp[j], dp[j - coin] + 1);
        }
    }
    return dp[amount] < amount ? dp[amount] : -1;
}
```

#### 零钱兑换问题--多少种组合

```java
/**
 <pre>
 面值     0       1       2       3       4       5   （总金额 - 类比为背包容量）
   1     1       1       11      111     1111    11111

   2     1       1       11      111     1111    11111
                         2       21      211     2111
                                         22      221

   5     1       1       11      111     1111    11111
                         2       21      211     2111
                                         22      221
                                                 5
    i: 币种，j: 列（总金额）
                                                   上一次组合种类：1 + 剩余容量的组合种类
      eg: 总金额为3，面值有1、2，共有多少种组合： dp[i][j] = dp[i-1][j] + dp[i][j-coin]

 if (放得下) {
    dp[i][j] = dp[i-1][j] + dp[i][j-coin]
 } else { 放不下
    dp[i][j] = dp[i-1][j]
 }

 * @param coins     面值种类
 * @param amount    总金额
 * @return 多少种组合
 */
public int change(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    dp[0] = 1;
    for (int j = 1; j < amount + 1; j++) {
        if (j >= coins[0]) {
            dp[j] = dp[j - coins[0]];
        }
    }
    for (int i = 1; i < coins.length; i++) {
        for (int j = 1; j < amount + 1; j++) {
            // 容量放得下
            if (j >= coins[i]) {
                dp[j] = dp[j] + dp[j - coins[i]];
            }
        }
    }
    return dp[amount];
}
```

#### 钢条切割问题

```java
/**
 <pre>
 钢条切割问题：怎么个切法能够构成最大价值.

 钢条长度： 0    1   2   3   4   5   6   7   8   9   10
 钢条价值： 0    1   5   8   9   10  17  17  20  24  30

 假定钢条长3m：
    0   1   2   3   4
 1      1   11  111 1111
 价值：  1   2   3   4

 2      1   11  111 1111
            2   21  211
                    22
 价值：  1   5   6   10

 3      1   11  111 1111
            2   21  211
                3   22
                    31
 ... ...

 if (放得下) {
                 上一次最大价值 ,   当前物品价值 + 剩余容量能装下的最大价值
    dp[i][j] = max(dp[i-1][j], 当前物品价值 + dp[i][j-物品重量])
 } else { 放不下
    dp[i][j] = dp[i-1][j]
 }

 * @param values 价值数组 - 钢条长度(物品重量)
 *               钢条长度： 0    1   2   3   4   5   6   7   8   9   10   （索引）
 *               钢条价值： 0    1   5   8   9   10  17  17  20  24  30
 * @param n 钢条长度
 * @return 最大价值
 */
public int cut(int[] values, int n) {
    int[] dp = new int[n + 1];
    for (int i = 1; i < values.length; i++) {
        for (int j = 1; j < n + 1; j++) {
            if (j >= i) {
                dp[j] = Integer.max(dp[j], values[i] + dp[j - i]);
            }
        }
    }
    return dp[n];
}
```

#### 最长公共子串

```java
/**

 最长公共字串：abcdeoma  opcdeima  则两个字符串的最长公共字串为3，（连续的子串）

    b   c   d   e   i   m   a
 c  0   1   0   0   0   0   0
 d  0   0   2   0   0   0   0
 e  0   0   0   3   0   0   0
 o  0   0   0   0   0   0   0
 m  0   0   0   0   0   1   0
 a  0   0   0   0   0   0   2

 if (字符相同) {
    dp[i][j] = dp[i-1][j-1] + 1
 } else {
    dp[i][j] = 0
 }

 * @param a 串A
 * @param b 串B
 * @return 最长公共字串
 */
public int lcs(String a, String b) {
    int[][] dp = new int[b.length()][a.length()];
    int max = 0;
    for (int i = 0; i < b.length(); i++) {
        for (int j = 0; j < a.length(); j++) {
            if (a.charAt(j) == b.charAt(i)) {
                // 特殊处理0行或0列，如果为0行或0列，则dp[i - 1][j - 1] 索引为负数
                if (i == 0 || j == 0) {
                    dp[i][j] = 1;
                } else {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                }
                max = Integer.max(max, dp[i][j]);
            } else {
                dp[i][j] = 0;
            }
        }
    }
    return max;
}
```

#### 最长公共子序列

