数据结构

![github](https://upload-images.jianshu.io/upload_images/2615789-1345e368181ad779.png) 


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


hbase：高可靠性、高性能、面向列、可伸缩、 实时读写的海量数据分布式数据库
批处理


流计算
jstorm
flink

分布式系统设计https://zhuanlan.zhihu.com/p/145015244

#### 高性能
并发：多线程数量，线程利用率，减少线程阻塞和等待；
异步：异步队列（生产者消费者）、异步日志log4j2、异步发消息、dubbo future异步
锁优化：synchronized-》reentrantlock-》CAS -》disruptor
缓存：本地缓存、分布式缓存（画像提前缓存，指标缓存）
批量：批量写hbase、MongoDB、mysql数据库
连接池：http连接池，数据库连接池。redis
预计算：登录预计算、预加载缓存
高性能引擎：采用自研编译型脚本引擎替换解释型的jexl引擎。
#### 稳定性
隔离、限流、熔断、降级、补偿，业务监控、系统监控、报警
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