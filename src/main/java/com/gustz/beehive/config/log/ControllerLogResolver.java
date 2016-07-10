package com.gustz.beehive.config.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Logger resolver for controller log
 *
 * @author zhangzhenfeng
 * @since [May 3, 2015]
 */
@Aspect
//@Component
public class ControllerLogResolver extends BaseLogResolver {

    private static final String PREFIX = "Controller";

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    private void controllerLogPct() {
        // point cut method
    }

    /**
     * Write log
     *
     * @param jp
     * @throws Throwable
     */
    @Around("controllerLogPct()")
    public Object writeLog(ProceedingJoinPoint jp) throws Throwable {
        if (!isEnabled()) {
            return jp.proceed();
        }
        return doWriteLog(PREFIX, "", jp);
    }

}
