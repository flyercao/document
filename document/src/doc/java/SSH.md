
MVC的各个部分都由那些技术来实现?
IOC：指创建对象的控制权转移给Spring框架进行管理，使用方不需要关心对象的创建，由Spring根据配置文件去创建实例和管理各个实例之间的依赖关系，使对象与对象之间松散耦合。构造器注入、setter方法注入、根据注解注入。

AOP：用于将那些与业务无关，但却对多个对象产生影响的公共行为和逻辑，抽取并封装为一个可重用的模块，这个模块被命名为“切面”（Aspect），减少系统中的重复代码，降低了模块间的耦合度，提高系统的可维护性。可用于权限认证、日志、事务处理。
AspectJ是静态代理。在**编译阶段**生成AOP代理类，并将AspectJ(切面)织入到Java字节码中，运行的时候就是增强之后的AOP对象。
CGLIB和JDK动态代理。不会在编译时去修改字节码，而是每次运行时在内存中临时为方法生成一个新的对象，这个对象包含了目标对象的全部方法，并且在特定的切点做了增强处理。

BeanFactory和ApplicationContext区别？
BeanFactory是Spring最底层接口，IOC实现核心，管理bean的生命周期；BeanFactroy延迟加载bean；
ApplicationContext接口作为BeanFactory的子类，提供了BeanFactory之外的国际化、资源访问ResourceLoader等功能；ApplicationContext启动时创建所有Bean，有利于及时发现问题。

Spring Bean 的生命周期：实例化 -> 属性赋值 -> 初始化 -> 销毁。实例化和属性赋值对应构造方法和setter方法的注入，初始化和销毁是用户能自定义扩展的两个阶段。

循环依赖
Spring只能解决Setter方法注入的单例bean之间的循环依赖
通过Spring提前暴露的Bean实例的引用在第三级缓存中进行存储，解决bean之间的相互依赖。

## Spring事务的实现
声明式事务管理建立在AOP之上，对方法前后进行拦截，在目标方法开始之前启动一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。事务上下文保存在ThreadLocal。
唯一不足地方是，最细粒度只能作用到方法级别，无法做到像编程式事务那样可以作用到代码块级别。

## 事务传播机制
 REQUIRED 如果有事务, 那么加入事务, 没有的话新建一个(默认情况下)
 NOT_SUPPORTED 容器不为这个方法开启事务
 REQUIRES_NEW 不管是否存在事务,都创建一个新的事务,原来的挂起,新的执行完毕,继续执行老的事务
 MANDATORY 必须在一个已有的事务中执行,否则抛出异常
 NEVER 必须在一个没有的事务中执行,否则抛出异常(与Propagation.MANDATORY相反)
 SUPPORTS 如果其他bean调用这个方法,在其他bean中声明事务,那就用事务.如果其他bean没有声明事务,那就不用事务.
 PROPAGATION_NESTED 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION_REQUIRED类似的操作。
 
 ## 隔离级别
  ISOLATION_DEFAULT：这是个 PlatfromTransactionManager 默认的隔离级别，使用数据库默认的事务隔离级别。
 ② ISOLATION_READ_UNCOMMITTED：读未提交，允许事务在执行过程中，读取其他事务未提交的数据。
 ③ ISOLATION_READ_COMMITTED：读已提交，允许事务在执行过程中，读取其他事务已经提交的数据。
 ④ ISOLATION_REPEATABLE_READ：可重复读，在同一个事务内，任意时刻的查询结果都是一致的。
 ⑤ ISOLATION_SERIALIZABLE：所有事务逐个依次执行。