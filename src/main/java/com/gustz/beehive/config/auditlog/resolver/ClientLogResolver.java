package com.gustz.beehive.config.auditlog.resolver;

import com.gustz.beehive.config.auditlog.ClientAuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * logger resolver for client audit log
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Aspect
@Component
public class ClientLogResolver extends BaseLogResolver {

    @Pointcut(value = "@annotation(com.gustz.beehive.config.auditlog.ClientAuditLog)")
    private void sdkClientLogPct() {
        // point cut method
    }

    /**
     * Write log around
     *
     * @param jp
     * @throws Throwable
     */
    @Around("sdkClientLogPct()")
    private Object writeClientAuditLog(ProceedingJoinPoint jp) throws Throwable {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        if (method == null || !method.isAnnotationPresent(ClientAuditLog.class)) {
            return jp.proceed();
        }
        final ClientAuditLog clientAuditLog = method.getAnnotation(ClientAuditLog.class);
        final AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setModule(clientAuditLog.module());
        logInfo.setMetric(clientAuditLog.metric());
        // s1: before log
        this.writeBeforeLog(LogHelpers.session, logInfo, method.getParameters(), jp.getArgs());
        // s2: return log
        final Object retVal = jp.proceed();
        this.writeAfterLog(LogHelpers.session, logInfo, retVal);
        //
        return retVal;
    }

}
