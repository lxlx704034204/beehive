package com.gustz.beehive.service;

import com.gustz.beehive.config.cache.conf.BaseCacheConfig;
import com.gustz.beehive.config.cache.conf.ZCacheType;
import com.gustz.beehive.model.CacheStatisticsDto;
import org.springframework.cache.Cache;

import java.util.List;

/**
 * caching service
 *
 * @author zhangzhenfeng
 * @since 2016-10-16
 */
public interface ZCachingService {

    /**
     * get base cache config
     *
     * @return
     */
    BaseCacheConfig getBaseCacheConfig();

    /**
     * add current cache name
     *
     * @param cacheType
     * @param cacheName
     */
    void addCurrCacheName(ZCacheType cacheType, String cacheName);

    /**
     * clear cache
     *
     * @param cacheNameList
     */
    void clearCache(List<String> cacheNameList);

    /**
     * clear expire cache
     */
    void clearExpireCache();

    /**
     * get cache
     *
     * @param cacheType
     * @param cacheName
     * @return
     */
    Cache getCache(ZCacheType cacheType, String cacheName);

    /**
     * set cache expire
     *
     * @param cacheType
     * @param useExpire
     * @param cacheKey
     * @param cacheName
     */
    void setCacheExpire(ZCacheType cacheType, int useExpire, String cacheKey, String cacheName);

    /**
     * get local statistics
     *
     * @param cacheName
     * @return
     */
    CacheStatisticsDto getLocalStatistics(String cacheName);

}
