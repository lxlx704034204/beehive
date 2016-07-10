package com.gustz.beehive.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
//@Component
public class WhitelistUrlFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WhitelistUrlFilter.class);

    /**
     * init
     *
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * do filter
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("doFilter...1111111111");
        chain.doFilter(request,response);
    }

    /**
     * destroy
     */
    @Override
    public void destroy() {

    }
}
