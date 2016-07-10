package com.gustz.beehive.config.auditlog;

import java.lang.annotation.*;

/*
 * Audit log config for DAO
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DaoAuditLog {

    /**
     * module name
     *
     * @return
     */
    String module();

    /**
     * item name
     */
    String item();
}

