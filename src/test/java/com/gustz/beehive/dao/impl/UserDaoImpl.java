package com.gustz.beehive.dao.impl;

import com.gustz.beehive.config.auditlog.AuditLogArg;
import com.gustz.beehive.config.auditlog.DaoAuditLog;
import com.gustz.beehive.config.cache.conf.ZCacheConfig;
import com.gustz.beehive.config.cache.conf.ZCacheType;
import com.gustz.beehive.config.cache.conf.ZCacheable;
import com.gustz.beehive.dao.UserDao;
import com.gustz.beehive.dao.mapper.UserMapper;
import com.gustz.beehive.model.db.User;
import com.gustz.beehive.model.db.UserExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * user dao impl
 *
 * @author zhangzhenfeng
 * @since 2016-08-31
 */
@ZCacheConfig("UserDaoImpl")
@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Autowired
    private UserMapper userMapper;

    @ZCacheable(type = ZCacheType.LOCAL, expire = 70)
    @Override
    public List<User> getUsers(String name) {
        return doGetUsers(name);
    }

    private List<User> doGetUsers(String name) {
        logger.info("getUsers: arg name={}", name);
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        //
        return userMapper.selectByExample(example);
    }

    @Override
    public User getUser(String gid) {
        logger.info("getUser: arg gid={}", gid);
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid);
        //
        List<User> list = userMapper.selectByExample(example);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Integer getId(String gid) {
        logger.info("getId: arg gid={}", gid);
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andGidEqualTo(gid);
        //
        List<User> list = userMapper.selectByExample(example);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0).getId();
    }

    @DaoAuditLog(module = "m1", metric = "a1")
    @Override
    public int insertOrUpdateUser(@AuditLogArg User user) {
        logger.info("insertOrUpdateUser: arg user={}", user);
        List<User> users = getUsers(user.getName());
        if (users.size() > 0) {
            user.setId(users.get(0).getId());
            userMapper.updateByPrimaryKey(user);
        } else {
            userMapper.insertSelective(user);
        }
        return 0;
    }
}
