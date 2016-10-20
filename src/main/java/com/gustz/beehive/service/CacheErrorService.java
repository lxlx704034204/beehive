package com.gustz.beehive.service;

/**
 * cache error service
 *
 * @author zhangzhenfeng
 * @since 2016-10-14
 */
public interface CacheErrorService {

    enum ErrType {
        clear, evict, put, get;
    }

    /**
     * handle cache error
     *
     * @param type
     * @param msg
     * @param cacheName
     * @param key
     */
    void handleError(ErrType type, String msg, String cacheName, Object key);
}
