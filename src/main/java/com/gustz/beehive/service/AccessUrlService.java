package com.gustz.beehive.service;

import javax.servlet.http.HttpServletRequest;

/**
 * Access URL Service
 *
 * @author zhangzhenfeng
 * @since 2015-12-24
 */
public interface AccessUrlService {

    /**
     * Set http servlet request
     *
     * @param request
     */
    void setRequest(HttpServletRequest request);
}
