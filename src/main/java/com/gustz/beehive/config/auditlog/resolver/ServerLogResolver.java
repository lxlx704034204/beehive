package com.gustz.beehive.config.auditlog.resolver;

import com.gustz.beehive.config.auditlog.ServerAuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Logger resolver for server audit log
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Aspect
//@Component
public class ServerLogResolver extends BaseLogResolver {

    @Pointcut(value = "@annotation(com.gustz.beehive.config.auditlog.ServerAuditLog)")
    private void serverLogPct() {
        // point cut method
    }

    /**
     * Write log around
     *
     * @param jp
     * @throws Throwable
     */
    @Around("serverLogPct()")
    public Object writeServerAuditLog(ProceedingJoinPoint jp) throws Throwable {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        if (method == null || !method.isAnnotationPresent(ServerAuditLog.class)) {
            return jp.proceed();
        }
        final ServerAuditLog serverAuditLog = method.getAnnotation(ServerAuditLog.class);
        final AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setModule(serverAuditLog.module());
        logInfo.setItem(serverAuditLog.item());
        // s1: before log
        this.writeBeforeLog(LogHelpers.session, logInfo, method.getParameters(), jp.getArgs());
        // s2: return log
        final Object retVal = jp.proceed();
        this.writeAfterLog(LogHelpers.session, logInfo, retVal);
        //
        return retVal;
    }

}
