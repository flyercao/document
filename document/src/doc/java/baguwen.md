
数据结构

![github](https://upload-images.jianshu.io/upload_images/2615789-1345e368181ad779.png) 


### 多线程
BLOCKED：一个线程因为等待对象或类的监视器锁被阻塞产生的状态。只有执行synchronize关键字没有获取到锁才会进入。进入同步代码块执行object.wait方法进入的是**WAITING**状态。
WAITING：线程通过notify,join,LockSupport.park方式进入wating状态，一直等待其他线程唤醒(notify或notifyAll)才有机会进入RUNNABLE状态。sleep、wait和lock方式都会使线程进入WAITING状态。
interrupted是线程的一个标志位。其他线程可以调用该线程的interrupt()方法对其进行中断操作，同时该线程可以调用isInterrupted（）来感知其他线程对其自身的中断操作，从而做出响应。
join是线程间协作的一种方式。如果一个线程实例A执行了threadB.join(),其含义是：当前线程A会等待threadB线程终止后threadA才会继续执行。
sleep是让当前线程进入WAITING状态。如果当前线程获得了锁，sleep方法并不会释放锁。sleep其实跟锁没有关系。
yield当前线程让出CPU，进入RUNNABLE状态。但是，需要注意的是，让出的CPU并不是代表当前线程不再运行了，如果在下一次竞争中，又获得了CPU时间片当前线程依然会继续运行。另外，让出的时间片只会分配给当前线程相同优先级的线程
JMM
通信模式：java内存模型是共享内存的并发模型，线程之间主要通过读-写共享变量来完成隐式通信。
共享变量：实例域，静态域和数组元素都是放在堆内存中，堆内存是所有线程都可访问，是共享的。
JMM抽象结构模型：CPU会有多级缓存，从储存加载到cpu的变量会先保存在缓存。这样就可能会出现"脏读"现象。
线程安全
### 锁
synchronized
偏向锁、轻量级锁、重量级锁
AQS
ReentryLock
无锁队列
disruptor
缓存
guava cache
Caffeine Cache

限流
guava rater

JVM内存
垃圾回收期
类加载机制

设计模式

事务隔离级别
实现原理
事务传播机制

dubbo RPC
原理
参数优化


mysql
innoDB引擎
事务机制
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


