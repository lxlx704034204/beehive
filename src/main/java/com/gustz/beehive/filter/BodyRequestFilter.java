package com.gustz.beehive.filter;

import com.gustz.beehive.util.BodyRequestWrapper;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * body request filter
 *
 * @author zhangzhenfeng
 * @since 2017-01-23
 */
@Component
public class BodyRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest) {
            requestWrapper = new BodyRequestWrapper((HttpServletRequest) request);
        }
        if (requestWrapper != null) {
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // nothing
    }
}
