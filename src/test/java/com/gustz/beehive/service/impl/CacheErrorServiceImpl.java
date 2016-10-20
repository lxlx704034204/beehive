package com.gustz.beehive.service.impl;

import com.gustz.beehive.service.CacheErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * cache error service impl for test
 *
 * @author zhangzhenfeng
 * @since 2016-10-14
 */
@Service
public class CacheErrorServiceImpl implements CacheErrorService {

    private static final Logger logger = LoggerFactory.getLogger(CacheErrorServiceImpl.class);

    /**
     * handle cache error
     *
     * @param type
     * @param msg
     * @param cacheName
     * @param key
     */
    @Override
    public void handleError(ErrType type, String msg, String cacheName, Object key) {
        logger.debug("handleError: arg type={},msg={},cacheName={},key={}", type, msg, cacheName, key);
        //
    }
}
