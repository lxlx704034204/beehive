package com.gustz.beehive.config.cache.conf;

/**
 * cache base conf
 *
 * @author zhangzhenfeng
 * @since 2016-10-13
 */
public class BaseCacheConfig {

    public static final int OFFSET = 1000;

    public static final int DEFAULT_EXPIRE_TIME = 60;

    public static final int MAX_DEFAULT_EXPIRE_TIME = 3600;

    public static final int MIN_DEFAULT_EXPIRE_TIME = 1;

    public static final String INNER_CACHE_PREFIX = "cache:";

    private String cachePrefix;

    private String[] needClearCaches;

    private boolean clearExpireCacheTask = true;

    public boolean isClearExpireCacheTask() {
        return clearExpireCacheTask;
    }

    public void setClearExpireCacheTask(boolean clearExpireCacheTask) {
        this.clearExpireCacheTask = clearExpireCacheTask;
    }

    public String getCachePrefix() {
        return cachePrefix;
    }

    public void setCachePrefix(String cachePrefix) {
        this.cachePrefix = cachePrefix;
    }

    public String[] getNeedClearCaches() {
        return needClearCaches;
    }

    public void setNeedClearCaches(String[] needClearCaches) {
        this.needClearCaches = needClearCaches;
    }
}
