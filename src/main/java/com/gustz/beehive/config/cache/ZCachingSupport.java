package com.gustz.beehive.config.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustz.beehive.config.cache.conf.*;
import com.gustz.beehive.service.CacheErrorService;
import com.gustz.beehive.service.ZCachingService;
import com.gustz.beehive.util.SecureCipherHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * z caching config support
 *
 * @author zhangzhenfeng
 * @since 2016-09-11
 */
@EnableCaching
public class ZCachingSupport extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(ZCachingSupport.class);

    @Autowired
    private ZCachingService zCachingService;

    private EhCacheCacheManager ehCacheCacheManager;

    private GuavaCacheManager guavaCacheManager;

    private RedisCacheManager redisCacheManager;

    private StringRedisTemplate cacheRedisTemplate;

    private CacheErrorService cacheErrorService;

    private BaseCacheConfig baseCacheConfig;

    private EhcacheCacheConfig ehcacheCacheConfig;

    private String cachePrefix;

    private static final String CLS_NAME = ZCachingSupport.class.getName();

    public ZCachingSupport(BaseCacheConfig baseCacheConfig, RedisConnectionFactory redisConnectionFactory, CacheManager localCacheManager) {
        this.baseCacheConfig = baseCacheConfig;
        this.cacheRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        //
        this.setRemoteCacheManager(redisConnectionFactory);
        this.setLocalCacheManager(localCacheManager);
    }

    public ZCachingSupport(BaseCacheConfig baseCacheConfig, RedisConnectionFactory redisConnectionFactory, CacheManager localCacheManager, CacheErrorService cacheErrorService) {
        this(baseCacheConfig, redisConnectionFactory, localCacheManager);
        this.cacheErrorService = cacheErrorService;
    }

    private void init() {
        // check bean service
        Assert.notNull(baseCacheConfig);
        Assert.notNull(redisCacheManager);
        Assert.notNull(this.getLocalCacheManager());
        // check config
        cachePrefix = baseCacheConfig.getCachePrefix();
        if (cachePrefix == null || cachePrefix.isEmpty()) {
            logger.error("{}-init: cache prefix invalid.", CLS_NAME);
            throw new Error("cache prefix invalid.");
        }
        if (cacheErrorService == null) {
            logger.warn("{}-init: cacheErrorService is null.", CLS_NAME);
        }
        if (ehcacheCacheConfig == null) {
            logger.warn("{}-init: ehcacheCacheConfig is null.", CLS_NAME);
        }
        String[] needClearCaches = baseCacheConfig.getNeedClearCaches();
        if (needClearCaches != null && needClearCaches.length > 0) {
            logger.info("{}-init: do need clear cache,size={}", CLS_NAME, needClearCaches.length);
            zCachingService.clearCache(Arrays.asList(needClearCaches));
        }
        // update local cache config
        this.updateLocalCacheConfig();
    }

    private void setLocalCacheManager(CacheManager localCacheManager) {
        if (localCacheManager instanceof EhCacheCacheManager) {
            if (this.ehCacheCacheManager == null) {
                this.ehCacheCacheManager = (EhCacheCacheManager) localCacheManager;
            }
        } else if (localCacheManager instanceof GuavaCacheManager) {
            if (this.guavaCacheManager == null) {
                this.guavaCacheManager = (GuavaCacheManager) localCacheManager;
            }
        }
    }

    private void updateLocalCacheConfig() {
        // ehcache
        if (this.ehCacheCacheManager != null && this.ehcacheCacheConfig != null) {
            // load config
            net.sf.ehcache.CacheManager tmpCacheManager = this.ehCacheCacheManager.getCacheManager();
            // config items
            net.sf.ehcache.config.CacheConfiguration cacheConfig = new net.sf.ehcache.config.CacheConfiguration();
            cacheConfig.setEternal(this.ehcacheCacheConfig.isEternal());
            cacheConfig.setOverflowToOffHeap(this.ehcacheCacheConfig.isOverflowToOffHeap());
            cacheConfig.setMaxEntriesLocalHeap(this.ehcacheCacheConfig.getMaxElementsInMemory());
            cacheConfig.setMaxEntriesLocalDisk(this.ehcacheCacheConfig.getMaxElementsOnDisk());
            cacheConfig.setTimeToLiveSeconds(this.ehcacheCacheConfig.getTimeToLiveSeconds());
            cacheConfig.setTimeToIdleSeconds(this.ehcacheCacheConfig.getTimeToIdleSeconds());
            cacheConfig.setDiskExpiryThreadIntervalSeconds(this.ehcacheCacheConfig.getDiskExpiryThreadIntervalSeconds());
            //
            tmpCacheManager.getConfiguration().defaultCache(cacheConfig);
            this.ehCacheCacheManager.setCacheManager(tmpCacheManager);
        }
        // guava
    }

    private CacheManager getLocalCacheManager() {
        CacheManager cacheManager = this.ehCacheCacheManager;
        if (cacheManager == null) {
            cacheManager = this.guavaCacheManager;
        }
        return cacheManager;
    }

    public BaseCacheConfig getBaseCacheConfig() {
        return baseCacheConfig;
    }

    public StringRedisTemplate getCacheRedisTemplate() {
        return cacheRedisTemplate;
    }

    public EhCacheCacheManager getEhCacheCacheManager() {
        return ehCacheCacheManager;
    }

    public void setEhCacheCacheManager(EhCacheCacheManager ehCacheCacheManager) {
        this.ehCacheCacheManager = ehCacheCacheManager;
    }

    public GuavaCacheManager getGuavaCacheManager() {
        return guavaCacheManager;
    }

    public void setGuavaCacheManager(GuavaCacheManager guavaCacheManager) {
        this.guavaCacheManager = guavaCacheManager;
    }

    public void setRedisCacheManager(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
    }

    public void setBaseCacheConfig(BaseCacheConfig baseCacheConfig) {
        this.baseCacheConfig = baseCacheConfig;
    }

    public void setEhcacheCacheConfig(EhcacheCacheConfig ehcacheCacheConfig) {
        this.ehcacheCacheConfig = ehcacheCacheConfig;
    }

    public EhcacheCacheConfig getEhcacheCacheConfig() {
        return ehcacheCacheConfig;
    }

    public void setCacheErrorService(CacheErrorService cacheErrorService) {
        this.cacheErrorService = cacheErrorService;
    }

    public RedisCacheManager getRedisCacheManager() {
        return redisCacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                final Class tgtCls = target.getClass();
                final StringBuilder sbd = new StringBuilder();
                sbd.append(tgtCls.getName());
                sbd.append(method.getName());
                for (Object obj : params) {
                    sbd.append(obj.toString());
                }
                // s1: get cache name
                final String cacheName = getInnerZCaching(tgtCls, method).name;
                if (cacheName == null || cacheName.isEmpty()) {
                    logger.error("keyGenerator: cacheName is null,tgtCls={},method={}", tgtCls, method);
                    if (cacheErrorService != null) {
                        cacheErrorService.handleError(CacheErrorService.ErrType.put, "cacheName is null", null, null);
                    }
                    return null;
                }
                final String cacheKey = cacheName + ":" + SecureCipherHelper.getMd5Key(sbd.toString());
                // s2: set expire time
                setCacheExpire(tgtCls, method, cacheKey, cacheName);
                return cacheKey;
            }
        };
    }

    @Override
    public CacheResolver cacheResolver() {
        return new CacheResolver() {
            @Override
            public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
                // check annotation
                InnerZCaching innerZCaching = getInnerZCaching(context.getTarget().getClass(), context.getMethod());
                String cacheName = innerZCaching.getName();
                Assert.hasText(cacheName);
                //
                final ZCacheType cacheType = innerZCaching.type;
                final int expire = innerZCaching.expire;
                if (expire < BaseCacheConfig.MIN_DEFAULT_EXPIRE_TIME || expire > BaseCacheConfig.MAX_DEFAULT_EXPIRE_TIME) {
                    logger.error("cacheResolver: out of expire range,expire={}", expire);
                    throw new IllegalArgumentException("expire range in " + BaseCacheConfig.MIN_DEFAULT_EXPIRE_TIME + " to " + BaseCacheConfig.MAX_DEFAULT_EXPIRE_TIME);
                }
                // add cache to list
                zCachingService.addCurrCacheName(cacheType, cacheName);
                //　resolve caches
                return Arrays.asList(zCachingService.getCache(cacheType, cacheName));
            }
        };
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                if (cacheErrorService == null) {
                    logger.warn("errorHandler: cacheErrorService is null.");
                    return;
                }
                cacheErrorService.handleError(CacheErrorService.ErrType.get, msg, cacheName, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                if (cacheErrorService == null) {
                    logger.warn("errorHandler: cacheErrorService is null.");
                    return;
                }
                cacheErrorService.handleError(CacheErrorService.ErrType.put, msg, cacheName, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                if (cacheErrorService == null) {
                    logger.warn("errorHandler: cacheErrorService is null.");
                    return;
                }
                cacheErrorService.handleError(CacheErrorService.ErrType.evict, msg, cacheName, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                if (cacheErrorService == null) {
                    logger.warn("errorHandler: cacheErrorService is null.");
                    return;
                }
                cacheErrorService.handleError(CacheErrorService.ErrType.clear, msg, cacheName, null);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void setRemoteCacheManager(RedisConnectionFactory redisConnectionFactory) {
        if (this.redisCacheManager == null) {
            final RedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
            Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            serializer.setObjectMapper(mapper);
            redisTemplate.setValueSerializer(serializer);
            redisTemplate.afterPropertiesSet();
            //
            this.redisCacheManager = new RedisCacheManager(redisTemplate);
        }
        this.redisCacheManager.setDefaultExpiration(BaseCacheConfig.MAX_DEFAULT_EXPIRE_TIME);
        this.redisCacheManager.setUsePrefix(true);
        this.redisCacheManager.setCachePrefix(new RedisCachePrefix() {
            @Override
            public byte[] prefix(String cacheName) {
                return (BaseCacheConfig.INNER_CACHE_PREFIX + cachePrefix + ":").getBytes();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static InnerZCaching getInnerZCaching(Class tgCls, Method method) {
        if (tgCls == null || method == null) {
            return null;
        }
        String name = null;
        // target class object
        if (tgCls.isAnnotationPresent(ZCacheConfig.class)) {
            name = ((ZCacheConfig) tgCls.getAnnotation(ZCacheConfig.class)).value();
        }
        InnerZCaching innerZCaching = null;
        // method
        if (method.isAnnotationPresent(ZCacheable.class)) {
            ZCacheable zCacheable = method.getAnnotation(ZCacheable.class);
            innerZCaching = new InnerZCaching(zCacheable.value(), zCacheable.expire(), zCacheable.type());
        } else if (method.isAnnotationPresent(ZCachePut.class)) {
            ZCachePut zCachePut = method.getAnnotation(ZCachePut.class);
            innerZCaching = new InnerZCaching(zCachePut.value(), zCachePut.expire(), zCachePut.type());
        } else if (method.isAnnotationPresent(ZCacheEvict.class)) {
            ZCacheEvict zCacheEvict = method.getAnnotation(ZCacheEvict.class);
            innerZCaching = new InnerZCaching(zCacheEvict.value(), zCacheEvict.expire(), zCacheEvict.type());
        }
        if (innerZCaching != null && StringUtils.isNotBlank(name)) {
            innerZCaching.setName(name);
        }
        Assert.notNull(innerZCaching);
        return innerZCaching;
    }

    private void setCacheExpire(Class tgCls, Method method, final String cacheKey, final String cacheName) {
        InnerZCaching innerZCaching = getInnerZCaching(tgCls, method);
        zCachingService.setCacheExpire(innerZCaching.type, innerZCaching.expire, cacheKey, cacheName);
    }

    private static class InnerZCaching {

        private String name;

        private ZCacheType type;

        private int expire;

        public InnerZCaching() {
        }

        public InnerZCaching(String name) {
            this.name = name;
        }

        public InnerZCaching(String name, int expire, ZCacheType type) {
            this.name = name;
            this.expire = expire;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getExpire() {
            return expire;
        }

        public void setExpire(int expire) {
            this.expire = expire;
        }

        public ZCacheType getType() {
            return type;
        }

        public void setType(ZCacheType type) {
            this.type = type;
        }
    }

}
