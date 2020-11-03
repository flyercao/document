# chopper

## 简介
   chopper是一个功能丰富、性能高效、扩展方便的表达式解析器。
### 原理
   Chopper的基本原理是通过Antlr4将表达式文本解析为语法树，遍历语法树翻译成对应的Class类源代码，加载、编译、实例化得到对应的类对象实例，最后传入上下文参数并调用实例方法，得到最终的执行结果。
   相比于其他解释性的表达式引擎，Chopper采用编译模式执行，保证高效的执行性能。同时， Chopper定位是表达式执行引擎，主要支持数字计算、逻辑关系判断、字符处理等核心功能，默认不支持文件访问、网络IO等高阶功能（用户可以通过自定义函数的方式实现）。
### 特性
1. 支持绝大多数运算操作符，包括算术操作符、关系运算符、逻辑操作符、 三元表达式(?:)、正则匹配。
2. 支持操作符优先级和括号强制设定优先级。
3. 支持访问上下文变量，支持自定义临时变量。
3. 支持多表达式运算，返回最后一个表达式运算结果。
4. 支持Double、Integer、String、Boolean、List、Map、BigInteger、BigDecima等类型，还支持访问对象属性和子属性。
1. *性能优秀*，通过直接将脚本翻译成 java代码，再动态编译运行，性能接近原生java代码。


## 使用手册

### Hello world
Chopper是一种依附于java的表达式计算引擎，必须在java环境中才能运行。注意，Chopper 仅支持 JDK 8 及以上版本。
首先，在java 项目里添加 Chopper 的依赖：
```
<dependency>
    <groupId>com.flyer</groupId>
    <artifactId>chopper</artifactId>
    <version>0.4</version>
<dependency/>
```
编写一个测试类，在main方法执行Chopper表达是脚本，

```
public class ChopperEngineTest {


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        try {
            Object result = new ExpressEngine().execute("a+b", map);
            System.out.println("a+b="+result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
```

运行得到如下结果
```
a+b=3

```
这段代码非常简单，创建ExpressEngine实例，调用execute方法，传入脚本和上下文参数，执行得到结果 。

### 多行表达式

Chopper支持多行表达式，表达式之间通过换行符和英文分号(;)进行分隔。对于多行表达式求值的最终结果将是最后一个表达式的结果。
例如ExpressEngine.execute("var o=a+b;o=c*2;println('o='+o);o+100000")的结果将是最后一个表达式 o+100000,但是中间的表达式也将执行，包括执行println('o='+o)。通过在表达式求值过程中加入 println函数，可以方便调试。


### 类型

#### 数字类型

Chopper支持常用数据类型，例如double、long、int、float、String、boolean、以及BigInteger、BigDecimal、List、array等数据类型。
• 任何超过Long类型最大值和最小值的整数都被认为是 BigInteger 类型。
• 任何以大写字母 D 结尾的数字都被认为是 BigDecimal 类型。
• 其他的任何整数都将被转换为 Long。
• 其他任何浮点数都将被转换为 Double。
• 超过 Long 范围的整数字面量都将自动转换为 big int 类型
```$xslt
        String express = "((2+a)*4/(b-2.0D))>1&&b =~[2,4,5] &&'eee'==e&&a*1000 > 222222222222222222222222222&&b>1&&d";
        HashMap<String, Object> context = new HashMap<>();
        context.put("a", 1);
        context.put("b", 8.0);
        context.put("c", 9.009);
        context.put("d", true);
        context.put("e", "eee");
        ExpressEngine expressEngine = new ExpressEngine();
        Object result = expressEngine.execute(express, context);
```
得到如下结果
```$xslt
false
```
注意：对于float和int类型，Chopper引擎执行时会先转换为Double和Long类型，在再执行计算。所以，如下表达式计算结果分别为java.lang.Double和java.lang.Long
```$xslt
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1.0);
        map.put("b", 2);
        map.put("c", 3);
        String express = "a+b";
        Object result = new ExpressEngine().execute(express, map);
        System.out.println(result.getClass().getName());

        express = "b+1";
        result = new ExpressEngine().execute(express, map);
        System.out.println(result.getClass().getName());
```

#### 字符串型
Chopper支持String字符串类型。单引号或者双引号括起来的文本串,如'你好'或"Hello world!"。

#### 布尔类型
Chopper支持Boolean布尔类型: 常量true和false,表示真值和假值,与 java 的Boolean.TRUE和Boolean.False对应。


### 操作符

#### 算术运算符
Chopper 支持常见的算术运算符,包括+ - * / %五个二元运算符,和一元运算符-(负)。其中- * / %和一元的-（负）仅能作用于数字类型。
+不仅能用于Number类型,还可以用于String的相加,或者字符串与其他对象的相加。任何类型与String相加,结果为String类型。

#### 逻辑运算符
Chopper 的支持的逻辑运算符包括,一元否定运算符!、逻辑与&&,逻辑或||。逻辑运算符的操作数只能为Boolean类型，返回结果也是true或false。
#### 关系运算符
Chopper 支持的关系运算符包括<, <=, >, >=以及==和!= 。
关系运算符可以作用于Number之间、String之间、Boolean之间、变量之间进行比较，不同的类型之间不能相互比较。

#### 位运算符
Chopper 支持所有的 Java 位运算符,包括&, |, ^, ~, >>, <<, >>>，计算逻辑也是与Java原生的位运算符一致。
#### 匹配运算符
匹配运算符=~用于对象与正则表达式之间的匹配,它的左操作数必须为String,右操作数为正则表达式脚本。
#### 三元运算符
Chopper 支持三元运算符?: ，使用方式与Java三元计算符一致。形式为bool ? exp1: exp2。 其中bool必须为Boolean类型的表达式, 而exp1和exp2可以为任何合法的 Chopper 表达式,对expr1和expr2表达式的返回结果也没有任务要求。

### 变量

#### 上下文变量
Chopper支持通过Map<String,Object>类型的上下文进行参数传递，上下文变量传递支持数字类型、字符串类型、布尔类型以及对象类型。
```$xslt
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("studentA",new Student(10,"张三",90.0));
        String express = "a+b-c*2";
        Object result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());
       
        express = "studentA.age+b-c*2";
        result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());
```
输出结果为
```$xslt
result : -3
result : 6

```
Chopper 支持对象属性访问。 当你要访问变量a中的某个属性b, 那么你可以通过a.b访问到, 更进一步, a.b.c将访问变量a的b属性中的c属性值, 也就是说 Chopper 支持对象的嵌套访问，
只要对象类型符合JavaBean规范, 并且访问方法是 public 的。

#### 自定义变量
Chopper引擎还支持用户自定义变量，使用var 加 变量名来自定义变量。如
```$xslt
        express = "var o=a+b;o=c*2;o+100000";
        result = new ExpressEngine().execute(express, map);
```
执行结果为
```$xslt
result : 100003.0
```

### 函数
Chopper支持函数调用，包括Chopper系统函数、用户自定义函数以及Java静态函数。同时，也支持函数的嵌套调用。
#### 系统函数
Chopper目前支持的系统函数包括println、getFirstNonNull、empty。
```
        express = "var o=a+b;o=c*2;println(empty(o));empty(o)";
        result = new ExpressEngine().execute(express, map);
```
输出内容
```$xslt
false
result :false
```

#### Java静态函数
Chopper支持调用Java类的静态函数
```
        FunctionInstanceManager.addStaticFunction(RuntimeUtils.class);//添加需要调用的静态类

        
        express="'张三的成绩是'+String.valueOf(98)+'分'";
        result= new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());
```
输出内容
```$xslt
result : 张三的成绩是98分
```

#### 用户自定义函数
如果系统函数和Java静态函数还不能满足需求，用户也可以自定义函数。只要实现ChopperFunction接口或继承AbstractChopperFunction类即可。

下面看一下stringToUpper函数实现字符串转大写。
```$xslt

public class StringToUpperFunction extends AbstractChopperFunction {


    private static final long serialVersionUID = -1592661531857237654L;


    @Override
    public String getName() {
        return "stringToUpper";
    }


    @Override
    public ChopperObject call(Map<String, Object> env, ChopperObject arg1) {
        Object value = arg1.getValue(env);
        if (Objects.isNull(value)) {
            throw new NullPointerException();
        } else if (value instanceof String) {
            return ChopperString.valueOf(((String)value).toUpperCase());
        } else if (value instanceof ChopperString) {
            return ChopperString.valueOf(((ChopperString) value).getValue(env).toUpperCase());
        } else if (value instanceof ChopperJavaType) {
            Object javaValue = ((ChopperJavaType) value).getValue(env);
            if(javaValue instanceof String){
                return ChopperString.valueOf(((String) javaValue).toUpperCase());
            }
            return ((Map) value).isEmpty() ? ChopperBoolean.TRUE : ChopperBoolean.FALSE;
        }
        throw new IllegalArityException("function "+getName()+" arg1 error!");
    }

}


        FunctionInstanceManager.addFunction(new StringToUpperFunction());
        express = "stringToUpper('zhangsan')";
        result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());
```
输出
```$xslt
result : ZHANGSAN
```

有时，自定义函数的参数个数比较多，或者参数个数不确定的情况下，可以定义不定参数的函数。只需要继承AbstractVariadicFunction，并实现其中的variadicCall方法即可。
```$xslt

public class StringJoinFunction extends AbstractVariadicFunction {

    public ChopperObject variadicCall(Map<String, Object> env, ChopperObject... args) {
        StringBuilder sb=new StringBuilder();
        if (args != null) {
            for (ChopperObject arg : args) {
                if (arg.getValue(env) != null) {
                    sb.append(arg.getValue(env).toString());
                }
            }
        }
        return new ChopperString(sb.toString());
    }


    @Override
    public String getName() {
        return "stringjoin";
    }

}

        FunctionInstanceManager.addFunction(new StringJoinFunction());
        express = "stringjoin('a=',a,',b=',b)";
        result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());



        FunctionInstanceManager.addFunction(new StringJoinFunction());
        express = "stringjoin('a=',a,',b=',b,',c=',c)";
        result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());

```


输出结果
```$xslt
result : a=1.0,b=2
result : a=1.0,b=2,c=3
```

#### 加载函数
除了调用FunctionInstanceManager.addFunction方法加载用户自定义函数之外，Chopper还支持使用SPI方式加载自定义函数（[关于SPI]）。
先看个例子。假设需要自定义一个函数，查询入参里面的最大值并返回。首先，还是新建自定义函数类FindMaxNumber，实现ChopperFunction接口或继承AbstractChopperFunction类
```$xslt

public class FindMaxNumber extends AbstractVariadicFunction {


    @Override
    public String getName() {
        return "findMaxNumber";
    }

    @Override
    public ChopperObject variadicCall(Map<String, Object> map, ChopperObject... chopperObjects) {

        BigInteger bigInteger = Stream.of(chopperObjects).map(o -> new BigInteger("" + o.numberValue(map))).max((o1, o2) -> o1.compareTo(o2)).get();
        return new ChopperBigInt(bigInteger);
    }
}

```
然后在资源目录下新建目录`META-INF/services/`,然后新建`com.flyer.chopper.core.runtime.function.ChopperFunction`文件，文件内容填写函数类的全类名`xxx.xxx.xxx.FindMaxNumber`。
编写脚本执行自定义函数`findMaxNumber`
```$xslt
    public static void testFunction() throws Exception{

        String express = "findMaxNumber(1,2,-9,99999,-99999999999999999999999,c-999999999999999999999999999999)";
        Object result = new ExpressEngine().execute(express, map);
        System.out.println("result:"+result);
    }
```
执行输出结果
```$xslt
result:99999
```


### 注释
Chopper脚本支持单行注释`//`和多行注释`/*  */`,使用方式与java注释语法一致。
```$xslt

        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        String express = "" +
                "// 单行注释\n" +
                "/* 多行注释1" +
                "\n多行注释2" +
                "\n多行注释3 \n" +
                "*/ \n" +
                "a+b-c*2;";
        Object result = new ExpressEngine().execute(express, map);
        System.out.println("result : " + result.toString());
```

执行结果
```$xslt
result : -3
```


[关于SPI]: https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html