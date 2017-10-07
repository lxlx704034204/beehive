package com.gustz.beehive.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CRC64 test
 *
 * @author zhangzhenfeng
 * @since 2017-10-07
 */
public class CRC64Test {

    private CRC64 crc64;

    @Test
    public void getValue() throws Exception {
        long bt = System.currentTimeMillis();
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            String uuid = BeehiveUtil.generateSpUUID();
            long val = Math.abs(new CRC64(uuid.getBytes(), uuid.length()).getValue());
            if (list.contains(val)) {
                System.err.println("getValue-val=" + val);
                continue;
            }
            list.add(val);
        }
        System.err.println("getValue-ls=" + list.size() + ",cost ts=" + (System.currentTimeMillis() - bt));
    }

    @Test
    public void test() {
        long ret = 0xff;
        System.err.println(ret);
        long ret2 = UUID.randomUUID().getMostSignificantBits();
        System.err.println(ret2);
    }

}