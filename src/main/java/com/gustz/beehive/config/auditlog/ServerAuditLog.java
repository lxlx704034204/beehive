package com.gustz.beehive.config.auditlog;

import java.lang.annotation.*;

/*
 * audit log config for server
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ServerAuditLog {

    /**
     * module name
     *
     * @return
     */
    String module();

    /**
     * metric name
     */
    String metric();
}

