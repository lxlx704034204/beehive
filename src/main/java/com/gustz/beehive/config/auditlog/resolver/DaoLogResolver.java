package com.gustz.beehive.config.auditlog.resolver;

import com.gustz.beehive.config.auditlog.DaoAuditLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * logger resolver for DAO audit log
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Aspect
@Component
public class DaoLogResolver extends BaseLogResolver {

    @Pointcut(value = "@annotation(com.gustz.beehive.config.auditlog.DaoAuditLog)")
    private void sdkDaoLogPct() {
        // point cut method
    }

    /**
     * Write log before
     *
     * @param jp
     */
    @Before("sdkDaoLogPct()")
    private void writeDaoAuditLog(JoinPoint jp) {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        if (method == null || !method.isAnnotationPresent(DaoAuditLog.class)) {
            return;
        }
        DaoAuditLog daoAuditLog = method.getAnnotation(DaoAuditLog.class);
        AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setModule(daoAuditLog.module());
        logInfo.setMetric(daoAuditLog.metric());
        this.writeBeforeLog(LogHelpers.stateless, logInfo, method.getParameters(), jp.getArgs());
    }

}
