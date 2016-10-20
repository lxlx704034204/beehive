package com.gustz.beehive.task;

import com.gustz.beehive.config.cache.conf.BaseCacheConfig;
import com.gustz.beehive.service.ZCachingService;
import com.gustz.beehive.util.BeehiveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * z caching task
 *
 * @author zhangzhenfeng
 * @since 2016-10-14
 */
@Component
public class ZCachingTask {

    private static final Logger logger = LoggerFactory.getLogger(ZCachingTask.class);

    @Autowired
    private ZCachingService zCachingService;

    /**
     * do clear expire cache
     * <br/>
     * 1h
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    private void doClearExpireCache() {
        BaseCacheConfig cacheConfig = zCachingService.getBaseCacheConfig();
        if (!cacheConfig.isClearExpireCacheTask()) {
            logger.debug("doClearExpireCache: it is locked.");
            return;
        }
        logger.debug("doClearExpireCache: triggered at: {}", BeehiveUtil.fmtYmdHms());
        zCachingService.clearExpireCache();
    }

}
