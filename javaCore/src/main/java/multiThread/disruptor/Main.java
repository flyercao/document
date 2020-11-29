package multiThread.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * Created by  huirong on 2020-11-28
 */
public class Main {


    public static void main(String[] args) throws InterruptedException {
        //创建ringBuffer
        RingBuffer<Order> ringBuffer =
                RingBuffer.create(ProducerType.MULTI,
                        new EventFactory<Order>() {
                            @Override
                            public Order newInstance() {
                                return new Order();
                            }
                        },
                        8,
                        new YieldingWaitStrategy());

        SequenceBarrier barrier = ringBuffer.newBarrier();

        Consumer[] consumers = new Consumer[1];
        for(int i=0;i<consumers.length;i++){
            consumers[i]=new Consumer("c"+i);
        }

        WorkerPool<Order> workerPool =
                new WorkerPool<Order>(ringBuffer,
                        barrier,
                        new IntEventExceptionHandler(),
                        consumers);
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        final CountDownLatch latch = new CountDownLatch(1);
        for(int i=0;i<1;i++){
            final Producer p = new Producer(i,ringBuffer);
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for(int j=0;j<100;j++){
                        p.onData(UUID.randomUUID().toString());
                    }
                }
            }).start();
        }
        Thread.sleep(2000);
        System.out.println("------------开始生产-------------");
        latch.countDown();
        Thread.sleep(5000);
        System.out.println("总数："+consumers[0].getCount());

    }

    static class IntEventExceptionHandler implements ExceptionHandler {
        @Override
        public void handleEventException(Throwable arg0, long arg1, Object arg2) {}

        @Override
        public void handleOnShutdownException(Throwable arg0) {}

        @Override
        public void handleOnStartException(Throwable arg0) {}
    }

}
