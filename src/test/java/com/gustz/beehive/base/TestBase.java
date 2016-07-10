package com.gustz.beehive.base;

import com.gustz.beehive.AppBootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test base config
 *
 * @author zhangzhenfeng
 * @date 2015-11-25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppBootstrap.class)
public class TestBase {

    @Before
    public void setUp() throws Exception {
        // null
    }

    @After
    public void tearDown() throws Exception {
        // null
    }
}
