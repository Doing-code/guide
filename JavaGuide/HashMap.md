# HashMap

## 继承关系
![](../image/hashmap_继承关系.png)

## 常见成员变量
### DEFAULT_INITIAL_CAPACITY
默认初始化容量 
```java
/**
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
```
#### 问题1：MUST be a power of two？
```txt
    DEFAULT_INITIAL_CAPACITY 必须是2的幂的原因是为了实现高效的哈希算法和均匀的散列分布。
    在HashMap中，当元素被插入时，会通过哈希算法计算元素的哈希值，并根据哈希值将元素放置在数组中的一个特定位置，这个位置被称为桶（bucket）。桶的数量由DEFAULT_INITIAL_CAPACITY决定。
    当桶的数量是2的幂时，可以通过位运算替代取模操作，从而实现高效的哈希算法。具体来说，计算元素的哈希值后，使用哈希值与桶的数量进行位与运算 `hash & (capacity - 1)`，可以得到桶的索引，将元素放置在对应的桶中。
    另外，桶的数量为2的幂也有利于实现均匀的散列分布。当桶的数量是2的幂时，对于一个给定的哈希值，通过哈希与运算后得到的索引值的低位与高位信息都能够得到充分利用，减少了哈希冲突的可能性，提高了散列性能。
    2的幂其实就是1后面多个0，2的幂-1就是n个1。且当容量是2的幂时，`hash & (capacity - 1)`等同于 `hash%length`
    
按位与运算，都是1的时候，结果为1，否则为0；

容量是2的幂的情况：
    hash=3, capacity=8
    3 & (8-1)
    00000011    3
    00000111    7
    -------------
    00000011    3   数组索引
   
    hash=2, capacity=8
    3 & (8-1)
    00000010    2
    00000111    7
    -------------
    00000010    2   数组索引
    
容量不是2的幂的情况：
    hash=3, capacity=9
    3 & (9-1)
    00000011    3
    00001000    8
    -------------
    00000000    0
    
    hash=2, capacity=9
    3 & (9-1)
    00000010    2
    00001000    8
    -------------
    00000000    0
```
#### 问题2：指定的初始容量不是2的幂会怎么样？
```java
/**
    返回给定目标容量的2次幂大小。
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

使用`int n = cap - 1`的目的是为了计算出大于等于给定容量cap的最小2的幂。
假设 cap 已经是 2 的幂了，如果没有执行`int n = cap - 1`，那么最终容量会是 cap 的两倍，如果 cap 给定16，最后容量为32，其实 16 已经是大于等于给定容量cap的最小2的幂了，没必要给32浪费空间。

| 运算，都是0的时候结果为0，否则为1.

假定`cap`=10
int n = cap - 1; > 9
n |= n >>> 1;
00000000 00000000 00000000 00001001     9
00000000 00000000 00000000 00000100     9 >>> 1     无符号右移1位
---------------------------------------------
00000000 00000000 00000000 00001101     |= 或运算  13

n = 13
n |= n >>> 2;
00000000 00000000 00000000 00001101     13
00000000 00000000 00000000 00000011     n >>> 2
---------------------------------------------
00000000 00000000 00000000 00001111     |= 或运算  15

n = 15
n |= n >>> 4;
00000000 00000000 00000000 00001111     15
00000000 00000000 00000000 00000000     n >>> 4
---------------------------------------------
00000000 00000000 00000000 00001111     |= 或运算  15
... ...

执行完或运算，左后 return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
```

### DEFAULT_LOAD_FACTOR 
```java
/**
 * The load factor used when none specified in constructor.
 */
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 0.75被认为是一个在内存消耗和性能之间平衡的合理值。它提供了较好的性能和空间利用率，使得哈希表在元素数量达到当前容量的75%时触发扩容，从而维持了较低的冲突率和较高的查询效率。
```

### MAXIMUM_CAPACITY
```java
/**
 * The maximum capacity, used if a higher value is implicitly specified
 * by either of the constructors with arguments.
 * MUST be a power of two <= 1<<30.
 */
static final int MAXIMUM_CAPACITY = 1 << 30;
```
HashMap 集合最大容量，2的30次幂。

### TREEIFY_THRESHOLD & UNTREEIFY_THRESHOLD & MIN_TREEIFY_CAPACITY
当链表的值超过8则会优化为红黑树（1.8新增的变量）。
```java
/**
 * The bin count threshold for using a tree rather than list for a
 * bin.  Bins are converted to trees when adding an element to a
 * bin with at least this many nodes. The value must be greater
 * than 2 and should be at least 8 to mesh with assumptions in
 * tree removal about conversion back to plain bins upon
 * shrinkage.
 */
static final int TREEIFY_THRESHOLD = 8;
// 链表长度必须大于2，并且应该至少为8。

/**
 * The bin count threshold for untreeifying a (split) bin during a
 * resize operation. Should be less than TREEIFY_THRESHOLD, and at
 * most 6 to mesh with shrinkage detection under removal.
 */
static final int UNTREEIFY_THRESHOLD = 6;
// 当某个桶（bucket）中的链表长度达到阈值（默认为8）时，会将链表转换为红黑树。而当红黑树中的节点数量减少到6以下时，会将红黑树转换回链表。

/**
 * The smallest table capacity for which bins may be treeified.
 * (Otherwise the table is resized if too many nodes in a bin.)
 * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
 * between resizing and treeification thresholds.
 */
static final int MIN_TREEIFY_CAPACITY = 64;
// 链表转红黑树的条件阈值，数组容量最小长度64
```
选择8是因为符合泊松分布，超过8的时候，概率已经非常小了。空间和事件的权衡。

### table 存储元素的数组
```java
/**
 * The table, initialized on first use, and resized as
 * necessary. When allocated, length is always a power of two.
 * (We also tolerate length zero in some operations to allow
 * bootstrapping mechanics that are currently not needed.)
 */
transient Node<K,V>[] table;
```
- JDK1.7是 `Entry<K,V>[] table`，在构造函数中创建这个数组。
- JDK1.8是 `Node<K,V>[] table`，在第一次`put()`时创建这个数组。

都实现的是`Map.Entry<K,V>`接口。

### entrySet 存放元素数组的集合
```java
/**
 * Holds cached entrySet(). Note that AbstractMap fields are used
 * for keySet() and values().
 */
transient Set<Map.Entry<K,V>> entrySet;
```
用法：
```java
for (Map.Entry<Object, Object> objectObjectEntry : map.entrySet()) {
    objectObjectEntry.getKey();
    objectObjectEntry.getValue();
}
```

### size
```java
/**
 * The number of key-value mappings contained in this map.
 */
transient int size;
```
存放元素的个数，不是数组长度。

### modCount 记录HashMap的修改次数
```java
/**
 * The number of times this HashMap has been structurally modified
 * Structural modifications are those that change the number of mappings in
 * the HashMap or otherwise modify its internal structure (e.g.,
 * rehash).  This field is used to make iterators on Collection-views of
 * the HashMap fail-fast.  (See ConcurrentModificationException).
 */
transient int modCount;
```
每次扩容和put都会自增。该字段用于使HashMap的集合视图上的迭代器快速失败。（ConcurrentModificationException）并发修改异常。

### threshold 扩容临界值
```java
/**
 * The next size value at which to resize (capacity * load factor).
 *
 * @serial
 */
// (The javadoc description is true upon serialization.
// Additionally, if the table array has not been allocated, this
// field holds the initial array capacity, or zero signifying
// DEFAULT_INITIAL_CAPACITY.)
int threshold;
```
threshold = (cap * DEFAULT_LOAD_FACTOR)，当实际大小超过临界值时，会进行阔偶然那个

### loadFactor 加载因子
```java
/**
 * The load factor for the hash table.
 *
 * @serial
 */
final float loadFactor;
```
等同于`DEFAULT_LOAD_FACTOR`。

## put()
`put()`方法实现步骤大致如下：
1. 通过 `hash` 值计算出 `key` 映射到哪个桶（数组索引）。
2. 如果桶（数组索引）没有碰撞冲突，则直接插入。
3. 如果出现碰撞冲突了，则需要处理冲突。
    1. 如果该桶（数组索引）使用红黑树处理冲突，则调用红黑树的方法插入数据。
    2. 否则采用传统的链表方式插入。如果链表的长度达到临界值，则链表转为红黑树。
4. 如果桶（数组索引）存在重复的键，则将该键替换为新值 value。
5. 如果 size 大于阈值 threshold，则进行扩容。

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
```

```java
/*
参数说明：
    int hash：hash(key)，计算出来的 hash 值
    K key：要存储的 key
    V value：要存储的 value
    boolean onlyIfAbsent：为 true 表示不更改现有的值
    boolean evict：为 false 表示 table 为创建状态
*/
final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    
    // 第一次put则会进入这个if判断，在 resize() 中执行数组扩容（初始化数组）。
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
        
    /*
    数组索引处为空的情况
        通过 `hash` 值计算出 `key` 映射到哪个桶（数组索引）。(n - 1) & hash
        这里 n 表示数组长度
    */
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
        
    // 数组索引处不为空的情况，hash 碰撞冲突
    else {
        Node<K,V> e; K k;
        
        // 数组索引处只有一个元素，不存在链表，且正好新存入的 key 和 old key hash碰撞冲突，则将old value 替换为 new value。
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
            
        // 已经是红黑树的情况
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            
        // 还没有优化为红黑树
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    
                    // 链表处理 hash 碰撞冲突
                    p.next = newNode(hash, key, value, null);
                    
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                    
                        // 将链表优化为 红黑树
                        treeifyBin(tab, hash);
                    break;
                }
                
                // hash 相同、key 相同的情况
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                    
                /*
                解释 【if ((e = p.next) == null) ... p = e;】
                现有一个 HashMap，在数组索引3处，有一个链表：
                Node1.next = Node2, Node2.next = Node3 Node3.next = null 
                第一次循环的时候 p = Node1；在 (p = tab[i = (n - 1) & hash]) 的时候赋值
                    第一次 for 循环 ((e = p.next) == null)，(p.next)是Node2，将 p.next 赋值给 e，e = Node2，e 不等于 null；（p = e;）将 Node2 赋值给 p；
                    第二次 for 循环 ((e = p.next) == null)，(p.next)是Node3，将 p.next 赋值给 e，e = Node3，e 不等于 null；（p = e;）将 Node3 赋值给 p；
                    第三次 for 循环 ((e = p.next) == null)，(Node3.next)是nul，e 等于 null，在链表尾部插入新节点；
                p = e; 可以理解为是循环条件
                */
                p = e;
            }
        }
        
        // old value 替换为 new value
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    
    // 记录HashMap的修改次数
    ++modCount;
    
    // 扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

## treeifyBin()
```java
/**
 * Replaces all linked nodes in bin at index for given hash unless
 * table is too small, in which case resizes instead.
 */
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```
1. 根据哈希表中元素个数确定是扩容还是树化。
2. 如果是树化怎遍历链表，创建相同个数的树形节点，构成一个双向链表。
3. 所在数组索引的第一个元素指向新创建的树根节点，链表替换为树化。
4. `treeify()`左旋右旋调整节点为红黑树。

## resize()
```java
/**
 * Initializes or doubles table size.  If null, allocates in
 * accord with initial capacity target held in field threshold.
 * Otherwise, because we are using power-of-two expansion, the
 * elements from each bin must either stay at same index, or move
 * with a power of two offset in the new table.
 *
 * @return the table
 */
final Node<K,V>[] resize() {
    // 获取 old 数组
    Node<K,V>[] oldTab = table;
    
    // 获取 old 数组长度
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    
    // 假定这是第一次扩容，oldThr = threshold = 12
    int oldThr = threshold;
    int newCap, newThr = 0;
    
    // oldCap = 16
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        
        // oldCap << 1 == oldCap*2；newCap = 32
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            
            // 第一次扩容，newThr = 24
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    
    // 不是第一次扩容，则重新计算 newThr 扩容临界值
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    
    // 将 old 数组依次遍历到 new 数组中
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            
            // 不等于 null，说明 old 数组索引有数据
            if ((e = oldTab[j]) != null) {
            
                // 将原数组索引处设置 null，预FULL GC
                oldTab[j] = null;
                
                // 原数组索引处只有一个元素，不是链表，直接计算新索引添加即可
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                
                    // 如果是红黑树情况，节点拆分
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    
                // 链表
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        
                        /*
                        假定现在是链表：Node1.next = Node2, Node2.next = Node3 Node3.next = null ，最开始的时候是 e = oldTab[j] = Node1
                            第一次循环：(next = e.next) = (next = Node1.next)，next = Node2；（假设第一次循环计算的索引是原索引）
                                第一次循环 (loTail == null) 为 true
                                    (loHead = e) = (loHead = Node1)；
                                    (loTail = e) = (loTail = Node1)；
                                    ((e = next) = (e = Node2))；循环条件
                            第二次循环：(next = e.next) = (next = Node2.next)，next = Node3；（假设第二次循环计算的索引是 原索引+原数组长度）
                                第二次循环 (hiTail == null) 为 true
                                    (hiHead = e) = (hiHead = Node2)；
                                    (hiTail = e) = (hiTail = Node2)；
                                    ((e = next) = (e = Node3))；循环条件
                            第三次循环：(next = e.next) = (next = Node3.next)，next = null；（假设第三次循环计算的索引是是原索引），loTail在第一次循环赋值中为 Node1
                                第三次循环 (loTail == null) 为 false
                                (loTail.next = e) = (loHead.next = Node3)；
                                (loTail = e) = (loTail = Node3)
                                ((e = next) = (e = null))；结束循环
                        loHead：Node1 -> Node3
                        hiHead：Node2
                        */
                        next = e.next;
                        
                        // 高位是0，放置在原索引处
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        
                        // 高位是1，放置在(原索引 + 旧的容量)处
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    
                    // 在新数组索引处挂载链表，loHead，hiHead
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

## remove()
删除操作首先找到元素的位置，如果是链表旧遍历链表找到元素之后删除。如果是红黑树旧遍历红黑树找到元素之后删除，如果树小于6的时候要转链表。
```java
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}
```
```java
final Node<K,V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    
    // 判断数组不为空，根据 hash 计算出数组所在的索引
    if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
        
        // 如果数组索引处的节点就是要删除的key，则将 node 指向该节点
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
            
        // 第一个数组索引元素不是要删除的key，需要遍历链表
        else if ((e = p.next) != null) {
            
            // 遍历红黑树
            if (p instanceof TreeNode)
                node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                
            // 遍历链表
            else {
                do {
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    
                    // p 指向的是上一个节点（主要应用于 p.next = node.next; 取消挂载要删除的 key）
                    p = e;
                } while ((e = e.next) != null);
            }
        }
        if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
        
            // 红黑树情况
            if (node instanceof TreeNode)
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                
            // 数组索引处只有一个元素的情况，不存在链表，相当于 tab[index] = null
            else if (node == p)
                tab[index] = node.next;
            
            // 链表情况
            else
                p.next = node.next;
            ++modCount;
            --size;
            afterNodeRemoval(node);
            return node;
        }
    }
    return null;
}
```

## get()
```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```
```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    
    // 判断数组不为空，根据 hash 计算出数组所在的索引
    if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
        // first.hash == hash：always check first node
        
        // 判断数组元素是否相等，总是检查第一个元素
        if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
            
        // 如果不是第一个元素，判断是否有后续节点，链表情况
        if ((e = first.next) != null) {
        
            // 红黑树情况
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                
            // 链表情况
            do {
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```
`get()`大致步骤：
1. 通过 hash 值获取该 key 映射到的桶（数组索引）。
2. 如果桶（数组索引）上的 key 就是要查找的 key，直接返回。
3. 桶（数组索引）上的 key 不是要找的 key，则查看后续节点。
    1. 如果后续节点是红黑树节点，调用红黑树的方法根据 key 获取 value。
    2. 如果后续节点是链表节点，则通过遍历链表根据 key 获取 value。
    
## 附录
### hash()
```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```
1. 如果 key 为 null，则哈希值是0。这与 Hashtable 是有区别的，Hashtable 不允许 null 值，没有 key == null 的判断。所以会有空指针异常。
2. 如果 key 不等于 null，计算 key 的 hashCode 值赋值给 h，然后与 h 无符号右移16位后的二进制按位异或得到最终的 hash 值。

`^`按位异或运算：数字相同，结果为0，不同为1。

`&`按位与运算：都为1时结果为1，否则为0。

### 链表插入方式
在JDK 7的HashMap实现中，采用的是头插法。当发生哈希冲突时，新的节点会插入到链表的头部。

而JDK8中，是在链表的尾部进行插入。

### 遍历 HashMap 的方式
#### 分别遍历 key 和 value
```java
for (String key: map.keySet()) {
    System.out.println(key);
}

for (String key: map.values()) {
    System.out.println(key);
}
```

#### 迭代器遍历
```java
Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
while (iterator.hasNext()) {
    Map.Entry<String, String> entry = iterator.next();
    System.out.println(entry.getKey() + entry.getValue());
}
```

#### JDK8 forEach lambda
```java
map.forEach((k, v)-> System.out.println(k + v););
```

### jdk7&jdk8HashMap的差别
```txt
1. 底层数据结构：
    在JDK 7中，HashMap使用数组加链表的方式来存储数据，即使用拉链法解决哈希冲突。
    在JDK 8中，当链表长度达到一定阈值（默认为8）时，会将链表转换为红黑树，以提高查找、插入和删除的性能。
2. 扩容机制：
    在JDK 7中，HashMap的扩容机制是在数组长度达到阈值时进行扩容，即使负载因子小于1也会扩容。当进行扩容时，HashMap 会创建一个新的两倍大小的数组，并将原来数组中的元素重新插入到新数组中，重新计算它们在新数组中的位置。
    在JDK 8中，HashMap的扩容机制改进为在数组长度达到阈值并且链表长度超过一定阈值时进行扩容，且负载因子必须大于等于0.75。
3. 迭代顺序：
    在JDK 7中，HashMap的迭代顺序是不确定的，即不保证插入顺序。
    在JDK 8中，HashMap保持了插入顺序，即在迭代时按照元素插入的先后顺序进行遍历。
```





