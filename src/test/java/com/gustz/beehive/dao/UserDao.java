package com.gustz.beehive.dao;

import com.gustz.beehive.model.db.User;

import java.util.List;

/**
 * user dao
 *
 * @author zhangzhenfeng
 * @since 2016-08-31
 */
public interface UserDao {

    List<User> getUsers(String name);

    User getUser(String gid);

    Integer getId(String gid);

    int insertOrUpdateUser(User user);
}
