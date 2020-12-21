
本地缓存组件

## 特性
### 高效读写
Guava Cache借鉴了ConcurrentHashMap的实现原理(基于1.7版本的实现，即没有使用红黑树)，使用了桶+链表的方式来实现。
### 自动清除数据
1.基于容量和总量：缓存项数量和容量大小限制，二者只能设置一个。设置数量时，每项的权重为1。*写入*或*重载*时从RecentQueue驱逐最早访问的数据，直到总量小于max值。
1.基于写入或读取时间：维护accessQueue和writeQueue两个segment内队列（非线程安全）和AccessQueue全局队列（线程安全）。按照access和write时间排序倒排序，*读取*或*写入*时，移除过期数据。
1.基于内存回收：通过使用弱引用的键、或弱引用的值、或软引用的值，Guava Cache可以把缓存设置为允许垃圾回收：
```
CacheBuilder.weakKeys()：使用弱引用存储键。当键没有其它（强或软）引用时，缓存项可以被垃圾回收。（随时可能被回收）
CacheBuilder.weakValues()：使用弱引用存储值。当值没有其它（强或软）引用时，缓存项可以被垃圾回收。（随时可能被回收）
CacheBuilder.softValues()：使用软引用存储值。软引用只有在响应内存需要时，才按照全局最近最少使用的顺序回收。（内存不足时可能被回收）
```
### LRU回收算法
LinkedHashMap实现LRU算法：通过HashMap来存储元素，从而解决了读写元素的时间复杂度的问题。我们都知道HashMap的时间复杂度为O(1)。其通过双向链表又解决了查找最少使用元素的问题，其时间复杂度仍然为O(1)。
Guava Cache中的LRU算法：通过ConcurrentHashMap+双向链表实现的。
```
在Guava Cache的LRU实现中，它的双向链表并不是全局的(即这个那个Guava Cache只有一个)。而是每个Segment(ConcurrentHashMap中的概念)中都有。其中一共涉及到三个Queue其中包括：AccessQueue和WriteQueue，以及RecentQueue。其中AccessQueue和WriteQueue就是双向链表；而RecentQueue才是真正的Queue，它就是ConcurrentLinkedQueue。

已经存在AccessQueue了，为什么还需要RecentQueue？
AccessQueue是非线程安全，一定是在获取了segment锁之后才能移动数据到AccessQueue头部。如果没有RecentQueue，每次读访问数据时，都需要获取segment锁，显然效率非常低。
有了RecentQueue之后，所有读访问数据都会添加到RecentQueue，RecentQueue是CocurrentLinkedQueue实现的同步队列（在需要accessqueue的时候；不需要的时候和AccessQueue一样是linkedlist实现），不需要获取segment锁，避免了segment锁竞争。
RecencyQueue与AccessQueue之间数据同步：在大多数get场景，都是维护RecentQueue队列，不需要维护AccessQueue队列。如果在某些场景已经获取了锁，则顺带把数据从RecentQueue同步到AccessQueue。
RecentQueue和AccessQueue的结合就实现了在确保get的高性能的场景下还能记录对元素的访问，从而实现LRU算法。
```
### 加载和刷新
Guava Cache支持"获取缓存-如果没有-则计算"[get-if-absent-compute]的原子语义。可以在get时传入一个Callable实例，指定加载函数，也可以通过put方式直接插入。更推荐使用CacheLoader自动加载初始值。
刷新和回收不太一样。刷新表示为键加载新值，这个过程可以是异步的。在刷新操作进行时，缓存仍然可以向其他线程返回旧值，而不像回收操作，读缓存的线程必须等待新值加载完成。
*定时重刷* CacheBuilder.refreshAfterWrite(long, TimeUnit)可以为缓存增加自动定时刷新功能。与自动过期原理类似，并没有一个后台线程在定时刷新数据，缓存项只有在被检索时才会真正刷新。因此，如果你在缓存上同时声明expireAfterWrite和refreshAfterWrite，可以实现定时刷新和过期移除。缓存并不会盲目地定时刷新，如果缓存项没有被检索，那刷新就不会真的发生，缓存项在过期时间后也变得可以回收。
### 监视移除
通过CacheBuilder.removalListener(RemovalListener)，你可以声明一个监听器，以便缓存项被移除时做一些额外操作。缓存项被移除时，RemovalListener会获取移除通知[RemovalNotification]，其中包含移除原因[RemovalCause]、键和值。
默认情况下，监听器方法是在移除缓存时同步调用的。因为缓存的维护和请求响应通常是同时进行的，代价高昂的监听器方法在同步模式下会拖慢正常的缓存请求。在这种情况下，你可以使用RemovalListeners.asynchronous(RemovalListener, Executor)把监听器装饰为异步操作。

## 统计
CacheBuilder.recordStats()用来开启Guava Cache的统计功能。统计打开后，Cache.stats()方法会返回CacheStats对象以提供如下统计信息：<br>
hitRate()：缓存命中率；<br>
averageLoadPenalty()：加载新值的平均时间，单位为纳秒；<br>
evictionCount()：缓存项被回收的总数，不包括显式清除。<br>
此外，还有其他很多统计信息。这些统计信息对于调整缓存设置是至关重要的，在性能要求高的应用中我们建议密切关注这些数据。<br>