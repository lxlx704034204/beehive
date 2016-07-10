package com.gustz.beehive.config.api;

import com.gustz.beehive.base.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * API vendor config
 *
 * @author zhangzhenfeng
 * @since 2016-04-23
 */
public class ApiVendorConfTest extends TestBase {

    @Autowired
    private ApiVendorConf apiVendorConf;

    @Override
    public void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetCodeSks() throws Exception {

    }

    @Test
    public void testGetCodeAks() throws Exception {
        List list = apiVendorConf.getCodeAks();
        System.out.println("==:" + list);
    }

    @Test
    public void testGetCodes() throws Exception {

    }
}
