


微服务架构
业务拆分、组织架构升级
按照业务进行系统应用拆分、数据库拆分、
应用之间服务化调用
问题：问题定位、稳定性、互相依赖

Prometheus指标采集、Grafana配置监控报警、pinpoint全链路、ELK日志搜索
dubbo rpc调用、zookeeper服务发现（临时节点失效后自动删除）、Diamond配置中心、接口熔断降级限流（Hystrix），功能降级
Kafka、codis、shardingjdbc、elastic-job
cap理论



设计思想：通过巧妙的设计提高效率，避免资源的浪费
预操作：对于耗时要求极高的业务，可以在登录时进行预加载操作。以空间换时间。
实时操作：常规操作，实时查询，实时加载，定时过期。
延迟操作：对于定时删除和失效之类的操作，增加失效时间字段，通过时间判断数据失效。延后统一批量删除。

延迟操作代替定时任务实时操作：目前定时刷新等操作是通过后台定时任务执行的，单独线程消耗资源；
令牌桶：通过记录下次可用时间代替保存令牌，来节省定时添加令牌的操作。

### 锁
## 锁
synchronized
偏向锁、轻量级锁、重量级锁
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
关注点隔离：生产者/消费者栅栏（barriers）。通过CAS操作处理生产者之间对于写入位置的竞争；写入完成后更新SequenceBarrier（包含游标cursor）,通知消费者，这里其实会存在等待，有多种等待策略（各种）；消费者之间通过CAS协调读取位置
多生产者：1.生产者之间（cas+自旋）向MultiProducerSequencer.cursor竞争写入位置，并确保不会覆盖最慢的消费者组的workSequence；2.在该位置写入值；3.在MultiProducerSequencer.availableBuffer的对应位置写入0，标记为可读；4.通知消费者；
多消费者：1.每组消费者都有workSequence消费序号，每个线程都有自己的sequence序号，线程sequence通过cas竞争获取workSequence的值并加1；2.如果sequence小于cachedAvailableSequence，则说明可以一直消费到cachedAvailableSequence。3.如果大于，则说明已读完了，需要从生产者cursor更新最大可读Sequence。
批量效应：当消费者等待RingBuffer中可用的前进游标序号时，如果消费者发现RingBuffer游标自上次检查以来已经前进了多个序号，消费者可以直接处理所有可用的多个序号，而不用引入并发机制。
应用场景：高性能跨线程通信，生产者消费者模型，日志处理。

disruptor:https://www.cnblogs.com/daoqidelv/p/7043696.html
#### 缓存
guava cache
异步加载
过期移除、容量移除、引用移除、显示移除
异步刷新
移除监听
统计信息

Caffeine Cache

限流
guava rater
降级
熔断
隔离

设计模式
工厂
单例
适配器
模板模式：一个抽象类公开定义了执行它的方法的方式/模板，它的子类可以按需要重写方法实现
组合模式
代理模式
策略模式
责任链模式：避免请求发送者与接收者耦合在一起，让多个对象都有可能接收请求，将这些对象连接成一条链，并且沿着这条链传递请求，直到有对象处理它为止。dubbo 的filter
ddd


事务传播机制


mysql
缓冲池优化：InnoDB对传统的LRU算法做了一些优化，LRU列表中添加了midpoint位置，将缓冲池分为冷热两部分，两部分数据根据策略进行交换。
Checkpoint：由于数据库采用write ahead log策略，需要通过Checkpoint来强制刷新内存数据到磁盘，缩短数据库启动和恢复时间。缓冲池溢出脏页、redo log满时，会强制CheckPoint。
InsertBuffer：对于非唯一的非聚集索引，插入时如果该页不在缓存中，则先放到InsertBuffer对象，在一定条件下，与索引页进行merge，可以将多个插入合并成一次操作。提高写入性能。
DoubleWrite：保障数据完整性。在对脏页进行刷新时，先把脏数据复制到内存中的doublewrite buffer，然后分两次写入磁盘的doublewrite（顺序写），最后写入数据到数据页（分散写）。
异步IO：异步IO在发送完IO请求后可以继续发出新的IO请求；且AIO可以进行IO Merge。
刷新邻接页：当刷新一个脏页时，InnoDB会检测该页所在区的所有页，如果是脏页，那么一起进行刷新。这样可以利用AIO合并多个IO操作。
innoDB引擎：完整支持ACID事务，在线事务处理。表锁、行锁；通过多版本并发控制MVCC来获得高并发性，并实现了4种隔离级别，默认Read Repeatable，使用next-key-locking间隙锁的策略来避免幻读。
特性
事务机制
Atom原子性：当事务对数据库进行修改时，InnoDB会生成对应的undo log；如果事务执行失败或调用了rollback，导致事务需要回滚，便可以利用undo log中的信息将数据回滚到修改之前的样子。当发生回滚时，InnoDB会根据undo log的内容做与之前相反的工作。
Durability持久性：当数据修改时，除了修改Buffer Pool中的数据，还会在redo log记录这次操作；当事务提交时，会调用fsync接口对redo log进行刷盘。如果MySQL宕机，重启时可以读取redo log中的数据，对数据库进行恢复。
Isolation隔离性:InnoDB通过锁机制（含next-key lock）和MVCC机制（包括数据的隐藏列、基于undo log的版本链、ReadView）保证多并发下的数据隔离。MVCC最大的优点是读不加锁，因此读写不冲突，并发性能好。主要基于一下技术：
```
隐藏列：InnoDB中每行数据都有隐藏列，隐藏列中包含了本行数据的事务id、指向undo log的指针
基于undo log的版本链：每行数据的隐藏列中包含了指向undo log的指针，而每条undo log也会指向更早版本的undo log，从而形成一条版本链
ReadView：通过隐藏列和版本链，MySQL可以将数据恢复到指定版本；进行读操作时，会将读取到的数据中的事务id与快照事务id比较，从而判断数据对该ReadView是否可见，即对事务A是否可见。数据的事务id>readview_id,则不可见；生成readview时，数据的事务id已提交，则可见。
读已提交（Read Commit）：避免脏读（读未提交数据）。事务每次select前生成readview，当select时，对于未提交的数据，数据事务id还未提交，该数据对ReadView不可见，则根据指针指向的undo log查询上一版本的数据。
可重复读（Repeatable Read）：避免不可重复读。事务首次select前生成readview，当select时，对于未提交的数据，数据事务id还未提交或小于readview事务id，该数据对ReadView不可见，则根据指针指向的undo log查询上一版本的数据。
串行读（Serializable）：MVCC避免幻读的机制与避免不可重复读非常类似，事务在首次select某范围内数据时生成readview，之后在此select该范围数据时，根据首次生成的readview对数据进行可见性判断，对于新插入的数据，事务根据其指针指向的undo log查询上一版本的数据，发现该数据并不存在，从而避免了幻读。

RC与RR都使用了MVCC，主要区别在于RR是在事务开始后第一次执行select前创建ReadView，直到事务提交都不会再创建，RR可以避免脏读、不可重复读和幻读。RC每次执行select前都会重新建立一个新的ReadView，如果中间有其他事务提交的话，后续的select是可见的，可以避免脏读。
对于加锁读select...for update，由于事务对数据进行加锁读后，其他事务无法对数据进行写操作，因此可以避免脏读和不可重复读。通过next-key lock，不仅会锁住记录本身(record lock的功能)，还会锁定一个范围(gap lock的功能)，因此，加锁读同样可以避免脏读、不可重复读和幻读，保证隔离性。
如果在事务中第一次读取采用非加锁读，第二次读取采用加锁读，则如果在两次读取之间数据发生了变化，两次读取到的结果不一样，因为加锁读时不会采用MVCC。
```
Consistency一致性：一致性是事务追求的最终目标：前面提到的原子性、持久性和隔离性，都是为了保证数据库处于正确的状态。

https://img2018.cnblogs.com/blog/1174710/201901/1174710-20190128201034603-681355962.png
https://www.cnblogs.com/kismetv/p/10331633.html
主键索引、查询优化
全值匹配、最左前缀、索引列无计算、范围索引失效、空值不等失效、like最右、覆盖查询（不用回表）、explain法宝
mysql主从延迟：mysql的主从复制都是单线程的操作，DDL导致线程卡死。解决方案：从库SSD随机读；业务低峰期执行DDL；
分库分表jbdc-sharding

## redis
（https://www.cnblogs.com/4AMLJW/p/redis202004161644.html）
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
持久化：
RDB：定时fork子进程，把数据以快照的形式保存在磁盘上。效率高，备份文件，恢复快，数据丢失；
AOF：将每一个收到的写命令都通过write函数写入缓冲区，根据同步策略追加到文件中，定期对AOF文件进行重写，保持最小命令量。保证数据不丢失，基本不影响客户端命令执行。文件大，恢复效率低。
RDB-AOF：Redis 4.0新增了混合持久化。打开之后，aof rewrite 的时候就直接把 rdb 的内容写到 aof 文件开头。
生产配置：master完全关闭持久化，这样可以让master的性能达到最好；slave关闭快照持久化，开启AOF，然后关闭AOF的自动重写，然后添加定时任务，在每天Redis闲时（如凌晨12点）调用bgrewriteaof。

redis cluster集群：高可用高扩展的无中心结构（分片+主备）；client随机选择节点发起请求，节点会进行moved重定向，
容错：半数以上的master节点投票确认节点失联。1.master和slave都失联则集群不可用（可配置兼容部分失败）。2.半数master失联则不可用。
选举：1.currentEpoch表示集群状态变更的递增版本号，节点的currentEpoch越大，表示数据越新。集群中所有节点的 currentEpoch 最终会达成一致。2.选举前，从节点会等待一段时间，数据越新，等待越短。节点把自己currentEpoch+1，广播给所有master进行投票；3.master节点 2s内不会重复为同一个master进行投票。
数据分布
一致性哈希。对每一个key进行hash运算，被哈希后的结果在哪个token的范围内，则按顺时针去找最近的节点，这个key将会被保存在这个节点上。缺点（翻倍伸缩才能保证负载均衡）。
虚拟槽分区。Redis Cluster采用的分区方式。把16384槽按照节点数量进行平均分配，由节点进行管理，对每个key按照CRC16规则进行hash运算，把hash结果对16383进行取余。可以对数据打散，又可以保证数据分布均匀

codis
只是一个转发代理中间件，可以启动多个Codis 实例，供客户端使用，每个 Codis 节点都是对等的。
虚拟槽分区：每个槽位都会唯一映射到后面的多个 Redis 实例之一，Codis 会在内存维护槽位和Redis 实例的映射关系。这样有了上面 key 对应的槽位，那么它应该转发到哪个 Redis 实例。
Codis 将槽位关系存储在 zk 中，也支持etcd
扩容：支持动态、自动扩容。迁移过程中，先请求原节点，原节点强制对该key进行迁移，之后再转发到新节点。
缺点：部分命令不支持，（keys、BLPOP、PUBLISH），单key容量不宜过大，性能比原生稍差、依赖zk保障可用性、对新功能支持不友好
优点：集群原理简单，后台管理方便，运维友好。

缓存一致性：更新数据库，再删缓存,失败则发消息，重新删除。终极方案，设置超时，订阅binlog，删除key。
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


zookeeper：高可用、高性能、强一致性的分布式开源协调服务。适合同步服务、配置维护和集群命名和管理。
目录树结构，节点可以存储少量数据；
节点数据只能原子操作
节点类型：1.永久节点，显式增加和删除；2.临时节点，生命周期跟session绑定，断开后会自动删除节点；
节点自增：节点支持有序自增。
监听节点：客户端可以watch节点的增、删、改操作，客户端今收到一条消息。
分布式锁：1.客户端申请锁目录下创建临时有序节点，并返回该目录所有子节点；2.如果自己是排第一，则获取到锁，执行业务代码；3.如果不是排第一，则加锁失败；4.监听前一个临时节点的删除事件，直到收到通知消息；5.业务代码执行完，主动删除当前节点。如果创建改临时节点的客户端断开连接，zookeeper会清除该临时节点；6。zookeeper发现临时节点被删除，会通知监听该节点的客户端重新获取锁节点列表

kafka
partition：每个主题可以分为多个partition，每个partition实际存储为append log；每条消息根据时间顺序分配一个单调递增的offset，以日志的形式顺序追加到文件尾部；由生产者决定消息发送到哪个partition。
https://blog.csdn.net/lingbo229/article/details/80761778
https://www.jianshu.com/p/aa4c6994687e


流计算
jstorm
flink

分布式系统设计https://zhuanlan.zhihu.com/p/145015244

#### 高性能
并发、锁优化、缓存、异步消息、批量、连接池
#### 稳定性
限流、熔断、降级、补偿
#### 服务发现与治理
zookeeper（1.provider启动时在接口providers目录下创建临时子节点，写入自己的IP和端口；2.consumer启动时，订阅接口providers目录下所有子节点，并根据子节点信息解析提供者；consumer在接口consumers目录下创建临时子节点，写入自己的URL地址；3.增加提供者时，providers下面创建新临时子节点，zookeeper会推送节点信息给consumer；4.provider出现故障或下线时，由于临时节点与session有关，改临时节点会被自动删除；5.zookeeper宕机之后，服务提供者信息保存在内存和本地文件，无法感知到提供者变更
#### 分布式ID生成器
雪花算法64bit（41位，用来记录时间戳（毫秒），10位用来记录工作机器id（5位datacenterId 和 5位workerId），12位，序列号，用来记录同毫秒内产生的不同id）。
时钟回拨问题：1.抛异常；2.等待；3.设计时钟回拨位，发生回拨时+1。美团leaf（zookeeper生成workid，并缓存在本地；定时上报机器时间到zk，重启时保证当前机器时间>zk保存时间；RPC访问其他机器时间，偏差大则启动失败。1.关闭NTP机器时间同步。2.出现时钟重播时，返回异常、等待重试或下线该节点）。
#### 分布式锁
zookeeper锁：可靠性很高，强一致性。依赖zookeeper，可用性不足，效率一般。
redis锁：不可重入、单节点。加锁（SET anyLock current_client NX PX 30000），释放锁（lua脚本实现，如果value值为当前线程，则删除key）；重入锁通过hash结构来实现。（效率高，超时问题、分布式环境下数据同步问题、主从切换问题）
redlock：在2N+1台机器上，同时执行上面逻辑，N+1台机器执行成功则成功。（效率稍低，解决分布式环境下数据同步问题，维护成本高）
#### 分布式限流
guava ratelimiter：next（上一次取数的终止时间点）speed（频率每秒） maxPermits(最大申请量)。1.(current-next)*speed表示当前桶令牌数；2.如果桶令牌数小于permit，可以预支，next为预支后的时间点；3.next>current表示已经预支，当前不可获取。
分布式限流：基于redis实现，把ratelimiter的逻辑通过redis lua脚本实现。
#### 一致性协议
CAP理论：一致性（Consistency）、可用性（Availability）、分区容错性（Partition tolerance）
BASE是Basically Available（基本可用）、Soft state（软状态）和Eventually consistent（最终一致性）。BASE是对CAP中一致性和可用性权衡的结果，即使无法做到强一致性（Strong consistency），但每个应用都可以根据自身的业务特点，采用适当的方式来使系统达到最终一致性（Eventual consistency）。

Raft
Raft将系统中的角色分为领导者（Leader，0~1个）、跟从者（Follower，1~n个）和候选人（Candidate，0~n个）；
选举：
1. 当服务器启动时，初始化为Follower。如果Follower在选举超时时间内没有收到Leader的heartbeat，就会等待一段随机的时间后发起一次Leader选举。
2. Follower将其当前term加一然后转换为Candidate，它首先给自己投票并且给集群中的其他服务器发送投票请求。节点在同一次投票周期内，只给一个节点投票。多数的选票，成功选举为Leader；收到了Leader的消息，表示有其它服务器已经抢先当选了Leader；没有服务器赢得多数的选票，Leader选举失败，等待选举时间超时后发起下一次选举。
3. Follower如果一段时间内没有收到Leader心跳，则认为Leader挂了，重新发起新一轮选主投票
数据同步：
1. 日志包含有序编号、当前任期、命令和前一条日志的编号和任期。如果follower找不到前一条日志的编号和任期，则拒绝该日志。
2. Leader要求Followe遵从他的指令，都将这个新的日志内容追加到他们各自日志中，大多数follower收到日志并返回确认后，Leader发出commit命令。
3. Leader通过强制Followers复制它的日志来处理日志的不一致，Followers上的不一致的日志会被Leader的日志覆盖。
Zab
基本原理与raft一致
日志同步差异
ZooKeeper在每次leader选举完成之后，都会进行数据之间的同步纠正，所以每一个轮次，大家都日志内容都是统一的。
Raft在leader选举完成之后没有这个同步过程，而是靠之后的AppendEntries RPC请求的一致性检查来实现纠正过程，则就会出现上述案例中隔了几个轮次还不统一的现象
投票过程差异
Raft：每个轮次内server只能投票一次，哪个candidate先请求就获得投票，可能出现多个candidate获得票数一样的情况，Raft通过candidate设置随机不同的超时时间，那么先超时的先发起投票获得选举。
ZooKeeper：在每个轮次内，可以投多次票。遇到更大更新的日志则更新投票结果，通知所有人，不存在获取票数一样多的情况，但是时间会更长

分布式事务
两阶段（2PC）
补偿事务（TCC）
seate
https://blog.csdn.net/lovexiaotaozi/article/details/89713937
https://www.cnblogs.com/dailyprogrammer/p/12272760.html


微服务架构
业务拆分、组织架构升级
按照业务进行系统应用拆分、数据库拆分、
应用之间服务化调用
问题：问题定位、稳定性、互相依赖

Prometheus指标采集、Grafana配置监控报警、pinpoint全链路、ELK日志搜索
dubbo rpc调用、zookeeper服务发现（临时节点失效后自动删除）、Diamond配置中心、接口熔断降级限流（Hystrix），功能降级
Kafka、codis、shardingjdbc、elastic-job
cap理论



设计思想：通过巧妙的设计提高效率，避免资源的浪费
预操作：对于耗时要求极高的业务，可以在登录时进行预加载操作。以空间换时间。
实时操作：常规操作，实时查询，实时加载，定时过期。
延迟操作：对于定时删除和失效之类的操作，增加失效时间字段，通过时间判断数据失效。延后统一批量删除。

延迟操作代替定时任务实时操作：目前定时刷新等操作是通过后台定时任务执行的，单独线程消耗资源；
令牌桶：通过记录下次可用时间代替保存令牌，来节省定时添加令牌的操作。

多线程https://blog.csdn.net/tanmomo/article/details/99671622
锁https://www.cnblogs.com/lu51211314/p/10237154.html
JVM垃圾回收https://blog.csdn.net/qq_41701956/article/details/100074023

架构：https://www.cnblogs.com/jiangzhaowei/p/9570638.html

https://baijiahao.baidu.com/s?id=1684770568561071010&wfr=spider&for=pc
https://vlambda.com/wz_7imxklhPNog.html
https://www.jianshu.com/p/bf2563631d39