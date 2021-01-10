

JVM优化
https://blog.csdn.net/weixin_42447959/article/details/81637909
优化目标：内存占用;延迟;吞吐量
工具：应用日志、堆栈错误信息、GC日志、线程快照、堆快照
jps（找到应用进程pid）
jstate 内存分布
top -H -p（分析CPU占用高的线程）
jstack 分析线程状态和栈信息
jmap （分析类实例和空间占用，dump堆内存到文件）
建议：-Xms和-Xmx的值设置成相等，避免动态调整内存大小；新生代尽量设置大一些；根据gc日志，结合延迟目标，分配新生代大小和比例
短命大对象和数组；大循环创建对象；大批量处理数据；强引用缓存集合；长时间占用对象。
### 内存问题排查：
现象：离线应用发布时接口超时，规律性full gc，同时实时应用接口查询超时，几乎同时。
分析：查看应用配置无明显问题；
查看超时原因，redis查询超时。超时redis命令主要是hgetall和mget，但分析发现这些命令并不会持续超时，只在某个特定时间点全部超时。
监控显示hgetAll命令耗时异常。怀疑是redis服务端的问题：然后找DBA分析了redis慢查询命令，发现有一条hgetall命令耗时5s，value是所有车牌号和车架号的对应关系，超大key导致了redis超时。
经过代码分析，发现应用在启动时，从redis加载所有数据到本地缓存，并且定时重新加载。

假设：初步怀疑是大量缓存数据导致的full gc。
验证：在业务低峰期，jstat -gcutil  发现old区一直在增长；添加gc日志：正常迁移；多次jmap -histo:live 内存数据进行比对分析，发现hashmap.Node实例数稳定持续增加。
移除本地缓存代码后，redis超时没有了，慢查询命令也没有了。dump内存发现hashmap.entry实例数保持稳定。
原因：1.由于车辆换牌等，定时本地缓存更新，查询redis大对象阻塞了redis线程，导致其他查询超时；2.频繁更新本地缓存数据，导致老年代存在大量垃圾对象，引起full gc。
### 应用假死
现象：应用启动超时，线程假死。
分析：1.应用没有报错日志，应用进程还在，没有退出。2.分析日志，发现发布过程中，应用在初始化spring bean的过程中假死，线程无响应。3.检查了变更代码，没有发现明显异常。
jstack 查看线程状态，发现初始化bean的线程状态为blocked，然后分析线程堆栈是阻塞在MongoDB执行代码，进一步分析，发现该代码是在创建索引。
假设：创建索引是个耗时动作，导致应用启动线程被阻塞，应用出现假死。
验证：在线下人工后台创建索引，移除该部分代码。重新发布后，应用启动正常。


### 活动应用性能优化
现状：单机压测qps在200不到，rt已经超过200ms，接口熔断了
目标：单机qps到1000，rt小于200ms。
排查：
现象：0.机器是4核8g；1.load到2；2.cpu20%；3.网络状态正常；4.rt超时 5.qps150
1. （日志异步）top和jstack命令，发现waiting业务线程，堆栈出现大量logback调用，结合arths工具，发现打印日志耗时几十ms。线程阻塞在ReentrantLock.lock方法。（logback已经配置了异步appender），分析阻塞原因。发现有个neverBlock属性，默认值为false。调用blockingQueue.put阻塞方法追加日志。如果为true，则通过blockingQueue.offer非阻塞方法追加日志。于是添加了neverBlock=true的配置。
qps到800，rt200ms以内。CPU30%；load5；rt超时
2. （异步发送kafka消息）同样发现了策略命中数量统计的方法耗时长，频繁多次发送kafka消息。优化成 disruptor异步发送和写日志发送。
qps在1000，dubbo线程数不足。
3. （Fastjson优化）还是top和jstack命令，发现大量出现JSON.toJSONString方法，结合arths工具，发现耗时0.1ms。调整为对象toString方法。
4. 发现线程不足，但是大量dubbo线程出于waiting状态，实际执行线程不到10 。
通过对dubbo线程分发模式进行研究，进行异步改造。结合服务端异步执行AsyncContext+客户端异步调用+客户端事件通知实现异步网关。（减少线程waiting时间，提高dubbo线程利用率，减少线程切换开销）
改造后内部rt降低到2ms+1ms，频繁ygc和fgc。
5. 采用高性能脚本引擎，替换Apache jexl；
6. hbase稳定性优化（关闭数据随机合并，指定夜间进行）
7. dubbo接口优化，并行请求，future异步接受结果。
8. mysql分库分表，（几亿）手机号风险库分64个表
5. （内存）内存不足。增加内存到6g，新生代2g，替换为g1垃圾回收器。（缓存、预热、超时控制、）
QPS达到5000+

1. top -H -p pid 命令和  jstack命令，分析占CPU高的线程；发现是apache jexl在解析和aqs执行脚本（解释型脚本解析器，非常占CPU）；升级机器为8核16g机器，重新压测
qps到400；CPU80%；load10；每秒4次YGC，30ms，没有full gc
2. 2g内存，新生代680M；jstate -gcutil 分析内存回收情况，新生代快速占满回收，没有向老年代迁移；扩大内存到4g；
qps到500，load不变，rt下降到180ms，YGC评率降低每秒一次，20ms；ioutil升高。

