package com.gustz.beehive.service.impl;

import com.gustz.beehive.config.cache.ZCachingSupport;
import com.gustz.beehive.config.cache.conf.BaseCacheConfig;
import com.gustz.beehive.config.cache.conf.EhcacheCacheConfig;
import com.gustz.beehive.config.cache.conf.ZCacheType;
import com.gustz.beehive.model.CacheStatisticsDto;
import com.gustz.beehive.service.ZCachingService;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * caching service impl
 *
 * @author zhangzhenfeng
 * @since 2016-10-16
 */
@Service
public class ZCachingServiceImpl implements ZCachingService {

    private static final Logger logger = LoggerFactory.getLogger(ZCachingServiceImpl.class);

    private StringRedisTemplate cacheRedisTemplate;

    private ZCachingSupport zCachingSupport;

    private BaseCacheConfig baseCacheConfig;

    private RedisCacheManager redisCacheManager;

    private final List<String> localCacheNames = new CopyOnWriteArrayList<>();

    private static final String REMOTE_KEY_SUFFIX = "~keys";

    @Autowired
    public ZCachingServiceImpl(ZCachingSupport zCachingSupport) {
        this.zCachingSupport = zCachingSupport;
        this.cacheRedisTemplate = zCachingSupport.getCacheRedisTemplate();
        this.baseCacheConfig = zCachingSupport.getBaseCacheConfig();
        this.redisCacheManager = zCachingSupport.getRedisCacheManager();
    }

    /**
     * get base cache config
     *
     * @return
     */
    @Override
    public BaseCacheConfig getBaseCacheConfig() {
        return this.baseCacheConfig;
    }

    /**
     * add current cache name
     *
     * @param cacheType
     * @param cacheName
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addCurrCacheName(ZCacheType cacheType, String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return;
        }
        if (ZCacheType.LOCAL.compareTo(cacheType) == 0) {
            if (!localCacheNames.contains(cacheName)) {
                localCacheNames.add(cacheName);
            }
        } else if (ZCacheType.REMOTE.compareTo(cacheType) == 0) {
            final String key = getRemoteKeysGroupName();
            final SetOperations opts = this.cacheRedisTemplate.opsForSet();
            final String val = this.getRemoteKeysName(cacheName);
            if (!opts.isMember(key, val)) {
                opts.add(key, val);
            }
        } else {
            logger.error("addCurrCacheName: cacheType invalid,cacheType={},cacheName={}", cacheType, cacheName);
            return;
        }
    }

    /**
     * clear cache
     *
     * @param cacheNameList
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clearCache(List<String> cacheNameList) {
        if (cacheNameList == null || cacheNameList.isEmpty()) {
            logger.debug("clearCache: cache name list is empty.");
            return;
        }
        logger.debug("clearCache: begin clear cache data...");
        final SetOperations setOpts = this.cacheRedisTemplate.opsForSet();
        for (String name : cacheNameList) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            // clear local
            this.clearLocalCache(name);
            // clear remote
            this.clearRemoteCache(setOpts, name);
        }
    }

    /**
     * clear expire cache
     */
    @Override
    public void clearExpireCache() {
        // clear local
        this.clearExpireLocalCache();
        // clear remote
        this.clearExpireRemoteCache();
    }

    private EhcacheCacheConfig getEhcacheCacheConfig() {
        return zCachingSupport.getEhcacheCacheConfig();
    }

    private void clearExpireLocalCache() {
        if (localCacheNames == null || localCacheNames.isEmpty()) {
            logger.debug("clearExpireLocalCache: localCacheName list is empty.");
            return;
        }
        final EhCacheCacheManager ehCacheCacheManager = zCachingSupport.getEhCacheCacheManager();
        for (String name : localCacheNames) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            try {
                // ehcache
                if (ehCacheCacheManager != null) {
                    net.sf.ehcache.Cache cache = ehCacheCacheManager.getCacheManager().getCache(name);
                    if (cache != null) {
                        cache.evictExpiredElements();
                    }
                    continue;
                }
                // guava ...

            } catch (Exception e) {
                logger.warn("clearExpireLocalCache: catch e.msg={}", e.getMessage());
                continue;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void clearExpireRemoteCache() {
        final SetOperations setOpts = this.cacheRedisTemplate.opsForSet();
        Set<String> cacheKeys = setOpts.members(getRemoteKeysGroupName());
        if (cacheKeys == null || cacheKeys.isEmpty()) {
            logger.debug("clearExpireRemoteCache: cacheKeys list is empty.");
            return;
        }
        final ZSetOperations zSetOpts = this.cacheRedisTemplate.opsForZSet();
        final ValueOperations<String, String> valOpts = this.cacheRedisTemplate.opsForValue();
        for (String key : cacheKeys) {
            if (key == null || key.isEmpty()) {
                continue;
            }
            try {
                if (!this.isExistRemoteKey(key)) {
                    continue;
                }
                long start = 0;
                long end = start + BaseCacheConfig.OFFSET;
                while (true) {
                    Set<String> itemKeys = zSetOpts.range(key, start, end);
                    if (itemKeys == null || itemKeys.isEmpty()) {
                        break;
                    }
                    // s1: do clear expire data key
                    for (String ik : itemKeys) {
                        if (StringUtils.isBlank(ik)) {
                            continue;
                        }
                        if (StringUtils.isBlank(valOpts.get(ik))) {
                            zSetOpts.remove(key, ik);
                        }
                    }
                    // s2: reset count
                    start = end;
                    end = start + BaseCacheConfig.OFFSET;
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                logger.warn("clearExpireRemoteCache: catch e.msg={}", e.getMessage());
                continue;
            } finally {
                // remove keys group
                if (zSetOpts.size(key) == 0) {
                    setOpts.remove(getRemoteKeysGroupName(), key);
                }
            }
        }
    }

    private void clearLocalCache(final String cacheName) {
        try {
            // ehcache
            EhCacheCacheManager ehCacheCacheManager = zCachingSupport.getEhCacheCacheManager();
            if (ehCacheCacheManager != null) {
                Cache cache = ehCacheCacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
                return;
            }
            // guava
            GuavaCacheManager guavaCacheManager = zCachingSupport.getGuavaCacheManager();
            if (guavaCacheManager != null) {
                Cache cache = guavaCacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
                return;
            }
        } catch (Exception e) {
            logger.warn("clearLocalCache: catch e.msg={}", e.getMessage());
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private void clearRemoteCache(final SetOperations setOpts, final String cacheName) {
        final String key = this.getRemoteKeysName(cacheName);
        final ZSetOperations zSetOpts = this.cacheRedisTemplate.opsForZSet();
        try {
            if (!this.isExistRemoteKey(key)) {
                return;
            }
            long start = 0;
            long end = start + BaseCacheConfig.OFFSET;
            while (true) {
                Set<String> itemKeys = zSetOpts.range(key, start, end);
                if (itemKeys == null || itemKeys.isEmpty()) {
                    // delete group key
                    this.cacheRedisTemplate.delete(key);
                    break;
                }
                // delete item key
                this.cacheRedisTemplate.delete(itemKeys);
                start = end;
                end = start + BaseCacheConfig.OFFSET;
            }
        } catch (Exception e) {
            logger.warn("clearRemoteCache: catch e.msg={}", e.getMessage());
            return;
        } finally {
            // remove keys group
            if (zSetOpts.size(key) == 0) {
                setOpts.remove(getRemoteKeysGroupName(), key);
            }
        }
    }

    /**
     * get cache
     *
     * @param cacheType
     * @param cacheName
     * @return
     */
    @Override
    public Cache getCache(ZCacheType cacheType, String cacheName) {
        if (ZCacheType.LOCAL.compareTo(cacheType) == 0) {
            // local cache
            return this.getLocalCache(cacheName);
        } else if (ZCacheType.REMOTE.compareTo(cacheType) == 0) {
            // remote cache
            return redisCacheManager.getCache(cacheName);
        } else {
            logger.error("getCache: cacheType invalid,cacheType={},cacheName={}", cacheType, cacheName);
            return null;
        }
    }

    /**
     * set cache expire
     *
     * @param cacheType
     * @param useExpire
     * @param cacheKey
     * @param cacheName
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setCacheExpire(ZCacheType cacheType, int useExpire, String cacheKey, String cacheName) {
        if (ZCacheType.LOCAL.compareTo(cacheType) == 0) { // local cache
            try {
                this.setLocalCacheExpire(useExpire, cacheKey, cacheName);
            } catch (Exception e) {
                logger.warn("setCacheExpire: catch set local expire time,e.msg={}", e.getMessage());
            }
        } else if (ZCacheType.REMOTE.compareTo(cacheType) == 0) { // remote cache
            // add remote cache keys
            final ZSetOperations opts = this.cacheRedisTemplate.opsForZSet();
            final String rcKey = this.getRemoteKeysName(cacheName);
            final String tmpRCacheKey = this.getRemoteValName(cacheKey);
            if (opts.rank(rcKey, tmpRCacheKey) == null) {
                opts.add(rcKey, tmpRCacheKey, 0);
            }
            try {
                final Long redisExpire = cacheRedisTemplate.getExpire(tmpRCacheKey);
                if (redisExpire != null && this.isSetExpire(redisExpire, useExpire)) {
                    logger.debug("setCacheExpire: cacheKey expired,cacheType={},currExpire={},useExpire={},cacheKey={}", cacheType, redisExpire, useExpire, cacheKey);
                    //
                    this.cacheRedisTemplate.expire(tmpRCacheKey, getTimeout(useExpire), TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                logger.warn("setCacheExpire: catch set remote expire time,e.msg={}", e.getMessage());
            }
        } else {
            logger.error("setCacheExpire: cacheType invalid,cacheType={},cacheKey={}", cacheType, cacheKey);
            return;
        }
    }

    /**
     * get local statistics
     *
     * @param cacheName
     * @return
     */
    @Override
    public CacheStatisticsDto getLocalStatistics(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            logger.debug("getLocalStatistics: cacheName is blank.");
            return null;
        }
        // ehcache
        EhCacheCacheManager ehCacheCacheManager = zCachingSupport.getEhCacheCacheManager();
        if (ehCacheCacheManager != null) {
            net.sf.ehcache.Cache ehCache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
            if (ehCache == null) {
                return null;
            }
            StatisticsGateway statisticsGateway = ehCache.getStatistics();
            if (statisticsGateway != null) {
                CacheStatisticsDto dto = new CacheStatisticsDto();
                dto.setCacheName(cacheName);
                // stat. info-- cache
                dto.setEvictedCount(statisticsGateway.cacheEvictedCount());
                dto.setExpiredCount(statisticsGateway.cacheExpiredCount());
                dto.setHitCount(statisticsGateway.cacheHitCount());
                dto.setHitRatio(statisticsGateway.cacheHitRatio());
                dto.setMissCount(statisticsGateway.cacheMissCount());
                dto.setMissExpiredCount(statisticsGateway.cacheMissExpiredCount());
                dto.setPutAddedCount(statisticsGateway.cachePutAddedCount());
                dto.setLocalDiskSizeInBytes(statisticsGateway.getLocalDiskSizeInBytes());
                dto.setLocalHeapSizeInBytes(statisticsGateway.getLocalHeapSizeInBytes());
                dto.setLocalOffHeapSizeInBytes(statisticsGateway.getLocalOffHeapSizeInBytes());
                // heap
                CacheStatisticsDto.LocalHeap localHeap = new CacheStatisticsDto.LocalHeap();
                localHeap.setHitCount(statisticsGateway.localHeapHitCount());
                localHeap.setMissCount(statisticsGateway.localHeapMissCount());
                localHeap.setPutAddedCount(statisticsGateway.localHeapPutAddedCount());
                // disk
                CacheStatisticsDto.LocalDisk localDisk = new CacheStatisticsDto.LocalDisk();
                localDisk.setHitCount(statisticsGateway.localDiskHitCount());
                localDisk.setMissCount(statisticsGateway.localDiskMissCount());
                localDisk.setPutAddedCount(statisticsGateway.localDiskPutAddedCount());
                return dto;
            }
        }
        return null;
    }

    private static boolean isSetExpire(final long currExpire, final long useExpire) {
        return (currExpire > useExpire //
                || currExpire < BaseCacheConfig.MIN_DEFAULT_EXPIRE_TIME //
                || currExpire > BaseCacheConfig.MAX_DEFAULT_EXPIRE_TIME);
    }

    private static long getTimeout(final int expire) {
        long timeout = expire;
        int ratio = 30;
        if (timeout <= 60) {
            ratio = (int) (timeout / 2);
        }
        timeout += Math.abs((int) (Math.random() * ratio));
        return timeout;
    }

    private void setLocalCacheExpire(final int useExpire, final String cacheKey, final String cacheName) {
        // ehcache
        EhCacheCacheManager ehCacheCacheManager = zCachingSupport.getEhCacheCacheManager();
        if (ehCacheCacheManager != null) {
            net.sf.ehcache.Ehcache ehcache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
            if (ehcache == null) {
                logger.warn("setLocalCacheExpire: ehcache is null.");
                return;
            }
            net.sf.ehcache.Element ehElement = ehcache.get(cacheKey);
            if (ehElement == null) {
                return;
            }
            final int ehcacheExpire = ehElement.getTimeToLive();
            if (isSetExpire(ehcacheExpire, useExpire) //
                    || ehcacheExpire == this.getEhcacheCacheConfig().getTimeToLiveSeconds()) {
                logger.debug("setLocalCacheExpire: cacheKey expired,currExpire={},useExpire={},cacheKey={}", ehcacheExpire, useExpire, cacheKey);
                //
                ehcache.get(cacheKey).setTimeToLive(useExpire);
                ehcache.get(cacheKey).setTimeToIdle(useExpire);
            }
            return;
        }
        // guava
        GuavaCacheManager guavaCacheManager = zCachingSupport.getGuavaCacheManager();
        if (guavaCacheManager != null) {
            // todo: impl
            return;
        }
    }

    private Cache getLocalCache(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            logger.warn("getLocalCache: cacheName is blank.");
            return null;
        }
        // ehcache
        EhCacheCacheManager ehCacheCacheManager = zCachingSupport.getEhCacheCacheManager();
        if (ehCacheCacheManager != null) {
            Cache ehCache = ehCacheCacheManager.getCache(cacheName);
            if (ehCache == null) {
                ehCacheCacheManager.getCacheManager().addCacheIfAbsent(cacheName);
            }
            return ehCacheCacheManager.getCache(cacheName);
        }
        // guava
        GuavaCacheManager guavaCacheManager = zCachingSupport.getGuavaCacheManager();
        if (guavaCacheManager != null) {
            return guavaCacheManager.getCache(cacheName);
        }
        return null;
    }

    private boolean isExistRemoteKey(String key) {
        return getExistRemoteKeyExpire(key) != null;
    }

    private Long getExistRemoteKeyExpire(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        final Long expire = cacheRedisTemplate.getExpire(key);
        if (expire == null) {
            return null;
        }
        return (expire == -2 || expire == 0) ? null : expire;
    }

    private String getRemoteValName(String cacheKey) {
        return BaseCacheConfig.INNER_CACHE_PREFIX + baseCacheConfig.getCachePrefix() + ":" + cacheKey;
    }

    private String getRemoteKeysName(String cacheName) {
        return BaseCacheConfig.INNER_CACHE_PREFIX + baseCacheConfig.getCachePrefix() + ":" + cacheName + REMOTE_KEY_SUFFIX;
    }

    private String getRemoteKeysGroupName() {
        return BaseCacheConfig.INNER_CACHE_PREFIX + baseCacheConfig.getCachePrefix() + REMOTE_KEY_SUFFIX;
    }

}
