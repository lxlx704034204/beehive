package com.gustz.beehive.service;

import com.gustz.beehive.model.UserDto;

import javax.validation.Valid;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
public interface TestLogService {

    void testLog(@Valid UserDto dto);

    void testLog2();

}
