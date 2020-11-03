package guava;

import com.google.common.cache.*;

import java.util.concurrent.TimeUnit;

/**
 * create by huirong on 2020-10-23 14:08
 */

public class GuavaCacheTest {


    public static LoadingCache<String, Long> methodInstanceCache = CacheBuilder.newBuilder().concurrencyLevel(8)
            .maximumSize(1000000).initialCapacity(16).removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    System.out.println(notification.getKey() + " was removed,value:"+notification.getValue().toString()+",costTime:"+(System.currentTimeMillis()-(Long)notification.getValue())+" cause is " + notification.getCause());
                }
            })
            .expireAfterWrite(100, TimeUnit.MILLISECONDS)
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String s) throws Exception {
                    return System.currentTimeMillis();
                }
            });


    public static void main(String[] args) throws Exception{
        for(int i=0;i<1000;i++) {
            GuavaCacheTest.methodInstanceCache.get("" + i);
        }
        Thread.sleep(1000);
        for(int i=0;i<1000;i++) {
            GuavaCacheTest.methodInstanceCache.get("" + i);
        }
        Thread.sleep(100000);



    }

}
