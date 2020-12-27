

dubbo：高性能开源RPC框架，包含容错、负载均衡和服务治理等功能。Dubbo 是由阿里开源，后来加入了 Apache。
第一层：service层，接口层，根据服务提供方和服务消费方的业务设计对应的接口和实现
第二层：config层，配置层，主要是对dubbo进行各种配置的
第三层：proxy层，服务接口透明代理，对服务接口进行动态代理
第四层：registry层，服务注册层，负责服务的注册与发现
第五层：cluster层，集群层，封装多个服务提供者的路由以及负载均衡，将多个实例组合成一个服务
第六层：monitor层，监控层，对rpc接口的调用次数和调用时间进行监控
第七层：protocol层，远程调用层，封装rpc调用
第八层：exchange层，信息交换层，封装请求响应模式，同步转异步
第九层：transport层，网络传输层，抽象mina和netty为统一接口
第十层：serialize层，数据序列化层，网络传输需要
调用流程：https://baijiahao.baidu.com/s?id=1645744285641737459&wfr=spider&for=pc

解析配置
1. dubbo.xsd文件定义dubbo标签。
2. 通过DubboNamespaceHandler，注册标签解析器，做一些特殊格式处理。
3. ServiceBean(服务提供者)与ReferenceBean(服务消费者)比较特殊，实现了Spring与Bean生命周期相关的接口。
4. 在afterPropertiesSet方法最后发布服务或获取服务。

发布服务ServiceConfig#doExport
1. 检查application、module、registries、protocols配置。
2. delay=-1,应用启动完之后再发布；delay>0，则交由ScheduledExecutorService延迟发布。如果不延迟启动，则doExport。
3. 填充属性默认值。顺序是系统参数 -> dubbo.properties ->ServiceBean
4. 遍历所有注册中心、所有协议，构建所有URL对象。method、generic、injvm、IP和端口
5. 根据配置的注册协议找到注册实现类ZookeeperRegistry，
6. 创建Netty Server时，传入1、解码器；2、编码器；3、业务类NettyHandler。服务端收到网络读事件后，将二进制流进入解码器，解析为RPC请求，然后交给NettyServer去处理。
7. NettyServer根据请求协议和策略，决定是转给业务线程还是IO线程自己处理。

zookeeper服务注册于发现
1. 在 dubbo/目录下创建服务目录，以服务名命名。dubbo/serviceName目录下分别有consumers、providers、configurators和routers目录。服务提供者和消费者分别往目录下写入URL信息。
2. 服务提供者在暴露服务时，会在${service interface}/providers目录下添加一个临时节点，服务提供者需要与注册中心保持长连接，连接断掉会话信息失效后，注册中心会认为该服务提供者不可用（提供者节点会被删除）。
3. 消费者在启动时，首先也会向注册中心注册自己，具体在${interface interface}/consumers目录下创建一个临时节点
2. 消费者监听上述目录的增删改事件。解析事件通知，重构服务调用器(Invoker)。并且将信息写入本地缓存和文件缓存

动态配置
dubbo管理员可以通过dubbo-admin管理系统在线上增加动态配置来修改dubbo服务提供者的参数。
动态配置保存在注册中心的configurators目录，通过更新机制通知消费者。
override覆盖OR absent追加、特定IP、服务名、持久生效、指定应用、超时时间等

动态路由
dubbo管理员可以通过dubbo-admin管理系统在线上增加动态路由来调整服务的路由规则。
动态配置保存在注册中心的routers目录，通过更新机制通知消费者。
**条件路由规则** =>之前的为消费者匹配条件， =>之后为新的路由规则。
**脚本路由规则**通过脚本定义路由规则。支持 JDK 脚本引擎的所有脚本，javascript, jruby, groovy
**标签路由规则**当服务提供者选择装配标签路由(TagRouter)且消费方在请求attachment添加tag后，每次 dubbo 调用能够根据请求携带的 tag 标签智能地选择对应 tag 的服务提供者进行调用。

网络通信NIO
1.服务端初始化：绑定端口，设置网络参数；添加解码器、编码器、处理handler；
客户端建立与服务端连接，此时Boss线程的连接事件触发，建立TCP连接，并向IO线程注册该通道(Channel0)的读事件；
客户端向服务端发送请求消息后，IO线程中的读事件触发，会首先调用adapter.getDecoder() 根据对应的请求协议（例如dubbo）从二进制流中解码出一个完整的请求对象，然后传入到业务handler,例如nettyServerHandler，执行相应的事件方法，例如recive方法。
服务端写入响应结果时，首先编码器会按照协议编码成二进制流，再写入。

消息派发模型：定义消息的执行线程，请求、响应、连接、断开、心跳消息分别是由IO线程还是dubbo线程处理。
Dubbo线程池的构建模式：
fixed : 固定大小线程池，启动时建立线程，不关闭，一致持有。线程池使用SynchronousQueue（默认）或LinkedBlockingQueue，活跃时间无限。（默认）
cached ：缓存线程池，空闲一分钟，线程会消费，需要时重新创建新线程。线程池使用SynchronousQueue（默认）或LinkedBlockingQueue，活跃时间默认一分钟。
limited ：可伸缩线程池，但池中的线程数只会增长不会收缩。线程池使用LinkedBlockingQueue，活跃时间无限。
eager ：优先使用线程来执行新提交任务。优先使用新线程，线程用光了再入队列。

负载均衡
服务调用方对服务提供者的选择是通过负载均衡算法实现，random、roundrobin和leastactive都提供了权重机制。并且通过权重机制，在服务提供者预热未完成时，实现了接口的客户端预热
random随机：生成(0-totalWeight)范围内随机数offset，如果减去(offset=（offset - provider.weight） < 0),则该invoker命中；如果权重相同，则直接随机即可。
roundrobin轮训：生成递增的随机数offset，纵向遍历提供者（外层循环maxWeight次，内层循环invokerSize次，每次offset-1，当前的提供者weight-1，忽略weight=0的，当offset=0时，该提供者就是服务提供者）。
leastactive最少活跃：ActiveLimitFilter负责在调用服务前+1，调用完成后-1来统计服务的活跃数，活跃数小说明处理快。首先遍历所有服务提供方，统计最小活跃数数组和对应的权重；如果只有一个，则返回；否则执行权重随机算法；
consistenthash一致性hash：虚拟环境分配2的32次方个节点，每个hash值都能对应一个节点。为了避免分配倾斜，每个服务提供方分配160个虚拟节点，对应环上160个节点；把请求进过hash值计算，对应到环上的节点，然后顺时针方向寻找遇到的第一个虚节点，让该虚拟节点对应的服务方提供服务。
自定义实现：通过SPI机制，继承AbstractLoadBalance类，实现doSelect方法。

集群容错
如果某次调用服务方失败，会根据集群容错机制决定是否重试。根据待选列表、已选列表，如果设置sticky机制（粘性），且调用过程无错误，后续会直接选择该提供者，默认不开启。
Failover：根据retries属性设置次数，失败后自动选择其他服务提供者进行重试，默认重试两次。默认。
Available：选择集群第一个可用的服务提供者。相当于服务的主备，但同时只有一个服务提供者承载流量，并没有使用集群的负载均衡机制。
Failfast：快速失败，不会进行重试。适合不支持幂等行接口。
Broadcast：广播调用，将调用所有服务提供者，一个服务调用者失败，并不会熔断，并且一个服务提供者调用失败，整个调用认为失败。适合刷新缓存等。
Failback：调用失败后，返回成功，但会在后台定时任务重试，重试次数（反复）。通常用于消息通知，但消费者重启后，重试任务丢失。
Failsafe：调用失败，返回成功。调用审计测试等，日志类服务接口。
Forking：并行调用多个服务提供者，当一个服务提供者返回成功，则返回成功。实时性要求比较高的场景，但浪费服务器资源，通常可以通过forks参数设置并发调用度。

序列化MonitorFilter
Dubbo支持多种序列化协议，java、compactedjava、nativejava、fastjson、fst、hessian2、kryo，其中默认hessian2。
Serialization(序列化策略)、DataInput(反序列化，二进制----》对象)、DataOutput（序列化，对象----》二进制流）

dubbo协议

消费者调用流程
https://blog.csdn.net/weixin_33828101/article/details/88811762?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control
https://segmentfault.com/a/1190000017823251
http://www.tianxiaobo.com/

提供者处理流程
https://segmentfault.com/a/1190000019420778?utm_source=tag-newest

Filter机制
在真正的请求前后做一些相应的通用的处理，权限的验证，日志的打印。在服务暴露和服务发现和订阅的时候，会组装好filter的调用链路。
可以通过实现Filter接口的invoke方法来自定义实现。指定group为provider、consumer或二者，order调用顺序，value过滤条件。
ConsumerContextFilter设置上下文、traceId等；
MonitorFilter：统计调用量等信息，上报到monitor；收集并发调用数、当前时间、调用耗时、异常等数据。
FutureFilter：执行事件通知，调用前（oninvoke）、同步调用后/异步调用完成后（onreturn/onthrow）的方法实现。
ActiveLimitFilte：客户端并发度控制。控制每个consumer调用指定方法的最大并发数，主要通过actives参数进行配置，在每次调用的时候活跃数+1，调用结束活跃数-1。如果获取不到，则synchronized/wait方式锁定，在超时范围内等待。最少活跃数负载均衡也有关系。
ExecuteLimitFilter：服务端并发度。老版本使用Semaphore信号量来控制并发数，2.7之后使用AtomLong来计数。
DefaultTPSLimiter：Tps限流。当服务调用者超过其TPS时，直接返回rpc exception。默认统计时长为一分钟。限流器使用漏桶算法（如果当前时间大于（上一次刷新时间+统计间隔），重新复位token为rate；每使用一次，消耗一个token；成功返回true；失败返回false）
ExceptionFilter：全局异常处理。

异步调用与事件通知
异步调用：客户端基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可并行调用多个服务，线程开销小。sent可以设置是否等待消息发出；return可以设置是否创建future返回结果。DubboInvoke实现调用方式
oneWay：只管调用，不管结果。根据sent参数决定是等待网络数据发出还是只写入缓存区。
异步：不管是同步还是异步，都是Future模式进行调用。异步模式则直接返回Future Response。用户需要时才调用get方法。
同步：同步模式下，则直接调用get方法，等待返回结果。
bug：异步调用模式下，dubbo异步调用具有传递性，不过只会传递一次。ServiceA异步调ServiceB，ServiceB再同步调ServiceC，此时ServiceC会当异步调用，返回结果为null。

事件通知：客户端在调用之前或之后，会触发oninvoke、onreturn、onthrow三个事件，可以配置事件发生时，回调的类和方法。根据是否同步和是否回调组合4种方式。异步回调、同步回调、异步无回调、同步无回调。
通过FutureFilter来实现的异步回调机制。FutureFilter在实际调用前同步触发oninvoke方法，通过反射执行方法；在实际调用后，同步或者异步调用onreturn或onthrow方法。

异步执行
1. 定义接口返回结果为CompletableFuture类型，使业务执行已从 Dubbo 线程切换到业务线程，避免了对 Dubbo 线程池的阻塞。
2. 通过AsyncContext手动传递上下文，异步执行逻辑，再写Response。

全异步网关
结合服务端异步执行AsyncContext+客户端异步调用+客户端事件通知实现高性能网关。
1.服务A的dubbo业务线程收到请求进行业务处理后，异步调用依赖服务B，并且在Future上绑定事件通知接口（onReturn和onThrow），直接返回；
2.netty的io线程没有分发Response响应事件，所有实际收到服务B的响应后，是netty的IO线程来处理；
3.IO线程收到响应后，根据请求id找到对应的Future，调用Future的ReentrantLock.condition.signal()进行通知，并且执行Future关联的通知事件；
4.在事件通知接口（onReturn和onThrow）里，使用AsyncContext.write手动返回结果；

降级
客户端在调用服务时，由于非业务异常导致的服务不可用时（服务器宕机/网络超时/并发数太高），可以返回默认值，避免异常影响主业务的处理。
mock 配置：mock="true"  或者mock="return null"；配置true，并且接口调用失败时，会调用mock实现类（缺省使用类名+Mock后缀）。还可以通过dubbo admin手动配置强制mock和失败mock，实际上是在zk上创建configurators的子节点，覆盖接口配置；
Dubbo降级通过Filter实现。通过一个记录器对每个方法出现RPC异常进行记录，并且可以配置在某个时间段内连续出现多少个异常可判定为服务提供端出现了宕机，从而进行服务降级；
通过配置检查服务的频率来达到定时检查远程服务是否可用，从而去除服务降级。
降级配置分配为应用级别，接口级别，方法级别 ；配置项包括break.limit出现多少个异常则降级；retry.frequency表示出现多少个异常则重试；circuit.break服务降级功能开关。

泛化
泛化调用：调用方不依赖服务提供方接口，参数及返回值均用map表示。常用语框架集成、测试框架或配置化动态调用等场景。
GenericImplFilter负责具体实现。1.解析服务接口方法名、参数类型数组和参数值数组，统一封装成Generic$INVOKE接口的参数。2.调用GenericImpl.$invoke方法进行远程调用；3.如果返回正常，将结果转换成map格式返回；4.如果是返回GenericException异常，则转化为原始异常并返回；5.其他异常直接返回。
泛化实现：服务提供方不依赖具体的接口，参数及返回值均用map表示。比如实现一个通用的Mock接口。或服务中转功能。
GenericFilter负责具体实现。1.将Generic.$invoke接口参数反射为java对象；2.调用具体实现。3.返回结果封装为map格式；4.如果返回异常，则封装为GenericException异常。


accessLog
访问日志通过AccessLogFilter实现。Constants.$INVOKE
配置项accesslog="true" 表示使用log4j等日志组件，打印info级别调用日志；
配置项accesslog="/logs/accesslog.log" 则表示dubbo异步写入日志文件。 
默认不建议开启，对性能有一定影响。排查问题时，可通过dubbo admin动态配置开启。

监控中心
MonitorFilter将调用数据传给监控中心（默认DubboMonitor），DubboMonitor先在内存中进行数据统计。
DubboMonitor通过后台定时任务将统计数据RPC发送到独立的监控中心。
监控中心把数据保存在本地文件。

设计模式

灰度发布方案
希望根据请求，某些请求走新版本服务器，某些请求走旧版本服务器，其本质就是路由机制，即通过一定的条件来缩小服务的服务提供者列表，通过dubbo router实现。
通过dubbo admin界面，配置路由脚本，根据请求参数，返回目标服务提供者列表。

SPI
SPI是JDK内置的一种服务提供发现机制，通过插件配置的形式给应用添加功能。dubbo框架通过SPI机制实现内核与扩展点的动态关联。SPI 的缺点。1.JDK 标准的 SPI 会一次性加载实例化扩展点的所有实现，浪费资源；2.如果扩展点加载失败，会导致调用方报错；
Dubbo SPI进行了优化；提供自适应扩展、指定名称扩展和激活扩展。
https://segmentfault.com/a/1190000024443652?utm_source=sf-related
