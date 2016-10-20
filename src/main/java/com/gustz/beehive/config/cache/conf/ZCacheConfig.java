package com.gustz.beehive.config.cache.conf;

import org.springframework.cache.annotation.CacheConfig;

import java.lang.annotation.*;

/**
 * CacheConfig
 *
 * @author zhangzhenfeng
 * @since 2016-10-13
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@CacheConfig
public @interface ZCacheConfig {

    /**
     * cache name
     *
     * @return
     */
    String value();

}
