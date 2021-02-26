
MVC的各个部分都由那些技术来实现?
AOP：设计模式 
IOC：反射机制

循环依赖
Spring只能解决Setter方法注入的单例bean之间的循环依赖
通过提前暴露（在set属性之前暴露bean），解决bean之间的相互依赖。

## 事务传播机制
 REQUIRED 如果有事务, 那么加入事务, 没有的话新建一个(默认情况下)
 NOT_SUPPORTED 容器不为这个方法开启事务
 REQUIRES_NEW 不管是否存在事务,都创建一个新的事务,原来的挂起,新的执行完毕,继续执行老的事务
 MANDATORY 必须在一个已有的事务中执行,否则抛出异常
 NEVER 必须在一个没有的事务中执行,否则抛出异常(与Propagation.MANDATORY相反)
 SUPPORTS 如果其他bean调用这个方法,在其他bean中声明事务,那就用事务.如果其他bean没有声明事务,那就不用事务.
 PROPAGATION_NESTED 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION_REQUIRED类似的操作。