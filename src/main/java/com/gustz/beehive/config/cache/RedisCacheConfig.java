package com.gustz.beehive.config.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * redis cache config
 *
 * @author zhangzhenfeng
 * @since 2016-09-11
 */
@Configuration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Value("${redis.data.cache_prefix}")
    private String cachePrefix;

    @Value("${redis.data.default_expire_time}")
    private long defaultExpireTime;

    @Value("${redis.data.need_clear_caches}")
    private String[] needClearCaches;

    @Value("${redis.data.expire_caches}")
    private String[] expireCaches;

    @Resource(name = "redisDataConnectionFactory")
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private CacheErrorService cacheErrorService;

    private StringRedisTemplate stringRedisTemplate;

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private final Map<String, Long> expiresMap = new ConcurrentHashMap<>();

    public interface CacheErrorService {
        enum Type {
            clear, evict, put, get;
        }

        void handleError(Type type, String msg, String cacheName, Object key);
    }

    @PostConstruct
    private void init() {
        if (cachePrefix == null || cachePrefix.isEmpty()) {
            logger.error("RedisCacheConfig-init: redis data cache prefix invalid.");
            throw new Error("redis data cache prefix invalid.");
        }
        stringRedisTemplate = new StringRedisTemplate(connectionFactory);
        // default expire time
        if (defaultExpireTime < 5) {
            logger.debug("RedisCacheConfig-init: default expire time < 5,reset to 0.");
            defaultExpireTime = 0;
        }
        // do clear cache data
        if (needClearCaches != null && needClearCaches.length > 0) {
            logger.debug("RedisCacheConfig-init: load need clear caches.");
            this.doClearCacheData(Arrays.asList(needClearCaches));
        }
        // add expire caches to map
        if (expireCaches != null && expireCaches.length > 0) {
            logger.debug("RedisCacheConfig-init: load expire caches.");
            for (String c : expireCaches) {
                if (c == null || c.isEmpty()) {
                    continue;
                }
                String[] tmp = c.split("-");
                String key = this.cachePrefix + tmp[0];
                expiresMap.put(key, Long.parseLong(tmp[1]));
            }
        }
    }

    /**
     * do clear cache keys
     * <br/>
     * 1h
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    private void doClearCacheKeys() {
        if (expiresMap == null || expiresMap.isEmpty()) {
            return;
        }
        // add lock for thread
        final long bt = System.currentTimeMillis();
        logger.debug("doClearCacheKeys: triggered at: {}", fmtYmdHms());
        long activeKeyCount = 0;
        try {
            final long offset = 1000;
            for (String prefix : expiresMap.keySet()) {
                if (StringUtils.isBlank(prefix)) {
                    continue;
                }
                final String key = prefix + "~keys";
                long start = 0;
                long end = start + offset;
                while (true) {
                    Set<String> itemKeys = stringRedisTemplate.opsForZSet().range(key, start, end);
                    if (itemKeys == null || itemKeys.isEmpty()) {
                        break;
                    }
                    // s1: do clear expire data key
                    for (String ik : itemKeys) {
                        if (StringUtils.isBlank(ik)) {
                            continue;
                        }
                        if (StringUtils.isBlank(stringRedisTemplate.opsForValue().get(ik))) {
                            stringRedisTemplate.opsForZSet().remove(key, ik);
                        }
                    }
                    // s2: reset count
                    start = end;
                    end = start + offset;
                    activeKeyCount += itemKeys.size();
                    Thread.sleep(50);
                }
            }
        } catch (Exception e) {
            logger.error("doClearCacheKeys: catch e.msg={}", e.getMessage());
        } finally {
            logger.debug("doClearCacheKeys: end,use time={}ms,activeKeyCount={}", (System.currentTimeMillis() - bt), activeKeyCount);
        }
    }

    /**
     * do clear cache data
     *
     * @param suffixNameList
     */
    public void doClearCacheData(List<String> suffixNameList) {
        if (suffixNameList == null || suffixNameList.isEmpty()) {
            logger.debug("doClearCacheData: need clear cache suffix name list is empty.");
            return;
        }
        final long bt = System.currentTimeMillis();
        logger.debug("doClearCacheData: begin clear cache data...");
        //
        final long offset = 1000;
        long keyCount = 0;
        for (String suffixName : suffixNameList) {
            if (suffixName == null || suffixName.isEmpty()) {
                continue;
            }
            String key = this.cachePrefix + suffixName + "~keys";
            long start = 0;
            long end = start + offset;
            while (true) {
                Set<String> itemKeys = stringRedisTemplate.opsForZSet().range(key, start, end);
                if (itemKeys == null || itemKeys.isEmpty()) {
                    // delete group key
                    stringRedisTemplate.delete(key);
                    break;
                }
                // delete item key
                stringRedisTemplate.delete(itemKeys);
                start = end;
                end = start + offset;
                keyCount += itemKeys.size();
            }
        }
        logger.debug("doClearCacheData: end clear cache data,key count={},use time={}ms.", keyCount, (System.currentTimeMillis() - bt));
    }

    public String getCachePrefix() {
        return cachePrefix;
    }

    public String[] getNeedClearCaches() {
        return needClearCaches;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(this.getDataRedisTemplate());
        cacheManager.setDefaultExpiration(defaultExpireTime);
        return cacheManager;
    }

    @Bean
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
                // get cache prefix name
                final String prefixName = getCachePrefixName(tgtCls, method);
                if (prefixName == null || prefixName.isEmpty()) {
                    logger.error("keyGenerator: prefixName is null,tgtCls={},method={}", tgtCls, method);
                    cacheErrorService.handleError(CacheErrorService.Type.put, "prefixName is null", null, null);
                    return null;
                }
                final String key = prefixName + "_" + getMd5Key(sbd.toString());
                if (!key.startsWith(cachePrefix)) {
                    logger.error("keyGenerator: key invalid,key={}", key);
                    cacheErrorService.handleError(CacheErrorService.Type.put, "key invalid", null, key);
                    return null;
                }
                // set expire time
                try {
                    if (expiresMap.size() > 0 && expiresMap.containsKey(prefixName)) {
                        Long expireTime = stringRedisTemplate.getExpire(key);
                        if (expireTime != null && expireTime < 1) {
                            long timeout = expiresMap.get(prefixName);
                            int ratio = 30;
                            if (timeout <= 60) {
                                ratio = (int) (timeout / 2);
                            }
                            timeout += Math.abs((int) (Math.random() * ratio));
                            //
                            stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("keyGenerator: catch set expire time e.msg={}", e.getMessage());
                }
                return key;
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
                cacheErrorService.handleError(CacheErrorService.Type.get, msg, cacheName, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                cacheErrorService.handleError(CacheErrorService.Type.put, msg, cacheName, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                cacheErrorService.handleError(CacheErrorService.Type.evict, msg, cacheName, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                String msg = exception.getMessage();
                String cacheName = cache.getName();
                cacheErrorService.handleError(CacheErrorService.Type.clear, msg, cacheName, null);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate getDataRedisTemplate() {
        RedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @SuppressWarnings("unchecked")
    private static String getCachePrefixName(Class tgCls, Method method) {
        String name = null;
        // target class object
        if (tgCls.isAnnotationPresent(Cacheable.class)) {
            String[] ss = ((Cacheable) tgCls.getAnnotation(Cacheable.class)).cacheNames();
            if (ss != null && ss.length > 0) {
                name = ss[0];
            }
        } else if (tgCls.isAnnotationPresent(CachePut.class)) {
            String[] ss = ((CachePut) tgCls.getAnnotation(CachePut.class)).cacheNames();
            if (ss != null && ss.length > 0) {
                name = ss[0];
            }
        } else if (tgCls.isAnnotationPresent(CacheEvict.class)) {
            String[] ss = ((CacheEvict) tgCls.getAnnotation(CacheEvict.class)).cacheNames();
            if (ss != null && ss.length > 0) {
                name = ss[0];
            }
        } else if (tgCls.isAnnotationPresent(CacheConfig.class)) {
            String[] ss = ((CacheConfig) tgCls.getAnnotation(CacheConfig.class)).cacheNames();
            if (ss != null && ss.length > 0) {
                name = ss[0];
            }
        }
        // method
        if (name == null || name.isEmpty()) {
            if (method.isAnnotationPresent(Cacheable.class)) {
                String[] ss = method.getAnnotation(Cacheable.class).cacheNames();
                if (ss != null && ss.length > 0) {
                    name = ss[0];
                }
            } else if (method.isAnnotationPresent(CachePut.class)) {
                String[] ss = method.getAnnotation(CachePut.class).cacheNames();
                if (ss != null && ss.length > 0) {
                    name = ss[0];
                }
            } else if (method.isAnnotationPresent(CacheEvict.class)) {
                String[] ss = method.getAnnotation(CacheEvict.class).cacheNames();
                if (ss != null && ss.length > 0) {
                    name = ss[0];
                }
            } else if (method.isAnnotationPresent(CacheConfig.class)) {
                String[] ss = method.getAnnotation(CacheConfig.class).cacheNames();
                if (ss != null && ss.length > 0) {
                    name = ss[0];
                }
            }
        }
        return name;
    }

    private static String getMd5Key(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(key.getBytes("UTF-8"));
            return encodeHexStr(digest.digest());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encodeHexStr(byte[] data) {
        int len = data.length;
        char[] out = new char[len << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }
        return new String(out);
    }

    private static String fmtYmdHms() {
        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
    }

}
