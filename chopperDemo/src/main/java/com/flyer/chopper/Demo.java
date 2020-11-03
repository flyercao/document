package com.flyer.chopper;

import com.flyer.chopper.core.engine.ExpressEngine;
import com.flyer.chopper.entity.Student;

import java.util.HashMap;
import java.util.Map;

/**
 * create by huirong on 2020-10-15 10:54
 */

public class Demo {

    static Map<String, Object> map;

    static{
        map = new HashMap<>();
        map.put("a", 1.0);
        map.put("b", 2);
        map.put("c", 9999999L);
        map.put("d", true);
        map.put("studentA", new Student(10, "张三", 90.0));
        map.put("empty", null);
    }

    public static void main(String[] args) throws Exception {
        test();
//        testFunction();
    }

    public static void test() throws Exception {
        String express = "((2+a)*4/(b-2.0D)*(c%2))>1&&b =~[2,4,5] &&'eee'==e&&a*1000 > 222222222222222222222222222&&b>1&&d?'result:true':'result:false'";
        HashMap<String, Object> context = new HashMap<>();
        context.put("a", 1);
        context.put("b", 8.0);
        context.put("c", 9.009);
        context.put("d", true);
        context.put("e", "eee");
        ExpressEngine expressEngine = new ExpressEngine();
        Object result = expressEngine.execute(express, context);

        System.out.println(result);

    }

    public static void testFunction() throws Exception{

        String express = "findMaxNumber(1,2,-9,99999,-99999999999999999999999,c-999999999999999999999999999999)";
        Object result = new ExpressEngine().execute(express, map);
        System.out.println("result:"+result);

    }

}
