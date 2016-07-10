package com.gustz.beehive.service.impl;

import com.gustz.beehive.base.TestBase;
import com.gustz.beehive.model.UserDto;
import com.gustz.beehive.service.TestLogService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * log service test
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
public class TestLogServiceImplTest extends TestBase {

    @Autowired
    private TestLogService testLogService;

    @Override
    public void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }

    @Test
    public void testLog() throws Exception {
        UserDto dto = new UserDto();
        testLogService.testLog(dto);
    }
}
