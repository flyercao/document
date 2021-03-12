[TOC]

## 数据结构
### hashMap
结合数组（查找快）和链表/树（增删快）  数据结构特点。
1.7头插法，容易出现逆序导致死循环；1.8尾插法
ConcurrentHashMap线程安全：采用table数组元素作为锁，对每一行数据进行加锁。使用Synchronized和CAS来并发控制。
### 二叉树
treeSet
treeMap



## JVM内存
### 堆Heap
保存对象、数组、常量池、静态变量  
年轻代、老年代。而年轻代又可分为Eden区、From Survivor、To Survivor三个区域，默认8:1:1。  
垃圾回收主要针对的就是堆空间。
### 方法区
方法区主要是存储类的元数据的，如虚拟机加载的类信息、编译后的代码等。JDK8使用元空间MetaSpace实现，使用的系统内存。
### 虚拟机栈
虚拟机栈是线程私有的，它的生命周期与线程相同。
存储局部变量表、操作栈、动态链接、方法出口等信息。
如果一个对象的指针被多个方法或者线程引用时，那么我们成这个对象的指针发生了逃逸。逃逸分析后的优化：栈上分配：消除同步：标量替换
### 本地方法栈
专门为Native方法来实现
### 程序计数器
当前线程所执行的字节码的行号指示器
### 直接内存
为避免在Java堆和Native堆中来回复制数据，在虚拟机之外分配的内存，受限于机器内存。

JVM调优
内存： -Xmx3550m -Xms3550m -Xss128k -XX:NewRatio=4 -XX:SurvivorRatio=4 -XX:MaxMetaSpaceSize=128m -XX:MaxTenuringThreshold=0
回收器： -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=5 -XX:+UseCMSCompactAtFullCollection
辅助信息：-XX:+PrintGC Printetails  -XX:+PrintGCApplicationStoppedTime


## 垃圾回收器
### 垃圾识别算法
引用计数法：统计对象被引用的次数，为0则表示该对象可以被回收。缺点是如果对象之间存在循环依赖，则这些对象无法被回收。
可达性算法：从一个被称为GC Roots的对象开始向下搜索，如果一个对象到GC Roots没有任何引用链相连时，则说明此对象不可用。GC Roots对象包括（虚拟机栈中对象、方法区类静态属性、方法区常量池、本地方法栈JNI引用对象）

分代回收的意义：针对不同年代对象存活时间的特点，采用不同的垃圾回收算法，提高垃圾回收的效率。
年轻代：复制回收算法。对Eden区和其中一块Survivor区对象进行分析，存活超过年代阈值（默认15）的对象移动到老年代，其余存活对象复制到另一块Survivor区。
ParNew收集器 -XX:+UseParNewGC 用于新生代，标记复制算法
### CMS（Concurrent Mark Sweep） 
![](https://img2018.cnblogs.com/blog/1326194/201810/1326194-20181017221500926-2071899824.png)
老年代回收算法，最短回收停顿时间为目标。标记清理算法，并发收集、低停顿；
初始标记（STW）、并发标记、重新标记（STW）、并发清除  
占用CPU：垃圾回收线程与应用线程并发执行，互相抢占CPU资源，对应用程序造成影响。
回收频率：在CMS 回收过程中，应用程序仍然在不停地产生新对象，要确保有足够的内存空间。因此，CMS 收集器在内存饱和之前就开始进行回收。默认阈值是68%。
回收器退化：如果回收过程中内存不足，CMS回收失败，启动老年代串行收集器。应用程序将完全中断，直到垃圾收集完成，应用程序可能出现长时间停顿。可以根据老年代内存增长速度调整启动回收的阈值。增长慢，则调大阈值，有效降低 CMS 的触发频率，改善程序性能。
内存碎片：标记-清除算法会导致大量内存碎片，无法分配大对象，会提前进行垃圾回收。可以通过参数设置每次垃圾回收后进行内存碎片整理和设定进行多少次 CMS 回收后，进行一次内存压缩。  
```
-Xms堆空间初始值；-Xmx对空间最大值；-Xmn年轻代大小；-XX:SurvivorRation年轻代Eden区与Survivor区的比例；-XX:PretenureSizeThreshold大对象阈值；
-XX:+UseConcMarkSweepGC老年代使用CMS回收算法；-XX:+UseCMSCompactAtFullCollection Fullgc后对内存进行压缩；
-XX:+CMSInitiatingOccupancyFraction=70老年代进行gc时空间使用率；-XX:+MaxTenuringThreshold=15对象进入老年代的年龄。
```
### G1收集器
![](https://img2020.cnblogs.com/blog/980882/202004/980882-20200423001534652-41920210.png)
设计原则就是简单可行的性能调优。-XX:MaxGCPauseMillis=200  。初始标记-并发标记-重新标记-复制清除
**并行与并发**：G1能充分利用多CPU、多核环境下的硬件优势，使用多个CPU来缩短 Stop-The-World 停顿的时间    
**分代收集**：独立管理整个GC堆,通过young gc和mix gc分别回收新生代和老年代。  
**空间整合**：基于 “标记-整理（复制）” 算法实现的收集器，不会产生内存空间碎片，利于分配大对象。  
**可预测的停顿**：这是G1相对于CMS的另外一大优势，还能建立可预测的停顿时间模型。G1跟踪各个Region里面的垃圾堆积的价值大小，在后台维护一个优先列表，每次根据允许的收集时间，优先回收价值最大的 Region。  
**Humongous区域**：用来存放超过了分区容量50%的大对象。如果一个H区装不下巨型对象，那么会寻找连续的H分区来存储，甚至提前启动Full gc。  
**Remembered Set**：每个Region都有一个与之对应的RememberedSet ，各个 Region 上记录自家的对象被外面对象引用的情况。当进行内存回收时，在GC根节点的枚举范围中加入RememberedSet 即可保证不对全堆扫描也不会有遗漏
```
-XX:MaxGCPauseMillis=200最大停顿时间；-XX:G1HeapRegionSize=n region大小；-XX:ParallelGCThreads=cpu STW 工作线程数的值；
-XX:ConcGCThreads=cpu/4 并行标记的线程数；-XX:InitiatingHeapOccupancyPercent=45触发标记周期的 Java 堆占用率阈值
```
某些情况下会退化成full gc，这时候单线程来完成GC。（晋升失败，巨型对象分配失败）

## 类加载
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
* 自定义类加载器：系统类加载器无法对其进行加载，这样则需要自定义类加载器来实现。自定义类加载器一般都是继承自 ClassLoader 类，我们只需要重写 findClass 方法即可。如果想打破双亲委派机制，则重写loadclass
破坏者——线程上下文类加载器： SPI 的接口属于 Java 核心库，一般存在rt.jar包中，由Bootstrap类加载器加载。 
SPI 的第三方实现代码则是作为Java应用所依赖的 jar 包被存放在classpath路径下，Bootstrap类加载器无法直接加载。
所以需要线程上下文类加载器（contextClassLoader）。初始线程的上下文类加载器是系统类加载器（AppClassLoader）,在线程中运行的代码可以通过此类加载器来加载类和资源。

### 反射原理
反射就是运行过程中动态加载类、获取类信息、调用类方法等。类class加载到内存后以class 实例对象的形式存在。通过class.forName获取类对象。
性能：1. 编译器没法对反射相关的代码做优化（JIT ）；2.类名和方法名初次查找耗时；3.类型检查，可见性检查等；

### SPI
Java提供的一套用来被第三方实现或者扩展的接口，用来启用框架扩展和替换组件。 SPI的作用就是为这些被扩展的API寻找服务实现。
1. 通过当前线程类加载器和class对象，构造ServiceLoader；并且实例化一个LazyIterator；
2. LazyIterator的作用是扫描所有引用的jar包里/META-INF/services/目录下配置文件，并parse配置中的所有service名字。
3. 将配置的类通过反射序列化，加载到缓存中，并返回。
**弊端**  
```
只能遍历所有的实现，并全部实例化。
扩展如果依赖其他的扩展，做不到自动注入和装配。
由于配置没有命名，如果有多个扩展实现，无法指定引用那种实现。
```
**Dubbo SPI**
整体原理与SPI相似，自己实现了一套机制。解析约定的路径下配置文件，找到扩展实现类，再通过反射实例化到缓存。
@SPI扩展点：注解作用于扩展点的接口上，表明该接口是一个扩展点。  全类名就是配置文件名。  
@Adaptive自适应实例：@Adaptive 注解添加在扩展点的方法上，表示该方法的实现是被代理的，在运行时动态根据url配置获取相应的实现。
@Adaptive 注解添加在类上，表示该类是一个自适应扩展点。可以根据请求的参数，即 URL 得到具体要调用的实现类名。  
Activate自动激活扩展点：提供了一些配置来允许我们配置加载条件，比如 group 过滤，比如 key 过滤。对于一个类会加载多个扩展点的实现，通过自动激活扩展点进行动态加载，从而简化配置。
实现了dubbo IOC和AOP机制
## 多线程
### 线程状态
![github](https://upload-images.jianshu.io/upload_images/2615789-1345e368181ad779.png) 

BLOCKED：一个线程因为等待对象或类的监视器锁被阻塞产生的状态。只有执行synchronize关键字没有获取到锁才会进入。进入同步代码块执行object.wait方法进入的是**WAITING**状态。  
WAITING：线程通过notify,join,LockSupport.park方式进入wating状态，一直等待其他线程唤醒(notify或notifyAll)才有机会进入RUNNABLE状态。sleep、wait和lock方式都会使线程进入WAITING状态。  

1. interrupted是线程的一个标志位。其他线程可以调用该线程的interrupt()方法对其进行中断操作，同时该线程可以调用isInterrupted（）来感知其他线程对其自身的中断操作，从而做出响应。
1. join是线程间协作的一种方式。如果一个线程实例A执行了threadB.join(),其含义是：当前线程A会等待threadB线程终止后threadA才会继续执行。
1. sleep是让当前线程进入WAITING状态。如果当前线程获得了锁，sleep方法并不会释放锁。sleep其实跟锁没有关系。
1. yield当前线程让出CPU，进入RUNNABLE状态。但是，需要注意的是，让出的CPU并不是代表当前线程不再运行了，如果在下一次竞争中，又获得了CPU时间片当前线程依然会继续运行。另外，让出的时间片只会分配给当前线程相同优先级的线程
### JMM
通信模式：java内存模型是共享内存的并发模型，线程之间主要通过读-写共享变量来完成隐式通信。
共享变量：实例域，静态域和数组元素都是放在堆内存中，堆内存是所有线程都可访问，是共享的。
JMM抽象结构模型：CPU工作内存与主内存之间会有多级缓存，从内存加载到cpu工作内存的变量会暂存在缓存。JMM抽象层次定义了一个线程对共享变量的写入何时对其他线程是可见的，何时将工作内存的变量副本同步到主内存。MESI缓存一致性协议，它通过定义一个状态机来保证缓存的一致性。
重排序：为了提高性能，编译器和处理器常常会对指令进行重排序。针对编译器重排序，JMM的编译器重排序规则会禁止一些特定类型的编译器重排序；针对处理器重排序，编译器在生成指令序列的时候会通过插入内存屏障指令来禁止某些特殊的处理器重排序。
happens-before原则：JMM可以通过happens-before关系向程序员提供跨线程的内存可见性保证，同时对编译器和处理器重排序进行约束。（如果A线程的写操作a与B线程的读操作b之间存在happens-before关系，尽管a操作和b操作在不同的线程中执行，但JMM向程序员保证a操作将对b操作可见）
### 线程安全
原子性：互斥访问，同一时刻只能有一个线程对数据进行操作（CAS、synchronized）
可见性：一个线程对主内存的修改可以及时地被其他线程看到，（synchronized,volatile）
有序性：一个线程的指令执行顺序，不影响其他线程观察到的结果（volatile）
AtomicStampedReference通过版本号避免CAS ABA问题。
### volatile
可见性：通过MESI缓存一致性协议保证A线程对变量i值做了变动之后，会立即刷回到主内存中，而其它线程CPU缓存到该变量的值也作废，强迫重新从主内存中读取该变量的值。
有序性：volatile有序性是通过内存屏障实现的。JVM和CPU都会对指令做重排优化，所以在指令间插入一个屏障点，就告诉JVM和CPU，不能进行重排优化。  
**使用场景** 对变量的写操作不依赖于当前值或只是赋值；volatile变量的单次读/写操作可以保证原子性；volatile变量没有其他依赖时；  

## 锁
### synchronized
任意线程对Object的访问，首先要获得Object的监视器，如果获取失败，线程状态变为BLOCKED。当Object的监视器占有者释放后，在同步队列中得线程就会有机会重新获取该监视器。  
获取监视器后调用wait()方法，线程释放锁，变成waiting状态，进入等待队列。直到被其他线程调用object的notify()或notifyAll()，才有机会重新获取锁对象。  
是一种重量级锁，会涉及到操作系统状态的切换影响效率  
#### 偏向锁（无竞争）
在对象头Mark Word  cas方式写入当前线程ID，成功则获取锁成功，下次申请锁只需要比较是否当前线程ID，只有一次cas开销。失败则锁升级为轻量级锁。  
当有其他线程竞争时，才会释放锁，撤销偏向锁后，恢复未锁定或轻量级锁状态。
#### 轻量级锁（执行时间短、竞争不激烈）
轻量级锁每次退出同步块都需要释放锁，而偏向锁是在竞争发生时才释放锁；  
每次进入退出同步块都需要CAS更新对象头；  
争夺轻量级锁失败时，自旋尝试抢占锁；多次失败则升级为重量级锁  
#### 重量级锁
加锁、解锁过程和轻量级锁差不多，区别是：竞争失败后，线程阻塞，释放锁后，唤醒阻塞的线程。
#### 锁消除
锁消除指的就是虚拟机即使编译器在运行时，如果检测到那些共享数据不可能存在竞争，就执行锁消除。
#### 锁粗化
写代码时推荐将同步块的作用范围限制得尽量小——只在共享数据的实际作用域才进行同步，这样是为了使得需要同步的操作数量尽可能变小，如果存在锁竞争，那等待线程也能尽快拿到锁。  
锁粗化通过扩大锁的范围，避免反复加锁和释放锁。

### Synchronized 和 ReenTrantLock 的对比
两者都是可重入锁，都能实现多线程安全；  
synchronized依赖于JVM；而ReenTrantLock依赖于API。所以synchronized使用相对简单，ReenTrantLock相对复杂。  
ReenTrantLock比synchronized增加了一些高级功能。①等待可中断；②可实现公平锁；③可实现选择性通知
1.6以前，synchronized的性能是比ReenTrantLock差很多。

AQS
ReentryLock
#### 无锁队列Disruptor
锁：锁竞争导致操作系统的上下文切换，执行线程的执行上下文丢失之前缓存的数据和指令集，给处理器带来严重的性能损耗。偏向锁在没有竞争时才有效；轻量级锁在竞争不激烈时有效，竞争激烈时，由于自旋给CPU带来压力。
CAS：处理器需要对指令pipeline加锁以确保原子性，并且会用到内存栅栏，缓存一致性协议，也有开销。
缓存行：如果两个变量不幸在同一个缓存行里，而且它们分别由不同的线程写入，那么这两个变量的写入会发生竞争，即为“伪共享”（false sharing）。
队列：队列的头（消费者要频繁使用），尾（生产者要频繁使用）以及长度（生产者和消费者都需要频繁更改）会产生资源竞争。queue会成为很大的垃圾回收源，队列中的对象需要被分配和替换。

![github](https://img-blog.csdn.net/20171221134247821) 
缓存行填充：就是每次把数据对齐到跟缓存行（通常是64B）一样大小。
环形队列：1.预分配大数组，避免频繁创建和销毁对象，频繁GC；
无锁：避免使用synchronized和lock锁，使用cas和volatile实现线程安全。
-- 关注点隔离：生产者/消费者栅栏（barriers）。通过CAS操作处理生产者之间对于写入位置的竞争；写入完成后更新SequenceBarrier（包含游标cursor）,通知消费者，这里其实会存在等待，有多种等待策略（各种）；消费者之间通过CAS协调读取位置
多生产者：1.生产者之间（cas+自旋）向MultiProducerSequencer.cursor竞争写入位置，并确保不会覆盖最慢的消费者组的workSequence；2.在该位置写入值；3.在MultiProducerSequencer.availableBuffer的对应位置写入0，标记为可读；4.通知消费者；
多消费者：1.每组消费者都有workSequence消费序号，每个线程都有自己的sequence序号，线程sequence通过cas竞争获取workSequence的值并加1；2.如果sequence小于cachedAvailableSequence，则说明可以一直消费到cachedAvailableSequence。3.如果大于，则说明已读完了，需要从生产者cursor更新最大可读Sequence。
批量效应：当消费者等待RingBuffer中可用的前进游标序号时，如果消费者发现RingBuffer游标自上次检查以来已经前进了多个序号，消费者可以直接处理所有可用的多个序号，而不用引入并发机制。
应用场景：高性能跨线程通信，生产者消费者模型，日志处理。

#### ThreadLocal
Thread类中有两个ThreadLocalMap类型的本地变量threadLocals和inheritableThreadLocals，而ThreadLocal<T>相当于map的key。
ThreadLocalMap内部实际上是一个Entry数组，该Entry的key是ThreadLocal<T>的WeakReference弱引用，弱引用的对象在没有其他强引用时会被gc回收。但是Entry<null,value>依然存在，造成了泄露。
Entry的Key设置成弱引用是为了对ThreadLocal<T>进行回收。否则，当ThreadLocal<T>的强引用置为null后，Entry的Key为强引用的话，则ThreadLocal<T>无法进行回收。
如果把ThreadLocal实例定义为类中的静态变量，则该实例的引用不会被释放（除非该类被回收）也就不会出现key为null的Entry了。但是如果线程复用的场景中，当前请求可能获取到上一个请求的数据，导致结果不正确。
结论：使用ThreadLocal后一定要remove。

#### 本地缓存
LRU:如果数据最近被访问过，那么将来被访问的几率也更高。根据数据的历史访问时间来进行淘汰数据。linkedList+HashMap实现LRU，或LinkedHashMap实现。实现简单，容易被批量查询污染。
LRU-K（LRU-2）：缓存最近使用过K次的数据，淘汰小于K次的数据。降低了“缓存污染”带来的问题，命中率比LRU要高。历史队列linkedList+LRU缓存队列。linkedList保存小于K的数据，附加最后访问时间和最近访问次数。实际应用中LRU-2是综合各种因素后最优的选择。
Two queues(2Q)：其实是LRU-2的一个实现版本。并且2Q的历史队列是采用FIFO的方法进行缓存的。
Muti Queue(MQ)：MQ算法其实是2Q算法的一个扩展。根据访问频率将数据划分为多个队列，不同的队列具有不同的访问优先级。成本较大。

disruptor:https://www.cnblogs.com/daoqidelv/p/7043696.html
#### 缓存 guava cache
类似ConcurrentHashMap下的线程安全LRU（分段锁）；Segment中AccessQueue（访问双向列表，非线程安全）、WriteAccess（写入双向列表，非线程安全，要获取锁）和RecencyQueue（暂存访问队列，线程安全）。
为了让get方法不阻塞，get时会尝试获取Segment锁，成功则加锁，调整AccessQueue数据顺序；失败则把访问数据记录往RecencyQueue，后续获取锁成功才把RecencyQueue数据更新到AccessQueue。
异步加载
过期移除、容量移除、引用移除、显示移除
异步刷新
移除监听
统计信息

#### 限流器rateLimiter
https://blog.csdn.net/netyeaxi/article/details/104270337
漏桶算法：控制流量，超过则丢弃。
令牌桶算法：控制流量，允许短暂突发大流量。
guava rateLimiter：（当前时间-上次空桶时间）* 速率    上次空桶时间<当前表示有可用令牌，可取并可预支；大于表示已预支，本次获取失败。
分布式限流：redisson.getRateLimiter
