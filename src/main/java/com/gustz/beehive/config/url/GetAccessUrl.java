package com.gustz.beehive.config.url;

import java.lang.annotation.*;

/*
 *　Get access URL
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GetAccessUrl {

    String value() default "";
}
