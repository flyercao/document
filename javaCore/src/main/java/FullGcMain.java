import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * create by huirong on 2021-01-05 10:53
 */

public class FullGcMain {


    public static Map cache=new HashMap();

    public static AtomicLong count=new AtomicLong(0);

    public static Map reloadCache(){
        HashMap hashMap = new HashMap();
        for (int i=0;i<100000;i++){
            hashMap.put(count,count+"fasgiahgruhiahagnanglaboarbaibrbnlanrvkanvniuhvraf");
        }
        cache=hashMap;
        return cache;
    }

    public static void main(String[] args) throws Exception{
        do{
            reloadCache();
            Thread.sleep(100);
        }
        while (true);
    }


}
