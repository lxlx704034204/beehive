package com.gustz.beehive.config.cache.conf;

import org.springframework.cache.annotation.CachePut;

import java.lang.annotation.*;

/**
 * CachePut config
 *
 * @author zhangzhenfeng
 * @since 2016-10-13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@CachePut
public @interface ZCachePut {

    /**
     * cache name
     *
     * @return
     */
    String value() default "";

    /**
     * cache type enum
     *
     * @return
     */
    ZCacheType type() default ZCacheType.REMOTE;

    /**
     * expire time(in seconds),default 60s,max 3600s.
     *
     * @return
     */
    int expire() default BaseCacheConfig.DEFAULT_EXPIRE_TIME;
}
