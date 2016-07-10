package com.gustz.beehive.service.impl;

import com.gustz.beehive.service.AccessUrlService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2015-12-24
 */
@Service
public class AccessUrlServiceImpl implements AccessUrlService {

    /**
     * Set http servlet request
     *
     * @param request
     */
    @Override
    public void setRequest(HttpServletRequest request) {
        System.out.println("getServletPath=:" + request.getServletPath());
    }
}
