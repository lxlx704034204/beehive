package com.gustz.beehive.config.url;

import com.gustz.beehive.service.AccessUrlService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * URL resolver for access URL
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
@Aspect
//@Component
public class AccessUrlResolver {

    private static final Logger logger = LoggerFactory.getLogger(AccessUrlResolver.class);

    private final Lock lock = new ReentrantLock();

    @Autowired(required = false)
    private AccessUrlService accessUrlService;

    @Pointcut(value = "@annotation(com.gustz.beehive.config.url.GetAccessUrl)")
    private void getAccessUrlPct() {
        // point cut method
    }

    /**
     * Do filter
     *
     * @param jp
     * @return
     * @throws IOException
     */
    @Around("getAccessUrlPct()")
    public Object doAround(ProceedingJoinPoint jp) throws IOException {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            // get parameters
            String uid = request.getParameter("u");
            String token = request.getParameter("t");
            String channel = request.getParameter("c");
            logger.debug("doAround begin: arg uri={}, uid={},token={},channel={}", request.getServletPath(), uid, token, channel);
            // check args
            return jp.proceed();
        } catch (Throwable e) {
            logger.error("doAround: is fail.", e);
            doSendError();
        } finally {
            logger.debug("doAround end.");
        }
        return null;
    }

    private void doSendError() throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "error");
    }

}
