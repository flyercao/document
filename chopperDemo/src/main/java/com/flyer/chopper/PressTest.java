package com.flyer.chopper;

import com.flyer.chopper.core.engine.ExpressEngine;
import com.flyer.chopper.entity.Student;
import com.googlecode.aviator.AviatorEvaluator;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.internal.Engine;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * create by huirong on 2020-10-15 16:23
 */
//@Threads(4) //线程池线程数
@Threads(1) //线程池线程数

public class PressTest {
    static Map<String, Object> map;
    static String chopperExpress = "((2+a)*4/(b-2.0D)*(c%2))>1&&'eee'==e&&a*1000 > 222222222222222222222222222&&b>1&&d?'result:true':'result:false'";
    static String aviatorExpress = "((2+a)*4/(b-2.0M)*(c%2))>1&&'eee'==e&&a*1000 > 222222222222222222222222222&&b>1&&d?'result:true':'result:false'";
    static String jexlExpress = "((2+a)*4 / (b-2.0)*(c%2))>1&&'eee'==e&&a*1000 > 222222222222222222222222222&&b>1&&d?'result:true':'result:false'";

    static ConcurrentHashMap expMap=new ConcurrentHashMap();
    static ExpressEngine expressEngine = new ExpressEngine();
    static JexlEngine jexl = new Engine(new JexlBuilder().strict(true));


    static AtomicLong count = new AtomicLong(0);

    static {
        map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 20.0);
        map.put("c", 9999999L);
        map.put("d", true);
        map.put("studentA", new Student(10, "张三", 90.0));
        map.put("empty", null);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PressTest.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    @Benchmark
    public void empty() {
        map.put("a", count.incrementAndGet());
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testChopper() {
        try {
            map.put("a", count.incrementAndGet());
            Object result = expressEngine.execute(chopperExpress, map);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testAviator() {
        return testAviator(false);
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testAviatorCached() {
        return testAviator(true);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testJexl(){
            JexlExpression e = getExpression(jexlExpress);
            JexlContext jc = new MapContext(map);
            Object evaluate = e.evaluate(jc);
            return evaluate;
    }


    private static JexlExpression getExpression(String jexlExp) {
        if (expMap.containsKey(jexlExp)) {
            return (JexlExpression)expMap.get(jexlExp);
        } else {
            synchronized(expMap) {
                if (expMap.containsKey(jexlExp)) {
                    return (JexlExpression)expMap.get(jexlExp);
                } else {
                    JexlExpression expression = jexl.createExpression(jexlExp);
                    expMap.put(jexlExp, expression);
                    return expression;
                }
            }
        }
    }

    public Object testAviator(boolean cached) {
        map.put("a", count.incrementAndGet());
        return AviatorEvaluator.execute(aviatorExpress, map, cached);
    }
//
//    @Benchmark
//    @BenchmarkMode({Mode.Throughput,Mode.AverageTime})
//    public Object testAviator(){
//
//    }


}
