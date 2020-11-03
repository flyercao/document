package guava;

import com.alibaba.nacos.client.naming.utils.StringUtils;
import com.github.benmanes.caffeine.cache.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.concurrent.TimeUnit;

/**
 * create by huirong on 2020-10-26 13:29
 */

public class CaffeineCacheTest {

    public static LoadingCache<String, Long> loadingCache = Caffeine.newBuilder()
            .maximumSize(10).recordStats()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, Long>() {
                @Override
                public void onRemoval(@Nullable String s, @Nullable Long aLong, @NonNull RemovalCause removalCause) {
                    System.out.println(s + " was removed,value:"+aLong+",costTime:"+(System.currentTimeMillis()-(Long)aLong)+" cause is " + removalCause);
                }
            })
            .build(new CacheLoader<String, Long>() {
                @Override
                @Nullable
                public Long load(String key) throws Exception {
                    System.out.println("从缓存中加载...key=" + key);

                    return System.currentTimeMillis();
                }
            });




    public static void main(String[] args) throws Exception{
        for(int i=0;i<1000;i++) {
            CaffeineCacheTest.loadingCache.get("" + i);
        }
        Thread.sleep(1000);
        for(int i=0;i<1000;i++) {
            CaffeineCacheTest.loadingCache.get("" + i);
        }
        Thread.sleep(100000);



    }

}
