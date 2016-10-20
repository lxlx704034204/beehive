package com.gustz.beehive.service.impl;

import com.gustz.beehive.base.TestBase;
import com.gustz.beehive.config.cache.conf.BaseCacheConfig;
import com.gustz.beehive.config.cache.conf.ZCacheType;
import com.gustz.beehive.model.CacheStatisticsDto;
import com.gustz.beehive.service.ZCachingService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * ZCaching service test
 *
 * @author zhangzhenfeng
 * @since 2016-10-14
 */
public class ZCachingServiceImplTest extends TestBase {

    @Autowired
    private ZCachingService zCachingService;

    @Test
    public void testGetBaseCacheConfig() throws Exception {
        BaseCacheConfig config = zCachingService.getBaseCacheConfig();
        System.out.println("getBaseCacheConfig-config=" + config.getCachePrefix());
        Assert.assertNotNull(config.getCachePrefix());
    }

    @Test
    public void testAddCurrCacheName() throws Exception {
        zCachingService.addCurrCacheName(ZCacheType.LOCAL, "name");
        zCachingService.addCurrCacheName(ZCacheType.REMOTE, "name");
        System.out.println("addCurrCacheName ok...");
    }

    @Test
    public void testClearCache() throws Exception {
        zCachingService.clearCache(Arrays.asList("UserDaoImpl"));
        System.out.println("clearCache ok...");
    }

    @Test
    public void testClearExpireCache() throws Exception {
        zCachingService.clearExpireCache();
        System.out.println("clearExpireCache ok...");
    }

    @Test
    public void testGetLocalStatistics() throws Exception {
        CacheStatisticsDto dto = zCachingService.getLocalStatistics("UserDaoImpl");
        System.out.println("getLocalStatistics-dto=" + dto);
        Assert.assertNotNull(dto);
    }
}