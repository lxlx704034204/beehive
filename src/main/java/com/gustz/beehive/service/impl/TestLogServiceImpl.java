package com.gustz.beehive.service.impl;

import com.gustz.beehive.config.log.TestLogger;
import com.gustz.beehive.dao.UserDao;
import com.gustz.beehive.model.UserDto;
import com.gustz.beehive.service.TestLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 * Logger service for test
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
@Service
public class TestLogServiceImpl implements TestLogService {

    private static final Logger logger = LoggerFactory.getLogger(TestLogServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Override
    public void testLog(@Valid UserDto dto) {
    }

    @Override
    public void testLog2() {
        System.out.println("testLog2...");
        userDao.listUser();
        //testLog3();
    }

    private void testLog3(){
        System.out.println("private testLog3...");
    }
}
