package com.gustz.beehive.config.auditlog;

import java.lang.annotation.*;

/*
 * Mask type log config
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MaskTypeLog {

    MaskType value();
}

