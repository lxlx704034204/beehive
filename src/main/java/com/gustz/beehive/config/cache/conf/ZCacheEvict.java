package com.gustz.beehive.config.cache.conf;

import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.*;

/**
 * CacheEvict config
 *
 * @author zhangzhenfeng
 * @since 2016-10-13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@CacheEvict
public @interface ZCacheEvict {

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

    boolean allEntries() default false;
}
