
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
/**
 * create by huirong on 2020-10-15 16:23
 */
//@Threads(4) //线程池线程数
@Threads(1) //线程池线程数

public class ThreadPerformanceTest {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ThreadPerformanceTest.class.getSimpleName())
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    long count=0;


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
    public Object testClass() {
        try {
            map.put("a", count.incrementAndGet());
            return class_1.execute0(map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testStaticClass() {
        try {
            map.put("a", count.incrementAndGet());
            return Class_1.executeStatic(map);
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
        map.put("a", count.incrementAndGet());
        JexlExpression e = getExpression(jexlExpress);
        JexlContext jc = new MapContext(map);
        Object evaluate = e.evaluate(jc);
        return evaluate;
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public Object testMVEL(){
        map.put("a", count.incrementAndGet());
        Object o = MVEL.executeExpression(compiled, map);
        return o;
    }

    private synchronized void syncIncOne(){
        count++;
    }


    private  void incOne(){
        count++;
    }




//
//    @Benchmark
//    @BenchmarkMode({Mode.Throughput,Mode.AverageTime})
//    public Object testAviator(){
//
//    }



//
//# Run complete. Total time: 00:13:44
//
//    Benchmark                           Mode  Cnt         Score        Error  Units
//    PerformanceTest.empty              thrpt   30  24779287.417 ± 773143.461  ops/s
//    PerformanceTest.testAviator        thrpt   30      2968.599 ±    428.098  ops/s
//    PerformanceTest.testAviatorCached  thrpt   30   5033455.929 ± 604856.916  ops/s
//    PerformanceTest.testChopper        thrpt   30   3287559.464 ± 104267.877  ops/s
//    PerformanceTest.testClass          thrpt   30   4877965.100 ± 213618.661  ops/s
//    PerformanceTest.testJexl           thrpt   30   2738475.946 ± 144855.063  ops/s
//    PerformanceTest.testStaticClass    thrpt   30   5163843.372 ± 424832.773  ops/s
//    PerformanceTest.testAviator         avgt   30         0.001 ±      0.001   s/op
//    PerformanceTest.testAviatorCached   avgt   30        ≈ 10⁻⁶                s/op
//    PerformanceTest.testChopper         avgt   30        ≈ 10⁻⁶                s/op
//    PerformanceTest.testClass           avgt   30        ≈ 10⁻⁶                s/op
//    PerformanceTest.testJexl            avgt   30        ≈ 10⁻⁶                s/op
//    PerformanceTest.testStaticClass     avgt   30        ≈ 10⁻⁶                s/op

}
