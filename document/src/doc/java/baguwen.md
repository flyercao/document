数据结构

![github](https://upload-images.jianshu.io/upload_images/2615789-1345e368181ad779.png) 


## 多线程
BLOCKED：一个线程因为等待对象或类的监视器锁被阻塞产生的状态。只有执行synchronize关键字没有获取到锁才会进入。进入同步代码块执行object.wait方法进入的是**WAITING**状态。
WAITING：线程通过notify,join,LockSupport.park方式进入wating状态，一直等待其他线程唤醒(notify或notifyAll)才有机会进入RUNNABLE状态。sleep、wait和lock方式都会使线程进入WAITING状态。
interrupted是线程的一个标志位。其他线程可以调用该线程的interrupt()方法对其进行中断操作，同时该线程可以调用isInterrupted（）来感知其他线程对其自身的中断操作，从而做出响应。
join是线程间协作的一种方式。如果一个线程实例A执行了threadB.join(),其含义是：当前线程A会等待threadB线程终止后threadA才会继续执行。
sleep是让当前线程进入WAITING状态。如果当前线程获得了锁，sleep方法并不会释放锁。sleep其实跟锁没有关系。
yield当前线程让出CPU，进入RUNNABLE状态。但是，需要注意的是，让出的CPU并不是代表当前线程不再运行了，如果在下一次竞争中，又获得了CPU时间片当前线程依然会继续运行。另外，让出的时间片只会分配给当前线程相同优先级的线程
JMM
通信模式：java内存模型是共享内存的并发模型，线程之间主要通过读-写共享变量来完成隐式通信。
共享变量：实例域，静态域和数组元素都是放在堆内存中，堆内存是所有线程都可访问，是共享的。
JMM抽象结构模型：CPU工作内存与主内存之间会有多级缓存，从内存加载到cpu工作内存的变量会暂存在缓存。JMM抽象层次定义了一个线程对共享变量的写入何时对其他线程是可见的，何时将工作内存的变量副本同步到主内存。MESI缓存一致性协议，它通过定义一个状态机来保证缓存的一致性。
重排序：为了提高性能，编译器和处理器常常会对指令进行重排序。针对编译器重排序，JMM的编译器重排序规则会禁止一些特定类型的编译器重排序；针对处理器重排序，编译器在生成指令序列的时候会通过插入内存屏障指令来禁止某些特殊的处理器重排序。
happens-before原则：JMM可以通过happens-before关系向程序员提供跨线程的内存可见性保证，同时对编译器和处理器重排序进行约束。（如果A线程的写操作a与B线程的读操作b之间存在happens-before关系，尽管a操作和b操作在不同的线程中执行，但JMM向程序员保证a操作将对b操作可见）
线程安全
在多线程开发时需要从原子性，有序性，可见性三个方面进行考虑。

### 锁
## 锁
synchronized
偏向锁、轻量级锁、重量级锁
AQS
ReentryLock
#### 无锁队列
锁：锁竞争导致操作系统的上下文切换，执行线程的执行上下文丢失之前缓存的数据和指令集，给处理器带来严重的性能损耗。偏向锁在没有竞争时才有效；轻量级锁在竞争不激烈时有效，竞争激烈时，由于自旋给CPU带来压力。
CAS：处理器需要对指令pipeline加锁以确保原子性，并且会用到内存栅栏，缓存一致性协议，也有开销。
缓存行：如果两个变量不幸在同一个缓存行里，而且它们分别由不同的线程写入，那么这两个变量的写入会发生竞争，即为“伪共享”（false sharing）。
队列：队列的头（消费者要频繁使用），尾（生产者要频繁使用）以及长度（生产者和消费者都需要频繁更改）会产生资源竞争。queue会成为很大的垃圾回收源，队列中的对象需要被分配和替换。

![github](https://img-blog.csdn.net/20171221134247821) 
缓存行填充：就是每次把数据对齐到跟缓存行（通常是64B）一样大小。
环形队列：1.预分配大数组，避免频繁创建和销毁对象，频繁GC；
无锁：避免使用synchronized和lock锁，使用cas和volatile实现线程安全。
关注点隔离：生产者/消费者栅栏（barriers）。通过CAS操作处理生产者之间对于写入位置的竞争；写入完成后更新SequenceBarrier（包含游标cursor）,通知消费者，这里其实会存在等待，有多种等待策略（各种）；消费者之间通过CAS协调读取位置
多生产者：1.生产者之间（cas+自旋）向MultiProducerSequencer.cursor竞争写入位置，并确保不会覆盖最慢的消费者组的workSequence；2.在该位置写入值；3.在MultiProducerSequencer.availableBuffer的对应位置写入0，标记为可读；4.通知消费者；
多消费者：1.每组消费者都有workSequence消费序号，每个线程都有自己的sequence序号，线程sequence通过cas竞争获取workSequence的值并加1；2.如果sequence小于cachedAvailableSequence，则说明可以一直消费到cachedAvailableSequence。3.如果大于，则说明已读完了，需要从生产者cursor更新最大可读Sequence。
批量效应：当消费者等待RingBuffer中可用的前进游标序号时，如果消费者发现RingBuffer游标自上次检查以来已经前进了多个序号，消费者可以直接处理所有可用的多个序号，而不用引入并发机制。
应用场景：高性能跨线程通信，生产者消费者模型，日志处理。

disruptor:https://www.cnblogs.com/daoqidelv/p/7043696.html
#### 缓存
guava cache
Caffeine Cache

限流
guava rater
降级
熔断
隔离

# JVM内存
https://imgconvert.csdnimg.cn/aHR0cDovL3d3MS5zaW5haW1nLmNuL2xhcmdlL2E1ZmE0YThkZ3kxZ2EyNDExaWZjcWoyMGswMGx6bXo1LmpwZw?x-oss-process=image/format,png

垃圾回收器


类加载机制
类初始化时机：只有当对类的主动使用的时候才会导致类的初始化。类加载的过程包括加载、验证、准备、解析、初始化五个阶段
加载：查找并加载类的二进制数据，转化为方法区的运行时数据结构，在Java堆中生成一个代表这个类的java.lang.Class对象。
验证：为了确保Class文件的字节流中包含的信息符合当前虚拟机的要求，不会危害虚拟机自身的安全。
准备：为类变量分配内存并设置类变量默认值。
解析：是虚拟机将常量池内的符号引用替换为直接引用的过程。
初始化：JVM负责对类变量进行初始化，执行静态代码块。
类加载模型
![github](http://images2015.cnblogs.com/blog/331425/201606/331425-20160621125943787-249179344.jpg)

* 启动类加载器：加载存放在JDK\jre\lib，并且能被虚拟机识别的类库。启动类加载器是无法被Java程序直接引用的。
* 扩展类加载器：加载JDK\jre\lib\ext目录，指定的路径中的所有类库。开发者可以直接使用扩展类加载器。
* 应用程序类加载器：负责加载用户类路径（ClassPath）所指定的类，开发者可以直接使用该类加载器。

双亲委派机制
* 双亲委派模型：如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把请求委托给父加载器去完成，依次向上，因此，所有的类加载请求最终都应该被传递到顶层的启动类加载器中，只有当父加载器在它的搜索范围中没有找到所需的类时，即无法完成该加载，子加载器才会尝试自己去加载该类。
* 双亲委派模型意义：系统类防止内存中出现多份同样的字节码；保证Java程序安全稳定运行，系统类不会被重写或覆盖。
* 自定义类加载器：系统类加载器无法对其进行加载，这样则需要自定义类加载器来实现。自定义类加载器一般都是继承自 ClassLoader 类，我们只需要重写 findClass 方法即可。
破坏者——线程上下文类加载器： SPI 的接口属于 Java 核心库，一般存在rt.jar包中，由Bootstrap类加载器加载。 
SPI 的第三方实现代码则是作为Java应用所依赖的 jar 包被存放在classpath路径下，Bootstrap类加载器无法直接加载。
所以需要线程上下文类加载器（contextClassLoader）。初始线程的上下文类加载器是系统类加载器（AppClassLoader）,在线程中运行的代码可以通过此类加载器来加载类和资源。

设计模式

事务隔离级别
实现原理
事务传播机制

mysql
innoDB引擎：完整支持ACID事务，在线事务处理。表锁、行锁；通过多版本并发控制MVCC来获得高并发性，并实现了4种隔离级别，默认Read Repeatable，使用next-key-locking间隙锁的策略来避免幻读。
事务机制
Atom原子性：当事务对数据库进行修改时，InnoDB会生成对应的undo log；如果事务执行失败或调用了rollback，导致事务需要回滚，便可以利用undo log中的信息将数据回滚到修改之前的样子。当发生回滚时，InnoDB会根据undo log的内容做与之前相反的工作。
Durability持久性：当数据修改时，除了修改Buffer Pool中的数据，还会在redo log记录这次操作；当事务提交时，会调用fsync接口对redo log进行刷盘。如果MySQL宕机，重启时可以读取redo log中的数据，对数据库进行恢复。
Isolation隔离性:InnoDB通过锁机制和MVCC机制保证多并发下的数据隔离。
https://www.cnblogs.com/kismetv/p/10331633.html
主键索引、查询优化
死锁分析https://www.cnblogs.com/jay-huaxiao/p/11456921.html
分库分表jbdc-sharding

redis
存储数据结构
string：常用作key-value存储；incr/decr 用作计数器；
list：双向列表结构。支持左右两端存取数据，支持范围读取；支持查询列表长度。
set：
分片
扩容
## redis
![github](https://pic4.zhimg.com/80/v2-d1128bb6e62db58955215c4c05ac1eab_1440w.jpg)
String:缓存K-V结构；计数器；
Hash：缓存多K-V对
List：有序列表。高性能分页；简单消息队列；
Set：无序集合。全局去重；集合的交集、并集、差集的操作。
Sorted Set：有序集合。排行榜；优先级队列；

Bitmap：位图（key为long，value为boolean的set），保存大规模的Boolean值。签到、在线状态等；BITCOUNT全局去重计数；BITOP 命令支持 AND 、 OR 、 NOT 、 XOR 操作；可以通过插件支持redis版BloomFilter
HyperLogLog：极高性能的全局去重计数（估算）。
Geospatial：地理空间(geospatial)以及索引半径查询。两点距离；经纬度返回Geohash；Geohash返回相近经纬度；圈内的所有位置；   
pub/sub：消息生产者消费者模式，数据可靠性无法保证，不建议生产环境使用。
pipeline：批量提交命令，批量返回结果，期间独占连接，返回结果会占用更多内存。
Lua脚本：多个脚本一次请求，减少网络开销；原子操作，无需担心竞争问题；复用，脚本会缓存在服务器，无需每次传输。
事务：Redis 事务的本质是一组命令的集合；watch来实现乐观锁，在事务中不能改变被watch（锁）的值。命令错误则不执行，执行错误继续执行所有命令。
持久化：RDB，定时把数据以快照的形式保存在磁盘上，效率高，数据丢失；AOF，将每一个收到的写命令都通过write函数追加到文件中。保证数据不丢失，文件大，效率低。
生产配置：master：完全关闭持久化，这样可以让master的性能达到最好；slave：关闭快照持久化，开启AOF，并定时对持久化文件进行备份，然后关闭AOF的自动重写，然后添加定时任务，在每天Redis闲时（如凌晨12点）调用bgrewriteaof。

redis cluster集群：高可用高扩展的无中心结构（分片+主备）；
容错：半数以上的master节点投票确认节点失联。1.master和slave都失联则集群不可用（可配置兼容部分失败）。2.半数master失联则不可用。
数据分布
一致性哈希。对每一个key进行hash运算，被哈希后的结果在哪个token的范围内，则按顺时针去找最近的节点，这个key将会被保存在这个节点上。缺点（翻倍伸缩才能保证负载均衡）。
虚拟槽分区。Redis Cluster采用的分区方式。把16384槽按照节点数量进行平均分配，由节点进行管理，对每个key按照CRC16规则进行hash运算，把hash结果对16383进行取余。可以对数据打散，又可以保证数据分布均匀

缓存一致性：先更新数据库，再删缓存,极小概率出现不一致。终极方案，设置超时，订阅binlog，删除key。
缓存穿透：外部的恶意攻击时，未命中缓存，直接查询DB。使用BloomFilter排除不存在对象。
缓存击穿：某个热点数据失效，大量请求会穿透到DB。使用互斥锁（SETNX）查询DB和更新缓存。多个热点 key 同时失效，失效时间考虑随机数。
缓存雪崩：快速失败+集群模式来保证高可用。
过期策略：定期删除+惰性删除。
淘汰机制：报错，LRU，random，TTL

mongo
文档型、模式自由、高性能、高可用、高扩展、支持事务、分布式数据库。
性能：1.充分使用系统内存作为缓存，内存不够才会写磁盘；2.索引直接关联地址，无需回表查询；3.内联数据模式，无需关联查询，数据相对集中。4.集群模式；
分片：https://img2020.cnblogs.com/blog/630480/202003/630480-20200303232850387-60758810.png
mongos：数据路由，和客户端打交道的模块。
config server：所有存、取数据的方式，所有shard节点的信息，分片功能的一些配置信息。
shard：真正的数据存储位置，以chunk（64M）为单位存数据。
Chunk：MongoDB会把数据分为chunks作为最小管理单位。1.超过size时，会被后台进程分裂；2.后台进程会自动平衡分片之间的chunks。
数据分区
分片键shard key：片键必须是一个索引（单个或组合索引）。1.按照范围分片，分布不均，导致热点；2.基于哈希分片，分布均匀，不利于范围查询。分片键选择建议：大方向随机递增，小范围随机分布。

单文档事务
Atom（原子性）：journal 机制来实现，支持单文档的执行、提交和回滚；如果是多文档，需要合并成一个文档。
Consistency（一致性）:通过Write Concern写策略，Read Concern读策略和Read Preference从哪读来实现灵活的一致性。
Isolation（隔离性）：读已提交，通过读写锁 + snapshot+ MVCC实现，与mysql类似。一个事务开始时，只能“看见”已经提交的事务所做的修改，会出现不可重复读（non-repeatable read）现象。
durability（持久性）：通过journal日志和Write Concern写入策略可以实现灵活的分布式持久性。
4.x版本支持多文档事务和分布式事务。


hbase：高可靠性、高性能、面向列、可伸缩、 实时读写的海量数据分布式数据库
批处理

分布式系统设计
微服务架构

hbase
批处理

分布式系统设计
微服务架构


dubbo
原理
参数优化


流计算
jstorm
flink