import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * create by huirong on 2021-03-03 11:26
 */
public class ThreadLocalOOM {
    private static final int TASK_LOOP_SIZE = 10000;

    final static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 5, 1,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    static class LocalVariable {
        private byte[] a = new byte[1024*1024*5];/*5M大小的数组*/
    }

    static ThreadLocal<LocalVariable> localVariable= new ThreadLocal<>();


    public static void main(String[] args) throws InterruptedException {
        /*5*5=25*/
        for (int i = 0; i < TASK_LOOP_SIZE; ++i) {
            poolExecutor.execute(new Runnable() {
                public void run() {
//                    ThreadLocalOOM oom = new ThreadLocalOOM();
//                    ThreadLocalOOM.localVariable = new ThreadLocal<>();
                    ThreadLocalOOM.localVariable.set(new LocalVariable());
//                    new LocalVariable();
                    //  System.out.println("use local varaible");
//                    oom.localVariable.remove();
                }
            });

            Thread.sleep(100);
        }
        System.out.println("pool execute over");
    }

}