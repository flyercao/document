package core.bloomFilter;

import java.util.BitSet;

/**
 * create by huirong on 2020-10-10 16:11
 */

public class Filter {

    private static final int DEFAULT_SIZE = 2 << 12;
    private static final int[] seeds = new int[]{7, 11, 13, 31, 37, 61,};

    private int[] bits = new int[DEFAULT_SIZE];
    private Filter.SimpleHash[] func = new Filter.SimpleHash[seeds.length];

    int period=5;


    public static void main(String[] args) throws Exception{
        String value = " stone2083@yahoo.cn ";
        Filter filter = new Filter();
        System.out.println(filter.contains(value));
        filter.add(value);
        System.out.println(filter.contains(value));
        Thread.sleep(6000);
        System.out.println(filter.contains(value));
    }

    public Filter() {
        for (int i = 0; i < seeds.length; i++) {
            func[i] = new Filter.SimpleHash(DEFAULT_SIZE, seeds[i]);
        }
    }

    public void add(String value) {
        for (Filter.SimpleHash f : func) {
            bits[f.hash(value)]=(int)( System.currentTimeMillis()/1000);
        }
    }

    public boolean contains(String value) {
        long currentTimeMillis = System.currentTimeMillis()/1000;
        if (value == null) {
            return false;
        }
        boolean ret = true;
        for (Filter.SimpleHash f : func) {
            ret = ret && currentTimeMillis<=bits[f.hash(value)]+period;
        }
        return ret;
    }

    public static class SimpleHash {

        private int cap;
        private int seed;

        public SimpleHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        public int hash(String value) {
            int result = 0;
            int len = value.length();
            for (int i = 0; i < len; i++) {
                result = seed * result + value.charAt(i);
            }
            return (cap - 1) & result;
        }

    }
}
