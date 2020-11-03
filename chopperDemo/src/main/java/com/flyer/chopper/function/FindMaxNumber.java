package com.flyer.chopper.function;

import com.flyer.chopper.core.runtime.ChopperBigInt;
import com.flyer.chopper.core.runtime.ChopperObject;
import com.flyer.chopper.core.runtime.function.AbstractChopperFunction;
import com.flyer.chopper.core.runtime.function.AbstractVariadicFunction;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Stream;

/**
 * create by huirong on 2020-10-15 11:03
 */

public class FindMaxNumber extends AbstractVariadicFunction {


    @Override
    public String getName() {
        return "findMaxNumber";
    }

    @Override
    public ChopperObject variadicCall(Map<String, Object> map, ChopperObject... chopperObjects) {

        BigInteger bigInteger = Stream.of(chopperObjects).map(o -> new BigInteger("" + o.numberValue(map))).max((o1, o2) -> o1.compareTo(o2)).get();
        return new ChopperBigInt(bigInteger);
    }
}
