package com.gustz.beehive.dao.impl;

import com.gustz.beehive.base.TestBase;
import com.gustz.beehive.dao.UserDao;
import com.gustz.beehive.model.CacheStatisticsDto;
import com.gustz.beehive.model.db.User;
import com.gustz.beehive.service.ZCachingService;
import com.gustz.beehive.util.BeehiveUtil;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import java.util.List;

/**
 * user dao impl cache test
 *
 * @author zhangzhenfeng
 * @since 2016-10-16
 */
public class UserDaoImplTest extends TestBase {

    @Autowired
    private UserDao userDao;

    @Autowired
    private EhCacheCacheManager ehCacheCacheManager;

    @Autowired
    private ZCachingService zCachingService;

    private int currTimestamp = BeehiveUtil.getCurrTimestamp();

    @Test
    public void testGetUsers() {
        for (int i = 0; i < 5; i++) {
            try {
                List<User> list = userDao.getUsers("name90");
                System.out.println("getUsers-list=" + list);
                Assert.assertNotNull(list);
                if (list != null && list.size() > 0) {
                    System.out.println("getName=" + list.get(0).getName());
                }
                // get statistics
                CacheStatisticsDto dto = zCachingService.getLocalStatistics("UserDaoImpl");
                System.out.println("getLocalStatistics-dto=" + dto);
                //
                if (i == 2) {
                    Thread.sleep(60000);
                } else if (i == 3) {
                    Thread.sleep(20000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ehcache
        net.sf.ehcache.CacheManager ehCacheManager = ehCacheCacheManager.getCacheManager();
        CacheConfiguration cacheConfiguration = ehCacheManager.getConfiguration().getDefaultCacheConfiguration();
        System.out.println("getUsers-config-isOverflowToOffHeap=" + cacheConfiguration.isOverflowToOffHeap());
        System.out.println("getUsers-config-getTimeToLiveSeconds=" + cacheConfiguration.getTimeToLiveSeconds());
        System.out.println("getUsers-config-isEternal=" + cacheConfiguration.isEternal());
        System.out.println("getUsers-config-getMaxEntriesLocalHeap=" + cacheConfiguration.getMaxEntriesLocalHeap());
        System.out.println("getUsers-getMonitoring=" + ehCacheManager.getConfiguration().getMonitoring());
        System.out.println("*********");
        String name = ehCacheManager.getName();
        System.out.println("getUsers-getName=" + name);
    }

    @Test
    public void testGetUser() throws Exception {
        User user = userDao.getUser("2eb2a8a1-92e7-4da3-892b-6c60efe8dc5b");
        System.out.println("getUser-user=" + user);
        Assert.assertNotNull(user);
        if (user != null) {
            System.out.println("getName=" + user.getName());
        }
    }

    @Test
    public void testGetId() throws Exception {
        Integer id = userDao.getId("2eb2a8a1-92e7-4da3-892b-6c60efe8dc5b");
        System.out.println("getId-id=" + id);
        Assert.assertNotNull(id);
    }

    @Test
    public void testInsertOrUpdateUser() throws Exception {
        User user = new User();
        user.setGid(BeehiveUtil.generateUUID());
        user.setCreateTime(currTimestamp);
        user.setUpdateTime(currTimestamp);
        user.setAge(2);
        user.setName("name2");
        int count = userDao.insertOrUpdateUser(user);
        System.out.println("insertOrUpdateUser-count=" + count);
        Assert.assertEquals(0, count);
    }
}