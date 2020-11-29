package multiThread.disruptor;

import com.lmax.disruptor.WorkHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by  huirong on 2020-11-28
 */
public class Consumer implements WorkHandler<Order> {


    private String consumerId;

    private static AtomicInteger count = new AtomicInteger(0);

    public Consumer(String consumerId){
        this.consumerId = consumerId;
    }
    @Override
    public void onEvent(Order order) throws Exception {
        System.out.println("当前消费者："+this.consumerId+"，消费信息："+order.getId());
        count.incrementAndGet();
    }

    public int getCount(){
        return count.get();
    }
}
