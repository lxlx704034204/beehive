package com.gustz.beehive.config.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Logger resolver for service log
 *
 * @author zhangzhenfeng
 * @since [May 3, 2015]
 */
@Aspect
//@Component
public class ServiceLogResolver extends BaseLogResolver {

    private static final String PREFIX = "Service";

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Pointcut(value = "@within(org.springframework.stereotype.Service)")
    private void serviceLogPct() {
        // point cut method
    }

    /**
     * Write log
     *
     * @param jp
     * @throws Throwable
     */
    @Around("serviceLogPct()")
    public Object writeLog(ProceedingJoinPoint jp) throws Throwable {
        if (!isEnabled()) {
            return jp.proceed();
        }
        return doWriteLog(PREFIX, "", jp);
    }

}
