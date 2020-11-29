package multiThread.disruptor;

import com.lmax.disruptor.RingBuffer;

/**
 * Created by  huirong on 2020-11-28
 */
public class Producer {
    private int id;
    private final RingBuffer<Order> ringBuffer;

    public Producer(int id,RingBuffer<Order> ringBuffer){
        this.id=id;
        this.ringBuffer=ringBuffer;
    }

    /**
     * onData用来发布事件，每调用一次就发布一次事件
     * 它的参数会通过事件传递给消费者
     * @param data
     */
    public void onData(String data){
        //可以把ringBuffer看作是一个事件队列，那么next就是得到下一个事件槽
        long sequence = ringBuffer.next();
        try {
            Order order = ringBuffer.get(sequence);
            order.setId(data);
            System.out.println("当前生产者："+id+"，生产信息："+order.getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            //发布事件
            ringBuffer.publish(sequence);
        }
    }
}
